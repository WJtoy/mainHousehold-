package com.wolfking.jeesite.modules.sd.web;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.es.mq.MQSyncServicePointMessage;
import com.kkl.kklplus.entity.es.mq.MQSyncServicePointStationMessage;
import com.kkl.kklplus.entity.es.mq.MQSyncType;
import com.kkl.kklplus.entity.push.AppMessageType;
import com.kkl.kklplus.entity.sys.SysSMSTypeEnum;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.exception.OrderException;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.utils.IdGen;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.entity.Engineer;
import com.wolfking.jeesite.modules.md.entity.ServicePoint;
import com.wolfking.jeesite.modules.md.entity.ServicePointStation;
import com.wolfking.jeesite.modules.md.service.ServicePointService;
import com.wolfking.jeesite.modules.mq.dto.MQPushMessage;
import com.wolfking.jeesite.modules.mq.sender.PushMessageSender;
import com.wolfking.jeesite.modules.mq.sender.ServicePointStationSender;
import com.wolfking.jeesite.modules.mq.service.OrderAutoPlanMessageService;
import com.wolfking.jeesite.modules.sd.entity.*;
import com.wolfking.jeesite.modules.sd.service.TestService;
import com.wolfking.jeesite.modules.sd.utils.OrderCacheUtils;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.servicepoint.ms.mq.SpSmsMQSender;
import com.wolfking.jeesite.modules.servicepoint.ms.sd.SpOrderCacheReadService;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.entity.AppPushMessage;
import com.wolfking.jeesite.ms.providermd.service.MSServicePointPriceService;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.shiro.authz.annotation.RequiresUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.wolfking.jeesite.modules.sd.utils.OrderUtils.ORDER_LOCK_EXPIRED;

/**
 * Created by Jeff on 2017/7/24.
 */
@Controller
@RequestMapping(value = "${adminPath}/sd/test")
@RequiresUser
@Slf4j(topic = "TestController")
public class TestController extends BaseController {

    @Autowired
    private TestService testService;


    @Autowired
    PushMessageSender pushMessageSender;

    @Autowired
    private ServicePointStationSender servicePointStationSender;

    @Autowired
    private ServicePointService servicePointService;

    @Autowired
    private MSServicePointPriceService msServicePointPriceService;

    @Autowired
    private OrderAutoPlanMessageService orderAutoPlanMessageService;

    @Autowired
    private SpOrderCacheReadService orderCacheReadService;

    @Value("${shortmessage.ignore-data-sources}")
    private String smIgnoreDataSources;

    @Autowired
    private SpSmsMQSender smsMQSender;

    @ResponseBody
    @RequestMapping(value = "soc")
    public AjaxJsonEntity selectOrderCondition(int pageIndex, int pageSize, HttpServletRequest request, HttpServletResponse response){
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        List<OrderCondition> ol = testService.findOrderConditionList(pageIndex, pageSize);
        result.setData(ol);
        return result;
    }

    @ResponseBody
    @RequestMapping(value = "socreadonly")
    public AjaxJsonEntity selectOrderConditionReadOnly(int pageIndex, int pageSize, HttpServletRequest request, HttpServletResponse response){
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        List<OrderCondition> ol = testService.findOrderConditionListReadOnly(pageIndex, pageSize);
        result.setData(ol);
        return result;
    }

    @ResponseBody
    @RequestMapping(value = "push")
    public AjaxJsonEntity push(int uid, int times, int mtype, HttpServletRequest request, HttpServletResponse response){
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        for (int i = 1; i <= times; i++) {
            MQPushMessage.PushMessage.Builder pushMessage = MQPushMessage.PushMessage.newBuilder();
            pushMessage.setUserId(uid);
            pushMessage.setPushMessageType(MQPushMessage.PushMessageType.Notification);
            pushMessage.setMessageType(mtype);
            pushMessage.setSubject("test subject");
            pushMessage.setDescription("test description");
            pushMessage.setContent("test content");
            pushMessage.setTimestamp(System.currentTimeMillis());
//            pushMessageSender.send(pushMessage.build());
//            try {
//                Thread.sleep(1500);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
        }
        result.setData("OK");
        return result;
    }

