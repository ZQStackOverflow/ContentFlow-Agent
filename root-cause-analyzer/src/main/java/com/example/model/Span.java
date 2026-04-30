package com.example.model;

import java.time.Instant;

/**
 * Span模型 - 用于追踪请求的执行路径
 */
public class Span {
    private String traceId;
    private String spanId;
    private String parentSpanId;
    private String serviceName;
    private Instant startTime;
    private Instant endTime;
    private String operation;
    private boolean isError;
    private String errorMessage;
    private long duration;

    public Span() {
    }

    public Span(String traceId, String spanId, String parentSpanId, String serviceName) {
        this.traceId = traceId;
        this.spanId = spanId;
        this.parentSpanId = parentSpanId;
        this.serviceName = serviceName;
        this.startTime = Instant.now();
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

    public Instant getStartTime() {
        return startTime;
    }

    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public void setEndTime(Instant endTime) {
        this.endTime = endTime;
        if (startTime != null) {
            this.duration = endTime.toEpochMilli() - startTime.toEpochMilli();
        }
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public boolean isError() {
        return isError;
    }

    public void setError(boolean error) {
        isError = error;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        if (errorMessage != null) {
            this.isError = true;
        }
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void complete() {
        this.endTime = Instant.now();
        if (startTime != null) {
            this.duration = endTime.toEpochMilli() - startTime.toEpochMilli();
        }
    }
}