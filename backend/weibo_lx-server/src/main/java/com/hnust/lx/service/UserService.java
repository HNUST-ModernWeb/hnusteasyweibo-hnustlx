package com.hnust.lx.service;

import com.hnust.lx.dto.*;
import com.hnust.lx.entity.User;
import com.hnust.lx.vo.UserVO;

public interface UserService {

    UserVO register(UserRegisterDTO dto);

    UserVO login(UserLoginDTO dto);

    UserVO getUserInfo(Long userId);

    UserVO updateUser(Long userId, UserUpdateDTO dto);

    User findByUsername(String username);
}
