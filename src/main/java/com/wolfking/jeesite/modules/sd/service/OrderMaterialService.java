/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.wolfking.jeesite.modules.sd.service;

import cn.hutool.core.util.StrUtil;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2bcenter.md.B2BDataSourceEnum;
import com.kkl.kklplus.entity.common.NameValuePair;
import com.kkl.kklplus.entity.common.material.B2BMaterialClose;
import com.kkl.kklplus.entity.lm.mq.MQLMExpress;
import com.kkl.kklplus.entity.md.MDCustomerAddress;
import com.kkl.kklplus.entity.md.MDCustomerEnum;
import com.kkl.kklplus.entity.push.AppMessageType;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.exception.OrderException;
import com.wolfking.jeesite.common.persistence.LongIDBaseEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.service.SequenceIdService;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.PushMessageUtils;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.wolfking.jeesite.modules.md.entity.*;
import com.wolfking.jeesite.modules.md.service.CustomerService;
import com.wolfking.jeesite.modules.md.service.MaterialService;
import com.wolfking.jeesite.modules.md.service.ProductService;
import com.wolfking.jeesite.modules.md.service.ServicePointService;
import com.wolfking.jeesite.modules.sd.dao.OrderMaterialDao;
import com.wolfking.jeesite.modules.sd.dao.OrderMaterialLogDao;
import com.wolfking.jeesite.modules.sd.entity.*;
import com.wolfking.jeesite.modules.sd.entity.mapper.MaterialMasterMapper;
import com.wolfking.jeesite.modules.sd.entity.viewModel.MaterialMasterVM;
import com.wolfking.jeesite.modules.sd.entity.viewModel.OrderMaterialSearchModel;
import com.wolfking.jeesite.modules.sd.utils.OrderCacheUtils;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.service.AreaService;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.common.config.MicroServicesProperties;
import com.wolfking.jeesite.ms.entity.AppPushMessage;
import com.wolfking.jeesite.ms.logistics.service.LogisticsBusinessService;
import com.wolfking.jeesite.ms.material.service.B2BMaterialExecutor;
import com.wolfking.jeesite.ms.material.service.B2BMaterialExecutorFactory;
import com.wolfking.jeesite.ms.providermd.service.MSCustomerNewService;
import com.wolfking.jeesite.ms.service.sys.MSUserService;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import com.wolfking.jeesite.ms.utils.MSUserUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.wolfking.jeesite.modules.sd.utils.OrderUtils.ORDER_LOCK_EXPIRED;

/**
 * 订单配件管理服务
 * 包含配件单及跟踪进度
 */
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Slf4j
public class OrderMaterialService extends OrderRegionService {

    //id generator
    //private static final SequenceIdUtils sequenceIdUtils = new SequenceIdUtils(ThreadLocalRandom.current().nextInt(32),ThreadLocalRandom.current().nextInt(32));

    @Autowired
    private SequenceIdService sequenceIdService;
    
    /**
     * 持久层对象
     */
    @Autowired
    protected OrderMaterialLogDao logDao;

    @Autowired
    private OrderMaterialDao dao;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductService productService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private AreaService areaService;

    @Autowired
    private MSUserService userService;

    @Autowired
    private MaterialService materialService;

    @Autowired
    private OrderMaterialReturnService returnService;

    @Autowired
    private LogisticsBusinessService logisticsBusinessService;

    @Value("${logistics.materialFlag}")
    private Boolean logisticsMaterialFlag;

    //b2b配件开关
    @Autowired
    private MicroServicesProperties msProperties;

    @Autowired
    private B2BMaterialExecutorFactory b2bMaterialExecutorFactory;

    @Autowired
    private MSCustomerNewService customerNewService;

    @Autowired
    private ServicePointService servicePointService;

    //region 日志
    @Transactional
    public void insertLog(MaterialLog log){
        log.setCreateBy(StringUtils.left(log.getCreateBy(),30));
        log.setContent(StringUtils.left(log.getContent(),150));
        logDao.insertLog(log);
    }

    public List<MaterialLog> getLogs(long materialMasterId,String quarter){
        return logDao.getLogs(materialMasterId, quarter,MaterialLog.SORT_DESC);
    }

    public List<MaterialLog> getLogs(long materialMasterId,String quarter,String sortBy){
        return logDao.getLogs(materialMasterId, quarter,sortBy);
    }

    //endregion

    //region 配件申请单

    /**
     * 按订单id返回配件申请记录（包含配件及图片）
     * 产品及配件需重缓存单独读取
     */
    public List<MaterialMaster> findMaterialMastersByOrderIdMS(Long orderId, String quarter) {
        List<MaterialMaster> list = dao.findMaterialMastersNoAttachByOrderId(orderId, quarter);
        if (list.size() > 0) {
            Map<String, Dict> expressTypeMap = MSDictUtils.getDictMap("express_type");
            //Map<String, Dict> materialTypeMap = MSDictUtils.getDictMap("MaterialType");
            Map<String, Dict> statusMap = MSDictUtils.getDictMap("material_apply_status");
            Map<String, Dict> applyTypeMap = MSDictUtils.getDictMap("material_apply_type");
            Map<String,Dict> rejectTypes = MSDictUtils.getDictMap("material_reject_type");
            Map<Long,Product> productMap = Maps.newHashMap();
            Map<Long, Material> materialMap =  Maps.newHashMap();
            Product product;
            Dict rejectType;
            //user微服务
            List<Long> userIds = list.stream().filter(i -> i.getCreateBy() != null && i.getCreateBy().getId() != null)
                    .map(i -> i.getCreateBy().getId()).distinct().collect(Collectors.toList());
            Map<Long, String> nameMap = MSUserUtils.getNamesByUserIds(userIds);
            for (MaterialMaster item : list) {
                if (item.getExpressCompany() != null && item.getExpressCompany().getValue() != null) {
                    Dict expressTypeDict = expressTypeMap.get(item.getExpressCompany().getValue());
                    item.getExpressCompany().setLabel(expressTypeDict != null ? expressTypeDict.getLabel() : "");
                }
                if (item.getStatus() != null && StringUtils.toInteger(item.getStatus().getValue()) > 0) {
                    Dict statusDict = statusMap.get(item.getStatus().getValue());
                    item.getStatus().setLabel(statusDict != null ? statusDict.getLabel() : "");
                }
                //驳回
                if(item.getStatus().getIntValue() == 5){
                    String closeType = item.getCloseType();
                    if(StrUtil.isNotBlank(closeType)){
                        rejectType = rejectTypes.computeIfAbsent(closeType, k -> new Dict(closeType, ""));
                        item.setCloseRemark(String.format("%s-%s",rejectType.getLabel(),item.getCloseRemark()));
                    }
                }
                if (item.getApplyType() != null && StringUtils.toInteger(item.getApplyType().getValue()) > 0) {
                    Dict appTypeDict = applyTypeMap.get(item.getApplyType().getValue());
                    item.getApplyType().setLabel(appTypeDict != null ? appTypeDict.getLabel() : "");
                }
                if (item.getCreateBy() != null && item.getCreateBy().getId() != null) {
                    item.getCreateBy().setName(StringUtils.toString(nameMap.get(item.getCreateBy().getId())));
                }
                product = productMap.get(item.getProduct().getId());
                if(product == null){
                    product = productService.getProductByIdFromCache(item.getProduct().getId());
                    if(product != null){
                        item.setProduct(product);
                        productMap.put(product.getId(),product);
                    }
                }else{
                    item.setProduct(product);
                }
                //items
                loadItemMaterials(item.getItems(),materialMap,false);
            }
        }
        return list;
    }

    /**
     * 配件申请单转前端视图模型实例
     */
    public List<MaterialMasterVM> materialMasterListToVMList(List<MaterialMaster> materialMasters,boolean waitingB2BCommand){
        if (ObjectUtils.isEmpty(materialMasters)){
            return null;
        }
        List<MaterialMasterVM> list = Lists.newArrayListWithCapacity(materialMasters.size()*2);
        MaterialMaster master;
        MaterialMasterVM form;
        MaterialReturn materialReturn;
        for(int i=0,size=materialMasters.size();i<size;i++){
            master = materialMasters.get(i);
            //单品或套组只申请了单品
            if(master.getProductIds().contains(",")){
                form = materialMasterManyProductToVModel(master);
            }else{
                form = materialMasterOneProductToVModel(master);
            }
            //厂家寄发
            if(master.getDataSource().equals(B2BDataSourceEnum.XYINGYAN.id)){
                form.setWaitingB2BCommand(0);
            }else{
                if(master.getApplyType().getIntValue() == MaterialMaster.APPLY_TYPE_CHANGJIA){
                    form.setWaitingB2BCommand(waitingB2BCommand?1:0);
                }
            }
            materialReturn =returnService.getReturnExpressNoAndStatus(master.getId(),master.getQuarter());
            if(materialReturn!=null && materialReturn.getStatus()!=null && materialReturn.getStatus().getIntValue()>2 && StringUtils.isNotBlank(materialReturn.getExpressNo())){
                form.setMaterialReturnSendFlag(1);
                StringBuffer receiverInfo = new StringBuffer("");
                Area area = areaService.getFromCache(materialReturn.getReceiverAreaId());
                receiverInfo.append(materialReturn.getReceivor());
                receiverInfo.append(" "+materialReturn.getReceivorPhone());
                receiverInfo.append(" 地址：");
                if(area!=null){
                    receiverInfo.append(area.getFullName());
                }
                receiverInfo.append(materialReturn.getReceivorAddress());
                form.setMaterialReturnReceiverInfo(receiverInfo.toString());
            }
            list.add(form);
        }
        return list;
    }

    /**
     * 单配件单转单视图模型
     * 配件单只有一个产品情况
     */
    private MaterialMasterVM materialMasterOneProductToVModel(MaterialMaster master){
        if(master == null || master.getId() == null || master.getId() <= 0 || ObjectUtils.isEmpty(master.getItems())){
            return null;
        }
        if(ObjectUtils.isEmpty(master.getApplyType()) || ObjectUtils.isEmpty(master.getStatus()) || master.getCreateDate() == null){
            return null;
        }
        MaterialMasterVM form;
        List<MaterialItem> materials = master.getItems();
        int size = materials.size();
        List<MaterialMasterVM.MaterialItemVM> items = Lists.newArrayListWithCapacity(size);
        MaterialItem item;
        double totalPrice = 0.0;
        Long productId = 0L;
        StringBuffer receiverInfo = new StringBuffer("");
        if(master.getApplyType().getIntValue()==2){
            Area area = areaService.getFromCache(master.getReceiverAreaId());
            receiverInfo.append(master.getReceiver());
            receiverInfo.append(" "+master.getReceiverPhone());
            receiverInfo.append(" 地址：");
            if(area!=null){
                receiverInfo.append(area.getFullName());
            }
            receiverInfo.append(master.getReceiverAddress());
        }
        for(int i=0;i<size;i++){
            item = materials.get(i);
            if(i==0){
                productId = item.getProduct().getId();
            }
            totalPrice = totalPrice + item.getTotalPrice();
            items.add(
            MaterialMasterVM.MaterialItemVM.builder()
                    .id(item.getId())
                    .materialId(item.getMaterial().getId())
                    .materialName(item.getMaterial().getName())
                    .qty(item.getQty())
                    .price(item.getPrice())
                    .totalPrice(item.getTotalPrice())
                    .returnFlag(item.getReturnFlag())
                    .recyclePrice(master.getStatus().getIntValue()==1?item.getMaterial().getRecyclePrice():item.getRecyclePrice())
                    .recycleFlag(master.getStatus().getIntValue()==1?item.getMaterial().getRecycleFlag():item.getRecycleFlag())
                    .totalRecyclePrice(master.getStatus().getIntValue()==1?item.getMaterial().getRecyclePrice()*item.getQty():item.getTotalRecyclePrice())
                    .build()
            );
        }
        Product product = productService.getProductByIdFromCache(productId);
        if(product == null){
            product = new Product(productId);
        }
        MaterialMasterVM.MaterialSubForm subForm = MaterialMasterVM.MaterialSubForm.builder()
                .product(product)
                .materials(items)
                .totalPrice(totalPrice)
                .build();
        form = MaterialMasterVM.builder()
                .id(master.getId())
                .quarter(master.getQuarter())
                .orderId(master.getOrderId())
                .orderNo(master.getOrderNo())
                .masterNo(master.getMasterNo())
                .applyType(master.getApplyType())
                .productId(master.getProductId())
                .expressNo(master.getExpressNo())
                .expressCompany(master.getExpressCompany())
                .status(master.getStatus())
                .returnFlag(master.getReturnFlag())
                .createDate(DateUtils.formatDate(master.getCreateDate(),"yyyy-MM-dd HH:mm"))
                .remarks(master.getRemarks())
                .totalPrice(master.getTotalPrice())
                .receivedInfo(receiverInfo.toString())
                .closeRemark(master.getCloseRemark())
                .subForms(Lists.newArrayList(subForm))
                .build();
        return form;
    }

