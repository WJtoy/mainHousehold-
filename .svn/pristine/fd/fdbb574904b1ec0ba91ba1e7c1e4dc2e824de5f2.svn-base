package com.wolfking.jeesite.modules.ws.web;

import com.google.common.collect.Lists;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.sys.entity.Notice;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.service.NoticeService;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.modules.ws.entity.WSRequest;
import com.wolfking.jeesite.modules.ws.entity.WSResponse;
import com.wolfking.jeesite.modules.ws.service.WebSocketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.util.Date;
import java.util.List;

/**
 * Created by Ryan Lu
 *
 */
@CrossOrigin
@Controller
@Slf4j
@RequestMapping(value = "${adminPath}/ws/")
public class WebSocketController {

    @Autowired
    private WebSocketService webSocketService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;
//    @Autowired
//    private NoticeService noticeService;

    /**
     * 未读WebSocket信息(sys_notice)
     */
    @ResponseBody
    @RequestMapping(value = "mynotices",method = RequestMethod.POST)
    public AjaxJsonEntity myNoticeList(){
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        User user = UserUtils.getUser();
        if(user==null){
            result.setSuccess(false);
            result.setMessage("登录超时，请重新登录");
            return result;
        }

        try {
            List<Notice> notices = null; //noticeService.getListByUserID(user.getId(),0); //mark on 2020-11-23
            if(notices==null){
                notices = Lists.newArrayList();
            }
            result.setData(notices);
        }catch (Exception e){
            result.setSuccess(false);
            result.setMessage("读取未读消息失败");
            log.error("[WebSocketController.myNoticeList]",e);
        }
        return result;
    }


    /**
     * 已读
     */
    @ResponseBody
    @RequestMapping(value = "read")
    public AjaxJsonEntity read(String id){
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        if(StringUtils.isBlank(id)){
            result.setSuccess(false);
            result.setMessage("参数错误");
            return result;
        }
        User user = UserUtils.getUser();
        if(user==null){
            result.setSuccess(false);
            result.setMessage("登录超时，请重新登录");
            return result;
        }
        Notice notice = new Notice(Long.valueOf(id));
        notice.setUpdateBy(user);
        notice.setUpdateDate(new Date());
        notice.setDelFlag(1);
        try {
            //noticeService.update(notice);  //mark on 2020-11-23
        }catch (Exception e){
            result.setSuccess(false);
            result.setMessage("更新数据错误");
            log.error("[WebSocketController.read]",e);
        }
        return result;
    }

    /* js:订阅:/topic/getResponse
     stompClient.subscribe('/user/queue/getResponse', function (response) {
        showResponse(JSON.parse(response.body).content);
    })
    js:发送到:/ws/welcome
    stompClient.send("/ws/welcome", {}, JSON.stringify({'id':1,'name': 'ryan','userType':1}));
    */
    @MessageMapping("/welcome")
    @SendToUser("/queue/getResponse")
    public WSResponse say(Principal principal,WSRequest message) {
        //System.out.println(message.getUserId());
        log.info("userId:{}",message.getUserId());
        WSResponse response = new WSResponse();
        response.setSendDate("09-16 17:05");
        response.setSender("ryan");
        response.setReferId("903192905273970688");
        response.setReferNo("O2017083100001");
        response.setTitle("测试");
        response.setContext("【问题反馈】工单号：K20170915000002回复问题反馈");
        response.setNoticeType(Notice.NOTICE_TYPE_FEEDBACK);
        return response;
    }

    //群发:websocket发送：stompClient.send("/ws/notice", {}, "notice 2");
    @MessageMapping("/notice")
    public void notice(Principal principal, WSRequest message) {
        // 参数说明 principal 当前登录的用户， message 客户端发送过来的内容
        // principal.getName() 可获得当前用户的username
        WSResponse response = new WSResponse();
        response.setSendDate("09-16 17:05");
        response.setSender("系统管理员");
        response.setReferId("");
        response.setReferNo("");
        response.setTitle("系统升级通知");
        response.setContext("系统将于今晚11:00进行升级，于明晨0点启用。");
        response.setNoticeType(Notice.NOTICE_TYPE_NOTICE);
        // 发送消息给订阅 "/topic/notice" 且在线的用户
        messagingTemplate.convertAndSend("/topic/notice", response);
    }

    /**
     * 测试广播
     */
    @ResponseBody
    @RequestMapping(value = { "testnotice" })
    public AjaxJsonEntity testNotice(HttpServletResponse response)
    {
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        try
        {
            WSResponse message = new WSResponse();
            message.setSendDate("09-16 17:05");
            message.setSender("系统管理员");
            message.setReferId("");
            message.setReferNo("");
            message.setTitle("系统升级通知");
            message.setContext("系统将于今晚11:00进行升级，于明晨0点启用。");
            // 发送消息给订阅 "/topic/notice" 且在线的用户
            messagingTemplate.convertAndSend("/topic/notice", message);
        } catch (Exception e)
        {
            result.setSuccess(false);
            result.setMessage(e.getMessage().toString());
        }
        return result;
    }

    /**
     * 测试点对点
     */
    @ResponseBody
    @RequestMapping(value = { "testpoint" })
    public AjaxJsonEntity testPoint(@RequestParam String toId, HttpServletResponse response)
    {
        response.setContentType("application/json; charset=UTF-8");
        AjaxJsonEntity result = new AjaxJsonEntity(true);
        try
        {
            // 发送消息给订阅 "/user/queue/notifications" 且在线的用户
            messagingTemplate.convertAndSendToUser(toId,"/queue/notifications", "send point to point");
        } catch (Exception e)
        {
            result.setSuccess(false);
            result.setMessage(e.getMessage().toString());
        }
        return result;
    }

}
