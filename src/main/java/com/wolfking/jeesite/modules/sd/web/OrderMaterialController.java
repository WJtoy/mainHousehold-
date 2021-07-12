package com.wolfking.jeesite.modules.sd.web;

import cn.hutool.core.util.StrUtil;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.*;
import com.google.common.collect.Table;
import com.kkl.kklplus.entity.b2bcenter.md.B2BDataSourceEnum;
import com.kkl.kklplus.entity.common.NameValuePair;
import com.kkl.kklplus.entity.md.*;
import com.kkl.kklplus.entity.push.AppMessageType;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.config.Global;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.exception.OrderException;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.service.SequenceIdService;
import com.wolfking.jeesite.common.utils.*;
import com.wolfking.jeesite.common.utils.excel.ExportExcel;
import com.wolfking.jeesite.common.utils.excel.ImportExcel;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.entity.*;
import com.wolfking.jeesite.modules.md.service.*;
import com.wolfking.jeesite.modules.sd.entity.*;
import com.wolfking.jeesite.modules.sd.entity.viewModel.ImportMaterialItemVM;
import com.wolfking.jeesite.modules.sd.entity.viewModel.ImportMaterialVM;
import com.wolfking.jeesite.modules.sd.entity.viewModel.MaterialMasterVM;
import com.wolfking.jeesite.modules.sd.entity.viewModel.OrderMaterialSearchModel;
import com.wolfking.jeesite.modules.sd.service.OrderItemCompleteService;
import com.wolfking.jeesite.modules.sd.service.OrderMaterialReturnService;
import com.wolfking.jeesite.modules.sd.service.OrderMaterialService;
import com.wolfking.jeesite.modules.sd.service.OrderService;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.KefuTypeEnum;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.service.AreaService;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import com.wolfking.jeesite.modules.sys.utils.SeqUtils;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.entity.AppPushMessage;
import com.wolfking.jeesite.ms.providermd.service.MSCustomerNewService;
import com.wolfking.jeesite.ms.providermd.service.MSCustomerService;
import com.wolfking.jeesite.ms.providermd.service.MSEngineerAddressService;
import com.wolfking.jeesite.ms.providermd.service.MSEngineerService;
import com.wolfking.jeesite.ms.service.sys.MSUserService;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 订单配件管理
 *
 */
@Controller
@RequestMapping(value = "${adminPath}/sd/material")
@Slf4j
public class OrderMaterialController extends BaseController
{

	private static final String MODEL_ATTR_PAGE = "page";
	private static final String MODEL_ATTR_SEARCH_MODEL = "searchModel";

	//id generator
	//private static final SequenceIdUtils sequenceIdUtils = new SequenceIdUtils(ThreadLocalRandom.current().nextInt(32),ThreadLocalRandom.current().nextInt(32));
	@Autowired
	private SequenceIdService sequenceIdService;
	
	@Autowired
	private OrderService orderService;

	@Autowired
	private MaterialService materialService;

	@Autowired
	private ServiceTypeService serviceTypeService;

	@Autowired
	private CustomerMaterialService customerMaterialService;

	@Autowired
	private OrderMaterialService orderMaterialService;

	@Autowired
	private OrderMaterialReturnService orderMaterialReturnService;

	@Autowired
	private ProductService productService;

	@Autowired
	private ServicePointService servicePointService;

	@Autowired
	private RedisUtils redisUtils;

	@Autowired
	private MSEngineerAddressService engineerAddressService;

	@Autowired
	private AreaService areaService;

	@Autowired
	private EngineerService engineerService;

	@Autowired
    private MSCustomerNewService customerNewService;

	@Autowired
	private CustomerService customerService;

	@Autowired
	private MSEngineerService msEngineerService;

	@Autowired
	private OrderItemCompleteService orderItemCompleteService;

	@Autowired
	private MSUserService userService;

	@Autowired
	private MSCustomerService mSCustomerService;


	//region 管理

	//region 配件

	/**
	 * 配件申请页面
	 * 从订单详情跳转
	 *
	 * @param orderDetailId
	 */
	@RequiresPermissions(value = { "sd:order:service" })
	@RequestMapping(value = "addMaterialApply")
	public String addMaterialApply(@RequestParam(required = true) String orderDetailId, @RequestParam(required = true) String orderId
			,@RequestParam(required = true) String quarter,@RequestParam(required = true) String productId,Model model,HttpServletRequest request) {
		String viewForm = "modules/sd/material/materialApplyForm";
		OrderDetail orderdetail = new OrderDetail();
		MaterialMaster materialMaster = new MaterialMaster();
		Long lorderId = Long.valueOf(orderId);
		Long lproductId = Long.valueOf(productId);
		Long lorderDetailId = Long.valueOf(orderDetailId);
		if (lorderId == null || lorderId <= 0 || lproductId == 0 || lproductId <= 0) {
			addMessage(model, "参数无效");
			model.addAttribute("canAction", false);
			return viewForm;
		}

		User user = UserUtils.getUser();
		Order order = orderService.getOrderById(lorderId, quarter, OrderUtils.OrderDataLevel.DETAIL, true);
		if (order == null || order.getOrderCondition() == null) {
			addMessage(model, "错误：获取订单信息失败，请重试");
			model.addAttribute("canAction", false);
			return viewForm;
		}
		Product product;
		Map<Long,ServiceType> serviceTypeMap = serviceTypeService.getAllServiceTypeMap();
		ServiceType serviceType;
		// 因客户可在上门前添加配件申请，
		// 如果在上门服务申请，检查是否有上门记录
		// 如在订单项目中申请，不检查上门记录
		if (lorderDetailId != null && lorderDetailId > 0) {
			//来自上门服务的申请
			List<OrderDetail> details = order.getDetailList();
			if (details == null || details.size() == 0) {
				return gotoFail(model, viewForm, "订单无上门服务明细，请确认", false);
			}

			orderdetail = details.stream().filter(t -> Objects.equals(lorderDetailId, t.getId()))
					.findFirst().orElse(null);
			if (orderdetail == null) {
				return gotoFail(model, viewForm, "该上门服务明细不存在，请确认", false);
			}
			if (!Objects.equals(lproductId, orderdetail.getProduct().getId())) {
				return gotoFail(model, viewForm, "参数中产品与该上门服务明细产品不符，请确认", false);
			}
			product = productService.getProductByIdFromCache(lproductId);
			//品牌，型号/规格,服务类型
			product.setBrand(orderdetail.getBrand());
			product.setModel(orderdetail.getProductSpec());
			//需要质保类型，重新从缓存中读取
			serviceType = serviceTypeMap.get(orderdetail.getServiceType().getId());
			product.setServiceType(serviceType);
		} else {
			// 来自订单项的申请
			// 检查定单是否有申请配件的产品
			List<OrderItem> items = order.getItems();
			if (items == null || items.size() == 0) {
				return gotoFail(model, viewForm, "该订单下无订单项，请确认", false);
			}
			product = productService.getProductByIdFromCache(lproductId);
			OrderItem orderItem = items.stream()
					.filter(t -> Objects.equals(t.getProduct().getId(), lproductId))
					.findFirst()
					.orElse(null);
			if (orderItem == null) {
				String msg = new String("");
				if (product != null) {
					msg = String.format("该订单下无产品[%s]", product.getName());
				} else {
					msg = "该订单下无此产品";
				}
				return gotoFail(model, viewForm, msg, false);
			}
		}
		materialMaster.setQuarter(order.getQuarter());//数据库分片
		materialMaster.setOrderId(lorderId);
		materialMaster.setOrderDetailId(lorderDetailId);
		materialMaster.setProductId(lproductId);
		materialMaster.setApplyType(new Dict(MaterialMaster.APPLY_TYPE_CHANGJIA.toString(), "厂家寄发"));
		long cid = order.getOrderCondition().getCustomer().getId();
		List<Material> materials = null;
		List<CustomerMaterial> customerMaterials = null;
		//订单项单品列表,包含品牌，型号/规格，服务类型等
		Set<Product> singleProductSet = orderMaterialService.getOrderProductSet(lorderId, order.getItems(),serviceTypeMap);
		if (org.springframework.util.ObjectUtils.isEmpty(singleProductSet)) {
			return gotoFail(model, viewForm, "解析订单项单品产品列表错误", false);
		}
		//按单品读取配件列表
		List<Long> ids = singleProductSet.stream().map(Product::getId).collect(Collectors.toList());
		//产品标准配件列表
		if (B2BDataSourceEnum.VIOMI.id == order.getDataSourceId()) {
			//TODO: 云米配件必须设置yunmiCode才能显示出来
			List<NameValuePair<Long, String>> productIdAndCustomerModels = orderMaterialService.getOrderProductIdAndCustomerModels(order.getItems());
			materials = materialService.getMaterialListByProductIdListNew(order.getDataSourceId(), order.getOrderCondition().getCustomerId(), productIdAndCustomerModels);
		} else {
			materials = materialService.getMaterialListByProductIdList(ids);
		}
		//客户特殊订单配件列表
		customerMaterials = customerMaterialService.getMaterialListByProductIdList(cid, ids);
		if (null == materials || materials.size() == 0) {
			return gotoFail(model, viewForm, String.format("申请失败，服务产品：%s 没有配件信息，无法申请配件！", product.getName()), false);
		}

		CustomerMaterial customerMaterial;
		//<productid,<materialid,material>>  -> 使用guava table 替换
		Table<Long, Long, CustomerMaterial> materialTable = HashBasedTable.create();
		if (!org.springframework.util.ObjectUtils.isEmpty(customerMaterials)) {
			for (int i = 0, size = customerMaterials.size(); i < size; i++) {
				customerMaterial = customerMaterials.get(i);
				materialTable.put(customerMaterial.getProduct().getId(), customerMaterial.getMaterial().getId(), customerMaterial);
			}
		}
		Map<Long, Product> productMap = singleProductSet.stream().collect(Collectors.toMap(Product::getId, item -> item));
		List<MaterialItem> list = Lists.newArrayList();
		Material material;
		long pid;
		Product prd;
		for (int i = 0, size = materials.size(); i < size; i++) {
			material = materials.get(i);
			MaterialItem item = new MaterialItem();
			pid = material.getProduct().getId();
			if (!productMap.containsKey(pid)) {
				continue;
			}
			prd = productMap.get(pid);
			item.setProduct(prd);
			item.setQuarter(order.getQuarter());//*
			item.setMaterial(material);
			item.setQty(1);
			item.setPrice(material.getPrice());
			item.setReturnFlag(material.getIsReturn());
			//读取客户配件价格 2019/06/06 00:02
			customerMaterial = materialTable.get(material.getProduct().getId(), material.getId());
			if (customerMaterial != null) {
				item.setPrice(customerMaterial.getPrice());
				item.setReturnFlag(customerMaterial.getIsReturn());
			}
			//end
			item.setFactoryPrice(material.getPrice());// 参考价,出厂价
			//item.setRemarks(material.getPrice().toString());// 把参考价格放到Remarks里面，初始实际价格为0
			list.add(item);
		}
		materialMaster.setMateirals(list.stream().collect(Collectors.groupingBy(MaterialItem::getProduct)));
		//attachments
		String key = String.format(RedisConstant.SD_MATERIAL_ATTACHE, orderId);
		List<MaterialAttachment> attachments = attachments = redisUtils.getList(RedisConstant.RedisDBType.REDIS_TEMP_DB, key, MaterialAttachment[].class);
		if (attachments == null) {
			attachments = Lists.newArrayList();
		}
		//收货地址信息 2020-7-21
		OrderCondition orderCondition = order.getOrderCondition();
		List<MaterialReceive> materialReceives = Lists.newArrayList();
		MaterialReceive engineerReceive = new MaterialReceive();
		MDEngineerAddress engineerAddress = null;
		if(orderCondition.getEngineer()!=null && orderCondition.getEngineer().getId()!=null){
			 engineerAddress = engineerAddressService.getFromCache(orderCondition.getServicePoint().getId(),orderCondition.getEngineer().getId());
		}
		if(engineerAddress!=null && engineerAddress.getId()!=null && engineerAddress.getId()>0){
			engineerReceive.setReceiveName(engineerAddress.getUserName());
			engineerReceive.setReceivePhone(engineerAddress.getContactInfo());
			engineerReceive.setAreaId(engineerAddress.getAreaId());
			engineerReceive.setProvinceId(engineerAddress.getProvinceId());
			engineerReceive.setCityId(engineerAddress.getCityId());
			Area area = areaService.getFromCache(engineerAddress.getAreaId());
			int pos = engineerAddress.getAddress().indexOf(area.getFullName());
			String address = pos==0?engineerAddress.getAddress().substring(area.getFullName().length()).trim():engineerAddress.getAddress();
			engineerReceive.setAddress(address);
			engineerReceive.setDetailAddress(area.getFullName());
		}
		engineerReceive.setServicePointId(orderCondition.getServicePoint().getId());
		if(orderCondition.getEngineer()!=null && orderCondition.getEngineer().getId()!=null){
			engineerReceive.setEngineerId(orderCondition.getEngineer().getId());
		}
		materialReceives.add(engineerReceive);
		//用户地址
		MaterialReceive userReceive = new MaterialReceive();
		userReceive.setReceiveName(orderCondition.getUserName());
		userReceive.setReceivePhone(orderCondition.getServicePhone());
		userReceive.setAreaId(orderCondition.getArea().getId());
		//Area userArea = areaService.getTripleAreaById(orderCondition.getArea().getId());
		Area area = areaService.getFromCache(orderCondition.getArea().getId());
		if (area != null) {
			List<String> areaIds = Splitter.onPattern(",")
					.omitEmptyStrings()
					.trimResults()
					.splitToList(area.getParentIds());
			if (areaIds.size() >= 2) {
				userReceive.setCityId(Long.valueOf(areaIds.get(areaIds.size() - 1)));
				userReceive.setProvinceId(Long.valueOf(areaIds.get(areaIds.size() - 2)));
			}
			userReceive.setDetailAddress(area.getFullName());
		}
		if(orderCondition.getSubArea()!=null && orderCondition.getSubArea().getId()!=null && orderCondition.getSubArea().getId()>3){
			Area subArea = areaService.getFromCache(orderCondition.getSubArea().getId());
			if(subArea!=null){
				userReceive.setAddress(subArea.getName().concat(orderCondition.getServiceAddress()));
			}
		}else{
			userReceive.setAddress(orderCondition.getServiceAddress());
		}
		materialReceives.add(userReceive);

		String action = request.getParameter("action");
		if (StringUtils.isNoneBlank(action)) {
			model.addAttribute("action", action);
		}
		String parentIndex = request.getParameter("parentIndex");
		if (StringUtils.isNoneBlank(parentIndex)) {
			model.addAttribute("parentIndex", parentIndex);
		}
		model.addAttribute("materialMaster", materialMaster);
		model.addAttribute("attachments", attachments);
		model.addAttribute("materialReceives", materialReceives);
		model.addAttribute("canAction", true);
		return viewForm;
	}

	/**
	 * 返回失败页面
	 * @param model
	 * @param viewForm  视图完整地址
	 * @param msg       错误提示
	 * @param canAction 是否可操作
	 * @return
	 */
	private String gotoFail(Model model, String viewForm, String msg, boolean canAction) {
		addMessage(model, msg);
		model.addAttribute("canAction", false);
		return viewForm;
	}

	/**
	 * 保存配件申请(ajax提交)
	 * 使用：productGroup 属性提交选择的配件
	 * 2019-08-06 一次提交生成一个配件单号
	 */
	@RequestMapping(value = "saveMaterialApply",method = RequestMethod.POST)
	@ResponseBody
	public AjaxJsonEntity saveMaterialApply(HttpServletRequest request, HttpServletResponse response, Model model, RedirectAttributes redirectAttributes)
	{
		AjaxJsonEntity responseEntity = new AjaxJsonEntity(true);
		User user = UserUtils.getUser();
		String json = request.getParameter("materialMaster");
		if(StringUtils.isBlank(json)){
			responseEntity.setSuccess(false);
			responseEntity.setMessage("请求参数错误!");
			return responseEntity;
		}
		MaterialMaster materialMaster = GsonUtils.getInstance().fromJson(json,MaterialMaster.class);
		if(materialMaster == null){
			responseEntity.setSuccess(false);
			responseEntity.setMessage("请求参数错误!");
			return responseEntity;
		}
		Order order = null;
		Date date = new Date();
		try
		{
			if(org.springframework.util.ObjectUtils.isEmpty(materialMaster.getProductGroup())){
				responseEntity.setSuccess(false);
				responseEntity.setMessage("请至少选择一个配件!");
				return responseEntity;
			}
			if(materialMaster.getApplyType() == null || StringUtils.isBlank(materialMaster.getApplyType().getValue())){
				responseEntity.setSuccess(false);
				responseEntity.setMessage("请选择申请类型!");
				return responseEntity;
			}
			if(materialMaster.getApplyType().getIntValue()==2){
			    if(materialMaster.getReceiverAreaId()==null || materialMaster.getReceiverAreaId()<=0){
                    responseEntity.setSuccess(false);
                    responseEntity.setMessage("请选择收件地址!");
                    return responseEntity;
                }
            }
			order = orderService.getOrderById(materialMaster.getOrderId(), materialMaster.getQuarter(), OrderUtils.OrderDataLevel.DETAIL, true);
			if (order == null || order.getOrderCondition() == null) {
				responseEntity.setSuccess(false);
				responseEntity.setMessage("读取订单错误，请重试!");
				return responseEntity;
			}
			materialMaster.setDataSource(order.getDataSource().getIntValue());//2019-09-20
			String shopId = Optional.ofNullable(order.getB2bShop()).map(t->t.getShopId()).orElse(StrUtil.EMPTY);
			materialMaster.setShopId(StrUtil.trimToEmpty(shopId));//店铺id 2021/06/22
			Long formId = sequenceIdService.nextId();
			materialMaster.setId(formId);
			String no;
			no = SeqUtils.NextSequenceNo("MaterialFormNo",0,3);
			if(StringUtils.isBlank(no)){
				responseEntity.setSuccess(false);
				responseEntity.setMessage("生成配件单号错误!");
				return responseEntity;
			}
			int applyTime = orderMaterialService.getNextApplyTime(order.getId(),order.getQuarter());
			if(applyTime == 0){
				applyTime = 1;
			}
			if(materialMaster.getApplyType().getIntValue()==2){
				if(materialMaster.getReceiverProvinceId()==null || materialMaster.getReceiverProvinceId()<=0 ||
						materialMaster.getReceiverCityId()==null || materialMaster.getReceiverCityId()<=0){
					Map<Integer,Area> areas = areaService.getAllParentsWithDistrict(materialMaster.getReceiverAreaId());
					Area province = areas.getOrDefault(Area.TYPE_VALUE_PROVINCE,new Area(0L));
					Area city = areas.getOrDefault(Area.TYPE_VALUE_CITY,new Area(0L));
					materialMaster.setReceiver(StringUtils.left(materialMaster.getReceiver(),50));
					materialMaster.setReceiverProvinceId(province.getId());
					materialMaster.setReceiverCityId(city.getId());
				}
			}
			materialMaster.setApplyTime(applyTime);
			materialMaster.setMasterNo(no);
			materialMaster.setOrderNo(order.getOrderNo());
			materialMaster.setThrdNo(order.getParentBizOrderId()==null?"":order.getParentBizOrderId());
			materialMaster.setProduct(new Product(materialMaster.getProductId()));
			//截取前30个字符
			materialMaster.setOrderCreator(StringUtils.left(order.getOrderCondition().getCreateBy().getName(),30));
			OrderCondition condition = order.getOrderCondition();
			materialMaster.setCustomer(condition.getCustomer());
			materialMaster.setArea(condition.getArea());
			materialMaster.setSubArea(condition.getSubArea());
			materialMaster.setUserName(condition.getUserName());
			materialMaster.setUserPhone(condition.getServicePhone());
			materialMaster.setUserAddress(condition.getAddress());
			materialMaster.setDescription(order.getDescription());
			materialMaster.setCreateBy(user);
			materialMaster.setCreateDate(date);
			materialMaster.setKefuType(condition.getKefuType());
			// 增加突击标识,省市,品类属性
			materialMaster.setCanRush(condition.getCanRush());
			materialMaster.setProductCategoryId(condition.getProductCategoryId());
			orderMaterialService.setArea(condition,materialMaster);

			Map<String,List<MaterialItem>> mateirals = materialMaster.getProductGroup();
			Map<String,Product> products = materialMaster.getProducts();
			long cid = condition.getCustomer().getId();
			String key = String.format(RedisConstant.SD_MATERIAL_ATTACHE,materialMaster.getOrderId());
			int productSize = mateirals.size();
			//产品id列表
			Set<Long> productIdSet = Sets.newHashSetWithExpectedSize(productSize);
			//产品名称列表
			Set<String> productNameSet = Sets.newHashSetWithExpectedSize(productSize);
			Integer applyType = Integer.parseInt(materialMaster.getApplyType().getValue());
			boolean isReturn = false;
			Map<Long,Material> materialSetingMap;
			Product product;
			//配件单产品信息表
			List<MaterialProduct> materialProducts = Lists.newArrayListWithCapacity(10);
			//配件列表
			List<MaterialItem> items = Lists.newArrayListWithCapacity(20);
			List<MaterialItem> productItems;
			MaterialProduct mProduct;
			MaterialItem item;
			Long itemId;
			Material material;
			//金额
			double amount;
			//合计金额
			double totalAmount = 0.0;
			int itemNo = 0;
			for(Map.Entry<String,List<MaterialItem>> entry:mateirals.entrySet()){
				product = products.get(entry.getKey());
				if(product == null){
					throw new RuntimeException("读取产品:" + entry.getKey() + " 失败");
				}
				itemNo++;
				productIdSet.add(product.getId());
				productNameSet.add(product.getName());
				productItems = entry.getValue();
				//产品信息
				mProduct = MaterialProduct.builder()
						.id(sequenceIdService.nextId())
						.materialMasterId(formId)
						.quarter(order.getQuarter())
						.itemNo(itemNo)
						.product(product)
						.brand(product.getBrand())
						.productSpec(product.getModel())
						.serviceType(product.getServiceType())
						.warrantyType(product.getServiceType().getWarrantyStatus().getValue())
						.build();
				materialProducts.add(mProduct);
				//配件列表
				materialSetingMap = customerMaterialService.getMapFromCache(cid,product.getId());
				for (int j=0,size=productItems.size();j<size;j++){
					item = productItems.get(j);
					//check
					material = materialSetingMap.get(item.getMaterial().getId());
					if(material == null){
						throw new OrderException("读取客户配件配置错误");
					}
					item.setMaterialMasterId(formId);
					item.setMaterialProductId(mProduct.getId());
					itemId = sequenceIdService.nextId();
					item.setId(itemId);
					item.setProduct(product);
					item.setCreateBy(user);
					item.setCreateDate(date);
					item.setQuarter(order.getQuarter());
					item.setUseQty(item.getQty());
					item.setRtvQty(0);
					item.setRtvFlag(0);
					item.setReturnFlag(material.getIsReturn());
					//有一个配件返件，单头标记为：需要返件
					if(!isReturn && material.getIsReturn() == 1){
						isReturn = true;
					}
					// ?价格已哪个为准 : 已输入为准
					//item.setPrice(material.getPrice());//以客户设定为准
					if (applyType.equals(MaterialMaster.APPLY_TYPE_ZIGOU)) //自购，向师傅购买
					{
						amount = item.getPrice() * item.getQty();
						item.setTotalPrice(amount);
						totalAmount = totalAmount + amount;
					} else
					{
						//厂家寄发，都属保内，价格为0
						amount = 0.00;
						item.setPrice(0.00);
						item.setTotalPrice(0.00);
						totalAmount = totalAmount + amount;
					}
					items.add(item);
				}
			}
			//是否需要返件
			materialMaster.setReturnFlag(isReturn?1:0);
			//合计金额
			materialMaster.setTotalPrice(totalAmount);
			materialMaster.setProductIds(String.format(",%s,",Joiner.on(",").join(productIdSet)));
			materialMaster.setProductNames(String.format(",%s,",Joiner.on(",").join(productNameSet)));
			//图片
			List<MaterialAttachment> attachments = redisUtils.getList(RedisConstant.RedisDBType.REDIS_TEMP_DB,key,MaterialAttachment[].class);
			materialMaster.setAttachments(attachments);
			//产品
			materialMaster.setProductInfos(materialProducts);
			//配件
			materialMaster.setItems(items);
			orderMaterialService.ajaxAddMaterialApply(order,materialMaster);
			//Collection
			mateirals = null;
			productIdSet = null;
			productNameSet = null;
			materialSetingMap = null;
			productItems = null;
			items = null;
			products = null;
			try {
				redisUtils.remove(RedisConstant.RedisDBType.REDIS_TEMP_DB, key);//删除缓存
			}catch (Exception e){
				try{
					redisUtils.remove(RedisConstant.RedisDBType.REDIS_TEMP_DB, key);//删除缓存
				}
				catch (Exception e1){}
			}
			responseEntity.setMessage("申请配件成功，请到配件单里面查看");
		} catch (OrderException oe){
			responseEntity.setSuccess(false);
			responseEntity.setMessage(String.format("申请配件失败:%s",oe.getMessage()));
		} catch (Exception e){
			responseEntity.setSuccess(false);
			responseEntity.setMessage(String.format("申请配件失败:%s",e.getMessage()));
			log.error("orderId:{}",materialMaster.getOrderId(), e);
		}
		return responseEntity;
	}

	/**
	 * 保存配件申请的附件 放到缓存中
	 */
	@RequiresPermissions(value = "sd:order:service")
	@RequestMapping(value = "saveMaterialApplyTempAttachment")
	public String saveMaterialApplyTempAttachment(String logo, String orignalName, String orderDetailId,String orderId, String quarter,String productId,HttpServletRequest request,
												  HttpServletResponse response, Model model, RedirectAttributes redirectAttributes)
	{
		String redirectUrl = "redirect:%s/sd/material/addMaterialApply?orderId=%s&quarter=%s&orderDetailId=%s&productId=%s";
		User user = UserUtils.getUser();
		try
		{
			String key = String.format(RedisConstant.SD_MATERIAL_ATTACHE,orderDetailId);
			List<MaterialAttachment> attachments = redisUtils.getList(RedisConstant.RedisDBType.REDIS_TEMP_DB,key,MaterialAttachment[].class);
			if(attachments==null){
				attachments = Lists.newArrayList();
			}
			Long id =0l;
			if(attachments.size()>0){
				MaterialAttachment m = attachments.stream().sorted(Comparator.comparing(MaterialAttachment::getId)).findFirst().orElse(null);
				if(m != null){
					id = m.getId();
				}
			}
			id = id + 1;
			MaterialAttachment attachment = new MaterialAttachment();
			attachment.setId(id);
			attachment.setFilePath(logo);
			attachment.setRemarks(orignalName);
			attachment.setCreateBy(user);
			attachment.setCreateDate(new Date());
			attachment.setUpdateBy(user);
			attachment.setUpdateDate(new Date());
			attachments.add(attachment);
			redisUtils.setEX(RedisConstant.RedisDBType.REDIS_TEMP_DB,key,attachments,30l*60);//30分钟
		} catch (OrderException oe){
			addMessage(redirectAttributes, "上传附件失败," + oe.getMessage());
		} catch (Exception e){
			log.error("[OrderController.saveMaterialApplyTempAttachment] orderId:{}",orderId, e);
			addMessage(redirectAttributes, "上传附件失败," + e.getMessage());
		}
		return String.format(redirectUrl, Global.getAdminPath(),orderId,quarter,orderDetailId,productId);
	}

	/**
	 * ajax保存配件申请的附件 放到缓存中
	 */
	@RequiresPermissions(value = "sd:order:service")
	@ResponseBody
	@RequestMapping(value = "ajax/saveMaterialApplyTempAttachment")
	public AjaxJsonEntity saveMaterialApplyTempAttachmentAjax(String logo, String orignalName, String orderDetailId,String orderId, String productId,
															  HttpServletRequest request, HttpServletResponse response)
	{
		response.setContentType("application/json; charset=UTF-8");
		AjaxJsonEntity jsonEntity = new AjaxJsonEntity(true);
		User user = UserUtils.getUser();
		try
		{
			String key = String.format(RedisConstant.SD_MATERIAL_ATTACHE,orderId);
			List<MaterialAttachment> attachments = redisUtils.getList(RedisConstant.RedisDBType.REDIS_TEMP_DB,key,MaterialAttachment[].class);
			if(attachments==null){
				attachments = Lists.newArrayList();
			}
			Long id =0l;
			if(attachments.size()>0){
				id = attachments.stream().sorted(Comparator.comparingLong(MaterialAttachment::getId).reversed()).mapToLong(t->t.getId()).findFirst().orElse(0l);
			}
			id = id + 1;
			MaterialAttachment attachment = new MaterialAttachment();
			attachment.setId(id);
			attachment.setOrderId(Long.valueOf(orderId));
			attachment.setFilePath(logo);
			attachment.setRemarks(orignalName);
			attachment.setCreateBy(user);
			attachment.setCreateDate(new Date());
			attachment.setUpdateBy(user);
			attachment.setUpdateDate(new Date());
			attachments.add(attachment);
			redisUtils.setEX(RedisConstant.RedisDBType.REDIS_TEMP_DB,key,attachments,30l*60);//30分钟
			attachments = null;
			jsonEntity.setData(attachment);
		} catch (OrderException oe){
			jsonEntity.setSuccess(false);
			jsonEntity.setMessage("上传附件失败," + oe.getMessage());
		} catch (Exception e){
			log.error("[OrderMaterialController.saveMaterialApplyTempAttachmentAjax] orderId:{}",orderId, e);
			jsonEntity.setSuccess(false);
			jsonEntity.setMessage("上传附件失败," + e.getMessage());
		}

		return jsonEntity;
	}


	/**
	 * 删除缓存中的配件申请附件图片
	 */
	@RequiresPermissions(value = "sd:order:service")
	@ResponseBody
	@RequestMapping(value = "deleteMaterialApplyAttachment")
	public AjaxJsonEntity deleteMaterialApplyAttachment(String attachmentId, String orderDetailId,String orderId,
														HttpServletRequest request, HttpServletResponse response)
	{
		User user = UserUtils.getUser();
		response.setContentType("application/json; charset=UTF-8");
		AjaxJsonEntity jsonEntity = new AjaxJsonEntity(true);
		try
		{
			String key = String.format(RedisConstant.SD_MATERIAL_ATTACHE,orderId);
			List<MaterialAttachment> attachments = redisUtils.getList(RedisConstant.RedisDBType.REDIS_TEMP_DB,key,MaterialAttachment[].class);
			if(attachments==null || attachments.size()==0){
				jsonEntity.setMessage("附件已删除。");
				return jsonEntity;
			}
			MaterialAttachment attachment;
			for (int i=0,size =attachments.size();i<size;i++){
				attachment=attachments.get(i);
				if(Objects.equals(attachment.getId().toString(),attachmentId)){
					attachments.remove(i);
					break;
				}
			}
			redisUtils.setEX(RedisConstant.RedisDBType.REDIS_TEMP_DB,key,attachments,30l*60);//30分钟

		} catch (Exception e)
		{
			jsonEntity.setSuccess(false);
			jsonEntity.setMessage(e.getMessage());
			log.error("[OrderController.deleteMaterialApplyAttachment] orderId:{}",orderId, e);
		}
		// 更新订单信息
		return jsonEntity;
	}

	/**
	 * 配件的上传照片查看
	 */
	@RequestMapping(value = "materialMasterAttachmentForm", method = RequestMethod.GET)
	public String materialMasterAttachmentForm(@RequestParam String masterId, @RequestParam String quarter,String orderId,Model model)
	{
		Long lmasterId = Long.valueOf(masterId);
		if (lmasterId==null || lmasterId<=0)
		{
			addMessage(model, "附件查看失败,配件申请ID错误!");
			model.addAttribute("canAction",false);
		} else
		{
			List<MaterialAttachment> attachments = orderMaterialService.findAttachementsByMasterId(lmasterId,quarter);
			List<OrderItemComplete> itemCompleteList = null;
			Long lorderId = 0L;
			if(orderId!=null){
				lorderId = Long.valueOf(orderId);
			}
			if (lorderId !=null || lorderId >= 0) {
				//获取已经上传照片的数据
				itemCompleteList = orderItemCompleteService.getByOrderId(lorderId, quarter);
				if (itemCompleteList != null && itemCompleteList.size() > 0) {
					for (OrderItemComplete entity : itemCompleteList) {
						entity.setProduct(productService.getProductByIdFromCache(entity.getProduct().getId()));
						List<ProductCompletePicItem> picItemList = null;
						picItemList = OrderUtils.fromProductCompletePicItemsJson(entity.getPicJson());
						entity.setItemList(picItemList);
					}
				}
				if (itemCompleteList == null) {
					itemCompleteList = Lists.newArrayList();
				}
			}
            model.addAttribute("itemCompleteList",itemCompleteList);
			model.addAttribute("canAction",true);
			model.addAttribute("attachments", attachments);
		}
		return "modules/sd/material/materialMasterAttachmentForm";
	}

