package com.wolfking.jeesite.modules.sys.web;

import com.wolfking.jeesite.common.config.Global;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.GsonUtils;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.service.GradeService;
import com.wolfking.jeesite.modules.md.service.ProductCategoryService;
import com.wolfking.jeesite.modules.md.service.ServicePointService;
import com.wolfking.jeesite.modules.md.service.ServiceTypeService;
import com.wolfking.jeesite.modules.mq.entity.OrderCreateBody;
import com.wolfking.jeesite.modules.mq.service.OrderCreateMessageService;
import com.wolfking.jeesite.modules.sd.entity.Order;
import com.wolfking.jeesite.modules.sd.entity.OrderStatus;
import com.wolfking.jeesite.modules.sd.entity.viewModel.CreateOrderModel;
import com.wolfking.jeesite.modules.sd.entity.viewModel.OrderGradeModel;
import com.wolfking.jeesite.modules.sd.entity.viewModel.OrderItemModel;
import com.wolfking.jeesite.modules.sd.service.OrderService;
import com.wolfking.jeesite.modules.sd.service.OrderTaskService;
import com.wolfking.jeesite.modules.sd.utils.OrderGradeModelAdapter;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.service.CacheManageService;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.service.sys.MSDictService;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created on 2017-05-05.
 */
@Controller
@RequestMapping(value = "${adminPath}/sys/cache")
public class CacheManageController extends BaseController {
    @Autowired
    private CacheManageService cacheManageService;

    @Autowired
    private ServiceTypeService serviceTypeService;

    @Autowired
    private GradeService gradeService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderCreateMessageService orderCreateMessageService;

    @Autowired
    private OrderTaskService orderTaskService;

    @Autowired
    private ProductCategoryService productCategoryService;

    @Autowired
    private ServicePointService servicePointService;


    /*@RequiresPermissions("sys:cache:view")*/
    @RequestMapping(value = {"list", ""})
    public String list(HttpServletRequest request, HttpServletResponse response, Model model) {

        List<HashMap<String, Object>> list = cacheManageService.queryCacheList();

        model.addAttribute("list", list);

        return "modules/sys/cacheManageList";
    }

    // ajax调用删除订单项目（更新缓存中标记,提交时才删除）,并返回所有列表（包括已删除）
    @ResponseBody
    @RequestMapping(value = "ajaxDelete")
    public AjaxJsonEntity ajaxDelete(@RequestParam String code, String id, HttpServletResponse response, HttpServletRequest request)
    {
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity result = new AjaxJsonEntity();
        User user = UserUtils.getUser();
        if (StringUtils.isBlank(code)) {
            result.setSuccess(false);
            result.setMessage("缓存类型无值");
            return result;
        }
        String key = new String();
        try
        {
            result.setSuccess(true);
            switch (code) {
                case "removeOrder":
                    if(StringUtils.isBlank(id)){
                        result.setSuccess(false);
                        result.setMessage("订单id无值");
                    }else if(id.length()>40){
                        result.setSuccess(false);
                        result.setMessage("订单id值过长");
                    }
                    else {
                        if(!orderService.removeOrderCache(id)){
                            result.setSuccess(false);
                            result.setMessage("删除订单缓存失败，或缓存不存在，请确认！");
                        }
                    }
                    break;
                case "updateToManualCharge":
                    orderTaskService.updateToManualCharge();
                    break;
                default:
                    break;
            }
        } catch (Exception e)
        {
            result.setSuccess(false);
            result.setMessage("删除缓存发生错误:" + e.getLocalizedMessage());
        }

        return result;
    }

    @ResponseBody
    @RequestMapping(value = "ajaxReload")
    public AjaxJsonEntity ajaxReload(@RequestParam String code, String id, HttpServletResponse response, HttpServletRequest request)
    {
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity result = new AjaxJsonEntity();
        User user = UserUtils.getUser();
        if (StringUtils.isBlank(code)) {
            result.setSuccess(false);
            result.setMessage("缓存类型无值");
            return result;
        }
        String key = new String();
        try
        {
            result.setSuccess(true);
            switch (code) {
                case "orderRepeateCache":
                    Date endDate = DateUtils.getEndOfDay(new Date());
                    Date beginDate = DateUtils.addMonth(endDate,-3);
                    beginDate = DateUtils.getDateStart(beginDate);
                    orderTaskService.reloadCheckRepeatOrderCache(beginDate,endDate);
                    break;
                case "productCategory":
                    //productCategoryService.reloadProductCategoryCache();
                    break;
                case "serviceType":
                    serviceTypeService.reloadServiceTypeCache();
                    break;
                case "noticeMessage":
                    Thread.sleep(3000);
                    orderService.reloadNoticeMessage();
                    break;
                case "grade":
                    reloadGrades();
                    break;
                case "servicePointAll":
                    //servicePointService.reloadAllServicePoint(); //mark on 2020-1-14  web端去servicePoint
                    break;
                case "servicePoint":
                    if (StringUtils.isNotBlank(id)) {
                        //servicePointService.reloadServicePoint(StringUtils.toLong(id));  //mark on 2020-1-14  web端去servicePoint
                    }
                    break;
                default:
                    break;
            }
        } catch (Exception e)
        {
            result.setSuccess(false);
            result.setMessage("删除缓存发生错误:" + e.getLocalizedMessage());
        }

        return result;
    }

