package com.xf.clouduser.model;

import com.xf.clouduser.enums.SystemStatus;
import lombok.Data;

/**
 * @program: xf-boot-base
 * @ClassName RetObj
 * @description: 全局统一响应对象
 * @author: xiongfeng
 * @create: 2022-07-04 15:57
 **/
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
