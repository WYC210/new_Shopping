package com.wyc.controller;

import com.wyc.domain.common.Result;
import com.wyc.service.IVisitorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 访客控制器
 * 处理未登录用户的浏览记录
 *
 * @author wyc
 */
@Slf4j
@RestController
@RequestMapping("/wyc/visitor")
public class VisitorController {

    @Autowired
    private IVisitorService visitorService;

    /**
     * 获取访客浏览历史
     *
     * @param request HTTP请求
     * @param limit   限制数量，默认10
     * @return 浏览历史列表
     */
    @GetMapping("/browsing/history")
    public Result<List<Object>> getBrowsingHistory(
            HttpServletRequest request,
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        String fingerprint = request.getHeader("X-Fingerprint");

        if (fingerprint == null || fingerprint.isEmpty()) {
            return Result.error("X-Fingerprint请求头不能为空");
        }

        List<Object> history = visitorService.getVisitorBrowsingHistory(fingerprint, limit);
        return Result.success(history);
    }

    /**
     * 将访客浏览记录关联到用户
     *
     * @param params  参数，包含userId
     * @param request HTTP请求
     * @return 操作结果
     */
    @PostMapping("/associate/user")
    public Result<Boolean> associateToUser(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        String fingerprint = request.getHeader("X-Fingerprint");
        Long userId = Long.valueOf(params.get("userId").toString());

        if (fingerprint == null || fingerprint.isEmpty() || userId == null) {
            return Result.error("参数错误");
        }

        boolean success = visitorService.associateVisitorToUser(fingerprint, userId);
        if (success) {
            return Result.success(true, "关联成功");
        } else {
            return Result.error("关联失败");
        }
    }

    /**
     * 清除访客浏览记录
     *
     * @param request HTTP请求
     * @return 操作结果
     */
    @DeleteMapping("/browsing/clear")
    public Result<Boolean> clearBrowsingHistory(HttpServletRequest request) {
        String fingerprint = request.getHeader("X-Fingerprint");

        if (fingerprint == null || fingerprint.isEmpty()) {
            return Result.error("X-Fingerprint请求头不能为空");
        }

        boolean success = visitorService.clearVisitorBrowsingHistory(fingerprint);
        if (success) {
            return Result.success(true, "清除成功");
        } else {
            return Result.error("清除失败");
        }
    }
}
