package com.wyc.domain.vo;

import lombok.Data;
import java.util.List;

@Data
public class DashboardOverviewVO {
    private Integer orderCount;
    private Double totalAmount;
    private Integer paidOrderCount;
    private Double paidTotalAmount;
    private Integer userCount;
    private Integer productCount;
    private List<CityUserStatVO> cityUserDistribution;
}