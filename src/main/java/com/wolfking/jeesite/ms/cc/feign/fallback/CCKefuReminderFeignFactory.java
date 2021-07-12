package com.wolfking.jeesite.ms.cc.feign.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.cc.Reminder;
import com.kkl.kklplus.entity.cc.vm.*;
import com.kkl.kklplus.entity.common.MSPage;
import com.wolfking.jeesite.ms.cc.feign.CCKefuReminderFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;



@Slf4j
@Component
public class CCKefuReminderFeignFactory implements FallbackFactory<CCKefuReminderFeign> {


    @Override
    public CCKefuReminderFeign create(Throwable throwable) {
        if(throwable != null) {
            log.error("CCKefuReminderFeignFactory FallbackFactory:{}", throwable.getMessage());
        }
        return new CCKefuReminderFeign(){

            @Override
            public MSResponse<MSPage<ReminderListModel>> waitReplyList(ReminderPageSearchModel serachModel) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<MSPage<ReminderListModel>> haveRepliedList(ReminderPageSearchModel reminderPageSearchModel) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<MSPage<ReminderListModel>> processedList(ReminderPageSearchModel serachModel) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<MSPage<ReminderListModel>> finishList(ReminderPageSearchModel reminderPageSearchModel) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<MSPage<ReminderListModel>> getReminderPage(ReminderPageSearchModel serachModel) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<MSPage<Reminder>> getListByOrderId(ReminderSearchModel serachModel) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<MSPage<ReminderListModel>> moreThan24HoursUnfinishedList(ReminderPageSearchModel reminderPageSearchModel) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<MSPage<ReminderListModel>> moreThan48HoursUnfinishedList(ReminderPageSearchModel reminderPageSearchModel) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };
    }
}
