package com.wyc.controller;

import com.wyc.domain.po.CartItems;
import com.wyc.service.ICartService;
import com.wyc.utils.R;
import com.wyc.security.SecurityUserDetails;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Api(tags = "购物车接口")
@RestController
@RequestMapping("/wyc/cart")
public class CartController {

    private static final Logger logger = LoggerFactory.getLogger(CartController.class);

    @Autowired
    private ICartService cartService;

    @ApiOperation("添加商品到购物车")
    @PostMapping("/items")
    public R<Long> addToCart(
            @AuthenticationPrincipal SecurityUserDetails userDetails,
            @ApiParam(value = "商品ID", required = true) @RequestParam(required = false) Long productId,
            @ApiParam(value = "SKU ID") @RequestParam(required = false) Long skuId,
            @ApiParam(value = "数量", required = true) @RequestParam(required = false) Integer quantity,
            @RequestBody(required = false) Map<String, Object> requestBody) {

        Long userId = userDetails.getUserId();

        // 尝试从请求体中获取参数
        if (productId == null && requestBody != null && requestBody.containsKey("productId")) {
            productId = Long.valueOf(requestBody.get("productId").toString());
        }

        if (skuId == null && requestBody != null && requestBody.containsKey("skuId")) {
            skuId = Long.valueOf(requestBody.get("skuId").toString());
        }

        if (quantity == null && requestBody != null && requestBody.containsKey("quantity")) {
            quantity = Integer.valueOf(requestBody.get("quantity").toString());
        }

        // 记录请求参数
        logger.info("添加商品到购物车: userId={}, productId={}, skuId={}, quantity={}",
                userId, productId, skuId, quantity);

        // 验证必需参数
        if (productId == null) {
            throw new IllegalArgumentException("商品ID不能为空");
        }

        if (quantity == null) {
            throw new IllegalArgumentException("数量不能为空");
        }

        return R.ok(cartService.addToCart(userId, productId, skuId, quantity));
    }

    @ApiOperation("更新购物车商品数量")
    @PutMapping("/items/{cartItemId}/quantity")
    public R<Void> updateQuantity(
            @AuthenticationPrincipal SecurityUserDetails userDetails,
            @ApiParam(value = "购物车项ID", required = true) @PathVariable Long cartItemId,
            @ApiParam(value = "数量", required = true) @RequestParam Integer quantity) {
        Long userId = userDetails.getUserId();
        cartService.updateQuantity(userId, cartItemId, quantity);
        return R.ok();
    }

    @ApiOperation("更新购物车商品选中状态")
    @PutMapping("/items/{cartItemId}/selected")
    public R<Void> updateSelected(
            @AuthenticationPrincipal SecurityUserDetails userDetails,
            @ApiParam(value = "购物车项ID", required = true) @PathVariable Long cartItemId,
            @ApiParam(value = "是否选中（0-未选中，1-已选中）", required = true) @RequestParam Integer isSelected) {
        Long userId = userDetails.getUserId();
        cartService.updateSelected(userId, cartItemId, isSelected);
        return R.ok();
    }

    @ApiOperation("更新所有购物车商品选中状态")
    @PutMapping("/items/selected")
    public R<Void> updateAllSelected(
            @AuthenticationPrincipal SecurityUserDetails userDetails,
            @ApiParam(value = "是否选中（0-未选中，1-已选中）", required = true) @RequestParam Integer isSelected) {
        Long userId = userDetails.getUserId();
        cartService.updateAllSelected(userId, isSelected);
        return R.ok();
    }

    @ApiOperation("删除购物车商品")
    @DeleteMapping("/items/{cartItemId}")
    public R<Void> deleteCartItem(
            @AuthenticationPrincipal SecurityUserDetails userDetails,
            @ApiParam(value = "购物车项ID", required = true) @PathVariable String cartItemId) {

        Long userId = userDetails.getUserId();

        // 处理前端传入的undefined值
        if (cartItemId == null || "undefined".equals(cartItemId)) {
            logger.warn("尝试删除购物车项，但ID为undefined: userId={}", userId);
            return R.error("购物车项ID无效");
        }

        try {
            Long itemId = Long.parseLong(cartItemId);
            cartService.deleteCartItem(userId, itemId);
            return R.ok();
        } catch (NumberFormatException e) {
            logger.error("购物车项ID格式错误: {}", cartItemId, e);
            return R.error("购物车项ID格式错误");
        }
    }

    @ApiOperation("清空购物车")
    @DeleteMapping("/items")
    public R<Void> clearCart(
            @AuthenticationPrincipal SecurityUserDetails userDetails) {
        Long userId = userDetails.getUserId();
        cartService.clearCart(userId);
        return R.ok();
    }

    @ApiOperation("删除选中的购物车商品")
    @DeleteMapping("/items/selected")
    public R<Void> deleteSelected(
            @AuthenticationPrincipal SecurityUserDetails userDetails) {
        Long userId = userDetails.getUserId();
        cartService.deleteSelected(userId);
        return R.ok();
    }

    @ApiOperation("获取购物车列表")
    @GetMapping("/items")
    public R<List<CartItems>> getCartItems(
            @AuthenticationPrincipal SecurityUserDetails userDetails) {
        Long userId = userDetails.getUserId();
        List<CartItems> cartItems = cartService.getCartItems(userId);

        return R.ok(cartItems);
    }

    @ApiOperation("获取选中的购物车商品列表")
    @GetMapping("/items/selected")
    public R<List<CartItems>> getSelectedItems(
            @AuthenticationPrincipal SecurityUserDetails userDetails) {
        Long userId = userDetails.getUserId();
        List<CartItems> selectedItems = cartService.getSelectedItems(userId);

        return R.ok(selectedItems);
    }
}