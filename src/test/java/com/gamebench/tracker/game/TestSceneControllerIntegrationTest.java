package com.gamebench.tracker.game;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamebench.tracker.game.dto.GameSaveRequest;
import com.gamebench.tracker.game.dto.TestSceneSaveRequest;
import com.gamebench.tracker.game.entity.TestScene;
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
class TestSceneControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private GameMapper gameMapper;

    @Autowired
    private TestSceneMapper testSceneMapper;

    @BeforeEach
    void clearData() {
        testSceneMapper.delete(new LambdaQueryWrapper<>());
        gameMapper.delete(new LambdaQueryWrapper<>());
    }

    @Test
    void createsSceneForGameAndPersistsIt() throws Exception {
        long gameId = createGame("CS2", "Steam");
        long sceneId = extractId(createScene(gameId, "Mirage 中路", "固定路线", 120, "1080p"));

        mockMvc.perform(get("/api/scenes/{id}", sceneId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.gameId").value(gameId))
                .andExpect(jsonPath("$.data.name").value("Mirage 中路"))
                .andExpect(jsonPath("$.data.method").value("固定路线"))
                .andExpect(jsonPath("$.data.durationSeconds").value(120))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());

        TestScene persisted = testSceneMapper.selectById(sceneId);
        assertNotNull(persisted);
        assertEquals("1080p", persisted.getRemark());
    }

    @Test
    void rejectsSceneForUnknownGame() throws Exception {
        mockMvc.perform(post("/api/games/{gameId}/scenes", 9999)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new TestSceneSaveRequest("Mirage", "固定路线", 120, null))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.error.code").value("GAME_NOT_FOUND"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void rejectsDuplicateSceneNameWithinSameGame() throws Exception {
        long gameId = createGame("CS2", "Steam");
        createScene(gameId, "Mirage", "固定路线", 120, null);

        mockMvc.perform(post("/api/games/{gameId}/scenes", gameId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new TestSceneSaveRequest("Mirage", "另一条路线", 90, null))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.error.code").value("CONFLICT"));
    }

    @Test
    void listsOnlyScenesForRequestedGame() throws Exception {
        long cs2Id = createGame("CS2", "Steam");
        long cyberpunkId = createGame("Cyberpunk 2077", "GOG");
        createScene(cs2Id, "Mirage", "固定路线", 120, null);
        createScene(cyberpunkId, "市中心", "固定路线", 60, null);

        mockMvc.perform(get("/api/games/{gameId}/scenes", cs2Id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].name").value("Mirage"))
                .andExpect(jsonPath("$.data[0].gameId").value(cs2Id));
    }

    @Test
    void rejectsInvalidSceneDurationWithUnifiedValidationResponse() throws Exception {
        long gameId = createGame("CS2", "Steam");

        mockMvc.perform(post("/api/games/{gameId}/scenes", gameId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new TestSceneSaveRequest("Mirage", "固定路线", 0, null))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.error.details.violations[0].path").value("request"))
                .andExpect(jsonPath("$.error.details.violations[0].message").value("测试时长必须大于 0"));
    }

    @Test
    void updatesAndDeletesSceneWithUnifiedResponses() throws Exception {
        long gameId = createGame("CS2", "Steam");
        long sceneId = extractId(createScene(gameId, "Mirage", "固定路线", 120, null));

        mockMvc.perform(put("/api/scenes/{id}", sceneId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new TestSceneSaveRequest("Mirage A 点", "烟雾路线", 150, "更新备注"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Mirage A 点"))
                .andExpect(jsonPath("$.data.durationSeconds").value(150))
                .andExpect(jsonPath("$.data.updatedAt").isNotEmpty());

        mockMvc.perform(delete("/api/scenes/{id}", sceneId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.error").isEmpty());

        mockMvc.perform(get("/api/scenes/{id}", sceneId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("SCENE_NOT_FOUND"));
    }

    @Test
    void deletingGameCascadesItsScenes() throws Exception {
        long gameId = createGame("CS2", "Steam");
        long sceneId = extractId(createScene(gameId, "Mirage", "固定路线", 120, null));

        mockMvc.perform(delete("/api/games/{id}", gameId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        assertNull(testSceneMapper.selectById(sceneId));
    }

    private long createGame(String name, String platform) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/games")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new GameSaveRequest(name, platform, null))))
                .andExpect(status().isCreated())
                .andReturn();
        return extractId(result);
    }

    private MvcResult createScene(Long gameId, String name, String method, Integer durationSeconds, String remark)
            throws Exception {
        return mockMvc.perform(post("/api/games/{gameId}/scenes", gameId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new TestSceneSaveRequest(name, method, durationSeconds, remark))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value(name))
                .andExpect(jsonPath("$.error").isEmpty())
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andReturn();
    }

    private long extractId(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString()).path("data").path("id").asLong();
    }
}
