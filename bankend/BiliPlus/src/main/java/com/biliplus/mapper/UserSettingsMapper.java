package com.biliplus.mapper;

import com.biliplus.pojo.entity.UserSettings;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserSettingsMapper {

    @Select("SELECT id, user_id, settings_json, create_time, update_time FROM user_settings WHERE user_id = #{userId}")
    UserSettings selectByUserId(@Param("userId") Long userId);

    @Insert("INSERT INTO user_settings(user_id, settings_json, create_time, update_time) " +
            "VALUES(#{userId}, #{settingsJson}, #{createTime}, #{updateTime})")
    int insert(UserSettings userSettings);

    @Update("UPDATE user_settings SET settings_json = #{settingsJson}, update_time = #{updateTime} WHERE user_id = #{userId}")
    int updateByUserId(@Param("userId") Long userId,
                       @Param("settingsJson") String settingsJson,
                       @Param("updateTime") java.time.LocalDateTime updateTime);
}
