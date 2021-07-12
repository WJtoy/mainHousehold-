package com.wolfking.jeesite.modules.sd.web;


import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.googlecode.protobuf.format.JsonFormat;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.md.CustomerProductModel;
import com.kkl.kklplus.entity.md.MDCustomerGallery;
import com.kkl.kklplus.entity.md.dto.CreateOrderDto;
import com.kkl.kklplus.entity.md.dto.MDCustomerGalleryStreamLineDto;
import com.kkl.kklplus.entity.md.dto.TreeDTO;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.service.UploadFileService;
import com.wolfking.jeesite.common.servlet.UploadFileModel;
import com.wolfking.jeesite.common.utils.GsonUtils;
import com.wolfking.jeesite.common.utils.ProtobufUtils;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.entity.Brand;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.entity.CustomerFinance;
import com.wolfking.jeesite.modules.md.service.CustomerService;
import com.wolfking.jeesite.modules.sd.config.CreateOrderConfig;
import com.wolfking.jeesite.modules.sd.entity.viewModel.CreateOrderModel;
import com.wolfking.jeesite.modules.sd.entity.viewModel.CustomerProductVM;
import com.wolfking.jeesite.modules.sd.service.OrderEditFormService;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.b2bcenter.md.utils.B2BMDUtils;
import com.wolfking.jeesite.ms.providermd.feign.MSCustomerGalleryFeign;
import com.wolfking.jeesite.ms.providermd.service.MSCustomerProductService;
import com.wolfking.jeesite.ms.providermd.service.MSProductTypeService;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


/**
 * 订单编辑Controller
 */
@Controller
@RequestMapping(value = "${adminPath}/sd/order/createOrEdit")
@Slf4j
public class OrderEditFormController extends BaseController {

    @Autowired
    private CreateOrderConfig createOrderConfig;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private OrderEditFormService orderEditFormService;

    @Autowired
    private MSCustomerProductService msCustomerProductService;

    @Autowired
    private MSProductTypeService msProductTypeService;

    @Autowired
    private UploadFileService uploadFileService;

    @Autowired
    private MSCustomerGalleryFeign msCustomerGalleryFeign;

    private static final String KEY_NAME_SUCCESS = "success";
    private static final String KEY_NAME_MESSAGE = "message";
    private static final String KEY_NAME_ITEMS = "items";
    private static final String KEY_NAME_PRODUCTS = "products";
    private static final String KEY_NAME_PRODUCT_TYPES = "productTypes";
    private static final String KEY_NAME_CUSTOMER_CATEGORIES = "customerCategories";
    private static final String KEY_NAME_EXPRESSES = "expresses";
    private static final String KEY_NAME_BALANCE = "balance";
    private static final String KEY_NAME_CREDIT = "credit";
    private static final String KEY_NAME_BLOCK_AMOUNT = "blockAmount";
    private static final String KEY_NAME_SHOPS = "shops";
    private static final String KEY_NAME_URGENT_FLAG = "urgentFlag";
    private static final String KEY_NAME_CUSTOMER = "customer";
    private static final String KEY_NAME_LOGINUSER = "currentUser";

    private static final String KEY_NAME_MODELS = "models";
    private static final String KEY_NAME_BRANDS = "brands";
    private static final String KEY_NAME_B2BPRODUCTCODES = "b2bProductCodes";

    private static final String ACTION_VALUE_NEW = "new";
    private static final String ACTION_VALUE_NEW_V2 = "newv2";
    private static final String ACTION_VALUE_EDIT = "edit";

    //region [客户功能]


