package com.csh.modules.user.dto;

import com.csh.common.constants.Role;
import com.csh.modules.user.entity.SysUser;
import lombok.Data;

@Data
public class UserVo {
    private Long id;
    private String username;
    private String realName;
    private String studentNo;
    private String phone;
    private String email;
    private Role role;
    private Integer creditScore;
    private Integer status;

    public static UserVo of(SysUser u) {
        if (u == null) return null;
        UserVo vo = new UserVo();
        vo.id = u.getId();
        vo.username = u.getUsername();
        vo.realName = u.getRealName();
        vo.studentNo = u.getStudentNo();
        vo.phone = u.getPhone();
        vo.email = u.getEmail();
        vo.role = u.getRole();
        vo.creditScore = u.getCreditScore();
        vo.status = u.getStatus();
        return vo;
    }
}
