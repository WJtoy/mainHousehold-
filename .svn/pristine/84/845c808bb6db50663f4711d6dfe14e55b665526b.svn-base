package com.wolfking.jeesite.test.ms.tmall.md;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.googlecode.protobuf.format.JsonFormat;
import com.kkl.kklplus.entity.b2b.common.B2BActionType;
import com.kkl.kklplus.entity.b2b.common.B2BWorkcardStatus;
import com.kkl.kklplus.entity.b2b.pb.MQWorkcardStatusUpdateMessage;
import com.wolfking.jeesite.ms.tmall.md.entity.B2BServicePointBatchLog;
import com.wolfking.jeesite.ms.tmall.md.service.MdB2bTmallService;
import com.wolfking.jeesite.ms.tmall.md.service.B2BServicePointService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import static com.wolfking.jeesite.ms.tmall.md.entity.MdB2bTmall.InterfaceType.INTERFACE_TYPE_SERVICESTORE;

@RunWith(SpringRunner.class)
@Slf4j
@SpringBootTest
public class ServiceStoreTest {

    @Autowired
    private B2BServicePointService b2BServicePointService;
    @Autowired
    private MdB2bTmallService mdB2bTmallService;

    @Test
    public void testGetServiceStore() {
//        ServiceStore serviceStore = serviceStoreService.getServiceStore(1L);
//        log.error("{}", serviceStore);
//        serviceStoreService.insertServiceStore(1L);
    }

    @Test
    public void testGetServiceStoreCoverService() {
//        ServiceStoreCoverService coverService = serviceStoreService.getServiceStoreCoverService(1L);
//        log.error("{}", coverService);

    }

    @Test
    public void testGetServiceStoreCapacity() {
//        ServiceStoreCapacity capacity = serviceStoreService.getServiceStoreCapacity(1L);
//        log.error("{}", capacity);

    }

    @Test
    public void testEnableServicePoint() {
//        mdB2bTmallService.enableServicePoint(1L);
//        mdB2bTmallService.enableEngineer(1, 3);

        B2BServicePointBatchLog.BatchProcessComment batchProcessComment = new B2BServicePointBatchLog.BatchProcessComment();
        batchProcessComment.setServicePointId(1L);
        batchProcessComment.setActionType(B2BActionType.ACTION_TYPE_CREATE.value);
        batchProcessComment.setInterfaceType(INTERFACE_TYPE_SERVICESTORE.value);
        batchProcessComment.setErrorCode(10);
        batchProcessComment.setErrorMsg("dddddd");
        List<B2BServicePointBatchLog.BatchProcessComment> list = Lists.newArrayList();
        list.add(batchProcessComment);list.add(batchProcessComment);
        Gson gson = new Gson();
        String json = gson.toJson(list, List.class);
    }

    @Test
    public void testJsonFormat() throws IOException {
        MQWorkcardStatusUpdateMessage.WorkcardStatusUpdateMessage message = MQWorkcardStatusUpdateMessage.WorkcardStatusUpdateMessage.newBuilder()
                .setWorkcardId("10001")
                .setStatus(B2BWorkcardStatus.WORKCARD_STATUS_SERVICED.value)
                .setUpdater("Admin")
                .setUpdateDate(new Date().getTime())
                .build();
        String jsonStr = new JsonFormat().printToString(message);
        MQWorkcardStatusUpdateMessage.WorkcardStatusUpdateMessage.Builder builder = MQWorkcardStatusUpdateMessage.WorkcardStatusUpdateMessage.newBuilder();
        new JsonFormat().merge(new ByteArrayInputStream(jsonStr.getBytes()), builder);
        MQWorkcardStatusUpdateMessage.WorkcardStatusUpdateMessage message1 = builder.build();
    }

}
