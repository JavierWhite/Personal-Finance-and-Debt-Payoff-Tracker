package com.javier.finance.analytics.dto;

import java.math.BigDecimal;
import java.time.YearMonth;

public record ChartDataPoint(YearMonth month, BigDecimal value) {
}
