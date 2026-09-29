package com.huashan.smartmallbackend.entity;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单明细实体（一对多的"多"）
 * <p>
 * 一个订单可以包含多个商品，每个商品一行明细。
 * productName / price 是下单时的快照，商品改价不影响历史订单。
 *
 * @author hs
 */
@Data
public class OrderItem {

    /** 主键 */
    private Long id;

    /** 所属订单 id */
    private Long orderId;

    /** 商品 id（关联 t_product.id） */
    private Long productId;

    /** 商品名称（下单时快照） */
    private String productName;

    /** 单价（下单时快照） */
    private BigDecimal price;

    /** 购买数量 */
    private Integer quantity;

    /** 小计 = price × quantity */
    private BigDecimal subtotal;
}
