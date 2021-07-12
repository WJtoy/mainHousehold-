package com.wolfking.jeesite.ms.jd.rpt.web;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.kkl.kklplus.entity.b2bcenter.rpt.B2BOrderProcesslog;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BRetryOperationData;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.GsonUtils;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.ms.canbo.rpt.entity.CanboAppointmentJsonEntity;
import com.wolfking.jeesite.ms.canbo.rpt.entity.CanboCancelJsonEntity;
import com.wolfking.jeesite.ms.canbo.rpt.entity.CompletedRetryBean;
import com.wolfking.jeesite.ms.jd.rpt.entity.JDSearchModel;
import com.wolfking.jeesite.ms.jd.rpt.service.JDOrderInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Controller
@RequestMapping(value = "${adminPath}/b2b/rpt/processlog/")
public class JDOrderInfoController extends BaseController {
    @Autowired
    private JDOrderInfoService orderInfoRptService;


    @ModelAttribute("JDSearchModel")
    public JDSearchModel get(@ModelAttribute("JDSearchModel") JDSearchModel jdSearchModel) {
        if (jdSearchModel == null) {
            jdSearchModel = new JDSearchModel();
        }
        Date now = new Date(); //默认使用当天作为查询条件

        if (jdSearchModel.getBeginCreateDt() == null) {
            jdSearchModel.setBeginDate(now);
        }
        if (jdSearchModel.getEndCreateDt() == null) {
            jdSearchModel.setEndDate(now);
        }
        if (jdSearchModel.getProcessFlag() == null) {
            jdSearchModel.setProcessFlag(2);
        }
        return jdSearchModel;
    }

    //工单
    @RequestMapping(value = "jdorder")
    public String JdOrderProcessLogInfo(JDSearchModel jdSearchModel, HttpServletRequest request, HttpServletResponse response, Model model) {
        jdSearchModel.setBeginCreateDt(DateUtils.getStartOfDay(jdSearchModel.getBeginDate()).getTime());
        jdSearchModel.setEndCreateDt(DateUtils.getEndOfDay(jdSearchModel.getEndDate()).getTime());
        Page<B2BOrderProcesslog> page = orderInfoRptService.getList(new Page<JDSearchModel>(request, response), jdSearchModel);
        List<Dict> b2bInterfaceId = Lists.newArrayListWithCapacity(4);
        b2bInterfaceId.add(new Dict(3001,"获取京东安维工单"));
        b2bInterfaceId.add(new Dict(3002,"京东安装状态回传(派工/预约)"));
        b2bInterfaceId.add(new Dict(3003,"京东安装反馈"));
        b2bInterfaceId.add(new Dict(3004,"京东取消安装"));
        model.addAttribute("b2bInterfaceId",b2bInterfaceId);
        model.addAttribute("page",page);
        model.addAttribute("jdSearchModel",jdSearchModel);
        return "modules/jd/rpt/jdOrderReport";
    }

    /**
     * 京东失败日志列表
     */
    @RequestMapping(value = "jdFailLog")
    public String jdFailLog(JDSearchModel jdSearchModel, HttpServletRequest request, HttpServletResponse response, Model model) {
        jdSearchModel.setBeginCreateDt(DateUtils.getStartOfDay(jdSearchModel.getBeginDate()).getTime());
        jdSearchModel.setEndCreateDt(DateUtils.getEndOfDay(jdSearchModel.getEndDate()).getTime());
        //第一次进入页面时，检查全局加急标记
        if(request.getMethod().equalsIgnoreCase("get")) {
            jdSearchModel.setB2bInterfaceId(2006);
            jdSearchModel.setProcessFlag(3);
            jdSearchModel.setDataSource(14);
        }
        Page<B2BOrderProcesslog> page = orderInfoRptService.getFailLogList(new Page<JDSearchModel>(request, response), jdSearchModel);

        List<Dict> b2bInterfaceId = Lists.newArrayListWithCapacity(3);
        b2bInterfaceId.add(new Dict(2006,"完成工单"));
        b2bInterfaceId.add(new Dict(2004,"工单预约"));
        b2bInterfaceId.add(new Dict(2005,"工单取消"));

        model.addAttribute("b2bInterfaceId",b2bInterfaceId);
        model.addAttribute("page",page);
        model.addAttribute("canboSearchModel",jdSearchModel);
        return "modules/jd/rpt/jdFailLogReport";
    }

