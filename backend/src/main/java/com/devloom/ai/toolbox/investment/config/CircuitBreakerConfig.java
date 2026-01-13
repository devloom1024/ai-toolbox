package com.devloom.ai.toolbox.investment.config;

import lombok.Data;

/**
 * 熔断器配置
 */
@Data
public class CircuitBreakerConfig {
    /**
     * 失败率阈值 (0-100)
     */
    private Integer failureRateThreshold = 50;

    /**
     * 打开状态等待时间 (秒)
     */
    private Integer waitDurationInOpenStateSeconds = 30;

    /**
     * 滑动窗口大小
     */
    private Integer slidingWindowSize = 100;

    /**
     * 滑动窗口类型 (COUNT_BASED, TIME_BASED)
     */
    private String slidingWindowType = "COUNT_BASED";

    /**
     * 半开状态允许的调用数
     */
    private Integer permittedNumberOfCallsInHalfOpenState = 10;

    /**
     * 最小调用数 (达到此数量后才开始计算失败率)
     */
    private Integer minimumNumberOfCalls = 10;

    /**
     * 是否自动从打开到半开状态
     */
    private Boolean automaticTransitionFromOpenToHalfOpenEnabled = true;
}
