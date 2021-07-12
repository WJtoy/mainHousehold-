package com.wolfking.jeesite.modules.sd.entity.viewModel;

import com.google.common.collect.Lists;
import com.google.gson.annotations.JsonAdapter;
import com.wolfking.jeesite.common.config.redis.GsonIgnore;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.entity.ProductCategory;
import com.wolfking.jeesite.modules.md.entity.UrgentLevel;
import com.wolfking.jeesite.modules.md.utils.AreaSimpleAdapter;
import com.wolfking.jeesite.modules.md.utils.CreatOrderCustomerAdapter;
import com.wolfking.jeesite.modules.md.utils.ProductCategoryAdapter;
import com.wolfking.jeesite.modules.md.utils.UrgentLevelSimpleAdapter;
import com.wolfking.jeesite.modules.sd.entity.Order;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.entity.adapter.DictSimpleAdapter;
import com.wolfking.jeesite.modules.sys.entity.adapter.UserSimpleAdapter;
import com.wolfking.jeesite.ms.tmall.md.entity.B2bCustomerMap;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 下单视图模型
 * 
 * @author Ryan
 */

public class CreateOrderModel implements Serializable{

	private static final long serialVersionUID = 1L;

	// Fields
	private Long version = 0l;//版本，用于检查及控制订单操作唯一性
	private String action = "new";
	private String quarter = "";//数据库分片
	protected Long id;		// id
	@JsonAdapter(CreatOrderCustomerAdapter.class)
	private Customer customer;
	@JsonAdapter(AreaSimpleAdapter.class)
	private Area area;//用户区域
	@JsonAdapter(AreaSimpleAdapter.class)
	private Area subArea;//用户的4级区域  // add on 2019-5-21
	private String orderNo="";//订单号
	//b2b的订单id
	private String b2bOrderNo = "";
    private String parentBizOrderId = "";//主交易订单编号
	@GsonIgnore
	private Order order = null; //修改时，判断为灯饰下单时，将order也传入，减少一次订单读取操作
	//用户
	@Length(min=1,max=50,message = "用户名长度不能超过50个汉字")
	private String userName="";//用户名

	@Length(min=11,max=11,message = "手机号码长度应为11位")
	private String phone1="";

	@Length(min=0,max=16,message = "座机长度不能超过16位")
	private String phone2="";

	@GsonIgnore
	private String phone3="";

	@GsonIgnore
	@Length(max=11,message = "用户实际联络电话长度应为11位")
	private String servicePhone="";//用户实际联络电话

	@GsonIgnore
	private String email="";

	@Length(min=1,max=60,message = "详细地址长度超过60个汉字")
	private String address="";//用户地址

	@GsonIgnore
	@Length(min=0,max=100,message = "地址长度超过100个汉字")
	private String serviceAddress="";//实际上门地址

	private Double expectCharge = 0.00;//订单预付金额(派单价)
	private Double blockedCharge = 0.00;//该订单冻结金额 合计每个产品冻结费用

	@Length(min=0,max=250,message = "服务描述长度不能超过250个汉字")
	private String description="";//服务描述

	private Double balanceCharge = 0.00;//厂商客户上次结存

	@GsonIgnore
	private String unit = "RMB";//货币

	//@Min(value = 1,message = "数量应大于0")
	private Integer  totalQty= 0;//产品数量

	@GsonIgnore
	private Integer status= Order.ORDER_STATUS_NEW;//状态

	//客户
	@JsonAdapter(DictSimpleAdapter.class)
	private Dict orderPaymentType = new Dict("0");//客户结算方式(维护自客户)

	private List<OrderItemModel> items = Lists.newArrayList(); // 订单明细

	private Double customerBalance = 0.00;//客户账户余额，辅助
	private Double customerBlockBalance = 0.00;//客户冻结余额，辅助
	private Double customerCredit = 0.00;//客户信用额度

	private String customerOwner;//客户负责人
	@JsonAdapter(UserSimpleAdapter.class)
	private User createBy;

	@GsonIgnore
	private Date createDate;
	@GsonIgnore
	private Date updateDate;
	private List<Dict> expresses = Lists.newArrayList();//快递公司

	private String actionType = "";//操作类型，用于修改订单保存成功后跳转用
	private String repeateNo = "";//疑似重单单号 2018.01.12
	@JsonAdapter(UrgentLevelSimpleAdapter.class)
	private UrgentLevel urgentLevel = new UrgentLevel(0l,"不加急"); //加急等级
	private double customerUrgentCharge = 0.00;//加急费(应收)
	private double engineerUrgentCharge = 0.00;//加急费(应付)
	private int urgentFlag = 0;//加急，1-加急
	// 2019-04-15
	private double longitude; //经度
	private double latitude; //维度

	@GsonIgnore
	private Dict dataSource = new Dict("1", "快可立");//数据来源;
	//b2b店铺id
	private B2bCustomerMap b2bShop;
	//销售渠道 1-线下
	private int orderChannel = 1;

	//当前订单项目类目，不允许有多个类目，前端通过它来控制选择
	@JsonAdapter(ProductCategoryAdapter.class)
	private ProductCategory category;

	public CreateOrderModel() {

	}

	public CreateOrderModel(Long id){
		this.id = id;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	
	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	
	public Area getArea() {
		return area;
	}

	public void setArea(Area area) {
		this.area = area;
	}

	
	public String getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}

