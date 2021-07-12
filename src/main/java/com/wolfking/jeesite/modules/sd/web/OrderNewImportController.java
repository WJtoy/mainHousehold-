package com.wolfking.jeesite.modules.sd.web;


import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.kkl.kklplus.entity.md.CustomerProductModel;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.config.Global;
import com.wolfking.jeesite.common.config.redis.GsonRedisSerializer;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.config.redis.RedisTuple;
import com.wolfking.jeesite.common.exception.OrderException;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.service.SequenceIdService;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.Encodes;
import com.wolfking.jeesite.common.utils.QuarterUtils;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.wolfking.jeesite.common.utils.excel.ExportExcel;
import com.wolfking.jeesite.common.utils.excel.ImportExcel;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.entity.*;
import com.wolfking.jeesite.modules.md.entity.viewModel.AreaUrgentModel;
import com.wolfking.jeesite.modules.md.entity.viewModel.UrgentChargeModel;
import com.wolfking.jeesite.modules.md.service.*;
import com.wolfking.jeesite.modules.mq.dto.MQOrderImportMessage;
import com.wolfking.jeesite.modules.mq.entity.mapper.OrderImportMessageMapper;
import com.wolfking.jeesite.modules.mq.sender.CreateCustomerCurrencySender;
import com.wolfking.jeesite.modules.mq.sender.CreateOrderPushMessageSender;
import com.wolfking.jeesite.modules.mq.sender.OrderImportMessageSender;
import com.wolfking.jeesite.modules.mq.service.OrderCreateMessageService;
import com.wolfking.jeesite.modules.sd.entity.*;
import com.wolfking.jeesite.modules.sd.entity.viewModel.CreateOrderModel;
import com.wolfking.jeesite.modules.sd.entity.viewModel.CustomerProductVM;
import com.wolfking.jeesite.modules.sd.entity.viewModel.OrderItemModel;
import com.wolfking.jeesite.modules.sd.service.OrderEditFormService;
import com.wolfking.jeesite.modules.sd.service.OrderImportService;
import com.wolfking.jeesite.modules.sd.service.OrderMQService;
import com.wolfking.jeesite.modules.sd.service.OrderService;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.service.AreaService;
import com.wolfking.jeesite.modules.sys.utils.AreaUtils;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import com.wolfking.jeesite.modules.sys.utils.SeqUtils;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.b2bcenter.md.utils.B2BMDUtils;
import com.wolfking.jeesite.ms.tmall.md.entity.B2bCustomerMap;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisZSetCommands;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//import org.springframework.data.redis.support.atomic.RedisAtomicLong;

/**
 * 订单导入(客户功能)
 * @version 2.0
 * @date 2018/10/20
 * @author Ryan
 * 1.导入数据使用当前帐号的userId作为key
 * 2.保存时，将数据逐一提交到消息队列处理
 *
 * @date 2018/12/19
 * @author Ryan
 * 1.最后一列新增："第三方单号"
 */
@Controller
@RequestMapping(value = "${adminPath}/sd/order/import/new/")
@Slf4j
public class OrderNewImportController extends BaseController
{
	//锁生存时间，30分钟
	public final static Long IMP_ORDER_TRANSFER_LOCK_TTL = 30l*60;
	//导入单生存时间，60分钟
	public final static long IMP_ORDER_CACHE_TTL = 2*60l*60;

	private final static String JSP_DIRECOTRY = "modules/sd/import/";

	@Autowired
	private SequenceIdService sequenceIdService;

	@Autowired
	private OrderService orderService;

	@Autowired
	private ProductService productService;

	@Autowired
	private CustomerService customerService;

	@Autowired
	private AreaService areaService;
	@Autowired
	private ServiceTypeService serviceTypeService;

	@Autowired
	private RedisUtils redisUtils;

	@Resource(name = "gsonRedisSerializer")
	public GsonRedisSerializer gsonRedisSerializer;

	@Autowired
	private CreateCustomerCurrencySender createCustomerCurrencySender;

	@Autowired
	private CreateOrderPushMessageSender createOrderMessageSender;

	@Autowired
	private OrderCreateMessageService orderCreateMessageService;

	@Autowired
	private UrgentLevelService urgentLevelService;

	@Autowired
	private UrgentCustomerService urgentCustomerService;

	@Autowired
	private OrderImportService orderImportService;

	@Autowired
	private OrderImportMessageSender messageSender;

	@Autowired
	private OrderMQService orderMQService;

    @Autowired
    private OrderEditFormService orderEditFormService;

	//region [订单导入]

	/**
	 * 订单导入主页面（List）[客户]
	 * 从缓存中读取
	 */
	@RequiresPermissions("sd:order:import")
	@RequestMapping(value = { "list", "" })
	public String importForm(@RequestParam Map<String, Object> paramMap, HttpServletRequest request, HttpServletResponse response, Model model)
	{
		//String ip = StringUtils.getIp(request);
		//System.out.println("ip:" + ip);

		Page<TempOrder> page = new Page();
		User user = UserUtils.getUser();

		boolean errorFlag = false;
		//检查客户帐号是否可下单
		if(user.isCustomer()){
			CustomerAccountProfile profile =  user.getCustomerAccountProfile();
			if(profile == null || profile.getCustomer() == null || profile.getCustomer().getId() == null){
				addMessage(model, "错误：读取帐号信息失败，请重试。");
				errorFlag = true;
			}else {
				Customer customer = customerService.getFromCache(profile.getCustomer().getId());
				if(customer == null){
					addMessage(model, "错误：读取帐号信息失败，请重试。");
					errorFlag = true;
				}else if(customer.getEffectFlag() != 1) {
					addMessage(model, "错误：您的帐号已冻结，暂时无法下单，请联系管理员。");
					errorFlag = true;
				}
			}
			if(errorFlag){
				model.addAttribute("canAction",false);
				return JSP_DIRECOTRY+"orderImportForm";
			}
		}

		String key = String.format(RedisConstant.SD_TMP_ORDER,user.getId());
		List<TempOrder> orders = redisUtils.zRange(RedisConstant.RedisDBType.REDIS_SD_DB,key,0,-1,TempOrder.class);
		List<TempOrder> orderList;
		if (orders == null)
		{
            orderList = Lists.newArrayList();
		}
		else {
            Supplier<Stream<TempOrder>> streamSupplier = () -> orders.stream();
		    Set<String> errorMsgList = Sets.newHashSetWithExpectedSize(orders.size());
		    streamSupplier.get().map(t->t.getErrorMsg()).distinct()
                    .forEach( t ->{
                        if(StringUtils.isNotBlank(t)){
                            if(t.startsWith("30天重复下单")){
                                errorMsgList.add("30天重复下单");
                            }else{
                                errorMsgList.add(t.trim());
                            }
                        }
                    });
            model.addAttribute("errorMsgList",errorMsgList);
            //按手机号码排序
            orderList = streamSupplier.get()
                    .sorted(
                            Comparator.comparing(TempOrder::getSort).reversed()
                                    .thenComparing(TempOrder::getPhone)
                                    .thenComparingInt(t -> -t.getSort())
                    )
                    .collect(Collectors.toList());
            //.sorted(Comparator.comparing(TempOrder::getPhone).thenComparingInt(t-> -t.getSort())).collect(Collectors.toList());
            if (paramMap.containsKey("errorMsg")) {
                String errorMsg = paramMap.get("errorMsg").toString();
                if (StringUtils.isNotBlank(errorMsg)) {
                    orderList = orderList.stream().filter(t -> t.getErrorMsg().startsWith(errorMsg)).collect(Collectors.toList());
                }
            }
		    //orders = orders.stream().sorted(Comparator.comparing(TempOrder::getLineNumber)).collect(Collectors.toList());
        }
		page.setList(orderList);
		model.addAttribute("canAction",true);
		model.addAttribute("page", page);
		model.addAttribute("customerId","");
		model.addAllAttributes(paramMap);
		return JSP_DIRECOTRY+"orderImportForm";
	}

