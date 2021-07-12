package com.wolfking.jeesite.modules.sd.dao;

import com.wolfking.jeesite.common.persistence.LongIDCrudDao;
import com.wolfking.jeesite.modules.sd.entity.*;
import com.wolfking.jeesite.modules.sd.entity.viewModel.*;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 客户数据访问接口
 * Created on 2017-04-12.
 */
@Mapper
public interface OrderDao extends LongIDCrudDao<Order> {

    /*订单状态*/
    OrderStatus getOrderStatusById(@Param("orderId") Long orderId, @Param("quarter") String quarter, @Param("fromMasterDb") Boolean fromMasterDb);

    /**
     * 订单基本信息(json)
     *
     * @param orderId 订单id
     * @return
     */
    Map<String,Object> getSpecialStatus(@Param("orderId") Long orderId, @Param("quarter") String quarter, @Param("fromMasterDb") Boolean fromMasterDb);

    /*订单费用*/
    OrderFee getOrderFeeById(@Param("orderId") Long orderId, @Param("quarter") String quarter, @Param("fromMasterDb") Boolean fromMasterDb);

    /*订单费用*/
    OrderFee getOrderFeeFromMasterById(@Param("orderId") Long orderId, @Param("quarter") String quarter);

    List<OrderFee> getOrderFeeByIds(List<Long> orderIds);

    /*订单
    Order getOrderById(@Param("orderId") Long orderId, @Param("quarter") String quarter);

    Order getOrderItemsById(@Param("orderId") Long orderId, @Param("quarter") String quarter);

    Order getOrderFromMasterById(@Param("orderId") Long orderId, @Param("quarter") String quarter);
    */
    /**
     * 订单基本信息(json)
     *
     * @param orderId 订单id
     * @return

    String getOrderInfoById(@Param("orderId") Long orderId, @Param("quarter") String quarter);
    */

    /**
     * 按订单id列表批量获得订单信息
     *
     * @param orderIds 订单id列表
     * @return
     comment at 2020-12-03 Ryan
    List<Order> getOrderByIds(List<Long> orderIds);
     */

    /**
     * 读取订单当前网点id
     * @param orderId
     * @param quarter
     * @return
     */
    Long getCurrentServicePointId(@Param("orderId") Long orderId, @Param("quarter") String quarter);

    /*订单扩展表(从主库读) 2017/11/19*/
    OrderCondition getOrderConditionFromMasterById(@Param("orderId") Long orderId, @Param("quarter") String quarter);

    /**
     * 从主库读个别特殊字段，主要给并发处理判断使用 2018/01/16
     * 状态，结帐标记，客评标记，配件标记
     */
    Map<String, Object> getOrderConditionSpecialFromMasterById(@Param("orderId") Long orderId, @Param("quarter") String quarter);

    OrderCondition getOrderConditionImportantPropertiesFromMasterById(@Param("orderId") Long orderId, @Param("quarter") String quarter);

    OrderCondition getOrderConditionImportantInfoById(@Param("orderId") Long orderId, @Param("quarter") String quarter);

    /*订单状态*/
    Integer getConditionStatusById(@Param("orderId") Long orderId, @Param("quarter") String quarter);

    /* 读取app异常标记 */
    Integer getAppAbnormalyFlag(@Param("orderId") Long orderId, @Param("quarter") String quarter);

    /**
     * 统计已分配客户的客服的数据(by customer)
     */
    List<Map<String, Object>> groupAppAbnormalyByKefuOfCustomer(@Param("quarters") List<String> quarters);

    /**
     * 统计未分配客户的客服的数据(by area)
     */
    List<Map<String, Object>> groupAppAbnormalyByKefuOfArea(@Param("quarters") List<String> quarters);

    /* 读取客评标记 */
    Integer getGradeFlag(@Param("orderId") Long orderId, @Param("quarter") String quarter);

    /* 读取对账标记 */
    Integer getChargeFlag(@Param("orderId") Long orderId, @Param("quarter") String quarter);

    String getOrderQuarter(@Param("orderId") Long orderId);

    void insertCondition(OrderCondition map);

    void updateCondition(HashMap<String, Object> condition);

    int kefuComplete(HashMap<String, Object> condition);

