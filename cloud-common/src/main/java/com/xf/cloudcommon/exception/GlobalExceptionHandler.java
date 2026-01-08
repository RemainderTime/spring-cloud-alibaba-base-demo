package com.xf.cloudcommon.exception;

import com.xf.cloudcommon.enums.SystemStatus;
import com.xf.cloudcommon.model.RetObj;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @Description: 全局异常处理器
 * @ClassName: GlobalExceptionHandler
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public RetObj<String> handleException(Exception e) {
        log.error("系统内部异常: ", e);
        return RetObj.error("系统繁忙，请稍后重试");
    }

    @ExceptionHandler(RuntimeException.class)
    public RetObj<String> handleRuntimeException(RuntimeException e) {
        log.error("运行时异常: ", e);
        return RetObj.error(e.getMessage());
    }
}
