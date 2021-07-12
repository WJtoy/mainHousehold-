package com.wolfking.jeesite.modules.api.dao;

import com.wolfking.jeesite.common.persistence.BaseDao;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.modules.api.entity.common.IntegerDoubleTuple;
import com.wolfking.jeesite.modules.fi.entity.EngineerCharge;
import com.wolfking.jeesite.modules.fi.entity.EngineerChargeMaster;
import com.wolfking.jeesite.modules.fi.entity.EngineerCurrency;
import com.wolfking.jeesite.modules.md.entity.ServicePointFinance;
import com.wolfking.jeesite.modules.sd.entity.Order;
import com.wolfking.jeesite.modules.sd.entity.OrderCondition;
import com.wolfking.jeesite.modules.sd.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * App访问接口
 */
@Mapper
public interface AppMyWalletDao extends BaseDao {

    /**
     * 获取服务网点帐务信息
     */
    ServicePointFinance getServicePointBalance(@Param("servicePointId") Long servicePointId);

    /**
     * 第一次网点流水的创建时间
     */
    EngineerCurrency getFirstCurrencyCreateDate(@Param("servicePointId") Long servicePointId);

    /**
     * 获取网点的完工单金额
     */
    List<EngineerChargeMaster> getServicePointCompletedChargeList(@Param("quarter") String quarter,
                                                                  @Param("servicePointId") Long servicePointId,
                                                                  @Param("beginCreateDate") Date beginCreateDate,
                                                                  @Param("endCreateDate") Date endCreateDate,
                                                                  @Param("page") Page<EngineerChargeMaster> page);

    /**
     * 获取网点的完工总金额与项目数量
     */
    IntegerDoubleTuple getServicePointCompletedChargeSummary(@Param("quarter") String quarter,
                                                            @Param("servicePointId") Long servicePointId,
                                                            @Param("beginCreateDate") Date beginCreateDate,
                                                            @Param("endCreateDate") Date endCreateDate);

    /**
     * 获取完工账单数量
     */
    Integer getServicePointCompletedQty(@Param("quarter") String quarter,
                                        @Param("servicePointId") Long servicePointId,
                                        @Param("beginCreateDate") Date beginCreateDate,
                                        @Param("endCreateDate") Date endCreateDate);

    /**
     * 获取网点的退补项目数量
     */
    Integer getServicePointWriteOffQty(@Param("quarter") String quarter,
                                       @Param("servicePointId") Long servicePointId,
                                       @Param("beginCreateDate") Date beginCreateDate,
                                       @Param("endCreateDate") Date endCreateDate);

    /**
     * 获取网点的退补单金额
     */
    List<EngineerCharge> getServicePointWriteOffChargeList(@Param("quarter") String quarter,
                                                           @Param("servicePointId") Long servicePointId,
                                                           @Param("beginCreateDate") Date beginCreateDate,
                                                           @Param("endCreateDate") Date endCreateDate,
                                                           @Param("page") Page<EngineerCharge> page);

    /**
     * 获取网点的退补总金额和账单数量
     */
    IntegerDoubleTuple getServicePointWriteOffChargeSummary(@Param("quarter") String quarter,
                                                            @Param("servicePointId") Long servicePointId,
                                                            @Param("beginCreateDate") Date beginCreateDate,
                                                            @Param("endCreateDate") Date endCreateDate);

    /**
     * 获取网点的提现明细
     */
    List<EngineerCurrency> getServicePointWithdrawList(@Param("quarter") String quarter,
                                                       @Param("servicePointId") Long servicePointId,
                                                       @Param("beginCreateDate") Date beginCreateDate,
                                                       @Param("endCreateDate") Date endCreateDate,
                                                       @Param("page") Page<EngineerCurrency> page);

    /**
     * 获取网点的提现总金额
     */
    Double getServicePointWithdrawTotalCharge(@Param("quarter") String quarter,
                                              @Param("servicePointId") Long servicePointId,
                                              @Param("beginCreateDate") Date beginCreateDate,
                                              @Param("endCreateDate") Date endCreateDate);

    /**
     * 获取网点的完工账单项的详情
     */
    EngineerChargeMaster getServicePointCompletedChargeDetail(@Param("quarter") String quarter,
                                                              @Param("engineerChargeMasterId") Long engineerChargeMasterId);

    /**
     * 获取网点的退补账单项的详情
     */
    EngineerCharge getServicePointWriteOffChargeDetail(@Param("quarter") String quarter,
                                                       @Param("engineerChargeId") Long engineerChargeId);

    /**
     * 获取工单信息
     */
    OrderCondition getOrderInfo(@Param("quarter") String quarter,
                                @Param("orderId") Long orderId);

    /**
     * 实际上门服务项目
     */
    List<OrderDetail> getOrderDetailList(@Param("quarter") String quarter,
                                         @Param("orderId") Long orderId);
}