    /**
     * 单配件单转多个视图模型
     * 配件单多产品情况
     */
    private MaterialMasterVM materialMasterManyProductToVModel(MaterialMaster master){
        if(master == null || master.getId() == null || master.getId() <= 0 || ObjectUtils.isEmpty(master.getItems())){
            return null;
        }
        if(ObjectUtils.isEmpty(master.getApplyType()) || ObjectUtils.isEmpty(master.getStatus()) || master.getCreateDate() == null){
            return null;
        }

        MaterialMasterVM form;
        Map<Long,List<MaterialItem>> materialMaps = master.getItems().stream()
                .collect(Collectors.groupingBy(
                        (m) -> m.getProduct().getId()
            ));
        List<MaterialMasterVM.MaterialItemVM> items;
        MaterialItem item;
        double totalPrice = 0.0;
        Long productId;
        List<MaterialItem> materials;
        List<MaterialMasterVM.MaterialSubForm> subForms = Lists.newArrayListWithCapacity(5);
        int size;
        Product product;

        StringBuffer receiverInfo = new StringBuffer("");
        if(master.getApplyType().getIntValue()==2){
            Area area = areaService.getFromCache(master.getReceiverAreaId());
            receiverInfo.append(master.getReceiver());
            receiverInfo.append(" "+master.getReceiverPhone());
            receiverInfo.append(" 地址：");
            if(area!=null){
                receiverInfo.append(area.getFullName());
            }
            receiverInfo.append(master.getReceiverAddress());
        }
        for(Map.Entry<Long,List<MaterialItem>> entry:materialMaps.entrySet()){
            materials = entry.getValue();
            if(ObjectUtils.isEmpty(materials)){
                continue;
            }
            size = materials.size();
            totalPrice = 0.0;
             items = Lists.newArrayListWithCapacity(size);
            for(int i=0;i<size;i++){
                item = materials.get(i);
                totalPrice = totalPrice + item.getTotalPrice();
                items.add(
                        MaterialMasterVM.MaterialItemVM.builder()
                                .id(item.getId())
                                .materialId(item.getMaterial().getId())
                                .materialName(item.getMaterial().getName())
                                .qty(item.getQty())
                                .price(item.getPrice())
                                .totalPrice(item.getTotalPrice())
                                .returnFlag(item.getReturnFlag())
                                .recyclePrice(master.getStatus().getIntValue()==1?(-item.getMaterial().getRecyclePrice()):item.getRecyclePrice())
                                .recycleFlag(master.getStatus().getIntValue()==1?item.getMaterial().getRecycleFlag():item.getRecycleFlag())
                                .totalRecyclePrice(master.getStatus().getIntValue()==1?(-item.getMaterial().getRecyclePrice()*item.getQty()):item.getTotalRecyclePrice())
                                .build()
                );
            }
            product = productService.getProductByIdFromCache(entry.getKey());
            if(product == null){
                product = new Product(entry.getKey());
            }
            MaterialMasterVM.MaterialSubForm subForm = MaterialMasterVM.MaterialSubForm.builder()
                    .product(product)
                    .materials(items)
                    .totalPrice(totalPrice)
                    .build();
            subForms.add(subForm);
        }

        form = MaterialMasterVM.builder()
                .id(master.getId())
                .quarter(master.getQuarter())
                .orderId(master.getOrderId())
                .orderNo(master.getOrderNo())
                .masterNo(master.getMasterNo())
                .applyType(master.getApplyType())
                .productId(master.getProductId())
                .expressNo(master.getExpressNo())
                .expressCompany(master.getExpressCompany())
                .status(master.getStatus())
                .returnFlag(master.getReturnFlag())
                .createDate(DateUtils.formatDate(master.getCreateDate(),"yyyy-MM-dd HH:mm"))
                .remarks(master.getRemarks())
                .totalPrice(master.getTotalPrice())
                .receivedInfo(receiverInfo.toString())
                .closeRemark(master.getCloseRemark())
                .subForms(subForms)
                .build();
        return form;
    }

    /**
     * 按订单返回配件申请单单头记录（不含配件和图片）
     */
    public List<MaterialMaster> findMaterialMasterHeadsByOrderId(Long orderId, String quarter) {
        return dao.findMaterialMasterHeadsByOrderId(orderId, quarter);
    }

    /**
     * 按配件申请单id查询其附件
     *
     * @param masterId
     * @return
     */
    public List<MaterialAttachment> findAttachementsByMasterId(Long masterId, String quarter) {
        return dao.findAttachementsByMasterId(masterId, quarter);
    }

    /**
     * 按id获得配件申请单信息,包含单身
     * 不包含图片
     * 产品及配件需重缓存单独读取
     * @param id
     */
    public MaterialMaster getMaterialMasterById(Long id, String quarter) {
        return getMaterialMasterById(id, quarter,false);
    }

    /**
     * 按id获得配件申请单信息,包含单身
     * 不包含图片
     * 产品及配件需重缓存单独读取
     * @param id
     */
    public MaterialMaster getMaterialMasterById(Long id, String quarter,boolean loadProductOfMateiral) {
        MaterialMaster materialMaster = dao.getMaterialMasterById(id, quarter);
        //切换为微服务
        if (materialMaster != null && materialMaster.getStatus() != null && StringUtils.toInteger(materialMaster.getStatus().getValue()) > 0) {
            String statusLabel = MSDictUtils.getDictLabel(materialMaster.getStatus().getValue(), "material_apply_status", "");
            materialMaster.getStatus().setLabel(statusLabel);
        }
        if(materialMaster != null){
            Product product = productService.getProductByIdFromCache(materialMaster.getProduct().getId());
            if(product != null){
                materialMaster.setProduct(product);
            }
            loadItemMaterials(materialMaster.getItems(),null,loadProductOfMateiral);
        }
        return materialMaster;
    }
    /**
     * 按id获得配件申请单单头信息
     *
     * @param id      申请单id
     * @param quarter 分片
     */
    public MaterialMaster getMaterialMasterHeadById(Long id, String quarter) {
        MaterialMaster master = dao.getMaterialMasterHeadById(id, quarter);
        if (master != null) {
            Dict status = MSDictUtils.getDictByValue(master.getStatus().getValue(), "material_apply_status");//切换为微服务
            if (status != null) {
                master.setStatus(status);
            }
            Product product = productService.getProductByIdFromCache(master.getProduct().getId());
            if(product != null){
                master.setProduct(product);
            }
        }
        return master;
    }

    /**
     * 按id获得配件申请单单身
     * 产品及配件需重缓存单独读取
     * @param id      申请单id
     * @param quarter 分片
     */
    public List<MaterialItem> getMaterialMasterItemsById(Long id, String quarter) {
        List<MaterialItem> list =  dao.getMaterialMasterItemsById(id, quarter);
        loadItemMaterials(list,null,false);
        return list;
    }

    /**
     * 订单未审核的配件申请单数量
     * 2018/04/18变更:
     * 1.配件及返件单状态必须都通过审核(2 - 待发货)或驳回(5 - 已驳回)
     * 2.返件：状态必须是：3 - 已发货  (配件单不必检查)
     */
    public Integer getNoApprovedMaterialMasterQty(Long orderId, String quarter) {
        return dao.getNoApprovedMaterialMasterQty(orderId, quarter);
    }

    /**
     * 保存APP配件申请
     * 一次提交只产生一个配件单
     */
    @Transactional
    public void addAppMaterialApplies(Order order, List<MaterialMaster> materialMasters) {
        if (order == null || order.getOrderCondition() == null || materialMasters == null || materialMasters.size() == 0) {
            return;
        }
        Long orderId = order.getId();

        String lockkey = String.format(RedisConstant.SD_ORDER_LOCK, orderId);
        //获得锁
        Boolean locked = redisUtils.setNX(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey, 1, ORDER_LOCK_EXPIRED);//60秒
        if (!locked) {
            throw new OrderException("此订单正在处理中，请稍候重试，或刷新页面。");
        }
        String quarter = order.getQuarter();
        User user = null;
        Date date = null;
        try {
            MaterialMaster materialMaster;
            MaterialItem item;
            List<MaterialItem> items;
            String shopId = Optional.ofNullable(order.getB2bShop()).map(t->t.getShopId()).orElse(StrUtil.EMPTY);
            Set<String> materialNames = Sets.newHashSet();
            for (int i = 0, size = materialMasters.size(); i < size; i++) {
                materialMaster = materialMasters.get(i);
                materialMaster.setKefuType(order.getOrderCondition().getKefuType());
                if (user == null) {
                    user = materialMaster.getCreateBy();
                    date = materialMaster.getCreateDate();
                }
                materialMaster.setShopId(shopId==null?StrUtil.EMPTY:shopId);
                dao.insertMaterialMaster(materialMaster);
                //product
                for(MaterialProduct mProduct : materialMaster.getProductInfos()){
                    dao.insertMaterialProduct(mProduct);
                }
                //item
                items = materialMaster.getItems();
                Long itemId;
                for (int j = 0, jsize = items.size(); j < jsize; j++) {
                    item = items.get(j);
                    materialNames.add(item.getMaterial().getName());
                    dao.insertMaterialItem(item);
                }
                //attachment
                if (materialMaster.getAttachments() != null && materialMaster.getAttachments().size() > 0) {
                    List<MaterialAttachment> attachments = materialMaster.getAttachments();
                    MaterialAttachment attachment;
                    Long attcId;
                    for (int k = 0, ksize = attachments.size(); k < ksize; k++) {
                        attachment = attachments.get(k);
                        dao.insertMaterialAttach(attachment);
                        //关系表
                        dao.insertMaterialMasterAttachMap(materialMaster.getId(), attachment.getId(), materialMaster.getQuarter());
                    }
                }
            }
            //log
            OrderProcessLog processLog = new OrderProcessLog();
            processLog.setQuarter(quarter);//*
            processLog.setAction("申请配件");
            processLog.setOrderId(orderId);
            processLog.setActionComment(String.format("安维/客服人员申请配件：%s,请到配件单里进行查看", materialNames.toString()));
            processLog.setStatus(order.getOrderCondition().getStatus().getLabel());
            processLog.setStatusValue(Integer.parseInt(order.getOrderCondition().getStatus().getValue()));
            processLog.setStatusFlag(OrderProcessLog.OPL_SF_NOT_CHANGE_STATUS);
            processLog.setCloseFlag(0);
            processLog.setCreateBy(user);
            processLog.setCreateDate(date);
//            dao.insertProcessLog(processLog);
            processLog.setCustomerId(order.getOrderCondition().getCustomerId());
            processLog.setDataSourceId(order.getDataSourceId());
            orderService.saveOrderProcessLogNew(processLog);

            HashMap<String, Object> params = Maps.newHashMap();
            //order condition
            params.clear();
            params.put("quarter", quarter);//*
            params.put("orderId", orderId);
            params.put("partsFlag", 1);
            params.put("partsApplyDate", System.currentTimeMillis());// 2019-03-17
            params.put("updateBy", user);
            params.put("updateDate", date);
            orderService.updateOrderCondition(params);
            // 向b2b申请配件单,厂家发货才同步到厂家系统
            materialMaster = materialMasters.get(0);
            /*if(materialMaster.getApplyType().getIntValue() == MaterialMaster.APPLY_TYPE_CHANGJIA) {
                newB2BMateiralForm(order.getDataSource().getIntValue(), materialMaster);
            }*/
            //向b2b申请配件单,厂家发货的配件才调用
            if ( materialMaster.getApplyType().getIntValue() == MaterialMaster.APPLY_TYPE_CHANGJIA &&
                    order.getDataSource().getIntValue() == B2BDataSourceEnum.JOYOUNG.id){ //九阳
                newB2BMateiralForm(order.getDataSource().getIntValue(),materialMaster);
            }else if(order.getDataSource().getIntValue() == B2BDataSourceEnum.XYINGYAN.id || order.getDataSource().getIntValue() == B2BDataSourceEnum.LB.id){ //新迎燕
                materialMaster.setB2bOrderId(order.getB2bOrderId());
                newB2BMateiralForm(order.getDataSource().getIntValue(),materialMaster);
            }
            //调用公共缓存
            OrderCacheParam.Builder builder = new OrderCacheParam.Builder();
            builder.setOpType(OrderCacheOpType.UPDATE)
                    .setOrderId(orderId)
                    .setDeleteField(OrderCacheField.CONDITION);
            OrderCacheUtils.update(builder.build());
        } catch (OrderException oe) {
            throw oe;
        } catch (Exception e) {
            log.error("[orderMaterialService.addAppMaterialApplies] orderId:{}", orderId, e);
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            if (locked && lockkey != null) {
                redisUtils.remove(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey);
            }
        }
    }

    /**
     * 向B2B微服务申请创建配件单
     * @param dataSource        数据来源，未定义在application.yml的ms.b2bcenter.material下的不请求
     * @param mateiralMaster    工单系统配件单
     */
    @Transactional
    public void newB2BMateiralForm(Integer dataSource,MaterialMaster mateiralMaster){
        // b2b配件
        if (msProperties.getB2bcenter().getMaterial().containsKey(dataSource)){
            B2BDataSourceEnum dataSourceEnum = B2BDataSourceEnum.valueOf(dataSource);
            if(dataSourceEnum != null) {
                B2BMaterialExecutor b2BMaterialExecutor = b2bMaterialExecutorFactory.getExecutor(dataSourceEnum);
                if(b2BMaterialExecutor != null){
                    StringBuilder address = new StringBuilder(250);
                    address.append(mateiralMaster.getArea().getName());
                    if(mateiralMaster.getSubArea()!= null && mateiralMaster.getSubArea().getId() != null && mateiralMaster.getSubArea().getId()>3){
                        address.append(" ").append(mateiralMaster.getSubArea().getName());
                    }
                    address.append(" ").append(mateiralMaster.getUserAddress());
                    mateiralMaster.setUserAddress(StringUtils.left(address.toString().trim(),250));
                    address = null;
                    MSResponse msResponse = b2BMaterialExecutor.newMaterialForm(mateiralMaster);
                    if(!MSResponse.isSuccessCode(msResponse)){
                        throw new RuntimeException(msResponse.getMsg());
                    }
                }
            }
        }
    }

