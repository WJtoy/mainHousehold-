package com.wolfking.jeesite.ms.utils;

import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.wolfking.jeesite.common.utils.Exceptions;

/**
 * @author: Zhoucy
 * @date: 2020/11/16
 * @Description:
 */
public class FallbackFactoryUtils {

    public static <T> MSResponse<T> createFeignFallbackResponse(Throwable throwable) {
       String errorMsg = Exceptions.getStackTraceAsString(throwable);
       return new MSResponse<>(new MSErrorCode(MSErrorCode.FALLBACK_FAILURE.getCode(), errorMsg));
    }

}
