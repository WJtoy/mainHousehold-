package com.wolfking.jeesite.ms.providermd.service;

import com.google.common.collect.Lists;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.md.MDAttachment;
import com.wolfking.jeesite.modules.md.entity.MdAttachment;
import com.wolfking.jeesite.ms.providermd.feign.MSAttachmentFeign;
import com.wolfking.jeesite.ms.providermd.utils.MDUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MSAttachmentService {

    @Autowired
    private MSAttachmentFeign attachmentFeign;

    /**
     * 保存附件信息-->返回微服务生成ID
     * @param attachment
     * @return
     */
    public MSErrorCode insert(MdAttachment attachment) {
        return MDUtils.genericSaveShouldReturnId(attachment, MDAttachment.class, true, attachmentFeign :: insert, true);
    }

    /**
     * 批量获取附件信息
     * @param ids
     * @return
     */
    public List<MdAttachment> findListByAttachmentIds(List<Long> ids) {
        List<MdAttachment> attachmentList = Lists.newArrayList();
        if (ids != null && !ids.isEmpty()) {
            Lists.partition(ids, 100).forEach(longList -> {
                List<MdAttachment> attachmentsFromMS = MDUtils.findListNecessaryConvertType(MdAttachment.class, ()-> attachmentFeign.findListByAttachmentIdsForMD(longList));
                if (attachmentsFromMS != null && !attachmentsFromMS.isEmpty()) {
                    attachmentList.addAll(attachmentsFromMS);
                }
            });
        }
        return attachmentList;
    }

}
