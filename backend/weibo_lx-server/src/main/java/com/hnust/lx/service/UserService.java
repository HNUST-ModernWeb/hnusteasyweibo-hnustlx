package com.hnust.lx.service;

import com.hnust.lx.dto.*;
import com.hnust.lx.entity.User;
import com.hnust.lx.vo.UserVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {

    UserVO register(UserRegisterDTO dto);

    UserVO login(UserLoginDTO dto);

    UserVO getUserInfo(Long userId);

    UserVO updateUser(Long userId, UserUpdateDTO dto);

    String uploadAvatar(Long userId, MultipartFile file);

    User findByUsername(String username);

    List<UserVO> getHotUsers(Integer limit);
}
