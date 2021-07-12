package com.wolfking.jeesite.ms.keg.rpt.web;

import com.kkl.kklplus.entity.keg.sd.KegOrderCompletedData;
import com.kkl.kklplus.entity.md.CustomerProductModel;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.keg.rpt.entity.KegSearchModel;
import com.wolfking.jeesite.ms.keg.rpt.service.MSKegFailLogService;
import com.wolfking.jeesite.ms.providermd.service.ProductModelService;
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
public class KegFailLogController extends BaseController {
    @Autowired
    private MSKegFailLogService kegFailLogService;

    @Autowired
    private ProductModelService productModelService;
    @ModelAttribute("KegSearchModel")
    public KegSearchModel get(@ModelAttribute("KegSearchModel") KegSearchModel joyoungSearchModel) {
        if (joyoungSearchModel == null) {
            joyoungSearchModel = new KegSearchModel();
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
     *
     */
    @RequestMapping(value = "kegFailLog")
    public String jdFailLog(@ModelAttribute("KegSearchModel") KegSearchModel searchModel, HttpServletRequest request, HttpServletResponse response, Model model) {
        searchModel.setBeginCreateDt(DateUtils.getStartOfDay(searchModel.getBeginDate()).getTime());
        searchModel.setEndCreateDt(DateUtils.getEndOfDay(searchModel.getEndDate()).getTime());
        Page<KegOrderCompletedData> page = kegFailLogService.getFailLogList(new Page<>(request, response), searchModel);

        model.addAttribute("page",page);
        model.addAttribute("searchModel",searchModel);
        return "modules/keg/kegFailLogReport";
    }

    /**
     * 韩电 form
     */
    @RequestMapping(value = "kegFailLogRetryForm", method = RequestMethod.GET)
    public String kegFailLogRetryForm(Long id, HttpServletRequest request, HttpServletResponse response, Model model) {
        String viewForm = "modules/keg/kegCompletedRetryForm";
        KegOrderCompletedData processlog = kegFailLogService.getLogById(id);
        List<CustomerProductModel> customerProductModelList = productModelService.getFromCache(processlog.getCustomerId(), processlog.getKklProductId());
        model.addAttribute("customerProductModelList", customerProductModelList);
        model.addAttribute("completedRetryBean", processlog);
        return viewForm;
    }


    /**
     *韩电 重发
     */
    @ResponseBody
    @RequestMapping(value = "kegCompletedRetry", method = RequestMethod.POST)
    public AjaxJsonEntity retryData(@RequestBody KegOrderCompletedData kegOrderCompletedData) {
        if (kegOrderCompletedData !=null) {
            User user = UserUtils.getUser();
            kegOrderCompletedData.setUpdateBy(user.getId());
            return kegFailLogService.retryData(kegOrderCompletedData);
        }
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity();
        ajaxJsonEntity.setSuccess(false);
        ajaxJsonEntity.setMessage("数据NULL");
        return ajaxJsonEntity;
    }

    /**
     * 韩电完成工单 忽略
     */
    @ResponseBody
    @RequestMapping(value = "kegCompletedCloseLog", method = RequestMethod.GET)
    public AjaxJsonEntity closeLog(@RequestParam("id") Long id) {
        //获取当前登录账号的id
        User user = UserUtils.getUser();
        long userId = 0;
        if (user!=null) {
             userId = user.getId();
        }
        if (id !=null) {
            return kegFailLogService.closeLog(id,userId);
        }
        AjaxJsonEntity ajaxJsonEntity = new AjaxJsonEntity();
        ajaxJsonEntity.setSuccess(false);
        ajaxJsonEntity.setMessage("数据NULL");
        return ajaxJsonEntity;
    }

}
