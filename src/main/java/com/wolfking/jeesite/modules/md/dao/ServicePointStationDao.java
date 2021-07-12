package com.wolfking.jeesite.modules.md.dao;

import com.wolfking.jeesite.common.persistence.LongIDCrudDao;
import com.wolfking.jeesite.modules.md.entity.ServicePointStation;
import com.wolfking.jeesite.modules.sys.entity.Area;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 网点服务点DAO
 */
@Mapper
public interface ServicePointStationDao extends LongIDCrudDao<ServicePointStation> {
    /**
     *
     * @param servicePointStation
     * @return
     */
    //List<ServicePointStation> findAutoPlanList(ServicePointStation servicePointStation); //mark on 2020-1-20 web端去md_servicepoint_station

    /**
     * 通过网点id和区域id查询服务点
     * @param servicePointStation
     * @return
     */
    //ServicePointStation getByServicePointIdAndAreaId(ServicePointStation servicePointStation); //mark on 2020-1-20 web端去md_servicepoint_station

    /**
     * 通过网点id查找该网点是否有自动派单(即autoPlanFlag 为1)
     * @param servicePointId
     * @return
     */
    //Long autoPlanByServicePointId(@Param("servicePointId") Long servicePointId);   //mark on 2020-1-20 web端去md_servicepoint_station
}
