package com.wyc.security;

import com.wyc.domain.po.Users;
import com.wyc.mapper.UsersMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MyUserDetailsService implements UserDetailsService {

    @Autowired
    private UsersMapper usersMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("开始加载用户信息，用户名/邮箱/手机号: {}", username);

        // 尝试通过用户名/邮箱/手机号查询
        Users user = usersMapper.selectByUsernameOrEmailOrPhone(username);
        if (user == null) {
            log.error("用户不存在: {}", username);
            throw new UsernameNotFoundException("用户不存在: " + username);
        }

        log.info("找到用户: userId={}, username={}, isDeleted={}",
                user.getUserId(), user.getUsername(), user.getIsDeleted());
        log.info("用户密码hash: {}", user.getPasswordHash());

        if (Boolean.TRUE.equals(user.getIsDeleted())) {
            log.error("用户已被删除: {}", username);
            throw new BadCredentialsException("用户已被删除");
        }

        return new SecurityUserDetails(user);
    }

    /**
     * 通过用户ID加载用户信息
     *
     * @param userId 用户ID
     * @return UserDetails
     * @throws UsernameNotFoundException 用户不存在时抛出
     */
    public UserDetails loadUserById(Long userId) throws UsernameNotFoundException {
        log.info("开始通过ID加载用户信息: {}", userId);

        Users user = usersMapper.selectById(userId);
        if (user == null) {
            log.error("用户不存在: {}", userId);
            throw new UsernameNotFoundException("用户不存在: " + userId);
        }

        log.info("找到用户: userId={}, username={}, isDeleted={}",
                user.getUserId(), user.getUsername(), user.getIsDeleted());

        if (Boolean.TRUE.equals(user.getIsDeleted())) {
            log.error("用户已被删除: {}", userId);
            throw new BadCredentialsException("用户已被删除");
        }

        return new SecurityUserDetails(user);
    }
}