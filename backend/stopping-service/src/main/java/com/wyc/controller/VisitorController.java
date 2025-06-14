package com.wyc.controller;

import com.wyc.domain.common.Result;
import com.wyc.service.IVisitorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
     * 记录访客浏览商品
     *
     * @param params 参数，包含fingerprint、productId和productName
     * @return 操作结果
     */
    @PostMapping("/browsing/record")
    public Result<Boolean> recordBrowsing(@RequestBody Map<String, Object> params) {
        String fingerprint = (String) params.get("fingerprint");
        Long productId = Long.valueOf(params.get("productId").toString());
        String productName = (String) params.get("productName");

        if (fingerprint == null || fingerprint.isEmpty() || productId == null) {
            return Result.error("参数错误");
        }

        boolean success = visitorService.recordVisitorBrowsing(fingerprint, productId, productName);
        if (success) {
            return Result.success(true, "记录成功");
        } else {
            return Result.error("记录失败");
        }
    }

    /**
     * 获取访客浏览历史
     *
     * @param fingerprint 浏览器指纹
     * @param limit       限制数量，默认10
     * @return 浏览历史列表
     */
    @GetMapping("/browsing/history")
    public Result<List<Object>> getBrowsingHistory(@RequestParam("fingerprint") String fingerprint,
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        if (fingerprint == null || fingerprint.isEmpty()) {
            return Result.error("参数错误");
        }

        List<Object> history = visitorService.getVisitorBrowsingHistory(fingerprint, limit);
        return Result.success(history);
    }

    /**
     * 将访客浏览记录关联到用户
     *
     * @param params 参数，包含fingerprint和userId
     * @return 操作结果
     */
    @PostMapping("/associate/user")
    public Result<Boolean> associateToUser(@RequestBody Map<String, Object> params) {
        String fingerprint = (String) params.get("fingerprint");
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
     * @param fingerprint 浏览器指纹
     * @return 操作结果
     */
    @DeleteMapping("/browsing/clear")
    public Result<Boolean> clearBrowsingHistory(@RequestParam("fingerprint") String fingerprint) {
        if (fingerprint == null || fingerprint.isEmpty()) {
            return Result.error("参数错误");
        }

        boolean success = visitorService.clearVisitorBrowsingHistory(fingerprint);
        if (success) {
            return Result.success(true, "清除成功");
        } else {
            return Result.error("清除失败");
        }
    }
}