    /**
     * 删除配件申请
     *
     * @param orderId
     * @param quarter
     * @param formId
     * @param user
     */
    @Transactional(readOnly = false)
    public void deleteMaterialAppy(Long orderId, String quarter, Long formId, User user) {
        HashMap<String, Object> params = Maps.newHashMap();
        params.put("id", formId);
        params.put("quarter", quarter);//*
        params.put("delFlag", 1);//*
        params.put("updateBy", user);
        params.put("updateDate", new Date());
        dao.updateMaterialMaster(params);
        int qty = dao.getMaterialMasterCountByOrderId(orderId, quarter, null);
        if (qty == 0) {
            params.clear();
            params.put("orderId", orderId);
            params.put("quarter", quarter);//*
            params.put("partsFlag", 0);
            params.put("partsApplyDate", 0); //2019-03-17
            orderService.updateOrderCondition(params);
        }
    }

    /**
     * 保存配件申请
     * 来源：
     * 1.订单明细申请（无order detail id,=0）
     * 2.上门明细申请（有order detail id）

    @Transactional(readOnly = false)
    public void addMaterialApply(MaterialMaster materialMaster) {
        if (materialMaster == null || materialMaster.getOrderId() == null || materialMaster.getOrderDetailId() == null) {
            return;
        }
        List<MaterialItem> items = materialMaster.getItems();
        items = items.stream().filter(t -> t.isChooseFlag() == true).collect(Collectors.toList());
        if (items.size() == 0) {
            throw new OrderException("请选择至少一个配件");
        }
        String lockkey = String.format(RedisConstant.SD_ORDER_LOCK, materialMaster.getOrderId());
        //获得锁
        Boolean locked = redisUtils.setNX(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey, 1, ORDER_LOCK_EXPIRED);//60秒
        if (!locked) {
            throw new OrderException("此订单正在处理中，请稍候重试，或刷新页面。");
        }
        User user = materialMaster.getCreateBy();
        try {
            String quarter = orderService.getOrderQuarterFromCache(materialMaster.getOrderId());
            Order order = orderService.getOrderById(materialMaster.getOrderId(), quarter, OrderUtils.OrderDataLevel.CONDITION, true);
            if (order == null || order.getOrderCondition() == null) {
                throw new OrderException("读取订单错误，请重试。");
            }
            quarter = order.getQuarter();//有可能传入参数为空
            //如是订单明细处的申请
            //自动绑定已有的上门明细（by产品筛选）
            if (materialMaster.getOrderDetailId() == 0) {
                List<OrderDetail> details = order.getDetailList();
                if (details != null || details.size() > 0) {
                    OrderDetail detail = details.stream()
                            .filter(t -> t.getDelFlag() == 0
                                    && Objects.equals(t.getProduct().getId(), materialMaster.getProductId())
                            )
                            .sorted(Comparator.comparing(OrderDetail::getCreateDate).reversed())
                            .findFirst().orElse(null);
                    if (detail != null && detail.getDelFlag() == 0) {
                        materialMaster.setOrderDetailId(detail.getId());
                    }
                }
            }


            Date date = materialMaster.getCreateDate();
            //master
            Long formId = SeqUtils.NextIDValue(SeqUtils.TableName.SdMaterialMaster);
            materialMaster.setId(formId);
            materialMaster.setQuarter(quarter);//分片
            dao.insertMaterialMaster(materialMaster);
            //item
            MaterialItem item;
            Long itemId;
            StringBuffer materialNames = new StringBuffer();
            for (int i = 0, size = items.size(); i < size; i++) {
                item = items.get(i);
                materialNames.append(",").append(item.getMaterial().getName());
                itemId = SeqUtils.NextIDValue(SeqUtils.TableName.SdMaterialItem);
                item.setId(itemId);
                item.setMaterialMasterId(formId);
                dao.insertMaterialItem(item);
            }
            //attachment
            if (materialMaster.getAttachments() != null) {
                List<MaterialAttachment> attachments = materialMaster.getAttachments();
                MaterialAttachment attachment;
                Long attcId;
                for (int i = 0, size = attachments.size(); i < size; i++) {
                    attachment = attachments.get(i);
                    attcId = SeqUtils.NextIDValue(SeqUtils.TableName.SdMaterialAttachment);
                    attachment.setId(attcId);
                    attachment.setOrderId(materialMaster.getOrderId());
                    attachment.setCreateBy(user);
                    attachment.setCreateDate(date);
                    attachment.setQuarter(order.getQuarter());//分片*
                    dao.insertMaterialAttach(attachment);
                    //关系表
                    dao.insertMaterialMasterAttachMap(formId, attcId, order.getQuarter());
                }
            }
            //log
            OrderProcessLog processLog = new OrderProcessLog();
            processLog.setQuarter(order.getQuarter());//*
            processLog.setAction("申请配件");
            processLog.setOrderId(order.getId());
            processLog.setActionComment(String.format("安维/客服人员申请配件：%s,请到配件单里进行查看", materialNames.toString().substring(1)));
            processLog.setStatus(order.getOrderCondition().getStatus().getLabel());
            processLog.setStatusValue(Integer.parseInt(order.getOrderCondition().getStatus().getValue()));
            processLog.setStatusFlag(OrderProcessLog.OPL_SF_NOT_CHANGE_STATUS);
            processLog.setCloseFlag(0);
            processLog.setCreateBy(user);
            processLog.setCreateDate(date);
//            dao.insertProcessLog(processLog);
            processLog.setDataSourceId(order.getDataSourceId());
            orderService.saveOrderProcessLogNew(processLog);

            HashMap<String, Object> params = Maps.newHashMap();
            //order condition
            params.clear();
            params.put("quarter", order.getQuarter());//*
            params.put("orderId", materialMaster.getOrderId());
            params.put("partsFlag", 1);
            params.put("partsApplyDate", System.currentTimeMillis());// 2019-03-17
            params.put("updateBy", user);
            params.put("updateDate", date);
            orderService.updateOrderCondition(params);
            //cache
            String key = String.format(RedisConstant.SD_MATERIAL_ATTACHE, materialMaster.getOrderDetailId());
            String orderKey = String.format(RedisConstant.SD_ORDER, materialMaster.getOrderId());
            try {
                redisUtils.remove(RedisConstant.RedisDBType.REDIS_TEMP_DB, key);
            } catch (Exception e) {
                log.error("[orderMaterialService.addMaterialApply] redis:{}", orderKey, e);
                try {
                    redisUtils.remove(RedisConstant.RedisDBType.REDIS_TEMP_DB, key);
                } catch (Exception e1) {
                }
            }
            //调用公共缓存
            OrderCacheParam.Builder builder = new OrderCacheParam.Builder();
            builder.setOpType(OrderCacheOpType.UPDATE)
                    .setOrderId(materialMaster.getOrderId())
                    .setPartsFlag(1)
                    .incrVersion(1L)
                    .setExpireSeconds(0L);
            OrderCacheUtils.update(builder.build());

        } catch (OrderException oe) {
            throw oe;
        } catch (Exception e) {
            log.error("[orderMaterialService.addMaterialApply] orderId:{}", materialMaster.getOrderId(), e);
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            if (locked && lockkey != null) {
                redisUtils.remove(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey);
            }
        }
    }
     */


    /**
     * 保存配件申请 (废弃 2019-08-06)
     * item 用materials 参数传递，套组自动拆分多个申请单
     * 来源：
     * 1.订单明细申请（无order detail id,=0）
     * 2.上门明细申请（有order detail id）

    @Transactional(readOnly = false)
    public void ajaxAddMaterialApply(Order order,List<MaterialMaster> forms) {
        if (ObjectUtils.isEmpty(forms)) {
            return;
        }

        //获得锁
        String lockkey = String.format(RedisConstant.SD_ORDER_LOCK, order.getId());
        Boolean locked = redisUtils.setNX(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey, 1, ORDER_LOCK_EXPIRED);//60秒
        if (!locked) {
            throw new OrderException("此订单正在处理中，请稍候重试，或刷新页面。");
        }
        MaterialMaster materialMaster = forms.get(0);
        long grpId = materialMaster.getProductId();
        User user = materialMaster.getCreateBy();
        OrderDetail detail;
        List<OrderDetail> details = null;
        try {
            //Order order = orderService.getOrderById(order.getId(), order.getQuarter(), OrderUtils.OrderDataLevel.DETAIL, true);
            //if (order == null || order.getOrderCondition() == null) {
            //    throw new OrderException("读取订单错误，请重试。");
            //}
            Date date = materialMaster.getCreateDate();
            //如是订单明细处的申请
            //自动绑定已有的上门明细（by产品筛选）
            Long orderDetailId = materialMaster.getOrderDetailId();
            if(orderDetailId == null){
                orderDetailId = 0l;
            }
            if (orderDetailId.longValue() == 0) {
                details = order.getDetailList();
                if (details != null && details.size() > 0) {
                    //1.套组/原始产品id
                    detail = details.stream()
                            .filter(t -> t.getDelFlag() == 0
                                    && t.getProduct().getId().longValue() == grpId)
                            .sorted(Comparator.comparing(OrderDetail::getCreateDate).reversed())
                            .findFirst().orElse(null);
                    if (detail != null && detail.getDelFlag() == 0) {
                        orderDetailId = detail.getId();
                    }
                }
            }

            //master
            StringBuffer materialNames = new StringBuffer();
            List<MaterialItem> items;
            MaterialItem item;
            for(int j=0,fsize=forms.size();j<fsize;j++){
                materialMaster = forms.get(j);
                if(ObjectUtils.isEmpty(materialMaster.getItems())){
                    continue;
                }
                if(orderDetailId.longValue() ==0 && details != null && details.size() > 0){
                    //2.单品
                    long pid = materialMaster.getProduct().getId();
                    if(pid != grpId) {
                        detail = details.stream()
                                .filter(t -> t.getDelFlag() == 0
                                        && t.getProduct().getId().longValue() == pid)
                                .sorted(Comparator.comparing(OrderDetail::getCreateDate).reversed())
                                .findFirst().orElse(null);
                        if (detail != null && detail.getDelFlag() == 0) {
                            materialMaster.setOrderDetailId(detail.getId());
                        }else{
                            materialMaster.setOrderDetailId(0l);
                        }
                    }
                }else {
                    materialMaster.setOrderDetailId(orderDetailId);
                }
                materialNames.append(",").append(materialMaster.getItems().stream().map(t->t.getMaterial().getName()).collect(Collectors.joining(",")));
                //save header
                dao.insertMaterialMaster(materialMaster);
                //save items
                items = materialMaster.getItems();
                for(int i=0,size=items.size();i<size;i++){
                    item = items.get(i);
                    dao.insertMaterialItem(item);
                }
                //save attachment
                if (!ObjectUtils.isEmpty(materialMaster.getAttachments())) {
                    List<MaterialAttachment> attachments = materialMaster.getAttachments();
                    MaterialAttachment attachment;
                    Long attcId;
                    for (int i = 0, size = attachments.size(); i < size; i++) {
                        attachment = attachments.get(i);
                        //attcId = SeqUtils.NextIDValue(SeqUtils.TableName.SdMaterialAttachment);
                        attcId = sequenceIdUtils.nextId();
                        attachment.setId(attcId);
                        attachment.setOrderId(order.getId());
                        attachment.setCreateBy(user);
                        attachment.setCreateDate(date);
                        attachment.setQuarter(order.getQuarter());//分片*
                        dao.insertMaterialAttach(attachment);
                        //关系表
                        dao.insertMaterialMasterAttachMap(materialMaster.getId(), attcId, order.getQuarter());
                    }
                }
            }

            //log
            OrderProcessLog processLog = new OrderProcessLog();
            processLog.setQuarter(order.getQuarter());//*
            processLog.setAction("申请配件");
            processLog.setOrderId(order.getId());
            processLog.setActionComment(String.format("安维/客服人员申请配件：%s,请到配件单里进行查看", materialNames.toString().substring(1)));
            processLog.setStatus(order.getOrderCondition().getStatus().getLabel());
            processLog.setStatusValue(Integer.parseInt(order.getOrderCondition().getStatus().getValue()));
            processLog.setStatusFlag(OrderProcessLog.OPL_SF_NOT_CHANGE_STATUS);
            processLog.setCloseFlag(0);
            processLog.setCreateBy(user);
            processLog.setCreateDate(date);
            processLog.setDataSourceId(order.getDataSourceId());
            orderService.saveOrderProcessLogNew(processLog);

            HashMap<String, Object> params = Maps.newHashMap();
            //order condition
            params.clear();
            params.put("quarter", order.getQuarter());//*
            params.put("orderId", materialMaster.getOrderId());
            params.put("partsFlag", 1);
            params.put("partsApplyDate", System.currentTimeMillis());// 2019-03-17
            params.put("updateBy", user);
            params.put("updateDate", date);
            orderService.updateOrderCondition(params);
            //cache
            String key = String.format(RedisConstant.SD_MATERIAL_ATTACHE, materialMaster.getOrderDetailId());
            String orderKey = String.format(RedisConstant.SD_ORDER, materialMaster.getOrderId());
            try {
                redisUtils.remove(RedisConstant.RedisDBType.REDIS_TEMP_DB, key);
            } catch (Exception e) {
                log.error("[orderMaterialService.addMaterialApply] redis:{}", orderKey, e);
                try {
                    redisUtils.remove(RedisConstant.RedisDBType.REDIS_TEMP_DB, key);
                } catch (Exception e1) {
                }
            }
            //调用公共缓存
            OrderCacheParam.Builder builder = new OrderCacheParam.Builder();
            builder.setOpType(OrderCacheOpType.UPDATE)
                    .setOrderId(materialMaster.getOrderId())
                    .setPartsFlag(1)
                    .incrVersion(1L)
                    .setExpireSeconds(0L);
            OrderCacheUtils.update(builder.build());

        } catch (OrderException oe) {
            throw oe;
        } catch (Exception e) {
            log.error("[orderMaterialService.addMaterialApply] orderId:{}", materialMaster.getOrderId(), e);
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            if (locked && lockkey != null) {
                redisUtils.remove(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey);
            }
        }
    }
    */

