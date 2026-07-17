package com.badminton.controller;

import com.badminton.common.Result;
import com.badminton.annotation.RequireRole;
import com.badminton.dto.CartItemDTO;
import com.badminton.dto.OrderCreateDTO;
import com.badminton.dto.ProductCategoryDTO;
import com.badminton.dto.ProductSkuDTO;
import com.badminton.dto.ProductSpuDTO;
import com.badminton.service.CartService;
import com.badminton.service.ProductCategoryService;
import com.badminton.service.ProductService;
import com.badminton.service.ShopOrderService;
import com.badminton.util.UserContext;
import com.badminton.vo.CartItemVO;
import com.badminton.vo.ProductCategoryVO;
import com.badminton.vo.ProductDetailVO;
import com.badminton.vo.ProductSkuVO;
import com.badminton.vo.ProductSpuVO;
import com.badminton.vo.ShopOrderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 商品商城模块接口
 */
@Tag(name = "商品商城模块", description = "商品分类、商品、购物车、订单相关接口")
@RestController
@RequestMapping("/api/shop")
@RequiredArgsConstructor
public class ShopController {

    private final ProductCategoryService productCategoryService;
    private final ProductService productService;
    private final CartService cartService;
    private final ShopOrderService shopOrderService;

    // ==================== 分类管理 ====================

    @Operation(summary = "新增分类")
    @RequireRole("ADMIN")
    @PostMapping("/categories")
    public Result<Long> createCategory(@Valid @RequestBody ProductCategoryDTO dto) {
        return Result.success(productCategoryService.createCategory(dto));
    }

    @Operation(summary = "修改分类")
    @RequireRole("ADMIN")
    @PutMapping("/categories/{id}")
    public Result<Void> updateCategory(@PathVariable Long id, @Valid @RequestBody ProductCategoryDTO dto) {
        dto.setId(id);
        productCategoryService.updateCategory(dto);
        return Result.success();
    }

    @Operation(summary = "删除分类")
    @RequireRole("ADMIN")
    @DeleteMapping("/categories/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        productCategoryService.deleteCategory(id);
        return Result.success();
    }

    @Operation(summary = "分类列表")
    @GetMapping("/categories")
    public Result<List<ProductCategoryVO>> listCategories() {
        return Result.success(productCategoryService.listOpenCategories());
    }

    // ==================== 商品管理 ====================

    @Operation(summary = "新增SPU")
    @RequireRole("ADMIN")
    @PostMapping("/products")
    public Result<Long> createSpu(@Valid @RequestBody ProductSpuDTO dto) {
        return Result.success(productService.createSpu(dto));
    }

    @Operation(summary = "修改SPU")
    @RequireRole("ADMIN")
    @PutMapping("/products/{id}")
    public Result<Void> updateSpu(@PathVariable Long id, @Valid @RequestBody ProductSpuDTO dto) {
        dto.setId(id);
        productService.updateSpu(dto);
        return Result.success();
    }

    @Operation(summary = "删除SPU")
    @RequireRole("ADMIN")
    @DeleteMapping("/products/{id}")
    public Result<Void> deleteSpu(@PathVariable Long id) {
        productService.deleteSpu(id);
        return Result.success();
    }

    @Operation(summary = "商品列表")
    @GetMapping("/products")
    public Result<List<ProductSpuVO>> listSpus(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        return Result.success(productService.listSpus(categoryId, keyword, status));
    }

    @Operation(summary = "商品详情")
    @GetMapping("/detail/{id}")
    public Result<ProductDetailVO> getProductDetail(@PathVariable Long id) {
        return Result.success(productService.getProductDetail(id));
    }

    @Operation(summary = "新增SKU")
    @RequireRole("ADMIN")
    @PostMapping("/skus")
    public Result<Long> createSku(@Valid @RequestBody ProductSkuDTO dto) {
        return Result.success(productService.createSku(dto));
    }

