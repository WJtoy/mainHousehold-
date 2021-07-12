package com.wolfking.jeesite.modules.finance.md.dao;

import com.wolfking.jeesite.modules.md.entity.ServicePointFinance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FiServicePointDao {

    /**
     * 从主库中获取获取服务网点帐务信息
     * @param id
     * @return
     */
    ServicePointFinance getFinanceFromMaster(@Param("id") Long id);

    /**
     * 从网点财务表中获取网点id列表  //add on 2019-12-29
     * @param servicePointFinance
     * @return
     */
    List<Long> findServicePointIdsFromFinance(ServicePointFinance servicePointFinance);

    /**
     * 新增FI
     */
    int insertFI(ServicePointFinance fi);

    /**
     * 获取网点的结算方式
     */
    Integer getServicePointPaymentType(@Param("servicePointId") Long servicePointId);

    /**
     * 修改FI
     */
    int updateFI(ServicePointFinance fi);
}
