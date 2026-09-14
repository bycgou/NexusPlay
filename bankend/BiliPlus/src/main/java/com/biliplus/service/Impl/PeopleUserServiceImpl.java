package com.biliplus.service.Impl;

import com.biliplus.mapper.PeopleUserMapper;
import com.biliplus.mapper.UserFollowMapper;
import com.biliplus.pojo.dto.userdto.UserDTO;
import com.biliplus.pojo.dto.userdto.UserRegisterDTO;
import com.biliplus.pojo.dto.userdto.VideoUploadDTO;
import com.biliplus.pojo.entity.User;
import com.biliplus.pojo.entity.Video;
import com.biliplus.pojo.vo.VideoUploadVO;
import com.biliplus.service.PeopleUserService;
import com.biliplus.utils.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
public class PeopleUserServiceImpl implements PeopleUserService {

    @Autowired
    private PeopleUserMapper peopleUserMapper;

    @Autowired
    private UserFollowMapper userFollowMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    // 登录


    @Override
    public User userlogin(UserRegisterDTO userRegisterDTO) {
        String email = userRegisterDTO.getEmail();
        String password = userRegisterDTO.getPassword();
        log.info("用户email:{}", email);
        // 日志脱敏：不输出密码明文
        log.info("用户密码长度:{}", password == null ? 0 : password.length());

        // 1. 先校验入参（避免无效查询，提前返回）
        if (email == null || email.trim().isEmpty()) {
            log.warn("登录失败：传入的邮箱为空");
            return null;
        }
        if (password == null || password.trim().isEmpty()) {
            log.warn("登录失败：传入的密码为空");
            return null;
        }

        // 调用mapper层方法
        User user = peopleUserMapper.userlogin(email);
        if (user == null || user.getEmail() == null) {
            log.warn("登录失败：邮箱不存在, email={}", email);
            return null;
        }

        // BCrypt 密码校验
        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("登录失败：密码错误, email={}", email);
            return null;
        }

