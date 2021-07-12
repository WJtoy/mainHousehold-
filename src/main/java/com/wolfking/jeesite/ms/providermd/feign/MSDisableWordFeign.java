package com.wolfking.jeesite.ms.providermd.feign;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.md.MDDisableWord;
import com.wolfking.jeesite.ms.providermd.fallback.MSDisableWordFeignFallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "provider-md", fallbackFactory = MSDisableWordFeignFallbackFactory.class)
public interface MSDisableWordFeign {

    /**
     * 分页查询禁用词列表
     *
     * @return
     */
    @PostMapping("/disableWord/findList")
    MSResponse<MSPage<MDDisableWord>> findList(@RequestBody MDDisableWord mdDisableWord);

    /**
     * 批量插入禁用词
     *
     * @param disableWords
     * @return
     */
    @PostMapping("/disableWord/batchInsert")
    MSResponse<Integer> batchInsert(@RequestBody List<MDDisableWord> disableWords);


    @DeleteMapping("/disableWord/delete")
    MSResponse<Integer> delete(@RequestBody MDDisableWord mdDisableWord);

}
