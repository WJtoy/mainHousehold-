package com.wolfking.jeesite.ms.tmall.mq.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class MqB2bTmallLogService {

//    @Resource
//    private MqB2bTmallDao mqB2bTmallDao;

//    @Autowired
//    private B2BCenterOrderService b2BCenterOrderService;


//    public MqB2bTmallLog get(Long id) {
//        MqB2bTmallLog mqB2bTmallLog = null;
//        if (id != null) {
//            mqB2bTmallLog = mqB2bTmallDao.get(id);
//        }
//        return mqB2bTmallLog;
//    }

//    private void insert(MqB2bTmallLog mqB2bTmallLog) {
//        if (mqB2bTmallLog != null) {
//            mqB2bTmallDao.insert(mqB2bTmallLog);
//        }
//    }

//    public void updateProcessStatus(Long id, B2BProcessFlag processFlag, String processComment) {
//        MqB2bTmallLog mqB2bTmallLog = get(id);
//        if (mqB2bTmallLog != null) {
//            mqB2bTmallLog.setProcessFlag(processFlag.value);
//            StringBuilder comment = new StringBuilder();
//            if (StringUtils.isNotEmpty(mqB2bTmallLog.getProcessComment())) {
//                comment.append(mqB2bTmallLog.getProcessComment().concat(" "));
//            }
//            if (StringUtils.isNotEmpty(processComment)) {
//                comment.append(processComment);
//            }
//            mqB2bTmallLog.setProcessComment(comment.toString());
//            mqB2bTmallLog.setProcessTime(mqB2bTmallLog.getProcessTime()+1);
//            mqB2bTmallLog.preUpdate();
//            mqB2bTmallDao.updateProcessStatus(mqB2bTmallLog);
//        }
//    }

//    @Transactional()
//    public void insertMqB2bTmallLog(B2BMQQueueType queueType, String messageJson, Long createById,
//                                    B2BProcessFlag processFlag, int processTime, String processComment) {
//        MqB2bTmallLog mqB2bTmallLog = new MqB2bTmallLog();
//        Date now = new Date();
//        mqB2bTmallLog.setQueueId(queueType.id);
//        mqB2bTmallLog.setMessageJson(messageJson);
//        mqB2bTmallLog.setProcessFlag(processFlag.value);
//        mqB2bTmallLog.setProcessTime(processTime);
//        mqB2bTmallLog.setProcessComment(processComment);
//        mqB2bTmallLog.setCreateBy(new User(createById));
//        mqB2bTmallLog.setCreateDate(now);
//        mqB2bTmallLog.setUpdateBy(new User(createById));
//        mqB2bTmallLog.setUpdateDate(now);
//        mqB2bTmallLog.setQuarter(QuarterUtils.getSeasonQuarter(now));
//        insert(mqB2bTmallLog);
//    }

//
//    public List<MQB2BOrderStatusUpdateMessage.B2BOrderStatusUpdateMessage> findList(B2BMQQueueType queueType, String quarter) {
//        Date today = DateUtils.getDate(2019, 8, 26);
//        Date beginDate = DateUtils.getDate(today, 10, 0, 0);
//        Date endDate = DateUtils.getDate(today, 14,0,0);
//        List<MqB2bTmallLog> list = mqB2bTmallDao.findList(queueType.id, quarter, beginDate, endDate);
//
//        List<MQB2BOrderStatusUpdateMessage.B2BOrderStatusUpdateMessage> result = Lists.newArrayList();
//        for (MqB2bTmallLog item : list) {
//            MQB2BOrderStatusUpdateMessage.B2BOrderStatusUpdateMessage.Builder builder = MQB2BOrderStatusUpdateMessage.B2BOrderStatusUpdateMessage.newBuilder();
//            try {
//                new JsonFormat().merge(new ByteArrayInputStream(item.getMessageJson().getBytes("utf-8")), builder);
//                result.add(builder.build());
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//        return result;
//    }

//
//    public void  retrySendB2BOrderStatusUpdateMessage() {
//        List<MQB2BOrderStatusUpdateMessage.B2BOrderStatusUpdateMessage> list = findList(B2BMQQueueType.DELAY_WORKCARD_STATUS_UPDATE, "20193");
//        for (MQB2BOrderStatusUpdateMessage.B2BOrderStatusUpdateMessage message : list) {
//            b2BCenterOrderService.processB2BOrderStatusUpdateMessage(message);
//        }
//    }

}
