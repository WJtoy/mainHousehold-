
package com.wolfking.jeesite.test.sd;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kkl.kklplus.entity.common.NameValuePair;
import com.wolfking.jeesite.common.config.Global;
import com.wolfking.jeesite.common.config.redis.GsonIgnoreStrategy;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.QuarterUtils;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.md.entity.*;
import com.wolfking.jeesite.modules.md.service.CustomerService;
import com.wolfking.jeesite.modules.md.service.ProductService;
import com.wolfking.jeesite.modules.md.service.ServicePointService;
import com.wolfking.jeesite.modules.sd.entity.*;
import com.wolfking.jeesite.modules.sd.entity.viewModel.CreateOrderModel;
import com.wolfking.jeesite.modules.sd.entity.viewModel.OrderItemModel;
import com.wolfking.jeesite.modules.sd.service.OrderItemService;
import com.wolfking.jeesite.modules.sd.service.OrderService;
import com.wolfking.jeesite.modules.sd.service.OrderTaskService;
import com.wolfking.jeesite.modules.sd.utils.*;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.service.AreaService;
import com.wolfking.jeesite.modules.sys.utils.DictUtils;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import com.wolfking.jeesite.modules.sys.utils.SeqUtils;
import com.wolfking.jeesite.modules.utils.PraiseUtils;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Created by yanshenglu on 2017/4/18.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@ActiveProfiles("dev")
@Slf4j
public class TestOrderGson {

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderTaskService orderTaskService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private AreaService areaService;

    @Autowired
    private ProductService productService;

    @Autowired
    private ServicePointService servicePointService;

    @Autowired
    private OrderItemService orderItemService;

