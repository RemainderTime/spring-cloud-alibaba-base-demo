package com.xf.cloudcommon.model;

import com.xf.cloudcommon.enums.SystemStatus;
import lombok.Data;

/**
 * @Description: 全局统一响应对象
 * @ClassName RetObj
 * @Author: xiongfeng
 */
@Data
public class RetObj<T> {

    private Integer code;

    private String message;

    private T data;

    public RetObj(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public RetObj(T data) {
        this.data = data;
    }

    public RetObj(SystemStatus status) {
        this.code = status.getCode();
        this.message = status.getErrorMessage();
    }

    public RetObj(SystemStatus status, T data) {
        this.code = status.getCode();
        this.message = status.getErrorMessage();
        this.data = data;
    }

    public RetObj(Integer code, String errorMsg) {
        this.code = code;
        this.message = errorMsg;
    }

    public static <T> RetObj<T> success() {
        return new RetObj(SystemStatus.SUSSES);
    }

    public static <T> RetObj<T> success(T data) {
        return new RetObj(SystemStatus.SUSSES, data);
    }


    public static <T> RetObj<T> error(SystemStatus status) {
        return new RetObj(status);
    }

    public static <T> RetObj<T> error(String errorMsg) {
        return new RetObj(SystemStatus.ERROR.getCode(), errorMsg);
    }

}
