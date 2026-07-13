package com.gamebench.tracker.game.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.gamebench.tracker.common.error.ErrorCode;
import com.gamebench.tracker.common.exception.ApplicationException;
import com.gamebench.tracker.game.dto.TestSceneSaveRequest;
import com.gamebench.tracker.game.entity.Game;
import com.gamebench.tracker.game.entity.TestScene;
import com.gamebench.tracker.game.mapper.GameMapper;
import com.gamebench.tracker.game.mapper.TestSceneMapper;
import com.gamebench.tracker.game.vo.TestSceneResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class TestSceneService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestSceneService.class);

    private final GameMapper gameMapper;
    private final TestSceneMapper testSceneMapper;

    public TestSceneService(GameMapper gameMapper, TestSceneMapper testSceneMapper) {
        this.gameMapper = gameMapper;
        this.testSceneMapper = testSceneMapper;
    }

    public TestSceneResponse create(Long gameId, TestSceneSaveRequest request) {
        /*
         * ========================================================================
         * 步骤1：创建测试场景
         * ========================================================================
         * 目标：在指定游戏下保存可复用的测试场景。
         * 数据源：路径 gameId、TestSceneSaveRequest 与 SQLite。
         * 操作：
         * 1) 校验目标游戏存在。
         * 2) 规范化字段，插入场景并返回持久化结果。
         */
        LOGGER.info("开始创建测试场景, gameId: {}", gameId);

        // 1.1 先保证场景始终属于已存在游戏。
        requireGame(gameId);

        // 1.2 规范化输入并写入 SQLite。
        TestScene scene = new TestScene();
        scene.setGameId(gameId);
        scene.setName(normalizeRequired(request.name()));
        scene.setMethod(normalizeRequired(request.method()));
        scene.setDurationSeconds(request.durationSeconds());
        scene.setRemark(normalizeNullable(request.remark()));
        testSceneMapper.insert(scene);
        TestSceneResponse response = toResponse(requireScene(scene.getId()));

        LOGGER.info("创建测试场景完成, id: {}, gameId: {}", scene.getId(), gameId);
        return response;
    }

    public List<TestSceneResponse> listByGameId(Long gameId) {
        /*
         * ========================================================================
         * 步骤2：查询游戏场景
         * ========================================================================
         * 目标：返回指定游戏的全部场景，避免跨游戏混入。
         * 数据源：SQLite game 与 test_scene 表。
         * 操作：
         * 1) 校验游戏存在。
         * 2) 按 gameId 查询并转换响应。
         */
        LOGGER.info("开始查询测试场景列表, gameId: {}", gameId);

        // 2.1 将不存在游戏统一转换为 GAME_NOT_FOUND。
        requireGame(gameId);

        // 2.2 仅按所属游戏查询，并使用稳定主键倒序。
        List<TestSceneResponse> responses = testSceneMapper.selectList(
                        new LambdaQueryWrapper<TestScene>()
                                .eq(TestScene::getGameId, gameId)
                                .orderByDesc(TestScene::getId))
                .stream()
                .map(this::toResponse)
                .toList();

        LOGGER.info("查询测试场景列表完成, gameId: {}, count: {}", gameId, responses.size());
        return responses;
    }

    public TestSceneResponse getById(Long id) {
        /*
         * ========================================================================
         * 步骤3：读取测试场景
         * ========================================================================
         * 目标：返回单个场景详情。
         * 数据源：SQLite test_scene 表。
         * 操作：
         * 1) 查询场景并校验存在。
         * 2) 转换为 API 响应对象。
         */
        LOGGER.info("开始查询测试场景, id: {}", id);

        // 3.1 不存在场景统一抛出 SCENE_NOT_FOUND。
        TestSceneResponse response = toResponse(requireScene(id));

        LOGGER.info("查询测试场景完成, id: {}", id);
        return response;
    }

    public TestSceneResponse update(Long id, TestSceneSaveRequest request) {
        /*
         * ========================================================================
         * 步骤4：更新测试场景
         * ========================================================================
         * 目标：修正场景名称、方法、时长与备注，不改变所属游戏。
         * 数据源：TestSceneSaveRequest 与 SQLite test_scene 表。
         * 操作：
         * 1) 校验目标场景存在。
         * 2) 更新规范化字段和 UTC 更新时间。
         */
        LOGGER.info("开始更新测试场景, id: {}", id);

        // 4.1 先检查存在性，避免静默更新零行。
        requireScene(id);

        // 4.2 写入新字段，保留原 gameId 归属。
        TestScene scene = new TestScene();
        scene.setName(normalizeRequired(request.name()));
        scene.setMethod(normalizeRequired(request.method()));
        scene.setDurationSeconds(request.durationSeconds());
        scene.setRemark(normalizeNullable(request.remark()));
        scene.setUpdatedAt(Instant.now().toString());
        testSceneMapper.update(scene, new LambdaUpdateWrapper<TestScene>().eq(TestScene::getId, id));
        TestSceneResponse response = toResponse(requireScene(id));

        LOGGER.info("更新测试场景完成, id: {}", id);
        return response;
    }

    public void delete(Long id) {
        /*
         * ========================================================================
         * 步骤5：删除测试场景
         * ========================================================================
         * 目标：删除指定场景；后续记录表会通过外键将 scene_id 置空。
         * 数据源：SQLite test_scene 表。
         * 操作：
         * 1) 校验目标场景存在。
         * 2) 删除数据库行。
         */
        LOGGER.info("开始删除测试场景, id: {}", id);

        // 5.1 先保证删除语义不会把不存在场景当作成功。
        requireScene(id);

        // 5.2 删除指定场景。
        testSceneMapper.deleteById(id);

        LOGGER.info("删除测试场景完成, id: {}", id);
    }

    private Game requireGame(Long gameId) {
        Game game = gameMapper.selectById(gameId);
        if (game == null) {
            throw new ApplicationException(ErrorCode.GAME_NOT_FOUND);
        }
        return game;
    }

    private TestScene requireScene(Long id) {
        TestScene scene = testSceneMapper.selectById(id);
        if (scene == null) {
            throw new ApplicationException(ErrorCode.SCENE_NOT_FOUND);
        }
        return scene;
    }

    private TestSceneResponse toResponse(TestScene scene) {
        return new TestSceneResponse(scene.getId(), scene.getGameId(), scene.getName(), scene.getMethod(),
                scene.getDurationSeconds(), scene.getRemark(), scene.getCreatedAt(), scene.getUpdatedAt());
    }

    private String normalizeRequired(String value) {
        return value.trim();
    }

    private String normalizeNullable(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
