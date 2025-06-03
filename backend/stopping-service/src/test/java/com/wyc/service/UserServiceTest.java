package com.wyc.service;

import com.wyc.domain.dto.LoginDTO;
import com.wyc.domain.po.Users;
import com.wyc.domain.vo.UserRegisterVO;
import com.wyc.mapper.UsersMapper;
import com.wyc.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class UserServiceTest {

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private UsersMapper usersMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private UserRegisterVO registerVO;
    private LoginDTO loginDTO;

    @BeforeEach
    void setUp() {
        // 准备测试数据
        registerVO = new UserRegisterVO();
        registerVO.setUsername("testuser");
        registerVO.setPassword("password123");
        registerVO.setEmail("test@example.com");
        registerVO.setPhone("13800138000");

        loginDTO = new LoginDTO();
        loginDTO.setUsername("testuser");
        loginDTO.setPassword("password123");
    }

    @Test
    void testUserRegistration() {
        // 执行注册
        userService.register(registerVO);

        // 验证用户是否成功注册
        Users registeredUser = usersMapper.selectByUsername(registerVO.getUsername());
        assertNotNull(registeredUser);
        assertEquals(registerVO.getUsername(), registeredUser.getUsername());
        assertEquals(registerVO.getEmail(), registeredUser.getEmail());
        assertEquals(registerVO.getPhone(), registeredUser.getPhone());
        assertTrue(passwordEncoder.matches(registerVO.getPassword(), registeredUser.getPasswordHash()));
    }

    @Test
    void testUserLogin() {
        // 先注册用户
        userService.register(registerVO);

        // 执行登录
        String token = userService.login(loginDTO);

        // 验证登录结果
        assertNotNull(token);
    }

    @Test
    void testUserDeletion() {
        // 先注册用户
        userService.register(registerVO);

        // 获取用户ID
        Users user = usersMapper.selectByUsername(registerVO.getUsername());
        assertNotNull(user);

        // 执行软删除
        usersMapper.softDelete(user.getUserId());

        // 验证用户是否被标记为已删除 - 使用selectByIdIncludeDeleted
        Users deletedUser = usersMapper.selectByIdIncludeDeleted(user.getUserId());
        assertNotNull(deletedUser);
        assertTrue(deletedUser.getIsDeleted());
    }

    @Test
    void testDuplicateRegistration() {
        // 先注册用户
        userService.register(registerVO);

        // 尝试使用相同的用户名再次注册
        assertThrows(RuntimeException.class, () -> {
            userService.register(registerVO);
        });
    }

    @Test
    void testInvalidLogin() {
        // 先注册用户
        userService.register(registerVO);

        // 使用错误的密码尝试登录
        loginDTO.setPassword("wrongpassword");
        assertThrows(Exception.class, () -> {
            userService.login(loginDTO);
        });
    }
}
