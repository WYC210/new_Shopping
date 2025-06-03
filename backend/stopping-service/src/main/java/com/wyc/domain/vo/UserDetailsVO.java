package com.wyc.domain.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class UserDetailsVO {
    private Long userId;
    private String username;
    private String email;
    private String phone;
    private BigDecimal balance;
    private Date balanceUpdatedAt;
    private Date createdAt;
    private Date updatedAt;
}