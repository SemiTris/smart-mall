package com.huashan.smartmallbackend.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.huashan.smartmallbackend.common.MD5Util;
import com.huashan.smartmallbackend.entity.User;
import com.huashan.smartmallbackend.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户服务
 * <p>
 * 说明：本项目不使用 DTO/VO，直接以实体收发；
 * 分页参数直接传入 pageNum / pageSize，返回 PageInfo（不引入 PageUtil）。
 *
 * @author hs
 */
@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    /**
     * 登录校验
     *
     * @param username    用户名
     * @param rawPassword 明文密码
     * @return 校验通过返回用户（password 已置 null），否则返回 null
     */
    public User login(String username, String rawPassword) {
        if (username == null || rawPassword == null) {
            return null;
        }
        User user = userMapper.selectByUsername(username);
        // 用户不存在 / 已禁用 / 密码不匹配
        if (user == null || user.getStatus() == null || user.getStatus() == 0) {
            return null;
        }
        if (!MD5Util.verify(rawPassword, user.getPassword())) {
            return null;
        }
        user.setPassword(null);
        return user;
    }

    /**
     * 注册（角色固定为普通用户）
     *
     * @param user 用户（含 username / password / nickname）
     * @return 注册成功的用户（password 已置 null）
     * @throws IllegalArgumentException 用户名已存在时抛出
     */
    public User register(User user) {
        if (user.getUsername() == null || user.getUsername().isBlank()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        if (userMapper.countByUsername(user.getUsername()) > 0) {
            throw new IllegalArgumentException("用户名已存在");
        }
        // 昵称缺省用用户名（AI 的 {{currentUser}} 依赖昵称）
        if (user.getNickname() == null || user.getNickname().isBlank()) {
            user.setNickname(user.getUsername());
        }
        user.setPassword(MD5Util.encrypt(user.getPassword()));
        user.setRole(0);
        user.setStatus(1);
        userMapper.insert(user);
        user.setPassword(null);
        return user;
    }

    /**
     * 按 id 查询用户（不返回密码）
     *
     * @param userId 用户 id
     * @return 用户，不存在返回 null
     */
    public User getById(Long userId) {
        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setPassword(null);
        }
        return user;
    }

    /**
     * 取用户昵称（流式对话时作为 {{currentUser}} 传给 AI）
     * <p>
     * 优先用昵称，没有就退回用户名；用户不存在返回"访客"。
     *
     * @param userId 用户 id
     * @return 昵称
     */
    public String nicknameOf(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return "访客";
        }
        String nickname = user.getNickname();
        return (nickname == null || nickname.isBlank()) ? user.getUsername() : nickname;
    }

    /**
     * 管理端分页查询
     *
     * @param pageNum  页码（从 1 开始）
     * @param pageSize 每页条数
     * @param username 用户名（模糊，可空）
     * @param role     角色（可空）
     * @return 分页结果
     */
    public PageInfo<User> page(Integer pageNum, Integer pageSize, String username, Integer role) {
        PageHelper.startPage(pageNum, pageSize);
        List<User> list = userMapper.selectPage(username, role);
        // 列表里的密码一律不返回
        list.forEach(u -> u.setPassword(null));
        return new PageInfo<>(list);
    }
}