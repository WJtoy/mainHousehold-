package com.wolfking.jeesite.modules.mq.service.servicepoint;

import com.google.common.collect.Maps;
import com.wolfking.jeesite.common.exception.OrderException;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.mq.dto.MQOrderServicePointMessage;
import com.wolfking.jeesite.modules.mq.entity.mapper.OrderServicePointMessageMapper;
import com.wolfking.jeesite.modules.sd.entity.OrderServicePoint;
import com.wolfking.jeesite.modules.sd.service.OrderServicePointService;
import com.wolfking.jeesite.modules.sys.entity.User;
import org.mapstruct.factory.Mappers;

import java.util.Map;

/**
 * 修改用户信息，包含电话，地址
 * @autor Ryan Lu
 * @date 2019/3/23 11:00
 */
public class UpdateUserInfoExecutor extends ServicePointExecutor {

    public UpdateUserInfoExecutor(OrderServicePointService service) {
        this.service = service;
    }

    // 处理方法，更新数据
    public void process(MQOrderServicePointMessage.ServicePointMessage message){

        if(!checkBaseParameters(message)){
            return;
        }

        if(message.getUserInfo() == null
                || ( StringUtils.isBlank(message.getUserInfo().getPhone())
                     && StringUtils.isBlank(message.getUserInfo().getAddress()) ) ) {
            //无跟新内容
            return;
        }
        Map<String,Object> params = Maps.newHashMapWithExpectedSize(5);
        params.put("orderId",message.getOrderId());
        params.put("quarter",message.getQuarter());
        if(StringUtils.isNotBlank(message.getUserInfo().getPhone())){
            params.put("servicePhone",StringUtils.left(StringUtils.trimToNull(message.getUserInfo().getPhone()),11));
        }
        if(StringUtils.isNotBlank(message.getUserInfo().getAddress())){
            params.put("serviceAddress",StringUtils.left(StringUtils.trimToNull(message.getUserInfo().getAddress()),11));
        }
        this.service.updateByOrder(params);
    }
}