	/**
	 * 审核通过配件信息
	 * @param masterId 配件申请单id
	 * @param isMaterialReturn 返件标识 1:有返件
	 */
	@ResponseBody
	@RequestMapping(value = "materialApprove",method = RequestMethod.POST)
	public AjaxJsonEntity materialMasterApprove(MDCustomerAddress customerAddress, String masterId, String quarter, Integer isMaterialReturn, String itemIds,Integer isMaterialRecycle,String recycleItemIds,String approveRemark,HttpServletResponse response)
	{
		response.setContentType("application/json; charset=UTF-8");
		Long lmaserId = null;
		try {
			lmaserId = Long.valueOf(masterId);
		}catch (Exception e) {
			return AjaxJsonEntity.fail("申请单ID错误",null);
		}
		try {
			User user = UserUtils.getUser();
			if (lmaserId == null || lmaserId <= 0) {
				return AjaxJsonEntity.fail("申请单ID错误",null);
			}
			//MaterialMaster form = orderMaterialService.getMaterialMasterHeadById(lmaserId, quarter);
			MaterialMaster form = orderMaterialService.getMaterialMasterById(lmaserId,quarter);;
			if (form == null) {
				return AjaxJsonEntity.fail("申请单不存在，或读取失败，请重试",null);
			}
			Order order = orderService.getOrderById(form.getOrderId(), form.getQuarter(), OrderUtils.OrderDataLevel.DETAIL, true);
			if (order == null || order.getOrderCondition() == null) {
				return AjaxJsonEntity.fail("错误：系统繁忙，读取订单失败，请重试",null);
			}
			OrderCondition condition = order.getOrderCondition();
			Customer customer = condition.getCustomer();
			String returnNo = StringUtils.EMPTY;
			if(isMaterialReturn == 1){
				if(customer == null){
					return AjaxJsonEntity.fail("读取订单客户信息失败",null);
				}
				//rtnId = SeqUtils.NextIDValue(SeqUtils.TableName.SdMaterialMaster);
				returnNo = SeqUtils.NextSequenceNo("ReturnMaterialFormNo",0,3);
				if(StringUtils.isBlank(returnNo)){
					return AjaxJsonEntity.fail("生成返件单号错误,请重试！",null);
				}
			}
			//save to db
			orderMaterialService.approveMaterialApply(form,order,isMaterialReturn, returnNo,itemIds.split(","),user,customerAddress,isMaterialRecycle,recycleItemIds.split(","),approveRemark);
			//message
			if(condition.getServicePoint() != null && condition.getServicePoint().getId() != null && condition.getEngineer() != null) {
				Engineer engineer = servicePointService.getEngineerFromCache(condition.getServicePoint().getId(), condition.getEngineer().getId());
				if (isMaterialReturn == 1 && engineer != null && engineer.getAccountId() > 0) {
					String content = String.format("%s %s 工单的配件申请已通过，请注意订单配件需返厂，请及时打开APP进行查看", condition.getOrderNo(), condition.getUserName());
					PushMessageUtils.push(AppPushMessage.PassThroughType.NOTIFICATION, AppMessageType.MPASS, "", content, engineer.getAccountId());
				}
			}
		} catch (Exception e) {
			return AjaxJsonEntity.fail(e.getMessage(),null);
		}

		return AjaxJsonEntity.success("",null);
	}

	@RequestMapping(value = "materialmasterreject",method = RequestMethod.GET)
	public String materialmasterrejectForm(String masterId,String quarter, Model model, HttpServletResponse response){
		Long lmasterId = Long.valueOf(masterId);
		boolean canAction = true;
		MaterialMaster material = new MaterialMaster();
		material.setId(lmasterId);
		material.setQuarter(quarter);
		model.addAttribute("material",material);
		if (lmasterId==null || lmasterId<=0)
		{
			addMessage(model, "请求参数错误：配件申请单ID错误。请重试!");
			canAction = false;
		} else {
			MaterialMaster form = orderMaterialService.getMaterialMasterHeadById(lmasterId, quarter);
			if (form == null) {
				addMessage(model, "请求参数错误：配件申请单ID错误。请重试!");
				canAction = false;
			}else if(form.getStatus().getIntValue() > 1){
				addMessage(model, "配件申请单已审核或驳回。请再次确认!");
				canAction = false;
			}
		}
		model.addAttribute("canAction",canAction);
		return "modules/sd/material/rejectForm";
	}

	/**
	 * 驳回配件申请
	 */
	@ResponseBody
	@RequestMapping(value = "materialmasterreject",method = RequestMethod.POST)
	public AjaxJsonEntity materialMasterReject(MaterialMaster material, Model model, HttpServletResponse response)
	{
		response.setContentType("application/json; charset=UTF-8");
		AjaxJsonEntity jsonEntity = new AjaxJsonEntity(true);
		if(Objects.isNull(material)){
			jsonEntity.setSuccess(false);
			jsonEntity.setMessage("提交的内容错误");
			return jsonEntity;
		}
		long materialId = Optional.ofNullable(material).map(t->t.getId()).orElse(0L);
		if (materialId <= 0) {
			jsonEntity.setSuccess(false);
			jsonEntity.setMessage("申请单ID错误");
			return jsonEntity;
		}
		if(StrUtil.isBlank(material.getCloseType())){
			jsonEntity.setSuccess(false);
			jsonEntity.setMessage("请选择驳回原因。");
			return jsonEntity;
		}
		try
		{
			User user = UserUtils.getUser();
			// 读取配件单单头
			MaterialMaster form = orderMaterialService.getMaterialMasterHeadById(materialId, material.getQuarter());
			if (form == null) {
				jsonEntity.setSuccess(false);
				jsonEntity.setMessage("申请单不存在，或读取失败，请重试");
				return jsonEntity;
			}
			if (form.getStatus().getIntValue() > 1) {
				jsonEntity.setSuccess(false);
				jsonEntity.setMessage("该配件申请已审核或驳回，请重新确认单据状态");
				return jsonEntity;
			}
			if(StrUtil.isBlank(material.getCloseType())){
				jsonEntity.setSuccess(false);
				jsonEntity.setMessage("未选择驳回原因，请选择。");
				return jsonEntity;
			}
			Dict rejectType = MSDictUtils.getDictByValue(StrUtil.trim(material.getCloseType()),"material_reject_type");
			if(rejectType == null){
				jsonEntity.setSuccess(false);
				jsonEntity.setMessage("驳回原因错误，请确认。");
				return jsonEntity;
			}
			material.setUpdateBy(user);
			Order order = orderService.getOrderById(form.getOrderId(), form.getQuarter(), OrderUtils.OrderDataLevel.CONDITION, true);
			if (order == null || order.getOrderCondition() == null) {
				jsonEntity.setSuccess(false);
				jsonEntity.setMessage("错误：系统繁忙，读取订单失败，请重试");
				return jsonEntity;
			}
			//save to db
			orderMaterialService.rejectMaterialApply(material,order,rejectType.getLabel());
			//push message
			OrderCondition condition = order.getOrderCondition();
			if(condition.getServicePoint() != null && condition.getEngineer() != null) {
				Engineer engineer = servicePointService.getEngineerFromCache(condition.getServicePoint().getId(), condition.getEngineer().getId());
				if (engineer != null && engineer.getAccountId() > 0) {
					String content = String.format("%s %s 工单的配件申请被厂家或客服驳回，请及时打开APP进行查看", condition.getOrderNo(), condition.getUserName());
					PushMessageUtils.push(AppPushMessage.PassThroughType.NOTIFICATION, AppMessageType.MREJECT, "", content, engineer.getAccountId());
				}
			}
			jsonEntity.setMessage("保存成功");

		} catch (Exception e)
		{
			jsonEntity.setSuccess(false);
			jsonEntity.setMessage(e.getMessage());
		}

		return jsonEntity;
	}

	/**
	 * 配件申请列表
	 *
	 * @param orderId
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "list", method = RequestMethod.GET)
	public String materialList(@RequestParam String orderId, @RequestParam String orderNo, String quarter , Model model)
	{
		User user = UserUtils.getUser();
		List<MaterialMaster> materials = Lists.newArrayList();
		List<MaterialMasterVM> forms = null;
		Long lorderId = Long.valueOf(orderId);
        //获取客户返件地址
        MaterialReceive materialReceive = new MaterialReceive();
        Long customerId = 0L;
		if (lorderId==null || lorderId<=0)
		{
			addMessage(model, "配件列表查看失败，订单参数错误");
		}else {
			materials = orderMaterialService.findMaterialMastersByOrderIdMS(lorderId, quarter);
			MaterialMaster materialMaster;
			if(materials !=null && materials.size()>0) {
				materialMaster = materials.get(0);
				String strQuarter = materialMaster.getQuarter();
				model.addAttribute("quarter", strQuarter);
				boolean waitingB2BCommand = false;
				if (orderMaterialService.isOpenB2BMaterialSource(materialMaster.getDataSource())){
					waitingB2BCommand = true;
				}
				forms = orderMaterialService.materialMasterListToVMList(materials,waitingB2BCommand);
                MDCustomerAddress customerAddress = customerNewService.getByCustomerIdAndTypeFromCache(materialMaster.getCustomer().getId(),MDCustomerEnum.CustomerAddressType.RETURNADDR.getValue());
                if(customerAddress!=null && customerAddress.getId()!=null && customerAddress.getId()>0){
                    materialReceive.setAreaId(customerAddress.getAreaId());
					materialReceive.setProvinceId(customerAddress.getProvinceId());
					materialReceive.setCityId(customerAddress.getCityId());
                    Area area = areaService.getFromCache(customerAddress.getAreaId());
                    if(area!=null){
                        materialReceive.setDetailAddress(area.getFullName());
                    }
                    materialReceive.setReceiveName(customerAddress.getUserName());
                    materialReceive.setReceivePhone(customerAddress.getContactInfo());
                    materialReceive.setAddress(customerAddress.getAddress());
                }
                customerId = materialMaster.getCustomer().getId();
			}
		}
		for(MaterialMasterVM materialMasterVM:forms){
			int maxRow = 0;
			for(MaterialMasterVM.MaterialSubForm materialSubForm:materialMasterVM.getSubForms()){
				maxRow = maxRow + materialSubForm.getMaterials().size();
			}
			materialMasterVM.setMaxRow(maxRow);
		}
		model.addAttribute("materialReceive",materialReceive);
		model.addAttribute("orderId", orderId);
		model.addAttribute("orderNo", orderNo);
		model.addAttribute("materials", forms);
        model.addAttribute("customerId", customerId);
		return "modules/sd/material/materialMasterApproveList";
	}

	/**
	 * 配件申请界面-填写物流信息
	 * 2019-11-01 增加b2b配件单判断逻辑，并记录错误日志
	 *
	 * @param materialMasterId 配件申请单id
	 */
	@RequestMapping(value = "expressForm", method = RequestMethod.GET)
	public String expressForm(@RequestParam String materialMasterId,@RequestParam String quarter, Model model)
	{
		String viewForm = "modules/sd/material/materialMasterForm";
		if (StringUtils.isBlank(materialMasterId)) {
			addMessage(model, "配件申请修改失败，配件申请ID错误.");
			model.addAttribute("canAction", false);
			return viewForm;
		}

		Long lmaterialMasterId = Long.valueOf(materialMasterId);
		if (lmaterialMasterId == null || lmaterialMasterId <= 0) {
			addMessage(model, "配件申请修改失败，配件申请ID错误.");
			model.addAttribute("canAction", false);
			return viewForm;
		}

		User user = UserUtils.getUser();
		MaterialMaster master = orderMaterialService.getMaterialMasterById(lmaterialMasterId,quarter);
		if(master == null){
            model.addAttribute("materialMaster", master);
            model.addAttribute("canAction", true);
            return viewForm;
        }
		//status
        Integer status = Optional.ofNullable(master.getStatus())
                .map(s->s.getIntValue())
                .orElse(0);
        if(status > MaterialMaster.STATUS_APPROVED){
            addMessage(model, "此配件已处理，已发货或完成，请确认.");
            model.addAttribute("canAction", false);
            return viewForm;
        }
		//b2b 配件单(向厂家申请)判断
        if (MaterialMaster.APPLY_TYPE_CHANGJIA.equals(master.getApplyType().getIntValue()) && orderMaterialService.isOpenB2BMaterialSource(master.getDataSource()) && master.getDataSource()== B2BDataSourceEnum.JOYOUNG.getId()){
            log.error("客服不应进入此配件发货页面,master id:{},user:{}",lmaterialMasterId,user==null?0L:user.getId());
            addMessage(model, "此配件已上传商家系统，请等待商家系统处理.");
            model.addAttribute("canAction", false);
            return viewForm;
        }
		if(!org.springframework.util.ObjectUtils.isEmpty(master.getItems())) {
			//items -> mateirals 分组显示
			Map<Product,List<MaterialItem>> materialMap =master.getItems().stream().collect(Collectors.groupingBy(MaterialItem::getProduct));
			Product p;
			for(Map.Entry<Product,List<MaterialItem>> entry:materialMap.entrySet()){
				p = productService.getProductByIdFromCache(entry.getKey().getId());
				if(p != null) {
					entry.getKey().setName(p.getName());
				}
			}
			master.setMateirals(materialMap);
		}
		model.addAttribute("materialMaster", master);
		model.addAttribute("canAction", true);
		return viewForm;
	}

	/**
	 * 保存配件-物流信息
	 *
	 * @param materialMaster
	 */
	@ResponseBody
	@RequestMapping(value = "saveExpress")
	public AjaxJsonEntity saveExpress(MaterialMaster materialMaster, Model model, HttpServletResponse response)
	{
		response.setContentType("application/json; charset=UTF-8");
		AjaxJsonEntity jsonEntity = new AjaxJsonEntity(true);
		try
		{
			User user = UserUtils.getUser();
			jsonEntity.setSuccess(true);
			materialMaster.setUpdateBy(user);
			materialMaster.setUpdateDate(new Date());
			Order order = orderService.getOrderById(materialMaster.getOrderId(), materialMaster.getQuarter(), OrderUtils.OrderDataLevel.CONDITION, true);
			if (order == null || order.getOrderCondition() == null) {
				jsonEntity.setSuccess(false);
				jsonEntity.setMessage("读取配件单的订单信息失败");
				return jsonEntity;
			}

			Product product = productService.getProductByIdFromCache(materialMaster.getProduct().getId());
			if(product != null){
				materialMaster.setProduct(product);
			}
			//save to db
			orderMaterialService.updateMaterialApplyExpress(materialMaster,order);
			//message
			try {
				OrderCondition condition = order.getOrderCondition();
				Engineer engineer = servicePointService.getEngineerFromCache(condition.getServicePoint().getId(), condition.getEngineer().getId());
				if (engineer != null && engineer.getAccountId() > 0) {
					String content = String.format("%s %s 工单申请的配件已发货，请及时打开APP进行查看", condition.getOrderNo(), condition.getUserName());
					PushMessageUtils.push(AppPushMessage.PassThroughType.NOTIFICATION, AppMessageType.MDELIVER, "", content, engineer.getAccountId());
				}
			}
			catch (Exception e){
				LogUtils.saveLog("配件发货APP推送失败","OrderController.editMaterialMaster",materialMaster.getOrderId().toString(),e,user);
			}
			jsonEntity.setMessage("保存成功");
		} catch (Exception e)
		{
			jsonEntity.setSuccess(false);
			jsonEntity.setMessage(e.getMessage());
		}

		return jsonEntity;
	}


	/**
	 * 返件-保存配件申请的附件
	 * 保存数据库同时，缓存中也保存
	 *
	 */
	@RequestMapping(value = "/return/saveTempAttachment")
	public String saveReturnTempAttachment(MaterialReturn materialReturn, String logo, String orignalName, Model model)
	{
		String viewForm = "modules/sd/material/return/materialReturnForm";
		User user = UserUtils.getUser();
		List<MaterialAttachment> attachments;
		try
		{
			materialReturn = orderMaterialReturnService.getMaterialReturnById(materialReturn.getId(),null,materialReturn.getQuarter(),0,0,1,0);
			String key = String.format(RedisConstant.SD_RETURN_MATERIAL_ATTACHE,materialReturn.getId());
			attachments = redisUtils.getList(RedisConstant.RedisDBType.REDIS_TEMP_DB,key,MaterialAttachment[].class);
			if(attachments==null){
				attachments = Lists.newArrayList();
			}
			materialReturn.setAttachments(attachments);
			materialReturn.setExpressNo("");//没提交前不赋值，否则页面控制出现问题，无法保存
			//save
			MaterialAttachment attachment = new MaterialAttachment();
			attachment.setOrderId(materialReturn.getOrderId());
			attachment.setQuarter(materialReturn.getQuarter());
			attachment.setFilePath(logo);
			attachment.setRemarks(orignalName);
			attachment.setCreateBy(user);
			attachment.setCreateDate(new Date());
			attachment.setUpdateBy(user);
			attachment.setUpdateDate(new Date());
			orderMaterialService.insertMaterialMasterAttach(materialReturn.getId(),attachment);
			//cache
			attachments.add(attachment);
			redisUtils.setEX(RedisConstant.RedisDBType.REDIS_TEMP_DB,key,attachments,10l*60);//10分钟
		} catch (OrderException oe){
			addMessage(model, "上传附件失败" + oe.getMessage());
		} catch (Exception e){
			log.error("[saveReturnTempAttachment]  fileName:{}",orignalName, e);
			addMessage(model, "上传附件失败" + e.getMessage());
		}
		model.addAttribute("canAction",true);
		model.addAttribute("materialReturn", materialReturn);
		return viewForm;
	}

	/**
	 * 返件-保存配件申请的附件
	 * 保存数据库同时，缓存中也保存
	 *
	 */
	@RequestMapping(value = "ajax/return/saveAjaxReturnTempAttachment")
	@ResponseBody
	public AjaxJsonEntity saveAjaxReturnTempAttachment(MaterialReturn materialReturn, String logo, String orignalName)
	{
		//String viewForm = "modules/sd/material/return/materialReturnForm";
		AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
		User user = UserUtils.getUser();
		List<MaterialAttachment> attachments;
		try
		{
			materialReturn = orderMaterialReturnService.getMaterialReturnById(materialReturn.getId(),null,materialReturn.getQuarter(),0,0,1,0);
			String key = String.format(RedisConstant.SD_RETURN_MATERIAL_ATTACHE,materialReturn.getId());
			attachments = redisUtils.getList(RedisConstant.RedisDBType.REDIS_TEMP_DB,key,MaterialAttachment[].class);
			if(attachments==null){
				attachments = Lists.newArrayList();
			}
			materialReturn.setAttachments(attachments);
			materialReturn.setExpressNo("");//没提交前不赋值，否则页面控制出现问题，无法保存
			//save
			MaterialAttachment attachment = new MaterialAttachment();
			attachment.setOrderId(materialReturn.getOrderId());
			attachment.setQuarter(materialReturn.getQuarter());
			attachment.setFilePath(logo);
			attachment.setRemarks(orignalName);
			attachment.setCreateBy(user);
			attachment.setCreateDate(new Date());
			attachment.setUpdateBy(user);
			attachment.setUpdateDate(new Date());
			orderMaterialService.insertMaterialMasterAttach(materialReturn.getId(),attachment);
			//cache
			attachments.add(attachment);
			redisUtils.setEX(RedisConstant.RedisDBType.REDIS_TEMP_DB,key,attachments,10l*60);//10分钟
			ajaxJsonEntity.setData(attachment);
		} catch (OrderException oe){
			//addMessage(model, "上传附件失败" + oe.getMessage());
			ajaxJsonEntity.setSuccess(false);
			ajaxJsonEntity.setMessage("上传附件失败"+oe.getMessage());
		} catch (Exception e){
			//log.error("[saveReturnTempAttachment]  fileName:{}",orignalName, e);
			//addMessage(model, "上传附件失败" + e.getMessage());
			ajaxJsonEntity.setSuccess(false);
			ajaxJsonEntity.setMessage("上传附件失败"+e.getMessage());
		}
		return ajaxJsonEntity;
	}


	/**
	 * 返件-删除返件附件
	 *
	 */
	@RequestMapping(value = "/return/deleteReturnMaterialAttachment")
	@ResponseBody
	public AjaxJsonEntity saveAjaxReturnTempAttachment(@RequestParam String attachmentId,@RequestParam String returnMaterialId,@RequestParam String quarter)
	{
		AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
		User user = UserUtils.getUser();
		Date date = new Date();
		try
		{
			Long lAttachmentId = Long.valueOf(attachmentId);
			if(lAttachmentId==null || lAttachmentId<=0){
				throw new RuntimeException("缺少返件附件Id,请重试");
			}
			Long lReturnMaterialId = Long.valueOf(returnMaterialId);
			if(lReturnMaterialId ==null || lReturnMaterialId<=0){
				throw new RuntimeException("缺少返件单Id,请重试");
			}
			orderMaterialReturnService.deleteReturnAttachment(lAttachmentId,quarter,date,user.getId());
            List<MaterialAttachment> materialAttachments = orderMaterialReturnService.findAttachementsByReturnId(lReturnMaterialId,quarter);
			String key = String.format(RedisConstant.SD_RETURN_MATERIAL_ATTACHE,lReturnMaterialId);
			redisUtils.setEX(RedisConstant.RedisDBType.REDIS_TEMP_DB,key,materialAttachments,10l*60);//10分钟
		} catch (OrderException oe){
			//addMessage(model, "上传附件失败" + oe.getMessage());
			ajaxJsonEntity.setSuccess(false);
			ajaxJsonEntity.setMessage("删除附件失败"+oe.getMessage());
		} catch (Exception e){
			//log.error("[saveReturnTempAttachment]  fileName:{}",orignalName, e);
			//addMessage(model, "上传附件失败" + e.getMessage());
			ajaxJsonEntity.setSuccess(false);
			ajaxJsonEntity.setMessage("删除附件失败"+e.getMessage());
		}
		return ajaxJsonEntity;
	}



	/**
	 * 保存配件申请

	 @RequestMapping(value = "saveMaterialApply")
	 public String saveMaterialApply(MaterialMaster materialMaster, HttpServletRequest request, HttpServletResponse response, Model model, RedirectAttributes redirectAttributes)
	 {
	 String redirectUrl = "redirect:%s/sd/order/addMaterialApply?orderId=%s&quarter=%s&orderDetailId=%s&productId=%s&action=added&parentIndex=%s";
	 User user = UserUtils.getUser();
	 String parentIndex = request.getParameter("parentIndex");
	 if(StringUtils.isBlank(parentIndex)){
	 parentIndex = "";
	 }
	 Order order = null;
	 Date date = new Date();
	 try
	 {
	 order = orderService.getOrderById(materialMaster.getOrderId(), materialMaster.getQuarter(), OrderUtils.OrderDataLevel.CONDITION, true);
	 if (order == null || order.getOrderCondition() == null) {
	 addMessage(model, "读取订单错误，请重试!");
	 return String.format(redirectUrl,Global.getAdminPath(),materialMaster.getOrderId(),materialMaster.getQuarter(),materialMaster.getOrderDetailId(),materialMaster.getProductId(),parentIndex);
	 }
	 //2019-06-03
	 materialMaster.setOrderNo(order.getOrderNo());
	 OrderCondition condition = order.getOrderCondition();
	 materialMaster.setCustomer(condition.getCustomer());
	 materialMaster.setArea(condition.getArea());
	 materialMaster.setSubArea(condition.getSubArea());
	 materialMaster.setUserName(condition.getUserName());
	 materialMaster.setUserPhone(condition.getServicePhone());
	 materialMaster.setUserAddress(condition.getAddress());
	 //end
	 int i = 0;
	 Integer applyType = Integer.parseInt(materialMaster.getApplyType().getValue());
	 Material material;
	 long cid = condition.getCustomer().getId();
	 long pid = materialMaster.getProduct().getId();
	 long mid;
	 Double p = 0.0;
	 for (MaterialItem item : materialMaster.getItems())
	 {
	 if (item.isChooseFlag())// 勾选了此项配件
	 {
	 item.setCreateBy(user);
	 item.setCreateDate(date);
	 item.setQuarter(order.getQuarter());
	 //2019-06-03
	 item.setUseQty(item.getQty());
	 item.setRtvQty(0);
	 item.setRtvFlag(0);
	 material = customerMaterialService.getMaterialInfoOfCustomer(cid,pid,item.getMaterial().getId());
	 if(material == null){
	 throw new OrderException("读取客户配件配置错误");
	 }
	 item.setReturnFlag(material.getIsReturn());
	 //有一个配件返件，单头标记为：需要返件
	 if(material.getIsReturn() == 1){
	 materialMaster.setReturnFlag(material.getIsReturn());
	 }
	 // ?价格已哪个为准 : 已输入为准
	 //item.setPrice(material.getPrice());//以客户设定为准
	 //end
	 if (applyType == MaterialMaster.APPLY_TYPE_ZIGOU) //自购，向师傅购买
	 {
	 p = item.getPrice() * item.getQty();
	 item.setTotalPrice(p);
	 materialMaster.setTotalPrice(materialMaster.getTotalPrice() + p);
	 } else
	 {
	 //厂家寄发，都属保内，价格为0
	 item.setPrice(0.00);
	 item.setTotalPrice(0.00);
	 }
	 i++;
	 }
	 }

	 if (i == 0)
	 {
	 addMessage(model, "请至少选择一个配件!");
	 } else
	 {
	 materialMaster.setCreateBy(user);
	 materialMaster.setCreateDate(date);
	 String key = String.format(RedisConstant.SD_MATERIAL_ATTACHE,materialMaster.getOrderId());
	 List<MaterialAttachment> attachments = attachments = redisUtils.getList(RedisConstant.RedisDBType.REDIS_TEMP_DB,key,MaterialAttachment[].class);
	 materialMaster.setAttachments(attachments);
	 orderMaterialService.addMaterialApply(materialMaster);
	 try {
	 redisUtils.remove(RedisConstant.RedisDBType.REDIS_TEMP_DB, key);//删除缓存
	 }catch (Exception e){
	 try{
	 redisUtils.remove(RedisConstant.RedisDBType.REDIS_TEMP_DB, key);//删除缓存
	 }
	 catch (Exception e1){}
	 }
	 addMessage(redirectAttributes, "申请配件成功，请到配件单里面查看");
	 }
	 return String.format(redirectUrl,Global.getAdminPath(),materialMaster.getOrderId(),materialMaster.getQuarter(),materialMaster.getOrderDetailId(),materialMaster.getProductId(),parentIndex);
	 } catch (OrderException oe){
	 addMessage(redirectAttributes, String.format("申请配件失败:%s",oe.getMessage()));
	 } catch (Exception e){
	 log.error("[OrderController.saveMaterialApply] orderId:{}",materialMaster.getOrderId(), e);
	 addMessage(redirectAttributes, String.format("申请配件失败:%s",e.getMessage()));
	 }
	 return String.format(redirectUrl,Global.getAdminPath(),materialMaster.getOrderId(),materialMaster.getQuarter(),materialMaster.getOrderDetailId(),materialMaster.getProductId(),parentIndex);
	 }

	 */

	//endregion

	//region 返件

	/**
	 * 返件查看
	 *
	 * @param materialMasterId 配件单id
	 */
	@RequestMapping(value = "/return/form", method = RequestMethod.GET)
	public String materialReturnform(@RequestParam String materialMasterId,@RequestParam String quarter ,String formNo,Model model) {
		String viewForm = "modules/sd/material/return/materialReturnForm";
		Long lmaterialMasterId = null;
		try {
			lmaterialMasterId = Long.valueOf(materialMasterId);
		}catch (Exception e){
			addMessage(model, "配件申请修改失败，配件申请ID不合法.");
			model.addAttribute("canAction", false);
			return viewForm;
		}
		if (lmaterialMasterId <= 0) {
			addMessage(model, "配件申请修改失败，配件申请ID错误.");
			model.addAttribute("canAction", false);
			return viewForm;
		}
		//MaterialMaster materialReturn = orderMaterialService.getReturnMaterialMasterById(null,lmaterialMasterId,quarter);// 返件独立存取
		//要返回返件地址等信息
		MaterialReturn materialReturn = orderMaterialReturnService.getMaterialReturnById(null, lmaterialMasterId, quarter, 1, 0, 1, 0);
		if(materialReturn == null){
			addMessage(model,"读取返件单失败，请重试!");
			model.addAttribute("canAction", false);
			return viewForm;
		}
		Long customerId = 0L;
		if(materialReturn.getCustomer()!=null && materialReturn.getCustomer().getId()!=null){
			customerId = materialReturn.getCustomer().getId();
		}
		if(materialReturn.getStatus().getIntValue()==2 && StringUtils.isBlank(materialReturn.getReceivorAddress())){
			MDCustomerAddress customerAddress = customerNewService.getByCustomerIdAndTypeFromCache(customerId,MDCustomerEnum.CustomerAddressType.RETURNADDR.getValue());
			if(customerAddress!=null && customerAddress.getId()!=null && customerAddress.getId()>0){
				materialReturn.setReceiverAreaId(customerAddress.getAreaId());
				materialReturn.setReceiverCityId(customerAddress.getCityId());
				materialReturn.setReceiverProvinceId(customerAddress.getProvinceId());
				materialReturn.setReceivorAddress(customerAddress.getAddress());
				if(StringUtils.isBlank(materialReturn.getReceivor())){
					materialReturn.setReceivor(customerAddress.getUserName());
				}
				if(StringUtils.isBlank(materialReturn.getReceivorPhone())){
					materialReturn.setReceivorPhone(customerAddress.getContactInfo());
				}
			}
		}
		String receiverAreaName = "";
		if(materialReturn.getReceiverAreaId()!=null && materialReturn.getReceiverAreaId()>0){
			Area area = areaService.getFromCache(materialReturn.getReceiverAreaId());
			if(area!=null){
				receiverAreaName = area.getFullName();
			}
		}
		List<MaterialAttachment> attachments = attachments = materialReturn.getAttachments();
		String key = String.format(RedisConstant.SD_RETURN_MATERIAL_ATTACHE, materialReturn.getId());
		if (attachments == null || attachments.size() > 0) {
			if (!redisUtils.exists(RedisConstant.RedisDBType.REDIS_TEMP_DB, key)) {
				redisUtils.setEX(RedisConstant.RedisDBType.REDIS_TEMP_DB, key, attachments, 10l * 60);//10分钟
			}
		}
		model.addAttribute("receiverAreaName", receiverAreaName);
		model.addAttribute("customerId", customerId);
		model.addAttribute("materialReturn", materialReturn);
		model.addAttribute("formNo", formNo);
		model.addAttribute("canAction", true);
		return viewForm;
	}

	/**
	 * 保存返件物流信息
	 *
	 * @param materialReturn
	 */
	@ResponseBody
	@RequestMapping(value = "/return/saveExpress")
	public AjaxJsonEntity saveReturnExpress(MaterialReturn materialReturn, Model model, HttpServletResponse response)
	{
		response.setContentType("application/json; charset=UTF-8");
		AjaxJsonEntity jsonEntity = new AjaxJsonEntity(true);
		try
		{
			User user = UserUtils.getUser();
			materialReturn.setUpdateBy(user);
			materialReturn.setUpdateDate(new Date());
			orderMaterialReturnService.updateMaterialReturnExpressInfo(null,materialReturn);

			jsonEntity.setMessage("保存成功");
		} catch (Exception e) {
			jsonEntity.setSuccess(false);
			jsonEntity.setMessage(e.getMessage());
		}

		return jsonEntity;
	}

	//endregion

	//endregion 管理

	//region 客服列表

