package com.huashan.smartmallbackend.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单实体（一对多的"一"）
 * <p>
 * items 字段由 OrderMapper.xml 里的 &lt;collection&gt; 关联装配，
 * 所以一次查询就能拿到"订单 + 订单下的所有商品"。
 *
 * @author hs
 */
@Data
public class Order {

    /** 主键 */
    private Long id;

    /** 订单号 */
    private String orderNo;

    /** 下单用户 id */
    private Long userId;

    /** 下单用户名（冗余字段，便于 AI 查询与展示） */
    private String username;

    /** 订单总金额 */
    private BigDecimal totalAmount;

    /** 状态：已支付 / 已发货 / 已完成 */
    private String status;

    /** 下单时间（@JsonFormat 管 JSON 出入参，@DateTimeFormat 管表单 / URL 参数） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 订单明细（一对多，一个订单含多个商品） */
    private List<OrderItem> items;
}
