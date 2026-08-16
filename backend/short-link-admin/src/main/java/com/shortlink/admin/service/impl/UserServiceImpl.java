package com.shortlink.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shortlink.admin.pojo.dto.UserLoginDTO;
import com.shortlink.admin.pojo.dto.UserRegisterDTO;
import com.shortlink.admin.pojo.vo.UserLoginVO;
import com.shortlink.admin.pojo.vo.UserInfoVO;
import com.shortlink.admin.pojo.entity.User;
import com.shortlink.admin.mapper.UserMapper;
import com.shortlink.admin.properties.JwtProperties;
import com.shortlink.admin.service.UserService;
import com.shortlink.common.constant.CommonConstant;
import com.shortlink.common.exception.BizException;
import com.shortlink.common.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

/**
 * 用户服务实现：BCrypt 加密 + JWT 签发（网关统一校验）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final UserMapper userMapper;

    private final JwtProperties jwtProperties;

    /**
     * 用户注册
     * @param reqDTO 注册请求参数，包含用户名、密码、手机号
     * @throws BizException 当用户名已存在时抛出
     */
    @Override
    public void register(UserRegisterDTO reqDTO) {
        User user = new User();
        user.setUsername(reqDTO.getUsername());
        user.setPassword(BCrypt.hashpw(reqDTO.getPassword()));
        user.setPhone(reqDTO.getPhone());
        user.setStatus(CommonConstant.STATUS_ENABLE);
        try {
            userMapper.insert(user);
        } catch (DuplicateKeyException e) {
            throw new BizException("用户名已存在");
        }
        log.info("用户注册成功：{}", user.getUsername());
    }

    /**
     * 用户登录
     * @param reqDTO 登录请求参数，包含用户名和密码
     * @return 登录响应对象，包含 JWT Token、用户名和过期时间
     * @throws BizException 当用户名或密码错误、账号被禁用时抛出
     */
    @Override
    public UserLoginVO login(UserLoginDTO reqDTO) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, reqDTO.getUsername()));
        if (user == null || !BCrypt.checkpw(reqDTO.getPassword(), user.getPassword())) {
            throw new BizException(401, "用户名或密码错误");
        }
        if (CommonConstant.STATUS_DISABLE.equals(user.getStatus())) {
            throw new BizException(403, "账号已被禁用");
        }
        String token = JwtUtil.createToken(user.getUsername(),
                jwtProperties.getSecret(), jwtProperties.getExpireHour());

        UserLoginVO respDTO = new UserLoginVO();
        respDTO.setToken(token);
        respDTO.setUsername(user.getUsername());
        respDTO.setExpireHour(jwtProperties.getExpireHour());
        log.info("用户登录成功：{}", user.getUsername());
        return respDTO;
    }

    /**
     * 获取用户信息
     * @param username 用户名
     * @return 用户信息对象
     * @throws BizException 当用户不存在时抛出
     */
    @Override
    public UserInfoVO getUserInfo(String username) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null) {
            throw new BizException(404, "用户不存在");
        }
        UserInfoVO respDTO = new UserInfoVO();
        BeanUtil.copyProperties(user, respDTO, "createTime");
        if (user.getCreateTime() != null && StrUtil.isNotBlank(user.getCreateTime().toString())) {
            respDTO.setCreateTime(user.getCreateTime().format(FORMATTER));
        }
        return respDTO;
    }
}