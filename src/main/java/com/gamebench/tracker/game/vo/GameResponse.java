package com.gamebench.tracker.game.vo;

public record GameResponse(
        Long id,
        String name,
        String platform,
        String remark,
        String createdAt,
        String updatedAt) {
}
