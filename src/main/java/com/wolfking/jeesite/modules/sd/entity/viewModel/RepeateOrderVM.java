package com.wolfking.jeesite.modules.sd.entity.viewModel;

import com.google.common.collect.Lists;
import com.google.gson.annotations.JsonAdapter;
import com.wolfking.jeesite.common.config.redis.GsonIgnore;
import com.wolfking.jeesite.common.mapper.adapters.DateTimeAdapter;
import com.wolfking.jeesite.common.persistence.LongIDDataEntity;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.utils.AreaSimpleAdapter;
import com.wolfking.jeesite.modules.md.utils.CustomerSimpleAdapter;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.entity.adapter.UserSimpleAdapter;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

/**
 * 重单检查时缓存在redis中订单信息
 * @author Ryan
 * @date 2018-10-20
 */
@Data
@NoArgsConstructor
public class RepeateOrderVM
{
	private long customerId;
	private String phone = "";//手机号
	private String orderNo = "";//工单号
	private long orderId;
}