	/**
	 * 从excel中读取订单数据
	 * 2019-12-22
	 * 增加筛选，客户设定产品中未设定的产品价格不包含在内
	 * @param file			excel附件
	 * @param customerId	客户id
	 */
	@RequiresPermissions("sd:order:import")
	@RequestMapping(value = "read", method = RequestMethod.POST)
	public String readExcel(MultipartFile file,@RequestParam(required = false) String customerId, String shopId,String shopName,RedirectAttributes redirectAttributes,HttpServletRequest request)
	{
		User user = UserUtils.getUser();

		if(file == null){
			redirectAttributes.addAttribute("customerId",customerId);
			addMessage(redirectAttributes,"请选择文件．");
			return "redirect:" + Global.getAdminPath() + "/sd/order/import/new/list?repage";
		}

		Customer customer = null;
		Long cid = 0l;
		if(!user.isCustomer())
		{
			if(StringUtils.isBlank(customerId))
			{
				addMessage(redirectAttributes,"请先选择客户．");
				return "redirect:" + Global.getAdminPath() + "/sd/order/import/new/list?repage";
			}
			cid = Long.valueOf(customerId);
		}
		else{
			cid = user.getCustomerAccountProfile().getCustomer().getId();
		}
		customer =customerService.getFromCache(cid);
		if(customer.getEffectFlag() != 1){
            redirectAttributes.addAttribute("customerId",customerId);
            addMessage(redirectAttributes,"客户帐号已冻结，暂时无法下单，请联系管理员。");
            return "redirect:" + Global.getAdminPath() + "/sd/order/import/new/list?repage";
        }

		String key =String.format(RedisConstant.SD_TMP_ORDER,user.getId());
		//移除上次读取数据
		redisUtils.remove(RedisConstant.RedisDBType.REDIS_SD_DB,key);

		try {
			//product and services of customer
			List<CustomerPrice> prices = customerService.getPricesFromCache(cid);
			if(prices==null || prices.size()==0){
				addMessage(redirectAttributes,"贵公司未维护产品及价格。");
				return "redirect:" + Global.getAdminPath() + "/sd/order/import/new/list?repage";
			}
			// 客户现在设定的产品列表
			List<Product> productList = productService.getCustomerProductList(cid);
			if(CollectionUtils.isEmpty(productList)){
				addMessage(redirectAttributes,"读取贵公司现有产品列表失败。");
				return "redirect:" + Global.getAdminPath() + "/sd/order/import/new/list?repage";
			}
			Set<Long> productIdSet = productList.stream().map(t->t.getId()).collect(Collectors.toSet());
			prices = prices.stream().filter(t-> productIdSet.contains(t.getProduct().getId())).collect(Collectors.toList());
			//Expresses
			List<Dict> expresses = MSDictUtils.getDictList("express_type");//切换为微服务
			if(expresses==null || expresses.size()==0){
				addMessage(redirectAttributes,"系统基础资料未维护快递公司清单。");
				return "redirect:" + Global.getAdminPath() + "/sd/order/import/new/list?repage";
			}

			List<CustomerProductVM> cproList = Lists.newArrayList();
			Map<Product,List<CustomerPrice>> maps = prices.stream().collect(Collectors.groupingBy(CustomerPrice::getProduct));
			maps.forEach((k,v)->{
				CustomerProductVM m = new CustomerProductVM();
				m.setId(k.getId());
				m.setName(k.getName());
				m.setBrand(k.getBrand());
				m.setModel(k.getModel());
				//types
				v.forEach(item ->{
					m.getServices().add(item.getServiceType());
				});
				cproList.add(m);
			});
			B2bCustomerMap shop;
			if(StringUtils.isNotBlank(shopId)){
				shop = new B2bCustomerMap(shopId,shopName);
			}else{
				shop = new B2bCustomerMap("","");
			}
			//read excel
			ImportExcel ei = new ImportExcel(file, 1, 0);
			List<TempOrder> orders = Lists.newArrayList();
			TempOrder temp = null;
			StringBuilder value = new StringBuilder(300);
			CustomerProductVM	cproduct;
			Optional<?> optional;
			String phone = new String("");
			StringBuffer desc;
			int qty;
			HashSet<String> set = new HashSet<String>();
			int maxTimes = ei.getLastDataRowNum()+2000;
			//随机，防止同用户产生重复id
			int workerId = ThreadLocalRandom.current().nextInt(32);
			int datacenterId = ThreadLocalRandom.current().nextInt(32);
			//SequenceIdUtils sequence = new SequenceIdUtils(workerId,datacenterId);
			for (int i = 0; i < maxTimes; i++) {
				//只取后14位
				//因数据保存redis时，id(Long)作为score,要转换为double类型，精度丢失
				//set.add(String.valueOf(sequence.nextId()));
				set.add(String.valueOf(sequenceIdService.nextId()));
			}
			//set 转 List
			LinkedList<String> idList = Lists.newLinkedList(set);
			//本次列表中重复
			Row row;
			//检查结果
			String checkResultStr = new String();
			//行检查成功标记
			Boolean rowCheckSucess = true;
			long timestamp = DateUtils.getTimestamp();
			StringBuilder rowCheckMsg = new StringBuilder(200);
			for (int i = ei.getDataRowNum(),size = ei.getLastDataRowNum(); i < size; i++) {
				//read row
				row = ei.getRow(i);
				rowCheckSucess = true;
				//用户名
				checkResultStr = StringUtils.strip(ei.getCellValue(row, 0).toString());
				//用户名为空的订单，忽略
				if(StringUtils.isBlank(checkResultStr)){
					continue;
				}

				rowCheckMsg.setLength(0);
				//read row
				temp = new TempOrder();
				Long id = Long.valueOf(idList.removeFirst());
				//行号
				temp.setLineNumber(i);
				temp.setId(id);
				//导入的时间，发生重复时便于确认是否是同一时间导入
				temp.setCreateTimeMillis(timestamp);
				temp.setCustomer(customer);
				temp.setUserName(checkResultStr);
				temp.setPhone(StringUtils.strip(StringUtils.cleanHtmlTagAndSpecChars(ei.getCellValue(row,1).toString())));
				temp.setTel(StringUtils.strip(StringUtils.cleanHtmlTagAndSpecChars(ei.getCellValue(row,2).toString())));
				if(temp.getTel().length()>16){
					rowCheckMsg.append(rowCheckSucess ? "" : "<br>").append("电话长度超过16");
					rowCheckSucess = false;
				}
				temp.setAddress(StringUtils.strip(StringUtils.cleanHtmlTagAndSpecChars(ei.getCellValue(row, 3).toString())));
				temp.setOrgProduct(StringUtils.strip(StringUtils.cleanHtmlTagAndSpecChars(ei.getCellValue(row, 4).toString())));
				temp.setProduct(new Product(0l,temp.getOrgProduct()));
				temp.setBrand(StringUtils.strip(StringUtils.cleanHtmlTagAndSpecChars(ei.getCellValue(row, 5).toString())));
				temp.setOrgProductSpec(StringUtils.strip(ei.getCellValue(row, 6).toString()));
				temp.setProductSpec(temp.getOrgProductSpec());
				if(temp.getOrgProductSpec().length()>100) {
					rowCheckMsg.append(rowCheckSucess ? "" : "<br>").append("型号/规格长度过长，超过100个汉字");
					rowCheckSucess = false;
				}
				temp.setOrgQty(ei.getCellValue(row, 7).toString());
				try{
					qty = Integer.parseInt(temp.getOrgQty());
				}catch (Exception e){
					rowCheckMsg.append(rowCheckSucess?"":"<br>").append("数量输入有误");
					rowCheckSucess = false;
					qty = 0;
					temp.setSort(95);
				}
				temp.setQty(qty);
				temp.setOrgServiceType(StringUtils.strip(ei.getCellValue(row, 8).toString()));
				temp.setServiceType(new ServiceType(0l,"",temp.getOrgServiceType()));

				//服务描述
				temp.setOrgDesription(ei.getCellValue(row, 9).toString());
				temp.setDefine1(StringUtils.strip(ei.getCellValue(row, 12).toString()));
				temp.setDefine2(StringUtils.strip(ei.getCellValue(row, 13).toString()));
				temp.setDefine3(StringUtils.strip(ei.getCellValue(row, 14).toString()));
				desc = new StringBuffer(255);
				desc.append(StringUtils.strip(StringUtils.cleanHtmlTagAndSpecChars(temp.getOrgDesription()))).append(" ");
				desc.append(temp.getDefine1()).append(" ");
				desc.append(temp.getDefine2()).append(" ");
				desc.append(temp.getDefine3()).append(" ");
				temp.setDescription(desc.toString().trim());
				if(temp.getDescription().length()>255){
					rowCheckMsg.append(rowCheckSucess?"":"<br>").append("服务描述长度过长，超过255个汉字");
					rowCheckSucess = false;
					temp.setSort(97);
				}
				//快递单号
				temp.setExpressCompany(new Dict("",StringUtils.strip(StringUtils.cleanHtmlTagAndSpecChars(ei.getCellValue(row, 10).toString()))));
				temp.setExpressNo(StringUtils.strip(StringUtils.cleanHtmlTagAndSpecChars(ei.getCellValue(row, 11).toString())));
				temp.setRemarks("");
				//第三方单号
				temp.setThdNo(StringUtils.strip(ei.getCellValue(row, 15).toString()));
				if(temp.getThdNo().length()>50) {
					rowCheckMsg.append(rowCheckSucess ? "" : "<br>").append("第三方单号长度超过50个字符或汉字");
					rowCheckSucess = false;
					temp.setSort(96);
				}
				temp.setSuccessFlag(0);
				if(!rowCheckSucess){
					temp.setErrorMsg(rowCheckMsg.toString());
					temp.setCanSave(0);
				}
				temp.setB2bShop(shop);
				//add to list
				orders.add(temp);//*

				//check order
				//rowCheckMsg.setLength(0);
				if(rowCheckSucess) {
					checkResultStr = StringUtils.isPhoneWithRelaxed(temp.getPhone());
					if (!checkResultStr.isEmpty()) {
						temp.setErrorMsg(checkResultStr);
						temp.setCanSave(0);
						temp.setSort(94);
						continue;
					}
					//service type
					if (StringUtils.isBlank(temp.getServiceType().getName())) {
						temp.setErrorMsg("未维护：服务类型");
						temp.setCanSave(0);
						temp.setSort(93);
						continue;
					}
					//product
					if (StringUtils.isBlank(temp.getProduct().getName())) {
						temp.setErrorMsg("未维护：产品");
						temp.setCanSave(0);
						temp.setSort(92);
						continue;
					}
					//address
					if (StringUtils.isBlank(temp.getAddress())) {
						temp.setErrorMsg("未维护：地址");
						temp.setCanSave(0);
						temp.setSort(91);
						continue;
					}
					//qty
					if (temp.getQty() <= 0) {
						temp.setErrorMsg("数量必须大于0");
						temp.setCanSave(0);
						temp.setSort(95);
						continue;
					}

					value.setLength(0);
					value.append(temp.getProduct().getName());
					optional = cproList.stream().filter(t -> t.getName().equalsIgnoreCase(value.toString())).findFirst();
					if (!optional.isPresent()) {
						temp.setErrorMsg(String.format("产品：%s 不在服务范围之内,或该产品未维护服务价格", temp.getProduct().getName()));
						temp.setCanSave(0);
						temp.setSort(90);
						continue;
					}
					cproduct = (CustomerProductVM) optional.get();
					temp.getProduct().setId(cproduct.getId());
					//service type
					value.setLength(0);
					value.append(temp.getServiceType().getName());
					optional = cproduct.getServices().stream().filter(t -> t.getName().equalsIgnoreCase(value.toString())).findFirst();
					if (!optional.isPresent()) {
						temp.setErrorMsg(String.format("产品:%s 未设定服务：%s 的价格", temp.getProduct().getName(), value.toString()));
						temp.setCanSave(0);
						temp.setSort(89);
						continue;
					}
					temp.getServiceType().setId(((ServiceType) optional.get()).getId());
					//express
					if (StringUtils.isNoneBlank(temp.getExpressCompany().getLabel())) {
						value.setLength(0);
						value.append(temp.getExpressCompany().getLabel());
						optional = expresses.stream().filter(t -> t.getLabel().equalsIgnoreCase(value.toString())).findFirst();
						if (!optional.isPresent()) {
							temp.setErrorMsg(String.format("快递公司:%s 系统中不存在", value.toString()));
							temp.setCanSave(0);
							temp.setSort(88);
							continue;
						}
						temp.setExpressCompany((Dict) optional.get());
					}
					temp.setSuccessFlag(1);//成功
				}
			}

			if (orders == null || orders.isEmpty()) {
				addMessage(redirectAttributes, "读取数据完成，但无符合的订单，请确认！");
				return "redirect:" + Global.getAdminPath() + "/sd/order/import/new/list?repage";
			}

			//region 重单检查
			//1.本次列表重复下单
			List<TempOrder> mval;
			TempOrder grpOrder;
			Map<String,List<TempOrder>> groupOrderMap = orders.stream().collect(Collectors.groupingBy(TempOrder::getPhone));
			for(String mkey : groupOrderMap.keySet()) {
				mval = groupOrderMap.get(mkey);
				if(mval.size()>1){
					for(int i=1,size=mval.size();i<size;i++){
						grpOrder = mval.get(i);
						grpOrder.setErrorMsg(grpOrder.getErrorMsg()==""?"Excel中重复订单":grpOrder.getErrorMsg()+"<br>Excel中重复订单");
						//grpOrder.setCanSave(1);
						grpOrder.setSort(99);
						grpOrder.setSuccessFlag(0);
					}
				}
			}
			//2.检查缓存，是否30天内重复下单
            String repeateOrderNo = new String();
			List<TempOrder> list = orders.stream().filter(t->t.getCanSave() == 1 && !t.getErrorMsg().contains("Excel中重复订单")).collect(Collectors.toList());
			for(int i=0,size=list.size();i<size;i++){
				grpOrder = list.get(i);
                repeateOrderNo = orderService.getRepeateOrderNo(cid,grpOrder.getPhone());
                if(!StringUtils.isBlank(repeateOrderNo)){
                    grpOrder.setRepeateOrderNo(repeateOrderNo);
					grpOrder.setSort(98);
					grpOrder.setErrorMsg(grpOrder.getErrorMsg()==""?"30天重复下单,相关单号:" + repeateOrderNo:grpOrder.getErrorMsg()+";30天重复下单,相关单号:" + repeateOrderNo);
                    //grpOrder.setErrorMsg("30天重复下单,相关单号:" + repeateOrderNo);
                }
 			}
			//endregion 重单检查

			//cache
			try {
				Set<RedisZSetCommands.Tuple> sets = Sets.newHashSet();
				orders.forEach(t->{
					sets.add(new RedisTuple(gsonRedisSerializer.serialize(t),Double.valueOf(StringUtils.right(t.getId().toString(),14))));
				});
				redisUtils.zAdd(RedisConstant.RedisDBType.REDIS_SD_DB, key, sets,IMP_ORDER_CACHE_TTL);
			}catch (Exception e){
				addMessage(redirectAttributes,"存储导入订单列表失败");
				return "redirect:" + Global.getAdminPath() + "/sd/order/import/new/list?repage";
			}

		} catch (Exception e)
		{
			addMessage(redirectAttributes, "读取订单失败！失败信息："+ e.getMessage());
		}
		return "redirect:" + Global.getAdminPath() + "/sd/order/import/new/list?repage";
	}

