package com.example.model;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * 服务日志模型
 */
public class ServiceLog {
    private String traceId;
    private String spanId;
    private String parentSpanId;
    private String serviceName;
    private String timestamp;
    private Instant instant;
    private String level;
    private String threadName;
    private String loggerName;
    private String message;
    private String exception;
    private String stackTrace;
    private boolean isError;
    private boolean isCascadeError;

    public ServiceLog() {
        this.instant = Instant.now();
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getSpanId() {
        return spanId;
    }

    public void setSpanId(String spanId) {
        this.spanId = spanId;
    }

    public String getParentSpanId() {
        return parentSpanId;
    }

    public void setParentSpanId(String parentSpanId) {
        this.parentSpanId = parentSpanId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
        this.instant = parseTimestamp(timestamp);
    }

    private Instant parseTimestamp(String timestamp) {
        try {
            // 尝试常见的日志时间格式
            DateTimeFormatter[] formatters = {
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss.SSS"),
                DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")
            };

            for (DateTimeFormatter formatter : formatters) {
                try {
                    LocalDateTime localDateTime = LocalDateTime.parse(timestamp, formatter);
                    return localDateTime.atZone(ZoneId.systemDefault()).toInstant();
                } catch (Exception e) {
                    // 继续尝试下一个格式
                }
            }
        } catch (Exception e) {
            // 如果解析失败，返回当前时间
        }
        return Instant.now();
    }

    public Instant getInstant() {
        return instant;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
        this.isError = "ERROR".equalsIgnoreCase(level) || "FATAL".equalsIgnoreCase(level);
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public String getLoggerName() {
        return loggerName;
    }

    public void setLoggerName(String loggerName) {
        this.loggerName = loggerName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
        if (exception != null) {
            this.isError = true;
        }
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }

    public boolean isError() {
        return isError;
    }

    public boolean isCascadeError() {
        return isCascadeError;
    }

    public void setCascadeError(boolean cascadeError) {
        this.isCascadeError = cascadeError;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s - %s: %s",
            timestamp, serviceName, level,
            exception != null ? exception : message);
    }
}