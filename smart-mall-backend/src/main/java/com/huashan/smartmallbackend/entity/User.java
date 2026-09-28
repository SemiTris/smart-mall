package com.huashan.smartmallbackend.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 用户实体
 * <p>
 * 直接作为收发包使用（本项目不使用 DTO/VO）：
 * 登录 / 注册的请求体就是本类，响应体也是本类（返回前会把 password 置 null）。
 *
 * @author hs
 */
@Data
public class User {

    /**
     * 主键
     */
    private Long id;

    /**
     * 登录账号
     */
    private String username;

    /**
     * 密码（MD5 + 盐），不会返回给前端
     */
    private String password;

    /**
     * 昵称（AI 系统提示词里的 {{currentUser}} 用的就是它）
     */
    private String nickname;

    /**
     * 角色：0 普通用户，1 管理员
     */
    private Integer role;

    /**
     * 状态：0 禁用，1 启用
     */
    private Integer status;

    /**
     * 逻辑删除：0 正常，1 已删
     */
    private Integer deleted;

    /**
     * 创建时间
     * <p>
     * 两个注解分工不同，缺一不可：
     * <ul>
     *     <li>{@code @JsonFormat} —— 管 JSON 的出入参（@RequestBody / 响应体），Jackson 用它</li>
     *     <li>{@code @DateTimeFormat} —— 管表单 / URL 查询参数的绑定，Spring 的转换器用它</li>
     * </ul>
     * 写在这里就不需要全局的 JacksonConfig 了，哪个字段要格式化一目了然。
     * 注意：LocalDateTime 没有时区信息，所以 @JsonFormat 不用写 timezone。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}