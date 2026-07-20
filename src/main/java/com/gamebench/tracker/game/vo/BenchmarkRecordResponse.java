package com.gamebench.tracker.game.vo;

import java.math.BigDecimal;

public record BenchmarkRecordResponse(
        Long id,
        Long gameId,
        Long sceneId,
        Long templateId,
        String recordedAt,
        BigDecimal avgFps,
        BigDecimal minFps,
        BigDecimal gpuTempCelsius,
        BigDecimal cpuTempCelsius,
        BigDecimal gpuPowerWatt,
        BigDecimal cpuUsagePercent,
        BigDecimal frameTimeMs,
        String notes,
        String createdAt,
        String updatedAt) {
}
