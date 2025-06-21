package com.wyc.mapper;

import com.wyc.domain.po.Users;
import com.wyc.domain.vo.UserProfileUpdateVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import org.springframework.stereotype.Repository;

/**
 * 用户Mapper接口
 */
@Mapper
@Repository
public interface UsersMapper {
    /**
     * 根据用户名查询用户
     */
    Users selectByUsername(@Param("username") String username);

    /**
     * 根据用户ID查询用户
     */
    Users selectById(@Param("userId") Long userId);

    /**
     * 根据用户ID查询用户（包含已删除用户）
     */
    Users selectByIdIncludeDeleted(@Param("userId") Long userId);

    Users selectByUuid(@Param("Uuid") String Uuid);

    /**
     * 根据邮箱查询用户
     */
    Users selectByEmail(@Param("email") String email);

    /**
     * 根据手机号查询用户
     */
    Users selectByPhone(@Param("phone") String phone);

    /**
     * 查询所有未删除的用户
     */
    List<Users> selectActiveUsers();

    /**
     * 插入新用户
     */
    int insert(Users user);

    /**
     * 更新用户信息
     */
    int updateUser(Users user);

    /**
     * 软删除用户
     */
    int softDelete(@Param("userId") Long userId);

    /**
     * 根据用户ID获取密码
     */
    String getPassword(@Param("userId") Long userId);

    /**
     * 更新用户密码
     */
    int updatePassword(@Param("userId") Long userId, @Param("password") String password);

    /**
     * 更新用户资料
     */
    int updateProfile(@Param("userId") Long userId, @Param("profile") UserProfileUpdateVO profile);

    /**
     * 更新用户余额
     *
     * @param userId 用户ID
     * @param amount 变动金额（正数增加，负数减少）
     */
    int updateBalance(@Param("userId") Long userId, @Param("amount") Double amount);

    Users selectByUsernameOrEmailOrPhone(@Param("login") String login);
}