    @Operation(summary = "修改SKU")
    @RequireRole("ADMIN")
    @PutMapping("/skus/{id}")
    public Result<Void> updateSku(@PathVariable Long id, @Valid @RequestBody ProductSkuDTO dto) {
        dto.setId(id);
        productService.updateSku(dto);
        return Result.success();
    }

    @Operation(summary = "删除SKU")
    @RequireRole("ADMIN")
    @DeleteMapping("/skus/{id}")
    public Result<Void> deleteSku(@PathVariable Long id) {
        productService.deleteSku(id);
        return Result.success();
    }

    @Operation(summary = "SKU列表")
    @GetMapping("/products/{spuId}/skus")
    public Result<List<ProductSkuVO>> listSkus(@PathVariable Long spuId) {
        return Result.success(productService.listSkusBySpuId(spuId));
    }

    // ==================== 购物车 ====================

    @Operation(summary = "加入购物车")
    @PostMapping("/cart/add")
    public Result<CartItemVO> addCart(@Valid @RequestBody CartItemDTO dto) {
        return Result.success(cartService.addToCart(UserContext.getUserId(), dto));
    }

    @Operation(summary = "修改购物车数量")
    @PutMapping("/cart/{id}")
    public Result<Void> updateCartQuantity(@PathVariable Long id, @RequestParam Integer quantity) {
        cartService.updateQuantity(UserContext.getUserId(), id, quantity);
        return Result.success();
    }

    @Operation(summary = "删除购物车项")
    @DeleteMapping("/cart/{id}")
    public Result<Void> deleteCart(@PathVariable Long id) {
        cartService.deleteCartItem(UserContext.getUserId(), id);
        return Result.success();
    }

    @Operation(summary = "选中/取消选中")
    @PostMapping("/cart/select/{id}")
    public Result<Void> selectCart(@PathVariable Long id, @RequestParam Integer selected) {
        cartService.selectCartItem(UserContext.getUserId(), id, selected);
        return Result.success();
    }

    @Operation(summary = "购物车列表")
    @GetMapping("/cart/list")
    public Result<List<CartItemVO>> listCart() {
        return Result.success(cartService.listCartItems(UserContext.getUserId()));
    }

    // ==================== 订单 ====================

    @Operation(summary = "创建订单")
    @PostMapping("/order/create")
    public Result<ShopOrderVO> createOrder(@Valid @RequestBody OrderCreateDTO dto) {
        return Result.success(shopOrderService.createOrder(UserContext.getUserId(), dto));
    }

    @Operation(summary = "取消订单")
    @PostMapping("/order/cancel/{id}")
    public Result<Void> cancelOrder(@PathVariable Long id) {
        shopOrderService.cancelOrder(UserContext.getUserId(), id);
        return Result.success();
    }

    @Operation(summary = "确认收货")
    @PostMapping("/order/receive/{id}")
    public Result<Void> receiveOrder(@PathVariable Long id) {
        shopOrderService.receiveOrder(UserContext.getUserId(), id);
        return Result.success();
    }

    @Operation(summary = "我的订单")
    @GetMapping("/order/my")
    public Result<List<ShopOrderVO>> listMyOrders(@RequestParam(required = false) Integer status) {
        return Result.success(shopOrderService.listMyOrders(UserContext.getUserId(), status));
    }

    @Operation(summary = "订单详情")
    @GetMapping("/order/{id}")
    public Result<ShopOrderVO> getOrderDetail(@PathVariable Long id) {
        return Result.success(shopOrderService.getOrderDetail(UserContext.getUserId(), id));
    }

    @Operation(summary = "发货", description = "管理后台使用")
    @RequireRole("ADMIN")
    @PostMapping("/order/ship/{id}")
    public Result<Void> shipOrder(@PathVariable Long id) {
        shopOrderService.shipOrder(id, "");
        return Result.success();
    }

    @Operation(summary = "全部订单", description = "管理后台使用")
    @RequireRole("ADMIN")
    @GetMapping("/orders")
    public Result<List<ShopOrderVO>> listAllOrders(@RequestParam(required = false) Integer status) {
        return Result.success(shopOrderService.listAllOrders(status));
    }
}
