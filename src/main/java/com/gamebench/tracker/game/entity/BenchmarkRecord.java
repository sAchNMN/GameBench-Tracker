package com.gamebench.tracker.game.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;

@TableName("benchmark_record")
public class BenchmarkRecord {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long gameId;
    private Long sceneId;
    private Long templateId;
    private String recordedAt;
    private BigDecimal avgFps;
    private BigDecimal minFps;
    private BigDecimal gpuTempCelsius;
    private BigDecimal cpuTempCelsius;
    private BigDecimal gpuPowerWatt;
    private BigDecimal cpuUsagePercent;
    private BigDecimal frameTimeMs;
    private String notes;
    private String createdAt;
    private String updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getGameId() { return gameId; }
    public void setGameId(Long gameId) { this.gameId = gameId; }
    public Long getSceneId() { return sceneId; }
    public void setSceneId(Long sceneId) { this.sceneId = sceneId; }
    public Long getTemplateId() { return templateId; }
    public void setTemplateId(Long templateId) { this.templateId = templateId; }
    public String getRecordedAt() { return recordedAt; }
    public void setRecordedAt(String recordedAt) { this.recordedAt = recordedAt; }
    public BigDecimal getAvgFps() { return avgFps; }
    public void setAvgFps(BigDecimal avgFps) { this.avgFps = avgFps; }
    public BigDecimal getMinFps() { return minFps; }
    public void setMinFps(BigDecimal minFps) { this.minFps = minFps; }
    public BigDecimal getGpuTempCelsius() { return gpuTempCelsius; }
    public void setGpuTempCelsius(BigDecimal gpuTempCelsius) { this.gpuTempCelsius = gpuTempCelsius; }
    public BigDecimal getCpuTempCelsius() { return cpuTempCelsius; }
    public void setCpuTempCelsius(BigDecimal cpuTempCelsius) { this.cpuTempCelsius = cpuTempCelsius; }
    public BigDecimal getGpuPowerWatt() { return gpuPowerWatt; }
    public void setGpuPowerWatt(BigDecimal gpuPowerWatt) { this.gpuPowerWatt = gpuPowerWatt; }
    public BigDecimal getCpuUsagePercent() { return cpuUsagePercent; }
    public void setCpuUsagePercent(BigDecimal cpuUsagePercent) { this.cpuUsagePercent = cpuUsagePercent; }
    public BigDecimal getFrameTimeMs() { return frameTimeMs; }
    public void setFrameTimeMs(BigDecimal frameTimeMs) { this.frameTimeMs = frameTimeMs; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