    void updateUserInfo(HashMap<String, Object> condition);

    void clearOrderFinishPics(@Param("orderId") Long orderId, @Param("quarter") String quarter);

    //标记为自动对账中
    int signAutoChargeing(HashMap<String, Object> condition);

    //驳回退单/取消申请
    void rejectCancel(HashMap<String, Object> condition);

    void insertFee(OrderFee fee);

    void updateFee(HashMap<String, Object> map);

    /**
     * 修改加急费
     */
    void changeOrderUrgentCharge(@Param("orderId") Long orderId, @Param("quarter") String quarter, @Param("customerUrgentCharge") Double customerUrgentCharge, @Param("engineerUrgentCharge") Double engineerUrgentCharge);

    /**
     * 调整下单金额
     *
     * @param fee
     */
    void adjustmentOrderCharge(OrderFee fee);

    void insertStatus(OrderStatus status);

    int updateStatus(HashMap<String, Object> status);

    //更新orderStatus中催单信息
    int updateReminderInfo(HashMap<String, Object> status);

    // 更新orderCondition.reminderFlag
    int updateConditionReminderFlag(HashMap<String, Object> stauts);

    int updateComplainInfo(HashMap<String, Object> status);

    /**
     * 有过上门记录的网点id(未去重)
     *
     * @param orderId
     * @param quarter
     * @return
     */
    List<Long> getServicePointListFromDetail(@Param("orderId") Long orderId, @Param("quarter") String quarter);

    //region 派单记录

    /**
     * 派单时，新增派单记录
     */
    void insertOrderPlan(OrderPlan model);

    /**
     * 读取网点具体安维师傅的派单记录
     */
    OrderPlan getOrderPlan(@Param("orderId") Long orderId, @Param("quarter") String quarter, @Param("servicePointId") Long servicePointId, @Param("engineerId") Long engineerId);

    /**
     * 订单派单记录
     */
    List<OrderPlan> getOrderPlanList(@Param("orderId") Long orderId, @Param("quarter") String quarter, @Param("isMaster") Integer isMaster);

    Integer getOrderPlanMaxTimes(@Param("orderId") Long orderId, @Param("quarter") String quarter);

    /**
     * 获得网点最初派单日期
     *
     * @param orderId
     * @param quarter
     * @param servicePointId
     * @param engineerId     如有值，具体到安维师傅
     * @return
     */
    Date getOrderStartPlanDateOfServicePoint(@Param("orderId") Long orderId, @Param("quarter") String quarter, @Param("servicePointId") Long servicePointId, @Param("engineerId") Long engineerId);

    /**
     * 更新
     */
    Integer UpdateOrderPlan(HashMap<String, Object> map);

    /**
     * 更新安维上门服务标记
     */
    Integer updateServiceFlagOfOrderPlan(@Param("orderId") Long orderId, @Param("quarter") String quarter, @Param("servicePointId") Long servicePointId, @Param("engineerId") Long engineerId, @Param("serviceFlag") Integer serviceFlag, @Param("updateBy") Long updateBy, @Param("updateDate") Date updateDate);

    void deleteOrderPlan(@Param("orderId") Long orderId, @Param("quarter") String quarter);

    //endregion 派单记录

    /**
     * 返回相同电话在指定时间段内订单数
     *
     * @param phone1     移动电话
     * @param phone2     固话
     * @param customerId 厂商
     * @param startDate  开始时间
     * @param endDate    结束时间
     * @return 最近重单单号
     */
    String checkRepeateOrder(@Param("phone1") String phone1, @Param("phone2") String phone2, @Param("customerId") Long customerId, @Param("startDate") Date startDate, @Param("endDate") Date endDate);

    /**
     * 返回对帐订单列表
     *
     * @param searchModel
     * @return
     */
    List<Map<String, Object>> findIdListForCharge(OrderSearchModel searchModel);
    List<Map<String, Object>> findIdListForChargeByOrderNo(OrderSearchModel searchModel);

    List<OrderChargeViewModel> findConditionFeeForCharge(OrderSearchModel searchModel);
    List<OrderDetailChargeViewModel> findDetailForCharge(@Param("orderId")Long orderId, @Param("quarter") String quarter);

