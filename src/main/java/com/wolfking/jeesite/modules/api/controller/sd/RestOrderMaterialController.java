package com.wolfking.jeesite.modules.api.controller.sd;

import com.google.common.base.Splitter;
import com.wolfking.jeesite.common.exception.AttachmentSaveFailureException;
import com.wolfking.jeesite.common.exception.OrderException;
import com.wolfking.jeesite.common.persistence.LongIDBaseEntity;
import com.wolfking.jeesite.common.service.SequenceIdService;
import com.wolfking.jeesite.common.utils.GsonUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.api.controller.RestBaseController;
import com.wolfking.jeesite.modules.api.entity.md.RestLoginUserInfo;
import com.wolfking.jeesite.modules.api.entity.md.RestProduct;
import com.wolfking.jeesite.modules.api.entity.sd.RestMaterialMasterNew;
import com.wolfking.jeesite.modules.api.entity.sd.request.RestGetOrderMaterialsRequest;
import com.wolfking.jeesite.modules.api.entity.sd.request.RestOrderCommonRequest;
import com.wolfking.jeesite.modules.api.entity.sd.request.RestSaveMaterialApplicationRequest;
import com.wolfking.jeesite.modules.api.service.sd.AppOrderMaterialService;
import com.wolfking.jeesite.modules.api.util.ErrorCode;
import com.wolfking.jeesite.modules.api.util.RestResult;
import com.wolfking.jeesite.modules.api.util.RestResultGenerator;
import com.wolfking.jeesite.modules.api.util.RestSessionUtils;
import com.wolfking.jeesite.modules.md.entity.Material;
import com.wolfking.jeesite.modules.md.entity.Product;
import com.wolfking.jeesite.modules.md.entity.ServiceType;
import com.wolfking.jeesite.modules.md.service.CustomerMaterialService;
import com.wolfking.jeesite.modules.md.service.ProductService;
import com.wolfking.jeesite.modules.md.service.ServiceTypeService;
import com.wolfking.jeesite.modules.sd.entity.*;
import com.wolfking.jeesite.modules.sd.service.OrderMaterialService;
import com.wolfking.jeesite.modules.sd.service.OrderService;
import com.wolfking.jeesite.modules.sd.utils.OrderPicUtils;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.service.AreaService;
import com.wolfking.jeesite.modules.sys.utils.AreaUtils;
import com.wolfking.jeesite.modules.sys.utils.SeqUtils;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orderMaterial/")
public class RestOrderMaterialController extends RestBaseController {

    @Autowired
    private AppOrderMaterialService appOrderMaterialService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private CustomerMaterialService customerMaterialService;

    @Autowired
    private OrderMaterialService orderMaterialService;

    @Autowired
    private ServiceTypeService serviceTypeService;

    @Autowired
    ProductService productService;

    @Autowired
    private AreaService areaService;

    @Autowired
    private SequenceIdService sequenceIdService;
    //private static final SequenceIdUtils sequenceIdUtils = new SequenceIdUtils(ThreadLocalRandom.current().nextInt(32), ThreadLocalRandom.current().nextInt(32));


    private RestLoginUserInfo getUserInfo(HttpServletRequest request) {
        return RestSessionUtils.getLoginUserInfoFromRestSession(request.getAttribute("sessionUserId").toString());
    }

    private boolean isLogin(HttpServletRequest request) {
        boolean result = false;
        RestLoginUserInfo userInfo = getUserInfo(request);
        if (userInfo != null && userInfo.getUserId() != null) {
            result = true;
        }
        return result;
    }

