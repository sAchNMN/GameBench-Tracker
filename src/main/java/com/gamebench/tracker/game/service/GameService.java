package com.gamebench.tracker.game.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.gamebench.tracker.common.error.ErrorCode;
import com.gamebench.tracker.common.exception.ApplicationException;
import com.gamebench.tracker.game.dto.GameSaveRequest;
import com.gamebench.tracker.game.entity.Game;
import com.gamebench.tracker.game.mapper.GameMapper;
import com.gamebench.tracker.game.vo.GamePageResponse;
import com.gamebench.tracker.game.vo.GameResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class GameService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GameService.class);

    private final GameMapper gameMapper;

    public GameService(GameMapper gameMapper) {
        this.gameMapper = gameMapper;
    }

    public GameResponse create(GameSaveRequest request) {
        /*
         * ========================================================================
         * 步骤1：创建游戏
         * ========================================================================
         * 目标：保存用户维护的游戏资料。
         * 数据源：GameSaveRequest 与 SQLite game 表。
         * 操作：
         * 1) 规范化名称、平台和备注。
         * 2) 插入数据并返回持久化后的响应。
         */
        LOGGER.info("开始创建游戏...");

        // 1.1 规范化请求字段，平台空值统一保存为空字符串。
        Game game = new Game();
        game.setName(normalizeRequired(request.name()));
        game.setPlatform(normalizeOptional(request.platform()));
        game.setRemark(normalizeNullable(request.remark()));

        // 1.2 写入 SQLite，并按生成主键重新读取完整数据。
        gameMapper.insert(game);
        GameResponse response = toResponse(requireGame(game.getId()));

        LOGGER.info("创建游戏完成, id: {}", game.getId());
        return response;
    }

    public GamePageResponse list(String keyword, int page, int size) {
        /*
         * ========================================================================
         * 步骤2：查询游戏列表
         * ========================================================================
         * 目标：按名称关键字返回稳定分页结果。
         * 数据源：SQLite game 表。
         * 操作：
         * 1) 按名称筛选并按主键倒序查询。
         * 2) 在结果集上计算当前页并转换为响应对象。
         */
        LOGGER.info("开始查询游戏列表, keyword: {}, page: {}, size: {}", keyword, page, size);

        // 2.1 构建可选关键字条件，避免空字符串触发无意义筛选。
        LambdaQueryWrapper<Game> wrapper = new LambdaQueryWrapper<Game>()
                .like(keyword != null && !keyword.isBlank(), Game::getName, keyword == null ? null : keyword.trim())
                .orderByDesc(Game::getId);
        List<Game> games = gameMapper.selectList(wrapper);

        // 2.2 截取当前页并保留全量计数。
        int fromIndex = Math.min((page - 1) * size, games.size());
        int toIndex = Math.min(fromIndex + size, games.size());
        List<GameResponse> items = games.subList(fromIndex, toIndex).stream().map(this::toResponse).toList();
        GamePageResponse response = new GamePageResponse(items, games.size(), page, size);

        LOGGER.info("查询游戏列表完成, total: {}, currentPageCount: {}", games.size(), items.size());
        return response;
    }

    public GameResponse getById(Long id) {
        /*
         * ========================================================================
         * 步骤3：读取游戏详情
         * ========================================================================
         * 目标：返回指定游戏，缺失时统一抛出业务错误码。
         * 数据源：SQLite game 表。
         * 操作：
         * 1) 查询并校验游戏存在。
         * 2) 转换为 API 响应对象。
         */
        LOGGER.info("开始查询游戏, id: {}", id);

        // 3.1 查询目标游戏并隔离不存在分支。
        GameResponse response = toResponse(requireGame(id));

        LOGGER.info("查询游戏完成, id: {}", id);
        return response;
    }

    public GameResponse update(Long id, GameSaveRequest request) {
        /*
         * ========================================================================
         * 步骤4：更新游戏
         * ========================================================================
         * 目标：修正名称、平台和备注，并刷新 UTC 更新时间。
         * 数据源：GameSaveRequest 与 SQLite game 表。
         * 操作：
         * 1) 确认目标记录存在。
         * 2) 写入规范化字段和更新时间，再读取最终数据。
         */
        LOGGER.info("开始更新游戏, id: {}", id);

        // 4.1 先检查存在性，确保不存在时返回 GAME_NOT_FOUND。
        requireGame(id);

        // 4.2 写入新字段和 UTC 更新时间。
        Game game = new Game();
        game.setName(normalizeRequired(request.name()));
        game.setPlatform(normalizeOptional(request.platform()));
        game.setRemark(normalizeNullable(request.remark()));
        game.setUpdatedAt(Instant.now().toString());
        gameMapper.update(game, new LambdaUpdateWrapper<Game>().eq(Game::getId, id));
        GameResponse response = toResponse(requireGame(id));

        LOGGER.info("更新游戏完成, id: {}", id);
        return response;
    }

    public void delete(Long id) {
        /*
         * ========================================================================
         * 步骤5：删除游戏
         * ========================================================================
         * 目标：删除指定游戏；后续表创建后由外键约束处理关联数据。
         * 数据源：SQLite game 表。
         * 操作：
         * 1) 确认目标记录存在。
         * 2) 删除记录并交由 Controller 返回统一成功响应。
         */
        LOGGER.info("开始删除游戏, id: {}", id);

        // 5.1 校验目标存在，避免把不存在记录当作删除成功。
        requireGame(id);

        // 5.2 删除数据库行。
        gameMapper.deleteById(id);

        LOGGER.info("删除游戏完成, id: {}", id);
    }

    private Game requireGame(Long id) {
        Game game = gameMapper.selectById(id);
        if (game == null) {
            throw new ApplicationException(ErrorCode.GAME_NOT_FOUND);
        }
        return game;
    }

    private GameResponse toResponse(Game game) {
        return new GameResponse(game.getId(), game.getName(), game.getPlatform(), game.getRemark(),
                game.getCreatedAt(), game.getUpdatedAt());
    }

    private String normalizeRequired(String value) {
        return value.trim();
    }

    private String normalizeOptional(String value) {
        return value == null ? "" : value.trim();
    }

    private String normalizeNullable(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
