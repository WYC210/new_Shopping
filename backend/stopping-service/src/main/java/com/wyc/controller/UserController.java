package com.wyc.controller;

import com.wyc.domain.dto.LoginDTO;
import com.wyc.domain.vo.BalanceUpdateVO;
import com.wyc.domain.vo.BrowsingRecordVO;
import com.wyc.domain.vo.PasswordUpdateVO;
import com.wyc.domain.vo.UserCouponVO;
import com.wyc.domain.vo.UserDetailsVO;
import com.wyc.domain.vo.UserProfileUpdateVO;
import com.wyc.domain.vo.UserRegisterVO;
import com.wyc.security.SecurityContext;
import com.wyc.service.IUserService;
import com.wyc.utils.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Api(tags = "用户相关接口")
@RestController
@RequestMapping("/wyc/users")
public class UserController {

    @Autowired
    private IUserService userService;

    @Value("${jwt.tokenHeader}")
    private String tokenHeader;

    @Value("${jwt.tokenPrefix}")
    private String tokenPrefix;

    @CircuitBreaker(name = "userService", fallbackMethod = "fallbackRegister")
    @RateLimiter(name = "userService", fallbackMethod = "fallbackRegister")
    @Bulkhead(name = "userService", fallbackMethod = "fallbackRegister")
    @PostMapping("/register")
    public R<?> register(@RequestBody UserRegisterVO registerVO) {
        userService.register(registerVO);
        return R.ok("注册成功");
    }

    public R<?> fallbackRegister(UserRegisterVO registerVO, Throwable t) {
        return R.error(503, "服务暂时不可用，请稍后重试");
    }

    @CircuitBreaker(name = "userService", fallbackMethod = "fallbackLogin")
    @RateLimiter(name = "userService", fallbackMethod = "fallbackLogin")
    @Bulkhead(name = "userService", fallbackMethod = "fallbackLogin")
    @PostMapping("/login")
    public R<?> login(@RequestBody LoginDTO loginDTO, HttpServletRequest request) {
        // 从请求头获取浏览器指纹
        String fingerprint = request.getHeader("X-Fingerprint");
        if (fingerprint != null && !fingerprint.isEmpty()) {
            loginDTO.setFingerprint(fingerprint);
        }
        String token = userService.login(loginDTO);
        return R.ok(token);
    }

    public R<?> fallbackLogin(LoginDTO loginDTO, HttpServletRequest request, Throwable t) {
        return R.error(503, "服务暂时不可用，请稍后重试");
    }

    @CircuitBreaker(name = "userService", fallbackMethod = "fallbackLogout")
    @RateLimiter(name = "userService", fallbackMethod = "fallbackLogout")
    @Bulkhead(name = "userService", fallbackMethod = "fallbackLogout")
    @PostMapping("/logout")
    public R<?> logout(HttpServletRequest request) {
        userService.logout(request);
        return R.ok("登出成功");
    }

    public R<?> fallbackLogout(HttpServletRequest request, Throwable t) {
        return R.error(503, "服务暂时不可用，请稍后重试");
    }

    @CircuitBreaker(name = "userService", fallbackMethod = "fallbackNoParam")
    @RateLimiter(name = "userService", fallbackMethod = "fallbackNoParam")
    @Bulkhead(name = "userService", fallbackMethod = "fallbackNoParam")
    @GetMapping("/details")
    public R getUserDetails() {
        Long userId = SecurityContext.getUserId();
        System.out.println(userId);
        return userService.getUserDetails(userId);
    }

    public R fallbackNoParam(Throwable t) {
        return R.error(503, "服务暂时不可用，请稍后重试");
    }

    @CircuitBreaker(name = "userService", fallbackMethod = "fallbackUpdateBalance")
    @RateLimiter(name = "userService", fallbackMethod = "fallbackUpdateBalance")
    @Bulkhead(name = "userService", fallbackMethod = "fallbackUpdateBalance")
    @PostMapping("/balance")
    public R<?> updateBalance(@RequestBody BalanceUpdateVO updateVO) {
        Long userId = SecurityContext.getUserId();
        userService.updateBalance(userId, updateVO.getAmount());
        return R.ok("余额更新成功");
    }

    public R<?> fallbackUpdateBalance(BalanceUpdateVO updateVO, Throwable t) {
        return R.error(503, "服务暂时不可用，请稍后重试");
    }

    @CircuitBreaker(name = "userService", fallbackMethod = "fallbackNoParam")
    @RateLimiter(name = "userService", fallbackMethod = "fallbackNoParam")
    @Bulkhead(name = "userService", fallbackMethod = "fallbackNoParam")
    @GetMapping("/balance")
    public R<UserDetailsVO> getBalance() {
        Long userId = SecurityContext.getUserId();
        return userService.getUserDetails(userId);
    }

