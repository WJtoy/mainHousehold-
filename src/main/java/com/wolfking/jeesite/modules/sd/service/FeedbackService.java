/**
 * Copyright &copy; 2012-2013 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.wolfking.jeesite.modules.sd.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wolfking.jeesite.common.config.redis.GsonRedisSerializer;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.service.LongIDBaseService;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.QuarterUtils;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.md.service.CustomerService;
import com.wolfking.jeesite.modules.sd.dao.FeedbackDao;
import com.wolfking.jeesite.modules.sd.dao.OrderDao;
import com.wolfking.jeesite.modules.sd.entity.*;
import com.wolfking.jeesite.modules.sd.utils.OrderCacheUtils;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.service.AreaService;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import com.wolfking.jeesite.ms.utils.MSUserUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 问题反馈Service
 *
 * @author RyanLu
 * @version 2014-10-29
 */
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Slf4j
public class FeedbackService extends LongIDBaseService {

    @Autowired
    private FeedbackDao feedbackDao;

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private OrderService orderService;

    @Autowired
    private AreaService areaService;

    @Autowired
    private CustomerService customerService;

    @Resource(name = "gsonRedisSerializer")
    public GsonRedisSerializer gsonRedisSerializer;

    @Autowired
    private RedisUtils redisUtils;

    // Feedback
    public Feedback get(@Param("id") Long id, @Param("quarter") String quarter) {
        Feedback feedback = feedbackDao.getById(id, quarter);
        List<Long> userIdList = Lists.newArrayList(feedback.getCreateBy().getId(), feedback.getCloseBy().getId(), feedback.getUpdateBy().getId());
        Map<Long, User> userMap = MSUserUtils.getMapByUserIds(userIdList);
        if (userMap != null && userMap.size() > 0) {
            feedback.setCreateBy(userMap.get(feedback.getCreateBy().getId()));
            feedback.setCloseBy(userMap.get(feedback.getCloseBy().getId()));
            feedback.setUpdateBy(userMap.get(feedback.getUpdateBy().getId()));
        }
        return feedback;
    }

    public Feedback getByOrderId(@Param("orderId") Long orderId, @Param("quarter") String quarter) {
        Feedback feedback = feedbackDao.getByOrderId(orderId, quarter);
        List<Long> userIdList = Lists.newArrayList(feedback.getCreateBy().getId(), feedback.getCloseBy().getId(), feedback.getUpdateBy().getId());
        Map<Long, User> userMap = MSUserUtils.getMapByUserIds(userIdList);
        if (userMap != null && userMap.size() > 0) {
            feedback.setCreateBy(userMap.get(feedback.getCreateBy().getId()));
            feedback.setCloseBy(userMap.get(feedback.getCloseBy().getId()));
            feedback.setUpdateBy(userMap.get(feedback.getUpdateBy().getId()));
        }
        return feedback;
    }

    public Feedback getWithItemsById(@Param("id") Long id, @Param("quarter") String quarter) {
        Feedback feedback = feedbackDao.getWithItemsById(id, quarter);
        List<Long> userIdList = Lists.newArrayList(feedback.getCreateBy().getId(), feedback.getCloseBy().getId(), feedback.getUpdateBy().getId());
        Map<Long, User> userMap = MSUserUtils.getMapByUserIds(userIdList);
        if (userMap != null && userMap.size() > 0) {
            feedback.setCreateBy(userMap.get(feedback.getCreateBy().getId()));
            feedback.setCloseBy(userMap.get(feedback.getCloseBy().getId()));
            feedback.setUpdateBy(userMap.get(feedback.getUpdateBy().getId()));
        }
        return feedback;
    }

    /**
     * 返回订单问题反馈文本内容列表
     * @param id
     * @return
     */
    public List<FeedbackItem> getFeedbackItems(@Param("id") Long id, @Param("quarter") String quarter) {
        List<FeedbackItem> feedbackItemList = feedbackDao.getFeedbackItems(id, quarter);
        List<Long> createByIdList = feedbackItemList.stream().map(fb -> fb.getCreateBy().getId()).collect(Collectors.toList());
        Map<Long, User> userMap = MSUserUtils.getMapByUserIds(createByIdList);
        if (userMap != null && userMap.size() > 0) {
            feedbackItemList.forEach(fbi -> {
                fbi.setCreateBy(userMap.get(fbi.getCreateBy().getId()));
            });
        }
        return feedbackItemList;
    }

    public Feedback getWithItemsByOrderId(@Param("id") Long orderId, @Param("quarter") String quarter) {
        Feedback feedback = feedbackDao.getWithItemsByOrderId(orderId, quarter);
        List<Long> userIdList = Lists.newArrayList(feedback.getCreateBy().getId(), feedback.getCloseBy().getId(), feedback.getUpdateBy().getId());
        Map<Long, User> userMap = MSUserUtils.getMapByUserIds(userIdList);
        if (userMap != null && userMap.size() > 0) {
            feedback.setCreateBy(userMap.get(feedback.getCreateBy().getId()));
            feedback.setCloseBy(userMap.get(feedback.getCloseBy().getId()));
            feedback.setUpdateBy(userMap.get(feedback.getUpdateBy().getId()));
        }
        return feedback;
    }


