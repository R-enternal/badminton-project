package com.badminton.service;

import com.badminton.dto.ProductCategoryDTO;
import com.badminton.vo.ProductCategoryVO;

import java.util.List;

/**
 * 商品分类服务接口
 */
public interface ProductCategoryService {

    Long createCategory(ProductCategoryDTO dto);

    void updateCategory(ProductCategoryDTO dto);

    void deleteCategory(Long id);

    List<ProductCategoryVO> listAllCategories();

    List<ProductCategoryVO> listOpenCategories();
}