    @RequestMapping(value = {"testButtonDbClick"},method = RequestMethod.GET)
    public String testButtonDbClickGet(Model model,
                                             HttpServletRequest request, HttpServletResponse response) {
        model.addAttribute("id", IdGen.uuid());
        return "modules/test/testDbClickForm";
    }

    @RequestMapping(value = {"testButtonDbClick"},method = RequestMethod.POST)
    public String testButtonDbClick(Model model,
                                    HttpServletRequest request, HttpServletResponse response) {
        log.info("testButtonDbClick:post request");
        log.info("id:{}",request.getParameter("id"));
        try {
            Thread.sleep(1000l);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        model.addAttribute("id", IdGen.uuid());
        return "modules/test/testDbClickForm";
    }

    @ResponseBody
    @RequestMapping(value = "testButtonDbClickAjax")
    public AjaxJsonEntity testButtonDbClickAjax(String id,HttpServletRequest request, HttpServletResponse response){
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        log.info("id:{}",id);
        try {
            Thread.sleep(2000l);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        result.setSuccess(true);
        result.setData(IdGen.uuid());
        return result;
    }

    @RequestMapping(value = {"testOneLogback"},method = RequestMethod.POST)
    @ResponseBody
    public AjaxJsonEntity testOneLogback(HttpServletRequest request, HttpServletResponse response) {
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        //log.info("test info");
        //log.error("我是错误！我是错误！我是错误！我是错误！我是错误！我是错误！我是错误！我是错误！我是错误！我是错误！我是错误！");
        //LogUtils.saveLog(request,"测试异常");//ok
        //LogUtils.saveLog(request,null,new OrderException("测试异常"),"测试异常");//ok
        //LogUtils.saveLog(request,null,new OrderException("测试异常"),"测试异常","params");//ok
        //LogUtils.saveLog(request,null,new OrderException("测试异常"),"测试异常","params",new User(10000l));//ok
        LogUtils.saveLog(request,null,new OrderException("测试异常"),"测试异常","test","params",new User(10000l));//ok

        result.setMessage("ok");
        return result;
    }

    @RequestMapping(value = {"testLogback"},method = RequestMethod.GET)
    public String testLogback(Model model,
                                    HttpServletRequest request, HttpServletResponse response) {
        final int threadNum = 10;
        CyclicBarrier cb = new CyclicBarrier(threadNum);
        ExecutorService es = Executors.newFixedThreadPool(threadNum);
        for (int i = 0; i < threadNum; i++) {
            es.execute(new LogThread(cb));
        }

        es.shutdown();
        //log.trace("trace:{}",System.currentTimeMillis());
        //log.info("info:{}",System.currentTimeMillis());
        //log.debug("debug:{}",System.currentTimeMillis());
        //log.warn("warn:{}",System.currentTimeMillis());
        //log.error("error:{}",System.currentTimeMillis());
        model.addAttribute("id", IdGen.uuid());
        return "modules/test/testDbClickForm";
    }

    @RequestMapping(value = {"testLogOutput"},method = RequestMethod.GET)
    public String testLogOutput(Model model,
                              HttpServletRequest request, HttpServletResponse response) {
        log.debug("测试日志-DEBUG");
        log.info("测试日志-INFO");
        log.warn("测试日志-WARN");
        double divRst = 0.00;
        try {
            divRst = 12/0;
        }catch (Exception e) {
            log.error("测试错误输出",e);
        }

        model.addAttribute("id", IdGen.uuid());
        return "modules/test/testDbClickForm";
    }

    @ResponseBody
    @RequestMapping("/servicePoint")
    public ServicePoint getServicePoint(Long id,HttpServletRequest request){
        //long lid = StringUtils.toLong(id);
        ServicePoint servicePoint = servicePointService.getFromCache(id);
        return servicePoint;
    }

    @ResponseBody
    @RequestMapping("/getHeaders")
    public AjaxJsonEntity getRequestHeaders(Long id,HttpServletRequest request){
        StringBuffer str=new StringBuffer();
        //String kklToken = request.getHeader("kkl");
        //System.out.println("kkl:" + kklToken);
        //return AjaxJsonEntity.success("success",kklToken);
        Enumeration<String> headerNames = request.getHeaderNames();//获取所有元素名字
        while (headerNames.hasMoreElements()){//下一个元素存在
            String name=headerNames.nextElement();//获取当前元素
            str.append(name+":"+request.getHeader(name)+" | ");//通过当前元素得到具体内容
        }
        return AjaxJsonEntity.success("success",str.toString());

    }

    @ResponseBody
    @RequestMapping("/teststation/{autoPlanFlag}")
    public String testStation(@PathVariable Integer autoPlanFlag) {
        MQSyncServicePointMessage.SyncServicePointMessage syncServicePointMessage = MQSyncServicePointMessage.SyncServicePointMessage.newBuilder()
                .setMessageId(1146712723934740480L)
                .setServicePointId(10833)
                .setServicePointNo("CS18772732342")
                .setName("陈明彬")
                .setContactInfo1("18772732347")
                .setPaymentType(10)
                .setLevel(1)
                .setAutoPlanFlag(autoPlanFlag)
                .build();

        MQSyncServicePointStationMessage.SyncStationMessage stationMessage = MQSyncServicePointStationMessage.SyncStationMessage.newBuilder()
                .setStationId(225)
                .setAreaId(1607)
                .setSubAreaId(20085)
                .setStationName("张槎街道")
                .setAutoPlanFlag(autoPlanFlag)
                .build();

        MQSyncServicePointStationMessage.SyncServicePointStationMessage syncServicePointStationMessage =
                MQSyncServicePointStationMessage.SyncServicePointStationMessage.newBuilder()
                        .setMessageId(1146712723910561792L)
                        .setSyncType(MQSyncType.SyncType.UPDATE)
                        .setServicePointMessage(syncServicePointMessage)
                        .addStationMessage(stationMessage)
                        .build();

        servicePointStationSender.send(syncServicePointStationMessage);
        log.warn("Es-ServicePointStation:{}", syncServicePointStationMessage);

        return "OK!"+ autoPlanFlag;
    }


    //添加上门服务
    static class LogThread implements Runnable {
        private CyclicBarrier cb;

        public LogThread(CyclicBarrier cb) {
            this.cb = cb;
        }
        @Override
        public void run() {
            try {
                // 等待所有任务准备就绪
                cb.await();
                // 定义每个线程负责的业务逻辑实现
                Thread.sleep(100);
                Logger log = LoggerFactory.getLogger(getClass());
                for(int i=0;i<1000;i++) {
                    log.info("info - thread: {} ,content:{}", this,i);
                    log.debug("debug - thread: {} ,content:{}", this,i);
                    log.warn("warn - thread: {} ,content:{}", this,i);
                    log.error("error - thread: {} ,content:{}", this,i);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

//    @ResponseBody
//    @RequestMapping("/reloadSpPriceCache/{id}")
//    public MSResponse<Integer> reloadServicePointPriceCache(@PathVariable Long id) {
//        return msServicePointPriceService.reloadPointPriceWithCache(id);
//    }

      @ResponseBody
      @RequestMapping(value="/queryAutoPlan", produces = "text/plain; charset=utf-8")
      public String queryAutoPlan(@RequestParam("subAreaId") Long subAreaId, @RequestParam("categoryId") Long categoryId) {
        try {
            com.kkl.kklplus.entity.es.ServicePointStation servicePointStation = orderAutoPlanMessageService.getNearServicePointBySubAreaIdNew(subAreaId, categoryId);
            String stationStr = servicePointStation != null?ToStringBuilder.reflectionToString(servicePointStation, ToStringStyle.JSON_STYLE):"";
            return stationStr;
        } catch(Exception ex) {
            return String.format("查询失败.失败原因:%s",ex.getMessage());
        }
      }


    @ResponseBody
    @RequestMapping(value = "servicePointPlan", method = RequestMethod.POST)
    public AjaxJsonEntity servicePointPlan(Order order, HttpServletRequest request, HttpServletResponse response) {
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        if (order == null || order.getId() == null) {
            result.setSuccess(false);
            result.setMessage("派单时发生错误：订单号丢失");
            return result;
        }
        OrderCondition condition = order.getOrderCondition();
        if (condition == null || condition.getEngineer() == null || condition.getEngineer().getId() == null) {
            result.setSuccess(false);
            result.setMessage("未指派安维人员");
            return result;
        }

        try {

            if (order == null || order.getId() == null) {
                throw new OrderException("派单失败：参数无值。");
            }

            Long servicePointId = order.getOrderCondition().getServicePoint().getId();
            //新派师傅
            Long engineerId = order.getOrderCondition().getEngineer().getId();
            Engineer engineer = servicePointService.getEngineerFromCache(servicePointId, engineerId);
            if (engineer == null) {
                throw new OrderException(String.format("未找到安维:%s的信息", engineer.getName()));
            }
            Order o = null;
            try {
                o = orderCacheReadService.getOrderById(order.getId(), order.getQuarter(), OrderUtils.OrderDataLevel.CONDITION, true);
            } catch (Exception e) {
                log.error("[网点分配订单]读取订单缓存错误,orderId:{}", order.getId(), e);
            }

            if (o == null || o.getOrderCondition() == null) {
                throw new OrderException("确认订单信息错误。");
            }

            //已派师傅
            Long oldEngineerId = o.getOrderCondition().getEngineer().getId();

            String lockkey = String.format(RedisConstant.SD_ORDER_LOCK, order.getId());
            User user = new User(1L,"管理员","");
            try {
                Date date = new Date();
                OrderCondition rediscondition = o.getOrderCondition();
                User engineerUser = new User();
                engineerUser.setId(engineer.getId());
                engineerUser.setName(engineer.getName());
                engineerUser.setMobile(engineer.getContactInfo());//2017/09/21

                rediscondition.setEngineer(engineerUser);
                //rediscondition.setUpdateBy(user);
                rediscondition.setUpdateDate(date);
                // 短信通知
                // 发送用户短信
                List<String> ignoreDataSources = StringUtils.isBlank(smIgnoreDataSources) ? Lists.newArrayList() : Splitter.on(",").trimResults().splitToList(smIgnoreDataSources);
                if (ignoreDataSources.contains(o.getDataSource().getValue())) {
                    result.setSuccess(false);
                    result.setMessage("此订单属于不发送短信的数据源");
                    return result;
                }
                if (CollectionUtils.isEmpty(o.getItems())) {
                    log.error("[网点分配订单]发送派单短信错误：无订单项,orderId:{}", order.getId());
                }
                StringBuffer userContent = new StringBuffer();
                // 派单后给用户发送短信
                if (engineer.getAppFlag() == 0)// 无APP的师傅人工派单给用户短信
                {
                    // 2019-07-18
                    //您的优盟燃气热水器1台安装，罗师傅18962284455已接单,客服李小姐0757-29235638/4006663653
                    userContent.append("您的");
                    OrderItem item;
                    for (int i = 0, size = o.getItems().size(); i < size; i++) {
                        item = o.getItems().get(i);
                        userContent
                                .append(item.getBrand())
                                .append(com.wolfking.jeesite.common.utils.StringUtils.getStandardProductName(item.getProduct().getName()))
                                .append(item.getQty())
                                .append(item.getProduct().getSetFlag() == 0 ? "台" : "套")
                                .append(item.getServiceType().getName())
                                .append((i == (size - 1)) ? "" : " ");
                    }
                    userContent.append("，");
                    userContent.append(engineer.getName().substring(0, 1));
                    userContent.append("师傅").append(engineer.getContactInfo())
                            .append("已接单，");
                    if (rediscondition.getKefu() != null) {
                        userContent
                                .append("客服")
                                .append(rediscondition.getKefu().getName().substring(0, 1)).append("小姐")
                                .append(rediscondition.getKefu().getPhone())
                                .append("/");
                    }
                    userContent.append(MSDictUtils.getDictSingleValue("400ServicePhone", "4006663653"));
                    if (StringUtils.isBlank(rediscondition.getServicePhone())) {
                        log.error("[网点分配订单]发送派单短信错误：无用户服务电话,orderId:{},servicePhone:{}", order.getId(), rediscondition.getServicePhone());
                    }
                    // 使用新的短信发送方法 2019/02/28
                    //log.error("发送短信1,phone:{} ,msg:{}", rediscondition.getServicePhone(), userContent.toString());
                    smsMQSender.sendNew(rediscondition.getServicePhone(),
                            userContent.toString(),
                            "",
                            user.getId(),
                            date.getTime(),
                            SysSMSTypeEnum.ORDER_PLANNED_SERVICE_POINT
                    );
                } else {
                    userContent.append("您的");
                    OrderItem item;
                    for (int i = 0, size = o.getItems().size(); i < size; i++) {
                        item = o.getItems().get(i);
                        userContent
                                .append(item.getBrand())
                                .append(com.wolfking.jeesite.common.utils.StringUtils.getStandardProductName(item.getProduct().getName()))
                                .append(item.getQty())
                                .append(item.getProduct().getSetFlag() == 0 ? "台" : "套")
                                .append(item.getServiceType().getName())
                                .append((i == (size - 1)) ? "" : " ");
                    }
                    userContent.append("，");
                    userContent.append(engineer.getName().substring(0, 1));
                    userContent.append("师傅").append(engineer.getContactInfo()).append("已接单,");
                    if (rediscondition.getKefu() != null) {
                        userContent
                                .append("客服")
                                .append(rediscondition.getKefu().getName().substring(0, 1)).append("小姐")
                                .append(rediscondition.getKefu().getPhone())
                                .append("/");
                    }
                    userContent.append(MSDictUtils.getDictSingleValue("400ServicePhone", "4006663653"));
                    if (StringUtils.isBlank(rediscondition.getServicePhone())) {
                        log.error("[网点分配订单]发送派单短信错误：无用户服务电话,orderId:{},servicePhone:{}", order.getId(), rediscondition.getServicePhone());
                    }
                    // 使用新的短信发送方法 2019/02/28
                    //log.error("发送短信2,phone:{} ,msg:{}", rediscondition.getServicePhone(), userContent.toString());
                    smsMQSender.sendNew(rediscondition.getServicePhone(),
                            userContent.toString(),
                            "",
                            user.getId(),
                            date.getTime(),
                            SysSMSTypeEnum.ORDER_PLANNED_SERVICE_POINT
                    );
                }

            } catch (OrderException oe) {
                throw oe;
            } catch (Exception e) {
                log.error("[OrderService.servicePointPlanOrder] orderId:{} ,servicePointId:{} ,engineerId:{}", order.getId(), servicePointId, engineerId, e);
                throw new RuntimeException("网点派单错误:" + e.getMessage(), e);
            }
            result.setSuccess(true);
            result.setMessage("派单成功");
        } catch (OrderException oe) {
            result.setSuccess(false);
            result.setMessage(oe.getMessage());
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage(e.getMessage());
            log.error("[OrderController.servicePointPlan] orderId:{}", order.getId(), e);
        }
        return result;
    }

}
