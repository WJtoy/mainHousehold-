package com.wolfking.jeesite.test.sd;

import cn.hutool.core.util.StrUtil;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.reflect.TypeToken;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2bcenter.md.B2BDataSourceEnum;
import com.kkl.kklplus.entity.es.ServicePointStation;
import com.kkl.kklplus.entity.lm.mq.MQLMExpress;
import com.kkl.kklplus.entity.praise.PraiseStatusEnum;
import com.kkl.kklplus.utils.SequenceIdUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.config.Global;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.exception.OrderException;
import com.wolfking.jeesite.common.utils.*;
import com.wolfking.jeesite.modules.api.entity.md.RestLoginUserInfo;
import com.wolfking.jeesite.modules.api.entity.sd.RestOrderDetail;
import com.wolfking.jeesite.modules.api.entity.sd.RestOrderDetailInfo;
import com.wolfking.jeesite.modules.md.entity.*;
import com.wolfking.jeesite.modules.md.entity.viewModel.UrgentChargeModel;
import com.wolfking.jeesite.modules.md.service.*;
import com.wolfking.jeesite.modules.md.utils.CustomerUtils;
import com.wolfking.jeesite.modules.mq.dto.MQCommon;
import com.wolfking.jeesite.modules.mq.dto.MQOrderGradeMessage;
import com.wolfking.jeesite.modules.mq.service.OrderAutoPlanMessageService;
import com.wolfking.jeesite.modules.sd.config.CreateOrderConfig;
import com.wolfking.jeesite.modules.sd.dao.OrderDao;
import com.wolfking.jeesite.modules.sd.dao.OrderGradeDao;
import com.wolfking.jeesite.modules.sd.dao.OrderTaskDao;
import com.wolfking.jeesite.modules.sd.dao.TestDao;
import com.wolfking.jeesite.modules.sd.entity.*;
import com.wolfking.jeesite.modules.sd.entity.mapper.OrderGradeMessageMapper;
import com.wolfking.jeesite.modules.sd.entity.viewModel.CustomerProductVM;
import com.wolfking.jeesite.modules.sd.entity.viewModel.NoticeMessageItemVM;
import com.wolfking.jeesite.modules.sd.entity.viewModel.OrderGradeModel;
import com.wolfking.jeesite.modules.sd.entity.viewModel.RepeateOrderVM;
import com.wolfking.jeesite.modules.sd.service.*;
import com.wolfking.jeesite.modules.sd.utils.OrderCacheUtils;
import com.wolfking.jeesite.modules.sd.utils.OrderGradeModelAdapter;
import com.wolfking.jeesite.modules.sd.utils.OrderRedisAdapter;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.entity.VisibilityFlagEnum;
import com.wolfking.jeesite.modules.sys.service.AreaService;
import com.wolfking.jeesite.modules.sys.utils.SeqUtils;
import com.wolfking.jeesite.ms.logistics.service.LogisticsBusinessService;
import com.wolfking.jeesite.ms.logistics.service.LogisticsService;
import com.wolfking.jeesite.ms.providermd.service.MSAreaTimeLinessService;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.MapperFacade;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.lang.math.LongRange;
import org.apache.commons.lang3.RandomUtils;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.joda.time.Minutes;
import org.joda.time.format.DateTimeFormat;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by yanshenglu on 2017/4/18.
 */
@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@ActiveProfiles("dev")
public class TestOrder {

    @Autowired
    private CreateOrderConfig createOrderConfig;

    @Autowired
    private MapperFacade mapper;

    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderDao    orderDao;

    @Autowired
    private OrderGradeDao gradeDao;

    @Resource
    protected OrderTaskDao orderTaskDao;

    @Autowired
    private TestService testService;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private FeedbackService feebackService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private ServiceTypeService serviceTypeService;

    @Autowired
    private OrderAttachmentService attachmentService;

    @Autowired
    private GradeService gradeService;

    @Autowired
    private ServicePointService servicePointService;

    @Autowired
    private OrderTaskService orderTaskService;

    @Autowired
    private OrderAutoPlanMessageService autoPlanMessageService;

    @Autowired
    private PlanRadiusService planRadiusService;

    @SuppressWarnings("rawtypes")
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private OrderMaterialService orderMaterialService;

    @Autowired
    private LogisticsService logisticsService;

    @Autowired
    private LogisticsBusinessService logisticsBusinessService;

    @Autowired
    private OrderGradeService orderGradeService;

    @Autowired
    private OrderEditFormService orderEditFormService;

    @Autowired
    private UrgentLevelService urgentLevelService;

    @Autowired
    private AreaService areaService;

    @Autowired
    private MSAreaTimeLinessService msAreaTimeLinessService;

    @Autowired
    private TimeLinessPriceService timeLinessPriceService;

    @Autowired
    private TestDao testDao;

    /**
     * 测试从数据库读取客评项及评分
     */
    @Test
    public void testGetGrades(){
        /*List<Grade> grades = gradeService.findAllEnabledGradeAndItems();
        Assert.assertTrue(grades !=null && grades.size()>0);
        for (Grade grade:grades){
            System.out.println("grade id:" + grade.getId().toString() + " name:" + grade.getName() + " sort:" + grade.getSort());
            for(GradeItem item:grade.getItemList()){
                System.out.println("  item id:" + item.getId().toString() + " title:" + item.getRemarks() + " port:" + item.getPoint());
                System.out.println("     grade id:" + item.getGrade().getId() + " name:" + item.getGrade().getName());
            }
        }*/
    }

    /**
     * 测试读取客评项及评分
     */
    @Test
    public void testGetGradeList(){
        List<Grade> grades = gradeService.findAllListCache();
        Assert.assertTrue(grades !=null && grades.size()>0);
        for (Grade grade:grades){
            System.out.println("grade id:" + grade.getId().toString() + " name:" + grade.getName() + " sort:" + grade.getSort());
            for(GradeItem item:grade.getItemList()){
                System.out.println("  item id:" + item.getId().toString() + " title:" + item.getRemarks() + " port:" + item.getPoint());
                System.out.println("     grade id:" + item.getGrade().getId() + " name:" + item.getGrade().getName());
            }
        }
    }

    //region 并发测试写和读上门服务
    @Test
    public void testOrderDetail(){
        final int threadNum = 10;
        CyclicBarrier cb = new CyclicBarrier(threadNum);
        Long[] products = new Long[]{72l,74l,75l};
        Long orderId = 922288296346914816l;

        int idx;
        ExecutorService es = Executors.newFixedThreadPool(threadNum);
        for (int i = 0; i < threadNum; i++) {
            if(i%2 == 0) {
                idx = java.util.concurrent.ThreadLocalRandom.current().nextInt(3);
                es.execute(new AddDetailThread(cb,orderId,products[idx],orderService));
            }else{
                es.execute(new GetDetailThread(cb,orderId,orderService));
            }
        }
        es.shutdown();
    }

    //添加上门服务
    static class AddDetailThread implements Runnable {
        private CyclicBarrier cb;
        private Long orderId;
        private Long productId;
        private OrderService orderService;

