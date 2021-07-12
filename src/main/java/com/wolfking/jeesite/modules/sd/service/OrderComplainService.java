/**
 * Copyright &copy; 2012-2013 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.wolfking.jeesite.modules.sd.service;

import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2bcenter.md.B2BDataSourceEnum;
import com.kkl.kklplus.entity.b2bcenter.pb.MQB2BOrderComplainProcessMessage;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderEnum;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.exception.OrderException;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.BitUtils;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.entity.ProductCategory;
import com.wolfking.jeesite.modules.md.service.CustomerService;
import com.wolfking.jeesite.modules.md.service.ProductCategoryService;
import com.wolfking.jeesite.modules.mq.dto.MQOrderServicePointMessage;
import com.wolfking.jeesite.modules.mq.entity.RPTOrderComplainModel;
import com.wolfking.jeesite.modules.mq.service.RPTOrderComplainService;
import com.wolfking.jeesite.modules.mq.service.ServicePointOrderBusinessService;
import com.wolfking.jeesite.modules.sd.dao.OrderComplainDao;
import com.wolfking.jeesite.modules.sd.entity.*;
import com.wolfking.jeesite.modules.sd.entity.viewModel.ComplainSearchModel;
import com.wolfking.jeesite.modules.sd.utils.OrderCacheUtils;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.entity.VisibilityFlagEnum;
import com.wolfking.jeesite.modules.sys.service.AreaService;
import com.wolfking.jeesite.modules.sys.service.UserRegionService;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.b2bcenter.mq.sender.B2BCenterOrderComplainProcessMQSender;
import com.wolfking.jeesite.ms.b2bcenter.sd.service.B2BCenterOrderService;
import com.wolfking.jeesite.ms.providermd.service.MSCustomerService;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import com.wolfking.jeesite.ms.utils.MSUserUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 订单投诉Service
 *
 * @author RyanLu
 * @version 2020-04-21
 */
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Slf4j
public class OrderComplainService extends OrderRegionService {

    @Autowired
    private OrderComplainDao complainDao;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private MSCustomerService msCustomerService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ServicePointOrderBusinessService servicePointOrderBusinessService;

    @Autowired
    private ProductCategoryService productCategoryService;

    @Autowired
    private B2BCenterOrderService b2BCenterOrderService;

    @Autowired
    private RPTOrderComplainService orderComplainService;

    /**
     * 某订单下投诉单列表
     *
     * @param orderId
     * @param orderNo
     * @param quarter
     * @param loadDicts 装载数据字典
     * @return
     */
    public List<OrderComplain> getComplainListByOrder(Long orderId, String orderNo, String quarter, boolean loadDicts) {
        List<OrderComplain> list = complainDao.getByOrderNo(orderId, orderNo, quarter);
        List<Long> createByIdList = list.stream().map(complain -> complain.getCreateBy().getId()).collect(Collectors.toList());
        Map<Long, String> nameMap = MSUserUtils.getNamesByUserIds(createByIdList);
        if (nameMap.size() > 0) {
            list.forEach(complain -> {
                if (nameMap.get(complain.getCreateBy().getId()) != null) {
                    complain.getCreateBy().setName(nameMap.get(complain.getCreateBy().getId()));
                }
            });
        }
        if (loadDicts && list != null && list.size() > 0) {
            List<Dict> status = MSDictUtils.getDictList("complain_status");
            List<Dict> types = MSDictUtils.getDictList("complain_type");
            List<Dict> complainObjects = MSDictUtils.getDictList("complain_object");
            List<Dict> complainItems = MSDictUtils.getDictList("complain_item");
            //judge
            List<Dict> judgeObjects = MSDictUtils.getDictList("judge_object");
            String[] objectValues = judgeObjects.stream().map(t -> t.getValue()).toArray(String[]::new);
            List<Dict> judgeItems = MSDictUtils.getDictList("judge_item_", objectValues);
            //close
            List<Dict> completeResults = MSDictUtils.getDictList("complete_result");
            OrderComplain complain;
            Dict dict;
            List<String> ids;
            List<Dict> dictList;
            Customer customer;
            final StringBuffer buffer = new StringBuffer();
            for (int i = 0, size = list.size(); i < size; i++) {
                complain = list.get(i);
                customer = customerService.getFromCache(complain.getCustomer().getId());
                if (customer != null) {
                    complain.setCustomer(customer);
                }
                //status
                buffer.setLength(0);
                buffer.append(complain.getStatus().getValue());
                dict = status.stream().filter(t -> t.getValue().equalsIgnoreCase(buffer.toString())).findFirst().orElse(null);
                if (dict != null) {
                    complain.setStatus(dict);
                }
                //complain_type
                buffer.setLength(0);
                buffer.append(complain.getComplainType().getValue());
                dict = types.stream().filter(t -> t.getValue().equalsIgnoreCase(buffer.toString())).findFirst().orElse(null);
                if (dict != null) {
                    complain.setComplainType(dict);
                }
                //complain_object
                ids = BitUtils.getPositions(complain.getComplainObject(),String.class);
                if (ids.size() > 0) {
                    dictList = Lists.newArrayList();
                    for (int j = 0, jsize = ids.size(); j < jsize; j++) {
                        buffer.setLength(0);
                        buffer.append(ids.get(j));
                        dict = complainObjects.stream().filter(t -> t.getValue().equalsIgnoreCase(buffer.toString())).findFirst().orElse(null);
                        if (dict != null) {
                            dictList.add(dict);
                        }
                    }
                    complain.setComplainObjects(dictList);
                }
                //complain_item
                ids = BitUtils.getPositions(complain.getComplainItem(),String.class);
                if (ids.size() > 0) {
                    dictList = Lists.newArrayList();
                    for (int j = 0, jsize = ids.size(); j < jsize; j++) {
                        buffer.setLength(0);
                        buffer.append(ids.get(j));
                        dict = complainItems.stream().filter(t -> t.getValue().equalsIgnoreCase(buffer.toString())).findFirst().orElse(null);
                        if (dict != null) {
                            dictList.add(dict);
                        }
                    }
                    complain.setComplainItems(dictList);
                }
                //判定
                //judgeObjects
                if (complain.getJudgeObject() > 0) {
                    ids = BitUtils.getPositions(complain.getJudgeObject(),String.class);
                    if (ids.size() > 0) {
                        dictList = Lists.newArrayList();
                        for (int j = 0, jsize = ids.size(); j < jsize; j++) {
                            buffer.setLength(0);
                            buffer.append(ids.get(j));
                            dict = judgeObjects.stream().filter(t -> t.getValue().equalsIgnoreCase(buffer.toString())).findFirst().orElse(null);
                            if (dict != null) {
                                dictList.add(dict);
                            }
                        }
                        complain.setJudgeObjects(dictList);
                    }

                }
                //judgeItems
                if (complain.getJudgeItem() > 0) {
                    ids = BitUtils.getPositions(complain.getJudgeItem(),String.class);
                    if (ids.size() > 0) {
                        dictList = Lists.newArrayList();
                        for (int j = 0, jsize = ids.size(); j < jsize; j++) {
                            buffer.setLength(0);
                            buffer.append(ids.get(j));
                            dict = judgeItems.stream().filter(t -> t.getValue().equalsIgnoreCase(buffer.toString())).findFirst().orElse(null);
                            if (dict != null) {
                                dictList.add(dict);
                            }
                        }
                        complain.setJudgeItems(dictList);
                    }

                }
                //结案
                //complete_result
                ids = BitUtils.getPositions(complain.getCompleteResult(),String.class);
                if (ids.size() > 0) {
                    dictList = Lists.newArrayList();
                    for (int j = 0, jsize = ids.size(); j < jsize; j++) {
                        buffer.setLength(0);
                        buffer.append(ids.get(j));
                        dict = completeResults.stream().filter(t -> t.getValue().equalsIgnoreCase(buffer.toString())).findFirst().orElse(null);
                        if (dict != null) {
                            dictList.add(dict);
                        }
                    }
                    complain.setCompleteResults(dictList);
                }

            }
        }
        return list;
    }

