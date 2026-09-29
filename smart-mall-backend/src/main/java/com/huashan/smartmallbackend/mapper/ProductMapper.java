package com.huashan.smartmallbackend.mapper;

import com.huashan.smartmallbackend.entity.Product;

import java.util.List;

/**
 * 商品 Mapper
 * <p>
 * SQL 全部写在 resources/mapper/ProductMapper.xml 里。
 *
 * @author hs
 */
public interface ProductMapper {

    /**
     * 按主键查询
     *
     * @param id 商品 id
     * @return 商品，不存在返回 null
     */
    Product selectById(Long id);

    /**
     * 管理端分页查询（PageHelper 自动包 count）
     *
     * @param keyword  名称/品牌关键词（可空）
     * @param category 类别（可空）
     * @param status   状态（可空）
     * @return 商品列表
     */
    List<Product> selectPage(String keyword, String category, Integer status);

    /**
     * 新增商品
     *
     * @param product 商品
     * @return 影响行数
     */
    int insert(Product product);

    /**
     * 修改商品
     *
     * @param product 商品（必须带 id）
     * @return 影响行数
     */
    int update(Product product);

    /**
     * 逻辑删除
     *
     * @param id 商品 id
     * @return 影响行数
     */
    int deleteById(Long id);

    /**
     * 上下架
     *
     * @param id     商品 id
     * @param status 0 下架 / 1 上架
     * @return 影响行数
     */
    int updateStatus(Long id, Integer status);

    /**
     * 查询全部上架商品（供用户端浏览）
     *
     * @return 商品列表
     */
    List<Product> selectAllOnShelf();
}
