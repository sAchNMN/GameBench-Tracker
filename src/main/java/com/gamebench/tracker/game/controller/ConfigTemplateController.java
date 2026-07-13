package com.gamebench.tracker.game.controller;

import com.gamebench.tracker.common.api.ApiResponse;
import com.gamebench.tracker.game.dto.ConfigTemplateSaveRequest;
import com.gamebench.tracker.game.service.ConfigTemplateService;
import com.gamebench.tracker.game.vo.ConfigTemplateResponse;
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
public class ConfigTemplateController {

    private final ConfigTemplateService configTemplateService;

    public ConfigTemplateController(ConfigTemplateService configTemplateService) {
        this.configTemplateService = configTemplateService;
    }

    @PostMapping("/api/games/{gameId}/config-templates")
    public ResponseEntity<ApiResponse<ConfigTemplateResponse>> create(
            @PathVariable @Min(1) Long gameId,
            @Valid @RequestBody ConfigTemplateSaveRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(configTemplateService.create(gameId, request)));
    }

    @GetMapping("/api/games/{gameId}/config-templates")
    public ApiResponse<List<ConfigTemplateResponse>> listByGameId(@PathVariable @Min(1) Long gameId) {
        return ApiResponse.success(configTemplateService.listByGameId(gameId));
    }

    @GetMapping("/api/config-templates/{id}")
    public ApiResponse<ConfigTemplateResponse> getById(@PathVariable @Min(1) Long id) {
        return ApiResponse.success(configTemplateService.getById(id));
    }

    @PutMapping("/api/config-templates/{id}")
    public ApiResponse<ConfigTemplateResponse> update(
            @PathVariable @Min(1) Long id,
            @Valid @RequestBody ConfigTemplateSaveRequest request) {
        return ApiResponse.success(configTemplateService.update(id, request));
    }

    @DeleteMapping("/api/config-templates/{id}")
    public ApiResponse<Void> delete(@PathVariable @Min(1) Long id) {
        configTemplateService.delete(id);
        return ApiResponse.success(null);
    }
}
