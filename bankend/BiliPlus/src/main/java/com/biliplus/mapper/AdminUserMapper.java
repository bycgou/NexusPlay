package com.biliplus.mapper;

import com.biliplus.pojo.entity.AdminUser;
import org.apache.ibatis.annotations.Select;

public interface AdminUserMapper {

    @Select("select * from admin_user where account = #{adminAccount} ")
    AdminUser login(String adminAccount);
}
