package com.biliplus.mapper;

import com.biliplus.pojo.entity.UserWallet;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserWalletMapper {

    @Select("SELECT * FROM user_wallet WHERE user_id = #{userId}")
    UserWallet selectByUserId(@Param("userId") Long userId);

    @Insert("INSERT INTO user_wallet(user_id, balance, version) VALUES(#{userId}, 0, 0) " +
            "ON DUPLICATE KEY UPDATE user_id = user_id")
    void ensureWallet(@Param("userId") Long userId);

    @Update("UPDATE user_wallet SET balance = balance + #{amount} WHERE user_id = #{userId}")
    int recharge(@Param("userId") Long userId, @Param("amount") long amount);

    /** 条件扣款，防止超扣；影响行数=0 表示余额不足 */
    @Update("UPDATE user_wallet SET balance = balance - #{amount} " +
            "WHERE user_id = #{userId} AND balance >= #{amount}")
    int deduct(@Param("userId") Long userId, @Param("amount") long amount);
}