	public String getB2bOrderNo() {
		return b2bOrderNo;
	}

	public void setB2bOrderNo(String b2bOrderNo) {
		this.b2bOrderNo = b2bOrderNo;
	}

    public String getParentBizOrderId() {
        return parentBizOrderId;
    }

    public void setParentBizOrderId(String parentBizOrderId) {
        this.parentBizOrderId = parentBizOrderId;
    }

    public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	
	public String getPhone1() {
		return phone1;
	}

	public void setPhone1(String phone1) {
		this.phone1 = phone1;
	}

	
	public String getPhone2() {
		return phone2;
	}

	public void setPhone2(String phone2) {
		this.phone2 = phone2;
	}

	
	public String getPhone3() {
		return phone3;
	}

	public void setPhone3(String phone3) {
		this.phone3 = phone3;
	}

	
	public String getServicePhone() {
		return servicePhone;
	}

	public void setServicePhone(String servicePhone) {
		this.servicePhone = servicePhone;
	}

	
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	
	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	
	public String getServiceAddress() {
		return serviceAddress;
	}

	public void setServiceAddress(String serviceAddress) {
		this.serviceAddress = serviceAddress;
	}

	public Double getExpectCharge() {
		return expectCharge;
	}

	public void setExpectCharge(Double expectCharge) {
		this.expectCharge = expectCharge;
	}

	public Double getBlockedCharge() {
		return blockedCharge;
	}

	public void setBlockedCharge(Double blockedCharge) {
		this.blockedCharge = blockedCharge;
	}

	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Double getBalanceCharge() {
		return balanceCharge;
	}

	public void setBalanceCharge(Double balanceCharge) {
		this.balanceCharge = balanceCharge;
	}

	
	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public Integer getTotalQty() {
		return totalQty;
	}

	public void setTotalQty(Integer totalQty) {
		this.totalQty = totalQty;
	}

	
	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	
	public Dict getOrderPaymentType() {
		return orderPaymentType;
	}

	public void setOrderPaymentType(Dict orderPaymentType) {
		this.orderPaymentType = orderPaymentType;
	}

	public List<OrderItemModel> getItems() {
		return items;
	}

	public void setItems(List<OrderItemModel> items) {
		this.items = items;
	}

	public Double getCustomerBalance() {
		return customerBalance;
	}

	public void setCustomerBalance(Double customerBalance) {
		this.customerBalance = customerBalance;
	}

	public Double getCustomerCredit() {
		return customerCredit;
	}

	public void setCustomerCredit(Double customerCredit) {
		this.customerCredit = customerCredit;
	}

	
	public User getCreateBy() {
		return createBy;
	}

	public void setCreateBy(User createBy) {
		this.createBy = createBy;
	}

	
	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	
	public Date getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

	public String getActionType() {
		return actionType;
	}

	public void setActionType(String actionType) {
		this.actionType = actionType;
	}

	public Double getCustomerBlockBalance() {
		return customerBlockBalance;
	}

	public void setCustomerBlockBalance(Double customerBlockBalance) {
		this.customerBlockBalance = customerBlockBalance;
	}

	public String getQuarter() {
		return quarter;
	}

	public void setQuarter(String quarter) {
		this.quarter = quarter;
	}

	public List<Dict> getExpresses() {
		return expresses;
	}

	public void setExpresses(List<Dict> expresses) {
		this.expresses = expresses;
	}

	public String getRepeateNo() {
		return repeateNo;
	}

	public void setRepeateNo(String repeateNo) {
		this.repeateNo = repeateNo;
	}

	public UrgentLevel getUrgentLevel() {
		return urgentLevel;
	}

	public void setUrgentLevel(UrgentLevel urgentLevel) {
		this.urgentLevel = urgentLevel;
	}

	public double getCustomerUrgentCharge() {
		return customerUrgentCharge;
	}

	public void setCustomerUrgentCharge(double customerUrgentCharge) {
		this.customerUrgentCharge = customerUrgentCharge;
	}
	public double getEngineerUrgentCharge() {
		return engineerUrgentCharge;
	}
	public void setEngineerUrgentCharge(double engineerUrgentCharge) {
		this.engineerUrgentCharge = engineerUrgentCharge;
	}

	public int getUrgentFlag() {
		return urgentFlag;
	}

	public void setUrgentFlag(int urgentFlag) {
		this.urgentFlag = urgentFlag;
	}

	public String getCustomerOwner() {
		return customerOwner;
	}

	public void setCustomerOwner(String customerOwner) {
		this.customerOwner = customerOwner;
	}

	public Dict getDataSource() {
		return dataSource;
	}

	public void setDataSource(Dict dataSource) {
		this.dataSource = dataSource;
	}

	public B2bCustomerMap getB2bShop() {
		return b2bShop;
	}

	public void setB2bShop(B2bCustomerMap b2bShop) {
		this.b2bShop = b2bShop;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public Area getSubArea() {
		return subArea;
	}

	public void setSubArea(Area subArea) {
		this.subArea = subArea;
	}

	public ProductCategory getCategory() {
		return category;
	}

	public void setCategory(ProductCategory category) {
		this.category = category;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}

	public int getOrderChannel() {
		return orderChannel;
	}

	public void setOrderChannel(int orderChannel) {
		this.orderChannel = orderChannel;
	}
}