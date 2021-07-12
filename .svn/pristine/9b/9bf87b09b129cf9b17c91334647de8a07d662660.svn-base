package com.wolfking.jeesite.modules.sd.dao;

import com.wolfking.jeesite.common.persistence.LongIDCrudDao;
import com.wolfking.jeesite.modules.sd.entity.MaterialLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author Ryan Lu
 * @version 1.0.0
 * @description 订单配件访问接口
 * @date 2019/5/31 11:24 AM
 */
@Mapper
public interface OrderMaterialLogDao extends LongIDCrudDao<MaterialLog> {

    //region 日志

    /**
     * 新增日志
     */
    public void insertLog(MaterialLog log);

    /**
     * 读取配件跟踪进度记录
     */
    public List<MaterialLog> getLogs(@Param("materialMasterId") long orderId, @Param("quarter") String quarter, @Param("sortBy") String sortBy);


    //endregion 日志

}