    public OrderComplain getComplain(Long id, String quarter) {
        OrderComplain orderComplain = complainDao.getById(id, quarter);
        String createByName = MSUserUtils.getName(orderComplain.getCreateBy().getId());
        orderComplain.getCreateBy().setName(createByName);
        return orderComplain;
    }

    /**
     * 按订单读取所有投诉单Id及状态  For B2B投诉单状态变更使用
     * B2B投诉可以同时发起多个投诉单，订单投诉单状态需考虑多个投诉单情况
     * 返回字段: id,status
     */
    public List<LongTwoTuple> getStatusByOrder(Long orderId,String quarter){
        if(orderId == null || orderId <=0 || StringUtils.isBlank(quarter)){
            return Lists.newArrayList();
        }
        return complainDao.getStatusByOrder(orderId,quarter);
    }

    /**
     * 投诉单附件列表
     *
     * @param id          投诉单id
     * @param quarter     数据库分片
     * @param attacheType 附件类型 0-投诉附件 1-判定附件 2-结案附件
     * @return
     */
    public List<OrderComplainAttachment> getComplainAttachements(Long id, String quarter, Integer attacheType) {
        return complainDao.getAttachments(id, quarter, attacheType);
    }

    /**
     * 投诉单附件
     * @param id          附件id
     * @param quarter     数据库分片
     * @return
     */
    public OrderComplainAttachment getComplainAttachement(Long id, String quarter) {
        return complainDao.getAttachment(id, quarter);
    }

    public int deleteComplainAttachment(Long complainId,Long id,String quarter,User updateBy,Date updateDate){
        int effectrows = complainDao.delAttachment(id,quarter,updateBy,updateDate);
        if(effectrows>0){
            complainDao.incOrDescJudgeAttachQty(complainId,quarter,-1);
        }
        return effectrows;
    }

