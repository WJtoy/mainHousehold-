package com.wolfking.jeesite.ms.b2bcenter.sd.entity;

import com.google.common.collect.Lists;
import com.google.gson.annotations.JsonAdapter;
import com.kkl.kklplus.entity.b2bcenter.md.B2BCustomerMapping;
import com.wolfking.jeesite.common.config.redis.GsonIgnore;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.entity.ProductCategory;
import com.wolfking.jeesite.modules.md.entity.UrgentLevel;
import com.wolfking.jeesite.modules.md.utils.AreaSimpleAdapter;
import com.wolfking.jeesite.modules.md.utils.CustomerSimpleAdapter;
import com.wolfking.jeesite.modules.sd.entity.Order;
import com.wolfking.jeesite.modules.sd.entity.viewModel.OrderItemModel;
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
 * B2B订单人工(UI)转换视图模型
 *
 * @author Ryan
 * @date 2018/05/21
 */
@Data
@NoArgsConstructor
public class B2BOrderConvertVModel implements Serializable {

    private static final long serialVersionUID = 1L;
    //详细地址(不包含省市区，4级)的最大长度，参考数据库
    public static final int ADDRESS_MAX_LENGTH = 60;

    // Fields
    //订单类型
    private Dict orderType;
    //订单服务类型 1:安装 2:维修，...
    private int orderServiceType;
    //数据库分片
    private String quarter = "";
    // id
    protected Long id;
    //b2b的订单id
    private String b2bOrderNo = "";
    //主交易订单编号
    private String parentBizOrderId = "";
    //数据源
    private Dict dataSource;
    @JsonAdapter(CustomerSimpleAdapter.class)
    private Customer customer;
    //用户区域
    @JsonAdapter(AreaSimpleAdapter.class)
    private Area area;
    @JsonAdapter(AreaSimpleAdapter.class)
    private Area subArea;       //4级区域  // add on 2019-6-3
    //订单号
    private String orderNo = "";
    //用户
    //用户名
    @Length(min = 1, max = 50, message = "用户名长度不能超过50个汉字")
    private String userName = "";
    @Length(min = 11, max = 11, message = "手机号码长度应为11位")
    private String phone1 = "";

    @Length(min = 0, max = 16, message = "座机长度不能超过16位")
    private String phone2 = "";

    @GsonIgnore
    private String phone3 = "";

    @GsonIgnore
    @Length(max = 11, message = "用户实际联络电话长度应为11位")
    private String servicePhone = "";//用户实际联络电话

    @GsonIgnore
    private String email = "";

    @Length(min = 1, max = 60, message = "详细地址长度超过60个汉字")
    private String address = "";//用户地址

    @GsonIgnore
    @Length(min = 0, max = 100, message = "地址长度超过100个汉字")
    private String serviceAddress = "";//实际上门地址

    private Double expectCharge = 0.00;//订单预付金额(派单价)
    private Double blockedCharge = 0.00;//该订单冻结金额 合计每个产品冻结费用

    @Length(min = 0, max = 250, message = "服务描述长度不能超过250个汉字")
    private String description = "";//服务描述

    private Double balanceCharge = 0.00;//厂商客户上次结存

    @GsonIgnore
    private String unit = "RMB";//货币

    private Integer totalQty = 0;//产品数量

    @GsonIgnore
    private Integer status = Order.ORDER_STATUS_NEW;//状态

    //客户
    @JsonAdapter(DictSimpleAdapter.class)
    private Dict orderPaymentType = new Dict("0");//客户结算方式(维护自客户)

    private List<OrderItemModel> items = Lists.newArrayList(); // 订单明细

    private Double customerBalance = 0.00;//客户账户余额，辅助
    private Double customerBlockBalance = 0.00;//客户冻结余额，辅助
    private Double customerCredit = 0.00;//客户信用额度
    //加急
    private Double customerUrgentCharge = 0.00;
    private Double engineerUrgentCharge = 0.00;
    private UrgentLevel urgentLevel = new UrgentLevel(0l);

    @JsonAdapter(UserSimpleAdapter.class)
    private User createBy;

    @GsonIgnore
    private Date createDate;
    private Long createDt;
    @GsonIgnore
    private Date updateDate;
    private List<Dict> expresses = Lists.newArrayList();//快递公司

    private String actionType = "";//操作类型，用于修改订单保存成功后跳转用
    private String repeateNo = "";//疑似重单单号 2018.01.12

    private User kefu;

    private String fullAddress = "";//原始地址

    //b2b店铺id
//	private B2bCustomerMap b2bShop;

    private B2BCustomerMapping customerMapping;

    private String customerOwner = "";//客户负责人

    private String estimatedReceiveDate;//预计到货时间
    private Long buyDate;
    private String expectServiceTime;
    /**
     * 网点编号
     */
    private String siteCode = "";

    /**
     * 网点名称
     */
    private String siteName = "";

    /**
     * 师傅名称
     */
    private String engineerName = "";
    /**
     * 师傅手机
     */
    private String engineerMobile = "";
    /**
     * 工单来源
     */
    private String orderDataSource = "";


    // 2019-04-15
    private double longitude; //经度
    private double latitude; //维度

    private Long b2bOrderId = 0L;
    private String b2bQuarter = "";
    private ProductCategory category;
    private Long createById = 0L;
}