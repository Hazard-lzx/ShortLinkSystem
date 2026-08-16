package com.shortlink.admin.init;

import cn.hutool.crypto.digest.BCrypt;
import com.shortlink.admin.pojo.entity.User;
import com.shortlink.admin.mapper.UserMapper;
import com.shortlink.common.constant.CommonConstant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 默认管理员初始化：用户表为空时自动创建 admin / 123456
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminUserInitializer implements ApplicationRunner {

    private final UserMapper userMapper;

    @Override
    public void run(ApplicationArguments args) {
        Long count = userMapper.selectCount(null);
        if (count != null && count > 0) {
            return;
        }
        User admin = new User();
        admin.setUsername(CommonConstant.DEFAULT_ADMIN_USERNAME);
        admin.setPassword(BCrypt.hashpw(CommonConstant.DEFAULT_ADMIN_PASSWORD));
        admin.setStatus(CommonConstant.STATUS_ENABLE);
        userMapper.insert(admin);
        log.info("已初始化默认管理员账号：{} / {}", CommonConstant.DEFAULT_ADMIN_USERNAME, CommonConstant.DEFAULT_ADMIN_PASSWORD);
    }
}
