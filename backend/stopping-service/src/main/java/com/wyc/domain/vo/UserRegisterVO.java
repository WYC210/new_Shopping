package com.wyc.domain.vo;

import lombok.Data;

@Data
public class UserRegisterVO {
    private String username;
    private String password;
    private String email;
    private String phone;
    private String Uuid;
}
