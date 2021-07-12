package com.wolfking.jeesite.modules.customer.md.web;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.entity.md.CustomerBrand;
import com.kkl.kklplus.entity.md.CustomerProductModel;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.config.Global;
import com.wolfking.jeesite.common.config.redis.GsonRedisSerializer;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.config.redis.RedisTuple;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.service.SequenceIdService;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.Encodes;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.wolfking.jeesite.common.utils.excel.ExportExcel;
import com.wolfking.jeesite.common.utils.excel.ImportExcel;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.customer.md.service.CtCustomerBrandService;
import com.wolfking.jeesite.modules.customer.md.service.CtCustomerProductModelService;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.entity.Product;
import com.wolfking.jeesite.modules.md.utils.CustomerUtils;
import com.wolfking.jeesite.modules.mq.dto.MQOrderImportMessage;
import com.wolfking.jeesite.modules.sd.entity.TempOrder;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providermd.entity.ProductModelTemp;
import com.wolfking.jeesite.ms.providermd.service.MSCustomerProductService;
import com.wolfking.jeesite.ms.providermd.service.MSCustomerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisZSetCommands;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

import static com.wolfking.jeesite.modules.sd.web.OrderNewImportController.IMP_ORDER_CACHE_TTL;

/**
 * 客户产品型号服务
 */
@Controller
@RequestMapping(value = "${adminPath}/customer/md/customerProductModel")
@Slf4j
public class CtCustomerProductModelImportController extends BaseController {
    @Autowired
    private CtCustomerBrandService ctCustomerBrandService;

    @Autowired
    private RedisUtils redisUtils;

    @Resource(name = "gsonRedisSerializer")
    public GsonRedisSerializer gsonRedisSerializer;

    @Autowired
    private SequenceIdService sequenceIdService;

    @Autowired
    private MSCustomerService msCustomerService;

    @Autowired
    private MSCustomerProductService msCustomerProductService;

    @Autowired
    private CtCustomerProductModelService ctCustomerProductModelService;

    /**
     * 分页查询
     *
     * @param customerProductModel
     * @return
     */
    @RequiresPermissions("customer:md:customerproductmodel:view")
    @RequestMapping(value = {"importForm"})
    public String importForm(CustomerProductModel customerProductModel, HttpServletRequest request, HttpServletResponse response, Model model) {
        Page<ProductModelTemp> page = new Page<>(request, response);
        User user = UserUtils.getUser();
        Boolean erroFlag = false;
        CustomerBrand searchCustomerBrand = new CustomerBrand();
        if(user.isCustomer()){
            if (user.getCustomerAccountProfile() != null && user.getCustomerAccountProfile().getCustomer() != null) {
                //登录用户的客户，防篡改
                customerProductModel.setCustomerId(user.getCustomerAccountProfile().getCustomer().getId());
                searchCustomerBrand.setCustomerId(user.getCustomerAccountProfile().getCustomer().getId());
            } else {
                addMessage(model, "错误：登录超时，请退出后重新登录。");
                erroFlag = true;
            }
        }else if(user.isSaleman() && (customerProductModel.getCustomerId()==null || customerProductModel.getCustomerId()<=0)){
            List<Customer> customers = CustomerUtils.getMyCustomerList();
            if(customers !=null && customers.size()>0){
                customerProductModel.setCustomerId(customers.get(0).getId());
            }else{
                customerProductModel.setCustomerId(0L);
            }
        }
        List<CustomerBrand> customerBrandList = ctCustomerBrandService.findAllList(searchCustomerBrand);
        if (erroFlag){
            model.addAttribute("customerProductModel", customerProductModel);
            model.addAttribute("customerBrandList",customerBrandList);
            return "modules/customer/md/ctCustomerProductModelImportForm";
        }

        String key = String.format(RedisConstant.MD_TMP_PRODUCT_MODEL,user.getId());
        List<ProductModelTemp> productModelTemps = redisUtils.zRange(RedisConstant.RedisDBType.REDIS_MD_DB,key,0,-1,ProductModelTemp.class);
        if (productModelTemps == null)
        {
            productModelTemps = Lists.newArrayList();
        }
        page.setList(productModelTemps);
        model.addAttribute("customerBrandList",customerBrandList);
        model.addAttribute("page",page);
        model.addAttribute("customerProductModel", customerProductModel);
        return "modules/customer/md/ctCustomerProductModelImportForm";
    }