    /**
     * 新增或修改投诉申请
     * 2020-11-16 新增参数: orderStatus
     *  不为null，直接更新缓存(用于直接操作产生催单)
     *  null：直接清除缓存中的orderStatus(用于B2B或短信回复产生催单)
     * @param complain
     * @param orderStatus 订单状态实例
     */
    @Transactional(readOnly = false)
    public void saveComplainApply(OrderComplain complain,OrderStatus orderStatus) {
        int action = complain.getAction();
        HashMap<String, Object> maps = Maps.newHashMap();
        OrderComplainAttachment attach;
        List<OrderComplainAttachment> attachments;
        int intValue;
        int dictValue;
        Double doubleValue;
        Date date = new Date();
        List<Integer> items;
        if (action == 0) {//new
            attachments = complain.getApplyAttaches();
            if (attachments != null && attachments.size() > 0) {
                attachments = attachments.stream().filter(t -> StringUtils.isNotBlank(t.getFileName()))
                        .collect(Collectors.toList());
            }

            complain.setAttachmentQty(attachments.size());
            //投诉对象complain_object
            intValue = 0;
            if(!CollectionUtils.isEmpty(complain.getComplainObjectsIds())) {
                items = complain.getComplainObjectsIds().stream().map(t -> Integer.valueOf(t)).collect(Collectors.toList());
                intValue = BitUtils.markedAndToTags(items);
            }
            complain.setComplainObject(intValue);

            //投诉项目complain_item
            intValue = 0;
            if(!CollectionUtils.isEmpty(complain.getComplainItemsIds())) {
                items = complain.getComplainItemsIds().stream().map(t -> Integer.valueOf(t)).collect(Collectors.toList());
                intValue = BitUtils.markedAndToTags(items);
            }
            complain.setComplainItem(intValue);

            //新建的时间把待跟进时间也设置为创建时间
            if(complain.getCreateDate() == null) {
                complain.setCreateDate(date);
                complain.setAppointDate(date);
            }else{
                complain.setAppointDate(complain.getCreateDate());
            }
            //B2B信息
            if(complain.getB2bComplainNo() == null){
                complain.setB2bComplainNo(StringUtils.EMPTY);
            }
            complainDao.insert(complain);
            //attachments
            if (attachments != null && attachments.size() > 0) {
                for (int i = 0, size = attachments.size(); i < size; i++) {
                    attach = attachments.get(i);
                    attach.setComplainId(complain.getId());
                    attach.setQuarter(complain.getQuarter());
                    attach.setCreateBy(complain.getCreateBy());
                    attach.setCreateDate(date);
                    attach.setCreateDate(complain.getCreateDate());
                    complainDao.insertAttachement(attach);
                }
            }
            //投诉的order condition 迁移到 order status
            //update order status
            User user = complain.getCreateBy();
            maps.clear();
            maps.put("orderId",complain.getOrderId());
            maps.put("quarter",complain.getQuarter());
            maps.put("complainFlag",1);
            maps.put("complainAt",date.getTime());
            maps.put("complainBy",user.getName());
            maps.put("complainStatus",complain.getStatus().getValue());
            orderService.updateComplainInfo(maps);


            //save complain log
            OrderComplainLog orderComplainLog = new OrderComplainLog();
            orderComplainLog.setComplainId(complain.getId());
            orderComplainLog.setQuarter(complain.getQuarter());
            orderComplainLog.setContent("新增投诉单,投诉描述：" + complain.getComplainRemark());
            orderComplainLog.setStatus(complain.getStatus());
            orderComplainLog.setCreateBy(user);
            orderComplainLog.setCreateDate(new Date());
            saveOrderComplainLog(orderComplainLog);

            //同步网点工单数据
            servicePointOrderBusinessService.relatedForm(
                    complain.getOrderId(),
                    complain.getQuarter(),
                    0,
                    1,
                    0,
                    user.getId(),
                    date.getTime()
            );
            //调用公共缓存
            OrderCacheParam.Builder builder = new OrderCacheParam.Builder();
            if(orderStatus != null){
                //直接更新缓存 1.complainStatus  2.complainFlag
                orderStatus.setComplainFlag(1);
                orderStatus.setComplainStatus(complain.getStatus().getIntValue());
                builder.setOpType(OrderCacheOpType.UPDATE)
                        .setOrderId(complain.getOrderId())
                        .setOrderStatus(orderStatus);
            }else {//清除缓存
                builder.setOpType(OrderCacheOpType.UPDATE)
                        .setOrderId(complain.getOrderId())
                        .setDeleteField(OrderCacheField.ORDER_STATUS);
            }
            OrderCacheUtils.update(builder.build());
        } else {//edit
            attachments = complain.getApplyAttaches();
            if (attachments != null && attachments.size() > 0) {
                attachments = attachments.stream().filter(t -> StringUtils.isNotBlank(t.getFileName())).collect(Collectors.toList());
            }
            //投诉对象complain_object
            intValue = 0;
            if(!CollectionUtils.isEmpty(complain.getComplainObjectsIds())) {
                items = complain.getComplainObjectsIds().stream().map(t -> Integer.valueOf(t)).collect(Collectors.toList());
                intValue = BitUtils.markedAndToTags(items);
            }
            complain.setComplainObject(intValue);

            //投诉项目complain_item
            intValue = 0;
            if(!CollectionUtils.isEmpty(complain.getComplainItemsIds())) {
                items = complain.getComplainItemsIds().stream().map(t -> Integer.valueOf(t)).collect(Collectors.toList());
                intValue = BitUtils.markedAndToTags(items);
            }
            complain.setComplainItem(intValue);
            maps.clear();
            maps.put("id", complain.getId());
            maps.put("quarter", complain.getQuarter());
            maps.put("complainObject", complain.getComplainObject());//投诉对象
            maps.put("complainType", complain.getComplainType());
            maps.put("complainBy", complain.getComplainBy());
            maps.put("complainDate", complain.getComplainDate());
            maps.put("complainItem", complain.getComplainItem());
            maps.put("complainRemark", complain.getComplainRemark());
            if (attachments != null && attachments.size() > 0) {
                maps.put("attachmentQty", attachments.size());
            }
            maps.put("updateBy", complain.getUpdateBy());
            maps.put("updateDate", complain.getUpdateDate());
            complainDao.UpdateOrderComplain(maps);
            //attachment
            List<Long> attachIds = complainDao.getAttachmentIds(complain.getId(), complain.getQuarter(), 0);
            if (attachIds == null || attachIds.size() == 0) {
                //add
                if (attachments != null && attachments.size() > 0) {
                    for (int i = 0, size = attachments.size(); i < size; i++) {
                        attach = attachments.get(i);
                        attach.setComplainId(complain.getId());
                        attach.setQuarter(complain.getQuarter());
                        attach.setCreateBy(complain.getUpdateBy());
                        attach.setCreateDate(complain.getUpdateDate());
                        complainDao.insertAttachement(attach);
                    }
                }
            } else {
                List<String> ids = attachments.stream().map(t -> t.getStrId()).distinct().collect(Collectors.toList());
                //1.del
                for (int j = 0, size = attachIds.size(); j < size; j++) {
                    if (!ids.contains(attachIds.get(j).toString())) {
                        complainDao.delAttachment(attachIds.get(j), complain.getQuarter(), complain.getUpdateBy(), complain.getUpdateDate());
                    }
                }
                //2.add
                if (attachments != null && attachments.size() > 0) {
                    for (int i = 0, size = attachments.size(); i < size; i++) {
                        attach = attachments.get(i);
                        //if(attach.getId() ==null || attach.getId()==0) {
                        if (StringUtils.isBlank(attach.getStrId()) && (attach.getId() == null || attach.getId() == 0)) {
                            attach.setComplainId(complain.getId());
                            attach.setQuarter(complain.getQuarter());
                            attach.setCreateBy(complain.getUpdateBy());
                            attach.setCreateDate(complain.getUpdateDate());
                            complainDao.insertAttachement(attach);
                        }
                    }
                }
            }

            //save complain log
            User user = UserUtils.getUser();
            OrderComplainLog orderComplainLog = new OrderComplainLog();
            orderComplainLog.setComplainId(complain.getId());
            orderComplainLog.setQuarter(complain.getQuarter());
            orderComplainLog.setContent("修改投诉单,投诉描述：" + complain.getComplainRemark());
            orderComplainLog.setStatus(complain.getStatus());
            orderComplainLog.setCreateBy(user);
            orderComplainLog.setCreateDate(new Date());
            saveOrderComplainLog(orderComplainLog);

        }
    }

