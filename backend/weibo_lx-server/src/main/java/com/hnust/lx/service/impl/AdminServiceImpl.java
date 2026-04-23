package com.hnust.lx.service.impl;

import com.hnust.lx.constant.JwtClaimsConstant;
import com.hnust.lx.constant.MessageConstant;
import com.hnust.lx.dto.AdminLoginDTO;
import com.hnust.lx.entity.User;
import com.hnust.lx.exception.PasswordErrorException;
import com.hnust.lx.exception.ResourceNotFoundException;
import com.hnust.lx.mapper.AdminMapper;
import com.hnust.lx.properties.JwtProperties;
import com.hnust.lx.result.PageResult;
import com.hnust.lx.service.AdminService;
import com.hnust.lx.utils.JwtUtil;
import com.hnust.lx.vo.StatsVO;
import com.hnust.lx.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final AdminMapper adminMapper;
    private final JwtProperties jwtProperties;
    private final PasswordEncoder passwordEncoder;

    private String generateToken(Long userId, Integer userType) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, userId);
        claims.put(JwtClaimsConstant.USER_TYPE, userType);
        return JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims
        );
    }

    @Override
    public UserVO login(AdminLoginDTO dto) {
        User user = adminMapper.login(dto.getUsername());
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new PasswordErrorException(MessageConstant.ADMIN_PASSWORD_ERROR);
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
    public PageResult listUsers(Integer page, Integer pageSize) {
        int offset = (page - 1) * pageSize;
        List<User> users = adminMapper.listUsers(offset, pageSize);
        Long total = adminMapper.countUsers();
        
        List<UserVO> userVOList = users.stream().map(user -> UserVO.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .avatar(user.getAvatar())
                .createTime(user.getRegisterTime())
                .userType(user.getUserType())
                .status(user.getIsDeleted() == 0 ? 1 : 0)
                .build()).collect(Collectors.toList());
        
        return new PageResult(total, userVOList);
    }

    @Override
    public void updateUserStatus(Long userId, Integer isDeleted) {
        User user = adminMapper.getById(userId);
        if (user == null) {
            throw new ResourceNotFoundException(MessageConstant.USER_NOT_FOUND);
        }
        adminMapper.updateStatus(userId, isDeleted);
    }

    @Override
    public void deleteUser(Long userId) {
        User user = adminMapper.getById(userId);
        if (user == null) {
            throw new ResourceNotFoundException(MessageConstant.USER_NOT_FOUND);
        }
        adminMapper.deleteById(userId);
    }

    @Override
    public StatsVO getStats() {
        Long userCount = adminMapper.countAllUsers();
        Long postCount = adminMapper.countAllPosts();
        Long commentCount = adminMapper.countAllComments();
        Long tagCount = adminMapper.countAllTags();
        
        return StatsVO.builder()
                .userCount(userCount)
                .postCount(postCount)
                .commentCount(commentCount)
                .tagCount(tagCount)
                .build();
    }
}