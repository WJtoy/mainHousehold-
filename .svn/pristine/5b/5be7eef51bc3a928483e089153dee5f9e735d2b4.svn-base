package com.wolfking.jeesite.modules.fi.dao;

import com.kkl.kklplus.entity.fi.servicepoint.ServicePointDeductedDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author: jeff.zhao
 * @date: 2019/10/28 14:11
 * @Description:
 */
@Mapper
public interface ServicePointDeductedDetailDao {
    /**
     * 获取抵扣款列表按品类
     * @param withdrawId
     * @return
     */
    List<ServicePointDeductedDetail> getDeductedDetailAmountList(@Param("withdrawId") Long withdrawId);
}
