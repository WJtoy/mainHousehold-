/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.wolfking.jeesite.modules.mq.web;

import com.kkl.kklplus.entity.sys.SysSMSTypeEnum;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.mq.sender.sms.SmsMQSender;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.modules.td.entity.Message2;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;

/**
 * 发送短信,前端客服订单详情页触发
 * @author Ryan
 * @version 2018-4-9
 */
@Controller
@RequestMapping(value = "${adminPath}/mq/shortmessage")
public class MQShortMessageController extends BaseController {

	@Autowired
	private SmsMQSender smsMQSender;

	/**
	 * 发送短信
	 * @param message  消息体
	 * @return
	 */
	@RequiresAuthentication
	@ResponseBody
	@RequestMapping(value = "/send")
	public AjaxJsonEntity send(Message2 message, HttpServletResponse response) {
		response.setContentType("application/json; charset=UTF-8");
		AjaxJsonEntity ajaxEntity = new AjaxJsonEntity(true);
		User user = UserUtils.getUser();
		if(StringUtils.isBlank(message.getMobile())){
			ajaxEntity.setSuccess(false);
			ajaxEntity.setMessage("发送失败：手机号码未设定");
			return ajaxEntity;
		}
		if(StringUtils.isBlank(message.getContent())){
			ajaxEntity.setSuccess(false);
			ajaxEntity.setMessage("发送失败：短信内容未设定");
			return ajaxEntity;
		}
//		smsMQSender.send(
//				message.getMobile(),
//				message.getContent(),
//				"",
//				user==null?2l:user.getId(),
//				System.currentTimeMillis()
//		);
		//TODO: 短信类型
		smsMQSender.sendNew(
				message.getMobile(),
				message.getContent(),
				"",
				user==null?2l:user.getId(),
				System.currentTimeMillis(),
				SysSMSTypeEnum.KEFU_ORDERDETAIL_PAGE_TRIGGER
		);
		/*
		String result = SendMessageUtils.SendMessage(message);
		if(result.equalsIgnoreCase("failed")){
			ajaxEntity.setSuccess(false);
			ajaxEntity.setMessage("发送失败");

		}*/
		return ajaxEntity;
	}

}
