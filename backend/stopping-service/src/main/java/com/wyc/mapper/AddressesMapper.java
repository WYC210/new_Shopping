package com.wyc.mapper;

import com.wyc.domain.po.Addresses;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AddressesMapper {
    /**
     * 插入地址
     */
    int insert(Addresses address);

    /**
     * 根据ID查询地址
     */
    Addresses selectById(@Param("addressId") Long addressId);

    /**
     * 根据用户ID查询地址列表
     */
    @Select("SELECT * FROM useraddresses WHERE user_id = #{userId}")
    List<Addresses> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID查询默认地址
     */
    @Select("SELECT * FROM useraddresses WHERE user_id = #{userId} AND is_default = 1 LIMIT 1")
    Addresses selectDefaultByUserId(@Param("userId") Long userId);

    /**
     * 更新地址
     */
    int updateById(Addresses address);

    /**
     * 删除地址
     */
    int deleteById(@Param("addressId") Long addressId);

    /**
     * 设置默认地址
     */
    int setDefaultAddress(@Param("addressId") Long addressId, @Param("userId") Long userId);

    /**
     * 取消默认地址
     */
    int unsetDefaultAddress(@Param("userId") Long userId);

    /**
     * 更新用户所有地址的默认状态
     */
    int updateDefaultStatus(@Param("userId") Long userId, @Param("isDefault") Integer isDefault);
}