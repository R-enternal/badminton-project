package com.badminton.controller;

import com.badminton.common.Result;
import com.badminton.dto.LoginDTO;
import com.badminton.service.UserService;
import com.badminton.util.UserContext;
import com.badminton.vo.LoginVO;
import com.badminton.vo.UserInfoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户相关接口
 */
@Tag(name = "用户模块", description = "用户登录、个人信息相关接口")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 微信登录
     */
    @Operation(summary = "微信登录", description = "小程序调用 wx.login 获取 code 后传入")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        return Result.success(userService.wxLogin(loginDTO));
    }

    /**
     * 获取当前登录用户信息
     */
    @Operation(summary = "获取用户信息")
    @GetMapping("/info")
    public Result<UserInfoVO> info() {
        return Result.success(userService.getUserInfo(UserContext.getUserId()));
    }

    /**
     * 退出登录
     */
    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public Result<Void> logout() {
        // token 无状态，退出时前端删除即可
        return Result.success();
    }
}
