package com.wolfking.jeesite.modules.sd.web;


import com.google.common.collect.Lists;
import com.wolfking.jeesite.common.config.redis.GsonRedisSerializer;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.web.BaseController;
import com.wolfking.jeesite.modules.md.entity.ProductCategory;
import com.wolfking.jeesite.modules.sd.entity.viewModel.NoticeMessageItemVM;
import com.wolfking.jeesite.modules.sd.entity.viewModel.NoticeMessageVM;
import com.wolfking.jeesite.modules.sd.service.FeedbackService;
import com.wolfking.jeesite.modules.sd.service.OrderService;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.service.AreaService;
import com.wolfking.jeesite.modules.sys.service.SystemService;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 消息提醒
 *
 * @author Ryan
 */
@Controller
@RequestMapping(value = "${adminPath}/sd/message/")
@Slf4j
public class OrderMessageController extends BaseController
{

	@Autowired
	private AreaService areaService;

	@Autowired
	private SystemService systemService;

	@Autowired
	private OrderService orderService;

	@Autowired
	private FeedbackService feedbackService;

	@Autowired
	private RedisUtils redisUtils;

	@Resource(name = "gsonRedisSerializer")
	public GsonRedisSerializer gsonRedisSerializer;

	//region 提醒

	/**
	 * 我的消息
	 */
	@ResponseBody
	@RequestMapping(value = "mymessages",method = RequestMethod.GET)
	public AjaxJsonEntity myMessages(){
		AjaxJsonEntity result = new AjaxJsonEntity(true);
		User user = UserUtils.getUser();
		if(user==null){
			result.setSuccess(false);
			result.setMessage("登录超时，请重新登录");
			return result;
		}

		try {
			HashMap<Integer,NoticeMessageItemVM> messages = NoticeMessageItemVM.getDefaultMessages(user.getUserType());
			NoticeMessageItemVM message;
			String field = new String("");
			Long qty;
			if(user.isCustomer()){
				//region 客户
				Long cid = user.getCompany()==null?user.getCustomerAccountProfile().getCustomer().getId():user.getCompanyId();
				//问题反馈
				if(user.getUserType()==User.USER_TYPE_CUSTOMER){//主帐号
					field = "total";
				}else{
					field = user.getId().toString();
				}
				 qty = redisUtils.hGet(
						RedisConstant.RedisDBType.REDIS_MS_DB,
						String.format(RedisConstant.MS_FEEDBACK_CUSTOMER,cid),
						field,
						Long.class
						);
				if(qty != null && qty>0) {
					message = messages.get(NoticeMessageItemVM.NOTICE_TYPE_FEEDBACK);
					if (message != null) {
						message.setQty(qty);
					}
				}
				//待处理问题反馈
				qty =  redisUtils.hGet(
						RedisConstant.RedisDBType.REDIS_MS_DB,
						String.format(RedisConstant.MS_FEEDBACK_PENDING_CUSTOMER,cid),
						field,
						Long.class
				);
				if(qty != null && qty>0) {
					message = messages.get(NoticeMessageItemVM.NOTICE_TYPE_FEEDBACK_PENDING);
					if (message != null) {
						message.setQty(qty);
					}
				}

				//endregion 客户
			}else if(user.isSaleman()){
				//region 业务
				//问题反馈
				field = "total";
				byte[] bytes;
				long total = 0;
				//1.分配客户
				Set<Long> customers = user.getCustomerIds();
 				if(customers != null && customers.size()>0){
					bytes = field.getBytes(StandardCharsets.UTF_8);
					//问题反馈 by customers
					//final List<Long> results = Lists.newArrayList();
					total = (Long)redisUtils.redisTemplate.execute(new RedisCallback<Object>() {
						@Override
						public Long doInRedis(RedisConnection connection)
								throws DataAccessException {
							long id = 0;
							try {
								connection.select(RedisConstant.RedisDBType.REDIS_MS_DB.ordinal());
								Long stotal = 0l;
								for(Long cid:customers){
									byte[] bvalue = connection.hGet(String.format(RedisConstant.MS_FEEDBACK_CUSTOMER,cid).getBytes(StandardCharsets.UTF_8),bytes);
									if(bvalue !=null) {
										stotal = stotal + new BigDecimal(gsonRedisSerializer.deserialize(bvalue).toString()).longValue();
									}
								}
								return stotal;
							} catch (Exception e) {
								return 0l;
							}
						}
					});
					message = messages.get(NoticeMessageItemVM.NOTICE_TYPE_FEEDBACK);
					message.setQty(total);
					/*
					redisUtils.redisTemplate.executePipelined(new RedisCallback<Object>() {
						public Object doInRedis(RedisConnection connection) throws DataAccessException {
							connection.select(RedisConstant.RedisDBType.REDIS_MS_DB.ordinal());
							Long stotal = 0l;
							for(Long cid:customers){
								byte[] bvalue = connection.hGet(String.format(RedisConstant.MS_FEEDBACK_CUSTOMER,cid).getBytes(StandardCharsets.UTF_8),bytes);
								if(bvalue !=null) {
									stotal = stotal + Long.valueOf(gsonRedisSerializer.deserialize(bvalue).toString());
									//stotal = stotal + (Long) gsonRedisSerializer.deserialize(bvalue);
								}
							}
							results.add(stotal);
							return null;
						}
					});*/



					//待处理问题反馈
					total = (Long)redisUtils.redisTemplate.execute(new RedisCallback<Object>() {
						@Override
						public Long doInRedis(RedisConnection connection)
								throws DataAccessException {
							long id = 0;
							try {
								connection.select(RedisConstant.RedisDBType.REDIS_MS_DB.ordinal());
								Long stotal = 0l;
								for(Long cid:customers){
									byte[] bvalue = connection.hGet(String.format(RedisConstant.MS_FEEDBACK_PENDING_CUSTOMER,cid).getBytes(StandardCharsets.UTF_8),bytes);
									if(bvalue !=null) {
										stotal = stotal + new BigDecimal(gsonRedisSerializer.deserialize(bvalue).toString()).longValue();
									}
								}
								return stotal;
							} catch (Exception e) {
								return 0l;
							}
						}
					});
					message = messages.get(NoticeMessageItemVM.NOTICE_TYPE_FEEDBACK_PENDING);
					message.setQty(total);

					//final List<Long> results1 = Lists.newArrayList();
					//redisUtils.redisTemplate.executePipelined(new RedisCallback<Object>() {
					//	public Object doInRedis(RedisConnection connection) throws DataAccessException {
					//		//StringRedisConnection stringRedisConn = (StringRedisConnection)connection;
					//		connection.select(RedisConstant.RedisDBType.REDIS_MS_DB.ordinal());
					//		Long stotal = 0l;
					//		for(Long cid:customers){
					//			byte[] bvalue = connection.hGet(String.format(RedisConstant.MS_FEEDBACK_PENDING_CUSTOMER,cid).getBytes(StandardCharsets.UTF_8),bytes);
					//			if(bvalue !=null) {
					//				stotal = stotal + Long.valueOf((String) gsonRedisSerializer.deserialize(bvalue));
					//			}
					//		}
					//		//connection.lPush("total".getBytes(StandardCharsets.UTF_8),stotal.toString().getBytes(StandardCharsets.UTF_8));
					//		results1.add(stotal);
					//		return null;
					//	}
					//});
					//if(results1!=null && results1.size()>0){
					//	//total = Long.valueOf(StringUtils.toString((byte[])results.get(0)));
					//	total = results1.get(0);
					//}

				}
				//endregion 业务
			} else if(user.isKefu()){
				//region 客服
				long total = 0;
				List<byte[]> lstbytes;
				byte[] bytes;
				//1.分配客户
				Set<Long> customers = user.getCustomerIds();
				if(customers != null && customers.size()>0){
					String[] strids = customers.stream().map(t->t.toString()).toArray(String[]::new);
					//问题反馈 by customers
					lstbytes = redisUtils.hGet(
							RedisConstant.RedisDBType.REDIS_MS_DB,
							RedisConstant.MS_FEEDBACK_KEFUBYCUSTOMER,
							strids
					);
					if(lstbytes !=null && lstbytes.size() >0){
						lstbytes = lstbytes.stream().filter(t->t != null).collect(Collectors.toList());
						for(int i=0,size = lstbytes.size();i<size;i++){
							bytes = lstbytes.get(i);
							if(bytes!=null) {
								qty = StringUtils.toLong(StringUtils.toString(bytes));
								total = total + qty;
							}
						}
					}
					if(total>0){
						message = messages.get(NoticeMessageItemVM.NOTICE_TYPE_FEEDBACK);
						message.setQty(total);
					}
					//待处理问题反馈
					lstbytes = redisUtils.hGet(
							RedisConstant.RedisDBType.REDIS_MS_DB,
							RedisConstant.MS_FEEDBACK_PENDING_KEFUBYCUSTOMER,
							strids
					);
					total = 0;
					if(lstbytes !=null && lstbytes.size() >0){
						lstbytes = lstbytes.stream().filter(t->t != null).collect(Collectors.toList());
						for(int i=0,size = lstbytes.size();i<size;i++){
							bytes = lstbytes.get(i);
							if(bytes!=null) {
								qty = StringUtils.toLong(StringUtils.toString(bytes));
								total = total + qty;
							}
						}
					}
					if(total>0){
						message = messages.get(NoticeMessageItemVM.NOTICE_TYPE_FEEDBACK_PENDING);
						message.setQty(total);
					}
					//app异常
					// by customers
					total = 0;
					lstbytes = redisUtils.hGet(
							RedisConstant.RedisDBType.REDIS_MS_DB,
							RedisConstant.MS_APP_ABNORMALY_KEFUBYCUSTOMER,
							strids
					);
					if(lstbytes !=null && lstbytes.size() >0){
						lstbytes = lstbytes.stream().filter(t->t != null).collect(Collectors.toList());
						for(int i=0,size = lstbytes.size();i<size;i++){
							bytes = lstbytes.get(i);
							if(bytes!=null) {
								qty = StringUtils.toLong(StringUtils.toString(bytes));
								total = total + qty;
							}
						}
					}
					if(total>0){
						message = messages.get(NoticeMessageItemVM.NOTICE_TYPE_APPABNORMALY);
						if(message !=null) {
							message.setQty(total);
						}
					}

				}else {// by 区域
					List<Area> areas = areaService.getAreaListOfKefu(user.getId());
					if (areas != null && areas.size() > 0) {
						String[] strids = areas.stream().map(t->t.getId().toString()).toArray(String[]::new);
						//问题反馈
						lstbytes = redisUtils.hGet(
								RedisConstant.RedisDBType.REDIS_MS_DB,
								RedisConstant.MS_FEEDBACK_KEFUBYAREA,
								strids
						);
						total = 0;
						for(int i=0,size = lstbytes.size();i<size;i++){
							bytes = lstbytes.get(i);
							if(bytes!=null) {
								qty = StringUtils.toLong(StringUtils.toString(bytes));
								total = total + qty;
							}
						}
						if(total>0){
							message = messages.get(NoticeMessageItemVM.NOTICE_TYPE_FEEDBACK);
							message.setQty(total);
						}
						//待处理问题反馈
						lstbytes = redisUtils.hGet(
								RedisConstant.RedisDBType.REDIS_MS_DB,
								RedisConstant.MS_FEEDBACK_PENDING_KEFUBYAREA,
								strids
						);
						total = 0;
						for(int i=0,size = lstbytes.size();i<size;i++){
							bytes = lstbytes.get(i);
							if(bytes!=null) {
								qty = StringUtils.toLong(StringUtils.toString(bytes));
								total = total + qty;
							}
						}
						if(total>0){
							message = messages.get(NoticeMessageItemVM.NOTICE_TYPE_FEEDBACK_PENDING);
							message.setQty(total);
						}

						//app异常
						// by customers
						total = 0;
						lstbytes = redisUtils.hGet(
								RedisConstant.RedisDBType.REDIS_MS_DB,
								RedisConstant.MS_APP_ABNORMALY_KEFUBYAREA,
								strids
						);
						if(lstbytes !=null && lstbytes.size() >0){
							for(int i=0,size = lstbytes.size();i<size;i++){
								bytes = lstbytes.get(i);
								if(bytes!=null) {
									qty = StringUtils.toLong(StringUtils.toString(bytes));
									total = total + qty;
								}
							}
						}
						if(total>0){
							message = messages.get(NoticeMessageItemVM.NOTICE_TYPE_APPABNORMALY);
							if(message !=null) {
								message.setQty(total);
							}
						}
					}
				}
				//endregion 客服
			}
			NoticeMessageVM messagevm = new NoticeMessageVM();
			//total
			long total = 0;
			/*
			for (Map.Entry<Integer, NoticeMessageItemVM> entry : messages.entrySet()) {
				total = total + entry.getValue().getQty();
			}*/
			message = messages.get(NoticeMessageItemVM.NOTICE_TYPE_FEEDBACK);
			if(message != null) {
				messagevm.setTotal(message.getQty());
			}
			messagevm.setItems(messages.values().stream().collect(Collectors.toList()));
			result.setData(messagevm);
		}catch (Exception e){
			result.setSuccess(false);
			result.setMessage("读取消息失败");
			LogUtils.saveLog("读取消息失败","OrderMessageController.myMessages",user.getId().toString(),e,user);
		}
		return result;
	}

