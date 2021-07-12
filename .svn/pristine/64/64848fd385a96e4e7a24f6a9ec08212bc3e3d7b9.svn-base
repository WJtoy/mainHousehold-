package com.wolfking.jeesite.common.exception;

/**
 * B2B催单处理异常类
 */
public class OrderReminderException extends RuntimeException {

    public static final int ERROR_CODE_ORDER_NOT_FOUND = 10;//工单没有找到
    public static final int ERROR_CODE_ORDER_STATUS_ERROR = 20;//工单状态不满足催单要求
    public static final int ERROR_CODE_DO_NOT_REMINDER = 30;//不允许催单，工单当前有催单未完成
    public static final int ERROR_CODE_GENERATE_REMINDER_NO_FAILURE = 40;//生成催单单号失败

    private Integer errorCode = 0;

    public OrderReminderException(){
        super();
    }

    public OrderReminderException(Integer errorCode, String message){
        super(message);
        this.errorCode = errorCode;
    }

    public Integer getErrorCode() {
        return errorCode;
    }
}
