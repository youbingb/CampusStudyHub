package com.csh.modules.user.dto;

import com.csh.common.constants.Role;
import lombok.Data;

@Data
public class UserQuery {
    private Integer page = 1;
    private Integer size = 10;
    private String keyword;
    private Role role;
    private Integer status;
}
