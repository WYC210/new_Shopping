package com.wyc.mapper;

import com.wyc.domain.vo.DashboardOverviewVO;
import com.wyc.domain.vo.CityUserStatVO;
import com.wyc.domain.vo.DashboardTrendVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DashboardMapper {
    DashboardOverviewVO getDashboardOverview();

    List<CityUserStatVO> getCityUserDistribution();

    List<DashboardTrendVO> getDashboardTrend(@Param("startDate") String startDate, @Param("endDate") String endDate);
}