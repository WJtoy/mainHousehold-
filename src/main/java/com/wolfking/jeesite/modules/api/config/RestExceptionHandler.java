package com.wolfking.jeesite.modules.api.config;

import com.wolfking.jeesite.modules.api.util.RestResult;
import com.wolfking.jeesite.modules.api.util.RestResultGenerator;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

/**
 * 异常处理
 */
@RestController
//@ControllerAdvice(annotations = RestController.class)
@RestControllerAdvice(annotations = RestController.class)
public class RestExceptionHandler {

    //404
    //@ExceptionHandler(NoHandlerFoundException.class)
    //@ResponseStatus(value= HttpStatus.NOT_FOUND)
    //@ResponseBody
    //private <T> RestResult<T> requestHandlingNoHandlerFound(NoHandlerFoundException e) {
    //    return RestResultGenerator.custom(ErrorCode.NOT_FOUND.code,ErrorCode.NOT_FOUND.message);
    //}

    //400
    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    private <T> RestResult<T> runtimeExceptionHandler(Exception e) {
        return RestResultGenerator.exception(e.getMessage());
    }

    //400
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    private <T> RestResult<T> illegalParamsExceptionHandler(MethodArgumentNotValidException e) {
        return RestResultGenerator.exception("参数错误");
    }

}
