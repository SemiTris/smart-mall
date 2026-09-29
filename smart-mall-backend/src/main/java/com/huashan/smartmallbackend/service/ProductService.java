package com.huashan.smartmallbackend.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.huashan.smartmallbackend.entity.Product;
import com.huashan.smartmallbackend.mapper.ProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品服务
 * <p>
 * 分页参数直接传入 pageNum / pageSize，返回 PageInfo（不使用 PageUtil）。
 *
 * @author hs
 */
@Service
public class ProductService {

    @Autowired
    private ProductMapper productMapper;

    /**
     * 管理端分页查询
     *
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @param keyword  名称/品牌关键词（可空）
     * @param category 类别（可空）
     * @param status   状态（可空）
     * @return 分页结果
     */
    public PageInfo<Product> page(Integer pageNum, Integer pageSize,
                                  String keyword, String category, Integer status) {
        PageHelper.startPage(pageNum, pageSize);
        List<Product> list = productMapper.selectPage(keyword, category, status);
        return new PageInfo<>(list);
    }

    /**
     * 查询全部上架商品（用户端浏览用）
     *
     * @return 商品列表
     */
    public List<Product> listOnShelf() {
        return productMapper.selectAllOnShelf();
    }

    /**
     * 按 id 查询
     *
     * @param id 商品 id
     * @return 商品
     */
    public Product getById(Long id) {
        return productMapper.selectById(id);
    }

    /**
     * 新增或修改商品（id 为空走新增，否则走修改）
     *
     * @param product 商品
     * @return 保存后的商品
     */
    public Product saveOrUpdate(Product product) {
        if (product.getName() == null || product.getName().isBlank()) {
            throw new IllegalArgumentException("商品名称不能为空");
        }
        if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("商品价格不能为空且不能为负");
        }
        if (product.getCategory() == null || product.getCategory().isBlank()) {
            throw new IllegalArgumentException("商品类别不能为空");
        }
        // 状态缺省为上架
        if (product.getStatus() == null) {
            product.setStatus(1);
        }

        if (product.getId() == null) {
            productMapper.insert(product);
        } else {
            productMapper.update(product);
        }
        return productMapper.selectById(product.getId());
    }

    /**
     * 逻辑删除商品
     *
     * @param id 商品 id
     */
    public void delete(Long id) {
        productMapper.deleteById(id);
    }

    /**
     * 商品上下架
     *
     * @param id     商品 id
     * @param status 0 下架 / 1 上架
     */
    public void updateStatus(Long id, Integer status) {
        productMapper.updateStatus(id, status);
    }
}