    /**
     * 从excel中读取订单数据
     * @param file			excel附件
     * @param customerId	客户id
     */
    @RequiresPermissions("customer:md:customerproductmodel:edit")
    @RequestMapping(value = "read", method = RequestMethod.POST)
    public String readExcel(MultipartFile file, @RequestParam(required = false) String customerId, @RequestParam(required = false) String brandId, RedirectAttributes redirectAttributes, HttpServletRequest request)
    {
        User user = UserUtils.getUser();

        if(file == null){
            redirectAttributes.addAttribute("customerId",customerId);
            redirectAttributes.addAttribute("brandId",brandId);
            addMessage(redirectAttributes,"请选择文件．");
            return "redirect:" + Global.getAdminPath() + "/customer/md/customerProductModel/importForm?repage";
        }

        Customer customer = null;
        Long cid = 0l;
        if(!user.isCustomer())
        {
            if(StringUtils.isBlank(customerId))
            {
                addMessage(redirectAttributes,"请先选择客户．");
                return "redirect:" + Global.getAdminPath() + "/customer/md/customerProductModel/importForm?repage";
            }
            cid = Long.valueOf(customerId);
        }
        else{
            cid = user.getCustomerAccountProfile().getCustomer().getId();
        }
        customer = msCustomerService.getFromCache(cid);
        CustomerBrand customerBrand = null;
        if (StringUtils.isBlank(brandId)){
            addMessage(redirectAttributes,"请先选择品牌．");
            return "redirect:" + Global.getAdminPath() + "/customer/md/customerProductModel/importForm?repage";
        }else {
            customerBrand = ctCustomerBrandService.getById(Long.valueOf(brandId));
        }

        String key =String.format(RedisConstant.MD_TMP_PRODUCT_MODEL,user.getId());
        //移除上次读取数据
        redisUtils.remove(RedisConstant.RedisDBType.REDIS_MD_DB,key);
        List<Product> customerProductList = msCustomerProductService.findProductByCustomerIdFromCache(Long.valueOf(customerId));
        Map<String, Product> stringProductMap = customerProductList.stream().collect(Collectors.toMap(Product::getName, item -> item));
        try {
            //product and services of customer

            //Expresses


            //read excel
            ImportExcel ei = new ImportExcel(file, 1, 0);

            List<CustomerProductModel> models = Lists.newArrayList();
            ProductModelTemp productModel = null;
            StringBuilder stringBuilder = new StringBuilder(300);

            StringBuffer desc;

            HashSet<String> set = new HashSet<String>();
            int maxTimes = ei.getLastDataRowNum()+2000;
            //随机，防止同用户产生重复id
            //int workerId = ThreadLocalRandom.current().nextInt(32);
            //int datacenterId = ThreadLocalRandom.current().nextInt(32);
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
                //read row
                productModel = new ProductModelTemp();
                Long id = Long.valueOf(idList.removeFirst());
                //行号
                productModel.setLineNumber(i);
                productModel.setId(id);
                rowCheckMsg.setLength(0);
                //产品名称
                checkResultStr = StringUtils.strip(ei.getCellValue(row, 0).toString());
                //用户名为空的订单，忽略
//                if(StringUtils.isBlank(checkResultStr)){
//                    continue;
//                }
                productModel.setCustomerId(customer.getId());
                productModel.setCustomerName(customer.getName());
                productModel.setBrandId(customerBrand.getId());
                productModel.setBrandName(customerBrand.getBrandName());
                productModel.setProductName(checkResultStr);
                Product product = stringProductMap.get(checkResultStr);
                if (product==null){
                    rowCheckMsg.append("产品名称不匹配");
                    rowCheckSucess = false;
                }else {
                    productModel.setProductId(product.getId());
                }

                //型号
                String strip = StringUtils.strip(StringUtils.cleanHtmlTagAndSpecChars(ei.getCellValue(row, 1).toString()));
                productModel.setCustomerModel(strip);
                productModel.setCustomerProductName(StringUtils.strip(StringUtils.cleanHtmlTagAndSpecChars(ei.getCellValue(row,2).toString())));
                productModel.setRemarks(StringUtils.strip(StringUtils.cleanHtmlTagAndSpecChars(ei.getCellValue(row,3).toString())));
                if (StringUtils.isBlank(productModel.getCustomerModel())){
                    rowCheckMsg.append(rowCheckSucess ? "" : "<br>").append("型号不能为空");
                    rowCheckSucess = false;
                }
                if(productModel.getCustomerModel().length()>100) {
                    rowCheckMsg.append(rowCheckSucess ? "" : "<br>").append("型号/规格长度过长，超过100个汉字");
                    rowCheckSucess = false;
                }
                if (!rowCheckSucess){
                    productModel.setCanSave(0);
                    productModel.setErrorMsg(rowCheckMsg.toString());
                }else {
                    productModel.setCanSave(1);
                }

                //add to list
                models.add(productModel);//*

                //check order
                //rowCheckMsg.setLength(0);

            }

            if (models == null || models.isEmpty()) {
                addMessage(redirectAttributes, "读取数据完成，但无符合的产品，请确认！");
                return "redirect:" + Global.getAdminPath() + "/customer/md/customerProductModel/importForm?repage";
            }


            //cache
            try {
                Set<RedisZSetCommands.Tuple> sets = Sets.newHashSet();
                models.forEach(t->{
                    sets.add(new RedisTuple(gsonRedisSerializer.serialize(t),Double.valueOf(StringUtils.right(t.getId().toString(),14))));
                });
                redisUtils.zAdd(RedisConstant.RedisDBType.REDIS_MD_DB, key, sets,IMP_ORDER_CACHE_TTL);
            }catch (Exception e){
                addMessage(redirectAttributes,"存储导入订单列表失败");
                return "redirect:" + Global.getAdminPath() + "/customer/md/customerProductModel/importForm?repage";
            }

        } catch (Exception e)
        {
            addMessage(redirectAttributes, "读取产品型号失败！失败信息："+ e.getMessage());
        }
        return "redirect:" + Global.getAdminPath() + "/customer/md/customerProductModel/importForm?repage";
    }

    /**
     * 导出检查有问题的产品型号
     *
     * @param paramMap
     * @param request
     * @param response
     * @param redirectAttributes
     * @return
     */
    @RequiresPermissions("customer:md:customerproductmodel:edit")
    @RequestMapping(value = "errorlist")
    public String exportErrorList(@RequestParam Map<String, Object> paramMap, HttpServletRequest request, HttpServletResponse response, RedirectAttributes redirectAttributes, Model model)
    {
        try
        {
            User user = UserUtils.getUser();
            if(user==null || user.getId()==null){
                addMessage(model, "登录超时，请重新登录。");
            }

            String xName = "异常导入明细";
            SXSSFWorkbook xBook;
            Sheet xSheet;
            Map<String, CellStyle> xStyle;

            String key =String.format(RedisConstant.MD_TMP_PRODUCT_MODEL,user.getId());
            List<ProductModelTemp> orders =redisUtils.zRange(RedisConstant.RedisDBType.REDIS_MD_DB,key,0,-1,ProductModelTemp.class);
            if(orders==null || orders.size()==0){
                addMessage(model, "导出Excel失败！失败信息：订单列表为空。");
                return "modules/customer/md/productModelImportForm";
            }
            //延时过期时间
            try {
                redisUtils.expire(RedisConstant.RedisDBType.REDIS_MD_DB,key,IMP_ORDER_CACHE_TTL);
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
            xSheet.setColumnWidth(20,50*256);
            CellStyle wrapCellStyle = xBook.createCellStyle();
            wrapCellStyle.setWrapText(true);
//			xSheet.setDefaultColumnStyle(12,wrapCellStyle);
            // 加入表头
            headRow = xSheet.createRow(rowNum++);
            headRow.setHeightInPoints(16);

            String[] TableTitle = new String[]
                    { "产品","型号", "客户产品名称", "备注","异常信息"
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
            TempOrder tempOrder;
            ProductModelTemp order;
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
                        cell.setCellValue(order.getProductName());

                        cell = dataRow.createCell(1);
                        cell.setCellStyle(xStyle.get("data"));
                        cell.setCellValue(order.getCustomerModel());

                        cell = dataRow.createCell(2);
                        cell.setCellStyle(xStyle.get("data"));
                        cell.setCellValue(order.getCustomerProductName() );

                        cell = dataRow.createCell(3);
                        cell.setCellStyle(xStyle.get("data"));
                        cell.setCellValue(order.getRemarks());

                        //error
                        cell = dataRow.createCell(4);
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
            return "modules/customer/md/productModelImportForm";
        }
    }

    //endregion

    /**
     * 保存数据
     *
     */
    @RequiresPermissions("customer:md:customerproductmodel:edit")
    @ResponseBody
    @RequestMapping(value = "importSave",method = RequestMethod.POST)
    public AjaxJsonEntity improtSave(@RequestParam(value = "ids[]") Long[] ids, HttpServletResponse response) {
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
                result.setMessage("请选择要保存的产品型号");
                return result;
            }
            String key =String.format(RedisConstant.MD_TMP_PRODUCT_MODEL,user.getId());
            List<ProductModelTemp> models = redisUtils.zRange(RedisConstant.RedisDBType.REDIS_MD_DB,key,0,-1,ProductModelTemp.class);
            if(models == null || models.size()==0){
                result.setSuccess(false);
                result.setMessage("导入的数据不见了");
                return result;
            }
            //延时过期时间
            try {
                redisUtils.expire(RedisConstant.RedisDBType.REDIS_MD_DB,key,IMP_ORDER_CACHE_TTL);
            }catch (Exception e){}

            StringBuffer msg = new StringBuffer();
            //list -> Map<id,TempOrder>
            Map<Long,ProductModelTemp> maps = models.stream().collect(Collectors.toMap(ProductModelTemp::getId, item -> item));
            Long id;
            ProductModelTemp temp;
            MQOrderImportMessage.OrderImportMessage message;
            Date date =new Date();
            List<Double> rmIds = Lists.newArrayList();
            for (int i = 0,length = ids.length; i < length; i++)
            {
                id = ids[i];
                temp = maps.get(id);
                if(temp.getCanSave() == 0){
                    //不符合要求订单，不能保存
                    msg.append(String.format("<div class='alert alert-error'>产品:%s %s</div>",temp.getProductName(),"不符合要求，不能保存"));
                    continue;
                }

                //save
                CustomerProductModel customerProductModel = new CustomerProductModel();
                customerProductModel.setCreateById(user.getId());
                customerProductModel.setUpdateById(user.getId());
                customerProductModel.setRemarks(temp.getRemarks());
                customerProductModel.setCustomerModel(temp.getCustomerModel());
                customerProductModel.setCustomerProductName(temp.getCustomerProductName());
                customerProductModel.setProductId(temp.getProductId());
                customerProductModel.setProductName(temp.getProductName());
                customerProductModel.setCustomerId(temp.getCustomerId());
                customerProductModel.setCustomerName(temp.getCustomerName());
                customerProductModel.setBrandName(temp.getBrandName());
                customerProductModel.setBrandId(temp.getBrandId());
                customerProductModel.setCreateBy(user);
                customerProductModel.setCreateDate(date);
                MSErrorCode mSResponse = ctCustomerProductModelService.save(customerProductModel);
                if (mSResponse.getCode() == 0) {
                    maps.remove(id);
                    rmIds.add(Double.valueOf(StringUtils.right(id.toString(), 14)));

                } else {
                    msg.append(String.format("<div class='alert alert-error'>产品:%s %s</div>",customerProductModel.getProductName(),mSResponse.getMsg()));
                    temp.setCanSave(0);
                    temp.setErrorMsg(customerProductModel.getProductName()+mSResponse.getMsg());
                    maps.put(id,temp);
                }
            }
            //cache update
            if(maps.size()==0){
                try {
                    redisUtils.remove(RedisConstant.RedisDBType.REDIS_MD_DB, key);
                }catch (Exception e){
                    log.error("remove key:"+key,e);
                    try{
                        redisUtils.remove(RedisConstant.RedisDBType.REDIS_MD_DB, key);
                    }catch (Exception e1){
                        log.error("remove key:"+key,e1);
                    }
                }
            }else{
//            try {
//                Double score;
//                for(int i=0,size=rmIds.size();i<size;i++){
//                    score = rmIds.get(i);
//                    redisUtils.zRemRangeByScore(RedisConstant.RedisDBType.REDIS_MD_DB, key,score,score);
//                }
//                //过期时间
//                redisUtils.expire(RedisConstant.RedisDBType.REDIS_MD_DB,key,IMP_ORDER_CACHE_TTL);
//            }catch (Exception e){
//                log.error("remove key:"+key,e);
//            }
                try {
                    redisUtils.remove(RedisConstant.RedisDBType.REDIS_MD_DB,key);
                    Set<RedisZSetCommands.Tuple> sets = Sets.newHashSet();
                    maps.forEach((k, v) -> {
                        sets.add(new RedisTuple(gsonRedisSerializer.serialize(v), Double.valueOf(StringUtils.right(k.toString(), 14))));
                    });

                    redisUtils.zAdd(RedisConstant.RedisDBType.REDIS_MD_DB, key, sets, IMP_ORDER_CACHE_TTL);
                } catch (Exception e1) {
                    log.error("update key:" + key);
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
    @RequiresPermissions("customer:md:customerproductmodel:edit")
    @ResponseBody
    @RequestMapping(value = "importClear",method = RequestMethod.POST)
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
            String key = String.format(RedisConstant.MD_TMP_PRODUCT_MODEL, user.getId());
            if (ids == null || ids.length == 0) {
                try {
                    redisUtils.remove(RedisConstant.RedisDBType.REDIS_MD_DB, key);
                    jsonEntity.setSuccess(true);
                } catch (Exception e) {
                    jsonEntity.setSuccess(false);
                    jsonEntity.setMessage("清除数据失败:" + e.getMessage());
                }
                return jsonEntity;
            }

            List<ProductModelTemp> modelTemps = redisUtils.zRange(RedisConstant.RedisDBType.REDIS_MD_DB, key, 0, -1, ProductModelTemp.class);
            if (modelTemps == null || modelTemps.size() == 0) {
                jsonEntity.setSuccess(false);
                jsonEntity.setMessage("数据不见了,请刷新");
                return jsonEntity;
            }
            //clear all
            if (ids.length == modelTemps.size()) {
                try {
                    redisUtils.remove(RedisConstant.RedisDBType.REDIS_MD_DB, key);
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
            Map<Long, ProductModelTemp> maps = modelTemps.stream().collect(Collectors.toMap(ProductModelTemp::getId, item -> item));
            modelTemps.clear();
            modelTemps = null;
            Long id;
            ProductModelTemp model;
            List<Double> rmIds = Lists.newArrayList();
            for (int i = 0, length = ids.length; i < length; i++) {
                id = ids[i];
                model = maps.get(id);
                try {
                    maps.remove(id);
                    rmIds.add(Double.valueOf(StringUtils.right(id.toString(), 14)));
                } catch (Exception e) {
                    msg.append(String.format("型号:%s 的产品移除失败：%s", model.getCustomerModel(), e.getMessage()));
                }
            }
            //cache update
            try {
                Double score;
                for(int i=0,size=rmIds.size();i<size;i++){
                    score = rmIds.get(i);
                    redisUtils.zRemRangeByScore(RedisConstant.RedisDBType.REDIS_MD_DB, key,score,score);
                }
                //过期时间
                redisUtils.expire(RedisConstant.RedisDBType.REDIS_MD_DB,key,IMP_ORDER_CACHE_TTL);
            } catch (Exception e) {
                try {
                    Set<RedisZSetCommands.Tuple> sets = Sets.newHashSet();
                    maps.forEach((k, v) -> {
                        sets.add(new RedisTuple(gsonRedisSerializer.serialize(v), Double.valueOf(StringUtils.right(k.toString(), 14))));
                    });
                    //先重命名
                    if(redisUtils.renameNX(RedisConstant.RedisDBType.REDIS_MD_DB, key,key+":DEL")) {
                        //再过期
                        redisUtils.expire(RedisConstant.RedisDBType.REDIS_MD_DB,key+":DEL",2);
                    }else {
                        //重命名失败，直接删除
                        redisUtils.remove(RedisConstant.RedisDBType.REDIS_MD_DB, key);
                    }
                    redisUtils.zAdd(RedisConstant.RedisDBType.REDIS_MD_DB, key, sets, IMP_ORDER_CACHE_TTL);
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
}
