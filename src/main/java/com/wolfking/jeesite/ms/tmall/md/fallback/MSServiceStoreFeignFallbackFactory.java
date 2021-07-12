package com.wolfking.jeesite.ms.tmall.md.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2b.servicestore.ServiceStore;
import com.kkl.kklplus.entity.b2b.servicestore.ServiceStoreCapacity;
import com.kkl.kklplus.entity.b2b.servicestore.ServiceStoreCoverService;
import com.kkl.kklplus.entity.b2b.servicestore.Worker;
import com.wolfking.jeesite.ms.tmall.md.feign.MSServiceStoreFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MSServiceStoreFeignFallbackFactory implements FallbackFactory<MSServiceStoreFeign> {

    @Override
    public MSServiceStoreFeign create(Throwable throwable) {
        if(throwable != null) {
            log.error("MSServiceStoreFeignFallbackFactory:{}", throwable.getMessage());
        }
        return new MSServiceStoreFeign() {

            @Override
            public MSResponse<String> insertServiceStore(ServiceStore serviceStore) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<String> updateServiceStore(ServiceStore serviceStore) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<String> deleteServiceStore(ServiceStore serviceStore) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<String> insertServiceStoreCoverService(ServiceStoreCoverService serviceStoreCoverService) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<String> updateServiceStoreCoverService(ServiceStoreCoverService serviceStoreCoverService) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<String> deleteServiceStoreCoverService(ServiceStoreCoverService serviceStoreCoverService) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<String> insertServiceStoreCapacity(ServiceStoreCapacity serviceStoreCapacity) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<String> updateServiceStoreCapacity(ServiceStoreCapacity serviceStoreCapacity) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<String> deleteServiceStoreCapacity(ServiceStoreCapacity serviceStoreCapacity) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<String> insertWorker(Worker worker) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<String> updateWorker(Worker worker) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<String> deleteWorker(Worker worker) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };
    }
}
