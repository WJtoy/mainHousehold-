package com.wolfking.jeesite.test.ms.tmall.sd;

import com.google.common.collect.Lists;
import com.googlecode.protobuf.format.JsonFormat;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2b.common.B2BTmallConstant;
import com.kkl.kklplus.entity.b2b.order.WorkcardSearchModel;
import com.kkl.kklplus.entity.b2b.pb.MQTmallPushWorkcardMessage;
import com.kkl.kklplus.entity.b2bcenter.md.B2BDataSourceEnum;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrder;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderSearchModel;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderTransferResult;
import com.kkl.kklplus.entity.common.MSPage;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.GsonUtils;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.entity.Engineer;
import com.wolfking.jeesite.modules.md.entity.ServicePoint;
import com.wolfking.jeesite.modules.mq.dto.MQCommon;
import com.wolfking.jeesite.modules.mq.dto.MQOrderGradeMessage;
import com.wolfking.jeesite.modules.mq.dto.MQOrderProcessLog;
import com.wolfking.jeesite.modules.mq.entity.OrderCreateBody;
import com.wolfking.jeesite.modules.mq.sender.OrderGradeMessageSender;
import com.wolfking.jeesite.modules.mq.service.OrderCreateMessageService;
import com.wolfking.jeesite.modules.sd.entity.OrderGrade;
import com.wolfking.jeesite.modules.sd.entity.OrderProcessLog;
import com.wolfking.jeesite.modules.sd.entity.mapper.OrderGradeMessageMapper;
import com.wolfking.jeesite.modules.sd.entity.mapper.OrderProcessLogMessageMapper;
import com.wolfking.jeesite.modules.sd.entity.viewModel.OrderGradeModel;
import com.wolfking.jeesite.modules.sd.utils.OrderGradeModelAdapter;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.ms.b2bcenter.sd.entity.B2BOrderSearchVModel;
import com.wolfking.jeesite.ms.b2bcenter.sd.entity.B2BOrderVModel;
import com.wolfking.jeesite.ms.b2bcenter.sd.mapper.B2BOrderMapper;
import com.wolfking.jeesite.ms.tmall.mq.sender.TMallWorkcardMQSender;
import com.wolfking.jeesite.ms.tmall.sd.entity.TMallWorkcardSearchVM;
import com.wolfking.jeesite.ms.tmall.sd.entity.WorkcardInfoModel;
import com.wolfking.jeesite.ms.tmall.sd.feign.WorkcardFeign;
import com.wolfking.jeesite.ms.tmall.sd.mapper.B2BOrderModelMapper;
//import com.wolfking.jeesite.ms.tmall.sd.mapper.WorkcardInfoMapper;
import com.wolfking.jeesite.ms.tmall.sd.service.TmallOrderService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.Date;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
//@RunWith(SpringRunner.class)
@Slf4j
@SpringBootTest
public class OrderTest {

    @Autowired
    private TMallWorkcardMQSender tMallWorkcardMQSender;

    @Autowired
    private TmallOrderService b2BOrderService;

    @Autowired
    private WorkcardFeign tmallFeign;

    @Autowired
    private OrderGradeMessageSender gradeMessageSender;

    @Autowired
    private OrderCreateMessageService orderCreateMessageService;

    //region bean convert

    /**
     * 测试bean转换
     */
    @Test
    public void testWorkcardMapper() {
        TMallWorkcardSearchVM vm = new TMallWorkcardSearchVM();
        Customer customer = new Customer(1L,"客户");
        customer.setShopId("123");
        //vm.setCustomer(customer);
        vm.setGmtCreateEnd(new Date());
        vm.setGmtCreateStart(DateUtils.getStartDayOfMonth(new Date()));
        vm.setDataSource(B2BTmallConstant.DATA_SOURCE);
        Page page = new Page();
        page.setPageSize(10);
        page.setPageNo(1);
        vm.setPage(page);

        /*
        WorkcardSearchModel search = Mappers.getMapper(WorkcardInfoMapper.class).toSearchModel(vm);
        Assert.assertFalse("转换失败:null",search == null);
        String json = GsonUtils.toGsonString(search);
        System.out.println(json);
        */
    }