    /**
     * 分页查询投诉单
     *
     * @date 2021-01-15
     * @author ryan
     * @description 增加订单状态字段，读取时读取数据字典
     *
     */
    public Page<OrderComplain> findComplainList(Page<ComplainSearchModel> page, ComplainSearchModel entity) {
        page.setPageSize(12);
        entity.setPage(page);
        double value;
        //处理查询对象为投诉对象和投诉项目
        if (entity.getComplainObject() != null && StringUtils.isNotBlank(entity.getComplainObject().getValue())) {
            value = Double.valueOf(entity.getComplainObject().getValue());
            Double searcehvalue = Math.pow(2, value);

            entity.setComplainObjectValue(searcehvalue);
        }
        if (entity.getComplainItem() != null && StringUtils.isNotBlank(entity.getComplainItem().getValue())) {
            value = Double.valueOf(entity.getComplainItem().getValue());
            Double searcehvalue = Math.pow(2, value);
            entity.setComplainItemValue(searcehvalue);
        }

        //处理查询对象为责任对象和责任项目
        if (entity.getJudgeObject() != null && StringUtils.isNotBlank(entity.getJudgeObject().getValue())) {
            value = Double.valueOf(entity.getJudgeObject().getValue());
            entity.setJudgeObjectValue(Math.pow(2, value));
        }
        if (entity.getJudgeItem() != null && StringUtils.isNotBlank(entity.getJudgeItem().getValue())) {
            value = Double.valueOf(entity.getJudgeItem().getValue());
            entity.setJudgeItemValue(Math.pow(2, value));
        }

        //处理查询对象为投诉对象和投诉项目
        List<OrderComplain> list = complainDao.findComplainListNew(entity);

        Page<OrderComplain> rtnPage = new Page<>();
        rtnPage.setPageNo(page.getPageNo());
        rtnPage.setPageSize(page.getPageSize());
        rtnPage.setCount(page.getCount());
        rtnPage.setOrderBy(page.getOrderBy());
        if (list != null && list.size() > 0) {

            Supplier<Stream<OrderComplain>> streamSupplier = () -> list.stream();
            List<Long> createByIdList = streamSupplier.get().map(complain -> complain.getCreateBy().getId()).collect(Collectors.toList());
            Map<Long, String> nameMap = MSUserUtils.getNamesByUserIds(createByIdList);
            List<Long> customerIds = streamSupplier.get().map(t->t.getCustomer()).map(t-> t.getId()).collect(Collectors.toList());
            Map<Long,Customer> customerMap = msCustomerService.findCutomersWithIdsToMap(customerIds);

            List<Dict> status = MSDictUtils.getDictList("complain_status");//切换为微服务
            List<Dict> types = MSDictUtils.getDictList("complain_type");//切换为微服务
            List<Dict> complainObjects = MSDictUtils.getDictList("complain_object");//切换为微服务
            List<Dict> complainItems = MSDictUtils.getDictList("complain_item");//切换为微服务
            Map<String,Dict> orderStatusMap = MSDictUtils.getDictMap("order_status");//订单状态
            List<Dict> judgeObjects = null;
            List<Dict> judgeItems = Lists.newArrayList();
            List<Dict> completeResults = null;
            Map<Long,String> productCategoryMap = Maps.newHashMapWithExpectedSize(20);
            if(entity.getProductCategoryId() != null && entity.getProductCategoryId()>0){
                ProductCategory productCategory = productCategoryService.getFromCache(entity.getProductCategoryId());
                productCategoryMap.put(entity.getProductCategoryId(),productCategory==null?"":productCategory.getName());
                productCategory = null;
            }else{
                List<ProductCategory> productCategoryList = productCategoryService.findAllList();
                if(!org.springframework.util.ObjectUtils.isEmpty(productCategoryList)){
                    productCategoryMap.putAll(
                            productCategoryList.stream()
                                    .collect(Collectors.toMap(ProductCategory::getId,ProductCategory::getName))
                    );
                }
                productCategoryList = null;
            }
            OrderComplain complain;
            Dict dict;
            List<String> ids;
            List<Dict> dictList;
            Customer customer;
            Dict orderStatus;
            final StringBuffer buffer = new StringBuffer();
            for (int i = 0, len = list.size(); i < len; i++) {
                complain = list.get(i);
                if(complain.getProductCategoryId()>0 && productCategoryMap.containsKey(complain.getProductCategoryId())){
                    complain.setProductCategoryName(productCategoryMap.get(complain.getProductCategoryId()));
                }
                if (nameMap.get(complain.getCreateBy().getId()) != null) {
                    complain.getCreateBy().setName(nameMap.get(complain.getCreateBy().getId()));
                }
                customer = customerMap.get(complain.getCustomer().getId());
                //customer = customerService.getFromCache(complain.getCustomer().getId());
                if (customer != null) {
                    complain.setCustomer(customer);
                }
                //order status
                orderStatus = orderStatusMap.get(complain.getOrderStatus().getValue());
                if(orderStatus != null){
                    complain.getOrderStatus().setLabel(orderStatus.getLabel());
                }
                //status
                buffer.setLength(0);
                buffer.append(complain.getStatus().getValue());
                dict = status.stream().filter(t -> t.getValue().equalsIgnoreCase(buffer.toString())).findFirst().orElse(null);
                if (dict != null) {
                    complain.setStatus(dict);
                }
                //complain_type
                buffer.setLength(0);
                buffer.append(complain.getComplainType().getValue());
                dict = types.stream().filter(t -> t.getValue().equalsIgnoreCase(buffer.toString())).findFirst().orElse(null);
                if (dict != null) {
                    complain.setComplainType(dict);
                }
                //complain_object
                ids = BitUtils.getPositions(complain.getComplainObject(),String.class);
                if (ids.size() > 0) {
                    dictList = Lists.newArrayList();
                    for (int j = 0, jsize = ids.size(); j < jsize; j++) {
                        buffer.setLength(0);
                        buffer.append(ids.get(j));
                        dict = complainObjects.stream().filter(t -> t.getValue().equalsIgnoreCase(buffer.toString())).findFirst().orElse(null);
                        if (dict != null) {
                            dictList.add(dict);
                        }
                    }
                    complain.setComplainObjects(dictList);
                }
                //complain_item
                ids = BitUtils.getPositions(complain.getComplainItem(),String.class);
                if (ids.size() > 0) {
                    dictList = Lists.newArrayList();
                    for (int j = 0, jsize = ids.size(); j < jsize; j++) {
                        buffer.setLength(0);
                        buffer.append(ids.get(j));
                        dict = complainItems.stream().filter(t -> t.getValue().equalsIgnoreCase(buffer.toString())).findFirst().orElse(null);
                        if (dict != null) {
                            dictList.add(dict);
                        }
                    }
                    complain.setComplainItems(dictList);
                }
                //judge
                if (complain.getStatus().getValue().equalsIgnoreCase(OrderComplain.STATUS_PROCESSING.toString())
                        || complain.getStatus().getValue().equalsIgnoreCase(OrderComplain.STATUS_CLOSED.toString())
                        || complain.getStatus().getValue().equalsIgnoreCase(OrderComplain.STATUS_APPEAL.toString())) {
                    if (judgeObjects == null) {
                        judgeObjects = MSDictUtils.getDictList("judge_object");//切换为微服务
                        String[] objectValues = judgeObjects.stream().map(t -> t.getValue()).toArray(String[]::new);
                        judgeItems = MSDictUtils.getDictList("judge_item_", objectValues);//切换为微服务
                    }
                    if (judgeObjects != null) {
                        ids = BitUtils.getPositions(complain.getJudgeObject(),String.class);
                        if (ids.size() > 0) {
                            dictList = Lists.newArrayList();
                            for (int j = 0, jsize = ids.size(); j < jsize; j++) {
                                buffer.setLength(0);
                                buffer.append(ids.get(j));
                                dict = judgeObjects.stream().filter(t -> t.getValue().equalsIgnoreCase(buffer.toString())).findFirst().orElse(null);
                                if (dict != null) {
                                    dictList.add(dict);
                                }
                            }
                            complain.setJudgeObjects(dictList);
                        }
                    }
                    if (judgeItems != null) {
                        ids = BitUtils.getPositions(complain.getJudgeItem(),String.class);
                        if (ids.size() > 0) {
                            dictList = Lists.newArrayList();
                            for (int j = 0, jsize = ids.size(); j < jsize; j++) {
                                buffer.setLength(0);
                                buffer.append(ids.get(j));
                                dict = judgeItems.stream().filter(t -> t.getValue().equalsIgnoreCase(buffer.toString())).findFirst().orElse(null);
                                if (dict != null) {
                                    dictList.add(dict);
                                }
                            }
                            complain.setJudgeItems(dictList);
                        }
                    }
                }
                //结案
                if (complain.getStatus().getValue().equalsIgnoreCase(OrderComplain.STATUS_CLOSED.toString())
                        || complain.getStatus().getValue().equalsIgnoreCase(OrderComplain.STATUS_APPEAL.toString())) {
                    if (completeResults == null) {
                        completeResults = MSDictUtils.getDictList("complete_result");//切换为微服务
                    }
                    if (completeResults != null) {
                        ids = BitUtils.getPositions(complain.getCompleteResult(),String.class);
                        if (ids.size() > 0) {
                            dictList = Lists.newArrayList();
                            for (int j = 0, jsize = ids.size(); j < jsize; j++) {
                                buffer.setLength(0);
                                buffer.append(ids.get(j));
                                dict = completeResults.stream().filter(t -> t.getValue().equalsIgnoreCase(buffer.toString())).findFirst().orElse(null);
                                if (dict != null) {
                                    dictList.add(dict);
                                }
                            }
                            complain.setCompleteResults(dictList);
                        }
                    }
                }
            }
            rtnPage.setList(list);
        }
        return rtnPage;
    }

