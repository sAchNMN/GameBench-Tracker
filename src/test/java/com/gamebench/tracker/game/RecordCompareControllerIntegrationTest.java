package com.gamebench.tracker.game;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamebench.tracker.game.dto.BenchmarkRecordSaveRequest;
import com.gamebench.tracker.game.dto.GameSaveRequest;
import com.gamebench.tracker.game.dto.RecordCompareRequest;
import com.gamebench.tracker.game.mapper.BenchmarkRecordMapper;
import com.gamebench.tracker.game.mapper.ConfigTemplateMapper;
import com.gamebench.tracker.game.mapper.GameMapper;
import com.gamebench.tracker.game.mapper.TestSceneMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RecordCompareControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private GameMapper gameMapper;

    @Autowired
    private TestSceneMapper testSceneMapper;

    @Autowired
    private ConfigTemplateMapper configTemplateMapper;

    @Autowired
    private BenchmarkRecordMapper benchmarkRecordMapper;

    @BeforeEach
    void clearData() {
        benchmarkRecordMapper.delete(new LambdaQueryWrapper<>());
        testSceneMapper.delete(new LambdaQueryWrapper<>());
        configTemplateMapper.delete(new LambdaQueryWrapper<>());
        gameMapper.delete(new LambdaQueryWrapper<>());
    }

    @Test
    void comparesTwoRecordsOfSameGameAndReturnsMetrics() throws Exception {
        long gameId = createGame("Cyberpunk 2077", "Steam");
        long baseId = createRecord(gameId, baseRequest());
        long targetId = createRecord(gameId, targetRequest());

        mockMvc.perform(post("/api/benchmark-records/compare")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new RecordCompareRequest(baseId, targetId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.base.id").value(baseId))
                .andExpect(jsonPath("$.data.target.id").value(targetId))
                .andExpect(jsonPath("$.data.avgFpsChangeRate").value(20.0))
                .andExpect(jsonPath("$.data.gpuPowerChangeRate").value(6.67))
                .andExpect(jsonPath("$.data.gpuPowerDropRate").value(-6.67))
                .andExpect(jsonPath("$.data.gpuTempDiff").value(5.0))
                .andExpect(jsonPath("$.data.baseFpsPerWatt").value(0.4))
                .andExpect(jsonPath("$.data.targetFpsPerWatt").value(0.45))
                .andExpect(jsonPath("$.data.fpsPerWattChangeRate").value(12.5))
                .andExpect(jsonPath("$.error").isEmpty());
    }

    @Test
    void returnsNullMetricsWhenBasePowerIsAbsent() throws Exception {
        long gameId = createGame("Cyberpunk 2077", "Steam");
        long baseId = createRecord(gameId, requestWithoutPower());
        long targetId = createRecord(gameId, targetRequest());

        mockMvc.perform(post("/api/benchmark-records/compare")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new RecordCompareRequest(baseId, targetId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.gpuPowerChangeRate").isEmpty())
                .andExpect(jsonPath("$.data.gpuPowerDropRate").isEmpty())
                .andExpect(jsonPath("$.data.baseFpsPerWatt").isEmpty())
                .andExpect(jsonPath("$.data.fpsPerWattChangeRate").isEmpty())
                .andExpect(jsonPath("$.data.avgFpsChangeRate").value(20.0));
    }

    @Test
    void rejectsUnknownBaseRecord() throws Exception {
        long gameId = createGame("Cyberpunk 2077", "Steam");
        long targetId = createRecord(gameId, targetRequest());

        mockMvc.perform(post("/api/benchmark-records/compare")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new RecordCompareRequest(9999L, targetId))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("RECORD_NOT_FOUND"));
    }

    @Test
    void rejectsCrossGameCompare() throws Exception {
        long gameA = createGame("Cyberpunk 2077", "Steam");
        long gameB = createGame("CS2", "Steam");
        long baseId = createRecord(gameA, baseRequest());
        long targetId = createRecord(gameB, targetRequest());

        mockMvc.perform(post("/api/benchmark-records/compare")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new RecordCompareRequest(baseId, targetId))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("CONFLICT"));
    }

    @Test
    void rejectsSameRecordId() throws Exception {
        long gameId = createGame("Cyberpunk 2077", "Steam");
        long recordId = createRecord(gameId, baseRequest());

        mockMvc.perform(post("/api/benchmark-records/compare")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new RecordCompareRequest(recordId, recordId))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("CONFLICT"));
    }

    private long createGame(String name, String platform) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/games")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new GameSaveRequest(name, platform, null))))
                .andExpect(status().isCreated())
                .andReturn();
        return extractId(result);
    }

    private long createRecord(long gameId, BenchmarkRecordSaveRequest request) throws Exception {
        return extractId(mockMvc.perform(post("/api/games/{gameId}/records", gameId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn());
    }

    private BenchmarkRecordSaveRequest baseRequest() {
        return new BenchmarkRecordSaveRequest(null, null, "2026-07-20T10:00:00Z",
                new BigDecimal("120"), new BigDecimal("90"),
                new BigDecimal("65"), new BigDecimal("60"),
                new BigDecimal("300"), new BigDecimal("80"),
                new BigDecimal("8.3"), "base");
    }

    private BenchmarkRecordSaveRequest targetRequest() {
        return new BenchmarkRecordSaveRequest(null, null, "2026-07-21T10:00:00Z",
                new BigDecimal("144"), new BigDecimal("100"),
                new BigDecimal("70"), new BigDecimal("62"),
                new BigDecimal("320"), new BigDecimal("85"),
                new BigDecimal("6.9"), "target");
    }

    private BenchmarkRecordSaveRequest requestWithoutPower() {
        return new BenchmarkRecordSaveRequest(null, null, "2026-07-20T10:00:00Z",
                new BigDecimal("120"), new BigDecimal("90"),
                new BigDecimal("65"), new BigDecimal("60"),
                null, new BigDecimal("80"),
                new BigDecimal("8.3"), "no power");
    }

    private long extractId(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString()).path("data").path("id").asLong();
    }
}