    /**
     * 新增问题反馈
     * 1.客户或业务员反馈
     *   客服未读及未处理数量+1，如同订单客服还未读，数量不累加 (有的客服只负责指定的客户，有的客服只按区域负责)
     * 2.客服反馈
     *   客户未读及未处理数量+1，如同订单客户或业务还未读，数量不累加
     * @param feedback
     * @throws Exception
     */
    @Transactional(readOnly = false)
    public void save(Feedback feedback) throws Exception {
        if (feedback.getOrder() == null || feedback.getOrder().getId() == null) {
            throw new RuntimeException("未关联订单:无法保存问题反馈。");
        }
        Order order = orderService.getOrderById(feedback.getOrder().getId(), feedback.getQuarter(), OrderUtils.OrderDataLevel.CONDITION, true);
        if (order == null || order.getOrderCondition() == null) {
            throw new RuntimeException("读取订单信息失败");
        }
        if (order.getOrderCondition().getFeedbackId() > 0) {
            throw new RuntimeException("该订单已有反馈");
        }
        OrderCondition condition = order.getOrderCondition();
        int orgReplyFlag = condition.getReplyFlag();
        int orgReplyCustomer = condition.getReplyFlagCustomer();
        int orgReplyKefu = condition.getReplyFlagKefu();
		/*
		Feedback f = feedbackDao.getByOrderId(feedback.getOrder().getId());
		if(f != null){
			throw new RuntimeException("该订单已有反馈");
		}*/
        //数据库分片
        if (StringUtils.isBlank(feedback.getQuarter())) {
            feedback.setQuarter(order.getQuarter());
        }
        feedback.setCreateDate(new Date());
        //int applyBy = 2;//发起人: 1-kefu 2-customer
        if (feedback.getCreateBy().isCustomer() || feedback.getCreateBy().isSaleman()) {
            feedback.setReplyFlag(2);
            condition.setReplyFlag(2);
            condition.setReplyFlagCustomer(1);
        } else {
            feedback.setReplyFlag(1);
            condition.setReplyFlag(1);
            condition.setReplyFlagKefu(1);
            //applyBy = 1;
        }

        feedback.setCloseFlag(0);
        feedbackDao.insert(feedback);
        //order
        if (feedback.getOrder() != null && feedback.getOrder().getId() != null) {
            HashMap<String, Object> params = new HashMap<String, Object>();
            params.put("quarter", order.getQuarter());//数据库分片
            params.put("orderId", feedback.getOrder().getId());
            params.put("feedbackId", feedback.getId());//上一步保存后返回的id
            params.put("replyFlag", feedback.getReplyFlag());
            params.put("replyFlagKefu", condition.getReplyFlagKefu());
            params.put("replyFlagCustomer", condition.getReplyFlagCustomer());
            params.put("feedbackFlag", 1);//未关闭，2：关闭 0：无反馈
            //问题反馈，移除标题
            params.put("feedbackTitle", StringUtils.substring(feedback.getRemarks(), 0, 20));//2017-09-23
            params.put("feedbackDate", feedback.getCreateDate());
            params.put("updateDate", feedback.getCreateDate());
            params.put("updateBy", feedback.getCreateBy());
            orderDao.updateCondition(params);

            //cache

		/*	String key = String.format(RedisConstant.SD_ORDER,feedback.getOrder().getId());
			if(redisUtils.exists(RedisConstant.RedisDBType.REDIS_SD_DB,key)) {
				//condition
				try {
					condition.setFeedbackTitle(StringUtils.substring(feedback.getRemarks(), 0, 20));
					//condition.setReplyFlag(feedback.getReplyFlag());
					condition.setFeedbackId(feedback.getId());
					condition.setFeedbackFlag(1);
					condition.setFeedbackDate(feedback.getCreateDate());

					params.clear();
					params.put("feedbackDate", feedback.getCreateDate());
					params.put("feedbackFlag", 1);//未关闭，2：关闭 0：无反馈
					//params.put("replyFlag", feedback.getReplyFlag());
					params.put("condition", orderConditionRedisAdapter.toJson(condition));//to string
					params.put("syncDate", new Date().getTime());
					redisUtils.hmSetAll(RedisConstant.RedisDBType.REDIS_SD_DB, key, params, 0l);
				}catch (Exception e){
					try{
						redisUtils.remove(RedisConstant.RedisDBType.REDIS_SD_DB,key);
					}catch (Exception e1){
						log.error("[FeedbackService.save] feedbackId:{}",feedback.getId(),e1);
					}
				}
			}*/

            //调用订单公共缓存
            condition.setFeedbackTitle(StringUtils.substring(feedback.getRemarks(), 0, 20));
            condition.setFeedbackId(feedback.getId());
            condition.setFeedbackFlag(1);
            condition.setFeedbackDate(feedback.getCreateDate());
            OrderCacheParam.Builder builder = new OrderCacheParam.Builder();
            builder.setOpType(OrderCacheOpType.UPDATE)
                    .setOrderId(feedback.getOrder().getId())
                    .setFeedbackDate(feedback.getCreateDate())
                    .setFeedbackFlag(1)
                    .setCondition(condition)
                    .setSyncDate(new Date().getTime())
                    .setExpireSeconds(0L);
            OrderCacheUtils.update(builder.build());

            //message
            try {
                User user = feedback.getCreateBy();
                Long cid = condition.getCustomer().getId();
                if (user.isCustomer() || user.isSaleman()) {
                    //客户/业务员->客服
                    User kefu = condition.getKefu();
                    if (kefu == null) {
                        return;
                    }
                    Boolean assinedCustomer = customerService.checkAssignedCustomer(kefu.getId(), StringUtils.toDouble(cid));

                    //未读
                    if (orgReplyFlag != 2) {
                        if (assinedCustomer) { //客服有分配的客户,by area
                            redisUtils.hIncrBy(
                                    RedisConstant.RedisDBType.REDIS_MS_DB,
                                    RedisConstant.MS_FEEDBACK_KEFUBYCUSTOMER,
                                    cid.toString(),
                                    1l
                            );
                        } else {//客服无分配的客户,by customer
                            redisUtils.hIncrBy(
                                    RedisConstant.RedisDBType.REDIS_MS_DB,
                                    RedisConstant.MS_FEEDBACK_KEFUBYAREA,
                                    condition.getArea().getId().toString(),
                                    1l
                            );
                        }
                    }
                    //未处理
                    if (orgReplyCustomer == 0) {
                        if (assinedCustomer) { //客服有分配的客户,by area
                            redisUtils.hIncrBy(
                                    RedisConstant.RedisDBType.REDIS_MS_DB,
                                    RedisConstant.MS_FEEDBACK_PENDING_KEFUBYCUSTOMER,
                                    cid.toString(),
                                    1l
                            );
                        } else {//客服无分配的客户,by customer
                            redisUtils.hIncrBy(
                                    RedisConstant.RedisDBType.REDIS_MS_DB,
                                    RedisConstant.MS_FEEDBACK_PENDING_KEFUBYAREA,
                                    condition.getArea().getId().toString(),
                                    1l
                            );
                        }
                    }
                    return;
                }

                //客服发送->客户 and 业务,by customer id
                if (orgReplyFlag != 1) {
                    //total +1
                    //createby +1
                    redisUtils.hIncrByFields(
                            RedisConstant.RedisDBType.REDIS_MS_DB,
                            String.format(RedisConstant.MS_FEEDBACK_CUSTOMER, cid),
                            new String[]{"total", condition.getCreateBy().getId().toString()},
                            1l
                    );
                }
                //未处理
                if (orgReplyKefu == 0) {
                    //total +1
                    //createby +1
                    redisUtils.hIncrByFields(
                            RedisConstant.RedisDBType.REDIS_MS_DB,
                            String.format(RedisConstant.MS_FEEDBACK_PENDING_CUSTOMER, cid),
                            new String[]{"total", condition.getCreateBy().getId().toString()},
                            1l
                    );
                }

            } catch (Exception e) {
                LogUtils.saveLog("统计问题反馈", "FeedbackService.save", String.format("order id:%s", condition.getOrderId()), e, feedback.getCreateBy());
            }
			/* mq
			try{
				User user = feedback.getCreateBy();
				MQWebSocketMessage.WebSocketMessage message = MQWebSocketMessage.WebSocketMessage.newBuilder()
						.setCreateId(user.getId())
						.setCustomerId(condition.getCustomer().getId())
						.setAreaId(condition.getArea().getId())
						.setOrderId(condition.getOrderId())
						.setKefuId(condition.getKefu()==null?0l:condition.getKefu().getId())
						.setSalesId(condition.getCustomer().getSales().getId())
						.setOrderNo(condition.getOrderNo())
						.setQuarter(condition.getQuarter())
						.setNoticeType(Notice.NOTICE_TYPE_FEEDBACK)
						.setTitle("问题反馈")
						.setContext(String.format("【问题反馈】工单号：%s,新的问题反馈",condition.getOrderNo()))
						.setTriggerBy(MQWebSocketMessage.User.newBuilder()
								.setId(user.getId())
								.setName(user.getName())
								.setUserType(user.getUserType())
								.setCompanyId(user.getCompanyId())
								.setOfficeId(user.getOfficeId())
								.build()
						)
						.setTriggerDate(feedback.getCreateDate().getTime())
						.build();
				wsMessageSender.send(message);

			}catch (Exception e){
				log.error("[FeedbackService.save]send message",e);
			}
			*/
        }
    }