    /**
     * 京东B2B完成工单 form
     */
    @RequestMapping(value = "jdFailLogRetryForm", method = RequestMethod.GET)
    public String jdFailLogRetryForm(Long id,int b2bInterfaceId, HttpServletRequest request, HttpServletResponse response, Model model) {
        String viewForm = "modules/jd/rpt/jdRetryForm";
        B2BOrderProcesslog processlog = orderInfoRptService.getLogById(id);

        CompletedRetryBean completedRetryBean = null;
        if (processlog!=null){
            completedRetryBean = new Gson().fromJson(processlog.getInfoJson(), CompletedRetryBean.class);
            completedRetryBean.setDataSource(processlog.getDataSource());
            completedRetryBean.setB2bInterfaceId(b2bInterfaceId);
            completedRetryBean.setProcessLogId(processlog.getId());
            completedRetryBean.setProcessComment(processlog.getProcessComment());
            HashMap<String, Object> orderInfoByCanbo = orderInfoRptService.getOrderInfoByCanbo(completedRetryBean.getDataSource(), completedRetryBean.getOrderNo());
            if (orderInfoByCanbo!=null &&orderInfoByCanbo.size()>0) {
                completedRetryBean.setOrderId(Long.valueOf(String.valueOf(orderInfoByCanbo.get("orderId"))));
                completedRetryBean.setKklOrderNo(String.valueOf(orderInfoByCanbo.get("kklOrderNo")));
                completedRetryBean.setQuarter(String.valueOf(orderInfoByCanbo.get("quarter")));
            }
        }
        model.addAttribute("completedRetryBean", completedRetryBean);
        return viewForm;
    }


    /**
     *京东完成工单 重发
     */
    @ResponseBody
    @RequestMapping(value = "jdCompletedRetry", method = RequestMethod.POST)
    public AjaxJsonEntity retryData(@RequestBody CompletedRetryBean completedRetryBean) {
        if (completedRetryBean !=null) {
            int length = completedRetryBean.getPic1s().size();
            List<CompletedRetryBean.Items> items = new ArrayList<>();
            for (int i = 0; i < length; i++) {
                CompletedRetryBean.Items item = new CompletedRetryBean.Items();
                item.setPic1(completedRetryBean.getPic1s().get(i));
                item.setPic2(completedRetryBean.getPic2s().get(i));
                item.setPic3(completedRetryBean.getPic3s().get(i));
                item.setPic4(completedRetryBean.getPic4s().get(i));
                item.setBarcode(completedRetryBean.getBarcodes().get(i));
                item.setItemCode(completedRetryBean.getItemCodes().get(i));
                item.setOutBarcode(completedRetryBean.getOutBarcodes().get(i));
                items.add(item);
            }
            completedRetryBean.setItems(items);
            String jsonString = GsonUtils.getInstance().toGson(completedRetryBean);
            B2BRetryOperationData b2BRetryOperationData = new B2BRetryOperationData();
            b2BRetryOperationData.setB2bOrderNo(completedRetryBean.getOrderNo());
            b2BRetryOperationData.setDataSource(completedRetryBean.getDataSource());
            b2BRetryOperationData.setB2bInterfaceId(completedRetryBean.getB2bInterfaceId());
            b2BRetryOperationData.setProcessLogId(completedRetryBean.getProcessLogId());
            b2BRetryOperationData.setDataJson(jsonString);
            return orderInfoRptService.retryData(b2BRetryOperationData);

        }
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity();
        ajaxJsonEntity.setSuccess(false);
        ajaxJsonEntity.setMessage("数据NULL");
        return ajaxJsonEntity;
    }

