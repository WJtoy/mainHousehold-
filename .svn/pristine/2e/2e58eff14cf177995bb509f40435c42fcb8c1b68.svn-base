/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.wolfking.jeesite.modules.sys.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.annotations.JsonAdapter;
import com.kkl.kklplus.entity.md.GlobalMappingSalesSubFlagEnum;
import com.wolfking.jeesite.common.config.redis.GsonIgnore;
import com.wolfking.jeesite.common.persistence.LongIDDataEntity;
import com.wolfking.jeesite.common.utils.Collections3;
import com.wolfking.jeesite.common.utils.excel.annotation.ExcelField;
import com.wolfking.jeesite.common.utils.excel.fieldtype.RoleListType;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.entity.CustomerAccountProfile;
import com.wolfking.jeesite.modules.md.utils.CustomerSimpleListAdapter;
import com.wolfking.jeesite.modules.sys.entity.adapter.UserAdapter;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Length;

import java.beans.Transient;
import java.util.*;
import java.util.stream.Collectors;

//import com.wolfking.jeesite.common.persistence.DataEntity;

/**
 * 用户Entity
 * @author ThinkGem
 * @version 2013-12-05
 */
@JsonAdapter(UserAdapter.class)
public class User extends LongIDDataEntity<User> {

	/**
	 * 用户类型
	 */
	public static final Integer USER_TYPE_SYSTEM = 1;//普通系统用户
	public static final Integer USER_TYPE_SERVICE = 2;//客服
	public static final Integer USER_TYPE_CUSTOMER = 3;//厂商 主账号
	public static final Integer USER_TYPE_SUBCUSTOMER = 4;//厂商子账号
	public static final Integer USER_TYPE_SEARCH_CUSTOMER = 9;//厂商vip账号
	public static final Integer USER_TYPE_ENGINEER = 5;//安维
	public static final Integer USER_TYPE_CONSUMER = 6;//消费者（终端用户）
	public static final Integer USER_TYPE_SALES = 7;//业务人员
	public static final Integer USER_TYPE_INNER = 8;//内部账号,可设置负责区域
	public static final Integer USER_TYPE_GROUP = 10;//事业部账号
	public static final Integer USER_TYPE_FINANCE = 11; //财务账号   //2020-5-9

	public static final User APPLET_USER = new User(5L, "小程序账号", "");

	private static final long serialVersionUID = 1L;
	public static final long APPROVE_FLAG_NO = 2;
	public static final long USER_TYPE_KEFU = 2;
	private Office company;	// 归属公司
	private Office office;	// 归属部门
	private String loginName = "";// 登录名
	private String password = "";// 密码
//	private String no;		// 工号
	private String name = "";	// 姓名
	private String email = "";	// 邮箱
	private String phone = "";	// 电话
	private String mobile = "";	// 手机
	private String qq = "";		// QQ
	private Integer userType = 1;// 用户类型：公司账户，客户，维修员
	@GsonIgnore
	private String userTypeName = "";//用户类型名称，为微服务新增

	private String loginIp = "";	// 最后登陆IP
	private Date loginDate;	// 最后登陆日期
	private Integer subFlag = 0;	//是否为子帐号标记，1：是子帐号　0：不是子帐号
	private String photo = "";	// 头像
	private Integer managerFlag = 0; //职员
	private Long engineerId = 0l; //安维人员ID，与md_engineer.id关联
	private Long servicePointId = 0L; // 安维人员网点id，与md_servicepoint.id关联 // add on 2019-9-16 //ServicePoint微服务时加此属性
	private CustomerAccountProfile customerAccountProfile; // 客户账号属性，与md_customer_account_profile.id关联

	private UserRegionViewModel userRegion; //用户授权区域
	private List<String> userRegionNames = Lists.newArrayList();
	private String userRegionJson;

	private Integer statusFlag; //用户启用停用状态

	@GsonIgnore
	private List<Long> customerAccountProfileIds;    //客户账号id列表 // add 2019-7-29

	//业务员id,查询用
	@GsonIgnore
	private Long salesId;

	@GsonIgnore
	private String oldLoginName = "";// 原登录名
	@GsonIgnore
	private String newPassword = "";	// 新密码
	@GsonIgnore
	private String oldLoginIp = "";	// 上次登陆IP
	@GsonIgnore
	private Date oldLoginDate;	// 上次登陆日期
	@GsonIgnore
	private String orderBy = "";