    // Item

    /**
     * 保存回复(文本)
     *
     * @param item
     */
    @Transactional(readOnly = false)
    public void addItem(FeedbackItem item) throws Exception {
        if (item.getFeedbackId() == null) {
            throw new RuntimeException("问题反馈主键为空");
        }
        if (StringUtils.isBlank(item.getQuarter())) {
            throw new RuntimeException("数据库分片为空");
        }
        Feedback feedback = get(item.getFeedbackId(), item.getQuarter());
        Order order = null;
        try {
            order = orderService.getOrderById(feedback.getOrder().getId(), feedback.getQuarter(), OrderUtils.OrderDataLevel.CONDITION, true);
        } catch (Exception e) {
            LogUtils.saveLog("读取订单错误", "FeedbackService.addItem", feedback.getOrder().getId().toString(), e, feedback.getCreateBy());
        }
        if (order == null || order.getOrderCondition() == null) {
            throw new RuntimeException("读取订单信息失败");
        }
        User user = item.getCreateBy();
        OrderCondition condition = order.getOrderCondition();
        int orgReplyFlag = condition.getReplyFlag();
        int orgReplyCustomer = condition.getReplyFlagCustomer();
        int orgReplyKefu = condition.getReplyFlagKefu();

        feedbackDao.incrNextFloor(item.getFeedbackId(), item.getQuarter());
        Integer floor = feedbackDao.getNextFloor(item.getFeedbackId(), item.getQuarter());
        item.setQuarter(feedback.getQuarter());
        item.setFloor(floor);
        item.setContentType(0);
        Integer replayFlag = 1;
        if (user.isCustomer() || user.isSaleman()) {
            item.setUserType(0);//customer
            replayFlag = 2;
        } else {
            item.setUserType(1);//kkl
        }
        feedbackDao.insertFeedbackItem(item);

        //feedback
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("id", item.getFeedbackId());
        map.put("replayFlag", replayFlag);
        map.put("updateBy", user);
        map.put("updateDate", item.getCreateDate());
        //客户添加新回复，自动变更为未关闭状态
        if (item.getCreateBy().isCustomer()) {
            map.put("closeFlag", 0);
            map.put("closeDate", null);
            map.put("closeBy", 0);
        }
        feedbackDao.updateFeedback(map);

        //order
        map.clear();
        map.put("quarter", feedback.getQuarter());
        map.put("orderId", feedback.getOrder().getId());
        map.put("feedbackFlag", 1);
        map.put("feedbackTitle", StringUtils.substring(item.getRemarks(), 0, 20));//2017-09-23
        map.put("feedbackDate", item.getCreateDate());
        map.put("replyFlag", replayFlag);
        if (user.isKefu()) {
            map.put("replyFlagKefu", 1);//客服回复问题反馈，订单标记异常，需要客户手动处理异常
        } else if (user.isCustomer() || user.isSaleman()) {
            map.put("replyFlagCustomer", 1);//客户回复问题反馈，订单标记异常，需要客服手动处理异常
        }
        map.put("updateDate", item.getCreateDate());
        map.put("updateBy", item.getCreateBy());
        orderDao.updateCondition(map);

		/*String key = String.format(RedisConstant.SD_ORDER,feedback.getOrder().getId());
			if(redisUtils.exists(RedisConstant.RedisDBType.REDIS_SD_DB,key)) {
				//cache
				map.remove("orderId");
				map.remove("updateDate");
				map.remove("updateBy");
				map.put("syncDate",new Date().getTime());
				try {
					redisUtils.hmSetAll(RedisConstant.RedisDBType.REDIS_SD_DB, key, map, 0l);
				}catch (Exception e){
					try{
						redisUtils.remove(RedisConstant.RedisDBType.REDIS_SD_DB,key);
					}catch (Exception e1){
						log.error("[FeedbackService.addItem] feedbackId:{}",item.getFeedbackId(),e1);
					}
				}
		}*/
        //调用公共缓存
        OrderCacheParam.Builder builder = new OrderCacheParam.Builder();
        builder.setOpType(OrderCacheOpType.UPDATE)
                .setOrderId(feedback.getOrder().getId())
                .setQuarter(feedback.getQuarter())
                .setFeedbackFlag(1)
                .setFeedbackTitle(StringUtils.substring(item.getRemarks(), 0, 20))
                .setFeedbackDate(item.getCreateDate())
                .setReplyFlag(replayFlag)
                .setSyncDate(new Date().getTime())
                .setExpireSeconds(0L);
        if (user.isKefu()) {
            builder.setReplyFlagKefu(1);//客服回复问题反馈，订单标记异常，需要客户手动处理异常
        } else if (user.isCustomer() || user.isSaleman()) {
            builder.setReplyFlagCustomer(1);//客户回复问题反馈，订单标记异常，需要客服手动处理异常
        }
        OrderCacheUtils.update(builder.build());

        // 提醒数量统计
        //message
        try {

            Long cid = condition.getCustomer().getId();
            if (user.isCustomer() || user.isSaleman()) {
                User kefu = condition.getKefu();
                if (kefu == null) {
                    return;
                }
                Boolean assinedCustomer = customerService.checkAssignedCustomer(kefu.getId(), StringUtils.toDouble(cid));
                //客户/业务发送->客服,如客服还未读(replyFlag=2)，不重复累计
                if (orgReplyFlag != 2) {
                    if (assinedCustomer == true) { //客服已分配了客户,by customer
                        redisUtils.hIncrBy(
                                RedisConstant.RedisDBType.REDIS_MS_DB,
                                RedisConstant.MS_FEEDBACK_KEFUBYCUSTOMER,
                                cid.toString(),
                                1l
                        );
                    } else {//客服无分配的客户,by area
                        redisUtils.hIncrBy(
                                RedisConstant.RedisDBType.REDIS_MS_DB,
                                RedisConstant.MS_FEEDBACK_KEFUBYAREA,
                                condition.getArea().getId().toString(),
                                1l
                        );
                    }

                    //未处理
                    if (orgReplyCustomer == 0) {
                        if (assinedCustomer) { //客服有分配的客户,by customer
                            redisUtils.hIncrBy(
                                    RedisConstant.RedisDBType.REDIS_MS_DB,
                                    RedisConstant.MS_FEEDBACK_PENDING_KEFUBYCUSTOMER,
                                    cid.toString(),
                                    1
                            );
                        } else {//客服无分配的客户,by area
                            redisUtils.hIncrBy(
                                    RedisConstant.RedisDBType.REDIS_MS_DB,
                                    RedisConstant.MS_FEEDBACK_PENDING_KEFUBYAREA,
                                    condition.getArea().getId().toString(),
                                    1
                            );
                        }
                    }
                }
            } else if (user.isKefu()) {
                //客服发送->客户,by customer id,如客服还未读(replyFlag=1)，不重复累计
                //未读
                if (orgReplyFlag != 1) {
                    //total +1
                    //createby +1
                    redisUtils.hIncrByFields(
                            RedisConstant.RedisDBType.REDIS_MS_DB,
                            String.format(RedisConstant.MS_FEEDBACK_CUSTOMER, cid),
                            new String[]{"total", condition.getCreateBy().getId().toString()},
                            1
                    );

                    //未处理
                    if (orgReplyKefu == 0) {
                        //total +1
                        //createby +1
                        redisUtils.hIncrByFields(
                                RedisConstant.RedisDBType.REDIS_MS_DB,
                                String.format(RedisConstant.MS_FEEDBACK_PENDING_CUSTOMER, cid),
                                new String[]{"total", condition.getCreateBy().getId().toString()},
                                1
                        );
                    }
                }
            }
        } catch (Exception e) {
            LogUtils.saveLog("统计问题反馈", "FeedbackService.save", String.format("order id:%s", order.getId()), e, feedback.getCreateBy());
        }

		/* mq
		try{
			User user = UserUtils.getUser();
			Order order = orderService.getOrderById(feedback.getOrder().getId(),feedback.getQuarter(), OrderUtils.OrderDataLevel.CONDITION,true);
			OrderCondition condition = order.getOrderCondition();
			MQWebSocketMessage.WebSocketMessage message = MQWebSocketMessage.WebSocketMessage.newBuilder()
					.setCreateId(condition.getCreateBy().getId())
					.setCustomerId(condition.getCustomer().getId())
					.setAreaId(condition.getArea().getId())
					.setOrderId(condition.getOrderId())
					.setKefuId(condition.getKefu()==null?0l:condition.getKefu().getId())
					.setSalesId(condition.getCustomer().getSales().getId())
					.setOrderNo(condition.getOrderNo())
					.setQuarter(condition.getQuarter())
					.setNoticeType(Notice.NOTICE_TYPE_FEEDBACK)
					.setTitle("问题反馈")
					.setContext(String.format("【问题反馈】工单号：%s,回复新的内容",condition.getOrderNo()))
					.setTriggerBy(MQWebSocketMessage.User.newBuilder()
							.setId(user.getId())
							.setName(user.getName())
							.setUserType(user.getUserType())
							.setCompanyId(user.getCompanyId())
							.setOfficeId(user.getOfficeId())
							.build()
					)
					.setTriggerDate(item.getCreateDate().getTime())
					.build();
			wsMessageSender.send(message);
		}catch (Exception e){
			log.error("[FeedbackService.addItem]send message",e);
		}
		*/

    }

