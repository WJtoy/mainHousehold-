/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.wolfking.jeesite.modules.sd.service;

import com.wolfking.jeesite.common.service.LongIDBaseService;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.sd.dao.OrderPayableDao;
import com.wolfking.jeesite.modules.sd.entity.OrderPayable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * 订单应付表服务层
 */
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Slf4j
public class OrderPayableService extends LongIDBaseService {

    /**
     * 持久层对象
     */
    @Resource
    protected OrderPayableDao dao;

    /**
     * 新增
     */
    @Transactional(readOnly = false)
    public void insert(OrderPayable entity) {
        dao.insert(entity);
    }

    /**
     * 按订单id读取
     * @param orderId
     * @param quarter
     * @return
     */
    public List<OrderPayable> getByOrderId(long orderId, String quarter){
        if(orderId <= 0 || StringUtils.isBlank(quarter) ){
            return null;
        }
        return dao.getByOrderId(orderId,quarter);
    }

    /**
     * 按应付项目更新
     * @param orderId   订单id
     * @param quarter   分片
     * @param itemNo    应收项目
     * @param amount    金额
     * @param formNo    表单号
     * @param remark    备注
     * @return
     */
    @Transactional
    public int updateByItem(long orderId, String quarter, int itemNo,
                            double amount, String formNo, String remark,
                            long updateBy,long updateAt){
        if(orderId <= 0 || StringUtils.isBlank(quarter) || itemNo <= 0){
            return 0;
        }
        return dao.updateByItem(orderId,quarter,itemNo,amount,formNo,remark,updateBy,updateAt);
    }

    /**
     * 按应付项目切换有效状态
     * @param orderId
     * @param quarter
     * @param itemNo 应付项目
     * @param delFlag   状态 1:无效 0:有效
     * @param updateBy
     * @param updateAt
     * @return
     */
    @Transactional
    public int switchEnabled(long orderId, String quarter, String itemNo, int delFlag, long updateBy,long updateAt){
        return dao.switchEnabled(orderId,quarter,itemNo,delFlag,updateBy,updateAt);
    }

}
