package com.csh.modules.user.dto;

import lombok.Data;

@Data
public class LoginResp {
    private String token;
    private UserVo user;
}