    /**
     * 保存配件申请
     * item 用materials 参数传递
     * 来源：
     * 1.订单明细申请（无order detail id,=0）
     * 2.上门明细申请（有order detail id）
     */
    @Transactional(readOnly = false)
    public void ajaxAddMaterialApply(Order order,MaterialMaster materialMaster) {
        if (materialMaster == null || materialMaster.getId() == null || materialMaster.getId() <= 0L) {
            return;
        }

        //获得锁
        String lockkey = String.format(RedisConstant.SD_ORDER_LOCK, order.getId());
        Boolean locked = redisUtils.setNX(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey, 1, ORDER_LOCK_EXPIRED);//60秒
        if (!locked) {
            throw new OrderException("此订单正在处理中，请稍候重试，或刷新页面。");
        }
        long grpId = materialMaster.getProductId();
        User user = materialMaster.getCreateBy();
        OrderDetail detail;
        List<OrderDetail> details = null;
        try {
            Date date = materialMaster.getCreateDate();
            //如是订单明细处的申请
            //自动绑定已有的上门明细
            Long orderDetailId = materialMaster.getOrderDetailId();
            if(orderDetailId == null){
                orderDetailId = 0L;
            }
            //有上门服务，绑定第一上门服务即可
            if (orderDetailId.longValue() == 0) {
                details = order.getDetailList();
                if (details != null && details.size() > 0) {
                    detail = details.get(0);
                    /* 1.套组/原始产品id
                    detail = details.stream()
                            .filter(t -> t.getDelFlag() == 0
                                    && t.getProduct().getId().longValue() == grpId)
                            .sorted(Comparator.comparing(OrderDetail::getCreateDate).reversed())
                            .findFirst().orElse(null);
                    */
                    if (detail != null && detail.getDelFlag() == 0) {
                        orderDetailId = detail.getId();
                    }
                }
            }
            materialMaster.setOrderDetailId(orderDetailId);

            //master
            StringBuilder materialNames = new StringBuilder(250);
            List<MaterialItem> items;
            MaterialItem item;
            materialNames.append(materialMaster.getItems().stream().map(t->t.getMaterial().getName()).collect(Collectors.joining(",")));
            //save header
            setArea(order.getOrderCondition(),materialMaster);
            materialMaster.setCanRush(order.getOrderCondition().getCanRush());
            dao.insertMaterialMaster(materialMaster);
            //product
            for(MaterialProduct mProduct : materialMaster.getProductInfos()){
                dao.insertMaterialProduct(mProduct);
            }
            //save items
            items = materialMaster.getItems();
            for(int i=0,size=items.size();i<size;i++){
                item = items.get(i);
                dao.insertMaterialItem(item);
            }
            //save attachment
            if (!ObjectUtils.isEmpty(materialMaster.getAttachments())) {
                List<MaterialAttachment> attachments = materialMaster.getAttachments();
                MaterialAttachment attachment;
                Long attcId;
                for (int i = 0, size = attachments.size(); i < size; i++) {
                    attachment = attachments.get(i);
                    //attcId = SeqUtils.NextIDValue(SeqUtils.TableName.SdMaterialAttachment);
                    attcId = sequenceIdService.nextId();
                    attachment.setId(attcId);
                    attachment.setOrderId(order.getId());
                    attachment.setCreateBy(user);
                    attachment.setCreateDate(date);
                    attachment.setQuarter(order.getQuarter());//分片*
                    dao.insertMaterialAttach(attachment);
                    //关系表
                    dao.insertMaterialMasterAttachMap(materialMaster.getId(), attcId, order.getQuarter());
                }
            }
            //log
            OrderProcessLog processLog = new OrderProcessLog();
            processLog.setQuarter(order.getQuarter());//*
            processLog.setAction("申请配件");
            processLog.setOrderId(order.getId());
            processLog.setActionComment(String.format("安维/客服人员申请配件：%s%s,请到配件单里进行查看", org.apache.commons.lang3.StringUtils.left(materialNames.toString(),220),(materialNames.length()>220?"...":"")));
            processLog.setStatus(order.getOrderCondition().getStatus().getLabel());
            processLog.setStatusValue(Integer.parseInt(order.getOrderCondition().getStatus().getValue()));
            processLog.setStatusFlag(OrderProcessLog.OPL_SF_NOT_CHANGE_STATUS);
            processLog.setCloseFlag(0);
            processLog.setCreateBy(user);
            processLog.setCreateDate(date);
            processLog.setCustomerId(order.getOrderCondition().getCustomerId());
            processLog.setDataSourceId(order.getDataSourceId());
            orderService.saveOrderProcessLogNew(processLog);

            HashMap<String, Object> params = Maps.newHashMap();
            //order condition
            params.clear();
            params.put("quarter", order.getQuarter());//*
            params.put("orderId", materialMaster.getOrderId());
            params.put("partsFlag", 1);
            params.put("partsApplyDate", System.currentTimeMillis());// 2019-03-17
            params.put("updateBy", user);
            params.put("updateDate", date);
            orderService.updateOrderCondition(params);

            //向b2b申请配件单,厂家发货的配件才调用
            if ( materialMaster.getApplyType().getIntValue() == MaterialMaster.APPLY_TYPE_CHANGJIA
                    && msProperties.getB2bcenter().getMaterial().containsKey(order.getDataSource().getIntValue()) &&
                    order.getDataSource().getIntValue() == B2BDataSourceEnum.JOYOUNG.id){ //九阳
                newB2BMateiralForm(order.getDataSource().getIntValue(),materialMaster);
            }else if( msProperties.getB2bcenter().getMaterial().containsKey(order.getDataSource().getIntValue()) &&
                    (order.getDataSource().getIntValue() == B2BDataSourceEnum.XYINGYAN.id || order.getDataSource().getIntValue() == B2BDataSourceEnum.LB.id)){ //新迎燕
                materialMaster.setB2bOrderId(order.getB2bOrderId());
                newB2BMateiralForm(order.getDataSource().getIntValue(),materialMaster);
            }

            //cache
            String key = String.format(RedisConstant.SD_MATERIAL_ATTACHE, materialMaster.getOrderDetailId());
            String orderKey = String.format(RedisConstant.SD_ORDER, materialMaster.getOrderId());
            try {
                redisUtils.remove(RedisConstant.RedisDBType.REDIS_TEMP_DB, key);
            } catch (Exception e) {
                log.error("redis:{}", orderKey, e);
                try {
                    redisUtils.remove(RedisConstant.RedisDBType.REDIS_TEMP_DB, key);
                } catch (Exception e1) {
                }
            }
            //调用公共缓存
            OrderCacheParam.Builder builder = new OrderCacheParam.Builder();
            builder.setOpType(OrderCacheOpType.UPDATE)
                    .setOrderId(materialMaster.getOrderId())
                    .setPartsFlag(1)
                    .incrVersion(1L)
                    .setExpireSeconds(0L);
            OrderCacheUtils.update(builder.build());

        } catch (OrderException oe) {
            throw oe;
        } catch (Exception e) {
            log.error("[orderMaterialService.addMaterialApply] orderId:{}", materialMaster.getOrderId(), e);
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            if (locked && lockkey != null) {
                redisUtils.remove(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey);
            }
        }
    }

    /**
     * 保存配件附件
     * 前提：必须要有配件单id及分片
     *
     * @param masterId
     * @param attachment
     */
    @Transactional(readOnly = false)
    public void insertMaterialMasterAttach(Long masterId, MaterialAttachment attachment) {
        if (masterId == null) {
            throw new OrderException("配件单ID无效");
        }
        if (attachment.getId() == null) {
            //Long attcId = SeqUtils.NextIDValue(SeqUtils.TableName.SdMaterialAttachment);
            Long attcId = sequenceIdService.nextId();
            attachment.setId(attcId);
        }
        dao.insertMaterialAttach(attachment);
        //关系表
        dao.insertMaterialMasterAttachMap(masterId, attachment.getId(), attachment.getQuarter());
    }