	//region 公共
	/**
	 * 设置及初始化查询条件
	 * @param user  当前帐号
	 * @param searchModel   查询条件
	 * @param initMonths    初始最小查询时间段(月)
	 * @param byApplyDateRange by申请日期查询开关
	 * @param maxOrderDays   下单最大查询范围(天)
	 */
	private OrderMaterialSearchModel setSearchModel(User user, OrderMaterialSearchModel searchModel, Model model, Page<MaterialMaster> page,int initMonths, boolean byApplyDateRange , int maxOrderDays) {
		if (searchModel == null) {
			searchModel = new OrderMaterialSearchModel();
		}

		if (searchModel.getStatus() == null || searchModel.getStatus() == 0) {
			searchModel.setStatus(MaterialMaster.STATUS_NEW);
		}

		Date now = new Date();
		//申请日期
		if(byApplyDateRange) {
			if (searchModel.getBeginDate() == null) {
				searchModel.setEndDate(DateUtils.getDateEnd(new Date()));
				searchModel.setBeginDate(DateUtils.getStartDayOfMonth(DateUtils.addMonth(new Date(), 0 - initMonths)));
			} else {
				searchModel.setEndDate(DateUtils.getDateEnd(searchModel.getEndDate()));
			}
			//检查最大时间范围
			if(maxOrderDays > 0){
				Date maxDate = DateUtils.addDays(searchModel.getEndDate(),1-maxOrderDays);
				maxDate = DateUtils.getDateStart(maxDate);
				if(DateUtils.pastDays(searchModel.getBeginDate(),maxDate)>0){
					searchModel.setBeginDate(maxDate);
				}
				/*
				Date maxDate = DateUtils.addDays(searchModel.getBeginDate(),maxOrderDays-1);
				maxDate = DateUtils.getDateEnd(maxDate);
				if(DateUtils.pastDays(maxDate,searchModel.getEndDate())>0){
					searchModel.setEndDate(maxDate);
				}*/
			}
		}
		//if (!request.getMethod().equalsIgnoreCase("post")) {
		//	searchModel.setMaterialType(1);//配件申请
		//}
		Boolean isValide = checkOrderNoAndPhone(searchModel,model,page);
		if(!isValide){
			searchModel.setValid(false);
		}
		String checkRegion = orderMaterialService.loadAndCheckUserRegions(searchModel,user);
		if(org.apache.commons.lang3.StringUtils.isNotEmpty(checkRegion)){
			addMessage(model, checkRegion);
			searchModel.setValid(false);
			return searchModel;
		}
		//客服主管
		boolean isServiceSupervisor = user.getRoleEnNames().contains("Customer service supervisor");

		//vip客服查询自己负责的单，by客户+区域+品类
		//1.by 客户，前端客户已按客服筛选了
		if(user.isKefu()) {
			if (user.getSubFlag() == KefuTypeEnum.VIPKefu.getCode()) {
				//vip客服查询自己负责的单，by客户+区域+品类
				searchModel.setCustomerType(1);//指派客户，关联sys_user_customer
				searchModel.setRushType(null);//忽略突击区域
			} else if (user.getSubFlag() == KefuTypeEnum.Kefu.getCode()) {
				//普通客服
				searchModel.setCustomerType(0);//不能查询vip客户订单
				searchModel.setRushType(0);//不能查看突击区域订单
			} else if (user.getSubFlag() == KefuTypeEnum.Rush.getCode()){
				//突击客服，只看自己负责的单
				searchModel.setCustomerType(null);//查询vip客户订单
				searchModel.setRushType(1);//查看突击区域订单
			}else if(user.getSubFlag() == KefuTypeEnum.AutomaticKefu.getCode()){
				searchModel.setCustomerType(null); //查询所有客户的工单
				searchModel.setRushType(0);
			}else if(user.getSubFlag() == KefuTypeEnum.COMMON_KEFU.getCode()){
				searchModel.setCustomerType(null); //查询所有客户的工单
				searchModel.setRushType(0);
			}else {
				//超级客服，查询所有客户订单，包含Vip客户
				searchModel.setCustomerType(null); //可查看Vip客户订单
				searchModel.setRushType(null);//可查看突击区域订单
			}
		}else{
			//其他类型帐号，不限制客户及突击区域订单
			searchModel.setCustomerType(null);
			searchModel.setRushType(null);
		}
		//2.by 区域
		if (isServiceSupervisor) {
			searchModel.setCreateBy(user);//*
		} else if (user.isKefu()) {
			searchModel.setCreateBy(user);//*,只有客服才按帐号筛选
		} else if (user.isInnerAccount()) { //内部帐号
			searchModel.setCreateBy(user);//*
		}
		return searchModel;
	}

	/**
	 * 检查订单号，手机号输入
	 * @param searchModel
	 * @param model
	 * @return
	 */
	private Boolean checkOrderNoAndPhone(OrderMaterialSearchModel searchModel, Model model, Page page){
		if(searchModel == null){
			return true;
		}
		//检查电话
		if (StringUtils.isNotBlank(searchModel.getOrderNo())){
			int orderSerchType = searchModel.getOrderNoSearchType();
			if(orderSerchType != 1) {
				addMessage(model, "错误：请输入正确的工单号");
				model.addAttribute("canAction", true);
				model.addAttribute("page", page);
				model.addAttribute("searchModel", searchModel);
				return false;
			}else{
				//检查分片
				try {
					Date goLiveDate = OrderUtils.getGoLiveDate();
					String[] quarters = DateUtils.getQuarterRange(goLiveDate, new Date());
					if(quarters.length == 2) {
						int start = StringUtils.toInteger(quarters[0]);
						int end = StringUtils.toInteger(quarters[1]);
						if(start>0 && end > 0){
							int quarter = StringUtils.toInteger(searchModel.getQuarter());
							if(quarter < start || quarter > end){
								addMessage(model, "错误：请输入正确的工单号,日期超出范围");
								model.addAttribute("canAction", true);
								model.addAttribute("page", page);
								model.addAttribute("searchModel", searchModel);
								return false;
							}
						}
					}
				}catch (Exception e){
					log.error("检查分片错误,orderNo:{}",searchModel.getOrderNo(),e);
				}
			}
		}
		if (StringUtils.isNotBlank(searchModel.getUserPhone())){
			if(searchModel.getIsPhone() != 1){
				addMessage(model, "错误：请输入正确的用户电话");
				model.addAttribute("canAction", true);
				model.addAttribute("page", page);
				model.addAttribute("searchModel", searchModel);
				return false;
			}
		}
		return true;
	}
	//endregion 公共

	//region 待审核

	/**
	 * 待审核配件列表
	 */
	@RequiresPermissions(value = { "sd:order:service" })
	@RequestMapping(value = "/kefu/applylist")
	public String kefuApplyList(OrderMaterialSearchModel searchModel, HttpServletRequest request, HttpServletResponse response, Model model) {
		String viewForm = "modules/sd/material/kefu/applyList";
		Page<MaterialMaster> page = new Page<>();
		User user = UserUtils.getUser();
		searchModel = setSearchModel(user,searchModel,model,page,3,true,365);
		if(!searchModel.getValid()){
			model.addAttribute(MODEL_ATTR_PAGE, page);
			model.addAttribute(MODEL_ATTR_SEARCH_MODEL, searchModel);
			return viewForm;
		}
		//状态
		searchModel.setStatus(MaterialMaster.STATUS_NEW);
		if (!request.getMethod().equalsIgnoreCase("post")) {
			searchModel.setMaterialType(1);//配件申请
		}
		try {
			//查询
			page = orderMaterialService.findKefuMaterialList(new Page<OrderMaterialSearchModel>(request, response), searchModel);
		} catch (Exception e) {
			addMessage(model, "查询错误：" + e.getMessage());
		}
		model.addAttribute("page", page);
		model.addAttribute("searchModel", searchModel);
		return viewForm;
	}

	/**
	 * 导出
	 */
	@RequestMapping(value = "/kefu/applylist/export",method = RequestMethod.POST)
	public String exportKefuApplyList(OrderMaterialSearchModel searchModel, HttpServletRequest request, HttpServletResponse response, RedirectAttributes redirectAttributes, Model model)
	{
		String viewForm = "modules/sd/material/kefu/applyList";
		Page<MaterialMaster> page = new Page<>();
		try
		{
			User user = UserUtils.getUser();
			if(user==null || user.getId()==null){
				addMessage(model, "登录超时，请重新登录。");
				return kefuApplyList(searchModel,request,response,model);
			}
			//date
			if (searchModel.getBeginDate() != null) {
				Date sDate = searchModel.getBeginDate();
				Date eDate = searchModel.getEndDate();
				int monthes = DateUtils.getDateDiffMonth(sDate,eDate);
				if(monthes>12){
					addMessage(model, "导出数据跨度不能超过12个月，请分多次导出。");
					model.addAttribute(MODEL_ATTR_PAGE, page);
					model.addAttribute(MODEL_ATTR_SEARCH_MODEL, searchModel);
					return viewForm;
				}
			}
			searchModel = setSearchModel(user,searchModel,model,page,3,true,365);
			if(!searchModel.getValid()){
				model.addAttribute(MODEL_ATTR_PAGE, page);
				model.addAttribute(MODEL_ATTR_SEARCH_MODEL, searchModel);
				return viewForm;
			}
			//状态
			searchModel.setStatus(MaterialMaster.STATUS_NEW);
			Page<OrderMaterialSearchModel> searchModelPage = new Page<>(1,200000);
			try {
				//查询
				page = orderMaterialService.findKefuMaterialList(searchModelPage, searchModel);
			} catch (Exception e) {
				addMessage(model, "查询错误：" + e.getMessage());
				model.addAttribute(MODEL_ATTR_PAGE, page);
				model.addAttribute(MODEL_ATTR_SEARCH_MODEL, searchModel);
				return viewForm;
			}
			String[] tableTitle = new String[]
					{ "序号","订单号", "厂商", "状态", "类型", "配件类型", "产品",
							"用户姓名", "电话", "区域", "详细地址", "申请人","申请时间",
							"跟踪状态","跟踪时间","跟踪内容"
					};
			String xName = "待审核配件列表";
			SXSSFWorkbook xBook;
			SXSSFSheet xSheet;
			Map<String, CellStyle> xStyle;
			ExportExcel exportExcel = new ExportExcel();
			xBook = new SXSSFWorkbook(500);
			xSheet = xBook.createSheet(xName);
			xSheet.setDefaultColumnWidth(15);
			xStyle = exportExcel.createStyles(xBook);

			//region style
			Font headerFont = xBook.createFont();
			headerFont.setFontName("微软雅黑");//Arial
			headerFont.setFontHeightInPoints((short) 12);
			//headerFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
			headerFont.setColor(IndexedColors.GREY_80_PERCENT.getIndex());

			CellStyle headStyle = xBook.createCellStyle();
			//XSSFColor xssfColor = new XSSFColor(new java.awt.Color(191,215,237));
			//headStyle.setFillForegroundColor(xssfColor.getIndex());
			headStyle.setFillForegroundColor(IndexedColors.TURQUOISE.getIndex());
			headStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
			headStyle.setAlignment(CellStyle.ALIGN_CENTER);
			headStyle.setFont(headerFont);

			CellStyle titleStyle = xBook.createCellStyle();
			titleStyle.cloneStyleFrom(xStyle.get("title"));
			Font titleFont = xBook.createFont();
			titleFont.setFontName("微软雅黑");//Arial
			titleFont.setFontHeightInPoints((short) 14);
			titleFont.setBold(true);
			titleStyle.setFont(titleFont);

			Font dataFont = xBook.createFont();
			dataFont.setFontName("微软雅黑");//Arial
			dataFont.setFontHeightInPoints((short) 10);

			CellStyle wrapCellStyle = xBook.createCellStyle();
			wrapCellStyle.cloneStyleFrom(xStyle.get("data"));
			wrapCellStyle.setFont(dataFont);
			wrapCellStyle.setWrapText(true);

			CellStyle dataCellStyle = xBook.createCellStyle();
			dataCellStyle.cloneStyleFrom(xStyle.get("data"));
			dataCellStyle.setFont(dataFont);

			// 加入标题
			int rowNum = 0;
			Row titleRow = xSheet.createRow(rowNum++); // 添加一行
			//titleRow.setHeightInPoints(30); // row高度
			Cell titleCell = titleRow.createCell(0); // 对cell(0)编辑, 对应A1
			titleCell.setCellStyle(titleStyle); // cell样式
			titleCell.setCellValue(xName); // 写入cell内容

			//endregion

			Row headRow;
			Row dataRow;
			Cell cell;
			// 加入表头
			headRow = xSheet.createRow(rowNum++);
			//headRow.setHeightInPoints(16);
			for (int i = 0,titleSize=tableTitle.length; i < titleSize; i++)
			{
				cell = headRow.createCell(i);
				cell.setCellStyle(headStyle);
				cell.setCellValue(tableTitle[i]);
			}

			xSheet.createFreezePane(0, rowNum); // 冻结单元格(x, y)
			List<MaterialMaster> list = page.getList();
			MaterialMaster item;
			int idx = 0;
			for (int j = 0,length=list.size(); j < length; j++) {
				item = list.get(j);
				if (item == null) {
					continue;
				}
				idx++;
				dataRow = xSheet.createRow(rowNum++);
				//dataRow.setHeightInPoints(12);

				cell = dataRow.createCell(0);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(idx);

				cell = dataRow.createCell(1);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(StringUtils.toString(item.getOrderNo()));

				cell = dataRow.createCell(2);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getCustomer() == null?"":StringUtils.toString(item.getCustomer().getName()));

				cell = dataRow.createCell(3);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getStatus().getLabel());

				cell = dataRow.createCell(4);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getApplyType().getLabel());

				cell = dataRow.createCell(5);
				cell.setCellStyle(dataCellStyle);
				//cell.setCellValue(item.getMaterialType().getLabel());
				cell.setCellValue("配件申请");

				cell = dataRow.createCell(6);
				cell.setCellStyle(dataCellStyle);
				//cell.setCellValue(item.getProduct()==null?"":StringUtils.toString(item.getProduct().getName()));
				cell.setCellValue(item.getProductNames());

				cell = dataRow.createCell(7);
				cell.setCellStyle(dataCellStyle);
				//cell.setCellType(Cell.CELL_TYPE_NUMERIC);
				cell.setCellValue(item.getUserName());

