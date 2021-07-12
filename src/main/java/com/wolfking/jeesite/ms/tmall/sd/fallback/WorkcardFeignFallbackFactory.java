package com.wolfking.jeesite.ms.tmall.sd.fallback;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2b.order.WorkcardInfo;
import com.kkl.kklplus.entity.b2b.order.WorkcardSearchModel;
import com.kkl.kklplus.entity.b2b.order.WorkcardStatusUpdate;
import com.kkl.kklplus.entity.b2b.order.WorkcardTransferResult;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrder;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderSearchModel;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderTransferResult;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.tmall.sd.AnomalyRecourseRemarkUpdate;
import com.kkl.kklplus.entity.tmall.sd.ServiceMonitorMessageUpdate;
import com.wolfking.jeesite.ms.tmall.sd.feign.WorkcardFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.assertj.core.util.Lists;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Random;

@Slf4j
@Component
public class WorkcardFeignFallbackFactory implements FallbackFactory<WorkcardFeign> {

    private static String errorMsg = "操作超时";

    @Override
    public WorkcardFeign create(Throwable throwable) {
        if(throwable != null) {
            log.error("WorkcardFeignFallbackFactory:{}", throwable.getMessage());
        }
        return new WorkcardFeign() {

            /**
             * 查询待转换订单列表
             */
            @Override
            public MSResponse<MSPage<WorkcardInfo>> getList(WorkcardSearchModel workcardSearchModel) {
                /* 正式代码*/
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);

                /*以下是测试代码
                return testGetList();
                */
            }

            /**
             * 查询待转换订单列表(新版本)
             */
            @Override
            public MSResponse<MSPage<B2BOrder>> getListOrder(B2BOrderSearchModel orderSearchModel) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            /**
             * 批量检查工单是否可转换
             */
            @Override
            public MSResponse checkWorkcardProcessFlag(@RequestBody List<B2BOrderTransferResult> workcardIds) {
                /* 正式代码 */
                return new MSResponse<>(MSErrorCode.newInstance(MSErrorCode.FALLBACK_FAILURE, errorMsg));

                /*以下是测试代码，默认通过检查
                return testCheckWorkcardProcessFlag();
                */
            }

            @Override
            public MSResponse updateTransferResult(List<WorkcardTransferResult> workcardTransferResult) {
                /* 正式代码 */
                return new MSResponse<>(MSErrorCode.newInstance(MSErrorCode.FALLBACK_FAILURE, errorMsg));

                /*以下是测试代码
                return new MSResponse<>(MSErrorCode.newInstance(MSErrorCode.SUCCESS, errorMsg));
                */
            }

            @Override
            public MSResponse updateOrderTransferResult(List<B2BOrderTransferResult> workcardTransferResult) {
                /* 正式代码 */
                return new MSResponse<>(MSErrorCode.newInstance(MSErrorCode.FALLBACK_FAILURE, errorMsg));

                /*以下是测试代码
                return new MSResponse<>(MSErrorCode.newInstance(MSErrorCode.SUCCESS, errorMsg));
                */
            }

            @Override
            public MSResponse<String> updateWorkcardProcessStatus(WorkcardStatusUpdate workcardStatusUpdate) {
                return new MSResponse<>(MSErrorCode.newInstance(MSErrorCode.FALLBACK_FAILURE, errorMsg));
            }

            /**
             * 反馈天猫一键求助
             */
            @Override
            public MSResponse anomalyRecourseFeedback(AnomalyRecourseRemarkUpdate feedback) {
                return new MSResponse<>(MSErrorCode.newInstance(MSErrorCode.FALLBACK_FAILURE, errorMsg));
            }

            /**
             * 反馈天猫预警
             */
            @Override
            public MSResponse updateServiceMonitorMessageStatus(ServiceMonitorMessageUpdate serviceMonitorMessageUpdate) {
                return new MSResponse<>(MSErrorCode.newInstance(MSErrorCode.FALLBACK_FAILURE, errorMsg));
            }

            /**
             * 取消转单
             */
            @Override
            public MSResponse cancelOrderTransition(B2BOrderTransferResult b2BOrderTransferResult) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse directCancel(B2BOrderTransferResult workcardTransferResults) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse ignoreCancel(B2BOrderTransferResult result) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse updateAbnormalOrderFlagAll() {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<MSPage<B2BOrder>> getListUnknownOrder(B2BOrderSearchModel b2BOrderSearchModel) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse updateSystemIdAll() {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };
    }

    /**
     * 测试返回订单列表
     * 返回3笔
     */
    public MSResponse<MSPage<WorkcardInfo>> testGetList() {
        Random random = new Random();
        List<WorkcardInfo> list = Lists.newArrayList();
        WorkcardInfo workcardInfo = new WorkcardInfo();
        workcardInfo.setQuarter("20182");
        workcardInfo.setWorkcardId(RandomUtils.nextLong(1, 9999999999L));
        //workcardInfo.setWorkcardId(12345678L);
        workcardInfo.setSellerShopId(500295137L);
        workcardInfo.setBrand("Setir/森太");
        workcardInfo.setTaskStatus(2);
        workcardInfo.setAuctionName("侧吸式油烟机B560Q");
        workcardInfo.setCategory("油烟机");
        workcardInfo.setCategoryId(350511L);
        workcardInfo.setServiceCode("sendAndInstall");
        workcardInfo.setServiceCount(RandomUtils.nextInt(1, 3));
        workcardInfo.setBuyerName("戚生");
        workcardInfo.setBuyerMobile("18700000000");//13700000000
        //workcardInfo.setBuyerAddress("广东省深圳市龙华区尚美时代东观东路57号813室");
        workcardInfo.setBuyerAddress("上海市上海市松江区新桥镇新桥明中路1588弄");
        workcardInfo.setProcessFlag(0);
        workcardInfo.setProcessTime(0);
        list.add(workcardInfo);

        workcardInfo = new WorkcardInfo();
        workcardInfo.setQuarter("20182");
        //workcardInfo.setWorkcardId(12345679L);
        //workcardInfo.setWorkcardId(Math.abs(random.nextLong()));
        workcardInfo.setWorkcardId(RandomUtils.nextLong(1, 9999999999L));
        workcardInfo.setSellerShopId(500295137L);
        workcardInfo.setBrand("Setir/森太");
        workcardInfo.setTaskStatus(2);
        workcardInfo.setAuctionName("燃气灶T81");
        workcardInfo.setCategory("燃气灶");
        workcardInfo.setCategoryId(50015382L);
        workcardInfo.setServiceCode("sendAndInstall");
        //workcardInfo.setServiceCount(1);
        workcardInfo.setServiceCount(RandomUtils.nextInt(1, 3));
        workcardInfo.setBuyerName("吴用");
        workcardInfo.setBuyerMobile("13300000000");
        workcardInfo.setBuyerAddress("广东省中山市坦洲区雅居乐三栋501");
        workcardInfo.setProcessFlag(2);
        workcardInfo.setProcessTime(1);
        workcardInfo.setProcessComment("业务数据不满足要求");
        list.add(workcardInfo);

        workcardInfo = new WorkcardInfo();
        workcardInfo.setQuarter("20182");
        //workcardInfo.setWorkcardId(12345676L);
        workcardInfo.setWorkcardId(RandomUtils.nextLong(1, 9999999999L));
        workcardInfo.setSellerShopId(500295137L);
        workcardInfo.setBrand("Setir/森太");
        workcardInfo.setTaskStatus(2);
        workcardInfo.setAuctionName("集成灶90T1");
        workcardInfo.setCategory("集成灶");
        workcardInfo.setCategoryId(124406004L);
        workcardInfo.setServiceCode("sendAndInstall");
        //workcardInfo.setServiceCount(1);
        workcardInfo.setServiceCount(RandomUtils.nextInt(1, 3));
        workcardInfo.setBuyerName("周春晖");
        workcardInfo.setBuyerMobile("13662880516");
        workcardInfo.setBuyerAddress("广东省 广州市 增城区  新塘镇 新景豪庭1-1205");
        //workcardInfo.setBuyerAddress("黑龙江省鸡西市鸡冠区向阳街道园林小区正门进去右侧楼第四个门市房");
        workcardInfo.setProcessFlag(3);
        workcardInfo.setProcessTime(2);
        workcardInfo.setProcessComment("处理发生错误");
        list.add(workcardInfo);

        MSPage<WorkcardInfo> page = new MSPage<>(1, 10);
        page.setRowCount(80);
        page.setList(list);
        MSResponse msResponse = new MSResponse<>(MSErrorCode.newInstance(MSErrorCode.SUCCESS, errorMsg));
        msResponse.setData(page);
        return msResponse;
    }

    /**
     * 测试检查成功
     */
    private MSResponse testCheckWorkcardProcessFlag() {
        return new MSResponse<>(MSErrorCode.newInstance(MSErrorCode.SUCCESS, ""));
    }
}
