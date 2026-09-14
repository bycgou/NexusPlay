package com.biliplus.pojo.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class AdminUserLoginVO implements Serializable {

    private Integer id;

    private String account;

    private String name;

    private String token;

}