	/**
	 * 批量转单(ajax)
	 * @param ids 选择的id列表
	 */
	@RequiresPermissions("sd:order:import")
	@ResponseBody
	@RequestMapping(value = "transferOrders",method = RequestMethod.POST)
	public AjaxJsonEntity transferOrders(@RequestParam(value = "ids[]") Long[] ids, HttpServletResponse response)
	{
		response.setContentType("application/json; charset=UTF-8");
		AjaxJsonEntity result = new AjaxJsonEntity();
        User user = UserUtils.getUser();
        if(user==null || user.getId()==null){
            result.setSuccess(false);
            result.setMessage("登录超时，请重新登录。");
            return result;
        }

		OrderImportMessageMapper mapper = Mappers.getMapper(OrderImportMessageMapper.class);
		try
		{
			if(ids == null || ids.length==0) {
                result.setSuccess(false);
                result.setMessage("请选择要保存的订单");
				return result;
			}
			String key =String.format(RedisConstant.SD_TMP_ORDER,user.getId());
			List<TempOrder> orders =redisUtils.zRange(RedisConstant.RedisDBType.REDIS_SD_DB,key,0,-1,TempOrder.class);
			if(orders == null || orders.size()==0){
                result.setSuccess(false);
                result.setMessage("导入的数据不见了");
				return result;
			}
			//延时过期时间
			try {
				redisUtils.expire(RedisConstant.RedisDBType.REDIS_SD_DB,key,IMP_ORDER_CACHE_TTL);
			}catch (Exception e){}

			StringBuffer msg = new StringBuffer();
			//list -> Map<id,TempOrder>
			Map<Long,TempOrder> maps = orders.stream().collect(Collectors.toMap(TempOrder::getId, item -> item));
			Long id;
			TempOrder order;
			MQOrderImportMessage.OrderImportMessage message;
			Date date =new Date();
			List<Double> rmIds = Lists.newArrayList();
			for (int i = 0,length = ids.length; i < length; i++)
			{
				id = ids[i];
				order = maps.get(id);
				if(order.getCanSave() == 0){
					//不符合要求订单，不能保存
					msg.append(String.format("<div class='alert alert-error'>用户:%s %s</div>",order.getUserName(),"不符合订单要求，不能保存"));
					continue;
				}
				order.setCreateBy(user);
				order.setCreateDate(date);
				order.setRetryTimes(0);
				//order.setCreateDate(new Date());
				//save
				try {
					//save
					//orderService.insertTempOrder(order);
					message = mapper.modelToMq(order);
					if(message != null) {
						messageSender.send(message);
						//remove from list
						rmIds.add(Double.valueOf(StringUtils.right(id.toString(), 14)));
						maps.remove(id);
					}else{
						msg.append(String.format("<div class='alert alert-error'>用户:%s %s</div>",order.getUserName(),"转换为自动消息失败"));
					}
				}catch (Exception e) {
					msg.append(String.format("<div class='alert alert-error'>用户:%s %s</div>",order.getUserName(),e.getMessage()));
				}
			}
			//cache update
			if(maps.size()==0){
				try {
					redisUtils.remove(RedisConstant.RedisDBType.REDIS_SD_DB, key);
				}catch (Exception e){
					log.error("remove key:"+key,e);
					try{
						redisUtils.remove(RedisConstant.RedisDBType.REDIS_SD_DB, key);
					}catch (Exception e1){
						log.error("remove key:"+key,e1);
					}
				}
			}else{
				try {
					Double score;
					for(int i=0,size=rmIds.size();i<size;i++){
						score = rmIds.get(i);
						redisUtils.zRemRangeByScore(RedisConstant.RedisDBType.REDIS_SD_DB, key,score,score);
					}
					//过期时间
					redisUtils.expire(RedisConstant.RedisDBType.REDIS_SD_DB,key,IMP_ORDER_CACHE_TTL);
				}catch (Exception e){
					log.error("remove key:"+key,e);
					try {
						Set<RedisZSetCommands.Tuple> sets = Sets.newHashSet();
						maps.forEach((k, v) -> {
							sets.add(new RedisTuple(gsonRedisSerializer.serialize(v), Double.valueOf(StringUtils.right(k.toString(), 14))));
						});
						//先重命名
						if(redisUtils.renameNX(RedisConstant.RedisDBType.REDIS_SD_DB, key,key+":DEL")) {
							//再过期
							redisUtils.expire(RedisConstant.RedisDBType.REDIS_SD_DB,key+":DEL",2);
						}else {
							//重命名失败，直接删除
							redisUtils.remove(RedisConstant.RedisDBType.REDIS_SD_DB, key);
						}
						redisUtils.zAdd(RedisConstant.RedisDBType.REDIS_SD_DB, key, sets, IMP_ORDER_CACHE_TTL);
					} catch (Exception e1) {
						log.error("update key:" + key);
					}
				}
			}
			if(msg.length()>0){
                result.setSuccess(false);
                result.setMessage(msg.toString());
			}else {
                result.setSuccess(true);
			}
			msg = null;
		} catch (Exception e)
		{
            result.setSuccess(false);
            result.setMessage(e.getMessage().toString());
		}
		return result;
	}

