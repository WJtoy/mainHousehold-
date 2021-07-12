package com.wolfking.jeesite.modules.td.web;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.HttpClientUtils;
import com.wolfking.jeesite.common.utils.ObjectUtils;
import com.wolfking.jeesite.common.utils.QuarterUtils;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.entity.Grade;
import com.wolfking.jeesite.modules.md.entity.GradeItem;
import com.wolfking.jeesite.modules.md.service.GradeService;
import com.wolfking.jeesite.modules.sd.entity.OrderGrade;
import com.wolfking.jeesite.modules.sd.entity.viewModel.OrderGradeModel;
import com.wolfking.jeesite.modules.sd.service.OrderService;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.Log;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import com.wolfking.jeesite.modules.td.dao.Message2Dao;
import com.wolfking.jeesite.modules.td.entity.Message2;
import com.wolfking.jeesite.modules.td.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


/**
 * 短信消息Controller
 * 接收 sms.1xinxi.cn 的短信
 * @author Ryan
 */
@Controller
@RequestMapping(value = "${adminPath}/td/")
@Slf4j
public class ShortMessageController extends BaseController
{

	@Autowired
	private MessageService messageService;
	@Autowired
	private OrderService orderService;
	@Autowired
	private GradeService gradeService;
	@Autowired
	private Message2Dao message2Dao;

	//region [自动客评]


	@ResponseBody
	@RequestMapping(value = "receiveMessage", method = RequestMethod.POST)
	public String receiveMessage(@RequestParam String name, @RequestParam String pwd, @RequestParam(required = true) String args,
								 HttpServletRequest request,HttpServletResponse response)
	{
		String returnMsg = new String("");
		if (StringUtils.isNoneBlank(args))
		{
			try {
				// args="13800210021#@#1#@#2013-08-21 21:20:11#@#008123#@@#13978767656#@#content2#@#2013-08-21 21:20:30#@#";
				autoGrade(args);
				returnMsg = "OK";
			}catch (Exception e){
				log.error("[ShortMessageController.receiveMessage]短信回复自动客评,短信内容:{}",args,e);
				returnMsg = e.getMessage();
			}

		} else
		{
			returnMsg = "没有内容";
		}
		return returnMsg;
	}

