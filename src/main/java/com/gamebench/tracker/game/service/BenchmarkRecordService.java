package com.gamebench.tracker.game.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.gamebench.tracker.common.error.ErrorCode;
import com.gamebench.tracker.common.exception.ApplicationException;
import com.gamebench.tracker.game.dto.BenchmarkRecordSaveRequest;
import com.gamebench.tracker.game.dto.RecordCompareRequest;
import com.gamebench.tracker.game.entity.BenchmarkRecord;
import com.gamebench.tracker.game.entity.Game;
import com.gamebench.tracker.game.mapper.BenchmarkRecordMapper;
import com.gamebench.tracker.game.mapper.ConfigTemplateMapper;
import com.gamebench.tracker.game.mapper.GameMapper;
import com.gamebench.tracker.game.mapper.TestSceneMapper;
import com.gamebench.tracker.game.vo.BenchmarkRecordResponse;
import com.gamebench.tracker.game.vo.RecordCompareResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;

@Service
public class BenchmarkRecordService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BenchmarkRecordService.class);
    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private final GameMapper gameMapper;
    private final BenchmarkRecordMapper benchmarkRecordMapper;
    private final TestSceneMapper testSceneMapper;
    private final ConfigTemplateMapper configTemplateMapper;

    public BenchmarkRecordService(GameMapper gameMapper, BenchmarkRecordMapper benchmarkRecordMapper,
                                  TestSceneMapper testSceneMapper, ConfigTemplateMapper configTemplateMapper) {
        this.gameMapper = gameMapper;
        this.benchmarkRecordMapper = benchmarkRecordMapper;
        this.testSceneMapper = testSceneMapper;
        this.configTemplateMapper = configTemplateMapper;
    }

    public BenchmarkRecordResponse create(Long gameId, BenchmarkRecordSaveRequest request) {
        /*
         * ========================================================================
         * 步骤1：创建性能记录
         * ========================================================================
         * 目标：在指定游戏下保存一次实测性能数据。
         * 数据源：路径 gameId、BenchmarkRecordSaveRequest 与 SQLite。
         * 操作：
         * 1) 校验归属游戏与可选的场景/模板存在。
         * 2) 映射并写入 SQLite，返回持久化结果。
         */
        LOGGER.info("开始创建性能记录, gameId: {}", gameId);

        // 1.1 先保证记录始终属于已存在游戏，可选关联也需真实存在。
        requireGame(gameId);
        requireSceneIfPresent(request.sceneId());
        requireTemplateIfPresent(request.templateId());

        // 1.2 写入 SQLite 并按生成主键重新读取完整数据。
        BenchmarkRecord record = toEntity(request);
        record.setGameId(gameId);
        benchmarkRecordMapper.insert(record);
        BenchmarkRecordResponse response = toResponse(requireRecord(record.getId()));

        LOGGER.info("创建性能记录完成, id: {}, gameId: {}", record.getId(), gameId);
        return response;
    }

    public List<BenchmarkRecordResponse> listByGameId(Long gameId) {
        /*
         * ========================================================================
         * 步骤2：查询游戏性能记录
         * ========================================================================
         * 目标：只返回指定游戏拥有的记录，避免跨游戏混入。
         * 数据源：SQLite game 与 benchmark_record 表。
         * 操作：
         * 1) 校验游戏存在。
         * 2) 按 gameId 查询并转换响应。
         */
        LOGGER.info("开始查询性能记录列表, gameId: {}", gameId);

        // 2.1 将不存在游戏统一转换为 GAME_NOT_FOUND。
        requireGame(gameId);

        // 2.2 仅按所属游戏查询，使用稳定主键倒序。
        List<BenchmarkRecordResponse> responses = benchmarkRecordMapper.selectList(
                        new LambdaQueryWrapper<BenchmarkRecord>()
                                .eq(BenchmarkRecord::getGameId, gameId)
                                .orderByDesc(BenchmarkRecord::getId))
                .stream()
                .map(this::toResponse)
                .toList();

        LOGGER.info("查询性能记录列表完成, gameId: {}, count: {}", gameId, responses.size());
        return responses;
    }

    public BenchmarkRecordResponse getById(Long id) {
        /*
         * ========================================================================
         * 步骤3：读取性能记录
         * ========================================================================
         * 目标：返回单条记录详情。
         * 数据源：SQLite benchmark_record 表。
         * 操作：
         * 1) 查询记录并校验存在。
         * 2) 转换为 API 响应。
         */
        LOGGER.info("开始查询性能记录, id: {}", id);

        // 3.1 不存在记录统一抛出 RECORD_NOT_FOUND。
        BenchmarkRecordResponse response = toResponse(requireRecord(id));

        LOGGER.info("查询性能记录完成, id: {}", id);
        return response;
    }

    public BenchmarkRecordResponse update(Long id, BenchmarkRecordSaveRequest request) {
        /*
         * ========================================================================
         * 步骤4：更新性能记录
         * ========================================================================
         * 目标：修正性能指标，不改变记录所属游戏。
         * 数据源：BenchmarkRecordSaveRequest 与 SQLite benchmark_record 表。
         * 操作：
         * 1) 校验记录与可选关联存在。
         * 2) 更新字段和 UTC 更新时间后返回最终数据。
         */
        LOGGER.info("开始更新性能记录, id: {}", id);

        // 4.1 先校验目标记录存在，可选关联也需真实存在。
        requireRecord(id);
        requireSceneIfPresent(request.sceneId());
        requireTemplateIfPresent(request.templateId());

        // 4.2 写入指标字段，保留原有 gameId 归属。
        BenchmarkRecord record = toEntity(request);
        record.setUpdatedAt(Instant.now().toString());
        benchmarkRecordMapper.update(record, new LambdaUpdateWrapper<BenchmarkRecord>().eq(BenchmarkRecord::getId, id));
        BenchmarkRecordResponse response = toResponse(requireRecord(id));

        LOGGER.info("更新性能记录完成, id: {}", id);
        return response;
    }

    public void delete(Long id) {
        /*
         * ========================================================================
         * 步骤5：删除性能记录
         * ========================================================================
         * 目标：删除指定记录；关联的场景/模板仅丢失引用，不受影响。
         * 数据源：SQLite benchmark_record 表。
         * 操作：
         * 1) 校验记录存在。
         * 2) 删除数据库行。
         */
        LOGGER.info("开始删除性能记录, id: {}", id);

        // 5.1 保证不存在记录不会被当作删除成功。
        requireRecord(id);

        // 5.2 删除当前记录。
        benchmarkRecordMapper.deleteById(id);

        LOGGER.info("删除性能记录完成, id: {}", id);
    }

    public RecordCompareResponse compare(RecordCompareRequest request) {
        /*
         * ========================================================================
         * 步骤6：双记录对比
         * ========================================================================
         * 目标：对比同一游戏下两条性能记录的指标变化。
         * 数据源：SQLite benchmark_record 表。
         * 操作：
         * 1) 校验两条记录存在、非同一条、且同属一游戏。
         * 2) 计算 FPS / 帧时间变化率、功耗变化率与下降率、温度与占用差异、FPS/W 及其变化率。
         * 3) base 值为空或 0 时对应变化率返回 null；功耗为空或 0 时 FPS/W 返回 null。
         */
        LOGGER.info("开始对比性能记录, baseId: {}, targetId: {}", request.baseRecordId(), request.targetRecordId());

        // 6.1 同一条记录无法对比，统一按业务冲突拒绝。
        if (request.baseRecordId().equals(request.targetRecordId())) {
            throw new ApplicationException(ErrorCode.CONFLICT);
        }

        // 6.2 两条记录必须真实存在。
        BenchmarkRecord base = requireRecord(request.baseRecordId());
        BenchmarkRecord target = requireRecord(request.targetRecordId());

        // 6.3 仅允许对比同一游戏下的记录，避免跨游戏混比。
        if (!base.getGameId().equals(target.getGameId())) {
            throw new ApplicationException(ErrorCode.CONFLICT);
        }

        // 6.4 计算各项变化率与差异，空值/零值由工具方法自行返回 null。
        BigDecimal avgFpsChangeRate = changeRate(base.getAvgFps(), target.getAvgFps());
        BigDecimal minFpsChangeRate = changeRate(base.getMinFps(), target.getMinFps());
        BigDecimal frameTimeMsChangeRate = changeRate(base.getFrameTimeMs(), target.getFrameTimeMs());
        BigDecimal gpuPowerChangeRate = changeRate(base.getGpuPowerWatt(), target.getGpuPowerWatt());
        BigDecimal gpuPowerDropRate = gpuPowerChangeRate == null
                ? null : gpuPowerChangeRate.negate().setScale(2, RoundingMode.HALF_UP);
        BigDecimal gpuTempDiff = diff(base.getGpuTempCelsius(), target.getGpuTempCelsius());
        BigDecimal cpuTempDiff = diff(base.getCpuTempCelsius(), target.getCpuTempCelsius());
        BigDecimal cpuUsageDiff = diff(base.getCpuUsagePercent(), target.getCpuUsagePercent());
        BigDecimal baseFpsPerWatt = fpsPerWatt(base.getAvgFps(), base.getGpuPowerWatt());
        BigDecimal targetFpsPerWatt = fpsPerWatt(target.getAvgFps(), target.getGpuPowerWatt());
        BigDecimal fpsPerWattChangeRate = changeRate(baseFpsPerWatt, targetFpsPerWatt);

        RecordCompareResponse response = new RecordCompareResponse(
                toResponse(base), toResponse(target),
                avgFpsChangeRate, minFpsChangeRate, frameTimeMsChangeRate,
                gpuPowerChangeRate, gpuPowerDropRate,
                gpuTempDiff, cpuTempDiff, cpuUsageDiff,
                baseFpsPerWatt, targetFpsPerWatt, fpsPerWattChangeRate);

        LOGGER.info("对比性能记录完成, baseId: {}, targetId: {}", request.baseRecordId(), request.targetRecordId());
        return response;
    }

    private BigDecimal changeRate(BigDecimal base, BigDecimal target) {
        if (base == null || target == null || base.signum() == 0) {
            return null;
        }
        return target.subtract(base)
                .divide(base, 4, RoundingMode.HALF_UP)
                .multiply(HUNDRED)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal diff(BigDecimal base, BigDecimal target) {
        if (base == null || target == null) {
            return null;
        }
        return target.subtract(base).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal fpsPerWatt(BigDecimal fps, BigDecimal power) {
        if (fps == null || power == null || power.signum() == 0) {
            return null;
        }
        return fps.divide(power, 2, RoundingMode.HALF_UP);
    }

    private Game requireGame(Long gameId) {
        Game game = gameMapper.selectById(gameId);
        if (game == null) {
            throw new ApplicationException(ErrorCode.GAME_NOT_FOUND);
        }
        return game;
    }

    private BenchmarkRecord requireRecord(Long id) {
        BenchmarkRecord record = benchmarkRecordMapper.selectById(id);
        if (record == null) {
            throw new ApplicationException(ErrorCode.RECORD_NOT_FOUND);
        }
        return record;
    }

    private void requireSceneIfPresent(Long sceneId) {
        if (sceneId == null) {
            return;
        }
        if (testSceneMapper.selectById(sceneId) == null) {
            throw new ApplicationException(ErrorCode.SCENE_NOT_FOUND);
        }
    }

    private void requireTemplateIfPresent(Long templateId) {
        if (templateId == null) {
            return;
        }
        if (configTemplateMapper.selectById(templateId) == null) {
            throw new ApplicationException(ErrorCode.TEMPLATE_NOT_FOUND);
        }
    }

    private BenchmarkRecord toEntity(BenchmarkRecordSaveRequest request) {
        BenchmarkRecord record = new BenchmarkRecord();
        record.setSceneId(request.sceneId());
        record.setTemplateId(request.templateId());
        record.setRecordedAt(normalizeNullable(request.recordedAt()));
        record.setAvgFps(request.avgFps());
        record.setMinFps(request.minFps());
        record.setGpuTempCelsius(request.gpuTempCelsius());
        record.setCpuTempCelsius(request.cpuTempCelsius());
        record.setGpuPowerWatt(request.gpuPowerWatt());
        record.setCpuUsagePercent(request.cpuUsagePercent());
        record.setFrameTimeMs(request.frameTimeMs());
        record.setNotes(normalizeNullable(request.notes()));
        return record;
    }

    private BenchmarkRecordResponse toResponse(BenchmarkRecord record) {
        return new BenchmarkRecordResponse(record.getId(), record.getGameId(), record.getSceneId(),
                record.getTemplateId(), record.getRecordedAt(), record.getAvgFps(), record.getMinFps(),
                record.getGpuTempCelsius(), record.getCpuTempCelsius(), record.getGpuPowerWatt(),
                record.getCpuUsagePercent(), record.getFrameTimeMs(), record.getNotes(),
                record.getCreatedAt(), record.getUpdatedAt());
    }

    private String normalizeNullable(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
