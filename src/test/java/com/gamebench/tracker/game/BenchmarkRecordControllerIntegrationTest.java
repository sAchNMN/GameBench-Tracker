package com.gamebench.tracker.game;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamebench.tracker.game.dto.BenchmarkRecordSaveRequest;
import com.gamebench.tracker.game.dto.GameSaveRequest;
import com.gamebench.tracker.game.entity.BenchmarkRecord;
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

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BenchmarkRecordControllerIntegrationTest {

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
    void createsRecordForGameAndPersistsIt() throws Exception {
        long gameId = createGame("Cyberpunk 2077", "Steam");
        BenchmarkRecordSaveRequest request = sampleRequest(null, null);

        long recordId = extractId(mockMvc.perform(post("/api/games/{gameId}/records", gameId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.gameId").value(gameId))
                .andExpect(jsonPath("$.data.avgFps").value(120))
                .andExpect(jsonPath("$.data.minFps").value(90))
                .andExpect(jsonPath("$.data.gpuPowerWatt").value(300))
                .andExpect(jsonPath("$.data.recordedAt").value("2026-07-20T10:00:00Z"))
                .andExpect(jsonPath("$.error").isEmpty())
                .andReturn());

        BenchmarkRecord persisted = benchmarkRecordMapper.selectById(recordId);
        assertNull(persisted.getSceneId());
        assertNull(persisted.getTemplateId());
    }

    @Test
    void rejectsRecordForUnknownGame() throws Exception {
        mockMvc.perform(post("/api/games/{gameId}/records", 9999)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(sampleRequest(null, null))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("GAME_NOT_FOUND"));
    }

    @Test
    void rejectsUnknownRecordLookup() throws Exception {
        mockMvc.perform(get("/api/records/{id}", 9999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("RECORD_NOT_FOUND"));
    }

    @Test
    void listsOnlyRecordsForRequestedGame() throws Exception {
        long cyberpunkId = createGame("Cyberpunk 2077", "Steam");
        long cs2Id = createGame("CS2", "Steam");
        createRecord(cyberpunkId, sampleRequest(null, null));
        createRecord(cyberpunkId, sampleRequest(null, null));
        createRecord(cs2Id, sampleRequest(null, null));

        mockMvc.perform(get("/api/games/{gameId}/records", cyberpunkId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void rejectsNegativeAvgFpsWithUnifiedValidationResponse() throws Exception {
        long gameId = createGame("Cyberpunk 2077", "Steam");
        BenchmarkRecordSaveRequest bad = new BenchmarkRecordSaveRequest(null, null, null,
                new BigDecimal("-1"), new BigDecimal("90"),
                new BigDecimal("65"), new BigDecimal("60"),
                new BigDecimal("300"), new BigDecimal("80"),
                new BigDecimal("8.3"), "bad");

        mockMvc.perform(post("/api/games/{gameId}/records", gameId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(bad)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED"));
    }

    @Test
    void rejectsUnknownSceneReference() throws Exception {
        long gameId = createGame("Cyberpunk 2077", "Steam");
        BenchmarkRecordSaveRequest request = new BenchmarkRecordSaveRequest(9999L, null, null,
                new BigDecimal("120"), new BigDecimal("90"),
                new BigDecimal("65"), new BigDecimal("60"),
                new BigDecimal("300"), new BigDecimal("80"),
                new BigDecimal("8.3"), "with scene");

        mockMvc.perform(post("/api/games/{gameId}/records", gameId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("SCENE_NOT_FOUND"));
    }

    @Test
    void rejectsUnknownTemplateReference() throws Exception {
        long gameId = createGame("Cyberpunk 2077", "Steam");
        BenchmarkRecordSaveRequest request = new BenchmarkRecordSaveRequest(null, 9999L, null,
                new BigDecimal("120"), new BigDecimal("90"),
                new BigDecimal("65"), new BigDecimal("60"),
                new BigDecimal("300"), new BigDecimal("80"),
                new BigDecimal("8.3"), "with template");

        mockMvc.perform(post("/api/games/{gameId}/records", gameId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("TEMPLATE_NOT_FOUND"));
    }

    @Test
    void updatesAndDeletesRecordWithUnifiedResponses() throws Exception {
        long gameId = createGame("Cyberpunk 2077", "Steam");
        long recordId = createRecord(gameId, sampleRequest(null, null));
        BenchmarkRecordSaveRequest update = new BenchmarkRecordSaveRequest(null, null, null,
                new BigDecimal("144"), new BigDecimal("100"),
                new BigDecimal("70"), new BigDecimal("62"),
                new BigDecimal("320"), new BigDecimal("85"),
                new BigDecimal("6.9"), "updated");

        mockMvc.perform(put("/api/records/{id}", recordId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.avgFps").value(144))
                .andExpect(jsonPath("$.data.updatedAt").isNotEmpty());

        mockMvc.perform(delete("/api/records/{id}", recordId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/records/{id}", recordId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("RECORD_NOT_FOUND"));
    }

    @Test
    void deletingGameCascadesItsRecords() throws Exception {
        long gameId = createGame("Cyberpunk 2077", "Steam");
        long recordId = createRecord(gameId, sampleRequest(null, null));

        mockMvc.perform(delete("/api/games/{id}", gameId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        assertNull(benchmarkRecordMapper.selectById(recordId));
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

    private BenchmarkRecordSaveRequest sampleRequest(Long sceneId, Long templateId) {
        return new BenchmarkRecordSaveRequest(sceneId, templateId, "2026-07-20T10:00:00Z",
                new BigDecimal("120"), new BigDecimal("90"),
                new BigDecimal("65"), new BigDecimal("60"),
                new BigDecimal("300"), new BigDecimal("80"),
                new BigDecimal("8.3"), "demo");
    }

    private long extractId(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString()).path("data").path("id").asLong();
    }
}
