package com.hnust.lx.service;

import com.hnust.lx.dto.AdminLoginDTO;
import com.hnust.lx.result.PageResult;
import com.hnust.lx.vo.UserVO;

public interface AdminService {

    UserVO login(AdminLoginDTO dto);

    PageResult listUsers(Integer page, Integer pageSize);

    void updateUserStatus(Long userId, Integer isDeleted);

    void deleteUser(Long userId);
}