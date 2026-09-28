package com.huashan.smartmallbackend.mapper;

import com.huashan.smartmallbackend.entity.User;

import java.util.List;

/**
 * 用户 Mapper
 * <p>
 * <b>SQL 全部写在 resources/mapper/UserMapper.xml 里</b>，本接口只声明方法签名。
 * 接口上没有 {@code @Mapper} 注解 —— 启动类上的
 * {@code @MapperScan("com.hs.smart.mapper")} 已经统一扫描了这个包。
 *
 * @author hs
 */
public interface UserMapper {

    /**
     * 按用户名查询（登录用）
     *
     * @param username 用户名
     * @return 用户，不存在返回 null
     */
    User selectByUsername(String username);

    /**
     * 按主键查询
     *
     * @param id 用户 id
     * @return 用户，不存在返回 null
     */
    User selectById(Long id);

    /**
     * 管理端分页查询（PageHelper 会自动包 count）
     *
     * @param username 用户名（模糊，可空）
     * @param role     角色（可空）
     * @return 用户列表
     */
    List<User> selectPage(String username, Integer role);

    /**
     * 新增用户（注册）
     *
     * @param user 用户（password 需调用方先加密）
     * @return 影响行数
     */
    int insert(User user);

    /**
     * 统计用户名是否已存在
     *
     * @param username 用户名
     * @return 数量
     */
    int countByUsername(String username);
}