	/**
	 * 清除指定的读取的订单
	 * @param ids
	 * @param response
	 * @return
	 */
	@RequiresPermissions("sd:order:import")
	@ResponseBody
	@RequestMapping(value = "clear",method = RequestMethod.POST)
	public AjaxJsonEntity clearImportData(@RequestParam(value = "ids[]") Long[] ids, HttpServletResponse response)
	{
		response.setContentType("application/json; charset=UTF-8");
		AjaxJsonEntity jsonEntity = new AjaxJsonEntity();
        User user = UserUtils.getUser();
        if(user==null || user.getId()==null){
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage("登录超时，请重新登录。");
            return jsonEntity;
        }

		try {
			String key = String.format(RedisConstant.SD_TMP_ORDER, user.getId());
			if (ids == null || ids.length == 0) {
				try {
					redisUtils.remove(RedisConstant.RedisDBType.REDIS_SD_DB, key);
					jsonEntity.setSuccess(true);
				} catch (Exception e) {
					jsonEntity.setSuccess(false);
					jsonEntity.setMessage("清除数据失败:" + e.getMessage());
				}
				return jsonEntity;
			}

			List<TempOrder> orders = redisUtils.zRange(RedisConstant.RedisDBType.REDIS_SD_DB, key, 0, -1, TempOrder.class);
			if (orders == null || orders.size() == 0) {
				jsonEntity.setSuccess(false);
				jsonEntity.setMessage("数据不见了,请刷新");
				return jsonEntity;
			}
			//clear all
			if (ids.length == orders.size()) {
				try {
					redisUtils.remove(RedisConstant.RedisDBType.REDIS_SD_DB, key);
					jsonEntity.setSuccess(true);
				} catch (Exception e) {
					jsonEntity.setSuccess(false);
					jsonEntity.setMessage("清除数据失败:" + e.getMessage());
				}
				return jsonEntity;
			}

			//逐条移除
			StringBuffer msg = new StringBuffer();
			//list -> Map<id,TempOrder>
			Map<Long, TempOrder> maps = orders.stream().collect(Collectors.toMap(TempOrder::getId, item -> item));
			orders.clear();
			orders = null;
			Long id;
			TempOrder order;
			List<Double> rmIds = Lists.newArrayList();
			for (int i = 0, length = ids.length; i < length; i++) {
				id = ids[i];
				order = maps.get(id);
				try {
					maps.remove(id);
					rmIds.add(Double.valueOf(StringUtils.right(id.toString(), 14)));
				} catch (Exception e) {
					msg.append(String.format("用户:%s 的订单移除失败：%s", order.getUserName(), e.getMessage()));
				}
			}
			//cache update
			try {
				Double score;
				for(int i=0,size=rmIds.size();i<size;i++){
					score = rmIds.get(i);
					redisUtils.zRemRangeByScore(RedisConstant.RedisDBType.REDIS_SD_DB, key,score,score);
				}
				//过期时间
				redisUtils.expire(RedisConstant.RedisDBType.REDIS_SD_DB,key,IMP_ORDER_CACHE_TTL);
			} catch (Exception e) {
				try {
					Set<RedisZSetCommands.Tuple> sets = Sets.newHashSet();
					maps.forEach((k, v) -> {
						sets.add(new RedisTuple(gsonRedisSerializer.serialize(v), Double.valueOf(StringUtils.right(k.toString(), 14))));
					});
					//先重命名
					if(redisUtils.renameNX(RedisConstant.RedisDBType.REDIS_SD_DB, key,key+":DEL")) {
						//再过期
						redisUtils.expire(RedisConstant.RedisDBType.REDIS_SD_DB,key+":DEL",2);
					}else {
						//重命名失败，直接删除
						redisUtils.remove(RedisConstant.RedisDBType.REDIS_SD_DB, key);
					}
					redisUtils.zAdd(RedisConstant.RedisDBType.REDIS_SD_DB, key, sets, IMP_ORDER_CACHE_TTL);
				} catch (Exception e1) {
					log.error("update key:" + key);
				}
			}

			jsonEntity.setSuccess(true);
		} catch (Exception e)
		{
			jsonEntity.setSuccess(false);
			jsonEntity.setMessage(e.getMessage().toString());
		}
		return jsonEntity;
	}


	/**
	 * 导出检查有问题的订单
	 * 模板与导入模板相同，增加一列：检查结果，供修正资料参考
	 *
	 * @param paramMap
	 * @param request
	 * @param response
	 * @param redirectAttributes
	 * @return
	 */
	@RequestMapping(value = "errorlist")
	public String exportErrorList(@RequestParam Map<String, Object> paramMap, HttpServletRequest request, HttpServletResponse response, RedirectAttributes redirectAttributes, Model model)
	{
		try
		{
			User user = UserUtils.getUser();
			if(user==null || user.getId()==null){
				addMessage(model, "登录超时，请重新登录。");
				return JSP_DIRECOTRY + "orderImportForm";
			}

			String xName = "异常导入明细";
			SXSSFWorkbook xBook;
			Sheet xSheet;
			Map<String, CellStyle> xStyle;

			String key =String.format(RedisConstant.SD_TMP_ORDER,user.getId());
			List<TempOrder> orders =redisUtils.zRange(RedisConstant.RedisDBType.REDIS_SD_DB,key,0,-1,TempOrder.class);
			if(orders==null || orders.size()==0){
				addMessage(model, "导出Excel失败！失败信息：订单列表为空。");
				return JSP_DIRECOTRY + "orderImportForm";
			}
			//延时过期时间
			try {
				redisUtils.expire(RedisConstant.RedisDBType.REDIS_SD_DB,key,IMP_ORDER_CACHE_TTL);
			}catch (Exception e){}

			ExportExcel exportExcel = new ExportExcel();
			xBook = new SXSSFWorkbook(500);
			xSheet = xBook.createSheet(xName);
			xSheet.setDefaultColumnWidth(15);
			xStyle = exportExcel.createStyles(xBook);

			// 加入标题
			int rowNum = 0;
			Row titleRow = xSheet.createRow(rowNum++); // 添加一行
			titleRow.setHeightInPoints(30); // row高度
			Cell titleCell = titleRow.createCell(0); // 对cell(0)编辑, 对应A1
			titleCell.setCellStyle(xStyle.get("redTitle")); // cell样式
			titleCell.setCellValue(xName); // 写入cell内容
			//xSheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 12));//合并单元格

			Row headRow;
			Row dataRow;
			Cell cell;
			xSheet.setColumnWidth(15,50*256);
			CellStyle wrapCellStyle = xBook.createCellStyle();
			wrapCellStyle.setWrapText(true);
//			xSheet.setDefaultColumnStyle(12,wrapCellStyle);
			// 加入表头
			headRow = xSheet.createRow(rowNum++);
			headRow.setHeightInPoints(16);

			String[] TableTitle = new String[]
					{ "联系人","手机", "电话", "安装地址", "产品名称","品牌",  "型号",
					  "数量", "服务项目", "服务描述", "快递公司", "快递单号",
					  "自定义1", "自定义2","自定义3","第三方单号","异常信息"
					};
			for (int i = 0; i < TableTitle.length; i++)
			{
				//xSheet.addMergedRegion(new CellRangeAddress(1, 1, i, i));
				cell = headRow.createCell(i);
				cell.setCellStyle(xStyle.get("header"));
				cell.setCellValue(TableTitle[i]);
				if(i == TableTitle.length-1){
					cell.setCellStyle(xStyle.get("redHeader"));
				}
			}

			xSheet.createFreezePane(0, rowNum); // 冻结单元格(x, y)

			TempOrder order;
			for (int j = 0,length=orders.size(); j < length; j++)
			{
				order = orders.get(j);
				if (order != null)
				{
					if ( StringUtils.isNoneBlank(order.getErrorMsg()) )
					{
						dataRow = xSheet.createRow(rowNum++);
						dataRow.setHeightInPoints(12);

						cell = dataRow.createCell(0);
						cell.setCellStyle(xStyle.get("data"));
						cell.setCellValue(order.getUserName() == null ? "" : order.getUserName());

						cell = dataRow.createCell(1);
						cell.setCellStyle(xStyle.get("data"));
						cell.setCellValue(order.getPhone() == null ? "" : order.getPhone());

						cell = dataRow.createCell(2);
						cell.setCellStyle(xStyle.get("data"));
						cell.setCellValue(order.getTel() == null ? "" : order.getTel());

						cell = dataRow.createCell(3);
						cell.setCellStyle(xStyle.get("data"));
						cell.setCellValue(order.getAddress() == null ? "" : order.getAddress());

						cell = dataRow.createCell(4);
						cell.setCellStyle(xStyle.get("data"));
						cell.setCellValue(order.getOrgProduct());
						//cell.setCellValue(order.getProduct()==null ? "" : order.getProduct().getName());

						cell = dataRow.createCell(5);
						cell.setCellStyle(xStyle.get("data"));
						cell.setCellValue(order.getBrand() == null ? "" : order.getBrand());

						cell = dataRow.createCell(6);
						cell.setCellStyle(xStyle.get("data"));
						cell.setCellValue(order.getOrgProductSpec());
						//cell.setCellValue(order.getProductSpec() == null ? "" : order.getProductSpec());

						cell = dataRow.createCell(7);
						cell.setCellStyle(xStyle.get("data"));
						cell.setCellType(Cell.CELL_TYPE_NUMERIC);
						cell.setCellValue(order.getOrgQty());

						cell = dataRow.createCell(8);
						cell.setCellStyle(xStyle.get("data"));
						cell.setCellValue(order.getOrgServiceType());
						//cell.setCellValue(order.getServiceType() == null ? "" : order.getServiceType().getName());

						cell = dataRow.createCell(9);
						cell.setCellStyle(wrapCellStyle);
						cell.setCellValue(order.getOrgDesription() == null ? "" : order.getOrgDesription());
						//快递
						cell = dataRow.createCell(10);
						cell.setCellStyle(wrapCellStyle);
						cell.setCellValue(order.getOrgExpressCompany());

						cell = dataRow.createCell(11);
						cell.setCellStyle(wrapCellStyle);
						cell.setCellValue(order.getExpressNo());
						//自定义
						cell = dataRow.createCell(12);
						cell.setCellStyle(wrapCellStyle);
						cell.setCellValue(order.getDefine1());

						cell = dataRow.createCell(13);
						cell.setCellStyle(wrapCellStyle);
						cell.setCellValue(order.getDefine2());

						cell = dataRow.createCell(14);
						cell.setCellStyle(wrapCellStyle);
						cell.setCellValue(order.getDefine3());
						//error
						cell = dataRow.createCell(15);
						cell.setCellStyle(wrapCellStyle);
						cell.setCellValue(order.getThdNo());
						//error
						cell = dataRow.createCell(16);
						cell.setCellStyle(xStyle.get("redData"));
						cell.setCellValue(order.getErrorMsg());

					}
				}
			}



			response.reset();
			response.setContentType("application/octet-stream; charset=utf-8");
			response.setHeader("Content-Disposition", "attachment; filename=" + Encodes.urlEncode(xName + ".xlsx"));
			xBook.write(response.getOutputStream());
			xBook.dispose();
			return null;//否则报错：getOutputStream() has already been called for this response
		} catch (Exception e)
		{
			addMessage(model, "导出Excel失败！失败信息：" + e.getMessage());
			return JSP_DIRECOTRY + "orderImportForm";
		}
	}

