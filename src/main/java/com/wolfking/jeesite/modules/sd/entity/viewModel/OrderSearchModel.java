package com.wolfking.jeesite.modules.sd.entity.viewModel;

import com.google.common.collect.Lists;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.config.Global;
import com.wolfking.jeesite.common.persistence.IntegerRange;
import com.wolfking.jeesite.common.utils.QuarterUtils;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.entity.ServicePoint;
import com.wolfking.jeesite.modules.md.entity.UrgentLevel;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.ms.tmall.md.entity.B2bCustomerMap;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * 订单查询模型
 * 
 * @author Ryan
 */
@Data
@NoArgsConstructor
public class OrderSearchModel extends RegionSearchModel<OrderSearchModel> {

	private static final int ORDER_LENGTH = 14;

	private static final long serialVersionUID = 1L;

	private Boolean valid = true;

	private int dataSource = 0; //数据来源，如天猫，快可立下单
	private String parentBizOrderId = ""; //B2B第三方单号
	private String shopId = "";//店铺id
	private String quarter = "";//数据库分片
	private Long orderId;
	private String orderNo = "";// 订单号
	private Customer customer = new Customer(0l);//上游客户(订单来源)
	private Area area;// 用户区域(终端用户)
	// 用户
	private String userName = "";// 用户名
	private String phone1 = "";//上门手机号
	private String phone2 = "";//下单手机号
	private String servicePhone = "";// 用户实际联络电话
    private int isPhone = 0; //是否是合法的手机号码
	private int isPhone2 = 0; //下单手机号是否合法

	private String address = "";// 用户地址
	private String serviceAddress = "";// 实际上门地址

	private Dict status = new Dict();// 状态
	private IntegerRange statusRange = null;

	private User kefu;// 客服
	private Integer serviceTimes = 0;//上门次数
	//停滞
	private Integer pendingFlag = 0;// 异常标记 1:异常 2:正常,3:修改完成再次对帐,具体修改存入orderprocesslog
	private Dict pendingType;// 停滞原因
	private Date pendingTypeDate;// 开始停滞日期

	private Integer appointmentFlag = 0;//预约标记（查询）
	private Date appointmentDate;// 预约日期

	//反馈标记
	private Integer feedbackFlag = 0;// 反馈标记 0：无投诉 1：有投诉,但未关闭 2:投诉已关闭
	private String feedbackTitle = "";//反馈标题
	private Date feedbackDate;// 最后反馈日期

	private Integer appAbnormalyFlag = 0;// 1:app异常标记 app_abnormaly_flag
	private Integer isBackApprove = 0;//回收，安维接单后，2小时内未预约，也未和用户联系

	private Date closeDate;// 关闭日期 -->OrderStatus也有

	//配件
	private Integer partsFlag = 0;//配件标记
	private Integer returnPartsFlag = 0;//反件标记

	private Integer replyFlag = 0;//
	private Integer replyFlagKefu = 0;// 问题反馈异常标记： 1-客服回复新的标记订单为异常 ；0-厂家处理之后需要手动改为正常
	private Integer replyFlagCustomer = 0;// 1:厂家回复新的标记订单为异常 ，0:客服处理之后需要手动改为正常

	private Integer gradeFlag = 0;// 客评标记
	private Integer subStatus = 0;// 子状态 2019/01/24
	private String productIds = ""; //订单item中所有产品id(用逗号分隔),用于查询

	//查询时是否按客户负责的客户做子查询，如果sys_user_customer有记录，且customer=null，则加子查询
	//0:not in sys_user_customer
	//1: in sys_user_customer
	//2: 不关联 sys_user_customer
	private Integer subQueryUserCustomer = 0;
	private Integer subQueryUserArea = 0;
	private Integer subQueryComplain = 0;//是否子查询投诉表
	//客户类型 2019/12/11
	private Integer customerType;
	//可突击订单 0:非突击区域订单 1:突击区域订单
	private Integer rushType;

	//安维
	private ServicePoint servicePoint; //安维网点,servicepoint_id,对应原来 engineer:安维人(主账号)  -->OrderStatus也有
	private User engineer;// 安维主账号派单给安维子账号 (engineer_id,原来的sub engineer) -->OrderStatus也有

	private Integer serviceTypeId = 0;//订单服务类型,主要用于查询
	private Integer productId = 0;//产品
	private Integer hasSet = 0; //服务产品是否有套组
	private Integer autoGradeFlag = 0;//自动客评标记（终端用户发送短信评价）
	private Integer appSubmitService = 0;//安维提交上门服务