    /**
     * 审核通过配件
     *
     * @param materialMaster   配件申请单
     * @param isMaterialReturn 是否需要反件
     * @Param isMaterialRecycle 是否回收
     * @param user 帐号
     */
    @Transactional(readOnly = false)
    public void approveMaterialApply( MaterialMaster materialMaster,Order order,Integer isMaterialReturn,String returnNo,String[] itemIds,User user,MDCustomerAddress customerAddress,Integer isMaterialRecycle,String[] recycleItemIds,String materialApprove) {
        // 审核通过增加订单的配件费
        if (materialMaster == null) {
            throw new OrderException("读取配件申请单失败");
        }
        if (order == null || order.getOrderCondition() == null) {
            throw new OrderException("读取订单错误，请重试。");
        }
        if (!materialMaster.getStatus().getValue().equalsIgnoreCase("1")) {
            throw new OrderException("该配件申请已处理");
        }
        if(isMaterialReturn==1 && isMaterialRecycle==1){
            HashSet<String> set = new HashSet<>(Arrays.asList(itemIds));
            set.retainAll(Arrays.asList(recycleItemIds));
            if(set.size() > 0){
                throw new OrderException("同个配件不能同时是返件和回收");
            }
        }
        //Customer customer = order.getOrderCondition().getCustomer();
        if(isMaterialReturn == 1){
            if(ObjectUtils.isEmpty(itemIds)){
                throw new OrderException("请选择要返件的配件");
            }
            if(StringUtils.isBlank(returnNo)) {
                throw new OrderException("该配件申请需返件，但无返件单号");
            }
            /*if(materialMaster.getDataSource().equals(B2BDataSourceEnum.XYINGYAN.id)){
                 if(StringUtils.isBlank(customerAddress.getAddress())){
                     throw new OrderException("获取新迎燕返件地址失败，请重试");
                 }
            }else{
                if(customerAddress==null || customerAddress.getAreaId()==null || StringUtils.isBlank(customerAddress.getAddress())){
                    customerAddress = customerNewService.getByCustomerIdAndTypeFromCache(customer.getId(), MDCustomerEnum.CustomerAddressType.RETURNADDR.getValue());
                    if(customerAddress==null || customerAddress.getId()==null || customerAddress.getId()<=0){
                        throw new OrderException("读取返件地址失败，请重试");
                    }
                }
            }*/
        }
        if(isMaterialRecycle==1){
            if(ObjectUtils.isEmpty(recycleItemIds)){
                throw new OrderException("请选择要回收的配件");
            }
        }
        String lockkey = String.format(RedisConstant.SD_ORDER_LOCK, materialMaster.getOrderId());
        //获得锁
        Boolean locked = redisUtils.setNX(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey, 1, ORDER_LOCK_EXPIRED);//60秒
        if (!locked) {
            throw new OrderException("此订单正在处理中，请稍候重试，或刷新页面。");
        }
        try {
            //是否有配件需要关联的上门明细
            //未关联上门明细的：在添加上门明细时自动关联，并更新金额
            Boolean isRelateDetail = false;//是否已关联上门服务
            if (materialMaster.getOrderDetailId() != null && materialMaster.getOrderDetailId().longValue() > 0) {
                isRelateDetail = true;
            }
            OrderDetail detail = null;
            List<OrderDetail> details = order.getDetailList();
            if (isRelateDetail == true) {
                // 已关联上门服务
                detail = details.stream()
                        .filter(t -> Objects.equals(t.getId(), materialMaster.getOrderDetailId())
                                && t.getDelFlag().intValue() == 0)
                        .findFirst().orElse(null);
                if (detail == null) {
                    throw new OrderException("订单上门明细中无符合条件的记录。请确认上门明细是否已经删除了。");
                }
            } else {
                Product p;
                long pid = materialMaster.getProduct().getId();
                //先按申请的产品id处理
                detail = details.stream()
                        .filter(t -> t.getProduct().getId().equals(pid)
                                && t.getDelFlag().intValue() == 0
                        ).findFirst().orElse(null);

                if (detail == null) {//未找到上门服务
                    p = productService.getProductByIdFromCache(pid);
                    if (p.getSetFlag() == 1) {//申请是套组
                        String pids = String.format(",%s,", p.getProductIds());
                        detail = details.stream().filter(
                                t -> t.getDelFlag().intValue() == 0
                                        && pids.contains(String.format(",%s,", t.getProductId()))
                        )
                                .sorted(Comparator.comparingLong(OrderDetail::getId).reversed())
                                .findFirst().orElse(null);
                    } else {
                        //配件申请是单品,上门是套组
                        // 拆分上门服务中的套组
                        List<OrderDetail> fltList = details.stream()
                                .filter(t -> t.getDelFlag().intValue() == 0)
                                .sorted(Comparator.comparingLong(OrderDetail::getId).reversed())
                                .collect(Collectors.toList());
                        OrderDetail d;
                        for (int i = 0, size = fltList.size(); i < size; i++) {
                            d = fltList.get(i);
                            p = productService.getProductByIdFromCache(d.getProductId());
                            if (p.getSetFlag() == 1
                                    && String.format(",%s,", p.getProductIds()).indexOf(",".concat(materialMaster.getProductId().toString()).concat(",")) != -1) {
                                detail = d;//按单品找到上门服务
                                break;
                            }
                        }
                    }
                }
            }

            //save to db
            Date date = new Date();
            //配件申请
            HashMap<String, Object> params = Maps.newHashMap();
            params.put("id", materialMaster.getId());
            params.put("quarter", order.getQuarter());//*
            params.put("returnFlag", isMaterialReturn);
            params.put("recycleFlag",isMaterialRecycle);
            if(StringUtils.isNotBlank(materialApprove)){
                params.put("materialApprove",materialApprove);
            }
            Dict status = new Dict("2", "待发货");
            //自购，向师傅购买，不需要发件 -> 已完成
            if (materialMaster.getApplyType().getValue().equals(MaterialMaster.APPLY_TYPE_ZIGOU.toString())) {
                status.setValue(MaterialMaster.STATUS_CLOSED.toString());
                status.setLabel("已完成");
                params.put("closeBy",user);
                params.put("closeDate",date);
                params.put("closeRemark","向师傅购买,审核时自动关闭");
            }
            params.put("status", status);
            //关联上门明细
            if (detail != null) {
                params.put("orderDetailId", detail.getId());
            }
            params.put("updateBy", user);
            params.put("updateDate", date);
            params.put("prevStatus",MaterialMaster.STATUS_NEW);
            double masterTotalPrice = 0.0;
            List<MaterialItem> recycleItemList = Lists.newArrayList();
            if(isMaterialRecycle==1){
                List<MaterialItem> items = materialMaster.getItems();
                Set<Long> recycleItemSet = Arrays.stream(recycleItemIds).map(t -> Long.valueOf(t)).collect(Collectors.toSet());
                Material material = null;
                MaterialItem recycleItem = null;
                for(MaterialItem item:items){
                    if(recycleItemSet.contains(item.getId())){
                        material = materialService.getFromCache(item.getMaterial().getId());
                        if(material!=null){
                            recycleItem = new MaterialItem();
                            recycleItem.setId(item.getId());
                            recycleItem.setRecycleFlag(1);//回收
                            recycleItem.setRecyclePrice(-material.getRecyclePrice());
                            recycleItem.setTotalRecyclePrice(-(material.getRecyclePrice()*item.getQty()));
                            recycleItem.setUpdateBy(user);
                            recycleItem.setUpdateDate(date);
                            masterTotalPrice = masterTotalPrice+recycleItem.getTotalRecyclePrice();
                            recycleItemList.add(recycleItem);
                        }
                    }
                }
                params.put("totalPrice",materialMaster.getTotalPrice()+masterTotalPrice);
                materialMaster.setTotalPrice(materialMaster.getTotalPrice()+masterTotalPrice);
            }
            int updateRow = dao.updateMaterialMaster(params);
            if (updateRow == 0){
                // 配件状态已变更，不是 1
                throw new OrderException("该配件单当前状态已变更，请刷新配件单并确认状态。");
            }
            for(MaterialItem item:recycleItemList){
                dao.updateItemRecycle(item);
            }
            //1.返件
            if (isMaterialReturn == 1) {
                //返件地址-从客户基本信息中读取
                Long rtnId = sequenceIdService.nextId();
                MaterialMasterMapper mapper = Mappers.getMapper(MaterialMasterMapper.class);
                MaterialReturn returnMaster = mapper.toReturnForm(materialMaster);
                returnMaster.setId(rtnId);
                returnMaster.setReturnNo(returnNo);
                returnMaster.setCreateBy(user);
                returnMaster.setCreateDate(date);
                returnMaster.setStatus(new Dict(MaterialMaster.STATUS_APPROVED, "待发货"));//返件单无需审核
                returnMaster.setCreateBy(user);
                returnMaster.setCreateDate(date);
                //返件 (返件信息改为从客户地址表读取 2020-7-27)
                //returnMaster.setReceivor(customer.getMaster());//负责人姓名
                //returnMaster.setReceivorPhone(customer.getPhone());//负责人电话
                //returnMaster.setReceivorAddress(customer.getReturnAddress());//返件地址
                returnMaster.setReceivor(Optional.ofNullable(customerAddress.getUserName()).orElse(""));
                returnMaster.setReceivorPhone(Optional.ofNullable(customerAddress.getContactInfo()).orElse(""));
                returnMaster.setReceivorAddress(Optional.ofNullable(customerAddress.getAddress()).orElse(""));
                returnMaster.setReceiverProvinceId(Optional.ofNullable(customerAddress.getProvinceId()).orElse(0L));
                returnMaster.setReceiverCityId(Optional.ofNullable(customerAddress.getCityId()).orElse(0L));
                returnMaster.setReceiverAreaId(Optional.ofNullable(customerAddress.getAreaId()).orElse(0L));
                returnMaster.setProvinceId(materialMaster.getProvinceId());
                returnMaster.setCityId(materialMaster.getCityId());
                returnMaster.setKefuType(materialMaster.getKefuType());
                returnMaster.setProductCategoryId(materialMaster.getProductCategoryId());

                //item
                MaterialItem item;
                MaterialReturnItem rtnItem;
                List<MaterialReturnItem> rtnItems = Lists.newArrayList();
                List<MaterialItem> orgUpdateItems = Lists.newArrayList();
                List<MaterialItem> items = materialMaster.getItems();
                Set<Long> selItemSet = Arrays.stream(itemIds).map(t -> Long.valueOf(t)).collect(Collectors.toSet());
                double totalPrice = 0.0;
                //因app提交可能有价格
                //boolean caclPrice = materialMaster.getApplyType().getValue().equalsIgnoreCase(MaterialMaster.APPLY_TYPE_ZIGOU.toString());
                for (int i = 0, size = items.size(); i < size; i++) {
                    item = items.get(i);
                    if (selItemSet.contains(item.getId())) {
                        rtnItem = mapper.toReturnItem(item);
                        rtnItem.setQuarter(returnMaster.getQuarter());
                        //rtnItem.setId(SeqUtils.NextIDValue(SeqUtils.TableName.SdMaterialItem));//mycat
                        rtnItem.setId(sequenceIdService.nextId());
                        rtnItem.setProduct(returnMaster.getProduct());
                        rtnItem.setFormId(rtnId);
                        rtnItem.setCreateBy(user);
                        rtnItem.setCreateDate(date);
                        rtnItems.add(rtnItem);
                        //if(caclPrice){
                        totalPrice = totalPrice + rtnItem.getTotalPrice();
                        //}
                        orgUpdateItems.add(item);
                    }
                }
                //申请单
                //所有先标记为不返件
                dao.updateItemNoReturn(materialMaster.getId(), order.getQuarter());
                //返件的配件单独标记为返件
                for (int k = 0, size = orgUpdateItems.size(); k < size; k++) {
                    item = orgUpdateItems.get(k);
                    dao.updateItemIsReturn(returnMaster.getQuarter(), item.getId());
                }
                //返件单
                returnMaster.setItems(rtnItems);
                returnMaster.setTotalPrice(totalPrice);
                returnService.insertMaterialReturn(returnMaster);
            }

            //不返件
            if(isMaterialReturn == 0){
                //配件都标记为不返件
                dao.updateItemNoReturn(materialMaster.getId(),order.getQuarter());
            }

            Dict orderStatus = order.getOrderCondition().getStatus();
            //厂商发货，或金额为0
            //log
            OrderProcessLog processLog = new OrderProcessLog();
            processLog.setQuarter(order.getQuarter());//*
            processLog.setAction("配件审核");
            processLog.setOrderId(order.getId());
            processLog.setActionComment(String.format("配件审核,操作人:%s", user.getName()));
            processLog.setStatus(orderStatus.getLabel());
            processLog.setStatusValue(order.getOrderCondition().getStatusValue());
            processLog.setStatusFlag(OrderProcessLog.OPL_SF_NOT_CHANGE_STATUS);
            processLog.setCloseFlag(0);
            processLog.setCreateBy(user);
            processLog.setCreateDate(date);
            processLog.setCustomerId(order.getOrderCondition().getCustomerId());
            processLog.setDataSourceId(order.getDataSourceId());
            orderService.saveOrderProcessLogNew(processLog);

            OrderFee fee = null;
            // 已关联上门明细
            // 2020-11-12 配件费不为0才更新及汇总
            if (detail != null && materialMaster.getTotalPrice() != 0) {
                //更新配件费及订单费用
                detail.setEngineerMaterialCharge(detail.getEngineerMaterialCharge() + materialMaster.getTotalPrice());
                detail.setMaterialCharge(detail.getMaterialCharge() + materialMaster.getTotalPrice());

                params.clear();
                params.put("id", detail.getId());//*
                params.put("quarter", order.getQuarter());//*
                params.put("engineerMaterialCharge", detail.getEngineerMaterialCharge());//安维
                params.put("materialCharge", detail.getMaterialCharge());//厂家
                params.put("updateBy", user);
                params.put("updateDate", date);
                orderService.updateDetail(params);

                fee = order.getOrderFee();
                //应收
                fee.setMaterialCharge(fee.getMaterialCharge() + materialMaster.getTotalPrice());
                fee.setOrderCharge(fee.getOrderCharge() + materialMaster.getTotalPrice());
                //应付
                fee.setEngineerMaterialCharge(fee.getEngineerMaterialCharge() + materialMaster.getTotalPrice());
                fee.setEngineerTotalCharge(fee.getEngineerTotalCharge() + materialMaster.getTotalPrice());

                params.clear();
                params.put("quarter", order.getQuarter());//*
                params.put("orderId", order.getId());
                //应收
                params.put("materialCharge", fee.getMaterialCharge());
                params.put("orderCharge", fee.getOrderCharge());
                //应付
                params.put("engineerMaterialCharge", fee.getEngineerMaterialCharge());
                params.put("engineerTotalCharge", fee.getEngineerTotalCharge());
                orderService.updateFee(params);
            }
            if (isMaterialReturn == 1) {
                //需要返件
                //condition
                params.clear();
                params.put("quarter", order.getQuarter());
                params.put("orderId", order.getId());
                params.put("returnPartsFlag", 1);
                orderService.updateOrderCondition(params);
            }
            //调用公共缓存
            OrderCacheUtils.delete(order.getId());

        } catch (OrderException oe) {
            throw oe;
        } catch (DuplicateKeyException dpe){
            log.error("[orderMaterialService.addMaterialApply] orderId:{}", materialMaster.getOrderId(), dpe);
            throw new RuntimeException("生成返件单重复，该配件单已生成返件单。", dpe);
        } catch (Exception e) {
            log.error("[orderMaterialService.approveMaterialApply] masterId:{}", materialMaster.getId(), e);
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            if (locked && lockkey != null) {
                redisUtils.remove(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey);
            }
        }
    }