	//endregion

	//region 异常管理
	/**
	 * 导入的订单管理
	 */
	@RequiresPermissions("sd:temporder:view")
	@RequestMapping(value = "manage")
	public String retryManage(TempOrder order, HttpServletRequest request, HttpServletResponse response, Model model)
	{
		Page<TempOrder> page = new Page<TempOrder>();
		//导入异常列表每页显示500笔 2019-11-12
		//导入异常列表每页显示100笔 2020-11-02
		page.setPageSize(100);
		User user = UserUtils.getUser();
		Boolean canSearch = true;
		//提交查询
			Boolean errorFlag = false;
			//检查客户帐号信息
			if (user.isCustomer()) {
                CustomerAccountProfile profile = user.getCustomerAccountProfile();
                if(profile == null || profile.getCustomer() == null){
                    addMessage(model, "错误：读取帐号信息失败，请重试");
                    errorFlag = true;
                }else {
                    Customer customer = customerService.getFromCache(user.getCustomerAccountProfile().getCustomer().getId());
                    if (customer == null) {
                        addMessage(model, "错误：读取帐号信息失败，请重试");
                        errorFlag = true;
                    } else if (customer.getEffectFlag() != 1) {
                        addMessage(model, "错误：您的账户已冻结，暂时无法下单，请联系管理员。");
                        errorFlag = true;
                    }else {
                        order.setCustomer(customer);
                    }
                }
				//客户账号负责的店铺 2021/06/22
				List<String> shopIds = UserUtils.getShopIdsOfCustomerAccount(user);
				if(!CollectionUtils.isEmpty(shopIds)){
					shopIds.add(StringUtils.EMPTY);//无店铺情况
					order.setShopIds(shopIds);
				}
			}
			if(errorFlag){
				model.addAttribute("page", page);
				model.addAttribute("order", order);
				model.addAttribute("canSearch",false);
				return JSP_DIRECOTRY + "retryOrderList";
			}
			if(order.getUpdateDate() ==null) {
				order.setUpdateDate(new Date());
				order.setCreateDate(DateUtils.addMonth(order.getUpdateDate(), -1));
			}else{
			    order.setUpdateDate(DateUtils.getDateEnd(order.getUpdateDate()));
            }
			try {
				page = orderImportService.findRetryTempOrder(new Page<TempOrder>(request, response,page.getPageSize()), order);
			} catch (Exception e) {
				addMessage(model, "错误："+e.getMessage());
			}
		model.addAttribute("page", page);
		model.addAttribute("order",order);
		model.addAttribute("canSearch",canSearch);

		return JSP_DIRECOTRY + "retryOrderList";

	}

	/**
	 * 客户取消订单 form
	 *
	 * @param id  订单id
	 */
	@RequiresPermissions("sd:temporder:cancel")
	@RequestMapping(value = "cancel", method = RequestMethod.GET)
	public String cancel(@RequestParam(required = true) Long id, HttpServletRequest request, Model model) {
		Order order = new Order(id);
		model.addAttribute("order", order);
		return JSP_DIRECOTRY + "cancelForm";
	}

	/**
	 * 客户ajax提交取消订单
	 *
	 * @param order
	 * @param response
	 * @return
	 */
	@RequiresPermissions("sd:temporder:cancel")
	@ResponseBody
	@RequestMapping(value = "cancel", method = RequestMethod.POST)
	public AjaxJsonEntity cancel(TempOrder order, HttpServletResponse response)
	{
		response.setContentType("application/json; charset=UTF-8");
		AjaxJsonEntity result = new AjaxJsonEntity(true);
        User user = UserUtils.getUser();
        if(user==null || user.getId()==null){
            result.setSuccess(false);
            result.setMessage("登录超时，请重新登录。");
            return result;
        }
		try
		{
			orderService.cancelTempOrder(order.getId(),user,order.getRemarks());
		} catch (Exception e) {
			result.setSuccess(false);
			result.setMessage(e.getMessage());
			log.error("[OrderImportController.cancel] orderId:{}",order.getId(),e);
		}
		return result;
	}

	/**
	 * 修改导入后未成功转换的订单，人工转换
	 * @param order
	 * @param request
	 * @param response
	 * @param model
	 * @return
	 */
	@RequiresPermissions(value = { "sd:temporder:edit", "sd:temporder:add" }, logical = Logical.OR)
	@RequestMapping(value = "form",method = RequestMethod.GET)
	//@FormToken(save=true)
	public String form(CreateOrderModel order, HttpServletRequest request, HttpServletResponse response, Model model)
	{
		String returnForm = JSP_DIRECOTRY + "orderEditForm";
		//first request
		String cid = request.getParameter("id");
		Long id = Long.parseLong(cid);
		boolean errorFlag = false;
		if(id == null || id<=0){
			addMessage(model,"错误：订单参数为空");
            errorFlag = true;
		}
		//String lockKey = new String();
		//if(!errorFlag) {
		//	lockKey = String.format(RedisConstant.SD_TMP_ORDER_TRANSFER, cid);
		//	if (redisUtils.exists(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockKey)) {
		//		addMessage(model, "错误：此订单已在处理中，请稍候重试，或刷新页面!");
		//		errorFlag = true;
		//	}
		//}

		TempOrder o = null;
		if(!errorFlag) {
            o = orderImportService.getTempOrder(id);
            if (o == null || o.getCustomer() == null || o.getCustomer().getId() == null) {
                addMessage(model, "错误：订单信息不完整");
                errorFlag = true;
            }
            if (o.getSuccessFlag() == 1 || o.getDelFlag() == TempOrder.DEL_FLAG_DELETE) {
                addMessage(model, o.getSuccessFlag() == 1 ? "错误：订单已转成正式订单" : "错误：订单已被取消");
                errorFlag = true;
            }
            Product product = o.getProduct();
            if(product == null || product.getId() == null || product.getId() == 0){
				addMessage(model,  "错误：读取产品信息错误");
				errorFlag = true;
			}else{
            	product = productService.getProductByIdFromCache(product.getId());
            	if(product == null){
					addMessage(model,  "错误：读取产品信息错误");
					errorFlag = true;
				}else{
            		o.setProduct(product);
				}
			}
        }
        if(errorFlag) {
            model.addAttribute("order", order);
            model.addAttribute("canCreateOrder", false);
            return returnForm;
        }

		Customer customer = null;
		User user = UserUtils.getUser();
		//检查是否可下单
		if(user.isCustomer()){
            CustomerAccountProfile profile = user.getCustomerAccountProfile();
            if(profile == null || profile.getCustomer() == null){
                addMessage(model, "读取账户信息失败，请重试。");
                errorFlag = true;
            }else {
                customer = customerService.getFromCache(profile.getCustomer().getId());
                if (customer == null) {
                    addMessage(model, "读取账户信息失败，请重试。");
                    errorFlag = true;
                } else if (customer.getEffectFlag() == 0) {
                    addMessage(model, "您的账户已冻结，暂时无法下单，请联系管理员。");
                    errorFlag = true;
                }
            }
			if(errorFlag) {
				model.addAttribute("order", order);
				model.addAttribute("canCreateOrder", false);
				return returnForm;
			}
        }else{
			customer = customerService.getFromCache(o.getCustomer().getId());
		}

		try {
			order = toCreateOrderModel(o);
			order.setCustomer(customer);
			order.setExpresses(MSDictUtils.getDictList("express_type"));//切换为微服务
            order.setDataSource(new Dict("1","快可立"));
            //models、brand、b2bProductCodes 2019/02/24
            List<Long> productIds = order.getItems().stream().map(OrderItem::getProductId).collect(Collectors.toList());
            Map<Long, ThreeTuple<List<Brand>, List<CustomerProductModel>, List<String>>> productPropertiesMap = orderEditFormService.getProductPropertyEntris(customer.getId(),StringUtils.toInteger(order.getDataSource().getValue()), productIds);
            if (!productPropertiesMap.isEmpty()) {
                ThreeTuple<List<Brand>, List<CustomerProductModel>, List<String>> productProperties;
                for (OrderItemModel item : order.getItems()) {

                    productProperties = productPropertiesMap.get(item.getProductId());
                    if (productProperties != null) {
                        item.setBrands(productProperties.getAElement());
                        item.setModels(productProperties.getBElement());
                        item.setB2bProductCodes(productProperties.getCElement());
                    }
                }
            }
            //end
		}
		catch (Exception e){
			addMessage(model,e.getMessage());
			model.addAttribute("order", order);
			model.addAttribute("canCreateOrder", false);
			return returnForm;
		}
		StringBuilder params = new StringBuilder();
		Map<String, String[]>  map = request.getParameterMap();
		map.forEach((k,v)->{
			if(!k.equalsIgnoreCase("id")){
				if(v.length>0) {
					params.append("&").append(k).append("=").append(URLEncoder.encode(v[0]));
				}
			}
		});
		request.getSession().setAttribute("queryparams",params.toString());
		//order.setCreateBy(user);//更改为当前用户 -> 2020/08/04 不变更负责人
		//model.addAttribute("b2bShopList",B2BMDUtils.getCustomerKKLShopListNew(order.getCustomer().getId()));
		Long customerId = Optional.ofNullable(order.getCustomer()).map(Customer::getId).orElse(0L);
		List<Dict> shops = UserUtils.getShops(customerId,user);// 按客户+账号读取店铺清单
		model.addAttribute("b2bShopList",shops);
		model.addAttribute("order", order);
		//加急，下单选择了加急或者加急开关开启
		String urgentFlag = MSDictUtils.getDictSingleValue("OrderUrgentFlag","0");
		if(urgentFlag.equalsIgnoreCase("1") || (order.getUrgentLevel() != null && order.getUrgentLevel().getId() != null && order.getUrgentLevel().getId().longValue()>0 )){
			order.setUrgentFlag(1);
			List<UrgentLevel> urgentLevels = urgentLevelService.findAllList();
			model.addAttribute("urgentLevels",urgentLevels);
		} else {
			order.setUrgentFlag(0);
		}
		model.addAttribute("canCreateOrder", true);

		return returnForm;
	}