        // 返回entity 实体 登陆成功
        return user;
    }

    // 注册
    @Override
    public void PeopleRegister(String email, String password, String emailCaptcha) {
        // 1. 参数校验
        if (email == null || email.trim().isEmpty()) {
            throw new RuntimeException("邮箱不能为空");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new RuntimeException("密码不能为空");
        }
        if (emailCaptcha == null || emailCaptcha.trim().isEmpty()) {
            throw new RuntimeException("验证码不能为空");
        }

        // 2. 检查邮箱是否已注册
        if (peopleUserMapper.countByEmail(email) == 1) {
            throw new RuntimeException("邮箱已存在");
        }

        // 3. 从 Redis 校验邮箱验证码
        String redisKey = String.format("email:verify:%s", email);
        String storedCode = stringRedisTemplate.opsForValue().get(redisKey);
        if (storedCode == null) {
            throw new RuntimeException("验证码已过期，请重新获取");
        }
        if (!storedCode.equals(emailCaptcha.trim())) {
            throw new RuntimeException("验证码错误");
        }

        // 4. 验证码校验通过，删除 Redis 中的验证码（防止重复使用）
        stringRedisTemplate.delete(redisKey);

        // 5. 创建用户
        String username = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 10);
        User user = new User();
        user.setUsername(username);
        // BCrypt 加密存储密码
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        user.setRole(0); // 角色,0-普通用户,1-UP主,2-管理员
        user.setStatus(1); // 状态,0-禁用,1-正常
        peopleUserMapper.insert(user);
        log.info("用户注册成功, email={}, username={}", email, username);
    }

    // 修改用户信息
    @Override
    public void updateUser(UserDTO userDTO) {
        log.info("userDTO的Service实现");
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null) {
            throw new RuntimeException("请先登录");
        }
        // 强制使用 JWT 身份，禁止改他人资料
        User user = new User();
        user.setId(currentUserId);
        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        user.setPhone(userDTO.getPhone());
        user.setAvatar(userDTO.getAvatar());
        user.setNickname(userDTO.getNickname());
        user.setSignature(userDTO.getSignature());
        user.setUpdateTime(LocalDateTime.now());
        log.info("需要修改的用户信息user:{}", user);
        peopleUserMapper.update(user);

    }

    @Override
    public Video uploadVideo(VideoUploadDTO videoUploadDTO) {
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null) {
            throw new RuntimeException("请先登录后再投稿");
        }
        log.info("投稿视频: userId={}, title={}, categoryId={}",
                currentUserId, videoUploadDTO.getTitle(), videoUploadDTO.getCategoryId());

        Video video = new Video();
        // 强制使用登录用户，忽略请求体中的 userId
        video.setUserId(currentUserId);
        video.setTitle(videoUploadDTO.getTitle());
        video.setDescription(videoUploadDTO.getDescription());
        video.setCoverUrl(videoUploadDTO.getCoverUrl());
        video.setVideoUrl(videoUploadDTO.getVideoUrl());
        video.setDuration(videoUploadDTO.getDuration());
        // 默认待审核，管理端通过后变为 1
        video.setStatus(0);
        video.setViewCount(0L);
        video.setLikeCount(0L);
        video.setCommentCount(0);
        video.setShareCount(0);
        video.setCreateTime(LocalDateTime.now());
        video.setUpdateTime(LocalDateTime.now());

        Integer categoryId = videoUploadDTO.getCategoryId();
        video.setCategoryId(categoryId != null ? categoryId : 1);

        peopleUserMapper.insertVideo(video);
        log.info("投稿视频成功, videoId={}", video.getId());
        return video;
    }

    @Override
    public UserDTO getUserById(Long userId) {
        log.info("Service层-根据id查询用户信息:{}", userId);

        User user = peopleUserMapper.getUserById(userId);
        if (user == null) {
            return null;
        }
        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user, userDTO);
        try {
            userDTO.setFansCount(userFollowMapper.countFans(userId));
            userDTO.setFollowingCount(userFollowMapper.countFollowing(userId));
        } catch (Exception e) {
            log.warn("查询粉丝/关注数失败: {}", e.getMessage());
        }
        log.info("查询用户信息成功:{}", userDTO);
        return userDTO;
    }

    @Override
    public UserDTO getUserByName(String name) {
        log.info("Service层-根据id查询用户信息:{}", name);

        User user = peopleUserMapper.getUserByName(name);
        UserDTO userDTO = new UserDTO();
        if (user != null) {
            BeanUtils.copyProperties(user, userDTO);
            log.info("查询用户信息成功:{}", userDTO);
            return userDTO;
        }
        return null;
    }

    @Override
    public java.util.List<Video> listMyVideos(Long userId) {
        if (userId == null) {
            throw new RuntimeException("请先登录");
        }
        return peopleUserMapper.selectVideosByUserId(userId);
    }

    @Override
    public java.util.List<UserDTO> searchUsers(String keyword, Integer limit) {
        String kw = keyword == null ? "" : keyword.trim();
        if (kw.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        int max = limit == null || limit < 1 ? 20 : Math.min(limit, 50);
        java.util.List<User> users = peopleUserMapper.searchByKeyword(kw);
        if (users == null || users.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        java.util.List<UserDTO> result = new java.util.ArrayList<>();
        for (int i = 0; i < users.size() && i < max; i++) {
            User u = users.get(i);
            UserDTO dto = new UserDTO();
            dto.setId(u.getId());
            dto.setUsername(u.getUsername());
            dto.setNickname(u.getNickname());
            dto.setAvatar(u.getAvatar());
            dto.setSignature(u.getSignature());
            try {
                dto.setFansCount(userFollowMapper.countFans(u.getId()));
            } catch (Exception ignored) {
                dto.setFansCount(0L);
            }
            result.add(dto);
        }
        return result;
    }
}
