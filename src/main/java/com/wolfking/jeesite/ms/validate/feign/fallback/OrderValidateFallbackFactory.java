package com.wolfking.jeesite.ms.validate.feign.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.validate.OrderValidate;
import com.wolfking.jeesite.ms.validate.feign.OrderValidateFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class OrderValidateFallbackFactory implements FallbackFactory<OrderValidateFeign> {

    @Override
    public OrderValidateFeign create(Throwable throwable) {
        if(throwable != null) {
            log.error("OrderValidateFallbackFactory:{}", throwable.getMessage());
        }
        return new OrderValidateFeign() {

            @Override
            public MSResponse<OrderValidate> saveValidate(OrderValidate validate) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<Integer> approve(OrderValidate validate) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<Integer> reject(OrderValidate validate) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<List<OrderValidate>> findListByOrderId(Long orderId, String quarter) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };
    }
}