	/**
	 * 标记已读/已处理
	 * @param noticeType 类型
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "marked")
	public AjaxJsonEntity marked(Integer noticeType,HttpServletResponse response) {
		response.setContentType("application/json; charset=UTF-8");
		AjaxJsonEntity result = new AjaxJsonEntity(true);
		if(noticeType==null || noticeType<=0){
			result.setSuccess(false);
			result.setMessage("参数错误");
			return result;
		}

		User user = UserUtils.getUser();
		if(user==null || user.getId()==null){
			result.setSuccess(false);
			result.setMessage("登录超时，请重新登录。");
			return result;
		}

		try {
//			feedbackService.markedFeedback(noticeType,user);
			feedbackService.markedFeedbackNew(noticeType, user);
			if(user.isCustomer()){
				//region 客户
				Long cid = user.getCompany()==null?user.getCustomerAccountProfile().getCustomer().getId():user.getCompanyId();
				if(user.getUserType()==User.USER_TYPE_CUSTOMER){
					//主帐号
					if(noticeType == NoticeMessageItemVM.NOTICE_TYPE_FEEDBACK) {
						redisUtils.remove(
								RedisConstant.RedisDBType.REDIS_MS_DB,
								String.format(RedisConstant.MS_FEEDBACK_CUSTOMER, cid)
						);
					}else if(noticeType == NoticeMessageItemVM.NOTICE_TYPE_FEEDBACK_PENDING) {
						redisUtils.remove(
								RedisConstant.RedisDBType.REDIS_MS_DB,
								String.format(RedisConstant.MS_FEEDBACK_PENDING_CUSTOMER, cid)
						);
					}
				}else{
					if(noticeType == NoticeMessageItemVM.NOTICE_TYPE_FEEDBACK) {
						redisUtils.hdel(
								RedisConstant.RedisDBType.REDIS_MS_DB,
								String.format(RedisConstant.MS_FEEDBACK_CUSTOMER,cid),
								user.getId().toString()
						);
					}else if(noticeType == NoticeMessageItemVM.NOTICE_TYPE_FEEDBACK_PENDING) {
						redisUtils.hdel(
								RedisConstant.RedisDBType.REDIS_MS_DB,
								String.format(RedisConstant.MS_FEEDBACK_PENDING_CUSTOMER,cid),
								user.getId().toString()
						);
					}
				}

				//endregion 客户
			}else if(user.isSaleman()){
				//region 业务
				Set<Long> customers = user.getCustomerIds();//客户
				if(customers != null && customers.size()>0){
					for(Long cid:customers){
						if(noticeType == NoticeMessageItemVM.NOTICE_TYPE_FEEDBACK) {
							redisUtils.remove(
									RedisConstant.RedisDBType.REDIS_MS_DB,
									String.format(RedisConstant.MS_FEEDBACK_CUSTOMER, cid)
							);
						}else if(noticeType == NoticeMessageItemVM.NOTICE_TYPE_FEEDBACK_PENDING) {
							redisUtils.remove(
									RedisConstant.RedisDBType.REDIS_MS_DB,
									String.format(RedisConstant.MS_FEEDBACK_PENDING_CUSTOMER, cid)
							);
						}
					}
				}
				//endregion 业务
			} else if(user.isKefu()){
				//region 客服
				//1.分配客户
				Set<Long> customers = user.getCustomerIds();
				if(customers != null && customers.size()>0){
					//问题反馈 by customers
					String[] strids = customers.stream().map(t->t.toString()).toArray(String[]::new);
					if(noticeType == NoticeMessageItemVM.NOTICE_TYPE_FEEDBACK) {
						redisUtils.hdel(
								RedisConstant.RedisDBType.REDIS_MS_DB,
								RedisConstant.MS_FEEDBACK_KEFUBYCUSTOMER,
								strids
						);
					}else if(noticeType == NoticeMessageItemVM.NOTICE_TYPE_FEEDBACK_PENDING) {
						redisUtils.hdel(
								RedisConstant.RedisDBType.REDIS_MS_DB,
								RedisConstant.MS_FEEDBACK_PENDING_KEFUBYCUSTOMER,
								strids
						);
					}else if(noticeType == NoticeMessageItemVM.NOTICE_TYPE_APPABNORMALY){
						redisUtils.hdel(
								RedisConstant.RedisDBType.REDIS_MS_DB,
								RedisConstant.MS_APP_ABNORMALY_KEFUBYCUSTOMER,
								strids
						);
					}
					/*
					for(Long cid:customers){
						if(noticeType == NoticeMessageItemVM.NOTICE_TYPE_FEEDBACK) {

							redisUtils.hdel(
									RedisConstant.RedisDBType.REDIS_MS_DB,
									RedisConstant.MS_FEEDBACK_KEFUBYCUSTOMER,
									cid.toString()
							);
						}else if(noticeType == NoticeMessageItemVM.NOTICE_TYPE_FEEDBACK_PENDING) {
							redisUtils.hdel(
									RedisConstant.RedisDBType.REDIS_MS_DB,
									RedisConstant.MS_FEEDBACK_PENDING_KEFUBYCUSTOMER,
									cid.toString()
							);
						}
					}
					*/

				}else {// by 区域
					List<Area> areas = areaService.getAreaListOfKefu(user.getId());
					if (areas != null && areas.size() > 0) {
						String[] strids = areas.stream().map(t->t.getId().toString()).toArray(String[]::new);
						if(noticeType == NoticeMessageItemVM.NOTICE_TYPE_FEEDBACK) {
							redisUtils.hdel(
									RedisConstant.RedisDBType.REDIS_MS_DB,
									RedisConstant.MS_FEEDBACK_KEFUBYAREA,
									strids
							);
						}else if(noticeType == NoticeMessageItemVM.NOTICE_TYPE_FEEDBACK_PENDING) {
							redisUtils.hdel(
									RedisConstant.RedisDBType.REDIS_MS_DB,
									RedisConstant.MS_FEEDBACK_PENDING_KEFUBYAREA,
									strids
							);
						}else if(noticeType == NoticeMessageItemVM.NOTICE_TYPE_APPABNORMALY){
							redisUtils.hdel(
									RedisConstant.RedisDBType.REDIS_MS_DB,
									RedisConstant.MS_APP_ABNORMALY_KEFUBYAREA,
									strids
							);
						}
						/*
						for(int i=0,size = areas.size();i<size;i++){
							if(noticeType == NoticeMessageItemVM.NOTICE_TYPE_FEEDBACK) {
								redisUtils.hdel(
										RedisConstant.RedisDBType.REDIS_MS_DB,
										RedisConstant.MS_FEEDBACK_KEFUBYAREA,
										areas.get(i).getId().toString()
								);
							}else if(noticeType == NoticeMessageItemVM.NOTICE_TYPE_FEEDBACK_PENDING) {
								redisUtils.hdel(
										RedisConstant.RedisDBType.REDIS_MS_DB,
										RedisConstant.MS_FEEDBACK_PENDING_KEFUBYAREA,
										areas.get(i).getId().toString()
								);
							}
						}
						*/
					}
				}
				//endregion 客服
			}

		}catch (Exception e){
			result.setSuccess(false);
			result.setMessage("处理失败："+e.getMessage());
			LogUtils.saveLog(
					"标记提醒错误",
					"OrderMessageController.marked",
					String.format("noticeType:%s user id:%s",noticeType.toString(),user.getId()),
					e,
					null
					);
		}
		return  result;
	}

	//endregion 提醒

}

