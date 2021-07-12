package com.wolfking.jeesite.modules.fi.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.kkl.kklplus.entity.md.MDServicePointViewModel;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.service.LongIDCrudService;
import com.wolfking.jeesite.common.utils.QuarterUtils;
import com.wolfking.jeesite.modules.fi.dao.EngineerChargeDao;
import com.wolfking.jeesite.modules.fi.dao.EngineerCurrencyDao;
import com.wolfking.jeesite.modules.fi.dao.EngineerCurrencyDepositDao;
import com.wolfking.jeesite.modules.fi.entity.*;
import com.wolfking.jeesite.modules.fi.entity.viewModel.EngineerDepositVM;
import com.wolfking.jeesite.modules.md.dao.ServicePointDao;
import com.wolfking.jeesite.modules.md.entity.Engineer;
import com.wolfking.jeesite.modules.md.entity.ServicePoint;
import com.wolfking.jeesite.modules.md.entity.ServicePointFinance;
import com.wolfking.jeesite.modules.md.service.ServicePointService;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.ms.providermd.service.MSServicePointService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 网点质保金流水
 */
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Slf4j
public class EngineerCurrencyDepositService extends LongIDCrudService<EngineerCurrencyDepositDao, EngineerCurrencyDeposit> {

    @Autowired
    private MSServicePointService msServicePointService;

    @Autowired
    private ServicePointService servicePointService;

    /**
     * 分页查询质保金流水
     * @param page
     * @param engineerDeposit
     * @return
     */
    public Page<EngineerCurrencyDeposit> findCurrencyList(Page<EngineerCurrencyDeposit> page, EngineerDepositVM engineerDeposit){
        engineerDeposit.setPage(page);
        List<EngineerCurrencyDeposit> currencyList = dao.findCurrencyList(engineerDeposit);
        page.setList(currencyList);
        return page;
    }

    /**
     * 质保金充值
     * @param currency
     */
    @Transactional
    public void recharge(EngineerCurrencyDeposit currency){
        if(StringUtils.isNotBlank(currency.getCurrencyNo())) {
            Long id = dao.getCurrencyIdByCurrencyNoFromMaster(currency.getCurrencyNo());
            if (id != null) {
                throw new RuntimeException(MessageFormat.format("单号{0}已存在，<br>不能重复使用。", currency.getCurrencyNo()));
            }
        }
        long cnt = servicePointService.depositRecharge(currency);
        if(cnt > 0){
            //流水
            if(currency.getRemarks() == null) {
                currency.setRemarks(StringUtils.EMPTY);
            }else{
                currency.setRemarks(StringUtils.left(currency.getRemarks(), 200));
            }
            currency.setActionType(FIEnums.DepositActionTypeENum.OFFLINE_RECHARGE.getValue());
            currency.setCurrencyType(FIEnums.CurrencyTypeENum.IN.getValue());
            dao.insert(currency);
        }
    }

}
