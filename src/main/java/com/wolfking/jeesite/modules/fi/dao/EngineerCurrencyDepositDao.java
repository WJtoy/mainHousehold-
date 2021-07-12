package com.wolfking.jeesite.modules.fi.dao;

import com.wolfking.jeesite.common.persistence.LongIDCrudDao;
import com.wolfking.jeesite.modules.fi.entity.EngineerCurrencyDeposit;
import com.wolfking.jeesite.modules.fi.entity.viewModel.EngineerDepositVM;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 网点质保金流水
 */
@Mapper
public interface EngineerCurrencyDepositDao extends LongIDCrudDao<EngineerCurrencyDeposit> {

    List<EngineerCurrencyDeposit> findCurrencyList(EngineerDepositVM engineerDeposit);

    /**
     * 从主库按相关充值单号查询流水id,用于检查是否重复充值
     * @param currencyNo
     * @return
     */
    Long getCurrencyIdByCurrencyNoFromMaster(@Param("currencyNo") String currencyNo);

}