    /**
     * 开始处理投诉
     *
     * @param id
     * @param quarter
     * @param updateBy
     * @param updateDate
     */
    @Transactional(readOnly = false)
    public void acceptComplain(Long id, String quarter, User updateBy, Date updateDate,Long orderId) {
        HashMap<String, Object> maps = Maps.newHashMapWithExpectedSize(5);
        maps.put("id", id);
        maps.put("quarter", quarter);
        maps.put("status", OrderComplain.STATUS_PROCESSING);
        maps.put("updateBy", updateBy);
        maps.put("updateDate", updateDate);
        complainDao.UpdateOrderComplain(maps);

        maps.clear();
        maps.put("orderId",orderId);
        maps.put("quarter", quarter);
        maps.put("complainStatus",OrderComplain.STATUS_PROCESSING);
        orderService.updateComplainInfo(maps);

        OrderComplainLog orderComplainLog = new OrderComplainLog();
        orderComplainLog.setQuarter(quarter);
        orderComplainLog.setComplainId(id);
        orderComplainLog.setContent("开始处理投诉单");
        orderComplainLog.setCreateBy(updateBy);
        orderComplainLog.setCreateDate(updateDate);
        Dict status = new Dict(OrderComplain.STATUS_PROCESSING, "处理中");
        orderComplainLog.setStatus(status);
        saveOrderComplainLog(orderComplainLog);

        /*同步网点工单数据
        servicePointOrderBusinessService.relatedForm(
                orderId,
                quarter,
                0,
                OrderComplain.STATUS_PROCESSING.intValue(),
                0,
                updateBy.getId(),
                updateDate.getTime()
        );
        */
    }

    @Transactional(readOnly = false)
    public void saveOrderComplainAppoint(Long id, String quarter, Date date) {

        User user = UserUtils.getUser();

        HashMap<String, Object> maps = Maps.newHashMap();
        maps.put("id", id);
        maps.put("quarter", quarter);
        maps.put("appointDate", date);
        maps.put("updateBy", user);
        maps.put("updateDate", new Date());
        complainDao.UpdateOrderComplain(maps);

        OrderComplainLog orderComplainLog = new OrderComplainLog();
        orderComplainLog.setQuarter(quarter);
        orderComplainLog.setComplainId(id);
        orderComplainLog.setContent("投诉单设定跟进日期:" + DateUtils.formatDate(date, "yyyy-MM-dd HH:mm"));
        orderComplainLog.setCreateDate(new Date());
        orderComplainLog.setCreateBy(user);
        Dict status = new Dict(OrderComplain.STATUS_PROCESSING, "处理中");
        orderComplainLog.setStatus(status);
        saveOrderComplainLog(orderComplainLog);
                /*同步网点工单数据
        servicePointOrderBusinessService.relatedForm(
                orderId,
                quarter,
                0,
                OrderComplain.STATUS_PROCESSING.intValue(),
                0,
                updateBy.getId(),
                updateDate.getTime()
        );
        */
    }


