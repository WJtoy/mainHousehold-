package com.wolfking.jeesite.ms.b2bcenter.sd.service;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2bcenter.md.B2BDataSourceEnum;
import com.kkl.kklplus.entity.b2bcenter.pb.MQB2BOrderComplainProcessMessage;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderEnum;
import com.wolfking.jeesite.modules.sd.entity.OrderComplain;
import com.wolfking.jeesite.modules.sd.entity.OrderComplainLog;
import com.wolfking.jeesite.modules.sd.service.OrderComplainService;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.ms.b2bcenter.sd.entity.B2BOrderVModel;
import com.wolfking.jeesite.ms.joyoung.sd.service.JoyoungOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;


@Slf4j
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Service
public class B2BOrderComplainService {

    @Autowired
    private JoyoungOrderService joyoungOrderService;

    @Autowired
    private OrderComplainService orderComplainService;

    /**
     * 更新投诉单的kkl投诉单Id
     */
    public MSResponse updateFlag(Long b2bConsultingId,Long kklConsultingId,Integer dataSource){
        MSResponse msResponse = new MSResponse(MSErrorCode.SUCCESS);
        if(dataSource!=null && dataSource== B2BDataSourceEnum.JOYOUNG.id){
            msResponse = joyoungOrderService.updateFlag(b2bConsultingId,kklConsultingId);
        }
        return msResponse;
    }

    /**
     * 收第三方系统回调的投诉单处理进度
     */
     public MSResponse processCallback(MQB2BOrderComplainProcessMessage.B2BOrderComplainProcessMessage msgObj){
         MSResponse msResponse = new MSResponse(MSErrorCode.SUCCESS);
         try {
             OrderComplain orderComplain = orderComplainService.getOrderComplainById(msgObj.getKklComplainId());
             if(orderComplain==null){
                 return new MSResponse(MSErrorCode.newInstance(MSErrorCode.FAILURE,"读取投诉单信息失败"));
             }
             if(msgObj.getOperationType()== B2BOrderEnum.ComplainOperationTypeEnum.LOG.value){//增加日志
                 //save complain log
                 saveOrderComplainLog(orderComplain,msgObj,"第三方跟进信息,普通日志：" + msgObj.getContent());
             }else if(msgObj.getOperationType()== B2BOrderEnum.ComplainOperationTypeEnum.EXCEPTION_LOG.value){//申诉
                 if(orderComplain.getStatus().getIntValue().equals(OrderComplain.STATUS_CLOSED)){
                     OrderComplainLog orderComplainLog = new OrderComplainLog();
                     orderComplainLog.setCreateBy(B2BOrderVModel.b2bUser);
                     orderComplainLog.setCreateDate(new Date(msgObj.getCreateAt()));
                     Dict status =new Dict(OrderComplain.STATUS_APPEAL,"申诉");
                     orderComplainLog.setStatus(status);
                     orderComplainLog.setComplainId(orderComplain.getId());
                     orderComplainLog.setQuarter(orderComplain.getQuarter());
                     orderComplainLog.setContent(StringUtils.left(msgObj.getContent(),200));
                     orderComplainService.saveAppeal(orderComplainLog);
                 }else{
                     saveOrderComplainLog(orderComplain,msgObj,"第三方跟进信息,异常日志：" + msgObj.getContent());
                 }
             }else if(msgObj.getOperationType()== B2BOrderEnum.ComplainOperationTypeEnum.CLOSE.value){ //关闭投诉
                 if(Integer.parseInt(orderComplain.getStatus().getValue())<OrderComplain.STATUS_CLOSED ||orderComplain.getStatus().getIntValue().equals(OrderComplain.STATUS_APPEAL)){ //如果未关闭
                     orderComplain.setCompleteBy(B2BOrderVModel.b2bUser);
                     orderComplain.setCompleteDate(new Date(msgObj.getCreateAt()));
                     orderComplain.setCompleteRemark(StringUtils.left(msgObj.getContent(),500));
                     orderComplainService.closeComplainByB2B(orderComplain);
                 }else{
                     saveOrderComplainLog(orderComplain,msgObj,"第三方跟进信息,关闭日志：" + msgObj.getContent());
                 }
             }
         }catch (Exception e){
             return new MSResponse(MSErrorCode.newInstance(MSErrorCode.FAILURE,e.getMessage()));
         }
         return msResponse;
     }

     public void saveOrderComplainLog( OrderComplain orderComplain,MQB2BOrderComplainProcessMessage.B2BOrderComplainProcessMessage msgObj,String content){
         OrderComplainLog orderComplainLog = new OrderComplainLog();
         orderComplainLog.setComplainId(msgObj.getKklComplainId());
         orderComplainLog.setQuarter(orderComplain.getQuarter());
         orderComplainLog.setContent(content);
         orderComplainLog.setStatus(orderComplain.getStatus());
         orderComplainLog.setCreateBy(B2BOrderVModel.b2bUser);
         orderComplainLog.setCreateDate(new Date(msgObj.getCreateAt()));
         orderComplainService.saveOrderComplainLog(orderComplainLog);
     }

}
