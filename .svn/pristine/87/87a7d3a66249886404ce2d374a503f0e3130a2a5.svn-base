package com.wolfking.jeesite.test.sd;

import cn.hutool.core.math.MathUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.google.protobuf.InvalidProtocolBufferException;
import com.googlecode.protobuf.format.JsonFormat;
import com.kkl.kklplus.entity.common.NameValuePair;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.utils.GsonUtils;
import com.wolfking.jeesite.modules.fi.entity.CustomerCurrency;
import com.wolfking.jeesite.modules.fi.service.ChargeServiceNew;
import com.wolfking.jeesite.modules.fi.service.CustomerCurrencyService;
import com.wolfking.jeesite.modules.md.entity.Product;
import com.wolfking.jeesite.modules.md.entity.ServiceType;
import com.wolfking.jeesite.modules.mq.dto.MQCreateOrderPushMessage;
import com.wolfking.jeesite.modules.mq.sender.CreateOrderPushMessageSender;
import com.wolfking.jeesite.modules.mq.service.OrderCreateMessageService;
import com.wolfking.jeesite.modules.sd.dao.OrderDao;
import com.wolfking.jeesite.modules.sd.dao.OrderHeadDao;
import com.wolfking.jeesite.modules.sd.entity.*;
import com.wolfking.jeesite.modules.sd.entity.dto.OrderPbDto;
import com.wolfking.jeesite.modules.sd.service.OrderAdditionalInfoService;
import com.wolfking.jeesite.modules.sd.service.OrderItemService;
import com.wolfking.jeesite.modules.sd.service.OrderService;
import com.wolfking.jeesite.modules.sd.utils.OrderAdditionalInfoUtils;
import com.wolfking.jeesite.modules.sd.utils.OrderItemUtils;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.ms.b2bcenter.sd.dao.B2BOrderDao;
import com.wolfking.jeesite.ms.b2bcenter.sd.service.B2BOrderBaseService;
import com.wolfking.jeesite.ms.canbo.rpt.dao.CanboProcessLogDao;
import com.wolfking.jeesite.ms.tmall.rpt.dao.B2BOrderInfoDao;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 订单数据处理.
 */
//@RunWith(SpringRunner.class)
//@SpringBootTest
//@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@Slf4j
@SpringBootTest
@ActiveProfiles("dev")
public class OrderUpdate {

    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderDao    orderDao;

    @Autowired
    private OrderAdditionalInfoService orderAdditionalInfoService;

    @Autowired
    private OrderCreateMessageService createMessageService;

    @Autowired
    private CreateOrderPushMessageSender createOrderPushMessageSender;

    @Autowired
    private CustomerCurrencyService customerCurrencyService;

    @Autowired
    private OrderHeadDao orderHeadDao;

    @Autowired
    private CanboProcessLogDao canboProcessLogDao;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private B2BOrderInfoDao b2BOrderInfoDao;

    @Autowired
    private B2BOrderDao b2BOrderDao;

    @Autowired
    private B2BOrderBaseService b2BOrderBaseService;



    //region old