				cell = dataRow.createCell(8);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getUserPhone());

				cell = dataRow.createCell(9);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getArea()==null?"":item.getArea().getFullName());
				//快递
				cell = dataRow.createCell(10);
				cell.setCellStyle(wrapCellStyle);
				cell.setCellValue(item.getUserAddress());

				cell = dataRow.createCell(11);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getCreateBy().getName());

				cell = dataRow.createCell(12);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(DateUtils.formatDate(item.getCreateDate(),"yyyy-MM-dd HH:mm"));

				cell = dataRow.createCell(13);
				cell.setCellStyle(dataCellStyle);
				if(item.getPendingType() != null && item.getPendingType().getIntValue()>0) {
					cell.setCellValue(item.getPendingType().getLabel());
				}else{
					cell.setCellValue("");
				}

				cell = dataRow.createCell(14);
				cell.setCellStyle(dataCellStyle);
				if(item.getPendingDate() != null) {
					cell.setCellValue(DateUtils.formatDate(item.getPendingDate(), "yyyy-MM-dd HH:mm"));
				}else{
					cell.setCellValue("");
				}

				cell = dataRow.createCell(15);
				cell.setCellStyle(wrapCellStyle);
				cell.setCellValue(StringUtils.toString(item.getPendingContent()));

				//cell = dataRow.createCell(16);
				//cell.setCellStyle(dataCellStyle);
				////cell.setCellStyle(xStyle.get("redData"));
				//cell.setCellValue(item.getReturnFlag()==1?"返件":"否");
			}

			//设置自动列宽
			xSheet.trackAllColumnsForAutoSizing();
			for (int i = 0; i < tableTitle.length; i++) {
				xSheet.autoSizeColumn(i);
				//sheet.setColumnWidth(i,sheet.getColumnWidth(i)*17/10);
			}
			xSheet.setColumnWidth(0,10*256);
			xSheet.setColumnWidth(9,30*256);
			xSheet.setColumnWidth(10,30*256);
			xSheet.setColumnWidth(15,50*256);

			//标题合并单元格
			xSheet.addMergedRegion(new CellRangeAddress(0, 0, 0, tableTitle.length-1));

			response.reset();
			response.setContentType("application/octet-stream; charset=utf-8");
			response.setHeader("Content-Disposition", "attachment; filename=" + Encodes.urlEncode(xName + ".xlsx"));
			xBook.write(response.getOutputStream());
			xBook.dispose();
			return null;//否则报错：getOutputStream() has already been called for this response
		} catch (Exception e){
			addMessage(model, "导出Excel失败：" + e.getMessage());
			model.addAttribute(MODEL_ATTR_PAGE, page);
			model.addAttribute(MODEL_ATTR_SEARCH_MODEL, searchModel);
			return viewForm;
			//return kefuApplyList(searchModel,request,response,model);
		}
	}

	//endregion

	//region 待发配件

	/**
	 * 待发货配件列表
	 */
	@RequiresPermissions(value = { "sd:order:service" })
	@RequestMapping(value = "/kefu/tosendlist")
	public String kefuToSendList(OrderMaterialSearchModel searchModel, HttpServletRequest request, HttpServletResponse response, Model model) {
		String viewForm = "modules/sd/material/kefu/toSendList";
		Page<MaterialMaster> page = new Page<>();
		User user = UserUtils.getUser();
		searchModel = setSearchModel(user,searchModel,model,page,3,true,365);
		if(!searchModel.getValid()){
			model.addAttribute(MODEL_ATTR_PAGE, page);
			model.addAttribute(MODEL_ATTR_SEARCH_MODEL, searchModel);
			return viewForm;
		}
		//状态：
		searchModel.setStatus(MaterialMaster.STATUS_APPROVED);
		if (!request.getMethod().equalsIgnoreCase("post")) {
			searchModel.setMaterialType(1);//配件申请
		}
		try {
			page = orderMaterialService.findKefuMaterialList(new Page<OrderMaterialSearchModel>(request, response), searchModel);
		} catch (Exception e) {
			addMessage(model, "查询错误：" + e.getMessage());
		}
		model.addAttribute("page", page);
		model.addAttribute("searchModel", searchModel);
		return viewForm;
	}

	/**
	 * 导出
	 */
	@RequestMapping(value = "/kefu/tosendlist/export",method = RequestMethod.POST)
	public String exportKefuToSendList(OrderMaterialSearchModel searchModel, HttpServletRequest request, HttpServletResponse response, RedirectAttributes redirectAttributes, Model model)
	{
		String viewForm = "modules/sd/material/kefu/toSendList";
		Page<MaterialMaster> page = new Page<>();
		try
		{
			User user = UserUtils.getUser();
			if(user==null || user.getId()==null){
				addMessage(model, "登录超时，请重新登录。");
				return kefuToSendList(searchModel,request,response,model);
			}
			//date
			if (searchModel.getBeginDate() != null) {
				Date sDate = searchModel.getBeginDate();
				Date eDate = searchModel.getEndDate();
				int monthes = DateUtils.getDateDiffMonth(sDate,eDate);
				if(monthes>12){
					addMessage(model, "导出数据跨度不能超过12个月，请分多次导出。");
					return kefuToSendList(searchModel,request,response,model);
				}
			}
			searchModel = setSearchModel(user,searchModel,model,page,3,true,365);
			if(!searchModel.getValid()){
				model.addAttribute(MODEL_ATTR_PAGE, page);
				model.addAttribute(MODEL_ATTR_SEARCH_MODEL, searchModel);
				return viewForm;
			}
			//状态
			searchModel.setStatus(MaterialMaster.STATUS_APPROVED);
			Page<OrderMaterialSearchModel> searchModelPage = new Page<>(1,200000);
			try {
				page = orderMaterialService.findKefuMaterialList(searchModelPage, searchModel);
			} catch (Exception e) {
				addMessage(model, "查询错误：" + e.getMessage());
				model.addAttribute(MODEL_ATTR_PAGE, page);
				model.addAttribute(MODEL_ATTR_SEARCH_MODEL, searchModel);
				return viewForm;
			}
			String[] tableTitle = new String[]
					{ "序号","订单号", "厂商", "状态", "类型", "配件类型", "产品",
							"用户姓名", "电话", "区域", "详细地址", "申请人","申请时间",
							"审核人","审核时间","跟踪状态","跟踪时间","跟踪内容","是否返件"
					};
			String xName = "待发货配件列表";
			SXSSFWorkbook xBook;
			SXSSFSheet xSheet;
			Map<String, CellStyle> xStyle;
			ExportExcel exportExcel = new ExportExcel();
			xBook = new SXSSFWorkbook(500);
			xSheet = xBook.createSheet(xName);
			xSheet.setDefaultColumnWidth(15);
			xStyle = exportExcel.createStyles(xBook);

			//region style
			Font headerFont = xBook.createFont();
			headerFont.setFontName("微软雅黑");//Arial
			headerFont.setFontHeightInPoints((short) 12);
			//headerFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
			headerFont.setColor(IndexedColors.GREY_80_PERCENT.getIndex());

			CellStyle headStyle = xBook.createCellStyle();
			//XSSFColor xssfColor = new XSSFColor(new java.awt.Color(191,215,237));
			//headStyle.setFillForegroundColor(xssfColor.getIndex());
			headStyle.setFillForegroundColor(IndexedColors.TURQUOISE.getIndex());
			headStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
			headStyle.setAlignment(CellStyle.ALIGN_CENTER);
			headStyle.setFont(headerFont);

			CellStyle titleStyle = xBook.createCellStyle();
			titleStyle.cloneStyleFrom(xStyle.get("title"));
			Font titleFont = xBook.createFont();
			titleFont.setFontName("微软雅黑");//Arial
			titleFont.setFontHeightInPoints((short) 14);
			titleFont.setBold(true);
			titleStyle.setFont(titleFont);

			Font dataFont = xBook.createFont();
			dataFont.setFontName("微软雅黑");//Arial
			dataFont.setFontHeightInPoints((short) 10);

			CellStyle wrapCellStyle = xBook.createCellStyle();
			wrapCellStyle.cloneStyleFrom(xStyle.get("data"));
			wrapCellStyle.setFont(dataFont);
			wrapCellStyle.setWrapText(true);

			CellStyle dataCellStyle = xBook.createCellStyle();
			dataCellStyle.cloneStyleFrom(xStyle.get("data"));
			dataCellStyle.setFont(dataFont);

			// 加入标题
			int rowNum = 0;
			Row titleRow = xSheet.createRow(rowNum++); // 添加一行
			//titleRow.setHeightInPoints(30); // row高度
			Cell titleCell = titleRow.createCell(0); // 对cell(0)编辑, 对应A1
			titleCell.setCellStyle(titleStyle); // cell样式
			titleCell.setCellValue(xName); // 写入cell内容

			//endregion

			Row headRow;
			Row dataRow;
			Cell cell;
			// 加入表头
			headRow = xSheet.createRow(rowNum++);
			//headRow.setHeightInPoints(16);
			for (int i = 0,titleSize=tableTitle.length; i < titleSize; i++)
			{
				cell = headRow.createCell(i);
				cell.setCellStyle(headStyle);
				cell.setCellValue(tableTitle[i]);
			}

			xSheet.createFreezePane(0, rowNum); // 冻结单元格(x, y)
			List<MaterialMaster> list = page.getList();
			MaterialMaster item;
			int idx = 0;
			for (int j = 0,length=list.size(); j < length; j++) {
				item = list.get(j);
				if (item == null) {
					continue;
				}
				idx++;
				dataRow = xSheet.createRow(rowNum++);
				//dataRow.setHeightInPoints(12);

				cell = dataRow.createCell(0);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(idx);

				cell = dataRow.createCell(1);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(StringUtils.toString(item.getOrderNo()));

				cell = dataRow.createCell(2);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getCustomer() == null?"":StringUtils.toString(item.getCustomer().getName()));

				cell = dataRow.createCell(3);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getStatus().getLabel());

				cell = dataRow.createCell(4);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getApplyType().getLabel());

				cell = dataRow.createCell(5);
				cell.setCellStyle(dataCellStyle);
				//cell.setCellValue(item.getMaterialType().getLabel());
				cell.setCellValue("配件申请");

				cell = dataRow.createCell(6);
				cell.setCellStyle(dataCellStyle);
				//cell.setCellValue(item.getProduct()==null?"":StringUtils.toString(item.getProduct().getName()));
				cell.setCellValue(item.getProductNames());

				cell = dataRow.createCell(7);
				cell.setCellStyle(dataCellStyle);
				//cell.setCellType(Cell.CELL_TYPE_NUMERIC);
				cell.setCellValue(item.getUserName());

				cell = dataRow.createCell(8);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getUserPhone());

				cell = dataRow.createCell(9);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getArea()==null?"":item.getArea().getFullName());
				//快递
				cell = dataRow.createCell(10);
				cell.setCellStyle(wrapCellStyle);
				cell.setCellValue(item.getUserAddress());

				cell = dataRow.createCell(11);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getCreateBy().getName());

				cell = dataRow.createCell(12);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(DateUtils.formatDate(item.getCreateDate(),"yyyy-MM-dd HH:mm"));

				cell = dataRow.createCell(13);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getUpdateBy().getName());

				cell = dataRow.createCell(14);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(DateUtils.formatDate(item.getUpdateDate(),"yyyy-MM-dd HH:mm"));

				cell = dataRow.createCell(15);
				cell.setCellStyle(dataCellStyle);
				if(item.getPendingType() != null && item.getPendingType().getIntValue()>0) {
					cell.setCellValue(item.getPendingType().getLabel());
				}else{
					cell.setCellValue("");
				}

				cell = dataRow.createCell(16);
				cell.setCellStyle(dataCellStyle);
				if(item.getPendingDate() != null) {
					cell.setCellValue(DateUtils.formatDate(item.getPendingDate(), "yyyy-MM-dd HH:mm"));
				}else{
					cell.setCellValue("");
				}

				cell = dataRow.createCell(17);
				cell.setCellStyle(wrapCellStyle);
				cell.setCellValue(StringUtils.toString(item.getPendingContent()));

				cell = dataRow.createCell(18);
				cell.setCellStyle(dataCellStyle);
				//cell.setCellStyle(xStyle.get("redData"));
				cell.setCellValue(item.getReturnFlag()==1?"返件":"否");
			}

			//设置自动列宽
			xSheet.trackAllColumnsForAutoSizing();
			for (int i = 0; i < tableTitle.length; i++) {
				xSheet.autoSizeColumn(i);
				//sheet.setColumnWidth(i,sheet.getColumnWidth(i)*17/10);
			}
			xSheet.setColumnWidth(0,10*256);
			xSheet.setColumnWidth(9,30*256);
			xSheet.setColumnWidth(10,30*256);
			xSheet.setColumnWidth(17,50*256);

			//标题合并单元格
			xSheet.addMergedRegion(new CellRangeAddress(0, 0, 0, tableTitle.length-1));

			response.reset();
			response.setContentType("application/octet-stream; charset=utf-8");
			response.setHeader("Content-Disposition", "attachment; filename=" + Encodes.urlEncode(xName + ".xlsx"));
			xBook.write(response.getOutputStream());
			xBook.dispose();
			return null;//否则报错：getOutputStream() has already been called for this response
		} catch (Exception e)
		{
			addMessage(model, "导出Excel失败：" + e.getMessage());
			model.addAttribute(MODEL_ATTR_PAGE, page);
			model.addAttribute(MODEL_ATTR_SEARCH_MODEL, searchModel);
			return viewForm;
		}
	}

	//endregion

	//region 已发货

	/**
	 * 已发货配件列表
	 */
	@RequiresPermissions(value = { "sd:order:service" })
	@RequestMapping(value = "/kefu/sendlist")
	public String kefuSendList(OrderMaterialSearchModel searchModel, HttpServletRequest request, HttpServletResponse response, Model model) {
		String viewForm = "modules/sd/material/kefu/sendList";
		Page<MaterialMaster> page = new Page<>();
		User user = UserUtils.getUser();
		searchModel = setSearchModel(user,searchModel,model,page,3,true,365);
		if(!searchModel.getValid()){
			model.addAttribute(MODEL_ATTR_PAGE, page);
			model.addAttribute(MODEL_ATTR_SEARCH_MODEL, searchModel);
			return viewForm;
		}
		//状态：
		searchModel.setStatus(MaterialMaster.STATUS_SENDED);
		if (!request.getMethod().equalsIgnoreCase("post")) {
			searchModel.setMaterialType(1);//配件申请
		}
		try {
			page = orderMaterialService.findKefuMaterialList(new Page<OrderMaterialSearchModel>(request, response), searchModel);
		} catch (Exception e) {
			addMessage(model, "查询错误：" + e.getMessage());
		}
		model.addAttribute("page", page);
		model.addAttribute("searchModel", searchModel);
		return viewForm;
	}


	/**
	 * 导出
	 */
	@RequestMapping(value = "/kefu/sendlist/export",method = RequestMethod.POST)
	public String exportKefuSendList(OrderMaterialSearchModel searchModel, HttpServletRequest request, HttpServletResponse response, RedirectAttributes redirectAttributes, Model model)
	{
		String viewForm = "modules/sd/material/kefu/sendList";
		Page<MaterialMaster> page = new Page<>();
		try
		{
			User user = UserUtils.getUser();
			if(user==null || user.getId()==null){
				addMessage(model, "登录超时，请重新登录。");
				return kefuSendList(searchModel,request,response,model);
			}
			//date
			if (searchModel.getBeginDate() != null) {
				Date sDate = searchModel.getBeginDate();
				Date eDate = searchModel.getEndDate();
				int monthes = DateUtils.getDateDiffMonth(sDate,eDate);
				if(monthes>12){
					addMessage(model, "导出数据跨度不能超过12个月，请分多次导出。");
					return kefuSendList(searchModel,request,response,model);
				}
			}
			searchModel = setSearchModel(user,searchModel,model,page,3,true,365);
			if(!searchModel.getValid()){
				model.addAttribute(MODEL_ATTR_PAGE, page);
				model.addAttribute(MODEL_ATTR_SEARCH_MODEL, searchModel);
				return viewForm;
			}
			//状态
			searchModel.setStatus(MaterialMaster.STATUS_SENDED);
			//客服
			if(user.isKefu() || user.isKefuLeader()) {
				searchModel.setKefu(user.getId());
			}
			Page<OrderMaterialSearchModel> searchModelPage = new Page<>(1,200000);
			try {
				//查询
				page = orderMaterialService.findKefuMaterialList(searchModelPage, searchModel);
			} catch (Exception e) {
				addMessage(model, "查询错误：" + e.getMessage());
				return kefuSendList(searchModel,request,response,model);
			}
			String[] tableTitle = new String[]
					{ "序号","订单号", "厂商", "状态", "类型", "配件类型", "产品",
							"用户姓名", "电话", "区域", "详细地址", "申请人", "申请时间",
							"发货人","发货时间","跟踪状态","跟踪时间","跟踪内容","是否返件"
					};
			String xName = "已发货配件列表";
			SXSSFWorkbook xBook;
			SXSSFSheet xSheet;
			Map<String, CellStyle> xStyle;
			ExportExcel exportExcel = new ExportExcel();
			xBook = new SXSSFWorkbook(500);
			xSheet = xBook.createSheet(xName);
			xSheet.setDefaultColumnWidth(15);
			xStyle = exportExcel.createStyles(xBook);

			//region style
			Font headerFont = xBook.createFont();
			headerFont.setFontName("微软雅黑");//Arial
			headerFont.setFontHeightInPoints((short) 12);
			//headerFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
			headerFont.setColor(IndexedColors.GREY_80_PERCENT.getIndex());

			CellStyle headStyle = xBook.createCellStyle();
			//XSSFColor xssfColor = new XSSFColor(new java.awt.Color(191,215,237));
			//headStyle.setFillForegroundColor(xssfColor.getIndex());
			headStyle.setFillForegroundColor(IndexedColors.TURQUOISE.getIndex());
			headStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
			headStyle.setAlignment(CellStyle.ALIGN_CENTER);
			headStyle.setFont(headerFont);

			CellStyle titleStyle = xBook.createCellStyle();
			titleStyle.cloneStyleFrom(xStyle.get("title"));
			Font titleFont = xBook.createFont();
			titleFont.setFontName("微软雅黑");//Arial
			titleFont.setFontHeightInPoints((short) 14);
			titleFont.setBold(true);
			titleStyle.setFont(titleFont);

			Font dataFont = xBook.createFont();
			dataFont.setFontName("微软雅黑");//Arial
			dataFont.setFontHeightInPoints((short) 10);

			CellStyle wrapCellStyle = xBook.createCellStyle();
			wrapCellStyle.cloneStyleFrom(xStyle.get("data"));
			wrapCellStyle.setFont(dataFont);
			wrapCellStyle.setWrapText(true);

			CellStyle dataCellStyle = xBook.createCellStyle();
			dataCellStyle.cloneStyleFrom(xStyle.get("data"));
			dataCellStyle.setFont(dataFont);

			// 加入标题
			int rowNum = 0;
			Row titleRow = xSheet.createRow(rowNum++); // 添加一行
			//titleRow.setHeightInPoints(30); // row高度
			Cell titleCell = titleRow.createCell(0); // 对cell(0)编辑, 对应A1
			titleCell.setCellStyle(titleStyle); // cell样式
			titleCell.setCellValue(xName); // 写入cell内容

			//endregion

			Row headRow;
			Row dataRow;
			Cell cell;
			// 加入表头
			headRow = xSheet.createRow(rowNum++);
			//headRow.setHeightInPoints(16);
			for (int i = 0,titleSize=tableTitle.length; i < titleSize; i++)
			{
				cell = headRow.createCell(i);
				cell.setCellStyle(headStyle);
				cell.setCellValue(tableTitle[i]);
			}

			xSheet.createFreezePane(0, rowNum); // 冻结单元格(x, y)
			List<MaterialMaster> list = page.getList();
			MaterialMaster item;
			int idx = 0;
			for (int j = 0,length=list.size(); j < length; j++) {
				item = list.get(j);
				if (item == null) {
					continue;
				}
				idx++;
				dataRow = xSheet.createRow(rowNum++);
				//dataRow.setHeightInPoints(12);

				cell = dataRow.createCell(0);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(idx);

				cell = dataRow.createCell(1);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(StringUtils.toString(item.getOrderNo()));

				cell = dataRow.createCell(2);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getCustomer() == null?"":StringUtils.toString(item.getCustomer().getName()));

				cell = dataRow.createCell(3);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getStatus().getLabel());

				cell = dataRow.createCell(4);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getApplyType().getLabel());

				cell = dataRow.createCell(5);
				cell.setCellStyle(dataCellStyle);
				//cell.setCellValue(item.getMaterialType().getLabel());
				cell.setCellValue("配件申请");

				cell = dataRow.createCell(6);
				cell.setCellStyle(dataCellStyle);
				//cell.setCellValue(item.getProduct()==null?"":StringUtils.toString(item.getProduct().getName()));
				cell.setCellValue(item.getProductNames());

				cell = dataRow.createCell(7);
				cell.setCellStyle(dataCellStyle);
				//cell.setCellType(Cell.CELL_TYPE_NUMERIC);
				cell.setCellValue(item.getUserName());

				cell = dataRow.createCell(8);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getUserPhone());

				cell = dataRow.createCell(9);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getArea()==null?"":item.getArea().getFullName());
				//快递
				cell = dataRow.createCell(10);
				cell.setCellStyle(wrapCellStyle);
				cell.setCellValue(item.getUserAddress());

				cell = dataRow.createCell(11);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getCreateBy().getName());

				cell = dataRow.createCell(12);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(DateUtils.formatDate(item.getCreateDate(),"yyyy-MM-dd HH:mm"));

				cell = dataRow.createCell(13);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getUpdateBy().getName());

				cell = dataRow.createCell(14);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(DateUtils.formatDate(item.getUpdateDate(),"yyyy-MM-dd HH:mm"));

				cell = dataRow.createCell(15);
				cell.setCellStyle(dataCellStyle);
				if(item.getPendingType() != null && item.getPendingType().getIntValue()>0) {
					cell.setCellValue(item.getPendingType().getLabel());
				}else{
					cell.setCellValue("");
				}

				cell = dataRow.createCell(16);
				cell.setCellStyle(dataCellStyle);
				if(item.getPendingDate() != null) {
					cell.setCellValue(DateUtils.formatDate(item.getPendingDate(), "yyyy-MM-dd HH:mm"));
				}else{
					cell.setCellValue("");
				}

				cell = dataRow.createCell(17);
				cell.setCellStyle(wrapCellStyle);
				cell.setCellValue(StringUtils.toString(item.getPendingContent()));

				cell = dataRow.createCell(18);
				cell.setCellStyle(dataCellStyle);
				//cell.setCellStyle(xStyle.get("redData"));
				cell.setCellValue(item.getReturnFlag()==1?"返件":"否");
			}

			//设置自动列宽
			xSheet.trackAllColumnsForAutoSizing();
			for (int i = 0; i < tableTitle.length; i++) {
				xSheet.autoSizeColumn(i);
				//sheet.setColumnWidth(i,sheet.getColumnWidth(i)*17/10);
			}
			xSheet.setColumnWidth(0,10*256);
			xSheet.setColumnWidth(9,30*256);
			xSheet.setColumnWidth(10,30*256);
			xSheet.setColumnWidth(17,50*256);

			//标题合并单元格
			xSheet.addMergedRegion(new CellRangeAddress(0, 0, 0, tableTitle.length-1));

			response.reset();
			response.setContentType("application/octet-stream; charset=utf-8");
			response.setHeader("Content-Disposition", "attachment; filename=" + Encodes.urlEncode(xName + ".xlsx"));
			xBook.write(response.getOutputStream());
			xBook.dispose();
			return null;//否则报错：getOutputStream() has already been called for this response
		} catch (Exception e)
		{
			addMessage(model, "导出Excel失败：" + e.getMessage());
			model.addAttribute(MODEL_ATTR_PAGE, page);
			model.addAttribute(MODEL_ATTR_SEARCH_MODEL, searchModel);
			return viewForm;
		}
	}

	//endregion

	//region 驳回

	/**
	 * 驳回列表
	 */
	@RequiresPermissions(value = { "sd:order:service" })
	@RequestMapping(value = "/kefu/rejectlist")
	public String kefuRejectList(OrderMaterialSearchModel searchModel, HttpServletRequest request, HttpServletResponse response, Model model) {
		String viewForm = "modules/sd/material/kefu/rejectList";
		Page<MaterialMaster> page = new Page<>();
		User user = UserUtils.getUser();
		searchModel = setSearchModel(user,searchModel,model,page,3,true,365);
		if(!searchModel.getValid()){
			model.addAttribute(MODEL_ATTR_PAGE, page);
			model.addAttribute(MODEL_ATTR_SEARCH_MODEL, searchModel);
			return viewForm;
		}
		//状态：
		searchModel.setStatus(MaterialMaster.STATUS_REJECT);
		if (!request.getMethod().equalsIgnoreCase("post")) {
			searchModel.setMaterialType(1);//配件申请
		}
		try {
			page = orderMaterialService.findKefuMaterialList(new Page<OrderMaterialSearchModel>(request, response), searchModel);
		} catch (Exception e) {
			addMessage(model, "查询错误：" + e.getMessage());
		}
		model.addAttribute("page", page);
		model.addAttribute("searchModel", searchModel);
		return viewForm;
	}


	/**
	 * 导出
	 */
	@RequestMapping(value = "/kefu/rejectlist/export",method = RequestMethod.POST)
	public String exportKefuRejectList(OrderMaterialSearchModel searchModel, HttpServletRequest request, HttpServletResponse response, RedirectAttributes redirectAttributes, Model model)
	{
		String viewForm = "modules/sd/material/kefu/rejectList";
		Page<MaterialMaster> page = new Page<>();
		try
		{
			User user = UserUtils.getUser();
			if(user==null || user.getId()==null){
				addMessage(model, "登录超时，请重新登录。");
				return kefuRejectList(searchModel,request,response,model);
			}
			//date
			if (searchModel.getBeginDate() != null) {
				Date sDate = searchModel.getBeginDate();
				Date eDate = searchModel.getEndDate();
				int monthes = DateUtils.getDateDiffMonth(sDate,eDate);
				if(monthes>12){
					addMessage(model, "导出数据跨度不能超过12个月，请分多次导出。");
					model.addAttribute(MODEL_ATTR_PAGE, page);
					model.addAttribute(MODEL_ATTR_SEARCH_MODEL, searchModel);
					return viewForm;
				}
			}
			searchModel = setSearchModel(user,searchModel,model,page,3,true,365);
			if(!searchModel.getValid()){
				model.addAttribute(MODEL_ATTR_PAGE, page);
				model.addAttribute(MODEL_ATTR_SEARCH_MODEL, searchModel);
				return viewForm;
			}
			//状态
			searchModel.setStatus(MaterialMaster.STATUS_REJECT);
			Page<OrderMaterialSearchModel> searchModelPage = new Page<>(1,200000);
			try {
				page = orderMaterialService.findKefuMaterialList(searchModelPage, searchModel);
			} catch (Exception e) {
				addMessage(model, "查询错误：" + e.getMessage());
				model.addAttribute(MODEL_ATTR_PAGE, page);
				model.addAttribute(MODEL_ATTR_SEARCH_MODEL, searchModel);
				return viewForm;
			}
			String[] tableTitle = new String[]
					{ "序号","订单号", "厂商", "状态", "类型", "配件类型", "产品",
							"用户姓名", "电话", "区域", "详细地址", "申请人","申请时间",
							"审核人","驳回时间","跟踪状态","跟踪时间","跟踪内容"
					};
			String xName = "驳回配件列表";
			SXSSFWorkbook xBook;
			SXSSFSheet xSheet;
			Map<String, CellStyle> xStyle;
			ExportExcel exportExcel = new ExportExcel();
			xBook = new SXSSFWorkbook(500);
			xSheet = xBook.createSheet(xName);
			xSheet.setDefaultColumnWidth(15);
			xStyle = exportExcel.createStyles(xBook);

			//region style
			Font headerFont = xBook.createFont();
			headerFont.setFontName("微软雅黑");//Arial
			headerFont.setFontHeightInPoints((short) 12);
			//headerFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
			headerFont.setColor(IndexedColors.GREY_80_PERCENT.getIndex());

			CellStyle headStyle = xBook.createCellStyle();
			//XSSFColor xssfColor = new XSSFColor(new java.awt.Color(191,215,237));
			//headStyle.setFillForegroundColor(xssfColor.getIndex());
			headStyle.setFillForegroundColor(IndexedColors.TURQUOISE.getIndex());
			headStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
			headStyle.setAlignment(CellStyle.ALIGN_CENTER);
			headStyle.setFont(headerFont);

			CellStyle titleStyle = xBook.createCellStyle();
			titleStyle.cloneStyleFrom(xStyle.get("title"));
			Font titleFont = xBook.createFont();
			titleFont.setFontName("微软雅黑");//Arial
			titleFont.setFontHeightInPoints((short) 14);
			titleFont.setBold(true);
			titleStyle.setFont(titleFont);

			Font dataFont = xBook.createFont();
			dataFont.setFontName("微软雅黑");//Arial
			dataFont.setFontHeightInPoints((short) 10);

			CellStyle wrapCellStyle = xBook.createCellStyle();
			wrapCellStyle.cloneStyleFrom(xStyle.get("data"));
			wrapCellStyle.setFont(dataFont);
			wrapCellStyle.setWrapText(true);

			CellStyle dataCellStyle = xBook.createCellStyle();
			dataCellStyle.cloneStyleFrom(xStyle.get("data"));
			dataCellStyle.setFont(dataFont);

			// 加入标题
			int rowNum = 0;
			Row titleRow = xSheet.createRow(rowNum++); // 添加一行
			//titleRow.setHeightInPoints(30); // row高度
			Cell titleCell = titleRow.createCell(0); // 对cell(0)编辑, 对应A1
			titleCell.setCellStyle(titleStyle); // cell样式
			titleCell.setCellValue(xName); // 写入cell内容

			//endregion

			Row headRow;
			Row dataRow;
			Cell cell;
			// 加入表头
			headRow = xSheet.createRow(rowNum++);
			//headRow.setHeightInPoints(16);
			for (int i = 0,titleSize=tableTitle.length; i < titleSize; i++)
			{
				cell = headRow.createCell(i);
				cell.setCellStyle(headStyle);
				cell.setCellValue(tableTitle[i]);
			}

			xSheet.createFreezePane(0, rowNum); // 冻结单元格(x, y)
			List<MaterialMaster> list = page.getList();
			MaterialMaster item;
			int idx = 0;
			for (int j = 0,length=list.size(); j < length; j++) {
				item = list.get(j);
				if (item == null) {
					continue;
				}
				idx++;
				dataRow = xSheet.createRow(rowNum++);
				//dataRow.setHeightInPoints(12);

				cell = dataRow.createCell(0);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(idx);

				cell = dataRow.createCell(1);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(StringUtils.toString(item.getOrderNo()));

				cell = dataRow.createCell(2);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getCustomer() == null?"":StringUtils.toString(item.getCustomer().getName()));

				cell = dataRow.createCell(3);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getStatus().getLabel());

				cell = dataRow.createCell(4);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getApplyType().getLabel());

				cell = dataRow.createCell(5);
				cell.setCellStyle(dataCellStyle);
				//cell.setCellValue(item.getMaterialType().getLabel());
				cell.setCellValue("配件申请");

				cell = dataRow.createCell(6);
				cell.setCellStyle(dataCellStyle);
				//cell.setCellValue(item.getProduct()==null?"":StringUtils.toString(item.getProduct().getName()));
				cell.setCellValue(item.getProductNames());

				cell = dataRow.createCell(7);
				cell.setCellStyle(dataCellStyle);
				//cell.setCellType(Cell.CELL_TYPE_NUMERIC);
				cell.setCellValue(item.getUserName());

				cell = dataRow.createCell(8);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getUserPhone());

				cell = dataRow.createCell(9);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getArea()==null?"":item.getArea().getFullName());
				//快递
				cell = dataRow.createCell(10);
				cell.setCellStyle(wrapCellStyle);
				cell.setCellValue(item.getUserAddress());

				cell = dataRow.createCell(11);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getCreateBy().getName());

				cell = dataRow.createCell(12);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(DateUtils.formatDate(item.getCreateDate(),"yyyy-MM-dd HH:mm"));

				cell = dataRow.createCell(13);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getUpdateBy().getName());

				cell = dataRow.createCell(14);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(DateUtils.formatDate(item.getUpdateDate(),"yyyy-MM-dd HH:mm"));

				cell = dataRow.createCell(15);
				cell.setCellStyle(dataCellStyle);
				if(item.getPendingType() != null && item.getPendingType().getIntValue()>0) {
					cell.setCellValue(item.getPendingType().getLabel());
				}else{
					cell.setCellValue("");
				}

				cell = dataRow.createCell(16);
				cell.setCellStyle(dataCellStyle);
				if(item.getPendingDate() != null) {
					cell.setCellValue(DateUtils.formatDate(item.getPendingDate(), "yyyy-MM-dd HH:mm"));
				}else{
					cell.setCellValue("");
				}

				cell = dataRow.createCell(17);
				cell.setCellStyle(wrapCellStyle);
				cell.setCellValue(StringUtils.toString(item.getPendingContent()));

				//cell = dataRow.createCell(18);
				//cell.setCellStyle(dataCellStyle);
				////cell.setCellStyle(xStyle.get("redData"));
				//cell.setCellValue(item.getReturnFlag()==1?"返件":"否");
			}

			//设置自动列宽
			xSheet.trackAllColumnsForAutoSizing();
			for (int i = 0; i < tableTitle.length; i++) {
				xSheet.autoSizeColumn(i);
				//sheet.setColumnWidth(i,sheet.getColumnWidth(i)*17/10);
			}
			xSheet.setColumnWidth(0,10*256);
			xSheet.setColumnWidth(9,30*256);
			xSheet.setColumnWidth(10,30*256);
			xSheet.setColumnWidth(17,50*256);

			//标题合并单元格
			xSheet.addMergedRegion(new CellRangeAddress(0, 0, 0, tableTitle.length-1));

			response.reset();
			response.setContentType("application/octet-stream; charset=utf-8");
			response.setHeader("Content-Disposition", "attachment; filename=" + Encodes.urlEncode(xName + ".xlsx"));
			xBook.write(response.getOutputStream());
			xBook.dispose();
			return null;//否则报错：getOutputStream() has already been called for this response
		} catch (Exception e)
		{
			addMessage(model, "导出Excel失败：" + e.getMessage());
			model.addAttribute(MODEL_ATTR_PAGE, page);
			model.addAttribute(MODEL_ATTR_SEARCH_MODEL, searchModel);
			return viewForm;
		}
	}

	//endregion

	//region 完成

	/**
	 * 完成列表
	 */
	@RequiresPermissions(value = { "sd:order:service" })
	@RequestMapping(value = "/kefu/closelist")
	public String kefuCloseList(OrderMaterialSearchModel searchModel, HttpServletRequest request, HttpServletResponse response, Model model) {
		String viewForm = "modules/sd/material/kefu/closeList";
		Page<MaterialMaster> page = new Page<>();
		User user = UserUtils.getUser();
		searchModel = setSearchModel(user,searchModel,model,page,3,true,365);
		if(!searchModel.getValid()){
			model.addAttribute(MODEL_ATTR_PAGE, page);
			model.addAttribute(MODEL_ATTR_SEARCH_MODEL, searchModel);
			return viewForm;
		}
		//状态：
		searchModel.setStatus(MaterialMaster.STATUS_CLOSED);
		Boolean isValide = checkOrderNoAndPhone(searchModel,model,page);
		if(!isValide){
			return viewForm;
		}
		try {
			page = orderMaterialService.findKefuMaterialList(new Page<OrderMaterialSearchModel>(request, response), searchModel);
		} catch (Exception e) {
			addMessage(model, "查询错误：" + e.getMessage());
		}
		model.addAttribute("page", page);
		model.addAttribute("searchModel", searchModel);
		return viewForm;
	}


	/**
	 * 导出
	 */
	@RequestMapping(value = "/kefu/closelist/export",method = RequestMethod.POST)
	public String exportKefuCloseList(OrderMaterialSearchModel searchModel, HttpServletRequest request, HttpServletResponse response, RedirectAttributes redirectAttributes, Model model)
	{
		String viewForm = "modules/sd/material/kefu/closeList";
		Page<MaterialMaster> page = new Page<>();
		try
		{
			User user = UserUtils.getUser();
			if(user==null || user.getId()==null){
				addMessage(model, "登录超时，请重新登录。");
				model.addAttribute(MODEL_ATTR_PAGE, page);
				model.addAttribute(MODEL_ATTR_SEARCH_MODEL, searchModel);
				return viewForm;
			}
			//date
			if (searchModel.getBeginDate() != null) {
				Date sDate = searchModel.getBeginDate();
				Date eDate = searchModel.getEndDate();
				int monthes = DateUtils.getDateDiffMonth(sDate,eDate);
				if(monthes>12){
					addMessage(model, "导出数据跨度不能超过12个月，请分多次导出。");
					model.addAttribute(MODEL_ATTR_PAGE, page);
					model.addAttribute(MODEL_ATTR_SEARCH_MODEL, searchModel);
					return viewForm;
				}
			}

			searchModel = setSearchModel(user,searchModel,model,page,3,true,365);
			if(!searchModel.getValid()){
				model.addAttribute(MODEL_ATTR_PAGE, page);
				model.addAttribute(MODEL_ATTR_SEARCH_MODEL, searchModel);
				return viewForm;
			}
			//状态
			searchModel.setStatus(MaterialMaster.STATUS_CLOSED);
			Page<OrderMaterialSearchModel> searchModelPage = new Page<>(1,200000);
			try {
				page = orderMaterialService.findKefuMaterialList(searchModelPage, searchModel);
			} catch (Exception e) {
				addMessage(model, "查询错误：" + e.getMessage());
				model.addAttribute(MODEL_ATTR_PAGE, page);
				model.addAttribute(MODEL_ATTR_SEARCH_MODEL, searchModel);
				return viewForm;
			}
			String[] tableTitle = new String[]
					{ "序号","订单号", "厂商", "状态", "类型", "配件类型", "产品",
							"用户姓名", "电话", "区域", "详细地址", "申请人","申请时间",
							"完成人","完成时间","跟踪状态","跟踪时间","跟踪内容","是否返件"
					};
			String xName = "完成配件列表";
			SXSSFWorkbook xBook;
			SXSSFSheet xSheet;
			Map<String, CellStyle> xStyle;
			ExportExcel exportExcel = new ExportExcel();
			xBook = new SXSSFWorkbook(500);
			xSheet = xBook.createSheet(xName);
			xSheet.setDefaultColumnWidth(15);
			xStyle = exportExcel.createStyles(xBook);

			//region style
			Font headerFont = xBook.createFont();
			headerFont.setFontName("微软雅黑");//Arial
			headerFont.setFontHeightInPoints((short) 12);
			//headerFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
			headerFont.setColor(IndexedColors.GREY_80_PERCENT.getIndex());

			CellStyle headStyle = xBook.createCellStyle();
			//XSSFColor xssfColor = new XSSFColor(new java.awt.Color(191,215,237));
			//headStyle.setFillForegroundColor(xssfColor.getIndex());
			headStyle.setFillForegroundColor(IndexedColors.TURQUOISE.getIndex());
			headStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
			headStyle.setAlignment(CellStyle.ALIGN_CENTER);
			headStyle.setFont(headerFont);

			CellStyle titleStyle = xBook.createCellStyle();
			titleStyle.cloneStyleFrom(xStyle.get("title"));
			Font titleFont = xBook.createFont();
			titleFont.setFontName("微软雅黑");//Arial
			titleFont.setFontHeightInPoints((short) 14);
			titleFont.setBold(true);
			titleStyle.setFont(titleFont);

			Font dataFont = xBook.createFont();
			dataFont.setFontName("微软雅黑");//Arial
			dataFont.setFontHeightInPoints((short) 10);

			CellStyle wrapCellStyle = xBook.createCellStyle();
			wrapCellStyle.cloneStyleFrom(xStyle.get("data"));
			wrapCellStyle.setFont(dataFont);
			wrapCellStyle.setWrapText(true);

			CellStyle dataCellStyle = xBook.createCellStyle();
			dataCellStyle.cloneStyleFrom(xStyle.get("data"));
			dataCellStyle.setFont(dataFont);

			// 加入标题
			int rowNum = 0;
			Row titleRow = xSheet.createRow(rowNum++); // 添加一行
			//titleRow.setHeightInPoints(30); // row高度
			Cell titleCell = titleRow.createCell(0); // 对cell(0)编辑, 对应A1
			titleCell.setCellStyle(titleStyle); // cell样式
			titleCell.setCellValue(xName); // 写入cell内容

			//endregion

			Row headRow;
			Row dataRow;
			Cell cell;
			// 加入表头
			headRow = xSheet.createRow(rowNum++);
			//headRow.setHeightInPoints(16);
			for (int i = 0,titleSize=tableTitle.length; i < titleSize; i++)
			{
				cell = headRow.createCell(i);
				cell.setCellStyle(headStyle);
				cell.setCellValue(tableTitle[i]);
			}

			xSheet.createFreezePane(0, rowNum); // 冻结单元格(x, y)
			List<MaterialMaster> list = page.getList();
			MaterialMaster item;
			int idx = 0;
			for (int j = 0,length=list.size(); j < length; j++) {
				item = list.get(j);
				if (item == null) {
					continue;
				}
				idx++;
				dataRow = xSheet.createRow(rowNum++);
				//dataRow.setHeightInPoints(12);

				cell = dataRow.createCell(0);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(idx);

				cell = dataRow.createCell(1);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(StringUtils.toString(item.getOrderNo()));

				cell = dataRow.createCell(2);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getCustomer() == null?"":StringUtils.toString(item.getCustomer().getName()));

				cell = dataRow.createCell(3);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getStatus().getLabel());

				cell = dataRow.createCell(4);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getApplyType().getLabel());

				cell = dataRow.createCell(5);
				cell.setCellStyle(dataCellStyle);
				//cell.setCellValue(item.getMaterialType().getLabel());
				cell.setCellValue("配件申请");

				cell = dataRow.createCell(6);
				cell.setCellStyle(dataCellStyle);
				//cell.setCellValue(item.getProduct()==null?"":StringUtils.toString(item.getProduct().getName()));
				cell.setCellValue(item.getProductNames());

				cell = dataRow.createCell(7);
				cell.setCellStyle(dataCellStyle);
				//cell.setCellType(Cell.CELL_TYPE_NUMERIC);
				cell.setCellValue(item.getUserName());

				cell = dataRow.createCell(8);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getUserPhone());

				cell = dataRow.createCell(9);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getArea()==null?"":item.getArea().getFullName());
				//快递
				cell = dataRow.createCell(10);
				cell.setCellStyle(wrapCellStyle);
				cell.setCellValue(item.getUserAddress());

				cell = dataRow.createCell(11);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getCreateBy().getName());

				cell = dataRow.createCell(12);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(DateUtils.formatDate(item.getCreateDate(),"yyyy-MM-dd HH:mm"));

				cell = dataRow.createCell(13);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getUpdateBy().getName());

				cell = dataRow.createCell(14);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(DateUtils.formatDate(item.getUpdateDate(),"yyyy-MM-dd HH:mm"));

				cell = dataRow.createCell(15);
				cell.setCellStyle(dataCellStyle);
				if(item.getPendingType() != null && item.getPendingType().getIntValue()>0) {
					cell.setCellValue(item.getPendingType().getLabel());
				}else{
					cell.setCellValue("");
				}

				cell = dataRow.createCell(16);
				cell.setCellStyle(dataCellStyle);
				if(item.getPendingDate() != null) {
					cell.setCellValue(DateUtils.formatDate(item.getPendingDate(), "yyyy-MM-dd HH:mm"));
				}else{
					cell.setCellValue("");
				}

				cell = dataRow.createCell(17);
				cell.setCellStyle(wrapCellStyle);
				cell.setCellValue(StringUtils.toString(item.getPendingContent()));

				cell = dataRow.createCell(18);
				cell.setCellStyle(dataCellStyle);
				//cell.setCellStyle(xStyle.get("redData"));
				cell.setCellValue(item.getReturnFlag()==1?"返件":"否");
			}

			//设置自动列宽
			xSheet.trackAllColumnsForAutoSizing();
			for (int i = 0; i < tableTitle.length; i++) {
				xSheet.autoSizeColumn(i);
				//sheet.setColumnWidth(i,sheet.getColumnWidth(i)*17/10);
			}
			xSheet.setColumnWidth(0,10*256);
			xSheet.setColumnWidth(9,30*256);
			xSheet.setColumnWidth(10,30*256);
			xSheet.setColumnWidth(17,50*256);

			//标题合并单元格
			xSheet.addMergedRegion(new CellRangeAddress(0, 0, 0, tableTitle.length-1));

			response.reset();
			response.setContentType("application/octet-stream; charset=utf-8");
			response.setHeader("Content-Disposition", "attachment; filename=" + Encodes.urlEncode(xName + ".xlsx"));
			xBook.write(response.getOutputStream());
			xBook.dispose();
			return null;//否则报错：getOutputStream() has already been called for this response
		} catch (Exception e)
		{
			addMessage(model, "导出Excel失败：" + e.getMessage());
			model.addAttribute(MODEL_ATTR_PAGE, page);
			model.addAttribute(MODEL_ATTR_SEARCH_MODEL, searchModel);
			return viewForm;
		}
	}

	//endregion

	//region 所有

	/**
	 * 所有列表
	 */
	@RequiresPermissions(value = { "sd:order:service" })
	@RequestMapping(value = "/kefu/alllist")
	public String kefuAllList(OrderMaterialSearchModel searchModel, HttpServletRequest request, HttpServletResponse response, Model model) {
		String viewForm = "modules/sd/material/kefu/allList";
		Page<MaterialMaster> page = new Page<>();
		User user = UserUtils.getUser();
		searchModel = setSearchModel(user,searchModel,model,page,3,true,365);
		if(!searchModel.getValid()){
			model.addAttribute(MODEL_ATTR_PAGE, page);
			model.addAttribute(MODEL_ATTR_SEARCH_MODEL, searchModel);
			return viewForm;
		}
		//状态：所有
		searchModel.setStatus(0);
		try {
			page = orderMaterialService.findKefuMaterialList(new Page<OrderMaterialSearchModel>(request, response), searchModel);
		} catch (Exception e) {
			addMessage(model, "查询错误：" + e.getMessage());
		}
		model.addAttribute("page", page);
		model.addAttribute("searchModel", searchModel);
		return viewForm;
	}


	/**
	 * 导出
	 */
	@RequestMapping(value = "/kefu/alllist/export",method = RequestMethod.POST)
	public String exportKefuAllList(OrderMaterialSearchModel searchModel, HttpServletRequest request, HttpServletResponse response, RedirectAttributes redirectAttributes, Model model)
	{
		String viewForm = "modules/sd/material/kefu/allList";
		Page<MaterialMaster> page = new Page<>();
		try
		{
			User user = UserUtils.getUser();
			if(user==null || user.getId()==null){
				addMessage(model, "登录超时，请重新登录。");
				return kefuAllList(searchModel,request,response,model);
			}
			//date
			if (searchModel.getBeginDate() != null) {
				Date sDate = searchModel.getBeginDate();
				Date eDate = searchModel.getEndDate();
				int monthes = DateUtils.getDateDiffMonth(sDate,eDate);
				if(monthes>12){
					addMessage(model, "导出数据跨度不能超过12个月，请分多次导出。");
					model.addAttribute(MODEL_ATTR_PAGE, page);
					model.addAttribute(MODEL_ATTR_SEARCH_MODEL, searchModel);
					return viewForm;
				}
			}
			searchModel = setSearchModel(user,searchModel,model,page,3,true,365);
			if(!searchModel.getValid()){
				model.addAttribute(MODEL_ATTR_PAGE, page);
				model.addAttribute(MODEL_ATTR_SEARCH_MODEL, searchModel);
				return viewForm;
			}
			//状态
			searchModel.setStatus(0);
			Page<OrderMaterialSearchModel> searchModelPage = new Page<>(1,200000);
			try {
				page = orderMaterialService.findKefuMaterialList(searchModelPage, searchModel);
			} catch (Exception e) {
				addMessage(model, "查询错误：" + e.getMessage());
				model.addAttribute(MODEL_ATTR_PAGE, page);
				model.addAttribute(MODEL_ATTR_SEARCH_MODEL, searchModel);
				return viewForm;
			}
			String[] tableTitle = new String[]
					{ "序号","订单号", "厂商", "状态", "类型", "配件类型", "产品",
							"用户姓名", "电话", "区域", "详细地址", "申请人","申请时间",
							"完成人","完成时间","完成内容","跟踪状态","跟踪时间","跟踪内容","是否返件"
					};
			String xName = "所有配件列表";
			SXSSFWorkbook xBook;
			SXSSFSheet xSheet;
			Map<String, CellStyle> xStyle;
			ExportExcel exportExcel = new ExportExcel();
			xBook = new SXSSFWorkbook(500);
			xSheet = xBook.createSheet(xName);
			xSheet.setDefaultColumnWidth(15);
			xStyle = exportExcel.createStyles(xBook);

			//region style
			Font headerFont = xBook.createFont();
			headerFont.setFontName("微软雅黑");//Arial
			headerFont.setFontHeightInPoints((short) 12);
			//headerFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
			headerFont.setColor(IndexedColors.GREY_80_PERCENT.getIndex());

			CellStyle headStyle = xBook.createCellStyle();
			//XSSFColor xssfColor = new XSSFColor(new java.awt.Color(191,215,237));
			//headStyle.setFillForegroundColor(xssfColor.getIndex());
			headStyle.setFillForegroundColor(IndexedColors.TURQUOISE.getIndex());
			headStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
			headStyle.setAlignment(CellStyle.ALIGN_CENTER);
			headStyle.setFont(headerFont);

			CellStyle titleStyle = xBook.createCellStyle();
			titleStyle.cloneStyleFrom(xStyle.get("title"));
			Font titleFont = xBook.createFont();
			titleFont.setFontName("微软雅黑");//Arial
			titleFont.setFontHeightInPoints((short) 14);
			titleFont.setBold(true);
			titleStyle.setFont(titleFont);

			Font dataFont = xBook.createFont();
			dataFont.setFontName("微软雅黑");//Arial
			dataFont.setFontHeightInPoints((short) 10);

			CellStyle wrapCellStyle = xBook.createCellStyle();
			wrapCellStyle.cloneStyleFrom(xStyle.get("data"));
			wrapCellStyle.setFont(dataFont);
			wrapCellStyle.setWrapText(true);

			CellStyle dataCellStyle = xBook.createCellStyle();
			dataCellStyle.cloneStyleFrom(xStyle.get("data"));
			dataCellStyle.setFont(dataFont);

			// 加入标题
			int rowNum = 0;
			Row titleRow = xSheet.createRow(rowNum++); // 添加一行
			//titleRow.setHeightInPoints(30); // row高度
			Cell titleCell = titleRow.createCell(0); // 对cell(0)编辑, 对应A1
			titleCell.setCellStyle(titleStyle); // cell样式
			titleCell.setCellValue(xName); // 写入cell内容

			//endregion

			Row headRow;
			Row dataRow;
			Cell cell;
			// 加入表头
			headRow = xSheet.createRow(rowNum++);
			//headRow.setHeightInPoints(16);
			for (int i = 0,titleSize=tableTitle.length; i < titleSize; i++)
			{
				cell = headRow.createCell(i);
				cell.setCellStyle(headStyle);
				cell.setCellValue(tableTitle[i]);
			}

			xSheet.createFreezePane(0, rowNum); // 冻结单元格(x, y)
			List<MaterialMaster> list = page.getList();
			MaterialMaster item;
			int idx = 0;
			for (int j = 0,length=list.size(); j < length; j++) {
				item = list.get(j);
				if (item == null) {
					continue;
				}
				idx++;
				dataRow = xSheet.createRow(rowNum++);
				//dataRow.setHeightInPoints(12);

				cell = dataRow.createCell(0);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(idx);

				cell = dataRow.createCell(1);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(StringUtils.toString(item.getOrderNo()));

				cell = dataRow.createCell(2);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getCustomer() == null?"":StringUtils.toString(item.getCustomer().getName()));

				cell = dataRow.createCell(3);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getStatus().getLabel());

				cell = dataRow.createCell(4);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getApplyType().getLabel());

				cell = dataRow.createCell(5);
				cell.setCellStyle(dataCellStyle);
				//cell.setCellValue(item.getMaterialType().getLabel());
				cell.setCellValue("配件申请");

				cell = dataRow.createCell(6);
				cell.setCellStyle(dataCellStyle);
				//cell.setCellValue(item.getProduct()==null?"":StringUtils.toString(item.getProduct().getName()));
				cell.setCellValue(item.getProductNames());

				cell = dataRow.createCell(7);
				cell.setCellStyle(dataCellStyle);
				//cell.setCellType(Cell.CELL_TYPE_NUMERIC);
				cell.setCellValue(item.getUserName());

				cell = dataRow.createCell(8);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getUserPhone());

				cell = dataRow.createCell(9);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getArea()==null?"":item.getArea().getFullName());
				//快递
				cell = dataRow.createCell(10);
				cell.setCellStyle(wrapCellStyle);
				cell.setCellValue(item.getUserAddress());

				cell = dataRow.createCell(11);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getCreateBy().getName());

				cell = dataRow.createCell(12);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(DateUtils.formatDate(item.getCreateDate(),"yyyy-MM-dd HH:mm"));

				cell = dataRow.createCell(13);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getCloseBy().getId()>0?item.getCloseBy().getName():"");

				cell = dataRow.createCell(14);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getCloseBy().getId()>0?DateUtils.formatDate(item.getUpdateDate(),"yyyy-MM-dd HH:mm"):"");

				cell = dataRow.createCell(15);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getCloseBy().getId()>0?item.getCloseRemark():"");

				cell = dataRow.createCell(16);
				cell.setCellStyle(dataCellStyle);
				if(item.getPendingType() != null && item.getPendingType().getIntValue()>0) {
					cell.setCellValue(item.getPendingType().getLabel());
				}else{
					cell.setCellValue("");
				}

				cell = dataRow.createCell(17);
				cell.setCellStyle(dataCellStyle);
				if(item.getPendingDate() != null) {
					cell.setCellValue(DateUtils.formatDate(item.getPendingDate(), "yyyy-MM-dd HH:mm"));
				}else{
					cell.setCellValue("");
				}

				cell = dataRow.createCell(18);
				cell.setCellStyle(wrapCellStyle);
				cell.setCellValue(StringUtils.toString(item.getPendingContent()));

				cell = dataRow.createCell(19);
				cell.setCellStyle(dataCellStyle);
				//cell.setCellStyle(xStyle.get("redData"));
				cell.setCellValue(item.getReturnFlag()==1?"返件":"否");
			}

			//设置自动列宽
			xSheet.trackAllColumnsForAutoSizing();
			for (int i = 0; i < tableTitle.length; i++) {
				xSheet.autoSizeColumn(i);
				//sheet.setColumnWidth(i,sheet.getColumnWidth(i)*17/10);
			}
			xSheet.setColumnWidth(0,10*256);
			xSheet.setColumnWidth(9,30*256);
			xSheet.setColumnWidth(10,30*256);
			xSheet.setColumnWidth(15,50*256);
			xSheet.setColumnWidth(18,50*256);

			//标题合并单元格
			xSheet.addMergedRegion(new CellRangeAddress(0, 0, 0, tableTitle.length-1));

			response.reset();
			response.setContentType("application/octet-stream; charset=utf-8");
			response.setHeader("Content-Disposition", "attachment; filename=" + Encodes.urlEncode(xName + ".xlsx"));
			xBook.write(response.getOutputStream());
			xBook.dispose();
			return null;//否则报错：getOutputStream() has already been called for this response
		} catch (Exception e)
		{
			addMessage(model, "导出Excel失败：" + e.getMessage());
			model.addAttribute(MODEL_ATTR_PAGE, page);
			model.addAttribute(MODEL_ATTR_SEARCH_MODEL, searchModel);
			return viewForm;
		}
	}

	//endregion

	//endregion

	//region 客户列表

	//region 待审核

	/**
	 * 待审核配件列表
	 */
	//@RequiresPermissions(value = { "sd:order:service" })
	@RequestMapping(value = "/customer/applylist")
	public String customerApplyList(OrderMaterialSearchModel searchModel, HttpServletRequest request, HttpServletResponse response, Model model) {
		String viewForm = "modules/sd/material/customer/applyList";
		Page<MaterialMaster> page = new Page<>();
		User user = UserUtils.getUser();
		//状态
		searchModel.setStatus(MaterialMaster.STATUS_NEW);
		//检查客户帐号信息
		if (user.isCustomer()) {
			if (user.getCustomerAccountProfile() != null && user.getCustomerAccountProfile().getCustomer() != null) {
				//登录用户的客户，防篡改
				searchModel.setCustomer(user.getCustomerAccountProfile().getCustomer());
			} else {
				addMessage(model, "错误：登录超时，请退出后重新登录。");
				model.addAttribute("canAction", false);
				model.addAttribute("page", page);
				model.addAttribute("searchModel", searchModel);
				return viewForm;
			}
			//客户账号负责的店铺 2021/06/22
			List<String> shopIds = UserUtils.getShopIdsOfCustomerAccount(user);
			if(!CollectionUtils.isEmpty(shopIds)){
				searchModel.setShopIds(shopIds);
			}
		} else if (user.isSaleman()) {
			searchModel.setSales(user.getId());
			searchModel.setSubUserType(user.getSubFlag());//子账号类型
			List<Long> offlineCustomers = customerService.findIdListByOfflineOrderFlagFromCacheForSD();
			if(!org.springframework.util.CollectionUtils.isEmpty(offlineCustomers)){
				searchModel.setOfflineCustomerList(offlineCustomers);
			}
		}
		//date
		if (searchModel.getBeginDate() == null) {
			searchModel.setEndDate(DateUtils.getDateEnd(new Date()));
			searchModel.setBeginDate(DateUtils.getStartDayOfMonth(DateUtils.addMonth(new Date(), -3)));
		} else {
			searchModel.setEndDate(DateUtils.getDateEnd(searchModel.getEndDate()));
		}
		if (!request.getMethod().equalsIgnoreCase("post")) {
			searchModel.setMaterialType(1);//配件申请
		}
		Boolean isValide = checkOrderNoAndPhone(searchModel,model,page);
		if(!isValide){
			return viewForm;
		}
		try {
			//查询
			page = orderMaterialService.findCustomerMaterialList(new Page<OrderMaterialSearchModel>(request, response), searchModel);
			model.addAttribute("canAction", true);
		} catch (Exception e) {
			model.addAttribute("canAction", false);
			addMessage(model, "查询错误：" + e.getMessage());
		}
		model.addAttribute("page", page);
		model.addAttribute("searchModel", searchModel);
		return viewForm;
	}

	/**
	 * 导出
	 */
	@RequestMapping(value = "/customer/applylist/export",method = RequestMethod.POST)
	public String exportCustomerApplyList(OrderMaterialSearchModel searchModel, HttpServletRequest request, HttpServletResponse response, RedirectAttributes redirectAttributes, Model model)
	{
		try
		{
			User user = UserUtils.getUser();
			if(user==null || user.getId()==null){
				addMessage(model, "登录超时，请重新登录。");
				return customerApplyList(searchModel,request,response,model);
			}
			//date
			if (searchModel.getBeginDate() != null) {
				Date sDate = searchModel.getBeginDate();
				Date eDate = searchModel.getEndDate();
				int monthes = DateUtils.getDateDiffMonth(sDate,eDate);
				if(monthes>12){
					addMessage(model, "导出数据跨度不能超过12个月，请分多次导出。");
					return customerApplyList(searchModel,request,response,model);
				}
			}
			//检查客户帐号信息
			boolean isCustomer = user.isCustomer();
			if (isCustomer) {
				if (user.getCustomerAccountProfile() != null && user.getCustomerAccountProfile().getCustomer() != null) {
					//登录用户的客户，防篡改
					searchModel.setCustomer(user.getCustomerAccountProfile().getCustomer());
				}
				//客户账号负责的店铺 2021/06/22
				List<String> shopIds = UserUtils.getShopIdsOfCustomerAccount(user);
				if(!CollectionUtils.isEmpty(shopIds)){
					searchModel.setShopIds(shopIds);
				}
			} else if (user.isSaleman()) {
				searchModel.setSales(user.getId());
				searchModel.setSubUserType(user.getSubFlag());//子账号类型
				List<Long> offlineCustomers = customerService.findIdListByOfflineOrderFlagFromCacheForSD();
				if(!org.springframework.util.CollectionUtils.isEmpty(offlineCustomers)){
					searchModel.setOfflineCustomerList(offlineCustomers);
				}
			}
			if (searchModel.getBeginDate() == null) {
				searchModel.setEndDate(DateUtils.getDateEnd(new Date()));
				searchModel.setBeginDate(DateUtils.getStartDayOfMonth(DateUtils.addMonth(new Date(), -3)));
			} else {
				searchModel.setEndDate(DateUtils.getDateEnd(searchModel.getEndDate()));
			}
			Page<MaterialMaster> page = new Page<>();
			//状态
			searchModel.setStatus(MaterialMaster.STATUS_NEW);
			Page<OrderMaterialSearchModel> searchModelPage = new Page<>(1,200000);
			try {
				//查询
				page = orderMaterialService.findCustomerMaterialList(searchModelPage, searchModel);
			} catch (Exception e) {
				addMessage(model, "查询错误：" + e.getMessage());
				return customerApplyList(searchModel,request,response,model);
			}
			List<String> tableTitle = Lists.newArrayList(
					"序号","订单号", "状态", "类型", "配件类型", "产品",
					"用户姓名", "电话", "区域", "详细地址", "申请人","申请时间",
					"跟踪状态","跟踪时间","跟踪内容"
			);
			if(!isCustomer){
				tableTitle.add(2,"厂商");
			}
			String xName = "待审核配件列表";
			SXSSFWorkbook xBook;
			SXSSFSheet xSheet;
			Map<String, CellStyle> xStyle;
			ExportExcel exportExcel = new ExportExcel();
			xBook = new SXSSFWorkbook(500);
			xSheet = xBook.createSheet(xName);
			xSheet.setDefaultColumnWidth(15);
			xStyle = exportExcel.createStyles(xBook);

			//region style
			Font headerFont = xBook.createFont();
			headerFont.setFontName("微软雅黑");//Arial
			headerFont.setFontHeightInPoints((short) 12);
			//headerFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
			headerFont.setColor(IndexedColors.GREY_80_PERCENT.getIndex());

			CellStyle headStyle = xBook.createCellStyle();
			headStyle.setFillForegroundColor(IndexedColors.TURQUOISE.getIndex());
			headStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
			headStyle.setAlignment(CellStyle.ALIGN_CENTER);
			headStyle.setFont(headerFont);

			CellStyle titleStyle = xBook.createCellStyle();
			titleStyle.cloneStyleFrom(xStyle.get("title"));
			Font titleFont = xBook.createFont();
			titleFont.setFontName("微软雅黑");//Arial
			titleFont.setFontHeightInPoints((short) 14);
			titleFont.setBold(true);
			titleStyle.setFont(titleFont);

			Font dataFont = xBook.createFont();
			dataFont.setFontName("微软雅黑");//Arial
			dataFont.setFontHeightInPoints((short) 10);

			CellStyle wrapCellStyle = xBook.createCellStyle();
			wrapCellStyle.cloneStyleFrom(xStyle.get("data"));
			wrapCellStyle.setFont(dataFont);
			wrapCellStyle.setWrapText(true);

			CellStyle dataCellStyle = xBook.createCellStyle();
			dataCellStyle.cloneStyleFrom(xStyle.get("data"));
			dataCellStyle.setFont(dataFont);

			// 加入标题
			int rowNum = 0;
			Row titleRow = xSheet.createRow(rowNum++); // 添加一行
			//titleRow.setHeightInPoints(30); // row高度
			Cell titleCell = titleRow.createCell(0); // 对cell(0)编辑, 对应A1
			titleCell.setCellStyle(titleStyle); // cell样式
			titleCell.setCellValue(xName); // 写入cell内容

			//endregion

			Row headRow;
			Row dataRow;
			Cell cell;
			// 加入表头
			headRow = xSheet.createRow(rowNum++);
			for (int i = 0,titleSize=tableTitle.size(); i < titleSize; i++)
			{
				cell = headRow.createCell(i);
				cell.setCellStyle(headStyle);
				cell.setCellValue(tableTitle.get(i));
			}

			xSheet.createFreezePane(0, rowNum); // 冻结单元格(x, y)
			List<MaterialMaster> list = page.getList();
			MaterialMaster item;
			int idx = 0;
			int col = 0;
			for (int j = 0,length=list.size(); j < length; j++) {
				item = list.get(j);
				if (item == null) {
					continue;
				}
				idx++;
				dataRow = xSheet.createRow(rowNum++);
				col = 0;
				cell = dataRow.createCell(col);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(idx);

				col++;
				cell = dataRow.createCell(col);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(StringUtils.toString(item.getOrderNo()));

				if(!isCustomer) {
					col++;
					cell = dataRow.createCell(col);
					cell.setCellStyle(dataCellStyle);
					cell.setCellValue(item.getCustomer() == null ? "" : StringUtils.toString(item.getCustomer().getName()));
				}

				col++;
				cell = dataRow.createCell(col);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getStatus().getLabel());

				col++;
				cell = dataRow.createCell(col);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getApplyType().getLabel());

				col++;
				cell = dataRow.createCell(col);
				cell.setCellStyle(dataCellStyle);
				//cell.setCellValue(item.getMaterialType().getLabel());
				cell.setCellValue("配件申请");

				col++;
				cell = dataRow.createCell(col);
				cell.setCellStyle(dataCellStyle);
				//cell.setCellValue(item.getProduct()==null?"":StringUtils.toString(item.getProduct().getName()));
				cell.setCellValue(item.getProductNames());

				col++;
				cell = dataRow.createCell(col);
				cell.setCellStyle(dataCellStyle);
				//cell.setCellType(Cell.CELL_TYPE_NUMERIC);
				cell.setCellValue(item.getUserName());

				col++;
				cell = dataRow.createCell(col);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getUserPhone());

				col++;
				cell = dataRow.createCell(col);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getArea()==null?"":item.getArea().getFullName());

				//快递
				col++;
				cell = dataRow.createCell(col);
				cell.setCellStyle(wrapCellStyle);
				cell.setCellValue(item.getUserAddress());

				col++;
				cell = dataRow.createCell(col);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getCreateBy().getName());

				col++;
				cell = dataRow.createCell(col);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(DateUtils.formatDate(item.getCreateDate(),"yyyy-MM-dd HH:mm"));

				col++;
				cell = dataRow.createCell(col);
				cell.setCellStyle(dataCellStyle);
				if(item.getPendingType() != null && item.getPendingType().getIntValue()>0) {
					cell.setCellValue(item.getPendingType().getLabel());
				}else{
					cell.setCellValue("");
				}

				col++;
				cell = dataRow.createCell(col);
				cell.setCellStyle(dataCellStyle);
				if(item.getPendingDate() != null) {
					cell.setCellValue(DateUtils.formatDate(item.getPendingDate(), "yyyy-MM-dd HH:mm"));
				}else{
					cell.setCellValue("");
				}

				col++;
				cell = dataRow.createCell(col);
				cell.setCellStyle(wrapCellStyle);
				cell.setCellValue(StringUtils.toString(item.getPendingContent()));

				//col++;
				//cell = dataRow.createCell(col);
				//cell.setCellStyle(dataCellStyle);
				////cell.setCellStyle(xStyle.get("redData"));
				//cell.setCellValue(item.getReturnFlag()==1?"返件":"否");
			}

			//设置自动列宽
			xSheet.trackAllColumnsForAutoSizing();
			for (int i = 0,size=tableTitle.size(); i < size; i++) {
				xSheet.autoSizeColumn(i);
				//sheet.setColumnWidth(i,sheet.getColumnWidth(i)*17/10);
			}
			xSheet.setColumnWidth(0,10*256);
			if(isCustomer){
				xSheet.setColumnWidth(8, 30 * 256);
				xSheet.setColumnWidth(9, 30 * 256);
				xSheet.setColumnWidth(14, 50 * 256);
			}else {
				xSheet.setColumnWidth(9, 30 * 256);
				xSheet.setColumnWidth(10, 30 * 256);
				xSheet.setColumnWidth(15, 50 * 256);
			}
			//标题合并单元格
			xSheet.addMergedRegion(new CellRangeAddress(0, 0, 0, tableTitle.size()-1));

			response.reset();
			response.setContentType("application/octet-stream; charset=utf-8");
			response.setHeader("Content-Disposition", "attachment; filename=" + Encodes.urlEncode(xName + ".xlsx"));
			xBook.write(response.getOutputStream());
			xBook.dispose();
			return null;//否则报错：getOutputStream() has already been called for this response
		} catch (Exception e)
		{
			addMessage(model, "导出Excel失败：" + e.getMessage());
			return customerApplyList(searchModel,request,response,model);
		}
	}

	//endregion

	//region 待发配件

	/**
	 * 待发货配件列表
	 */
	//@RequiresPermissions(value = { "sd:order:service" })
	@RequestMapping(value = "/customer/tosendlist")
	public String customerToSendList(OrderMaterialSearchModel searchModel, HttpServletRequest request, HttpServletResponse response, Model model) {
		String viewForm = "modules/sd/material/customer/toSendList";
		Page<MaterialMaster> page = new Page<>();
		User user = UserUtils.getUser();
		//状态
		searchModel.setStatus(MaterialMaster.STATUS_APPROVED);
		//检查客户帐号信息
		if (user.isCustomer()) {
			if (user.getCustomerAccountProfile() != null && user.getCustomerAccountProfile().getCustomer() != null) {
				//登录用户的客户，防篡改
				searchModel.setCustomer(user.getCustomerAccountProfile().getCustomer());
			} else {
				addMessage(model, "错误：登录超时，请退出后重新登录。");
				model.addAttribute("canAction", false);
				model.addAttribute("page", page);
				model.addAttribute("searchModel", searchModel);
				return viewForm;
			}
			//客户账号负责的店铺 2021/06/22
			List<String> shopIds = UserUtils.getShopIdsOfCustomerAccount(user);
			if(!CollectionUtils.isEmpty(shopIds)){
				searchModel.setShopIds(shopIds);
			}
		} else if (user.isSaleman()) {
			searchModel.setSales(user.getId());
			searchModel.setSubUserType(user.getSubFlag());//子账号类型
			List<Long> offlineCustomers = customerService.findIdListByOfflineOrderFlagFromCacheForSD();
			if(!org.springframework.util.CollectionUtils.isEmpty(offlineCustomers)){
				searchModel.setOfflineCustomerList(offlineCustomers);
			}
		}
		//date
		if (searchModel.getBeginDate() == null) {
			searchModel.setEndDate(DateUtils.getDateEnd(new Date()));
			searchModel.setBeginDate(DateUtils.getStartDayOfMonth(DateUtils.addMonth(new Date(), -3)));
		} else {
			searchModel.setEndDate(DateUtils.getDateEnd(searchModel.getEndDate()));
		}
		if (!request.getMethod().equalsIgnoreCase("post")) {
			searchModel.setMaterialType(1);//配件申请
		}
		Boolean isValide = checkOrderNoAndPhone(searchModel,model,page);
		if(!isValide){
			return viewForm;
		}
		try {
			//查询
			page = orderMaterialService.findCustomerMaterialList(new Page<OrderMaterialSearchModel>(request, response), searchModel);
			model.addAttribute("canAction", true);
		} catch (Exception e) {
			model.addAttribute("canAction", false);
			addMessage(model, "查询错误：" + e.getMessage());
		}
		model.addAttribute("page", page);
		model.addAttribute("searchModel", searchModel);
		return viewForm;
	}

	/**
	 * 导出
	 */
	@RequestMapping(value = "/customer/tosendlist/export",method = RequestMethod.POST)
	public String exportCustomerToSendList(OrderMaterialSearchModel searchModel, HttpServletRequest request, HttpServletResponse response, RedirectAttributes redirectAttributes, Model model)
	{
		//String viewForm = "modules/sd/material/customer/toSendList";
		try
		{
			User user = UserUtils.getUser();
			if(user==null || user.getId()==null){
				addMessage(model, "登录超时，请重新登录。");
				return customerToSendList(searchModel,request,response,model);
			}
			//检查客户帐号信息
			boolean isCustomer = user.isCustomer();
			if (isCustomer) {
				if (user.getCustomerAccountProfile() != null && user.getCustomerAccountProfile().getCustomer() != null) {
					//登录用户的客户，防篡改
					searchModel.setCustomer(user.getCustomerAccountProfile().getCustomer());
				}
				//客户账号负责的店铺 2021/06/22
				List<String> shopIds = UserUtils.getShopIdsOfCustomerAccount(user);
				if(!CollectionUtils.isEmpty(shopIds)){
					searchModel.setShopIds(shopIds);
				}
			} else if (user.isSaleman()) {
				searchModel.setSales(user.getId());
				searchModel.setSubUserType(user.getSubFlag());//子账号类型
				List<Long> offlineCustomers = customerService.findIdListByOfflineOrderFlagFromCacheForSD();
				if(!org.springframework.util.CollectionUtils.isEmpty(offlineCustomers)){
					searchModel.setOfflineCustomerList(offlineCustomers);
				}
			}
			//date
			if (searchModel.getBeginDate() != null) {
				Date sDate = searchModel.getBeginDate();
				Date eDate = searchModel.getEndDate();
				int monthes = DateUtils.getDateDiffMonth(sDate,eDate);
				if(monthes>12){
					addMessage(model, "导出数据跨度不能超过12个月，请分多次导出。");
					return customerToSendList(searchModel,request,response,model);
				}
			}
			if (searchModel.getBeginDate() == null) {
				searchModel.setEndDate(DateUtils.getDateEnd(new Date()));
				searchModel.setBeginDate(DateUtils.getStartDayOfMonth(DateUtils.addMonth(new Date(), -3)));
			} else {
				searchModel.setEndDate(DateUtils.getDateEnd(searchModel.getEndDate()));
			}
			Page<MaterialMaster> page = new Page<>();
			List<MaterialMaster> materialMasters = Lists.newArrayList();
			//状态
			searchModel.setStatus(MaterialMaster.STATUS_APPROVED);
			Page<OrderMaterialSearchModel> searchModelPage = new Page<>(1,200000);
			try {
				//查询
				//page = orderMaterialService.findCustomerMaterialList(searchModelPage, searchModel);
				materialMasters = orderMaterialService.findMaterialExportList(searchModel);
			} catch (Exception e) {
				addMessage(model, "查询错误：" + e.getMessage());
				return customerToSendList(searchModel,request,response,model);
			}
			List<String> tableTitle = Lists.newArrayList(
					"序号","订单号", "厂商","第三方单号","状态", "用户姓名", "电话",
					"区域", "详细地址", "产品","品牌","型号","配件单号","配件名称","数量",
					"申请人","申请时间", "申请备注","审核人","审核时间","审核备注","跟踪状态",
					"跟踪时间","跟踪内容","是否返件","快递公司","快递单号"
			);
			String xName = "待发货配件列表";
			SXSSFWorkbook xBook;
			SXSSFSheet xSheet;
			Map<String, CellStyle> xStyle;
			ExportExcel exportExcel = new ExportExcel();
			xBook = new SXSSFWorkbook(500);
			xSheet = xBook.createSheet(xName);
			xSheet.setDefaultColumnWidth(15);
			xStyle = exportExcel.createStyles(xBook);

			//region style
			Font headerFont = xBook.createFont();
			headerFont.setFontName("微软雅黑");//Arial
			headerFont.setFontHeightInPoints((short) 12);
			//headerFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
			headerFont.setColor(IndexedColors.GREY_80_PERCENT.getIndex());

			CellStyle headStyle = xBook.createCellStyle();
			headStyle.setFillForegroundColor(IndexedColors.TURQUOISE.getIndex());
			headStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
			headStyle.setAlignment(CellStyle.ALIGN_CENTER);
			headStyle.setFont(headerFont);

			CellStyle titleStyle = xBook.createCellStyle();
			titleStyle.cloneStyleFrom(xStyle.get("title"));
			Font titleFont = xBook.createFont();
			titleFont.setFontName("微软雅黑");//Arial
			titleFont.setFontHeightInPoints((short) 14);
			titleFont.setBold(true);
			titleStyle.setFont(titleFont);

			Font dataFont = xBook.createFont();
			dataFont.setFontName("微软雅黑");//Arial
			dataFont.setFontHeightInPoints((short) 10);

			CellStyle wrapCellStyle = xBook.createCellStyle();
			wrapCellStyle.cloneStyleFrom(xStyle.get("data"));
			wrapCellStyle.setFont(dataFont);
			wrapCellStyle.setWrapText(true);

			CellStyle dataCellStyle = xBook.createCellStyle();
			dataCellStyle.cloneStyleFrom(xStyle.get("data"));
			dataCellStyle.setFont(dataFont);

			// 加入标题
			int rowNum = 0;
			Row titleRow = xSheet.createRow(rowNum++); // 添加一行
			//titleRow.setHeightInPoints(30); // row高度
			Cell titleCell = titleRow.createCell(0); // 对cell(0)编辑, 对应A1
			titleCell.setCellStyle(titleStyle); // cell样式
			titleCell.setCellValue(xName); // 写入cell内容

			//endregion

			Row headRow;
			Row dataRow;
			Cell cell;
			// 加入表头
			headRow = xSheet.createRow(rowNum++);
			for (int i = 0,titleSize=tableTitle.size(); i < titleSize; i++)
			{
				cell = headRow.createCell(i);
				cell.setCellStyle(headStyle);
				cell.setCellValue(tableTitle.get(i));
			}

			xSheet.createFreezePane(0, rowNum); // 冻结单元格(x, y)
			List<MaterialMaster> list = materialMasters;
			MaterialMaster item;
			int idx = 0;
			int col = 0;
			Map<Long,Customer> customerMap = Maps.newHashMap();
			Map<Long,User> userMaps = Maps.newHashMap();
			Map<Long,Area> areaMap = Maps.newHashMap();
			Map<Long,Material> materialMap = Maps.newHashMap();
			Map<Long,Product> productMap = Maps.newHashMap();
			Map<String,Dict> pendingTypes = MSDictUtils.getDictMap("material_pending_type");
			Map<String,Dict> statuses = MSDictUtils.getDictMap("material_apply_status");
			Map<Long,MaterialProduct> materialProductMap = Maps.newHashMap();
			Customer customer=null;
			Dict status = null;
			Area area = null;
			Product product=null;
			Material material = null;
			MaterialProduct materialProduct=null;
			User createBy=null;
			User updateBy=null;
			Dict pendingType = null;
			int firstRow;//合并行开始位置
			int lastRow =0;//合并行结束位置
			int productInfoFirsRow;
			int productInfoLastRow;
			for (int j = 0,length=list.size(); j < length; j++) {
				item = list.get(j);
				if (item == null) {
					continue;
				}
				materialProductMap= item.getProductInfos().stream().collect(Collectors.toMap(t->t.getProduct().getId(), a -> a,(k1,k2)->k1));
				idx++;
				if(customerMap.containsKey(item.getCustomer().getId())){
					customer = customerMap.get(item.getCustomer().getId());
				}else{
					customer = mSCustomerService.getFromCache(item.getCustomer().getId());
					if(customer!=null){
						customerMap.put(customer.getId(),customer);
					}
				}
				status = statuses.get(item.getStatus().getValue());
				if(areaMap.containsKey(item.getArea().getId())){
					area = areaMap.get(item.getArea().getId());
				}else{
					area = areaService.getFromCache(item.getArea().getId());
					if(area!=null){
						areaMap.put(area.getId(),area);
					}
				}
				if(userMaps.containsKey(item.getCreateBy().getId())){
                    createBy = userMaps.get(item.getCreateBy().getId());
				}else{
					createBy = userService.get(item.getCreateBy().getId());
					if(createBy!=null){
						userMaps.put(createBy.getId(),createBy);
					}
				}
				if(userMaps.containsKey(item.getUpdateBy().getId())){
					updateBy = userMaps.get(item.getUpdateBy().getId());
				}else{
					updateBy = userService.get(item.getUpdateBy().getId());
					if(updateBy!=null){
						userMaps.put(updateBy.getId(),updateBy);
					}
				}
				pendingType = pendingTypes.get(item.getPendingType().getValue());
				firstRow = rowNum;
				lastRow = rowNum+item.getItems().size()-1;

				Map<Long, List<MaterialItem>> integerListMap = item.getItems().stream().collect(Collectors.groupingBy(t->t.getProduct().getId()));
				for(Map.Entry<Long, List<MaterialItem>> entry : integerListMap.entrySet()){
					productInfoFirsRow = rowNum;
					productInfoLastRow = rowNum+entry.getValue().size()-1;
					for(MaterialItem materialItem:entry.getValue()){
						if(productMap.containsKey(materialItem.getProduct().getId())){
							product = productMap.get(materialItem.getProduct().getId());
						}else{
							product = productService.getProductByIdFromCache(materialItem.getProduct().getId());
							if(productMap!=null){
								productMap.put(product.getId(),product);
							}
						}
						materialProduct = materialProductMap.get(materialItem.getProduct().getId());
						if(materialMap.containsKey(materialItem.getMaterial().getId())){
							material = materialMap.get(materialItem.getMaterial().getId());
						}else{
							material = materialService.getFromCache(materialItem.getMaterial().getId());
							if(material!=null){
								materialMap.put(material.getId(),material);
							}
						}
						dataRow = xSheet.createRow(rowNum++);
						col = 0;
						cell = dataRow.createCell(col);
						cell.setCellStyle(dataCellStyle);
						cell.setCellValue(idx);

						col++;
						cell = dataRow.createCell(col);
						cell.setCellStyle(dataCellStyle);
						cell.setCellValue(StringUtils.toString(item.getOrderNo()));

						col++;
						cell = dataRow.createCell(col);
						cell.setCellStyle(dataCellStyle);
						cell.setCellValue(customer == null ? "" : StringUtils.toString(customer.getName()));

						col++;
						cell = dataRow.createCell(col);
						cell.setCellStyle(dataCellStyle);
						cell.setCellValue(item.getThrdNo());

						col++;
						cell = dataRow.createCell(col);
						cell.setCellStyle(dataCellStyle);
						cell.setCellValue(status==null?"":status.getLabel());

						col++;
						cell = dataRow.createCell(col);
						cell.setCellStyle(dataCellStyle);
						cell.setCellValue(item.getUserName());

						col++;
						cell = dataRow.createCell(col);
						cell.setCellStyle(dataCellStyle);
						cell.setCellValue(item.getUserPhone());

						col++;
						cell = dataRow.createCell(col);
						cell.setCellStyle(dataCellStyle);
						cell.setCellValue(area==null?"":area.getFullName());

						col++;
						cell = dataRow.createCell(col);
						cell.setCellStyle(wrapCellStyle);
						cell.setCellValue(item.getUserAddress());

						col++;
						cell = dataRow.createCell(col);
						cell.setCellStyle(wrapCellStyle);
						cell.setCellValue(product==null?"":product.getName());

						col++;
						cell = dataRow.createCell(col);
						cell.setCellStyle(wrapCellStyle);
						cell.setCellValue(materialProduct==null?"":materialProduct.getBrand());

						col++;
						cell = dataRow.createCell(col);
						cell.setCellStyle(wrapCellStyle);
						cell.setCellValue(materialProduct==null?"":materialProduct.getProductSpec());

						col++;
						cell = dataRow.createCell(col);
						cell.setCellStyle(wrapCellStyle);
						cell.setCellValue(item.getMasterNo());

						col++;
						cell = dataRow.createCell(col);
						cell.setCellStyle(wrapCellStyle);
						cell.setCellValue(material==null?"":material.getName());

						col++;
						cell = dataRow.createCell(col);
						cell.setCellStyle(wrapCellStyle);
						cell.setCellValue(materialItem.getQty());


						col++;
						cell = dataRow.createCell(col);
						cell.setCellStyle(dataCellStyle);
						cell.setCellValue(createBy==null?"":createBy.getName());

						col++;
						cell = dataRow.createCell(col);
						cell.setCellStyle(dataCellStyle);
						cell.setCellValue(DateUtils.formatDate(item.getCreateDate(),"yyyy-MM-dd HH:mm"));

						col++;
						cell = dataRow.createCell(col);
						cell.setCellStyle(dataCellStyle);
						cell.setCellValue(item.getRemarks());

						col++;
						cell = dataRow.createCell(col);
						cell.setCellStyle(dataCellStyle);
						cell.setCellValue(updateBy==null?"":updateBy.getName());

						col++;
						cell = dataRow.createCell(col);
						cell.setCellStyle(dataCellStyle);
						cell.setCellValue(DateUtils.formatDate(item.getUpdateDate(),"yyyy-MM-dd HH:mm"));

						col++;
						cell = dataRow.createCell(col);
						cell.setCellStyle(wrapCellStyle);
						cell.setCellValue(item.getApproveRemark());

						col++;
						cell = dataRow.createCell(col);
						cell.setCellStyle(dataCellStyle);
						if(pendingType != null && pendingType.getIntValue()>0) {
							cell.setCellValue(pendingType.getLabel());
						}else{
							cell.setCellValue("");
						}

						col++;
						cell = dataRow.createCell(col);
						cell.setCellStyle(dataCellStyle);
						if(item.getPendingDate() != null) {
							cell.setCellValue(DateUtils.formatDate(item.getPendingDate(), "yyyy-MM-dd HH:mm"));
						}else{
							cell.setCellValue("");
						}

						col++;
						cell = dataRow.createCell(col);
						cell.setCellStyle(wrapCellStyle);
						cell.setCellValue(StringUtils.toString(item.getPendingContent()));

						col++;
						cell = dataRow.createCell(col);
						cell.setCellStyle(dataCellStyle);
						cell.setCellValue(item.getReturnFlag()==1?"返件":"否");


						col++;
						cell = dataRow.createCell(col);
						cell.setCellStyle(dataCellStyle);
						cell.setCellValue("");

						col++;
						cell = dataRow.createCell(col);
						cell.setCellStyle(dataCellStyle);
						cell.setCellValue("");
					}
					if(entry.getValue().size()>1){
						xSheet.addMergedRegion(new CellRangeAddress(productInfoFirsRow,productInfoLastRow,9,9));
						xSheet.addMergedRegion(new CellRangeAddress(productInfoFirsRow,productInfoLastRow,10,10));
						xSheet.addMergedRegion(new CellRangeAddress(productInfoFirsRow,productInfoLastRow,11,11));
					}
				}
				//合并行
				if(item.getItems().size()>1){
					List<CellRangeAddress> cellRangeAddresses = Lists.newArrayList();
					cellRangeAddresses.add(new CellRangeAddress(firstRow,lastRow,0,0));
					cellRangeAddresses.add(new CellRangeAddress(firstRow,lastRow,1,1));
					cellRangeAddresses.add(new CellRangeAddress(firstRow,lastRow,2,2));
					cellRangeAddresses.add(new CellRangeAddress(firstRow,lastRow,3,3));
					cellRangeAddresses.add(new CellRangeAddress(firstRow,lastRow,4,4));
					cellRangeAddresses.add(new CellRangeAddress(firstRow,lastRow,5,5));
					cellRangeAddresses.add(new CellRangeAddress(firstRow,lastRow,6,6));
					cellRangeAddresses.add(new CellRangeAddress(firstRow,lastRow,7,7));
					cellRangeAddresses.add(new CellRangeAddress(firstRow,lastRow,8,8));
					cellRangeAddresses.add(new CellRangeAddress(firstRow,lastRow,12,12));
					cellRangeAddresses.add(new CellRangeAddress(firstRow,lastRow,15,15));
					cellRangeAddresses.add(new CellRangeAddress(firstRow,lastRow,16,16));
					cellRangeAddresses.add(new CellRangeAddress(firstRow,lastRow,17,17));
					cellRangeAddresses.add(new CellRangeAddress(firstRow,lastRow,18,18));
					cellRangeAddresses.add(new CellRangeAddress(firstRow,lastRow,19,19));
					cellRangeAddresses.add(new CellRangeAddress(firstRow,lastRow,20,20));
					cellRangeAddresses.add(new CellRangeAddress(firstRow,lastRow,21,21));
					cellRangeAddresses.add(new CellRangeAddress(firstRow,lastRow,22,22));
					cellRangeAddresses.add(new CellRangeAddress(firstRow,lastRow,23,23));
					cellRangeAddresses.add(new CellRangeAddress(firstRow,lastRow,25,25));
					cellRangeAddresses.add(new CellRangeAddress(firstRow,lastRow,26,26));
					for(CellRangeAddress cellRangeAddress:cellRangeAddresses){
						xSheet.addMergedRegion(cellRangeAddress);
					}
				}
			}

			List<Dict> dictList = MSDictUtils.getDictList("express_type");
			String[] expressArray = dictList.stream().map(Dict::getLabel).toArray(String[]::new);
			// 设置快递公司下拉
			exportExcel.setDropdownList(xBook,expressArray,xSheet,2,lastRow,25,1);
			//设置自动列宽
			xSheet.trackAllColumnsForAutoSizing();
			for (int i = 0,size=tableTitle.size(); i < size; i++) {
				xSheet.autoSizeColumn(i);
				//sheet.setColumnWidth(i,sheet.getColumnWidth(i)*17/10);
			}
			xSheet.setColumnWidth(0,5*256);

			xSheet.setColumnWidth(9,30*256);
			xSheet.setColumnWidth(10,30*256);
			xSheet.setColumnWidth(17,30*256);
			xSheet.setColumnWidth(20,50*256);
			xSheet.setColumnWidth(23,50*256);

			//标题合并单元格
			xSheet.addMergedRegion(new CellRangeAddress(0, 0, 0, tableTitle.size()-1));

			response.reset();
			response.setContentType("application/octet-stream; charset=utf-8");
			response.setHeader("Content-Disposition", "attachment; filename=" + Encodes.urlEncode(xName + ".xlsx"));
			xBook.write(response.getOutputStream());
			xBook.dispose();
			return null;//否则报错：getOutputStream() has already been called for this response
		} catch (Exception e)
		{
			log.error("[客户待发配件]导出Excel失败",e);
			addMessage(model, "导出Excel失败：" + e.getMessage());
			return customerToSendList(searchModel,request,response,model);
		}
	}


	/**
	 * 从excel中读取配件单数据
	 * @param file	excel附件
	 */
	@RequestMapping(value = "import/read", method = RequestMethod.POST)
	public String readExcel(MultipartFile file, OrderMaterialSearchModel searchModel, RedirectAttributes redirectAttributes, HttpServletRequest request, Model model)
	{

		if(file == null){
			addMessage(redirectAttributes,"请选择文件．");
			return "redirect:" + Global.getAdminPath() + "/sd/material/customer/tosendlist?repage";
		}
		List<ImportMaterialVM> materialVMList = Lists.newArrayList();
		Map<String,ImportMaterialVM> materialVMMap = Maps.newHashMap();
		try {
			//read excel
			ImportExcel ei = new ImportExcel(file, 1, 0);
			//本次列表中重复
			Row row;
			//检查结果
			String masterNo = new String();
			MaterialMaster dbMaterialMaster=null;
			ImportMaterialVM  materialVM;
			ImportMaterialItemVM materialItemVM;
			//行检查成功标记
			Boolean rowCheckSucess = true;
			StringBuilder rowCheckMsg = new StringBuilder(200);
			for (int i = ei.getDataRowNum(),size = ei.getLastDataRowNum(); i < size; i++) {
				//read row
				row = ei.getRow(i);
				rowCheckSucess = true;
				masterNo = StringUtils.strip(ei.getCellValue(row, 12).toString());
				materialItemVM = new ImportMaterialItemVM();
				if(!materialVMMap.containsKey(masterNo)){
					materialVM = new ImportMaterialVM();
					materialVM.setOrderNo(StringUtils.strip(ei.getCellValue(row, 1).toString()));
					materialVM.setCustomerName(StringUtils.strip(ei.getCellValue(row, 2).toString()));
					materialVM.setThirdNo(StringUtils.strip(ei.getCellValue(row, 3).toString()));
					materialVM.setStatusLabel(StringUtils.strip(ei.getCellValue(row, 4).toString()));
					materialVM.setUserName(StringUtils.strip(ei.getCellValue(row, 5).toString()));
					materialVM.setUserPhone(StringUtils.strip(ei.getCellValue(row, 6).toString()));
					materialVM.setUserArea(StringUtils.strip(ei.getCellValue(row, 7).toString()));
					materialVM.setDetailsArea(StringUtils.strip(ei.getCellValue(row, 8).toString()));
					materialItemVM.setProductName(StringUtils.strip(ei.getCellValue(row, 9).toString()));
					materialItemVM.setBrand(StringUtils.strip(ei.getCellValue(row, 10).toString()));
					materialItemVM.setProductSpace(StringUtils.strip(ei.getCellValue(row, 11).toString()));
					materialVM.setMasterNo(masterNo);
					materialItemVM.setMaterialName(StringUtils.strip(ei.getCellValue(row, 13).toString()));
					materialItemVM.setQyt(Integer.valueOf(StringUtils.strip(ei.getCellValue(row, 14).toString())));
					materialVM.setApplicant(StringUtils.strip(ei.getCellValue(row, 15).toString()));
					materialVM.setApplyDate(StringUtils.strip(ei.getCellValue(row, 16).toString()));
					materialVM.setApplyRemark(StringUtils.strip(ei.getCellValue(row, 17).toString()));
					materialVM.setReviewer(StringUtils.strip(ei.getCellValue(row, 18).toString()));
					materialVM.setApproveTime(StringUtils.strip(ei.getCellValue(row, 19).toString()));
					materialVM.setApproveRemark(StringUtils.strip(ei.getCellValue(row, 20).toString()));
					materialVM.setPendingLabel(StringUtils.strip(ei.getCellValue(row, 21).toString()));
					materialVM.setPendingTime(StringUtils.strip(ei.getCellValue(row, 22).toString()));
					materialVM.setPendingContent(StringUtils.strip(ei.getCellValue(row, 23).toString()));
					materialItemVM.setReturnFlagLabel(StringUtils.strip(ei.getCellValue(row, 24).toString()));

					//用户名为空的订单，忽略
					if(StringUtils.isBlank(masterNo)){
						rowCheckMsg.append("获取配件单号失败,请检查");
						rowCheckSucess = false;
					}
					String dbStatusValue="";
					dbMaterialMaster = orderMaterialService.getByMasterNo(masterNo);
					if(dbMaterialMaster!=null && dbMaterialMaster.getId()!=null && dbMaterialMaster.getId()>0){
						//materialMaster.setId(dbMaterialMaster.getId());
						//materialMaster.setQuarter(dbMaterialMaster.getQuarter());
						materialVM.setId(dbMaterialMaster.getId());
						materialVM.setQuarter(dbMaterialMaster.getQuarter());
						dbStatusValue = dbMaterialMaster.getStatus().getValue();
					/*materialMaster.setCreateDate(dbMaterialMaster.getCreateDate());
					materialMaster.setUpdateDate(dbMaterialMaster.getUpdateDate());
					materialMaster.setPendingDate(dbMaterialMaster.getPendingDate());*/
					}else{
						rowCheckMsg.append(rowCheckSucess?"":"<br/>").append("获取配件失败,请检查配件单号是否正确");
						rowCheckSucess = false;
					}
					String statusLabel = materialVM.getStatusLabel();
					Dict status = MSDictUtils.getDictByValue(dbStatusValue,"material_apply_status");
					if(status==null){
						rowCheckMsg.append(rowCheckSucess?"":"<br/>").append("获取最新配件单状态失败");
						rowCheckSucess = false;
					}else if(!status.getLabel().equals(statusLabel)){
						rowCheckMsg.append(rowCheckSucess?"":"<br/>").append("配件单最新状态为:"+status.getLabel());
						rowCheckSucess = false;
					}
					//materialMaster.setStatus(new Dict("0",statusLabel));
					String expressCompanyLabel = StringUtils.strip(ei.getCellValue(row, 25).toString());
					String expressCompanyValue = MSDictUtils.getDictValue(expressCompanyLabel,"express_type","");
					if(StringUtils.isBlank(expressCompanyValue)){
						rowCheckMsg.append(rowCheckSucess?"":"<br/>").append("缺少快递公司或系统无快递公司:"+expressCompanyLabel);
						rowCheckSucess = false;
					}
					String expressNo =  StringUtils.strip(ei.getCellValue(row, 26).toString());
					if(StringUtils.isBlank(expressNo)){
						rowCheckMsg.append(rowCheckSucess?"":"<br/>").append("缺少快递单号");
						rowCheckSucess = false;
					}else{
						//materialMaster.setExpressNo(expressNo);
						materialVM.setExpressNo(expressNo);
					}
					//materialMaster.setExpressCompany(new Dict(expressCompanyValue,expressCompanyLabel));
					materialVM.setExpressCompany(new Dict(expressCompanyValue,expressCompanyLabel));
					//materialMaster.setRemarks(rowCheckMsg.toString());
					materialVM.setCheckMessage(rowCheckMsg.toString());
					if(!rowCheckSucess){
						//materialMaster.setDelFlag(1);
						materialVM.setSuccessFlag(1);
					}
					//materialMasters.add(materialMaster);
					materialVM.getItems().add(materialItemVM);
					if(StringUtils.isNotBlank(masterNo)){
						materialVMList.add(materialVM);
						materialVMMap.put(masterNo,materialVM);
					}else{
						materialVMList.add(materialVM);
					}
					rowCheckMsg.setLength(0);
				}else{
					materialItemVM.setProductName(StringUtils.strip(ei.getCellValue(row, 9).toString()));
					materialItemVM.setBrand(StringUtils.strip(ei.getCellValue(row, 10).toString()));
					materialItemVM.setProductSpace(StringUtils.strip(ei.getCellValue(row, 11).toString()));
					materialItemVM.setMaterialName(StringUtils.strip(ei.getCellValue(row, 13).toString()));
					materialItemVM.setQyt(Integer.valueOf(StringUtils.strip(ei.getCellValue(row, 14).toString())));
					materialItemVM.setReturnFlagLabel(StringUtils.strip(ei.getCellValue(row, 24).toString()));
					//materialVMMap.get(masterNo).getItems().add(materialItemVM);
					final String finalMasterNo = masterNo;
					materialVM = materialVMList.stream().filter(t->t.getMasterNo().equals(finalMasterNo)).findFirst().orElse(null);
					if(materialVM!=null){
						materialVM.getItems().add(materialItemVM);
					}
				}
			}
			//List<ImportMaterialVM> mapToList = new ArrayList<>(materialVMMap.values());
			//materialVMList.addAll(mapToList);
			if (materialVMList.isEmpty()) {
				addMessage(redirectAttributes, "读取数据完成，但无符合的订单，请确认！");
				return "redirect:" + Global.getAdminPath() + "/sd/material/customer/tosendlist?repage";
			}

		} catch (Exception e)
		{
			addMessage(redirectAttributes, "读取配件单失败："+ e.getMessage());
			return "redirect:" + Global.getAdminPath() + "/sd/material/customer/tosendlist?repage";
		}
		model.addAttribute("materialMasters",materialVMList);
		model.addAttribute("searchModel",searchModel);
		return "modules/sd/material/customer/importToSendList";
	}

	/**
	 * 批量发货
	 * @param materialMasters
	 */
	@RequestMapping("batchSend")
	@ResponseBody
	public AjaxJsonEntity batchSaveExpress(@RequestBody List<MaterialMaster> materialMasters){
		AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
		if(materialMasters==null || materialMasters.size()<=0){
			ajaxJsonEntity.setSuccess(false);
			ajaxJsonEntity.setMessage("没选中要保存的数据");
			return ajaxJsonEntity;
		}
		User user = UserUtils.getUser();
		if(user==null || user.getId()==null || user.getId()<=0){
			ajaxJsonEntity.setSuccess(false);
			ajaxJsonEntity.setMessage("用户不存在,请重新登录");
			return ajaxJsonEntity;
		}
		try {
			orderMaterialService.batchSaveExpress(materialMasters,user);
		}catch (Exception e){
			ajaxJsonEntity.setSuccess(false);
			ajaxJsonEntity.setMessage(e.getMessage());
		}
		return ajaxJsonEntity;
	}

	//endregion

	//region 已发货

	/**
	 * 已发货配件列表
	 */
	//@RequiresPermissions(value = { "sd:order:service" })
	@RequestMapping(value = "/customer/sendlist")
	public String customerSendList(OrderMaterialSearchModel searchModel, HttpServletRequest request, HttpServletResponse response, Model model) {
		String viewForm = "modules/sd/material/customer/sendList";
		Page<MaterialMaster> page = new Page<>();
		User user = UserUtils.getUser();
		//状态
		searchModel.setStatus(MaterialMaster.STATUS_SENDED);
		//检查客户帐号信息
		if (user.isCustomer()) {
			if (user.getCustomerAccountProfile() != null && user.getCustomerAccountProfile().getCustomer() != null) {
				//登录用户的客户，防篡改
				searchModel.setCustomer(user.getCustomerAccountProfile().getCustomer());
			} else {
				addMessage(model, "错误：登录超时，请退出后重新登录。");
				model.addAttribute("canAction", false);
				model.addAttribute("page", page);
				model.addAttribute("searchModel", searchModel);
				return viewForm;
			}
			//客户账号负责的店铺 2021/06/22
			List<String> shopIds = UserUtils.getShopIdsOfCustomerAccount(user);
			if(!CollectionUtils.isEmpty(shopIds)){
				searchModel.setShopIds(shopIds);
			}
		} else if (user.isSaleman()) {
			searchModel.setSales(user.getId());
			searchModel.setSubUserType(user.getSubFlag());//子账号类型
			List<Long> offlineCustomers = customerService.findIdListByOfflineOrderFlagFromCacheForSD();
			if(!org.springframework.util.CollectionUtils.isEmpty(offlineCustomers)){
				searchModel.setOfflineCustomerList(offlineCustomers);
			}
		}
		//date
		if (searchModel.getBeginDate() == null) {
			searchModel.setEndDate(DateUtils.getDateEnd(new Date()));
			searchModel.setBeginDate(DateUtils.getStartDayOfMonth(DateUtils.addMonth(new Date(), -3)));
		} else {
			searchModel.setEndDate(DateUtils.getDateEnd(searchModel.getEndDate()));
		}
		if (!request.getMethod().equalsIgnoreCase("post")) {
			searchModel.setMaterialType(1);//配件申请
		}
		Boolean isValide = checkOrderNoAndPhone(searchModel,model,page);
		if(!isValide){
			return viewForm;
		}
		try {
			//查询
			page = orderMaterialService.findCustomerMaterialList(new Page<OrderMaterialSearchModel>(request, response), searchModel);
			model.addAttribute("canAction", true);
		} catch (Exception e) {
			model.addAttribute("canAction", false);
			addMessage(model, "查询错误：" + e.getMessage());
		}
		model.addAttribute("page", page);
		model.addAttribute("searchModel", searchModel);
		return viewForm;
	}


	/**
	 * 导出
	 */
	@RequestMapping(value = "/customer/sendlist/export",method = RequestMethod.POST)
	public String exportCustomerSendList(OrderMaterialSearchModel searchModel, HttpServletRequest request, HttpServletResponse response, RedirectAttributes redirectAttributes, Model model)
	{
		//String viewForm = "modules/sd/material/customer/sendList";
		try
		{
			User user = UserUtils.getUser();
			if(user==null || user.getId()==null){
				addMessage(model, "登录超时，请重新登录。");
				return customerSendList(searchModel,request,response,model);
			}
			//date
			if (searchModel.getBeginDate() != null) {
				Date sDate = searchModel.getBeginDate();
				Date eDate = searchModel.getEndDate();
				int monthes = DateUtils.getDateDiffMonth(sDate,eDate);
				if(monthes>12){
					addMessage(model, "导出数据跨度不能超过12个月，请分多次导出。");
					return customerSendList(searchModel,request,response,model);
				}
			}
			//检查客户帐号信息
			boolean isCustomer = user.isCustomer();
			if (isCustomer) {
				if (user.getCustomerAccountProfile() != null && user.getCustomerAccountProfile().getCustomer() != null) {
					//登录用户的客户，防篡改
					searchModel.setCustomer(user.getCustomerAccountProfile().getCustomer());
				}
				//客户账号负责的店铺 2021/06/22
				List<String> shopIds = UserUtils.getShopIdsOfCustomerAccount(user);
				if(!CollectionUtils.isEmpty(shopIds)){
					searchModel.setShopIds(shopIds);
				}
			} else if (user.isSaleman()) {
				searchModel.setSales(user.getId());
				searchModel.setSubUserType(user.getSubFlag());//子账号类型
				List<Long> offlineCustomers = customerService.findIdListByOfflineOrderFlagFromCacheForSD();
				if(!org.springframework.util.CollectionUtils.isEmpty(offlineCustomers)){
					searchModel.setOfflineCustomerList(offlineCustomers);
				}
			}
			if (searchModel.getBeginDate() == null) {
				searchModel.setEndDate(DateUtils.getDateEnd(new Date()));
				searchModel.setBeginDate(DateUtils.getStartDayOfMonth(DateUtils.addMonth(new Date(), -3)));
			} else {
				searchModel.setEndDate(DateUtils.getDateEnd(searchModel.getEndDate()));
			}
			Page<MaterialMaster> page = new Page<>();
			//状态
			searchModel.setStatus(MaterialMaster.STATUS_SENDED);
			Page<OrderMaterialSearchModel> searchModelPage = new Page<>(1,200000);
			try {
				//查询
				page = orderMaterialService.findCustomerMaterialList(searchModelPage, searchModel);
			} catch (Exception e) {
				addMessage(model, "查询错误：" + e.getMessage());
				return customerSendList(searchModel,request,response,model);
			}
			List<String> tableTitle = Lists.newArrayList(
					"序号","订单号", "状态", "类型", "配件类型", "产品",
					"用户姓名", "电话", "区域", "详细地址", "申请人","申请时间",
					"发货人","发货时间","跟踪状态","跟踪时间","跟踪内容","是否返件"
			);
			if(!isCustomer){
				tableTitle.add(2,"厂商");
			}
			String xName = "已发货配件列表";
			SXSSFWorkbook xBook;
			SXSSFSheet xSheet;
			Map<String, CellStyle> xStyle;
			ExportExcel exportExcel = new ExportExcel();
			xBook = new SXSSFWorkbook(500);
			xSheet = xBook.createSheet(xName);
			xSheet.setDefaultColumnWidth(15);
			xStyle = exportExcel.createStyles(xBook);

			//region style
			Font headerFont = xBook.createFont();
			headerFont.setFontName("微软雅黑");//Arial
			headerFont.setFontHeightInPoints((short) 12);
			//headerFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
			headerFont.setColor(IndexedColors.GREY_80_PERCENT.getIndex());

			CellStyle headStyle = xBook.createCellStyle();
			//XSSFColor xssfColor = new XSSFColor(new java.awt.Color(191,215,237));
			//headStyle.setFillForegroundColor(xssfColor.getIndex());
			headStyle.setFillForegroundColor(IndexedColors.TURQUOISE.getIndex());
			headStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
			headStyle.setAlignment(CellStyle.ALIGN_CENTER);
			headStyle.setFont(headerFont);

			CellStyle titleStyle = xBook.createCellStyle();
			titleStyle.cloneStyleFrom(xStyle.get("title"));
			Font titleFont = xBook.createFont();
			titleFont.setFontName("微软雅黑");//Arial
			titleFont.setFontHeightInPoints((short) 14);
			titleFont.setBold(true);
			titleStyle.setFont(titleFont);

			Font dataFont = xBook.createFont();
			dataFont.setFontName("微软雅黑");//Arial
			dataFont.setFontHeightInPoints((short) 10);

			CellStyle wrapCellStyle = xBook.createCellStyle();
			wrapCellStyle.cloneStyleFrom(xStyle.get("data"));
			wrapCellStyle.setFont(dataFont);
			wrapCellStyle.setWrapText(true);

			CellStyle dataCellStyle = xBook.createCellStyle();
			dataCellStyle.cloneStyleFrom(xStyle.get("data"));
			dataCellStyle.setFont(dataFont);

			// 加入标题
			int rowNum = 0;
			Row titleRow = xSheet.createRow(rowNum++); // 添加一行
			//titleRow.setHeightInPoints(30); // row高度
			Cell titleCell = titleRow.createCell(0); // 对cell(0)编辑, 对应A1
			titleCell.setCellStyle(titleStyle); // cell样式
			titleCell.setCellValue(xName); // 写入cell内容

			//endregion

			Row headRow;
			Row dataRow;
			Cell cell;
			// 加入表头
			headRow = xSheet.createRow(rowNum++);
			for (int i = 0,titleSize=tableTitle.size(); i < titleSize; i++)
			{
				cell = headRow.createCell(i);
				cell.setCellStyle(headStyle);
				cell.setCellValue(tableTitle.get(i));
			}

			xSheet.createFreezePane(0, rowNum); // 冻结单元格(x, y)
			List<MaterialMaster> list = page.getList();
			MaterialMaster item;
			int idx = 0;
			int col = 0;
			for (int j = 0,length=list.size(); j < length; j++) {
				item = list.get(j);
				if (item == null) {
					continue;
				}
				idx++;
				dataRow = xSheet.createRow(rowNum++);
				//dataRow.setHeightInPoints(12);
				col = 0;
				cell = dataRow.createCell(col);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(idx);

				col++;
				cell = dataRow.createCell(col);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(StringUtils.toString(item.getOrderNo()));

				if(!isCustomer) {
					col++;
					cell = dataRow.createCell(col);
					cell.setCellStyle(dataCellStyle);
					cell.setCellValue(item.getCustomer() == null ? "" : StringUtils.toString(item.getCustomer().getName()));
				}

				col++;
				cell = dataRow.createCell(col);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getStatus().getLabel());

				col++;
				cell = dataRow.createCell(col);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getApplyType().getLabel());

				col++;
				cell = dataRow.createCell(col);
				cell.setCellStyle(dataCellStyle);
				//cell.setCellValue(item.getMaterialType().getLabel());
				cell.setCellValue("配件申请");

				col++;
				cell = dataRow.createCell(col);
				cell.setCellStyle(dataCellStyle);
				//cell.setCellValue(item.getProduct()==null?"":StringUtils.toString(item.getProduct().getName()));
				cell.setCellValue(item.getProductNames());

				col++;
				cell = dataRow.createCell(col);
				cell.setCellStyle(dataCellStyle);
				//cell.setCellType(Cell.CELL_TYPE_NUMERIC);
				cell.setCellValue(item.getUserName());

				col++;
				cell = dataRow.createCell(col);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getUserPhone());

				col++;
				cell = dataRow.createCell(col);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getArea()==null?"":item.getArea().getFullName());

				//快递
				col++;
				cell = dataRow.createCell(col);
				cell.setCellStyle(wrapCellStyle);
				cell.setCellValue(item.getUserAddress());

				col++;
				cell = dataRow.createCell(col);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getCreateBy().getName());

				col++;
				cell = dataRow.createCell(col);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(DateUtils.formatDate(item.getCreateDate(),"yyyy-MM-dd HH:mm"));

				col++;
				cell = dataRow.createCell(col);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getUpdateBy().getName());

				col++;
				cell = dataRow.createCell(col);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(DateUtils.formatDate(item.getUpdateDate(),"yyyy-MM-dd HH:mm"));

				col++;
				cell = dataRow.createCell(col);
				cell.setCellStyle(dataCellStyle);
				if(item.getPendingType() != null && item.getPendingType().getIntValue()>0) {
					cell.setCellValue(item.getPendingType().getLabel());
				}else{
					cell.setCellValue("");
				}

				col++;
				cell = dataRow.createCell(col);
				cell.setCellStyle(dataCellStyle);
				if(item.getPendingDate() != null) {
					cell.setCellValue(DateUtils.formatDate(item.getPendingDate(), "yyyy-MM-dd HH:mm"));
				}else{
					cell.setCellValue("");
				}

				col++;
				cell = dataRow.createCell(col);
				cell.setCellStyle(wrapCellStyle);
				cell.setCellValue(StringUtils.toString(item.getPendingContent()));

				col++;
				cell = dataRow.createCell(col);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getReturnFlag()==1?"返件":"否");
			}

			//设置自动列宽
			xSheet.trackAllColumnsForAutoSizing();
			for (int i = 0,size=tableTitle.size(); i < size; i++) {
				xSheet.autoSizeColumn(i);
				//sheet.setColumnWidth(i,sheet.getColumnWidth(i)*17/10);
			}
			xSheet.setColumnWidth(0,10*256);
			if(isCustomer){
				xSheet.setColumnWidth(8,30*256);
				xSheet.setColumnWidth(9,30*256);
				xSheet.setColumnWidth(16,50*256);
			}else {

				xSheet.setColumnWidth(9,30*256);
				xSheet.setColumnWidth(10,30*256);
				xSheet.setColumnWidth(17,50*256);
			}

			//标题合并单元格
			xSheet.addMergedRegion(new CellRangeAddress(0, 0, 0, tableTitle.size()-1));

			response.reset();
			response.setContentType("application/octet-stream; charset=utf-8");
			response.setHeader("Content-Disposition", "attachment; filename=" + Encodes.urlEncode(xName + ".xlsx"));
			xBook.write(response.getOutputStream());
			xBook.dispose();
			return null;//否则报错：getOutputStream() has already been called for this response
		} catch (Exception e)
		{
			addMessage(model, "导出失败：" + e.getMessage());
			return customerSendList(searchModel,request,response,model);
		}
	}

	//endregion

	//region 驳回

	/**
	 * 驳回列表
	 */
	//@RequiresPermissions(value = { "sd:order:service" })
	@RequestMapping(value = "/customer/rejectlist")
	public String customerRejectList(OrderMaterialSearchModel searchModel, HttpServletRequest request, HttpServletResponse response, Model model) {
		String viewForm = "modules/sd/material/customer/rejectList";
		Page<MaterialMaster> page = new Page<>();
		User user = UserUtils.getUser();
		//状态
		searchModel.setStatus(MaterialMaster.STATUS_REJECT);
		//检查客户帐号信息
		if (user.isCustomer()) {
			if (user.getCustomerAccountProfile() != null && user.getCustomerAccountProfile().getCustomer() != null) {
				//登录用户的客户，防篡改
				searchModel.setCustomer(user.getCustomerAccountProfile().getCustomer());
			} else {
				addMessage(model, "错误：登录超时，请退出后重新登录。");
				model.addAttribute("canAction", false);
				model.addAttribute("page", page);
				model.addAttribute("searchModel", searchModel);
				return viewForm;
			}
			//客户账号负责的店铺 2021/06/22
			List<String> shopIds = UserUtils.getShopIdsOfCustomerAccount(user);
			if(!CollectionUtils.isEmpty(shopIds)){
				searchModel.setShopIds(shopIds);
			}
		} else if (user.isSaleman()) {
			searchModel.setSales(user.getId());
			searchModel.setSubUserType(user.getSubFlag());//子账号类型
			List<Long> offlineCustomers = customerService.findIdListByOfflineOrderFlagFromCacheForSD();
			if(!org.springframework.util.CollectionUtils.isEmpty(offlineCustomers)){
				searchModel.setOfflineCustomerList(offlineCustomers);
			}
		}
		//date
		if (searchModel.getBeginDate() == null) {
			searchModel.setEndDate(DateUtils.getDateEnd(new Date()));
			searchModel.setBeginDate(DateUtils.getStartDayOfMonth(DateUtils.addMonth(new Date(), -3)));
		} else {
			searchModel.setEndDate(DateUtils.getDateEnd(searchModel.getEndDate()));
		}
		if (!request.getMethod().equalsIgnoreCase("post")) {
			searchModel.setMaterialType(1);//配件申请
		}
		Boolean isValide = checkOrderNoAndPhone(searchModel,model,page);
		if(!isValide){
			return viewForm;
		}
		try {
			//查询
			page = orderMaterialService.findCustomerMaterialList(new Page<OrderMaterialSearchModel>(request, response), searchModel);
			model.addAttribute("canAction", true);
		} catch (Exception e) {
			model.addAttribute("canAction", false);
			addMessage(model, "查询错误：" + e.getMessage());
		}
		model.addAttribute("page", page);
		model.addAttribute("searchModel", searchModel);
		return viewForm;
	}


	/**
	 * 导出
	 */
	@RequestMapping(value = "/customer/rejectlist/export",method = RequestMethod.POST)
	public String exportCustomerRejectList(OrderMaterialSearchModel searchModel, HttpServletRequest request, HttpServletResponse response, RedirectAttributes redirectAttributes, Model model)
	{
		//String viewForm = "modules/sd/material/customer/rejectList";
		try
		{
			User user = UserUtils.getUser();
			if(user==null || user.getId()==null){
				addMessage(model, "登录超时，请重新登录。");
				return customerRejectList(searchModel,request,response,model);
			}
			//date
			if (searchModel.getBeginDate() != null) {
				Date sDate = searchModel.getBeginDate();
				Date eDate = searchModel.getEndDate();
				int monthes = DateUtils.getDateDiffMonth(sDate,eDate);
				if(monthes>12){
					addMessage(model, "导出数据跨度不能超过12个月，请分多次导出。");
					return customerRejectList(searchModel,request,response,model);
				}
			}
			//检查客户帐号信息
			boolean isCustomer = user.isCustomer();
			if (isCustomer) {
				if (user.getCustomerAccountProfile() != null && user.getCustomerAccountProfile().getCustomer() != null) {
					//登录用户的客户，防篡改
					searchModel.setCustomer(user.getCustomerAccountProfile().getCustomer());
				}
				//客户账号负责的店铺 2021/06/22
				List<String> shopIds = UserUtils.getShopIdsOfCustomerAccount(user);
				if(!CollectionUtils.isEmpty(shopIds)){
					searchModel.setShopIds(shopIds);
				}
			} else if (user.isSaleman()) {
				searchModel.setSales(user.getId());
				searchModel.setSubUserType(user.getSubFlag());//子账号类型
				List<Long> offlineCustomers = customerService.findIdListByOfflineOrderFlagFromCacheForSD();
				if(!org.springframework.util.CollectionUtils.isEmpty(offlineCustomers)){
					searchModel.setOfflineCustomerList(offlineCustomers);
				}
			}
			if (searchModel.getBeginDate() == null) {
				searchModel.setEndDate(DateUtils.getDateEnd(new Date()));
				searchModel.setBeginDate(DateUtils.getStartDayOfMonth(DateUtils.addMonth(new Date(), -3)));
			} else {
				searchModel.setEndDate(DateUtils.getDateEnd(searchModel.getEndDate()));
			}
			Page<MaterialMaster> page = new Page<>();
			//状态
			searchModel.setStatus(MaterialMaster.STATUS_REJECT);
			Page<OrderMaterialSearchModel> searchModelPage = new Page<>(1,200000);
			try {
				//查询
				page = orderMaterialService.findCustomerMaterialList(searchModelPage, searchModel);
			} catch (Exception e) {
				addMessage(model, "导出失败：" + e.getMessage());
				return customerRejectList(searchModel,request,response,model);
			}
			List<String> tableTitle = Lists.newArrayList(
					"序号","订单号", "状态", "类型", "配件类型", "产品",
					"用户姓名", "电话", "区域", "详细地址", "申请人","申请时间",
					"审核人","驳回时间","驳回原因","详细描述","跟踪状态","跟踪时间","跟踪内容"
			);
			if(!isCustomer){
				tableTitle.add(2,"厂商");
			}

			String xName = "驳回配件列表";
			SXSSFWorkbook xBook;
			SXSSFSheet xSheet;
			Map<String, CellStyle> xStyle;
			ExportExcel exportExcel = new ExportExcel();
			xBook = new SXSSFWorkbook(500);
			xSheet = xBook.createSheet(xName);
			xSheet.setDefaultColumnWidth(15);
			xStyle = exportExcel.createStyles(xBook);

			//region style
			Font headerFont = xBook.createFont();
			headerFont.setFontName("微软雅黑");//Arial
			headerFont.setFontHeightInPoints((short) 12);
			//headerFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
			headerFont.setColor(IndexedColors.GREY_80_PERCENT.getIndex());

			CellStyle headStyle = xBook.createCellStyle();
			//XSSFColor xssfColor = new XSSFColor(new java.awt.Color(191,215,237));
			//headStyle.setFillForegroundColor(xssfColor.getIndex());
			headStyle.setFillForegroundColor(IndexedColors.TURQUOISE.getIndex());
			headStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
			headStyle.setAlignment(CellStyle.ALIGN_CENTER);
			headStyle.setFont(headerFont);

			CellStyle titleStyle = xBook.createCellStyle();
			titleStyle.cloneStyleFrom(xStyle.get("title"));
			Font titleFont = xBook.createFont();
			titleFont.setFontName("微软雅黑");//Arial
			titleFont.setFontHeightInPoints((short) 14);
			titleFont.setBold(true);
			titleStyle.setFont(titleFont);

			Font dataFont = xBook.createFont();
			dataFont.setFontName("微软雅黑");//Arial
			dataFont.setFontHeightInPoints((short) 10);

			CellStyle wrapCellStyle = xBook.createCellStyle();
			wrapCellStyle.cloneStyleFrom(xStyle.get("data"));
			wrapCellStyle.setFont(dataFont);
			wrapCellStyle.setWrapText(true);

			CellStyle dataCellStyle = xBook.createCellStyle();
			dataCellStyle.cloneStyleFrom(xStyle.get("data"));
			dataCellStyle.setFont(dataFont);

			// 加入标题
			int rowNum = 0;
			Row titleRow = xSheet.createRow(rowNum++); // 添加一行
			//titleRow.setHeightInPoints(30); // row高度
			Cell titleCell = titleRow.createCell(0); // 对cell(0)编辑, 对应A1
			titleCell.setCellStyle(titleStyle); // cell样式
			titleCell.setCellValue(xName); // 写入cell内容

			//endregion

			Row headRow;
			Row dataRow;
			Cell cell;
			// 加入表头
			headRow = xSheet.createRow(rowNum++);
			for (int i = 0,titleSize=tableTitle.size(); i < titleSize; i++)
			{
				cell = headRow.createCell(i);
				cell.setCellStyle(headStyle);
				cell.setCellValue(tableTitle.get(i));
			}

			xSheet.createFreezePane(0, rowNum); // 冻结单元格(x, y)
			List<MaterialMaster> list = page.getList();
			MaterialMaster item;
			int idx = 0;
			int col = 0;
			for (int j = 0,length=list.size(); j < length; j++) {
				item = list.get(j);
				if (item == null) {
					continue;
				}
				idx++;
				dataRow = xSheet.createRow(rowNum++);

				col = 0;
				cell = dataRow.createCell(col);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(idx);

				col++;
				cell = dataRow.createCell(col);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(StringUtils.toString(item.getOrderNo()));

				if(!isCustomer) {
					col++;
					cell = dataRow.createCell(col);
					cell.setCellStyle(dataCellStyle);
					cell.setCellValue(item.getCustomer() == null ? "" : StringUtils.toString(item.getCustomer().getName()));
				}

				col++;
				cell = dataRow.createCell(col);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getStatus().getLabel());

				col++;
				cell = dataRow.createCell(col);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getApplyType().getLabel());

				col++;
				cell = dataRow.createCell(col);
				cell.setCellStyle(dataCellStyle);
				//cell.setCellValue(item.getMaterialType().getLabel());
				cell.setCellValue("配件申请");

				col++;
				cell = dataRow.createCell(col);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getProductNames());

				col++;
				cell = dataRow.createCell(col);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getUserName());

				col++;
				cell = dataRow.createCell(col);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getUserPhone());

				col++;
				cell = dataRow.createCell(col);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getArea()==null?"":item.getArea().getFullName());

				//快递
				col++;
				cell = dataRow.createCell(col);
				cell.setCellStyle(wrapCellStyle);
				cell.setCellValue(item.getUserAddress());

				col++;
				cell = dataRow.createCell(col);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getCreateBy().getName());

				col++;
				cell = dataRow.createCell(col);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(DateUtils.formatDate(item.getCreateDate(),"yyyy-MM-dd HH:mm"));

				col++;
				cell = dataRow.createCell(col);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getUpdateBy().getName());

				col++;
				cell = dataRow.createCell(col);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(DateUtils.formatDate(item.getUpdateDate(),"yyyy-MM-dd HH:mm"));

				//驳回原因
				col++;
				cell = dataRow.createCell(col);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getCloseType());

				col++;
				cell = dataRow.createCell(col);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getCloseRemark());

				col++;
				cell = dataRow.createCell(col);
				cell.setCellStyle(dataCellStyle);
				if(item.getPendingType() != null && item.getPendingType().getIntValue()>0) {
					cell.setCellValue(item.getPendingType().getLabel());
				}else{
					cell.setCellValue("");
				}

				col++;
				cell = dataRow.createCell(col);
				cell.setCellStyle(dataCellStyle);
				if(item.getPendingDate() != null) {
					cell.setCellValue(DateUtils.formatDate(item.getPendingDate(), "yyyy-MM-dd HH:mm"));
				}else{
					cell.setCellValue("");
				}

				col++;
				cell = dataRow.createCell(col);
				cell.setCellStyle(wrapCellStyle);
				cell.setCellValue(StringUtils.toString(item.getPendingContent()));

				//col++;
				//cell = dataRow.createCell(col);
				//cell.setCellStyle(dataCellStyle);
				//cell.setCellValue(item.getReturnFlag()==1?"返件":"否");
			}

			//设置自动列宽
			xSheet.trackAllColumnsForAutoSizing();
			for (int i = 0,size=tableTitle.size(); i < size; i++) {
				xSheet.autoSizeColumn(i);
				//sheet.setColumnWidth(i,sheet.getColumnWidth(i)*17/10);
			}
			xSheet.setColumnWidth(0,10*256);
			if(isCustomer){
				xSheet.setColumnWidth(8,30*256);
				xSheet.setColumnWidth(9,30*256);
				xSheet.setColumnWidth(16,50*256);
			}else {

				xSheet.setColumnWidth(9,30*256);
				xSheet.setColumnWidth(10,30*256);
				xSheet.setColumnWidth(17,50*256);
			}

			//标题合并单元格
			xSheet.addMergedRegion(new CellRangeAddress(0, 0, 0, tableTitle.size()-1));

			response.reset();
			response.setContentType("application/octet-stream; charset=utf-8");
			response.setHeader("Content-Disposition", "attachment; filename=" + Encodes.urlEncode(xName + ".xlsx"));
			xBook.write(response.getOutputStream());
			xBook.dispose();
			return null;//否则报错：getOutputStream() has already been called for this response
		} catch (Exception e)
		{
			addMessage(model, "导出失败：" + e.getMessage());
			return customerRejectList(searchModel,request,response,model);
			//response.reset();
			//response.setContentType("application/javascript; charset=utf-8");
			//try {
			//	response.getWriter().write("alert('导出Excel失败！失败信息：" +  e.getMessage() + "');");
			//	response.getWriter().close();
			//}catch (Exception oe){
			//	log.error("导出Excel错误",oe);
			//}
			////addMessage(model, "导出Excel失败！失败信息：" + e.getMessage());
			//return viewForm;
		}
	}

	//endregion

	//region 完成

	/**
	 * 完成列表
	 */
	//@RequiresPermissions(value = { "sd:order:service" })
	@RequestMapping(value = "/customer/closelist")
	public String customerCloseList(OrderMaterialSearchModel searchModel, HttpServletRequest request, HttpServletResponse response, Model model) {
		String viewForm = "modules/sd/material/customer/closeList";
		Page<MaterialMaster> page = new Page<>();
		User user = UserUtils.getUser();
		//状态
		searchModel.setStatus(MaterialMaster.STATUS_CLOSED);
		//检查客户帐号信息
		if (user.isCustomer()) {
			if (user.getCustomerAccountProfile() != null && user.getCustomerAccountProfile().getCustomer() != null) {
				//登录用户的客户，防篡改
				searchModel.setCustomer(user.getCustomerAccountProfile().getCustomer());
			} else {
				addMessage(model, "错误：登录超时，请退出后重新登录。");
				model.addAttribute("canAction", false);
				model.addAttribute("page", page);
				model.addAttribute("searchModel", searchModel);
				return viewForm;
			}
			//客户账号负责的店铺 2021/06/22
			List<String> shopIds = UserUtils.getShopIdsOfCustomerAccount(user);
			if(!CollectionUtils.isEmpty(shopIds)){
				searchModel.setShopIds(shopIds);
			}
		} else if (user.isSaleman()) {
			searchModel.setSales(user.getId());
			searchModel.setSubUserType(user.getSubFlag());//子账号类型
			List<Long> offlineCustomers = customerService.findIdListByOfflineOrderFlagFromCacheForSD();
			if(!org.springframework.util.CollectionUtils.isEmpty(offlineCustomers)){
				searchModel.setOfflineCustomerList(offlineCustomers);
			}
		}
		//date
		if (searchModel.getBeginDate() == null) {
			searchModel.setEndDate(DateUtils.getDateEnd(new Date()));
			searchModel.setBeginDate(DateUtils.getStartDayOfMonth(DateUtils.addMonth(new Date(), -3)));
		} else {
			searchModel.setEndDate(DateUtils.getDateEnd(searchModel.getEndDate()));
		}
		if (!request.getMethod().equalsIgnoreCase("post")) {
			searchModel.setMaterialType(1);//配件申请
		}
		Boolean isValide = checkOrderNoAndPhone(searchModel,model,page);
		if(!isValide){
			return viewForm;
		}
		try {
			//查询
			page = orderMaterialService.findCustomerMaterialList(new Page<OrderMaterialSearchModel>(request, response), searchModel);
			model.addAttribute("canAction", true);
		} catch (Exception e) {
			model.addAttribute("canAction", false);
			addMessage(model, "查询错误：" + e.getMessage());
		}
		model.addAttribute("page", page);
		model.addAttribute("searchModel", searchModel);
		return viewForm;
	}


	/**
	 * 导出
	 */
	@RequestMapping(value = "/customer/closelist/export",method = RequestMethod.POST)
	public String exportCustomerCloseList(OrderMaterialSearchModel searchModel, HttpServletRequest request, HttpServletResponse response, RedirectAttributes redirectAttributes, Model model)
	{
		//String viewForm = "modules/sd/material/customer/closeList";
		try
		{
			User user = UserUtils.getUser();
			if(user==null || user.getId()==null){
				addMessage(model, "登录超时，请重新登录。");
				return customerCloseList(searchModel,request,response,model);
			}
			//date
			if (searchModel.getBeginDate() != null) {
				Date sDate = searchModel.getBeginDate();
				Date eDate = searchModel.getEndDate();
				int monthes = DateUtils.getDateDiffMonth(sDate,eDate);
				if(monthes>12){
					addMessage(model, "导出数据跨度不能超过12个月，请分多次导出。");
					return customerCloseList(searchModel,request,response,model);
				}
			}
			//检查客户帐号信息
			boolean isCustomer = user.isCustomer();
			if (isCustomer) {
				if (user.getCustomerAccountProfile() != null && user.getCustomerAccountProfile().getCustomer() != null) {
					//登录用户的客户，防篡改
					searchModel.setCustomer(user.getCustomerAccountProfile().getCustomer());
				}
				//客户账号负责的店铺 2021/06/22
				List<String> shopIds = UserUtils.getShopIdsOfCustomerAccount(user);
				if(!CollectionUtils.isEmpty(shopIds)){
					searchModel.setShopIds(shopIds);
				}
			} else if (user.isSaleman()) {
				searchModel.setSales(user.getId());
				searchModel.setSubUserType(user.getSubFlag());//子账号类型
				List<Long> offlineCustomers = customerService.findIdListByOfflineOrderFlagFromCacheForSD();
				if(!org.springframework.util.CollectionUtils.isEmpty(offlineCustomers)){
					searchModel.setOfflineCustomerList(offlineCustomers);
				}
			}
			if (searchModel.getBeginDate() == null) {
				searchModel.setEndDate(DateUtils.getDateEnd(new Date()));
				searchModel.setBeginDate(DateUtils.getStartDayOfMonth(DateUtils.addMonth(new Date(), -3)));
			} else {
				searchModel.setEndDate(DateUtils.getDateEnd(searchModel.getEndDate()));
			}
			Page<MaterialMaster> page = new Page<>();
			//状态
			searchModel.setStatus(MaterialMaster.STATUS_CLOSED);
			Page<OrderMaterialSearchModel> searchModelPage = new Page<>(1,200000);
			try {
				//查询
				page = orderMaterialService.findCustomerMaterialList(searchModelPage, searchModel);
			} catch (Exception e) {
				addMessage(model, "查询错误：" + e.getMessage());
				return customerCloseList(searchModel,request,response,model);
			}
			List<String> tableTitle = Lists.newArrayList(
					"序号","订单号", "状态", "类型", "配件类型", "产品",
					"用户姓名", "电话", "区域", "详细地址", "申请人","申请时间",
					"完成人","完成时间","跟踪状态","跟踪时间","跟踪内容","是否返件"
			);
			if(!isCustomer){
				tableTitle.add(2,"厂商");
			}
			String xName = "完成配件列表";
			SXSSFWorkbook xBook;
			SXSSFSheet xSheet;
			Map<String, CellStyle> xStyle;
			ExportExcel exportExcel = new ExportExcel();
			xBook = new SXSSFWorkbook(500);
			xSheet = xBook.createSheet(xName);
			xSheet.setDefaultColumnWidth(15);
			xStyle = exportExcel.createStyles(xBook);

			//region style
			Font headerFont = xBook.createFont();
			headerFont.setFontName("微软雅黑");//Arial
			headerFont.setFontHeightInPoints((short) 12);
			//headerFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
			headerFont.setColor(IndexedColors.GREY_80_PERCENT.getIndex());

			CellStyle headStyle = xBook.createCellStyle();
			//XSSFColor xssfColor = new XSSFColor(new java.awt.Color(191,215,237));
			//headStyle.setFillForegroundColor(xssfColor.getIndex());
			headStyle.setFillForegroundColor(IndexedColors.TURQUOISE.getIndex());
			headStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
			headStyle.setAlignment(CellStyle.ALIGN_CENTER);
			headStyle.setFont(headerFont);

			CellStyle titleStyle = xBook.createCellStyle();
			titleStyle.cloneStyleFrom(xStyle.get("title"));
			Font titleFont = xBook.createFont();
			titleFont.setFontName("微软雅黑");//Arial
			titleFont.setFontHeightInPoints((short) 14);
			titleFont.setBold(true);
			titleStyle.setFont(titleFont);

			Font dataFont = xBook.createFont();
			dataFont.setFontName("微软雅黑");//Arial
			dataFont.setFontHeightInPoints((short) 10);

			CellStyle wrapCellStyle = xBook.createCellStyle();
			wrapCellStyle.cloneStyleFrom(xStyle.get("data"));
			wrapCellStyle.setFont(dataFont);
			wrapCellStyle.setWrapText(true);

			CellStyle dataCellStyle = xBook.createCellStyle();
			dataCellStyle.cloneStyleFrom(xStyle.get("data"));
			dataCellStyle.setFont(dataFont);

			// 加入标题
			int rowNum = 0;
			Row titleRow = xSheet.createRow(rowNum++); // 添加一行
			//titleRow.setHeightInPoints(30); // row高度
			Cell titleCell = titleRow.createCell(0); // 对cell(0)编辑, 对应A1
			titleCell.setCellStyle(titleStyle); // cell样式
			titleCell.setCellValue(xName); // 写入cell内容

			//endregion

			Row headRow;
			Row dataRow;
			Cell cell;
			// 加入表头
			headRow = xSheet.createRow(rowNum++);
			for (int i = 0,titleSize=tableTitle.size(); i < titleSize; i++)
			{
				cell = headRow.createCell(i);
				cell.setCellStyle(headStyle);
				cell.setCellValue(tableTitle.get(i));
			}

			xSheet.createFreezePane(0, rowNum); // 冻结单元格(x, y)
			List<MaterialMaster> list = page.getList();
			MaterialMaster item;
			int idx = 0;
			int col = 0;
			for (int j = 0,length=list.size(); j < length; j++) {
				item = list.get(j);
				if (item == null) {
					continue;
				}
				idx++;
				dataRow = xSheet.createRow(rowNum++);

				col = 0;
				cell = dataRow.createCell(col);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(idx);

				col++;
				cell = dataRow.createCell(col);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(StringUtils.toString(item.getOrderNo()));

				if(!isCustomer) {
					col++;
					cell = dataRow.createCell(col);
					cell.setCellStyle(dataCellStyle);
					cell.setCellValue(item.getCustomer() == null ? "" : StringUtils.toString(item.getCustomer().getName()));
				}

				col++;
				cell = dataRow.createCell(col);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getStatus().getLabel());

				col++;
				cell = dataRow.createCell(col);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getApplyType().getLabel());

				col++;
				cell = dataRow.createCell(col);
				cell.setCellStyle(dataCellStyle);
				//cell.setCellValue(item.getMaterialType().getLabel());
				cell.setCellValue("配件申请");

				col++;
				cell = dataRow.createCell(col);
				cell.setCellStyle(dataCellStyle);
				//cell.setCellValue(item.getProduct()==null?"":StringUtils.toString(item.getProduct().getName()));
				cell.setCellValue(item.getProductNames());

				col++;
				cell = dataRow.createCell(col);
				cell.setCellStyle(dataCellStyle);
				//cell.setCellType(Cell.CELL_TYPE_NUMERIC);
				cell.setCellValue(item.getUserName());

				col++;
				cell = dataRow.createCell(col);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getUserPhone());

				col++;
				cell = dataRow.createCell(col);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getArea()==null?"":item.getArea().getFullName());

				//快递
				col++;
				cell = dataRow.createCell(col);
				cell.setCellStyle(wrapCellStyle);
				cell.setCellValue(item.getUserAddress());

				col++;
				cell = dataRow.createCell(col);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getCreateBy().getName());

				col++;
				cell = dataRow.createCell(col);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(DateUtils.formatDate(item.getCreateDate(),"yyyy-MM-dd HH:mm"));

				col++;
				cell = dataRow.createCell(col);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getUpdateBy().getName());

				col++;
				cell = dataRow.createCell(col);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(DateUtils.formatDate(item.getUpdateDate(),"yyyy-MM-dd HH:mm"));

				col++;
				cell = dataRow.createCell(col);
				cell.setCellStyle(dataCellStyle);
				if(item.getPendingType() != null && item.getPendingType().getIntValue()>0) {
					cell.setCellValue(item.getPendingType().getLabel());
				}else{
					cell.setCellValue("");
				}

				col++;
				cell = dataRow.createCell(col);
				cell.setCellStyle(dataCellStyle);
				if(item.getPendingDate() != null) {
					cell.setCellValue(DateUtils.formatDate(item.getPendingDate(), "yyyy-MM-dd HH:mm"));
				}else{
					cell.setCellValue("");
				}

				col++;
				cell = dataRow.createCell(col);
				cell.setCellStyle(wrapCellStyle);
				cell.setCellValue(StringUtils.toString(item.getPendingContent()));

				col++;
				cell = dataRow.createCell(col);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getReturnFlag()==1?"返件":"否");
			}

			//设置自动列宽
			xSheet.trackAllColumnsForAutoSizing();
			for (int i = 0,size=tableTitle.size(); i < size; i++) {
				xSheet.autoSizeColumn(i);
				//sheet.setColumnWidth(i,sheet.getColumnWidth(i)*17/10);
			}
			xSheet.setColumnWidth(0,10*256);
			if(isCustomer){
				xSheet.setColumnWidth(8,30*256);
				xSheet.setColumnWidth(9,30*256);
				xSheet.setColumnWidth(16,50*256);
			}else {

				xSheet.setColumnWidth(9,30*256);
				xSheet.setColumnWidth(10,30*256);
				xSheet.setColumnWidth(17,50*256);
			}

			//标题合并单元格
			xSheet.addMergedRegion(new CellRangeAddress(0, 0, 0, tableTitle.size()-1));

			response.reset();
			response.setContentType("application/octet-stream; charset=utf-8");
			response.setHeader("Content-Disposition", "attachment; filename=" + Encodes.urlEncode(xName + ".xlsx"));
			xBook.write(response.getOutputStream());
			xBook.dispose();
			return null;//否则报错：getOutputStream() has already been called for this response
		} catch (Exception e)
		{
			addMessage(model, "导出失败：" + e.getMessage());
			return customerCloseList(searchModel,request,response,model);
		}
	}

	//endregion

	//region 所有

	/**
	 * 所有列表
	 */
	//@RequiresPermissions(value = { "sd:order:service" })
	@RequestMapping(value = "/customer/alllist")
	public String customerAllList(OrderMaterialSearchModel searchModel, HttpServletRequest request, HttpServletResponse response, Model model) {
		String viewForm = "modules/sd/material/customer/allList";
		Page<MaterialMaster> page = new Page<>();
		User user = UserUtils.getUser();
		//状态
		searchModel.setStatus(0);
		//检查客户帐号信息
		if (user.isCustomer()) {
			if (user.getCustomerAccountProfile() != null && user.getCustomerAccountProfile().getCustomer() != null) {
				//登录用户的客户，防篡改
				searchModel.setCustomer(user.getCustomerAccountProfile().getCustomer());
			} else {
				addMessage(model, "错误：登录超时，请退出后重新登录。");
				model.addAttribute("canAction", false);
				model.addAttribute("page", page);
				model.addAttribute("searchModel", searchModel);
				return viewForm;
			}
			//客户账号负责的店铺 2021/06/22
			List<String> shopIds = UserUtils.getShopIdsOfCustomerAccount(user);
			if(!CollectionUtils.isEmpty(shopIds)){
				searchModel.setShopIds(shopIds);
			}
		} else if (user.isSaleman()) {
			searchModel.setSales(user.getId());
			searchModel.setSubUserType(user.getSubFlag());//子账号类型
			List<Long> offlineCustomers = customerService.findIdListByOfflineOrderFlagFromCacheForSD();
			if(!org.springframework.util.CollectionUtils.isEmpty(offlineCustomers)){
				searchModel.setOfflineCustomerList(offlineCustomers);
			}
		}
		//date
		if (searchModel.getBeginDate() == null) {
			searchModel.setEndDate(DateUtils.getDateEnd(new Date()));
			searchModel.setBeginDate(DateUtils.getStartDayOfMonth(DateUtils.addMonth(new Date(), -3)));
		} else {
			searchModel.setEndDate(DateUtils.getDateEnd(searchModel.getEndDate()));
		}
		if (!request.getMethod().equalsIgnoreCase("post")) {
			searchModel.setMaterialType(1);//配件申请
		}
		Boolean isValide = checkOrderNoAndPhone(searchModel,model,page);
		if(!isValide){
			return viewForm;
		}
		try {
			//查询
			page = orderMaterialService.findCustomerMaterialList(new Page<OrderMaterialSearchModel>(request, response), searchModel,true);
			model.addAttribute("canAction", true);
		} catch (Exception e) {
			model.addAttribute("canAction", false);
			addMessage(model, "查询错误：" + e.getMessage());
		}
		model.addAttribute("page", page);
		model.addAttribute("searchModel", searchModel);
		return viewForm;
	}


	/**
	 * 导出
	 */
	@RequestMapping(value = "/customer/alllist/export",method = RequestMethod.POST)
	public String exportCustomerAllList(OrderMaterialSearchModel searchModel, HttpServletRequest request, HttpServletResponse response, RedirectAttributes redirectAttributes, Model model)
	{
		try
		{
			User user = UserUtils.getUser();
			if(user==null || user.getId()==null){
				addMessage(model, "登录超时，请重新登录。");
				return customerAllList(searchModel,request,response,model);
			}
			//date
			if (searchModel.getBeginDate() != null) {
				Date sDate = searchModel.getBeginDate();
				Date eDate = searchModel.getEndDate();
				int monthes = DateUtils.getDateDiffMonth(sDate,eDate);
				if(monthes>12){
					addMessage(model, "导出数据跨度不能超过12个月，请分多次导出。");
					return customerAllList(searchModel,request,response,model);
				}
			}
			//检查客户帐号信息
			boolean isCustomer = user.isCustomer();
			if (isCustomer) {
				if (user.getCustomerAccountProfile() != null && user.getCustomerAccountProfile().getCustomer() != null) {
					//登录用户的客户，防篡改
					searchModel.setCustomer(user.getCustomerAccountProfile().getCustomer());
				}
				//客户账号负责的店铺 2021/06/22
				List<String> shopIds = UserUtils.getShopIdsOfCustomerAccount(user);
				if(!CollectionUtils.isEmpty(shopIds)){
					searchModel.setShopIds(shopIds);
				}
			} else if (user.isSaleman()) {
				searchModel.setSales(user.getId());
				searchModel.setSubUserType(user.getSubFlag());//子账号类型
				List<Long> offlineCustomers = customerService.findIdListByOfflineOrderFlagFromCacheForSD();
				if(!org.springframework.util.CollectionUtils.isEmpty(offlineCustomers)){
					searchModel.setOfflineCustomerList(offlineCustomers);
				}
			}
			if (searchModel.getBeginDate() == null) {
				searchModel.setEndDate(DateUtils.getDateEnd(new Date()));
				searchModel.setBeginDate(DateUtils.getStartDayOfMonth(DateUtils.addMonth(new Date(), -3)));
			} else {
				searchModel.setEndDate(DateUtils.getDateEnd(searchModel.getEndDate()));
			}
			Page<MaterialMaster> page = new Page<>();
			//状态
			searchModel.setStatus(0);
			Page<OrderMaterialSearchModel> searchModelPage = new Page<>(1,200000);
			try {
				//查询
				page = orderMaterialService.findCustomerMaterialList(searchModelPage, searchModel,true);
			} catch (Exception e) {
				addMessage(model, "查询错误：" + e.getMessage());
				return customerAllList(searchModel,request,response,model);
			}
			List<String> tableTitle = Lists.newArrayList(
					"序号","订单号", "状态", "类型", "配件类型", "产品",
					"用户姓名", "电话", "区域", "详细地址", "申请人","申请时间",
					"完成人","完成时间","完成内容","跟踪状态","跟踪时间","跟踪内容","是否返件"
			);
			if(!isCustomer){
				tableTitle.add(2,"厂商");
			}
			String xName = "所有配件列表";
			SXSSFWorkbook xBook;
			SXSSFSheet xSheet;
			Map<String, CellStyle> xStyle;
			ExportExcel exportExcel = new ExportExcel();
			xBook = new SXSSFWorkbook(500);
			xSheet = xBook.createSheet(xName);
			xSheet.setDefaultColumnWidth(15);
			xStyle = exportExcel.createStyles(xBook);

			//region style
			Font headerFont = xBook.createFont();
			headerFont.setFontName("微软雅黑");//Arial
			headerFont.setFontHeightInPoints((short) 12);
			//headerFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
			headerFont.setColor(IndexedColors.GREY_80_PERCENT.getIndex());

			CellStyle headStyle = xBook.createCellStyle();
			//XSSFColor xssfColor = new XSSFColor(new java.awt.Color(191,215,237));
			//headStyle.setFillForegroundColor(xssfColor.getIndex());
			headStyle.setFillForegroundColor(IndexedColors.TURQUOISE.getIndex());
			headStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
			headStyle.setAlignment(CellStyle.ALIGN_CENTER);
			headStyle.setFont(headerFont);

			CellStyle titleStyle = xBook.createCellStyle();
			titleStyle.cloneStyleFrom(xStyle.get("title"));
			Font titleFont = xBook.createFont();
			titleFont.setFontName("微软雅黑");//Arial
			titleFont.setFontHeightInPoints((short) 14);
			titleFont.setBold(true);
			titleStyle.setFont(titleFont);

			Font dataFont = xBook.createFont();
			dataFont.setFontName("微软雅黑");//Arial
			dataFont.setFontHeightInPoints((short) 10);

			CellStyle wrapCellStyle = xBook.createCellStyle();
			wrapCellStyle.cloneStyleFrom(xStyle.get("data"));
			wrapCellStyle.setFont(dataFont);
			wrapCellStyle.setWrapText(true);

			CellStyle dataCellStyle = xBook.createCellStyle();
			dataCellStyle.cloneStyleFrom(xStyle.get("data"));
			dataCellStyle.setFont(dataFont);

			// 加入标题
			int rowNum = 0;
			Row titleRow = xSheet.createRow(rowNum++); // 添加一行
			//titleRow.setHeightInPoints(30); // row高度
			Cell titleCell = titleRow.createCell(0); // 对cell(0)编辑, 对应A1
			titleCell.setCellStyle(titleStyle); // cell样式
			titleCell.setCellValue(xName); // 写入cell内容

			//endregion

			Row headRow;
			Row dataRow;
			Cell cell;
			// 加入表头
			headRow = xSheet.createRow(rowNum++);
			for (int i = 0,titleSize=tableTitle.size(); i < titleSize; i++)
			{
				cell = headRow.createCell(i);
				cell.setCellStyle(headStyle);
				cell.setCellValue(tableTitle.get(i));
			}

			xSheet.createFreezePane(0, rowNum); // 冻结单元格(x, y)
			List<MaterialMaster> list = page.getList();
			MaterialMaster item;
			int idx = 0;
			int col = 0;
			for (int j = 0,length=list.size(); j < length; j++) {
				item = list.get(j);
				if (item == null) {
					continue;
				}
				idx++;
				dataRow = xSheet.createRow(rowNum++);

				col = 0;
				cell = dataRow.createCell(col);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(idx);

				col++;
				cell = dataRow.createCell(col);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(StringUtils.toString(item.getOrderNo()));

				if(!isCustomer) {
					col++;
					cell = dataRow.createCell(col);
					cell.setCellStyle(dataCellStyle);
					cell.setCellValue(item.getCustomer() == null ? "" : StringUtils.toString(item.getCustomer().getName()));
				}

				col++;
				cell = dataRow.createCell(col);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getStatus().getLabel());

				col++;
				cell = dataRow.createCell(col);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getApplyType().getLabel());

				col++;
				cell = dataRow.createCell(col);
				cell.setCellStyle(dataCellStyle);
				//cell.setCellValue(item.getMaterialType().getLabel());
				cell.setCellValue("配件申请");

				col++;
				cell = dataRow.createCell(col);
				cell.setCellStyle(dataCellStyle);
				//cell.setCellValue(item.getProduct()==null?"":StringUtils.toString(item.getProduct().getName()));
				cell.setCellValue(item.getProductNames());

				col++;
				cell = dataRow.createCell(col);
				cell.setCellStyle(dataCellStyle);
				//cell.setCellType(Cell.CELL_TYPE_NUMERIC);
				cell.setCellValue(item.getUserName());

				col++;
				cell = dataRow.createCell(col);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getUserPhone());

				col++;
				cell = dataRow.createCell(col);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getArea()==null?"":item.getArea().getFullName());

				//快递
				col++;
				cell = dataRow.createCell(col);
				cell.setCellStyle(wrapCellStyle);
				cell.setCellValue(item.getUserAddress());

				col++;
				cell = dataRow.createCell(col);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getCreateBy().getName());

				col++;
				cell = dataRow.createCell(col);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(DateUtils.formatDate(item.getCreateDate(),"yyyy-MM-dd HH:mm"));

				col++;
				cell = dataRow.createCell(col);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getCloseBy().getId()>0?item.getCloseBy().getName():"");

				col++;
				cell = dataRow.createCell(col);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getCloseBy().getId()>0?DateUtils.formatDate(item.getUpdateDate(),"yyyy-MM-dd HH:mm"):"");

				col++;
				cell = dataRow.createCell(col);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getCloseBy().getId()>0?item.getCloseRemark():"");

				col++;
				cell = dataRow.createCell(col);
				cell.setCellStyle(dataCellStyle);
				if(item.getPendingType() != null && item.getPendingType().getIntValue()>0) {
					cell.setCellValue(item.getPendingType().getLabel());
				}else{
					cell.setCellValue("");
				}

				col++;
				cell = dataRow.createCell(col);
				cell.setCellStyle(dataCellStyle);
				if(item.getPendingDate() != null) {
					cell.setCellValue(DateUtils.formatDate(item.getPendingDate(), "yyyy-MM-dd HH:mm"));
				}else{
					cell.setCellValue("");
				}

				col++;
				cell = dataRow.createCell(col);
				cell.setCellStyle(wrapCellStyle);
				cell.setCellValue(StringUtils.toString(item.getPendingContent()));

				col++;
				cell = dataRow.createCell(col);
				cell.setCellStyle(dataCellStyle);
				cell.setCellValue(item.getReturnFlag()==1?"返件":"否");
			}

			//设置自动列宽
			xSheet.trackAllColumnsForAutoSizing();
			for (int i = 0,size=tableTitle.size(); i < size; i++) {
				xSheet.autoSizeColumn(i);
				//sheet.setColumnWidth(i,sheet.getColumnWidth(i)*17/10);
			}
			xSheet.setColumnWidth(0,10*256);
			if(isCustomer){
				xSheet.setColumnWidth(8,30*256);
				xSheet.setColumnWidth(9,30*256);
				xSheet.setColumnWidth(17,50*256);
			}else {

				xSheet.setColumnWidth(9,30*256);
				xSheet.setColumnWidth(10,30*256);
				xSheet.setColumnWidth(15,50*256);//完成内容
				xSheet.setColumnWidth(18,50*256);//跟踪内容
			}

			//标题合并单元格
			xSheet.addMergedRegion(new CellRangeAddress(0, 0, 0, tableTitle.size()-1));

			response.reset();
			response.setContentType("application/octet-stream; charset=utf-8");
			response.setHeader("Content-Disposition", "attachment; filename=" + Encodes.urlEncode(xName + ".xlsx"));
			xBook.write(response.getOutputStream());
			xBook.dispose();
			return null;//否则报错：getOutputStream() has already been called for this response
		} catch (Exception e)
		{
			addMessage(model, "导出失败：" + e.getMessage());
			return customerAllList(searchModel,request,response,model);
		}
	}

	/**
	 * 返件代签收列表
	 */
	@RequestMapping(value = "/customer/findCustomerWaitMaterialReturnList")
	public String findCustomerWaitMaterialReturnList(OrderMaterialSearchModel searchModel, HttpServletRequest request, HttpServletResponse response, Model model) {
		String viewForm = "modules/sd/material/customer/waitSignMaterialReturnList";
		Page<MaterialReturn> page = new Page<>();
		User user = UserUtils.getUser();
		//检查客户帐号信息
		if (user.isCustomer()) {
			if (user.getCustomerAccountProfile() != null && user.getCustomerAccountProfile().getCustomer() != null) {
				//登录用户的客户，防篡改
				searchModel.setCustomer(user.getCustomerAccountProfile().getCustomer());
			} else {
				addMessage(model, "错误：登录超时，请退出后重新登录。");
				model.addAttribute("canAction", false);
				model.addAttribute("page", page);
				model.addAttribute("searchModel", searchModel);
				return viewForm;
			}
			//客户账号负责的店铺 2021/06/22
			List<String> shopIds = UserUtils.getShopIdsOfCustomerAccount(user);
			if(!CollectionUtils.isEmpty(shopIds)){
				searchModel.setShopIds(shopIds);
			}
		} else if (user.isSaleman()) {
			searchModel.setSales(user.getId());
			searchModel.setSubUserType(user.getSubFlag());//子账号类型
			List<Long> offlineCustomers = customerService.findIdListByOfflineOrderFlagFromCacheForSD();
			if(!org.springframework.util.CollectionUtils.isEmpty(offlineCustomers)){
				searchModel.setOfflineCustomerList(offlineCustomers);
			}
		}
		//date
		if (searchModel.getBeginDate() == null) {
			searchModel.setEndDate(DateUtils.getDateEnd(new Date()));
			searchModel.setBeginDate(DateUtils.getStartDayOfMonth(DateUtils.addMonth(new Date(), -3)));
		} else {
			searchModel.setEndDate(DateUtils.getDateEnd(searchModel.getEndDate()));
		}
		Boolean isValide = checkOrderNoAndPhone(searchModel,model,page);
		if(!isValide){
			return viewForm;
		}
		try {
			//查询
			page = orderMaterialReturnService.findCustomerWaitMaterialReturnList(new Page<OrderMaterialSearchModel>(request, response), searchModel);
			model.addAttribute("canAction", true);
		} catch (Exception e) {
			model.addAttribute("canAction", false);
			addMessage(model, "查询错误：" + e.getMessage());
		}
		model.addAttribute("page", page);
		model.addAttribute("searchModel", searchModel);
		return viewForm;
	}

	//endregion

	//endregion

	//region 跟踪

	//region 配件单

	/**
	 * 设定跟踪进度
	 *
	 * @param id   配件申请单id
	 * @param quarter  	分片
	 */
	@RequiresPermissions(value = "sd:material:pending")
	@RequestMapping(value = "/pending", method = RequestMethod.GET)
	public String pending(String id,String quarter,HttpServletRequest rermquest, Model model)
	{
		String viewForm = "modules/sd/material/pendingForm";
		MaterialMaster materialMaster = new MaterialMaster();
		Long lId = Long.valueOf(id);
		if (lId == null || lId<=0) {
			addMessage(model, "错误：配件单参数无效");
			model.addAttribute("canSave", false);
			model.addAttribute("materialMaster", materialMaster);
			return viewForm;
		}

		materialMaster = orderMaterialService.getMaterialMasterHeadById(lId,quarter);
		if(materialMaster == null ){
			addMessage(model, "错误：配件单不存在或查询数据错误。");
			model.addAttribute("canSave", false);
			model.addAttribute("materialMaster", new MaterialMaster());
			return viewForm;
		}
		List<MaterialLog> logs = orderMaterialService.getLogs(lId,quarter,MaterialLog.SORT_DESC);
		materialMaster.setPendingContent("");
		model.addAttribute("materialMaster", materialMaster);
		model.addAttribute("logs", logs);
		return viewForm;
	}

	/**
	 * 保存跟踪进度(ajax)
	 */
	@RequiresPermissions(value = "sd:material:pending")
	@ResponseBody
	@RequestMapping(value = "pending", method = RequestMethod.POST)
	public AjaxJsonEntity pending(MaterialMaster pending, HttpServletResponse response)
	{
		AjaxJsonEntity result = new AjaxJsonEntity(true);
		try
		{
			User user = UserUtils.getUser();
			pending.setCreateBy(user);
			if(pending.getPendingContent() == null){
				pending.setPendingContent("");
			}
			orderMaterialService.updateMaterialPendingInfo(pending.getId(),pending.getQuarter(),pending.getPendingType(),new Date(),pending.getPendingContent(),user.getName());
		} catch (OrderException oe){
			result.setSuccess(false);
			result.setMessage(oe.getMessage());
		} catch (Exception e){
			result.setSuccess(false);
			result.setMessage(e.getMessage());
			if(pending != null && pending.getId() != null) {
				log.error("[OrderMaterialController.pending] id:{}",pending.getId(), e);
			}else{
				log.error("[OrderMaterialController.pending]", e);
			}
		}
		return result;
	}
	//endregion

	//region 返件单
	/**
	 * 设定跟踪进度
	 *
	 * @param id   配件申请单id
	 * @param quarter  	分片
	 */
	@RequiresPermissions(value = {"sd:material:pending","sd:material:return:pending"},logical = Logical.OR)
	@RequestMapping(value = "/return/pending", method = RequestMethod.GET)
	public String returnPending(String id,String quarter,HttpServletRequest request, Model model)
	{
		String viewForm = "modules/sd/material/return/pendingForm";
		MaterialReturn materialReturn = new MaterialReturn();
		Long lId = Long.valueOf(id);
		if (lId == null || lId<=0) {
			addMessage(model, "错误：返件单参数无效");
			model.addAttribute("canSave", false);
			model.addAttribute("materialReturn", materialReturn);
			return viewForm;
		}

		materialReturn = orderMaterialReturnService.getMaterialReturnHeadById(lId,null,quarter,0,0,0,0);
		if(materialReturn == null ){
			addMessage(model, "错误：返件单不存在或查询数据错误。");
			model.addAttribute("canSave", false);
			model.addAttribute("materialReturn", new MaterialReturn());
			return viewForm;
		}

		materialReturn.setPendingContent("");
		model.addAttribute("materialReturn", materialReturn);
		return viewForm;
	}

	/**
	 * 保存跟踪进度(ajax)
	 */
	@RequiresPermissions(value = {"sd:material:pending","sd:material:return:pending"},logical = Logical.OR)
	@ResponseBody
	@RequestMapping(value = "/return/pending", method = RequestMethod.POST)
	public AjaxJsonEntity returnPending(MaterialReturn pending, HttpServletResponse response)
	{
		AjaxJsonEntity result = new AjaxJsonEntity(true);
		try
		{
			User user = UserUtils.getUser();
			pending.setCreateBy(user);
			if(pending.getPendingContent() == null){
				pending.setPendingContent("");
			}
			orderMaterialReturnService.updateMaterialPendingInfo(pending.getId(),pending.getQuarter(),pending.getPendingType(),new Date(),pending.getPendingContent(),user.getName());
		} catch (OrderException oe){
			result.setSuccess(false);
			result.setMessage(oe.getMessage());
		} catch (Exception e){
			result.setSuccess(false);
			result.setMessage(e.getMessage());
			if(pending != null && pending.getId() != null) {
				log.error("[OrderMaterialController.returnPending] id:{}",pending.getId(), e);
			}else{
				log.error("[OrderMaterialController.returnPending]", e);
			}
		}
		return result;
	}
	//endregion

	/**
	 * 设定跟踪进度
	 *
	 * @param id   配件申请单id
	 * @param quarter  	分片
	 */
	@RequiresPermissions(value = {"sd:material:pending","sd:material:return:pending"},logical = Logical.OR)
	@RequestMapping(value = "pendingLog", method = RequestMethod.GET)
	public String pendingLog(String id,String orderNo,String quarter,HttpServletRequest request, Model model)
	{
		String viewForm = "modules/sd/material/pendingLog";
		model.addAttribute("orderNo",orderNo);
		MaterialMaster materialMaster = new MaterialMaster();
		Long lId = Long.valueOf(id);
		if (lId == null || lId<=0) {
			addMessage(model, "错误：配件单参数无效");
			model.addAttribute("logs", Lists.newArrayList());
			return viewForm;
		}
		List<MaterialLog> logs = orderMaterialService.getLogs(lId,quarter,MaterialLog.SORT_DESC);
		model.addAttribute("logs", logs);
		return viewForm;
	}

	//endregion

	//region 关闭

	/**
	 * 关闭配件单窗口
	 *
	 * @param id   配件申请单id
	 * @param quarter  	分片
	 */
	@RequiresPermissions(value = "sd:material:close")
	@RequestMapping(value = "/close", method = RequestMethod.GET)
	public String closeForm(String id,String quarter,HttpServletRequest request, Model model)
	{
		String viewForm = "modules/sd/material/closeForm";
		MaterialMaster materialMaster = new MaterialMaster();
		Long lId = Long.valueOf(id);
		if (lId == null || lId<=0) {
			addMessage(model, "错误：配件单参数无效");
			model.addAttribute("canSave", false);
			model.addAttribute("materialMaster", materialMaster);
			return viewForm;
		}

		materialMaster = orderMaterialService.getMaterialMasterHeadById(lId,quarter);
		if(materialMaster == null ){
			addMessage(model, "错误：配件单不存在或查询数据错误。");
			model.addAttribute("canSave", false);
			model.addAttribute("materialMaster", new MaterialMaster());
			return viewForm;
		}
		if(materialMaster.getStatus().getIntValue().intValue() != MaterialMaster.STATUS_APPROVED && materialMaster.getStatus().getIntValue().intValue() != MaterialMaster.STATUS_SENDED && materialMaster.getReturnFlag() == 0){
			addMessage(model, "错误：该配件单：" + materialMaster.getStatus().getLabel() + ",不能关闭！");
			model.addAttribute("canSave", false);
			model.addAttribute("materialMaster", new MaterialMaster());
			return viewForm;
		}
		materialMaster.setCloseRemark("");
		model.addAttribute("materialMaster", materialMaster);
		return viewForm;
	}

	/**
	 * 关闭配件单(ajax)
	 */
	@RequiresPermissions(value = "sd:material:close")
	@ResponseBody
	@RequestMapping(value = "close", method = RequestMethod.POST)
	public AjaxJsonEntity close(MaterialMaster closeForm, HttpServletResponse response)
	{
		AjaxJsonEntity result = new AjaxJsonEntity(true);
		try
		{
			MaterialMaster form = orderMaterialService.getMaterialMasterHeadById(closeForm.getId(),closeForm.getQuarter());
			if(form == null){
				return AjaxJsonEntity.fail("读取配件申请单错误，请检查传入参数",null);
			}
			if(form.getStatus().getIntValue() == 3 && closeForm.getStatus().getIntValue() == 5){
                return AjaxJsonEntity.fail("该配件申请单已发货，不能取消。",null);
            }
			User user = UserUtils.getUser();
			closeForm.setCloseBy(user);
			closeForm.setCloseDate(new Date());
			if(closeForm.getCloseRemark() == null){
				closeForm.setCloseRemark("");
			}
			String closeType = closeForm.getStatus().getValue().equalsIgnoreCase("6")?StringUtils.left(closeForm.getCloseType(),30):StringUtils.EMPTY;
			orderMaterialService.manuClose(form.getDataSource(),form.getId(),form.getMasterNo(),form.getQuarter(),user,new Date(),closeForm.getCloseRemark(),closeType,closeForm.getReturnFlag(),closeForm.getStatus().getIntValue());
		} catch (OrderException oe){
			result.setSuccess(false);
			result.setMessage(oe.getMessage());
		} catch (Exception e){
			result.setSuccess(false);
			result.setMessage(e.getMessage());
			if(closeForm != null && closeForm.getId() != null) {
				log.error("[OrderMaterialController.close] id:{}",closeForm.getId(), e);
			}else{
				log.error("[OrderMaterialController.close]", e);
			}
		}
		return result;
	}

	/**
	 * 关闭返件单窗口
	 *
	 * @param id   返件申请单id
	 * @param quarter  	分片
	 */
	@RequiresPermissions(value = "sd:material:close")
	@RequestMapping(value = "/return/close", method = RequestMethod.GET)
	public String closeReturnForm(String id,String quarter,HttpServletRequest request, Model model)
	{
		String viewForm = "modules/sd/material/return/closeForm";
		MaterialReturn materialReturn = null;
		Long lId = Long.valueOf(id);
		if (lId == null || lId<=0) {
			addMessage(model, "错误：返件单参数无效");
			model.addAttribute("canSave", false);
			return viewForm;
		}

		materialReturn = orderMaterialReturnService.getMaterialReturnHeadById(lId,null,quarter,0,0,0,0);
		if(materialReturn == null ){
			addMessage(model, "错误：返件单不存在或查询数据错误。");
			model.addAttribute("canSave", false);
			return viewForm;
		}
		if(materialReturn.getStatus().getIntValue().intValue() != MaterialMaster.STATUS_APPROVED){
			addMessage(model, "错误：该返件单：" + materialReturn.getStatus().getLabel() + ",不能关闭！");
			model.addAttribute("canSave", false);
			return viewForm;
		}
		materialReturn.setCloseRemark("");
		model.addAttribute("materialReturn", materialReturn);
		return viewForm;
	}

	/**
	 * 关闭返件单(ajax)
	 */
	@RequiresPermissions(value = "sd:material:close")
	@ResponseBody
	@RequestMapping(value = "/return/close", method = RequestMethod.POST)
	public AjaxJsonEntity closeReturn(MaterialReturn closeForm, HttpServletResponse response)
	{
		AjaxJsonEntity result = new AjaxJsonEntity(true);
		try
		{
			MaterialReturn form = orderMaterialReturnService.getMaterialReturnHeadById(closeForm.getId(),null,closeForm.getQuarter(),0,0,0,0);
			if(form == null){
				return AjaxJsonEntity.fail("读取返件申请单错误，请检查传入参数",null);
			}
			User user = UserUtils.getUser();
			closeForm.setCloseBy(user);
			closeForm.setCloseDate(new Date());
			if(closeForm.getCloseRemark() == null){
				closeForm.setCloseRemark("");
			}
			orderMaterialReturnService.manuRejectAndCloseReturnForm(form.getId(),null,form.getQuarter(),user,new Date(),closeForm.getCloseRemark(),MaterialMaster.STATUS_REJECT);
		} catch (OrderException oe){
			result.setSuccess(false);
			result.setMessage(oe.getMessage());
		} catch (Exception e){
			result.setSuccess(false);
			result.setMessage(e.getMessage());
			if(closeForm != null && closeForm.getId() != null) {
				log.error("[OrderMaterialController.closeReturn] id:{}",closeForm.getId(), e);
			}else{
				log.error("[OrderMaterialController.closeReturn]", e);
			}
		}
		return result;
	}

	/**
	 * 跳转返件待签收页面
	 */
	@RequestMapping("return/waitSign")
	public String waitSign(String materialReturnId,String quarter,HttpServletRequest request, Model model){
		String viewForm = "modules/sd/material/return/signForm";
		Long lMaterialReturn = null;
		try {
            lMaterialReturn = Long.valueOf(materialReturnId);
		}catch (Exception e){
			addMessage(model, "旧件待签收失败，返件单ID不合法.");
			model.addAttribute("canAction", false);
			return viewForm;
		}
		if (lMaterialReturn <= 0) {
			addMessage(model, "旧件待签收失败，返件单ID不合法.");
			model.addAttribute("canAction", false);
			return viewForm;
		}
		MaterialReturn materialReturn = orderMaterialReturnService.getMaterialReturnById(lMaterialReturn, null, quarter, 1, 0, 1, 0);
		if(materialReturn == null){
			addMessage(model,"读取返件单失败，请重试!");
			model.addAttribute("canAction", false);
			return viewForm;
		}
		if(materialReturn.getStatus()==null || !materialReturn.getStatus().getIntValue().equals(MaterialMaster.STATUS_SENDED)){
            addMessage(model,"返件单非已发货，请重试!");
            model.addAttribute("canAction", false);
            return viewForm;
        }
		List<MaterialAttachment> attachments = attachments = materialReturn.getAttachments();
		String key = String.format(RedisConstant.SD_RETURN_MATERIAL_ATTACHE, materialReturn.getId());
		if (attachments == null || attachments.size() > 0) {
			if (!redisUtils.exists(RedisConstant.RedisDBType.REDIS_TEMP_DB, key)) {
				redisUtils.setEX(RedisConstant.RedisDBType.REDIS_TEMP_DB, key, attachments, 10l * 60);//10分钟
			}
		}
		model.addAttribute("materialReturn", materialReturn);
		model.addAttribute("canAction", true);
		return viewForm;
	}

    /**
     * 保存旧件签收
     */
    @RequestMapping("return/saveSign")
    @ResponseBody
    public AjaxJsonEntity saveSign(MaterialReturn materialReturn){
         AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
         User user = UserUtils.getUser();
         if(user == null || user.getId()==null || user.getId()<=0){
             ajaxJsonEntity.setSuccess(false);
             ajaxJsonEntity.setMessage("当前用户不存在,请重新登录");
         }
		Integer returnStatus = orderMaterialReturnService.getMaterialReturnStatus(materialReturn.getId(),materialReturn.getQuarter());
         if(returnStatus==null || !returnStatus.equals(MaterialMaster.STATUS_SENDED)){
			 ajaxJsonEntity.setSuccess(false);
			 ajaxJsonEntity.setMessage("返件单非发货状态,请重新刷新页面");
		 }
        materialReturn.setSignBy(user);
        materialReturn.setStatus(new Dict(MaterialMaster.STATUS_SIGN.toString(),"签收"));
        materialReturn.setSignAt(new Date());
        try {
            orderMaterialReturnService.saveSign(materialReturn);
        }catch (Exception e){
            ajaxJsonEntity.setSuccess(false);
            ajaxJsonEntity.setMessage("保存旧件签收失败:"+e.getMessage());
        }
         return ajaxJsonEntity;
    }


	/**
	 * 跳转到修改收件地址页面
	 * */
	@RequestMapping("editReceiveAddress")
	public String editReceiveAddress(Long servicePointId,Long engineerId,Model model){
	   model.addAttribute("canSave",true);
       if(engineerId==null || engineerId<=0){
       	addMessage(model,"错误:师傅不存在");
		   model.addAttribute("canSave",false);
		   return "modules/sd/material/receiveAddressForm";
	   }
		if(servicePointId==null || servicePointId<=0){
			addMessage(model,"错误:网点不存在");
			model.addAttribute("canSave",false);
			return "modules/sd/material/receiveAddressForm";
		}
	   List<MaterialReceive> materialReceives = Lists.newArrayList();
       //师傅收货地址标识
		Integer addressFlag = 3; //自定义地址
	   //获取网点地址
		MaterialReceive servicePointAddressReceive = new MaterialReceive();
		//MDServicePointAddress servicePointAddress = msServicePointService.getAddressByServicePointIdFromCache(servicePointId);
		ServicePoint servicePoint = servicePointService.getFromCache(servicePointId);
		List<Engineer> engineers =msEngineerService.findEngineerByServicePointIdFromCache(servicePointId);
		//获得安维主账号
		Engineer masterEngineer = engineers.stream().filter(t->t.getMasterFlag()==1).findFirst().orElse(null);
		Area area=null;
		if(servicePoint!=null && masterEngineer!=null){
			servicePointAddressReceive.setReceiveName(masterEngineer.getName());
			servicePointAddressReceive.setReceivePhone(masterEngineer.getContactInfo());
			servicePointAddressReceive.setAreaId(servicePoint.getArea().getId());
			area= areaService.getFromCache(servicePoint.getArea().getId());
			if(area!=null){
				servicePointAddressReceive.setDetailAddress(area.getFullName());
			}
			servicePointAddressReceive.setAddress(servicePoint.getSubAddress());
		}
		materialReceives.add(servicePointAddressReceive);
		//师傅个人地址
		MaterialReceive engineerReceive = new MaterialReceive();
		Engineer engineer = engineerService.getEngineerFromCache(engineerId);
		String[] strAddress = engineer.getAddress().split(" ");
		engineerReceive.setReceiveName(engineer.getName());
		engineerReceive.setReceivePhone(engineer.getContactInfo());
		area = areaService.getFromCache(engineer.getArea().getId());
		if(area!=null){
			engineerReceive.setDetailAddress(area.getFullName());
		}
		if(strAddress.length>=4){
			engineerReceive.setAddress(strAddress[3]);
		}else{
			engineerReceive.setAddress(strAddress[strAddress.length-1]);
		}
		engineerReceive.setAreaId(engineer.getArea().getId());
		materialReceives.add(engineerReceive);
        //师傅收件地址
		MaterialReceive engineerAddressReceive = new MaterialReceive();
		MDEngineerAddress engineerAddress = engineerAddressService.getByEngineerId(engineerId);
		if(engineerAddress!=null && engineerAddress.getId()!=null && engineerAddress.getId()>0){
			engineerAddressReceive.setReceiveName(engineerAddress.getUserName());
			engineerAddressReceive.setReceivePhone(engineerAddress.getContactInfo());
			engineerAddressReceive.setAreaId(engineerAddress.getAreaId());
			area = areaService.getFromCache(engineerAddress.getAreaId());
			if(area!=null){
				engineerAddressReceive.setDetailAddress(area.getFullName());
			}
			int pos = engineerAddress.getAddress().indexOf(area.getFullName());
			String address = pos==0?engineerAddress.getAddress().substring(area.getFullName().length()).trim():engineerAddress.getAddress();
			engineerAddressReceive.setAddress(address);
			engineerAddressReceive.setId(engineerAddress.getId());
			engineerAddressReceive.setServicePointId(engineerAddress.getServicePointId());
			engineerAddressReceive.setEngineerId(engineerAddress.getEngineerId());
			addressFlag = engineerAddress.getAddressFlag();
		}else {
			engineerAddressReceive.setServicePointId(servicePointId);
			engineerAddressReceive.setEngineerId(engineerId);
			engineerAddressReceive.setReceiveName(engineer.getName());
			engineerAddressReceive.setReceivePhone(engineer.getContactInfo());
		}
        materialReceives.add(engineerAddressReceive);
		model.addAttribute("addressFlag",addressFlag);
		model.addAttribute("materialReceives",materialReceives);
		return "modules/sd/material/receiveAddressForm";
	}


	/**
	 *  申请配件保存师傅收件地址
	 */
	@RequestMapping("saveEngineerAddress")
	@ResponseBody
	public AjaxJsonEntity saveEngineerAddress(MDEngineerAddress engineerAddress){
           AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
           User user = UserUtils.getUser();
           if(user==null || user.getId()==null || user.getId()<=0){
			   ajaxJsonEntity.setSuccess(false);
			   ajaxJsonEntity.setMessage("当前用户不存在,请重新登录");
			   return ajaxJsonEntity;
		   }
		   if(engineerAddress.getAreaId() ==null || engineerAddress.getAreaId()<=0){
			   ajaxJsonEntity.setSuccess(false);
			   ajaxJsonEntity.setMessage("请选择区域!");
			   return ajaxJsonEntity;
		   }
		   try {
			   engineerAddress.setCreateById(user.getId());
			   engineerAddress.setUpdateById(user.getId());
			   Map<Integer,Area> areas = areaService.getAllParentsWithDistrict(engineerAddress.getAreaId());
			   Area province = areas.getOrDefault(Area.TYPE_VALUE_PROVINCE,new Area(0L));
			   Area city = areas.getOrDefault(Area.TYPE_VALUE_CITY,new Area(0L));
			   engineerAddress.setCityId(city.getId());
			   engineerAddress.setProvinceId(province.getId());
			   engineerAddressService.save(engineerAddress);
			   ajaxJsonEntity.setData(engineerAddress);
		   }catch (Exception e){
			   ajaxJsonEntity.setSuccess(false);
			   ajaxJsonEntity.setMessage(e.getMessage());
		   }
		   return ajaxJsonEntity;
	}

	/**
     * 获取客户地址,发货地址,返件地址
     * */
	@RequestMapping("returnReceiveAddressForm")
    public String returnReceiveAddressForm(Long customerId, Long materialId,Model model){
        model.addAttribute("canSave",true);
        if(customerId==null || customerId<=0){
            addMessage(model,"缺少厂商Id");
            model.addAttribute("canSave",false);
            return "modules/sd/material/returnReceiveAddressForm";
        }
        List<MDCustomerAddress> customerAddresses = customerNewService.getCustomerAllAddress(customerId);
        if(customerAddresses==null){
			customerAddresses = Lists.newArrayList();
		}
        List<MaterialReceive> materialReceives = Lists.newArrayList();
        //公司地址
        MaterialReceive customerReceive =  new MaterialReceive();
        MDCustomerAddress customerAddress = customerAddresses.stream().filter(t->t.getAddressType()==1).findFirst().orElse(null);
        Area area;
        if(customerAddress!=null){
            customerReceive.setReceiveName(customerAddress.getUserName());
            customerReceive.setReceivePhone(customerAddress.getContactInfo());
            customerReceive.setAreaId(customerAddress.getAreaId());
            area = areaService.getFromCache(customerAddress.getAreaId());
            if(area!=null){
                customerReceive.setDetailAddress(area.getFullName());
            }
            customerReceive.setAddress(customerAddress.getAddress());
        }
		materialReceives.add(customerReceive);
        //客户发货地址
        MaterialReceive consignmentAddress = new MaterialReceive();
        customerAddress = customerAddresses.stream().filter(t->t.getAddressType()==2).findFirst().orElse(null);
        if(customerAddress!=null){
            consignmentAddress.setReceiveName(customerAddress.getUserName());
            consignmentAddress.setReceivePhone(customerAddress.getContactInfo());
            consignmentAddress.setAreaId(customerAddress.getAreaId());
            area = areaService.getFromCache(customerAddress.getAreaId());
            if(area!=null){
                consignmentAddress.setDetailAddress(area.getFullName());
            }
            consignmentAddress.setAddress(customerAddress.getAddress());
        }
		materialReceives.add(consignmentAddress);
        //客户收货地址
        MaterialReceive receiveAddress = new MaterialReceive();
        String returnAddress="";
        customerAddress = customerAddresses.stream().filter(t->t.getAddressType()==3).findFirst().orElse(null);
        if(customerAddress!=null){
            receiveAddress.setReceiveName(customerAddress.getUserName());
            receiveAddress.setReceivePhone(customerAddress.getContactInfo());
            receiveAddress.setAreaId(customerAddress.getAreaId());
            area = areaService.getFromCache(customerAddress.getAreaId());
            if(area!=null){
                receiveAddress.setDetailAddress(area.getFullName());
            }
            receiveAddress.setAddress(customerAddress.getAddress());
            receiveAddress.setId(customerAddress.getId());
        }else{
        	Customer customer = customerService.getFromCache(customerId);
        	if(customer!=null){
				returnAddress = customer.getMaster()+" " +customer.getPhone()+" "+ customer.getReturnAddress();
			}
		}
		materialReceives.add(receiveAddress);
        model.addAttribute("materialReceives",materialReceives);
        model.addAttribute("customerId",customerId);
        model.addAttribute("materialId",materialId);
		model.addAttribute("returnAddress",returnAddress);
        return "modules/sd/material/returnReceiveAddressForm";
  }


     /**
      * 配件单审核页面保存客户收货地址
      * */
    @RequestMapping("saveCustomerAddress")
    @ResponseBody
	public AjaxJsonEntity saveCustomerAddress(MDCustomerAddress customerAddress){
    	AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity(true);
		User user = UserUtils.getUser();
		if(user==null || user.getId()==null || user.getId()<=0){
			ajaxJsonEntity.setSuccess(false);
			ajaxJsonEntity.setMessage("当前用户不存在,请重新登录");
			return ajaxJsonEntity;
		}
		if(customerAddress.getAreaId() ==null || customerAddress.getAreaId()<=0){
			ajaxJsonEntity.setSuccess(false);
			ajaxJsonEntity.setMessage("请选择区域!");
			return ajaxJsonEntity;
		}

		try {
			customerAddress.setCreateById(user.getId());
			customerAddress.setUpdateById(user.getId());
			Map<Integer,Area> areas = areaService.getAllParentsWithDistrict(customerAddress.getAreaId());
			Area province = areas.getOrDefault(Area.TYPE_VALUE_PROVINCE,new Area(0L));
			Area city = areas.getOrDefault(Area.TYPE_VALUE_CITY,new Area(0L));
			customerAddress.setCityId(city.getId());
			customerAddress.setProvinceId(province.getId());
			ajaxJsonEntity.setData(customerAddress);
		}catch (Exception e){
			ajaxJsonEntity.setSuccess(false);
			ajaxJsonEntity.setMessage(e.getMessage());
		}
		return ajaxJsonEntity;
  }