    /**
     * 返回客户产品-服务信息(ajax)
     * @date 2019-09-24
     * @author Ryan Lu
     * 产品增加：categoryId,categoryName输出，用于前端做类目控制
     *
     * @param customerId 客户id
     * @return {
     * // 产品列表，包含各产品可服务的项目
     * products:
     * [
     * {
     * id:id,name:name,model:model,brand:"brandName",categoryId:id,categoryName:"",
     * services:[
     * {id:id,code:code,name:name},...
     * ]
     * },
     * models:["model1","model2",...],
     * brands:[{id:id1,name:"name1"},{id:id2,name:"name2"},...],
     * b2bProductCodes:["b2bProductCode1","b2bProductCode2",...],
     * ...
     * ],
     * //快递信息
     * expresses:[
     * {label:label,value:value},...
     * ],
     * //已维护的订单项目
     * items: [
     * ],
     * //客户信息
     * customer:{
     * balance:1000.00,credit:200.00,blockAmount:500.00,
     * //加急标志
     * urgentFlag : 0,
     * //店铺列表
     * shops: []
     * }
     * }
     * @version 0.2
     */
    @RequiresUser
    @ResponseBody
    @RequestMapping(value = "form_products", method = RequestMethod.GET)
    public Map<String, Object> formProducts(@RequestParam(required = false, defaultValue = ACTION_VALUE_NEW) String action,
                                            @RequestParam Long customerId,
                                            @RequestParam(required = false, defaultValue = "1") Integer dataSourceId,
                                            HttpServletRequest request, HttpServletResponse response) {

        Map<String, Object> result = Maps.newHashMap();
        result.put(KEY_NAME_SUCCESS, false);
        result.put(KEY_NAME_PRODUCTS, Lists.newArrayList());
        result.put(KEY_NAME_EXPRESSES, Lists.newArrayList());

        User user = UserUtils.getUser();
        if (user == null || user.getId() == null) {
            result.put(KEY_NAME_MESSAGE, "登录超时，请重新登录。");
            return result;
        }
        if (customerId == null || customerId <= 0L) {
            result.put(KEY_NAME_MESSAGE, "无效的厂商ID。");
            return result;
        }
        //清除临时订单缓存
        try {
            String cachekey = OrderUtils.getUserTmpOrderCacheKey(request, response,action);
            if (StringUtils.isNotBlank(cachekey)) {
                CreateOrderModel order = (CreateOrderModel) redisUtils.get(RedisConstant.RedisDBType.REDIS_TEMP_DB, cachekey, CreateOrderModel.class);
                if (order.getCustomer() != null && order.getCustomer().getId() != null && !order.getCustomer().getId().equals(customerId)) {
                    redisUtils.remove(RedisConstant.RedisDBType.REDIS_TEMP_DB, cachekey);
                } else {
                    if (order.getItems() != null && order.getItems().size() > 0) {
                        result.put(KEY_NAME_ITEMS, order.getItems());
                    }
                }
            }
        } catch (Exception e) {
        }
        Customer customer = customerService.getFromCache(customerId);
        if (customer == null) {
            result.put(KEY_NAME_MESSAGE, "读取厂商信息失败。");
            return result;
        }
        List<CustomerProductVM> productVMList = orderEditFormService.getCustomerProducts(customerId);
        if (productVMList == null || productVMList.size() == 0) {
            productVMList = null;
            result.put(KEY_NAME_MESSAGE, "读取厂商产品服务价格信息失败。");
            return result;
        }
        // 根据下单配置(applicaiton.yml)排除指定的品类
        if(!CollectionUtils.isEmpty(createOrderConfig.getFirstType().getExceptCategories())){
            Set<Long> productCategories = createOrderConfig.getFirstType().getExceptCategories();
            productVMList = productVMList.stream()
                    .filter(
                            t-> !productCategories.contains(t.getCategoryId())
                    )
                    .collect(Collectors.toList());
        }
        result.put(KEY_NAME_PRODUCTS, productVMList);

        List<Dict> expresses = MSDictUtils.getDictList(Dict.DICT_TYPE_EXPRESS_TYPE);
        result.put(KEY_NAME_EXPRESSES, expresses);

        Map<String, Object> customerMap = Maps.newHashMap();
        if (action == null || !action.equalsIgnoreCase(ACTION_VALUE_EDIT)) {
            CustomerFinance finance = customerService.getFinance(customerId);
            double balance = 0.0;
            double credit = 0.0;
            double blockAmount = 0.0;
            if (finance != null && finance.getBalance() != null) {
                balance = finance.getBalance();
            }
            if (finance != null && finance.getCreditFlag() == 1 && finance.getCredit() != null) {
                credit = finance.getCredit();
            }
            if (finance != null && finance.getBlockAmount() != null) {
                blockAmount = finance.getBlockAmount();
            }
            customerMap.put(KEY_NAME_BALANCE, balance);
            customerMap.put(KEY_NAME_CREDIT, credit);
            customerMap.put(KEY_NAME_BLOCK_AMOUNT, blockAmount);
        }
        //List<Dict> shops = B2BMDUtils.getCustomerKKLShopListNew(customerId);
        List<Dict> shops = UserUtils.getShops(customerId,user);// 按客户+账号读取店铺清单 2021/06/22
        customerMap.put(KEY_NAME_SHOPS, shops);
        customerMap.put(KEY_NAME_URGENT_FLAG, customer.getUrgentFlag());
        result.put(KEY_NAME_CUSTOMER, customerMap);
        result.put(KEY_NAME_SUCCESS, true);
        return result;
    }

