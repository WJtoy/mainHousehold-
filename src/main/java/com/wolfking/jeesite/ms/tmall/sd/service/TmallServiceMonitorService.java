package com.wolfking.jeesite.ms.tmall.sd.service;

import com.google.common.base.Splitter;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2b.pb.MQTmallServiceMonitorMessageMessage;
import com.kkl.kklplus.entity.tmall.sd.AnomalyRecourseRemarkUpdate;
import com.kkl.kklplus.entity.tmall.sd.ServiceMonitorMessageUpdate;
import com.wolfking.jeesite.common.persistence.AjaxJsonEntity;
import com.wolfking.jeesite.common.service.LongIDCrudService;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.sd.entity.Order;
import com.wolfking.jeesite.modules.sd.service.OrderService;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.service.AreaService;
import com.wolfking.jeesite.ms.tmall.sd.dao.TmallServiceMonitorDao;
import com.wolfking.jeesite.ms.tmall.sd.entity.TmallServiceMonitor;
import com.wolfking.jeesite.ms.tmall.sd.feign.WorkcardFeign;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


/**
 * 天猫预警
 */
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Slf4j
public class TmallServiceMonitorService extends LongIDCrudService<TmallServiceMonitorDao, TmallServiceMonitor> {

    @Autowired
    private WorkcardFeign workcardFeign;

    @Autowired
    private TmallServiceMonitorDao monitorDao;

    @Autowired
    private OrderService orderService;

    @Autowired
    private AreaService areaService;

    SimpleDateFormat sdf= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    /**
     * 保存b2b发过的预警信息
     * @param monitorMsg
     * @returno
     */
    public AjaxJsonEntity saveMonitor(MQTmallServiceMonitorMessageMessage.TmallServiceMonitorMessageMessage monitorMsg){
        AjaxJsonEntity jsonEntity = new AjaxJsonEntity(true);
        TmallServiceMonitor entity = new TmallServiceMonitor();
        if(monitorMsg.getOrderId()<=0){
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage("缺少订单号");
            return jsonEntity;
        }
        if(!StringUtils.isNotBlank(monitorMsg.getQuarter())){
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage("缺少分片");
            return jsonEntity;
        }
        if(monitorMsg.getMonitorId()<=0){
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage("缺少预警id");
            return jsonEntity;
        }
        if(monitorMsg.getLevel()<=0){
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage("缺少预警等级");
            return jsonEntity;
        }
        if(!StringUtils.isNotBlank(monitorMsg.getGmtCreate())){
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage("缺少预警时间");
            return jsonEntity;
        }
        if(!StringUtils.isNotBlank(monitorMsg.getGmtCreate())){
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage("缺少预警内容");
            return jsonEntity;
        }
        if(!StringUtils.isNotBlank(monitorMsg.getRuleId())){
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage("缺少预警规则id");
            return jsonEntity;
        }
        Order order = orderService.getOrderById(monitorMsg.getOrderId(), monitorMsg.getQuarter(), OrderUtils.OrderDataLevel.CONDITION,true);
        if(order == null){
            order = orderService.getOrderById(monitorMsg.getOrderId(), "", OrderUtils.OrderDataLevel.CONDITION,true);
            if(order ==null){
                jsonEntity.setSuccess(false);
                jsonEntity.setMessage("工单系统中没该订单");
                return jsonEntity;
            }
        }
        entity.setOrderId(monitorMsg.getOrderId());
        entity.setQuarter(order.getQuarter());
        entity.setOrderNo(order.getOrderNo());
        entity.setMonitorId(monitorMsg.getMonitorId());
        entity.setLevel(monitorMsg.getLevel());
        try {
            entity.setGmtDate(sdf.parse(monitorMsg.getGmtCreate()));
        } catch (ParseException e) {
            jsonEntity.setSuccess(false);
            jsonEntity.setMessage("预警时间转换失败");
            return jsonEntity;
        }
        entity.setStatus(1);
        entity.setContent(monitorMsg.getContent());
        entity.setServiceCode(monitorMsg.getServiceCode());
        entity.setRuleId(monitorMsg.getRuleId());
        entity.setReplyDate(new Date());
        entity.setAreaId(order.getOrderCondition().getArea().getId());//2019/03/15
        entity.setAreaName(order.getOrderCondition().getArea().getName());//2019/03/15
        //增加客户,品类,省,市,是否能突击栏位
        entity.setProductCategoryId(order.getOrderCondition().getProductCategoryId());
        entity.setCustomerId(order.getOrderCondition().getCustomerId());
        entity.setCanRush(order.getOrderCondition().getCanRush());
        entity.setKefuType(order.getOrderCondition().getKefuType());
        Area area = areaService.getFromCache(order.getOrderCondition().getArea().getId());
        if (area != null) {
            List<String> ids = Splitter.onPattern(",")
                    .omitEmptyStrings()
                    .trimResults()
                    .splitToList(area.getParentIds());
            if (ids.size() >= 2) {
                entity.setCityId(Long.valueOf(ids.get(ids.size() - 1)));
                entity.setProvinceId(Long.valueOf(ids.get(ids.size() - 2)));
            }
        }
        super.save(entity);
        return jsonEntity;
    }

    /**
     * 天猫预警反馈
     * @param serviceMonitor
     * @returno
     */
    @Transactional()
    public void updateFeedback(TmallServiceMonitor serviceMonitor){
        if(serviceMonitor == null || serviceMonitor.getId()==null){
            return;
        }
        try {
            ServiceMonitorMessageUpdate entity = new ServiceMonitorMessageUpdate();
            entity.setServiceMonitorMessageId(serviceMonitor.getMonitorId());
            entity.setMemo(serviceMonitor.getReplyContent());
            entity.setStatus(3);
            entity.setCreateById(serviceMonitor.getReplierId());
            entity.setUpdateById(serviceMonitor.getReplierId());
            super.save(serviceMonitor);
            //sync to b2b
            MSResponse msResponse = workcardFeign.updateServiceMonitorMessageStatus(entity);
            if(!MSResponse.isSuccessCode(msResponse)){
                throw new RuntimeException(StringUtils.isNotBlank(msResponse.getMsg())?msResponse.getMsg():"同步天猫错误,请稍后重试");
            }
        } catch (FeignException fe){
            log.error("[TmallAnomalyRecouseService].feedback orderId:{}",serviceMonitor.getOrderId(), fe);
            throw fe;
        } catch (Exception e) {
            log.error("[TmallServiceMonitorService].feedback orderId:{}",serviceMonitor.getOrderId(), e);
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 根据订单id获取列表
     * @param orderId
     * @returno
     */
    public List<TmallServiceMonitor> getListByOrderId(Long orderId,String quarter){
        return monitorDao.getListByOrderId(orderId,quarter);
    }
}
