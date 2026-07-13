package com.gamebench.tracker.game.controller;

import com.gamebench.tracker.common.api.ApiResponse;
import com.gamebench.tracker.game.dto.TestSceneSaveRequest;
import com.gamebench.tracker.game.service.TestSceneService;
import com.gamebench.tracker.game.vo.TestSceneResponse;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class TestSceneController {

    private final TestSceneService testSceneService;

    public TestSceneController(TestSceneService testSceneService) {
        this.testSceneService = testSceneService;
    }

    @PostMapping("/api/games/{gameId}/scenes")
    public ResponseEntity<ApiResponse<TestSceneResponse>> create(
            @PathVariable @Min(1) Long gameId,
            @Valid @RequestBody TestSceneSaveRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(testSceneService.create(gameId, request)));
    }

    @GetMapping("/api/games/{gameId}/scenes")
    public ApiResponse<List<TestSceneResponse>> listByGameId(@PathVariable @Min(1) Long gameId) {
        return ApiResponse.success(testSceneService.listByGameId(gameId));
    }

    @GetMapping("/api/scenes/{id}")
    public ApiResponse<TestSceneResponse> getById(@PathVariable @Min(1) Long id) {
        return ApiResponse.success(testSceneService.getById(id));
    }

    @PutMapping("/api/scenes/{id}")
    public ApiResponse<TestSceneResponse> update(
            @PathVariable @Min(1) Long id,
            @Valid @RequestBody TestSceneSaveRequest request) {
        return ApiResponse.success(testSceneService.update(id, request));
    }

    @DeleteMapping("/api/scenes/{id}")
    public ApiResponse<Void> delete(@PathVariable @Min(1) Long id) {
        testSceneService.delete(id);
        return ApiResponse.success(null);
    }
}
