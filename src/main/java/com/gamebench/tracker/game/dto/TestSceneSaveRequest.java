package com.gamebench.tracker.game.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record TestSceneSaveRequest(
        @NotBlank(message = "场景名称不能为空") String name,
        @NotBlank(message = "测试方法不能为空") String method,
        @Positive(message = "测试时长必须大于 0") Integer durationSeconds,
        String remark) {
}
