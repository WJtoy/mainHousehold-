package com.wolfking.jeesite.ms.tmall.rpt.web;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.kkl.kklplus.entity.b2bcenter.md.B2BInterfaceIdEnum;
import com.kkl.kklplus.entity.b2bcenter.rpt.B2BOrderFailureLog;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BRetryOperationData;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.GsonUtils;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.service.ServicePointService;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.ms.tmall.rpt.entity.B2BRptSearchModel;
import com.wolfking.jeesite.ms.tmall.rpt.entity.B2BTmallJsonBean;
import com.wolfking.jeesite.ms.tmall.rpt.service.CanboOrderInfoRptService;
import com.wolfking.jeesite.ms.tmall.rpt.service.OrderInfoRptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping(value = "${adminPath}/tmall/rpt/tmallorder/")
public class TmallFailProcessLogController extends BaseController {

    @Autowired
    private OrderInfoRptService orderInfoRptService;

    @Autowired
    private CanboOrderInfoRptService canboOrderInfoRptService;

    @Autowired
    private ServicePointService servicePointService;

    public static final int EXECL_CELL_WIDTH_15 		= 15;
    public static final int EXECL_CELL_WIDTH_10 		= 10;
    public static final int EXECL_CELL_HEIGHT_TITLE 	= 30;
    public static final int EXECL_CELL_HEIGHT_HEADER 	= 20;
    public static final int EXECL_CELL_HEIGHT_DATA 		= 20;

    @ModelAttribute("processlogSearchModel")
    public B2BRptSearchModel get(@ModelAttribute("processlogSearchModel") B2BRptSearchModel processlogSearchModel) {
        if (processlogSearchModel == null) {
            processlogSearchModel = new B2BRptSearchModel();
        }
        Date now = new Date(); //默认使用当天作为查询条件

        if (processlogSearchModel.getEndCreateDt() == null) {
            processlogSearchModel.setEndDate(now);
        }
        if (processlogSearchModel.getBeginCreateDt() == null) {
            processlogSearchModel.setBeginDate(now);
        }
        if (processlogSearchModel.getActionType() == 0){
            processlogSearchModel.setActionType(50);
        }
        return processlogSearchModel;
    }

    /**
     * 天猫失败日志信息
     * @param processlogSearchModel
     * @param request
     * @param response
     * @param model
     * @return
     */
    @RequestMapping(value = "failLog")
    public String tmallFailLog(@ModelAttribute("processlogSearchModel") B2BRptSearchModel processlogSearchModel, HttpServletRequest request, HttpServletResponse response, Model model) {
        processlogSearchModel.setBeginCreateDt(DateUtils.getStartOfDay(processlogSearchModel.getBeginDate()).getTime());
        processlogSearchModel.setEndCreateDt(DateUtils.getEndOfDay(processlogSearchModel.getEndDate()).getTime());
        Page<B2BOrderFailureLog> tmallFailLogList = orderInfoRptService.getTmallFailLogList(new Page<B2BRptSearchModel>(request, response), processlogSearchModel);
        List<Dict> status = Lists.newArrayListWithCapacity(4);
        status.add(new Dict(10,"预约"));
        status.add(new Dict(30,"取消"));
        status.add(new Dict(40,"退单"));
        status.add(new Dict(50,"完工"));
        model.addAttribute("page",tmallFailLogList);
        model.addAttribute("status",status);
        model.addAttribute("processlogSearchModel",processlogSearchModel);
        return "modules/tmall/rpt/tmallFailLogReport";
    }

//    @RequiresPermissions("b2b:order:canboResend")
    @RequestMapping(value = "tmallRetryForm", method = RequestMethod.GET)
    public String tmallFailLogRetryForm(Long id, HttpServletRequest request, HttpServletResponse response, Model model) {
        String viewForm = "modules/tmall/rpt/tmallFailLogRetryForm";
        B2BOrderFailureLog processlog = orderInfoRptService.getLogById(id);
        B2BTmallJsonBean b2BTmallJsonBean = null;
        if (processlog!=null){
            b2BTmallJsonBean = new Gson().fromJson(processlog.getInfoJson(), B2BTmallJsonBean.class);
            b2BTmallJsonBean.setDataSource(2);
//            canboJsonBean.setB2bInterfaceId(b2bInterfaceId);
            b2BTmallJsonBean.setProcessLogId(processlog.getId());
            if (b2BTmallJsonBean.getCompleteDate()!=null){
                b2BTmallJsonBean.setCompleteDateD(DateUtils.longToDate(b2BTmallJsonBean.getCompleteDate()));
            }
            if (b2BTmallJsonBean.getServiceDate()!=null){
                b2BTmallJsonBean.setServiceDateD(DateUtils.longToDate(b2BTmallJsonBean.getServiceDate()));
            }
            if (b2BTmallJsonBean.getProcessUpdateDate()!=null){
                b2BTmallJsonBean.setProcessUpdateDateD(DateUtils.longToDate(b2BTmallJsonBean.getProcessUpdateDate()));
            }
            b2BTmallJsonBean.setB2bInterfaceId(B2BInterfaceIdEnum.getByCode(processlog.getInterfaceName()).id);
            b2BTmallJsonBean.setKklOrderId(processlog.getKklOrderId());
            b2BTmallJsonBean.setKklOrderNo(processlog.getKklOrderNo());
            b2BTmallJsonBean.setProcessComment(processlog.getProcessComment());
            String quarterFromOrderNo = orderInfoRptService.getQuarterFromOrderNo(processlog.getKklOrderNo());
            b2BTmallJsonBean.setQuarter(quarterFromOrderNo);
        }
        model.addAttribute("b2BTmallJsonBean",b2BTmallJsonBean);
        return viewForm;
    }

    /**
     *天猫工单失败日志 重试
     * @param
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "tmallFailLogRetryData", method = RequestMethod.POST)
    public AjaxJsonEntity tmallFailLogRetry(@RequestBody B2BTmallJsonBean tmallJsonBean) {
        if (tmallJsonBean!=null) {
            if (tmallJsonBean.getCompleteDateD()!=null){
                tmallJsonBean.setCompleteDate(tmallJsonBean.getCompleteDateD().getTime());
            }
            if (tmallJsonBean.getServiceDateD()!=null){
                tmallJsonBean.setServiceDate(tmallJsonBean.getServiceDateD().getTime());
            }
            String jsonString = GsonUtils.getInstance().toGson(tmallJsonBean);
            B2BRetryOperationData b2BRetryOperationData = new B2BRetryOperationData();
            b2BRetryOperationData.setB2bOrderNo(tmallJsonBean.getWorkcardId());
            b2BRetryOperationData.setDataSource(tmallJsonBean.getDataSource());
            b2BRetryOperationData.setB2bInterfaceId(tmallJsonBean.getB2bInterfaceId());
            b2BRetryOperationData.setProcessLogId(tmallJsonBean.getProcessLogId());
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
     * @param
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "tmallFailLogClose", method = RequestMethod.POST)
    public AjaxJsonEntity tmallFailLogCloseLog(@RequestBody B2BTmallJsonBean b2BTmallJsonBean) {
        if (b2BTmallJsonBean!=null) {
            B2BRetryOperationData b2BRetryOperationData = new B2BRetryOperationData();
            b2BRetryOperationData.setProcessLogId(b2BTmallJsonBean.getProcessLogId());
            return orderInfoRptService.closeLog(b2BRetryOperationData);
        }
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity();
        ajaxJsonEntity.setSuccess(false);
        ajaxJsonEntity.setMessage("数据NULL");
        return ajaxJsonEntity;
    }

}