    /**
     * 测试B2BOrder -> B2BOrderVModel
     */
    @Test
    public void testOrderToOrderVMmodel(){
        B2BOrder order =new B2BOrder();
        order.setId(1l);
        order.setOrderNo("12345");
        order.setParentBizOrderId("123456789");
        order.setShopId("shopid");
        B2BOrderVModel model = Mappers.getMapper(B2BOrderMapper.class).toB2BOrderVModel(order);
        System.out.println("workcard id:"+model.getOrderNo());
        System.out.println("parent id:"+model.getParentBizOrderId());
        System.out.println("shop id:"+model.getShopId());
    }

    /**
     * 测试pb message转换
     */
    @Test
    public void testMQMessagToWorkcardInfoModelMapper() throws IOException {
        MQTmallPushWorkcardMessage.TmallPushWorkcardMessage message = MQTmallPushWorkcardMessage.TmallPushWorkcardMessage.newBuilder()
                .setWorkcardId("56797416")
                .setSellerId(2449756547l)
                .setSellerShopId(2449756547l)
                .setShopName("setir森太厨宝专卖店")
                //buyer
                .setBuyerId(2929556009l)
                .setBuyerName("苏志崇")
                .setBuyerNick("t_1488693161144_055")
                //.setBuyerPhone() //无
                .setBuyerMobile("15007785058")
                .setBuyerZipCode("547000")
                .setBuyerAddress("广西壮族自治区 河池市 金城江区 金城江街道金城中路，东风社区，7组10号")
                //.setBuyerMail() //无
                //product
                .setAuctionId(44102013543l)
                .setAuctionName("【狂欢价】Setir/森太 CXW-268-B16油烟机侧吸式双电机自动清洗抽油烟机特价")
                .setBuyAmount(1)
                //.setServiceProduct("") //无
                .setModelNumber("CXW-268-B16")
                .setBrand("Setir/森太")
                .setCategoryId(350511l)
                .setCategory("350511")
                //service
                .setServiceCode("sendAndInstall")
                .setServiceName("sendAndInstall")
                .setServiceCount(1)
                //.setExpectDateNumber() //无
                //.setTaskMemo("") //无
                .setGmtCreate(1529297122000l)
                .setGmtModify(1529297122000l)
                .setBizOrderId(160118818577550960l)
                .setAcceptType(0)
                //cancel
                //.setCanceler
                //.setCancelDate()
                //.setCancelMemo()
                //receive
                //.setReceiveTimeNumber()
                .setTaskType(0)
                .setTaskStatus(-1)
                .setMemo("")
                .build();
        /*
        String json = new JsonFormat().printToString(message);
        log.info(json);
        MQTmallPushWorkcardMessage.TmallPushWorkcardMessage.Builder builder = MQTmallPushWorkcardMessage.TmallPushWorkcardMessage.newBuilder();
        new JsonFormat().merge(new ByteArrayInputStream(json.getBytes()),builder);
        message = builder.build();
        */
        /*
        WorkcardInfoModel model = Mappers.getMapper(WorkcardInfoMapper.class).mqMessagetoWorkcardInfoModel(message);
        log.info(GsonUtils.toGsonString(model));
        */
    }

