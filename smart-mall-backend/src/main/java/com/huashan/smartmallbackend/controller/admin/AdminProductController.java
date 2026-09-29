package com.huashan.smartmallbackend.controller.admin;

import com.github.pagehelper.PageInfo;
import com.huashan.smartmallbackend.common.R;
import com.huashan.smartmallbackend.entity.Product;
import com.huashan.smartmallbackend.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端 - 商品管理
 * <p>
 * 新增 / 修改统一走 POST /admin/product（id 为空是新增，否则是修改），
 * 请求体和响应体都直接用 Product 实体。
 *
 * @author hs
 */
@RestController
@RequestMapping("/admin/product")
public class AdminProductController {

    @Autowired
    private ProductService productService;

    /**
     * 商品列表（分页）
     *
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @param keyword  名称/品牌关键词（可空）
     * @param category 类别（可空）
     * @param status   状态（可空）
     * @return 分页结果
     */
    @GetMapping("/page")
    public R<PageInfo<Product>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                     @RequestParam(defaultValue = "10") Integer pageSize,
                                     @RequestParam(required = false) String keyword,
                                     @RequestParam(required = false) String category,
                                     @RequestParam(required = false) Integer status) {
        return R.ok(productService.page(pageNum, pageSize, keyword, category, status));
    }

    /**
     * 新增或修改商品
     *
     * @param product 商品实体（id 为空=新增，非空=修改）
     * @return 保存后的商品
     */
    @PostMapping
    public R<Product> saveOrUpdate(@RequestBody Product product) {
        return R.ok("保存成功", productService.saveOrUpdate(product));
    }

    /**
     * 删除商品（逻辑删除）
     *
     * @param id 商品 id
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return R.ok("删除成功", null);
    }

    /**
     * 商品上下架
     *
     * @param id     商品 id
     * @param status 0 下架 / 1 上架
     * @return 操作结果
     */
    @PutMapping("/{id}/status")
    public R<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        productService.updateStatus(id, status);
        return R.ok("操作成功", null);
    }
}