//endregion
	/**
	 * 返回修改返件地址页面
	 * */
	@RequestMapping("return/returnAddressForm")
	public String returnAddressForm(Long customerId,Long returnMaterialId,MaterialReturn materialReturn,String quarter,Model model){
		model.addAttribute("canSave",true);
		if(customerId==null || customerId<=0){
			addMessage(model,"获取厂商信息失败");
			model.addAttribute("canSave",false);
			return "modules/sd/material/return/returnAddressForm";
		}
		/*MaterialReturn materialReturn = orderMaterialReturnService.getMaterialReturnAddress(returnMaterialId,quarter);
		if(materialReturn==null){
			addMessage(model,"读取返件信息失败");
			model.addAttribute("canSave",false);
			return "modules/sd/material/return/returnAddressForm";
		}*/
		Area area =null;
		String materialAreaName = "";
		area = areaService.getFromCache(materialReturn.getReceiverAreaId());
		if(area!=null){
			materialAreaName = area.getFullName();
		}
		List<MDCustomerAddress> customerAddressList = customerNewService.getCustomerAllAddress(customerId);
		if(customerAddressList!=null){
			for(MDCustomerAddress entity:customerAddressList){
				 area = areaService.getFromCache(entity.getAreaId());
				if(area!=null){
					entity.setAreaName(area.getFullName());
				}
			}
		}else{
			customerAddressList = Lists.newArrayList();
		}
		model.addAttribute("materialAreaName",materialAreaName);
		model.addAttribute("materialReturn",materialReturn);
		model.addAttribute("customerAddressList",customerAddressList);
		return "modules/sd/material/return/returnAddressForm";
	}

	/**
	 * 更新返件收货地址
	 * */
	@RequestMapping("return/saveReturnAddress")
	@ResponseBody
	public AjaxJsonEntity saveReturnAddress(MaterialReturn materialReturn){
        AjaxJsonEntity jsonEntity = new AjaxJsonEntity(true);
        User user = UserUtils.getUser();
        if(user==null || user.getId()==null || user.getId()<=0){
			jsonEntity.setSuccess(false);
			jsonEntity.setMessage("当前用户不存在,请重新登录！");
		}
		if(materialReturn.getReceiverAreaId()==null || materialReturn.getReceiverAreaId()<=0){
			jsonEntity.setSuccess(false);
			jsonEntity.setMessage("请选择区域！");
		}
		Area area = areaService.getFromCache(materialReturn.getReceiverAreaId());
		if (area != null) {
			List<String> ids = Splitter.onPattern(",")
					.omitEmptyStrings()
					.trimResults()
					.splitToList(area.getParentIds());
			if (ids.size() >= 2) {
				materialReturn.setReceiverCityId(Long.valueOf(ids.get(ids.size() - 1)));
				materialReturn.setReceiverProvinceId(Long.valueOf(ids.get(ids.size() - 2)));
			}
		}
		materialReturn.preInsert();
		try {
			orderMaterialReturnService.saveReturnAddress(materialReturn);
		}catch (Exception e){
			jsonEntity.setSuccess(false);
			jsonEntity.setMessage(e.getMessage());
		}
         return jsonEntity;
	}

}