    /**
     * 保存回复(图片)
     * @param item
     * @return
     */
    @Transactional(readOnly = false)
    public void addImageItem(FeedbackItem item) throws Exception {
        if (item.getFeedbackId() == null) {
            throw new RuntimeException("问题反馈主键为空");
        }
        if (StringUtils.isBlank(item.getQuarter())) {
            throw new RuntimeException("数据库分片为空");
        }
        Feedback feedback = get(item.getFeedbackId(), item.getQuarter());
        Order order = null;
        try {
            order = orderService.getOrderById(feedback.getOrder().getId(), feedback.getQuarter(), OrderUtils.OrderDataLevel.CONDITION, true);
        } catch (Exception e) {
            LogUtils.saveLog("读取订单错误", "FeedbackService.addItem", feedback.getOrder().getId().toString(), e, feedback.getCreateBy());
        }
        if (order == null || order.getOrderCondition() == null) {
            throw new RuntimeException("读取订单信息失败");
        }
        OrderCondition condition = order.getOrderCondition();
        int orgReplyFlag = condition.getReplyFlag();
        int orgReplyCustomer = condition.getReplyFlagCustomer();
        int orgReplyKefu = condition.getReplyFlagKefu();

        feedbackDao.incrNextFloor(item.getFeedbackId(), item.getQuarter());
        Integer floor = feedbackDao.getNextFloor(item.getFeedbackId(), item.getQuarter());
        item.setQuarter(feedback.getQuarter());
        item.setFloor(floor);
        item.setContentType(1);//图片
        Integer replayFlag = 1;
        User user = item.getCreateBy();
        if (user.isCustomer() || user.isSaleman()) {
            item.setUserType(0);//customer
            replayFlag = 2;
        } else {
            item.setUserType(1);//kkl
        }

        feedbackDao.insertFeedbackItem(item);

        //feedback
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("quarter", feedback.getQuarter());
        map.put("id", item.getFeedbackId());
        map.put("replayFlag", replayFlag);
        map.put("updateBy", item.getCreateBy());
        map.put("updateDate", item.getCreateDate());
        //客户添加新回复，自动变更为未关闭状态
        if (item.getCreateBy().isCustomer()) {
            map.put("closeFlag", 0);
            map.put("closeDate", null);
        }
        feedbackDao.updateFeedback(map);
        //递增已上传图片数
        feedbackDao.incrAttachmentCount(item.getFeedbackId(), feedback.getQuarter());

        //order
        map.clear();
        map.put("quarter", feedback.getQuarter());
        map.put("orderId", feedback.getOrder().getId());
        map.put("feedbackFlag", 1);
        map.put("feedbackTitle", "上传了新图片");//2017-09-23
        map.put("feedbackDate", item.getCreateDate());
        map.put("replyFlag", replayFlag);
        if (item.getCreateBy().isKefu()) {
            map.put("replyFlagKefu", 1);//客服回复问题反馈，订单标记异常，需要客户手动处理异常
        } else if (item.getCreateBy().isCustomer() || item.getCreateBy().isSaleman()) {
            map.put("replyFlagCustomer", 1);//客户回复问题反馈，订单标记异常，需要客服手动处理异常
        }
        map.put("updateDate", item.getCreateDate());
        map.put("updateBy", item.getCreateBy());
        orderDao.updateCondition(map);

	/*	String key = String.format(RedisConstant.SD_ORDER,feedback.getOrder().getId());
		if(redisUtils.exists(RedisConstant.RedisDBType.REDIS_SD_DB,key)) {
			//cache
			map.remove("orderId");
			map.remove("updateDate");
			map.remove("updateBy");
			map.put("syncDate",new Date().getTime());
			try {
				redisUtils.hmSetAll(RedisConstant.RedisDBType.REDIS_SD_DB, key, map, 0l);
			}catch (Exception e){
				try{
					redisUtils.remove(RedisConstant.RedisDBType.REDIS_SD_DB,key);
				}catch (Exception e1){
					log.error("[FeedbackService.addImageItem] feedbackId:",item.getFeedbackId(),e1);
				}
			}
		}*/

        //调用公共缓存
        OrderCacheParam.Builder builder = new OrderCacheParam.Builder();
        builder.setOpType(OrderCacheOpType.UPDATE)
                .setOrderId(feedback.getOrder().getId())
                .setQuarter(feedback.getQuarter())
                .setFeedbackFlag(1)
                .setFeedbackTitle("上传了新图片")
                .setFeedbackDate(item.getCreateDate())
                .setReplyFlag(replayFlag)
                .setSyncDate(new Date().getTime())
                .setExpireSeconds(0L);
        if (user.isKefu()) {
            builder.setReplyFlagKefu(1);//客服回复问题反馈，订单标记异常，需要客户手动处理异常
        } else if (user.isCustomer() || user.isSaleman()) {
            builder.setReplyFlagCustomer(1);//客户回复问题反馈，订单标记异常，需要客服手动处理异常
        }
        OrderCacheUtils.update(builder.build());


        // 提醒数量统计
        //message
        try {
            Long cid = condition.getCustomer().getId();
            if (user.isCustomer() || user.isSaleman()) {
                User kefu = condition.getKefu();
                if (kefu == null) {
                    return;
                }
                Boolean assinedCustomer = customerService.checkAssignedCustomer(kefu.getId(), StringUtils.toDouble(cid));
                //客户/业务发送->客服,如客服还未读(replyFlag=2)，不重复累计
                if (orgReplyFlag != 2) {
                    if (assinedCustomer == true) { //客服已分配了客户,by customer
                        redisUtils.hIncrBy(
                                RedisConstant.RedisDBType.REDIS_MS_DB,
                                RedisConstant.MS_FEEDBACK_KEFUBYCUSTOMER,
                                cid.toString(),
                                1l
                        );
                    } else {//客服无分配的客户,by area
                        redisUtils.hIncrBy(
                                RedisConstant.RedisDBType.REDIS_MS_DB,
                                RedisConstant.MS_FEEDBACK_KEFUBYAREA,
                                condition.getArea().getId().toString(),
                                1l
                        );
                    }

                    //未处理
                    if (orgReplyCustomer == 0) {
                        if (assinedCustomer) { //客服有分配的客户,by customer
                            redisUtils.hIncrBy(
                                    RedisConstant.RedisDBType.REDIS_MS_DB,
                                    RedisConstant.MS_FEEDBACK_PENDING_KEFUBYCUSTOMER,
                                    cid.toString(),
                                    1l
                            );
                        } else {//客服无分配的客户,by area
                            redisUtils.hIncrBy(
                                    RedisConstant.RedisDBType.REDIS_MS_DB,
                                    RedisConstant.MS_FEEDBACK_PENDING_KEFUBYAREA,
                                    condition.getArea().getId().toString(),
                                    1l
                            );
                        }
                    }
                }
            } else if (user.isKefu()) {
                //客服发送->客户,by customer id,如客服还未读(replyFlag=1)，不重复累计
                //未读
                if (orgReplyFlag != 1) {
                    //total +1
                    //createby +1
                    redisUtils.hIncrByFields(
                            RedisConstant.RedisDBType.REDIS_MS_DB,
                            String.format(RedisConstant.MS_FEEDBACK_CUSTOMER, cid),
                            new String[]{"total", condition.getCreateBy().getId().toString()},
                            1l
                    );

                    //未处理
                    if (orgReplyKefu == 0) {
                        //total +1
                        //createby +1
                        redisUtils.hIncrByFields(
                                RedisConstant.RedisDBType.REDIS_MS_DB,
                                String.format(RedisConstant.MS_FEEDBACK_PENDING_CUSTOMER, cid),
                                new String[]{"total", condition.getCreateBy().getId().toString()},
                                1l
                        );
                    }
                }
            }
        } catch (Exception e) {
            LogUtils.saveLog("统计问题反馈", "FeedbackService.save", String.format("order id:%s", order.getId()), e, feedback.getCreateBy());
        }
    }

