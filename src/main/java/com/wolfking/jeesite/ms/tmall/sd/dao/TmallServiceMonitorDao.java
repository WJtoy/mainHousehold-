package com.wolfking.jeesite.ms.tmall.sd.dao;

import com.wolfking.jeesite.common.persistence.LongIDCrudDao;
import com.wolfking.jeesite.ms.tmall.sd.entity.TmallServiceMonitor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * 天猫预警记录表
 */
@Mapper
public interface TmallServiceMonitorDao extends LongIDCrudDao<TmallServiceMonitor> {

    List<TmallServiceMonitor> getListByOrderId(@Param("orderId") Long orderId,@Param("quarter") String quarter);

}
