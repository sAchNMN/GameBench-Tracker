package com.gamebench.tracker.common.web;

import com.gamebench.tracker.common.api.ApiResponse;
import com.gamebench.tracker.common.error.ErrorCode;
import com.gamebench.tracker.common.exception.ApplicationException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 仅用于验证 Spring MVC 请求链的测试控制器。
 */
@RestController
@RequestMapping("/test")
public class ValidationProbeController {

    @PostMapping("/validation/body")
    public ApiResponse<Map<String, Object>> validateBody(@Valid @RequestBody ValidationBody body) {
        return ApiResponse.success(Map.of("name", body.name(), "count", body.count()));
    }

    @GetMapping("/validation/query")
    public ApiResponse<Map<String, Integer>> validateQuery(
            @RequestParam(name = "limit") @Min(value = 1, message = "必须大于等于 1") int limit) {
        return ApiResponse.success(Map.of("limit", limit));
    }

    @GetMapping("/validation/path/{id}")
    public ApiResponse<Map<String, Long>> validatePath(
            @PathVariable(name = "id") @Min(value = 1, message = "必须大于等于 1") long id) {
        return ApiResponse.success(Map.of("id", id));
    }

    @GetMapping("/errors/application")
    public ApiResponse<Void> throwApplicationException() {
        throw new ApplicationException(ErrorCode.CONFLICT, Map.of("resource", "test"));
    }

    @GetMapping("/errors/unexpected")
    public ApiResponse<Void> throwUnexpectedException() {
        throw new IllegalStateException("internal-secret-message");
    }

    public record ValidationBody(
            @NotBlank(message = "name 不能为空") String name,
            @NotNull(message = "count 不能为空") @Min(value = 1, message = "count 必须大于等于 1") Integer count) {
    }
}