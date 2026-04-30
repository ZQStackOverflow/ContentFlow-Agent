package com.example.parser;

import com.example.model.ServiceLog;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 日志过滤器
 */
public class LogFilter {

    /**
     * 根据Trace ID过滤日志
     */
    public List<ServiceLog> filterByTraceId(List<ServiceLog> logs, String traceId) {
        return logs.stream()
                  .filter(log -> traceId.equals(log.getTraceId()))
                  .collect(Collectors.toList());
    }

    /**
     * 根据服务名过滤日志
     */
    public List<ServiceLog> filterByServiceName(List<ServiceLog> logs, String serviceName) {
        return logs.stream()
                  .filter(log -> serviceName.equals(log.getServiceName()))
                  .collect(Collectors.toList());
    }

    /**
     * 根据时间范围过滤日志
     */
    public List<ServiceLog> filterByTimeRange(List<ServiceLog> logs, long startTimestamp, long endTimestamp) {
        return logs.stream()
                  .filter(log -> {
                      long logTimestamp = log.getInstant().toEpochMilli();
                      return logTimestamp >= startTimestamp && logTimestamp <= endTimestamp;
                  })
                  .collect(Collectors.toList());
    }

    /**
     * 过滤出错误日志
     */
    public List<ServiceLog> filterErrorLogs(List<ServiceLog> logs) {
        return logs.stream()
                  .filter(ServiceLog::isError)
                  .collect(Collectors.toList());
    }

    /**
     * 按服务分组
     */
    public java.util.Map<String, List<ServiceLog>> groupByService(List<ServiceLog> logs) {
        return logs.stream()
                  .collect(Collectors.groupingBy(ServiceLog::getServiceName));
    }

    /**
     * 按时间排序
     */
    public List<ServiceLog> sortByTimestamp(List<ServiceLog> logs) {
        return logs.stream()
                  .sorted((a, b) -> a.getInstant().compareTo(b.getInstant()))
                  .collect(Collectors.toList());
    }
}