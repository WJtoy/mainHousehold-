package com.wolfking.jeesite.ms.callbackService.controller;


import com.google.common.base.Splitter;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.voiceservice.TalkRecord;
import com.kkl.kklplus.entity.voiceservice.VoiceStatus;
import com.kkl.kklplus.entity.voiceservice.VoiceTask;
import com.wolfking.jeesite.common.config.Global;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.callbackService.feign.VoiceCallbackFeign;
import com.wolfking.jeesite.ms.common.config.MicroServicesProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.MessageFormat;
import java.util.List;

/**
 * 智能回访控制器
 */
@Configurable
@Slf4j
@Controller
@RequestMapping(value = "${adminPath}/voice/")
public class VoiceCallbackController extends BaseController {

    @Value("${site.code}")
    private String siteCode;

    @Autowired
    private MicroServicesProperties msProperties;

    @Autowired
    private VoiceCallbackFeign voiceCallbackFeign;

    /**
     * 查看智能客服回访信息
     * 包含文字及语音
     */
    @RequiresPermissions("sd:order:grade")
    @RequestMapping(value = "taskInfo/{quarter}/{orderId}")
    public String taskInfo(@PathVariable("quarter") String quarter,@PathVariable("orderId") Long orderId, HttpServletRequest request, HttpServletResponse response, Model model) {
        String viewForm = "modules/sd/voice/taskInfo";
        if(StringUtils.isBlank(siteCode)){
            addMessage(model, "错误：未设定子系统(site)参数，或读取失败！");
            return viewForm;
        }
        VoiceTask task = new VoiceTask();
        model.addAttribute("task",task);
        if (!msProperties.getVoiceService().getEnabled()) {
            addMessage(model, MSErrorCode.MICROSERVICE_DISABLED.msg);
            return viewForm;
        }
        Session session = UserUtils.getSession();
        if (session == null) {
            addMessage(model, "错误：登录超时！");
            return viewForm;
        }
        MSResponse<VoiceTask> msResponse = voiceCallbackFeign.getTaskInfo(siteCode,quarter,orderId);
        if(!MSResponse.isSuccessCode(msResponse)){
            addMessage(model, "错误："+ msResponse.getMsg());
            return viewForm;
        }
        task = msResponse.getData();
        if(StringUtils.isNoneBlank(task.getLabelling())) {
            List<String> labels = Splitter.onPattern("[;|,]")
                    .omitEmptyStrings()
                    .trimResults()
                    .splitToList(task.getLabelling());
            task.setLabels(labels);
        }
        /* v1.6
        if(task.getTalkInfo() != null && task.getTalkInfo().getFragment() != null && !task.getTalkInfo().getFragment().isEmpty()){
            List<TalkRecord> talkRecords = task.getTalkInfo().getFragment();
            for(TalkRecord talkRecord:talkRecords){
                //if(StringUtils.isBlank(talkRecord.getAnswer())){
                if(!talkRecord.getVocfile().equalsIgnoreCase("listen")){
                    talkRecord.setVocfile(StringUtils.substringAfterLast(talkRecord.getVocfile(),"#!"));
                }
            }
        }*/
        // v1.7
        if(task.getTalkInfo() != null && !task.getTalkInfo().isEmpty()){
            List<TalkRecord> talkRecords = task.getTalkInfo();
            for(TalkRecord talkRecord:talkRecords){
                //if(StringUtils.isBlank(talkRecord.getAnswer())){
                if(!talkRecord.getVocfile().equalsIgnoreCase("listen")){
                    if(StringUtils.isBlank(talkRecord.getCaption())){
                        talkRecord.setCaption(StringUtils.substringAfterLast(talkRecord.getVocfile(), "#!"));
                    }
                }
            }
        }
        VoiceStatus voiceStatus = VoiceStatus.fromCode(task.getStatus());
        if(voiceStatus != null) {
            task.setStatus(MessageFormat.format("{0}-{1}",voiceStatus.getCode(),voiceStatus.name));
        }
        model.addAttribute("task",task);
        return viewForm;
    }

}

