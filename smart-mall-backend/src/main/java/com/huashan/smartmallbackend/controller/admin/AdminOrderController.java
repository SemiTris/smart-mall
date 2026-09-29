package com.huashan.smartmallbackend.controller.admin;

import com.github.pagehelper.PageInfo;
import com.huashan.smartmallbackend.common.R;
import com.huashan.smartmallbackend.entity.Order;
import com.huashan.smartmallbackend.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端 - 订单管理
 * <p>
 * 返回的 Order 对象自带 items 明细（一对多），
 * 前端表格展开行即可看到订单里的多个商品。
 *
 * @author hs
 */
@RestController
@RequestMapping("/admin/order")
public class AdminOrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 订单列表（分页，含明细）
     *
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @param username 用户名（模糊，可空）
     * @param status   状态（可空）
     * @return 分页结果
     */
    @GetMapping("/page")
    public R<PageInfo<Order>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                   @RequestParam(defaultValue = "10") Integer pageSize,
                                   @RequestParam(required = false) String username,
                                   @RequestParam(required = false) String status) {
        return R.ok(orderService.page(pageNum, pageSize, username, status));
    }
}
