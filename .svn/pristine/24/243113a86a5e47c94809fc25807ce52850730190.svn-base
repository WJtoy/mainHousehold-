package com.wolfking.jeesite.ms.viomi.rpt.web;


import com.kkl.kklplus.entity.viomi.sd.VioMiApiLog;
import com.kkl.kklplus.entity.viomi.sd.VioMiExceptionOrder;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;

import com.wolfking.jeesite.ms.viomi.rpt.entity.ViomiFailLogSearchModel;
import com.wolfking.jeesite.ms.viomi.rpt.service.ViomiFailLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping(value = "${adminPath}/b2b/rpt/processlog/")
public class ViomiFailLogController extends BaseController {

    @Autowired
    private ViomiFailLogService viomiFailLogService;


    @ModelAttribute("ViomiSearchModel")
    public ViomiFailLogSearchModel get(@ModelAttribute("ViomiSearchModel") ViomiFailLogSearchModel model) {
        if (model == null) {
            model = new ViomiFailLogSearchModel();
        }
        Date now = new Date(); //默认使用当天作为查询条件

        if (model.getBeginUpdateDt() == null) {
            model.setBeginDate(now);
        }
        if (model.getEndUpdateDt() == null) {
            model.setEndDate(now);
        }
//        if (joyoungSearchModel.getProcessFlag() == null) {
//            joyoungSearchModel.setProcessFlag(2);
//        }
        return model;
    }


    /**
     *
     */
    @RequestMapping(value = "viomiFailLog")
    public String viomiFailLog(@ModelAttribute("ViomiSearchModel") ViomiFailLogSearchModel searchModel, HttpServletRequest request, HttpServletResponse response, Model model) {
        searchModel.setBeginUpdateDt(DateUtils.getStartOfDay(searchModel.getBeginDate()).getTime());
        searchModel.setEndUpdateDt(DateUtils.getEndOfDay(searchModel.getEndDate()).getTime());
        Page<VioMiExceptionOrder> page = viomiFailLogService.getFailLogList(new Page<>(request, response), searchModel);

        model.addAttribute("page",page);
        model.addAttribute("searchModel",searchModel);
        return "modules/viomi/rpt/viomiFailLogReport";
    }

    /**
     *  form
     */
    @RequestMapping(value = "viomiFailLogRetryForm", method = RequestMethod.GET)
    public String viomiFailLogRetryForm(Long id, HttpServletRequest request, HttpServletResponse response, Model model) {
        String viewForm = "modules/viomi/rpt/viomiRetryForm";
        List<VioMiApiLog> processlog = viomiFailLogService.getLogById(id);
        VioMiExceptionOrder orderInfo = viomiFailLogService.getOrderInfo(id);
        model.addAttribute("completedRetryBean", processlog);
        model.addAttribute("orderInfo", orderInfo);
        return viewForm;
    }

    /**
     *重发
     */
    @ResponseBody
    @RequestMapping(value = "viomiRetry", method = RequestMethod.GET)
    public AjaxJsonEntity retryData(@RequestParam("id") Long apiLogId) {
        if (apiLogId !=null) {
            User user = UserUtils.getUser();
           if (user!=null){
               return viomiFailLogService.retryViomiData(apiLogId,user.getName(),user.getId());
           }
        }
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity();
        ajaxJsonEntity.setSuccess(false);
        ajaxJsonEntity.setMessage("数据NULL");
        return ajaxJsonEntity;
    }


}