    /**
     * 标记反馈状态为：已读(只更新redis)
     * @param feedback
     * @return
     */
    @Transactional(readOnly = false)
    public void read(Feedback feedback, OrderCondition condition) {

        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("quarter", feedback.getQuarter());
        map.put("orderId", feedback.getOrder().getId());
        map.put("replyFlag", 0);
        map.put("updateBy", feedback.getUpdateBy());
        map.put("updateDate", feedback.getUpdateDate());
        orderDao.updateCondition(map);

        map.remove("orderId");
        map.put("id", feedback.getId());
        feedbackDao.updateFeedback(map);

        //cache
		/*String key = new String("");
		key = String.format(RedisConstant.SD_ORDER,feedback.getOrder().getId());
		if(redisUtils.exists(RedisConstant.RedisDBType.REDIS_SD_DB,key)) {
			try {
				redisUtils.hmSet(RedisConstant.RedisDBType.REDIS_SD_DB, key, "replyFlag", 0, 0l);
				redisUtils.hmSet(RedisConstant.RedisDBType.REDIS_SD_DB,key,"syncDate",new Date().getTime(),0l);
			}catch (Exception e){
				try{
					redisUtils.remove(RedisConstant.RedisDBType.REDIS_SD_DB,key);
				}catch (Exception e1){
					log.error("[FeedbackService.read] id:{}",feedback.getId(),e1);
				}
			}
		}*/

        //调用公共缓存
        OrderCacheParam.Builder builder = new OrderCacheParam.Builder();
        builder.setOpType(OrderCacheOpType.UPDATE)
                .setOrderId(feedback.getOrder().getId())
                .setReplyFlag(0)
                .setSyncDate(new Date().getTime())
                .setExpireSeconds(0L);
        OrderCacheUtils.update(builder.build());


        // 更新提醒统计
        // message
        try {
            Order order = orderService.getOrderById(feedback.getOrder().getId(), feedback.getQuarter(), OrderUtils.OrderDataLevel.CONDITION, true);
            if (order != null && order.getOrderCondition() != null) {
                User user = feedback.getUpdateBy();
                OrderCondition condition1 = order.getOrderCondition();
                Long cid = condition1.getCustomer().getId();
                if (user.isCustomer() || user.isSaleman()) {
                    //客户或业务员读，递减客户未读数量
                    //total -1
                    //createby -1
                    redisUtils.hIncrByFields(
                            RedisConstant.RedisDBType.REDIS_MS_DB,
                            String.format(RedisConstant.MS_FEEDBACK_CUSTOMER, cid),
                            new String[]{"total", condition1.getCreateBy().getId().toString()},
                            -1
                    );
                } else {
                    //客服读，递减客服未读数量
                    User kefu = condition.getKefu();
                    if (kefu == null) {
                        return;
                    }
                    if (!user.isKefu() && !user.isKefuLeader()) {
                        return;
                    }
                    //Boolean assinedCustomer = customerService.checkAssignedCustomer(user.getId(),StringUtils.toDouble(cid));
                    //当前帐号是客服或客服主管
                    if (user.getCustomerIds() != null && user.getCustomerIds().contains(cid)) {//by customer
                        redisUtils.hIncrBy(
                                RedisConstant.RedisDBType.REDIS_MS_DB,
                                RedisConstant.MS_FEEDBACK_KEFUBYCUSTOMER,//key
                                cid.toString(), //field
                                -1l //-1
                        );
                    } else { // by area
                        redisUtils.hIncrBy(
                                RedisConstant.RedisDBType.REDIS_MS_DB,
                                RedisConstant.MS_FEEDBACK_KEFUBYAREA, //key
                                condition1.getArea().getId().toString(),//field
                                -1l // -1
                        );
                    }
                }
            }
        } catch (Exception e) {
            LogUtils.saveLog("统计问题反馈错误", "FeedbackService.read", feedback.getOrder().getId().toString(), e, feedback.getCreateBy());
        }

		/* mq
		String uid = new String("");
		if(feedback.getUpdateBy().isCustomer()){
			//客户
			uid = condition.getCreateBy().getId().toString();
			key = String.format(RedisConstant.WS_FEEDBACK_CUSTOMER,feedback.getCustomer().getId());
			//子帐号-1
			redisUtils.hDecr(RedisConstant.RedisDBType.REDIS_SD_DB,key,uid);
			//主帐号-1
			List<String> ids = customerService.getAccountMasters(feedback.getCustomer().getId());
			for (String id:ids) {
				if(id.equalsIgnoreCase(uid)){
					continue;
				}
				redisUtils.hDecr(RedisConstant.RedisDBType.REDIS_SD_DB,key,id);
			}
		}else{
			key = RedisConstant.WS_FEEDBACK_KEFU;
			//客服
			//客服主管
			List<String> leaders = UserUtils.getOfficeLeaderIds(feedback.getUpdateBy().getOffice().getId(),2);
			if(Integer.parseInt(condition.getStatus().getValue())== Order.ORDER_STATUS_APPROVED) {
				//待接单,所有符合的客服-1
				List<Long> users = UserUtils.getKefuIdListByOrder(feedback.getCustomer().getId(),condition.getArea().getId());
				for (Long id: users) {
					redisUtils.hDecr(RedisConstant.RedisDBType.REDIS_SD_DB, key, id.toString());
				}
				//客服主管
				if(leaders !=null &&leaders.size()>0){
					for (String id:leaders) {
						if(users.contains(Long.valueOf(id))){
							continue;
						}
						redisUtils.hDecr(RedisConstant.RedisDBType.REDIS_SD_DB, key, id);
					}
				}
			}else{
				//已接单
				uid = condition.getKefu().getId().toString();
				redisUtils.hDecr(RedisConstant.RedisDBType.REDIS_SD_DB, key, uid);
				//客服主管
				if(leaders !=null &&leaders.size()>0){
					for (String id:leaders) {
						if(id.equalsIgnoreCase(uid)){
							continue;
						}
						redisUtils.hDecr(RedisConstant.RedisDBType.REDIS_SD_DB, key, id);
					}
				}
			}
			//业务
			key = RedisConstant.WS_FEEDBACK_SALES;
			uid = condition.getCustomer().getSales().getId().toString();
			redisUtils.hDecr(RedisConstant.RedisDBType.REDIS_SD_DB, key, uid);
			//业务主管
			User u = UserUtils.get(condition.getCustomer().getSales().getId());
			leaders = UserUtils.getOfficeLeaderIds(u.getOffice().getId(),u.getUserType());
			if(leaders !=null &&leaders.size()>0){
				for (String id:leaders) {
					if(uid.equalsIgnoreCase(id)){
						continue;
					}
					redisUtils.hDecr(RedisConstant.RedisDBType.REDIS_SD_DB, key, id);
				}
			}
		}*/
    }