    /**
     * 获取客户产品的型号、品牌、b2b料号
     * 修改订单时调用
     * @param customerId
     * @param productId
     * @param dataSourceId
     * @return
     */
    @RequiresUser
    @ResponseBody
    @RequestMapping(value = "form_product_properties", method = RequestMethod.GET)
    public AjaxJsonEntity formProductModels(@RequestParam Long customerId,
                                            @RequestParam Long productId,
                                            @RequestParam(required = false, defaultValue = "1") Integer dataSourceId) {
        AjaxJsonEntity result = new AjaxJsonEntity(false);
        User user = UserUtils.getUser();
        if (user == null || user.getId() == null) {
            result.setMessage("登录超时，请重新登录。");
            return result;
        }
        if (customerId == null || customerId <= 0L) {
            result.setMessage("客户编号未传入。");
            return result;
        }
        if (productId == null || productId == 0) {
            result.setMessage("产品编号未传入。");
            return result;
        }
        Customer customer = new Customer(customerId);
        List<CustomerProductModel> models = orderEditFormService.getProductSpecModels(customerId, productId);
        List<Brand> brands = orderEditFormService.getBrands(customer, productId);
        List<String> b2bProductCodes = orderEditFormService.getB2BProductCodes(dataSourceId, productId);

        Map<String, List> map = Maps.newHashMap();
        map.put(KEY_NAME_MODELS, models);
        map.put(KEY_NAME_BRANDS, brands);
        map.put(KEY_NAME_B2BPRODUCTCODES, b2bProductCodes);
        result.setData(map);
        result.setSuccess(true);
        return result;
    }

    //region 新下单

    //region 图库

    @RequestMapping(value = "/v2/getProductGallery", method = RequestMethod.GET)
    public String getProductGallery(@RequestParam Long customerId ,@RequestParam Long productCategoryId,@RequestParam Long productTypeId,
                                    @RequestParam Long productTypeItemId,@RequestParam(defaultValue = "0") int limitQty,
                                    @RequestParam String selIds,HttpServletRequest request, Model model) {
        String view = "modules/sd/secondOrder/productGallerySelectForm";
        Map<String, Object> map = Maps.newHashMapWithExpectedSize(10);
        map.put("customerId", customerId);
        map.put("productCategoryId", productCategoryId);
        map.put("productTypeId", productTypeId);
        map.put("productTypeItemId", productTypeItemId);
        map.put("limitQty", limitQty);//可选择图片数量
        map.put("maxPicQty", createOrderConfig.getSecondType().getMaxUploadQty());//产品分类下最多上传图片数量
        if(StringUtils.isNotEmpty(selIds)){
            List<String> idList = Splitter.onPattern(",") //[~|-]
                    .omitEmptyStrings()
                    .trimResults()
                    .splitToList(selIds);
            map.put("idList", idList);//已选择图片id
        }else{
            map.put("idList",Lists.newArrayList());
        }
        model.addAllAttributes(map);
        MSResponse<List<MDCustomerGalleryStreamLineDto>> msResponse = msCustomerGalleryFeign.findByProductTypeItem(customerId, productTypeItemId);
        if (!MSResponse.isSuccessCode(msResponse)) {
            addMessage(model, String.format("错误：%s", msResponse.getMsg()));
            model.addAttribute("canSave", false);
            return view;
        }
        model.addAttribute("gallery", msResponse.getData().stream().sorted(Comparator.comparingLong(MDCustomerGalleryStreamLineDto::getId)).collect(Collectors.toList()));
        model.addAttribute("canSave", true);
        return view;

    }


