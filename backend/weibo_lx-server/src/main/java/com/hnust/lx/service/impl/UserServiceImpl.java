package com.hnust.lx.service.impl;

import com.hnust.lx.constant.JwtClaimsConstant;
import com.hnust.lx.constant.MessageConstant;
import com.hnust.lx.dto.*;
import com.hnust.lx.entity.User;
import com.hnust.lx.exception.AccountLockedException;
import com.hnust.lx.exception.AccountNotFoundException;
import com.hnust.lx.exception.PasswordErrorException;
import com.hnust.lx.mapper.PostMapper;
import com.hnust.lx.mapper.UserMapper;
import com.hnust.lx.properties.JwtProperties;
import com.hnust.lx.service.UserService;
import com.hnust.lx.utils.JwtUtil;
import com.hnust.lx.vo.UserVO;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PostMapper postMapper;
    private final JwtProperties jwtProperties;
    private final PasswordEncoder passwordEncoder;

    @Value("${weibo.web.upload-path}")
    private String uploadPath;

    private String generateToken(Long userId, Integer userType) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, userId);
        claims.put(JwtClaimsConstant.USER_TYPE, userType);
        return JwtUtil.createJWT(
            jwtProperties.getUserSecretKey(),
            jwtProperties.getUserTtl(),
            claims
        );
    }

    @Override
    public UserVO register(UserRegisterDTO dto) {
        User user = User.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .avatar(dto.getAvatar())
                .userType(0)
                .isDeleted(0)
                .build();
        userMapper.insert(user);
        
        String token = generateToken(user.getUserId(), user.getUserType());
        return UserVO.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .avatar(user.getAvatar())
                .createTime(user.getRegisterTime())
                .userType(user.getUserType())
                .status(1)
                .token(token)
                .build();
    }

    @Override
    public UserVO login(UserLoginDTO dto) {
        User user = userMapper.findByUsername(dto.getUsername());
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }
        if (user.getIsDeleted() == 1) {
            throw new AccountLockedException(MessageConstant.ACCOUNT_DISABLED);
        }
        
        String token = generateToken(user.getUserId(), user.getUserType());
        return UserVO.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .avatar(user.getAvatar())
                .createTime(user.getRegisterTime())
                .userType(user.getUserType())
                .status(user.getIsDeleted() == 0 ? 1 : 0)
                .token(token)
                .build();
    }

    @Override
    public UserVO getUserInfo(Long userId) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new AccountNotFoundException(MessageConstant.USER_NOT_FOUND);
        }
        return UserVO.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .avatar(user.getAvatar())
                .createTime(user.getRegisterTime())
                .status(user.getIsDeleted() == 0 ? 1 : 0)
                .build();
    }

    @Override
    public UserVO updateUser(Long userId, UserUpdateDTO dto) {
        // 1. 先查询当前用户信息
        User currentUser = userMapper.findById(userId);
        if (currentUser == null) {
            throw new AccountNotFoundException(MessageConstant.USER_NOT_FOUND);
        }
        
        // 2. 只更新非空的字段
        String username = dto.getUsername() != null ? dto.getUsername() : currentUser.getUsername();
        String avatar = dto.getAvatar() != null ? dto.getAvatar() : currentUser.getAvatar();
        
        User user = User.builder()
                .userId(userId)
                .username(username)
                .avatar(avatar)
                .build();
        userMapper.update(user);
        return getUserInfo(userId);
    }

    @Override
    public String uploadAvatar(Long userId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }
        
        // 获取文件扩展名
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        
        // 生成新文件名
        String fileName = UUID.randomUUID().toString().replace("-", "") + extension;
        
        // 创建上传目录
        File uploadDir = new File(uploadPath + "avatar/");
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
        
        // 保存文件
        File destFile = new File(uploadDir, fileName);
        try {
            file.transferTo(destFile);
        } catch (IOException e) {
            throw new RuntimeException("文件保存失败: " + e.getMessage());
        }
        
        // 更新用户头像路径，返回完整URL
        String avatarUrl = "http://localhost:8080/upload/avatar/" + fileName;
        
        // 先查询当前用户，保留原来的 username
        User currentUser = userMapper.findById(userId);
        String username = currentUser != null ? currentUser.getUsername() : null;
        
        User user = User.builder()
                .userId(userId)
                .username(username)
                .avatar(avatarUrl)
                .build();
        userMapper.update(user);
        
        return avatarUrl;
    }

    @Override
    public User findByUsername(String username) {
        return userMapper.findByUsername(username);
    }

    @Override
    public List<UserVO> getHotUsers(Integer limit) {
        List<PostMapper.UserLikes> userLikes = postMapper.countLikesByUserId(limit);
        
        return userLikes.stream()
                .map(ul -> {
                    User user = userMapper.findById(ul.getUserId());
                    return UserVO.builder()
                            .userId(ul.getUserId())
                            .username(user != null ? user.getUsername() : null)
                            .avatar(user != null ? user.getAvatar() : null)
                            .totalLikes(ul.getTotalLikes())
                            .build();
                })
                .collect(java.util.stream.Collectors.toList());
    }
}