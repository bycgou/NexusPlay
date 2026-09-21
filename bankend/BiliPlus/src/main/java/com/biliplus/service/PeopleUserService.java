package com.biliplus.service;

import com.biliplus.pojo.dto.userdto.UserDTO;
import com.biliplus.pojo.dto.userdto.UserRegisterDTO;
import com.biliplus.pojo.dto.userdto.VideoEditDTO;
import com.biliplus.pojo.dto.userdto.VideoUploadDTO;
import com.biliplus.pojo.entity.User;
import com.biliplus.pojo.entity.Video;
import com.biliplus.pojo.vo.MyVideoVO;

public interface PeopleUserService {


    User userlogin(UserRegisterDTO userRegisterDTO);


    void PeopleRegister(String email, String password, String emailCaptcha);

    void updateUser(UserDTO userDTO);

    Video uploadVideo(VideoUploadDTO videoUploadDTO);

    UserDTO getUserById(Long userId);

    UserDTO getUserByName(String name);

    /** 当前登录用户的投稿列表（含审核状态、不通过原因与标签） */
    java.util.List<MyVideoVO> listMyVideos(Long userId);

    /** 编辑自有稿件；被驳回/已下架的稿件改完自动回到待审 */
    void updateMyVideo(Long userId, Long videoId, VideoEditDTO dto);

    /** 软删自有稿件（status=-1），前台列表与详情不再可见 */
    void deleteMyVideo(Long userId, Long videoId);

    /** 驳回稿件修改后重新提交审核 */
    void resubmitMyVideo(Long userId, Long videoId);

    /** 按昵称/用户名搜索用户（不含敏感字段） */
    java.util.List<UserDTO> searchUsers(String keyword, Integer limit);

    /** 修改当前登录用户密码 */
    void changePassword(String oldPassword, String newPassword);
}