    /**
     * 上传图库
     */
    @PostMapping("/v2/uploadGallery")
    public void uploadGallery(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        Long customerId = StringUtils.toLong(request.getParameter("customerId"));
        Long productCategoryId = StringUtils.toLong(request.getParameter("productCategoryId"));
        Long productTypeId = StringUtils.toLong(request.getParameter("productTypeId"));
        Long productTypeItemId = StringUtils.toLong(request.getParameter("productTypeItemId"));
        if(customerId <= 0 || productCategoryId <= 0 || productTypeId<=0 || productTypeItemId <=0){
            log.error("参数错误 - customerId:{},productCategoryId:{},productTypeId:{},productTypeItemId:{}",customerId,productCategoryId,productTypeId,productTypeItemId);
            response.getWriter().print("{id:'',fileName:'参数错误',status:'false',origalName:'','isImage':false}");
            return;
        }
        Map<Long,String> map = uploadFileService.getRootFolder("");
        String json;
        try {
            json = uploadFileService.uploadSingle(0L, map.get(0L), map.get(1L), request, response);
        }catch (Exception e){
            log.error("上传图片失败",e);
            response.getWriter().print("{id:'',fileName:'读取上传图片失败',status:'false',origalName:'','isImage':false}");
            return;
        }
        UploadFileModel entity = null;
        entity = GsonUtils.getInstance().fromJson(json, UploadFileModel.class);
        if(entity == null){
            log.error("解析json失败:{}",json);
            response.getWriter().print("{id:'',fileName:'解析图片上传结果失败',status:'false',origalName:'','isImage':false}");
            return;
        }

        if(entity.getStatus().equalsIgnoreCase("success")){
            User user = UserUtils.getUser();
            MDCustomerGallery gallery = new MDCustomerGallery();
            gallery.setCustomerId(customerId);
            gallery.setProductCategoryId(productCategoryId);
            gallery.setProductTypeId(productTypeId);
            gallery.setProductTypeItemId(productTypeItemId);
            gallery.setCreateBy(user.getId());
            gallery.setCreateAt(System.currentTimeMillis());
            gallery.setPicUrl(entity.getFileName());
            try {
                MSResponse<Long> msResponse = msCustomerGalleryFeign.add(gallery);
                if(MSResponse.isSuccessCode(msResponse)){
                    entity.setId(String.valueOf(msResponse.getData()));
                    response.getWriter().print(GsonUtils.getInstance().toGson(entity));
                }else{
                    response.getWriter().print("{id:'',fileName:'图片上传成功，保存数据库失败',status:'false',origalName:'','isImage':false}");
                }
            }catch (Exception e){
                log.error("图片上传成功，保存数据库失败:{}",json,e);
                response.getWriter().print("{id:'',fileName:'图片上传成功，保存数据库失败',status:'false',origalName:'','isImage':false}");
            }
        }
    }

    @RequestMapping(value = "/v2/deleteProductGallery", method = RequestMethod.POST)
    public AjaxJsonEntity deleteProductGallery(@RequestParam Long galleryId ,HttpServletRequest request, Model model) {
        if(galleryId == null || galleryId <=0 ) {
            return AjaxJsonEntity.fail("参数无内容",null);
        }
        MDCustomerGallery gallery = new MDCustomerGallery();
        gallery.setId(galleryId);
        MSResponse<Integer> response = msCustomerGalleryFeign.deleteById(gallery);
        if(MSResponse.isSuccessCode(response)) {
            return AjaxJsonEntity.success("删除产品图片成功!", null);
        }else{
            return AjaxJsonEntity.fail("删除产品图片失败!",null);
        }
    }
    //endregion 图库

