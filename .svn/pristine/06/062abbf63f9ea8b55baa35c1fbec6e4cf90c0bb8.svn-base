package com.wolfking.jeesite.ms.providermd.feign;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.md.MDAttachment;
import com.wolfking.jeesite.ms.providermd.fallback.MSAttachmentFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name="provider-md", fallbackFactory = MSAttachmentFeignFallbackFactory.class)
public interface MSAttachmentFeign {

    /**
     * 保存附件信息-->基础资料
     * @param mdAttachment
     * @return
     */
    @PostMapping("/attachment/insert")
    MSResponse<Integer> insert(@RequestBody MDAttachment mdAttachment);

    /**
     * 批量获取附件信息-->基础资料
     * @param ids
     * @return
     */
    @PostMapping("/attachment/findListByAttachmentIdsForMD")
    MSResponse<List<MDAttachment>> findListByAttachmentIdsForMD(@RequestBody List<Long> ids);

}
