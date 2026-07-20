package com.gamebench.tracker.game.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record BenchmarkRecordSaveRequest(
        Long sceneId,
        Long templateId,
        String recordedAt,
        @Positive(message = "平均帧率必须大于 0") BigDecimal avgFps,
        @Positive(message = "最低帧率必须大于 0") BigDecimal minFps,
        @DecimalMin(value = "-273.15", message = "GPU 温度不能低于绝对零度") BigDecimal gpuTempCelsius,
        @DecimalMin(value = "-273.15", message = "CPU 温度不能低于绝对零度") BigDecimal cpuTempCelsius,
        @PositiveOrZero(message = "GPU 功耗不能为负数") BigDecimal gpuPowerWatt,
        @DecimalMin(value = "0", message = "CPU 占用不能为负数")
        @DecimalMax(value = "100", message = "CPU 占用不能超过 100") BigDecimal cpuUsagePercent,
        @Positive(message = "平均帧时间必须大于 0") BigDecimal frameTimeMs,
        String notes) {
}
