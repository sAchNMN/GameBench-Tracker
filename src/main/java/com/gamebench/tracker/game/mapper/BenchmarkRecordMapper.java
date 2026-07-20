package com.gamebench.tracker.game.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gamebench.tracker.game.entity.BenchmarkRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BenchmarkRecordMapper extends BaseMapper<BenchmarkRecord> {
}
