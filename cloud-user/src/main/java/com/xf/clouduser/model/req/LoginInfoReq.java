package com.xf.clouduser.model.req;

import lombok.Data;

/**
 * @program: xf-boot-base
 * @ClassName LoginInfoRes
 * @description:
 * @author: xiongfeng
 * @create: 2022-07-04 11:46
 **/
@Data
public class LoginInfoReq {
    private String account;
    private String password;

}
