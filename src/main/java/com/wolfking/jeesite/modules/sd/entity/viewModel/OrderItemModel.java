package com.wolfking.jeesite.modules.sd.entity.viewModel;


import com.google.gson.annotations.JsonAdapter;
import com.kkl.kklplus.entity.common.NameValuePair;
import com.wolfking.jeesite.common.config.redis.GsonIgnore;
import com.wolfking.jeesite.modules.md.entity.UrgentLevel;
import com.wolfking.jeesite.modules.sd.entity.OrderItem;
import com.wolfking.jeesite.modules.sd.utils.OrderItemNewAdapter;
import lombok.Data;
import org.assertj.core.util.Lists;

import java.util.List;


/**
 * 订单项目 视图数据模型
 * @author Ryan Lu
 * @version 2014-09-24
 */
@Data
public class OrderItemModel extends OrderItem {

	private static final long serialVersionUID = 1L;

	//新建订单时使用
	private String tmpId = "";//新增Item临时id,删除时使用
	private Long customerId;//客户id，当临时订单缓存失效时，基于这个id补全客户信息
	private String flag="";//订单管理时标识 del:删除 add:新增 lastadd:最后添加的item，且没有读取到前台

	@GsonIgnore
	private UrgentLevel urgentLevel; //加急等级
	private double customerUrgentCharge = 0.00;//加急费(应收)
	private double engineerUrgentCharge = 0.00;//加急费(应付)

	private double balance = 0.00; //余额
	private double credit = 0.00; //信用额度
	//型号，订单修改时用 2018/12/22
	private List models = null;
	private List brands = null;
	private List<String> b2bProductCodes = null;

	//灯饰下单 2020-03-18
	private String action = "new";

	//是否有品牌，B2B转单
	private Boolean hasBrand = false;
	//下单项中的品牌对应的品牌ID, B2B转单
	private Long brandId = 0L;

	public OrderItemModel() {}

	public OrderItemModel(Long id) {super(id);}

}