    private void reloadGrades() {
        //delete
        //gradeService.delGradeCache();  // mark on 2020-1-7
        //load
        //gradeService.findAllListCache(); //mark on 2020-1-7
        orderService.getToOrderGrade();
    }

    /**
     * 测试多线程写日志(调用LogUtils)
     */
    @RequestMapping(value ="testSaveLogQueue")
    public String testSaveLogQueue(HttpServletRequest request, HttpServletResponse response, Model model) {
        /*
        int threadNum = 10;
        String str = request.getParameter("queue");
        if(StringUtils.isNoneBlank(str)){
            threadNum = Integer.parseInt(str);
        }


        CyclicBarrier cb = new CyclicBarrier(threadNum);

        int idx;
        ExecutorService es = Executors.newFixedThreadPool(threadNum);
        for (int i = 0; i < threadNum; i++) {
            es.execute(new SaveLogThread(cb, i));
        }
        es.shutdown();
        */
        return "ok";
    }

    //保存日志线程
    static class SaveLogThread implements Runnable {
        private CyclicBarrier cb;
        private int index;

        public SaveLogThread(CyclicBarrier cb,int index) {
            this.cb = cb;
            this.index = index;
        }
        @Override
        public void run() {
            // TODO Auto-generated method stub
            try {
                // 等待所有任务准备就绪
                cb.await();
                // 定义每个线程负责的业务逻辑实现
                LogUtils.saveLog("test", "", "", null, null, 1);
                System.out.println(String.format(">>> pass-%s",index));
                //Thread.sleep(100);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(String.format(">>> fail-%s",index));
            }
        }
    }

    @ResponseBody
    @RequestMapping(value = "tool-retrySaveGrade")
    public AjaxJsonEntity retrySaveGrade(@RequestParam String quarter, @RequestParam String orderId,HttpServletResponse response, HttpServletRequest request)
    {
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity result = new AjaxJsonEntity();
        if (StringUtils.isBlank(orderId) || StringUtils.isBlank(quarter)) {
            result.setSuccess(false);
            result.setMessage("参数错误");
            return result;
        }
        long id = StringUtils.toLong(orderId);
        if(id<=0){
            result.setSuccess(false);
            result.setMessage("参数类型错误");
            return result;
        }

        try
        {
            OrderCreateBody entry = orderCreateMessageService.getByOrderId(quarter,id);
            if(entry == null){
                result.setSuccess(false);
                result.setMessage("无待处理数据，清确认参数是否正确。");
                return result;
            }
            OrderGradeModel gradeModel = OrderGradeModelAdapter.getInstance().fromJson(entry.getJson());
            if(gradeModel == null){
                result.setSuccess(false);
                result.setMessage("json格式错误。");
                return result;
            }
            //jsong处理
            gradeModel.setContent(GsonUtils.MyCatJsonFormat(entry.getJson()));
            //读取orderStatus,用到reminderStaus，来决定是否调用微服务关闭催单 2019/08/15
            OrderStatus orderStatus = orderService.getOrderStatusById(gradeModel.getOrderId(),gradeModel.getQuarter(),false);
            Order order = new Order(gradeModel.getOrderId());
            order.setQuarter(gradeModel.getQuarter());
            order.setOrderStatus(orderStatus);
            gradeModel.setOrder(order);
            //end 2019/08/15
            orderService.saveGradeRecordAndServicePoint(gradeModel);
            entry.setStatus(30);
            entry.setRetryTimes(1);
            entry.setUpdateDate(new Date());
            entry.setRemarks("");
            orderCreateMessageService.update(entry);
            result.setSuccess(true);
            result.setMessage("处理成功");
        } catch (Exception e)
        {
            result.setSuccess(false);
            result.setMessage("处理过程中发生错误:" + e.getLocalizedMessage());
        }

        return result;
    }
}
