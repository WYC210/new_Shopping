package com.wyc.service;

import com.wyc.domain.vo.DashboardOverviewVO;
import com.wyc.domain.vo.CityUserStatVO;
import com.wyc.domain.vo.DashboardTrendVO;
import java.util.List;

public interface DashboardService {
    DashboardOverviewVO getDashboardOverview();

    List<CityUserStatVO> getCityUserDistribution();

    List<DashboardTrendVO> getDashboardTrend(String startDate, String endDate);
}