    /**
     * 返回相同电话在指定时间段内导入订单数
     *
     * @param phone     移动电话
     * @param startDate 开始时间
     * @param endDate   结束时间
     * @return
     */
    Integer checkRepeateTempOrder(@Param("phone") String phone, @Param("startDate") Date startDate, @Param("endDate") Date endDate);

    /**
     * 按客户查询未转临时订单列表（不分页）
     *
     * @param order
     * @return
     */
    List<TempOrder> findTempOrder(TempOrder order);

    void insertTempOrder(TempOrder order);

    TempOrder getTempOrder(@Param("id") Long id);

    TempOrder getTempOrderStatus(@Param("id") Long id);

    /**
     * 修改临时订单
     *
     * @param order
     */
    void updateTempOrder(HashMap<String, Object> order);

    /**
     * 对帐时标记异常单
     *
     * @param id
     */
    void setPending(@Param("id") Long id, @Param("updateBy") Long updateBy, @Param("updateDate") Date updateDate);

    /**
     * 按客户+品类+区县读取vip客服列表
     * @param customerId    客户id
     * @param productCategoryId 品类
     * @param areaId    区县id
     * @return
     */
    //List<User> getKefuListOfVipCustomer(@Param("customerId") Long customerId,@Param("areaId") Long areaId,@Param("productCategoryId") Long productCategoryId);

    /**
     * 按客户+品类+区县读取+市+省vip客服列表
     * @param customerId    客户id
     * @param productCategoryId 品类
     * @param areaId    区县id
     * @param cityId    市id
     * @param provinceId  省id
     * @return
     */
    List<User> getKefuListOfVipCustomerNew(@Param("customerId") Long customerId,@Param("areaId") Long areaId,@Param("productCategoryId") Long productCategoryId,
                                            @Param("cityId") Long cityId,@Param("provinceId") Long provinceId);

    /**
     * 按品类+区县+客服类型读取客服列表
     * @param productCategoryId 品类
     * @param areaId    区县id
     * @param subFlag 客服类型
     * @return
     */
    //List<User> getKefuList(@Param("areaId") Long areaId,@Param("productCategoryId") Long productCategoryId,@Param("subFlag") Integer subFla);

    /**
     * 按品类+区县+客服类型+市+省读取客服列表
     * @param productCategoryId 品类
     * @param areaId    区县id
     * @param subFlag 客服类型
     * @param cityId    市id
     * @param provinceId  省id
     * @return
     */
    List<User> getKefuListNew(@Param("areaId") Long areaId,@Param("productCategoryId") Long productCategoryId,@Param("subFlag") Integer subFlag,
                              @Param("cityId") Long cityId,@Param("provinceId") Long provinceId);

    /**
     * 按区域+产品类别随机返回负责该区域的客服信息
     */
    //User getRandomKefu(@Param("areaId") Long areaId,@Param("productCategoryId") Long productCategoryId);


    //region 日志

    /**
     * 新增日志
     */
    void insertProcessLog(OrderProcessLog log);

    /**
     * 返回变更前的订单状态
     * 读取最后两笔，最后一笔的状态如与当前一样，则倒数第二笔就是之前的状态
     *
     * @param orderId
     * @return
     */
    List<Dict> getLastOrderLog(@Param("orderId") Long orderId, @Param("quarter") String quarter);

    /**
     * 按订单id及标志(一个)返回日志
     *
     * @param orderId
     * @param statusFlag
     * @return
     */
    List<OrderProcessLog> getOrderLogs(@Param("orderId") Long orderId, @Param("quarter") String quarter, @Param("statusFlag") Integer statusFlag);

    /**
     * 返回APP需要的日志列表
     *
     * @param orderId
     * @param quarter
     * @return
     */
    List<OrderProcessLog> getAppOrderLogs(@Param("orderId") Long orderId, @Param("quarter") String quarter);

    /**
     * 按订单id及标志列表(多个)返回日志
     *
     * @param orderId
     * @param statusFlags
     * @return
     */
    List<OrderProcessLog> getOrderLogsByFlags(@Param("orderId") Long orderId, @Param("quarter") String quarter,
                                              @Param("statusFlags") List<Integer> statusFlags, @Param("closeFlag") Integer closeFlag);

