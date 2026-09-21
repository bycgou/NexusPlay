package com.biliplus.service.Impl;

import com.biliplus.constant.VideoStatus;
import com.biliplus.exception.BusinessException;
import com.biliplus.mapper.PeopleUserMapper;
import com.biliplus.mapper.UserFollowMapper;
import com.biliplus.mapper.VideoTagMapper;
import com.biliplus.pojo.dto.userdto.UserDTO;
import com.biliplus.pojo.dto.userdto.UserRegisterDTO;
import com.biliplus.pojo.dto.userdto.VideoEditDTO;
import com.biliplus.pojo.dto.userdto.VideoUploadDTO;
import com.biliplus.pojo.entity.Tag;
import com.biliplus.pojo.entity.User;
import com.biliplus.pojo.entity.Video;
import com.biliplus.pojo.vo.MyVideoVO;
import com.biliplus.pojo.vo.VideoUploadVO;
import com.biliplus.service.PeopleUserService;
import com.biliplus.utils.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
public class PeopleUserServiceImpl implements PeopleUserService {

    /** 编辑稿件：单条标签长度上限 */
    private static final int MAX_TAG_LENGTH = 20;
    /** 编辑稿件：标签数量上限 */
    private static final int MAX_TAG_COUNT = 10;

    @Autowired
    private PeopleUserMapper peopleUserMapper;

    @Autowired
    private UserFollowMapper userFollowMapper;

    @Autowired
    private VideoTagMapper videoTagMapper;

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
        replaceVideoTags(video.getId(), videoUploadDTO.getTags());
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
    public List<MyVideoVO> listMyVideos(Long userId) {
        if (userId == null) {
            throw new BusinessException("请先登录");
        }
        List<Video> videos = peopleUserMapper.selectVideosByUserId(userId);
        List<MyVideoVO> result = new ArrayList<>();
        if (videos == null || videos.isEmpty()) {
            return result;
        }
        for (Video video : videos) {
            MyVideoVO vo = new MyVideoVO();
            BeanUtils.copyProperties(video, vo);
            vo.setTags(joinTagNames(video.getId()));
            result.add(vo);
        }
        return result;
    }

    @Override
    @Transactional
    public void updateMyVideo(Long userId, Long videoId, VideoEditDTO dto) {
        Video video = requireOwnedVideo(userId, videoId);
        if (dto == null) {
            throw new BusinessException("缺少修改内容");
        }

        String title = dto.getTitle() == null ? null : dto.getTitle().trim();
        if (title != null) {
            if (title.isEmpty()) {
                throw new BusinessException("标题不能为空");
            }
            if (title.length() > 100) {
                throw new BusinessException("标题不能超过100字");
            }
        }
        String description = dto.getDescription() == null ? null : dto.getDescription().trim();
        if (description != null && description.length() > 2000) {
            throw new BusinessException("简介不能超过2000字");
        }
        String coverUrl = dto.getCoverUrl() == null ? null : dto.getCoverUrl().trim();

        boolean tagsProvided = dto.getTags() != null;
        if (title == null && description == null && dto.getCategoryId() == null
                && coverUrl == null && !tagsProvided) {
            throw new BusinessException("没有需要修改的内容");
        }

        int rows = peopleUserMapper.updateVideoInfo(
                videoId, userId, title, description, dto.getCategoryId(), coverUrl);
        if (rows <= 0) {
            throw new BusinessException("稿件不存在或无权修改");
        }
        if (tagsProvided) {
            replaceVideoTags(videoId, dto.getTags());
        }

        // 被驳回或已下架的稿件改完重新进入待审；审核通过的稿件改文案直接生效
        Integer status = video.getStatus();
        if (status != null && (status == VideoStatus.REJECTED || status == VideoStatus.OFFLINE)) {
            peopleUserMapper.updateVideoStatus(videoId, userId, VideoStatus.PENDING, null);
        }
        log.info("用户 {} 编辑稿件 {}, status={}", userId, videoId, status);
    }

    @Override
    @Transactional
    public void deleteMyVideo(Long userId, Long videoId) {
        requireOwnedVideo(userId, videoId);
        int rows = peopleUserMapper.updateVideoStatus(videoId, userId, VideoStatus.DELETED, null);
        if (rows <= 0) {
            throw new BusinessException("稿件不存在或无权删除");
        }
        // 稿件已不可见，关联标签一并清理，避免脏数据
        videoTagMapper.deleteByVideoId(videoId);
        log.info("用户 {} 删除稿件 {}", userId, videoId);
    }

