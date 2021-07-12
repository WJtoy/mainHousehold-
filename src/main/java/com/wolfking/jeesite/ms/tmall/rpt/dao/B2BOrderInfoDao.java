package com.wolfking.jeesite.ms.tmall.rpt.dao;

import com.kkl.kklplus.entity.b2b.order.B2BWorkcardQtyDaily;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface B2BOrderInfoDao {


    Map<String,Object> getOrderIdQuarter(@Param("workCardId") String workCardId);
}
