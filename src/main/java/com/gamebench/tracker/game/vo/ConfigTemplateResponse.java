package com.gamebench.tracker.game.vo;

import java.math.BigDecimal;

public record ConfigTemplateResponse(
        Long id,
        Long gameId,
        String name,
        String resolution,
        String graphicsPreset,
        String upscalingTech,
        String upscalingQuality,
        Boolean vsyncEnabled,
        Boolean frameGenerationEnabled,
        BigDecimal gpuCoreClockMhz,
        BigDecimal gpuVoltageMv,
        BigDecimal gpuMemoryClockMhz,
        BigDecimal gpuPowerLimitPercent,
        String driverVersion,
        String customDescription,
        String createdAt,
        String updatedAt) {
}
