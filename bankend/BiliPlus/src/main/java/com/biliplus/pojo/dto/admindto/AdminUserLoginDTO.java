package com.biliplus.pojo.dto.admindto;

import lombok.Data;

import java.io.Serializable;

@Data
public class AdminUserLoginDTO implements Serializable {
    private String account;
    private String password;
}
