package com.biliplus.mapper;

import com.biliplus.pojo.entity.RechargeOrder;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface RechargeOrderMapper {

    @Insert("INSERT INTO recharge_order(order_no, user_id, amount, pay_amount, status, pay_channel, create_time, update_time) " +
            "VALUES(#{orderNo}, #{userId}, #{amount}, #{payAmount}, #{status}, #{payChannel}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(RechargeOrder order);

    @Select("SELECT * FROM recharge_order WHERE order_no = #{orderNo}")
    RechargeOrder selectByOrderNo(@Param("orderNo") String orderNo);

    /** 条件更新：只有待支付订单能支付成功，保证并发下不重复入账 */
    @Update("UPDATE recharge_order SET status = 1, pay_channel = #{channel}, paid_time = #{paidTime}, update_time = NOW() " +
            "WHERE order_no = #{orderNo} AND user_id = #{userId} AND status = 0")
    int markPaid(@Param("orderNo") String orderNo,
                 @Param("userId") Long userId,
                 @Param("channel") String channel,
                 @Param("paidTime") LocalDateTime paidTime);
}
