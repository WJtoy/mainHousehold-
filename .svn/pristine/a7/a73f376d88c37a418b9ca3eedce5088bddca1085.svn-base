package com.wolfking.jeesite.modules.mq.service.servicepoint;

import com.wolfking.jeesite.modules.mq.dto.MQOrderServicePointMessage.OperationType;
import com.wolfking.jeesite.modules.sd.service.OrderServicePointService;

/**
 * 订单-网点表数据更新工厂
 * @autor Ryan Lu
 * @date 2019/3/23 2:34 PM
 */
public class ServicePointExecutorFactory {

    public static ServicePointExecutor getExecutor(OperationType operationType, OrderServicePointService service){
        ServicePointExecutor executor = null;
        switch (operationType.getNumber()){
            case OperationType.Create_VALUE:
                //抢单/派单
                executor = new PlanExecutor(service);
                break;
            case OperationType.UpdateServiceInfo_VALUE:
                //更改用户信息
                executor = new UpdateUserInfoExecutor(service);
                break;
            case OperationType.ChangeEngineer_VALUE:
                //网点派单
                executor = new ChangEngineerExecutor(service);
                break;
            case OperationType.Pending_VALUE:
                //预约时间
                executor = new PendingExecutor(service);
                break;
            case OperationType.OnSiteService_VALUE:
                //上门服务
                executor = new OnSiteServiceExecutor(service);
                break;
            case OperationType.ConfirmOnSiteService_VALUE:
                //确认上门
                executor = new ConfirmOnSiteServiceExecutor(service);
                break;
            case OperationType.DelOnSiteService_VALUE:
                //删除上门
                executor = new DelOnSiteServiceExecutor(service);
                break;
            case OperationType.GoBackToAccept_VALUE:
                //回退
                executor = new GoBackToAcceptExecutor(service);
                break;
            case OperationType.Grade_VALUE:
                //客评
                executor = new GradeExecutor(service);
                break;
            case OperationType.Charge_VALUE:
                //对账
                executor = new ChargeExecutor(service);
                break;
            case OperationType.AbnormalyFlag_VALUE:
                //标记异常
                executor = new AbnormalyFlagExecutor(service);
                break;
            case OperationType.RelatedForm_VALUE:
                //关联单号
                executor = new RelatedFormExecutor(service);
                break;
            case OperationType.OrderAppComplete_VALUE:
                executor = new AbnormalyFlagExecutor(service);
                break;
            case OperationType.PraiseForm_VALUE:
                //好评单
                executor = new PraiseFormExecutor(service);
                break;
            default:
                // 只更新status,subStatus
                // 退单申请,退单确认,工单取消,APP完成,回访失败
                executor = new OrderStatusUpdateExecutor(service);
                break;
        }
        return executor;
    }
}
