package com.wolfking.jeesite.ms.providermd.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.md.MDAppFeedbackType;
import com.kkl.kklplus.entity.md.dto.MDAppFeedbackTypeDto;
import com.wolfking.jeesite.ms.providermd.feign.MSAppFeedbackTypeFeign;
import feign.hystrix.FallbackFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Component
public class MSAppFeedbackTypeeFeignFallbackFactory implements FallbackFactory<MSAppFeedbackTypeFeign> {
    @Override
    public MSAppFeedbackTypeFeign create(Throwable throwable) {
        return new MSAppFeedbackTypeFeign() {

            /**
             * 获取所有app反馈类型
             */
            @Override
            public MSResponse<List<MDAppFeedbackType>> findAllList(){
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            /**
             * 根据反馈类型标识获取指定app反馈类型
             */
            @Override
            public MSResponse<List<MDAppFeedbackType>> findListByFeedbackType(Integer feedbackType){
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            /**
             * 根据id从缓存读取
             * @param id
             * @return
             */
            @Override
            public MSResponse<MDAppFeedbackType> getByIdFromCache(Long id){
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            /**
             * DB查询根据id
             * @param id
             * @return
             */
            @Override
            public MSResponse<MDAppFeedbackType> getById(@RequestParam("id") Long id){
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            /**
             * 分页查询
             * @param mdAppFeedbackType
             * @return
             */
            @Override
            public MSResponse<MSPage<MDAppFeedbackTypeDto>> findList(MDAppFeedbackType mdAppFeedbackType) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<Integer> insert(MDAppFeedbackType appFeedbackType) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<Integer> update(MDAppFeedbackType appFeedbackType) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<Integer> getMaxSortBy() {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<Integer> disableOrEnable(MDAppFeedbackType appFeedbackType) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<Long> checkLabel(Long parentId, String label) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<Long> checkValue(Long parentId,Integer value) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };
    }
}
