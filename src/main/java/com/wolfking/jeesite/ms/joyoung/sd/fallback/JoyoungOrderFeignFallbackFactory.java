package com.wolfking.jeesite.ms.joyoung.sd.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2b.common.B2BBase;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrder;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderSearchModel;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderTransferResult;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.common.material.B2BMaterial;
import com.kkl.kklplus.entity.common.material.B2BMaterialArrival;
import com.kkl.kklplus.entity.common.material.B2BMaterialClose;
import com.kkl.kklplus.entity.joyoung.sd.*;
import com.wolfking.jeesite.ms.joyoung.sd.feign.JoyoungOrderFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Slf4j
@Component
public class JoyoungOrderFeignFallbackFactory implements FallbackFactory<JoyoungOrderFeign> {

    @Override
    public JoyoungOrderFeign create(Throwable throwable) {
        if(throwable != null) {
            log.error("JoyoungOrderFeignFallbackFactory:{}", throwable.getMessage());
        }
        return new JoyoungOrderFeign() {
            @Override
            public MSResponse<MSPage<B2BOrder>> getList(B2BOrderSearchModel workcardSearchModel) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse checkWorkcardProcessFlag(List<B2BOrderTransferResult> orderNos) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse updateTransferResult(List<B2BOrderTransferResult> workcardTransferResults) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse cancelOrderTransition(B2BOrderTransferResult b2BOrderTransferResult) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse orderPlanned(JoyoungOrderPlanned joyoungOrderPlanned) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse orderAppointed(JoyoungOrderAppointed joyoungOrderAppointed) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse orderVisited(JoyoungOrderVisited joyoungOrderVisited) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse orderCompleted(JoyoungOrderCompleted joyoungOrderCompleted) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse orderCancelled(JoyoungOrderCancelled joyoungOrderCancelled) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse saveOrderProcesslog(JoyoungOrderProcessLog orderProcessLog) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            //region 配件

            /**
             * 配件申请
             */
            @Override
            public MSResponse newMaterialForm(@RequestBody B2BMaterial joyoungMaterial){
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            /**
             * by配件单关闭
             */
            @Override
            public MSResponse materialClose(@RequestBody B2BMaterialClose joyoungMaterialClose){
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            /**
             * by订单关闭
             */
            @Override
            public MSResponse materialCloseByOrder(@RequestBody B2BMaterialClose joyoungMaterialClose){
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            /**
             * 到货
             */
            @Override
            public MSResponse materialArrival(@RequestBody B2BMaterialArrival joyoungMaterialArrival){
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            /**
             * 审核
             * 消息队列处理成功后，同步更新微服务
             */
            @Override
            public MSResponse updateApplyFlag(@PathVariable("id") Long id){
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            /**
             * 发货回调
             * 消息队列处理成功后，同步更新微服务
             */
            @Override
            public MSResponse updateDeliverFlag(@PathVariable("id") Long id){
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }


            /**
             * 更新九阳投诉单的kkl投诉单Id
             */
            @Override
            public MSResponse<String> updateFlag(Long id, Long kklConsultingId) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse process(JoyoungConsultingOrderProcess consultingOrderProcess) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            //endregion 配件


            @Override
            public MSResponse getProductData(String productBarCode) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };
    }
}
