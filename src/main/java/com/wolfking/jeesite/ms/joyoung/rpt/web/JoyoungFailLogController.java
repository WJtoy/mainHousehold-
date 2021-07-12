package com.wolfking.jeesite.ms.joyoung.rpt.web;

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
import com.wolfking.jeesite.ms.joyoung.rpt.entity.JoyoungSearchModel;
import com.wolfking.jeesite.ms.joyoung.rpt.service.JoyoungFailLogService;
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
public class JoyoungFailLogController extends BaseController {
    @Autowired
    private JoyoungFailLogService orderInfoRptService;


    @ModelAttribute("JoyoungSearchModel")
    public JoyoungSearchModel get(@ModelAttribute("JoyoungSearchModel") JoyoungSearchModel joyoungSearchModel) {
        if (joyoungSearchModel == null) {
            joyoungSearchModel = new JoyoungSearchModel();
        }
        Date now = new Date(); //默认使用当天作为查询条件

        if (joyoungSearchModel.getBeginCreateDt() == null) {
            joyoungSearchModel.setBeginDate(now);
        }
        if (joyoungSearchModel.getEndCreateDt() == null) {
            joyoungSearchModel.setEndDate(now);
        }
//        if (joyoungSearchModel.getProcessFlag() == null) {
//            joyoungSearchModel.setProcessFlag(2);
//        }
        return joyoungSearchModel;
    }


    /**
     * 九阳失败日志列表
     */
    @RequestMapping(value = "joyoungFailLog")
    public String jdFailLog(@ModelAttribute("JoyoungSearchModel") JoyoungSearchModel searchModel, HttpServletRequest request, HttpServletResponse response, Model model) {
        searchModel.setBeginCreateDt(DateUtils.getStartOfDay(searchModel.getBeginDate()).getTime());
        searchModel.setEndCreateDt(DateUtils.getEndOfDay(searchModel.getEndDate()).getTime());
        //第一次进入页面时，检查全局加急标记
        if(request.getMethod().equalsIgnoreCase("get")) {
            searchModel.setB2bInterfaceId(5007);
//            searchModel.setProcessFlag(3);
//            searchModel.setDataSource(14);
        }
        Page<B2BOrderProcesslog> page = orderInfoRptService.getFailLogList(new Page<JDSearchModel>(request, response), searchModel);

        List<Dict> b2bInterfaceId = Lists.newArrayListWithCapacity(2);
        b2bInterfaceId.add(new Dict(5007,"完成工单"));
        b2bInterfaceId.add(new Dict(5006,"工单取消"));

        model.addAttribute("b2bInterfaceId",b2bInterfaceId);
        model.addAttribute("page",page);
        model.addAttribute("searchModel",searchModel);
        return "modules/joyoung/rpt/joyoungFailLogReport";
    }

    /**
     * 九阳B2B完成工单 form
     */
    @RequestMapping(value = "joyoungFailLogRetryForm", method = RequestMethod.GET)
    public String jdFailLogRetryForm(Long id,int b2bInterfaceId, HttpServletRequest request, HttpServletResponse response, Model model) {
        String viewForm = "modules/joyoung/rpt/joyoungCompletedRetryForm";
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
     *九阳完成工单 重发
     */
    @ResponseBody
    @RequestMapping(value = "joyoungCompletedRetry", method = RequestMethod.POST)
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
     * 九阳完成工单 忽略
     */
    @ResponseBody
    @RequestMapping(value = "joyoungCompletedCloseLog", method = RequestMethod.POST)
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
     * 九阳工单取消 form
     */
    @RequestMapping(value = "joyoungCancelForm", method = RequestMethod.GET)
    public String joyoungCancelRetryForm(Long id,int b2bInterfaceId, HttpServletRequest request, HttpServletResponse response, Model model) {
        String viewForm = "modules/joyoung/rpt/joyoungCancelRetryForm";
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
    @RequestMapping(value = "joyoungCancelRetry", method = RequestMethod.POST)
    public AjaxJsonEntity joyoungCancelRetry(@RequestBody CanboCancelJsonEntity canboJsonBean) {
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
     * 九阳取消工单 忽略
     * @param canboJsonBean
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "joyoungCancelCloseLog", method = RequestMethod.POST)
    public AjaxJsonEntity joyoungCancelCloseLog(CanboCancelJsonEntity canboJsonBean) {
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
