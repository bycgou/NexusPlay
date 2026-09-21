package com.biliplus.mapper;

import com.biliplus.pojo.entity.HostIncome;
import org.apache.ibatis.annotations.*;

@Mapper
public interface HostIncomeMapper {

    @Select("SELECT * FROM host_income WHERE user_id = #{userId}")
    HostIncome selectByUserId(@Param("userId") Long userId);

    @Insert("INSERT INTO host_income(user_id, total_income) VALUES(#{userId}, 0) " +
            "ON DUPLICATE KEY UPDATE user_id = user_id")
    void ensureIncome(@Param("userId") Long userId);

    @Update("UPDATE host_income SET total_income = total_income + #{amount} WHERE user_id = #{userId}")
    int addIncome(@Param("userId") Long userId, @Param("amount") long amount);
}
