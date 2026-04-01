package com.hnust.lx.service.impl;

import com.hnust.lx.constant.JwtClaimsConstant;
import com.hnust.lx.constant.MessageConstant;
import com.hnust.lx.dto.*;
import com.hnust.lx.entity.User;
import com.hnust.lx.exception.AccountLockedException;
import com.hnust.lx.exception.AccountNotFoundException;
import com.hnust.lx.exception.PasswordErrorException;
import com.hnust.lx.mapper.UserMapper;
import com.hnust.lx.properties.JwtProperties;
import com.hnust.lx.service.UserService;
import com.hnust.lx.utils.JwtUtil;
import com.hnust.lx.vo.UserVO;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final JwtProperties jwtProperties;

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
                .password(dto.getPassword())
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
        if (user == null || !user.getPassword().equals(dto.getPassword())) {
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
        User user = User.builder()
                .userId(userId)
                .username(dto.getUsername())
                .avatar(dto.getAvatar())
                .build();
        userMapper.update(user);
        return getUserInfo(userId);
    }

    @Override
    public User findByUsername(String username) {
        return userMapper.findByUsername(username);
    }
}