	/**
	 * 将导入订单转为订单修改视图
	 * @param order
	 * @return
	 */
	private  CreateOrderModel toCreateOrderModel(TempOrder order){
		CreateOrderModel model = new CreateOrderModel();
		if(order == null){
			return model;
		}
		if(order.getQty()<=0){
			throw new OrderException("产品数量应大于0");
		}
		model.setId(order.getId());
		model.setTotalQty(order.getQty());
		model.setDescription(order.getDescription());
		//condition
		model.setUserName(order.getUserName());
		model.setPhone1(order.getPhone());
		model.setServicePhone(order.getPhone());
		model.setPhone2(order.getTel());
		model.setAddress(order.getAddress());//详细地址
		model.setServiceAddress(order.getAddress());
		model.setCustomer(order.getCustomer());
		model.setArea(new Area());
		model.setB2bOrderNo(order.getThdNo());//第三方单号 2018/12/19
		List<CustomerPrice> prices = customerService.getPricesFromCache(order.getCustomer().getId());
		//items
		OrderItemModel item = new OrderItemModel();
		item.setProduct(order.getProduct());
		if(order.getProduct() != null && order.getProduct().getCategory() != null){
			model.setCategory(order.getProduct().getCategory());
		}
		item.setServiceType(order.getServiceType());
		item.setBrand(order.getBrand());
		item.setProductSpec(order.getProductSpec());
		item.setExpressCompany(order.getExpressCompany());
		item.setExpressNo(order.getExpressNo());
		item.setItemNo(1);
		item.setQty(order.getQty());
		//price
		CustomerPrice price = prices.stream()
				.filter(p ->
						Objects.equals(p.getProduct().getId(),order.getProduct().getId())
							&& Objects.equals(p.getServiceType().getId(),order.getServiceType().getId())
				)
				.findFirst()
				.orElse(null);
		if(price == null){
			throw new OrderException(String.format("系统中未定义产品:%s 服务:%s 的价格",order.getProduct().getName(),order.getServiceType().getName()));
		}

		item.setStandPrice(price.getPrice());
		item.setDiscountPrice(price.getDiscountPrice());
		item.setCharge(price.getPrice()*1 + price.getDiscountPrice()*(order.getQty()-1));
		item.setBlockedCharge(price.getBlockedPrice()*order.getQty());
		model.getItems().add(item);

		//Fee
		//CustomerFinance fi = customerService.getFinance(order.getCustomer().getId());
		CustomerFinance fi = customerService.getFinanceForAddOrder(order.getCustomer().getId());
		model.setOrderPaymentType(fi.getPaymentType());
		model.setTotalQty(order.getQty());
		model.setCustomerBalance(fi.getBalance()-fi.getBlockAmount());// 可下单金额=客户余额-冻结金额
		if(fi.getCreditFlag()==1) {
			model.setCustomerCredit(fi.getCredit());// 客户信用额度
		}

		model.setExpectCharge(item.getCharge()+item.getBlockedCharge());
		model.setBlockedCharge(item.getBlockedCharge());
		model.setTotalQty(item.getQty());//*

		//check余额
		if(model.getCustomerBalance() + model.getCustomerCredit() - model.getExpectCharge() - model.getBlockedCharge() <0){
			throw  new OrderException("账户余额不足...");
		}

		model.setCreateBy(order.getCreateBy());// 创建者
		model.setCreateDate(order.getCreateDate());// 创建日期
		model.setUpdateDate(order.getUpdateDate());
		model.setCustomerOwner(order.getCreateBy().getName());//负责人
		if(order.getB2bShop()!=null && StringUtils.isNotBlank(order.getB2bShop().getShopId())){ //购买店铺
			model.setB2bShop(order.getB2bShop());
		}

		return model;
	}