	private Integer appLoged = 0; //是否app登录过

	private int  shortMessageFlag = 0; //是否接收短信通知 1:接收  ->from servicepoint
	//安维
	private int appFlag = 0;  //安维是否可以在手机上接单
	private Role role;	// 根据角色查询用户条件
	private List<Role> roleList = Lists.newArrayList(); // 拥有角色列表
	@GsonIgnore
	private List<Area> areaList = Lists.newArrayList(); // 区域列表

	private String areas = "";//保存时提交，使用Json格式，包含id,type
	//区域权限
	private String regions = "";//保存时提交，使用Json格式，包含provinceId,cityId,areaId,type

	private List<Long> companyIds = Lists.newArrayList();   //公司ids列表

	private List<Long> officeIds = Lists.newArrayList();    //部门ids列表

	@JsonAdapter(CustomerSimpleListAdapter.class)
	private List<Customer> customerList = Lists.newArrayList(); // 授权客户

	private Set<Long> customerIds = Sets.newHashSet(); //授权客户id

	private List<Long> productCategoryIds = Lists.newArrayList();//

	private String productCategoryNames = "";//收取品类名称('','');

	private String salesCustomerIds; // 业务员授权客户

	private String userSubIds; // 业务主管下属业务员

	private Map<Integer,String> attributesMap = Maps.newHashMap();  //用户属性

	public String getSalesCustomerIds() {
		return salesCustomerIds;
	}

	public void setSalesCustomerIds(String salesCustomerIds) {
		this.salesCustomerIds = salesCustomerIds;
	}

	public String getUserSubIds() {
		return userSubIds;
	}

	public void setUserSubIds(String userSubIds) {
		this.userSubIds = userSubIds;
	}

	public User() {
		super();
	}

	public User(Long id){
		super(id);
	}

	public User(Long id, String loginName){
		super(id);
		this.loginName = loginName;
	}

	public User(Long id,String name,String mobile){
		super(id);
		this.mobile = mobile;
		this.name = name;
	}

	public User(Role role){
		super();
		this.role = role;
	}

	//region getset
	public String getPhoto() {
		return photo;
	}

	public void setPhoto(String photo) {
		this.photo = photo;
	}

	public Integer getSubFlag() {
		return subFlag;
	}

	public void setSubFlag(Integer subFlag) {
		this.subFlag = subFlag;
	}

	@ExcelField(title="ID", type=1, align=2, sort=1)
	public Long getId() {
		return id;
	}

	@JsonIgnore
//	@NotNull(message="归属公司不能为空")
	@ExcelField(title="归属公司", align=2, sort=20)
	public Office getCompany() {
		return company;
	}

	public void setCompany(Office company) {
		this.company = company;
	}

	@JsonIgnore
	public Long getCompanyId(){
		return company==null?0l:company.getId();
	}
	
	@JsonIgnore
//	@NotNull(message="归属部门不能为空")
	@ExcelField(title="归属部门", align=2, sort=25)
	public Office getOffice() {
		return office;
	}

	public void setOffice(Office office) {
		this.office = office;
	}

	@JsonIgnore
	public Long getOfficeId(){
		return office==null?0l:office.getId();
	}

	@Length(min=1, max=20, message="登录名长度必须介于 1 和 20 之间")
	@ExcelField(title="登录名", align=2, sort=30)
	public String getLoginName() {
		return loginName;
	}

	public void setLoginName(String loginName) {
		this.loginName = loginName;
	}

	@JsonIgnore
	@Length(max=100, message="密码长度必须介于 1 和 100 之间")
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Length(min=1, max=20, message="姓名长度必须介于 1 和 20 之间")
	@ExcelField(title="姓名", align=2, sort=40)
	public String getName() {
		return name;
	}
	
	@Length(min=0, max=11, message="QQ号长度应小于12")
	@ExcelField(title="QQ号", align=2, sort=120)
	public String getQq() {
		return qq;
	}