    @RequestMapping(value = "getProductMaterialList", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public RestResult<Object> getProductMaterialList(HttpServletRequest request, @RequestBody RestOrderCommonRequest params) {
        if (params == null || params.getOrderId() == null || params.getOrderId() == 0 || StringUtils.isBlank(params.getQuarter())) {
            return RestResultGenerator.custom(ErrorCode.WRONG_REQUEST_FORMAT.code, ErrorCode.WRONG_REQUEST_FORMAT.message);
        }
        try {
            if (!isLogin(request)) {
                return RestResultGenerator.custom(ErrorCode.LOGIN_INFO_MISSING.code, ErrorCode.LOGIN_INFO_MISSING.message);
            }
            Order order = orderService.getOrderById(params.getOrderId(), params.getQuarter(), OrderUtils.OrderDataLevel.CONDITION, true);
            if (order == null || order.getItems() == null || order.getDataSource().getIntValue() == null || order.getItems().isEmpty()) {
                return RestResultGenerator.custom(ErrorCode.DATA_PROCESS_ERROR.code, "订单不存在，或读取订单:" + ErrorCode.DATA_PROCESS_ERROR.message);
            }
            List<RestProduct> productMaterials = appOrderMaterialService.getProductMaterialList(order.getItems());
            return RestResultGenerator.success(productMaterials);
        } catch (OrderException oe) {
            return RestResultGenerator.exception(oe.getMessage());
        } catch (Exception e) {
            return RestResultGenerator.exception("获取工单产品的配件失败");
        }
    }


    @RequestMapping(value = "saveMaterialApplication", consumes = "multipart/form-data", method = RequestMethod.POST)
    public RestResult<Object> saveMaterialApplication(HttpServletRequest request, @RequestParam("file") MultipartFile[] files,
                                                      @RequestParam("json") String json) {
        if (files == null || files.length == 0 || StringUtils.isBlank(json)) {
            return RestResultGenerator.custom(ErrorCode.WRONG_REQUEST_FORMAT.code, ErrorCode.WRONG_REQUEST_FORMAT.message);
        }
        try {
            RestSaveMaterialApplicationRequest params = GsonUtils.getInstance().fromJson(json, RestSaveMaterialApplicationRequest.class);
            if (params.getOrderId() == null || params.getOrderId() == 0 || StringUtils.isBlank(params.getQuarter()) || params.getProducts().isEmpty()) {
                return RestResultGenerator.custom(ErrorCode.WRONG_REQUEST_FORMAT.code, ErrorCode.WRONG_REQUEST_FORMAT.message);
            }
            if (params.getMaterialQty() == 0) {
                return RestResultGenerator.custom(ErrorCode.WRONG_REQUEST_FORMAT.code, "配件不能为空");
            }
            RestLoginUserInfo userInfo = getUserInfo(request);
            if (userInfo == null || userInfo.getUserId() == null) {
                return RestResultGenerator.custom(ErrorCode.LOGIN_INFO_MISSING.code, ErrorCode.LOGIN_INFO_MISSING.message);
            }
            User user = UserUtils.getAcount(userInfo.getUserId());
            if (null == user) {
                return RestResultGenerator.custom(ErrorCode.LOGIN_INFO_MISSING.code, ErrorCode.LOGIN_INFO_MISSING.message);
            }
            Order order = orderService.getOrderById(params.getOrderId(), params.getQuarter(), OrderUtils.OrderDataLevel.DETAIL, true);
            if (order == null || order.getOrderCondition() == null) {
                return RestResultGenerator.custom(ErrorCode.DATA_PROCESS_ERROR.code, "订单不存在，或读取订单:" + ErrorCode.DATA_PROCESS_ERROR.message);
            }
            List<OrderItem> orderItems = order.getItems();
            if(ObjectUtils.isEmpty(orderItems)){
                return RestResultGenerator.custom(ErrorCode.DATA_PROCESS_ERROR.code, "订单无订单项，或读取订单:" + ErrorCode.DATA_PROCESS_ERROR.message);
            }
            //订单项单品列表,包含品牌，型号/规格，服务类型(已包含质保类型)等
            Map<Long, ServiceType> serviceTypeMap = serviceTypeService.getAllServiceTypeMap();
            Set<Product> singleProductSet = orderMaterialService.getOrderProductSet(params.getOrderId(), orderItems ,serviceTypeMap);
            if (org.springframework.util.ObjectUtils.isEmpty(singleProductSet)) {
                return RestResultGenerator.custom(ErrorCode.DATA_PROCESS_ERROR.code, "解析订单项单品产品列表错误");
            }
            Map<Long,Product> products = singleProductSet.stream().collect(Collectors.toMap(Product::getId, item -> item));
            MaterialMaster master = new MaterialMaster();
            Date now = new Date();

            TwoTuple<Boolean, List<String>> saveFileResponse = OrderPicUtils.saveImageFiles(request, files);
            if (!saveFileResponse.getAElement() || saveFileResponse.getBElement().isEmpty()) {
                throw new AttachmentSaveFailureException("保存配件申请的图片附件失败");
            }
            MaterialAttachment attachment;
            for (String filePath : saveFileResponse.getBElement()) {
                attachment = new MaterialAttachment();
                attachment.setOrderId(params.getOrderId());
                attachment.setQuarter(params.getQuarter());
                attachment.setId(sequenceIdService.nextId());
                attachment.setFilePath(filePath);
                attachment.setCreateBy(user);
                attachment.setCreateDate(now);
                master.getAttachments().add(attachment);
            }
            //2019-08-24
            int applyTime = orderMaterialService.getNextApplyTime(order.getId(),order.getQuarter());
            if(applyTime == 0){
                applyTime = 1;
            }
            master.setDataSource(order.getDataSource().getIntValue());//2019-09-20
            master.setApplyTime(applyTime);
            master.setId(sequenceIdService.nextId());
            String no = SeqUtils.NextSequenceNo("MaterialFormNo", 0, 3);
            if (StringUtils.isBlank(no)) {
                return RestResultGenerator.custom(ErrorCode.WRONG_REQUEST_FORMAT.code, "生成配件单号错误,请重试！");
            }
            master.setMasterNo(no);
            master.setThrdNo(order.getParentBizOrderId() == null ? "" : order.getParentBizOrderId());
            master.setOrderCreator(StringUtils.left(order.getOrderCondition().getCreateBy().getName(), 30));
            master.setOrderDetailId(0L);
            master.setOrderId(params.getOrderId());
            master.setQuarter(params.getQuarter());
            master.setCreateBy(user);
            master.setCreateDate(now);
            master.setApplyType(new Dict(params.getApplyType().toString()));
            master.setRemarks(params.getRemarks());

            master.setOrderNo(order.getOrderNo());
            OrderCondition condition = order.getOrderCondition();
            master.setCustomer(condition.getCustomer());
            master.setArea(condition.getArea());
            master.setSubArea(condition.getSubArea());
            master.setUserName(condition.getUserName());
            master.setUserPhone(condition.getServicePhone());
            master.setUserAddress(condition.getAddress());
            master.setCanRush(condition.getCanRush());
            master.setProductCategoryId(condition.getProductCategoryId());
            master.setDescription(order.getDescription());
            orderMaterialService.setArea(condition,master);


            //List<Product> products = Lists.newArrayList();
            //配件单产品信息表
            List<MaterialProduct> materialProducts = com.google.common.collect.Lists.newArrayListWithCapacity(10);
            Map<Long,Material> materialSetingMap;
            MaterialProduct mProduct;
            Product product;
            StringBuilder productIdsBuilder = new StringBuilder(",");
            StringBuilder productNamesBuilder = new StringBuilder(",");
            double totalPrice = 0.0;
            long customerId = order.getOrderCondition().getCustomer().getId();
            int itemNo = 0;
            Material material;
            int idx = 0;
            for (RestSaveMaterialApplicationRequest.Product pItem : params.getProducts()) {
                idx++;
                /*
                product = productService.getProductByIdFromCache(pItem.getProductId());
                if (product == null || product.getId() == null) {
                    return RestResultGenerator.custom(ErrorCode.WRONG_REQUEST_FORMAT.code, "读取产品:" + pItem.getProductId() + " 失败");
                }
                products.add(product);
                */
                product = products.get(pItem.getProductId());
                if(product == null){
                    return RestResultGenerator.custom(ErrorCode.WRONG_REQUEST_FORMAT.code, "读取产品:" + pItem.getProductId() + " 失败");
                }
                if(idx == 1){
                    master.setProduct(product);
                }
                productIdsBuilder.append(product.getId()).append(",");
                productNamesBuilder.append(product.getName()).append(",");
                //产品信息
                itemNo++;
                mProduct = MaterialProduct.builder()
                        .id(sequenceIdService.nextId())
                        .materialMasterId(master.getId())
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
                materialSetingMap = customerMaterialService.getMapFromCache(customerId,product.getId());
                for (RestSaveMaterialApplicationRequest.Material mItem : pItem.getItems()) {
                    //check //2019-08-24
                    material = materialSetingMap.get(mItem.getMaterialId());
                    if(material == null){
                        throw new OrderException("读取客户配件配置错误");
                    }
                    MaterialItem item = new MaterialItem();
                    item.setId(sequenceIdService.nextId());
                    item.setQuarter(params.getQuarter());
                    item.setMaterialMasterId(master.getId());
                    item.setMaterialProductId(mProduct.getId());//2019-08-24
                    item.setProduct(product);
                    item.setQty(mItem.getQty());
                    item.setCreateBy(user);
                    item.setCreateDate(now);
                    item.setUseQty(item.getQty());
                    item.setRtvQty(0);
                    item.setRtvFlag(0);
                    /*返件标记及价格以客户设定优先
                    Material material = customerMaterialService.getMaterialInfoOfCustomer(customerId, product.getId(), mItem.getMaterialId());
                    if (material == null) {
                        throw new OrderException("读取客户配件配置错误");
                    }*/
                    item.setMaterial(material);
                    item.setReturnFlag(material.getIsReturn());
                    //价格以输入为准
                    item.setPrice(mItem.getPrice());
                    item.setFactoryPrice(material.getPrice());//出厂价 //2019-08-24
                    //有一个配件返件，单头标记为：需要返件
                    if (material.getIsReturn() == 1) {
                        master.setReturnFlag(material.getIsReturn());
                    }
                    item.setTotalPrice(item.getPrice() * item.getQty());
                    totalPrice = totalPrice + item.getTotalPrice();
                    master.getItems().add(item);
                }
            }
            //产品
            master.setProductInfos(materialProducts);
            //master.setProduct(products.get(0));
            master.setProductIds(productIdsBuilder.toString());
            master.setProductNames(productNamesBuilder.toString());
            Set<Long> pIdSet = singleProductSet.stream().map(LongIDBaseEntity::getId).collect(Collectors.toSet());
            Long orderDetailId = appOrderMaterialService.getOrderDetailId(order.getDetailList(), pIdSet);
            master.setOrderDetailId(orderDetailId);
            orderMaterialService.addAppMaterialApplies(order, Lists.newArrayList(master));
            return RestResultGenerator.success();
        } catch (AttachmentSaveFailureException ae) {
            return RestResultGenerator.custom(ErrorCode.ORDER_CAN_NOT_SAVECOMPONENT.code, ErrorCode.ORDER_CAN_NOT_SAVECOMPONENT.message + ":保存文件失败");
        } catch (OrderException oe) {
            return RestResultGenerator.custom(ErrorCode.DATA_PROCESS_ERROR.code, oe.getMessage());
        } catch (Exception e) {
            return RestResultGenerator.exception("保存配件申请单失败");
        }
    }

    @RequestMapping(value = "saveMaterialApplicationNew", consumes = "multipart/form-data", method = RequestMethod.POST)
    public RestResult<Object> saveMaterialApplicationNew(HttpServletRequest request, @RequestParam("file") MultipartFile[] files,
                                                         @RequestParam("json") String json) {
        if (files == null || files.length == 0 || StringUtils.isBlank(json)) {
            return RestResultGenerator.custom(ErrorCode.WRONG_REQUEST_FORMAT.code, ErrorCode.WRONG_REQUEST_FORMAT.message);
        }
        try {
            RestSaveMaterialApplicationRequest params = GsonUtils.getInstance().fromJson(json, RestSaveMaterialApplicationRequest.class);
            if (params.getOrderId() == null || params.getOrderId() == 0 || StringUtils.isBlank(params.getQuarter()) || params.getProducts().isEmpty()) {
                return RestResultGenerator.custom(ErrorCode.WRONG_REQUEST_FORMAT.code, ErrorCode.WRONG_REQUEST_FORMAT.message);
            }
            if (params.getMaterialQty() == 0) {
                return RestResultGenerator.custom(ErrorCode.WRONG_REQUEST_FORMAT.code, "配件不能为空");
            }
            if (MaterialMaster.APPLY_TYPE_CHANGJIA.equals(params.getApplyType())) {
                if (StringUtils.isBlank(params.getReceiver()) || StringUtils.isBlank(params.getReceiverPhone())
                        || StringUtils.isBlank(params.getReceiverAddress()) || params.getReceiverType() == null || params.getReceiverType() == 0) {
                    return RestResultGenerator.custom(ErrorCode.WRONG_REQUEST_FORMAT.code, "厂家寄发的配件必须填写收件人信息");
                }
            }
            RestLoginUserInfo userInfo = getUserInfo(request);
            if (userInfo == null || userInfo.getUserId() == null) {
                return RestResultGenerator.custom(ErrorCode.LOGIN_INFO_MISSING.code, ErrorCode.LOGIN_INFO_MISSING.message);
            }
            User user = UserUtils.getAcount(userInfo.getUserId());
            if (null == user) {
                return RestResultGenerator.custom(ErrorCode.LOGIN_INFO_MISSING.code, ErrorCode.LOGIN_INFO_MISSING.message);
            }
            Long areaId = 0L;
            String subAddress = "";
            String[] result;
            if (MaterialMaster.APPLY_TYPE_CHANGJIA.equals(params.getApplyType())) {
                try {
                    result = AreaUtils.parseAddress(params.getReceiverAddress());
                } catch (Exception e) {
                    return RestResultGenerator.custom(ErrorCode.WRONG_REQUEST_FORMAT.code, "配件收件地址解析失败，请重试");
                }
                if (result != null && result.length > 2) {
                    areaId = StringUtils.toLong(result[0]);
                    subAddress = result[2];
                } else {
                    return RestResultGenerator.custom(ErrorCode.WRONG_REQUEST_FORMAT.code, "配件收件地址解析失败，请重试");
                }
            }
            Order order = orderService.getOrderById(params.getOrderId(), params.getQuarter(), OrderUtils.OrderDataLevel.DETAIL, true);
            if (order == null || order.getOrderCondition() == null) {
                return RestResultGenerator.custom(ErrorCode.DATA_PROCESS_ERROR.code, "订单不存在，或读取订单:" + ErrorCode.DATA_PROCESS_ERROR.message);
            }
            List<OrderItem> orderItems = order.getItems();
            if (ObjectUtils.isEmpty(orderItems)) {
                return RestResultGenerator.custom(ErrorCode.DATA_PROCESS_ERROR.code, "订单无订单项，或读取订单:" + ErrorCode.DATA_PROCESS_ERROR.message);
            }
            //订单项单品列表,包含品牌，型号/规格，服务类型(已包含质保类型)等
            Map<Long, ServiceType> serviceTypeMap = serviceTypeService.getAllServiceTypeMap();
            Set<Product> singleProductSet = orderMaterialService.getOrderProductSet(params.getOrderId(), orderItems, serviceTypeMap);
            if (org.springframework.util.ObjectUtils.isEmpty(singleProductSet)) {
                return RestResultGenerator.custom(ErrorCode.DATA_PROCESS_ERROR.code, "解析订单项单品产品列表错误");
            }
            Map<Long, Product> products = singleProductSet.stream().collect(Collectors.toMap(Product::getId, item -> item));
            MaterialMaster master = new MaterialMaster();
            Date now = new Date();

            TwoTuple<Boolean, List<String>> saveFileResponse = OrderPicUtils.saveImageFiles(request, files);
            if (!saveFileResponse.getAElement() || saveFileResponse.getBElement().isEmpty()) {
                throw new AttachmentSaveFailureException("保存配件申请的图片附件失败");
            }
            MaterialAttachment attachment;
            for (String filePath : saveFileResponse.getBElement()) {
                attachment = new MaterialAttachment();
                attachment.setOrderId(params.getOrderId());
                attachment.setQuarter(params.getQuarter());
                attachment.setId(sequenceIdService.nextId());
                attachment.setFilePath(filePath);
                attachment.setCreateBy(user);
                attachment.setCreateDate(now);
                master.getAttachments().add(attachment);
            }
            //2019-08-24
            int applyTime = orderMaterialService.getNextApplyTime(order.getId(), order.getQuarter());
            if (applyTime == 0) {
                applyTime = 1;
            }
            master.setDataSource(order.getDataSource().getIntValue());//2019-09-20
            master.setApplyTime(applyTime);
            master.setId(sequenceIdService.nextId());
            String no = SeqUtils.NextSequenceNo("MaterialFormNo", 0, 3);
            if (StringUtils.isBlank(no)) {
                return RestResultGenerator.custom(ErrorCode.WRONG_REQUEST_FORMAT.code, "生成配件单号错误,请重试！");
            }
            master.setMasterNo(no);
            master.setThrdNo(order.getParentBizOrderId() == null ? "" : order.getParentBizOrderId());
            master.setOrderCreator(StringUtils.left(order.getOrderCondition().getCreateBy().getName(), 30));
            master.setOrderDetailId(0L);
            master.setOrderId(params.getOrderId());
            master.setQuarter(params.getQuarter());
            master.setCreateBy(user);
            master.setCreateDate(now);
            master.setApplyType(new Dict(params.getApplyType().toString()));
            master.setRemarks(params.getRemarks());

            master.setOrderNo(order.getOrderNo());
            OrderCondition condition = order.getOrderCondition();
            master.setCustomer(condition.getCustomer());
            master.setArea(condition.getArea());
            master.setSubArea(condition.getSubArea());
            master.setUserName(condition.getUserName());
            master.setUserPhone(condition.getServicePhone());
            master.setUserAddress(condition.getAddress());
            master.setCanRush(condition.getCanRush());
            master.setProductCategoryId(condition.getProductCategoryId());
            master.setDescription(order.getDescription());

            //TODO: 2020-8-7 新增收件人信息
            if (MaterialMaster.APPLY_TYPE_CHANGJIA.equals(params.getApplyType())) {
                master.setReceiver(params.getReceiver());
                master.setReceiverPhone(params.getReceiverPhone());
                master.setReceiverAddress(subAddress);
                master.setReceiverAreaId(areaId);
                Area area = areaService.getFromCache(areaId);
                if (area != null) {
                    List<String> ids = Splitter.onPattern(",")
                            .omitEmptyStrings()
                            .trimResults()
                            .splitToList(area.getParentIds());
                    if (ids.size() >= 2) {
                        master.setReceiverCityId(StringUtils.toLong(ids.get(ids.size() - 1)));
                        master.setReceiverProvinceId(StringUtils.toLong(ids.get(ids.size() - 2)));
                    }
                }
                master.setReceiverType(params.getReceiverType());
            }

            orderMaterialService.setArea(condition, master);
            //List<Product> products = Lists.newArrayList();
            //配件单产品信息表
            List<MaterialProduct> materialProducts = com.google.common.collect.Lists.newArrayListWithCapacity(10);
            Map<Long, Material> materialSetingMap;
            MaterialProduct mProduct;
            Product product;
            StringBuilder productIdsBuilder = new StringBuilder(",");
            StringBuilder productNamesBuilder = new StringBuilder(",");
            double totalPrice = 0.0;
            long customerId = order.getOrderCondition().getCustomer().getId();
            int itemNo = 0;
            Material material;
            int idx = 0;
            for (RestSaveMaterialApplicationRequest.Product pItem : params.getProducts()) {
                idx++;
                /*
                product = productService.getProductByIdFromCache(pItem.getProductId());
                if (product == null || product.getId() == null) {
                    return RestResultGenerator.custom(ErrorCode.WRONG_REQUEST_FORMAT.code, "读取产品:" + pItem.getProductId() + " 失败");
                }
                products.add(product);
                */
                product = products.get(pItem.getProductId());
                if (product == null) {
                    return RestResultGenerator.custom(ErrorCode.WRONG_REQUEST_FORMAT.code, "读取产品:" + pItem.getProductId() + " 失败");
                }
                if (idx == 1) {
                    master.setProduct(product);
                }
                productIdsBuilder.append(product.getId()).append(",");
                productNamesBuilder.append(product.getName()).append(",");
                //产品信息
                itemNo++;
                mProduct = MaterialProduct.builder()
                        .id(sequenceIdService.nextId())
                        .materialMasterId(master.getId())
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
                materialSetingMap = customerMaterialService.getMapFromCache(customerId, product.getId());
                for (RestSaveMaterialApplicationRequest.Material mItem : pItem.getItems()) {
                    //check //2019-08-24
                    material = materialSetingMap.get(mItem.getMaterialId());
                    if (material == null) {
                        throw new OrderException("读取客户配件配置错误");
                    }
                    MaterialItem item = new MaterialItem();
                    item.setId(sequenceIdService.nextId());
                    item.setQuarter(params.getQuarter());
                    item.setMaterialMasterId(master.getId());
                    item.setMaterialProductId(mProduct.getId());//2019-08-24
                    item.setProduct(product);
                    item.setQty(mItem.getQty());
                    item.setCreateBy(user);
                    item.setCreateDate(now);
                    item.setUseQty(item.getQty());
                    item.setRtvQty(0);
                    item.setRtvFlag(0);
                    /*返件标记及价格以客户设定优先
                    Material material = customerMaterialService.getMaterialInfoOfCustomer(customerId, product.getId(), mItem.getMaterialId());
                    if (material == null) {
                        throw new OrderException("读取客户配件配置错误");
                    }*/
                    item.setMaterial(material);
                    item.setReturnFlag(material.getIsReturn());
                    //价格以输入为准
                    item.setPrice(mItem.getPrice());
                    item.setFactoryPrice(material.getPrice());//出厂价 //2019-08-24
                    //有一个配件返件，单头标记为：需要返件
                    if (material.getIsReturn() == 1) {
                        master.setReturnFlag(material.getIsReturn());
                    }
                    item.setTotalPrice(item.getPrice() * item.getQty());
                    totalPrice = totalPrice + item.getTotalPrice();
                    master.getItems().add(item);
                }
            }
            //产品
            master.setProductInfos(materialProducts);
            //master.setProduct(products.get(0));
            master.setProductIds(productIdsBuilder.toString());
            master.setProductNames(productNamesBuilder.toString());
            Set<Long> pIdSet = singleProductSet.stream().map(LongIDBaseEntity::getId).collect(Collectors.toSet());
            Long orderDetailId = appOrderMaterialService.getOrderDetailId(order.getDetailList(), pIdSet);
            master.setOrderDetailId(orderDetailId);
            orderMaterialService.addAppMaterialApplies(order, org.assertj.core.util.Lists.newArrayList(master));
            return RestResultGenerator.success();
        } catch (AttachmentSaveFailureException ae) {
            return RestResultGenerator.custom(ErrorCode.ORDER_CAN_NOT_SAVECOMPONENT.code, ErrorCode.ORDER_CAN_NOT_SAVECOMPONENT.message + ":保存文件失败");
        } catch (OrderException oe) {
            return RestResultGenerator.custom(ErrorCode.DATA_PROCESS_ERROR.code, oe.getMessage());
        } catch (Exception e) {
            return RestResultGenerator.exception("保存配件申请单失败");
        }
    }



    @RequestMapping(value = "getOrderMaterialApplicationList", produces = "application/json;charset=UTF-8", method = RequestMethod.POST)
    public RestResult<Object> getOrderMaterialApplicationList(HttpServletRequest request, @RequestBody RestGetOrderMaterialsRequest params) {
        if (params == null || params.getOrderId() == null || params.getOrderId() == 0 || StringUtils.isBlank(params.getQuarter())) {
            return RestResultGenerator.custom(ErrorCode.WRONG_REQUEST_FORMAT.code, ErrorCode.WRONG_REQUEST_FORMAT.message);
        }
        try {
            if (!isLogin(request)) {
                return RestResultGenerator.custom(ErrorCode.LOGIN_INFO_MISSING.code, ErrorCode.LOGIN_INFO_MISSING.message);
            }
            List<RestMaterialMasterNew> list = appOrderMaterialService.getOrderMaterials(params.getOrderId(), params.getQuarter());
            return RestResultGenerator.success(list);
        } catch (OrderException oe) {
            return RestResultGenerator.custom(ErrorCode.DATA_PROCESS_ERROR.code, oe.getMessage());
        } catch (Exception e) {
            return RestResultGenerator.custom(ErrorCode.DATA_PROCESS_ERROR.code, ErrorCode.DATA_PROCESS_ERROR.message);
        }
    }
}

