package com.wolfking.jeesite.ms.b2bcenter.sd.service;

import com.google.common.collect.Maps;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2bcenter.md.B2BDataSourceEnum;
import com.kkl.kklplus.entity.b2bcenter.pb.MQB2BOrderModifyMessage;
import com.wolfking.jeesite.common.utils.GsonUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.sd.entity.Order;
import com.wolfking.jeesite.modules.sd.entity.OrderCondition;
import com.wolfking.jeesite.modules.sd.service.OrderService;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.service.AreaService;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import com.wolfking.jeesite.ms.b2bcenter.mq.sender.B2BCenterModifyB2BOrderMQSender;
import com.wolfking.jeesite.ms.b2bcenter.sd.entity.B2BOrderModifyEntity;
import com.wolfking.jeesite.ms.b2bcenter.sd.utils.B2BOrderUtils;
import com.wolfking.jeesite.ms.lb.sb.service.LbOrderService;
import com.wolfking.jeesite.ms.suning.sd.service.SuningOrderService;
import com.wolfking.jeesite.ms.supor.sd.service.SuporOrderService;
import com.wolfking.jeesite.ms.xyyplus.sd.service.XYYPlusOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Map;

@Slf4j
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Service
public class B2BCenterOrderModifyService {

    @Autowired
    private B2BCenterModifyB2BOrderMQSender b2BCenterModifyB2BOrderMQSender;
    @Autowired
    private SuningOrderService suningOrderService;
    @Autowired
    private SuporOrderService suporOrderService;
    @Autowired
    private XYYPlusOrderService xyyPlusOrderService;
    @Autowired
    private LbOrderService lbOrderService;
    @Autowired
    private AreaService areaService;
    @Autowired
    private OrderService orderService;


    /**
     * 修改B2B工单
     */
    @Transactional()
    public void modifyB2BOrder(Order order, boolean isRereadOrder) {
        if (order != null && order.getId() != null && order.getId() > 0
                && B2BOrderUtils.canModifyB2BOrder(order.getDataSourceId())) {
            if (isRereadOrder) {
                order = orderService.getOrderById(order.getId(), order.getQuarter(), OrderUtils.OrderDataLevel.CONDITION, true);
            }
            if (order != null && order.getId() != null && order.getId() > 0
                    && StringUtils.isNotBlank(order.getWorkCardId())
                    && B2BOrderUtils.canModifyB2BOrder(order.getDataSourceId())) {
                B2BOrderModifyEntity.Builder builder = new B2BOrderModifyEntity.Builder();
                builder.setDataSourceId(order.getDataSource().getIntValue())
                        .setKklOrderId(order.getId())
                        .setB2bOrderNo(order.getWorkCardId())
                        .setOperateTime((new Date()).getTime());

                OrderCondition condition = order.getOrderCondition();
                if (condition != null) {
                    if (StringUtils.isNotBlank(condition.getUserName()) && StringUtils.isNotBlank(condition.getServicePhone())) {
                        builder.setUserName(condition.getUserName())
                                .setUserMobile(condition.getServicePhone())
                                .setUserPhone(StringUtils.toString(condition.getPhone2()));
                    }
                    if (condition.getArea() != null && condition.getArea().getId() != null && condition.getArea().getId() > 0
                            && StringUtils.isNotBlank(condition.getServiceAddress())) {
                        Area countyArea = areaService.getFromCache(condition.getArea().getId());
                        Area provinceArea = null;
                        Area cityArea = null;
                        if (countyArea != null && StringUtils.isNotBlank(countyArea.getParentIds())) {
                            String[] parentIds = countyArea.getParentIds().split(",");
                            if (parentIds.length == 4) {
                                provinceArea = areaService.getFromCache(StringUtils.toLong(parentIds[2]));
                                cityArea = areaService.getFromCache(StringUtils.toLong(parentIds[3]));
                            }
                        }
                        if (provinceArea != null && StringUtils.isNotBlank(provinceArea.getName())
                                && cityArea != null && StringUtils.isNotBlank(cityArea.getName())
                                && StringUtils.isNotBlank(countyArea.getName())) {
                            builder.setUserProvince(provinceArea.getName())
                                    .setUserCity(cityArea.getName())
                                    .setUserCounty(countyArea.getName())
                                    .setUserStreet(condition.getServiceAddress());
                        }
                    }
                }
                B2BOrderModifyEntity modifyEntity = builder.build();
                if (!(StringUtils.isNotBlank(modifyEntity.getUserName()) && StringUtils.isNotBlank(modifyEntity.getUserMobile())
                        && StringUtils.isNotBlank(modifyEntity.getUserProvince()) && StringUtils.isNotBlank(modifyEntity.getUserCity())
                        && StringUtils.isNotBlank(modifyEntity.getUserCounty()) && StringUtils.isNotBlank(modifyEntity.getUserStreet()))) {
                    B2BOrderModifyEntity.saveFailureLog(modifyEntity, "工单的用户信息与地址信息不完整", "B2BCenterOrderModifyService.modifyB2BOrder", null);
                }
                sendModifyB2BOrderMessage(modifyEntity);
            } else {
                Map<String, Object> params = Maps.newHashMap();
                if (order != null) {
                    params.put("orderId", order.getId() != null ? order.getId() : 0);
                    params.put("workCardId", StringUtils.toString(order.getWorkCardId()));
                    params.put("dataSourceId", order.getDataSourceId());
                } else {
                    params.put("order", "null");
                }
                String logJson = GsonUtils.toGsonString(params);
                LogUtils.saveLog("读取工单失败", "B2BCenterOrderModifyService.modifyB2BOrder", logJson, null, null);
            }
        }
    }