        public AddDetailThread(CyclicBarrier cb,Long orderId,Long productId,OrderService orderService) {
            this.cb = cb;
            this.orderId = orderId;
            this.productId = productId;
            this.orderService = orderService;
        }
        @Override
        public void run() {
            try {
                // 等待所有任务准备就绪
                cb.await();
                // 定义每个线程负责的业务逻辑实现
                Thread.sleep(100);
                OrderDetail model = new OrderDetail();
                model.setOrderId(orderId);
                model.setProduct(new Product(productId));
                model.setServiceType(new ServiceType(1l));
                model.setQty(1);
                model.setAddType(0);
                model.setServiceTimes(1);
                model.setServicePoint(new ServicePoint(4991l));
                model.setEngineer(new Engineer(5143l));
                model.setCreateBy(new User(1l,"系统管理员"));
                model.setCreateDate(new Date());
                orderService.addDetail(model);
                System.out.println(String.format(">>> Write-%s: ok",this));
                //Thread.sleep(100);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //读取上门服务
    static class GetDetailThread implements Runnable {
        private CyclicBarrier cb;
        private OrderService orderService;
        private Long orderId;

        public GetDetailThread(CyclicBarrier cb,Long orderId,OrderService orderService) {
            this.cb = cb;
            this.orderId = orderId;
            this.orderService = orderService;
        }
        @Override
        public void run() {
            try {
                // 等待所有任务准备就绪
                cb.await();
                // 定义每个线程负责的业务逻辑实现
                Thread.sleep(200);
               Order order = orderService.getOrderById(orderId,"",OrderUtils.OrderDataLevel.DETAIL,true);
                System.out.println(String.format(">>> Read-%s: ok",this));
                if(order != null && order.getDetailList() != null){
                    System.out.println(String.format(">>> Record Cound: %s",order.getDetailList().size()));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //先加后读上门服务
    static class AddAndGetDetailThread implements Runnable {
        private CyclicBarrier cb;
        private Long orderId;
        private Long productId;
        private OrderService orderService;

        public AddAndGetDetailThread(CyclicBarrier cb,Long orderId,Long productId,OrderService orderService) {
            this.cb = cb;
            this.orderId = orderId;
            this.productId = productId;
            this.orderService = orderService;
        }
        @Override
        public void run() {
            try {
                // 等待所有任务准备就绪
                cb.await();
                Thread.sleep(100);
                // 定义每个线程负责的业务逻辑实现
                OrderDetail model = new OrderDetail();
                model.setProduct(new Product(productId));
                model.setOrderId(orderId);
                model.setQty(1);
                model.setAddType(0);
                orderService.addDetail(model);
                System.out.println(String.format(">>> Write-%s: ok",this));
                Order order = orderService.getOrderById(orderId,"",OrderUtils.OrderDataLevel.DETAIL,true);
                if(order != null && order.getDetailList() != null){
                    System.out.println(String.format(">>> Record Cound: %s",order.getDetailList().size()));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //endregion

    //region 测试序列号

    @Test
    public void testSeqNo(){
        final int threadNum = 200;
        CyclicBarrier cb = new CyclicBarrier(threadNum);

        int idx;
        ExecutorService es = Executors.newFixedThreadPool(threadNum);
        for (int i = 0; i < threadNum; i++) {
            es.execute(new SeqNoThread(cb));
        }
        try {
            Thread.sleep(5000);
        }catch (Exception e){
            e.printStackTrace();
        }
        es.shutdown();
    }

    static class SeqNoThread implements Runnable {
        private CyclicBarrier cb;


        public SeqNoThread(CyclicBarrier cb) {
            this.cb = cb;
        }
        @Override
        public void run() {
            try {
                // 等待所有任务准备就绪
                cb.await();
                // 定义每个线程负责的业务逻辑实现
                //Thread.sleep(java.util.concurrent.ThreadLocalRandom.current().nextInt(100,1000));
                String seqNo = SeqUtils.NextSequenceNo("OrderNO");
                System.out.println(String.format(">>> pull: %s",seqNo));
                //Thread.sleep(100);
                SeqUtils.reputSequenceNo("OrderNO",new Date(),seqNo);
                System.out.println(String.format(">>> put: %s",seqNo));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    //endregion 测试序列号

    @Test
    public void testRechargeCustomerCharge() {
        List<OrderItem> items = Lists.newArrayList();

        OrderItem item1 = new OrderItem();
        item1.setId(1l);
        Product p1 = new Product();
        p1.setId(1l);
        p1.setName("热水器");
        item1.setProduct(p1);
        item1.setBrand("阿诗丹顿");
        item1.setProductSpec("");
        item1.setStandPrice(45.0);
        item1.setDiscountPrice(35.0);
        item1.setItemNo(1);
        item1.setDelFlag(0);
        items.add(item1);

        OrderItem item2 = new OrderItem();
        item2.setId(2l);
        Product p2 = new Product();
        p2.setId(2l);
        p2.setName("热水器2");
        item2.setProduct(p2);
        item2.setBrand("樱花");
        item2.setProductSpec("");
        item2.setStandPrice(75.0);
        item2.setDiscountPrice(50.0);
        item2.setItemNo(2);
        item2.setDelFlag(0);
        items.add(item2);

        OrderItem item3 = new OrderItem();
        item2.setId(3l);
        Product p3 = new Product();
        p3.setId(3l);
        p3.setName("热水器3");
        item3.setProduct(p3);
        item3.setBrand("测试");
        item3.setProductSpec("");
        item3.setStandPrice(75.0);
        item3.setDiscountPrice(55.0);
        item3.setItemNo(2);
        item3.setDelFlag(0);
        items.add(item3);

        AtomicInteger index = new AtomicInteger();

        items.stream()
                .filter(t->t.getDelFlag() == OrderItem.DEL_FLAG_NORMAL)
                .sorted(Comparator.comparingDouble(OrderItem::getStandPrice)
                        .thenComparingDouble(OrderItem::getDiscountPrice).reversed())
                .forEach(t-> {
                    int i = index.incrementAndGet();
                    t.setItemNo(i);
                    //第一个按标准价，其余按折扣价
                    t.setCharge((i == 1 ? t.getStandPrice() : t.getDiscountPrice())
                            + t.getDiscountPrice() * (t.getQty() - 1));
                });
        for(int i=0;i<items.size();i++){
            OrderItem item = items.get(i);
            System.out.println("item:" + item.getItemNo() + " brand:" + item.getBrand() + " charge:" + item.getCharge());
        }

        return;

        /*
        List<OrderItem> rlist = items.stream()
                .filter(t->t.getDelFlag() == OrderItem.DEL_FLAG_NORMAL)
                .sorted(Comparator.comparingDouble(OrderItem::getStandPrice)
                        .thenComparingDouble(OrderItem::getDiscountPrice).reversed())
                .collect(Collectors.toList());


        // 开始处理
        boolean hasrepaired = false;
        Integer testtimes = 0;
        for (int i = 0; i < rlist.size(); i++)
        {
            OrderItem d = rlist.get(i);
            d.setItemNo(i + 1);
            d.setCharge((i == 0 ? d.getStandPrice() : d.getDiscountPrice())
                    + d.getDiscountPrice() * (d.getQty() - 1));
        }

        for(int i=0;i<items.size();i++){
            OrderItem item = items.get(i);
            System.out.println("item:" + item.getItemNo() + " brand:" + item.getBrand() + " charge:" + item.getCharge());
        }
        */


    }

    //上门服务最新取价算法
    @Test
    public void testNewRechargeCustomerCharge2() {

        List<OrderDetail> details = Lists.newArrayList();

        OrderDetail item1 = new OrderDetail();
        item1.setId(913639196277739520l);
        item1.setServiceTimes(1);
        Product p1 = new Product();
        p1.setId(59l);
        p1.setName("电热水器40-60L");
        item1.setProduct(p1);
        item1.setBrand("");
        item1.setProductSpec("");
        item1.setStandPrice(60.0);
        item1.setDiscountPrice(50.0);
        item1.setEngineerStandPrice(30.0);
        item1.setEngineerDiscountPrice(10.0);
        item1.setItemNo(10);
//        item1.setDelFlag(0);
        details.add(item1);

        rechargeOrder(details,item1);
        System.out.println("details hashcode:" + details.hashCode());

        OrderDetail item2 = new OrderDetail();
        item2.setId(913677531641679872l);
        item2.setServiceTimes(1);
        Product p2 = new Product();
        p2.setId(59l);
        p2.setName("电热水器40-60L");
        item2.setProduct(p2);
        item2.setBrand("");
        item2.setProductSpec("");
        item2.setStandPrice(60.0);
        item2.setDiscountPrice(45.0);
        item2.setEngineerStandPrice(45.0);
        item2.setEngineerDiscountPrice(30.0);
        item2.setItemNo(20);
//        item2.setDelFlag(0);
        details.add(item2);
        rechargeOrder(details,item2);

        System.out.println("最终取价后列表：");
        List<OrderDetail> items = details.stream().filter(t->t.getDelFlag()!=1).collect(Collectors.toList());
        OrderDetail m;
        for(int i=0,size=items.size();i<size;i++){
            m = items.get(i);
            System.out.println(String.format("id:%s 项次:%s , 厂商服务费:%s , 网点服务费:%s",m.getId(),m.getItemNo(),m.getCharge() ,m.getEngineerServiceCharge()));
        }
    }

    //测试网点上门服务费预估金额
    //18-01-24
    @Test
    public void testServicePointCost() {
        //网点
        ServicePoint servicePoint = new ServicePoint();
        servicePoint.setId(13731l);
        servicePoint.setName("川L0015");
        List<OrderItem> list = Lists.newArrayList();

        OrderItem item1 = new OrderItem();
        Product p1 = new Product();
        p1.setId(55l);
        p1.setName("速热电热水器");
        item1.setProduct(p1);
        ServiceType st1 = new ServiceType(2l);
        st1.setName("维修");
        item1.setServiceType(st1);
        list.add(item1);

        OrderItem item2 = new OrderItem();
        Product p2 = new Product();
        p2.setId(44l);
        p2.setName("烟机+灶具+燃气热水器");
        item2.setProduct(p2);
        ServiceType st2 = new ServiceType(1l);
        st2.setName("安装");
        item2.setServiceType(st2);
        list.add(item2);

        OrderItem item3 = new OrderItem();
        Product p3 = new Product();
        p3.setId(60l);
        p3.setName("小厨宝");
        item3.setProduct(p3);
        ServiceType st3 = new ServiceType(3l);
        st3.setName("检测");
        item3.setServiceType(st3);
        list.add(item3);

        //测试
        Double amount = 0d;
        //1.1单服务
        List<OrderItem> items = Lists.newArrayList();
        for(int i=0,size=list.size();i<size;i++){
            items.clear();
            OrderItem item = list.get(i);
            items.add(item);
            amount = orderService.calcServicePointCost(servicePoint,items);
            System.out.println("1.单个服务");
            System.out.println("1."+ (i+1) +" " + item.getProduct().getName() + " " + item.getServiceType().getName());

            System.out.println("预估金额:" + amount);
            System.out.println("");
        }
        System.out.println(StringUtils.leftPad("",100,"="));

        System.out.println("2.二个服务");
        int idx=0;
        Set<OrderItem> itemSet = list.stream().collect(Collectors.toSet());
        Set<List<OrderItem>> subSets =  Sets.cartesianProduct(itemSet,itemSet);//笛卡尔积

        for (List<OrderItem> subItems : subSets) {
            if(subItems.get(0).getProduct().equals(subItems.get(1).getProduct())){
                continue;
            }
            amount = orderService.calcServicePointCost(servicePoint,subItems);
            idx=0;
            for(OrderItem item:subItems){
                idx++;
                System.out.println("" + idx + "." + item.getProduct().getName() + " " + item.getServiceType().getName());
            }
            System.out.println("预估金额:" + amount);
            System.out.println("");
        }
        System.out.println(StringUtils.leftPad("",100,"="));

        //3个服务
        System.out.println("");
        amount = orderService.calcServicePointCost(servicePoint,list);

        System.out.println("3.三个服务");
        idx=0;
        for(OrderItem item:list){
            idx++;
            System.out.println("3."+idx+"."+item.getProduct().getName() + " " + item.getServiceType().getName());
        }
        System.out.println("预估金额:" + amount);
        System.out.println(StringUtils.leftPad("",100,"="));
    }


    //上门服务最新取价算法 new
    @Test
    public void testNewRechargeCustomerCharge() {
        //循环取加，取最低的
        List<OrderDetail> details = Lists.newArrayList();

        //1.确认上门，添加烟灶消套装：2017-09-29 13:38:54
        System.out.println("1.确认上门，添加烟灶消套装：2017-09-29 13:38:54");
        OrderDetail item1 = new OrderDetail();
        item1.setId(913639196277739520l);
        Product p1 = new Product();
        p1.setId(80l);
        p1.setName("烟灶消套装");
        item1.setProduct(p1);
        item1.setBrand("");
        item1.setProductSpec("");
        item1.setStandPrice(100.0);
        item1.setDiscountPrice(85.0);
        item1.setEngineerStandPrice(60.0);
        item1.setEngineerDiscountPrice(50.0);
        item1.setItemNo(10);
        item1.setDelFlag(0);
        details.add(item1);

        rechargeOrder(details,item1);

        //2.删除烟灶消套装：2017-09-29 16:11:08
        System.out.println("2.删除烟灶消套装：2017-09-29 16:11:08");
        item1.setDelFlag(1);
        rechargeOrder(details,item1);

        //3.添加油烟机：2017-09-29 16:11:13
        System.out.println("3.添加油烟机：2017-09-29 16:11:13");
        OrderDetail item2 = new OrderDetail();
        item2.setId(913677531641679872l);
        Product p2 = new Product();
        p2.setId(75l);
        p2.setName("油烟机");
        item2.setProduct(p2);
        item2.setBrand("");
        item2.setProductSpec("");
        item2.setStandPrice(65.0);
        item2.setDiscountPrice(50.0);
        item2.setEngineerStandPrice(40.0);
        item2.setEngineerDiscountPrice(30.0);
        item2.setItemNo(10);
        item2.setDelFlag(0);
        details.add(item2);
        rechargeOrder(details,item2);

        //4.添加燃气灶：2017-09-29 16:11:21
        System.out.println("4.添加燃气灶：2017-09-29 16:11:21");
        OrderDetail item3 = new OrderDetail();
        item3.setId(913677562864078848l);
        Product p3 = new Product();
        p3.setId(72l);
        p3.setName("燃气灶");
        item3.setProduct(p3);
        item3.setBrand("");
        item3.setProductSpec("");
        item3.setStandPrice(50.0);
        item3.setDiscountPrice(20.0);
        item3.setEngineerStandPrice(35.0);
        item3.setEngineerDiscountPrice(10.0);
        item3.setItemNo(20);
        item3.setDelFlag(0);
        details.add(item3);
        rechargeOrder(details,item3);

        //5.删除燃气灶：2017-09-30 21:11:04
        System.out.println("5.删除燃气灶：2017-09-30 21:10:54");
        item3.setDelFlag(1);
        rechargeOrder(details,item3);
        //6.添加燃气灶：2017-09-30 21:11:01
        System.out.println("6.添加燃气灶：2017-09-30 21:11:01");
        OrderDetail item4 = new OrderDetail();
        item4.setId(914115362499465216l);
        p3 = new Product();
        p3.setId(72l);
        p3.setName("燃气灶");
        item4.setProduct(p3);
        item4.setStandPrice(50.0);
        item4.setDiscountPrice(20.0);
        item4.setEngineerStandPrice(35.0);
        item4.setEngineerDiscountPrice(10.0);
        item4.setItemNo(20);
        item4.setDelFlag(0);
        details.add(item4);
        rechargeOrder(details,item4);
        //7.删除燃气灶：2017-09-30 21:11:04
        System.out.println("7.删除燃气灶：2017-09-30 21:11:04");
        item4.setDelFlag(1);
        rechargeOrder(details,item4);

        //8.添加燃气灶：2017-09-30 21:12:14
        System.out.println("8.添加燃气灶：2017-09-30 21:12:14");
        OrderDetail item5 = new OrderDetail();
        item5.setId(914115671401566208l);
        p3 = new Product();
        p3.setId(72l);
        p3.setName("燃气灶");
        item5.setProduct(p3);
        item5.setStandPrice(50.0);
        item5.setDiscountPrice(20.0);
        item5.setEngineerStandPrice(35.0);
        item5.setEngineerDiscountPrice(10.0);
        item5.setItemNo(20);
        item5.setDelFlag(0);
        details.add(item5);
        rechargeOrder(details,item5);

        //9.删除燃气灶：2017-09-30 21:11:04
        System.out.println("9.删除燃气灶：2017-09-30 21:12:17");
        item5.setDelFlag(1);
        rechargeOrder(details,item5);

        //10.添加燃气灶：2017-09-30 21:14:46
        System.out.println("10.添加燃气灶：2017-09-30 21:14:46");
        OrderDetail item6 = new OrderDetail();
        item6.setId(914116309061603328l);
        p3 = new Product();
        p3.setId(72l);
        p3.setName("燃气灶");
        item6.setProduct(p3);
        item6.setStandPrice(50.0);
        item6.setDiscountPrice(20.0);
        item6.setEngineerStandPrice(35.0);
        item6.setEngineerDiscountPrice(10.0);
        item6.setItemNo(20);
        item6.setDelFlag(0);
        details.add(item6);
        rechargeOrder(details,item6);

        //11.删除燃气灶：2017-09-30 21:16:00
        System.out.println("11.删除燃气灶：2017-09-30 21:16:00");
        item6.setDelFlag(1);
        rechargeOrder(details,item6);

        //12.添加燃气灶：2017-09-30 21:16:07
        System.out.println("12.添加燃气灶：2017-09-30 21:16:07");
        OrderDetail item7 = new OrderDetail();
        item7.setId(914116647508381696l);
        p3 = new Product();
        p3.setId(72l);
        p3.setName("燃气灶");
        item7.setProduct(p3);
        item7.setStandPrice(50.0);
        item7.setDiscountPrice(20.0);
        item7.setEngineerStandPrice(35.0);
        item7.setEngineerDiscountPrice(10.0);
        item7.setItemNo(20);
        item7.setDelFlag(0);
        details.add(item7);
        rechargeOrder(details,item7);

        //13.删除燃气灶：2017-09-30 21:18:45
        System.out.println("13.删除燃气灶：2017-09-30 21:18:45");
        item7.setDelFlag(1);
        rechargeOrder(details,item7);

        //14.添加燃气灶：2017-09-30 21:18:55
        System.out.println("14.添加燃气灶：2017-09-30 21:18:55");
        OrderDetail item8 = new OrderDetail();
        item8.setId(914117358933643264l);
        p3 = new Product();
        p3.setId(72l);
        p3.setName("燃气灶");
        item8.setProduct(p3);
        item8.setStandPrice(50.0);
        item8.setDiscountPrice(20.0);
        item8.setEngineerStandPrice(35.0);
        item8.setEngineerDiscountPrice(10.0);
        item8.setItemNo(20);
        item8.setDelFlag(0);
        details.add(item8);
        rechargeOrder(details,item8);

        System.out.println("最终取价后列表：");
        List<OrderDetail> items = details.stream().filter(t->t.getDelFlag()!=1).collect(Collectors.toList());
        OrderDetail m;
        for(int i=0,size=items.size();i<size;i++){
            m = items.get(i);
            System.out.println(String.format("id:%s 项次:%s , 厂商服务费:%s , 网点服务费:%s",m.getId(),m.getItemNo(),m.getCharge() ,m.getEngineerServiceCharge()));
        }
    }


    /**
     * 重新计算费用，只计算本次上门服务项目
     * 循环计价，厂商取最低价，网点取最低价
     */
    public void rechargeOrder_v2(List<OrderDetail> list,OrderDetail detail){
        //本次上门，删除标记不等于1
        List<OrderDetail> items = list.stream().filter(t->t.getServiceTimes()==detail.getServiceTimes() && t.getDelFlag().intValue() !=  1).collect(Collectors.toList());
        if(items.size()==0) {
            System.out.println("无上门服务");
            return;
        }

        OrderDetail m;
        int size = items.size();
        Double cprice = 0.0;
        Double sprice = 0.0;
        Map<Integer,Double> custprices = Maps.newHashMap();//厂商
        Map<Integer,Double> servprices = Maps.newHashMap();//网点
        //1.循环累计，当前行取标准加，其余取折扣加
        for(int i=0;i<size;i++){
            cprice = 0.0;
            sprice = 0.0;

            for(int j=0;j<size;j++) {
                m = items.get(j);
                if(i==j){
                    cprice = cprice + m.getStandPrice()+m.getDiscountPrice()*(m.getQty()-1);
                    sprice = sprice + m.getEngineerStandPrice() + m.getEngineerDiscountPrice()*(m.getQty()-1);
                }else{
                    cprice = cprice + m.getDiscountPrice()*m.getQty();
                    sprice = sprice + m.getEngineerDiscountPrice()*m.getQty();
                }
            }
            custprices.put(i,cprice);
            servprices.put(i,sprice);
        }
        //2.厂商取最低价
        cprice = custprices.values().stream().min(Comparator.comparingDouble(Double::doubleValue)).get();
        int cidx = 0;
        for(int i=0;i<size;i++){
            if(custprices.get(i).equals(cprice)){
                cidx = i;
                break;
            }
        }
        int sidx = 0;
        //3.网点取最低价
        sprice = servprices.values().stream().min(Comparator.comparingDouble(Double::doubleValue)).get();
        for(int j=0;j<size;j++){
            if(servprices.get(j).equals(sprice)){
                sidx = j;
                break;
            }
        }
        //4.更新最终计价费用
        for(int i=0;i<size;i++){
            m = items.get(i);
            //System.out.println(String.format("序号:%s , 厂商 标准价:%s , 折扣价：%s 网点 标准价:%s , 折扣价:%s",i,m.getStandPrice() ,m.getDiscountPrice() ,m.getEngineerStandPrice() ,m.getEngineerDiscountPrice()));
            //System.out.println("cprice"+ m.getStandPrice() +" dis:" + m.getDiscountPrice() + " sprice:" + m.getEngineerStandPrice() + " dis:" + m.getEngineerDiscountPrice());
            //厂商
            if(i==cidx){
                m.setCharge(m.getStandPrice() + m.getDiscountPrice()*(m.getQty()-1));
            }else{
                m.setCharge(m.getDiscountPrice()*m.getQty());
            }
            //网点
            if(i==sidx){
                m.setEngineerServiceCharge(m.getEngineerStandPrice()+m.getEngineerDiscountPrice()*(m.getQty()-1));
            }else{
                m.setEngineerServiceCharge(m.getEngineerDiscountPrice()*m.getQty());
            }
        }

        //region 输出
        System.out.println("----------------------------------------------------------------------");
        System.out.println(String.format("厂商取价序号:%s , 厂商服务费(最低)：%s" ,cidx, cprice));
        System.out.println("厂商服务费取价列表：");
        custprices.forEach((k,v)->{
            System.out.println(String.format("序号 : %s , 费用: %s", k ,v));
        });


        System.out.println("----------------------------------------------------------------------");
        System.out.println(String.format("网点取价序号:%s , 网点服务费(最低):%s" ,sidx,sprice));
        System.out.println("网点服务费取价列表：");
        servprices.forEach((k,v)->{
            System.out.println(String.format("序号 : %s , 费用: %s", k ,v));
        });

        /*
        System.out.println("----------------------------------------------------------------------");
        System.out.println("取价后列表：");
        for(int i=0;i<size;i++){
            m = items.get(i);
            System.out.println(String.format("项次:%s , 厂商服务费:%s , 网点服务费:%s",m.getItemNo(),m.getCharge() ,m.getEngineerServiceCharge()));
        }
        */
        //region 输出
    }

    /**
     * 2017-10-23
     * 重新计算费用，只计算本次上门服务项目
     * 厂商:先去标准价最高，标准价相同，取折扣价最低的
     * 网点:循环计价，取最低价
     */
    public void rechargeOrder(List<OrderDetail> list,OrderDetail detail){
        if(list == null || list.size()==0){
            return;
        }
        //本次上门，删除标记不等于1
        List<OrderDetail> items;
        OrderDetail m;
        int size;
        //1.厂商,先去标准价最高，标准价相同，取折扣价最低的
        Double cprice = 0.0;
        items = list.stream()
                .filter(t->t.getServiceTimes()==detail.getServiceTimes() && t.getDelFlag().intValue() !=  1)
                .sorted(Comparator.comparingDouble(OrderDetail::getStandPrice).reversed()
                        .thenComparingDouble(OrderDetail::getDiscountPrice))
                .collect(Collectors.toList());
        if(items.size()==0){
            return;
        }
        size = items.size();
        for(int i=0;i<size;i++) {
            m = items.get(i);
            if (i == 0) {
                m.setCharge(m.getStandPrice() + m.getDiscountPrice() * (m.getQty() - 1));
            } else {
                m.setCharge(m.getDiscountPrice() * m.getQty());
            }
            cprice = cprice + m.getCharge();
        }

        //2.网点取价，循环累计，取最低价
        items = list.stream()
                .filter(t->t.getServiceTimes()==detail.getServiceTimes() && t.getDelFlag().intValue() !=  1)
                .collect(Collectors.toList());
        size = items.size();
        Double sprice = 0.0;
        Map<Integer,Double> servprices = Maps.newHashMap();//网点
        //1.循环累计，当前行取标准加，其余取折扣加
        for(int i=0;i<size;i++){
            sprice = 0.0;

            for(int j=0;j<size;j++) {
                m = items.get(j);
                if(i==j){
                    sprice = sprice + m.getEngineerStandPrice() + m.getEngineerDiscountPrice()*(m.getQty()-1);
                }else{
                    sprice = sprice + m.getEngineerDiscountPrice()*m.getQty();
                }
            }
            servprices.put(i,sprice);
        }
        //2.取最低价
        int sidx = 0;
        sprice = servprices.values().stream().min(Comparator.comparingDouble(Double::doubleValue)).get();
        for(int j=0;j<size;j++){
            if(servprices.get(j).equals(sprice)){
                sidx = j;
                break;
            }
        }

        //计价费用
        for(int i=0;i<size;i++){
            m = items.get(i);
            //网点
            if(i==sidx){
                m.setEngineerServiceCharge(m.getEngineerStandPrice()+m.getEngineerDiscountPrice()*(m.getQty()-1));
            }else{
                m.setEngineerServiceCharge(m.getEngineerDiscountPrice()*m.getQty());
            }
        }

        //region 输出
        System.out.println("----------------------------------------------------------------------");
        System.out.println(String.format("厂商服务费：%s" , cprice));


        System.out.println("----------------------------------------------------------------------");
        System.out.println(String.format("网点取价序号:%s , 网点服务费(最低):%s" ,sidx,sprice));
        System.out.println("网点服务费取价列表：");
        servprices.forEach((k,v)->{
            System.out.println(String.format("序号 : %s , 费用: %s", k ,v));
        });

        System.out.println("----------------------------------------------------------------------");
        System.out.println("取价后列表：");
        for(int i=0;i<size;i++){
            m = items.get(i);
            System.out.println(String.format("项次:%s , 厂商服务费:%s , 网点服务费:%s",m.getItemNo(),m.getCharge() ,m.getEngineerServiceCharge()));
        }
        System.out.println("======================================================================");
        System.out.println(" ");
        items = null;
    }

    // 客评
    @Test
    public void testCanGrade(){
        Long id = 947274834705264641l;
        Order order = orderService.getOrderById(id,"20174", OrderUtils.OrderDataLevel.CONDITION,true);
        Assert.assertNotNull(order);
        Assert.assertNotNull(order.getOrderCondition());
        System.out.println("app Abnormaly:" + order.getOrderCondition().getAppAbnormalyFlag());
        if(order.canGrade()){
            System.out.println("can grade");
        }else{
            System.out.println("can not grade");
        }
    }

    //
    @Test
    public void testGetSeason(){
        String season = DateUtils.getYear()+DateUtils.getSeason();
        System.out.println(season);
        Assert.assertTrue(season.equals("20172"));
    }

    @Test
    public void testSaveOrder(){
        /* 测试下单保存 */
        Order order = new Order(10l);
        order.setOrderNo("O20170420001");
        order.setDescription("单元测试订单");
//        order.setFeedback(new Feedback(0l));
        order.setQuarter(DateUtils.getYear()+DateUtils.getSeason());//季度
        Dict orderType = new Dict();
        orderType.setLabel("电商下单");
        orderType.setValue("0");
        order.setOrderType(orderType);
        order.setTotalQty(2);
//        order.setUnit("RMB");

        //item
        List<OrderItem> items = Lists.newArrayList();
        OrderItem item1 = new OrderItem();
        item1.setId(1l);
        Product p1 = new Product();
        p1.setId(1l);
        p1.setName("热水器");
        item1.setProduct(p1);
        item1.setBrand("阿诗丹顿");
        item1.setProductSpec("规格1");
        item1.setStandPrice(45.0);
        item1.setDiscountPrice(35.0);
        item1.setItemNo(1);
        item1.setDelFlag(0);
        item1.setServiceType(new ServiceType(1l));
        items.add(item1);

        OrderItem item2 = new OrderItem();
        item2.setId(2l);
        Product p2 = new Product();
        p2.setId(2l);
        p2.setName("热水器2");
        item2.setProduct(p2);
        item2.setBrand("樱花");
        item2.setProductSpec("spec 2");
        item2.setStandPrice(75.0);
        item2.setDiscountPrice(50.0);
        item2.setItemNo(2);
        item2.setDelFlag(0);
        item2.setServiceType(new ServiceType(1l));
        items.add(item2);
        order.setItems(items);

        //Status
        OrderStatus ostatus = new OrderStatus();
        ostatus.setOrderId(order.getId());
        ostatus.setCustomerApproveDate(new Date());
        order.setOrderStatus(ostatus);


        //Condition
        OrderCondition condition = new OrderCondition();
        condition.setOrderId(order.getId());
        condition.setOrderNo(order.getOrderNo());
        condition.setServiceTypes("");
        condition.setUserName("王有明");
        condition.setPhone1("13646746359");
        condition.setPhone2("0793-6661808");
        condition.setServicePhone("13646746359");
//        condition.setEmail("");
        condition.setAddress("古县渡镇古南乡松树村委会");
        condition.setDelFlag(0);
        condition.setCreateDate(new Date());
        condition.setCreateBy(new User(1l));
        condition.setUpdateBy(new User(1l));
        condition.setUpdateDate(new Date());
        condition.setTotalQty(order.getTotalQty());

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

        //fee
        OrderFee fee = new OrderFee();
        fee.setOrderId(order.getId());
        fee.setExpectCharge(110.0);
        fee.setBlockedCharge(20.0);
        fee.setRebateFlag(0);
        // 客户
        Dict orderPaymentType = new Dict();
        orderPaymentType.setLabel("日结");
        orderPaymentType.setValue("1");
        fee.setOrderPaymentType(orderPaymentType);
        fee.setServiceCharge(110.0);
        fee.setMaterialCharge(0.0);
        fee.setExpectCharge(0.0);
        fee.setTravelCharge(0.0);
        fee.setOtherCharge(0.0);
        fee.setOrderCharge(50.0);
        fee.setCustomerPlanTravelCharge(0.0);

        // 安维
        fee.setEngineerServiceCharge(25.0);
        fee.setEngineerTravelCharge(0.0);
        fee.setEngineerExpressCharge(0.0);
        fee.setEngineerTotalCharge(25.0);
        Dict engineerPaymentType = new Dict();
        engineerPaymentType.setLabel("日结");
        engineerPaymentType.setValue("1");
        fee.setEngineerPaymentType(engineerPaymentType);
        order.setOrderFee(fee);

        //orderService.createOrder(order,null);

    }

    @Test
    public void testOrder(){
        Long orderId = 1082467507752800257l;
        String quarter = "20191";
        /* 1.检查重复下单
        Boolean has = orderService.checkRepeateOrder("13646746359","",10);
        Assert.assertTrue(has);
        */

        /* 2.get order
        Order order = orderService.getOrderById(orderId,quarter, OrderUtils.OrderDataLevel.DETAIL,true);
        Assert.assertTrue("按订单号获取订单失败",order != null);
        log.info("id:{} ,dataSource:{} ,workcardId:{}",order.getId(),order.getDataSource().getValue(),order.getWorkCardId());
        Assert.assertTrue("按订单号获取订单失败",order.getId().longValue() == orderId);
        */

        Order order = orderService.getOrderById(orderId,quarter, OrderUtils.OrderDataLevel.CONDITION,true);
        //Assert.assertTrue("按订单号获取订单失败",order != null);
        //Assert.assertTrue("按订单号获取订单失败",order.getOrderCondition().getStatus() != null);
        //log.info("status:{}",order.getOrderCondition().getStatus().getValue());
        System.out.println(GsonUtils.toGsonString(order));

        //System.out.println(order.getItems().size());
        //lazy load
        //OrderFee fee = order.getOrderFee();
        //Assert.assertTrue(fee != null && fee.getServiceCharge()==110.0);

        /* 3. 测试getOrderFeeById
        //主库
        OrderFee fee = orderService.getOrderFeeById(orderId,quarter,true);
        Assert.assertTrue("is null",fee != null);
        //从库
        fee = orderService.getOrderFeeById(orderId,quarter,false);
        Assert.assertTrue("is null",fee != null);

        */
    }

    @Test
    public void testGetOrderCondition(){
//        OrderCondition condition = orderService.getOrderConditionById(100l);
        //OrderCondition condition = orderService.getRedisOrderConditionById(100l,"",true);
        //Assert.assertTrue(condition.getStatus() != null);

    }

    /**
     * 测试使用hashmap传参
     */
    @Test
    public void testUpdateOrderCondition(){
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("orderId",7);
        Dict status = new Dict("20","测试");
        map.put("status",status);
        orderService.updateOrderCondition(map);

    }

    /**
     * 测试读取订单实际服务项目
     */
    @Test
    public void testgetOrderDetails(){
        List<OrderDetail> details = orderService.getOrderDetails(10l,"",false);
        Assert.assertTrue(details != null && details.size()>0);
    }

    @Test
    public void testGetOrderForRedis(){
        Order order = orderService.getOrderById(7l, "",OrderUtils.OrderDataLevel.FEE,true);
        String json = OrderRedisAdapter.getInstance().toJson(order);
        System.out.println(json);
    }

    @Test
    public void testGetOrderById(){
        //测试优先从缓存读取
        //1.完成单
        //Order order = orderService.getOrderById(980102499233042432l, "20181",OrderUtils.OrderDataLevel.CONDITION,true);
        //Assert.assertTrue(order !=null);
        //Assert.assertTrue(order.getOrderCondition() !=null);
        //Assert.assertTrue(order.getOrderStatus() !=null);
        //Assert.assertTrue(order.getOrderFee() !=null);
        //Assert.assertTrue(order.getDetailList() !=null);
        //Assert.assertTrue(order.getOrderCondition().getRushOrderFlag() == 2);
//        Assert.assertTrue(order!=null && Objects.equals(order.getId(),100l));

        //2.未完成
        //Order order = orderService.getOrderById(975621472951341056l, "20181",OrderUtils.OrderDataLevel.CONDITION,true);
        //Assert.assertTrue(order !=null);
        //Assert.assertTrue(order.getOrderCondition() !=null);
        //Assert.assertTrue("突击单标志不为：1",order.getOrderCondition().getRushOrderFlag() == 1);
        //Assert.assertTrue("突击单标志不为：2",order.getOrderCondition().getRushOrderFlag() == 2);
        /*
        Map<Long,OrderServicePointFee> maps = orderService.getOrderServicePointFeeMaps(996577753400741888L,"20182");
        Assert.assertFalse("读取错误",maps==null);
        Set<Map.Entry<Long, OrderServicePointFee>> entrySet = maps.entrySet();
        Iterator<Map.Entry<Long, OrderServicePointFee>> iter = entrySet.iterator();
        while (iter.hasNext())
        {
            Map.Entry<Long, OrderServicePointFee> entry = iter.next();
            System.out.println(entry.getKey() + "\t" + GsonUtils.getInstance().toGson(entry.getValue()));
        }*/
        /*
        Order order = orderService.getOrderById(996226028437377024L, "20182",OrderUtils.OrderDataLevel.FEE,true);
        Assert.assertFalse("订单读取错误",order==null);
        Assert.assertFalse("网点汇总数据错误",order.getServicePointFees()==null);
        Assert.assertTrue("网点汇总数据返回记录：1",order.getServicePointFees().size()==1);
        log.info(GsonUtils.getInstance().toGson(order.getServicePointFees()));
        //Assert.assertTrue("网点汇总数据错误",order.getServicePointFees()==null);
        log.info("orderfee.insuranceCharge:" + order.getOrderFee().getInsuranceCharge());
        */
    }

    @Test
    public void testMybati_parameter(){
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("id",1);
        map.put("closeDate",null);
        feebackService.updateFeedback(map);
    }

    /**
     * 测试订单是否可以自动结账

    @Test
    public void testCompareOrderDetail(){
        Order order = new Order(1l);
        OrderFee fee = new OrderFee();
        order.setOrderFee(fee);
        OrderCondition condition = new OrderCondition();
        order.setOrderCondition(condition);
        Long azTypeId = 2l;
        List<OrderItem> items = Lists.newArrayList();
        order.setItems(items);
        List<OrderDetail> details = Lists.newArrayList();
        order.setDetailList(details);

        //A:
        //费用相同
        fee.setExpectCharge(100.0);
        fee.setOrderCharge(100.0);
        //1.都是安装单,1:1
        //1.1.items
        OrderItem item1 = new OrderItem();
        item1.setProduct(new Product(1l));
        item1.setServiceType(new ServiceType(azTypeId));
        item1.setQty(1);
        items.add(item1);
        //1.2.details
        OrderDetail detail1 = new OrderDetail();
        detail1.setProduct(new Product(1l));
        detail1.setServiceType(new ServiceType(azTypeId));
        detail1.setQty(1);
        details.add(detail1);

        boolean canInvoice = orderService.compareOrderDetail(order);
        System.out.println("sample A - canInvoice:" + canInvoice);

        //B.费用不相同,其它都相同
        fee.setExpectCharge(100.0);
        fee.setOrderCharge(115.0);
        canInvoice = orderService.compareOrderDetail(order);
        System.out.println("sample B - canInvoice:" + canInvoice);

        //C.费用相同,上门多加一个测试项目
        fee.setExpectCharge(100.0);
        fee.setOrderCharge(100.0);
        OrderDetail detailTest = new OrderDetail(2l);
        detailTest.setProduct(new Product(1l));
        detailTest.setServiceType(new ServiceType(9l));
        detailTest.setQty(1);
        details.add(detailTest);
        canInvoice = orderService.compareOrderDetail(order);
        System.out.println("sample C - canInvoice:" + canInvoice);

        //D.上门是检测，而不是安装
        details.clear();
        detail1.setServiceType(new ServiceType(9l));
        details.add(detail1);
        canInvoice = orderService.compareOrderDetail(order);
        System.out.println("sample D - canInvoice:" + canInvoice);

        //E.下单:检测，上门：检测
        items.clear();
        item1.setServiceType(new ServiceType(9l));
        items.add(item1);
        canInvoice = orderService.compareOrderDetail(order);
        System.out.println("sample E - canInvoice:" + canInvoice);

        //F.下单:检测，上门：安装
        items.clear();
        item1.setServiceType(new ServiceType(9l));
        items.add(item1);

        details.clear();
        detail1.setServiceType(new ServiceType(azTypeId));
        details.add(detail1);
        canInvoice = orderService.compareOrderDetail(order);
        System.out.println("sample F - canInvoice:" + canInvoice);

        //G.下单：安装 Qty:2 上门：安装 Qty:1
        items.clear();
        item1.setServiceType(new ServiceType(azTypeId));
        item1.setQty(2);
        items.add(item1);

        details.clear();
        detail1.setServiceType(new ServiceType(azTypeId));
        detail1.setQty(2);
        details.add(detail1);
        canInvoice = orderService.compareOrderDetail(order);
        System.out.println("sample G - canInvoice:" + canInvoice);
//        Assert.assertTrue(canInvoice);

    }
     */

    /**
     * 测试订单是否可以自动结账

    @Test
    public void testOrderCanAutoCharge(){
        Long orderId = 1012157175503654912l;//W2017081200022
        Order order = orderService.getOrderById(orderId,"20182", OrderUtils.OrderDataLevel.DETAIL,true);
        Boolean canInvoice = orderService.compareOrderDetail(order);
        log.info("compareOrderDetail- order id:{} result:{}",orderId,canInvoice);
        //System.out.println("Order:" + orderId.toString() + ",can auto charge:" + canInvoice);
        Assert.assertTrue("can not auto charge",canInvoice);
    } */

    /**
     * 对账时检查订单费用是否一致
     */
    @Test
    public void testcheckOrderFeeAndServiceAmount(){
        Long orderId = 1010431238256857088l;
        Order order = orderService.getOrderById(orderId,"20182", OrderUtils.OrderDataLevel.DETAIL,true);
        Boolean rst = orderService.checkOrderFeeAndServiceAmount(order,false);
        //System.out.println(String.format("check result:%s",rst));
        log.info("checkOrderFeeAndServiceAmount:{}",rst);
        Assert.assertTrue("check fail",rst);
    }

    /**
     * 客评时检查订单费用是否一致
     */
    @Test
    public void testcheckOrderFeeAndServiceAmountBeforeGrade(){
        Long orderId = 1011436187883278336l;
        Order order = orderService.getOrderById(orderId,"20182", OrderUtils.OrderDataLevel.DETAIL,true);
        Boolean rst = orderService.checkOrderFeeAndServiceAmountBeforeGrade(order,true);
        //System.out.println(String.format("check result:%s",rst));
        log.info("checkOrderFeeAndServiceAmount:{}",rst);
        Assert.assertTrue("check fail",rst);
    }

    @Test
    /**
     * 测试客户日下单统计数据
     */
    public void testUpdateCustomerDialyPlan() {
//        Customer customer = customerService.getFromCache(1058l);
//        CustomerFinance finance = customerService.getFinance(1058l);
//        Date date = new Date();
//        MQCustomerModel c = new MQCustomerModel();
//        c.setId(customer.getId());
//        c.setCode(customer.getCode());
//        c.setName(customer.getName());
//        c.setSalesId(customer.getSales().getId());
//        c.setSalesMan(customer.getSales().getName());
//        c.setPaymentType(Integer.parseInt(finance.getPaymentType().getValue()));
//        c.setPaymentTypeName(finance.getPaymentType().getLabel());
//        mqOrderReportService.updateCustomerDialyPlan(c, date, 1,20.0);
    }

    @Test
    /**
     * 测试获得随机客户
     */
    public void testGetRandomKefu() {
        long cid = 1332L;
        long areaId = 3403l;
        long pCategoryId = 1L;
        //1.vip客户，有区域，有品类
        User kefu = orderService.getRandomKefu(cid,areaId,pCategoryId,0,176L,13L);
        //Assert.assertNotNull(kefu);
        if(kefu == null){
            log.error("1.customer:{} area:{} productCategory:{}",cid,areaId,pCategoryId,0);
        }else {
            log.warn("1.kefu:{} name:{}", kefu.getId(), kefu.getName());
        }
        //2.vip客户，有区域，无品类
        pCategoryId = 10L;
        kefu = orderService.getRandomKefu(cid,areaId,pCategoryId,0,176L,13L);
        //Assert.assertNotNull(kefu);
        if(kefu == null){
            log.error("2.customer:{} area:{} productCategory:{}",cid,areaId,pCategoryId,0);
        }else {
            log.warn("2.kefu:{} name:{}", kefu.getId(), kefu.getName());
        }
        //3.vip客户，无区域
        areaId = 4528l;
        pCategoryId = 1L;
        kefu = orderService.getRandomKefu(cid,areaId,pCategoryId,0,176L,13L);
        if(kefu == null){
            log.error("3.customer:{} area:{} productCategory:{}",cid,areaId,pCategoryId);
        }else {
            log.warn("3.kefu:{} name:{}", kefu.getId(), kefu.getName());
        }
        //4.非vip客户
        cid= 1482L;
        areaId = 3403l;
        pCategoryId = 1L;
        kefu = orderService.getRandomKefu(cid,areaId,pCategoryId,0,176L,13L);
        if(kefu == null){
            log.error("4.customer:{} area:{} productCategory:{}",cid,areaId,pCategoryId);
        }else {
            log.warn("4.kefu:{} name:{}", kefu.getId(), kefu.getName());
        }
    }


    @Test
    public void getOrderFromMasterDb(){
        Long orderId = 914847302739431424l;
        String quarter = "20174";
        Order order = orderService.getOrderFromMasterDb(orderId,quarter);
        Assert.assertNotNull(order);
        Assert.assertNotNull(order.getOrderCondition());
        Assert.assertNotNull(order.getOrderFee());
        System.out.println(GsonUtils.getInstance().toGson(order));
    }

    @Test
    public void getLastOrderLog(){
        Long orderId = 915771149235916800l;
        String quarter = "20174";
        Dict prvStatus = orderService.getLastOrderLog(orderId,quarter,Order.ORDER_STATUS_RETURNING.toString());
        if(prvStatus == null){
            System.out.println("错误");
        }else{
            System.out.println("status value:" + prvStatus.getValue() + " label:" + prvStatus.getLabel());
        }
    }

    /**
     * 检查是否可以自动产生对账单
     */
    @Test
    public void testCanAutoCharge(){
        List<Long> ids = Lists.newArrayList(995865557423230976l,996229851184762880l,996709234630729728l);
        Long orderId;
        String quarter =    "20182";
        Order order;
        for(int i=0,size=ids.size();i<size;i++){
            orderId = ids.get(i);
            order = orderService.getOrderById(orderId,quarter, OrderUtils.OrderDataLevel.DETAIL,true);
            Boolean canCharge = compareOrderDetail(order);
            log.info(">>>order:{} can charge:{}",orderId,canCharge);
            try{
                Thread.sleep(300);
            }catch (Exception e){}
        }
    }

    /**
     * 是否可以自动产生对账单的实现逻辑
     * @param order
     * @return
     */
    public Boolean compareOrderDetail(Order order)
    {
        //检查订单
        if(order == null || order.getOrderFee()==null || order.getOrderCondition()==null
                || order.getDetailList().size()==0 || order.getItems().size()==0){
            return false;
        }
        //检查安装服务类型
        String azCode = new String("II");
        List<ServiceType> serviceTypes = serviceTypeService.findAllList();
        ServiceType azServiceType = serviceTypes.stream().filter(t->t.getCode().equalsIgnoreCase(azCode)).findFirst().orElse(null);
        if (azServiceType==null){
            return false;
        }
        final Long azServiceTypeId = azServiceType.getId();
		/*
		String azId = String.format(",%,",azServiceType.getId());
		if(order.getOrderCondition().getServiceTypes().indexOf(azId)==-1){
			//不是安装单
			return false;
		}*/
        //非安装单，排除，如果订单中一安装，一维修，不能自动结账
//        long azcnt = order.getDetailList().stream().filter(t->Objects.equals(t.getServiceType().getId(),azServiceTypeId)).count();
//        System.out.println("安装服务数:" + azcnt);
//        System.out.println("订单项目数:" + order.getItems().size());
//        //1.非安装单 azcnt =0
//        //2.订单中多个服务项目，有安装，也有其它服务 azcnt>0
//        if(azcnt==0 || azcnt != order.getItems().size()){
//            return false;
//        }

        //非安装单，排除，如果订单中一安装，一维修，不能自动结账
        long nonazcnt = order.getDetailList().stream().filter(t->!Objects.equals(t.getServiceType().getId(),azServiceTypeId)).count();
        System.out.println("非安装服务数:" + nonazcnt);
        System.out.println("订单项目数:" + order.getItems().size());
        //1.非安装单 nonazcnt >0
        //2.订单项目数=0
        if(nonazcnt>0 || order.getItems().size()==0){
            return false;
        }

        //应收金额一致,项次数量一致 才可自动生成对帐单（也有可能出现安装单->维修单）
        OrderFee orderFee = order.getOrderFee();
        System.out.println("下单服务金额:" + orderFee.getExpectCharge());
        System.out.println("上门服务金额:" + orderFee.getOrderCharge());

        System.out.println("下单服务项目数:" + order.getItems().size());
        System.out.println("上门服务项目数:" + order.getDetailList().size());
        if(!Objects.equals(orderFee.getExpectCharge(),orderFee.getOrderCharge())
                || order.getDetailList().size() != order.getItems().size()){
            return false;
        }
        //实际上门明细费用判断，配件费、其它、远程费、快递费，不能自动对账
        OrderDetail detail;
        List<OrderDetail> details = order.getDetailList();
        for(int i=0,size=order.getDetailList().size();i<size;i++){
            detail = details.get(i);
            System.out.println(String.format("id:%s,product:%s,service:%s,qty:%s",detail.getId(),detail.getProduct().getName(),detail.getServiceType().getName(),detail.getQty()));
            //应收
//            if(logger.isDebugEnabled()){
//                System.out.println("Material Charge:"+detail.getMaterialCharge().toString());
//                System.out.println("Other Charge:"+detail.getOtherCharge().toString());
//                System.out.println("Express Charge:"+detail.getExpressCharge().toString());
//                System.out.println("Travel Charge:"+detail.getTravelCharge().toString());
//            }
            if(detail.getMaterialCharge()>0 || detail.getOtherCharge()>0
                    || detail.getTravelCharge()>0 || detail.getExpressCharge()>0){
                return false;
            }
            //应付
//            if(logger.isDebugEnabled()){
//                System.out.println("Engineer Material Charge:"+detail.getEngineerMaterialCharge().toString());
//                System.out.println("Engineer Other Charge:"+detail.getEngineerOtherCharge().toString());
//                System.out.println("Engineer Express Charge:"+detail.getEngineerExpressCharge().toString());
//                System.out.println("Engineer Travel Charge:"+detail.getEngineerTravelCharge().toString());
//            }
            if(detail.getEngineerMaterialCharge()>0 || detail.getEngineerOtherCharge()>0 ||
                    detail.getEngineerTravelCharge()>0 || detail.getEngineerExpressCharge()>0){

                return false;
            }
        }

        //下单明细
        StringBuilder itemstring = new StringBuilder(200);
        order.getItems().stream()
                .sorted(Comparator.comparing(OrderItem::getProductId))
                .forEach(t->{
                    itemstring.append("S#").append(t.getServiceType().getId()).append("#S")
                            .append("P#").append(t.getProduct().getId()).append("#P")
                            .append("Q#").append(t.getQty()).append("#Q");
                });
        //实际上门明细,只读取安装项目
        StringBuilder detailstring = new StringBuilder(200);
        order.getDetailList().stream()
                .filter(t->Objects.equals(t.getServiceType().getId(),azServiceTypeId))
                .sorted(Comparator.comparing(OrderDetail::getProductId))
                .forEach(t->{
                    detailstring.append("S#").append(t.getServiceType().getId()).append("#S")
                            .append("P#").append(t.getProduct().getId()).append("#P")
                            .append("Q#").append(t.getQty()).append("#Q");
                });
        System.out.println("下单服务明细："+itemstring.toString());
        System.out.println("上门服务明细："+detailstring.toString());
        return itemstring.toString().equals(detailstring.toString());
    }

    /**
     * 测试装载问题反馈消息统计内容到缓存
     */
    @Test
    public void TestLoadFeedbackGroupToRedis(){
        //1.客服
        System.out.println("服务启动任务：客服问题反馈 ");
        //1.1.by customer
        List<NoticeMessageItemVM> items = orderService.groupFeedbackByKefuOfCustomer();
        System.out.println("1.1.For Kefu by Customer:" + items==null?0:items.size());
        if(items != null && items.size()>0){

            redisTemplate.executePipelined(new RedisCallback<Object>() {    // enable Redis Pipeline
                @Override
                public Object doInRedis(RedisConnection connection) throws DataAccessException {
                    connection.select(RedisConstant.RedisDBType.REDIS_MS_DB.ordinal());
                    final byte[] feedbackbycustomer = RedisConstant.MS_FEEDBACK_KEFUBYCUSTOMER.getBytes(StandardCharsets.UTF_8);
                    final byte[] feedbackbyarea = RedisConstant.MS_FEEDBACK_KEFUBYAREA.getBytes(StandardCharsets.UTF_8);
                    final byte[] pendingbycustomer = RedisConstant.MS_FEEDBACK_PENDING_KEFUBYCUSTOMER.getBytes(StandardCharsets.UTF_8);
                    final byte[] pendingbyarea = RedisConstant.MS_FEEDBACK_KEFUBYAREA.getBytes(StandardCharsets.UTF_8);

                    items.forEach(t -> {
                        if(t.getNoticeType()==NoticeMessageItemVM.NOTICE_TYPE_FEEDBACK) {
                            connection.hSet(feedbackbycustomer, String.valueOf(t.getCustomerId()).getBytes(StandardCharsets.UTF_8), StringUtils.getBytes(String.valueOf(t.getQty())));
                        }else if(t.getNoticeType()==NoticeMessageItemVM.NOTICE_TYPE_FEEDBACK_PENDING) {
                            connection.hSet(pendingbycustomer, String.valueOf(t.getCustomerId()).getBytes(StandardCharsets.UTF_8), StringUtils.getBytes(String.valueOf(t.getQty())));
                        }
                    });
                    return null;
                }
            });
        }
        //1.2.by area
        List<NoticeMessageItemVM> items1 = orderService.groupFeedbackByKefuOfArea();
        System.out.println("1.2.For Kefu by Area:" + items1==null?0:items1.size());
        if(items1 != null && items1.size()>0){
            redisTemplate.executePipelined(new RedisCallback<Object>() {    // enable Redis Pipeline
                @Override
                public Object doInRedis(RedisConnection connection) throws DataAccessException {
                    connection.select(RedisConstant.RedisDBType.REDIS_MS_DB.ordinal());
                    final byte[] feedbackbycustomer = RedisConstant.MS_FEEDBACK_KEFUBYCUSTOMER.getBytes(StandardCharsets.UTF_8);
                    final byte[] feedbackbyarea = RedisConstant.MS_FEEDBACK_KEFUBYAREA.getBytes(StandardCharsets.UTF_8);
                    final byte[] pendingbycustomer = RedisConstant.MS_FEEDBACK_PENDING_KEFUBYCUSTOMER.getBytes(StandardCharsets.UTF_8);
                    final byte[] pendingbyarea = RedisConstant.MS_FEEDBACK_KEFUBYAREA.getBytes(StandardCharsets.UTF_8);

                    items1.forEach(t -> {
                        if(t.getNoticeType()==NoticeMessageItemVM.NOTICE_TYPE_FEEDBACK) {
                            System.out.println("feedback area:" +  String.valueOf(t.getAreaId()) + " qty:" + String.valueOf(t.getQty()));
                            connection.hSet(feedbackbyarea, String.valueOf(t.getAreaId()).getBytes(StandardCharsets.UTF_8), StringUtils.getBytes(String.valueOf(t.getQty())));
                        }else if(t.getNoticeType()==NoticeMessageItemVM.NOTICE_TYPE_FEEDBACK_PENDING) {
                            System.out.println("pending qty:" +  String.valueOf(t.getAreaId()) + " qty:" + String.valueOf(t.getQty()));
                            connection.hSet(pendingbyarea, String.valueOf(t.getAreaId()).getBytes(StandardCharsets.UTF_8), StringUtils.getBytes(String.valueOf(t.getQty())));
                        }
                    });
                    return null;
                }
            });
        }

        //2.For 客户 and 业务
        List<NoticeMessageItemVM> items2 = orderService.groupFeedbackByCustomer();
        System.out.println("2.For customer:" + items2==null?0:items2.size());
        if(items2 != null && items2.size()>0){
            redisTemplate.executePipelined(new RedisCallback<Object>() {    // enable Redis Pipeline
                @Override
                public Object doInRedis(RedisConnection connection) throws DataAccessException {
                    connection.select(RedisConstant.RedisDBType.REDIS_MS_DB.ordinal());

                    items2.forEach(t -> {
                        if(t.getNoticeType()==NoticeMessageItemVM.NOTICE_TYPE_FEEDBACK) {
                            final byte[] bkey = String.format(RedisConstant.MS_FEEDBACK_CUSTOMER,t.getCustomerId()).getBytes(StandardCharsets.UTF_8);
                            connection.hSet(bkey, String.valueOf(t.getCreateBy()).getBytes(StandardCharsets.UTF_8), StringUtils.getBytes(String.valueOf(t.getQty())));
                        }else if(t.getNoticeType()==NoticeMessageItemVM.NOTICE_TYPE_FEEDBACK_PENDING) {
                            final byte[] bkey = String.format(RedisConstant.MS_FEEDBACK_PENDING_CUSTOMER,t.getCustomerId()).getBytes(StandardCharsets.UTF_8);
                            connection.hSet(bkey, String.valueOf(t.getCreateBy()).getBytes(StandardCharsets.UTF_8), StringUtils.getBytes(String.valueOf(t.getQty())));
                        }
                    });
                    return null;
                }
            });
        }

    }

    @Test
    public void getQuarterByNo(){
        System.out.println("quarter:" + QuarterUtils.getOrderQuarterFromNo("K2017100100001"));
        System.out.println("quarter:" + QuarterUtils.getOrderQuarterFromNo("K20171001000011"));
        System.out.println("quarter:" + QuarterUtils.getOrderQuarterFromNo("K2018011200203"));
    }


    @Test
    public void testUpdateOrderServicePointFeeByMaps(){
        HashMap<String,Object> maps = Maps.newHashMap();
        maps.put("quarter","20181");
        maps.put("orderId",979544089206853632L);
        maps.put("servicePointId",1622038L);
        //maps.put("serviceCharge",10.00);
        //maps.put("expressCharge",5.00);
        maps.put("timeLinessCharge",5.00);
        maps.put("timeLiness",2.00);

        orderDao.updateOrderServicePointFeeByMaps(maps);

    }

    /**
     * 测试获得客户时效信息
     */
    @Test
    public void testGetCustomerTimeLinessInfo(){
        Long orderId = 1005390692962406400l;
        String quarter = "20182";
        Order order = orderService.getOrderById(orderId,quarter, OrderUtils.OrderDataLevel.DETAIL,true);
        try {
            Dict customerTimeLinessInfo = orderService.getCustomerTimeLinessInfo(Order.TimeLinessType.ALL, order.getOrderCondition());
            if(customerTimeLinessInfo != null){
                log.info("用时:{} 等级:{} 应收金额:{} 应付金额:{}",customerTimeLinessInfo.getType(),customerTimeLinessInfo.getValue(),customerTimeLinessInfo.getLabel(),customerTimeLinessInfo.getRemarks());
                //type:用时 value:等级 label:金额
            }
            Assert.assertNotNull("is null",customerTimeLinessInfo);
        }catch (Exception e){
            log.error("计算客户时效费用错误：{}",orderId,e);
        }
    }

    /**
     * 测试获得客户时效信息
     */
    @Test
    public void testGetServicePointTimeLinessInfo(){
        Order.TimeLinessType timeLinessType = Order.TimeLinessType.ALL;
        Long orderId = 1005390692962406400l;
        String quarter = "20182";
        Order order = orderService.getOrderById(orderId,quarter, OrderUtils.OrderDataLevel.DETAIL,true);
        Date startDate = DateUtils.parseDate("2019-05-18 10:06:59");
        Set<Long> cids = null;
        long productCategoryId = Optional.ofNullable(order.getOrderCondition()).map(t->t.getProductCategoryId()).orElse(0L);
        try {
            //cids = order.getItems().stream().map(t -> t.getProduct().getCategory().getId()).distinct().collect(Collectors.toSet());
            //grade 1
            Dict timeLinessInfo = orderService.getServicePointTimeLinessInfo(timeLinessType, order.getOrderCondition(),startDate,productCategoryId,1);
            if(timeLinessInfo != null){
                log.info("manu grade:1 - 用时:{} 等级:{} 金额:{}",timeLinessInfo.getType(),timeLinessInfo.getValue(),timeLinessInfo.getLabel());
                //type:用时 value:等级 label:金额
            }
            Assert.assertNotNull("is null",timeLinessInfo);

            //grade 2
            timeLinessInfo = orderService.getServicePointTimeLinessInfo(timeLinessType, order.getOrderCondition(),startDate,productCategoryId,2);
            if(timeLinessInfo != null){
                log.info("manu grade:1 - 用时:{} 等级:{} 金额:{}",timeLinessInfo.getType(),timeLinessInfo.getValue(),timeLinessInfo.getLabel());
                //type:用时 value:等级 label:金额
            }
            Assert.assertNotNull("is null",timeLinessInfo);
            /*
            timeLinessInfo = orderService.getServicePointTimeLinessInfo(timeLinessType, order.getOrderCondition(),startDate,cids,null);
            if(timeLinessInfo != null){
                log.info("auto grade:1 - 用时:{} 等级:{} 金额:{}",timeLinessInfo.getType(),timeLinessInfo.getValue(),timeLinessInfo.getLabel());
                //type:用时 value:等级 label:金额
            }
            Assert.assertNotNull("is null",timeLinessInfo);
            */
        }catch (Exception e){
            log.error("计算网点时效费(快可立补贴)错误：{}",orderId,e);
        }
    }

    /**
     * 测试根据开始日期及结束日期获得查询用到的分片列表
     */
    @Test
    public void testGetQuarters(){
        Date startDate = null;
        Date endDate = null;
        Date[] dates = OrderUtils.getQuarterDates(startDate,endDate,0,0);
        log.info("dates[0]:{} dates[1]:{}",DateUtils.formatDate(dates[0],"yyyy-MM-dd"),DateUtils.formatDate(dates[1],"yyyy-MM-dd"));
        List<String> quarters = QuarterUtils.getQuarters(dates[0],dates[1]);
        if(quarters != null && quarters.size()>0){
            log.info("default quarters:{}", Joiner.on(",").join(quarters));
        }
        //当前日期前推6个月
        dates = OrderUtils.getQuarterDates(startDate,endDate,6,0);
        log.info("dates[0]:{} dates[1]:{}",DateUtils.formatDate(dates[0],"yyyy-MM-dd"),DateUtils.formatDate(dates[1],"yyyy-MM-dd"));
        quarters = QuarterUtils.getQuarters(dates[0],dates[1]);
        if(quarters != null && quarters.size()>0){
            log.info(" quarters:{}", Joiner.on(",").join(quarters));
        }

        startDate = DateUtils.parseDate("2016-01-01");
        endDate = DateUtils.parseDate("2018-05-31");
        dates = OrderUtils.getQuarterDates(startDate,endDate,0,0);
        log.info("dates[0]:{} dates[1]:{}",DateUtils.formatDate(dates[0],"yyyy-MM-dd"),DateUtils.formatDate(dates[1],"yyyy-MM-dd"));
        quarters = QuarterUtils.getQuarters(dates[0],dates[1]);
        if(quarters != null && quarters.size()>0){
            log.info(" quarters:{}", Joiner.on(",").join(quarters));
        }

        startDate = DateUtils.parseDate("2018-02-01");
        endDate = DateUtils.parseDate("2018-05-31");
        dates = OrderUtils.getQuarterDates(startDate,endDate,0,0);
        log.info("dates[0]:{} dates[1]:{}",DateUtils.formatDate(dates[0],"yyyy-MM-dd"),DateUtils.formatDate(dates[1],"yyyy-MM-dd"));
        quarters = QuarterUtils.getQuarters(dates[0],dates[1]);
        if(quarters != null && quarters.size()>0){
            log.info(" quarters:{}", Joiner.on(",").join(quarters));
        }

        startDate = DateUtils.parseDate("2018-02-01");
        endDate = DateUtils.parseDate("2018-05-31");
        dates = OrderUtils.getQuarterDates(startDate,endDate,2,0);
        log.info("dates[0]:{} dates[1]:{}",DateUtils.formatDate(dates[0],"yyyy-MM-dd"),DateUtils.formatDate(dates[1],"yyyy-MM-dd"));
        quarters = QuarterUtils.getQuarters(dates[0],dates[1]);
        if(quarters != null && quarters.size()>0){
            log.info(" quarters:{}", Joiner.on(",").join(quarters));
        }
    }


    /**
     * 测试mycat的返回id和SequenceIdUtils的id是否类似
     */
    @Test
    public void testMyCatIdAndSequenceIdUtils(){
        Long mycatId = SeqUtils.NextIDValue(SeqUtils.TableName.Order);//mycat
        SequenceIdUtils sequenceIdUtils = new SequenceIdUtils(0,0);
        Long id = sequenceIdUtils.nextId();
        log.info("mycat:{} ,id:{}",mycatId,id);
    }

    /**
     * 测试客评时取加急费
     */
    @Test
    public void testgetGradeCustomerUrgentCharge(){
        Customer customer = new Customer(1476l);
        Long areaId = 3403l;
        Long urgentLevelId = 5l;
        Double hours = 10.0;
        UrgentChargeModel urgentChargeModel = orderService.getGradeCustomerUrgentCharge(customer,areaId,urgentLevelId,hours);
        if(urgentChargeModel == null){
            log.error("no any return");
        }else {
            log.info("{}", GsonUtils.toGsonString(urgentChargeModel));
        }
    }

    /**
     * 测试随机分配订单数
     */
    @Test
    public void testRandomAssignOrder(){
        int customerCount = 2501;//2500个，随机方法不取上限
        int rowCount = 200000;//20w
        int min = 1;
        int max = 20000;
        List<Integer> array = new ArrayList<>();
        for(int i=1;i<customerCount-1;i++){
            int safe_total=(rowCount-(customerCount-1-i)*min)/(customerCount-1-i);
            Double money= Math.random()*(safe_total-min)+min;
            BigDecimal money_bd=new BigDecimal(money);
            money = money_bd.setScale(0,BigDecimal.ROUND_HALF_UP).doubleValue();
            rowCount=rowCount-money.intValue();
            BigDecimal total_bd=new BigDecimal(rowCount);
            rowCount=total_bd.setScale(0,BigDecimal.ROUND_HALF_UP).intValue();
            array.add(money.intValue());
        }
        array.add(rowCount);

        for(Integer a:array){
            log.info("{}",a);
        }
    }

    /**
     * 测试检查订单重单-随机20w数据写入文件，及读取文件写入redis
     */
    @Test
    public void testrepeateOrderUsingRedisHashWrite(){
        Long start;
        Long end;
        int customerCount = 2501;//2500个，随机方法不取上限
        int rowCount = 200000;//20w
        int min = 1;
        int max = 20000;
        StringBuffer str = new StringBuffer();
        String filepath = "/Users/yanshenglu/Develope/java/myproject/KKL Plus/文档/test/check_repeate_orderno.txt";
        //customer 2400
        // record 20w

        /*1.create random data ,and write to text file  */
        // 随机将20w分组
        List<Integer> array = new ArrayList<>();
        for(int i=1;i<customerCount-1;i++){
            int safe_total=(rowCount-(customerCount-1-i)*min)/(customerCount-1-i);
            Double money= Math.random()*(safe_total-min)+min;
            BigDecimal money_bd=new BigDecimal(money);
            money = money_bd.setScale(0,BigDecimal.ROUND_HALF_UP).doubleValue();
            rowCount=rowCount-money.intValue();
            BigDecimal total_bd=new BigDecimal(rowCount);
            rowCount=total_bd.setScale(0,BigDecimal.ROUND_HALF_UP).intValue();
            array.add(money.intValue());
        }
        array.add(rowCount);
        FileWriter fw = null;
        try{
            start = System.currentTimeMillis();
            fw = new FileWriter(filepath);
            int qty;
            for (int i = 0,size=array.size(); i < size; i++) {
                qty = array.get(i);
                for(int j=0;j<qty;j++) {
                    str.setLength(0);
                    str.append("SD:REPEATE:ORDER:")
                            .append(i+1)
                            .append(";")
                            .append(String.valueOf(RandomUtils.nextLong(13000000000l, 19999999999l)))
                            .append(";")
                            .append("K20180705")
                            .append(StringUtils.leftPad(String.valueOf(RandomUtils.nextInt(1,100000)),5,"0"))
                            .append("\r\n");
                    fw.write(str.toString());
                }
            }
            fw.close();
            end = System.currentTimeMillis();
            log.info("写执行耗时:{} 毫秒" ,(end - start));
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                fw.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /* 2.read from text ,then write to redis
        File file = null;
        BufferedInputStream fis = null;
        BufferedReader reader = null;
        try {

            file = new File(filepath);
            fis = new BufferedInputStream(new FileInputStream(file));
            reader = new BufferedReader(new InputStreamReader(fis, "utf-8"), 5 * 1024 * 1024);// 用5M的缓冲读取文本文件

            String line = new String();
            String[] datas;
            //String:key,Map->String:field,Map->Object:value
            Map<String,Map<String,Object>> groups = Maps.newHashMap();
            Map<String,Object> map;
            while ((line = reader.readLine()) != null) {
                if(StringUtils.isNotBlank(line)) {
                    datas = StringUtils.split(line, ";");
                    if(groups.containsKey(datas[0])){
                        map = groups.get(datas[0]);
                        map.put(datas[1],datas[2]);
                        groups.put(datas[0],map);
                        //groups.get(datas[0]).put(datas[1],datas[1]);
                    }else{
                        map = Maps.newHashMap();
                        map.put(datas[1],datas[2]);
                        groups.put(datas[0],map);
                    }
                    //redisUtils.hmSet(datas[0],datas[1],datas[2],0l);
                }
            }
            start = System.currentTimeMillis();
            for (Map.Entry<String,Map<String,Object>> entry : groups.entrySet()) {
                redisUtils.hmSetAll(entry.getKey(),entry.getValue(),0l);
            }
            end = System.currentTimeMillis();
            log.warn("写redis用时：{} 毫秒",(end - start));
            reader.close();
            fis.close();
        }catch (Exception e){
            e.printStackTrace();
            try{
                reader.close();
                fis.close();
            }catch (Exception e1){
                e1.printStackTrace();
            }
        }*/
    }


    @Test
    public void testrepeateOrderUsingRedisHashRead(){
        //write 20w
        int count = 200000;
        String key = new String("");
        Long start = System.currentTimeMillis();
        String value = new String("");
        String field = new String("");
        for(int i=0;i<count;i++){
            key = String.format("SD:REPEATE:ORDER:%s", RandomUtils.nextInt(1,2400));
            field = String.valueOf(RandomUtils.nextLong(13000000000l,19999999999l));
            value = redisUtils.hGet(key,field,String.class);
            log.warn("key:{},value:{}",key,value);
        }
        Long end = System.currentTimeMillis();
        double time = 1.0d * (end - start) / 1000d;
        log.warn("写入用时：{} 秒",time);
    }

    @Test
    public void testRestOrderDetail(){
        long orderId = 1016964299329523712l;
        String quarter = "20183";
        RestLoginUserInfo userInfo = new RestLoginUserInfo();
        userInfo.setServicePointId(13120l);
        userInfo.setEngineerId(13714);
        userInfo.setUserId(31882l);
        userInfo.setPrimary(true);
        Order order = orderService.getOrderById(orderId, quarter, OrderUtils.OrderDataLevel.DETAIL, true);
        if (order == null || order.getOrderCondition() == null) {
            log.error("订单不存在，或读取订单");
        }
        //完成照片
        if (order.getOrderCondition().getFinishPhotoQty() > 0) {
            List<OrderAttachment> attachments = org.assertj.core.util.Lists.newArrayList();
            attachments = attachmentService.getAttachesByOrderId(orderId, order.getQuarter());
            if (attachments != null && attachments.size() > 0) {
                order.setAttachments(attachments);
            }
        }
        //配件
        if (1 == order.getOrderCondition().getPartsFlag()) {
            List<MaterialMaster> materials = orderMaterialService.findMaterialMasterHeadsByOrderId(orderId, order.getQuarter());
            order.setMaterials(materials);
        }
        //products
        Set<Product> products = orderService.getOrderProducts(order.getItems(), true, false);
        if (products.size() > 0) {
            order.setProducts(products.stream().collect(Collectors.toList()));
        }

        RestOrderDetailInfo entity = mapper.map(order, RestOrderDetailInfo.class);
        //进一步处理
        entity.setIsAppCompleted(0);
        if (!order.getOrderCondition().getServicePoint().getId().equals(new Long(userInfo.getServicePointId()))) {
            entity.setIsAppCompleted(1);
        } else if (StringUtils.isNoneBlank(order.getOrderCondition().getAppCompleteType())) {
            entity.setIsAppCompleted(1);
        }

        //预估服务费用，从派单记录表中取
        OrderCondition orderCondition = order.getOrderCondition();
        OrderPlan orderPlan = orderService.getOrderPlan(order.getId(), order.getQuarter(), orderCondition.getServicePoint().getId(), orderCondition.getEngineer().getId());
        if (orderPlan == null) {
            entity.setEstimatedServiceCost(0.0d);
        } else {
            entity.setEstimatedServiceCost(orderPlan.getEstimatedServiceCost() + orderPlan.getEstimatedTravelCost() + orderPlan.getEstimatedOtherCost());//=服务费+远程费+其它费用
        }
        if (entity.getServices().size() > 0) {
            //String input = "engineerServiceCharge=0.0,engineerTravelCharge=0.0,engineerExpressCharge=0.0,engineerMaterialCharge=0.0,engineerOtherCharge=0.0,engineerChage=0.0";
            //Map<String,String> fees =  Splitter.on(",")
            //        .omitEmptyStrings()
            //        .trimResults()
            //        .withKeyValueSeparator("=")
            //        .split(input);
            Map<String, Double> fees = Maps.newHashMap();
            fees.put("engineerServiceCharge", 0.0);
            fees.put("engineerTravelCharge", 0.0);
            fees.put("engineerExpressCharge", 0.0);
            fees.put("engineerMaterialCharge", 0.0);
            fees.put("engineerOtherCharge", 0.0);
            fees.put("engineerChage", 0.0);
            //安维只能看到自己做的上门服务
            List<RestOrderDetail> services = entity.getServices().stream()
                    .filter(t -> t.getServicePointId().equals(Long.valueOf(userInfo.getServicePointId()))
                            && t.getEngineerId().equals(Long.valueOf(userInfo.getEngineerId()))
                    )
                    .collect(Collectors.toList());
            if (services.size() > 0) {
                services.stream().forEach(t -> {
                    fees.put("engineerServiceCharge", fees.get("engineerServiceCharge") + t.getEngineerServiceCharge());
                    fees.put("engineerTravelCharge", fees.get("engineerTravelCharge") + t.getEngineerTravelCharge());
                    fees.put("engineerExpressCharge", fees.get("engineerExpressCharge") + t.getEngineerExpressCharge());
                    fees.put("engineerMaterialCharge", fees.get("engineerMaterialCharge") + t.getEngineerMaterialCharge());
                    fees.put("engineerOtherCharge", fees.get("engineerOtherCharge") + t.getEngineerOtherCharge());
                    fees.put("engineerChage", fees.get("engineerChage") + t.getEngineerChage());
                });
                entity.setServices(services);
                entity.setEngineerServiceCharge(fees.get("engineerServiceCharge"));
                entity.setEngineerTravelCharge(fees.get("engineerTravelCharge"));
                entity.setEngineerExpressCharge(fees.get("engineerExpressCharge"));
                entity.setEngineerMaterialCharge(fees.get("engineerMaterialCharge"));
                entity.setEngineerOtherCharge(fees.get("engineerOtherCharge"));
                entity.setEngineerCharge(fees.get("engineerChage"));
            } else {
                entity.setServices(org.assertj.core.util.Lists.newArrayList());
            }
        }
    }

    @Test
    public void checkRepeatOrder(){
        /* 旧版本，从数据库检查
        String orderNo = orderService.checkRepeateOrder("123", "", 1L, 30);
        orderNo = orderService.checkRepeateOrder("123", "456", 1L, 30);
        orderNo = "";
        */

        /* 新版本，从缓存检查*/
        String repeateOrderNo = orderService.getRepeateOrderNo(1006l,"13036815382");
        System.out.println("order No:" + repeateOrderNo);
        //Assert.assertEquals("重单方法检查错误","K2018092815269",repeateOrderNo);
        assertThat(repeateOrderNo, equalTo("K2018092815269"));

    }

    @Test
    public void testCheckUploadSNBeforeGrade(){
        Long orderId = 1010076615872483328l;
        String quarter = "20182";
        Order order = orderService.getOrderById(orderId,quarter, OrderUtils.OrderDataLevel.DETAIL,true);
        //List<Long> productIds = order.getDetailList().stream().map(t->t.getProduct().getId()).distinct().collect(Collectors.toList());
//        Boolean checkResult = orderService.checkOrderProductBarCode(orderId,quarter,order.getOrderCondition().getCustomer().getId());
//        System.out.println("check resutl:" + checkResult);
    }

    @Test
    public void testSaveGrade() throws IOException {
        /*
        Order order = orderService.getOrderById(1056086434664431616l,"20184", OrderUtils.OrderDataLevel.DETAIL,true);
        OrderGradeModel gradeModel = new OrderGradeModel();
        OrderCondition orderCondition = order.getOrderCondition();
        gradeModel.setServicePoint(orderCondition.getServicePoint());
        User u = orderCondition.getEngineer();
        Engineer engineer = new Engineer(u.getId());
        engineer.setName(u.getName());
        gradeModel.setEngineer(engineer);
        gradeModel.setOrderId(order.getId());
        gradeModel.setQuarter(order.getQuarter());
        gradeModel.setRushCloseFlag(0);
        gradeModel.setCreateBy(new User(51739l,"高丽姗"));
        gradeModel.setCreateDate(DateUtils.parseDate("2018-10-28 14:28:16"));

        List<OrderGrade> gradeList = Lists.newArrayList();
        OrderGrade grade = new OrderGrade();
        */


        /*from json
        String json = "{\"quarter\":\"20184\",\"orderId\":1056086434664431616,\"orderNo\":\"K2018102765604\",\"servicePoint\":{\"id\":1627598,\"name\":\"格力(世纪大街)\"},\"engineer\":{\"id\":34089,\"name\":\"邢春荣\"},\"point\":100,\"autoGradeFlag\":0,\"timeLiness\":0.43,\"gradeList\":[{\"gradeId\":1,\"gradeName\":\"时效性\",\"gradeItemId\":10001,\"gradeItemName\":\"24小时以内\",\"sort\":1,\"point\":30},{\"gradeId\":2,\"gradeName\":\"服务态度\",\"gradeItemId\":10006,\"gradeItemName\":\"非常好\",\"sort\":2,\"point\":30},{\"gradeId\":3,\"gradeName\":\"技术水平\",\"gradeItemId\":10009,\"gradeItemName\":\"专业\",\"sort\":3,\"point\":20},{\"gradeId\":4,\"gradeName\":\"收费\",\"gradeItemId\":10012,\"gradeItemName\":\"无额外收费、有收费但属于正常标准收费\",\"sort\":4,\"point\":20}],\"createBy\":{\"id\":51739,\"name\":\"高丽姗\"},\"createDate\":\"2018-10-28 14:28:16\"}";
        OrderGradeModel gradeModel = OrderGradeModelAdapter.getInstance().fromJson(json);
        Assert.assertTrue("json 解析失败",gradeModel != null);
        Assert.assertEquals("20184",gradeModel.getQuarter());
        */
        //from message
        /*
        {
            "quarter":"20184",
                "orderId":1056086434664431616,
                "orderNo":"K2018102765604",
                "servicePoint":{
                    "id":1627598,
                            "name":"格力(世纪大街)"
                },
            "engineer":{
                    "id":34089,
                            "name":"邢春荣"
                },
            "point":100,
                "autoGradeFlag":0,
                "timeLiness":0.43,
                "gradeList":[
                        {
                            "gradeId":1,
                                "gradeName":"时效性",
                                "gradeItemId":10001,
                                "gradeItemName":"24小时以内",
                                "sort":1,
                                "point":30
                        },
                        {
                            "gradeId":2,
                                "gradeName":"服务态度",
                                "gradeItemId":10006,
                                "gradeItemName":"非常好",
                                "sort":2,
                                "point":30
                        },
                        {
                            "gradeId":3,
                                "gradeName":"技术水平",
                                "gradeItemId":10009,
                                "gradeItemName":"专业",
                                "sort":3,
                                "point":20
                        },
                        {
                            "gradeId":4,
                                "gradeName":"收费",
                                "gradeItemId":10012,
                                "gradeItemName":"无额外收费、有收费但属于正常标准收费",
                                "sort":4,
                                "point":20
                        }
                ],
            "createBy":{
                    "id":51739,
                            "name":"高丽姗"
                },
            "createDate":"2018-10-28 14:28:16"
        }
        */
        MQOrderGradeMessage.OrderGradeMessage message = MQOrderGradeMessage.OrderGradeMessage.newBuilder()
                .setQuarter("20184")
                .setOrderId(1056086434664431616l)
                .setOrderNo("K2018102765604")
                .setServicePoint(
                        MQCommon.User.newBuilder()
                                .setId(1627598)
                                .setName("格力(世纪大街)")
                                .build()
                )
                .setEngineer(
                        MQCommon.User.newBuilder()
                                .setId(34089)
                                .setName("邢春荣")
                                .build()
                )
                .setPoint(100)
                .setAutoGradeFlag(0)
                .setTimeLiness(0.43)
                .setCreateBy(
                        MQCommon.User.newBuilder()
                        .setId(51739l)
                        .setName("高丽姗")
                        .build()
                )
                .setCreateDate(DateUtils.parseDate("2018-10-28 14:28:16").getTime())
                .addItems(
                        MQOrderGradeMessage.GradeItemMessage.newBuilder()
                        .setGradeId(1)
                        .setGradeName("时效性")
                        .setGradeItemId(10001)
                        .setGradeItemName("24小时以内")
                        .setSort(1)
                        .setPoint(30)
                )
                .addItems(
                        MQOrderGradeMessage.GradeItemMessage.newBuilder()
                                .setGradeId(2)
                                .setGradeName("服务态度")
                                .setGradeItemId(10006)
                                .setGradeItemName("非常好")
                                .setSort(2)
                                .setPoint(30)
                )
                .addItems(
                        MQOrderGradeMessage.GradeItemMessage.newBuilder()
                                .setGradeId(3)
                                .setGradeName("技术水平")
                                .setGradeItemId(10009)
                                .setGradeItemName("专业")
                                .setSort(3)
                                .setPoint(20)
                )
                .addItems(
                        MQOrderGradeMessage.GradeItemMessage.newBuilder()
                                .setGradeId(4)
                                .setGradeName("收费")
                                .setGradeItemId(10012)
                                .setGradeItemName("有点贵（超出10%-30%）")
                                .setSort(4)
                                .setPoint(20)
                )
                .build();
        OrderGradeModel gradeModel = Mappers.getMapper(OrderGradeMessageMapper.class).mqToModel(message);
        String json = OrderGradeModelAdapter.getInstance().toJson(gradeModel);
        json = GsonUtils.MyCatJsonFormat(json);
        gradeModel.setContent(json);
        gradeDao.insertOrderGrade(gradeModel);

    }

    /**
     * 测试数字分组
     * 比如最大数值为2500，每组1000个，分成三组，如下：
     * 1 ~ 1000
     * 1001 ~ 2000
     * 2001 ~ 2500
     */
    @Test
    public void testNumberGroup(){
        long maxId = 2500;
        long minId = 1;
        long perOfGroup = 600;
        Double d = Math.ceil(1.0*maxId/perOfGroup);
        int grpNum = d.intValue();
        List<LongRange> customerIdGroup = Lists.newArrayList();
        for(int i=0;i<grpNum;i++){
            if(i == grpNum-1){
                customerIdGroup.add(new LongRange(i*perOfGroup+1,maxId));
            }else{
                customerIdGroup.add(new LongRange(i*perOfGroup+1,perOfGroup*(i+1)));
            }
        }

        customerIdGroup.stream().forEach(t->{
            System.out.println("start:" + t.getMinimumLong() + " end:" + t.getMaximumLong());
        });

    }

    @Test
    public void testLoadOrderRepeateCheck(){
        long start = System.currentTimeMillis();
        Date endDate = DateUtils.getEndOfDay(new Date());
        Date beginDate1 = DateUtils.addDays(endDate,-30);
        Date beginDate = DateUtils.getDateStart(beginDate1);
        List<String> quarters = QuarterUtils.getQuarters(beginDate, endDate);
        //all customers
        //按客户分多次取数据
        List<Customer> customers = CustomerUtils.getCustomerList();
        //long minId = customers.stream().map(t->t.getId()).reduce(Long::min).get();
        long maxId = customers.stream().map(t->t.getId()).reduce(Long::max).get();
        long minId = 1;
        long perOfGroup = 600;
        Double d = Math.ceil(1.0*maxId/perOfGroup);
        int grpNum = d.intValue();
        List<LongRange> customerIdGroups = com.google.common.collect.Lists.newArrayList();
        for(int i=0;i<grpNum;i++){
            if(i == grpNum-1){
                customerIdGroups.add(new LongRange(i*perOfGroup+1,maxId));
            }else{
                customerIdGroups.add(new LongRange(i*perOfGroup+1,perOfGroup*(i+1)));
            }
        }
        Map<Long,List<RepeateOrderVM>> maps = Maps.newHashMap();
        CompletableFuture[] cfs = customerIdGroups.stream()
                .map(range -> CompletableFuture.supplyAsync(() -> {
                            List<RepeateOrderVM> list = orderTaskDao.getOrderCreateInfo(null,quarters,
                                    beginDate,endDate,
                                    range.getMinimumLong(),range.getMaximumLong()
                            );
                            return list;
                        }).whenComplete((s, t) -> {
                            if (t != null) {
                                System.out.println(t.getMessage());
                            }else {
                                maps.put(range.getMinimumLong(), s);
                            }
                        })
                ).toArray(CompletableFuture[]::new);
        CompletableFuture.allOf(cfs).join();
        List<RepeateOrderVM> list;
        for(Map.Entry<Long,List<RepeateOrderVM>> entry :maps.entrySet()){
            list = entry.getValue();
            System.out.println("key: " + entry.getKey() + " ,size:" + list.size());
        }
        long end = System.currentTimeMillis();
        System.out.println("finsih write cache ,use time:" + (end-start)/1000.0);
    }

    @Test
    public void testReloadCheckRepeatOrderCache(){
        Date endDate = DateUtils.getEndOfDay(new Date());
        Date beginDate1 = DateUtils.addDays(endDate,-30);
        Date beginDate = DateUtils.getDateStart(beginDate1);
        orderTaskService.reloadCheckRepeatOrderCache(beginDate,endDate);
    }

    @Test
    public void checkOrderProductBarCode(){
        long orderId = 1064426913634205696l;
        String quarter = "20184";
        Order order = orderService.getOrderById(orderId,quarter, OrderUtils.OrderDataLevel.DETAIL,true);
        OrderCondition orderCondition = order.getOrderCondition();
        Boolean checkResult = orderService.checkOrderProductBarCode(orderId,quarter,orderCondition.getCustomer().getId(),order.getDetailList());
        System.out.println(checkResult);

    }

    /**
     * 测试TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();后面sql是否执行
     */
    @Test
    public void testSetRollbackOnly(){
        testService.testSetRollbackOnly();
    }

    /**
     * 测试复杂排序
     * 1.先按服务类型，安装优先
     * 2.再按套组优先
     */
    @Test
    public void testSorted(){
        List<OrderItem> items = Lists.newArrayList();
        //单品，安装
        OrderItem item1 = new OrderItem();
        item1.setId(1l);
        Product p1 = new Product();
        p1.setId(1l);
        p1.setName("热水器");
        p1.setSetFlag(0);
        item1.setProduct(p1);
        item1.setBrand("阿诗丹顿");
        item1.setProductSpec("规格1");
        item1.setStandPrice(45.0);
        item1.setDiscountPrice(35.0);
        item1.setItemNo(1);
        item1.setDelFlag(0);
        item1.setServiceType(new ServiceType(1l,"II","安装"));
        items.add(item1);

        //套组，检测
        OrderItem item2 = new OrderItem();
        item2.setId(2l);
        Product p2 = new Product();
        p2.setId(2l);
        p2.setName("烟机+灶具");
        p2.setSetFlag(1);
        item2.setProduct(p2);
        item2.setBrand("樱花");
        item2.setProductSpec("spec 2");
        item2.setStandPrice(75.0);
        item2.setDiscountPrice(50.0);
        item2.setItemNo(2);
        item2.setDelFlag(0);
        item2.setServiceType(new ServiceType(3l,"IT","检测"));
        items.add(item2);

        //套组，安装
        OrderItem item3 = new OrderItem();
        item3.setId(3l);
        Product p3 = new Product();
        p3.setId(3l);
        p3.setName("燃气热水器+灶具");
        p3.setSetFlag(1);
        item3.setProduct(p3);
        item3.setBrand("樱花");
        item3.setProductSpec("spec 2");
        item3.setStandPrice(75.0);
        item3.setDiscountPrice(50.0);
        item3.setItemNo(3);
        item3.setDelFlag(0);
        item3.setServiceType(new ServiceType(1l,"II","安装"));
        items.add(item3);

        OrderItem firstItem = null;

        List<OrderItem> list = items.stream().sorted(
                Comparator.comparing(OrderItem::getProduct,(x,y)->{
                    //再按套组邮箱
                    if(x == null && y != null){
                        return 1;
                    }else if(x !=null && y == null){
                        return -1;
                    }else if(x == null && y == null){
                        return -1;
                    }else {
                        if(x.getId().equals(y.getId())){
                            return 0;
                        }else if(x.getSetFlag() == 1){
                            return -1;
                        }else{
                            return 1;
                        }
                    }
                }).thenComparing(OrderItem::getServiceType,(x,y)->{
                    //先按照服务排序,安装优先
                    if(x == null && y != null){
                        return 1;
                    }else if(x !=null && y == null){
                        return -1;
                    }else if(x == null && y == null){
                        return -1;
                    }else {
                        if(x.getId().equals(y.getId())){
                            return 0;
                        }else if(x.getName().contains("安装")){
                            return -1;
                        }else{
                            return 1;
                        }
                    }
                }))
                .collect(Collectors.toList());
        for(OrderItem item:list){
            System.out.printf("get id:%d ,product name:%s, setFlag:%d, service:%s\n",
                    item.getId(),
                    item.getProduct().getName(),
                    item.getProduct().getSetFlag(),
                    item.getServiceType().getName()
            );
        }
        /*
        if(firstItem != null) {
            System.out.printf("get id:%d ,product name:%s, setFlag:%d, service:%s",
                    firstItem.getId(),
                    firstItem.getProduct().getName(),
                    firstItem.getProduct().getSetFlag(),
                    firstItem.getServiceType().getName()
            );
        }*/
    }

    /**
     * 测试递归读取自动派单网点
     */
    @Test
    public void testGetNearServicepoints(){
        int[] radiuses = IntStream.of(20000,40000,60000).toArray();
        ServicePointStation station =  autoPlanMessageService.getNearServicePoint(1609,radiuses,113.089392,22.957949);
        if(station == null){
            log.error("无符合自动派单的网点");
        }else{
            log.warn("网点：" + GsonUtils.getInstance().toGson(station));
        }
    }

    @Test
    public void testMode(){
        int mode_value = 50;
        for(int i=0;i<10000;i++){
            log.info("{} mode: {}",i,i%mode_value);
        }
    }

    @Test
    public void testGetPlanRadiusFromCache() {
        /*
        long areaId = 1594;
        System.out.println("area id:" + areaId);
        PlanRadius planRadius = planRadiusService.getByAreaIdFromCache(areaId);
        Assert.assertNotNull("返回对象null", planRadius);
        System.out.println("planRadius:" + GsonUtils.getInstance().toGson(planRadius));

        areaId = 1595;
        System.out.println("area id:" + areaId);
        planRadius = planRadiusService.getByAreaIdFromCache(areaId);
        Assert.assertNotNull("返回对象null", planRadius);
        System.out.println("planRadius:" + GsonUtils.getInstance().toGson(planRadius));

        areaId = 3063;
        System.out.println("area id:" + areaId);
        planRadius = planRadiusService.getByAreaIdFromCache(areaId);
        Assert.assertNotNull("返回对象null", planRadius);
        System.out.println("planRadius:" + GsonUtils.getInstance().toGson(planRadius));

        areaId = 3403;
        System.out.println("area id:" + areaId);
        planRadius = planRadiusService.getByAreaIdFromCache(areaId);
        Assert.assertNotNull("返回对象null", planRadius);
        System.out.println("planRadius:" + GsonUtils.getInstance().toGson(planRadius));
        */
    }

    @Test
    public void testDistance(){
        double distance = 1205.447/1000d;
        distance = new BigDecimal(distance).setScale(2,   RoundingMode.HALF_UP).doubleValue();
        System.out.println("distance:" + distance);
    }

    //region 配件

    @Test
    public void testCheckCanAutoComplete(){
        Order order = orderService.getOrderById(953489482995609600l,"20181", OrderUtils.OrderDataLevel.DETAIL,true);
        Boolean autoCompleteFlag = true;//自动完工标记
        String completeType = "compeled_all";
        //检查未审核或未发货配件申请单
        Integer qty = orderMaterialService.getMaterialMasterCountByOrderId(order.getId(),order.getQuarter(),1);
        if(qty>0){
            System.out.println("还有未审核或未发货配件申请单");
            return;
        }
        //检查客户要求完成照片数量
        Customer customer = customerService.getFromCache(order.getOrderCondition().getCustomer().getId());
        if(customer == null){
            System.out.println("客户不存在，或读取客户信息失败");
            return;
        }
        if(customer.getMinUploadNumber()>0 && order.getOrderCondition().getFinishPhotoQty()<customer.getMinUploadNumber()){
            System.out.println("请先上传客户要求的最少服务效果图");
            return;
        }
        OrderCondition condition = order.getOrderCondition();
        Integer orgAppAbnormaly = condition.getAppAbnormalyFlag();//如果原来异常，本次也异常，不需要累加异常数量
        //完成类型
        Integer appAbnormalyFlag = 0;
        if(1==orgAppAbnormaly){//订单已经app异常，改成短信回复客评
            autoCompleteFlag = false;
        }
        else if(!completeType.equalsIgnoreCase("compeled_all")){
            autoCompleteFlag = false;
            appAbnormalyFlag = 1;
        }

        if(autoCompleteFlag) {
            String checkResult = "";
            if(StringUtils.isBlank(checkResult)) {
                //网点自动完工检查
                List<Long> points = order.getDetailList().stream()
                        .map(t -> t.getServicePoint().getId())
                        .distinct()
                        .collect(Collectors.toList());
                ServicePoint servicePoint;
                if (null == points || 0 == points.size()) {
                    autoCompleteFlag = false;
                } else {
                    for (int i = 0, size = points.size(); i < size; i++) {
                        servicePoint = servicePointService.getFromCache(points.get(i));
                        if (servicePoint == null || servicePoint.getAutoCompleteOrder() == 0) {
                            autoCompleteFlag = false;
                            break;
                        }
                    }
                }
            }
        }

        System.out.println("autoCompleteFlag:" + autoCompleteFlag.toString());
        if(autoCompleteFlag){
            System.out.println("autoCompleteFlag");
        }
    }

    @Test
    public void testCancelRelationOfServiceAndMaterial(){
        Long orderId = 0L;
        Long detailId = 917581562860670976l;
        String quarter = "20174";
        orderMaterialService.cancelRelationOfServiceAndMaterial(orderId,quarter,detailId);
    }

    @Test
    public void checkMaterialWhenApprove(){
        Long masterId = 1007068679454216192l;
        String quarter = "20182";
        MaterialMaster materialMaster = orderMaterialService.getMaterialMasterById(masterId, quarter);
        Order order = orderService.getOrderById(materialMaster.getOrderId(), quarter, OrderUtils.OrderDataLevel.DETAIL, true);
        if (order == null || order.getOrderCondition() == null) {
            throw new RuntimeException("读取订单错误，请重试。");
        }
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
                log.error("订单上门明细中无符合条件的记录。请确认上门明细是否已经删除了。");
            }else{
                log.info("detail:{}",GsonUtils.toGsonString(detail));
            }
        }
    }

    @Test
    public void testMaterialLog(){
        MaterialLog materialLog = MaterialLog.builder()
                .quarter("20192")
                .MaterialMasterId(1l)
                .content("测试配件跟踪进度日志1")
                .createBy("管理员")
                .createDate(new Date())
                .build();
        orderMaterialService.insertLog(materialLog);

        materialLog = MaterialLog.builder()
                .quarter("20192")
                .MaterialMasterId(1l)
                .content("测试配件跟踪进度日志2")
                .createBy("管理员")
                .createDate(new Date())
                .build();
        orderMaterialService.insertLog(materialLog);
        //get
        List<MaterialLog> logs = orderMaterialService.getLogs(1l,"20192");
        Assert.assertNotNull("返回列表",logs);
        System.out.println(GsonUtils.getInstance().toGson(logs));

    }

    @Test
    public void testCanGradeOfMaterialForm(){
        long orderId = 1138005571312685057l;
        String quarter = "20192";
        try {
            MSResponse response = orderMaterialService.canGradeOfMaterialForm(1, orderId, quarter);
            System.out.println(GsonUtils.getInstance().toGson(response));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    // 测试发送物流订阅方法 subsLogisticsMessage
    @Test
    public void testSubsLogisticsMessage(){
        List<OrderItem> items = Lists.newArrayList();
        OrderItem item1 = new OrderItem();
        item1.setId(1l);
        Product p1 = new Product();
        p1.setId(1l);
        p1.setName("热水器");
        p1.setSetFlag(0);
        item1.setProduct(p1);
        item1.setBrand("阿诗丹顿");
        item1.setProductSpec("规格1");
        item1.setStandPrice(45.0);
        item1.setDiscountPrice(35.0);
        item1.setItemNo(1);
        item1.setDelFlag(0);
        item1.setExpressCompany(new Dict("jd","京东物流"));
        item1.setExpressNo("111111111");
        item1.setServiceType(new ServiceType(1l,"II","安装"));
        items.add(item1);

        OrderItem item2 = ObjectUtils.clone(item1);
        item2.getProduct().setId(2l);
        item2.getProduct().setName("油烟机");
        item2.setExpressCompany(new Dict("jd","京东物流"));
        item2.setExpressNo("111111111");
        items.add(item2);

        logisticsBusinessService.subsLogisticsMessage(MQLMExpress.GoodsType.Goods,1136162670899236865l,"K2019061500001","20192","13700000001",items);
    }

    //endregion

    @Test
    public void testcanAutoGrade(){
        Long orderId = 1317382850944831488l;
        String quarter  ="20204";
        Order order = orderService.getOrderById(orderId,quarter, OrderUtils.OrderDataLevel.DETAIL,true);
        NameValuePair checkResult = orderGradeService.canAutoGrade(order);
        System.out.println("checkResult:" + GsonUtils.getInstance().toGson(checkResult));
        int checkResultValue = StringUtils.toInteger(checkResult.getValue());
        System.out.println("checkResultValue:" + checkResultValue);
    }

    @Test
    public void testUpdateSubArea() {
        boolean needUpdateSubAreaId = Boolean.parseBoolean(Global.getConfig("updateSubAreaId.enabled"));
        if (needUpdateSubAreaId) {
            String quarter = Global.getConfig("updateSubAreaId.quarter");
            String status = Global.getConfig("updateSubAreaId.status");
            orderService.updateSubAreaId(quarter, status);
        }
    }

    /**
     * 测试下单时读取客户产品+价格
     */
    @Test
    public void testGetCutomerPriceWhenCreateOrder(){
        Long cid = 1584L;
        List<CustomerProductVM> productVMList = orderEditFormService.getCustomerProducts(cid);
        if(CollectionUtils.isEmpty(productVMList)){
            log.error("读取客户价格错误，或客户未维护产品价格");
            return;
        }
        CustomerProductVM customerProduct = productVMList.stream().filter(t->t.getId() == 70L).findAny().orElse(null);
        if(customerProduct == null){
            log.warn("正确，客户暂不做中岛烟机的服务");
        }else{
            log.error("product:{} name:{} service:{}",customerProduct.getId(),customerProduct.getName(),customerProduct.getServices());
        }

    }

    @Test
    public void testUrgentBug(){
        Double urgentHours = 11.87;
        //根据选择的加急等级进行判断
        List<UrgentLevel> urgentLevels = urgentLevelService.findAllList();
        if (urgentLevels == null || urgentLevels.size() == 0) {
            log.error("未设定加急费标准!");
        }
        UrgentChargeModel urgentChargeModel = new UrgentChargeModel();
        UrgentLevel urgentLevel = urgentLevels.stream().filter(t -> t.getId().longValue() == 6).findFirst().orElse(null);
        if (urgentLevel == null) {
            log.error("加急标准不存在!");
        } else {
            List<String> ranges = Splitter.onPattern("[~|-]")
                    .omitEmptyStrings()
                    .trimResults()
                    .splitToList(urgentLevel.getLabel());
            if (ranges.size() == 2) {
                if (urgentHours <= Double.valueOf(ranges.get(1))) {
                    //小于等于上限,符合加急条件，取下单/修改订单/修改加急时的费用，不需要重新取值
                    urgentChargeModel = new UrgentChargeModel();
                    urgentChargeModel.setChargeIn(urgentLevel.getChargeIn());
                    urgentChargeModel.setChargeOut(urgentLevel.getChargeOut());
                }
            } else {
                log.error("加急等级描述格式错误!");
            }
        }
        log.warn(GsonUtils.getInstance().toGson(urgentChargeModel));
    }

    @Test
    public void testCreateOrderConfig(){
        System.out.println(createOrderConfig.toString());
    }

    @Test
    public void testGetServicepointFeeForCharge() {
        Map<Long, OrderServicePointFee> orderServicePointFeeMapsForCharge = orderService.getOrderServicePointFeeMapsForCharge(1247027107289239552l, "20202");
    }


    @Test
    public void testTimeLinessCharge(){
        Order.TimeLinessType timeLinessType = Order.TimeLinessType.HOURS;
        Order.TimeLinessType customerTimeLinessType = Order.TimeLinessType.HOURS;
        double timeLinessCharge = 0.0;
        double subsidyTimeLinessCharge = 0.0;
        com.kkl.kklplus.entity.common.NameValuePair<Double,Double> pair;

        System.out.println("1.开关都关");
        pair = chargeTimeLiness(timeLinessType,customerTimeLinessType,timeLinessCharge,subsidyTimeLinessCharge,1);
        System.out.printf("费用 快可立补贴:%.2f , 客户补贴:%.2f \n",pair.getName(),pair.getValue());
        System.out.println(StringUtils.repeat("=",100));

        System.out.println("2.快可立：开  客户时效：关 逻辑：快可立优先 都有费用");
        timeLinessType = Order.TimeLinessType.ALL;
        customerTimeLinessType = Order.TimeLinessType.HOURS;
        timeLinessCharge = 10.0;
        subsidyTimeLinessCharge = 5.0;
        pair = chargeTimeLiness(timeLinessType,customerTimeLinessType,timeLinessCharge,subsidyTimeLinessCharge,2);
        System.out.printf("费用 快可立补贴:%.2f , 客户补贴:%.2f \n",pair.getName(),pair.getValue());
        System.out.println(StringUtils.repeat("=",100));

        System.out.println("3.快可立：开  客户时效：关 逻辑：客户优先 都有费用");
        timeLinessType = Order.TimeLinessType.ALL;
        customerTimeLinessType = Order.TimeLinessType.HOURS;
        timeLinessCharge = 10.0;
        subsidyTimeLinessCharge = 5.0;
        pair = chargeTimeLiness(timeLinessType,customerTimeLinessType,timeLinessCharge,subsidyTimeLinessCharge,1);
        System.out.printf("费用 快可立补贴:%.2f , 客户补贴:%.2f \n",pair.getName(),pair.getValue());
        System.out.println(StringUtils.repeat("=",100));

        System.out.println("4.快可立：关  客户时效：开 逻辑：快可立优先 都有费用");
        timeLinessType = Order.TimeLinessType.ALL;
        customerTimeLinessType = Order.TimeLinessType.HOURS;
        timeLinessCharge = 10.0;
        subsidyTimeLinessCharge = 5.0;
        pair = chargeTimeLiness(timeLinessType,customerTimeLinessType,timeLinessCharge,subsidyTimeLinessCharge,2);
        System.out.printf("费用 快可立补贴:%.2f , 客户补贴:%.2f \n",pair.getName(),pair.getValue());
        System.out.println(StringUtils.repeat("=",100));

        System.out.println("5.快可立：关  客户时效：开 逻辑：客户优先 都有费用");
        timeLinessType = Order.TimeLinessType.HOURS;
        customerTimeLinessType = Order.TimeLinessType.ALL;
        timeLinessCharge = 10.0;
        subsidyTimeLinessCharge = 5.0;
        pair = chargeTimeLiness(timeLinessType,customerTimeLinessType,timeLinessCharge,subsidyTimeLinessCharge,1);
        System.out.printf("费用 快可立补贴:%.2f , 客户补贴:%.2f \n",pair.getName(),pair.getValue());
        System.out.println(StringUtils.repeat("=",100));

        System.out.println("7.快可立：开  客户时效：开 逻辑：快可立优先 都有费用");
        timeLinessType = Order.TimeLinessType.ALL;
        customerTimeLinessType = Order.TimeLinessType.ALL;
        timeLinessCharge = 10.0;
        subsidyTimeLinessCharge = 5.0;
        pair = chargeTimeLiness(timeLinessType,customerTimeLinessType,timeLinessCharge,subsidyTimeLinessCharge,2);
        System.out.printf("费用 快可立补贴:%.2f , 客户补贴:%.2f \n",pair.getName(),pair.getValue());
        System.out.println(StringUtils.repeat("=",100));

        System.out.println("8.快可立：开  客户时效：开 逻辑：客户优先 都有费用");
        timeLinessType = Order.TimeLinessType.ALL;
        customerTimeLinessType = Order.TimeLinessType.ALL;
        timeLinessCharge = 10.0;
        subsidyTimeLinessCharge = 5.0;
        pair = chargeTimeLiness(timeLinessType,customerTimeLinessType,timeLinessCharge,subsidyTimeLinessCharge,1);
        System.out.printf("费用 快可立补贴:%.2f , 客户补贴:%.2f \n",pair.getName(),pair.getValue());
        System.out.println(StringUtils.repeat("=",100));

        System.out.println("8.快可立：开  客户时效：开 逻辑：叠加 都有费用");
        timeLinessType = Order.TimeLinessType.ALL;
        customerTimeLinessType = Order.TimeLinessType.ALL;
        timeLinessCharge = 10.0;
        subsidyTimeLinessCharge = 5.0;
        pair = chargeTimeLiness(timeLinessType,customerTimeLinessType,timeLinessCharge,subsidyTimeLinessCharge,3);
        System.out.printf("费用 快可立补贴:%.2f , 客户补贴:%.2f \n",pair.getName(),pair.getValue());
        System.out.println(StringUtils.repeat("=",100));
    }

    private com.kkl.kklplus.entity.common.NameValuePair<Double,Double> chargeTimeLiness(Order.TimeLinessType timeLinessType, Order.TimeLinessType customerTimeLinessType,
                                                                                        Double timeLinessCharge, Double subsidyTimeLinessCharge, int logicStatus){
        com.kkl.kklplus.entity.common.NameValuePair<Double,Double> pair = new com.kkl.kklplus.entity.common.NameValuePair<Double,Double>(0.0,0.0);
        //3.费用处理规则，两个开关都开启，且有费用时，才判断规则
        if( timeLinessType == Order.TimeLinessType.ALL && customerTimeLinessType == Order.TimeLinessType.ALL
                && (timeLinessCharge > 0 || subsidyTimeLinessCharge > 0) ) {
            //int timeLiness = StringUtils.toInteger(MSDictUtils.getDictSingleValue("timeline_charge_logic", "1"));
            switch(logicStatus){
                case 1:
                    //客户时效优先
                    timeLinessCharge = 0.0;//快可立补贴

                    System.out.println("快可立补贴清零");
                    break;
                case 2:
                    //快可立补贴优先
                    //customerTimeLinessCharge 应收客户的钱，照收
                    subsidyTimeLinessCharge = 0.0;//客户补贴
                    System.out.println("客户补贴清零");
                    break;
                default:
                    //叠加，不做特殊处理
                    System.out.println("叠加");
                    break;
            }
        }else{
            System.out.println("费用为0或开关未全开，不进行逻辑处理");
        }
        pair.setName(timeLinessCharge);
        pair.setValue(subsidyTimeLinessCharge);
        return pair;
    }

    /**
     * 测试检查时效费重复计费
     */
    @Test
    public void testRepleateTimelinessFee() {
        DateTime startDate = new DateTime(2020,7,24,0,0,0);
        DateTime endDate = new DateTime(2020,7,24,23,59,59,999);
        long customerId = 1584;
        long servicePointId = 1622038;
        long productCategoryId= 18;
        String userPhone = "15899998888";
        List<Map<String,Object>> list = orderDao.getRepeateTimelinessFeeInfo(startDate.toDate(),endDate.toDate(),customerId,servicePointId,productCategoryId,userPhone,Lists.newArrayList("20201","20202","20203"));
        if(CollectionUtils.isEmpty(list)){
            System.out.println("return : no order");
        }else{
            Map<String,Object> map;
            for(int i=0,size=list.size();i<size;i++){
                map=list.get(i);
                System.out.println(MessageFormat.format("id:{0} customerFee:{1} kklFee:{2}",
                        map.get("order_id"),
                        map.get("customer_time_liness_charge"),
                        map.get("time_liness_charge")
                        )
                );
            }
        }

        boolean checkResult = orderService.checkRepeateTimeLinessFee(startDate.toDate(),endDate.toDate(),customerId,servicePointId,productCategoryId,userPhone);
        System.out.println("checkResult:" + checkResult);
    }

    @Test
    public void checkTimeliness() {
        Boolean saveSuccess = false;

        Long orderId = 1398617620755910656L;
        String quarter = "20212";
        Order order = null;
        //create date
        Date date = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").parseDateTime("2021-05-29 20:30:01").toDate();
        log.error("下单日期:{}",DateUtils.formatDate(date,"yyyy-MM-dd HH:mm:ss"));
        boolean autoReviewPraise = false;
        order = orderService.getOrderById(orderId, quarter, OrderUtils.OrderDataLevel.CONDITION, true, true, false, true);
        if (order == null || order.getOrderCondition() == null) {
            log.error("读取客评订单信息失败");
            throw new OrderException("读取客评订单信息失败。");
        }

        OrderCondition condition = order.getOrderCondition();
        List<OrderDetail> details = order.getDetailList();
        if (CollectionUtils.isEmpty(details)) {
            log.error("此订单无上门服务，不能客评。");
            throw new OrderException("此订单无上门服务，不能客评。");
        }
        //根据上门服务来判断是否是安装，只要上门服务有安装，就算安装
        boolean installService = details.stream().filter(t -> t.getDelFlag() == 0 && t.getServiceType().getId().longValue() == 1).count() > 0;
        ServicePoint servicePoint = condition.getServicePoint();
        Long sid = servicePoint.getId();
        //region 时效
        //1.网点时效(应付，快可立补贴)
        double timeLinessCharge = 0.0;
        Date timeLinessStartDate = orderService.getServicePointTimeLinessStartDate(order, orderId, order.getQuarter(), sid);
        if (timeLinessStartDate == null) {
            log.error("读取网点派单日期失败。");
            throw new OrderException("读取网点派单日期失败，请重试。");
        }
        log.error("时效开始时间:{}",DateUtils.formatDate(timeLinessStartDate,"yyyy-MM-dd HH:mm:ss"));
        Order.TimeLinessType timeLinessType = Order.TimeLinessType.HOURS;
        Dict timeLinessInfo = null;
        Double hours = 0.0;
        long productCategoryId = Optional.ofNullable(condition.getProductCategoryId()).orElse(0L);
        Boolean repeateTimelinessFee = false;
        Date beginDate = null;
        Boolean recordTimelinessLog = false;//是否记录时效计算日志
        // (网点开关打开，且是保内安装单)
        if (servicePoint.getTimeLinessFlag() == 1 && installService) {
            timeLinessType = Order.TimeLinessType.ALL;
            log.error("重复时效费检查");
            //重复时效费检查
            //beginDate = new DateTime().withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0).toDate();
            //Date endDate = new DateTime().withHourOfDay(23).withMinuteOfHour(59).withSecondOfMinute(59).withMillisOfSecond(999).toDate();
            beginDate = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").parseDateTime("2021-05-31 00:00:00").toDate();
            Date endDate = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").parseDateTime("2021-05-31 23:59:59").toDate();
            repeateTimelinessFee = orderService.checkRepeateTimeLinessFee(beginDate, endDate, condition.getCustomer().getId(), sid, productCategoryId, condition.getPhone1());
            if (repeateTimelinessFee) {
                timeLinessType = Order.TimeLinessType.HOURS;
            }
        }
        log.error("timeLinessType：{}-{}",timeLinessType.type,timeLinessType.name);
        try {
            Date closeDate = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").parseDateTime("2021-05-31 14:06:11").toDate();
            timeLinessInfo = getServicePointTimeLinessInfo(timeLinessType, condition, timeLinessStartDate,closeDate, productCategoryId, 0,new StringBuilder(256));
        } catch (Exception e) {
            log.error("orderId:{}, timeLinessType:{} , timeLinessStartDate:{} ,productCategoryId:{}, manuTimeLinessLevel:{}"
                    ,orderId
                    ,timeLinessType.name
                    ,DateUtils.formatDate(timeLinessStartDate,"yyyy-MM-dd HH:mm:ss")
                    ,productCategoryId
                    ,0
            );
            throw new OrderException("计算网点时效费用错误：" + e.getMessage(), e);
        }
        log.error("timeLinessInfo：{}",timeLinessInfo);
        if (timeLinessInfo != null) {
            hours = Double.valueOf(timeLinessInfo.getType());
            timeLinessCharge = Double.valueOf(timeLinessInfo.getLabel());
        }
        log.error("hours:{}  charge:{}",hours,timeLinessCharge);
        //增加时效启用时间判断
        Date timelinessEffectiveDate = null;
        String timelinessEffectiveDateDict = MSDictUtils.getDictSingleValue("order_timeliness_effective_date", "2021-05-19");
        if (StrUtil.isNotEmpty(timelinessEffectiveDateDict)) {
            timelinessEffectiveDate = DateUtils.parseDate(timelinessEffectiveDateDict);
        }
        boolean resetTimeLineessCharge = false;//不支付网点时效费
        if(servicePoint.getTimeLinessFlag() == 0 || !installService){
            recordTimelinessLog = true;
        }
        //如果订单下单日期早于数据字典中设定的时效补贴启用日期，不补贴网点费用
        if (timelinessEffectiveDate == null || timelinessEffectiveDate.getTime() > condition.getCreateDate().getTime()) {
            timeLinessCharge = 0.0;
            resetTimeLineessCharge = true;
            recordTimelinessLog = true;
        }
        if(recordTimelinessLog) {
            StringBuffer timelinessCheck = new StringBuffer(256);
            timelinessCheck.append("OrderId:").append(orderId).append(" ,timeLinessStartDate:").append(DateUtils.formatDate(timeLinessStartDate, "yyyy-MM-dd HH:mm:ss"));
            timelinessCheck.append(" ,时效生效日期：").append(StrUtil.trimToEmpty(timelinessEffectiveDateDict));
            timelinessCheck.append(" ,订单创建时间:").append(DateUtils.formatDate(condition.getCreateDate(), "yyyy-MM-dd HH:mm:ss"));
            if (servicePoint.getTimeLinessFlag() == 0) {
                timelinessCheck.append(" ,网点快可立时效开关:关闭");
            }
            if (!installService) {
                timelinessCheck.append(" ,工单类型:非安装单");
            }
            timelinessCheck.append(" ,timeLinessType:").append(timeLinessType.name).append(" ,客服选择的时效：").append(0);
            if(repeateTimelinessFee) {
                timelinessCheck.append(" ,重复时效费检查[结果: 重复, 检查日期:")
                        .append(DateUtils.formatDate(beginDate, "yyyy-MM-dd HH:mm:ss"))
                        .append(", cid:").append(condition.getCustomer().getId())
                        .append(", sid:").append(sid)
                        .append(", categoryId：").append(productCategoryId)
                        .append("phone：").append(condition.getPhone1()).append(" ]");
            }
            if (timeLinessInfo != null) {
                timelinessCheck.append(" ,timeLinessInfo [ 用时:").append(timeLinessInfo.getType()).append(", 等级:").append(timeLinessInfo.getValue()).append(",金额:").append(timeLinessInfo.getLabel()).append(" ]");
            }

            if(resetTimeLineessCharge) {
                timelinessCheck.append(" ,下单日期早于生效日期，时效费用清零");
            }
            System.out.println(timelinessCheck.toString());
            timelinessCheck.setLength(0);
        }
        log.error("END");
        //endregion 时效

    }

    @Test
    public void batchCheckTimeliness() {
        String quarter = "20212";
        Order order = null;
        List<OrderTimelinessInfo> orders = testDao.batchCheckTimeliness();
        if(CollectionUtils.isEmpty(orders)){
            log.error("无数据返回");
            return;
        }else{
            System.out.println("total: " + orders.size());
        }
        Date createDate;
        OrderTimelinessInfo orderInfo;
        Long orderId;
        StringBuilder sLog = new StringBuilder(1024);
        for(int i=0,size=orders.size();i<size;i++){
            orderInfo = orders.get(i);
            try {
                TimeUnit.MILLISECONDS.sleep(500l);
            } catch (InterruptedException e) {
                log.error("sleep exception",e);
            }
            sLog.setLength(0);
            sLog.append("orderInfo: ").append(GsonUtils.getInstance().toGson(orderInfo));
            orderId = orderInfo.getOrderId();
            order = orderService.getOrderById(orderId, quarter, OrderUtils.OrderDataLevel.DETAIL, true, true, false, true);
            if (order == null || order.getOrderCondition() == null) {
                sLog.append(" 读取客评订单信息失败");
                System.out.println(sLog.toString());
                continue;
            }
            OrderCondition condition = order.getOrderCondition();
            List<OrderDetail> details = order.getDetailList();
            if (CollectionUtils.isEmpty(details)) {
                sLog.append(" 此订单无上门服务，不能客评。");
                System.out.println(sLog.toString());
                continue;
            }
            //根据上门服务来判断是否是安装，只要上门服务有安装，就算安装
            boolean installService = details.stream().filter(t -> t.getDelFlag() == 0 && t.getServiceType().getId().longValue() == 1).count() > 0;
            ServicePoint servicePoint = condition.getServicePoint();
            Long sid = servicePoint.getId();
            //region 时效
            //1.网点时效(应付，快可立补贴)
            double timeLinessCharge = 0.0;
            Date timeLinessStartDate = orderService.getServicePointTimeLinessStartDate(order, orderId, quarter, sid);
            if (timeLinessStartDate == null) {
                sLog.append(" 读取网点派单日期失败");
                System.out.println(sLog.toString());
                continue;
            }
            sLog.append("\n时效开始时间:").append(DateUtils.formatDate(timeLinessStartDate,"yyyy-MM-dd HH:mm:ss"));
            Order.TimeLinessType timeLinessType = Order.TimeLinessType.HOURS;
            Dict timeLinessInfo = null;
            Double hours = 0.0;
            long productCategoryId = Optional.ofNullable(condition.getProductCategoryId()).orElse(0L);
            Boolean repeateTimelinessFee = false;
            Date beginDate = null;
            Boolean recordTimelinessLog = false;//是否记录时效计算日志
            // (网点开关打开，且是保内安装单)
            if (servicePoint.getTimeLinessFlag() == 1 && installService) {
                timeLinessType = Order.TimeLinessType.ALL;
                //重复时效费检查
                beginDate = DateUtils.getStartOfDay(orderInfo.getCloseDate());
                Date endDate = DateUtils.getDateEnd(orderInfo.getCloseDate());
                sLog.append("\n重复时效费检查:[ start:").append(DateUtils.formatDate(beginDate,"yyyy-MM-dd HH:mm:ss"))
                        .append(" end:").append(DateUtils.formatDate(endDate,"yyyy-MM-dd HH:mm:ss"));
                //beginDate = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").parseDateTime("2021-05-31 00:00:00").toDate();
                //Date endDate = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").parseDateTime("2021-05-31 23:59:59").toDate();
                repeateTimelinessFee = orderService.checkRepeateTimeLinessFee(beginDate, endDate, condition.getCustomer().getId(), sid, productCategoryId, condition.getPhone1());
                if (repeateTimelinessFee) {
                    sLog.append(" 结果:重复]\n");
                    timeLinessType = Order.TimeLinessType.HOURS;
                }else{
                    sLog.append(" 结果：不重复]\n");
                }
            }
            sLog.append(" timeLinessType:").append(timeLinessType.type).append(" - ").append(timeLinessType.name);
            try {
                //Date closeDate = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").parseDateTime("2021-05-31 14:06:11").toDate();
                timeLinessInfo = getServicePointTimeLinessInfo(timeLinessType, condition, timeLinessStartDate,orderInfo.getCloseDate(), productCategoryId, 0,sLog);
            } catch (Exception e) {
                System.out.println(sLog.toString());
                log.error("计算网点时效费用错误 orderId:{}, timeLinessType:{} , timeLinessStartDate:{} ,productCategoryId:{}, manuTimeLinessLevel:{}"
                        ,orderId
                        ,timeLinessType.name
                        ,DateUtils.formatDate(timeLinessStartDate,"yyyy-MM-dd HH:mm:ss")
                        ,productCategoryId
                        ,0
                        ,e
                );
                continue;
            }
            sLog.append(" 时效结果[timeLinessInfo]:");
            if (timeLinessInfo != null) {
                hours = Double.valueOf(timeLinessInfo.getType());
                timeLinessCharge = Double.valueOf(timeLinessInfo.getLabel());
                sLog.append(" - hours:").append(hours).append(" charge:").append(timeLinessCharge);
            }else{
                sLog.append(" - null");
            }
            System.out.println(sLog.toString());
            sLog.setLength(0);
            //增加时效启用时间判断
            Date timelinessEffectiveDate = null;
            String timelinessEffectiveDateDict = MSDictUtils.getDictSingleValue("order_timeliness_effective_date", "2021-05-19");
            if (StrUtil.isNotEmpty(timelinessEffectiveDateDict)) {
                timelinessEffectiveDate = DateUtils.parseDate(timelinessEffectiveDateDict);
            }
            boolean resetTimeLineessCharge = false;//不支付网点时效费
            if(servicePoint.getTimeLinessFlag() == 0 || !installService){
                recordTimelinessLog = true;
            }
            //如果订单下单日期早于数据字典中设定的时效补贴启用日期，不补贴网点费用
            if (timelinessEffectiveDate == null || timelinessEffectiveDate.getTime() > condition.getCreateDate().getTime()) {
                timeLinessCharge = 0.0;
                resetTimeLineessCharge = true;
                recordTimelinessLog = true;
            }
            if(recordTimelinessLog) {
                StringBuffer timelinessCheck = new StringBuffer(256);
                timelinessCheck.append("OrderId:").append(orderId).append(" ,timeLinessStartDate:").append(DateUtils.formatDate(timeLinessStartDate, "yyyy-MM-dd HH:mm:ss"));
                timelinessCheck.append(" ,时效生效日期：").append(StrUtil.trimToEmpty(timelinessEffectiveDateDict));
                timelinessCheck.append(" ,订单创建时间:").append(DateUtils.formatDate(condition.getCreateDate(), "yyyy-MM-dd HH:mm:ss"));
                if (servicePoint.getTimeLinessFlag() == 0) {
                    timelinessCheck.append(" ,网点快可立时效开关:关闭");
                }
                if (!installService) {
                    timelinessCheck.append(" ,工单类型:非安装单");
                }
                timelinessCheck.append(" ,timeLinessType:").append(timeLinessType.name).append(" ,客服选择的时效：").append(0);
                if(repeateTimelinessFee) {
                    timelinessCheck.append(" ,重复时效费检查[结果: 重复, 检查日期:")
                            .append(DateUtils.formatDate(beginDate, "yyyy-MM-dd HH:mm:ss"))
                            .append(", cid:").append(condition.getCustomer().getId())
                            .append(", sid:").append(sid)
                            .append(", categoryId：").append(productCategoryId)
                            .append("phone：").append(condition.getPhone1()).append(" ]");
                }
                if (timeLinessInfo != null) {
                    timelinessCheck.append(" ,timeLinessInfo [ 用时:").append(timeLinessInfo.getType()).append(", 等级:").append(timeLinessInfo.getValue()).append(",金额:").append(timeLinessInfo.getLabel()).append(" ]");
                }

                if(resetTimeLineessCharge) {
                    timelinessCheck.append(" ,下单日期早于生效日期，时效费用清零");
                }
                System.out.println(timelinessCheck.toString());
                timelinessCheck.setLength(0);
            }
            //endregion 时效
        }
        //create date
    }

    /**
     * 计算网点时效等级及金额
     *
     * @param timeLinessType      要返回时效的内容类型
     * @param condition           订单实例
     * @param timeLinessStartDate 时效开始计时时间
     * @param productCategoryId   产品类别
     * @param manuTimeLinessLevel 客服选择的时效区间
     * @return Dict
     * type:用时
     * value:等级
     * label:金额
     */
    private Dict getServicePointTimeLinessInfo(Order.TimeLinessType timeLinessType, OrderCondition condition, Date timeLinessStartDate,Date closeDate, long productCategoryId, Integer manuTimeLinessLevel,StringBuilder sLog) throws RuntimeException {
        sLog.append(" [2.计算时效]\n");
        Dict level = null;
        //用时
        Date appCompleteDate = closeDate;
        //如果是app完成(compeled_all,compeled_all_notest,compeled_maintain)，结束时间已app完成为准
        //否则已当前时间为准
        Set<String> appCompletSet = Sets.newHashSet("compeled_all", "compeled_all_notest", "compeled_maintain");
        if (condition.getAppCompleteDate() != null && appCompletSet.contains(condition.getAppCompleteType().toLowerCase().trim())) {
            appCompleteDate = condition.getAppCompleteDate();
        }
        sLog.append(" 时效结束时间:").append(DateUtils.formatDate(appCompleteDate,"yyyy-MM-dd HH:mm:ss"));
        //Long minutes = DateUtils.pastMinutes(timeLinessStartDate, appCompleteDate);
        //sLog.append(" minuts:").append(minutes);
        Double hours = pastHours(timeLinessStartDate, appCompleteDate);
        //if (minutes > 0) {
        //    hours = minutes / 60.0;
        //}
        sLog.append(" hours:").append(String.format("%.2f", hours));
        if (hours <= 0 && (manuTimeLinessLevel == null || manuTimeLinessLevel <= 0)) {
            return level;
        }
        sLog.append(" 品类:").append(productCategoryId);
        if(productCategoryId <= 0){
            //throw new OrderException("读取订单品类错误，清重试。");
            sLog.append(" 读取订单品类错误。]\n");
            return level;
        }
        sLog.append(" 时效类型:").append(timeLinessType.type).append("-").append(timeLinessType.name);
        level = new Dict();
        level.setType(String.format("%.2f", hours));
        level.setLabel("0.0");
        if (timeLinessType.type >= Order.TimeLinessType.LEVEL.type) {//等级
            if (manuTimeLinessLevel != null && manuTimeLinessLevel > 0) {
                level.setValue(manuTimeLinessLevel.toString());//客服选择的等级
            } else {
                List<Dict> timeLinessTypes = MSDictUtils.getDictList(TimeLinessPrice.TIME_LINESS_LEVEL);
                if (CollectionUtils.isEmpty(timeLinessTypes)) {
                    sLog.append("请检查是否设定网点时效配置。]\n");
                    return level;
                }
                //排序
                timeLinessTypes.stream().sorted(Comparator.comparing(Dict::getIntValue));
                Dict dict;
                List<String> ranges;
                for (int i = 0, size = timeLinessTypes.size(); i < size; i++) {
                    dict = timeLinessTypes.get(i);
                    ranges = Splitter.onPattern("[~|-]")
                            .omitEmptyStrings()
                            .trimResults()
                            .splitToList(dict.getLabel());
                    if (ranges.size() == 2) {
                        if (hours > Double.valueOf(ranges.get(0)) && hours <= Double.valueOf(ranges.get(1))) {
                            level.setValue(dict.getValue());
                            break;
                        }
                    }
                }
            }
        }
        if (timeLinessType.type == Order.TimeLinessType.ALL.type) {
            //增加时效区域开关判断 2019-05-18
            Area area = condition.getArea();
            if(area==null || area.getId() == null || area.getId() <=0){
                sLog.append("读取订单区域错误，请重试。]\n");
                return level;
            }
            area = areaService.getFromCache(area.getId());
            if(area==null || area.getId() == null || area.getId() <=0
                    || area.getParent() == null || area.getParent().getId() == null
                    || area.getParent().getId() <= 0){
                sLog.append("读取订单区域错误，请重试。]\n");
                return level;
            }
            AreaTimeLiness areaTimeLiness = msAreaTimeLinessService.getFromCacheForSD(area.getParent().getId(),productCategoryId); //2020-06-24
            if(areaTimeLiness == null){
                sLog.append("区域时效开关未设定。]\n");
                return level;
            }
            //订单只能有一个品类，按品类请求微服务
            //奖金
            if (areaTimeLiness.getIsOpen() == 1 && level != null && StringUtils.isNotBlank(level.getValue())) {
                List<TimeLinessPrice> prices = timeLinessPriceService.getTimeLinessPrices(productCategoryId);
                if (prices == null || prices.size() == 0) {
                    sLog.append("请检查产品类别是否设定网点时效费用。]\n");
                    return level;
                }
                final int intLevel = level.getIntValue();
                Double timeLinessCharge = prices.stream().filter(t -> t.getTimeLinessLevel().getIntValue() == intLevel).map(t -> t.getAmount()).min(Double::compareTo).orElse(0.00);
                level.setLabel(String.format("%.2f", timeLinessCharge));
            }
        }
        sLog.append("]\n");
        return level;
    }

    private double pastHours(Date startDate,Date toDate){
        long t = toDate.getTime() - startDate.getTime();
        double hours =  t / (3600d * 1000d);
        return hours;
    }

    public static void main(String[] args) {
        /*StringBuffer sb = new StringBuffer(256);
        User user = null;
        sb.append("null:").append(user).append(";").append("Integer:").append(1).append(";")
                .append("Long:").append(1L).append(";")
                .append("Boolean:").append(true).append(";")
                .append("Dict:").append(new Dict("ryan","13800000000"));
        System.out.println(sb.toString());*/
        /*
        //测试：两个时间之间相隔分钟
        DateTime startDateTime = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").parseDateTime("2021-06-02 17:37:59");
        DateTime toDateTime = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").parseDateTime("2021-06-02 17:38:53");
        Date startDate = startDateTime.toDate();;
        Date toDate = toDateTime.toDate();
        long t = toDate.getTime() - startDate.getTime();
        double minuts =  t / (60d * 1000d);
        System.out.println("diff minuts:" + minuts + " 分钟");
        System.out.println(Minutes.minutesBetween(startDateTime, toDateTime).getMinutes() + " 分钟 ");
        //小时
        double hours = t/3600d/1000d;
        System.out.println("diff hours:" + String.format("%.2f",hours) + " 小时");
        */
        Map<Integer, Object> map = Maps.newHashMap();
        CustomerShop shop = new CustomerShop();
        shop.setDataSource(1);
        shop.setUserId(1L);
        shop.setId("12313123133");
        shop.setName("测试店铺");
        map.put(1,shop);
        String json = GsonUtils.getInstance().toGson(map);
        System.out.println(json);

        Type type = new TypeToken<Map<Integer, Object>>(){}.getType();
        Map<Integer, Object> maps = GsonUtils.getInstance().getGson().fromJson(json, type);
        //for (Map.Entry<Integer, Object> entry : maps.entrySet()) {
        //    System.out.println("key: " + entry.getKey());
        //
        //}
        Object value = maps.get(1);
        System.out.println(GsonUtils.getInstance().toGson(value));

    }
}