    /**
     * 保存判定结果
     */
    @Transactional(readOnly = false)
    public void judgeComplain(OrderComplain complain) {

        User user = UserUtils.getUser();
        HashMap<String, Object> maps = Maps.newHashMap();
        maps.put("id", complain.getId());
        maps.put("quarter", complain.getQuarter());
        int intValue;
        int dictValue;
        Double doubleValue;
        Dict dict;
        List<String> items;

        /*更新判定附件
        List<OrderComplainAttachment> attachments = complain.getJudgeAttaches();
        if (attachments != null && attachments.size() > 0) {
            attachments = attachments.stream().filter(t -> StringUtils.isNotBlank(t.getFileName()))
                    .collect(Collectors.toList());
        }*/
        //责任对象
        intValue = 0;
        int isComplained = 1;
        items = complain.getJudgeObjectsIds();
        for (int i = 0, size = items.size(); i < size; i++) {
            dictValue = Integer.valueOf(items.get(i));
            if (dictValue == 1) {//网点
                isComplained = 2;
            }
            doubleValue = Math.pow(2, dictValue);
            intValue = intValue + doubleValue.intValue();
        }
        maps.put("judgeObject", intValue);
        //责任项目
        intValue = 0;
        items = complain.getJudgeItemsIds();
        for (int i = 0, size = items.size(); i < size; i++) {
            dictValue = Integer.valueOf(items.get(i));
            doubleValue = Math.pow(2, dictValue);
            intValue = intValue + doubleValue.intValue();
        }
        maps.put("judgeItem", intValue);

        maps.put("judgeRemark", complain.getJudgeRemark());
        //maps.put("judgeAttachmentQty", attachments.size());
        maps.put("judgeBy", complain.getJudgeBy());
        maps.put("judgeDate", complain.getJudgeDate());
        maps.put("updateBy", complain.getJudgeBy());
        maps.put("updateDate", complain.getJudgeDate());
        if (complain.getServicePoint() != null && complain.getServicePoint().getId() != null && complain.getServicePoint().getId() != 0) {
            maps.put("servicePoint", complain.getServicePoint());
        }
        complainDao.UpdateOrderComplain(maps);
        //order status
        maps.clear();
        maps.put("orderId", complain.getOrderId());
        maps.put("quarter", complain.getQuarter());
        maps.put("complainFlag", isComplained);
        orderService.updateComplainInfo(maps);

        /* 获取判定附件
        List<Long> attachIds = complainDao.getAttachmentIds(complain.getId(), complain.getQuarter(), OrderComplainAttachment.ATTACHMENTTYPE_JUDEG);
        OrderComplainAttachment attach;
        if (attachIds == null || attachIds.size() == 0) {
            //add
            if (attachments != null && attachments.size() > 0) {
                for (int i = 0, size = attachments.size(); i < size; i++) {
                    attach = attachments.get(i);
                    attach.setComplainId(complain.getId());
                    attach.setQuarter(complain.getQuarter());
                    attach.setCreateBy(user);
                    attach.setCreateDate(new Date());
                    attach.setAttachmentType(OrderComplainAttachment.ATTACHMENTTYPE_JUDEG);
                    complainDao.insertAttachement(attach);
                }
            }
        } else {
            List<String> ids = attachments.stream().map(t -> t.getStrId()).distinct().collect(Collectors.toList());
            //1.del
            for (int j = 0, size = attachIds.size(); j < size; j++) {
                if (!ids.contains(attachIds.get(j).toString())) {
                    complainDao.delAttachment(attachIds.get(j), complain.getQuarter(), complain.getUpdateBy(), complain.getUpdateDate());
                }
            }
            //2.add
            if (attachments != null && attachments.size() > 0) {
                for (int i = 0, size = attachments.size(); i < size; i++) {
                    attach = attachments.get(i);
                    //if(attach.getId() ==null || attach.getId()==0) {
                    if (StringUtils.isBlank(attach.getStrId()) && (attach.getId() == null || attach.getId() == 0)) {
                        attach.setComplainId(complain.getId());
                        attach.setQuarter(complain.getQuarter());
                        attach.setAttachmentType(OrderComplainAttachment.ATTACHMENTTYPE_JUDEG);
                        attach.setCreateBy(user);
                        attach.setCreateDate(new Date());
                        complainDao.insertAttachement(attach);
                    }
                }
            }
        }
        */
        //sync to b2b
        if(complain.getCreateType().equals(OrderComplain.CREATE_TYPE_B2B)){
            MQB2BOrderComplainProcessMessage.B2BOrderComplainProcessMessage message = MQB2BOrderComplainProcessMessage.B2BOrderComplainProcessMessage.newBuilder()
                    .setDataSource(complain.getDataSource())
                    .setKklComplainId(complain.getId())
                    .setKklComplainNo(complain.getComplainNo())
                    .setB2BComplainNo(complain.getB2bComplainNo())
                    .setOperationType(B2BOrderEnum.ComplainOperationTypeEnum.LOG.value) //处理日志
                    .setOperatorId(user.getId())
                    .setOperator(user.getName())
                    .setContent(complain.getJudgeRemark())
                    .setCreateAt(System.currentTimeMillis())
                    .build();
            b2BCenterOrderService.complainProcessProductor(message);
        }
        //Save Log
        OrderComplainLog orderComplainLog = new OrderComplainLog();
        orderComplainLog.setQuarter(complain.getQuarter());
        orderComplainLog.setComplainId(complain.getId());
        orderComplainLog.setStatus(new Dict(OrderComplain.STATUS_PROCESSING.toString(), "处理中"));
        orderComplainLog.setCreateBy(user);
        orderComplainLog.setCreateDate(new Date());
        orderComplainLog.setContent("完成投诉判责,判责意见：" + complain.getJudgeRemark());
        saveOrderComplainLog(orderComplainLog);

        /*同步网点工单数据
        servicePointOrderBusinessService.relatedForm(
                complain.getOrderId(),
                complain.getQuarter(),
                0,
                OrderComplain.STATUS_PROCESSING.intValue(),
                0,
                complain.getJudgeBy().getId(),
                complain.getJudgeDate().getTime()
        );
        */
    }

    @Transactional
    public long insertComplainAttachement(OrderComplainAttachment attach){
        long effectRows = complainDao.insertAttachement(attach);
        if(effectRows>0) {
            complainDao.incOrDescJudgeAttachQty(attach.getComplainId(), attach.getQuarter(), 1);
        }
        return effectRows;
    }

