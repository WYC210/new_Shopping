package com.wyc.service.impl;

import com.wyc.domain.vo.DashboardOverviewVO;
import com.wyc.domain.vo.CityUserStatVO;
import com.wyc.domain.vo.DashboardTrendVO;
import com.wyc.mapper.DashboardMapper;
import com.wyc.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private DashboardMapper dashboardMapper;

    @Override
    public DashboardOverviewVO getDashboardOverview() {
        DashboardOverviewVO vo = dashboardMapper.getDashboardOverview();
        List<CityUserStatVO> cityStats = dashboardMapper.getCityUserDistribution();
        vo.setCityUserDistribution(cityStats);
        return vo;
    }

    @Override
    public List<CityUserStatVO> getCityUserDistribution() {
        return dashboardMapper.getCityUserDistribution();
    }

    @Override
    public List<DashboardTrendVO> getDashboardTrend(String startDate, String endDate) {
        // 如果开始日期和结束日期都为空，默认获取本月数据
        if (startDate == null && endDate == null) {
            // 获取当前月份的第一天
            LocalDate today = LocalDate.now();
            LocalDate firstDayOfMonth = today.withDayOfMonth(1);
            LocalDate lastDayOfMonth = today.withDayOfMonth(today.lengthOfMonth());

            startDate = firstDayOfMonth.toString();
            endDate = lastDayOfMonth.toString();
        }
        // 如果只有开始日期为空，使用结束日期所在月的第一天
        else if (startDate == null) {
            LocalDate end = LocalDate.parse(endDate);
            startDate = end.withDayOfMonth(1).toString();
        }
        // 如果只有结束日期为空，使用开始日期所在月的最后一天
        else if (endDate == null) {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
            endDate = end.toString();
        }

        return dashboardMapper.getDashboardTrend(startDate, endDate);
    }
}