    /**
     * 读取快递信息
     * @param request
     * @param model
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/v2/getExpressInfo", method = RequestMethod.GET)
    public AjaxJsonEntity getExpressInfo(HttpServletRequest request, Model model) {
        try {
            List<Dict> expresses = MSDictUtils.getDictList(Dict.DICT_TYPE_EXPRESS_TYPE);
            return AjaxJsonEntity.success("success",expresses);
        }catch (Exception e){
            log.error("读取快递信息错误",e);
            return AjaxJsonEntity.fail("读取快递信息错误",null);
        }
    }


    /**
     * 下单时根据客户装载：账户余额,信用额度，及一级分类列表
     * @date 2020-03-13
     * @author Ryan Lu
     *
     * @param customerId 客户id
     * @return {
     * //一级分类
     * productTypes:
     * [
     * { id:id,referId: 品类id,parentId: 父类id,name:name ,items:[{id,name}]},
     * ],
     * //物流公司
     * expresses:[
     * {label:label,value:value},...
     * ],
     * //已维护的订单项目
     * items: [
     * ],
     * //客户信息
     * customer:{
     * balance:1000.00,credit:200.00,blockAmount:500.00,
     * //加急标志
     * urgentFlag : 0,
     * //店铺列表
     * shops: []
     * }
     * }
     * @version 1.0
     */
    @RequiresUser
    @ResponseBody
    @RequestMapping(value = "/v2/getCustomerInfoForCreateOrder", method = RequestMethod.GET)
    public Map<String, Object> getCustomerInfoForCreateOrder(@RequestParam(value = "action",required = false, defaultValue = ACTION_VALUE_NEW_V2) String action,
                                            @RequestParam(value = "customerId") Long customerId,
                                            @RequestParam(value="dataSourceId",required = false, defaultValue = "1") Integer dataSourceId,
                                            HttpServletRequest request, HttpServletResponse response) {

        Map<String, Object> result = Maps.newHashMap();
        result.put(KEY_NAME_SUCCESS, false);
        result.put(KEY_NAME_PRODUCT_TYPES, Lists.newArrayList());//一级分类
        //result.put(KEY_NAME_EXPRESSES, Lists.newArrayList());//物流
        User user = UserUtils.getUser();
        if (user == null || user.getId() == null) {
            result.put(KEY_NAME_MESSAGE, "登录超时，请重新登录。");
            return result;
        }
        if (customerId == null || customerId <= 0L) {
            result.put(KEY_NAME_MESSAGE, "无效的厂商ID。");
            return result;
        }
        Customer customer = customerService.getFromCache(customerId);
        if (customer == null) {
            result.put(KEY_NAME_MESSAGE, "读取厂商信息失败。");
            return result;
        }
        //result.put(KEY_NAME_LOGINUSER,user);

        //清除临时订单缓存
        try {
            String cachekey = OrderUtils.getUserTmpOrderCacheKey(request, response,action);
            if (StringUtils.isNotBlank(cachekey)) {
                CreateOrderModel order = (CreateOrderModel) redisUtils.get(RedisConstant.RedisDBType.REDIS_TEMP_DB, cachekey, CreateOrderModel.class);
                if (order.getCustomer() != null && order.getCustomer().getId() != null && !order.getCustomer().getId().equals(customerId)) {
                    redisUtils.remove(RedisConstant.RedisDBType.REDIS_TEMP_DB, cachekey);
                } else {
                    if (order.getItems() != null && order.getItems().size() > 0) {
                        result.put(KEY_NAME_ITEMS, order.getItems());
                    }
                }
            }
        } catch (Exception e) {
        }
        // customer category
        MSResponse<List<Long>> msResponseCategory = msCustomerProductService.getCustomerCategories(customerId);
        if(!MSResponse.isSuccessCode(msResponseCategory)){
            result.put(KEY_NAME_MESSAGE, "读取客户产品分类失败，请重试。");
            return result;
        }
        // 根据下单配置(applicaiton.yml)指定的品类读取:
        //  一级分类 及 二级分类
        MSResponse<List<TreeDTO>> msResponse = msProductTypeService.findTypeAndItemsByCategoryIds(customerId,new ArrayList(createOrderConfig.getSecondType().getCategories()));
        if(!MSResponse.isSuccessCode(msResponse)){
            result.put(KEY_NAME_MESSAGE, "因网络等原因，读取产品分类失败，请重试。");
            return result;
        }
        if(CollectionUtils.isEmpty(msResponse.getData())){
            result.put(KEY_NAME_MESSAGE, "无产品分类信息。");
            return result;
        }
        result.put(KEY_NAME_PRODUCT_TYPES, msResponse.getData());
        //customer categories

        //物流
        //List<Dict> expresses = MSDictUtils.getDictList(Dict.DICT_TYPE_EXPRESS_TYPE);
        //result.put(KEY_NAME_EXPRESSES, expresses);

        Map<String, Object> customerMap = Maps.newHashMap();
        if (action == null || !action.equalsIgnoreCase(ACTION_VALUE_EDIT)) {
            CustomerFinance finance = customerService.getFinance(customerId);
            double balance = 0.0;
            double credit = 0.0;
            double blockAmount = 0.0;
            if (finance != null && finance.getBalance() != null) {
                balance = finance.getBalance();
            }
            if (finance != null && finance.getCreditFlag() == 1 && finance.getCredit() != null) {
                credit = finance.getCredit();
            }
            if (finance != null && finance.getBlockAmount() != null) {
                blockAmount = finance.getBlockAmount();
            }
            customerMap.put(KEY_NAME_BALANCE, balance);
            customerMap.put(KEY_NAME_CREDIT, credit);
            customerMap.put(KEY_NAME_BLOCK_AMOUNT, blockAmount);
        }
        //List<Dict> shops = B2BMDUtils.getCustomerKKLShopListNew(customerId);
        List<Dict> shops = UserUtils.getShops(customerId,user);// 按客户+账号读取店铺清单
        customerMap.put(KEY_NAME_SHOPS, shops);
        customerMap.put(KEY_NAME_URGENT_FLAG, customer.getUrgentFlag());
        result.put(KEY_NAME_CUSTOMER, customerMap);
        result.put(KEY_NAME_SUCCESS, true);
        return result;
    }

