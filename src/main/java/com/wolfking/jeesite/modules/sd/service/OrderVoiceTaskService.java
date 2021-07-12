/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.wolfking.jeesite.modules.sd.service;

import com.google.common.collect.Maps;
import com.wolfking.jeesite.common.config.redis.GsonRedisSerializer;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.service.LongIDBaseService;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.QuarterUtils;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.service.CustomerService;
import com.wolfking.jeesite.modules.md.utils.CustomerUtils;
import com.wolfking.jeesite.modules.sd.dao.OrderTaskDao;
import com.wolfking.jeesite.modules.sd.dao.OrderVoiceTaskDao;
import com.wolfking.jeesite.modules.sd.entity.OrderVoiceTask;
import com.wolfking.jeesite.modules.sd.entity.viewModel.RepeateOrderVM;
import com.wolfking.jeesite.modules.sys.entity.Notice;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.math.LongRange;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 订单Job Service
 */
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Slf4j
public class OrderVoiceTaskService extends LongIDBaseService {

    /**
     * 持久层对象
     */
    @Resource
    protected OrderVoiceTaskDao dao;

    /**
     * 按订单读取完整智能回访记录
     * @param quarter
     * @param orderId
     * @return
     */
    public OrderVoiceTask getByOrderId(String quarter,Long orderId){
        return dao.getByOrderId(quarter,orderId);
    }

    /**
     * 按订单读取智能回访记录的基本信息
     * @param quarter
     * @param orderId
     * @return
     */
    public OrderVoiceTask getBaseInfoByOrderId(String quarter,Long orderId){
        return dao.getBaseInfoByOrderId(quarter,orderId);
    }

    /**
     * 读取智能回访结果
     * @param quarter
     * @param orderId
     * @return
     */
    public Integer getVoiceTaskResult(String quarter,Long orderId){
        return dao.getVoiceTaskResult(quarter,orderId);
    }

    @Transactional(readOnly = false)
    public void insert(OrderVoiceTask entity){
        dao.insert(entity);
    }

    @Transactional(readOnly = false)
    public void update(OrderVoiceTask task){
        dao.update(task);
    }

    @Transactional(readOnly = false)
    public void cancel(String quarter,Long orderId,Long updateDate){
        dao.cancel(quarter,orderId,updateDate);
    }

}