	public void setQq(String qq) {
		this.qq = qq;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Email(message="邮箱格式不正确")
	@Length(min=0, max=100, message="邮箱长度必须小于100")
	@ExcelField(title="邮箱", align=1, sort=50)
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	
	@Length(min=0, max=20, message="电话长度必须小于20")
	@ExcelField(title="电话", align=2, sort=60)
	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	@Length(max=11, message="手机长度应为11位")
	@ExcelField(title="手机", align=2, sort=70)
	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	@ExcelField(title="备注", align=1, sort=900)
	public String getRemarks() {
		return remarks;
	}
	
	@ExcelField(title="用户类型", align=2, sort=80, dictType="sys_user_type")
	public Integer getUserType() {
		return userType;
	}

	public void setUserType(Integer userType) {
		this.userType = userType;
	}

	public String getUserTypeName() {
		return userTypeName;
	}

	public void setUserTypeName(String userTypeName) {
		this.userTypeName = userTypeName;
	}

	public Long getEngineerId() {
		return engineerId;
	}

	public void setEngineerId(Long engineerId) {
		this.engineerId = engineerId;
	}

	public CustomerAccountProfile getCustomerAccountProfile() {
		return customerAccountProfile;
	}

	public void setCustomerAccountProfile(CustomerAccountProfile customerAccountProfile) {
		this.customerAccountProfile = customerAccountProfile;
	}

	@ExcelField(title="创建时间", type=0, align=1, sort=90)
	public Date getCreateDate() {
		return createDate;
	}

	@ExcelField(title="最后登录IP", type=1, align=1, sort=100)
	public String getLoginIp() {
		return loginIp;
	}

	public void setLoginIp(String loginIp) {
		this.loginIp = loginIp;
	}

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@ExcelField(title="最后登录日期", type=1, align=1, sort=110)
	public Date getLoginDate() {
		return loginDate;
	}

	public void setLoginDate(Date loginDate) {
		this.loginDate = loginDate;
	}

	public String getOldLoginName() {
		return oldLoginName;
	}

	public void setOldLoginName(String oldLoginName) {
		this.oldLoginName = oldLoginName;
	}

	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}

	public String getOldLoginIp() {
		if (oldLoginIp == null){
			return loginIp;
		}
		return oldLoginIp;
	}

	public void setOldLoginIp(String oldLoginIp) {
		this.oldLoginIp = oldLoginIp;
	}

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	public Date getOldLoginDate() {
		if (oldLoginDate == null){
			return loginDate;
		}
		return oldLoginDate;
	}