    /**
     * 保存结案
     */
    @Transactional(readOnly = false)
    public void completeComplain(OrderComplain complain,OrderComplain orgComplain,Integer orderComplainStatus) {

        User user = UserUtils.getUser();
        HashMap<String, Object> maps = Maps.newHashMap();
        maps.put("id", complain.getId());
        maps.put("quarter", complain.getQuarter());
        maps.put("status", OrderComplain.STATUS_CLOSED);
        maps.put("completeRemark", complain.getCompleteRemark());
        //处理方案
        int intValue;
        int dictValue;
        Double doubleValue;
        intValue = 0;
        List<String> items = complain.getCompleteResultIds();
        for (int i = 0, size = items.size(); i < size; i++) {
            dictValue = Integer.valueOf(items.get(i));
            doubleValue = Math.pow(2, dictValue);
            intValue = intValue + doubleValue.intValue();
        }
        maps.put("completeResult", intValue);

        //赔偿
        items = complain.getCompensateResultIds();
        int compensateResult = 0;
        if (items != null && items.size() > 0) {
            for (String value : items) {
                compensateResult = compensateResult + Integer.valueOf(value);
            }
        }
        maps.put("compensateResult", compensateResult);

        if (compensateResult == 1 || compensateResult == 3) {//厂商
            maps.put("customerAmount", complain.getCustomerAmount());
        } else {
            maps.put("customerAmount", 0.00);
        }

        if (compensateResult == 2 || compensateResult == 3) {//用户
            maps.put("userAmount", complain.getUserAmount());
        } else {
            maps.put("userAmount", 0.00);
        }
        //罚款
        List<String> amerceResultIds = complain.getAmerceResultIds();
        int amerceResult = 0;
        if (amerceResultIds != null && amerceResultIds.size() > 0) {
            for (String value : amerceResultIds) {
                amerceResult = amerceResult + Integer.valueOf(value);
            }
        }
        maps.put("amerceResult", amerceResult);
        if (amerceResult == 1 || amerceResult == 3) {//网点
            maps.put("servicePointAmount", complain.getServicePointAmount());
            //maps.put("servicePoint", complain.getServicePoint());
        } else {
            maps.put("servicePointAmount", 0.00);
            //maps.put("servicePoint", new ServicePoint(0l));
        }

        if (amerceResult == 2 || amerceResult == 3) {//客服
            maps.put("kefuAmount", complain.getKefuAmount());

        } else {
            maps.put("kefuAmount", 0.00);
        }

        maps.put("completeBy", complain.getCompleteBy());
        maps.put("completeDate", complain.getCompleteDate());
        maps.put("updateBy", complain.getCompleteBy());
        maps.put("updateDate", complain.getCompleteDate());
        complainDao.UpdateOrderComplain(maps);


        //更新投诉状态到order status
        maps.clear();
        maps.put("orderId",complain.getOrderId());
        maps.put("quarter",complain.getQuarter());
        if(B2BDataSourceEnum.isB2BDataSource(complain.getDataSource()) && orderComplainStatus != null && orderComplainStatus > -1){
            maps.put("complainStatus",orderComplainStatus);
        }else {
            maps.put("complainStatus", OrderComplain.STATUS_CLOSED);
        }
        orderService.updateComplainInfo(maps);


        //Save Log
        OrderComplainLog orderComplainLog = new OrderComplainLog();
        orderComplainLog.setQuarter(complain.getQuarter());
        orderComplainLog.setComplainId(complain.getId());
        orderComplainLog.setStatus(new Dict(OrderComplain.STATUS_CLOSED.toString(), "完成"));
        orderComplainLog.setCreateBy(user);
        orderComplainLog.setCreateDate(new Date());
        orderComplainLog.setContent("完成处理,处理意见：" + complain.getCompleteRemark());
        saveOrderComplainLog(orderComplainLog);

        //sync to b2b
        if(orgComplain.getCreateType().equals(OrderComplain.CREATE_TYPE_B2B)){
            MQB2BOrderComplainProcessMessage.B2BOrderComplainProcessMessage message = MQB2BOrderComplainProcessMessage.B2BOrderComplainProcessMessage.newBuilder()
                    .setDataSource(complain.getDataSource())
                    .setKklComplainId(complain.getId())
                    .setKklComplainNo(orgComplain.getComplainNo())
                    .setB2BComplainNo(orgComplain.getB2bComplainNo())
                    .setOperationType(B2BOrderEnum.ComplainOperationTypeEnum.CLOSE.value)
                    .setOperatorId(user.getId())
                    .setOperator(user.getName())
                    .setContent(complain.getCompleteRemark())
                    .setCreateAt(System.currentTimeMillis())
                    .build();
            b2BCenterOrderService.complainProcessProductor(message);
        }
        RPTOrderComplainModel orderComplainModel = new RPTOrderComplainModel();
        orderComplainModel.setId(orgComplain.getId());
        if(orgComplain.getComplainDate() != null){
            orderComplainModel.setComplainDt(orgComplain.getComplainDate().getTime());
        }
        orderComplainModel.setJudgeItem(orgComplain.getJudgeItem());
        orderComplainModel.setJudgeObject(orgComplain.getJudgeObject());
        orderComplainModel.setStatus(orgComplain.getStatus().getIntValue());
        orderComplainService.sendRPTOrderComplain(orderComplainModel);

    }

    /**
     * 读取日志
     */
    public List<OrderComplainLog> getComplainLogListByCompliaId(Long complainId, String quarter) {
        return complainDao.findListByComplainId(complainId, quarter);
    }

    /**
     * 保存申诉
     *
     * @param entity
     */
    @Transactional(readOnly = false)
    public void saveAppeal(OrderComplainLog entity) {
        Date appealDate = new Date();
        complainDao.updateComplainAppeal(entity.getComplainId(), entity.getQuarter(), entity.getStatus(), entity.getContent(), entity.getCreateBy(), appealDate);
        OrderComplain complain = getComplain(entity.getComplainId(), entity.getQuarter());
        //更新 order status
        HashMap<String,Object> map = Maps.newHashMap();
        map.put("orderId",complain.getOrderId());
        map.put("quarter",entity.getQuarter());
        map.put("complainStatus",OrderComplain.STATUS_APPEAL);
        orderService.updateComplainInfo(map);
        entity.setContent("申诉,申诉内容：" + entity.getContent());
        saveOrderComplainLog(entity);

        /*同步网点工单数据
        servicePointOrderBusinessService.relatedForm(
                complain.getOrderId(),
                complain.getQuarter(),
                0,
                OrderComplain.STATUS_APPEAL.intValue(),
                0,
                entity.getCreateBy().getId(),
                appealDate.getTime()
        );
        */
    }