    /**
     * 测试发送天猫订单推送消息
     */
    @Test
    public void testSendTmallPushWorkcardMessage(){
        MQTmallPushWorkcardMessage.TmallPushWorkcardMessage message = MQTmallPushWorkcardMessage.TmallPushWorkcardMessage.newBuilder()
                .setWorkcardId("56797416")
                .setSellerId(2449756547l)
                .setSellerShopId(2449756547l)
                .setShopName("setir森太厨宝专卖店")
                //buyer
                .setBuyerId(2929556009l)
                .setBuyerName("苏志崇")
                .setBuyerNick("t_1488693161144_055")
                //.setBuyerPhone() //无
                .setBuyerMobile("15007785058")
                .setBuyerZipCode("547000")
                .setBuyerAddress("广西壮族自治区 河池市 金城江区 金城江街道金城中路，东风社区，7组10号")
                //.setBuyerMail() //无
                //product
                .setAuctionId(44102013543l)
                .setAuctionName("【狂欢价】Setir/森太 CXW-268-B16油烟机侧吸式双电机自动清洗抽油烟机特价")
                .setBuyAmount(1)
                //.setServiceProduct("") //无
                .setModelNumber("CXW-268-B16")
                .setBrand("Setir/森太")
                .setCategoryId(350511l)
                .setCategory("350511")
                //service
                .setServiceCode("sendAndInstall")
                .setServiceName("sendAndInstall")
                .setServiceCount(1)
                //.setExpectDateNumber() //无
                //.setTaskMemo("") //无
                .setGmtCreate(1529297122000l)
                .setGmtModify(1529297122000l)
                .setBizOrderId(160118818577550960l)
                .setAcceptType(0)
                //cancel
                //.setCanceler
                //.setCancelDate()
                //.setCancelMemo()
                //receive
                //.setReceiveTimeNumber()
                .setTaskType(0)
                .setTaskStatus(-1)
                .setMemo("")
                .build();
        try {
            tMallWorkcardMQSender.send(message);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 测试B2B共用数据模型转换到天猫数据模型
     */
    @Test
    public void testB2BOrderModelToTMall() {
        B2BOrderVModel customerPo = new B2BOrderVModel();
        customerPo.setDataSource(B2BDataSourceEnum.TMALL.id);
        customerPo.setOrderNo("99999990");
        customerPo.setShopId("2449756547");
        customerPo.setUserName("卢工");
        customerPo.setUserMobile("13760468206");
        customerPo.setUserPhone("");
        customerPo.setUserAddress("广东省深圳市光明新区光明大地1号");
        customerPo.setBrand("Setir/森太");
        customerPo.setServiceType("sendAndInstall");
        customerPo.setDescription("备注-自动清洗抽油烟机特价智能侧吸式抽烟机");
        customerPo.setStatus(-1);
        customerPo.setProcessFlag(0);
        customerPo.setProcessTime(0);
        customerPo.setProcessComment("测试");
        customerPo.setQuarter("20183");
        B2BOrder.B2BOrderItem item = new B2BOrder.B2BOrderItem();
        item.setProductCode("350511");
        item.setQty(2);
        item.setProductName("自动清洗抽油烟机特价智能侧吸式抽烟机");
        item.setProductSpec("CXW-268-B960");
        customerPo.getItems().add(item);

        B2BOrder.B2BOrderItem item2 = new B2BOrder.B2BOrderItem();
        item2.setProductCode("50015382");
        item2.setQty(1);
        item2.setProductName("燃气灶");
        item2.setProductSpec("T258A");
        customerPo.getItems().add(item2);

        customerPo.setCustomer(new Customer(1l, "测试客户"));
        /*
        WorkcardInfoModel workcard = Mappers.getMapper(B2BOrderModelMapper.class).toTMallModel(customerPo);
        Assert.assertFalse("转换失败:null",workcard == null);
        String json = GsonUtils.toGsonString(workcard);
        System.out.println(json);
        */
        List<B2BOrderVModel> list = Lists.newArrayList(customerPo);
        List<WorkcardInfoModel> dlist = Mappers.getMapper(B2BOrderModelMapper.class).listToTMallModel(list);
        Assert.assertFalse("转换失败:null", dlist == null || dlist.size()==0);
        String json = GsonUtils.toGsonString(dlist);
        System.out.println(json);
    }

    //endregion bean convert

    //region 测试天猫微服务

    /**
     * 测试检查订单转换状态
     */
    @Test
    public void testCheckB2BOrderProcessFlag() {
        try {
            List<B2BOrderTransferResult> b2bOrders = Lists.newArrayList();
            B2BOrderTransferResult transferResult = new B2BOrderTransferResult(B2BDataSourceEnum.TMALL.id, "99999001");
            b2bOrders.add(transferResult);
            MSResponse msResponse = tmallFeign.checkWorkcardProcessFlag(b2bOrders);
            Assert.assertFalse("调用B2B失败:返回null",msResponse == null);
            if(msResponse != null) {
                System.out.println("code:" + msResponse.getCode());
                System.out.println("msg:" + msResponse.getMsg());
            }
            //log.info("code:{} ,msg:{}", msResponse.getCode(), msResponse.getMsg());
        }catch (Exception e){
            System.out.println("Exception:");
            e.printStackTrace();
        }
    }

    /**
     * 测试获取待接单订单列表
     */
    @Test
    public void testGetOrderList() {
        try {
            B2BOrderSearchVModel orderSearchModel = new B2BOrderSearchVModel();
            Date now = new Date();
            orderSearchModel.setBeginCreateDate(DateUtils.addMonth(now, -1));
            orderSearchModel.setEndCreateDate(DateUtils.getEndOfDay(now));
            orderSearchModel.setBeginCreateDt(orderSearchModel.getBeginCreateDate().getTime());
            orderSearchModel.setEndCreateDt(orderSearchModel.getEndCreateDate().getTime());
            orderSearchModel.setDataSource(B2BDataSourceEnum.TMALL.id);
            orderSearchModel.setProcessFlags(org.assertj.core.util.Lists.newArrayList(0,2,3));
            MSPage<B2BOrderSearchModel> msPage = new MSPage<>(1, 10);
            orderSearchModel.setPage(msPage);
            MSResponse<MSPage<B2BOrder>> msResponse = tmallFeign.getListOrder(orderSearchModel);
            Assert.assertFalse("调用B2B失败:返回null",msResponse == null);
            if(msResponse != null) {
                System.out.println("code:" + msResponse.getCode());
                System.out.println("msg:" + msResponse.getMsg());
                if(msResponse.getData() != null && msResponse.getData().getList() != null) {
                    System.out.println("list size:" + msResponse.getData().getList().size());
                }
            }
            //log.info("code:{} ,msg:{}", msResponse.getCode(), msResponse.getMsg());
        }catch (Exception e){
            System.out.println("Exception:");
            e.printStackTrace();
        }
    }

    //endregion 测试天猫微服务

    //region 客评

    @Test
    public void testOrderProcessLogToPb(){
        MQOrderProcessLog.OrderProcessLog message = MQOrderProcessLog.OrderProcessLog.newBuilder()
                .setId(1049584876891803649l)
                .setOrderId(1025283714474708992l)
                .setQuarter("20183")
                .setAction("客评")
                .setActionComment("客评,操作人:系统管理员")
                .setStatus("完成")
                .setStatusValue(80)
                .setStatusFlag(1)
                .setCloseFlag(0)
                .setCreateBy(
                        MQCommon.User.newBuilder()
                                .setId(1)
                                .setName("系统管理员")
                                .build()
                )
                .setCreateDate(1539157724000l)
                .setRemarks("")
                .build();
        OrderProcessLog processLog = Mappers.getMapper(OrderProcessLogMessageMapper.class)
                .messageToModel(message);
        Assert.assertEquals("不相等",Long.valueOf(1049584876891803649l).longValue(),processLog.getId().longValue());
        log.warn(GsonUtils.getInstance().toGson(processLog));

    }

    /**
     * 测试pb message转换
     */
    @Test
    public void testGradeMQToModel() throws IOException {
        MQOrderGradeMessage.OrderGradeMessage message = MQOrderGradeMessage.OrderGradeMessage.newBuilder()
                .setOrderId(1025283714474708992l)
                .setOrderNo("K2018080306998")
                .setQuarter("20183")
                .setAutoGradeFlag(0)
                .setPoint(100)
                .setServicePoint(
                        MQCommon.User.newBuilder()
                                .setId(4991l)
                                .setName("快可立全国联保")
                                .build()
                )
                .setEngineer(
                        MQCommon.User.newBuilder()
                                .setId(5143l)
                                .setName("快可立")
                                .build()
                )
                .setTimeLiness(10.0)
                .setRushCloseFlag(1)
                .setCreateBy(
                        MQCommon.User.newBuilder()
                                .setId(1l)
                                .setName("系统管理员")
                                .build()
                )
                .setCreateDate(1539157724000l)
                .setProcessLog(MQOrderProcessLog.OrderProcessLog.newBuilder()
                        .setId(1049584876891803649l)
                        .setOrderId(1025283714474708992l)
                        .setQuarter("20183")
                        .setAction("客评")
                        .setActionComment("客评,操作人:系统管理员")
                        .setStatus("完成")
                        .setStatusValue(80)
                        .setStatusFlag(1)
                        .setCloseFlag(0)
                        .setCreateBy(
                                MQCommon.User.newBuilder()
                                .setId(1)
                                .setName("系统管理员")
                                .build()
                        )
                        .setCreateDate(1539157724000l)
                        .setRemarks("")
                        .build()
                )
                .addItems(
                        MQOrderGradeMessage.GradeItemMessage.newBuilder()
                        .setGradeId(1l)
                        .setGradeName("时效性")
                        .setGradeItemId(10001l)
                        .setGradeItemName("24小时以内")
                        .setSort(1)
                        .setPoint(30)
                        .build()
                )
                .addItems(
                        MQOrderGradeMessage.GradeItemMessage.newBuilder()
                                .setGradeId(2l)
                                .setGradeName("服务态度")
                                .setGradeItemId(10006l)
                                .setGradeItemName("非常好")
                                .setSort(2)
                                .setPoint(30)
                                .build()
                )
                .addItems(
                        MQOrderGradeMessage.GradeItemMessage.newBuilder()
                                .setGradeId(3l)
                                .setGradeName("技术水平")
                                .setGradeItemId(10009l)
                                .setGradeItemName("专业")
                                .setSort(3)
                                .setPoint(20)
                                .build()
                )
                .addItems(
                        MQOrderGradeMessage.GradeItemMessage.newBuilder()
                                .setGradeId(4l)
                                .setGradeName("收费")
                                .setGradeItemId(10012l)
                                .setGradeItemName("无额外收费、有收费但属于正常标准收费")
                                .setSort(4)
                                .setPoint(20)
                                .build()
                )
                .build();
        OrderGradeModel model = Mappers.getMapper(OrderGradeMessageMapper.class).mqToModel(message);
        //log.info(GsonUtils.toGsonString(model));
        //log.info(OrderGradeModelAdapter.getInstance().toJson(model));
        String json = OrderGradeModelAdapter.getInstance().toJson(model);
        //System.out.println("json:" + json);
        log.warn(json);
    }

    /**
     * 测试pb message转换
     */
    @Test
    public void testModelToMQ() throws IOException {
        OrderGradeModel model = new OrderGradeModel();
        model.setOrderId(1025283714474708992l);
        model.setOrderNo("K2018080306998");
        model.setQuarter("20183");
        model.setAutoGradeFlag(0);
        model.setPoint(100);
        model.setTimeLiness(10.0);
        model.setRushCloseFlag(1);
        ServicePoint servicePoint = new ServicePoint(4991l);
        servicePoint.setName("快可立全国联保");
        model.setServicePoint(servicePoint);
        Engineer engineer = new Engineer(5143l);
        engineer.setName("快可立");
        model.setEngineer(engineer);
        User createBy = new User(1l,"系统管理员","");
        model.setCreateBy(createBy);
        model.setCreateDate(DateUtils.longToDate(1539157724000l));

        List<OrderGrade> items = Lists.newArrayList();

        OrderGrade item = new OrderGrade();
        item.setGradeId(1l);
        item.setGradeName("时效性");
        item.setGradeItemId(10001l);
        item.setGradeItemName("24小时以内");
        item.setSort(1);
        item.setPoint(30);
        items.add(item);

        item = new OrderGrade();
        item.setGradeId(2l);
        item.setGradeName("服务态度");
        item.setGradeItemId(10006l);
        item.setGradeItemName("非常好");
        item.setSort(2);
        item.setPoint(30);
        items.add(item);

        item = new OrderGrade();
        item.setGradeId(3l);
        item.setGradeName("技术水平");
        item.setGradeItemId(10009l);
        item.setGradeItemName("专业");
        item.setSort(3);
        item.setPoint(20);
        items.add(item);

        item = new OrderGrade();
        item.setGradeId(4l);
        item.setGradeName("收费");
        item.setGradeItemId(10012l);
        item.setGradeItemName("无额外收费、有收费但属于正常标准收费");
        item.setSort(4);
        item.setPoint(20);
        items.add(item);

        model.setGradeList(items);

        OrderProcessLog processLog = new OrderProcessLog();
        processLog.setId(1049584876891803649l);
        processLog.setOrderId(1025283714474708992l);
        processLog.setQuarter("20183");
        processLog.setAction("客评");
        processLog.setActionComment("客评,操作人:系统管理员");
        processLog.setStatus("完成");
        processLog.setStatusValue(80);
        processLog.setStatusFlag(1);
        processLog.setCloseFlag(0);
        processLog.setCreateBy(createBy);
        processLog.setCreateDate(model.getCreateDate());
        processLog.setRemarks("");
        model.setProcessLog(processLog);

        MQOrderGradeMessage.OrderGradeMessage message = Mappers.getMapper(OrderGradeMessageMapper.class).modelToMq(model);
        log.warn(new JsonFormat().printToString(message));
    }

    /**
     * 测试发送客评消息
     */
    @Test
    public void testSendOrderGradeMessage(){
        MQOrderGradeMessage.OrderGradeMessage message = MQOrderGradeMessage.OrderGradeMessage.newBuilder()
                .setId(1050322658585088001l)
                .setOrderId(1025283714474708992l)
                .setOrderNo("K2018080306998")
                .setQuarter("20183")
                .setAutoGradeFlag(0)
                .setPoint(100)
                .setServicePoint(
                        MQCommon.User.newBuilder()
                                .setId(4991l)
                                .setName("快可立全国联保")
                                .build()
                )
                .setEngineer(
                        MQCommon.User.newBuilder()
                                .setId(5143l)
                                .setName("快可立")
                                .build()
                )
                .setTimeLiness(10.0)
                .setRushCloseFlag(1)
                .setCreateBy(
                        MQCommon.User.newBuilder()
                                .setId(1l)
                                .setName("系统管理员")
                                .build()
                )
                .setCreateDate(1539157724000l)
                .addItems(
                        MQOrderGradeMessage.GradeItemMessage.newBuilder()
                                .setGradeId(1l)
                                .setGradeName("时效性")
                                .setGradeItemId(10001l)
                                .setGradeItemName("24小时以内")
                                .setSort(1)
                                .setPoint(30)
                                .build()
                )
                .addItems(
                        MQOrderGradeMessage.GradeItemMessage.newBuilder()
                                .setGradeId(2l)
                                .setGradeName("服务态度")
                                .setGradeItemId(10006l)
                                .setGradeItemName("非常好")
                                .setSort(2)
                                .setPoint(30)
                                .build()
                )
                .addItems(
                        MQOrderGradeMessage.GradeItemMessage.newBuilder()
                                .setGradeId(3l)
                                .setGradeName("技术水平")
                                .setGradeItemId(10009l)
                                .setGradeItemName("专业")
                                .setSort(3)
                                .setPoint(20)
                                .build()
                )
                .addItems(
                        MQOrderGradeMessage.GradeItemMessage.newBuilder()
                                .setGradeId(4l)
                                .setGradeName("收费")
                                .setGradeItemId(10012l)
                                .setGradeItemName("无额外收费、有收费但属于正常标准收费")
                                .setSort(4)
                                .setPoint(20)
                                .build()
                )
                .setProcessLog(MQOrderProcessLog.OrderProcessLog.newBuilder()
                        .setId(1049584876891803649l)
                        .setOrderId(1025283714474708992l)
                        .setQuarter("20183")
                        .setAction("客评")
                        .setActionComment("客评,操作人:系统管理员")
                        .setStatus("完成")
                        .setStatusValue(80)
                        .setStatusFlag(1)
                        .setCloseFlag(0)
                        .setCreateBy(
                                MQCommon.User.newBuilder()
                                        .setId(1)
                                        .setName("系统管理员")
                                        .build()
                        )
                        .setCreateDate(1539157724000l)
                        .setRemarks("")
                        .build()
                )
                .build();
        //send
        gradeMessageSender.sendRetry(message,10*1000,0);//retry
        //gradeMessageSender.send(message);
    }

    /**
     * 手工处理客评记录保存错误
     */
    @Test
    public void manualActionSaveGradeError(){
        List<OrderCreateBody> list =  orderCreateMessageService.getResendList(1,2);
        String json = new String();
        for(OrderCreateBody entry:list){
            json = entry.getJson();
            System.out.println("json:" + json);
        }
    }

    //endregion 客评
}
