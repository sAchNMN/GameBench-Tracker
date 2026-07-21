package com.gamebench.tracker.game.controller;

import com.gamebench.tracker.common.api.ApiResponse;
import com.gamebench.tracker.game.dto.BenchmarkRecordSaveRequest;
import com.gamebench.tracker.game.dto.RecordCompareRequest;
import com.gamebench.tracker.game.service.BenchmarkRecordService;
import com.gamebench.tracker.game.vo.BenchmarkRecordResponse;
import com.gamebench.tracker.game.vo.RecordCompareResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
public class BenchmarkRecordController {

    private final BenchmarkRecordService benchmarkRecordService;

    public BenchmarkRecordController(BenchmarkRecordService benchmarkRecordService) {
        this.benchmarkRecordService = benchmarkRecordService;
    }

    @PostMapping("/api/games/{gameId}/records")
    public ResponseEntity<ApiResponse<BenchmarkRecordResponse>> create(
            @PathVariable @Min(1) Long gameId,
            @Valid @RequestBody BenchmarkRecordSaveRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(benchmarkRecordService.create(gameId, request)));
    }

    @GetMapping("/api/games/{gameId}/records")
    public ApiResponse<List<BenchmarkRecordResponse>> listByGameId(@PathVariable @Min(1) Long gameId) {
        return ApiResponse.success(benchmarkRecordService.listByGameId(gameId));
    }

    @GetMapping(value = "/api/games/{gameId}/records/export", produces = MediaType.TEXT_PLAIN_VALUE + ";charset=utf-8")
    public ResponseEntity<String> exportCsv(@PathVariable @Min(1) Long gameId) {
        String csv = benchmarkRecordService.exportRecordsCsv(gameId);
        String filename = "benchmark-records-game-" + gameId + ".csv";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .header(HttpHeaders.CACHE_CONTROL, "no-cache")
                .body("\uFEFF" + csv);
    }

    @GetMapping("/api/records/{id}")
    public ApiResponse<BenchmarkRecordResponse> getById(@PathVariable @Min(1) Long id) {
        return ApiResponse.success(benchmarkRecordService.getById(id));
    }

    @PutMapping("/api/records/{id}")
    public ApiResponse<BenchmarkRecordResponse> update(
            @PathVariable @Min(1) Long id,
            @Valid @RequestBody BenchmarkRecordSaveRequest request) {
        return ApiResponse.success(benchmarkRecordService.update(id, request));
    }

    @DeleteMapping("/api/records/{id}")
    public ApiResponse<Void> delete(@PathVariable @Min(1) Long id) {
        benchmarkRecordService.delete(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/api/benchmark-records/compare")
    public ApiResponse<RecordCompareResponse> compare(@Valid @RequestBody RecordCompareRequest request) {
        return ApiResponse.success(benchmarkRecordService.compare(request));
    }
}
