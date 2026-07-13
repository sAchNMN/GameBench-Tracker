package com.gamebench.tracker.game.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;

@TableName("config_template")
public class ConfigTemplate {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long gameId;
    private String name;
    private String resolution;
    private String graphicsPreset;
    private String upscalingTech;
    private String upscalingQuality;
    private Boolean vsyncEnabled;
    private Boolean frameGenerationEnabled;
    private BigDecimal gpuCoreClockMhz;
    private BigDecimal gpuVoltageMv;
    private BigDecimal gpuMemoryClockMhz;
    private BigDecimal gpuPowerLimitPercent;
    private String driverVersion;
    private String customDescription;
    private String createdAt;
    private String updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getGameId() { return gameId; }
    public void setGameId(Long gameId) { this.gameId = gameId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getResolution() { return resolution; }
    public void setResolution(String resolution) { this.resolution = resolution; }
    public String getGraphicsPreset() { return graphicsPreset; }
    public void setGraphicsPreset(String graphicsPreset) { this.graphicsPreset = graphicsPreset; }
    public String getUpscalingTech() { return upscalingTech; }
    public void setUpscalingTech(String upscalingTech) { this.upscalingTech = upscalingTech; }
    public String getUpscalingQuality() { return upscalingQuality; }
    public void setUpscalingQuality(String upscalingQuality) { this.upscalingQuality = upscalingQuality; }
    public Boolean getVsyncEnabled() { return vsyncEnabled; }
    public void setVsyncEnabled(Boolean vsyncEnabled) { this.vsyncEnabled = vsyncEnabled; }
    public Boolean getFrameGenerationEnabled() { return frameGenerationEnabled; }
    public void setFrameGenerationEnabled(Boolean frameGenerationEnabled) { this.frameGenerationEnabled = frameGenerationEnabled; }
    public BigDecimal getGpuCoreClockMhz() { return gpuCoreClockMhz; }
    public void setGpuCoreClockMhz(BigDecimal gpuCoreClockMhz) { this.gpuCoreClockMhz = gpuCoreClockMhz; }
    public BigDecimal getGpuVoltageMv() { return gpuVoltageMv; }
    public void setGpuVoltageMv(BigDecimal gpuVoltageMv) { this.gpuVoltageMv = gpuVoltageMv; }
    public BigDecimal getGpuMemoryClockMhz() { return gpuMemoryClockMhz; }
    public void setGpuMemoryClockMhz(BigDecimal gpuMemoryClockMhz) { this.gpuMemoryClockMhz = gpuMemoryClockMhz; }
    public BigDecimal getGpuPowerLimitPercent() { return gpuPowerLimitPercent; }
    public void setGpuPowerLimitPercent(BigDecimal gpuPowerLimitPercent) { this.gpuPowerLimitPercent = gpuPowerLimitPercent; }
    public String getDriverVersion() { return driverVersion; }
    public void setDriverVersion(String driverVersion) { this.driverVersion = driverVersion; }
    public String getCustomDescription() { return customDescription; }
    public void setCustomDescription(String customDescription) { this.customDescription = customDescription; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