    //endregion 日志

    //region 上门服务明细

    /**
     * 订单实际服务明细
     *
     * @param orderId
     * @return
     */
    List<OrderDetail> getOrderDetails(@Param("orderId") Long orderId, @Param("quarter") String quarter, @Param("fromMasterDb") Boolean fromMasterDb);

    /**
     * 删除上门服务
     * Modify at 2018/06/25
     * 由逻辑删除变更为物理删除
     */
    void deleteDetail(@Param("id") Long id, @Param("quarter") String quarter);

    void insertDetail(OrderDetail detail);

    /**
     * 更改上门服务费用
     */
    void updateDetail(HashMap<String, Object> params);

    /**
     * 修改上门服务
     *
     * @param params
     */
    void editDetail(HashMap<String, Object> params);

    //endregion 上门服务明细

    /**
     * 生成网点结帐信息时更新结帐日期
     *
     * @param orderDetail
     */
    void updateDetailInvoiceDate(OrderDetail orderDetail);

    /**
     * 更新结帐日期，根据订单ID
     *
     * @param orderDetail
     */
    void updateDetailInvoiceDateByOrderId(OrderDetail orderDetail);

    /**
     * 生成对帐信息时更新订单对帐标记
     *
     * @param orderCondition
     */
    void updateChargeFlag(OrderCondition orderCondition);

    /**
     * 生成对帐结帐数据时更新订单标记
     *
     * @param orderStatus
     */
    void updateStatusFlagsFromCharge(OrderStatus orderStatus);

    //region 客评

    /**
     * 返回待客评的所有项目
     */
    List<OrderGrade> getToOrderGrade();

    List<OrderGrade> getOrderGradeByOrderId(@Param("orderId") Long orderId, @Param("quarter") String quarter);

    void insertOrderGrade(OrderGrade grade);

    void deleteOrderGrade(@Param("orderId") Long orderId, @Param("quarter") String quarter);

    /**
     * 获得指定手机号用户的待客评的订单id列表
     *
     * @param phone
     * @return
     */
    List<OrderCondition> getToGradeOrdersByPhone(@Param("phone") String phone, @Param("quarters") List<String> quarters);

    //endregion 客评

    //region 数据处理

    /**
     * 返回要重置订单列表
     */
    List<Map<String, Object>> findIdsResetProduct();

    //endregion 数据处理

    //region 工单保险单

    /**
     * 获得网点保险费
     *
     * @param quarter
     * @param orderId
     * @param servicePointId
     * @return
     */
    Double getOrderInsuranceAmount(@Param("quarter") String quarter, @Param("orderId") Long orderId, @Param("servicePointId") Long servicePointId);

    /**
     * 获得网点保险单
     * 2021/03/04 改为读主库，防止主从不一致造成保险费未扣费
     */
    OrderInsurance getOrderInsuranceByServicePoint(@Param("quarter") String quarter, @Param("orderId") Long orderId, @Param("servicePointId") Long servicePointId);

    /**
     * 财务对帐获取保险信息
     *
     * @param quarter        订单分片
     * @param orderId        订单ID
     * @param servicePointId 网点ID
     * @return
     */
    OrderInsurance getOrderInsuranceByServicePointForCharge(@Param("quarter") String quarter, @Param("orderId") Long orderId, @Param("servicePointId") Long servicePointId);

    /**
     * 获得订单下所有有效保单
     */
    List<OrderInsurance> getOrderInsurances(@Param("quarter") String quarter, @Param("orderId") Long orderId);

    /**
     * 新增保险单
     */
    void insertOrderInsurance(OrderInsurance insurance);

    /**
     * 更改
     */
    void updateOrderInsurance(OrderInsurance insurance);

    /**
     * 订单回退到派单区，删除订单的保险单记录
     *
     * @param quarter
     * @param orderId
     */
    void deleteOrderInsurance(@Param("quarter") String quarter, @Param("orderId") Long orderId);

    //endregion


    //region 工单网点费用汇总

