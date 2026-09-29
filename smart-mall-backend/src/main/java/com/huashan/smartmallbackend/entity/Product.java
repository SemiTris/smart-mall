package com.huashan.smartmallbackend.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品实体
 * <p>
 * 管理端直接用它收发包（本项目不使用 DTO/VO）。
 * 注意：商品数据与 RAG 知识库（D:/rag 下的售后文档）完全解耦，改商品不影响向量库。
 *
 * @author hs
 */
@Data
public class Product {

    /** 主键 */
    private Long id;

    /** 商品名称（与订单明细的 productName 对齐） */
    private String name;

    /** 售价 */
    private BigDecimal price;

    /** 类别：手机 / 耳机 / 笔记本… */
    private String category;

    /** 品牌 */
    private String brand;

    /** 商品描述 */
    private String description;

    /** 状态：0 下架，1 上架 */
    private Integer status;

    /** 逻辑删除：0 正常，1 已删 */
    private Integer deleted;

    /** 创建时间（@JsonFormat 管 JSON 出入参，@DateTimeFormat 管表单 / URL 参数） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
