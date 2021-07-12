/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.wolfking.jeesite.modules.sd.service;

import com.wolfking.jeesite.common.service.LongIDBaseService;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.sd.dao.OrderServicepointReceivableDao;
import com.wolfking.jeesite.modules.sd.entity.OrderServicepointReceivable;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * 网点应收应付表服务层
 */
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Slf4j
public class OrderServicepointReceivableService extends LongIDBaseService {

    /**
     * 持久层对象
     */
    @Resource
    protected OrderServicepointReceivableDao dao;

    /**
     * 新增
     */
    @Transactional(readOnly = false)
    public void insert(OrderServicepointReceivable entity) {
        dao.insert(entity);
    }

    /**
     * 按订单id读取
     * @param orderId
     * @param quarter
     * @param servicePointId 网点id
     * @param itemNo    应收应付项目
     * @return
     */
    public List<OrderServicepointReceivable> getByOrderId(long orderId, String quarter,Integer itemNo,Long servicePointId){
        if(orderId <= 0 || StringUtils.isBlank(quarter) ){
            return null;
        }
        return dao.getByOrderId(orderId,quarter,itemNo,servicePointId);
    }

    /**
     * 按网点+应收应付项目更新
     * @param orderId   订单id
     * @param quarter   分片
     * @param servicePointId 网点id
     * @param itemNo    应收项目
     * @param amount    金额
     * @param formNo    表单号
     * @param remark    备注
     * @return
     */
    @Transactional
    public int updateByItemAndServicePoint( long orderId, String quarter, long servicePointId,int itemNo,
                                            double amount, String formNo, String remark,
                                            long updateBy,long updateAt){
        if(orderId <= 0 || StringUtils.isBlank(quarter) || itemNo <= 0){
            return 0;
        }
        return dao.updateByItemAndServicePoint(orderId,quarter,servicePointId,itemNo,amount,formNo,remark,updateBy,updateAt);
    }

    @Transactional
    public int switchEnabled(long orderId, String quarter,long servicePointId ,int itemNo,int delFlag, long updateBy,long updateAt){
        return dao.switchEnabled(orderId,quarter,servicePointId,itemNo,delFlag,updateBy,updateAt);
    }
}
