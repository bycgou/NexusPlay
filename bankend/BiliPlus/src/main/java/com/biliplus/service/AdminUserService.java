package com.biliplus.service;

import com.biliplus.pojo.dto.admindto.AdminUserLoginDTO;
import com.biliplus.pojo.entity.AdminUser;

public interface AdminUserService {
    AdminUser login(AdminUserLoginDTO adminUserLoginDTO);
}
