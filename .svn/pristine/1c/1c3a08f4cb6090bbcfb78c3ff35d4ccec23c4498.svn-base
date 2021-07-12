package com.wolfking.jeesite.modules.servicepoint.ms.sd;

import com.wolfking.jeesite.modules.sd.entity.OrderAttachment;
import com.wolfking.jeesite.modules.sd.service.OrderAttachmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class SpOrderAttachmentService {

    @Autowired
    private OrderAttachmentService attachmentService;


    public List<OrderAttachment> getAttachesByOrderId(Long orderId, String quarter) {
        return attachmentService.getAttachesByOrderId(orderId, quarter);
    }

    /**
     * 删除订单附件
     */
    @Transactional()
    public void delete(OrderAttachment attachment) {
        attachmentService.delete(attachment);
    }

    /**
     * 新增订单附件
     * 订单完成附件数量+1,redis保持同步
     */
    @Transactional()
    public void save(OrderAttachment attachment) {
        attachmentService.save(attachment);
    }
}
