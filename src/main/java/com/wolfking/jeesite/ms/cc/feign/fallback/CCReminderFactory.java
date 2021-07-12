package com.wolfking.jeesite.ms.cc.feign.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.cc.Reminder;
import com.kkl.kklplus.entity.cc.ReminderItem;
import com.kkl.kklplus.entity.cc.ReminderLog;
import com.kkl.kklplus.entity.cc.vm.*;
import com.kkl.kklplus.entity.common.MSPage;
import com.wolfking.jeesite.ms.cc.feign.CCReminderFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class CCReminderFactory implements FallbackFactory<CCReminderFeign> {

    @Override
    public CCReminderFeign create(Throwable throwable) {

        if(throwable != null) {
            log.error("CCReminderFeign FallbackFactory:{}", throwable.getMessage());
        }

        return new CCReminderFeign() {

            //region 催单

            /**
             * 创建
             */
            @Override
            public MSResponse<Reminder> newReminder(@RequestBody Reminder reminder){
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            /**
             * 待回复列表
             */
            @Override
            public MSResponse<MSPage<ReminderListModel>> waitReplyList(@RequestBody ReminderPageSearchModel serachModel){
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            /**
             * 确认处理
             */
            @Override
            public MSResponse<Integer> confirmProcessed(@RequestBody Reminder reminder){
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            /**
             * 驳回催单(再次催单)
             */
            @Override
            public MSResponse<Integer> rejectProcessed(@RequestBody Reminder reminder){
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            /**
             * 已处理列表(跟单/客户)
             */
            @Override
            public MSResponse<MSPage<ReminderListModel>> processedList(@RequestBody ReminderPageSearchModel serachModel){
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            /**
             * 分页查询催单列表
             */
            @Override
            public MSResponse<MSPage<ReminderListModel>> getReminderPage(@RequestBody ReminderPageSearchModel serachModel){
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            /**
             * 查询订单下催单列表
             */
            @Override
            public MSResponse<MSPage<Reminder>> getListByOrderId(@RequestBody ReminderSearchModel serachModel){
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            /**
             * 批量检查订单是否可以再次催单
             */
            @Override
            public MSResponse<Map<Long,Reminder>> bulkRereminderCheck(@RequestBody BulkRereminderCheckModel serachModel){
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            /**
             * 检查订单是否可以再次催单
             */
            @Override
            public MSResponse<RereminderCheckRespone> reReminderCheck(@PathVariable("quarter") String quarter, @PathVariable("orderId") Long orderId,@PathVariable("orderCreateAt") Long orderCreateAt){
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }


            /**
             * 按订单id批量读取订单催单时效信息
             */
            @Override
            public MSResponse<Map<Long, ReminderTimeLinessModel>> bulkGetReminderTimeLinessByOrders(@RequestBody BulkRereminderCheckModel serachModel){
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            /**
             * 根据Id+分片获取具体某个催单单据
             */
            @Override
            public MSResponse<Reminder> getReminderById(@PathVariable("quarter") String quarter,@PathVariable("id") Long id,@PathVariable("itemFlag") Integer itemFlag){
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            /**
             * 根据订单Id+分片获取待回复催单单据
             */
            @Override
            public MSResponse<Reminder> getWaitReplyReminder(@PathVariable("quarter") String quarter, @PathVariable("orderId") Long orderId){
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            /**
             * 根据订单Id+分片获取最后一次催单单据
             */
            @Override
            public MSResponse<Reminder> getLast(@PathVariable("quarter") String quarter, @PathVariable("orderId") Long orderId){
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<Map<Long, ReminderTimeLinessModel>> findReminderTimelinessByOrderIds(BulkRereminderCheckModel serchModel) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            /**
             * 回复处理(客服/网点回复)
             */
            @Override
            public MSResponse<Integer> replyReminder(@RequestBody Reminder reminder){
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            /**
             * 完成
             * 取消订单，退单并审核，客评
             */
            @Override
            public MSResponse<Integer> completeByOrder(@RequestBody Reminder reminder){
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            /**
             * 查询已回复列表(已关闭,已完成)
             */
            @Override
            public MSResponse<MSPage<ReminderListModel>> haveRepliedList(ReminderPageSearchModel reminderPageSearchModel) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            /**
             * 查询工单完成的列表(已关闭,已完成)
             */
            @Override
            public MSResponse<MSPage<ReminderListModel>> finishList(ReminderPageSearchModel reminderPageSearchModel) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            /**
             * 工单关闭更新相关信息(工单状态,工单关闭时间,工单关闭时效)
             */
            @Override
            public MSResponse<Integer> updateOrderCloseInfo(Reminder reminder) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            //endregion

            //region 日志

            /**
             * 新增日志
             */
            @Override
            public MSResponse<Long> newLog(@RequestBody ReminderLog log){
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            /**
             * 读取具体催单日志列表
             */
            @Override
            public MSResponse<MSPage<ReminderLog>> getReminderLogs(String quarter , String reminderId){
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<Integer> getStatusById(Long id, String quarter) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<Long> insertReminderItem(Reminder reminder) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<ReminderItem> getReminderItemById(Long id, String quarter) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<Integer> rejectReminderForB2B(Reminder reminder) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<Integer> closeReminderItemForB2B(Reminder reminder) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<Integer> insertReminderLogByB2B(Reminder reminder) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }


            //public MSResponse<MSPage<ReminderLog>> getReminderLogs(String quarter , @PathVariable("reminderId") Long reminderId){
            //    return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            //}

            //endregion

        };
    }
}
