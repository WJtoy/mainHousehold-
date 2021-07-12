package com.wolfking.jeesite.test.rpt;

import com.google.common.collect.Lists;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.validator.BeanValidators;
import com.wolfking.jeesite.modules.md.dao.ServicePointDao;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.entity.CustomerFinance;
import com.wolfking.jeesite.modules.md.service.CustomerService;
import com.wolfking.jeesite.modules.md.service.ProductService;
import com.wolfking.jeesite.modules.md.service.ServicePointService;
import com.wolfking.jeesite.modules.md.service.ServiceTypeService;
import com.wolfking.jeesite.modules.mq.entity.MQCustomerModel;
import com.wolfking.jeesite.modules.rpt.dao.ServicePointBalanceMonthlyDao;
import com.wolfking.jeesite.modules.rpt.service.ServicePointBalanceMonthlyService;
import com.wolfking.jeesite.modules.sd.dao.OrderDao;
import com.wolfking.jeesite.modules.sd.entity.OrderItem;
import com.wolfking.jeesite.modules.sd.service.OrderCrushService;
import com.wolfking.jeesite.modules.sd.service.OrderService;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.ms.tmall.md.entity.B2bCustomerMap;
import com.wolfking.jeesite.ms.tmall.md.utils.B2BMapUtils;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Created by f1008783 on 2017/6/15.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class RptOrderTest {

    @Autowired
    CustomerService customerService;

    @Autowired
    OrderService orderService;

    @Autowired
    OrderCrushService crushService;

    @Autowired
    OrderDao    orderDao;

    @Autowired
    ServicePointDao servicePointDao;

    @Autowired
    ServicePointBalanceMonthlyDao servicePointBalanceMonthlyDao;
    @Autowired
    ServicePointBalanceMonthlyService servicePointBalanceMonthlyService;

    @Autowired
    private ServiceTypeService  serviceTypeService;

    @Autowired
    private ServicePointService servicePointService;

//    @Autowired
//    private GradeQtyRptService gradeQtyRptService;

    @Autowired
    private ProductService productService;

    private org.slf4j.Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 工作薄对象
     */
    private SXSSFWorkbook wb;

    /**
     * 工作表对象
     */
    private Sheet sheet;

    /**
     * 样式列表
     */
    private Map<String, CellStyle> styles;

    public static final int EXECL_CELL_WIDTH_15 		= 15;
    public static final int EXECL_CELL_WIDTH_10 		= 10;
    public static final int EXECL_CELL_HEIGHT_TITLE 	= 30;
    public static final int EXECL_CELL_HEIGHT_HEADER 	= 20;
    public static final int EXECL_CELL_HEIGHT_DATA 		= 20;

//    @Autowired
//    private OrderDetailReportService orderDetailService;

    @Test
    public void test(){
    }

    @Test
    public void testUpdateComplete(){
        Date date = DateUtils.getDate(2019, 11, 13);
        Date endDate = DateUtils.getDate(2019, 11, 19);
        long l = (endDate.getTime() - date.getTime()) / (1000 * 60 * 60);
        if (l > 96){
            System.out.println("大于96");
        }
//        Date endDate = new Date();
//        Date startDate = DateUtils.getDateStart(DateUtils.addMonth(endDate,-10));
//        finishedOrderDetailService.updateCompleteOrder(startDate,endDate,618L);
    }
    @Test
    public void testUpdateCustomer(){
//        Date endDate = new Date();
//        Date startDate = DateUtils.getDateStart(DateUtils.addMonth(endDate,-12));
//        finishedOrderDetailService.updateCustomerTuibu(startDate,endDate,null);
    }

    @Test
    public void testUpdateEngineerTuibu(){
//        Date endDate = new Date();
//        Date startDate = DateUtils.getDateStart(DateUtils.addMonth(endDate,-12));
//        finishedOrderDetailService.updateEngineerTuibuOfMiss(startDate,endDate,null);
    }
    @Test
    public void test11111(){
//        Date endDate = new Date();
//        Date startDate = DateUtils.getDateStart(DateUtils.addMonth(endDate,-10));
//        cancelOrderRptService.insertCancelMissData(startDate,endDate);
   }

    @Test
    public void testCancelMid(){
//        Date endDate = new Date();
//        Date startDate = DateUtils.getDateStart(DateUtils.addMonth(endDate,-5));
//        cancelOrderRptService.insertDataOfDate(startDate,endDate);
//        List<OrderDetailRptEntity> returnedOrCanceledOrderList = cancelOrderRptDao.getReturnedOrCanceledOrderList(null, startDate, endDate);
//        for (OrderDetailRptEntity entity:returnedOrCanceledOrderList) {
//            System.out.println(entity.getCancelResponsible().getValue());
//        }
    }

    @Test
    public void testMid(){
//        Date endDate = new Date();
//        Date startDate = DateUtils.getDateStart(DateUtils.addMonth(endDate,-5));
//        String seasonQuarter = QuarterUtils.getSeasonQuarter(startDate);
//        String seasonQuarter1 = QuarterUtils.getSeasonQuarter(endDate);
//        List<Long> idList = finishOrderMidTabDao.getFinishOrderIdList(startDate, endDate,seasonQuarter.equals(seasonQuarter1)?seasonQuarter:null);
//
//        for (Long id:idList) {
//            System.out.println(id.toString());
//        }
    }

    @Test
    public void testMidEngineer(){
//        Date endDate = new Date();
//        Date startDate = DateUtils.getDateStart(DateUtils.addMonth(endDate,-5));
//        String seasonQuarter = QuarterUtils.getSeasonQuarter(startDate);
//        String seasonQuarter1 = QuarterUtils.getSeasonQuarter(endDate);
//        List<Map<String, Long>> engineerFinishOrderId = finishOrderMidTabDao.getEngineerFinishOrderId(startDate, endDate,seasonQuarter.equals(seasonQuarter1)?seasonQuarter:null);
//
//        int index  =0;
//        for (Map<String,Long> map:engineerFinishOrderId) {
//            index++;
//            System.out.println(index+"--"+map.get("orderId").toString()+"--"+map.get("servicePointId").toString());
//        }

    }
    /**
     * 测试网点完成工单写入中间表
     */
    @Test
    public void testEngineerFinish() {
//        Long start = System.currentTimeMillis();
//        Date endDate = new Date();
//        Date startDate = DateUtils.getDateStart(DateUtils.addMonth(endDate,-5));
//           finishedOrderDetailService.insertEngineerOrderOfDate(startDate,endDate);

    }
    @Test
    public void testInsurance(){
//        Date endDate = new Date();
//        Date startDate = DateUtils.getDateStart(DateUtils.addMonth(endDate,-5));
//        List<OrderDetailRptEntity> finishedOrderList = orderDetailRptDao.getFinishedOrderList(null, startDate, endDate, null);
//        for (OrderDetailRptEntity entity:finishedOrderList) {
//            System.out.println(entity.getInsuranceCharge());
//        }
    }


    @Test
    public void test111(){
//        Date endDate = new Date();
//        Date startDate = DateUtils.getDateStart(DateUtils.addMonth(endDate,-5));
//
//        List<OrderDetail> finishedOrderDetailList = orderDetailRptDao.getFinishedOrderDetailList(null, startDate, endDate, null);
//        for (OrderDetail detail:finishedOrderDetailList) {
//            System.out.println(detail.getEngineerPaymentType().getValue());
//        }
    }

    /**
     * 测试网点完成工单写入中间表
     */
    @Test
    public void testEngineerFinishOfMiss() {
//        Long start = System.currentTimeMillis();
//        Date endDate = new Date();
//        Date startDate = DateUtils.getDateStart(DateUtils.addMonth(endDate,-6));
//        try {
//            finishedOrderDetailService.saveEngineerFinishOfMiss( startDate, endDate);
//
//        }catch (Exception e){
//            System.out.println(e.getMessage());
//        }

    }

    @Test
    public void testProcessTime(){


//        Date endDate = new Date();
//
//        Date startDate = DateUtils.getDateStart(DateUtils.addMonth(endDate,-10));
//        processTimeService.insertProcessTimeMissData(startDate,endDate);


    }

    @Test
    public void testQueryFinish(){
        Date endDate = new Date();
        Date startDate = DateUtils.getDateStart(DateUtils.addMonth(endDate,-3));
        //List<FinishedOrderDetail> finishedOrderList = finishOrderMidTabDao.getFinishedOrderList(null, startDate, endDate, null);
//        List<OrderDetailRptEntity> finishOrder = finishedOrderDetailService.getFinishOrder(null, null, startDate, endDate);
//        //Map<String, Object> completedOrderList = orderDetailReportService.getCompletedOrderList(null, null, startDate, endDate);
//        List<OrderDetailRptEntity> servicePointTuiBu = finishedOrderDetailService.getServicePointTuiBu(null, null, startDate, endDate);
        //List<OrderDetailRptEntity> oldFinish = (List<OrderDetailRptEntity>) completedOrderList.get("list");
//        for (OrderDetailRptEntity list : oldFinish) {
//            System.out.println("----------------------------"+list.getExpectCharge());
//
//        }
//        for (OrderDetailRptEntity entity : finishOrder) {
//            System.out.println("************************");
//            for (OrderDetail detail:entity.getDetailList()) {
//                System.out.println("----------------------------"+detail.getEngineerChage());
//                System.out.println("+++++++++++++++++++++"+detail.getCustomerCharge());
//                //
//            }
//
//        }
//        for (OrderDetailRptEntity entity : servicePointTuiBu) {
//            // System.out.println("----------------------------"+entity.getOrderDetail().getServicePoint().getId());
//
//        }
    }

    /**
     * 测试完成工单插入遗漏重写
     */
    @Test
    public void testFinishOfMiss() {
//        Long start = System.currentTimeMillis();
//        Date endDate = new Date();
//        Date startDate = DateUtils.getDateStart(DateUtils.addMonth(endDate,-10));
//
//            finishedOrderDetailService.saveFinishOfMiss(startDate,endDate);


    }
    /**
     * 测试客户退补工单插入遗漏重写
     */
    @Test
    public void testCustomerOfMiss() {
//        Long start = System.currentTimeMillis();
//        Date endDate = new Date();
//        Date startDate = DateUtils.getDateStart(DateUtils.addMonth(endDate,-12));
//        try {
//            finishedOrderDetailService.saveCustomerTuibuOfMiss(startDate,endDate);
//        }catch (Exception e){
//            System.out.println(e.getMessage());
//        }

    }
    /**
     * 测试网点退补工单插入遗漏重写
     */
    @Test
    public void testEngineerOfMiss() {
//        Long start = System.currentTimeMillis();
//        Date endDate = new Date();
//        Date startDate = DateUtils.getDateStart(DateUtils.addMonth(endDate,-12));
//        try {
//            finishedOrderDetailService.saveEngineerTuibuOfMiss(startDate,endDate);
//        }catch (Exception e){
//            System.out.println(e.getMessage());
//        }

    }
    //测试完成工单详情插入到中间表
    @Test
    public void testFinish(){

//        Date endDate = new Date();
//        Date startDate = DateUtils.getDateStart(DateUtils.addMonth(endDate,-10));
//        finishedOrderDetailService.insertCompletedOrderToMid(startDate,endDate);
////        String str = "0,1,444,5433,";
//        String[] split = str.split(",");
//////        System.out.println("----------------------------"+split[3]);
////        for (int i = 0; i < split.length; i++) {
////            System.out.println("----------------------------"+split[i]);
////        }

    }
    //测试网点退补工单详情插入到中间表
    @Test
    public void testServiceTuiBu(){
//        Date endDate = new Date();
//        Date startDate = DateUtils.getDateStart(DateUtils.addMonth(endDate,-10));
//
//        List<OrderDetailRptEntity> finishOrder = finishedOrderDetailService.getServicePointTuiBu(null, null, startDate, endDate);
//        for ( OrderDetailRptEntity item: finishOrder) {
//            int i = finishedOrderDetailService.saveServicePointTuiBu(item);
//            System.out.println("----------------------------"+i);
//        }
        //        System.out.println(finishOrder.size()+"--------------------------------------------");
//        for (OrderDetailRptEntity entity: finishOrder) {
//            System.out.println(entity.getOrderNo());
//        }
    }
    //测试客户退补工单详情插入到中间表
    @Test
    public void testCustomerTuiBu(){
//        Date endDate = new Date();
//        Date startDate = DateUtils.getDateStart(DateUtils.addMonth(endDate,-10));

//        List<OrderDetailRptEntity> finishOrder = finishedOrderDetailService.getCustomerTuiBu(null, null, startDate, endDate);
//        for ( OrderDetailRptEntity item: finishOrder) {
//            int i = finishedOrderDetailService.saveCustomerTuiBu(item);
//            System.out.println("----------------------------"+i);
//        }

//        List<OrderDetailRptEntity> finishOrder = finishedOrderDetailService.getCustomerChargeTuiBuRptData(null, null, startDate, endDate);
//        for ( OrderDetailRptEntity item: finishOrder) {
//            int i = finishedOrderDetailService.saveCustomerTuiBu(item);
//            System.out.println("----------------------------"+i);
//        }
        //        System.out.println(finishOrder.size()+"--------------------------------------------");
//        for (OrderDetailRptEntity entity: finishOrder) {
//            System.out.println(entity.getOrderNo());
//        }
    }

    @Test
    public void testOrderProcessTime() {
//        processTimeRptTasks.saveProcessTime();
    }



    // 测试订单历史资料json产生
    @Test
    public void testOrderDetailReportService() throws InterruptedException {
        //to json
        /*
        HisOrderJson order = orderDetailReportService.getOrderJsonInfoById(894509134752387072l,10);
        try {
            String json = orderDetailReportService.convertHisOrderToJson(order);
            System.out.println(json);
        }catch (Exception e){
            e.printStackTrace();
        }*/
        String ids = "914272762275696640,914304416964808704,914313524266799104,914315559703810048,914322445245222912,914326262187692032,914330020397322240,914334136607576064,914351811568209920,914354308735176704,914356346751684608,914370981299097600,914371156583256064,914374342098423808,914376910681804800,914379280086077440,914380329987477504,914387695776698368";
        String[] list = StringUtils.split(ids,",");
        //not charge
        /*
        for(String id:list) {
            ors.SaveHisOrderDetailById(Long.valueOf(id),null, 30, new Date());
        }*/
        //charged
        //cancel
//        ors.SaveHisOrderDetailById(897386846571732992L,30);
        //return
        /*
        List<Long> ids = Lists.newArrayList();
        ids.add(894773849500164096l);
        ids.add(894859033763389440l);
        ids.add(895104655347163136l);
        ids.add(895185511201906688l);
        ids.add(895590294366724096l);
        ids.add(895590539737702400l);
        ids.add(895113561783934976l);
        ids.add(896624484373766144l);
        ids.add(896661894939811840l);
        ids.add(896937731454935040l);
        ids.add(896931926563364864l);
        for (Long id: ids) {
            ors.SaveHisOrderDetailById(id,40);
            System.out.println("return order id:" + id.toString());
            Thread.sleep(1000l);
        }*/
    }

    /* 测试订单历史资料json产生
    2019/08/29 ryan
    @Test
    public void testOrderTransHistoryJob() {
        Long start = System.currentTimeMillis();
        try {
            //Date endDate = DateUtils.addDays(new Date(),-1);
            Date endDate = new Date();
            Date startDate = DateUtils.getDateStart(DateUtils.addDays(endDate,-3));
            List<Map<String, Object>> list= orderService.findTransToHistoryList(startDate,endDate,3000);
            //Assert.assertNotNull(list);
            //Assert.assertNotEquals(list.size(),0);

            if(list != null && list.size()>0){
                Long orderId;
                Integer status;
                String quarter = new String("");
                Date closeDate;
                int orderType;
                Map<String,Object> map;
                for(int i=0,size=list.size();i<size;i++) {
                    orderId = null;
                    try {
                        map = list.get(i);
                        status = (Integer)map.get("status");
                        switch (status){
                            case 80:
                                orderType=10;
                                break;
                            case 90:
                                orderType = 40;//return
                                break;
                            case 100:
                                orderType = 30;//cancel
                                break;
                            default:
                                orderType = 0;
                                break;
                        }
                        if(orderType==0){
                            continue;
                        }
                        orderId = (Long)map.get("order_id");
                        quarter = (String) map.get("quarter");
                        closeDate = (Date) map.get("close_date");
                        try {
                            //orderDetailReportService.SaveHisOrderDetailById(orderId,quarter, orderType, closeDate);
                        } catch (Exception ex) {
                            LogUtils.saveLog("历史订单记录失败","OrderToHistory",orderId.toString(),ex,null);
                        }
                    } catch (Exception e) {
                        LogUtils.saveLog("历史订单记录失败","OrderToHistory",orderId==null?"":orderId.toString(),e,null);
                    }
                }
            }
            Long end = System.currentTimeMillis();
            StringBuilder message = new StringBuilder();
            double time = 1.0d*(end-start)/1000d;
            message.append("运行时间:")
                    .append(DateUtils.formatDateTime(DateUtils.longToDate(start)))
                    .append(",用时:")
                    .append(StringUtils.formatNum(time))
                    .append("秒");
            LogUtils.saveLog("定时任务", "OrderToHistory", message.toString(), null, null);
        } catch (Exception e) {
            LogUtils.saveLog("定时任务","OrderToHistory","错误,运行时间:" + DateUtils.getDateTime(),e,null);
        }

    }*/

    /**
     * 重新产生订单历史资料

    @Test
    public void recreateOrderHistoryDetail() throws InterruptedException {
        Page<OrderSearchModel> page = new Page<OrderSearchModel>(1,10000);
        OrderSearchModel searchModel = new OrderSearchModel();
        searchModel.setQuarter("20173");
        searchModel.setSearchType("finish");
        IntegerRange statusRange = new IntegerRange(Order.ORDER_STATUS_COMPLETED,0);//完成订单>=80
        searchModel.setStatusRange(statusRange);
//        searchModel.setBeginDate(new Date(2017-1900,8,29));//2017/09/01
//        searchModel.setEndDate(new Date(2017-1900,8,30));//包含 2017/09/25
        searchModel.setPage(page);
        List<Map<String, Object>> list = orderDao.findIdListForKefu(searchModel);

        System.out.println("order count:" + list.size());
        if(list != null && list.size()>0) {
            Long id;
            Integer status;
            int orderType = 10;
            int qty=0;
            Integer chargeFlag;
            for (int i = 0, len = list.size(); i < len; i++) {
                id = (Long) list.get(i).get("order_id");
                status = (Integer) list.get(i).get("status");
                chargeFlag = (Integer)list.get(i).get("charge_flag");
                if (status == 80) {
                    if(chargeFlag==0){
                        System.out.println("not charge, order id:" + id.toString());
                        continue;
                    }
                    qty++;
                    orderType = 10;
                    System.out.println("return order id:" + id.toString());
                } else if (status == 90) {
                    orderType = 40;
                    qty++;
                    System.out.println("return order id:" + id.toString());
                } else if (status == 100) {
                    qty++;
                    orderType = 30;
                    System.out.println("cancel order id:" + id.toString());
                } else {
                    id = null;
                    System.out.println("not action, order id:" + id.toString());
                    continue;
                }
                try {
                    //ors.SaveHisOrderDetailById(id,null, orderType, new Date());
                }catch (Exception e){
                    e.printStackTrace();
                }
                Thread.sleep(1000);
            }
            System.out.print("qty:" + qty);
        }

    }
     */

    /**
     * 重新产生订单历史资料
    @Test
    public void recreateOrderHistoryDetailThread() throws InterruptedException {
        ExecutorService es = Executors.newFixedThreadPool(5);
        Page<OrderSearchModel> page = new Page<OrderSearchModel>(1,5000);
        OrderSearchModel searchModel = new OrderSearchModel();
        searchModel.setQuarter("20173");
        searchModel.setSearchType("finish");
        IntegerRange statusRange = new IntegerRange(Order.ORDER_STATUS_COMPLETED,0);//完成订单>=80
        searchModel.setStatusRange(statusRange);
        searchModel.setPage(page);
        List<Map<String, Object>> list = orderDao.findIdListForKefu(searchModel);
        System.out.println("order count:" + list.size());
        Long start = System.currentTimeMillis();
        if(list != null && list.size()>0) {
            Long id;
            Integer status;
            int orderType = 10;
            int qty=0;
            Integer chargeFlag;
            for (int i = 0, len = list.size(); i < len; i++) {
                id = (Long) list.get(i).get("order_id");
                status = (Integer) list.get(i).get("status");
                chargeFlag = (Integer)list.get(i).get("charge_flag");
                if (status == 80) {
                    if(chargeFlag==0){
                        System.out.println("not charge, order id:" + id.toString());
                        continue;
                    }
                    qty++;
                    orderType = 10;
                    System.out.println("return order id:" + id.toString());
                } else if (status == 90) {
                    orderType = 40;
                    qty++;
                    System.out.println("return order id:" + id.toString());
                } else if (status == 100) {
                    qty++;
                    orderType = 30;
                    System.out.println("cancel order id:" + id.toString());
                } else {
                    id = null;
                    System.out.println("not action, order id:" + id.toString());
                    continue;
                }
                final int index = i;
                final Long orderId = id;
                final int otype = orderType;
                es.execute(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println(Thread.currentThread().getName()+":");
                        try {
                            //ors.SaveHisOrderDetailById(orderId, null,otype, new Date());
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            System.out.print("[Fail] order id:"+orderId);
                        }
                    }
                });
            }
        }
        es.shutdown();
        while (!es.isTerminated()) {
        }
        Long end = System.currentTimeMillis();
        System.out.println(String.format("用时:%s" ,end - start));

        //awaitTermination方法是堵塞式的，只有等真的把线程池停掉才会让程序继续往下执行
        //try {
        //    es.awaitTermination(2, TimeUnit.SECONDS);
        //} catch (InterruptedException e) {
        //    e.printStackTrace();
        //}
    }
    */


    /**
     * 重新产生订单历史资料

    @Test
    public void recreateOrderHistoryDetailSemaphore() throws InterruptedException {
        Semaphore semaphore = new Semaphore(3); //数目
        Page<OrderSearchModel> page = new Page<OrderSearchModel>(1,5000);
        OrderSearchModel searchModel = new OrderSearchModel();
        searchModel.setQuarter("20173");
        searchModel.setSearchType("finish");
        IntegerRange statusRange = new IntegerRange(Order.ORDER_STATUS_COMPLETED,0);//完成订单>=80
        searchModel.setStatusRange(statusRange);
        searchModel.setPage(page);
        List<Map<String, Object>> list = orderDao.findIdListForKefu(searchModel);
        System.out.println("order count:" + list.size());
        Long start = System.currentTimeMillis();
        if(list != null && list.size()>0) {
            Long id;
            Integer status;
            int orderType = 10;
            int qty=0;
            Integer chargeFlag;
            for (int i = 0, len = list.size(); i < len; i++) {
                id = (Long) list.get(i).get("order_id");
                status = (Integer) list.get(i).get("status");
                chargeFlag = (Integer)list.get(i).get("charge_flag");
                if (status == 80) {
                    if(chargeFlag==0){
                        System.out.println("not charge, order id:" + id.toString());
                        continue;
                    }
                    qty++;
                    orderType = 10;
                    System.out.println("return order id:" + id.toString());
                } else if (status == 90) {
                    orderType = 40;
                    qty++;
                    System.out.println("return order id:" + id.toString());
                } else if (status == 100) {
                    qty++;
                    orderType = 30;
                    System.out.println("cancel order id:" + id.toString());
                } else {
                    id = null;
                    System.out.println("not action, order id:" + id.toString());
                    continue;
                }
                new OrderHistoryReportWorker(ors,id,orderType,semaphore).start();
            }
        }

        while (semaphore.hasQueuedThreads()){

        }
        Long end = System.currentTimeMillis();
        System.out.println(String.format("用时:%s" ,end - start));
    }
     */

    /**
     * 历史订单产生线程
     */
//    static class OrderHistoryReportWorker extends Thread{
//        private OrderDetailReportService service;
//        private long orderId;
//        private int orderType;
//        private Semaphore semaphore;
//        public OrderHistoryReportWorker(OrderDetailReportService service,long orderId,int orderType,Semaphore semaphore){
//            this.service = service;
//            this.orderId = orderId;
//            this.orderType = orderType;
//            this.semaphore = semaphore;
//        }
//
//        @Override
//        public void run() {
//            try {
//                System.out.println(this.getName()+":" + orderId);
//                semaphore.acquire();
//                //service.SaveHisOrderDetailById(orderId, null,orderType, new Date());
////                System.out.println("工人"+this.num+"占用一个机器在生产...");
////                Thread.sleep(2000);
////                System.out.println("工人"+this.num+"释放出机器");
//                semaphore.release();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//    }

    // 测试更新客户每日派单数据
    @Test
    public void testUpdateCustomerDialyPlan(){
        Customer customer = customerService.getFromCache(1058l);
        Assert.assertNotNull("读取客户信息:OK",customer);
        CustomerFinance finance = customerService.getFinance(1058l);
        MQCustomerModel c = new MQCustomerModel();
        c.setId(customer.getId());
        c.setCode(customer.getCode());
        c.setName(customer.getName());
        c.setSalesId(customer.getSales().getId());
        c.setSalesMan(customer.getSales().getName());
        c.setPaymentType(Integer.parseInt(finance.getPaymentType().getValue()));
        c.setPaymentTypeName(finance.getPaymentType().getLabel());
        Date date = new Date();//今天
        date = DateUtils.addDays(date,1);//明天
//        mqOrderReportService.updateCustomerDialyPlan(c,date,1,66.0d);
    }

    //更新rpt_servicepoint_balance_monthly表的数据
    @Test
    public void updateRptServicePointBalanceMonthly() {
//        List<ServicePoint> servicePointList = servicePointBalanceMonthlyDao.getAllServicePointIds();
//
//        List<Long> sid = Lists.newArrayList();
//        sid.add(1623957l);
//        sid.add(1629608l);
//        sid.add(1629638l);
//
//        List<Integer> pType = Lists.newArrayList();
//        pType.add(10);
//        pType.add(20);
//        for (Integer item : pType) {
//            for (Long id : sid) {
//                servicePointBalanceMonthlyService.calculateAndUpdateServicePointAllBalanceMonthly(id,item);;
//            }
//
//
//        }
//        List<ServicePoint> servicePointList = Lists.newArrayList();
//        servicePointList.add(new ServicePoint(5003L));
//        servicePointList.add(new ServicePoint(13760L));
//        servicePointList.add(new ServicePoint(13961L));
//        servicePointBalanceMonthlyService.updateRptServicepointBalanceMonthly(2018,5, servicePointList);

//        for (int i=0; i<servicePointList.size(); i=i+100) {
//            servicePointBalanceMonthlyService.updateRptServicepointBalanceMonthly(2018,5, servicePointList.subList(i,i+100));
//        }
//        servicePointBalanceMonthlyService.updateRptServicepointBalanceMonthly(2018,1, servicePointList.subList(31000,servicePointList.size()));
//        List<ServicePoint> list = servicePointDao.findAllList();
//        for (ServicePoint item: list) {
//            servicePointBalanceMonthlyService.calculateAndUpdateServicePointAllBalanceMonthly(item.getId(),20);
//        }
//        List<Integer> servicePointIds = Lists.newArrayList();
//        servicePointIds.add(10622);
//        servicePointIds.add(14032);
//        servicePointIds.add(14564);
//        servicePointIds.add(16895);
//        for (Integer sid: servicePointIds) {
//            servicePointBalanceMonthlyService.calculateAndUpdateServicePointAllBalanceMonthly(sid,10);
//        }
    }


//    /**
//     * 导出完工单明细报表
//     */
//    @Test
//    public void completedReportExport() {
//        Date beginDate = DateUtils.getStartOfDay(DateUtils.parseDate("2017-11-1"));
//        Date endDate = DateUtils.getEndOfDay(DateUtils.parseDate("2017-11-1 23:59:59"));
//        Integer paymentType = null;
//        List<OrderDetailRptEntity> orderMasterList = orderDetailService.getCompletedOrderList(null, paymentType, beginDate, endDate);
//        SXSSFWorkbook xBook = null;
//        try {
//            String xName = "订单完工明细表（" + DateUtils.formatDate(beginDate, "yyyy年MM月dd日") +
//                    "~" + DateUtils.formatDate(endDate, "yyyy年MM月dd日") + "）";
//
//            ExportExcel exportExcel = new ExportExcel();
//            xBook = new SXSSFWorkbook(2000);
//            Sheet xSheet = xBook.createSheet(xName);
//            xSheet.setDefaultColumnWidth(EXECL_CELL_WIDTH_10);
//            Map<String, CellStyle> xStyle = exportExcel.createStyles(xBook);
//            int rowIndex = 0;
//
//            //====================================================绘制标题行============================================================
//            Row titleRow = xSheet.createRow(rowIndex++);
//            titleRow.setHeightInPoints(EXECL_CELL_HEIGHT_TITLE);
//            ExportExcel.createCell(titleRow, 0, xStyle, ExportExcel.CELL_STYLE_NAME_TITLE, xName);
//            xSheet.addMergedRegion(new CellRangeAddress(titleRow.getRowNum(), titleRow.getRowNum(), 0, 55));
//
//            //====================================================绘制表头============================================================
//            //表头第一行
//            Row headerFirstRow = xSheet.createRow(rowIndex++);
//            headerFirstRow.setHeightInPoints(EXECL_CELL_HEIGHT_HEADER);
//
//            ExportExcel.createCell(headerFirstRow, 0, xStyle, ExportExcel.CELL_STYLE_NAME_HEADER, "序号");
//            xSheet.addMergedRegion(new CellRangeAddress(headerFirstRow.getRowNum(), headerFirstRow.getRowNum() + 1, 0, 0));
//
//            ExportExcel.createCell(headerFirstRow, 1, xStyle, ExportExcel.CELL_STYLE_NAME_HEADER, "客户信息");
//            xSheet.addMergedRegion(new CellRangeAddress(headerFirstRow.getRowNum(), headerFirstRow.getRowNum(), 1, 4));
//
//            ExportExcel.createCell(headerFirstRow, 5, xStyle, ExportExcel.CELL_STYLE_NAME_HEADER, "跟进业务员");
//            xSheet.addMergedRegion(new CellRangeAddress(headerFirstRow.getRowNum(), headerFirstRow.getRowNum() + 1, 5, 5));
//
//            ExportExcel.createCell(headerFirstRow, 6, xStyle, ExportExcel.CELL_STYLE_NAME_HEADER, "签约业务员");
//            xSheet.addMergedRegion(new CellRangeAddress(headerFirstRow.getRowNum(), headerFirstRow.getRowNum() + 1, 6, 6));
//
//            ExportExcel.createCell(headerFirstRow, 7, xStyle, ExportExcel.CELL_STYLE_NAME_HEADER, "下单信息");
//            xSheet.addMergedRegion(new CellRangeAddress(headerFirstRow.getRowNum(), headerFirstRow.getRowNum(), 7, 17));
//
//            ExportExcel.createCell(headerFirstRow, 18, xStyle, ExportExcel.CELL_STYLE_NAME_HEADER, "下单时间");
//            xSheet.addMergedRegion(new CellRangeAddress(headerFirstRow.getRowNum(), headerFirstRow.getRowNum() + 1, 18, 18));
//
//            ExportExcel.createCell(headerFirstRow, 19, xStyle, ExportExcel.CELL_STYLE_NAME_HEADER, "客服");
//            xSheet.addMergedRegion(new CellRangeAddress(headerFirstRow.getRowNum(), headerFirstRow.getRowNum() + 1, 19, 19));
//
//            ExportExcel.createCell(headerFirstRow, 20, xStyle, ExportExcel.CELL_STYLE_NAME_HEADER, "派单时间");
//            xSheet.addMergedRegion(new CellRangeAddress(headerFirstRow.getRowNum(), headerFirstRow.getRowNum() + 1, 20, 20));
//
//            ExportExcel.createCell(headerFirstRow, 21, xStyle, ExportExcel.CELL_STYLE_NAME_HEADER, "安维人员信息");
//            xSheet.addMergedRegion(new CellRangeAddress(headerFirstRow.getRowNum(), headerFirstRow.getRowNum(), 21, 27));
//
//            ExportExcel.createCell(headerFirstRow, 28, xStyle, ExportExcel.CELL_STYLE_NAME_HEADER, "预约上门时间");
//            xSheet.addMergedRegion(new CellRangeAddress(headerFirstRow.getRowNum(), headerFirstRow.getRowNum() + 1, 28, 28));
//
//            ExportExcel.createCell(headerFirstRow, 29, xStyle, ExportExcel.CELL_STYLE_NAME_HEADER, "跟综进度");
//            xSheet.addMergedRegion(new CellRangeAddress(headerFirstRow.getRowNum(), headerFirstRow.getRowNum() + 1, 29, 29));
//
//            ExportExcel.createCell(headerFirstRow, 30, xStyle, ExportExcel.CELL_STYLE_NAME_HEADER, "完成日期");
//            xSheet.addMergedRegion(new CellRangeAddress(headerFirstRow.getRowNum(), headerFirstRow.getRowNum() + 1, 30, 30));
//
//            ExportExcel.createCell(headerFirstRow, 31, xStyle, ExportExcel.CELL_STYLE_NAME_HEADER, "实际服务项目");
//            xSheet.addMergedRegion(new CellRangeAddress(headerFirstRow.getRowNum(), headerFirstRow.getRowNum(), 31, 37));
//
//            ExportExcel.createCell(headerFirstRow, 38, xStyle, ExportExcel.CELL_STYLE_NAME_HEADER, "应付安维费用");
//            xSheet.addMergedRegion(new CellRangeAddress(headerFirstRow.getRowNum(), headerFirstRow.getRowNum(), 38, 43));
//
//            ExportExcel.createCell(headerFirstRow, 44, xStyle, ExportExcel.CELL_STYLE_NAME_HEADER, "应收客户货款");
//            xSheet.addMergedRegion(new CellRangeAddress(headerFirstRow.getRowNum(), headerFirstRow.getRowNum(), 44, 49));
//
//            ExportExcel.createCell(headerFirstRow, 50, xStyle, ExportExcel.CELL_STYLE_NAME_HEADER, "状态");
//            xSheet.addMergedRegion(new CellRangeAddress(headerFirstRow.getRowNum(), headerFirstRow.getRowNum() + 1, 50, 50));
//
//
//            ExportExcel.createCell(headerFirstRow, 51, xStyle, ExportExcel.CELL_STYLE_NAME_HEADER, "对帐时间");
//            xSheet.addMergedRegion(new CellRangeAddress(headerFirstRow.getRowNum(), headerFirstRow.getRowNum() + 1, 51, 51));
//
//            ExportExcel.createCell(headerFirstRow, 52, xStyle, ExportExcel.CELL_STYLE_NAME_HEADER, "退补描述");
//            xSheet.addMergedRegion(new CellRangeAddress(headerFirstRow.getRowNum(), headerFirstRow.getRowNum() + 1, 52, 52));
//
//            ExportExcel.createCell(headerFirstRow, 53, xStyle, ExportExcel.CELL_STYLE_NAME_HEADER, "结账日期");
//            xSheet.addMergedRegion(new CellRangeAddress(headerFirstRow.getRowNum(), headerFirstRow.getRowNum() + 1, 53, 53));
//
//            ExportExcel.createCell(headerFirstRow, 54, xStyle, ExportExcel.CELL_STYLE_NAME_HEADER, "付款日期");
//            xSheet.addMergedRegion(new CellRangeAddress(headerFirstRow.getRowNum(), headerFirstRow.getRowNum() + 1, 54, 54));
//
//            ExportExcel.createCell(headerFirstRow, 55, xStyle, ExportExcel.CELL_STYLE_NAME_HEADER, "付款描述");
//            xSheet.addMergedRegion(new CellRangeAddress(headerFirstRow.getRowNum(), headerFirstRow.getRowNum() + 1, 55, 55));
//
//            //表头第二行
//            Row headerSecondRow = xSheet.createRow(rowIndex++);
//            headerSecondRow.setHeightInPoints(EXECL_CELL_HEIGHT_HEADER);
//
//            ExportExcel.createCell(headerSecondRow, 1, xStyle, ExportExcel.CELL_STYLE_NAME_HEADER, "客户编号");
//            ExportExcel.createCell(headerSecondRow, 2, xStyle, ExportExcel.CELL_STYLE_NAME_HEADER, "客户名称");
//            ExportExcel.createCell(headerSecondRow, 3, xStyle, ExportExcel.CELL_STYLE_NAME_HEADER, "签约时间");
//            ExportExcel.createCell(headerSecondRow, 4, xStyle, ExportExcel.CELL_STYLE_NAME_HEADER, "结算方式");
//            ExportExcel.createCell(headerSecondRow, 7, xStyle, ExportExcel.CELL_STYLE_NAME_HEADER, "接单编码");
//            ExportExcel.createCell(headerSecondRow, 8, xStyle, ExportExcel.CELL_STYLE_NAME_HEADER, "服务类型");
//            ExportExcel.createCell(headerSecondRow, 9, xStyle, ExportExcel.CELL_STYLE_NAME_HEADER, "产品");
//            ExportExcel.createCell(headerSecondRow, 10, xStyle, ExportExcel.CELL_STYLE_NAME_HEADER, "型号规格");
//            ExportExcel.createCell(headerSecondRow, 11, xStyle, ExportExcel.CELL_STYLE_NAME_HEADER, "品牌");
//            ExportExcel.createCell(headerSecondRow, 12, xStyle, ExportExcel.CELL_STYLE_NAME_HEADER, "台数");
//            ExportExcel.createCell(headerSecondRow, 13, xStyle, ExportExcel.CELL_STYLE_NAME_HEADER, "下单金额");
//            ExportExcel.createCell(headerSecondRow, 14, xStyle, ExportExcel.CELL_STYLE_NAME_HEADER, "服务描述");
//            ExportExcel.createCell(headerSecondRow, 15, xStyle, ExportExcel.CELL_STYLE_NAME_HEADER, "用户名");
//            ExportExcel.createCell(headerSecondRow, 16, xStyle, ExportExcel.CELL_STYLE_NAME_HEADER, "用户电话");
//            ExportExcel.createCell(headerSecondRow, 17, xStyle, ExportExcel.CELL_STYLE_NAME_HEADER, "用户地址");
//            ExportExcel.createCell(headerSecondRow, 21, xStyle, ExportExcel.CELL_STYLE_NAME_HEADER, "网点编号");
//            ExportExcel.createCell(headerSecondRow, 22, xStyle, ExportExcel.CELL_STYLE_NAME_HEADER, "姓名");
//            ExportExcel.createCell(headerSecondRow, 23, xStyle, ExportExcel.CELL_STYLE_NAME_HEADER, "电话");
//            ExportExcel.createCell(headerSecondRow, 24, xStyle, ExportExcel.CELL_STYLE_NAME_HEADER, "支行");
//            ExportExcel.createCell(headerSecondRow, 25, xStyle, ExportExcel.CELL_STYLE_NAME_HEADER, "账户名");
//            ExportExcel.createCell(headerSecondRow, 26, xStyle, ExportExcel.CELL_STYLE_NAME_HEADER, "账号");
//            ExportExcel.createCell(headerSecondRow, 27, xStyle, ExportExcel.CELL_STYLE_NAME_HEADER, "结算方式");
//            ExportExcel.createCell(headerSecondRow, 31, xStyle, ExportExcel.CELL_STYLE_NAME_HEADER, "上门次数");
//            ExportExcel.createCell(headerSecondRow, 32, xStyle, ExportExcel.CELL_STYLE_NAME_HEADER, "服务类型");
//            ExportExcel.createCell(headerSecondRow, 33, xStyle, ExportExcel.CELL_STYLE_NAME_HEADER, "产品");
//            ExportExcel.createCell(headerSecondRow, 34, xStyle, ExportExcel.CELL_STYLE_NAME_HEADER, "型号规格");
//            ExportExcel.createCell(headerSecondRow, 35, xStyle, ExportExcel.CELL_STYLE_NAME_HEADER, "品牌");
//            ExportExcel.createCell(headerSecondRow, 36, xStyle, ExportExcel.CELL_STYLE_NAME_HEADER, "台数");
//            ExportExcel.createCell(headerSecondRow, 37, xStyle, ExportExcel.CELL_STYLE_NAME_HEADER, "备注");
//            ExportExcel.createCell(headerSecondRow, 38, xStyle, ExportExcel.CELL_STYLE_NAME_HEADER, "服务费");
//            ExportExcel.createCell(headerSecondRow, 39, xStyle, ExportExcel.CELL_STYLE_NAME_HEADER, "配件费");
//            ExportExcel.createCell(headerSecondRow, 40, xStyle, ExportExcel.CELL_STYLE_NAME_HEADER, "远程费");
//            ExportExcel.createCell(headerSecondRow, 41, xStyle, ExportExcel.CELL_STYLE_NAME_HEADER, "拆机费");
//            ExportExcel.createCell(headerSecondRow, 42, xStyle, ExportExcel.CELL_STYLE_NAME_HEADER, "其他费用");
//            ExportExcel.createCell(headerSecondRow, 43, xStyle, ExportExcel.CELL_STYLE_NAME_HEADER, "应付合计");
//            ExportExcel.createCell(headerSecondRow, 44, xStyle, ExportExcel.CELL_STYLE_NAME_HEADER, "服务费");
//            ExportExcel.createCell(headerSecondRow, 45, xStyle, ExportExcel.CELL_STYLE_NAME_HEADER, "配件费");
//            ExportExcel.createCell(headerSecondRow, 46, xStyle, ExportExcel.CELL_STYLE_NAME_HEADER, "配件费");
//            ExportExcel.createCell(headerSecondRow, 47, xStyle, ExportExcel.CELL_STYLE_NAME_HEADER, "配件费");
//            ExportExcel.createCell(headerSecondRow, 45, xStyle, ExportExcel.CELL_STYLE_NAME_HEADER, "配件费");
//            ExportExcel.createCell(headerSecondRow, 46, xStyle, ExportExcel.CELL_STYLE_NAME_HEADER, "远程费");
//            ExportExcel.createCell(headerSecondRow, 47, xStyle, ExportExcel.CELL_STYLE_NAME_HEADER, "拆机费");
//            ExportExcel.createCell(headerSecondRow, 48, xStyle, ExportExcel.CELL_STYLE_NAME_HEADER, "其他费用");
//            ExportExcel.createCell(headerSecondRow, 49, xStyle, ExportExcel.CELL_STYLE_NAME_HEADER, "应收合计");
//
//            xSheet.createFreezePane(0, rowIndex); // 冻结单元格(x, y)
//
//            //====================================================绘制表格数据单元格============================================================
//            int totalCount = 0;
//            double totalBlocked = 0d;
//            int totalActualCount = 0;
//            double totalInCharge = 0d;
//            double totalOutCharge = 0d;
//            if (orderMasterList != null) {
//                int rowNumber = 0;
//                for (OrderDetailRptEntity orderMaster : orderMasterList) {
//                    rowNumber++;
//                    int rowSpan = orderMaster.getMaxRow() - 1;
//                    List<OrderItem> itemList = orderMaster.getItemList();
//                    List<OrderDetail> detailList = orderMaster.getDetailList();
//
//                    Row dataRow = xSheet.createRow(rowIndex++);
//                    dataRow.setHeightInPoints(EXECL_CELL_HEIGHT_DATA);
//                    logger.error(String.format("***%d******", rowNumber));
//                    ExportExcel.createCell(dataRow, 0, xStyle, ExportExcel.CELL_STYLE_NAME_DATA, rowNumber);
//                    if (rowSpan > 0) {
//                        xSheet.addMergedRegion(new CellRangeAddress(dataRow.getRowNum(), dataRow.getRowNum() + rowSpan, 0, 0));
//                    }
//
//                    ExportExcel.createCell(dataRow, 1, xStyle, ExportExcel.CELL_STYLE_NAME_DATA, (orderMaster.getCustomerCode()));
//                    if (rowSpan > 0) {
//                        xSheet.addMergedRegion(new CellRangeAddress(dataRow.getRowNum(), dataRow.getRowNum() + rowSpan, 1, 1));
//                    }
//
//                    ExportExcel.createCell(dataRow, 2, xStyle, ExportExcel.CELL_STYLE_NAME_DATA, (orderMaster.getCustomerName()));
//                    if (rowSpan > 0) {
//                        xSheet.addMergedRegion(new CellRangeAddress(dataRow.getRowNum(), dataRow.getRowNum() + rowSpan, 2, 2));
//                    }
//
//                    ExportExcel.createCell(dataRow, 3, xStyle, ExportExcel.CELL_STYLE_NAME_DATA, (DateUtils.formatDate(orderMaster.getContractDate(), "yyyy-MM-dd")));
//                    if (rowSpan > 0) {
//                        xSheet.addMergedRegion(new CellRangeAddress(dataRow.getRowNum(), dataRow.getRowNum() + rowSpan, 3, 3));
//                    }
//
//                    ExportExcel.createCell(dataRow, 4, xStyle, ExportExcel.CELL_STYLE_NAME_DATA,
//                            (orderMaster.getPaymentType() == null ? "" : orderMaster.getPaymentType().getLabel()));
//                    if (rowSpan > 0) {
//                        xSheet.addMergedRegion(new CellRangeAddress(dataRow.getRowNum(), dataRow.getRowNum() + rowSpan, 4, 4));
//                    }
//
//                    ExportExcel.createCell(dataRow, 5, xStyle, ExportExcel.CELL_STYLE_NAME_DATA, (orderMaster.getSalesName()));
//                    if (rowSpan > 0) {
//                        xSheet.addMergedRegion(new CellRangeAddress(dataRow.getRowNum(), dataRow.getRowNum() + rowSpan, 5, 5));
//                    }
//
//                    ExportExcel.createCell(dataRow, 6, xStyle, ExportExcel.CELL_STYLE_NAME_DATA, (orderMaster.getSalesName()));
//                    if (rowSpan > 0) {
//                        xSheet.addMergedRegion(new CellRangeAddress(dataRow.getRowNum(), dataRow.getRowNum() + rowSpan, 6, 6));
//                    }
//
//                    ExportExcel.createCell(dataRow, 7, xStyle, ExportExcel.CELL_STYLE_NAME_DATA, (orderMaster.getOrderNo()));
//                    if (rowSpan > 0) {
//                        xSheet.addMergedRegion(new CellRangeAddress(dataRow.getRowNum(), dataRow.getRowNum() + rowSpan, 7, 7));
//                    }
//
//                    if (itemList != null && itemList.size() > 0) {
//                        OrderItem item = itemList.get(0);
//                        ExportExcel.createCell(dataRow, 8, xStyle, ExportExcel.CELL_STYLE_NAME_DATA, (item.getServiceType() == null ? "" : item.getServiceType().getName()));
//                        ExportExcel.createCell(dataRow, 9, xStyle, ExportExcel.CELL_STYLE_NAME_DATA, (item.getProduct() == null ? "" : item.getProduct().getName()));
//                        ExportExcel.createCell(dataRow, 10, xStyle, ExportExcel.CELL_STYLE_NAME_DATA, item.getProductSpec());
//                        ExportExcel.createCell(dataRow, 11, xStyle, ExportExcel.CELL_STYLE_NAME_DATA, item.getBrand());
//                        ExportExcel.createCell(dataRow, 12, xStyle, ExportExcel.CELL_STYLE_NAME_DATA, item.getQty());
//                        totalCount = totalCount + (item.getQty() == null ? 0 : item.getQty());
//                    } else {
//                        for (int columnIndex = 8; columnIndex <= 12; columnIndex++) {
//                            ExportExcel.createCell(dataRow, columnIndex, xStyle, ExportExcel.CELL_STYLE_NAME_DATA, "");
//                        }
//                    }
//
//                    double expectCharge = (orderMaster.getExpectCharge());
//                    ExportExcel.createCell(dataRow, 13, xStyle, ExportExcel.CELL_STYLE_NAME_DATA, expectCharge);
//                    totalBlocked = totalBlocked + expectCharge;
//                    if (rowSpan > 0) {
//                        xSheet.addMergedRegion(new CellRangeAddress(dataRow.getRowNum(), dataRow.getRowNum() + rowSpan, 13, 13));
//                    }
//
//                    ExportExcel.createCell(dataRow, 14, xStyle, ExportExcel.CELL_STYLE_NAME_DATA, (orderMaster.getDescription()));
//                    if (rowSpan > 0) {
//                        xSheet.addMergedRegion(new CellRangeAddress(dataRow.getRowNum(), dataRow.getRowNum() + rowSpan, 14, 14));
//                    }
//
//                    ExportExcel.createCell(dataRow, 15, xStyle, ExportExcel.CELL_STYLE_NAME_DATA, (orderMaster.getUserName()));
//                    if (rowSpan > 0) {
//                        xSheet.addMergedRegion(new CellRangeAddress(dataRow.getRowNum(), dataRow.getRowNum() + rowSpan, 15, 15));
//                    }
//
//                    ExportExcel.createCell(dataRow, 16, xStyle, ExportExcel.CELL_STYLE_NAME_DATA, (orderMaster.getUserPhone()));
//                    if (rowSpan > 0) {
//                        xSheet.addMergedRegion(new CellRangeAddress(dataRow.getRowNum(), dataRow.getRowNum() + rowSpan, 16, 16));
//                    }
//
//                    ExportExcel.createCell(dataRow, 17, xStyle, ExportExcel.CELL_STYLE_NAME_DATA, (orderMaster.getUserAddress()));
//                    if (rowSpan > 0) {
//                        xSheet.addMergedRegion(new CellRangeAddress(dataRow.getRowNum(), dataRow.getRowNum() + rowSpan, 17, 17));
//                    }
//
//                    ExportExcel.createCell(dataRow, 18, xStyle, ExportExcel.CELL_STYLE_NAME_DATA,
//                            (DateUtils.formatDate(orderMaster.getCustomerApproveDate(), "yyyy-MM-dd HH:mm:ss")));
//                    if (rowSpan > 0) {
//                        xSheet.addMergedRegion(new CellRangeAddress(dataRow.getRowNum(), dataRow.getRowNum() + rowSpan, 18, 18));
//                    }
//
//                    ExportExcel.createCell(dataRow, 19, xStyle, ExportExcel.CELL_STYLE_NAME_DATA,
//                            (orderMaster.getKefu() == null ? "" : orderMaster.getKefu().getName()));
//                    if (rowSpan > 0) {
//                        xSheet.addMergedRegion(new CellRangeAddress(dataRow.getRowNum(), dataRow.getRowNum() + rowSpan, 19, 19));
//                    }
//
//                    ExportExcel.createCell(dataRow, 20, xStyle, ExportExcel.CELL_STYLE_NAME_DATA,
//                            (DateUtils.formatDate(orderMaster.getPlanDate(), "yyyy-MM-dd HH:mm:ss")));
//                    if (rowSpan > 0) {
//                        xSheet.addMergedRegion(new CellRangeAddress(dataRow.getRowNum(), dataRow.getRowNum() + rowSpan, 20, 20));
//                    }
//
//                    OrderDetail detail = null;
//                    if (detailList != null && detailList.size() > 0) {
//                        detail = detailList.get(0);
//                    }
//                    ExportExcel.createCell(dataRow, 21, xStyle, ExportExcel.CELL_STYLE_NAME_DATA,
//                            (detail == null ? "" : detail.getServicePoint() == null ? "" : detail.getServicePoint().getServicePointNo()));
//                    ExportExcel.createCell(dataRow, 22, xStyle, ExportExcel.CELL_STYLE_NAME_DATA,
//                            (detail == null ? "" : detail.getEngineer() == null ? "" : detail.getEngineer().getName()));
//                    ExportExcel.createCell(dataRow, 23, xStyle, ExportExcel.CELL_STYLE_NAME_DATA,
//                            (detail == null ? "" : detail.getServicePoint() == null ? "" : detail.getServicePoint().getContactInfo1()));
//                    ExportExcel.createCell(dataRow, 24, xStyle, ExportExcel.CELL_STYLE_NAME_DATA,
//                            (detail == null ? "" : detail.getServicePoint() == null ? "" : detail.getServicePoint().getFinance() == null ? "" :
//                                    detail.getServicePoint().getFinance().getBank() == null ? "" : detail.getServicePoint().getFinance().getBank().getLabel()));
//                    ExportExcel.createCell(dataRow, 25, xStyle, ExportExcel.CELL_STYLE_NAME_DATA,
//                            (detail == null ? "" : detail.getServicePoint() == null ? "" : detail.getServicePoint().getFinance() == null ? "" : detail.getServicePoint().getFinance().getBankOwner()));
//                    ExportExcel.createCell(dataRow, 26, xStyle, ExportExcel.CELL_STYLE_NAME_DATA,
//                            (detail == null ? "" : detail.getServicePoint() == null ? "" : detail.getServicePoint().getFinance() == null ? "" : detail.getServicePoint().getFinance().getBankNo()));
//                    ExportExcel.createCell(dataRow, 27, xStyle, ExportExcel.CELL_STYLE_NAME_DATA,
//                            (detail == null ? "" : detail.getServicePoint() == null ? "" : detail.getServicePoint().getFinance() == null ? "" :
//                                    detail.getServicePoint().getFinance().getPaymentType() == null ? "" : detail.getServicePoint().getFinance().getPaymentType().getLabel()));
//
//                    ExportExcel.createCell(dataRow, 28, xStyle, ExportExcel.CELL_STYLE_NAME_DATA,
//                            DateUtils.formatDate(orderMaster.getAppointmentDate(), "yyyy-MM-dd HH:mm:ss"));
//                    if (rowSpan > 0) {
//                        xSheet.addMergedRegion(new CellRangeAddress(dataRow.getRowNum(), dataRow.getRowNum() + rowSpan, 28, 28));
//                    }
//
//                    ExportExcel.createCell(dataRow, 29, xStyle, ExportExcel.CELL_STYLE_NAME_DATA, orderMaster.getTrackingComment());
//                    if (rowSpan > 0) {
//                        xSheet.addMergedRegion(new CellRangeAddress(dataRow.getRowNum(), dataRow.getRowNum() + rowSpan, 29, 29));
//                    }
//
//
//                    ExportExcel.createCell(dataRow, 30, xStyle, ExportExcel.CELL_STYLE_NAME_DATA,
//                            (DateUtils.formatDate(orderMaster.getCloseDate(), "yyyy-MM-dd HH:mm:ss")));
//                    if (rowSpan > 0) {
//                        xSheet.addMergedRegion(new CellRangeAddress(dataRow.getRowNum(), dataRow.getRowNum() + rowSpan, 30, 30));
//                    }
//
//                    ExportExcel.createCell(dataRow, 31, xStyle, ExportExcel.CELL_STYLE_NAME_DATA, (detail == null ? 0 : detail.getServiceTimes()));
//                    ExportExcel.createCell(dataRow, 32, xStyle, ExportExcel.CELL_STYLE_NAME_DATA, (detail == null ? "" : detail.getServiceType() == null ? "" : detail.getServiceType().getName()));
//                    ExportExcel.createCell(dataRow, 33, xStyle, ExportExcel.CELL_STYLE_NAME_DATA, (detail == null ? "" : detail.getProduct() == null ? "" : detail.getProduct().getName()));
//                    ExportExcel.createCell(dataRow, 34, xStyle, ExportExcel.CELL_STYLE_NAME_DATA, (detail == null ? "" : detail.getProductSpec()));
//                    ExportExcel.createCell(dataRow, 35, xStyle, ExportExcel.CELL_STYLE_NAME_DATA, (detail == null ? "" : detail.getBrand()));
//
//                    int qty = (detail == null ? 0 : detail.getQty());
//                    ExportExcel.createCell(dataRow, 36, xStyle, ExportExcel.CELL_STYLE_NAME_DATA, qty);
//                    totalActualCount = totalActualCount + qty;
//
//                    ExportExcel.createCell(dataRow, 37, xStyle, ExportExcel.CELL_STYLE_NAME_DATA, (detail == null ? "" : detail.getRemarks()));
//
//                    double engineerServiceCharge = (detail == null ? 0.0d : detail.getEngineerServiceCharge() == null ? 0.0d : detail.getEngineerServiceCharge());
//                    ExportExcel.createCell(dataRow, 38, xStyle, ExportExcel.CELL_STYLE_NAME_DATA, engineerServiceCharge);
//
//                    double engineerMaterialCharge = (detail == null ? 0.0d : detail.getEngineerMaterialCharge() == null ? 0.0d : detail.getEngineerMaterialCharge());
//                    ExportExcel.createCell(dataRow, 39, xStyle, ExportExcel.CELL_STYLE_NAME_DATA, engineerMaterialCharge);
//
//                    double engineerTravelCharge = (detail == null ? 0.0d : detail.getEngineerTravelCharge() == null ? 0.0d : detail.getEngineerTravelCharge());
//                    ExportExcel.createCell(dataRow, 40, xStyle, ExportExcel.CELL_STYLE_NAME_DATA, engineerTravelCharge);
//
//                    double engineerDismantleCharge = (detail == null ? 0.0d : detail.getEngineerExpressCharge() == null ? 0.0d : detail.getEngineerExpressCharge());
//                    ExportExcel.createCell(dataRow, 41, xStyle, ExportExcel.CELL_STYLE_NAME_DATA, engineerDismantleCharge);
//
//                    double engineerOtherCharge = (detail == null ? 0.0d : detail.getEngineerOtherCharge() == null ? 0.0d : detail.getEngineerOtherCharge());
//                    ExportExcel.createCell(dataRow, 42, xStyle, ExportExcel.CELL_STYLE_NAME_DATA, engineerOtherCharge);
//
//                    ExportExcel.createCell(dataRow, 43, xStyle, ExportExcel.CELL_STYLE_NAME_DATA, (engineerServiceCharge + engineerMaterialCharge + engineerTravelCharge + engineerDismantleCharge + engineerOtherCharge));
//                    totalOutCharge = (totalOutCharge + engineerServiceCharge + engineerMaterialCharge + engineerTravelCharge + engineerDismantleCharge + engineerOtherCharge);
//
//                    double customerServiceCharge = (detail == null ? 0.0d : detail.getCharge() == null ? 0.0d : detail.getCharge());
//                    ExportExcel.createCell(dataRow, 44, xStyle, ExportExcel.CELL_STYLE_NAME_DATA, customerServiceCharge);
//
//                    double customerMaterialCharge = (detail == null ? 0.0d : detail.getMaterialCharge() == null ? 0.0d : detail.getMaterialCharge());
//                    ExportExcel.createCell(dataRow, 45, xStyle, ExportExcel.CELL_STYLE_NAME_DATA, customerMaterialCharge);
//
//                    double customerTravelCharge = (detail == null ? 0.0d : detail.getTravelCharge() == null ? 0.0d : detail.getTravelCharge());
//                    ExportExcel.createCell(dataRow, 46, xStyle, ExportExcel.CELL_STYLE_NAME_DATA, customerTravelCharge);
//
//                    double customerDismantleCharge = (detail == null ? 0.0d : detail.getExpressCharge() == null ? 0.0d : detail.getExpressCharge());
//                    ExportExcel.createCell(dataRow, 47, xStyle, ExportExcel.CELL_STYLE_NAME_DATA, customerDismantleCharge);
//
//                    double customerOtherCharge = (detail == null ? 0.0d : detail.getOtherCharge() == null ? 0.0d : detail.getOtherCharge());
//                    ExportExcel.createCell(dataRow, 48, xStyle, ExportExcel.CELL_STYLE_NAME_DATA, customerOtherCharge);
//
//                    ExportExcel.createCell(dataRow, 49, xStyle, ExportExcel.CELL_STYLE_NAME_DATA, (customerServiceCharge + customerMaterialCharge + customerTravelCharge + customerDismantleCharge + customerOtherCharge));
//                    totalInCharge = (totalInCharge + customerServiceCharge + customerMaterialCharge + customerTravelCharge + customerDismantleCharge + customerOtherCharge);
//
//
//                    ExportExcel.createCell(dataRow, 50, xStyle, ExportExcel.CELL_STYLE_NAME_DATA, orderMaster.getStatus() == null ? "" : orderMaster.getStatus().getLabel());
//                    if (rowSpan > 0) {
//                        xSheet.addMergedRegion(new CellRangeAddress(dataRow.getRowNum(), dataRow.getRowNum() + rowSpan, 50, 50));
//                    }
//
//                    ExportExcel.createCell(dataRow, 51, xStyle, ExportExcel.CELL_STYLE_NAME_DATA, DateUtils.formatDate(orderMaster.getChargeDate(), "yyyy-MM-dd HH:mm:ss"));
//                    if (rowSpan > 0) {
//                        xSheet.addMergedRegion(new CellRangeAddress(dataRow.getRowNum(), dataRow.getRowNum() + rowSpan, 51, 51));
//                    }
//
//                    ExportExcel.createCell(dataRow, 52, xStyle, ExportExcel.CELL_STYLE_NAME_DATA, orderMaster.getTuiBuRemarks());
//                    if (rowSpan > 0) {
//                        xSheet.addMergedRegion(new CellRangeAddress(dataRow.getRowNum(), dataRow.getRowNum() + rowSpan, 52, 52));
//                    }
//
//                    ExportExcel.createCell(dataRow, 53, xStyle, ExportExcel.CELL_STYLE_NAME_DATA, DateUtils.formatDate(orderMaster.getCustomerInvoiceDate(), "yyyy-MM-dd HH:mm:ss"));
//                    if (rowSpan > 0) {
//                        xSheet.addMergedRegion(new CellRangeAddress(dataRow.getRowNum(), dataRow.getRowNum() + rowSpan, 53, 53));
//                    }
//
//                    ExportExcel.createCell(dataRow, 54, xStyle, ExportExcel.CELL_STYLE_NAME_DATA, detail == null ? "" : DateUtils.formatDate(detail.getEngineerInvoiceDate(), "yyyy-MM-dd HH:mm:ss"));
//                    ExportExcel.createCell(dataRow, 55, xStyle, ExportExcel.CELL_STYLE_NAME_DATA, detail == null ? "" : detail.getEngineerInvoiceRemarks());
//
//                    if (rowSpan > 0) {
//                        for (int index = 1; index <= rowSpan; index++) {
//                            dataRow = xSheet.createRow(rowIndex++);
//                            if (itemList != null && index < itemList.size()) {
//                                OrderItem item = itemList.get(index);
//
//                                ExportExcel.createCell(dataRow, 8, xStyle, ExportExcel.CELL_STYLE_NAME_DATA, (item.getServiceType() == null ? "" : item.getServiceType().getName()));
//                                ExportExcel.createCell(dataRow, 9, xStyle, ExportExcel.CELL_STYLE_NAME_DATA, (item.getProduct() == null ? "" : item.getProduct().getName()));
//                                ExportExcel.createCell(dataRow, 10, xStyle, ExportExcel.CELL_STYLE_NAME_DATA, item.getProductSpec());
//                                ExportExcel.createCell(dataRow, 11, xStyle, ExportExcel.CELL_STYLE_NAME_DATA, item.getBrand());
//                                ExportExcel.createCell(dataRow, 12, xStyle, ExportExcel.CELL_STYLE_NAME_DATA, item.getQty());
//                                totalCount = totalCount + StringUtils.toInteger(item.getQty());
//                            } else {
//                                for (int columnIndex = 8; columnIndex <= 12; columnIndex++) {
//                                    ExportExcel.createCell(dataRow, columnIndex, xStyle, ExportExcel.CELL_STYLE_NAME_DATA, "");
//                                }
//                            }
//
//                            if (detailList != null && index < detailList.size()) {
//                                detail = detailList.get(index);
//
//                                ExportExcel.createCell(dataRow, 21, xStyle, ExportExcel.CELL_STYLE_NAME_DATA,
//                                        (detail == null ? "" : detail.getServicePoint() == null ? "" : detail.getServicePoint().getName()));
//                                ExportExcel.createCell(dataRow, 22, xStyle, ExportExcel.CELL_STYLE_NAME_DATA,
//                                        (detail == null ? "" : detail.getEngineer() == null ? "" : detail.getEngineer().getName()));
//                                ExportExcel.createCell(dataRow, 23, xStyle, ExportExcel.CELL_STYLE_NAME_DATA,
//                                        (detail == null ? "" : detail.getServicePoint() == null ? "" : detail.getServicePoint().getContactInfo1()));
//                                ExportExcel.createCell(dataRow, 24, xStyle, ExportExcel.CELL_STYLE_NAME_DATA,
//                                        (detail == null ? "" : detail.getServicePoint() == null ? "" : detail.getServicePoint().getFinance() == null ? "" : detail.getServicePoint().getFinance().getBank() == null ? "" : detail.getServicePoint().getFinance().getBank().getLabel()));
//                                ExportExcel.createCell(dataRow, 25, xStyle, ExportExcel.CELL_STYLE_NAME_DATA,
//                                        (detail == null ? "" : detail.getServicePoint() == null ? "" : detail.getServicePoint().getFinance() == null ? "" : detail.getServicePoint().getFinance().getBankOwner()));
//                                ExportExcel.createCell(dataRow, 26, xStyle, ExportExcel.CELL_STYLE_NAME_DATA,
//                                        (detail == null ? "" : detail.getServicePoint() == null ? "" : detail.getServicePoint().getFinance() == null ? "" : detail.getServicePoint().getFinance().getBankNo()));
//                                ExportExcel.createCell(dataRow, 27, xStyle, ExportExcel.CELL_STYLE_NAME_DATA,
//                                        (detail == null ? "" : detail.getServicePoint() == null ? "" : detail.getServicePoint().getFinance() == null ? "" : detail.getServicePoint().getFinance().getPaymentType() == null ? "" : detail.getServicePoint().getFinance().getPaymentType().getLabel()));
//                                ExportExcel.createCell(dataRow, 28, xStyle, ExportExcel.CELL_STYLE_NAME_DATA,
//                                        (DateUtils.formatDate(orderMaster.getAppointmentDate(), "yyyy-MM-dd HH:mm:ss")));
//                                ExportExcel.createCell(dataRow, 29, xStyle, ExportExcel.CELL_STYLE_NAME_DATA, "");//cell.setCellValue(orderMaster.get("trackingComment") == null ? "" : orderMaster.get("trackingComment").toString());
//                                ExportExcel.createCell(dataRow, 30, xStyle, ExportExcel.CELL_STYLE_NAME_DATA,
//                                        (DateUtils.formatDate(orderMaster.getCloseDate(), "yyyy-MM-dd HH:mm:ss")));
//                                ExportExcel.createCell(dataRow, 31, xStyle, ExportExcel.CELL_STYLE_NAME_DATA, detail.getServiceTimes());
//                                ExportExcel.createCell(dataRow, 32, xStyle, ExportExcel.CELL_STYLE_NAME_DATA, (detail.getServiceType() == null ? "" : detail.getServiceType().getName()));
//                                ExportExcel.createCell(dataRow, 33, xStyle, ExportExcel.CELL_STYLE_NAME_DATA, (detail.getProduct() == null ? "" : detail.getProduct().getName()));
//                                ExportExcel.createCell(dataRow, 34, xStyle, ExportExcel.CELL_STYLE_NAME_DATA, detail.getProductSpec());
//                                ExportExcel.createCell(dataRow, 35, xStyle, ExportExcel.CELL_STYLE_NAME_DATA, detail.getBrand());
//
//                                int orderDetailQty = (detail == null ? 0 : detail.getQty());
//                                ExportExcel.createCell(dataRow, 36, xStyle, ExportExcel.CELL_STYLE_NAME_DATA, orderDetailQty);
//                                totalActualCount = totalActualCount + orderDetailQty;
//
//                                ExportExcel.createCell(dataRow, 37, xStyle, ExportExcel.CELL_STYLE_NAME_DATA, detail.getRemarks());
//
//                                engineerServiceCharge = (detail == null ? 0.0d : detail.getEngineerServiceCharge() == null ? 0.0d : detail.getEngineerServiceCharge());
//                                ExportExcel.createCell(dataRow, 38, xStyle, ExportExcel.CELL_STYLE_NAME_DATA, engineerServiceCharge);
//
//                                engineerMaterialCharge = detail == null ? 0.0d : detail.getEngineerMaterialCharge() == null ? 0.0d : detail.getEngineerMaterialCharge();
//                                ExportExcel.createCell(dataRow, 39, xStyle, ExportExcel.CELL_STYLE_NAME_DATA, engineerMaterialCharge);
//
//                                engineerTravelCharge = detail == null ? 0.0d : detail.getEngineerTravelCharge() == null ? 0.0d : detail.getEngineerTravelCharge();
//                                ExportExcel.createCell(dataRow, 40, xStyle, ExportExcel.CELL_STYLE_NAME_DATA, engineerTravelCharge);
//
//                                engineerDismantleCharge = detail == null ? 0.0d : detail.getEngineerExpressCharge() == null ? 0.0d : detail.getEngineerExpressCharge();
//                                ExportExcel.createCell(dataRow, 41, xStyle, ExportExcel.CELL_STYLE_NAME_DATA, engineerDismantleCharge);
//
//                                engineerOtherCharge = detail == null ? 0.0d : detail.getEngineerOtherCharge() == null ? 0.0d : detail.getEngineerOtherCharge();
//                                ExportExcel.createCell(dataRow, 42, xStyle, ExportExcel.CELL_STYLE_NAME_DATA, engineerOtherCharge);
//
//                                ExportExcel.createCell(dataRow, 43, xStyle, ExportExcel.CELL_STYLE_NAME_DATA, (engineerServiceCharge + engineerMaterialCharge + engineerTravelCharge + engineerDismantleCharge + engineerOtherCharge));
//                                totalOutCharge = totalOutCharge + engineerServiceCharge + engineerMaterialCharge + engineerTravelCharge + engineerDismantleCharge + engineerOtherCharge;
//
//                                customerServiceCharge = detail == null ? 0.0d : detail.getCharge() == null ? 0.0d : detail.getCharge();
//                                ExportExcel.createCell(dataRow, 44, xStyle, ExportExcel.CELL_STYLE_NAME_DATA, customerServiceCharge);
//
//                                customerMaterialCharge = detail == null ? 0.0d : detail.getMaterialCharge() == null ? 0.0d : detail.getMaterialCharge();
//                                ExportExcel.createCell(dataRow, 45, xStyle, ExportExcel.CELL_STYLE_NAME_DATA, customerMaterialCharge);
//
//                                customerTravelCharge = detail == null ? 0.0d : detail.getTravelCharge() == null ? 0.0d : detail.getTravelCharge();
//                                ExportExcel.createCell(dataRow, 46, xStyle, ExportExcel.CELL_STYLE_NAME_DATA, customerTravelCharge);
//
//                                customerDismantleCharge = detail == null ? 0.0d : detail.getExpressCharge() == null ? 0.0d : detail.getExpressCharge();
//                                ExportExcel.createCell(dataRow, 47, xStyle, ExportExcel.CELL_STYLE_NAME_DATA, customerDismantleCharge);
//
//                                customerOtherCharge = detail == null ? 0.0d : detail.getOtherCharge() == null ? 0.0d : detail.getOtherCharge();
//                                ExportExcel.createCell(dataRow, 48, xStyle, ExportExcel.CELL_STYLE_NAME_DATA, customerOtherCharge);
//
//                                ExportExcel.createCell(dataRow, 49, xStyle, ExportExcel.CELL_STYLE_NAME_DATA, (customerServiceCharge + customerMaterialCharge + customerTravelCharge + customerDismantleCharge + customerOtherCharge));
//                                totalInCharge = totalInCharge + customerServiceCharge + customerMaterialCharge + customerTravelCharge + customerDismantleCharge + customerOtherCharge;
//
//                                ExportExcel.createCell(dataRow, 54, xStyle, ExportExcel.CELL_STYLE_NAME_DATA, DateUtils.formatDate(detail.getEngineerInvoiceDate(), "yyyy-MM-dd HH:mm:ss"));
//                                ExportExcel.createCell(dataRow, 55, xStyle, ExportExcel.CELL_STYLE_NAME_DATA, detail.getEngineerInvoiceRemarks());
//                            } else {
//                                for (int columnIndex = 20; columnIndex <= 25; columnIndex++) {
//                                    ExportExcel.createCell(dataRow, columnIndex, xStyle, ExportExcel.CELL_STYLE_NAME_DATA, "");
//                                }
//
//                                for (int columnIndex = 29; columnIndex <= 55; columnIndex++) {
//                                    ExportExcel.createCell(dataRow, columnIndex, xStyle, ExportExcel.CELL_STYLE_NAME_DATA, "");
//                                }
//                            }
//                        }
//                    }
//                }
//                Row dataRow = xSheet.createRow(rowIndex++);
//                dataRow.setHeightInPoints(EXECL_CELL_HEIGHT_DATA);
//                ExportExcel.createCell(dataRow, 0, xStyle, ExportExcel.CELL_STYLE_NAME_HEADER, "合计");
//                xSheet.addMergedRegion(new CellRangeAddress(dataRow.getRowNum(), dataRow.getRowNum(), 0, 11));
//
//                ExportExcel.createCell(dataRow, 12, xStyle, ExportExcel.CELL_STYLE_NAME_DATA, "");
//                ExportExcel.createCell(dataRow, 13, xStyle, ExportExcel.CELL_STYLE_NAME_DATA, "");
//
//                ExportExcel.createCell(dataRow, 14, xStyle, ExportExcel.CELL_STYLE_NAME_HEADER, "");
//                xSheet.addMergedRegion(new CellRangeAddress(dataRow.getRowNum(), dataRow.getRowNum(), 14, 35));
//
//                ExportExcel.createCell(dataRow, 36, xStyle, ExportExcel.CELL_STYLE_NAME_DATA, totalActualCount);
//
//                ExportExcel.createCell(dataRow, 37, xStyle, ExportExcel.CELL_STYLE_NAME_HEADER, "");
//                xSheet.addMergedRegion(new CellRangeAddress(dataRow.getRowNum(), dataRow.getRowNum(), 37, 41));
//
//                ExportExcel.createCell(dataRow, 42, xStyle, ExportExcel.CELL_STYLE_NAME_HEADER, "应付合计");
//                ExportExcel.createCell(dataRow, 43, xStyle, ExportExcel.CELL_STYLE_NAME_DATA, totalOutCharge);
//
//                ExportExcel.createCell(dataRow, 44, xStyle, ExportExcel.CELL_STYLE_NAME_HEADER, "");
//                xSheet.addMergedRegion(new CellRangeAddress(dataRow.getRowNum(), dataRow.getRowNum(), 44, 47));
//
//                ExportExcel.createCell(dataRow, 48, xStyle, ExportExcel.CELL_STYLE_NAME_HEADER, "应收合计");
//                ExportExcel.createCell(dataRow, 49, xStyle, ExportExcel.CELL_STYLE_NAME_DATA, totalInCharge);
//
//                ExportExcel.createCell(dataRow, 50, xStyle, ExportExcel.CELL_STYLE_NAME_HEADER, "");
//                xSheet.addMergedRegion(new CellRangeAddress(dataRow.getRowNum(), dataRow.getRowNum(), 50, 55));
//            }
//            //[end] create data
////			logger.error(String.format("********************************%d****************************", System.currentTimeMillis()));
//            //输出到磁盘中
//            FileOutputStream fileOutputStream = new FileOutputStream(new File("C:\\Zhoucy\\Workspace\\uploads"));
//            xBook.write(fileOutputStream);
//            xBook.dispose();
//        }
//        catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            xBook.dispose();
//        }
//    }

    @Test
    public void recreateCompletedOrderAndCustomerTuiBuAndServicePointTuiBu() {

        Date beginDate = DateUtils.parseDate("2019-8-1 00:00:00");
        Date endDate = DateUtils.getEndOfDay(DateUtils.parseDate("2019-8-30 23:59:59"));
        List<Date> dateList = Lists.newArrayList();
        while (beginDate.getTime() < endDate.getTime()) {
            dateList.add(beginDate);
            beginDate = DateUtils.addDays(beginDate, 1);
        }

        Date sDate = null;
        Date eDate = null;
//        List<Long> customerIds = Lists.newArrayList(17L,618L, 1584L, 1044L,1059L,1402L, 1495L,1332L);
//        for (Long  customerId: customerIds) {
            for (Date date : dateList) { //2018-01-29T00:00:00.000+0800 2018-01-29当天的未执行
                sDate = DateUtils.getStartOfDay(date);
                eDate = DateUtils.getEndOfDay(date);
//                processTimeService.insertProcessTimeMissData(sDate, eDate);
//            sDate = DateUtils.parseDate("2018-5-24 18:00:00");
//            eDate = DateUtils.parseDate("2018-5-24 23:59:59");
//                finishedOrderDetailService.updateCompleteOrder(sDate, eDate, customerId);
//                finishedOrderDetailService.updateCustomerTuibu(sDate, eDate, customerId);
//                finishedOrderDetailService.updateEngineerTuibuOfMiss(sDate, eDate, customerId);

//                finishedOrderDetailService.saveFinishOfMiss(sDate, eDate);
//               finishedOrderDetailService.saveEngineerFinishOfMiss(sDate, eDate);
//                finishedOrderDetailService.saveCustomerTuibuOfMiss(sDate, eDate);
//                finishedOrderDetailService.saveEngineerTuibuOfMiss(sDate, eDate);
//                cancelOrderRptService.insertCancelMissData(sDate, eDate);
//                processTimeService.insertProcessTimeMissData(sDate, eDate);
//                 processTimeService.insertProcessTimeRpt(sDate, eDate);

//                finishedOrderDetailService.insertCompletedOrderToMid(sDate, eDate);
//                finishedOrderDetailService.insertEngineerOrderOfDate(sDate, eDate);



//                cancelOrderRptService.insertDataOfDate(sDate, eDate);

//            List<OrderDetailRptEntity> list = cancelOrderRptService.getReturnOrCancelOrderOfDate(sDate, endDate);

//            list.size();
//            }
        }

    }

    @Test
    public void testGetShop() {
        Map<Integer, Map<String, B2bCustomerMap>> allShopMap = B2BMapUtils.getAllShopMap();
        B2bCustomerMap shop = B2BMapUtils.getShop(2, "500295137", allShopMap);
        B2bCustomerMap shop2 = shop;
    }

//    @Autowired
//    private RebuildRptDataService rebuildRptDataService;

//    @Test
//    public void testUpdateRptKefuDaily() {
//        rebuildRptDataService.rebuidKefuAcceptOrderQty(2018, 10, 28);
//    }

    /**
     * 导出突击单

    @Test
    public void exportCrushReport(){
        OrderCrushSearchVM orderCrush = new OrderCrushSearchVM();
        orderCrush.setQuarter("20184");
        orderCrush.setStatus(null);
        orderCrush.setBeginDate(DateUtils.parseDate("2018-11-01"));
        orderCrush.setEndDate(DateUtils.getDateEnd(DateUtils.parseDate("2018-11-30")));
        //List<OrderCrush> list = orderDao.findOrderCrushList(orderCrush);
        Page<OrderCrushSearchVM> page = new Page<OrderCrushSearchVM>(1,10000);
        Page<OrderCrush> rtnPage = crushService.findOrderCrushList(page, orderCrush);
        List<OrderCrush> list =  rtnPage.getList();
        if(list == null || list.isEmpty()){
            System.out.println("no data");
            return;
        }
        int cnt = list.size();
        List<CrushReportEntry> reportEntries = Lists.newArrayListWithCapacity(cnt);
        Order order;
        OrderCondition orderCondition;
        OrderFee orderFee;
        List<OrderItem> items;
        ServicePoint servicePoint;
        Engineer engineer;
        Map<Long, ServiceType> serviceTypes = serviceTypeService.getAllServiceTypeMap();
        if(serviceTypes==null || serviceTypes.isEmpty()){
            System.out.println("no servie types");
            return;
        }
        Map<Long, Product> products = ProductUtils.getAllProductMap();
        if(products==null || products.isEmpty()){
            System.out.println("no products");
            return;
        }
        OrderCrush crush;
        CrushReportEntry entry;
        String quarter = new String("");
        String[] types;
        StringBuilder sb = sb=new StringBuilder(200);
        ServiceType serviceType;
        List<OrderPlan> orderPlanList;
        OrderPlan orderPlan;
        Long orderId;
        Product product;
        String info = new String("");
        //region data
        for(int i=0;i<cnt;i++){
            crush =list.get(i);
            orderId = crush.getOrderId();
            quarter = crush.getQuarter();
            System.out.printf("orderId:%d \n",orderId);
            entry = new CrushReportEntry();
            entry.setCreateBy(crush.getCreateBy());
            entry.setCreateDate(crush.getCreateDate());
            entry.setCreateDateString(DateUtils.formatDate(crush.getCreateDate(),"yyyy.mm.dd"));
            entry.setOrderNo(crush.getOrderNo());
            entry.setOrderId(orderId);
            entry.setAcceptor(crush.getCloseBy());
            entry.setStatus(crush.getStatus()==1?"已完成":"突击中");
            //get order
            order = orderService.getOrderById(orderId,quarter, OrderUtils.OrderDataLevel.FEE,true);
            if(order == null){
                System.out.println("get order error:"+crush.getOrderNo());
            }else{
                orderCondition = order.getOrderCondition();
                //service types
                sb.setLength(0);
                sb.append(orderCondition.getServiceTypes());
                types = StringUtils.split(sb.toString(),",,");
                sb.setLength(0);
                for (String type:types){
                    if(StringUtils.isNotBlank(type) && !type.equals(",")){
                        serviceType = serviceTypes.get(Long.parseLong(type));
                        if(serviceType != null){
                            if(sb.length() > 0){
                                sb.append(",");
                            }
                            sb.append(serviceType.getName());
                        }
                    }
                }
                entry.setServiceTypes(sb.toString());
                //servicepoint & engineer
                orderPlan = null;
                orderPlanList = orderService.getOrderPlanList(orderId,quarter,1);
                if(orderPlanList!=null && !orderPlanList.isEmpty()){
                    final Date createDate = crush.getCreateDate();
                    orderPlan = orderPlanList.stream().filter(t-> t.getCreateDate().getTime()> createDate.getTime()).sorted(Comparator.comparing(OrderPlan::getCreateDate)).findFirst().orElse(null);
                    if(orderPlan != null) {
                        servicePoint = servicePointService.getFromCache(orderPlan.getServicePoint().getId());
                        engineer = servicePointService.getEngineerFromCache(orderPlan.getServicePoint().getId(), orderPlan.getEngineer().getId());
                        sb.setLength(0);
                        sb.append(servicePoint.getServicePointNo()).append(" ").append(servicePoint.getFinance().getBankIssue().getLabel())
                                .append(" 姓名：").append(engineer.getName()).append("(主)").append(" 手机号：").append(engineer.getContactInfo());
                        entry.setEngineer(sb.toString());
                    }else{
                        System.out.println("no plan data:" + orderId.toString());
                    }
                }

                //order info
                sb.setLength(0);
                sb.append("单号:").append(order.getOrderNo()).append(" ").append(orderCondition.getCustomer().getName()).append("\n");
                sb.append("联系人:").append(orderCondition.getUserName()).append(" ").append(orderCondition.getServicePhone()).append(" ")
                        .append(orderCondition.getAreaName()).append(" ").append(orderCondition.getServiceAddress())
                        .append(" 电话：").append(orderCondition.getPhone1()).append(" 座机:").append(orderCondition.getPhone2()).append("\n");
                sb.append("服务明细: ");
                items = OrderItemUtils.fromOrderItemsJson(order.getOrderItemJson());
                if(items == null || items.isEmpty()){
                    System.out.println("items is empty");
                }
                for(OrderItem item:items){
                    product = products.get(item.getProduct().getId());
                    serviceType = serviceTypes.get(item.getServiceType().getId());
                    if(product != null && serviceType !=null) {
                        sb.append(" ").append(product.getName()).append(" ").append(item.getQty()).append(" ").append(serviceType.getName());
                    }
                }
                sb.append("\n");
                sb.append("服务描述:").append(order.getDescription());
                entry.setOrderInfo(sb.toString());
                orderFee = order.getOrderFee();
                entry.setCharge(orderFee.getExpectCharge());
                entry.setTravelCharge(orderPlan==null?0.0:orderPlan.getEstimatedTravelCost());
                entry.setOtherCharge(orderPlan==null?0.0:orderPlan.getEstimatedOtherCost());

                reportEntries.add(entry);
            }
            //endregion

            //region write to excel
            ExportExcel exportExcel = new ExportExcel();
            //SXSSFWorkbook xBook = new SXSSFWorkbook(5000);
            HSSFWorkbook xBook = new HSSFWorkbook();
            try {
                String title = "厨卫开发部突击表";
                //SXSSFSheet xSheet = xBook.createSheet(title);
                HSSFSheet xSheet = xBook.createSheet(title);
                Map<String, CellStyle> xStyle;
                xSheet.setDefaultColumnWidth(14);
                //xStyle = exportExcel.createStyles(xBook);
                CellStyle titleStyle = xBook.createCellStyle();
                Font dataFont = xBook.createFont();
                dataFont.setFontName("宋体");//Arial
                dataFont.setFontHeightInPoints((short) 12);
                titleStyle.setFont(dataFont);
                titleStyle.setWrapText(true);
                titleStyle.setAlignment(CellStyle.ALIGN_CENTER); // 设置单元格水平方向对齐方式
                titleStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
                //边框
                titleStyle.setBorderBottom(CellStyle.BORDER_THIN);
                titleStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
                titleStyle.setBorderLeft(CellStyle.BORDER_THIN);
                titleStyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());
                titleStyle.setBorderRight(CellStyle.BORDER_THIN);
                titleStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());
                titleStyle.setBorderTop(CellStyle.BORDER_THIN);
                titleStyle.setTopBorderColor(IndexedColors.BLACK.getIndex());

                CellStyle dataStyle = xBook.createCellStyle();
                dataFont = xBook.createFont();
                dataFont.setFontName("宋体");
                dataFont.setFontHeightInPoints((short) 11);
                dataStyle.setFont(dataFont);
                dataStyle.setWrapText(true);//自动换行
                dataStyle.setAlignment(CellStyle.ALIGN_CENTER); // 设置单元格水平方向对齐方式
                dataStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
                dataStyle.setAlignment(CellStyle.ALIGN_CENTER); // 设置单元格水平方向对齐方式
                dataStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
                //边框
                dataStyle.setBorderBottom(CellStyle.BORDER_THIN);
                dataStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
                dataStyle.setBorderLeft(CellStyle.BORDER_THIN);
                dataStyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());
                dataStyle.setBorderRight(CellStyle.BORDER_THIN);
                dataStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());
                dataStyle.setBorderTop(CellStyle.BORDER_THIN);
                dataStyle.setTopBorderColor(IndexedColors.BLACK.getIndex());

                // 加入标题
                int rowNum = 0;
                Row titleRow = xSheet.createRow(rowNum++); // 添加一行
                titleRow.setHeightInPoints(30); // row高度
                Cell titleCell = titleRow.createCell(0); // 对cell(0)编辑, 对应A1
                titleCell.setCellStyle(titleStyle); // cell样式
                titleCell.setCellValue(title); // 写入cell内容
                xSheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 13));
                Row headRow;
                Row dataRow;
                Cell cell;
                HSSFCell hssfCell;
                //xSheet.setColumnWidth(12, 50 * 256);
                //xSheet.autoSizeColumn((short)0); //调整第一列宽度
                xSheet.setColumnWidth((short)0,(int)5*256+184);
                xSheet.setColumnWidth((short)1,(int)10.83*256+184);
                xSheet.setColumnWidth((short)2,(int)10.83*256+184);
                xSheet.setColumnWidth((short)3,(int)10.83*256+184);
                xSheet.setColumnWidth((short)4,(int)15.67*256+184);
                xSheet.setColumnWidth((short)5,(int)8.17*256+184);
                xSheet.setColumnWidth((short)6,(int)10.33*256+184);
                xSheet.setColumnWidth((short)7,(int)22*256+184);
                xSheet.setColumnWidth((short)8,(int)50*256+184);
                xSheet.setColumnWidth((short)9,(int)8.33*256+184);
                xSheet.setColumnWidth((short)10,(int)4*256+184);
                xSheet.setColumnWidth((short)11,(int)4*256+184);
                xSheet.setColumnWidth((short)12,(int)7.33*256+184);
                xSheet.setColumnWidth((short)13,(int)15.83*256+184);

                // 加入表头
                headRow = xSheet.createRow(rowNum++);
                headRow.setHeightInPoints(16);
                Row sencondHead = xSheet.createRow(rowNum++);

                //第一行
                cell = headRow.createCell(0);
                cell.setCellValue("序号");
                xSheet.addMergedRegion(new CellRangeAddress(headRow.getRowNum(), headRow.getRowNum() + 1, cell.getColumnIndex(), cell.getColumnIndex()));
                cell.setCellStyle(titleStyle);

                cell = headRow.createCell(1);
                cell.setCellValue("日期");
                xSheet.addMergedRegion(new CellRangeAddress(headRow.getRowNum(), headRow.getRowNum() + 1, cell.getColumnIndex(), cell.getColumnIndex()));
                cell.setCellStyle(titleStyle);

                cell = headRow.createCell(2);
                cell.setCellValue("接单人");
                xSheet.addMergedRegion(new CellRangeAddress(headRow.getRowNum(), headRow.getRowNum() + 1, cell.getColumnIndex(), cell.getColumnIndex()));
                cell.setCellStyle(titleStyle);

                cell = headRow.createCell(3);
                cell.setCellValue("发单人");
                xSheet.addMergedRegion(new CellRangeAddress(headRow.getRowNum(), headRow.getRowNum() + 1, cell.getColumnIndex(), cell.getColumnIndex()));
                cell.setCellStyle(titleStyle);

                cell = headRow.createCell(4);
                cell.setCellValue("单号");
                xSheet.addMergedRegion(new CellRangeAddress(headRow.getRowNum(), headRow.getRowNum() + 1, cell.getColumnIndex(), cell.getColumnIndex()));
                cell.setCellStyle(titleStyle);

                cell = headRow.createCell(5);
                cell.setCellValue("执行项目");
                xSheet.addMergedRegion(new CellRangeAddress(headRow.getRowNum(), headRow.getRowNum() + 1, cell.getColumnIndex(), cell.getColumnIndex()));
                cell.setCellStyle(titleStyle);

                cell = headRow.createCell(6);
                cell.setCellValue("突击成功");
                xSheet.addMergedRegion(new CellRangeAddress(headRow.getRowNum(), headRow.getRowNum() + 1, cell.getColumnIndex(), cell.getColumnIndex()));
                cell.setCellStyle(titleStyle);

                cell = headRow.createCell(7);
                cell.setCellValue("师傅信息");
                xSheet.addMergedRegion(new CellRangeAddress(headRow.getRowNum(), headRow.getRowNum() + 1, cell.getColumnIndex(), cell.getColumnIndex()));
                cell.setCellStyle(titleStyle);

                cell = headRow.createCell(8);
                cell.setCellValue("订单信息");
                xSheet.addMergedRegion(new CellRangeAddress(headRow.getRowNum(), headRow.getRowNum() + 1, cell.getColumnIndex(), cell.getColumnIndex()));
                cell.setCellStyle(titleStyle);

                cell = headRow.createCell(9);
                cell.setCellValue("订单原价");
                xSheet.addMergedRegion(new CellRangeAddress(headRow.getRowNum(), headRow.getRowNum() + 1, cell.getColumnIndex(), cell.getColumnIndex()));
                cell.setCellStyle(titleStyle);

                cell = headRow.createCell(10);
                cell.setCellValue("加费用");
                //merge
                xSheet.addMergedRegion(new CellRangeAddress(headRow.getRowNum(), headRow.getRowNum(), cell.getColumnIndex(), cell.getColumnIndex() + 1));
                cell.setCellStyle(titleStyle);

                cell = headRow.createCell(12);
                cell.setCellValue("是否可以清洗");
                xSheet.addMergedRegion(new CellRangeAddress(headRow.getRowNum(), headRow.getRowNum() + 1, cell.getColumnIndex(), cell.getColumnIndex()));
                cell.setCellStyle(titleStyle);

                cell = headRow.createCell(13);
                cell.setCellValue("备注");
                xSheet.addMergedRegion(new CellRangeAddress(headRow.getRowNum(), headRow.getRowNum() + 1, cell.getColumnIndex(), cell.getColumnIndex()));
                cell.setCellStyle(titleStyle);

                //sencond header
                cell = sencondHead.createCell(0);
                cell.setCellStyle(titleStyle);

                cell = sencondHead.createCell(1);
                cell.setCellStyle(titleStyle);

                cell = sencondHead.createCell(2);
                cell.setCellStyle(titleStyle);

                cell = sencondHead.createCell(3);
                cell.setCellStyle(titleStyle);
                cell = sencondHead.createCell(4);
                cell.setCellStyle(titleStyle);
                cell = sencondHead.createCell(5);
                cell.setCellStyle(titleStyle);
                cell = sencondHead.createCell(6);
                cell.setCellStyle(titleStyle);
                cell = sencondHead.createCell(7);
                cell.setCellStyle(titleStyle);
                cell = sencondHead.createCell(8);
                cell.setCellStyle(titleStyle);
                cell = sencondHead.createCell(9);
                cell.setCellStyle(titleStyle);

                cell = sencondHead.createCell(10);
                cell.setCellValue("远程");
                cell.setCellStyle(titleStyle);

                cell = sencondHead.createCell(11);
                cell.setCellValue("特殊");
                cell.setCellStyle(titleStyle);

                cell = sencondHead.createCell(12);
                cell.setCellStyle(titleStyle);

                cell = sencondHead.createCell(13);
                cell.setCellStyle(titleStyle);

                //xSheet.createFreezePane(0, rowNum); // 冻结单元格(x, y)

                for (int j = 0, length = reportEntries.size(); j < length; j++) {
                    entry = reportEntries.get(j);
                    dataRow = xSheet.createRow(rowNum++);
                    //dataRow.setHeightInPoints(12);

                    cell = dataRow.createCell(0);
                    cell.setCellStyle(dataStyle);
                    cell.setCellValue(j + 1);

                    cell = dataRow.createCell(1);
                    cell.setCellStyle(dataStyle);
                    cell.setCellValue(entry.getCreateDateString());

                    cell = dataRow.createCell(2);
                    cell.setCellStyle(dataStyle);
                    cell.setCellValue(entry.getAcceptor() == null ? "" : entry.getAcceptor().getName());

                    cell = dataRow.createCell(3);
                    cell.setCellStyle(dataStyle);
                    cell.setCellValue(entry.getCreateBy() == null ? "" : entry.getCreateBy().getName());

                    cell = dataRow.createCell(4);
                    cell.setCellValue(entry.getOrderNo());
                    cell.setCellStyle(dataStyle);

                    cell = dataRow.createCell(5);
                    cell.setCellValue(entry.getServiceTypes());
                    cell.setCellStyle(dataStyle);

                    cell = dataRow.createCell(6);
                    cell.setCellValue(entry.getStatus());
                    cell.setCellStyle(dataStyle);

                    cell = dataRow.createCell(7);
                    cell.setCellValue(new HSSFRichTextString(entry.getEngineer()));
                    cell.setCellStyle(dataStyle);

                    cell = dataRow.createCell(8);
                    cell.setCellValue(new HSSFRichTextString(entry.getOrderInfo()));
                    cell.setCellStyle(dataStyle);

                    cell = dataRow.createCell(9);
                    cell.setCellType(Cell.CELL_TYPE_NUMERIC);
                    cell.setCellValue(entry.getCharge());
                    cell.setCellStyle(dataStyle);

                    cell = dataRow.createCell(10);
                    cell.setCellStyle(dataStyle);
                    cell.setCellValue(entry.getTravelCharge());

                    cell = dataRow.createCell(11);
                    cell.setCellStyle(dataStyle);
                    cell.setCellValue(entry.getOtherCharge());

                    cell = dataRow.createCell(12);
                    cell.setCellStyle(dataStyle);

                    cell = dataRow.createCell(13);
                    cell.setCellStyle(dataStyle);
                }

                //CellRangeAddress region = new CellRangeAddress(0, rowNum-1, 0, 13);
                //RegionUtil.setBorderTop(CellStyle.BORDER_THIN,region, xSheet, xBook);
                //RegionUtil.setBorderBottom(CellStyle.BORDER_THIN,region, xSheet, xBook);
                //RegionUtil.setBorderLeft(CellStyle.BORDER_THIN,region, xSheet, xBook);
                //RegionUtil.setBorderRight(CellStyle.BORDER_THIN,region, xSheet, xBook);
                //
                //RegionUtil.setTopBorderColor(IndexedColors.BLACK.getIndex(), region, xSheet);
                //RegionUtil.setBottomBorderColor(IndexedColors.BLACK.getIndex(), region, xSheet);
                //RegionUtil.setLeftBorderColor(IndexedColors.BLACK.getIndex(), region, xSheet);
                //RegionUtil.setRightBorderColor(IndexedColors.BLACK.getIndex(), region, xSheet);

                //输出到磁盘中
                FileOutputStream fileOutputStream = new FileOutputStream(new File("/Users/yanshenglu/Desktop/tm/crush.xls"));
                xBook.write(fileOutputStream);
                xBook.close();
            }
            catch (Exception e) {
                e.printStackTrace();
            } finally {
                //xBook.dispose();
            }
            //endregion
        }
    }
    */

    @Data
    @NoArgsConstructor
    private class CrushReportEntry {
        private Date createDate;
        private String createDateString;
        private User acceptor;//接单人
        private User createBy;//发起人
        private String orderNo;
        private Long orderId;
        private String serviceTypes;
        private String status;
        //YH18648262203(网点编号) 二次操作失败(支付一次) 姓名: 金璐(主)
        //手机号: 18648262203
        private String engineer;
        //单号:K2018102759654  0234许俊茂(客户)
        //联系人:金璐  18648262203  内蒙古自治区 巴彦淖尔市 乌拉特前旗 明安镇金油坊九分子村邮编：015400  电话：18648262203 座机：
        //服务明细:  油烟机 1 需要安装
        //服务描述:
        private String orderInfo;
        private double charge;
        private double travelCharge = 0.00;// 远程费
        private double otherCharge = 0.00;// 特殊费
    }

    @Test
    public void testGradeQtyRptService(){
//        Date date = DateUtils.parseDate("2019-02-19");
//        gradeQtyRptService.sanaGradeQtyData(date);
    }

//    @Test
//    public void updateCustomerIdForEngineerCharge() {
//        Date beginDate = DateUtils.parseDate("2018-12-16");
//        Date endDate = DateUtils.parseDate("2019-1-1");
//        List<Date> dateList = Lists.newArrayList();
//        while (beginDate.getTime() < endDate.getTime()) {
//            dateList.add(beginDate);
//            beginDate = DateUtils.addDays(beginDate, 1);
//        }
//
//        Date sDate = null;
//        Date eDate = null;
//        for (Date date : dateList) {
//            sDate = DateUtils.getStartOfDay(date);
//            eDate = DateUtils.getEndOfDay(date);
//       //     customerIncomeAnalysisRptService.updateCustomerIdForEngineerCharge(sDate, eDate);
//            customerIncomeAnalysisRptService.updateCustomerIdForEngineerChargeOfNoCustomerId(sDate, eDate);
//            customerIncomeAnalysisRptService.updateCustomerIdForEngineerChargeMasterOfNoCustomerId(sDate, eDate);
//        }
//    }

//    @Test
//    public void updateCustomerIdForEngineerChargeOfNoCustomerId() {
//        Date beginDate = DateUtils.parseDate("2018-1-1");
//        Date endDate = DateUtils.parseDate("2019-4-22");
//        Date sDate = DateUtils.getStartOfDay(beginDate);
//        Date eDate = DateUtils.getEndOfDay(endDate);
//        customerIncomeAnalysisRptService.updateCustomerIdForEngineerChargeOfNoCustomerId(sDate, eDate);
//        customerIncomeAnalysisRptService.updateCustomerIdForEngineerChargeMasterOfNoCustomerId(sDate, eDate);
//    }

//    @Test
//    public void insertOrderProcess48HourData() {
//        Date beginDate = DateUtils.parseDate("2019-1-1");
//        Date endDate = DateUtils.parseDate("2019-4-27 1:00:00");
//        List<Date> dateList = Lists.newArrayList();
//        while (beginDate.getTime() < endDate.getTime()) {
//            dateList.add(beginDate);
//            beginDate = DateUtils.addDays(beginDate, 1);
//        }
//
//        Date sDate = null;
//        for (Date date : dateList) {
//            sDate = DateUtils.getStartOfDay(date);
//            orderProcess48hourRptService.writeCreateOrderFromYesterday(sDate);
//        }
//    }


    @Test
    public void updateOrderProcess48HourData() {
//        Date beginDate = DateUtils.parseDate("2019-7-1");
//        Date endDate = DateUtils.parseDate("2019-7-5 1:00:00");
//        List<Date> dateList = Lists.newArrayList();
//        while (beginDate.getTime() < endDate.getTime()) {
//            dateList.add(beginDate);
//            beginDate = DateUtils.addDays(beginDate, 1);
//        }
//
//        Date sDate = null;
//        for (Date date : dateList) {
//            sDate = DateUtils.getStartOfDay(date);
//            orderProcess48hourRptService.updateClosedOrder(sDate);
//            orderProcess48hourRptService.updateRptPlanInformation(sDate);
//            orderProcess48hourRptService.updateRptComplain(sDate);
//        }
    }


    @Test
    public void updateOrderProcess48HourDataBatch() {
//        Date beginDate = DateUtils.parseDate("2019-6-1");
//        Date endDate = DateUtils.parseDate("2019-7-30 1:00:00");
//        List<Date> dateList = Lists.newArrayList();
//        while (beginDate.getTime() < endDate.getTime()) {
//            dateList.add(beginDate);
//            beginDate = DateUtils.addDays(beginDate, 1);
//        }
//
//        for (Date date : dateList) {
//            orderProcess48hourRptService.updateRptRecordBatch(date);
//            orderProcess48hourRptService.updateRptDateSource(date);
//
//        }
    }

//    @Test
//    public void testProcessList(){
//        List<ProcessTimeListEntity> processTimeRpt = processTimeService.getProcessTimeRpt(2019, 1, null, null, null, null, 0L, null);
//        System.out.println(processTimeRpt);
//    }

    @Autowired
    protected Validator validator;

    /**
     * 服务端参数有效性验证
     *
     * @param object 验证的实体对象
     * @param groups 验证组
     * @return 验证成功：返回true；严重失败：将错误信息添加到 message 中
     */
    private boolean beanValidator(Object object, Class<?>... groups) {
        try {
            BeanValidators.validateWithException(validator, object, groups);
        } catch (ConstraintViolationException ex) {
            List<String> list = BeanValidators.extractPropertyAndMessageAsList(ex, ": ");
            list.add(0, "数据验证失败：");
            return false;
        }
        return true;
    }

    @Test
    public void test22() {
        OrderItem orderItem = new OrderItem();
        orderItem.setProductSpec("我是中国人我是中国人我是中国人我是中国人我是中国人我是中国人我是中国人我是中国人9");
        String t = StringUtils.left(orderItem.getProductSpec(), 40);
        beanValidator(orderItem);
    }

//    @Autowired
//    AreaOrderCompleteDayRptService areaOrderCompleteDayRptService;

    @Test
    public void update_rpt_order_complete_daily() {
//        Date beginDate = DateUtils.parseDate("2019-1-1 00:00:00");
//        Date endDate = DateUtils.getEndOfDay(DateUtils.parseDate("2019-7-30 23:59:59"));
//        List<Date> dateList = Lists.newArrayList();
//        while (beginDate.getTime() < endDate.getTime()) {
//            dateList.add(beginDate);
//            beginDate = DateUtils.addDays(beginDate, 1);
//        }
//
//        for (Date date : dateList) {
//            areaOrderCompleteDayRptService.QueryTheCompleteDataIntoTheIntermediateTable(date);
//        }
    }

//    /**
//     * 更新投诉统计报表中的customer_id
//     */
//    @Test
//    public void update_rpt_statistics_complain() {
//        Date beginDate = DateUtils.parseDate("2019-1-1 00:00:00");
//        Date endDate = DateUtils.getEndOfDay(DateUtils.parseDate("2019-7-30 23:59:59"));
//        List<Date> dateList = Lists.newArrayList();
//        while (beginDate.getTime() < endDate.getTime()) {
//            dateList.add(beginDate);
//            beginDate = DateUtils.addDays(beginDate, 1);
//        }
//
//        for (Date date : dateList) {
//            complainStatisticsRptService.updateRptCustomerId(date);
//        }
//    }
}