	/**
	 * 保存人工修改订单
	 * 将导入订单转成正在的订单
	 * @version 2.1
	 * 1.去掉sd_orderitem读写
	 * 2.sd_order_fee改为消息队列处理
	 * 3.md_customer_finance读取栏位精简，
	 *   按需读取(lock_flag,credit,credit_flag,block_amount,balance)
	 * 4.冻结流水使用消息队列处理(移到service外处理)
	 */
	@RequiresPermissions("sd:temporder:add")
	@RequestMapping(value = "manualTransferOrder",method = RequestMethod.POST)
	//@FormToken(remove=true)
	public String manualransferOrder(CreateOrderModel order, HttpServletRequest request,
							HttpServletResponse response, Model model,
							RedirectAttributes redirectAttributes)
	{


		//region 检查
		User user = UserUtils.getUser();
		if (user == null || user.getId()==null)
		{
			try
			{
				SecurityUtils.getSubject().logout();
			} catch (Exception e)
			{
				addMessage(redirectAttributes, "您的账号登录超时，请重新登录。");
				return "redirect:" + Global.getAdminPath() + "/sd/order/import/new/form?id=" + order.getId();
			}
		}
		// check
		if (!beanValidator(model, order))
		{
			return form(order,request, response, model);
		}

		// 如果区域为空的情况
		if (order.getArea() == null || order.getArea().getId() == null)
		{
			addMessage(model, "找不到指定的区域,请重新选择。");
			return form(order, request, response, model);
		}
		//检查区域type
		Area area  = areaService.getFromCache(order.getArea().getId());
		if(area == null){
			addMessage(model, "找不到指定的区域,请重新选择。");
			return form(order, request, response, model);
		}
		if(area.getType() != 4){
			addMessage(model, "区域请选择至区/县,请重新选择。");
			return form(order, request, response, model);
		}

		if(order.getCustomer()==null || order.getCustomer().getId()==null){
			addMessage(model, "请选择客户。");
			return form(order, request, response, model);
		}
		Long tmpId = order.getId();//临时订单id
		//检查是否已转成订单
		TempOrder tmpo = orderImportService.getTempOrder(tmpId);
		if(tmpo==null ||  tmpo.getCustomer() == null || tmpo.getCustomer().getId()==null) {
			addMessage(model, "读取导入订单信息错误");
			return form(order, request, response, model);
		}

		if(tmpo.getSuccessFlag() == 1 || tmpo.getDelFlag() == TempOrder.DEL_FLAG_DELETE){
			addMessage(model, tmpo.getSuccessFlag() == 1?"订单已转成正式订单":"订单已被取消");
			return form(order, request, response, model);
		}

		//customer
		Customer customer = customerService.getFromCache(order.getCustomer().getId());
		if(customer == null){
			addMessage(model, "检查客户结算方式错误。");
			return form(order, request, response, model);
		}

		//2018/04/08 检查客户是否冻结下单
		if(user.isCustomer() && customer.getEffectFlag() != 1){
			addMessage(model, "您的账户基本信息不完整，请完善基本信息。");
			return form(order, request, response, model);
		}

		CustomerFinance cacheFinance = customer.getFinance();
		if(cacheFinance == null || cacheFinance.getPaymentType() == null || StringUtils.isBlank(cacheFinance.getPaymentType().getValue())){
			addMessage(redirectAttributes,"未设置结算方式，请联系系统管理员。");
			return form(order, request, response, model);
		}

		//金额二次检查
		CustomerFinance finance = customerService.getFinanceForAddOrder(order.getCustomer().getId());
		if(finance.getBalance() + (finance.getCreditFlag() == 1 ? finance.getCredit() : 0) -finance.getBlockAmount() - order.getExpectCharge() - order.getBlockedCharge()<0){
			addMessage(model, "账户余额不足，请尽快充值。");
			return form(order, request, response, model);
		}
        order.setOrderPaymentType(finance.getPaymentType());
		//同步
		CustomerFinance fi = customer.getFinance();
		fi.setBalance(finance.getBalance());
		fi.setBlockAmount(finance.getBlockAmount());
		fi.setCredit(finance.getCredit());
		//endregion 检查

		order.setCustomer(customer);//*
		//order.setCreateBy(user);//2020-08-04 不变更负责人
		Long createById = Optional.ofNullable(order.getCreateBy()).map(t->t.getId()).orElse(null);
		if(createById == null || createById <=0){
			addMessage(model, "无订单负责人，无法保存订单。");
			return form(order, request, response, model);
		}
		String userName = StringUtils.trimToEmpty(order.getCreateBy().getName());
		if(StringUtils.isBlank(userName)){
			addMessage(model, "无订单负责人，无法保存订单。");
			return form(order, request, response, model);
		}

		// 订单项次处理
		List<OrderItemModel> list = order.getItems();
		List<CustomerPrice> prices = customerService.getPricesFromCache(order.getCustomer().getId());
		Optional<CustomerPrice> price ;
		Product p;
		Set<String> pids = Sets.newHashSet();//产品
		Long categoryId = null;//产品类别
		Set<String> sids = Sets.newHashSet();//服务项目
		Integer hasSet = 0;
		List<Dict> expressCompanys = MSDictUtils.getDictList("express_type");//切换为微服务
		Dict expressCompany;
		int orderServiceType = 0;
		// 根据服务类型中工单类型判断
		Map<Long,ServiceType> serviceTypeMap = serviceTypeService.getAllServiceTypeMap();
		ServiceType serviceType;
		StringBuilder content = new StringBuilder();
		content.append("师傅，在您附近有一张  ");
		// 移除产品为空的项目,并读取最新价格
		for (Iterator<OrderItemModel> it = list.iterator(); it.hasNext();)
		{
			OrderItem item = it.next();
			if(item.getProduct() == null || item.getServiceType() == null){
				it.remove();
				continue;
			}
			//价格
			price = prices.stream().filter(t->
					Objects.equals(t.getProduct().getId(),item.getProduct().getId()) && Objects.equals(t.getServiceType().getId(),item.getServiceType().getId())).findFirst();
			if(!price.isPresent()){
				addMessage(model, String.format("产品:%s 未定义服务项目:%s 的服务价格",item.getProduct().getName(),item.getServiceType().getName()));
				return form(order, request, response, model);
			}
			p = productService.getProductByIdFromCache(item.getProduct().getId());
			//类目检查 2019-09-25
			if(categoryId == null){
				categoryId = p.getCategory().getId();
			}else if(!categoryId.equals(p.getCategory().getId())){
				addMessage(model, "订单中产品属不同品类，无法保存。");
				return form(order, request, response, model);
			}
			item.setProduct(p);
			item.setStandPrice(price.get().getPrice());
			item.setDiscountPrice(price.get().getDiscountPrice());
			if(expressCompanys!=null && expressCompanys.size()>0){
				expressCompany = expressCompanys.stream().filter(t->t.getValue().equalsIgnoreCase(item.getExpressCompany().getValue())).findFirst().orElse(item.getExpressCompany());
				item.setExpressCompany(expressCompany);
			}
			if(p.getSetFlag()==1){
				hasSet = 1;
			}
			pids.add(String.format(",%s,",p.getId()));
			sids.add(String.format(",%s,",item.getServiceType().getId()));
			//工单类型按服务项目设定为准
			serviceType = serviceTypeMap.get(item.getServiceType().getId());
			if(serviceType == null){
				addMessage(model, "确认服务项目的工单类型错误，请重试");
				return form(order, request, response, model);
			}
			//除维修(2)外，值最大的优先
			if(orderServiceType == 0){
				orderServiceType = serviceType.getOrderServiceType();
			}else if (serviceType.getOrderServiceType() == 2){
				orderServiceType = serviceType.getOrderServiceType();
			}else if(orderServiceType < serviceType.getOrderServiceType()){
				orderServiceType = serviceType.getOrderServiceType();
			}
			content.append(item.getServiceType().getName())
					.append(item.getBrand())
					.append(com.wolfking.jeesite.common.utils.StringUtils.getStandardProductName(item.getProduct().getName()));
		}
		content.append("的工单，请尽快登陆APP接单~");
		int canRush = 0;
		int kefuType = 0;
		//vip客户，不检查突击区域 ， 街道id小于等于3也不检查突击区域 2020-06-20 Ryan
		//vip客户，不检查客服类型 ， 街道id小于等于3也不检查客服类型 2020-12-9
		long subAreaId = Optional.ofNullable(order.getSubArea()).map(t->t.getId()).orElse(0l);
		Map<Integer,Area> areas = areaService.getAllParentsWithDistrict(order.getArea().getId());
		Area province = areas.getOrDefault(Area.TYPE_VALUE_PROVINCE,new Area(0L));
		Area city = areas.getOrDefault(Area.TYPE_VALUE_CITY,new Area(0L));
		/*if(customer.getVipFlag()==1){
			kefuType = OrderCondition.VIP_KEFU_TYPE;
		}else{ //有街道
			//Map<Integer,Area> areas = areaService.getAllParentsWithDistrict(subAreaId);
			//Area canRushCity = areas.getOrDefault(Area.TYPE_VALUE_CITY,new Area(0L));
			canRush = orderService.isCanRush(categoryId,city.getId(),order.getArea().getId(),subAreaId);
			kefuType = orderService.getKefuType(categoryId,city.getId(),order.getArea().getId(),subAreaId);
		}*/
		OrderKefuTypeRuleEnum orderKefuTypeRuleEnum  = orderService.getKefuType(categoryId,city.getId(),order.getArea().getId(),subAreaId,customer.getVipFlag(),customer.getVip());
		kefuType = orderKefuTypeRuleEnum.getCode();
		if(kefuType == OrderCondition.RUSH_KEFU_TYPE){
			canRush = 1;
		}
		//随机客服
		User kefu = orderService.getRandomKefu(order.getCustomer().getId(),order.getArea().getId(),categoryId,kefuType,city.getId(),province.getId());
		if (kefu == null) {
			//无客服
			//String failReason=orderService.findKefuFail(kefuType,categoryId);
			//addMessage(model, "此区域暂未分配"+failReason+"，暂时无法下单。请联系管理员：18772732342，QQ:572202493");
			String tip = orderService.noFindKefuTip(user,customer,categoryId,kefuType,order.getArea().getId(),city.getId(),province.getId());
			model.addAttribute("noKefuFlag",1);
			model.addAttribute("tip",tip);
			return form(order, request, response, model);
		}
		if(orderServiceType==0){
			orderServiceType = 2;
		}
		//重新计算价格
		OrderUtils.rechargeOrder(list);
		Double totalCharge = 0.00;
		Double blockedCharge = 0.00;
		Integer qty = 0;
		for (OrderItemModel item : list) {
			totalCharge = totalCharge + item.getCharge();
			blockedCharge = blockedCharge + item.getBlockedCharge();
			qty = qty + item.getQty();
		}

		if (qty == 0)
		{
			addMessage(model, "订单无明细项目，请添加。");
			return form(order, request, response, model);
		}
		order.setExpectCharge(totalCharge);
		order.setBlockedCharge(blockedCharge);
		order.setTotalQty(qty);
		order.setCreateBy(tmpo.getCreateBy());//下单人为导入人帐号
		Date date = new Date();
		order.setCreateDate(date);

		Order o;
		//锁,失败立即解锁，成功不解锁
		String lockkey = String.format(RedisConstant.SD_TMP_ORDER_TRANSFER,tmpId);
		boolean locked = redisUtils.getLock(lockkey,tmpId.toString(),IMP_ORDER_TRANSFER_LOCK_TTL);
		if(!locked){
			addMessage(model, "错误:此订单已在处理中，请稍候重试，或刷新页面。");
			return form(order, request, response, model);
		}

		try
		{
			String quarter = QuarterUtils.getSeasonQuarter(order.getCreateDate());//分片
			order.setQuarter(quarter);

            String orderNo = orderService.getNewOrderNo();
			if(StringUtils.isBlank(orderNo)){
				addMessage(model, "生成订单号失败，请重试");
				return form(order, request, response, model);
			}
			order.setOrderNo(orderNo);
			if(StringUtils.isBlank(order.getCustomerOwner())) {
				order.setCustomerOwner(user.getName());//客户负责人 2018/08/03
			}
			//按提交日期算，产生新的订单id,否则订单排序不对 2019/03/02
			// 随机，防止同用户产生重复id
			//int workerId = ThreadLocalRandom.current().nextInt(32);
			//int datacenterId = ThreadLocalRandom.current().nextInt(32);
			//SequenceIdUtils sequence = new SequenceIdUtils(workerId,datacenterId);
			//long newOrderId = sequence.nextId();
			//order.setId(newOrderId);
			order.setId(sequenceIdService.nextId());

			o = OrderUtils.toOrder(order);// 转换
			o.setAppMessage(content.toString());
			OrderFee orderFee = o.getOrderFee();
			//加急
			UrgentChargeModel urgentCharge = null;
			UrgentLevel urgentLevel = o.getOrderCondition().getUrgentLevel();
			if(urgentLevel != null && urgentLevel.getId().longValue()>0){
				o.getOrderStatus().setUrgentDate(o.getCreateDate());
				Area urgentProvince = AreaUtils.getProvinceByArea(order.getArea().getId(),4);
				if(urgentProvince == null){
					addMessage(model, "读取省份错误!");
					order.setId(tmpId);//还原临时订单id
					return form(order, request, response, model);
				}
				AreaUrgentModel areaUrgentModel = urgentCustomerService.getAreaUrgentModel(order.getCustomer().getId(),urgentProvince.getId());
				if(areaUrgentModel != null && areaUrgentModel.getList() != null && areaUrgentModel.getList().size()>0){
					urgentCharge = areaUrgentModel.getList().stream().filter(t->t.getUrgentLevel().getId().longValue() == urgentLevel.getId().longValue()).findFirst().orElse(null);
					if(urgentCharge != null){
						orderFee.setCustomerUrgentCharge(urgentCharge.getChargeIn());//下单时写入，在客评时，判断是否符合加急，不符合更改为0
						orderFee.setEngineerUrgentCharge(urgentCharge.getChargeOut());//下单时写入，在客评时，判断是否符合加急，不符合更改为0
						orderFee.setExpectCharge(orderFee.getExpectCharge()+urgentCharge.getChargeIn());//冻结
					}
				}
			}else{
				o.getOrderStatus().setUrgentDate(null);
			}
			if(urgentCharge == null){
				orderFee.setCustomerUrgentCharge(0.0);
				orderFee.setEngineerUrgentCharge(0.0);
			}

			/* 针对手动选择区域，重新获得经纬度座标 2019-04-15 */
			if(order.getLongitude() <= 0 || order.getLatitude() <= 0){
				String address = MessageFormat.format("{0} {1}",area.getFullName(),o.getOrderCondition().getAddress());
				String[] areaParseResult = AreaUtils.getLocation(address);
				if(areaParseResult != null && areaParseResult.length == 2){
					o.getOrderLocation().setLongitude(StringUtils.toDouble(areaParseResult[0]));
					o.getOrderLocation().setLatitude(StringUtils.toDouble(areaParseResult[1]));
				}
			}
			//再次检查重单 2018/04/19
			if(StringUtils.isBlank(o.getRepeateNo())) {
				String repeateOrderNo = orderService.getRepeateOrderNo(order.getCustomer().getId(),order.getPhone1());
				if (StringUtils.isNotBlank(repeateOrderNo)) {
					o.setRepeateNo(repeateOrderNo);
				}
			}
			Dict status;
			if(user.isSystemUser() || user.isSaleman()){
				//不需审核
				status = MSDictUtils.getDictByValue(String.valueOf(Order.ORDER_STATUS_APPROVED), "order_status");//切换为微服务
			}else if(user.isCustomer() && user.getCustomerAccountProfile().getOrderApproveFlag()==0) {
				//不需审核
				status = MSDictUtils.getDictByValue(String.valueOf(Order.ORDER_STATUS_APPROVED), "order_status");//切换为微服务
			}else {
				status = MSDictUtils.getDictByValue(String.valueOf(Order.ORDER_STATUS_NEW), "order_status");//切换为微服务
			}
			o.getOrderCondition().setTotalQty(qty);
			o.getOrderCondition().setStatus(status);
			o.getOrderCondition().setKefu(kefu);

			o.getOrderCondition().setHasSet(hasSet);
			o.getOrderCondition().setProductIds(pids.stream().collect(Collectors.joining(",")).replace(",,,",",,"));
			o.getOrderCondition().setProductCategoryId(categoryId);//2019-09-25
			o.getOrderCondition().setServiceTypes(sids.stream().collect(Collectors.joining(",")).replace(",,,",",,"));
			o.getOrderCondition().setOrderServiceType(orderServiceType);//12-06

			//如果地址中出现null 字符串，则会引起 API 返回JSON数据到APP 导致格式不匹配,所以加字符串判断
			if(o.getOrderCondition().getAddress()!=null)
			{
				String address=o.getOrderCondition().getAddress().replace("null", "");
				o.getOrderCondition().setAddress(address);
			}

			Dict orderType = MSDictUtils.getDictByValue(String.valueOf(Order.ORDER_ORDERTYPE_DSXD),"order_type");//切换为微服务
			o.setOrderType(orderType);// 电商下单
			/* 省/市id 2019-09-25 */
			/*Map<Integer,Area> areas = areaService.getAllParentsWithDistrict(order.getArea().getId());
			Area province = areas.getOrDefault(Area.TYPE_VALUE_PROVINCE,new Area(0L));
			Area city = areas.getOrDefault(Area.TYPE_VALUE_CITY,new Area(0L));*/
			o.getOrderCondition().setProvinceId(province.getId());
			o.getOrderCondition().setCityId(city.getId());
			o.getOrderCondition().setCanRush(canRush);
			o.getOrderCondition().setKefuType(kefuType);

			//APP通知消息 & 短信
			boolean saveSuccess = true;
			String dailyLogKey = String.format(RedisConstant.SD_CREATE_ORDER_LOG,DateUtils.getDate());
			try{//成功，执行后面操作
				orderService.createOrder_v2_1(o,tmpId);
			}catch (Exception e){
				if(StringUtils.contains(e.getMessage(),"Duplicate")) {
					//订单id重复，一般为重复提交，比如一个账号多人使用
					try{
						orderImportService.retryError(order.getId(),StringUtils.left(e.getLocalizedMessage(),200),user,date,1);
					}catch (Exception e1){
						log.error("导入单保存失败,id:{}",order.getId(),e1);
					}
					addMessage(model,"重复提交，该订单已转换，请按用户信息核对是否已转单。");
				}else {
					try {
						orderImportService.retryError(order.getId(), StringUtils.left(e.getLocalizedMessage(), 200), user, date, null);
					} catch (Exception e1) {
						log.error("导入单保存失败,id:{}", order.getId(), e1);
					}
					saveSuccess = false;
					try {
						log.error("导入单保存失败,order:{}", gsonRedisSerializer.toJson(order), e);
					} catch (Exception e1) {
						log.error("导入单保存失败,id:{}", order.getId(), e);
					}
					//订单号返还redis列表
					try {
						if (StringUtils.isNoneBlank(orderNo)) {
							SeqUtils.reputSequenceNo("OrderNO", o.getCreateDate(), orderNo);
						}
					} catch (Exception e1) {
						log.error("导入单订单号返回失败,orderNo:{}", orderNo, e1);
					}
					addMessage(model, "下单失败,请重试。");
				}
				order.setId(tmpId);//还原临时订单id
				return form(order, request, response, model);
			}finally {
				//下单log，供每日核对，防止漏单
				//先加到redis,不成功记录到sys_log
				try{
					double score = Double.valueOf(orderNo.substring(1));
					if(saveSuccess) {
						redisUtils.zAdd(RedisConstant.RedisDBType.REDIS_TEMP_DB, dailyLogKey, orderNo, score, OrderUtils.REDIS_CREATE_LOG_EXPIRED);
					}else{
						redisUtils.zAdd(RedisConstant.RedisDBType.REDIS_TEMP_DB, dailyLogKey, gsonRedisSerializer.toJson(order), score, OrderUtils.REDIS_CREATE_LOG_EXPIRED);
					}
				}catch (Exception e){
					log.error("[OrderImportController.addOrder] push orderNo:{} to dailyLogKey:{}",orderNo,dailyLogKey,e);
				}
			}

			//region 消息队列
			orderMQService.sendCreateOrderMessage(o, "OrderNewImportController.transferOrder");
			//endregion 消息队列

			addMessage(model, "订单:".concat(o.getOrderNo()).concat(" 保存成功"));
			String params = request.getSession().getAttribute("queryparams").toString().trim();
			StringBuilder url = new StringBuilder("redirect:" + Global.getAdminPath() + "/sd/order/import/new/manage");
			if(StringUtils.isNoneBlank(url)) {
				url.append("?");
				if(params.startsWith("&")){
					url.append(params.substring(1));
				}else{
					url.append(params);
				}
			}
			return url.toString();//订单管理页面
		} catch (Exception e) {
            try {
                LogUtils.saveLog(request, null, e, "保存导入订单错误", gsonRedisSerializer.toJson(order));
            }catch (Exception e1){
                try {
                    LogUtils.saveLog(request, null, e, "保存导入订单错误");
                }catch (Exception e2){
                    log.error("保存导入订单错误",e2);
                }
            }
			addMessage(model, "下单失败,请重试。");
			order.setId(tmpId);//还原临时订单id
			return form(order, request, response, model);
		}finally {
			if(locked && lockkey != null) {
				redisUtils.releaseLockDelay(lockkey,tmpId.toString(),5);
			}
		}
	}

