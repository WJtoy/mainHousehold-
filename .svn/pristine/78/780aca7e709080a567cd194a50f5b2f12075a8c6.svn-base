package com.wolfking.jeesite.modules.md.dao;

import com.wolfking.jeesite.common.persistence.LongIDCrudDao;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.modules.fi.entity.ServicePointPayCondition;
import com.wolfking.jeesite.modules.fi.entity.ServicePointPayableMonthly;
import com.wolfking.jeesite.modules.fi.entity.ServicePointPayableMonthlyDetail;
import com.wolfking.jeesite.modules.md.entity.Engineer;
import com.wolfking.jeesite.modules.md.entity.ServicePoint;
import com.wolfking.jeesite.modules.md.entity.ServicePointFinance;
import com.wolfking.jeesite.modules.md.entity.ServicePrice;
import com.wolfking.jeesite.modules.sd.entity.LongTwoTuple;
import com.wolfking.jeesite.modules.sys.entity.User;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 客户数据访问接口
 * Created on 2017-04-12.
 */
@Mapper
public interface ServicePointDao extends LongIDCrudDao<ServicePoint> {

    //region 网点

    /**
     * 新增FI
     */
    int insertFI(ServicePointFinance fi);

    /**
     * 修改FI
     */
    int updateFI(ServicePointFinance fi);

    /**
     * 查询待审核数据列表，如果需要分页，请设置分页对象，如：entity.setPage(new Page<T>());
     * @return
     */
    List<ServicePoint> findApproveList(ServicePoint servicePoint);

    /**
     * 将网点下其余帐号对应的用户信息改为子帐号
     * @param engineerIds
     */
    void resetUserEngineerSubFlag(List<Long> engineerIds);

    List<ServicePointFinance> findAllFinanceList(@Param("page") Page<ServicePointFinance> page);

    /**
     * 获取服务网点帐务信息
     * @param id
     * @return
     */
    ServicePointFinance getFinance(@Param("id") Long id);


    /**
     * 批量获取网点账务信息  // add on 2019-11-9
     * @param ids
     * @return
     */
    List<ServicePointFinance> findFinanceListByIds(@Param("ids") List<Long> ids);
    /**
     * 获取服务网点帐务信息
     * @param id
     * @return
     */
    ServicePointFinance getFinanceNew(@Param("id") Long id);

    /**
     * 获取网点账务的扣点数据
     * @param id
     * @return
     */
    ServicePointFinance getFinanceForDiscount(@Param("id") Long id);

    /**
     * 从主库中获取获取服务网点帐务信息
     * @param id
     * @return
     */
    ServicePointFinance getFinanceFromMaster(@Param("id") Long id);


    /**
     * 获取服务网点帐务信息 -- api获取网点余额信息
     * @param id
     * @return
     */
    ServicePointFinance getFinanceForRestBalance(@Param("id") Long id);

    /**
     * 获取网点所有金额
     * @param id
     * @return
     */
    ServicePointFinance getAmounts(@Param("id") Long id);

    /**
     * 获取网点当前余额
     * @param servicePointId
     * @return
     */
    ServicePointFinance getBalanceById(@Param("servicePointId") Long servicePointId);

    /**
     * 批量获取网点金额与扣点标识
     * @param servicePointIds
     * @return
     */
    @MapKey("id")
    Map<Long, ServicePointFinance> getBalanceAndDiscountAndDeposit(@Param("servicePointIds") List<Long> servicePointIds);

    /**
     * 获取网点金额与扣点标识
     * @param servicePointId
     * @return
     */
    ServicePointFinance getBalanceAndDiscountById(@Param("servicePointId") Long servicePointId);

    /**
     * 获取网点扣点标识
     * @param servicePointId
     * @return
     */
    ServicePointFinance getDiscountFlagById(@Param("servicePointId") Long servicePointId);

    /**
     * 更新网点可提现余额
     * @param servicePointFinance
     */
    void updateBalance(ServicePointFinance servicePointFinance);


    /**
     * 更新网点付款失败原因
     * @param servicePointFinance
     */
    void updateBankIssueFI(ServicePointFinance servicePointFinance);

    /**
     * 网点付款
     * @param servicePointFinance
     */
    void payServicePoint(ServicePointFinance servicePointFinance);

