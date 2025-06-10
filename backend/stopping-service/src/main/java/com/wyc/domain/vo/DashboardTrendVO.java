package com.wyc.domain.vo;

import lombok.Data;
import java.time.LocalDate;

@Data
public class DashboardTrendVO {
    private LocalDate date;
    private Integer orderCount;
    private Double totalAmount;
    private Integer paidOrderCount;
    private Double paidTotalAmount;
    private Integer userCount;
}