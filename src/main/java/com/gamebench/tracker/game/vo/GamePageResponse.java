package com.gamebench.tracker.game.vo;

import java.util.List;

public record GamePageResponse(
        List<GameResponse> items,
        long total,
        int page,
        int size) {
}