    //endregion 网点

    //region 安维

    /**
     * 按手机号返回安维帐号
     * @param exceptId  排除的安维id
     * @param mobile    手机号
     * @return
     */
    User getEngineerByPhoneExpect(@Param("mobile") String mobile,@Param("exceptId") Long exceptId);

    /**
     * 重置安维人员密码（手机号后6位）
     * @param engineer
     */
    void resetPassword(User engineer);

    int updateUser(Engineer entity);


    /**
     * 逻辑删除用户
     */
    int deleteUser(Engineer entity);

    //endregion 安维人员


    //region 报表

    /**
     * 读取网点余额,最后付款日期及最后付款金额
     * @param servicePointIds
     * @return
     */
    List<Map<String,Object>> getServicePointBalances(@Param("servicePointIds") List<Long> servicePointIds);

    /**
     * 获取即结未付款清单(去ServicePoint关联) add on 2019-9-29
     * @return
     */
    List<ServicePointPayCondition> getPayableDailyListWithoutServicePoint(@Param("exceptBankIds") List<Integer> exceptBankIds);

    /**
     * 获取月结未付款清单(去ServicePoint关联) add on 2019-9-29
     * @return
     */
    List<ServicePointPayCondition> getPayableMonthlyListWithoutServicePoint(@Param("exceptBankIds") List<Integer> exceptBankIds);

    /**
     * 获取即结未付款清单明细(去ServicePoint) //add on 2019-9-29
     * @param servicePointFinance
     * @return
     */
    List<ServicePoint> getPayableDailyDetailListWithoutServicePoint(@Param("finance")ServicePointFinance servicePointFinance, @Param("exceptBankIds") List<Integer> exceptBankIds, @Param("areaId") Long areaId);

    /**
     * 从网点财务表中获取网点id列表  //add on 2019-12-29
     * @param servicePointFinance
     * @return
     */
    List<Long>  findServicePointIdsFromFinance(ServicePointFinance servicePointFinance);

    /**
     * 获取月结未付款清单明细(去ServicePoint) //add on 2019-9-29
     * @param servicePointFinance
     * @return
     */
    List<ServicePoint> getPayableMonthlyDetailListWithoutServicePoint(@Param("finance")ServicePointFinance servicePointFinance, @Param("exceptBankIds") List<Integer> exceptBankIds, @Param("areaId") Long areaId);

    /**
     * 获取应付为负月结列表
     * @return
     */
    List<ServicePointPayableMonthly> getPayableMinusMonthlyList();

    /**
     * 获取应付为负月结列表--根据网点ID
     * @return
     */
    List<ServicePointPayableMonthly> getPayableMinusMonthlyListByServicePointId(@Param("servicePointId")Long servicePointId);

    /**
     * 获取应付为负月结列表--根据网点ID,品类
     * @return
     */
    List<ServicePointPayableMonthlyDetail> getPayableMinusMonthlyDetailList(@Param("details")List<ServicePointPayableMonthlyDetail> details);

    /**
     * 获取网点的结算方式
     */
    Integer getServicePointPaymentType(@Param("servicePointId") Long servicePointId);

    /**
     * 根据用ID列表获取网点余额
     * @param ids
     * @return
     */
    List<ServicePointFinance> getBalanceByIds(@Param("ids")List<Long> ids);

    /**
     * 根据ID列表获取网点质保金余额
     * @param ids
     * @return
     */
    List<ServicePointFinance> getDepositByIds(@Param("ids")List<Long> ids);

    /**
     * 根据ID从主库获取网点质保金余额
     */
    ServicePointFinance getDepositFromMasterById(@Param("id") Long id);

    /**
     * 质保金充值时更新质保金余额(累加，而不是更新)
     * 同时，也更新充值的质保金余额(deposit_recharge)
     * @param id
     * @param deposit
     * @return
     */
    long updateDepositWhenRecharge(@Param("id") Long id,@Param("deposit") double deposit);

    /**
     * 更新网点银行账号信息
     * 参数：bank.value、branch、bankNo、bankOwner
     */
    void updateServicePointFIBankAccountInfo(ServicePointFinance servicePointFinance);


    /**
     * 获取所有网点产品映射关系
     */
    List<LongTwoTuple> findAllServicePointProductCategoryMapping();
}
