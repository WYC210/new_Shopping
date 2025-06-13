package com.wyc.controller;

import com.wyc.security.SecurityUserDetails;
import com.wyc.service.ISignInService;
import com.wyc.utils.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(tags = "用户签到接口")
@RestController
@RequestMapping("/wyc/sign")
public class SignInController {

    @Autowired
    private ISignInService signInService;

    @ApiOperation("用户签到")
    @PostMapping
    @CircuitBreaker(name = "signIn", fallbackMethod = "signInFallback")
    public R<Map<String, Object>> signIn(@AuthenticationPrincipal SecurityUserDetails userDetails) {
        Long userId = userDetails.getUserId();

        // 检查今天是否已签到
        if (signInService.isSignedToday(userId)) {
            return R.error("今天已经签到过了");
        }

        // 执行签到
        int continuousCount = signInService.signIn(userId);

        // 返回签到结果
        Map<String, Object> result = new HashMap<>();
        result.put("continuousCount", continuousCount);
        result.put("monthCount", signInService.getMonthSignCount(userId));

        return R.ok(result);
    }

    @ApiOperation("检查今天是否已签到")
    @GetMapping("/check")
    @CircuitBreaker(name = "checkSignIn", fallbackMethod = "checkSignInFallback")
    public R<Boolean> checkSignIn(@AuthenticationPrincipal SecurityUserDetails userDetails) {
        Long userId = userDetails.getUserId();
        return R.ok(signInService.isSignedToday(userId));
    }

    @ApiOperation("获取当月签到记录")
    @GetMapping("/month")
    @CircuitBreaker(name = "getMonthSignRecord", fallbackMethod = "getMonthSignRecordFallback")
    public R<Map<Integer, Boolean>> getMonthSignRecord(@AuthenticationPrincipal SecurityUserDetails userDetails) {
        Long userId = userDetails.getUserId();
        return R.ok(signInService.getMonthSignRecord(userId));
    }

    @ApiOperation("获取签到统计信息")
    @GetMapping("/stats")
    @CircuitBreaker(name = "getSignStats", fallbackMethod = "getSignStatsFallback")
    public R<Map<String, Object>> getSignStats(@AuthenticationPrincipal SecurityUserDetails userDetails) {
        Long userId = userDetails.getUserId();

        Map<String, Object> stats = new HashMap<>();
        stats.put("monthCount", signInService.getMonthSignCount(userId));
        stats.put("continuousCount", signInService.getContinuousSignCount(userId));
        stats.put("todaySigned", signInService.isSignedToday(userId));

        return R.ok(stats);
    }

    @ApiOperation("获取签到排行榜")
    @GetMapping("/rank")
    @CircuitBreaker(name = "getSignInRank", fallbackMethod = "getSignInRankFallback")
    public R<List<Map<String, Object>>> getSignInRank(
            @ApiParam(value = "获取前几名", defaultValue = "10") @RequestParam(defaultValue = "10") int limit) {
        return R.ok(signInService.getSignInRank(limit));
    }

    public R<Map<String, Object>> signInFallback(SecurityUserDetails userDetails, Throwable t) {
        return R.error(503, "服务暂时不可用，请稍后重试");
    }

    public R<Boolean> checkSignInFallback(SecurityUserDetails userDetails, Throwable t) {
        return R.error(503, "服务暂时不可用，请稍后重试");
    }

    public R<Map<Integer, Boolean>> getMonthSignRecordFallback(SecurityUserDetails userDetails, Throwable t) {
        return R.error(503, "服务暂时不可用，请稍后重试");
    }

    public R<Map<String, Object>> getSignStatsFallback(SecurityUserDetails userDetails, Throwable t) {
        return R.error(503, "服务暂时不可用，请稍后重试");
    }

    public R<List<Map<String, Object>>> getSignInRankFallback(int limit, Throwable t) {
        return R.error(503, "服务暂时不可用，请稍后重试");
    }
}