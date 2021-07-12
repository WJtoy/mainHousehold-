package com.wolfking.jeesite.modules.sd.dao;

import com.wolfking.jeesite.common.persistence.LongIDCrudDao;
import com.wolfking.jeesite.modules.sd.entity.OrderServicePoint;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 订单客评访问接口
 * Created on 2018-10-09
 */
@Mapper
public interface OrderServicePointDao extends LongIDCrudDao<OrderServicePoint> {


    public List<OrderServicePoint> findByOrder(@Param("orderId") Long orderId, @Param("quarter") String quarter,@Param("delFlag") Integer delFlag);

    public Integer getNextPlanOrder(@Param("orderId") Long orderId, @Param("quarter") String quarter);


    public OrderServicePoint findByOrderAndServicePoint(@Param("orderId") Long orderId, @Param("quarter") String quarter, @Param("servicePointId") Long servicePointId);

    public int updateData(Map<String,Object> params);

    // 更新非当前网点数据
    public int updateNotActiveServiePoint(Map<String,Object> params);

    // 更新指定网点数据
    public int updateByServiePoint(Map<String,Object> params);

    // 按订单id批量更新
    public int updateByOrder(Map<String,Object> params);

    // 设定当前网点
    public int activeServicePoint(OrderServicePoint entity);


    // 取消当前网点并更新状态(在新增网点之前执行）
    public int unActiveServicePointByOrder(
            @Param("orderId") Long orderId,
            @Param("quarter") String quarter,
            @Param("updator") long updator,
            @Param("updateDate") Date updateDate,
            @Param("exceptId") Long exceptId,
            @Param("status") Integer status,
            @Param("subStatus") Integer subStatus,
            @Param("complainFlag") Integer complainFlag,
            @Param("abnormalyFlag") Integer abnormalyFlag,
            @Param("reminderFlag") Integer reminderFlag,
            @Param("reminderSort") Integer reminderSort,
            @Param("urgentLevelId") Integer urgentLevelId
            );

    // 标记为逻辑删除(在新增网点之前执行）
    //public int logicDeleteRecordByOrder(@Param("orderId") Long orderId, @Param("quarter") String quarter, @Param("updator") long updator, @Param("updateDate") Date updateDate,@Param("exceptId") Long exceptId);

    public int goBackToAccept(OrderServicePoint entity);

    //region 好评单
    // 更新指定网点
    public void updatePraiseStatusByServiePoint(@Param("orderId") Long orderId,
                                                @Param("quarter") String quarter,
                                                @Param("servicePointId") Long servicePointId,
                                                @Param("praiseStatus") Integer praiseStatus,
                                                @Param("updateBy") long updateBy,
                                                @Param("updateDate") Date updateDate);

    // 更新非指定网点
    public void updatePraiseStatusByExceptServiePoint(@Param("orderId") Long orderId,
                                                @Param("quarter") String quarter,
                                                @Param("exceptId") Long exceptId,
                                                @Param("praiseStatus") Integer praiseStatus,
                                                @Param("updateBy") long updateBy,
                                                @Param("updateDate") Date updateDate);

    //endregion 好评单
}
