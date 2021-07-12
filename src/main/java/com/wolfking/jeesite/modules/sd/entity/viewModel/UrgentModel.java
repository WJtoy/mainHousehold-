package com.wolfking.jeesite.modules.sd.entity.viewModel;

import com.google.common.collect.Lists;
import com.google.gson.annotations.JsonAdapter;
import com.wolfking.jeesite.common.config.redis.GsonIgnore;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.entity.UrgentLevel;
import com.wolfking.jeesite.modules.md.utils.AreaSimpleAdapter;
import com.wolfking.jeesite.modules.md.utils.CreatOrderCustomerAdapter;
import com.wolfking.jeesite.modules.md.utils.UrgentLevelSimpleAdapter;
import com.wolfking.jeesite.modules.sd.entity.Order;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.entity.adapter.DictSimpleAdapter;
import com.wolfking.jeesite.modules.sys.entity.adapter.UserSimpleAdapter;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 加急视图模型
 * 
 * @author Ryan
 */
@Data
@NoArgsConstructor
public class UrgentModel implements Serializable{

	// Fields
	private Long areaId;	//区域id
	private Long customerId;//客户id
	private Long orderId;		// id
	private String orderNo = "";//订单号
	private String quarter = "";//数据库分片
	private String remarks = "";//说明
	private UrgentLevel urgentLevel;//选择的等级
	private List<UrgentLevel> urgentLevels = Lists.newArrayList();//可选的等级列表
	private User createBy = null;
	private Double chargeIn = 0.0; //应收
	private Double chargeOut = 0.0;//应付
}