    /**
     * B2B接口：驳回配件申请
     * 在调用该方法前需做：申请单检查及状态检查
     */
    @Transactional(readOnly = false)
    public void b2bRejectMaterialApply(Long masterId, Long orderId, String quarter, User user,String remark,Order order) {
        if (order == null || order.getOrderCondition() == null) {
            throw new OrderException("读取订单信息失败");
        }
        String lockkey = String.format(RedisConstant.SD_ORDER_LOCK, orderId);
        //获得锁
        Boolean locked = redisUtils.setNX(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey, 1, ORDER_LOCK_EXPIRED);//60秒
        if (!locked) {
            throw new OrderException("此订单正在处理中，请稍候重试，或刷新页面。");
        }
        try {
            //Order order = orderService.getOrderById(orderId, quarter, OrderUtils.OrderDataLevel.CONDITION, true);

            Date now = new Date();
            HashMap<String, Object> params = Maps.newHashMap();
            params.put("quarter", order.getQuarter());
            params.put("id", masterId);
            params.put("status", new Dict("5", "已驳回"));
            params.put("returnFlag", 0); //不返件
            params.put("closeRemark",StringUtils.left(remark,148));
            params.put("updateBy", user);
            params.put("updateDate", now);
            dao.updateMaterialMaster(params);
            //item标记不返件
            dao.updateItemNoReturn(masterId,order.getQuarter());
            //log
            OrderProcessLog processLog = new OrderProcessLog();
            processLog.setQuarter(order.getQuarter());//*
            processLog.setAction("配件驳回");
            processLog.setOrderId(order.getId());
            processLog.setActionComment(String.format("配件驳回,操作人:%s %s", user.getName(),remark));
            processLog.setStatus(order.getOrderCondition().getStatus().getLabel());
            processLog.setStatusValue(order.getOrderCondition().getStatusValue());
            processLog.setStatusFlag(OrderProcessLog.OPL_SF_NOT_CHANGE_STATUS);
            processLog.setCloseFlag(0);
            processLog.setCreateBy(user);
            processLog.setCreateDate(now);
            processLog.setCustomerId(order.getOrderCondition().getCustomerId());
            processLog.setDataSourceId(order.getDataSourceId());
            orderService.saveOrderProcessLogNew(processLog);
            //驳回不是删除，订单仍算有配件，所以count始终大于0
            //Integer count = dao.getMaterialMasterCountByOrderId(orderId, quarter, null);
            params.clear();
            params.put("quarter", order.getQuarter());
            params.put("orderId", orderId);
            params.put("pendingTypeDate", now);
            //调用公共缓存
            OrderCacheParam.Builder builder = new OrderCacheParam.Builder();
            builder.setOpType(OrderCacheOpType.UPDATE)
                    .setOrderId(orderId)
                    .setPendingTypeDate(now)
                    .setExpireSeconds(0L);
            orderService.updateOrderCondition(params);
            OrderCacheUtils.update(builder.build());
        } catch (OrderException oe) {
            throw oe;
        } catch (Exception e) {
            log.error("[orderMaterialService.rejectMaterialApply] masterId:{}", masterId, e);
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            if (locked && lockkey != null) {
                redisUtils.remove(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey);
            }
        }
    }

    /**
     * 驳回配件申请
     * 在调用该方法前需做：申请单检查及状态检查
     */
    @Transactional(readOnly = false)
    public void rejectMaterialApply(MaterialMaster material,Order order,String rejectReason) {
        if (order == null || order.getOrderCondition() == null) {
            throw new OrderException("读取订单信息失败");
        }
        String lockkey = String.format(RedisConstant.SD_ORDER_LOCK, order.getId());
        //获得锁
        Boolean locked = redisUtils.setNX(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey, 1, ORDER_LOCK_EXPIRED);//60秒
        if (!locked) {
            throw new OrderException("此订单正在处理中，请稍候重试，或刷新页面。");
        }
        long masterId = material.getId();
        try {
            Date now = new Date();
            //字段长度限制30个字
            material.setCloseType(StringUtils.left(material.getCloseType(),30));
            HashMap<String, Object> params = Maps.newHashMap();
            params.put("id", material.getId());
            params.put("quarter", material.getQuarter());
            params.put("status", new Dict("5", "已驳回"));
            params.put("closeType",material.getCloseType());
            params.put("closeRemark",material.getCloseRemark());
            params.put("closeDate", now); //不返件
            params.put("closeBy", material.getUpdateBy());
            dao.rejectMaterial(params);
            //item标记不返件
            dao.updateItemNoReturn(material.getId(),material.getQuarter());
            //log
            OrderProcessLog processLog = new OrderProcessLog();
            processLog.setQuarter(material.getQuarter());//*
            processLog.setAction("配件驳回");
            processLog.setOrderId(order.getId());
            processLog.setActionComment(String.format("配件驳回,操作人:%s %s %s", material.getUpdateBy().getName(),rejectReason,material.getCloseRemark()));
            processLog.setStatus(order.getOrderCondition().getStatus().getLabel());
            processLog.setStatusValue(order.getOrderCondition().getStatusValue());
            processLog.setStatusFlag(OrderProcessLog.OPL_SF_NOT_CHANGE_STATUS);
            processLog.setCloseFlag(0);
            processLog.setCreateBy(material.getUpdateBy());
            processLog.setCreateDate(now);
            processLog.setCustomerId(order.getOrderCondition().getCustomerId());
            processLog.setDataSourceId(order.getDataSourceId());
            orderService.saveOrderProcessLogNew(processLog);
            //驳回不是删除，订单仍算有配件，所以count始终大于0
            //Integer count = dao.getMaterialMasterCountByOrderId(orderId, quarter, null);
            params.clear();
            params.put("quarter", order.getQuarter());
            params.put("orderId", order.getId());
            params.put("pendingTypeDate", now);
            //调用公共缓存
            OrderCacheParam.Builder builder = new OrderCacheParam.Builder();
            builder.setOpType(OrderCacheOpType.UPDATE)
                    .setOrderId(order.getId())
                    .setPendingTypeDate(now)
                    .setExpireSeconds(0L);
            orderService.updateOrderCondition(params);
            OrderCacheUtils.update(builder.build());
        } catch (OrderException oe) {
            throw oe;
        } catch (Exception e) {
            log.error("[orderMaterialService.rejectMaterialApply] masterId:{}", masterId, e);
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            if (locked && lockkey != null) {
                redisUtils.remove(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey);
            }
        }
    }

    /**
     * 更新配件申请单-物流信息
     *
     * @author Ryan
     * @date 2019-06-28
     * 接入物流接口，订阅物流通知
     */
    @Transactional(readOnly = false)
    public void updateMaterialApplyExpress(MaterialMaster materialMaster,Order order) {
        if (order == null || order.getOrderCondition() == null) {
            throw new OrderException("读取订单失败,请重试");
        }
        //update 申请单
        HashMap<String, Object> params = Maps.newHashMapWithExpectedSize(10);
        params.put("quarter", order.getQuarter());
        params.put("id", materialMaster.getId());
        params.put("expressCompany", materialMaster.getExpressCompany());
        params.put("expressNo", materialMaster.getExpressNo());
        params.put("sendBy", materialMaster.getUpdateBy());
        params.put("sendDate", materialMaster.getUpdateDate());
        params.put("updateBy", materialMaster.getUpdateBy());
        params.put("updateDate", materialMaster.getUpdateDate());
        params.put("status", new Dict(MaterialMaster.STATUS_SENDED, "已发货"));
        dao.updateMaterialMaster(params);

        OrderCondition orderCondition = order.getOrderCondition();
        //订单log
        OrderProcessLog processLog = new OrderProcessLog();
        processLog.setQuarter(order.getQuarter());
        processLog.setAction("配件发货");
        processLog.setOrderId(order.getId());
        processLog.setActionComment(String.format("配件发货,操作人:%s", materialMaster.getUpdateBy().getName()));
        processLog.setStatus(orderCondition.getStatus().getLabel());
        processLog.setStatusValue(orderCondition.getStatusValue());
        processLog.setStatusFlag(OrderProcessLog.OPL_SF_NOT_CHANGE_STATUS);
        processLog.setCloseFlag(0);
        processLog.setCreateBy(materialMaster.getUpdateBy());
        processLog.setCreateDate(materialMaster.getUpdateDate());
        processLog.setCustomerId(orderCondition.getCustomerId());
        processLog.setDataSourceId(order.getDataSourceId());
        orderService.saveOrderProcessLogNew(processLog);
        //订阅物流通知
        if(logisticsMaterialFlag){
            try{
                logisticsBusinessService.subsPartsLogisticsMessage(
                        MQLMExpress.GoodsType.Parts
                        ,order.getId()
                        ,order.getOrderNo()
                        ,order.getQuarter()
                        ,orderCondition.getServicePhone()
                        ,materialMaster.getProduct()
                        ,materialMaster.getExpressCompany()
                        ,materialMaster.getExpressNo()
                );
            }catch (Exception e){
                log.error("配件单订阅物流信息错误- orderId:{},company:{},expressNo:{}",order.getId(),materialMaster.getExpressCompany().getValue(),materialMaster.getExpressNo(),e);
            }
        }
    }

    public Integer getMaterialMasterCountByOrderId(Long orderId,String quarter,Integer status){
        return dao.getMaterialMasterCountByOrderId(orderId, quarter,status);
    }

    /**
     * 取消配件与上门服务项的关联
     * @param orderId  订单id
     * @param quarter  分片
     * @param detailId 上门服务id
    */
    @Transactional
    public void cancelRelationOfServiceAndMaterial(Long orderId,String quarter,Long detailId){
        dao.cancelRelationOfServiceAndMaterial(orderId, quarter,detailId);
    }

    // 添加配件与上门服务项的关联
    @Transactional
    public void  addRelationOfServiceAndMaterial(MaterialMaster model){
        dao.addRelationOfServiceAndMaterial(model);
    }

    // 修改配件申请当头
    public void updateMaterialMaster(HashMap<String,Object> params){
        dao.updateMaterialMaster(params);
    }

    //endregion

    //region 物流接口

    //endregion 物流接口
    @Transactional
    public void updateLogisticSignAt(Long orderId,String quarter,String expressNo,Date signAt){
        dao.updateLogisticSignAt(orderId, quarter,expressNo,signAt,new Date());
    }

    //region 公共方法

    /**
     * 从缓存读取配件单明细中配件信息
     * @param items
     * @param materialMap   配件本地缓存
     * @param loadProductOfMateiral 是否装载配件所属产品信息
     */
    private void loadItemMaterials(List<MaterialItem> items,Map<Long, Material> materialMap,boolean loadProductOfMateiral){
        if(ObjectUtils.isEmpty(items)){
            return;
        }
        Material material,tmpMaterial;
        Map<Long,Product> productMaps = Maps.newHashMapWithExpectedSize(10);
        for(MaterialItem itm:items){
            if(itm == null) {
                continue;
            }
            tmpMaterial = itm.getMaterial();
            if(materialMap == null){
                material = materialService.getFromCache(tmpMaterial.getId());
                if(material != null){
                    itm.setMaterial(material);
                    if(loadProductOfMateiral) {
                        loadMatierlaProduct(itm,productMaps);
                    }
                }
            }else {
                if (materialMap.containsKey(tmpMaterial.getId())) {
                    itm.setMaterial(materialMap.get(tmpMaterial.getId()));
                } else {
                    material = materialService.getFromCache(tmpMaterial.getId());
                    if (material != null) {
                        materialMap.put(tmpMaterial.getId(), material);
                        itm.setMaterial(material);
                        if(loadProductOfMateiral) {
                            loadMatierlaProduct(itm,productMaps);
                        }
                    }
                }
            }
        }
    }

    /**
     * 装载配件所属产品信息
     * @param item
     * @param productMaps
     */
    private void loadMatierlaProduct(MaterialItem item,Map<Long,Product> productMaps) {
        Product product;
        if (productMaps.containsKey(item.getProduct().getId())) {
            item.setProduct(productMaps.get(item.getProduct().getId()));
        } else {
            product = productService.getProductByIdFromCache(item.getProduct().getId());
            if (product != null) {
                productMaps.put(product.getId(), product);
                item.setProduct(product);
            }
        }
    }

    /**
     * 获得订单项产品
     * 如是套组，拆分为单品,同时一并返回产品的品牌，型号/规格及服务类型
     * @param orderId 订单id
     * @param items 订单项目
     * @return Set<product> 可返回null
     */
    public Set<Product> getOrderProductSet(Long orderId,List<OrderItem> items,Map<Long, ServiceType> serviceTypeMap){
        if(orderId == null || orderId <= 0 || ObjectUtils.isEmpty(items) || ObjectUtils.isEmpty(serviceTypeMap)){
            return null;
        }
        List<Product> products = productService.findAllList();
        Map<Long, Product> productMap = products.stream().collect(Collectors.toMap(LongIDBaseEntity::getId, p->p));
        Set<Product> itemProductSet = Sets.newHashSetWithExpectedSize(items.size());
        OrderItem item;
        Product p;
        for(int i=0,size=items.size();i<size;i++){
            item = items.get(i);
            p = item.getProduct();
            if(itemProductSet.contains(p)){
                continue;
            }
            //使用订单项的品牌，型号/规格及服务类型
            p.setBrand(item.getBrand());
            p.setModel(item.getProductSpec());
            p.setServiceType(serviceTypeMap.get(item.getServiceType().getId()));
            itemProductSet.add(p);
        }
        Set<Product> productSet = Sets.newHashSetWithExpectedSize(itemProductSet.size()*2);
        for (Product product : itemProductSet) {
            if (!productSet.contains(product)) {
                if(product.getSetFlag() == 1) {
                    //套组：拆分
                    parseProductSet(orderId, product, productMap, productSet, serviceTypeMap);
                }else{
                    productSet.add(product);
                }
            }
        }
        return productSet;
    }

