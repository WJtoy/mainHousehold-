package com.wolfking.jeesite.ms.canbo.rpt.web;


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
import com.wolfking.jeesite.ms.tmall.rpt.entity.B2BRptSearchModel;
import com.wolfking.jeesite.ms.tmall.rpt.service.CanboOrderInfoRptService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
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
public class CanboProcessLogController extends BaseController {

    @Autowired
    private CanboOrderInfoRptService orderInfoRptService;


    @ModelAttribute("canboSearchModel")
    public B2BRptSearchModel get(@ModelAttribute("canboSearchModel") B2BRptSearchModel canboSearchModel) {
        if (canboSearchModel == null) {
            canboSearchModel = new B2BRptSearchModel();
        }
        Date now = new Date(); //默认使用当天作为查询条件

        if (canboSearchModel.getBeginCreateDt() == null) {
            canboSearchModel.setBeginDate(now);
        }
        if (canboSearchModel.getEndCreateDt() == null) {
            canboSearchModel.setEndDate(now);
        }
        if (canboSearchModel.getProcessFlag() == null) {
            canboSearchModel.setProcessFlag(3);
        }
        if (canboSearchModel.getDataSource() == null) {
            canboSearchModel.setDataSource(3);
        }
        if (canboSearchModel.getB2bInterfaceId() == null) {
            canboSearchModel.setB2bInterfaceId(2004);
        }
        return canboSearchModel;
    }

    //同望数据监控
    @RequestMapping(value = "canboauston")
    public String canboProcessLogInfo(@ModelAttribute("canboSearchModel") B2BRptSearchModel canboSearchModel, HttpServletRequest request, HttpServletResponse response, Model model) {
        canboSearchModel.setBeginCreateDt(DateUtils.getStartOfDay(canboSearchModel.getBeginDate()).getTime());
        canboSearchModel.setEndCreateDt(DateUtils.getEndOfDay(canboSearchModel.getEndDate()).getTime());
        Page<B2BOrderProcesslog> page = orderInfoRptService.getList(new Page<B2BRptSearchModel>(request, response), canboSearchModel);

        List<Dict> b2bInterfaceId = Lists.newArrayListWithCapacity(3);
        b2bInterfaceId.add(new Dict(2006,"完成工单"));
        b2bInterfaceId.add(new Dict(2004,"工单预约"));
        b2bInterfaceId.add(new Dict(2005,"工单取消"));

        //第一次进入页面时，检查全局加急标记
        if(request.getMethod().equalsIgnoreCase("get")) {
            canboSearchModel.setB2bInterfaceId(2006);
        }

        model.addAttribute("b2bInterfaceId",b2bInterfaceId);
        model.addAttribute("page",page);
        model.addAttribute("canboSearchModel",canboSearchModel);
        return "modules/canbo/rpt/canboProcessLogReport";
    }

    /**
     * 同望B2B完成工单发送的状态信息
     * @param id
     * @param b2bInterfaceId
     * @param request
     * @param response
     * @param model
     * @return
     */
    @RequiresPermissions("b2b:order:canboResend")
    @RequestMapping(value = "canboResend", method = RequestMethod.GET)
    public String canboCompleteResend(Long id,int b2bInterfaceId, HttpServletRequest request, HttpServletResponse response, Model model) {
        String viewForm = "modules/canbo/rpt/canboResendForm";
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
     *康宝完成工单 重试
     * @param completedRetryBean
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "retryData", method = RequestMethod.POST)
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
     * 康宝完成工单 忽略
     * @param completedRetryBean
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "closeLog", method = RequestMethod.POST)
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
     * 工单取消 重发form
     * @param id
     * @param b2bInterfaceId
     * @param request
     * @param response
     * @param model
     * @return
     */
    @RequiresPermissions("b2b:order:canboResend")
    @RequestMapping(value = "canboCancel", method = RequestMethod.GET)
    public String canboCancelResend(Long id,int b2bInterfaceId, HttpServletRequest request, HttpServletResponse response, Model model) {
        String viewForm = "modules/canbo/rpt/canboCancelRetryForm";
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
     *康宝取消工单 重试
     * @param canboJsonBean
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "canboCancelRetryData", method = RequestMethod.POST)
    public AjaxJsonEntity canboCancelRetryData(@RequestBody CanboCancelJsonEntity canboJsonBean) {
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

    @ResponseBody
    @RequestMapping(value = "canboCancelCloseLog", method = RequestMethod.POST)
    public AjaxJsonEntity canboCancelCloseLog(CanboCancelJsonEntity canboJsonBean) {
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
     * 工单预约 重发 form
     * @param id
     * @param b2bInterfaceId
     * @param request
     * @param response
     * @param model
     * @return
     */
    @RequiresPermissions("b2b:order:canboResend")
    @RequestMapping(value = "canboAppointment", method = RequestMethod.GET)
    public String canboAppointmentResend(Long id,int b2bInterfaceId, HttpServletRequest request, HttpServletResponse response, Model model) {
        String viewForm = "modules/canbo/rpt/canboAppointmentRetryForm";
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
     *康宝预约工单 重试
     * @param canboJsonBean
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "canboAppointmentRetryData", method = RequestMethod.POST)
    public AjaxJsonEntity canboAppointmentRetryData(@RequestBody CanboAppointmentJsonEntity canboJsonBean) {
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
     *同望预约  忽略
     * @param canboJsonBean
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "canboAppointmentCloseLog", method = RequestMethod.POST)
    public AjaxJsonEntity canboAppointmentCloseLog(CanboAppointmentJsonEntity canboJsonBean) {
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
