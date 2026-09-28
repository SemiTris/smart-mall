package com.huashan.smartmallbackend.controller;

import com.huashan.smartmallbackend.common.R;
import com.huashan.smartmallbackend.entity.User;
import com.huashan.smartmallbackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户接口：登录 / 注册 / 查资料
 * <p>
 * 本项目不使用 JWT：登录成功只返回用户信息（含 role），
 * 由前端存起来并做路由守卫，后端接口不做身份校验。
 *
 * @author hs
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 登录
     *
     * @param loginUser 请求体：{ "username": "...", "password": "..." }
     * @return 成功返回用户信息（含 role，password 已剔除）
     */
    @PostMapping("/login")
    public R<User> login(@RequestBody User loginUser) {
        User user = userService.login(loginUser.getUsername(), loginUser.getPassword());
        if (user == null) {
            return R.fail("用户名或密码错误");
        }
        return R.ok("登录成功", user);
    }

    /**
     * 注册（注册出来一律是普通用户）
     *
     * @param user 请求体：{ "username": "...", "password": "...", "nickname": "..." }
     * @return 注册成功的用户信息
     */
    @PostMapping("/register")
    public R<User> register(@RequestBody User user) {
        return R.ok("注册成功", userService.register(user));
    }

    /**
     * 查询用户资料（前端刷新页面时用）
     *
     * @param userId 用户 id
     * @return 用户信息
     */
    @GetMapping("/info")
    public R<User> info(@RequestParam Long userId) {
        User user = userService.getById(userId);
        if (user == null) {
            return R.fail("用户不存在");
        }
        return R.ok(user);
    }
}