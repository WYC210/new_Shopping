package com.wyc.domain.po;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

/**
 * 用户实体类
 */
@Data
public class Users {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码哈希
     */
    private String passwordHash;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 余额
     */
    private BigDecimal balance;

    /**
     * 余额更新时间
     */
    private Date balanceUpdatedAt;

    /**
     * 是否删除
     */
    private Boolean isDeleted;

    /**
     * UUID
     */
    private String uuid;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;
}
