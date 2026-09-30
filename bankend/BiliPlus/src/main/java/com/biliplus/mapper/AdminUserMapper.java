package com.biliplus.mapper;

import com.biliplus.pojo.entity.AdminUser;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface AdminUserMapper {

    @Select("select * from admin_user where account = #{adminAccount} ")
    AdminUser login(String adminAccount);

    @Update("update admin_user set password = #{password} where id = #{id}")
    int updatePassword(Long id, String password);
}
