package com.shortlink.admin.controller;

import com.shortlink.admin.pojo.dto.UserLoginDTO;
import com.shortlink.admin.pojo.dto.UserRegisterDTO;
import com.shortlink.admin.pojo.vo.UserInfoVO;
import com.shortlink.admin.pojo.vo.UserLoginVO;
import com.shortlink.admin.service.UserService;
import com.shortlink.common.constant.CommonConstant;
import com.shortlink.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "用户管理")
@RestController
@RequestMapping("/api/admin/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody UserRegisterDTO registerDTO) {
        userService.register(registerDTO);
        return Result.success();
    }

    @Operation(summary = "用户登录（返回 JWT Token）")
    @PostMapping("/login")
    public Result<UserLoginVO> login(@Valid @RequestBody UserLoginDTO loginDTO) {
        return Result.success(userService.login(loginDTO));
    }

    @Operation(summary = "当前登录用户信息（用户名由网关鉴权后透传）")
    @GetMapping("/info")
    public Result<UserInfoVO> info(
            @RequestHeader(CommonConstant.HEADER_USERNAME) String username) {
        return Result.success(userService.getUserInfo(username));
    }
}