    @Test
    public void createOneOrder(){
        String json = "{\"version\":0,\"quarter\":\"20174\",\"id\":929410662802145280,\"customer\":{\"id\":425,\"name\":\"0825中山市科莱德电器有限公司\"},\"area\":{\"id\":1034,\"name\":\"山东省 济南市 天桥区\",\"fullName\":\"\"},\"orderNo\":\"K2017111200123\",\"userName\":\"范山山\",\"phone1\":\"15753183020\",\"address\":\"城区药山街道小清河北路黄岗村黄岗村11号楼5单元501\",\"expectCharge\":63.0,\"blockedCharge\":0.0,\"description\":\"京东  65745883873\",\"balanceCharge\":0.0,\"totalQty\":1,\"orderPaymentType\":{\"label\":\"预付\",\"value\":\"30\"},\"items\":[{\"tmpId\":\"\",\"customerId\":null,\"flag\":\"\",\"quarter\":\"\",\"itemNo\":10,\"product\":{\"id\":59,\"setFlag\":0,\"name\":\"电热水器40-60L\",\"category\":{\"id\":2,\"name\":\"热水器\"}},\"brand\":\"熊猫\",\"productSpec\":\"\",\"serviceType\":{\"id\":1,\"code\":null,\"name\":\"安装\"},\"standPrice\":63.0,\"discountPrice\":50.0,\"qty\":1,\"charge\":63.0,\"blockedCharge\":0.0,\"expressCompany\":{\"label\":\"\",\"value\":\"\"},\"expressNo\":\"\",\"delFlag\":0,\"id\":0,\"remarks\":\"\",\"createBy\":null,\"createDate\":null}],\"customerBalance\":0.0,\"customerBlockBalance\":0.0,\"customerCredit\":0.0,\"createBy\":{\"id\":49246,\"name\":\"陈明彬\"},\"expresses\":[],\"actionType\":\"\"}";
        CreateOrderModel order = null;
        try {
            order = (CreateOrderModel) redisUtils.gsonRedisSerializer.fromJson(json, CreateOrderModel.class);
            if (order == null) {
                System.out.println("order is null");
            }else{
                User user = new User(1l,"系统管理员");
                Customer customer = customerService.getFromCache(order.getCustomer().getId());
                if(customer == null){
                    Assert.assertFalse("检查客户结算方式错误。",1==2);
                }

                CustomerFinance finance = customerService.getFinance(order.getCustomer().getId());
                if(finance == null || finance.getPaymentType() == null || StringUtils.isBlank(finance.getPaymentType().getValue())){
                    Assert.assertFalse("未设置结算方式，请联系系统管理员。",1==2);
                }
                order.setCustomer(customer);
                order.setOrderPaymentType(finance.getPaymentType());
                order.setCreateDate(new Date());
                order.setCreateBy(user);

                // 如果区域为空的情况
                if (order.getArea() == null || order.getArea().getId() == null)
                {
                    Assert.assertFalse("找不到指定的区域,请重新选择。",1==2);
                }
                //检查区域type
                Area area  = areaService.getFromCache(order.getArea().getId());
                if(area == null){
                    Assert.assertFalse("找不到指定的区域,请重新选择2。",1==2);
                }
                if(area.getType() != 4){
                    Assert.assertFalse("区域请选择至区/县,请重新选择。",1==2);
                }

                //完整区域
                List<Area> areas = areaService.getSelfAndParents(order.getArea().getId());
                if(areas==null || areas.size()<3){
                    Assert.assertFalse("检查区域错误",1==2);
                }

                int itemCount = 0;
                int totalQty =0;//下单的产品数量
                OrderItemModel item = null;
                int itemNo = 0;
                Product p;
                List<OrderItemModel> items = order.getItems();
                OrderUtils.rechargeOrder(items);//重新计价
                Double totalCharge = 0.00;
                Double blockedCharge = 0.00;
                Set<String> pids = Sets.newHashSet();//产品
                Long categoryId = null;//产品类别
                Set<String> sids = Sets.newHashSet();//服务项目
                Integer hasSet = 0;
                List<Dict> expressCompanys = MSDictUtils.getDictList("express_type");//切换为微服务
                Dict expressCompany;
                for (int j=0,size=items.size();j<size;j++)
                {
                    item = items.get(j);
                    if (item != null && item.getDelFlag().equals(OrderCondition.DEL_FLAG_NORMAL) && !item.getFlag().equalsIgnoreCase("del") )
                    {
                        if (item.getProduct() == null || item.getProduct().getId() == null)
                        {
                            Assert.assertFalse("选择产品",1==2);
                        } else if (item.getServiceType() == null || item.getServiceType().getId() == null)
                        {
                            Assert.assertFalse("选择服务类型",1==2);
                        }
                        item.setItemNo(itemNo += 10);
                        itemCount++;
                        p = productService.getProductByIdFromCache(item.getProduct().getId());
                        //类目检查 2019-09-25
                        if(categoryId == null){
                            categoryId = p.getCategory().getId();
                        }else if(!categoryId.equals(p.getCategory().getId())){
                            Assert.assertFalse("订单中产品属不同品类，无法保存。",1==2);
                        }
                        item.setProduct(p);
                        if(p.getSetFlag()==1){
                            hasSet = 1;
                        }
                        pids.add(String.format(",%s,",p.getId()));
                        sids.add(String.format(",%s,",item.getServiceType().getId()));
                        final Dict company = item.getExpressCompany();
                        if(expressCompanys!=null && expressCompanys.size()>0){
                            expressCompany = expressCompanys.stream().filter(t->t.getValue().equalsIgnoreCase(company.getValue())).findFirst().orElse(item.getExpressCompany());
                            item.setExpressCompany(expressCompany);
                        }

                        totalQty = totalQty + item.getQty();
                        totalCharge = totalCharge + item.getCharge();
                        blockedCharge = blockedCharge + item.getBlockedCharge();
                    }
                }
                order.setExpectCharge(totalCharge);//*
                order.setBlockedCharge(blockedCharge);//*
                order.setTotalQty(totalQty);//*

                order.setCreateDate(new Date());//*
                order.setCreateBy(user);//*

                if (itemCount == 0)
                {
                    Assert.assertFalse("订单无服务项目，请添加",1==2);
                }
                //金额二次检查
                if(finance.getBalance() + (finance.getCreditFlag() == 1 ? finance.getCredit() : 0) -finance.getBlockAmount() - order.getExpectCharge() - order.getBlockedCharge()<0){
                    Assert.assertFalse("账户余额不足，请尽快充值",1==2);
                }
                //随机客服
                User kefu = orderService.getRandomKefu(order.getCustomer().getId(),order.getArea().getId(),1L,0,176L,13L);
                if (kefu == null) {
                    //无客服
                    Assert.assertFalse("此区域暂未分配跟进客服，暂时无法下单",1==2);
                }
                Order o = new Order();

                String orderNo = new String("");
                try
                {

                    String quarter = QuarterUtils.getSeasonQuarter(order.getCreateDate());//分片
                    order.setQuarter(quarter);
                    Long oid = SeqUtils.NextIDValue(SeqUtils.TableName.Order);//production
                    //o.preInsert();
                    order.setId(oid);

                    orderNo = SeqUtils.NextSequenceNo("OrderNO");
                    order.setOrderNo(orderNo);

                    o = OrderUtils.toOrder(order);
                    if(o.getItems()==null || o.getItems().size()==0){
                        Assert.assertFalse("订单下无订单项目",1==2);
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
                    o.getOrderCondition().setTotalQty(totalQty);
                    o.getOrderCondition().setStatus(status);
                    o.getOrderCondition().setKefu(kefu);

                    o.getOrderCondition().setHasSet(hasSet);
                    o.getOrderCondition().setProductIds(pids.stream().collect(Collectors.joining(",")).replace(",,,",",,"));
                    o.getOrderCondition().setProductCategoryId(categoryId);
                    o.getOrderCondition().setServiceTypes(sids.stream().collect(Collectors.joining(",")).replace(",,,",",,"));

                    Dict orderType = MSDictUtils.getDictByValue(String.valueOf(Order.ORDER_ORDERTYPE_DSXD),"order_type");//切换为微服务
                    o.setOrderType(orderType);// 电商下单

                    //如果地址中出现null 字符串，则会引起 API 返回JSON数据到APP 导致格式不匹配,所以加字符串判断
                    if(o.getOrderCondition().getAddress()!=null)
                    {
                        String address=o.getOrderCondition().getAddress().replace("null", "");
                        o.getOrderCondition().setAddress(address);
                    }

                    //o.setVerificationCode(StringUtils.getSixVerifyCode());

                    //APP通知消息 & 短信
                    List<User> engineers = servicePointService.getEngineerAccountsListByAreaAndProductCategory(order.getArea().getId(),order.getCategory().getId());
                    try {
                        Long currencyId = SeqUtils.NextIDValue(SeqUtils.TableName.CustomerCurrency);
                        //orderService.createOrder(o,finance,areas,currencyId,engineers,null);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    System.out.println("ok");

                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Test
    public void createOneOrders(){
        String json = "{\"version\":0,\"quarter\":\"20174\",\"id\":929410662802145280,\"customer\":{\"id\":425,\"name\":\"0825中山市科莱德电器有限公司\"},\"area\":{\"id\":1034,\"name\":\"山东省 济南市 天桥区\",\"fullName\":\"\"},\"orderNo\":\"K2017111200123\",\"userName\":\"范山山\",\"phone1\":\"15753183020\",\"address\":\"城区药山街道小清河北路黄岗村黄岗村11号楼5单元501\",\"expectCharge\":63.0,\"blockedCharge\":0.0,\"description\":\"京东  65745883873\",\"balanceCharge\":0.0,\"totalQty\":1,\"orderPaymentType\":{\"label\":\"预付\",\"value\":\"30\"},\"items\":[{\"tmpId\":\"\",\"customerId\":null,\"flag\":\"\",\"quarter\":\"\",\"itemNo\":10,\"product\":{\"id\":59,\"setFlag\":0,\"name\":\"电热水器40-60L\",\"category\":{\"id\":2,\"name\":\"热水器\"}},\"brand\":\"熊猫\",\"productSpec\":\"\",\"serviceType\":{\"id\":1,\"code\":null,\"name\":\"安装\"},\"standPrice\":63.0,\"discountPrice\":50.0,\"qty\":1,\"charge\":63.0,\"blockedCharge\":0.0,\"expressCompany\":{\"label\":\"\",\"value\":\"\"},\"expressNo\":\"\",\"delFlag\":0,\"id\":0,\"remarks\":\"\",\"createBy\":null,\"createDate\":null}],\"customerBalance\":0.0,\"customerBlockBalance\":0.0,\"customerCredit\":0.0,\"createBy\":{\"id\":49246,\"name\":\"陈明彬\"},\"expresses\":[],\"actionType\":\"\"}";
        CreateOrderModel order = null;
        try {
            order = (CreateOrderModel) redisUtils.gsonRedisSerializer.fromJson(json, CreateOrderModel.class);
            if (order == null) {
                System.out.println("order is null");
            }else{
                User user = new User(1l,"系统管理员");
                Customer customer = customerService.getFromCache(order.getCustomer().getId());
                CustomerFinance finance = customerService.getFinance(order.getCustomer().getId());
                order.setCustomer(customer);
                order.setOrderPaymentType(finance.getPaymentType());
                order.setCreateDate(new Date());
                order.setCreateBy(user);
                //完整区域
                List<Area> areas = areaService.getSelfAndParents(order.getArea().getId());
                if(areas==null || areas.size()<3){
                    Assert.assertFalse("检查区域错误",1==2);
                }

                int itemCount = 0;
                int totalQty =0;//下单的产品数量
                OrderItemModel item = null;
                int itemNo = 0;
                Product p;
                List<OrderItemModel> items = order.getItems();
                OrderUtils.rechargeOrder(items);//重新计价
                Double totalCharge = 0.00;
                Double blockedCharge = 0.00;
//                Set<String> pids = Sets.newHashSet();//产品
//                Set<String> pcatids = Sets.newHashSet();//产品类别
//                Set<String> sids = Sets.newHashSet();//服务项目
                Integer hasSet = 0;
                List<Dict> expressCompanys = MSDictUtils.getDictList("express_type");//切换为微服务
                Dict expressCompany;
                for (int j=0,size=items.size();j<size;j++)
                {
                    item = items.get(j);
                    if (item != null && item.getDelFlag().equals(OrderCondition.DEL_FLAG_NORMAL) && !item.getFlag().equalsIgnoreCase("del") )
                    {
                        item.setItemNo(itemNo += 10);
                        itemCount++;
                        p = productService.getProductByIdFromCache(item.getProduct().getId());
                        item.setProduct(p);
                        if(p.getSetFlag()==1){
                            hasSet = 1;
                        }
//                        pids.add(String.format(",%s,",p.getId()));
//                        pcatids.add(String.format(",%s,",p.getCategory().getId()));
//                        sids.add(String.format(",%s,",item.getServiceType().getId()));
                        final Dict company = item.getExpressCompany();
                        if(expressCompanys!=null && expressCompanys.size()>0){
                            expressCompany = expressCompanys.stream().filter(t->t.getValue().equalsIgnoreCase(company.getValue())).findFirst().orElse(item.getExpressCompany());
                            item.setExpressCompany(expressCompany);
                        }

                        totalQty = totalQty + item.getQty();
                        totalCharge = totalCharge + item.getCharge();
                        blockedCharge = blockedCharge + item.getBlockedCharge();
                    }
                }
                order.setExpectCharge(totalCharge);//*
                order.setBlockedCharge(blockedCharge);//*
                order.setTotalQty(totalQty);//*

                order.setCreateDate(new Date());//*
                order.setCreateBy(user);//*


                Order o = new Order();
                //APP通知消息 & 短信
                List<User> engineers = servicePointService.getEngineerAccountsListByAreaAndProductCategory(order.getArea().getId(),order.getCategory().getId());

                String orderNo = new String("");
                //不需审核
                Dict status = MSDictUtils.getDictByValue(String.valueOf(Order.ORDER_STATUS_APPROVED), "order_status");//切换为微服务
                String quarter = QuarterUtils.getSeasonQuarter(order.getCreateDate());//分片
                order.setQuarter(quarter);

                ///线程
                final long s = System.currentTimeMillis();
                final int threadNum = 1;
//                CyclicBarrier cb = new CyclicBarrier(threadNum);
                CyclicBarrier cb = new CyclicBarrier(threadNum, new Runnable() {
                    public void run() {
                        System.out.print("耗时：" + (System.currentTimeMillis() - s));
                    }
                });

                ExecutorService es = Executors.newFixedThreadPool(threadNum);
                for (int i = 0; i < threadNum; i++) {
                    es.execute(new CreateOrderTask(cb,10000,order,orderService,finance,
                            areas,engineers,status));
                }
                es.shutdown();

            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private static class CreateOrderTask implements Runnable {
        private int count;
        private CyclicBarrier latch;
        private CreateOrderModel order;
        private OrderService orderService;
        private CustomerFinance finance;
        private List<Area> areas;
        private Long currencyId;
        private Dict status;
        private List<User> engineers;

        public CreateOrderTask(CyclicBarrier latch,int count,CreateOrderModel order ,OrderService orderService,CustomerFinance finance,
                               List<Area> areas,List<User> engineers,Dict status) {
            this.count = count;
            this.order = order;
            this.orderService = orderService;
            this.finance = finance;
            this.areas = areas;
            this.engineers = engineers;
            this.status = status;
            this.latch = latch;
        }

        public void run() {
            try {

                for (int i = 0; i < count; i++) {
                    try {
                        System.out.println(">>>" + (i+1));
                        //随机客服
                        User kefu = orderService.getRandomKefu(order.getCustomer().getId(), order.getArea().getId(),1L,0,176L,13L);
                        if (kefu == null) {
                            //无客服
                            System.out.println("此区域暂未分配跟进客服，暂时无法下单");
                            continue;
                        }
                        Long oid = SeqUtils.NextIDValue(SeqUtils.TableName.Order);//production
                        //o.preInsert();
                        order.setId(oid);

                        String orderNo = SeqUtils.NextSequenceNo("OrderNO");
                        order.setOrderNo(orderNo);

                        Order o = OrderUtils.toOrder(order);

                        o.getOrderCondition().setTotalQty(1);
                        o.getOrderCondition().setStatus(status);
                        o.getOrderCondition().setKefu(kefu);

                        o.getOrderCondition().setHasSet(0);
                        o.getOrderCondition().setProductIds(",59,");
                        o.getOrderCondition().setProductCategoryId(2L);
                        o.getOrderCondition().setServiceTypes(",1,");
                        Dict orderType = MSDictUtils.getDictByValue(String.valueOf(Order.ORDER_ORDERTYPE_DSXD), "order_type");//切换为微服务
                        o.setOrderType(orderType);// 电商下单

                        //o.setVerificationCode(StringUtils.getSixVerifyCode());
                        Long currencyId = SeqUtils.NextIDValue(SeqUtils.TableName.CustomerCurrency);
                        //orderService.createOrder(o, finance, areas, currencyId, engineers, null);
                        System.out.println(">>> "+ orderNo);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                try {
                    latch.await();
                } catch (BrokenBarrierException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Test
    public void testOrderItemAdapter() {
        OrderItem item = new OrderItem();
        item.setOrderId(1l);
        item.setId(111111111l);
        item.setItemNo(10);
        Product p = new Product();
        p.setId(11111l);
        p.setName("空气净化器");
        //p.setBrand("樱花");
        //p.setModel("JSQ24恒温");
        ProductCategory category = new ProductCategory();
        category.setId(111111l);
        category.setName("空气净化器");
        p.setCategory(category);
        item.setProduct(p);
        item.setBrand("樱花");
        item.setProductSpec("JSQ24恒温");
        item.setBlockedCharge(0.00);
        item.setCharge(50.0);
        item.setStandPrice(50.0);
        item.setDiscountPrice(45.0);
        item.setQty(1);
        ServiceType serviceType = new ServiceType();
        serviceType.setId(1l);
        serviceType.setName("安装");
        item.setServiceType(serviceType);
        item.setRemarks("网购请师傅上门时拍下用户产品的型号及配件的图片");
        item.setExpressCompany(new Dict("zhongtong","中通"));
        item.setExpressNo("3305536068518");

        System.out.println(OrderItemAdapter.getInstance().toJson(item));
    }


    /**
     * 测试订单基本json
     */
    @Test
    public void testgetOrderById(){
        Long id = 1007161820265451520l;
        String quarter = "20182";
        Order order = orderService.getOrderById(id,quarter, OrderUtils.OrderDataLevel.DETAIL,true);
        log.info(OrderAdapter.getInstance().toJson(order));
        /*
        Order order = orderService.getOrderById(100l);
        Assert.assertTrue(order != null);
        json = orderRedisAdapter.toJson(order);
        System.out.println(json);
        */
    }

    @Test
    public void testJsonStringWriteAndread(){
        StringBuilder json = new StringBuilder(100);
        json.append("{")
                .append("\"name\":\"ryan\"")
                .append(",\"No\":\"F1008783\"")
                .append("}");
        redisUtils.setEX("account",json.toString(),0l);
        String str = (String)redisUtils.get("account",String.class);
        System.out.println(str);
    }

    @Test
    public void testSelectRedisDb() {
        Order order = new Order();
        order.setId(1l);
        order.setOrderNo("O20170418001");
        Dict orderType = new Dict();
        orderType.setLabel("电商下单");
        orderType.setValue("0");
        order.setOrderType(orderType);
        order.setTotalQty(1);
        order.setVerificationCode("");
        order.setConfirmDoor(0);
        order.setServiceTimes(0);
        order.setDescription("服务描述");
//        order.setUnit("RMB");
//        order.setTrackingFlag(0);
//        order.setOperationAppFlag(0);
        //order.setKefu(null);

        //order.setCustomerApproveDate(DateUtils.parseDate("2017-04-18 16:12:43"));
//        order.setFeedbackDate(null);
        //order.setPlanDate(null);
        //order.setChargeDate(null);

        order.setOrderInfo("order Info json");

        //fee
        OrderFee fee = new OrderFee();
        fee.setOrderId(1l);
        fee.setExpectCharge(50.0);
        fee.setBlockedCharge(10.0);
        fee.setRebateFlag(0);
        // 客户
        Dict orderPaymentType = new Dict();
        orderPaymentType.setLabel("日结");
        orderPaymentType.setValue("1");
        fee.setOrderPaymentType(orderPaymentType);
        fee.setServiceCharge(50.0);
        fee.setMaterialCharge(0.0);
        fee.setExpectCharge(0.0);
        fee.setTravelCharge(0.0);
        fee.setOtherCharge(0.0);
        fee.setOrderCharge(50.0);
        fee.setCustomerPlanTravelCharge(0.0);

        // 安维
        fee.setEngineerServiceCharge(50.0);
        fee.setEngineerTravelCharge(0.0);
        fee.setEngineerExpressCharge(0.0);
        fee.setEngineerTotalCharge(50.0);
        Dict engineerPaymentType = new Dict();
        engineerPaymentType.setLabel("日结");
        engineerPaymentType.setValue("1");
        fee.setEngineerPaymentType(engineerPaymentType);
        order.setOrderFee(fee);

        //Condition
        OrderCondition condition = new OrderCondition();
        condition.setOrderNo(order.getOrderNo());
        condition.setUserName("王有明");
        condition.setPhone1("13646746359");
        condition.setPhone2("0793-6661808");
        condition.setServicePhone("13646746359");
//        condition.setEmail("");
        condition.setAddress("古县渡镇古南乡松树村委会");

        Dict status = new Dict();
        status.setLabel("下单已审核");
        status.setValue("20");
        condition.setStatus(status);
        Customer customer = new Customer();
        customer.setId(1l);
        customer.setName("267大中国电器商城");
        condition.setCustomer(customer);

        Area area = new Area();
        area.setId(1599l);
        area.setName("鄱阳县");
        area.setFullName("江西省 上饶市 鄱阳县");

        condition.setArea(area);
        condition.setAppAbnormalyFlag(0);
        //停滞
//        Dict pendingFlag = new Dict();
//        pendingFlag.setLabel("正常");
//        pendingFlag.setValue("1");
        condition.setPendingFlag(1);
        condition.setPendingType(new Dict(0l));
        condition.setPendingTypeDate(new Date());

        condition.setAppointmentDate(new Date());


        //反馈标记
//        Dict feedbackFlag = new Dict();
//        feedbackFlag.setLabel("无投诉");
//        feedbackFlag.setValue("0");
        condition.setFeedbackFlag(0);

        //配件
        condition.setPartsFlag(0);
        condition.setReturnPartsFlag(0);
        condition.setReplyFlagKefu(0);
        condition.setReplyFlagCustomer(0);
        condition.setGradeFlag(0);
        condition.setProductIds("1111111,12323123");

        //安维
        ServicePoint servicePoint = new ServicePoint();
        servicePoint.setId(1l);
        servicePoint.setName("华安县华翔制冷家电维修部");
        condition.setServicePoint(servicePoint);
        User engineer = new User();
        engineer.setId(11111232323l);
        engineer.setName("安维师傅");
        condition.setEngineer(engineer);

        //condition.setUserMessageFlag(0);
        //condition.setEngineerMessageFlag(0);

        order.setOrderCondition(condition);


        //Item
        List<OrderItem> items = Lists.newArrayList();
        order.setItems(items);

        OrderItem item = new OrderItem();
//        item.setOrder(order);
        item.setOrderId(1l);
        item.setId(111111111l);
        item.setItemNo(10);
        Product p = new Product();
        p.setId(11111l);
        p.setName("空气净化器");
        //p.setBrand("樱花");
        //p.setModel("JSQ24恒温");
        ProductCategory category = new ProductCategory();
        category.setId(111111l);
        category.setName("空气净化器");
        p.setCategory(category);
        item.setProduct(p);
        item.setBrand("樱花");
        item.setProductSpec("JSQ24恒温");
        item.setBlockedCharge(0.00);
        item.setCharge(50.0);
        item.setStandPrice(50.0);
        item.setDiscountPrice(45.0);
        item.setQty(1);
        ServiceType serviceType = new ServiceType();
        serviceType.setId(1l);
        serviceType.setName("安装");
        item.setServiceType(serviceType);
        item.setRemarks("网购请师傅上门时拍下用户产品的型号及配件的图片");
        item.setExpressCompany(new Dict("zhongtong","中通"));
        item.setExpressNo("3305536068518");


        items.add(item);


        Gson gson = new GsonBuilder()
                //.excludeFieldsWithoutExposeAnnotation() // <---
                .addSerializationExclusionStrategy(new GsonIgnoreStrategy())
                .setPrettyPrinting()
                .create();
//        for(int i=0;i<10000;i++) {
//            redisUtils.set("order:"+i, order, 0l);
//        }
        System.out.println(gson.toJson(order));


    }

    @Test
    public void testHgetOrderCondition(){
        //info
        String json = redisUtils.hGet(RedisConstant.RedisDBType.REDIS_SD_DB,"ORDER:7","info",String.class);
        System.out.println(json);
        /*condition
        String json = redisUtils.hGet(RedisConstant.RedisDBType.REDIS_SD_DB,"ORDER:7","condition",String.class);
        System.out.println(json);
        try {
            OrderCondition condition = orderConditionRedisAdapter.fromJson(json);
        }catch (Exception e){
            System.out.println(e.getMessage());
        }*/
    }

    @Test
    public void reCreateOrderJson(){
        Long orderId = 913635766108819456l;
        Order order = orderService.getOrderById(orderId,"", OrderUtils.OrderDataLevel.HEAD,false);
        String json = redisUtils.gsonRedisSerializer.toJson(order);
        System.out.println(json);
    }

    /**
     * 测试@Slf4j
     */
    @Test
    public void testSlf4jLog(){
        log.error("test Slf4j log:{}",1111);
    }

    @Test
    public void testOrderFeeAdapter(){
        Long orderId = 1003843771781222400l;
        String quarter = "20182";
        Order order = orderService.getOrderById(orderId,quarter, OrderUtils.OrderDataLevel.DETAIL,true);
        String json = OrderFeeAdapter.getInstance().toJson(order.getOrderFee());
        log.info("orderFeeAdpter to json:{}",json);
    }

    @Test
    public void testOrderConditionAdapter(){
        Long orderId = 979543790740180992l;
        String quarter = "20181";
        Order order = orderService.getOrderById(orderId,quarter, OrderUtils.OrderDataLevel.DETAIL,true);
        String json = OrderConditionAdapter.getInstance().toJson(order.getOrderCondition());
        log.info("OrderConditionAdapter to json:{}",json);
    }

    @Test
    public void testOrderConditionRedisAdapter(){
        Long orderId = 979543790740180992l;
        String key = String.format(RedisConstant.SD_ORDER,orderId);
        //del cache
        redisUtils.remove(RedisConstant.RedisDBType.REDIS_SD_DB,key);
        String quarter = "20181";
        OrderCondition condition;
        //from db
        Order order = orderService.getOrderById(orderId,quarter, OrderUtils.OrderDataLevel.DETAIL,false);
        condition = order.getOrderCondition();
        if(condition.getUrgentLevel() == null || condition.getUrgentLevel().getId()==0){
            UrgentLevel urgentLevel = new UrgentLevel();
            urgentLevel.setLabel("0~24");
            urgentLevel.setId(1l);
            condition.setUrgentLevel(urgentLevel);
        }
        String json = OrderConditionRedisAdapter.getInstance().toJson(condition);
        log.info("OrderConditionRedisAdapter to json(db):{}",json);

        //from cache
        order = orderService.getOrderById(orderId,quarter, OrderUtils.OrderDataLevel.DETAIL,false);
        condition = order.getOrderCondition();
        if(condition.getUrgentLevel() == null || condition.getUrgentLevel().getId()==0){
            UrgentLevel urgentLevel = new UrgentLevel();
            urgentLevel.setLabel("0~24");
            urgentLevel.setId(1l);
            condition.setUrgentLevel(urgentLevel);
        }
        json = OrderConditionRedisAdapter.getInstance().toJson(condition);
        log.info("OrderConditionRedisAdapter to json(cache):{}",json);
    }

    /*  去掉sd_orderitem读写 by ryan at 2018-09-03
    @Test
    public void testOrderItemsJson(){

        Date beginDate = DateUtils.parseDate("2018-04-01");
        Date endDate = DateUtils.getEndOfDay(DateUtils.parseDate("2018-06-23 23:59:59"));
        List<Date> dateList = Lists.newArrayList();
        while (beginDate.getTime() < endDate.getTime()) {
            dateList.add(beginDate);
            beginDate = DateUtils.addDays(beginDate, 1);
        }

        Date sDate = null;
        Date eDate = null;
        for(Date date : dateList) {
            sDate = DateUtils.getStartOfDay(date);
            eDate = DateUtils.getEndOfDay(date);
            orderItemService.updateOrderItemsJson(sDate, eDate);
        }



//        List<Order> orderList = orderItemService.getOrderItems("20182", 1010412679464095744L);
//        List<Order> orderList1 = orderList;

//        List<Order> orderList = orderItemService.getOrderItems("20182", 1010412679464095744L);
//        List<Order> orderList1 = orderList;

//        List<Order> orderList = orderItemService.getOrderItems("20182", 1010412679464095744L);
//        List<Order> orderList1 = orderList;

        orderItemService.updateOrderItemsJson("20182", Lists.newArrayList(997869489838231552L));

    }
    */

    @Test
    public void testOrderRedisAdapter()  {
        String json = "{\"dataSource\":{\"label\":\"手动下单\",\"value\":\"1\"},\"workCardId\":\"\",\"id\":\"1083242779402186752\",\"orderNo\":\"K2019011087570\",\"b2bShop\":{\"shopId\":\"\",\"shopName\":\"\"},\"orderType\":{\"label\":\"电商下单\",\"value\":1},\"totalQty\":0,\"verificationCode\":\"\",\"quarter\":\"20191\",\"confirmDoor\":0,\"repeateNo\":\"\",\"description\":\"tb980709334 --> 厨田电器专营店\",\"items\":[{\"id\":\"0\",\"orderId\":\"1083242779402186752\",\"itemNo\":10,\"brand\":\"\",\"productSpec\":\"\",\"serviceType\":{\"id\":1,\"name\":\"安装\"},\"standPrice\":65.0,\"discountPrice\":50.0,\"qty\":1,\"charge\":65.0,\"blockedCharge\":0.0,\"expressCompany\":{\"label\":\"\",\"value\":\"\"},\"expressNo\":\"\",\"remarks\":\"\",\"b2bProductCode\":\"\"}]}";
        try {
            Order order = OrderRedisAdapter.getInstance().fromJson(com.wolfking.jeesite.common.utils.StringUtils.fromGsonString(json.replace("\\\\\\\"", "").replace("\\r\\n", "<br>")));
            List<OrderItem> items = order.getItems();
            Product product;
            for(OrderItem item:items){
                product = item.getProduct();
                System.out.printf("product id:{0},name:{1}\n",product.getId(),product.getName());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 测试 定时处理将未即时自动对账的订单转为手动对账
     */
    @Test
    public void testUpdateToManualCharge(){
        orderTaskService.updateToManualCharge();

    }

    @Test
    public void testSetsContainMethod(){
        Set<String> azSets = Sets.newHashSet("II","OI");
        System.out.println("II:" + azSets.contains("II"));
        System.out.println("OI:" + azSets.contains("OI"));
        System.out.println("IR:" + azSets.contains("IR"));
    }

    @Test
    public void testPraiseCost(){
        double baseCost = 5.0;
        double upperCost = 15.0;
        double discount = 0.1;
        List<NameValuePair<String,Double>> items = org.assertj.core.util.Lists.newArrayList(
                new NameValuePair<String,Double>("五星照片",5.0),
                new NameValuePair<String,Double>("文字",5.0),
                new NameValuePair<String,Double>("合影",5.0)
        );
        NameValuePair<Double,Double> costs;
        /*
        //1.有上限，超过上限
        costs = PraiseUtils.calculatePraiseCost(baseCost,upperCost,discount,items);
        System.out.printf("[超过上限] customer:%9.2f , engineer:%9.2f\n",costs.getName(),costs.getValue());
        //2.有上限，不超过上限
        items = org.assertj.core.util.Lists.newArrayList(
                new NameValuePair<String,Double>("五星照片",5.0),
                new NameValuePair<String,Double>("文字",2.0),
                new NameValuePair<String,Double>("合影",1.0)
        );
        costs = PraiseUtils.calculatePraiseCost(baseCost,upperCost,discount,items);
        System.out.printf("[不超过上限] customer:%9.2f , engineer:%9.2f\n",costs.getName(),costs.getValue());
        //3.无上限，有图片
        upperCost = 0.0;
        items = org.assertj.core.util.Lists.newArrayList(
                new NameValuePair<String,Double>("五星照片",5.0),
                new NameValuePair<String,Double>("文字",5.0),
                new NameValuePair<String,Double>("合影",5.0)
        );
        costs = PraiseUtils.calculatePraiseCost(baseCost,upperCost,discount,items);
        System.out.printf("[无上限，有图片] customer:%9.2f , engineer:%9.2f\n",costs.getName(),costs.getValue());
        //4.无图片
        upperCost = 0.0;
        items = null;
        costs = PraiseUtils.calculatePraiseCost(baseCost,upperCost,discount,items);
        System.out.printf("[无上限，无图片] customer:%9.2f , engineer:%9.2f\n",costs.getName(),costs.getValue());
        */
        //5.精度
        baseCost = 0.0;
        upperCost = 12.0;
        discount = 0.45;
        items = org.assertj.core.util.Lists.newArrayList(
                new NameValuePair<String,Double>("五星照片",5.0),
                new NameValuePair<String,Double>("文字",3.7),
                new NameValuePair<String,Double>("合影",3.0)
        );
        costs = PraiseUtils.calculatePraiseCost(baseCost,upperCost,discount,items);
        System.out.printf("[精度] customer:%s , engineer:%s\n",costs.getName(),costs.getValue());
    }

    public static void main(String[] args) {
        Date endDate = DateUtils.getEndOfDay(new Date());
        System.out.println("endDate: " + DateUtils.formatDate(endDate,"yyyy-MM-dd HH:mm:ss"));
        Date startDate = DateUtils.addMonth(endDate,-3);
        startDate = DateUtils.getDateStart(startDate);
        System.out.println("startDate: " + DateUtils.formatDate(startDate,"yyyy-MM-dd HH:mm:ss"));
    }
}
