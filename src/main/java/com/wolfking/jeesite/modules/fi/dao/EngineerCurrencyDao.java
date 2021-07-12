package com.wolfking.jeesite.modules.fi.dao;

import com.wolfking.jeesite.common.persistence.LongIDCrudDao;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.modules.fi.entity.EngineerCurrency;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Jeff on 2017/4/19.
 */
@Mapper
public interface EngineerCurrencyDao extends LongIDCrudDao<EngineerCurrency> {

    List<EngineerCurrency> getServicePointCurrencyList(@Param("servicePointId") Long servicePointId,
                                                       @Param("actionType") Integer actionType,
                                                       @Param("beginDate") Date beginDate,
                                                       @Param("endDate") Date endDate,
                                                       @Param("currencyNo") String currencyNo,
                                                       @Param("page") Page<EngineerCurrency> page);

    List<EngineerCurrency> getServicePointCurrencyListWithoutServicePointAndEngineer(@Param("servicePointId") Long servicePointId,
                                                       @Param("actionType") Integer actionType,
                                                       @Param("beginDate") Date beginDate,
                                                       @Param("endDate") Date endDate,
                                                       @Param("currencyNo") String currencyNo,
                                                       @Param("page") Page<EngineerCurrency> page);


    List<EngineerCurrency> getServicePointCurrencyListForApi(@Param("servicePointId") Long servicePointId,
                                                       @Param("actionType") Integer actionType,
                                                       @Param("beginDate") Date beginDate,
                                                       @Param("endDate") Date endDate,
                                                       @Param("currencyNo") String currencyNo,
                                                       @Param("quarters") List<String> quarters,
                                                       @Param("page") Page<EngineerCurrency> page);

    /**
     * 网点帐户明细按月汇总
     * @param servicePointId
     * @param actionType
     * @param beginDate
     * @param endDate
     * @return
     */
    List<Map<String,Object>> getServicePointCurrencySummryByMonthApi(@Param("servicePointId") Long servicePointId,
                                                                     @Param("actionType") Integer actionType,
                                                                     @Param("beginDate") Date beginDate,
                                                                     @Param("endDate") Date endDate);

    List<EngineerCurrency> getModifyList();

    EngineerCurrency getFirstCurrency(@Param("servicePointId") long servicePointId);

    void updateMSF(HashMap<String, Object> map);

//    void updateFSPM(HashMap<String, Object> map);

//    void updateFSIM(HashMap<String, Object> map);

//    void updateFSBM(HashMap<String, Object> map);

//    void updateCurrency(HashMap<String, Object> map);

    void deleteById(HashMap<String, Object> map);

    EngineerCurrency getOnePrevious(@Param("id") Long id, @Param("servicePointId") Long servicePointId);

    EngineerCurrency getOneNext(@Param("id") Long id, @Param("servicePointId") Long servicePointId);

    /**
     * 补10月网点按品类应付数据
     * @param page
     * @param servicePointId
     * @return
     */
    List<EngineerCurrency> getCurrencyByDateRangeForUpdatePayable(@Param("page") Page<EngineerCurrency> page, @Param("servicePointId") Long servicePointId,
                                                  @Param("beginDate") String beginDate, @Param("endDate") String endDate);
}