    /**
     * 修改订单时，根据客户装载：店铺，物流，及分类和二级分类列表
     * @date 2020-03-20
     * @author Ryan Lu
     *
     * @param customerId 客户id
     * @return {
     * //一级分类
     * productTypes:
     * [
     * { id:id,referId: 品类id,parentId: 父类id,name:name ,items:[{id,name}]},
     * ],
     * //物流公司
     * expresses:[
     * {label:label,value:value},...
     * ],
     * //店铺列表
     * shops: []
     * }
     * @version 1.0
     */
    @RequiresUser
    @ResponseBody
    @RequestMapping(value = "/v2/getProductTypesForEditOrder", method = RequestMethod.GET)
    public Map<String, Object> getProductTypesForEditOrder(@RequestParam(value = "customerId") Long customerId,HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> result = Maps.newHashMap();
        result.put(KEY_NAME_SUCCESS, false);
        result.put(KEY_NAME_PRODUCT_TYPES, Lists.newArrayList());//一级分类
        result.put(KEY_NAME_EXPRESSES, Lists.newArrayList());//物流
        User user = UserUtils.getUser();
        if (user == null || user.getId() == null) {
            result.put(KEY_NAME_MESSAGE, "登录超时，请重新登录。");
            return result;
        }
        if (customerId == null || customerId <= 0L) {
            result.put(KEY_NAME_MESSAGE, "无效的厂商ID。");
            return result;
        }
        // 根据下单配置(applicaiton.yml)指定的品类读取:
        //  一级分类 及 二级分类
        MSResponse<List<TreeDTO>> msResponse = msProductTypeService.findTypeAndItemsByCategoryIds(customerId,new ArrayList(createOrderConfig.getSecondType().getCategories()));
        if(!MSResponse.isSuccessCode(msResponse)){
            result.put(KEY_NAME_MESSAGE, "因网络等原因，读取产品分类失败，请重试。");
            return result;
        }
        if(CollectionUtils.isEmpty(msResponse.getData())){
            result.put(KEY_NAME_MESSAGE, "无产品分类信息。");
            return result;
        }
        result.put(KEY_NAME_PRODUCT_TYPES, msResponse.getData());
        //物流
        List<Dict> expresses = MSDictUtils.getDictList(Dict.DICT_TYPE_EXPRESS_TYPE);
        result.put(KEY_NAME_EXPRESSES, expresses);
        //List<Dict> shops = B2BMDUtils.getCustomerKKLShopListNew(customerId);
        List<Dict> shops = UserUtils.getShops(customerId,user);// 按客户+账号读取店铺清单 2021/06/22
        result.put(KEY_NAME_SHOPS, shops);
        result.put(KEY_NAME_SUCCESS, true);
        return result;
    }