    /**
     * 关闭投诉
     *
     * @param item
     */
    @Transactional(readOnly = false)
    public void close(FeedbackItem item) {

        Feedback feedback = get(item.getFeedbackId(), item.getQuarter());
        if (feedback.getCloseFlag() == 1) {
            throw new RuntimeException("该投诉已关闭。");
        }
        Date date = new Date();
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("quarter", feedback.getQuarter());
        map.put("id", item.getFeedbackId());
        map.put("closeFlag", 1);
        map.put("closeDate", item.getCreateDate());
        map.put("updateBy", item.getCreateBy());
        map.put("updateDate", item.getCreateDate());
        feedbackDao.updateFeedback(map);

        //更新订单的feedback_flag
        map.clear();
        map.put("quarter", feedback.getQuarter());
        map.put("orderId", feedback.getOrder().getId());
        map.put("feedbackFlag", 2);//close
        map.put("replyFlag", 0);
        map.put("replyFlagKefu", 0);
        map.put("replyFlagCustomer", 0);
        map.put("updateDate", item.getCreateDate());
        map.put("updateBy", item.getCreateBy());
        orderDao.updateCondition(map);

        //item
        feedbackDao.incrNextFloor(feedback.getId(), feedback.getQuarter());
        Integer floor = feedbackDao.getNextFloor(feedback.getId(), feedback.getQuarter());
        item.setFloor(floor);
        if (item.getCreateBy().isCustomer()) {
            item.setUserType(0);//customer
        } else {
            item.setUserType(1);//kkl
        }
        feedbackDao.insertFeedbackItem(item);
        //cache
		/*String key = String.format(RedisConstant.SD_ORDER,feedback.getOrder().getId());
		if(redisUtils.exists(RedisConstant.RedisDBType.REDIS_SD_DB,key)) {
			map.remove("orderId");
			map.remove("updateDate");
			map.remove("updateBy");
			map.put("syncDate",new Date().getTime());
			try{
				redisUtils.hmSetAll(RedisConstant.RedisDBType.REDIS_SD_DB, String.format(RedisConstant.SD_ORDER, feedback.getOrder().getId()), map, 0l);
			}catch (Exception e){
				try{
					redisUtils.remove(RedisConstant.RedisDBType.REDIS_SD_DB,key);
				}catch (Exception e1){
					log.error("[FeedbackService.close] feedbackId:{}",item.getFeedbackId(),e1);
				}
			}
		}*/
        //调用公共缓存
        OrderCacheParam.Builder builder = new OrderCacheParam.Builder();
        builder.setOpType(OrderCacheOpType.UPDATE)
                .setOrderId(feedback.getOrder().getId())
                .setReplyFlag(0)
                .setReplyFlagKefu(0)
                .setReplyFlagCustomer(0)
                .setFeedbackFlag(2)
                .setQuarter(feedback.getQuarter())
                .setSyncDate(new Date().getTime())
                .setExpireSeconds(0L);
        OrderCacheUtils.update(builder.build());
        //TODO：问题反馈统计数据
    }

