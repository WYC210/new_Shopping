package com.wyc.controller;

import com.wyc.domain.po.Addresses;
import com.wyc.security.SecurityContext;
import com.wyc.service.IAddressService;
import com.wyc.utils.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "用户地址接口")
@RestController
@RequestMapping("/wyc/addresses")
public class AddressController {

    @Autowired
    private IAddressService addressService;

    @ApiOperation("添加地址")
    @PostMapping
    public R<Long> addAddress(
            @AuthenticationPrincipal UserDetails userDetails,
            @ApiParam(value = "地址信息", required = true) @RequestBody Addresses address) {
        Long userId = SecurityContext.getUserId();
        return R.ok(addressService.addAddress(userId, address));
    }

    @ApiOperation("更新地址")
    @PutMapping("/{addressId}")
    public R<Void> updateAddress(
            @AuthenticationPrincipal UserDetails userDetails,
            @ApiParam(value = "地址ID", required = true) @PathVariable Long addressId,
            @ApiParam(value = "地址信息", required = true) @RequestBody Addresses address) {
        Long userId = SecurityContext.getUserId();
        address.setAddressId(addressId);
        addressService.updateAddress(userId, address);
        return R.ok();
    }

    @ApiOperation("删除地址")
    @DeleteMapping("/{addressId}")
    public R<Void> deleteAddress(
            @AuthenticationPrincipal UserDetails userDetails,
            @ApiParam(value = "地址ID", required = true) @PathVariable Long addressId) {
        Long userId = SecurityContext.getUserId();
        addressService.deleteAddress(userId, addressId);
        return R.ok();
    }

    @ApiOperation("设置默认地址")
    @PutMapping("/{addressId}/default")
    public R<Void> setDefaultAddress(
            @AuthenticationPrincipal UserDetails userDetails,
            @ApiParam(value = "地址ID", required = true) @PathVariable Long addressId) {
        Long userId = SecurityContext.getUserId();
        addressService.setDefaultAddress(userId, addressId);
        return R.ok();
    }

    @ApiOperation("获取地址详情")
    @GetMapping("/{addressId}")
    public R<Addresses> getAddress(
            @AuthenticationPrincipal UserDetails userDetails,
            @ApiParam(value = "地址ID", required = true) @PathVariable Long addressId) {
        Long userId = SecurityContext.getUserId();
        return R.ok(addressService.getAddress(userId, addressId));
    }

    @ApiOperation("获取地址列表")
    @GetMapping
    public R<List<Addresses>> getAddressList(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = SecurityContext.getUserId();
        return R.ok(addressService.getAddressList(userId));
    }

    @ApiOperation("获取默认地址")
    @GetMapping("/default")
    public R<Addresses> getDefaultAddress(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = SecurityContext.getUserId();
        return R.ok(addressService.getDefaultAddress(userId));
    }
}