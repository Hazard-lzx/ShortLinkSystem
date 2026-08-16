package com.shortlink.admin.service;

import com.shortlink.admin.pojo.dto.UserLoginDTO;
import com.shortlink.admin.pojo.dto.UserRegisterDTO;
import com.shortlink.admin.pojo.vo.UserLoginVO;
import com.shortlink.admin.pojo.vo.UserInfoVO;

/**
 * 用户服务
 */
public interface UserService {

    /** 用户注册*/
    void register(UserRegisterDTO reqDTO);

    /** 用户登录 */
    UserLoginVO login(UserLoginDTO reqDTO);

    /** 获取用户信息 */
    UserInfoVO getUserInfo(String username);
}