	/**
	 * 批量转单(ajax)
	 * @param ids 选择的id列表
	 */
	@RequiresPermissions("sd:order:import")
	@ResponseBody
	@RequestMapping(value = "retryTransferOrders",method = RequestMethod.POST)
	public AjaxJsonEntity retryTransferOrders(@RequestParam(value = "ids[]") Long[] ids, HttpServletResponse response)
	{
		response.setContentType("application/json; charset=UTF-8");
		AjaxJsonEntity result = new AjaxJsonEntity();
		User user = UserUtils.getUser();
		if(user==null || user.getId()==null){
			result.setSuccess(false);
			result.setMessage("登录超时，请重新登录。");
			return result;
		}

		try
		{
			if(ids == null || ids.length==0) {
				result.setSuccess(false);
				result.setMessage("请选择要保存的订单");
				return result;
			}

			StringBuffer msg = new StringBuffer();
			Long id;
			TempOrder order;
			MQOrderImportMessage.OrderImportMessage.Builder messageBuilder;
			MQOrderImportMessage.OrderImportMessage message;
			Date date =new Date();
			//String quarter = QuarterUtils.getSeasonQuarter(date);
			List<Long> rmIds = Lists.newArrayList();
			OrderImportMessageMapper mapper = Mappers.getMapper(OrderImportMessageMapper.class);
			int idCnt = ids.length;
			Set<Long> idSets = Sets.newHashSet();
			//随机，防止同用户产生重复id
			int workerId = ThreadLocalRandom.current().nextInt(32);
			int datacenterId = ThreadLocalRandom.current().nextInt(32);
			//SequenceIdUtils sequence = new SequenceIdUtils(workerId,datacenterId);
			if(idCnt == 1){
				idSets.add(sequenceIdService.nextId());
			}else {
				Double addRange = Math.ceil(idCnt * 0.2);
				int maxTimes = idCnt + addRange.intValue();
				for (int i = 0; i < maxTimes; i++) {
					//idSets.add(sequence.nextId());
					idSets.add(sequenceIdService.nextId());
				}
			}

			//set 转 List
			LinkedList<Long> idList = Lists.newLinkedList(idSets);

			for (int i = 0; i < idCnt; i++)
			{
				id = ids[i];
				order = orderImportService.getTempOrder(id);
				if(order.getSuccessFlag() == 1){
					msg.append(String.format("<div class='alert alert-error'>用户:%s %s</div>",order.getUserName(),"已转单成功"));
					continue;
				}
				//order.setCreateBy(user);//异常处理，不变更订单负责人
				order.setCreateDate(date);
				if(order.getRetryTimes()<=0){
					order.setRetryTimes(1);//用retryTimes来决定转单后是否更新sd_temporder表
				}
				//save
				try {
					messageBuilder = mapper.modelToMqBuilder(order);
					if(messageBuilder != null) {
						messageBuilder.setId(idList.removeFirst());//新id
						messageBuilder.setTmpId(id);//原来id
						messageSender.send(messageBuilder.build());
					}else{
						msg.append(String.format("<div class='alert alert-error'>用户:%s %s</div>",order.getUserName(),"自动转到失败"));
					}
				}catch (Exception e) {
					msg.append(String.format("<div class='alert alert-error'>用户:%s %s</div>",order.getUserName(),e.getMessage()));
				}
			}

			if(msg.length()>0){
				result.setSuccess(false);
				result.setMessage(msg.toString());
			}else {
				result.setSuccess(true);
			}
			msg = null;
		} catch (Exception e)
		{
			result.setSuccess(false);
			result.setMessage(e.getMessage().toString());
		}
		return result;
	}

	/**
	 * 根据客户Id获取店铺(ajax)
	 * @param customerId 客户id
	 */
	@RequestMapping("getB2BShop")
	@ResponseBody
	public AjaxJsonEntity getB2BShop(Long customerId){
		User user = UserUtils.getUser();
		AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
		if(customerId==null || customerId==0){
			ajaxJsonEntity.setSuccess(false);
			ajaxJsonEntity.setMessage("缺少客户");
			return ajaxJsonEntity;
		}
		try {
             //List<Dict> list = B2BMDUtils.getCustomerKKLShopListNew(customerId);
             List<Dict> list = UserUtils.getShops(customerId,user);
			ajaxJsonEntity.setData(list);
		}catch (Exception e){
			ajaxJsonEntity.setSuccess(false);
			ajaxJsonEntity.setMessage(e.getMessage());
		}
		return ajaxJsonEntity;
	}
	//endregion
}

