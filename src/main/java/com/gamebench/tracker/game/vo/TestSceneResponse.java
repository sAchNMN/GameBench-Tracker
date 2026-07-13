package com.gamebench.tracker.game.vo;

public record TestSceneResponse(
        Long id,
        Long gameId,
        String name,
        String method,
        Integer durationSeconds,
        String remark,
        String createdAt,
        String updatedAt) {
}
