package com.gamebench.tracker.game.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record RecordCompareRequest(
        @NotNull(message = "基线记录 id 不能为空")
        @Min(value = 1, message = "基线记录 id 必须为正数") Long baseRecordId,
        @NotNull(message = "目标记录 id 不能为空")
        @Min(value = 1, message = "目标记录 id 必须为正数") Long targetRecordId) {
}
