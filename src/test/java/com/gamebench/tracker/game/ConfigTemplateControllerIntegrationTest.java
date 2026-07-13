package com.gamebench.tracker.game;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamebench.tracker.game.dto.ConfigTemplateSaveRequest;
import com.gamebench.tracker.game.dto.GameSaveRequest;
import com.gamebench.tracker.game.entity.ConfigTemplate;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
class ConfigTemplateControllerIntegrationTest {

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

    @BeforeEach
    void clearData() {
        configTemplateMapper.delete(new LambdaQueryWrapper<>());
        testSceneMapper.delete(new LambdaQueryWrapper<>());
        gameMapper.delete(new LambdaQueryWrapper<>());
    }

    @Test
    void createsCompleteTemplateAndPersistsIt() throws Exception {
        long gameId = createGame("Cyberpunk 2077", "Steam");
        ConfigTemplateSaveRequest request = completeRequest("2K 光追");

        long templateId = extractId(mockMvc.perform(post("/api/games/{gameId}/config-templates", gameId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.gameId").value(gameId))
                .andExpect(jsonPath("$.data.name").value("2K 光追"))
                .andExpect(jsonPath("$.data.gpuPowerLimitPercent").value(15))
                .andExpect(jsonPath("$.error").isEmpty())
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andReturn());

        ConfigTemplate persisted = configTemplateMapper.selectById(templateId);
        assertNotNull(persisted);
        assertEquals("2560x1440", persisted.getResolution());
        assertEquals(new BigDecimal("15"), persisted.getGpuPowerLimitPercent());
    }

    @Test
    void rejectsTemplateForUnknownGame() throws Exception {
        mockMvc.perform(post("/api/games/{gameId}/config-templates", 9999)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(completeRequest("模板"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.error.code").value("GAME_NOT_FOUND"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void rejectsDuplicateTemplateNameWithinSameGame() throws Exception {
        long gameId = createGame("Cyberpunk 2077", "Steam");
        createTemplate(gameId, completeRequest("2K 光追"));

        mockMvc.perform(post("/api/games/{gameId}/config-templates", gameId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(completeRequest("2K 光追"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.error.code").value("CONFLICT"));
    }

    @Test
    void acceptsPowerLimitBoundaryValuesAndRejectsOutOfRangeValues() throws Exception {
        long gameId = createGame("Cyberpunk 2077", "Steam");
        createTemplate(gameId, requestWithPowerLimit("负功耗", "-100"));
        createTemplate(gameId, requestWithPowerLimit("默认功耗", "0"));
        createTemplate(gameId, requestWithPowerLimit("正功耗", "100"));

        mockMvc.perform(post("/api/games/{gameId}/config-templates", gameId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestWithPowerLimit("过低", "-100.1"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED"));

        mockMvc.perform(post("/api/games/{gameId}/config-templates", gameId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestWithPowerLimit("过高", "100.1"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED"));
    }

    @Test
    void listsOnlyTemplatesForRequestedGame() throws Exception {
        long cyberpunkId = createGame("Cyberpunk 2077", "Steam");
        long cs2Id = createGame("CS2", "Steam");
        createTemplate(cyberpunkId, completeRequest("光追"));
        createTemplate(cs2Id, completeRequest("竞技"));

        mockMvc.perform(get("/api/games/{gameId}/config-templates", cyberpunkId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].name").value("光追"))
                .andExpect(jsonPath("$.data[0].gameId").value(cyberpunkId))
                .andExpect(jsonPath("$.error").isEmpty())
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void updatesDeletesAndReportsMissingTemplate() throws Exception {
        long gameId = createGame("Cyberpunk 2077", "Steam");
        long templateId = createTemplate(gameId, completeRequest("旧配置"));
        ConfigTemplateSaveRequest updateRequest = requestWithPowerLimit("新配置", "-10");

        mockMvc.perform(put("/api/config-templates/{id}", templateId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("新配置"))
                .andExpect(jsonPath("$.data.gpuPowerLimitPercent").value(-10))
                .andExpect(jsonPath("$.data.updatedAt").isNotEmpty());

        mockMvc.perform(delete("/api/config-templates/{id}", templateId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.error").isEmpty())
                .andExpect(jsonPath("$.timestamp").isNotEmpty());

        mockMvc.perform(get("/api/config-templates/{id}", templateId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("TEMPLATE_NOT_FOUND"));
    }

    @Test
    void deletingGameCascadesItsTemplates() throws Exception {
        long gameId = createGame("Cyberpunk 2077", "Steam");
        long templateId = createTemplate(gameId, completeRequest("光追"));

        mockMvc.perform(delete("/api/games/{id}", gameId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        assertNull(configTemplateMapper.selectById(templateId));
    }

    private long createGame(String name, String platform) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/games")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new GameSaveRequest(name, platform, null))))
                .andExpect(status().isCreated())
                .andReturn();
        return extractId(result);
    }

    private long createTemplate(long gameId, ConfigTemplateSaveRequest request) throws Exception {
        return extractId(mockMvc.perform(post("/api/games/{gameId}/config-templates", gameId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn());
    }

    private ConfigTemplateSaveRequest completeRequest(String name) {
        return new ConfigTemplateSaveRequest(name, "2560x1440", "Ultra", "DLSS", "质量", true, true,
                new BigDecimal("2700"), new BigDecimal("1050"), new BigDecimal("12000"),
                new BigDecimal("15"), "576.80", "稳定测试配置");
    }

    private ConfigTemplateSaveRequest requestWithPowerLimit(String name, String powerLimit) {
        return new ConfigTemplateSaveRequest(name, null, null, null, null, null, null,
                null, null, null, new BigDecimal(powerLimit), null, null);
    }

    private long extractId(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString()).path("data").path("id").asLong();
    }
}
