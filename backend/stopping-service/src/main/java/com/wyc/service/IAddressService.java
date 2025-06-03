package com.wyc.service;

import com.wyc.domain.po.Addresses;
import java.util.List;

public interface IAddressService {
    /**
     * 添加地址
     *
     * @param userId  用户ID
     * @param address 地址信息
     * @return 地址ID
     */
    Long addAddress(Long userId, Addresses address);

    /**
     * 更新地址
     *
     * @param userId  用户ID
     * @param address 地址信息
     */
    void updateAddress(Long userId, Addresses address);

    /**
     * 删除地址
     *
     * @param userId    用户ID
     * @param addressId 地址ID
     */
    void deleteAddress(Long userId, Long addressId);

    /**
     * 设置默认地址
     *
     * @param userId    用户ID
     * @param addressId 地址ID
     */
    void setDefaultAddress(Long userId, Long addressId);

    /**
     * 获取地址详情
     *
     * @param userId    用户ID
     * @param addressId 地址ID
     * @return 地址信息
     */
    Addresses getAddress(Long userId, Long addressId);

    /**
     * 获取用户地址列表
     *
     * @param userId 用户ID
     * @return 地址列表
     */
    List<Addresses> getAddressList(Long userId);

    /**
     * 获取用户默认地址
     *
     * @param userId 用户ID
     * @return 默认地址
     */
    Addresses getDefaultAddress(Long userId);
}