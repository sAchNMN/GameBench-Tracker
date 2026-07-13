package com.gamebench.tracker.game.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.gamebench.tracker.common.error.ErrorCode;
import com.gamebench.tracker.common.exception.ApplicationException;
import com.gamebench.tracker.game.dto.ConfigTemplateSaveRequest;
import com.gamebench.tracker.game.entity.ConfigTemplate;
import com.gamebench.tracker.game.entity.Game;
import com.gamebench.tracker.game.mapper.ConfigTemplateMapper;
import com.gamebench.tracker.game.mapper.GameMapper;
import com.gamebench.tracker.game.vo.ConfigTemplateResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class ConfigTemplateService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigTemplateService.class);

    private final GameMapper gameMapper;
    private final ConfigTemplateMapper configTemplateMapper;

    public ConfigTemplateService(GameMapper gameMapper, ConfigTemplateMapper configTemplateMapper) {
        this.gameMapper = gameMapper;
        this.configTemplateMapper = configTemplateMapper;
    }

    public ConfigTemplateResponse create(Long gameId, ConfigTemplateSaveRequest request) {
        /*
         * ========================================================================
         * 步骤1：创建配置模板
         * ========================================================================
         * 目标：在指定游戏下保存可复用的图形和硬件配置。
         * 数据源：路径 gameId、ConfigTemplateSaveRequest 与 SQLite。
         * 操作：
         * 1) 校验目标游戏存在。
         * 2) 规范化文本字段并保存完整配置。
         */
        LOGGER.info("开始创建配置模板, gameId: {}", gameId);

        // 1.1 校验归属游戏，阻止孤立模板。
        requireGame(gameId);

        // 1.2 映射请求并写入 SQLite。
        ConfigTemplate template = toEntity(request);
        template.setGameId(gameId);
        configTemplateMapper.insert(template);
        ConfigTemplateResponse response = toResponse(requireTemplate(template.getId()));

        LOGGER.info("创建配置模板完成, id: {}, gameId: {}", template.getId(), gameId);
        return response;
    }

    public List<ConfigTemplateResponse> listByGameId(Long gameId) {
        /*
         * ========================================================================
         * 步骤2：查询游戏配置模板
         * ========================================================================
         * 目标：只返回指定游戏拥有的模板。
         * 数据源：SQLite game 与 config_template 表。
         * 操作：
         * 1) 校验游戏存在。
         * 2) 按 gameId 查询并转换响应。
         */
        LOGGER.info("开始查询配置模板列表, gameId: {}", gameId);

        // 2.1 将不存在游戏转换为统一业务错误。
        requireGame(gameId);

        // 2.2 读取当前游戏的模板并按主键倒序返回。
        List<ConfigTemplateResponse> responses = configTemplateMapper.selectList(
                        new LambdaQueryWrapper<ConfigTemplate>()
                                .eq(ConfigTemplate::getGameId, gameId)
                                .orderByDesc(ConfigTemplate::getId))
                .stream()
                .map(this::toResponse)
                .toList();

        LOGGER.info("查询配置模板列表完成, gameId: {}, count: {}", gameId, responses.size());
        return responses;
    }

    public ConfigTemplateResponse getById(Long id) {
        /*
         * ========================================================================
         * 步骤3：读取配置模板
         * ========================================================================
         * 目标：返回单个模板详情。
         * 数据源：SQLite config_template 表。
         * 操作：
         * 1) 查询模板并校验存在。
         * 2) 转换为 API 响应。
         */
        LOGGER.info("开始查询配置模板, id: {}", id);

        // 3.1 不存在模板统一抛出 TEMPLATE_NOT_FOUND。
        ConfigTemplateResponse response = toResponse(requireTemplate(id));

        LOGGER.info("查询配置模板完成, id: {}", id);
        return response;
    }

    public ConfigTemplateResponse update(Long id, ConfigTemplateSaveRequest request) {
        /*
         * ========================================================================
         * 步骤4：更新配置模板
         * ========================================================================
         * 目标：修正配置字段，不改变模板所属游戏。
         * 数据源：ConfigTemplateSaveRequest 与 SQLite config_template 表。
         * 操作：
         * 1) 校验模板存在。
         * 2) 更新字段和 UTC 更新时间后返回最终数据。
         */
        LOGGER.info("开始更新配置模板, id: {}", id);

        // 4.1 先校验目标模板存在。
        requireTemplate(id);

        // 4.2 写入配置字段，保留原有 gameId。
        ConfigTemplate template = toEntity(request);
        template.setUpdatedAt(Instant.now().toString());
        configTemplateMapper.update(template, new LambdaUpdateWrapper<ConfigTemplate>().eq(ConfigTemplate::getId, id));
        ConfigTemplateResponse response = toResponse(requireTemplate(id));

        LOGGER.info("更新配置模板完成, id: {}", id);
        return response;
    }

    public void delete(Long id) {
        /*
         * ========================================================================
         * 步骤5：删除配置模板
         * ========================================================================
         * 目标：删除指定模板；未来历史记录仅保留来源 ID 和字段快照。
         * 数据源：SQLite config_template 表。
         * 操作：
         * 1) 校验模板存在。
         * 2) 删除数据库行。
         */
        LOGGER.info("开始删除配置模板, id: {}", id);

        // 5.1 保证不存在模板不会被当作删除成功。
        requireTemplate(id);

        // 5.2 删除当前模板。
        configTemplateMapper.deleteById(id);

        LOGGER.info("删除配置模板完成, id: {}", id);
    }

    private Game requireGame(Long gameId) {
        Game game = gameMapper.selectById(gameId);
        if (game == null) {
            throw new ApplicationException(ErrorCode.GAME_NOT_FOUND);
        }
        return game;
    }

    private ConfigTemplate requireTemplate(Long id) {
        ConfigTemplate template = configTemplateMapper.selectById(id);
        if (template == null) {
            throw new ApplicationException(ErrorCode.TEMPLATE_NOT_FOUND);
        }
        return template;
    }

    private ConfigTemplate toEntity(ConfigTemplateSaveRequest request) {
        ConfigTemplate template = new ConfigTemplate();
        template.setName(normalizeRequired(request.name()));
        template.setResolution(normalizeNullable(request.resolution()));
        template.setGraphicsPreset(normalizeNullable(request.graphicsPreset()));
        template.setUpscalingTech(normalizeNullable(request.upscalingTech()));
        template.setUpscalingQuality(normalizeNullable(request.upscalingQuality()));
        template.setVsyncEnabled(request.vsyncEnabled());
        template.setFrameGenerationEnabled(request.frameGenerationEnabled());
        template.setGpuCoreClockMhz(request.gpuCoreClockMhz());
        template.setGpuVoltageMv(request.gpuVoltageMv());
        template.setGpuMemoryClockMhz(request.gpuMemoryClockMhz());
        template.setGpuPowerLimitPercent(request.gpuPowerLimitPercent());
        template.setDriverVersion(normalizeNullable(request.driverVersion()));
        template.setCustomDescription(normalizeNullable(request.customDescription()));
        return template;
    }

    private ConfigTemplateResponse toResponse(ConfigTemplate template) {
        return new ConfigTemplateResponse(template.getId(), template.getGameId(), template.getName(),
                template.getResolution(), template.getGraphicsPreset(), template.getUpscalingTech(),
                template.getUpscalingQuality(), template.getVsyncEnabled(), template.getFrameGenerationEnabled(),
                template.getGpuCoreClockMhz(), template.getGpuVoltageMv(), template.getGpuMemoryClockMhz(),
                template.getGpuPowerLimitPercent(), template.getDriverVersion(), template.getCustomDescription(),
                template.getCreatedAt(), template.getUpdatedAt());
    }

    private String normalizeRequired(String value) {
        return value.trim();
    }

    private String normalizeNullable(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
