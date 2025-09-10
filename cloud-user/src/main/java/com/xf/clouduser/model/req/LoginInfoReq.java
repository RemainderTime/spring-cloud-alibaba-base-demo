package com.xf.clouduser.model.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @program: xf-boot-base
 * @ClassName LoginInfoRes
 * @description:
 * @author: xiongfeng
 * @create: 2022-07-04 11:46
 **/
@Schema(name = "LoginInfoReq", description = "登录请求对象")
@Data
public class LoginInfoReq {

    @Schema(description = "用户名", example = "admin")
    private String account;

    @Schema(description = "密码", example = "123456")
    private String password;
}
