package com.gamebench.tracker.common.error;

/**
 * 基础通用错误码。
 */
public enum ErrorCode {
    INVALID_REQUEST("INVALID_REQUEST", "请求无效"),
    VALIDATION_FAILED("VALIDATION_FAILED", "参数校验失败"),
    RESOURCE_NOT_FOUND("RESOURCE_NOT_FOUND", "资源不存在"),
    GAME_NOT_FOUND("GAME_NOT_FOUND", "游戏不存在"),
    SCENE_NOT_FOUND("SCENE_NOT_FOUND", "测试场景不存在"),
    TEMPLATE_NOT_FOUND("TEMPLATE_NOT_FOUND", "配置模板不存在"),
    RECORD_NOT_FOUND("RECORD_NOT_FOUND", "性能记录不存在"),
    CONFLICT("CONFLICT", "数据冲突"),
    INTERNAL_ERROR("INTERNAL_ERROR", "服务器内部错误");

    private final String code;
    private final String defaultMessage;

    ErrorCode(String code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    public String code() {
        return code;
    }

    public String defaultMessage() {
        return defaultMessage;
    }
}