    @Test
    public void customerChageOnlineFail(){
        String tradeNo = "R2018020600053";
        CustomerCurrency customerCurrency = customerCurrencyService.getByCurrencyNo(tradeNo, CustomerCurrency.ACTION_TYPE_TEMPRECHARGE);
        if (customerCurrency == null) {//流水单不存在
            System.out.println("支付宝异步页面：充值失败，读取单据错误");
        } else if (customerCurrency.getActionType().intValue() == CustomerCurrency.ACTION_TYPE_CHARGEONLINE
                || customerCurrency.getActionType().intValue() == CustomerCurrency.ACTION_TYPE_TEMPRECHARGE2) {
            //已处理
            System.out.println("支付宝异步页面：已成功，此次是重复通知");
        } else if (customerCurrency.getActionType().intValue() == CustomerCurrency.ACTION_TYPE_TEMPRECHARGE) {
            // 1.更新原记录状态
            // 2.新增真正的充值记录
            // 3.更新客户账户余额
            customerCurrencyService.updateEntity(customerCurrency);

            System.out.println("支付宝异步页面：充值成功");
        } else {

            System.out.println("支付宝异步页面：充值失败");
        }
    }
    /**
     * 重置客服
     2019/08/29 ryan
    @Test
    public void resetKefu() throws InterruptedException {

        List<Map<String, Object>> list = orderDao.getResetOrderList();

        System.out.println("order count:" + list.size());
        if(list != null && list.size()>0) {
            Long orderId;
            Long cId;
            Long areaId;
            String orderNo;
            User kefu;
            System.out.println(StringUtils.leftPad("",100,">"));
            for (int i = 0, len = list.size(); i < len; i++) {
                orderId = (Long) list.get(i).get("order_id");
                orderNo = (String) list.get(i).get("order_no");
                cId = (Long) list.get(i).get("customer_id");
                areaId = (Long) list.get(i).get("area_id");
                System.out.println(String.format(">>>order:%s ,cid:%s ,area:%s",orderId,cId,areaId));
                if(cId==null || cId.longValue()<=0){
                    System.out.println("> 客户ID错误");
                }else if(areaId == null || areaId.longValue()<=0){
                    System.out.println("> 区域ID错误");
                }else{
                    try {
                        kefu = orderService.getRandomKefu(cId,areaId);
                        if(kefu==null){
                            System.out.println("> 未发现客服");
                        }else{
                            System.out.println("> 新客服:".concat(kefu.getName()).concat(",id:").concat(kefu.getId().toString()));
                            orderService.resetKefu(orderId,kefu.getId());
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }

                //Thread.sleep(500);
            }
            System.out.println(StringUtils.leftPad("",100,">"));
        }

    }*/

    @Test
    public void reCreateOrderProcessLog(){
//        List<OrderCreateBody> list =  createMessageService.getResendList(60L);
//        MQCreateOrderPushMessage.CreateOrderPushMessage.Builder builder;
//        if(list != null && list.size()>0){
//            OrderCreateBody model;
//            builder = MQCreateOrderPushMessage.CreateOrderPushMessage.newBuilder();
//            try {
//                for (int i = 0, size = list.size(); i < size; i++) {
//                    model = list.get(i);
//                    log.error(">>> " + model.getOrderId());
//                    builder.setId(model.getId())
//                            .setOrderId(model.getOrderId())
//                            .setQuarter(model.getQuarter())
//                            .setTriggerBy(MQCreateOrderPushMessage.TriggerBy.newBuilder()
//                                    .setId(model.getTriggerBy().getId())
//                                    .setName(model.getTriggerBy().getName())
//                                    .build())
//                            .setTriggerDate(model.getTriggerDate().getTime());
//                    createOrderPushMessageSender.send(builder.build());
//                    try {
//                        Thread.sleep(200);
//                    } catch (Exception e) {
//                    }
//                }
//            }catch (Exception e){
//                e.printStackTrace();
//            }
//        }
    }

