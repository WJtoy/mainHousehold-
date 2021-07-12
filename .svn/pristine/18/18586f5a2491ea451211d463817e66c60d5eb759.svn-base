package com.wolfking.jeesite.ms.cc.feign.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.cc.vm.ReminderListModel;
import com.kkl.kklplus.entity.cc.vm.ReminderPageSearchModel;
import com.kkl.kklplus.entity.common.MSPage;
import com.wolfking.jeesite.ms.cc.feign.CCCustomerReminderFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class CCCustomerReminderFeignFactory implements FallbackFactory<CCCustomerReminderFeign> {


    @Override
    public CCCustomerReminderFeign create(Throwable throwable) {
        if(throwable != null) {
            log.error("CCCustomerReminderFeign FallbackFactory:{}", throwable.getMessage());
        }
        return new CCCustomerReminderFeign(){

            @Override
            public MSResponse<MSPage<ReminderListModel>> waitReplyList(ReminderPageSearchModel serachModel) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<MSPage<ReminderListModel>> haveRepliedList(ReminderPageSearchModel reminderPageSearchModel) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<MSPage<ReminderListModel>> getReminderPage(ReminderPageSearchModel serachModel) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

        };
    }
}
