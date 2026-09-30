package com.biliplus.mapper;

import com.biliplus.pojo.vo.AdminMemberVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface AdminMemberMapper {

    List<AdminMemberVO> adminList(@Param("keyword") String keyword,
                                  @Param("keywordId") Long keywordId,
                                  @Param("status") Integer status,
                                  @Param("role") Integer role,
                                  @Param("offset") int offset,
                                  @Param("limit") int limit);

    long adminCount(@Param("keyword") String keyword,
                    @Param("keywordId") Long keywordId,
                    @Param("status") Integer status,
                    @Param("role") Integer role);

    AdminMemberVO selectDetail(@Param("id") Long id);

    @Update("UPDATE user SET role = #{role}, update_time = NOW() WHERE id = #{id}")
    int updateRole(@Param("id") Long id, @Param("role") Integer role);
}
