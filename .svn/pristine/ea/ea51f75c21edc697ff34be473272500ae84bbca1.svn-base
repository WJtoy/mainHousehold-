package com.wolfking.jeesite.modules.servicepoint.ms.receipt;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.praise.Praise;
import com.wolfking.jeesite.modules.sd.entity.Order;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.ms.praise.feign.OrderPraiseFeign;
import com.wolfking.jeesite.ms.praise.service.OrderPraiseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SpOrderPraiseService {

    @Autowired
    private OrderPraiseService orderPraiseService;

    @Autowired
    private OrderPraiseFeign orderPraiseFeign;

    /**
     * 根据订单号,分片获取好评单
     * @param quarter
     * @param orderId
     */
    public Praise getByOrderId(String quarter,Long orderId,Long servicePointId){
        Praise praise = orderPraiseService.getByOrderId(quarter,orderId,servicePointId);
        return praise;
    }

    /**
     * 根据好评单id,分片获取好评单
     * @param quarter
     * @param orderId
     */
    public Praise getByPraiseId(String quarter,Long orderId){
        return orderPraiseService.getPraiseInfo(quarter,orderId);
    }

    /**
     * 好评单重新提交
     */
    public void resubmit(Praise praise){
        MSResponse<Integer> msResponse = orderPraiseFeign.resubmit(praise);
        if(!MSResponse.isSuccessCode(msResponse)){
            throw new RuntimeException("修改好评费失败:" + msResponse.getMsg());
        }
    }

    public void cancelled(Praise praise){
        MSResponse<Integer> msResponse = orderPraiseFeign.cancelled(praise);
        if(!MSResponse.isSuccessCode(msResponse)){
            throw new RuntimeException("审核通过好评单失败:" + msResponse.getMsg());
        }
    }

    /**
     * 提交好评单申请
     */
    public void saveApplyPraise(Praise praise, Order order, User user, int createType){
        orderPraiseService.saveApplyPraise(praise,order,user,createType);
    }


    /**
     * 申请好评费页面修改好评费
     */
     public void updatePraise(Praise praise,User user,int createType){
         orderPraiseService.updatePraise(praise,user,createType);
     }



}