	public void setOldLoginDate(Date oldLoginDate) {
		this.oldLoginDate = oldLoginDate;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public Integer getManagerFlag() {
		return managerFlag;
	}

	public void setManagerFlag(Integer managerFlag) {
		this.managerFlag = managerFlag;
	}

	public UserRegionViewModel getUserRegion() {
		return userRegion;
	}

	public void setUserRegion(UserRegionViewModel userRegion) {
		this.userRegion = userRegion;
	}

	public String getUserRegionJson() {
		return userRegionJson;
	}

	public void setUserRegionJson(String userRegionJson) {
		this.userRegionJson = userRegionJson;
	}

	public Integer getStatusFlag() {
		return statusFlag;
	}

	public void setStatusFlag(Integer statusFlag) {
		this.statusFlag = statusFlag;
	}

	@JsonIgnore
	@ExcelField(title="拥有角色", align=1, sort=800, fieldType=RoleListType.class)
	public List<Role> getRoleList() {
		return roleList;
	}
	
	public void setRoleList(List<Role> roleList) {
		this.roleList = roleList;
	}

	@JsonIgnore
	public List<Long> getRoleIdList() {
		List<Long> roleIdList = Lists.newArrayList();
		for (Role role : roleList) {
			roleIdList.add(role.getId());
		}
		return roleIdList;
	}

	public void setRoleIdList(List<Long> roleIdList) {
		roleList = Lists.newArrayList();
		for (Long roleId : roleIdList) {
			Role role = new Role();
			role.setId(roleId);
			roleList.add(role);
		}
	}
	
	/**
	 * 用户拥有的角色名称字符串, 多个角色名称用','分隔.
	 */
	public String getRoleNames() {
		return Collections3.extractToString(roleList, "name", ",");
	}

	/**
	 * 用户拥有的角色英文名称字符串, ','分隔.
	 */
	public Set<String> getRoleEnNames() {
		if(roleList==null || roleList.size()==0){
			return Sets.newHashSet();
		}
		return roleList.stream().map(t->t.getEnname()).collect(Collectors.toSet());
	}
	
	public static boolean isAdmin(Long id){
//		return id != null && id == 1L;  //mark on 2021-5-2
		if(id == null || id <= 0L){
			return false;
		}else if(id == 1L){
			return true;
		}else {
			// 2021-5-29 begin
			final Long RoleOfAdmin = 1L;  //系统管理员角色
			List<Role> roleList = UserUtils.findRoleList(id);
			long lCount = Optional.ofNullable(roleList).orElse(Collections.emptyList()).stream().filter(x -> x.getId().equals(RoleOfAdmin)).count();
			return lCount > 0 ? true : false;
		}
		// 2021-5-29 end
	}

	public List<Long> getAreaIds()
	{
		List<Long> nameIdList = Lists.newArrayList();
		for (Area area : areaList)
		{
			nameIdList.add(area.getId());
		}
		return nameIdList;
	}

	public void setAreaIds(List<Long> areaIds)
	{
		areaList = Lists.newArrayList();
		if (areaIds != null)
		{
			for (Long areaId : areaIds)
			{
				Area area = new Area();
				area.setId(areaId);
				areaList.add(area);
			}
		}
	}

	@JsonIgnore
	public List<Area> getAreaList()
	{
		return areaList;
	}

	public void setAreaList(List<Area> areaList)
	{
		this.areaList = areaList;
	}

	public List<Customer> getCustomerList()
	{
		return customerList;
	}

	public void setCustomerList(List<Customer> customerList)
	{
		this.customerList = customerList;
	}

	public Map<Integer, String> getAttributesMap() {
		return attributesMap;
	}

	public void setAttributesMap(Map<Integer, String> attributesMap) {
		this.attributesMap = attributesMap;
	}
	//public List<Long> getCustomerIds()
	//{
	//	List<Long> nameIdList = Lists.newArrayList();
	//	for (Customer customer : customerList)
	//	{
	//		nameIdList.add(customer.getId());
	//	}
	//	return nameIdList;
	//}
    //
	//public void setCustomerIds(List<Long> customerIds)
	//{
	//	customerList = Lists.newArrayList();
	//	if (customerIds != null)
	//	{
	//		for (Long customerId : customerIds)
	//		{
	//			Customer customer = new Customer();
	//			customer.setId(customerId);
	//			customerList.add(customer);
	//		}
	//	}
	//}
	
	@Override
	public String toString() {
		return id==null?"":id.toString();
	}




	public String getAreas() {
		return areas;
	}

	public void setAreas(String areas) {
		this.areas = areas;
	}

	public Integer getAppLoged() {
		return appLoged;
	}

	public void setAppLoged(Integer appLoged) {
		this.appLoged = appLoged;
	}

	public Long getSalesId() {
		return salesId;
	}

	public void setSalesId(Long salesId) {
		this.salesId = salesId;
	}

	public int getShortMessageFlag() {
		return shortMessageFlag;
	}

	public void setShortMessageFlag(int shortMessageFlag) {
		this.shortMessageFlag = shortMessageFlag;
	}

	public int getAppFlag() {
		return appFlag;
	}

	public void setAppFlag(int appFlag) {
		this.appFlag = appFlag;
	}

	public Set<Long> getCustomerIds() {
		return customerIds;
	}

	public void setCustomerIds(Set<Long> customerIds) {
		this.customerIds = customerIds;
	}

	public List<Long> getProductCategoryIds() {
		return productCategoryIds;
	}

	public void setProductCategoryIds(List<Long> productCategoryIds) {
		this.productCategoryIds = productCategoryIds;
	}

	public String getProductCategoryNames() {
		return productCategoryNames;
	}

	public void setProductCategoryNames(String productCategoryNames) {
		this.productCategoryNames = productCategoryNames;
	}

	public List<Long> getCustomerAccountProfileIds() {
		return customerAccountProfileIds;
	}

	public void setCustomerAccountProfileIds(List<Long> customerAccountProfileIds) {
		this.customerAccountProfileIds = customerAccountProfileIds;
	}

	public Long getServicePointId() {
		return servicePointId;
	}

	public void setServicePointId(Long servicePointId) {
		this.servicePointId = servicePointId;
	}

	public List<Long> getCompanyIds() {
		return companyIds;
	}

	public void setCompanyIds(List<Long> companyIds) {
		this.companyIds = companyIds;
	}

	public List<Long> getOfficeIds() {
		return officeIds;
	}

	public void setOfficeIds(List<Long> officeIds) {
		this.officeIds = officeIds;
	}

	//endregion getset

	//region 角色判断

	public boolean isAdmin(){
		return isAdmin(this.id);
	}

	@Transient
	public boolean isCustomer()
	{
		return this.userType == USER_TYPE_CUSTOMER || this.userType == USER_TYPE_SUBCUSTOMER
				|| this.userType == USER_TYPE_SEARCH_CUSTOMER;
	}

	public boolean isKefu(){
		return this.userType == USER_TYPE_KEFU;
	}

	// 是否是内部帐号，内部帐号可分配区域
	public boolean isInnerAccount(){
		return this.userType == USER_TYPE_INNER;
	}

	public boolean isKefuLeader(){
		if(this.getRoleList()!=null && this.getRoleList().stream().filter(t->t.getEnname().equalsIgnoreCase("kefuleader")).count()>0){
			return true;
		}

		return false;
	}

	@Transient
	public boolean isEngineer()
	{
		return this.userType == USER_TYPE_ENGINEER;
	}

	@Transient
	public boolean isSystemUser()
	{
		return this.userType == USER_TYPE_SYSTEM || this.userType == USER_TYPE_SERVICE
				|| this.userType == USER_TYPE_SALES || this.userType == USER_TYPE_INNER
				|| this.userType == USER_TYPE_GROUP;
	}

	/**
	 * 是否为业务员(user_type = 7,sub_flag=1,业务主管：by role)
	 */
	@Transient
	public boolean isSaleman() {
		// 系统中 业务 角色的 ID
		//return this.getRoleIdList().contains(USER_ROLE_SALEMAN);
		if(this.userType == USER_TYPE_SALES){
			return true;
		}
		//业务主管判断，暂时取消
		//if(this.getRoleList()!=null && this.getRoleList().stream().filter(t->t.getEnname().equalsIgnoreCase("salesleader")).count()>0){
		//	return true;
		//}
		return false;
	}

	/**
	 * 是否为业务员
	 * @return
	 */
	@Transient
	public boolean isSalesPerson() {
		// add on 2020-3-21
		// 系统中 业务 角色的 ID
		//return this.getRoleIdList().contains(USER_ROLE_SALEMAN);
		if  (this.userType == USER_TYPE_SALES && this.subFlag.equals(GlobalMappingSalesSubFlagEnum.SALES.getValue())) {
			return true;
		}
		return false;
	}

	/**
	 * 是否为跟单员(user_type = 7,sub_flag=2,业务主管：by role)
	 */
	@Transient
	public boolean isMerchandiser() {
		// add on 2019-11-15
		// 系统中 业务 角色的 ID
		//return this.getRoleIdList().contains(USER_ROLE_SALEMAN);
		if(this.userType == USER_TYPE_SALES && this.subFlag.equals(GlobalMappingSalesSubFlagEnum.MERCHANDISER.getValue()) ){
			return true;
		}
		return false;
	}

    /**
     * 是否为业务员主管(user_type = 7,sub_flag=3)
     * @return
     */
    @Transient
    public boolean isSalesManager() {
        // add on 2021-6-11
        // 系统中 业务 角色的 ID
        if(this.userType == USER_TYPE_SALES && this.subFlag.equals(GlobalMappingSalesSubFlagEnum.MANAGER.getValue()) ){
            return true;
        }
        return false;
    }


	/**
	 * 是否是突击客服(user_type = 2,sub_flag=3)
	 */
	public boolean isRushKefu(){
		if(this.userType == USER_TYPE_KEFU && this.subFlag.equals(KefuTypeEnum.Rush.getCode())){
			return true;
		}
		return false;
	}

	public List<String> getUserRegionNames() {
		return userRegionNames;
	}

	public void setUserRegionNames(List<String> userRegionNames) {
		this.userRegionNames = userRegionNames;
	}

	public String getRegions() {
		return regions;
	}

	public void setRegions(String regions) {
		this.regions = regions;
	}

	//endregion 角色判断
}