    /**
     * 获得具体网点的费用
     *
     * @param quarter
     * @param orderId
     * @param servicePointId
     * @return
     */
    OrderServicePointFee getOrderServicePointFee(@Param("quarter") String quarter, @Param("orderId") Long orderId, @Param("servicePointId") Long servicePointId);

    /**
     * 获得订单下所有网点费用汇总
     *
     * @param quarter
     * @param orderId
     * @return
     */
    List<OrderServicePointFee> getOrderServicePointFees(@Param("quarter") String quarter, @Param("orderId") Long orderId, @Param("fromMasterDb") Boolean fromMasterDb);

    @MapKey("servicePoint.id")
    Map<Long, OrderServicePointFee> getOrderServicePointFeesForCharge(@Param("quarter") String quarter, @Param("orderId") Long orderId);

    /**
     * 新增
     */
    void insertOrderServicePointFee(OrderServicePointFee servicePointFee);

    /**
     * 修改网点费用
     *
     * @param maps
     */
    void updateOrderServicePointFeeByMaps(HashMap<String, Object> maps);

    /**
     * 订单保险费合计(返回Null或负数)
     */
    Double getTotalOrderInsurance(@Param("orderId") Long orderId, @Param("quarter") String quarter);

    /**
     * 补时效用
     */
    List<HashMap<String, Object>> getReCreateOrderServicePointFee(@Param("quarter") String quarter, @Param("date") String date);

    /**
     * 回退到派单区，删除该订单的记录
     *
     * @param quarter
     * @param orderId
     */
    void deleteOrderServicePointFee(@Param("quarter") String quarter, @Param("orderId") Long orderId);

    //endregion 工单网点费用汇总

    //region B2B

    /**
     * 按B2B工单id+分片+数据源读取已转换工单系统工单信息
     * 包含:id,order_no,quarter
     *
     * @param dataSource
     * @param workCardId
     * @param quarter
     * @return 工单系统订单号或null
     */
    HashMap<String, Object> getB2BOrderNo(@Param("dataSource") int dataSource, @Param("workCardId") String workCardId, @Param("quarter") String quarter);

    Long getOrderProductCategoryId(@Param("quarter") String quarter, @Param("orderId") Long orderId);

    //endregion

    List<OrderCondition> getConditionForUpdateSubArea(OrderSearchModel orderSearchModel);

    void updateSubAreaId(HashMap<String, Object> map);


    /**
     * 根据突击单区县或街道ID读取同区域以往派单记录
     */
    List<HistoryPlanOrderModel> findOrderListOfCrush(OrderCrushSearchVM searchModel);

    List<HistoryPlanOrderModel> getOrderServiceItemList(@Param("quarter") String quarter, @Param("orderIds") List<Long> orderIds);

    /**
     * 根据订单读取街道id(突击用)
     */
    Long getSubAreaIdByOrderId(@Param("orderId") Long orderId, @Param("quarter") String quarter);

    /**
     * 修改街道,上门地址
     */
    void updateAddress(HashMap<String, Object> condition);

    List<Map<String, Object>> getRepeateTimelinessFeeInfo(@Param("beginDate") Date beginDate,
                                   @Param("endDate") Date endDate,
                                   @Param("customerId") Long customerId,
                                   @Param("servicePointId") Long servicePointId,
                                   @Param("productCategoryId") Long productCategoryId,
                                   @Param("userPhone") String userPhone,
                                   @Param("quarters") List<String> quarters
                                   );


    /**
     * 按订单id及标志列表(多个)返回日志（分页获取）
     * @param orderTrackingSearchModel
     * @return
     */
    List<OrderProcessLog> getOrderLogsByFlagsNew(OrderTrackingSearchModel orderTrackingSearchModel);


    OrderFee getPresetFeeWhenPlanFromMasterDB(@Param("orderId") Long orderId, @Param("quarter") String quarter);


    /**
     * 根据客户id查询一条工单id(用于判断删除客户时,客户是否有下过工单)
     * @param customerId
     * @return
     */
    Long getOrderIdByCustomerId(@Param("customerId") Long customerId);

    /**
     * 根据工单id获取店铺,数据源,第三方单号
     * @param orderId
     * @param quarter
     * @return
     */
    Order getB2BInfoById(@Param("orderId") Long orderId,@Param("quarter") String quarter);

}