	private Integer userType = 0;//用户类型
	private Integer subUserType = 0;//子账号类型
	private String searchType;//查询类型

	private Date beginDate;//下单开始日期
	private Date endDate;//下单结束日期

	private Date acceptBeginDate;//接单开始日期
	private Date acceptEndDate;//接单结束日期

	private Date completeBegin;//完成开始日期
	private Date completeEnd;//完成结束日期

	private Long beginAt; //开始时间戳
	private Long endAt;//结束时间戳

	private Long userId;//子账号只能查询自己开的订单
	private Integer areaLevel;//区域类型 0:省 1：市 2：区
	private OrderUtils.OrderDataLevel orderDataLevel = OrderUtils.OrderDataLevel.CONDITION;//返回订单数据内容分级
	private Double balance = 0.0d;//客户余额
	private String creator = "";//下单人（客户查询时用）

	private Long productCategoryId; // 品类
	private Integer totalQty = 0;//产品数量
	private Integer travelChargeFlag = 0;//远程费用标记
	private Integer otherChargeFlag = 0;//其他费用标记
	private Double totalInStart = 0d;//应收总额开始
	private Double totalInEnd = 0d;//应收总额结束
	private Double totalOutStart = 0d;//应付总额开始
	private Double totalOutEnd = 0d;//应付总额结束
	private Dict engineerChargeStatus;//安维对帐状态

	private String returnBy = "";//退单人
	private Long salesId; //业务员

	private Integer messageType;//提醒消息类型

	private int autoComplete = 0;//自动完工

	private int orderAutoComplete = 0; //自动完工 2019/03/19 因不确定哪些查询用到autoComplete，只能新增属性

	private String cancelResponsible; //退单类型
	private List<Object> dictValueList = Lists.newArrayList();//数据字典值列表

	private int orderNoSearchType = 0;//工单单号搜索类型
	private List<String> quarters;//分片列表
	private Date now = new Date();
	private Date startOfToday = null;

	private int urgentFlag = 0;//加急标记
	private UrgentLevel urgentLevel = new UrgentLevel(0l);//加急等级
    private List<UrgentLevel> urgentLevels = Lists.newArrayList();
	private List<B2bCustomerMap> shopList;
	private String customerOwner; //客户负责人
	private String customerOrderNo; //客户单号
	private Integer reservationTime =0; //预约次数

	private Integer appointmentTimeoutFlag = 0;//预约超时标记（派单2小时没预约的算预约超时）
	private Date planDateBegin = null;//派单开始时间
    private Integer appointmentStatusFlag = 0;//客服的停滞工单列表中工单的预约状态标记：1-未预约，仅待跟进 2-预约到期，待跟进未到期 3-预约超期，待跟进未到期

	private Integer reminderFlag = 0; //催单标记
	private List<Long> engineerIds = Lists.newArrayList();  // 安维人员列表  //add on 2019-10-28

	private List<Integer> searchDataSources;//查询数据来源
	private List<Integer> exclueDataSources;//排除数据来源

	//客服类型 0:大客服 1:自动客服 2:突击客服 3vip客服
	private Integer kefuType;
	//线下单客户列表，跟单及业务查询使用
	private List<Long> offlineCustomerList;
	//客户账号负责店铺Id清单
	private List<String> shopIds;

	public int getOrderNoSearchType(){
		if (StringUtils.isNotBlank(this.orderNo)){
		    this.orderNo = this.orderNo.trim().toUpperCase();
			String orderNoPrefix = Global.getConfig("OrderPrefix");
			if (orderNo.length() == ORDER_LENGTH && orderNo.startsWith(orderNoPrefix)){
				orderNoSearchType = 1;
				String quarter = QuarterUtils.getOrderQuarterFromNo(orderNo);
				if(StringUtils.isNotBlank(quarter)){
				    this.quarter = quarter;
                }
			}else if (orderNo.startsWith(orderNoPrefix)){
				orderNoSearchType = 2;
			}
		}
		return this.orderNoSearchType;
	}

	public int getIsPhone(){
        if (StringUtils.isNotBlank(this.phone1)){
            if("".equalsIgnoreCase(StringUtils.isPhoneWithRelaxed(this.phone1))){
                this.isPhone = 1;
            }
        }
        return this.isPhone;
    }

	public int getIsPhone2(){
        if (StringUtils.isNotBlank(this.phone2)){
            if("".equalsIgnoreCase(StringUtils.isPhoneWithRelaxed(this.phone2))){
                this.isPhone2 = 1;
            }
        }
        return this.isPhone2;
    }

}