	/**
	 * 接收第三方短信，自动客评
	 * 例子(两条信息在一起):13800210021#@##@#2013-08-21 21:20:11#@#008123#@@#13978767656#@#content2#@#2013-08-21 21:20:30#@#
	 * 1. 13800210021#@#1#@#2013-08-21 21:20:11#@#008123
	 * 2. 13978767656#@#content2#@#2013-08-21 21:20:30#@#
	 *    0:手机号 1-回复内容 2-日期 3-Extno
	 * 回复内容是：1 ，才自动客评，否则:记录log，并标记订单app异常
	 * 只有回复内容是1，才让厂商可见
	 * @param messages
	 */
	public void autoGrade(String messages) {
		String[] messagelist = messages.split("#@@#");
		if(messagelist==null || messagelist.length == 0){
			log.error("收到短信内容为空,msg:{}",messages);
			return;
		}
		List<Grade> gradeList = gradeService.findAllListCache();//all
		if(gradeList == null || gradeList.isEmpty()){
			log.error("读取客评项失败：无客评项");
			return;
		}

		List<OrderGrade> gradeItems = Lists.newArrayList();
		gradeList.stream().forEach(grade -> {
			GradeItem item = grade.getItemList().stream().sorted(Comparator.comparing(GradeItem::getPoint).reversed())
					.findFirst().orElse(null);
			if (item != null) {
				OrderGrade orderGrade = new OrderGrade();
				orderGrade.setGradeId(grade.getId());
				orderGrade.setGradeName(grade.getName());
				orderGrade.setGradeItemId(item.getId());
				orderGrade.setGradeItemName(item.getRemarks());
				orderGrade.setPoint(item.getPoint());
				orderGrade.setSort(grade.getSort());
				gradeItems.add(orderGrade);
			}
		});
		OrderGradeModel gradeModel = new OrderGradeModel();
		gradeModel.setCheckOrderFee(false);//已经检查过了
		//gradeModel.setCheckCanAutoCharge(false);//不检查能否自动生成对账单
		//gradeModel.setCanAutoCharge(true);//自动生成对账单
		gradeModel.setGradeList(gradeItems);
		//gradeModel.setAutoGradeFlag(1);
		gradeModel.setAutoGradeFlag(OrderUtils.OrderGradeType.MESSAGE_GRADE.getValue());

		Date date = new Date();
		User user = new User(2l);
		user.setName("用户回复短信");


		String phone = new String("");

		//记录收到的内容
		Message2 message = new Message2();
		message.setMobile(phone);
		message.setType("gt");//接收
		message.setCreateBy(user);
		message.setCreateDate(date);
		message.setTriggerBy(user.getId());
		message.setTriggerDate(date);
		message.setQuarter(QuarterUtils.getSeasonQuarter(date));
		Message2 savemessage = null;
		for (int i = 0; i < messagelist.length; i++) {
			String[] messageItem = messagelist[i].split("#@#");
			//4->3,因为ext no 为空自动丢弃
			if (messageItem.length < 3) {
				message.setRemarks(message.getRemarks().length() == 0 ? "短信内容格式错误" : message.getRemarks().concat(",").concat("未找到订单"));
				try {
					message2Dao.insert(message);//保存消息
					LogUtils.saveLog("短信平台错误", "MessageService.autoGrade", "内容不够：" + messagelist[i], null, user, Log.TYPE_EXCEPTION);
				}catch (Exception e){
					log.error("短信平台错误,短信内容格式错误,msg:{}",messagelist[i],e);
				}
				continue;//下一个消息
			}

			phone = messageItem[0].trim();
			//记录收到的内容
			savemessage = ObjectUtils.clone(message);
			savemessage.setMobile(phone);
			savemessage.setContent(messageItem[1].trim());
			savemessage.setSendTime(DateUtils.parseDate(messageItem[2]));
			// Extno为空时需特殊处理
			savemessage.setExtno(messageItem.length > 3 ? messageItem[3] : "");
			//messageService.autoGradeOne(savemessage, gradeModel);
			//2019/01/18改新方法
			messageService.autoGradeOneNew(savemessage, gradeModel);
			try {
				TimeUnit.SECONDS.sleep(1);//1秒
			} catch (InterruptedException e) {}
		}
	}

	//endregion 自动客评