    /**
     * 新增日志
     *
     * @param entity
     */
    @Transactional(readOnly = false)
    public void addOrderComplainLog(OrderComplainLog entity) {
        saveOrderComplainLog(entity);
    }

    @Transactional(readOnly = false)
    public void saveOrderComplainLog(OrderComplainLog entity) {
        if (entity.getVisibilityFlag() == null || entity.getVisibilityFlag() == VisibilityFlagEnum.NONE.getValue()) {
            entity.setVisibilityFlag(OrderComplainLog.VISIBILITY_FLAG_ALL);
        }
        entity.setContent(StringUtils.left(entity.getContent(), 520));
        complainDao.insertLog(entity);

    }

    /**
     * 撤销投诉单
     *
     * @param lcomplainId   投诉单id
     * @param quarter   分片
     * @param user  取消人
     */
    @Transactional(readOnly = false)
    public void cancleComplain(Long lcomplainId, String quarter,User user) throws Exception {

        OrderComplain complain = getComplain(lcomplainId, quarter);
        if (complain.getStatus().getValue().equalsIgnoreCase(OrderComplain.STATUS_APPLIED.toString())) {
            complainDao.cancleComplain(lcomplainId, quarter);

            //更新order status状态
            HashMap<String, Object> map = Maps.newHashMap();
            map.put("orderId",complain.getOrderId());
            map.put("quarter",quarter);
            map.put("complainStatus",OrderComplain.STATUS_CANCEL);
            orderService.updateComplainInfo(map);

            OrderComplainLog orderComplainLog = new OrderComplainLog();
            orderComplainLog.setQuarter(quarter);
            orderComplainLog.setComplainId(lcomplainId);
            Dict status = new Dict();
            status.setValue(OrderComplain.STATUS_CANCEL.toString());
            orderComplainLog.setContent("撤销投诉单");
            orderComplainLog.setStatus(status);
            orderComplainLog.setCreateBy(user);
            Date createDate = new Date();
            orderComplainLog.setCreateDate(createDate);
            saveOrderComplainLog(orderComplainLog);

            //撤销投诉，同步网点工单数据
            servicePointOrderBusinessService.relatedForm(
                    complain.getOrderId(),
                    quarter,
                    0,
                    -1,//撤销
                    0,
                    user.getId(),
                    createDate.getTime()
            );

        } else {
            throw new OrderException("该投诉单不允许撤销，请检查投诉单状态");
        }

        RPTOrderComplainModel orderComplainModel = new RPTOrderComplainModel();
        orderComplainModel.setId(complain.getId());
        orderComplainModel.setStatus(OrderComplain.STATUS_CANCEL);
        orderComplainService.sendRPTOrderComplain(orderComplainModel);
    }

    /**
     * 根据id读取投诉单状态,分片
     * @param id
     */
     public OrderComplain getOrderComplainById(Long id){
         return complainDao.getOrderComplainById(id);
     }

    /**
     * 第三方通知关闭投诉单
     * @param orderComplain
     */
    @Transactional()
     public void closeComplainByB2B(OrderComplain orderComplain){
         OrderComplain orgComplain = getComplain(orderComplain.getId(),orderComplain.getQuarter());
         HashMap<String, Object> maps = Maps.newHashMap();
         maps.put("id", orderComplain.getId());
         maps.put("quarter", orderComplain.getQuarter());
         maps.put("status", OrderComplain.STATUS_CLOSED);
         maps.put("completeRemark", orderComplain.getCompleteRemark());
         maps.put("completeBy", orderComplain.getCompleteBy());
         maps.put("completeDate", orderComplain.getCompleteDate());
         maps.put("updateBy", orderComplain.getCompleteBy());
         maps.put("updateDate", orderComplain.getCompleteDate());
         complainDao.UpdateOrderComplain(maps);
        Integer minStatus = null;
        List<LongTwoTuple> complainStatus =getStatusByOrder(orderComplain.getOrderId(),orderComplain.getQuarter());
        if(!CollectionUtils.isEmpty(complainStatus) && complainStatus.size() > 1) {
            //筛选状态：0-待处理，1-处理中，3-申诉
            List<Long> statusRange = Lists.newArrayList(0L, 1L, 3L);
            minStatus = complainStatus.stream().filter(t -> statusRange.contains(t.getBElement()) && !t.getAElement().equals(orderComplain.getId())).map(t -> t.getBElement().intValue()).min(Integer::compareTo).orElse(null);
        }
         //更新投诉状态到order status
         maps.clear();
         maps.put("orderId",orderComplain.getOrderId());
         maps.put("quarter",orderComplain.getQuarter());
         if(minStatus != null && minStatus > -1){
             maps.put("complainStatus",minStatus);
         }else {
             maps.put("complainStatus", OrderComplain.STATUS_CLOSED);
         }
         orderService.updateComplainInfo(maps);

        //Save Log
        OrderComplainLog orderComplainLog = new OrderComplainLog();
        orderComplainLog.setQuarter(orderComplain.getQuarter());
        orderComplainLog.setComplainId(orderComplain.getId());
        orderComplainLog.setStatus(new Dict(OrderComplain.STATUS_CLOSED.toString(), "完成"));
        orderComplainLog.setCreateBy(orderComplain.getCompleteBy());
        orderComplainLog.setCreateDate(orderComplain.getCompleteDate());
        orderComplainLog.setContent(orderComplain.getCompleteRemark());
        saveOrderComplainLog(orderComplainLog);
        if(orgComplain!=null){
            RPTOrderComplainModel orderComplainModel = new RPTOrderComplainModel();
            orderComplainModel.setId(orgComplain.getId());
            if(orgComplain.getComplainDate() != null){
                orderComplainModel.setComplainDt(orgComplain.getComplainDate().getTime());
            }
            orderComplainModel.setJudgeItem(orgComplain.getJudgeItem());
            orderComplainModel.setJudgeObject(orgComplain.getJudgeObject());
            orderComplainModel.setStatus(orgComplain.getStatus().getIntValue());
            orderComplainService.sendRPTOrderComplain(orderComplainModel);
        }
     }


}
