package com.wolfking.jeesite.ms.canbo.rpt.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.HashMap;


@Mapper
public interface CanboProcessLogDao {

     HashMap<String,Object> getOrderInfoByCanbo(@Param("dataSource") Integer dataSource,
                                                      @Param("workcardId") String workcardId
                                                      );
}