    /**
     * 根据客户id+二级分类id获取产品规格，属性及产品，品牌，服务
     * @param customerId
     * @param productTypeItemId
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/v2/getProductSpecAndInfoForCreateOrder", method = RequestMethod.GET)
    public AjaxJsonEntity getProductSpecAndInfoForCreateOrder(@RequestParam(value = "customerId") Long customerId ,@RequestParam(value="productTypeItemId") Long productTypeItemId,HttpServletRequest request){
        MSResponse<String> msResponse = msCustomerProductService.getProductSpecAndInfoForCreateOrder(customerId,productTypeItemId);
        if(!MSResponse.isSuccessCode(msResponse)){
            log.error("error:{}",msResponse.getMsg());
            return AjaxJsonEntity.fail(msResponse.getMsg(),null);
        }
        /* 1. 传递json
        if(StringUtils.isNotEmpty(msResponse.getData())){
            //byte[] baseBytes = Base64.getDecoder().decode(msResponse.getData());
            //InputStream is = new ByteArrayInputStream(baseBytes);
            CreateOrderDto.SpecMapProduct dto = null;
            try {
                //dto = CreateOrderDto.SpecMapProduct.parseFrom(msResponse.getData());
                CreateOrderDto.SpecMapProduct.Builder builder = CreateOrderDto.SpecMapProduct.newBuilder();
                //dto = CreateOrderDto.SpecMapProduct.parseFrom(msResponse.getData().getBytes("utf-8"));
                new JsonFormat().merge(new ByteArrayInputStream(msResponse.getData().getBytes("utf-8")),builder);
                dto = builder.build();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println(dto == null?"transfer null":new JsonFormat().printToString(dto));
            return "data";
        }else{
            System.out.println("return no data");
            return "no data";
        }
        */
        /*2.传递String */
        if(StringUtils.isNotEmpty(msResponse.getData())){
            /*
            byte[] baseBytes = Base64.getDecoder().decode(msResponse.getData());
            InputStream is = new ByteArrayInputStream(baseBytes);
            CreateOrderDto.SpecMapProduct dto = null;
            try {
                dto = CreateOrderDto.SpecMapProduct.parseFrom(is);
            } catch (Exception e) {
                e.printStackTrace();
            }*/
            CreateOrderDto.SpecMapProduct dto = ProtobufUtils.transStringToProtobuf(CreateOrderDto.SpecMapProduct.parser(),msResponse.getData());
            //System.out.println(dto == null?"transfer null":new JsonFormat().printToString(dto));
            return AjaxJsonEntity.success(msResponse.getMsg(),new JsonFormat().printToString(dto));
        }else{
            return AjaxJsonEntity.fail("数据传递失败!",null);
        }
    }


    //endregion 新下单

    //endregion [客户功能]
}

