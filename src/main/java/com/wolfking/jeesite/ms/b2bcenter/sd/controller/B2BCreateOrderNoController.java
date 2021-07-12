package com.wolfking.jeesite.ms.b2bcenter.sd.controller;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.sd.service.OrderService;
import com.wolfking.jeesite.modules.sys.utils.SeqUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 生成单号
 */
@RestController
@RequestMapping("${adminPath}/ms/b2bCenter")
@Slf4j
public class B2BCreateOrderNoController {


    @Autowired
    private OrderService orderService;

    /**
     * 生成工单号
     * */
    @GetMapping("createOrderNo")
    public MSResponse<String> createOrderNo(){
        String orderNo = "";
        try {
            orderNo = orderService.getNewOrderNo();
            if (StringUtils.isBlank(orderNo)) {
                orderNo = orderService.getNewOrderNo();
            }
        } catch (Exception e) {
            log.error("B2BCreateOrderNoController.createOrderNo:生成工单号失败:",e.getMessage());
        }
        if (StringUtils.isBlank(orderNo)) {
            return new MSResponse<>(MSErrorCode.newInstance(MSErrorCode.FAILURE,"获取工单号失败请重试"),orderNo);
        }
        return new MSResponse<>(MSErrorCode.SUCCESS,orderNo);
    }

    /**
     * 生成投诉单号
     * */
    @GetMapping("generateComplainNo")
    public MSResponse<String> generateComplainNo(){
        String no = generateFormNo("ComplainNo");
        if (StringUtils.isBlank(no)) {
            return new MSResponse<>(MSErrorCode.newInstance(MSErrorCode.FAILURE,"获取投诉单号失败"),no);
        }
        return new MSResponse<>(MSErrorCode.SUCCESS,no);
    }


    private String generateFormNo(String formCode){
        if(StringUtils.isBlank(formCode)){
            return StringUtils.EMPTY;
        }
        int times = 0;
        String no = StringUtils.EMPTY;
        while(times<3) {
            no = SeqUtils.NextSequenceNo(formCode);
            if(StringUtils.isNotBlank(no)){
                return no;
            }
            times++;
        }
        return no;
    }

}
