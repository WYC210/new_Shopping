package com.wyc.domain.po;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

/**
 * 用户表对象 users
 *
 * @author wyc
 * @date 2025-05-30
 */
@Data
public class Users {
  

    /** 用户ID */
    private Long userId;

    /** 用户名 */
    private String username;

    /** 用户邮箱 */
    private String email;

    /** 用户手机号 */
    private String phone;

    /** 用户UUID */
    private String Uuid;

    /** 密码哈希值 */
    private String passwordHash;

    /** 账户金额 */
    private BigDecimal balance;

    /** 余额更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date balanceUpdatedAt;

    /** 是否已注销 */
    private Boolean isDeleted;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createdAt;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updatedAt;

}
