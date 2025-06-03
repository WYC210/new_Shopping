package com.wyc.service.impl;

import com.wyc.domain.po.Addresses;
import com.wyc.exception.ServiceException;
import com.wyc.mapper.AddressesMapper;
import com.wyc.service.IAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class AddressServiceImpl implements IAddressService {

    @Autowired
    private AddressesMapper addressesMapper;

    @Override
    @Transactional
    public Long addAddress(Long userId, Addresses address) {
        // 1. 设置用户ID
        address.setUserId(userId);
        address.setCreatedAt(new Date());
        address.setUpdatedAt(new Date());

        // 2. 如果是默认地址，将其他地址设置为非默认
        if (address.getIsDefault() == 1) {
            addressesMapper.updateDefaultStatus(userId, 0);
        }

        // 3. 保存地址
        addressesMapper.insert(address);
        return address.getAddressId();
    }

    @Override
    @Transactional
    public void updateAddress(Long userId, Addresses address) {
        // 1. 验证地址是否存在
        Addresses existingAddress = addressesMapper.selectById(address.getAddressId());
        if (existingAddress == null || !existingAddress.getUserId().equals(userId)) {
            throw new ServiceException("地址不存在");
        }

        // 2. 设置更新时间
        address.setUpdatedAt(new Date());

        // 3. 如果设置为默认地址，将其他地址设置为非默认
        if (address.getIsDefault() == 1) {
            addressesMapper.updateDefaultStatus(userId, 0);
        }

        // 4. 更新地址
        addressesMapper.updateById(address);
    }

    @Override
    @Transactional
    public void deleteAddress(Long userId, Long addressId) {
        // 1. 验证地址是否存在
        Addresses address = addressesMapper.selectById(addressId);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new ServiceException("地址不存在");
        }

        // 2. 删除地址
        addressesMapper.deleteById(addressId);
    }

    @Override
    @Transactional
    public void setDefaultAddress(Long userId, Long addressId) {
        // 1. 验证地址是否存在
        Addresses address = addressesMapper.selectById(addressId);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new ServiceException("地址不存在");
        }

        // 2. 将其他地址设置为非默认
        addressesMapper.updateDefaultStatus(userId, 0);

        // 3. 设置当前地址为默认
        address.setIsDefault(1);
        address.setUpdatedAt(new Date());
        addressesMapper.updateById(address);
    }

    @Override
    public Addresses getAddress(Long userId, Long addressId) {
        Addresses address = addressesMapper.selectById(addressId);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new ServiceException("地址不存在");
        }
        return address;
    }

    @Override
    public List<Addresses> getAddressList(Long userId) {
        return addressesMapper.selectByUserId(userId);
    }

    @Override
    public Addresses getDefaultAddress(Long userId) {
        return addressesMapper.selectDefaultByUserId(userId);
    }
}