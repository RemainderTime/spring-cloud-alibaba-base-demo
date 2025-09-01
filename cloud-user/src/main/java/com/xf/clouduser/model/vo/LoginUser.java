package com.xf.clouduser.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @program: xf-boot-base
 * @ClassName LoginUserInfo
 * @description:
 * @author: xiongfeng
 * @create: 2022-06-17 14:54
 **/
@Data
public class LoginUser implements Serializable {

    private Integer id;

    private String name;

    private String account;

    private String phone;

    private String token;
}