    /**
     * 往修改B2B工单的消息队列发消息
     */
    private void sendModifyB2BOrderMessage(B2BOrderModifyEntity entity) {
        MQB2BOrderModifyMessage.B2BOrderModifyMessage.Builder builder = MQB2BOrderModifyMessage.B2BOrderModifyMessage.newBuilder();
        builder.setDataSource(entity.getDataSourceId())
                .setKklOrderId(entity.getKklOrderId())
                .setB2BOrderNo(entity.getB2bOrderNo())
                .setOperateTime(entity.getOperateTime())
                .setUserName(entity.getUserName())
                .setUserMobile(entity.getUserMobile())
                .setUserPhone(entity.getUserPhone())
                .setRemarks(entity.getRemarks())
                .setUserProvince(entity.getUserProvince())
                .setUserCity(entity.getUserCity())
                .setUserCounty(entity.getUserCounty())
                .setUserStreet(entity.getUserStreet());
        b2BCenterModifyB2BOrderMQSender.send(builder.build());
    }


    /**
     * 处理修改B2B工单的消息
     */
    public MSResponse processModifyB2BOrdeMessage(MQB2BOrderModifyMessage.B2BOrderModifyMessage message) {
        MSResponse response = new MSResponse<>(MSErrorCode.SUCCESS);
        if (B2BDataSourceEnum.isB2BDataSource(message.getDataSource()) && StringUtils.isNotBlank(message.getB2BOrderNo())) {
            if (message.getDataSource() == B2BDataSourceEnum.SUNING.id) {
                response = suningOrderService.modifyB2BOrder(message);
            }
        }
        return response;
    }

    /**
     * 处理修改快可立工单的消息
     */
    public MSResponse processModifyKKLOrderMessage(MQB2BOrderModifyMessage.B2BOrderModifyMessage message) {
        MSResponse response = new MSResponse(MSErrorCode.SUCCESS);
        B2BOrderModifyEntity modifyEntity = B2BOrderModifyEntity.toB2BOrderModifyEntity(message);
        if (modifyEntity != null && B2BDataSourceEnum.isB2BDataSource(modifyEntity.getDataSourceId())
                && B2BOrderUtils.canModifyKKLOrder(modifyEntity.getDataSourceId()) && modifyEntity.getKklOrderId() > 0) {
            if (modifyEntity.getDataSourceId() == B2BDataSourceEnum.SUNING.id) {
                response = suningOrderService.modifyKKLOrderBySuning(modifyEntity);
            } else if (modifyEntity.getDataSourceId() == B2BDataSourceEnum.SUPOR.id) {
                response = suporOrderService.modifyKKLOrderBySupor(modifyEntity);
            }
            else if (modifyEntity.getDataSourceId() == B2BDataSourceEnum.XYINGYAN.id) {
                response = xyyPlusOrderService.modifyKKLOrderByXYYPlus(modifyEntity);
            } else if (modifyEntity.getDataSourceId() == B2BDataSourceEnum.LB.id) {
                response = lbOrderService.modifyKKLOrderByXYYPlus(modifyEntity);
            }
        }
        if (!MSResponse.isSuccessCode(response)) {
            B2BOrderModifyEntity.saveFailureLog(modifyEntity, "处理修改快可立工单的消息", "B2BCenterOrderModifyService.processModifyKKLOrderMessage", null);
        }
        return response;
    }


}
