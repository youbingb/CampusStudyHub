package com.csh.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class LoginUserHolder {

    public static LoginUser current() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof LoginUser u)) {
            return null;
        }
        return u;
    }

    public static Long currentId() {
        LoginUser u = current();
        return u == null ? null : u.getId();
    }

    public static LoginUser requireCurrent() {
        LoginUser u = current();
        if (u == null) throw new com.csh.common.BusinessException(401, "未登录");
        return u;
    }
}
