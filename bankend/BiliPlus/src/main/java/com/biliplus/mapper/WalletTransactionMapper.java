package com.biliplus.mapper;

import com.biliplus.pojo.entity.WalletTransaction;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface WalletTransactionMapper {

    @Insert("INSERT INTO wallet_transaction(user_id, type, amount, balance_after, biz_type, biz_id, remark, create_time) " +
            "VALUES(#{userId}, #{type}, #{amount}, #{balanceAfter}, #{bizType}, #{bizId}, #{remark}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(WalletTransaction tx);

    @Select("<script>" +
            "SELECT * FROM wallet_transaction WHERE user_id = #{userId} " +
            "<if test='type != null'> AND type = #{type}</if> " +
            "ORDER BY id DESC LIMIT #{offset}, #{size}" +
            "</script>")
    List<WalletTransaction> pageByUser(@Param("userId") Long userId,
                                       @Param("type") Integer type,
                                       @Param("offset") int offset,
                                       @Param("size") int size);

    @Select("<script>" +
            "SELECT COUNT(*) FROM wallet_transaction WHERE user_id = #{userId} " +
            "<if test='type != null'> AND type = #{type}</if>" +
            "</script>")
    long countByUser(@Param("userId") Long userId, @Param("type") Integer type);

    /** 管理端对账：按用户/类型/业务类型/时间区间筛选 */
    @Select("<script>" +
            "SELECT * FROM wallet_transaction WHERE 1=1 " +
            "<if test='userId != null'> AND user_id = #{userId}</if> " +
            "<if test='type != null'> AND type = #{type}</if> " +
            "<if test='bizType != null and bizType != \"\"'> AND biz_type = #{bizType}</if> " +
            "<if test='from != null'> AND create_time &gt;= #{from}</if> " +
            "<if test='to != null'> AND create_time &lt;= #{to}</if> " +
            "ORDER BY id DESC LIMIT #{offset}, #{size}" +
            "</script>")
    List<WalletTransaction> adminList(@Param("userId") Long userId,
                                      @Param("type") Integer type,
                                      @Param("bizType") String bizType,
                                      @Param("from") String from,
                                      @Param("to") String to,
                                      @Param("offset") int offset,
                                      @Param("size") int size);

    @Select("<script>" +
            "SELECT COUNT(*) FROM wallet_transaction WHERE 1=1 " +
            "<if test='userId != null'> AND user_id = #{userId}</if> " +
            "<if test='type != null'> AND type = #{type}</if> " +
            "<if test='bizType != null and bizType != \"\"'> AND biz_type = #{bizType}</if> " +
            "<if test='from != null'> AND create_time &gt;= #{from}</if> " +
            "<if test='to != null'> AND create_time &lt;= #{to}</if>" +
            "</script>")
    long adminCount(@Param("userId") Long userId,
                    @Param("type") Integer type,
                    @Param("bizType") String bizType,
                    @Param("from") String from,
                    @Param("to") String to);
}