    /**
     * 京东完成工单 忽略
     */
    @ResponseBody
    @RequestMapping(value = "jdCompletedCloseLog", method = RequestMethod.POST)
    public AjaxJsonEntity closeLog(CompletedRetryBean completedRetryBean) {
        if (completedRetryBean !=null) {
            B2BRetryOperationData b2BRetryOperationData = new B2BRetryOperationData();
            b2BRetryOperationData.setProcessLogId(completedRetryBean.getProcessLogId());
            return orderInfoRptService.closeLog(b2BRetryOperationData);
        }
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity();
        ajaxJsonEntity.setSuccess(false);
        ajaxJsonEntity.setMessage("数据NULL");
        return ajaxJsonEntity;
    }

    /**
     * 京东工单取消 form
     */
    @RequestMapping(value = "jdCancelForm", method = RequestMethod.GET)
    public String jdCancelRetryForm(Long id,int b2bInterfaceId, HttpServletRequest request, HttpServletResponse response, Model model) {
        String viewForm = "modules/jd/rpt/jdCancelRetryForm";
        B2BOrderProcesslog processlog = orderInfoRptService.getLogById(id);
        CanboCancelJsonEntity entity  = null;
        if (processlog!=null){
            try {
                entity = GsonUtils.getInstance().fromJson(processlog.getInfoJson(), CanboCancelJsonEntity.class);
            }catch (Exception e){
                entity = new Gson().fromJson(processlog.getInfoJson(), CanboCancelJsonEntity.class);
            }
            entity.setDataSource(processlog.getDataSource());
            entity.setB2bInterfaceId(b2bInterfaceId);
            entity.setProcessLogId(processlog.getId());
            entity.setProcessComment(processlog.getProcessComment());
            HashMap<String, Object> orderInfoByCanbo = orderInfoRptService.getOrderInfoByCanbo(entity.getDataSource(), entity.getOrderNo());
            if (orderInfoByCanbo!=null &&orderInfoByCanbo.size()>0) {
                entity.setOrderId(Long.valueOf(String.valueOf(orderInfoByCanbo.get("orderId"))));
                entity.setKklOrderNo(String.valueOf(orderInfoByCanbo.get("kklOrderNo")));
                entity.setQuarter(String.valueOf(orderInfoByCanbo.get("quarter")));
            }
        }
        model.addAttribute("completedRetryBean",entity);
        return viewForm;
    }

    /**
     *京东取消工单 重发
     */
    @ResponseBody
    @RequestMapping(value = "jdCancelRetry", method = RequestMethod.POST)
    public AjaxJsonEntity jdCancelRetry(@RequestBody CanboCancelJsonEntity canboJsonBean) {
        if (canboJsonBean!=null) {
            String jsonString = GsonUtils.getInstance().toGson(canboJsonBean);
            B2BRetryOperationData b2BRetryOperationData = new B2BRetryOperationData();
            b2BRetryOperationData.setB2bOrderNo(canboJsonBean.getOrderNo());
            b2BRetryOperationData.setDataSource(canboJsonBean.getDataSource());
            b2BRetryOperationData.setB2bInterfaceId(canboJsonBean.getB2bInterfaceId());
            b2BRetryOperationData.setProcessLogId(canboJsonBean.getProcessLogId());
            b2BRetryOperationData.setDataJson(jsonString);
            return orderInfoRptService.retryData(b2BRetryOperationData);

        }
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity();
        ajaxJsonEntity.setSuccess(false);
        ajaxJsonEntity.setMessage("数据NULL");
        return ajaxJsonEntity;
    }