    //重新统计下单时的统计报表数据
    //1.客户日下单报表
    //2.业务业绩报表
    //3.区域下单报表
    //4.客服每日报表
    @Test
    public void reSetOrderCreateReportByOrderIds(){
        String quarter = "20182";
        List<Long> ids = Lists.newArrayList();
        ids.add(1006498489624444928l);

        List<OrderCondition> list = createMessageService.getReportResendMessageByOrderIds(quarter,ids);
        MQCreateOrderPushMessage.CreateOrderPushMessage orderPushMessage;
        OrderCondition model;
        try {
            for (int i = 0, size = list.size(); i < size; i++) {
                model = list.get(i);
                log.error(">>> " + model.getOrderId());
                orderPushMessage = MQCreateOrderPushMessage.CreateOrderPushMessage.newBuilder()
                        .setOrderId(model.getOrderId())
                        .setQuarter(model.getQuarter())
                        .setTriggerBy(MQCreateOrderPushMessage.TriggerBy.newBuilder()
                                .setId(model.getCreateBy().getId())
                                .setName(model.getCreateBy().getName())
                                .build())
                        .setTriggerDate(model.getCreateDate().getTime())
                        .build();
                createOrderPushMessageSender.send(orderPushMessage);
                try {
                    Thread.sleep(200);
                } catch (Exception e) {
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Test
    /**
     * 测试获得随机客户
     */
    public void testGetRandomKefu() {
        //1.有客户
        //'15','mike','兰州市','479','1058'
        User kefu = orderService.getRandomKefu(1058l,479l,1L,0,0L,0L);
        Assert.assertNotNull(kefu);
        System.out.println("kefu:" + kefu.getId() + " name:" + kefu.getName());
        //2.无客户，有区域
        //'74','普通客服','高明区','3219'
        kefu = orderService.getRandomKefu(2l,3219l,1L,0,0L,0L);
        Assert.assertNotNull(kefu);
        System.out.println("kefu:" + kefu.getId() + " name:" + kefu.getName());
        //3.无客服
        kefu = orderService.getRandomKefu(3l,4528l,1L,0,0L,0L);
        Assert.assertNotNull(kefu);
    }

    /**
     * 自动对账队列处理失败，手动对账
     * fi部分处理改成消息队列处理，要链接生产的消息队列

    @Test
    public void manuAutoCharge(){
        Date startDate = null;
        Date endDate = null;
        List<OrderCharge> list = null;
        try {
            startDate = DateUtils.parse("2018-06-23 00:00:00", "yyyy-MM-dd HH:mm:ss");
            endDate = DateUtils.parse("2018-06-23 23:59:59.999", "yyyy-MM-dd HH:mm:ss.SSS");
        } catch (ParseException e) {
            e.printStackTrace();
            return;
        }
        list = orderChargeService.selectRetryList(startDate,endDate,100);
        Assert.assertNotNull(list);
        //Assert.assertEquals(list.size() , 1);
        Assert.assertNotEquals(list.size(),0);
        for(OrderCharge order:list) {
            order.setRetryTimes(1);
            order.setRemarks("");
            try {
               chargeServiceNew.createCharge(order.getOrderId(), order.getTriggerBy());
               order.setStatus(30);
               orderChargeService.save(order);
               System.out.println(">>>PASS>>>" + order.getId().toString());
            } catch (Exception e) {
                e.printStackTrace();
                order.setStatus(40);
                order.setRemarks(e.getMessage());
                orderChargeService.save(order);
                System.out.println(">>>FAIL>>>" + order.getId().toString());
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
     */

    /**
     * 补时效上线前sd_order_servicepoint_fee
     */
    @Test
    public void reCreateOrderServicePointFee(){
        List<HashMap<String, Object>> list = orderDao.getReCreateOrderServicePointFee("20181","2018-01-01");
        Long orderId;
        String quarter = new String("");
        Order order;
        OrderServicePointFee spFee;
        List<OrderDetail> details;
        List<OrderPlan> plans;
        OrderPlan plan;
        OrderDetail servicePointFeeSum;
        Set<Long> sids;
        for (HashMap<String, Object> map : list){
            try {
                Thread.sleep(200);
            }catch (Exception e){

            }
            orderId = Long.parseLong(map.get("order_id").toString());
            quarter = (String)map.get("quarter");
            log.info(">>>orderId:{} quarter:{}",orderId,quarter);
            //order = orderService.getOrderById(orderId,quarter, OrderUtils.OrderDataLevel.DETAIL)
            plans = orderService.getOrderPlanList(orderId,quarter,1);
            details = orderService.getOrderDetails(orderId,quarter,false);
            if(details == null || details.size() == 0){
                for(int i=0,size=plans.size();i<size;i++){
                    plan = plans.get(i);
                    spFee = orderDao.getOrderServicePointFee(quarter,orderId,plan.getServicePoint().getId());
                    if(spFee == null) {
                        spFee = new OrderServicePointFee();
                        spFee.setOrderId(orderId);
                        spFee.setQuarter(quarter);
                        spFee.setServicePoint(plan.getServicePoint());
                        orderDao.insertOrderServicePointFee(spFee);
                    }
                }
            }else{
                for(int i=0,size=plans.size();i<size;i++){
                    plan = plans.get(i);
                    sids = details.stream().map(t->t.getServicePoint().getId()).distinct().collect(Collectors.toSet());
                //for(int j=0,jsize=sids.size();j<jsize;j++) {
                    final Long sid = plan.getServicePoint().getId();
                    servicePointFeeSum = details.stream().filter(t -> t.getServicePoint().getId().longValue() == sid.longValue() && t.getDelFlag() == OrderDetail.DEL_FLAG_NORMAL)
                            .reduce(new OrderDetail(), (item1, item2) -> {
                                return new OrderDetail(
                                        item1.getEngineerServiceCharge() + item2.getEngineerServiceCharge(),
                                        item1.getEngineerTravelCharge() + item2.getEngineerTravelCharge(),
                                        item1.getEngineerExpressCharge() + item2.getEngineerExpressCharge(),
                                        item1.getEngineerMaterialCharge() + item2.getEngineerMaterialCharge(),
                                        item1.getEngineerOtherCharge() + item2.getEngineerOtherCharge()
                                );
                            });
                    spFee = orderDao.getOrderServicePointFee(quarter,orderId,sid);
                    if(spFee == null) {
                        spFee = new OrderServicePointFee();
                        spFee.setOrderId(orderId);
                        spFee.setQuarter(quarter);
                        spFee.setServicePoint(plan.getServicePoint());
                        if (servicePointFeeSum != null) {
                            spFee.setServiceCharge(servicePointFeeSum.getEngineerServiceCharge());
                            spFee.setTravelCharge(servicePointFeeSum.getEngineerTravelCharge());
                            spFee.setExpressCharge(servicePointFeeSum.getEngineerExpressCharge());
                            spFee.setMaterialCharge(servicePointFeeSum.getEngineerMaterialCharge());
                            spFee.setOtherCharge(servicePointFeeSum.getEngineerOtherCharge());
                            spFee.setOrderCharge(spFee.getServiceCharge() + spFee.getTravelCharge() + spFee.getExpressCharge() + spFee.getMaterialCharge() + spFee.getOtherCharge());
                        }
                        if (sids.contains(sid)) {
                            spFee.setDelFlag(0);
                        } else {
                            spFee.setDelFlag(1);
                        }
                        orderDao.insertOrderServicePointFee(spFee);
                    }
                }
            }
        }

    }


    /**
     * 检查订单费用是否一致
     */
    @Test
    public void checkOrderFeeAndServiceAmount(){
        List<Long> ids = Lists.newArrayList(995865557423230976l,996229851184762880l,996709234630729728l);
        Long orderId;
        String quarter =    "20182";
        Order order;
        Boolean result;
        /*
        for(int i=0,size=ids.size();i<size;i++){
            orderId = ids.get(i);
            order = orderService.getOrderById(orderId,quarter, OrderUtils.OrderDataLevel.DETAIL,true);
            result = orderService.checkOrderFeeAndServiceAmount(order,true);
            log.info(">>>order:{} result:{}",orderId,result);
            try{
                Thread.sleep(300);
            }catch (Exception e){}
        }*/
        orderId = 1276804286139076629l;
        order = orderService.getOrderById(orderId, "", OrderUtils.OrderDataLevel.DETAIL, true, true);
        result = orderService.checkOrderFeeAndServiceAmount(order,false);
        log.error(">>>order:{} result:{}",orderId,result);
    }

    /**
     * 检查是否可以自动完工
     */
    @Test
    public void checkAutoComplete(){
        List<Long> ids = Lists.newArrayList(995865557423230976l,996229851184762880l,996709234630729728l);
        Long orderId;
        String quarter =    "20182";
        Order order;
        String msg = new String("");
        for(int i=0,size=ids.size();i<size;i++){
            orderId = ids.get(i);
            order = orderService.getOrderById(orderId,quarter, OrderUtils.OrderDataLevel.DETAIL,true);
            msg = orderService.checkAutoComplete(order);
            log.info(">>>order:{} return message:{}",orderId,msg);
            try{
                Thread.sleep(300);
            }catch (Exception e){}
        }
    }

    @Test
    public void reSendOrderCreatePushMessage(){
        MQCreateOrderPushMessage.CreateOrderPushMessage orderPushMessage = MQCreateOrderPushMessage.CreateOrderPushMessage.newBuilder()
                .setOrderId(1023809726888423424l)
                .setQuarter("20183")
                .setTriggerBy(MQCreateOrderPushMessage.TriggerBy.newBuilder()
                        .setId(20846)
                        .setName("602成都前锋")
                        .build())
                .setTriggerDate(1532930235000l)
                .build();
            createOrderPushMessageSender.send(orderPushMessage);
    }

    //endregion old

    //region 新增sd_order_head表数据转换

    /*
    @Test
    public void transfer(){
        Long start = System.currentTimeMillis();
        Long lastPageRecordId = 0L;
        Long maxId = 1335947212609560577L;
        String quarter = "20204";
        //Date closeDate = DateTime.parse("2020-12-01").toDate();
        Date closeDate = null;
        List<Order> orders;
        Integer pageSize = 2000;
        Long currId = 0L;
        while (true) {
            orders = orderHeadDao.getOrderds(lastPageRecordId, quarter, pageSize,closeDate);
            currId = transferOrderHead(orders);
            if(CollectionUtils.isEmpty(orders) || orders.size() < pageSize  || currId >= maxId){
                break;
            }else{
                lastPageRecordId = currId;
                System.out.println("最后一笔ID：" + lastPageRecordId);
            }
        }
        Long time = System.currentTimeMillis() - start;
        time = time /1000;
        System.out.println("用时:" + time + " 秒");
    }

    private Long transferOrderHead(List<Order> orders){
        List<OrderItem> items;
        OrderPbDto.OrderInfo info;
        OrderPbDto.OrderItemList itemList;
        Long currId = null;
        List<Order> batchList = Lists.newArrayListWithCapacity(orders.size());
        for (Order order : orders) {
            currId = order.getId();
            try{
                items = OrderItemUtils.fromOrderItemsJson(order.getOrderItemJson());
                if(CollectionUtils.isEmpty(items)){
                    order.setItemsPb(null);
                }else{
                    itemList = OrderItemUtils.ItemsToPb(items);
                    if(itemList != null && itemList.getItemCount() > 0){
                        order.setItemsPb(itemList.toByteArray());
                    }
                }

                info = OrderAdditionalInfoToPbBuilder(order.getOrderInfo());
                if(info != null && StrUtil.isNotBlank(info.toString())){
                    order.setAdditionalInfoPb(info.toByteArray());
                }else{
                    order.setAdditionalInfoPb(null);
                }
                batchList.add(order);
            }catch (Exception e){
                log.error("transfer error,id: {} quarter: {}",order.getId(),order.getQuarter(),e);
            }
        }
        //batch insert
        List<List<Order>> parts = Lists.partition(batchList,100);
        parts.stream().forEach(list->{
            orderHeadDao.batchInsert(list);
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        return currId;
    }
    */

    private OrderPbDto.OrderInfo OrderAdditionalInfoToPbBuilder(String json){
        if(StrUtil.isBlank(json)){
            return null;
        }
        OrderAdditionalInfo orderInfo  = OrderAdditionalInfoUtils.fromOrderAdditionalInfoJson(json);
        if(orderInfo == null) {
            return null;
        }
        OrderPbDto.OrderInfo.Builder builder = OrderPbDto.OrderInfo.newBuilder()
                .setEstimatedReceiveDate(orderInfo.getEstimatedReceiveDate())
                .setExpectServiceTime(orderInfo.getExpectServiceTime())
                .setBuyDate(orderInfo.getBuyDate()==null?0:orderInfo.getBuyDate());
        return builder.build();
    }

    @Test
    public void getFromOrderHeadById() throws InvalidProtocolBufferException {
        Long id = 1314485318283563008L;
        String quarter = "20204";
        Order order = orderHeadDao.getOrderById(id,quarter);
        Assert.assertNotNull(order);
        Assert.assertNotNull(order.getItemsPb());
        OrderPbDto.OrderItemList itemList = OrderPbDto.OrderItemList.parseFrom(order.getItemsPb());
        log.warn("pb item to json:{}",new JsonFormat().printToString(itemList));
        List<OrderItem> items = OrderItemUtils.pbToItems(order.getItemsPb());
        log.warn("items json:{}",OrderItemUtils.toOrderItemsJson(items));
        Assert.assertNotNull(order.getAdditionalInfoPb());
        OrderPbDto.OrderInfo orderInfo = OrderPbDto.OrderInfo.parseFrom(order.getAdditionalInfoPb());
        log.warn("pb additional info to pb json:{}",new JsonFormat().printToString(orderInfo));

        OrderAdditionalInfo additionalInfo = OrderAdditionalInfoUtils.pbBypesToAdditionalInfo(order.getAdditionalInfoPb());
        log.warn("pb additional info to json:{}", GsonUtils.getInstance().toGson(additionalInfo));
    }

    @Test
    public void testUpdateOrderAdditionalInfo() throws InvalidProtocolBufferException {
        Long orderId = 1314485318283563008L;
        String quarter="20204";
        //update
        //OrderAdditionalInfo orderInfo = new OrderAdditionalInfo();
        //orderInfo.setEstimatedReceiveDate("2020年10月16日");
        //orderInfo.setBuyDate(1601892282000L);
        //orderInfo.setExpectServiceTime("2020-10-31 11:00:00");
        //{"estimatedReceiveDate":"2020年10月16日","buyDate":1601892282000,"expectServiceTime":"2020-10-31 11:00:00"}
        orderAdditionalInfoService.updateBuyDate(orderId,quarter,1601546682000L);

        Order order = orderHeadDao.getOrderById(orderId,quarter);
        Assert.assertNotNull(order);
        Assert.assertNotNull(order.getItemsPb());
        OrderPbDto.OrderItemList itemList = OrderPbDto.OrderItemList.parseFrom(order.getItemsPb());
        log.warn("pb item to json:{}",new JsonFormat().printToString(itemList));
        List<OrderItem> items = OrderItemUtils.pbToItems(order.getItemsPb());
        log.warn("items json:{}",OrderItemUtils.toOrderItemsJson(items));
        Assert.assertNotNull(order.getAdditionalInfoPb());
        OrderPbDto.OrderInfo orderInfo = OrderPbDto.OrderInfo.parseFrom(order.getAdditionalInfoPb());
        log.warn("pb additional info to pb json:{}",new JsonFormat().printToString(orderInfo));

        OrderAdditionalInfo additionalInfo = OrderAdditionalInfoUtils.pbBypesToAdditionalInfo(order.getAdditionalInfoPb());
        log.warn("pb additional info to json:{}", GsonUtils.getInstance().toGson(additionalInfo));
    }

    //endregion

    //region sd_order -> sd_order_head

    //region CanboProcessLogDao

    @Test
    public void getOrderInfoByCanbo(){
        System.out.println("CanboProcessLogDao.getOrderInfoByCanbo");
        HashMap<String, Object> orderInfoByCanbo = canboProcessLogDao.getOrderInfoByCanbo(6, "0E4A0B0B83AA1EDAA0F3E86457140951");
        System.out.println("orderId: " + String.valueOf(orderInfoByCanbo.get("orderId")));
        System.out.println("kklOrderNo: " + String.valueOf(orderInfoByCanbo.get("kklOrderNo")));
        System.out.println("quarter: " + String.valueOf(orderInfoByCanbo.get("quarter")));
    }

    //endregion CanboProcessLogDao

    //region OrderItemDao

    @Test
    public void getOrderItems(){
        Order order = orderItemService.getOrderItems("20204", 1339136192079335424L);
        List<OrderItem> items = order.getItems();
        System.out.println("items: " + GsonUtils.getInstance().toGson(items));

        int qty = orderItemService.getProductQty(items,75L);
        System.out.println("qty: " + qty);
    }

    //endregion B2BOrderInfoDao

    //region

    @Test
    public void getOrderIdQuarter(){
        Map<String, Object> orderIdQuarter = b2BOrderInfoDao.getOrderIdQuarter("0E4A0B0B83AA1EDAA0F3E86457140951");
        System.out.println("id: " + String.valueOf(orderIdQuarter.get("id")));
        System.out.println("quarter: " + String.valueOf(orderIdQuarter.get("quarter")));

    }


    //endregion

    //region B2BOrderDao

    @Test
    public void getOrderAdditionalInfo(){
        OrderAdditionalInfo orderAdditionalInfo = null;
        Order order = b2BOrderDao.getOrderAdditionalInfo(1314485318283563008L, "20204");
        if(order != null && order.getAdditionalInfoPb() != null && order.getAdditionalInfoPb().length > 0){
            orderAdditionalInfo = OrderAdditionalInfoUtils.pbBypesToAdditionalInfo(order.getAdditionalInfoPb());
        }else{
            System.out.println("orderAdditionalInfo: null");
        }
        if(orderAdditionalInfo != null){
            System.out.println("orderAdditionalInfo: " + GsonUtils.getInstance().toGson(orderAdditionalInfo));
        }else{
            System.out.println("orderAdditionalInfo: null");
        }
    }

    //endregion

    //region OrderAdditionalInfoService

    @Test
    public void getBuyDate(){
        Long buyDate = orderAdditionalInfoService.getBuyDate(1314485318283563008L, "20204");
        System.out.println("buyDate: " + String.valueOf(buyDate));//should be 1601892282000
    }

    //endregion

    //region orderHeadDao

    @Test
    public void getOrderQuarter(){
        String quarter = orderHeadDao.getOrderQuarter(1314485318283563008L);
        System.out.println("quarter: " + quarter);
        Assert.assertTrue("返回分片错误","20204".equalsIgnoreCase(quarter));
    }

    // getOrderById
    @Test
    public void getOrderByIdMS(){
        Order order = orderService.getOrderByIdMS(1314485318283563008L,"20204");
        Assert.assertNotNull("返回无订单示例",order);
        List<OrderItem> items = order.getItems();
        Assert.assertNotNull("无订单项目",items);
        System.out.println("items: " + GsonUtils.getInstance().toGson(items));
        System.out.println("addition: " + GsonUtils.getInstance().toGson(order.getOrderAdditionalInfo()));
    }

    // getOrderFromMasterById
    @Test
    public void getOrderFromMasterById(){
        Order order = orderService.getOrderFromMasterDb(1314485318283563008L,"20204");
        Assert.assertNotNull("返回无订单示例",order);
        List<OrderItem> items = order.getItems();
        Assert.assertNotNull("无订单项目",items);
        System.out.println("items: " + GsonUtils.getInstance().toGson(items));
        System.out.println("addition: " + GsonUtils.getInstance().toGson(order.getOrderAdditionalInfo()));
    }

    // getB2BOrderNo
    @Test
    public void getB2BOrderNo(){
        Map<String, Object> map = orderService.getB2BOrderNo(6,"0E4A0B0B83AA1EDAA0F3E86457140951","20204");
        Assert.assertNotNull("返回无订单示例",map);
        System.out.println("id: " + String.valueOf(map.get("id")));
        System.out.println("quarter: " + String.valueOf(map.get("quarter")));
        System.out.println("order_no: " + String.valueOf(map.get("order_no")));
    }
    /*
    @Test
    public void getOrderItemsById(){
        Order order = orderService.getOrderItems(1314485318283563008L,"20204");
        Assert.assertNotNull("返回无订单示例",order);
        System.out.println("id: " + String.valueOf(order.getId()));
        System.out.println("quarter: " + order.getQuarter());
        List<OrderItem> items = order.getItems();
        Assert.assertNotNull("无订单项目",items);
        System.out.println("items: " + GsonUtils.getInstance().toGson(items));
    }
     */

    //endregion

    //region B2BOrderDao

    @Test
    public void getOrderInfo(){
        Order order = b2BOrderBaseService.getOrderInfo(6, "0E4A0B0B83AA1EDAA0F3E86457140951", "20204");
        Assert.assertNotNull("返回无订单示例",order);
        System.out.println("id: " + order.getId());
        System.out.println("quarter: " + order.getQuarter());
        System.out.println("orderNo: " + order.getOrderNo());
        System.out.println("b2bOrderId: " + order.getB2bOrderId());
        System.out.println("workCardId: " + order.getWorkCardId());
    }

    @Test
    public void getOrderInfoByB2BOrderId(){
        Order order = b2BOrderBaseService.getOrderInfoByB2BOrderId(6, 1339136189772468224L, "20204");
        Assert.assertNotNull("返回无订单示例",order);
        System.out.println("id: " + order.getId());
        System.out.println("quarter: " + order.getQuarter());
        System.out.println("orderNo: " + order.getOrderNo());
        System.out.println("b2bOrderId: " + order.getB2bOrderId());
        System.out.println("workCardId: " + order.getWorkCardId());
    }

    @Test
    public void getDataSourceIdByOrderId(){
        Integer dataSource = b2BOrderDao.getDataSourceIdByOrderId(1339136192079335424L, "20204");
        Assert.assertNotNull("无数据源返回",dataSource);
        System.out.println("dataSource: " + dataSource);
    }


    @Test
    public void getCustomerIdByOrderId(){
        Long customerId = b2BOrderDao.getCustomerIdByOrderId(1339136192079335424L, "20204");
        Assert.assertNotNull("无客户id返回",customerId);
        System.out.println("customerId: " + customerId);
    }


    //endregion

    //endregion sd_order -> sd_order_head
}
