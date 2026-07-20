package com.gamebench.tracker.game.dto;

import jakarta.validation.constraints.NotBlank;

public record GameSaveRequest(
        @NotBlank(message = "游戏名称不能为空") String name,
        String platform,
        String remark) {
}