    public List<NameValuePair<Long, String>> getOrderProductIdAndCustomerModels(List<OrderItem> items){
        if(ObjectUtils.isEmpty(items)){
            return Lists.newArrayList();
        }
        List<Long> productIds = items.stream().filter(i -> i.getProduct() != null && i.getProduct().getId() != null).map(OrderItem::getProductId).distinct().collect(Collectors.toList());
        Map<Long, Product> productMap = productService.getProductMap(productIds);
        List<NameValuePair<Long, String>> result = Lists.newArrayList();
        Product product;
        Set<Long> subProductIdSet;
        List<NameValuePair<Long, String>> subSet;
        for (OrderItem item : items) {
            product = productMap.get(item.getProductId());
            if (product != null) {
                if (product.getSetFlag() == 1) {
                    subProductIdSet = Sets.newHashSet();
                    final String[] setIds = product.getProductIds().split(",");
                    for (String id : setIds) {
                        subProductIdSet.add(StringUtils.toLong(id));
                    }
                    subSet = subProductIdSet.stream().filter(i->i > 0).map(i->new NameValuePair<>(i, item.getB2bProductCode())).collect(Collectors.toList());
                    if (!subSet.isEmpty()) {
                        result.addAll(subSet);
                    }
                } else {
                    result.add(new NameValuePair<>(item.getProductId(), item.getB2bProductCode()));
                }
            }
        }
        return result;
    }

    /**
     * 递归拆分套组
     * @param orderId 订单id
     * @param product   产品,包含了品牌，型号/规格及服务类型(来自订单项目)
     * @param productMap 所有产品Map
     * @param productSet  拆分后单品
     */
    private void parseProductSet(Long orderId,Product product,Map<Long, Product> productMap,Set<Product> productSet,Map<Long, ServiceType> serviceTypeMap){
        if(productSet.contains(product)){
            return;
        }
        Product p = productMap.get(product.getId());
        if(p == null){
            log.error("产品缓存中无产品:{} ,请检查缓存或订单项,订单id:{}",product.getId(),orderId);
        }else{
            if(p.getSetFlag() == 0){
                return;
            }
            List<Long> subProductIdList = getSubProductIdList(p.getProductIds());
            if(!ObjectUtils.isEmpty(subProductIdList)){
                Long id;
                for(int i=0,size=subProductIdList.size();i<size;i++){
                    id = subProductIdList.get(i);
                    p = productMap.get(id);
                    if(productSet.contains(p)){
                        continue;
                    }
                    //品牌，型号/规格及服务类型,来自订单项目
                    p.setBrand(product.getBrand());
                    p.setModel(product.getModel());
                    p.setServiceType(product.getServiceType());
                    if (p.getSetFlag() == 0){
                        productSet.add(p);
                    } else {
                        parseProductSet(orderId, p, productMap, productSet, serviceTypeMap);
                    }
                }
            }
        }
    }


    /**
     * 获得订单项产品Id
     * 如是套组，拆分为单品
     * @param orderId 订单id
     * @param items 订单项目
     * @return Set<productId> 可返回null

    public Set<Long> getOrderProductIdSet(Long orderId,List<OrderItem> items){
        if(ObjectUtils.isEmpty(items)){
            return null;
        }
        List<Product> products = productService.findAllList();
        Map<Long, Product> productMap = products.stream().collect(Collectors.toMap(LongIDBaseEntity::getId, p->p));
        Set<Long> idSet = items.stream().map(t->t.getProduct().getId()).collect(Collectors.toSet());
        Set<Long> productIdSet = Sets.newHashSetWithExpectedSize(idSet.size()*5);
        for (Long pid : idSet) {
            if (!productIdSet.contains(pid)) {
                parseProductSet(orderId, pid, productMap, productIdSet);
            }
        }
        return productIdSet;
    }*/

    /**
     * 递归拆分套组
     * @param orderId 订单id
     * @param pid   产品id
     * @param productMap 所有产品Map
     * @param productIdSet  拆分后单品id
     */
    private void parseProductSet(Long orderId,Long pid,Map<Long, Product> productMap,Set<Long> productIdSet){
        if(productIdSet.contains(pid)){
            return;
        }
        Product product = productMap.get(pid);
        if(product == null){
            log.error("产品缓存中无产品:{} ,请检查缓存或订单项,订单id:{}",pid,orderId);
        }else{
            if(product.getSetFlag() == 0){
                productIdSet.add(pid);
            }else{
               List<Long> subProductIdList = getSubProductIdList(product.getProductIds());
                if(!ObjectUtils.isEmpty(subProductIdList)){
                    Long id;
                    for(int i=0,size=subProductIdList.size();i<size;i++){
                        id = subProductIdList.get(i);
                        if(productIdSet.contains(id)){
                            continue;
                        }
                        parseProductSet(orderId,id,productMap,productIdSet);
                    }
                }
            }
        }
    }

    /**
     * 拆分套组
     * @param subProductIds 如:1,2,3,
     * @return
     */
    public List<Long> getSubProductIdList(String subProductIds){
        if(org.apache.commons.lang3.StringUtils.isBlank(subProductIds)){
            return null;
        }
        List<Long> subProductIdList = Lists.newArrayListWithCapacity(6);
        String[] ids = subProductIds.split(",");
        String sid = new String("");
        Long pid;
        for(int i=0,size=ids.length;i<size;i++){
            sid = ids[i];
            if(StringUtils.isBlank(sid)){
                continue;
            }
            pid = Long.valueOf(sid);
            if(pid>0) {
                subProductIdList.add(pid);
            }
        }
        return subProductIdList;
    }

    /**
     * 判断订单来源是否启用B2B配件微服务
     */
    public boolean isOpenB2BMaterialSource(Integer dataSourceId){

        B2BDataSourceEnum dataSource = B2BDataSourceEnum.valueOf(dataSourceId);
        if(dataSource == null){
            return false;
        }
        return msProperties.getB2bcenter().getMaterial().containsKey(dataSourceId);
    }

    //endregion 公共方法

    //region 跟踪进度

    /**
     * 更新跟踪进度
     * 更新申请单，并记录日志
     * @param id
     * @param quarter
     * @param pendingType
     * @param pendingDate
     * @param pendingContent
     * @param createBy
     */
    @Transactional
    public void updateMaterialPendingInfo(long id, String quarter, Dict pendingType, Date pendingDate, String pendingContent,String createBy){
        dao.updateMaterialPendingInfo(id, quarter,pendingType,pendingDate,pendingContent);
        MaterialLog log = MaterialLog.builder()
                .quarter(quarter)
                .MaterialMasterId(id)
                .content(pendingContent)
                .createBy(createBy)
                .createDate(pendingDate)
                .build();
        logDao.insertLog(log);
    }

    //endregion

    //region 列表

    //region 客服

    /**
     * 分页查询配件单(按status查询)
     * @param page
     * @param searchModel
     * @return
     */
    public Page<MaterialMaster> findKefuMaterialList(Page<OrderMaterialSearchModel> page,OrderMaterialSearchModel searchModel){
        searchModel.setPage(page);
        List<MaterialMaster> list = dao.findKefuMaterialList(searchModel);
        getMaterialMasterInfo(list);
        Page<MaterialMaster> rtnPage = new Page<>();
        rtnPage.setPageNo(page.getPageNo());
        rtnPage.setPageSize(page.getPageSize());
        rtnPage.setCount(page.getCount());
        rtnPage.setOrderBy(page.getOrderBy());
        rtnPage.setList(list);
        return rtnPage;
    }

    /**
     * 读取配件单基本信息
     * 包含客户，区域，产品等
     * @param list
     */
    private void getMaterialMasterInfo(List<MaterialMaster> list){
        getMaterialMasterInfo(list,true);
    }

    /**
     * 读取配件单基本信息
     * 包含客户，区域，产品等
     * @param list
     * @param merchRjectReasonAndRemark 合并驳回原因及详细描述到closeRemark属性，用去前端显示
     */
    private void getMaterialMasterInfo(List<MaterialMaster> list,boolean merchRjectReasonAndRemark){
        Map<Long,Customer> customerHashMap = Maps.newHashMap();
        Map<String,Dict> pendingTypes = MSDictUtils.getDictMap("material_pending_type");
        Map<String,Dict> statuses = MSDictUtils.getDictMap("material_apply_status");
        Map<String,Dict> materialTypes = MSDictUtils.getDictMap("MaterialType");
        Map<String,Dict> applyTypes = MSDictUtils.getDictMap("material_apply_type");
        Map<String,Dict> rejectTypes = MSDictUtils.getDictMap("material_reject_type");
        Map<Long,User> userMaps = Maps.newHashMap();
        Map<Long,Area> areaMap = Maps.newHashMap();
        MaterialMaster materialMaster;
        Customer customer;
        Product product;
        Area area, subArea;
        long id;
        Dict pendingType, status, rejectType;
        User user;
        for(int i=0,size=list.size();i<size;i++){
            materialMaster = list.get(i);
            //customer
            id = materialMaster.getCustomer().getId();
            if(customerHashMap.containsKey(id)){
                customer = customerHashMap.get(id);
            }else{
                customer = customerService.getFromCache(id);
                customerHashMap.put(id,customer);
            }
            if(customer != null) {
                materialMaster.setCustomer(customer);
            }
            // 产品名称中前后逗号处理
            String[] productNameArr=null;
            List<String> productNameList = Lists.newArrayList();
            if(materialMaster.getProductNames().startsWith(",")){
                productNameArr = org.apache.commons.lang3.StringUtils.split(materialMaster.getProductNames(),",");
                materialMaster.setProductNames(org.apache.commons.lang3.StringUtils.substringBetween(materialMaster.getProductNames(),","));
                if(productNameArr!=null && productNameArr.length>0){
                    productNameList = Lists.newArrayList(productNameArr);
                }
                materialMaster.setProductNameList(productNameList);
            }
            //area
            if(areaMap.containsKey(materialMaster.getArea().getId())){
                area = areaMap.get(materialMaster.getArea().getId());
            }else{
                area = areaService.getFromCache(materialMaster.getArea().getId());
                if(area != null){
                    areaMap.put(materialMaster.getArea().getId(),area);
                }
            }
            if(area != null) {
                materialMaster.setArea(area);
            }
            //sub area
            if(materialMaster.getSubArea().getId()>3){
                if(areaMap.containsKey(materialMaster.getSubArea().getId())){
                    subArea = areaMap.get(materialMaster.getSubArea().getId());
                }else {
                    subArea = areaService.getTownFromCache(area.getId(), materialMaster.getSubArea().getId());
                    if(subArea != null){
                        areaMap.put(materialMaster.getSubArea().getId(),subArea);
                    }
                }
                if(subArea != null){
                    materialMaster.setSubArea(subArea);
                }
            }else{
                materialMaster.getSubArea().setName("");
            }
            //pending type
            if(materialMaster.getPendingType().getIntValue()>0) {
                pendingType = pendingTypes.get(materialMaster.getPendingType().getValue());
                if (pendingType == null) {
                    materialMaster.getPendingType().setLabel(materialMaster.getPendingType().getValue() + ":跟踪状态已删除");
                } else {
                    materialMaster.getPendingType().setLabel(pendingType.getLabel());
                }
            }
            //status
            status = statuses.get(materialMaster.getStatus().getValue());
            if(status == null){
                materialMaster.getStatus().setLabel(materialMaster.getStatus().getValue() + ":状态已删除");
            }else {
                materialMaster.getStatus().setLabel(status.getLabel());
            }
            //materialType
            //pendingType = materialTypes.get(materialMaster.getMaterialType().getValue());
            //if(pendingType == null){
            //    materialMaster.getMaterialType().setLabel(materialMaster.getMaterialType().getValue() + ":配件类型已删除");
            //}else{
            //    materialMaster.getMaterialType().setLabel(pendingType.getLabel());
            //}
            //apply type
            pendingType = applyTypes.get(materialMaster.getApplyType().getValue());
            if(pendingType == null){
                materialMaster.getApplyType().setLabel(materialMaster.getApplyType().getValue() + ":申请类型已删除");
            }else{
                materialMaster.getApplyType().setLabel(pendingType.getLabel());
            }
            //createBy
            if(userMaps.containsKey(materialMaster.getCreateBy().getId())){
                user = userMaps.get(materialMaster.getCreateBy().getId());
            }else {
                user = userService.get(materialMaster.getCreateBy().getId());
                userMaps.put(materialMaster.getCreateBy().getId(),user);
            }
            if (user != null) {
                materialMaster.setCreateBy(user);
            }
            //updateBy
            if(userMaps.containsKey(materialMaster.getUpdateBy().getId())){
                user = userMaps.get(materialMaster.getUpdateBy().getId());
            }else {
                user = userService.get(materialMaster.getUpdateBy().getId());
                userMaps.put(materialMaster.getUpdateBy().getId(),user);
            }
            if (user != null) {
                materialMaster.setUpdateBy(user);
            }
            if(materialMaster.getStatus().getIntValue() == 5){//驳回
                materialMaster.setCloseDate(materialMaster.getUpdateDate());
                if(materialMaster.getCloseBy().getId() > 0) {
                    if (userMaps.containsKey(materialMaster.getCloseBy().getId())) {
                        user = userMaps.get(materialMaster.getCloseBy().getId());
                    } else {
                        user = userService.get(materialMaster.getCloseBy().getId());
                        userMaps.put(materialMaster.getCloseBy().getId(), user);
                    }
                    if (user != null) {
                        materialMaster.setCloseBy(user);
                    }
                }
                String closeType = materialMaster.getCloseType();
                if(StrUtil.isNotBlank(closeType)){
                    //驳回原因和详细描述都存储在closeRemark
                    rejectType = rejectTypes.computeIfAbsent(closeType, k -> new Dict(closeType, ""));
                    materialMaster.setCloseType(rejectType.getLabel());
                    if(merchRjectReasonAndRemark) {
                        if (StrUtil.isNotBlank(materialMaster.getCloseRemark())) {
                            materialMaster.setCloseRemark(String.format("%s-%s", rejectType.getLabel(), materialMaster.getCloseRemark()));
                        } else {
                            materialMaster.setCloseRemark(rejectType.getLabel());
                        }
                    }
                }
            } else if (materialMaster.getCloseBy() != null && materialMaster.getCloseBy().getId() != null && materialMaster.getCloseBy().getId()>0) {
                //closeBy
                if (userMaps.containsKey(materialMaster.getCloseBy().getId())) {
                    user = userMaps.get(materialMaster.getCloseBy().getId());
                } else {
                    user = userService.get(materialMaster.getCloseBy().getId());
                    userMaps.put(materialMaster.getCloseBy().getId(), user);
                }
                if (user != null) {
                    materialMaster.setCloseBy(user);
                }
            }

        }
    }

