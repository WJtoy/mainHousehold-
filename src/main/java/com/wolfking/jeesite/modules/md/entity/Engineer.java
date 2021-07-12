package com.wolfking.jeesite.modules.md.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;
import com.google.gson.annotations.JsonAdapter;
import com.kkl.kklplus.entity.md.MDEngineerAddress;
import com.kkl.kklplus.entity.md.MDEngineerCert;
import com.wolfking.jeesite.common.config.redis.GsonIgnore;
import com.wolfking.jeesite.common.persistence.LongIDDataEntity;
import com.wolfking.jeesite.modules.md.utils.EngineerAdapter;
import com.wolfking.jeesite.modules.md.utils.ServicePointSimpleAdapter;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.Dict;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 安维师傅信息表
 * Created on 2017-04-16.
 */
@JsonAdapter(EngineerAdapter.class)
public class Engineer extends LongIDDataEntity<Engineer> {

    public Engineer(){
        super();
    }

    public Engineer(Long id){
        super(id);
    }
    @JsonAdapter(ServicePointSimpleAdapter.class)
    private ServicePoint servicePoint;  //网点
    private String contactInfo="";     //联系方式(必须为手机号)
    private String name="";//姓名
    private Area area; //区域
    private String address="";  //详细地址
    private Double  grade =0d;   //评价分数
    private String qq = "";    //qq
    private Integer masterFlag = 0; //是否为负责人
    private Integer appFlag = 0;  //安维是否可以在手机上接单
    private Dict level = new Dict("0","");  //等级
    private int  orderCount =0; //完成订单数量
    private int  planCount=0;  //派单数
    private int  breakCount=0;  //违约单数
    private Integer exceptId; //例外ID,用于engselect.tag
    private String idNo = "";    //身份证号
    private String creditPhone = "";	// 身份证照片
    private String attachment = "";	// 身份证照片

    private Long accountId = 0l; //帐号id,sys_user at 2018/01/15

    @GsonIgnore
    private List<Area> areaList = Lists.newArrayList();//区域
    private String areas = "";//保存时提交，使用Json格式，包含id,type
    private List<MDEngineerCert> engineerCerts = new ArrayList();

    //辅助字段
    @GsonIgnore
    private int orgMasterFlag = 0; //是否为负责人(修改前)
    @GsonIgnore
    private String orderBy;
    private Integer appLoged = 0; //是否app登录过
    //是否是第一次查询
    private Integer firstSearch = 1;

    private Integer forTmall = 0; //是否是淘宝服务师傅，B2B

    private Integer reminderCount = 0;  // 催单
    private Integer complainCount = 0;  // 投诉单
    private MDEngineerAddress engineerAddress;// 收件信息

    public MDEngineerAddress getEngineerAddress() {
        return engineerAddress;
    }

    public void setEngineerAddress(MDEngineerAddress engineerAddress) {
        this.engineerAddress = engineerAddress;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }

    public String getAttachment() {
        return attachment;
    }

    public void setAttachment(String attachment) {
        this.attachment = attachment;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Double getGrade() {
        return grade;
    }

    public void setGrade(Double grade) {
        this.grade = grade;
    }

    public String getQq() {
        return qq;
    }

    public void setQq(String qq) {
        this.qq = qq;
    }

    public Integer getMasterFlag() {
        return masterFlag;
    }

    public void setMasterFlag(Integer masterFlag) {
        this.masterFlag = masterFlag;
    }

    public Integer getAppFlag() {
        return appFlag;
    }

    public void setAppFlag(Integer appFlag) {
        this.appFlag = appFlag;
    }

    public ServicePoint getServicePoint() {
        return servicePoint;
    }

    public void setServicePoint(ServicePoint servicePoint) {
        this.servicePoint = servicePoint;
    }

    public int getOrgMasterFlag() {
        return orgMasterFlag;
    }

    public void setOrgMasterFlag(int orgMasterFlag) {
        this.orgMasterFlag = orgMasterFlag;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }
    public int getBreakCount() {
        return breakCount;
    }

    public void setBreakCount(int breakCount) {
        this.breakCount = breakCount;
    }

    public int getPlanCount() {
        return planCount;
    }

    public void setPlanCount(int planCount) {
        this.planCount = planCount;
    }

    public int getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(int orderCount) {
        this.orderCount = orderCount;
    }

    public Dict getLevel() {
        return level;
    }

    public void setLevel(Dict level) {
        this.level = level;
    }

    public Integer getExceptId() {
        return exceptId;
    }

    public void setExceptId(Integer exceptId) {
        this.exceptId = exceptId;
    }

    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }

    public String getIdNo() {
        return idNo;
    }

    public void setIdNo(String idNo) {
        this.idNo = idNo;
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

    @JsonIgnore
    public List<MDEngineerCert> getEngineerCerts()
    {
        return engineerCerts;
    }

    public void setEngineerCerts(List<MDEngineerCert> engineerCerts)
    {
        this.engineerCerts = engineerCerts;
    }

    public List<Long> getAreaIds()
    {
        if(areaList==null || areaList.size()==0){
            return Lists.newArrayList();
        }

        return areaList.stream().map(t->t.getId()).collect(Collectors.toList());
        /*
        List<Long> nameIdList = Lists.newArrayList();
        for (Area area : areaList)
        {
            nameIdList.add(area.getId());
        }
        return nameIdList;
        */
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

    public String getAreas() {
        return areas;
    }

    public void setAreas(String areas) {
        this.areas = areas;
    }

    public String getCreditPhone() {
        return creditPhone;
    }

    public void setCreditPhone(String creditPhone) {
        this.creditPhone = creditPhone;
    }

    public Integer getAppLoged() {
        return appLoged;
    }

    public void setAppLoged(Integer appLoged) {
        this.appLoged = appLoged;
    }

    public Integer getFirstSearch() {
        return firstSearch;
    }

    public void setFirstSearch(Integer firstSearch) {
        this.firstSearch = firstSearch;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Integer getForTmall() {
        return forTmall;
    }

    public void setForTmall(Integer forTmall) {
        this.forTmall = forTmall;
    }

    public Integer getReminderCount() {
        return reminderCount;
    }

    public void setReminderCount(Integer reminderCount) {
        this.reminderCount = reminderCount;
    }

    public Integer getComplainCount() {
        return complainCount;
    }

    public void setComplainCount(Integer complainCount) {
        this.complainCount = complainCount;
    }
}
