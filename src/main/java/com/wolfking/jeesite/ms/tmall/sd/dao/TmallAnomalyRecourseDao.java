package com.wolfking.jeesite.ms.tmall.sd.dao;

import com.wolfking.jeesite.common.persistence.LongIDCrudDao;
import com.wolfking.jeesite.ms.tmall.md.entity.B2bCustomerMap;
import com.wolfking.jeesite.ms.tmall.sd.entity.TmallAnomalyRecourse;
import com.wolfking.jeesite.ms.tmall.sd.entity.ViewModel.TmallAnomalyRecourseSearchVM;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 *  天猫一键求助
 */
@Mapper
public interface TmallAnomalyRecourseDao extends LongIDCrudDao<TmallAnomalyRecourse> {

    /**
     * 按厂商获得说有关联店铺id(可能1:n)
     */
    List<TmallAnomalyRecourse> getListByOrder(@Param("orderId") Long  orderId, @Param("orderNo") String orderNo,@Param("quarter") String quarter);

    List<TmallAnomalyRecourse> findList(TmallAnomalyRecourseSearchVM searchModel);
}
