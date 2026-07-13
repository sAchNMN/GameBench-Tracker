package com.gamebench.tracker.game.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record ConfigTemplateSaveRequest(
        @NotBlank(message = "模板名称不能为空") String name,
        String resolution,
        String graphicsPreset,
        String upscalingTech,
        String upscalingQuality,
        Boolean vsyncEnabled,
        Boolean frameGenerationEnabled,
        @PositiveOrZero(message = "GPU 核心频率不能为负数") BigDecimal gpuCoreClockMhz,
        @PositiveOrZero(message = "GPU 电压不能为负数") BigDecimal gpuVoltageMv,
        @PositiveOrZero(message = "GPU 显存频率不能为负数") BigDecimal gpuMemoryClockMhz,
        @DecimalMin(value = "-100", message = "GPU 功耗限制不能小于 -100")
        @DecimalMax(value = "100", message = "GPU 功耗限制不能大于 100") BigDecimal gpuPowerLimitPercent,
        String driverVersion,
        String customDescription) {
}
