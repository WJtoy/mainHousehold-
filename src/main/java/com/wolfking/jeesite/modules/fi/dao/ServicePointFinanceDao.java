package com.wolfking.jeesite.modules.fi.dao;

import com.wolfking.jeesite.modules.md.entity.ServicePointFinance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ServicePointFinanceDao {
    /**
     * 根据用ID列表获取网点余额
     * @param ids
     * @return
     */
    List<ServicePointFinance> getBalanceByIds(@Param("ids")List<Long> ids);

    /**
     * 从网点财务表中获取网点id列表  //add on 2019-12-29
     * @param servicePointFinance
     * @return
     */
    List<Long>  findServicePointIdsFromFinance(ServicePointFinance servicePointFinance);
}