    @Override
    @Transactional
    public void resubmitMyVideo(Long userId, Long videoId) {
        Video video = requireOwnedVideo(userId, videoId);
        Integer status = video.getStatus();
        if (status == null
                || (status != VideoStatus.REJECTED && status != VideoStatus.OFFLINE)) {
            throw new BusinessException("只有被驳回或已下架的稿件可以重新提交");
        }
        int rows = peopleUserMapper.updateVideoStatus(videoId, userId, VideoStatus.PENDING, null);
        if (rows <= 0) {
            throw new BusinessException("重新提交失败，请稍后再试");
        }
        log.info("用户 {} 重新提交稿件 {}", userId, videoId);
    }

    /** 校验稿件存在、未删除且属于当前用户，避免越权改/删他人稿件 */
    private Video requireOwnedVideo(Long userId, Long videoId) {
        if (userId == null) {
            throw new BusinessException("请先登录");
        }
        if (videoId == null) {
            throw new BusinessException("稿件ID不能为空");
        }
        Video video = peopleUserMapper.selectVideoById(videoId);
        if (video == null || Objects.equals(video.getStatus(), VideoStatus.DELETED)) {
            throw new BusinessException("稿件不存在");
        }
        if (!Objects.equals(video.getUserId(), userId)) {
            throw new BusinessException("无权操作他人稿件");
        }
        return video;
    }

    private String joinTagNames(Long videoId) {
        if (videoId == null) {
            return null;
        }
        List<Tag> tags = videoTagMapper.selectTagsByVideoId(videoId);
        if (tags == null || tags.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (Tag tag : tags) {
            if (!StringUtils.hasText(tag.getName())) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append(',');
            }
            sb.append(tag.getName());
        }
        return sb.length() == 0 ? null : sb.toString();
    }

    /** 用传入的标签名整体替换稿件标签；空字符串表示清空 */
    private void replaceVideoTags(Long videoId, String tags) {
        if (videoId == null) {
            return;
        }
        videoTagMapper.deleteByVideoId(videoId);
        List<String> names = parseTagNames(tags);
        if (names.isEmpty()) {
            return;
        }
        for (String name : names) {
            Tag tag = new Tag();
            tag.setName(name);
            videoTagMapper.upsertTag(tag);
            if (tag.getId() != null) {
                videoTagMapper.insertVideoTag(videoId, tag.getId());
            }
        }
    }

    private List<String> parseTagNames(String tags) {
        if (!StringUtils.hasText(tags)) {
            return Collections.emptyList();
        }
        LinkedHashSet<String> unique = new LinkedHashSet<>();
        for (String raw : tags.split(",")) {
            String name = raw.trim();
            if (name.isEmpty()) {
                continue;
            }
            if (name.length() > MAX_TAG_LENGTH) {
                throw new BusinessException("单个标签不能超过" + MAX_TAG_LENGTH + "个字");
            }
            unique.add(name);
            if (unique.size() > MAX_TAG_COUNT) {
                throw new BusinessException("最多添加" + MAX_TAG_COUNT + "个标签");
            }
        }
        return new ArrayList<>(unique);
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

    @Override
    public void changePassword(String oldPassword, String newPassword) {
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null) {
            throw new RuntimeException("请先登录");
        }
        if (oldPassword == null || oldPassword.trim().isEmpty()) {
            throw new RuntimeException("请输入原密码");
        }
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new RuntimeException("请输入新密码");
        }
        if (newPassword.length() < 8) {
            throw new RuntimeException("新密码长度不能少于8位");
        }
        if (oldPassword.equals(newPassword)) {
            throw new RuntimeException("新密码不能与原密码相同");
        }

        User user = peopleUserMapper.getUserById(currentUserId);
        if (user == null || user.getPassword() == null) {
            throw new RuntimeException("用户不存在");
        }
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("原密码错误");
        }

        int rows = peopleUserMapper.updatePassword(
                currentUserId,
                passwordEncoder.encode(newPassword)
        );
        if (rows <= 0) {
            throw new RuntimeException("密码修改失败，请重试");
        }
        log.info("用户修改密码成功, userId={}", currentUserId);
    }
}
