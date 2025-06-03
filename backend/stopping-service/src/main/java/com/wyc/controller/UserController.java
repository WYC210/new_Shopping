package com.wyc.controller;

import com.wyc.domain.dto.LoginDTO;
import com.wyc.domain.vo.BalanceUpdateVO;
import com.wyc.domain.vo.BrowsingRecordVO;
import com.wyc.domain.vo.PasswordUpdateVO;
import com.wyc.domain.vo.UserCouponVO;
import com.wyc.domain.vo.UserDetailsVO;
import com.wyc.domain.vo.UserProfileUpdateVO;
import com.wyc.domain.vo.UserRegisterVO;
import com.wyc.exception.ServiceException;
import com.wyc.security.SecurityContext;
import com.wyc.service.IUserService;
import com.wyc.utils.JwtTokenUtil;
import com.wyc.utils.R;
import com.wyc.utils.RedisCache;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Api(tags = "用户相关接口")
@RestController
@RequestMapping("/wyc/users")
public class UserController {

    @Autowired
    private IUserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private RedisCache redisCache;

    @Value("${jwt.tokenHeader}")
    private String tokenHeader;

    @Value("${jwt.tokenPrefix}")
    private String tokenPrefix;

    @ApiOperation("用户注册")
    @PostMapping("/register")
    public R<?> register(@RequestBody UserRegisterVO registerVO) {
        userService.register(registerVO);
        return R.ok("注册成功");
    }

    @ApiOperation("用户登录")
    @PostMapping("/login")
    public R<?> login(@RequestBody LoginDTO loginDTO) {
        String token = userService.login(loginDTO);
        return R.ok(token);
    }

    @ApiOperation("用户登出")
    @PostMapping("/logout")
    public R<?> logout(HttpServletRequest request) {
        userService.logout(request);
        return R.ok("登出成功");
    }

    @ApiOperation("获取用户详情")
    @GetMapping("/details")
    public R getUserDetails() {
        Long userId = SecurityContext.getUserId();
        System.out.println(userId);
        return userService.getUserDetails(userId);
    }

    @ApiOperation("更新用户余额")
    @PostMapping("/balance")
    public R<?> updateBalance(@RequestBody BalanceUpdateVO updateVO) {
        Long userId = SecurityContext.getUserId();
        userService.updateBalance(userId, updateVO.getAmount());
        return R.ok("余额更新成功");
    }

    @ApiOperation("查询用户余额")
    @GetMapping("/balance")
    public R<UserDetailsVO> getBalance() {
        Long userId = SecurityContext.getUserId();
        return userService.getUserDetails(userId);
    }

    @ApiOperation("修改密码")
    @PostMapping("/password")
    public R<?> updatePassword(@RequestBody PasswordUpdateVO passwordUpdateVO) {
        Long userId = SecurityContext.getUserId();
        userService.updatePassword(userId, passwordUpdateVO);
        return R.ok("密码修改成功");
    }

    @ApiOperation("修改个人信息")
    @PutMapping("/profile")
    public R<?> updateProfile(@RequestBody UserProfileUpdateVO profileUpdateVO) {
        Long userId = SecurityContext.getUserId();
        userService.updateProfile(userId, profileUpdateVO);
        return R.ok("个人信息更新成功");
    }

    @ApiOperation("获取用户优惠券列表")
    @GetMapping("/coupons")
    public R<List<UserCouponVO>> getUserCoupons(
            @ApiParam(value = "优惠券状态", required = false) @RequestParam(required = false) String status) {
        Long userId = SecurityContext.getUserId();
        List<UserCouponVO> coupons = userService.getUserCoupons(userId, status);
        return R.ok(coupons);
    }

    @ApiOperation("获取浏览记录")
    @GetMapping("/browsing-history")
    public R<List<BrowsingRecordVO>> getBrowsingHistory() {
        Long userId = SecurityContext.getUserId();
        List<BrowsingRecordVO> history = userService.getBrowsingHistory(userId);
        return R.ok(history);
    }

    @ApiOperation("记录浏览历史")
    @PostMapping("/browsing-history")
    public R<?> recordBrowsing(
            @ApiParam(value = "浏览器指纹", required = true) @RequestParam String fingerprint,
            @ApiParam(value = "商品ID", required = true) @RequestParam Long productId,
            @ApiParam(value = "商品名称", required = true) @RequestParam String productName) {
        userService.recordBrowsing(fingerprint, productId, productName);
        return R.ok();
    }
}
