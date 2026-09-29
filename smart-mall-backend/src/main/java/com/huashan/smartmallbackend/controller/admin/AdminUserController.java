package com.huashan.smartmallbackend.controller.admin;

import com.github.pagehelper.PageInfo;
import com.huashan.smartmallbackend.common.R;
import com.huashan.smartmallbackend.entity.User;
import com.huashan.smartmallbackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端 - 用户管理
 *
 * @author hs
 */
@RestController
@RequestMapping("/admin/user")
public class AdminUserController {

    @Autowired
    private UserService userService;

    /**
     * 用户列表（分页）
     *
     * @param pageNum  页码，默认 1
     * @param pageSize 每页条数，默认 10
     * @param username 用户名（模糊，可空）
     * @param role     角色（可空）
     * @return 分页结果
     */
    @GetMapping("/page")
    public R<PageInfo<User>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                  @RequestParam(defaultValue = "10") Integer pageSize,
                                  @RequestParam(required = false) String username,
                                  @RequestParam(required = false) Integer role) {
        return R.ok(userService.page(pageNum, pageSize, username, role));
    }
}