	//region 物流接口 kuaidi100.com
	/* 根据快递单号从orderItem中查询orderId的功能已经不可用，故注释该功能 edited by Zhoucy

	@RequestMapping(value = "kuaidiMessage", method = RequestMethod.POST)
	public void kuaidiMessage(HttpServletRequest request,
							  HttpServletResponse response) throws IOException
	{

		NoticeResponse resp = new NoticeResponse();
		resp.setResult(false);
		resp.setReturnCode("500");
		resp.setMessage("保存失败");
		try
		{
			String paraminfo = request.getParameter("param");
			// logger.error(paraminfo);
			NoticeRequest nReq = JacksonHelper.fromJSON(paraminfo,
					NoticeRequest.class);

			Result result = nReq.getLastResult();
			// 处理快递结果
			// 0:在途 1:揽件 2:疑难 3:签收 4:退签 5:派件 6:退回 7转单
			// nReq.getLastResult().getState();

			pendingOrder(result);

			resp.setResult(true);
			resp.setReturnCode("200");
			response.getWriter().print(JacksonHelper.toJSON(resp)); // 这里必须返回，否则认为失败，过30分钟又会重复推送。

		} catch (Exception e)
		{
			resp.setMessage("保存失败" + e.getMessage());
			response.getWriter().print(JacksonHelper.toJSON(resp));// 保存失败，服务端等30分钟会重复推送。
		}
	}

	private void pendingOrder(Result result)
	{
		if (result == null || StringUtils.isBlank(result.getCom())
				|| StringUtils.isBlank(result.getNu()))
		{
			return;
		} else
		{
			String expressCompany = result.getCom();
			String expressNo = result.getNu();
			Integer state = Integer.parseInt(result.getState());
			Long orderId = orderService.getOrderIdByExpress(expressCompany, expressNo);
			if(orderId==null){
				return;
			}

			Order order = orderService.getOrderById(orderId,"", OrderUtils.OrderDataLevel.CONDITION,true);
			if(order == null || order.getOrderCondition() == null)
			{
				return;
			}
			OrderCondition orderCondition = order.getOrderCondition();
			OrderProcessLog log;
			HashMap<String,Object> map = new HashMap<>();
			// 0:在途 1:揽件 2:疑难 3:签收 4:退签 5:派件 6:退回 7转单

			// 如果返回的结果在派件之前，如果订单已经有预约时间 并且预约时间 是当前时间的后一天还后，则订单还是停滞状态
			// 如果订单没有预约时间，则把订单改为等到货
			// 如果是派件状态，则设置订单的停滞原因为空
			if(state>=0 && state<=1 || state==7)
			{
				if (StringUtils.isBlank(orderCondition.getPendingType().getLabel())
						&& !orderCondition.getAppointmentDate().after(DateUtils.addDays(new Date(), 1)))
				{
					orderCondition.setPendingFlag(1);
					orderCondition.setPendingType(new Dict("2","等到货"));
					orderCondition.setPendingTypeDate(new Date());
					Date date = DateUtils.addDays(new Date(), 1);
					date.setHours(8);
					orderCondition.setAppointmentDate(date);

					Dict status = orderCondition.getStatus();
					log = new OrderProcessLog();
					log.setQuarter(order.getQuarter());
					log.setAction("快递处理");
					log.setOrderId(orderId);
					log.setActionComment(String.format("收到快递推送，快递单号:%s 的状态为:%s,订单自动设置停滞", expressNo, status.getLabel()));
					log.setRemarks("快递公司:".concat(expressCompany));
					log.setStatus(status.getLabel());
					log.setStatusValue(Integer.parseInt(status.getValue()));
					log.setStatusFlag(OrderProcessLog.OPL_SF_CHANGED_STATUS);
					log.setCloseFlag(0);
					log.setCreateBy(orderCondition.getKefu());
					log.setCreateDate(new Date());
					orderService.autoPendingOrder(orderCondition,log);
				}

			} else if (state>=2 && state<=6)
			{

				if (StringUtils.isNotBlank(orderCondition.getPendingType().getLabel()))
				{
					orderCondition.setPendingFlag(2);//正常
					orderCondition.setPendingType(new Dict("0",""));

					Dict status = orderCondition.getStatus();
					log = new OrderProcessLog();
					log.setQuarter(order.getQuarter());
					log.setAction("快递处理");
					log.setOrderId(orderId);
					log.setActionComment(String.format("收到快递推送，快递单号:%s 的状态为:%s,订单取消停滞", expressNo, status.getLabel()));
					log.setRemarks("快递公司:".concat(expressCompany));
					log.setStatus(status.getLabel());
					log.setStatusValue(Integer.parseInt(status.getValue()));
					log.setStatusFlag(OrderProcessLog.OPL_SF_CHANGED_STATUS);
					log.setCloseFlag(0);
					log.setCreateBy(orderCondition.getKefu());
					log.setCreateDate(new Date());
					orderService.autoPendingOrder(orderCondition,log);

				}
			}

		}

	}
	*/

	//endregion 物流接口

	@RequestMapping(value = "requestTimeOut", method = RequestMethod.POST)
	public void requestTimeOut(HttpServletRequest request,
							  HttpServletResponse response) throws IOException{
		try {
			Thread.sleep(20000);
		}catch (Exception e){
		}finally {
			response.getWriter().print("ok");
		}

	}

	@RequestMapping(value = "testHttpPostUtilsTimeOut", method = RequestMethod.POST)
	public void testHttpPostUtilsTimeOut(HttpServletRequest request,
							   HttpServletResponse response) throws IOException{
		try {
			Map<String,String> map = Maps.newHashMap();
			map.put("name", "name");
			map.put("pwd","password");
			map.put("mobile","13760468206");
			map.put("content","测试内容");
			map.put("stime","");
			map.put("sign","");
			map.put("extno","");
			map.put("type","pt");
			HttpClientUtils.post("http://localhost:8080/td/requestTimeOut",map,"",8000,18000);
		}catch (Exception e){
			log.error("[ShortMessageController.testHttpPostUtilsTimeOut]",e);
		}finally {
			response.getWriter().print("ok");
		}

	}
}

