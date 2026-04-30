package com.example.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Trace信息模型
 */
public class TraceInfo {
    private String traceId;
    private long startTime;
    private long endTime;
    private List<ServiceLog> serviceLogs;
    private List<Span> spans;

    public TraceInfo() {
        this.serviceLogs = new ArrayList<>();
        this.spans = new ArrayList<>();
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public List<ServiceLog> getServiceLogs() {
        return serviceLogs;
    }

    public void setServiceLogs(List<ServiceLog> serviceLogs) {
        this.serviceLogs = serviceLogs;
    }

    public List<Span> getSpans() {
        return spans;
    }

    public void setSpans(List<Span> spans) {
        this.spans = spans;
    }

    public void addServiceLog(ServiceLog log) {
        this.serviceLogs.add(log);
    }

    public void addSpan(Span span) {
        this.spans.add(span);
    }
}