    //endregion

    //region 客户

    /**
     * 分页查询配件单(按status查询)
     * @param page
     * @param searchModel
     * @return
     */
    public Page<MaterialMaster> findCustomerMaterialList(Page<OrderMaterialSearchModel> page,OrderMaterialSearchModel searchModel){
        return findCustomerMaterialList(page,searchModel,false);
    }

    /**
     * 分页查询配件单(按status查询)
     * @param page
     * @param searchModel
     * @return
     */
    public Page<MaterialMaster> findCustomerMaterialList(Page<OrderMaterialSearchModel> page,OrderMaterialSearchModel searchModel,boolean merchRjectReasonAndRemark){
        searchModel.setPage(page);
        List<MaterialMaster> list = dao.findCustomerMaterialList(searchModel);
        getMaterialMasterInfo(list,merchRjectReasonAndRemark);
        Page<MaterialMaster> rtnPage = new Page<>();
        rtnPage.setPageNo(page.getPageNo());
        rtnPage.setPageSize(page.getPageSize());
        rtnPage.setCount(page.getCount());
        rtnPage.setOrderBy(page.getOrderBy());
        rtnPage.setList(list);
        return rtnPage;
    }
    //endregion

    //endregion

    //region 客评

    /**
     * 根据订单配件状态检查是否可以客评,退单,取消
     * old,作废
     * 1.配件单未审核,未发货，不能客评
     * 2.返件单未发货，不能客评
     * new 2019-07-10
     * 1.判断配件单，是否审核，没审核不能客评
     * 2.如驳回，可以客评
     * 3.审核通过，判断返件单是否需要返件，不需要返件，可以客评；需要返件，必须填了返件快递单号才能客评
     * @param orderId
     * @param quarter
     * @return MSResponse MSResponse.isSuccesCode() == true ,可客评
     *      code = 10000 ,不能客评
     *      code = 1，需手动关闭再客评
     */
    public MSResponse<String> canGradeOfMaterialForm(Integer dataSource, long orderId,String quarter){
        MSResponse<String> response = new MSResponse<String>();
        if(orderId<=0){
            return new MSResponse(MSErrorCode.newInstance(MSErrorCode.FAILURE,"订单参数错误"));
        }
        //1.配件单：未审核
        List<MaterialMaster> forms = dao.getMaterialFormsForGrade(orderId, quarter);
        //无配件单
        if(ObjectUtils.isEmpty(forms)){
            return response;
        }
        //流的重用,使用Supplier
        Supplier<Stream<MaterialMaster>> streamSupplier = () -> forms.stream();
        long cnt = streamSupplier.get()
                .filter(t -> t.getStatus().getIntValue() == MaterialMaster.STATUS_NEW.intValue())
                .count();
        if(cnt > 0){
            response.setCode(1);
            response.setMsg("订单有未审核的配件单");
            response.setData(forms.get(0).getOrderNo());
            return response;
        }
        //b2b配件单，检查：是否发货
        if (msProperties.getB2bcenter().getMaterial().containsKey(dataSource)) {
            cnt = streamSupplier.get()
                    .filter(t -> t.getStatus().getIntValue() == MaterialMaster.STATUS_APPROVED.intValue())
                    .count();
            if(cnt > 0){
                response.setCode(1);
                response.setMsg("订单有未发货的配件单，待厂商系统处理");
                response.setData(forms.get(0).getOrderNo());
                return response;
            }
        }
        streamSupplier = null;
        // 2.返件单：未发货，配件单审核后才产生的返件单，因此返件单无需审核
        List<MaterialReturn> returnForms = returnService.getMaterialReturnListForGrade(orderId, quarter);
        //无返件单
        if(ObjectUtils.isEmpty(returnForms)){
            return response;
        }
        cnt = returnForms.stream()
                .filter(t-> t.getStatus().getIntValue() <= MaterialMaster.STATUS_APPROVED.intValue())
                .count();
        if(cnt>0){
            //response.setCode(MSErrorCode.FAILURE.getCode());
            response.setCode(1);
            response.setMsg("订单有返件单未处理完成。");
            response.setData(returnForms.get(0).getOrderNo());
            return response;
        }
        return response;
    }

    /**
     * 客评时关闭配件单(by订单)
     * @param orderId
     * @param quarter
     */
    @Transactional
    public void closeMaterialMasterWhenGrade(Integer dataSource,Long orderId, String quarter,User closeBy,Date date,String remark) {
        dao.closeMaterialMasterWhenGrade(orderId, quarter,closeBy,date,remark);
        //判断数据源，调用b2b接口
        //by工单关闭
        closeB2BMaterialForm(dataSource,orderId,null,"", remark,MaterialMaster.STATUS_CLOSED,closeBy.getId());
    }

    /**
     * 关闭B2B配件单
     * 2019-11-01 增加数据源判断及日志输出
     *
     * @param dataSource  数据源
     * @param orderId       工单Id,不为null,按工单关闭
     * @param id            配件单id,orderId为null,按配件单关闭
     * @param formNo        配件单编号
     * @param closeRemark   关闭备注
     * @param status        关闭后状态，根据状态决定关闭类型
     */
    @Transactional
    public void closeB2BMaterialForm(Integer dataSource,Long orderId,Long id, String formNo, String closeRemark, Integer status,Long user) {
        //增加数据源判断
        if(dataSource == null || dataSource <=0){
            log.error("关闭b2b配件错误-无数据源,datasource:{} ,orderId:{}",dataSource==null?"null":dataSource.toString(),orderId);
            return;
        }
        if (!isOpenB2BMaterialSource(dataSource)) {
            return;
        }

        B2BDataSourceEnum dataSourceEnum = B2BDataSourceEnum.valueOf(dataSource);
        if(dataSourceEnum == null) {
            log.error("关闭b2b配件错误-数据源错误,datasource:{}", dataSource);
            return;
        }
        B2BMaterialExecutor b2BMaterialExecutor = b2bMaterialExecutorFactory.getExecutor(dataSourceEnum);
        if(b2BMaterialExecutor != null){
            B2BMaterialClose.CloseType b2bCloseType = B2BMaterialClose.CloseType.CLOSE;
            if(status.equals(MaterialMaster.STATUS_CLOSED)) {
                b2bCloseType = B2BMaterialClose.CloseType.CLOSE;
            }else if (status.equals(MaterialMaster.STATUS_REJECT)) {
                b2bCloseType = B2BMaterialClose.CloseType.CANCEL;
            }else if (status.equals(MaterialMaster.STATUS_ABNORMAL)){
                b2bCloseType = B2BMaterialClose.CloseType.ABNORMAL_CLOSE;
            }
            MSResponse msResponse = null;
            if(orderId != null){
                msResponse = b2BMaterialExecutor.materialCloseByOrder(orderId,b2bCloseType,closeRemark,user);
            }
            else{
                msResponse = b2BMaterialExecutor.materialClose(id,formNo,b2bCloseType,closeRemark,user);
            }
            if(!MSResponse.isSuccessCode(msResponse)){
                throw new RuntimeException(msResponse.getMsg());
            }
        }
    }

    /**
     * 取消或退单审核时驳回并关闭配件单(by订单)
     * @param orderId
     * @param quarter
     */
    @Transactional
    public void closeMaterialMasterWhenCancel(Integer dataSource,Long orderId, String quarter,User closeBy,Date date,String remark) {
        dao.closeNoSendedMaterialMasterWhenCancel(orderId, quarter,closeBy,date,remark);
        dao.closeSendedMaterialMasterWhenCancel(orderId, quarter,closeBy,date,remark);
        //by配件单关闭
        closeB2BMaterialForm(dataSource,orderId,null,"", remark,MaterialMaster.STATUS_REJECT,closeBy.getId());
    }

    /**
     * 人工关闭配件申请单(by配件单)
     * 状态：驳回时，同时驳回返件单
     * @param id 配件单id
     * @param quarter
     * @param user 关闭人
     * @param date 关闭日期
     * @param closeRemark 备注
     * @param returnFlag  返件
     * @param status 状态
     */
    @Transactional
    public void manuClose(Integer dataSource,Long id, String formNo, String quarter, User user, Date date, String closeRemark, String closeType,Integer returnFlag, Integer status) {
        dao.manuCloseMaterialForm(id, quarter,user,date,closeRemark,closeType,returnFlag,status);
        //判断数据源，调用b2b接口
        //by配件单关闭
        closeB2BMaterialForm(dataSource,null,id,formNo, closeRemark,status,user.getId());
        /*
        if(returnFlag == 0){
            //不返件,配件列表
            dao.updateItemNoReturn(id, quarter);
        }
        //不返件，返件单也驳回
        if(returnFlag == 0){
            returnService.manuRejectAndCloseReturnForm(null,id, quarter,user,date,closeRemark,MaterialMaster.STATUS_REJECT);
        }*/
    }

    /**
     * 读取下个申请次序
     */
    public int getNextApplyTime(Long orderId, String quarter) {
        return dao.getNextApplyTime(orderId, quarter);
    }

    //endregion

    //region 数据处理

    public List<MaterialMaster> findToFixMaterialForms(List<String> quarters,Long id,Long maxId,int pageSize){
        return dao.findToFixMaterialForms(quarters,id,maxId,pageSize);
    }

    public int updateMaterialItemProductItemId(String quarter,Long materialMasterId,Long productId,Long materialProductId){
        return dao.updateMaterialItemProductItemId(quarter,materialMasterId,productId,materialProductId);
    }

    public int updateMaterialFormApplyTime(String quarter,Long id,int applyTime){
        return dao.updateMaterialFormApplyTime(quarter,id,applyTime);
    }

    public void insertMaterialProduct(MaterialProduct materialProduct){
        dao.insertMaterialProduct(materialProduct);
    }
    //endregion


    //设置省和市
    public void setArea(OrderCondition orderCondition,MaterialMaster materialMaster){
        //省市
        Area area = areaService.getFromCache(orderCondition.getArea().getId());
        if (area != null) {
            List<String> ids = Splitter.onPattern(",")
                    .omitEmptyStrings()
                    .trimResults()
                    .splitToList(area.getParentIds());
            if (ids.size() >= 2) {
                materialMaster.setCityId(Long.valueOf(ids.get(ids.size() - 1)));
                materialMaster.setProvinceId(Long.valueOf(ids.get(ids.size() - 2)));
            }
        }
    }

    /**
     * 根据配件单号获取配件
     * */
    public MaterialMaster getByMasterNo(String masterNo){
        return dao.getByMasterNo(masterNo);
    }

    /**
     * 批量发货
     * @param materialMasters
     */
    @Transactional()
    public void batchSaveExpress(List<MaterialMaster> materialMasters,User user){
        MaterialMaster materialMaster =null;
        Order order = null;
        Product product = null;
        Date date = new Date();
       for(MaterialMaster entity:materialMasters){
           materialMaster = dao.getAppointFields(entity.getId(),entity.getQuarter());
           if(materialMaster!=null && materialMaster.getStatus()!=null && materialMaster.getStatus().getIntValue().equals(MaterialMaster.STATUS_APPROVED)){
               if(entity.getExpressCompany()==null || StringUtils.isBlank(entity.getExpressCompany().getValue())){
                   throw new RuntimeException("配件:"+materialMaster.getMasterNo()+"快递公司为空,请确认");
               }
               if(StringUtils.isBlank(entity.getExpressNo())){
                   throw new RuntimeException("配件:"+materialMaster.getMasterNo()+"快递单号为空,请确认");
               }
               order = orderService.getOrderById(materialMaster.getOrderId(), entity.getQuarter(), OrderUtils.OrderDataLevel.CONDITION, true);
               if(order ==null || order.getOrderCondition()==null){
                   throw new RuntimeException("读取配件单的订单信息失败");
               }
               product = productService.getProductByIdFromCache(materialMaster.getProduct().getId());
               if(product != null){
                   materialMaster.setProduct(product);
               }
               materialMaster.setExpressCompany(entity.getExpressCompany());
               materialMaster.setExpressNo(entity.getExpressNo());
               materialMaster.setUpdateBy(user);
               materialMaster.setUpdateDate(date);
               updateMaterialApplyExpress(materialMaster,order);
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
           }
       }
    }

    public List<MaterialMaster> findMaterialExportList(OrderMaterialSearchModel searchModel){
        List<MaterialMaster> materialMasters = dao.findMaterialExportList(searchModel);
        //getMaterialMasterInfo(materialMasters,false);
        return materialMasters;
    }
}
