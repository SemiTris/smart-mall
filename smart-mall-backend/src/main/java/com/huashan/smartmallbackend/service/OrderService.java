package com.huashan.smartmallbackend.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.huashan.smartmallbackend.entity.Order;
import com.huashan.smartmallbackend.mapper.OrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 订单服务
 * <p>
 * 查出来的 Order 对象已经带好了 items（订单明细），
 * 这是由 OrderMapper 的 &lt;collection&gt; 关联装配的。
 *
 * @author hs
 */
@Service
public class OrderService {

    @Autowired
    private OrderMapper orderMapper;

    /**
     * 管理端分页查询（含明细）
     *
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @param username 用户名（模糊，可空）
     * @param status   状态（可空）
     * @return 分页结果
     */
    public PageInfo<Order> page(Integer pageNum, Integer pageSize, String username, String status) {
        PageHelper.startPage(pageNum, pageSize);
        List<Order> list = orderMapper.selectPage(username, status);
        return new PageInfo<>(list);
    }

    /**
     * 按用户名查询订单（AI 工具调用用）
     *
     * @param username 用户名
     * @return 订单列表（含明细）
     */
    public List<Order> listByUsername(String username) {
        return orderMapper.selectByUsername(username);
    }

    /**
     * 按状态查询订单（AI 工具调用用）
     *
     * @param status 订单状态
     * @return 订单列表（含明细）
     */
    public List<Order> listByStatus(String status) {
        return orderMapper.selectByStatus(status);
    }
}
