package com.wolfking.jeesite.modules.mq.dao;

import com.wolfking.jeesite.common.persistence.LongIDCrudDao;
import com.wolfking.jeesite.modules.mq.entity.OrderAutoComplete;
import com.wolfking.jeesite.modules.mq.entity.OrderCharge;
import org.apache.ibatis.annotations.Mapper;

/**
 * APP确认完成，自动客评及对账队列
 * Created by Ryan on 2017/12/05.
 */
@Mapper
public interface OrderAutoCompleteDao extends LongIDCrudDao<OrderAutoComplete> {
}
