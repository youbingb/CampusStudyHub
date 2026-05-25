package com.csh.modules.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.csh.modules.user.entity.SysUser;
import com.csh.modules.user.mapper.SysUserMapper;
import com.csh.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final SysUserMapper sysUserMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser u = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
        if (u == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }
        return new LoginUser(u.getId(), u.getUsername(), u.getPassword(), u.getRole(),
                u.getCreditScore(), u.getStatus());
    }
}
