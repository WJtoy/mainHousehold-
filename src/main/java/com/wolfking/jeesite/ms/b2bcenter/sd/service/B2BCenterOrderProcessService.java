package com.wolfking.jeesite.ms.b2bcenter.sd.service;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2bcenter.md.B2BDataSourceEnum;
import com.kkl.kklplus.entity.b2bcenter.pb.MQB2BOrderProcessMessage;
import com.wolfking.jeesite.ms.b2bcenter.sd.entity.B2BOrderProcessEntity;
import com.wolfking.jeesite.ms.b2bcenter.sd.utils.B2BOrderUtils;
import com.wolfking.jeesite.ms.canbo.sd.service.CanboOrderService;
import com.wolfking.jeesite.ms.inse.sd.service.InseOrderService;
import com.wolfking.jeesite.ms.jd.sd.service.JdOrderService;
import com.wolfking.jeesite.ms.jinjing.service.JinJingOrderService;
import com.wolfking.jeesite.ms.jinran.sd.service.JinRanOrderService;
import com.wolfking.jeesite.ms.lb.sb.service.LbOrderService;
import com.wolfking.jeesite.ms.mbo.service.MBOOrderService;
import com.wolfking.jeesite.ms.mqi.sd.service.MqiOrderService;
import com.wolfking.jeesite.ms.pdd.sd.service.PddOrderService;
import com.wolfking.jeesite.ms.sf.sd.service.SFOrderService;
import com.wolfking.jeesite.ms.supor.sd.service.SuporOrderService;
import com.wolfking.jeesite.ms.um.sd.service.UMOrderService;
import com.wolfking.jeesite.ms.usatonga.service.UsatonGaOrderService;
import com.wolfking.jeesite.ms.viomi.sd.service.VioMiOrderService;
import com.wolfking.jeesite.ms.weber.service.WeberOrderService;
import com.wolfking.jeesite.ms.xyyplus.sd.service.XYYPlusOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Service
public class B2BCenterOrderProcessService {

    @Autowired
    private XYYPlusOrderService xyyPlusOrderService;

    @Autowired
    private LbOrderService lbOrderService;

    @Autowired
    private InseOrderService inseOrderService;

    @Autowired
    private JdOrderService jdOrderService;

    @Autowired
    private UMOrderService umOrderService;

    @Autowired
    private PddOrderService pddOrderService;

    @Autowired
    private CanboOrderService canboOrderService;

    @Autowired
    private WeberOrderService weberOrderService;
    @Autowired
    private MBOOrderService mboOrderService;
    @Autowired
    private SuporOrderService suporOrderService;
    @Autowired
    private JinJingOrderService jinJingOrderService;
    @Autowired
    private UsatonGaOrderService usatonGaOrderService;

    @Autowired
    private MqiOrderService mqiOrderService;

    @Autowired
    private JinRanOrderService jinRanOrderService;

    @Autowired
    private VioMiOrderService vioMiOrderService;
    @Autowired
    private SFOrderService sfOrderService;

    /**
     * B2B客户主动调用工单操作
     */
    public MSResponse processKKLOrderProcessMessage(MQB2BOrderProcessMessage.B2BOrderProcessMessage message) {
        MSResponse response = new MSResponse(MSErrorCode.SUCCESS);
        B2BOrderProcessEntity processEntity = B2BOrderProcessEntity.toB2BOrderProcessEntity(message);
        if (processEntity != null && B2BDataSourceEnum.isB2BDataSource(processEntity.getDataSourceId())
                && B2BOrderUtils.canProcessKKLOrder(processEntity.getDataSourceId()) && processEntity.getKklOrderId() > 0) {
            if (processEntity.getDataSourceId() == B2BDataSourceEnum.XYINGYAN.id) {
                response = xyyPlusOrderService.processKKLOrder(processEntity);
            } else if (processEntity.getDataSourceId() == B2BDataSourceEnum.LB.id) {
                response = lbOrderService.processKKLOrder(processEntity);
            } else if (processEntity.getDataSourceId() == B2BDataSourceEnum.INSE.id) {
                response = inseOrderService.processKKLOrder(processEntity);
            } else if (processEntity.getDataSourceId() == B2BDataSourceEnum.JD.id) {
                response = jdOrderService.processKKLOrder(processEntity);
            } else if (processEntity.getDataSourceId() == B2BDataSourceEnum.UM.id) {
                response = umOrderService.processKKLOrder(processEntity);
            } else if (processEntity.getDataSourceId() == B2BDataSourceEnum.PDD.id) {
                response = pddOrderService.processKKLOrder(processEntity);
            } else if (processEntity.getDataSourceId() == B2BDataSourceEnum.CANBO.id || processEntity.getDataSourceId() == B2BDataSourceEnum.USATON.id) {
                response = canboOrderService.processKKLOrder(processEntity);
            } else if (processEntity.getDataSourceId() == B2BDataSourceEnum.WEBER.id) {
                response = weberOrderService.processKKLOrder(processEntity);
            } else if (processEntity.getDataSourceId() == B2BDataSourceEnum.MBO.id) {
                response = mboOrderService.processKKLOrder(processEntity);
            } else if (processEntity.getDataSourceId() == B2BDataSourceEnum.SUPOR.id) {
                response = suporOrderService.processKKLOrder(processEntity);
            } else if (processEntity.getDataSourceId() == B2BDataSourceEnum.JINJING.id) {
                response = jinJingOrderService.processKKLOrder(processEntity);
            } else if (processEntity.getDataSourceId() == B2BDataSourceEnum.USATON_GA.id) {
                response = usatonGaOrderService.processKKLOrder(processEntity);
            } else if (processEntity.getDataSourceId() == B2BDataSourceEnum.MQI.id) {
                response = mqiOrderService.processKKLOrder(processEntity);
            } else if (processEntity.getDataSourceId() == B2BDataSourceEnum.JINRAN.id) {
                response = jinRanOrderService.processKKLOrder(processEntity);
            } else if (processEntity.getDataSourceId() == B2BDataSourceEnum.VIOMI.id) {
                response = vioMiOrderService.processKKLOrder(processEntity);
            } else if (processEntity.getDataSourceId() == B2BDataSourceEnum.SF.id) {
                response = sfOrderService.processKKLOrder(processEntity);
            }
        }
        if (!MSResponse.isSuccessCode(response)) {
            B2BOrderProcessEntity.saveFailureLog(processEntity, "B2B客户主动触发工单操作", "B2BCenterOrderProcessService.processKKLOrderProcessMessage", null);
        }
        return response;
    }


}
