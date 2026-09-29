package com.huashan.smartmallbackend.mapper;

import com.huashan.smartmallbackend.entity.OrderItem;

import java.util.List;

/**
 * 订单明细 Mapper
 * <p>
 * SQL 写在 resources/mapper/OrderItemMapper.xml 里。
 * 被 OrderMapper.xml 的 {@code <collection>} 调用，用来装配订单下的商品列表。
 *
 * @author hs
 */
public interface OrderItemMapper {

    /**
     * 按订单 id 查询明细列表
     *
     * @param orderId 订单 id
     * @return 明细列表
     */
    List<OrderItem> selectByOrderId(Long orderId);
}
