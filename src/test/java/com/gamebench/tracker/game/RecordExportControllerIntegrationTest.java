package com.gamebench.tracker.game;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamebench.tracker.game.dto.BenchmarkRecordSaveRequest;
import com.gamebench.tracker.game.dto.GameSaveRequest;
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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RecordExportControllerIntegrationTest {

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
    void exportsCsvWithHeaderAndRows() throws Exception {
        long gameId = createGame("Cyberpunk 2077", "Steam");
        createRecord(gameId, new BenchmarkRecordSaveRequest(null, null, "2026-07-20T10:00:00Z",
                new BigDecimal("120"), new BigDecimal("90"),
                new BigDecimal("65"), new BigDecimal("60"),
                new BigDecimal("300"), new BigDecimal("80"),
                new BigDecimal("8.3"), "first"));
        createRecord(gameId, new BenchmarkRecordSaveRequest(null, null, "2026-07-21T10:00:00Z",
                new BigDecimal("144"), new BigDecimal("100"),
                new BigDecimal("70"), new BigDecimal("62"),
                new BigDecimal("320"), new BigDecimal("85"),
                new BigDecimal("6.9"), "second"));

        MvcResult result = mockMvc.perform(get("/api/games/{gameId}/records/export", gameId))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        org.hamcrest.Matchers.containsString("benchmark-records-game-" + gameId + ".csv")))
                .andReturn();

        String csv = result.getResponse().getContentAsString();
        assertTrue(csv.contains("记录ID,场景ID,模板ID,测试时间,平均FPS,最低FPS,帧时间(ms),GPU温度(℃),CPU温度(℃),GPU功耗(W),CPU占用(%),备注"),
                "CSV 应含完整表头");
        assertTrue(csv.contains("120") && csv.contains("144"), "CSV 应含两条记录的平均 FPS");
        assertTrue(csv.contains("first") && csv.contains("second"), "CSV 应含两条记录的备注");
    }

    @Test
    void returnsNotFoundForUnknownGame() throws Exception {
        mockMvc.perform(get("/api/games/{gameId}/records/export", 9999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("GAME_NOT_FOUND"));
    }

    @Test
    void exportsOnlyHeaderWhenNoRecords() throws Exception {
        long gameId = createGame("Empty Game", "Steam");

        MvcResult result = mockMvc.perform(get("/api/games/{gameId}/records/export", gameId))
                .andExpect(status().isOk())
                .andReturn();

        String csv = result.getResponse().getContentAsString();
        assertTrue(csv.startsWith("﻿"), "CSV 应以 UTF-8 BOM 开头");
        assertTrue(csv.lines().count() == 1, "CSV 应只有表头行（末尾换行不计入空行），无数据行");
        assertTrue(csv.contains("记录ID,场景ID"), "CSV 仍应含表头");
    }

    @Test
    void escapesSpecialCharactersInCsv() throws Exception {
        long gameId = createGame("Cyberpunk 2077", "Steam");
        createRecord(gameId, new BenchmarkRecordSaveRequest(null, null, "2026-07-20T10:00:00Z",
                new BigDecimal("120"), new BigDecimal("90"),
                new BigDecimal("65"), new BigDecimal("60"),
                new BigDecimal("300"), new BigDecimal("80"),
                new BigDecimal("8.3"), "含逗号,与引号\"的备注"));

        MvcResult result = mockMvc.perform(get("/api/games/{gameId}/records/export", gameId))
                .andExpect(status().isOk())
                .andReturn();

        String csv = result.getResponse().getContentAsString();
        assertTrue(csv.contains("\"含逗号,与引号\"\"的备注\""), "含逗号与引号的备注应被双引号包裹且内部引号转义");
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

    private long extractId(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString()).path("data").path("id").asLong();
    }
}