    /**
     * 京东取消工单 忽略
     * @param canboJsonBean
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "jdCancelCloseLog", method = RequestMethod.POST)
    public AjaxJsonEntity jdCancelCloseLog(CanboCancelJsonEntity canboJsonBean) {
        if (canboJsonBean!=null) {
            B2BRetryOperationData b2BRetryOperationData = new B2BRetryOperationData();
            b2BRetryOperationData.setProcessLogId(canboJsonBean.getProcessLogId());
            return orderInfoRptService.closeLog(b2BRetryOperationData);
        }
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity();
        ajaxJsonEntity.setSuccess(false);
        ajaxJsonEntity.setMessage("数据NULL");
        return ajaxJsonEntity;
    }

    /**
     * 京东工单预约 form
     * @param id
     * @param b2bInterfaceId
     * @param request
     * @param response
     * @param model
     * @return
     */
    @RequestMapping(value = "jdAppointmentForm", method = RequestMethod.GET)
    public String jdAppointmentForm(Long id,int b2bInterfaceId, HttpServletRequest request, HttpServletResponse response, Model model) {
        String viewForm = "modules/jd/rpt/jdAppointmentRetryForm";
        B2BOrderProcesslog processlog = orderInfoRptService.getLogById(id);
        CanboAppointmentJsonEntity entity  = null;
        if (processlog!=null){
            try {
                entity = GsonUtils.getInstance().fromJson(processlog.getInfoJson(), CanboAppointmentJsonEntity.class);
            }catch (Exception e){
                entity = new Gson().fromJson(processlog.getInfoJson(), CanboAppointmentJsonEntity.class);
            }
            entity.setDataSource(processlog.getDataSource());
            entity.setB2bInterfaceId(b2bInterfaceId);
            entity.setProcessLogId(processlog.getId());
            entity.setProcessComment(processlog.getProcessComment());
            HashMap<String, Object> orderInfoByCanbo = orderInfoRptService.getOrderInfoByCanbo(entity.getDataSource(), entity.getOrderNo());
            if (orderInfoByCanbo!=null &&orderInfoByCanbo.size()>0) {
                entity.setOrderId(Long.valueOf(String.valueOf(orderInfoByCanbo.get("orderId"))));
                entity.setKklOrderNo(String.valueOf(orderInfoByCanbo.get("kklOrderNo")));
                entity.setQuarter(String.valueOf(orderInfoByCanbo.get("quarter")));
            }
        }
        model.addAttribute("completedRetryBean",entity);
        return viewForm;
    }



    /**
     *京东预约工单 重发
     * @param canboJsonBean
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "jdAppointmentRetry", method = RequestMethod.POST)
    public AjaxJsonEntity jdAppointmentRetry(@RequestBody CanboAppointmentJsonEntity canboJsonBean) {
        if (canboJsonBean!=null) {
            String jsonString = GsonUtils.getInstance().toGson(canboJsonBean);
            B2BRetryOperationData b2BRetryOperationData = new B2BRetryOperationData();
            b2BRetryOperationData.setB2bOrderNo(canboJsonBean.getOrderNo());
            b2BRetryOperationData.setDataSource(canboJsonBean.getDataSource());
            b2BRetryOperationData.setB2bInterfaceId(canboJsonBean.getB2bInterfaceId());
            b2BRetryOperationData.setProcessLogId(canboJsonBean.getProcessLogId());
            b2BRetryOperationData.setDataJson(jsonString);
            return orderInfoRptService.retryData(b2BRetryOperationData);

        }
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity();
        ajaxJsonEntity.setSuccess(false);
        ajaxJsonEntity.setMessage("数据NULL");
        return ajaxJsonEntity;
    }

    /**
     *京东预约  忽略
     * @param canboJsonBean
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "jdAppointmentCloseLog", method = RequestMethod.POST)
    public AjaxJsonEntity jdAppointmentCloseLog(CanboAppointmentJsonEntity canboJsonBean) {
        if (canboJsonBean!=null) {
            B2BRetryOperationData b2BRetryOperationData = new B2BRetryOperationData();
            b2BRetryOperationData.setProcessLogId(canboJsonBean.getProcessLogId());
            return orderInfoRptService.closeLog(b2BRetryOperationData);
        }
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity();
        ajaxJsonEntity.setSuccess(false);
        ajaxJsonEntity.setMessage("数据NULL");
        return ajaxJsonEntity;
    }


}
