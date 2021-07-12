package com.wolfking.jeesite.ms.tmall.md.dao;

import com.wolfking.jeesite.ms.tmall.md.entity.B2BServicePointBatchLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface B2BServicePointBatchLogDao {

    List<B2BServicePointBatchLog> getAllServicePointBatchLogs();

    B2BServicePointBatchLog getServicePointBatchLogByCityId(@Param("cityId") Long cityId);

    int insert(B2BServicePointBatchLog b2BServicePointBatchLog);

    int update(B2BServicePointBatchLog b2BServicePointBatchLog);

}

