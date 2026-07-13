package com.gamebench.tracker.game.controller;

import com.gamebench.tracker.common.api.ApiResponse;
import com.gamebench.tracker.game.dto.GameSaveRequest;
import com.gamebench.tracker.game.service.GameService;
import com.gamebench.tracker.game.vo.GamePageResponse;
import com.gamebench.tracker.game.vo.GameResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/games")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<GameResponse>> create(@Valid @RequestBody GameSaveRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(gameService.create(request)));
    }

    @GetMapping
    public ApiResponse<GamePageResponse> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size) {
        return ApiResponse.success(gameService.list(keyword, page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<GameResponse> getById(@PathVariable @Min(1) Long id) {
        return ApiResponse.success(gameService.getById(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<GameResponse> update(
            @PathVariable @Min(1) Long id,
            @Valid @RequestBody GameSaveRequest request) {
        return ApiResponse.success(gameService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable @Min(1) Long id) {
        gameService.delete(id);
        return ApiResponse.success(null);
    }
}