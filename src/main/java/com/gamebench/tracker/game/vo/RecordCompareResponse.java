package com.gamebench.tracker.game.vo;

import java.math.BigDecimal;

/**
 * 双记录对比结果。
 *
 * <p>base 为基线记录，target 为目标记录。变化率与差异均以 target 相对 base 计算：
 * 变化率 = (target - base) / base * 100；差异 = target - base。
 * 当 base 值为空或 0 时不计算对应变化率（返回 null）；当功耗为空或 0 时不计算 FPS/W（返回 null）。
 */
public record RecordCompareResponse(
        BenchmarkRecordResponse base,
        BenchmarkRecordResponse target,
        BigDecimal avgFpsChangeRate,
        BigDecimal minFpsChangeRate,
        BigDecimal frameTimeMsChangeRate,
        BigDecimal gpuPowerChangeRate,
        BigDecimal gpuPowerDropRate,
        BigDecimal gpuTempDiff,
        BigDecimal cpuTempDiff,
        BigDecimal cpuUsageDiff,
        BigDecimal baseFpsPerWatt,
        BigDecimal targetFpsPerWatt,
        BigDecimal fpsPerWattChangeRate) {
}