    @CircuitBreaker(name = "userService", fallbackMethod = "fallbackUpdatePassword")
    @RateLimiter(name = "userService", fallbackMethod = "fallbackUpdatePassword")
    @Bulkhead(name = "userService", fallbackMethod = "fallbackUpdatePassword")
    @PostMapping("/password")
    public R<?> updatePassword(@RequestBody PasswordUpdateVO passwordUpdateVO) {
        Long userId = SecurityContext.getUserId();
        userService.updatePassword(userId, passwordUpdateVO);
        return R.ok("密码修改成功");
    }

    public R<?> fallbackUpdatePassword(PasswordUpdateVO passwordUpdateVO, Throwable t) {
        return R.error(503, "服务暂时不可用，请稍后重试");
    }

    @CircuitBreaker(name = "userService", fallbackMethod = "fallbackUpdateProfile")
    @RateLimiter(name = "userService", fallbackMethod = "fallbackUpdateProfile")
    @Bulkhead(name = "userService", fallbackMethod = "fallbackUpdateProfile")
    @PutMapping("/profile")
    public R<?> updateProfile(@RequestBody UserProfileUpdateVO profileUpdateVO) {
        Long userId = SecurityContext.getUserId();
        userService.updateProfile(userId, profileUpdateVO);
        return R.ok("个人信息更新成功");
    }

    public R<?> fallbackUpdateProfile(UserProfileUpdateVO profileUpdateVO, Throwable t) {
        return R.error(503, "服务暂时不可用，请稍后重试");
    }

    @CircuitBreaker(name = "userService", fallbackMethod = "fallbackGetUserCoupons")
    @RateLimiter(name = "userService", fallbackMethod = "fallbackGetUserCoupons")
    @Bulkhead(name = "userService", fallbackMethod = "fallbackGetUserCoupons")
    @GetMapping("/coupons")
    public R<List<UserCouponVO>> getUserCoupons(@RequestParam(required = false) String status) {
        Long userId = SecurityContext.getUserId();
        List<UserCouponVO> coupons = userService.getUserCoupons(userId, status);
        return R.ok(coupons);
    }

    public R<List<UserCouponVO>> fallbackGetUserCoupons(String status, Throwable t) {
        return R.error(503, "服务暂时不可用，请稍后重试");
    }

    @CircuitBreaker(name = "userService", fallbackMethod = "fallbackClaimCoupon")
    @RateLimiter(name = "userService", fallbackMethod = "fallbackClaimCoupon")
    @Bulkhead(name = "userService", fallbackMethod = "fallbackClaimCoupon")
    @PostMapping("/coupons/{couponId}")
    public R<?> claimCoupon(@PathVariable Long couponId) {
        Long userId = SecurityContext.getUserId();
        userService.claimCoupon(userId, couponId);
        return R.ok("优惠券领取成功");
    }

    public R<?> fallbackClaimCoupon(Long couponId, Throwable t) {
        return R.error(503, "服务暂时不可用，请稍后重试");
    }

    @CircuitBreaker(name = "userService", fallbackMethod = "fallbackNoParam")
    @RateLimiter(name = "userService", fallbackMethod = "fallbackNoParam")
    @Bulkhead(name = "userService", fallbackMethod = "fallbackNoParam")
    @GetMapping("/browsing-history")
    public R<List<BrowsingRecordVO>> getBrowsingHistory() {
        Long userId = SecurityContext.getUserId();
        List<BrowsingRecordVO> history = userService.getBrowsingHistory(userId);
        return R.ok(history);
    }

    @CircuitBreaker(name = "userService", fallbackMethod = "fallbackRecordBrowsing")
    @RateLimiter(name = "userService", fallbackMethod = "fallbackRecordBrowsing")
    @Bulkhead(name = "userService", fallbackMethod = "fallbackRecordBrowsing")
    @PostMapping("/browsing-history")
    public R<?> recordBrowsing(@RequestBody BrowsingRecordVO record, HttpServletRequest request) {
        String fingerprint = request.getHeader("X-Fingerprint");
        if (fingerprint == null || fingerprint.isEmpty()) {
            return R.error("X-Fingerprint请求头不能为空");
        }
        if (record.getProductId() == null || record.getProductName() == null) {
            return R.error("商品ID和商品名称不能为空");
        }
        userService.recordBrowsing(fingerprint, record.getProductId(), record.getProductName());
        return R.ok();
    }

    public R<?> fallbackRecordBrowsing(BrowsingRecordVO record, HttpServletRequest request, Throwable t) {
        return R.error(503, "服务暂时不可用，请稍后重试");
    }
}
