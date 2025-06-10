package com.wyc.controller;

import com.wyc.domain.vo.DashboardOverviewVO;
import com.wyc.domain.vo.CityUserStatVO;
import com.wyc.domain.vo.DashboardTrendVO;
import com.wyc.service.DashboardService;
import com.wyc.utils.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.PermitAll;
import java.util.List;

@Api(tags = "数字大屏接口")
@RestController
@RequestMapping("/wyc/dashboard")
@PermitAll // 整个控制器的所有接口都不需要登录验证
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @ApiOperation("获取数字大屏总览数据")
    @GetMapping("/overview")
    @PermitAll // 不需要登录验证
    public R<DashboardOverviewVO> getDashboardOverview() {
        return R.ok(dashboardService.getDashboardOverview());
    }

    @ApiOperation("获取城市用户分布数据")
    @GetMapping("/city-distribution")
    @PermitAll // 不需要登录验证
    public R<List<CityUserStatVO>> getCityUserDistribution() {
        return R.ok(dashboardService.getCityUserDistribution());
    }

    @ApiOperation("获取数字大屏趋势数据（按天聚合）")
    @GetMapping("/trend")
    @PermitAll // 不需要登录验证
    public R<List<DashboardTrendVO>> getDashboardTrend(
            @ApiParam(value = "开始日期", example = "2025-05-01", required = false) @RequestParam(required = false) String startDate,
            @ApiParam(value = "结束日期", example = "2025-05-31", required = false) @RequestParam(required = false) String endDate) {
        return R.ok(dashboardService.getDashboardTrend(startDate, endDate));
    }
}
