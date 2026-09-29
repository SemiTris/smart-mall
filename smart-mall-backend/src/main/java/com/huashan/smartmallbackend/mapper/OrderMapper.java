package com.huashan.smartmallbackend.mapper;

import com.huashan.smartmallbackend.entity.Order;

import java.util.List;

/**
 * 订单 Mapper（一对多关联查询）
 * <p>
 * SQL 与关联装配全部写在 resources/mapper/OrderMapper.xml 里：
 * 通过 {@code <resultMap>} 里的 {@code <collection>} 调用
 * {@link OrderItemMapper#selectByOrderId}，查出订单的同时自动装好 items 列表。
 * <p>
 * <b>注意 N+1</b>：{@code <collection>} 是逐条子查询，10 条订单会执行 11 次 SQL。
 * 教学项目可接受；生产可改成 service 里两次查询手动组装。
 *
 * @author hs
 */
public interface OrderMapper {

    /**
     * 按订单号查询（含明细）
     *
     * @param orderNo 订单号
     * @return 订单，不存在返回 null
     */
    Order selectByOrderNo(String orderNo);

    /**
     * 按用户名查询订单（AI 工具调用用）
     *
     * @param username 用户名
     * @return 订单列表（含明细）
     */
    List<Order> selectByUsername(String username);

    /**
     * 按状态查询订单（AI 工具调用用）
     *
     * @param status 订单状态
     * @return 订单列表（含明细）
     */
    List<Order> selectByStatus(String status);

    /**
     * 管理端分页查询（PageHelper 会自动包 count）
     *
     * @param username 用户名（模糊，可空）
     * @param status   状态（可空）
     * @return 订单列表（含明细）
     */
    List<Order> selectPage(String username, String status);

    /**
     * 统计订单总数
     *
     * @return 总数
     */
    int countAll();
}
