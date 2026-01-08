package com.xf.cloudcommon.enums;

import lombok.Getter;

@Getter
public enum SystemStatus {

    SUSSES(200, "请求成功"),
    UNAVAILABILITY(401, "token无效"),
    ERROR(500, "系统异常")
    ;


    private Integer code;

    private String errorMessage;

    SystemStatus(Integer code, String errorMessage) {
        this.code = code;
        this.errorMessage = errorMessage;
    }
}
