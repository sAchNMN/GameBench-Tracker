package com.gamebench.tracker.game;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamebench.tracker.game.dto.GameSaveRequest;
import com.gamebench.tracker.game.entity.Game;
import com.gamebench.tracker.game.mapper.GameMapper;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GameControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private GameMapper gameMapper;

    @BeforeEach
    void clearGames() {
        gameMapper.delete(new LambdaQueryWrapper<>());
    }

    @Test
    void createsCs2ForSteamAndPersistsIt() throws Exception {
        MvcResult result = createGame("CS2", "Steam", "竞技基准测试");

        mockMvc.perform(get("/api/games/{id}", result.getResponse().getContentAsString()
                        .replaceAll(".*\\\"id\\\":(\\d+).*", "$1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("CS2"))
                .andExpect(jsonPath("$.data.platform").value("Steam"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());

        Game persisted = gameMapper.selectOne(new LambdaQueryWrapper<Game>()
                .eq(Game::getName, "CS2").eq(Game::getPlatform, "Steam"));
        assertNotNull(persisted);
        assertEquals("竞技基准测试", persisted.getRemark());
    }

    @Test
    void rejectsDuplicateNameAndPlatform() throws Exception {
        createGame("CS2", "Steam", null);

        mockMvc.perform(post("/api/games")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new GameSaveRequest("CS2", "Steam", null))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.error.code").value("CONFLICT"))
                .andExpect(jsonPath("$.error.details").isEmpty())
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void rejectsBlankGameNameWithUnifiedValidationResponse() throws Exception {
        mockMvc.perform(post("/api/games")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new GameSaveRequest("   ", "Steam", null))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.error.details.fieldErrors[0].field").value("name"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }
    @Test
    void listsGamesByKeywordAndPage() throws Exception {
        createGame("CS2", "Steam", null);
        createGame("Cyberpunk 2077", "GOG", null);

        mockMvc.perform(get("/api/games").param("keyword", "CS").param("page", "1").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.items[0].name").value("CS2"))
                .andExpect(jsonPath("$.error").isEmpty())
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void updatesExistingGame() throws Exception {
        long id = extractId(createGame("CS2", "Steam", null));

        mockMvc.perform(put("/api/games/{id}", id)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new GameSaveRequest("CS2", "Steam", "更新后的备注"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.remark").value("更新后的备注"))
                .andExpect(jsonPath("$.data.updatedAt").isNotEmpty())
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void deletesGameAndReturnsNotFoundAfterward() throws Exception {
        long id = extractId(createGame("CS2", "Steam", null));

        mockMvc.perform(delete("/api/games/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.error").isEmpty())
                .andExpect(jsonPath("$.timestamp").isNotEmpty());

        mockMvc.perform(get("/api/games/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("GAME_NOT_FOUND"));
    }

    private MvcResult createGame(String name, String platform, String remark) throws Exception {
        return mockMvc.perform(post("/api/games")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new GameSaveRequest(name, platform, remark))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value(name))
                .andExpect(jsonPath("$.data.platform").value(platform))
                .andExpect(jsonPath("$.error").isEmpty())
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andReturn();
    }

    private long extractId(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString()).path("data").path("id").asLong();
    }
}