    @Transactional(readOnly = false)
    public void updateFeedback(HashMap<String, Object> map) {
        feedbackDao.updateFeedback(map);
    }

    //	@Transactional(readOnly = false)
    public void markedFeedbackNew(Integer noticeType, User user) {
        if (user == null) {
            return;
        }

        Date now = new Date();
        Date beginDate = DateUtils.addMonth(now, -3);
        List<String> quarters = QuarterUtils.getQuarters(beginDate, now);

        HashMap<String, Object> maps = Maps.newHashMap();
        maps.put("noticeType", noticeType);
        maps.put("userType", user.getUserType());
        if (user.isCustomer()) {
            maps.put("customerId", user.getCustomerAccountProfile().getCustomer().getId());
            maps.put("createBy", user.getId());
            for (String quarter : quarters) {
                maps.put("quarter", quarter);
                feedbackDao.updateReadedNew(maps);
            }
        } else if (user.isKefu()) {
            maps.put("kefu", user.getId());
            if (user.getCustomerIds() != null && user.getCustomerIds().size() > 0) {
                for (String quarter : quarters) {
                    maps.put("quarter", quarter);
                    for (Long cId : user.getCustomerIds()) {
                        maps.put("customerId", cId);
                        feedbackDao.updateReadedNew(maps);
                    }
                }
            } else {
                List<Area> areas = areaService.getAreaListOfKefu(user.getId());
                for (String quarter : quarters) {
                    maps.put("quarter", quarter);
                    for (Area area : areas) {
                        maps.put("areaId", area.getId());
                        feedbackDao.updateReadedNew(maps);
                    }
                }
            }
        } else if (user.isSaleman()) {
            maps.put("salesId", user.getId());
            for (String quarter : quarters) {
                maps.put("quarter", quarter);
                for (Long cId : user.getCustomerIds()) {
                    maps.put("customerId", cId);
                    feedbackDao.updateReadedNew(maps);
                }
            }
        }
    }
}
