package com.biliplus.service;

import com.biliplus.pojo.dto.userdto.UserDTO;
import com.biliplus.pojo.dto.userdto.UserRegisterDTO;
import com.biliplus.pojo.dto.userdto.VideoUploadDTO;
import com.biliplus.pojo.entity.User;
import com.biliplus.pojo.entity.Video;

public interface PeopleUserService {


    User userlogin(UserRegisterDTO userRegisterDTO);


    void PeopleRegister(String email, String password, String emailCaptcha);

    void updateUser(UserDTO userDTO);

    Video uploadVideo(VideoUploadDTO videoUploadDTO);

    UserDTO getUserById(Long userId);

    UserDTO getUserByName(String name);

    /** 当前登录用户的投稿列表（含审核状态与不通过原因） */
    java.util.List<Video> listMyVideos(Long userId);

    /** 按昵称/用户名搜索用户（不含敏感字段） */
    java.util.List<UserDTO> searchUsers(String keyword, Integer limit);
}
