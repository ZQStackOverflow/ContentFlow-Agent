package com.example.analyzer;

import com.example.model.*;
import com.example.parser.SourceCodeParser;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Trace分析器 - 核心根因分析逻辑
 */
public class TraceAnalyzer {

    private SourceCodeParser sourceCodeParser;

    public TraceAnalyzer(SourceCodeParser sourceCodeParser) {
        this.sourceCodeParser = sourceCodeParser;
    }

    /**
     * 执行根因分析
     */
    public RootCauseAnalysisResult analyze(List<ServiceLog> logs) {
        // 1. 按时间排序日志
        List<ServiceLog> sortedLogs = logs.stream()
                                      .sorted(Comparator.comparing(ServiceLog::getInstant))
                                      .collect(Collectors.toList());

        // 2. 构建Trace信息
        TraceInfo traceInfo = buildTraceInfo(sortedLogs);

        // 3. 查找第一个错误点
        ServiceLog firstError = findFirstError(sortedLogs);

        if (firstError == null) {
            // 没有找到错误，创建一个未找到错误的报告
            return createNoErrorReport(sortedLogs);
        }

        // 4. 确定错误类型
        RootCauseAnalysisResult.ErrorType errorType = determineErrorType(firstError);

        // 5. 查找相关的源代码位置
        String codeLocation = findCodeLocation(firstError, errorType);

        // 6. 检查级联错误
        List<ServiceLog> cascadeErrors = findCascadeErrors(sortedLogs, firstError);

        // 7. 生成分析报告
        RootCauseAnalysisResult result = new RootCauseAnalysisResult();
        result.setFaultService(firstError.getServiceName());
        result.setRootCauseSummary(generateRootCauseSummary(firstError, errorType));
        result.setDetailedAnalysis(generateDetailedAnalysis(sortedLogs, firstError, cascadeErrors));
        result.setCodeLocation(codeLocation);
        result.setSuggestedFixType(determineFixType(errorType));
        result.setRelatedLogEntries(extractRelatedLogs(sortedLogs, firstError));
        result.setAffectedServices(findAffectedServices(sortedLogs));
        result.setErrorType(errorType);
        result.setSeverityLevel(determineSeverityLevel(firstError, cascadeErrors));

        return result;
    }

    /**
     * 构建Trace信息
     */
    private TraceInfo buildTraceInfo(List<ServiceLog> logs) {
        TraceInfo traceInfo = new TraceInfo();

        if (!logs.isEmpty()) {
            traceInfo.setTraceId(logs.get(0).getTraceId());
            traceInfo.setStartTime(logs.get(0).getInstant().toEpochMilli());
            traceInfo.setEndTime(logs.get(logs.size() - 1).getInstant().toEpochMilli());
        }

        // 构建spans
        Map<String, Span> spans = new HashMap<>();
        for (ServiceLog log : logs) {
            String spanId = log.getSpanId();
            if (spanId != null && !spanId.isEmpty() && !spans.containsKey(spanId)) {
                Span span = new Span(
                    log.getTraceId(),
                    spanId,
                    log.getParentSpanId(),
                    log.getServiceName()
                );
                span.setStartTime(log.getInstant());
                spans.put(spanId, span);
            }
        }

        traceInfo.setSpans(new ArrayList<>(spans.values()));
        return traceInfo;
    }

    /**
     * 查找第一个错误
     */
    private ServiceLog findFirstError(List<ServiceLog> logs) {
        return logs.stream()
                  .filter(ServiceLog::isError)
                  .findFirst()
                  .orElse(null);
    }

    /**
     * 确定错误类型
     */
    private RootCauseAnalysisResult.ErrorType determineErrorType(ServiceLog errorLog) {
        if (errorLog.getException() != null) {
            String exception = errorLog.getException();

            if (exception.contains("NullPointerException")) {
                return RootCauseAnalysisResult.ErrorType.NULL_POINTER_EXCEPTION;
            } else if (exception.contains("ArrayIndexOutOfBoundsException")) {
                return RootCauseAnalysisResult.ErrorType.ARRAY_INDEX_OUT_OF_BOUNDS;
            } else if (exception.contains("Deadlock")) {
                return RootCauseAnalysisResult.ErrorType.DATABASE_DEADLOCK;
            } else if (exception.contains("Timeout") || exception.contains("TimeoutException")) {
                return RootCauseAnalysisResult.ErrorType.UPSTREAM_TIMEOUT;
            }
        }

        // 根据错误消息判断
        String message = errorLog.getMessage();
        if (message.contains("database") || message.contains("DB")) {
            return RootCauseAnalysisResult.ErrorType.DATABASE_DEADLOCK;
        } else if (message.contains("config") || message.contains("configuration")) {
            return RootCauseAnalysisResult.ErrorType.CONFIGURATION_ERROR;
        }

        return RootCauseAnalysisResult.ErrorType.BUSINESS_LOGIC_BUG;
    }

    /**
     * 查找代码位置
     */
    private String findCodeLocation(ServiceLog errorLog, RootCauseAnalysisResult.ErrorType errorType) {
        if (sourceCodeParser == null) {
            return "未提供源代码目录，无法定位代码位置";
        }

        String serviceName = errorLog.getServiceName();

        // 根据错误类型搜索关键词
        String[] keywords = getKeywordsForErrorType(errorType);

        for (String keyword : keywords) {
            List<SourceCodeLine> lines = sourceCodeParser.searchCode(serviceName, keyword);
            if (!lines.isEmpty()) {
                SourceCodeLine line = lines.get(0);
                return String.format("%s:%d", line.getFilePath(), line.getLineNumber());
            }
        }

        return "未找到相关代码";
    }

    /**
     * 根据错误类型获取搜索关键词
     */
    private String[] getKeywordsForErrorType(RootCauseAnalysisResult.ErrorType errorType) {
        switch (errorType) {
            case NULL_POINTER_EXCEPTION:
                return new String[]{"NullPointerException", "Objects.requireNonNull", ".get(", ".find("};
            case ARRAY_INDEX_OUT_OF_BOUNDS:
                return new String[]{"ArrayIndexOutOfBoundsException", "length", "size", "[", "]"};
            case DATABASE_DEADLOCK:
                return new String[]{"@Transactional", "SELECT FOR UPDATE", "deadlock", "lock"};
            case UPSTREAM_TIMEOUT:
                return new String[]{"timeout", "@Timeout", "FeignClient", "HttpClient"};
            case CONFIGURATION_ERROR:
                return new String[]{"@Value", "application.yml", "application.properties", "config"};
            default:
                return new String[]{"Exception", "Error", "throw"};
        }
    }

    /**
     * 查找级联错误
     */
    private List<ServiceLog> findCascadeErrors(List<ServiceLog> logs, ServiceLog firstError) {
        Instant errorTime = firstError.getInstant();
        long timeWindow = Duration.ofSeconds(30).toMillis(); // 30秒时间窗口

        return logs.stream()
                  .filter(log -> !log.equals(firstError) && log.isError())
                  .filter(log -> {
                      long timeDiff = Math.abs(log.getInstant().toEpochMilli() - errorTime.toEpochMilli());
                      return timeDiff <= timeWindow;
                  })
                  .collect(Collectors.toList());
    }

    /**
     * 生成根因总结
     */
    private String generateRootCauseSummary(ServiceLog firstError, RootCauseAnalysisResult.ErrorType errorType) {
        String serviceName = firstError.getServiceName();
        String exception = firstError.getException() != null ?
            firstError.getException() : "Error occurred in " + serviceName;

        return String.format("%s 在服务 %s 中发生%s", exception, serviceName, getErrorTypeDescription(errorType));
    }

    /**
     * 获取错误类型描述
     */
    private String getErrorTypeDescription(RootCauseAnalysisResult.ErrorType errorType) {
        switch (errorType) {
            case NULL_POINTER_EXCEPTION: return "空指针异常";
            case ARRAY_INDEX_OUT_OF_BOUNDS: return "数组越界异常";
            case DATABASE_DEADLOCK: return "数据库死锁";
            case UPSTREAM_TIMEOUT: return "上游依赖超时";
            case CONFIGURATION_ERROR: return "配置错误";
            case RESOURCE_EXHAUSTION: return "资源耗尽";
            default: return "业务逻辑错误";
        }
    }

    /**
     * 生成详细分析
     */
    private String generateDetailedAnalysis(List<ServiceLog> logs, ServiceLog firstError, List<ServiceLog> cascadeErrors) {
        StringBuilder analysis = new StringBuilder();

        analysis.append("【根因分析过程】\n");
        analysis.append("1. 追踪Trace ID: ").append(firstError.getTraceId()).append("\n");
        analysis.append("2. 第一个错误发生在: ").append(firstError.getTimestamp()).append("\n");
        analysis.append("   服务: ").append(firstError.getServiceName()).append("\n");
        analysis.append("   线程: ").append(firstError.getThreadName()).append("\n");

        if (firstError.getException() != null) {
            analysis.append("   异常: ").append(firstError.getException()).append("\n");
        }

        analysis.append("3. 分析报错前的关键日志:\n");

        // 查找错误发生前1秒内的日志
        Instant errorTime = firstError.getInstant();
        List<ServiceLog> precedingLogs = logs.stream()
            .filter(log -> log.getInstant().isBefore(errorTime) &&
                         Duration.between(log.getInstant(), errorTime).toMillis() < 1000)
            .collect(Collectors.toList());

        for (ServiceLog log : precedingLogs) {
            analysis.append("   ").append(log.getTimestamp()).append(" - ")
                    .append(log.getServiceName()).append(": ")
                    .append(log.getMessage()).append("\n");
        }

        if (cascadeErrors.size() > 0) {
            analysis.append("4. 检测到 ").append(cascadeErrors.size()).append(" 个级联错误，已排除:\n");
            for (ServiceLog cascade : cascadeErrors) {
                analysis.append("   ").append(cascade.getTimestamp()).append(" - ")
                        .append(cascade.getServiceName()).append("\n");
            }
        }

        return analysis.toString();
    }

    /**
     * 确定修复类型
     */
    private String determineFixType(RootCauseAnalysisResult.ErrorType errorType) {
        switch (errorType) {
            case NULL_POINTER_EXCEPTION:
            case ARRAY_INDEX_OUT_OF_BOUNDS:
            case BUSINESS_LOGIC_BUG:
                return "CODE_PATCH";
            case CONFIGURATION_ERROR:
                return "CONFIG_CHANGE";
            case DATABASE_DEADLOCK:
            case RESOURCE_EXHAUSTION:
                return "DATA_FIX";
            case UPSTREAM_TIMEOUT:
                return "CONFIG_CHANGE";
            default:
                return "CODE_PATCH";
        }
    }

    /**
     * 提取相关日志条目
     */
    private List<String> extractRelatedLogs(List<ServiceLog> logs, ServiceLog firstError) {
        Instant errorTime = firstError.getInstant();
        long timeWindow = Duration.ofMinutes(5).toMillis(); // 5分钟时间窗口

        return logs.stream()
                  .filter(log -> Math.abs(log.getInstant().toEpochMilli() - errorTime.toEpochMilli()) <= timeWindow)
                  .map(log -> String.format("[%s] %s: %s",
                          log.getTimestamp(), log.getServiceName(),
                          log.getException() != null ? log.getException() : log.getMessage()))
                  .collect(Collectors.toList());
    }

    /**
     * 查找受影响的服务
     */
    private List<String> findAffectedServices(List<ServiceLog> logs) {
        return logs.stream()
                  .map(ServiceLog::getServiceName)
                  .distinct()
                  .collect(Collectors.toList());
    }

    /**
     * 确定严重级别
     */
    private RootCauseAnalysisResult.SeverityLevel determineSeverityLevel(ServiceLog firstError, List<ServiceLog> cascadeErrors) {
        if (firstError.getException() != null &&
            (firstError.getException().contains("Critical") ||
             firstError.getException().contains("Fatal"))) {
            return RootCauseAnalysisResult.SeverityLevel.CRITICAL;
        } else if (cascadeErrors.size() > 2) {
            return RootCauseAnalysisResult.SeverityLevel.HIGH;
        } else if (firstError.getException() != null) {
            return RootCauseAnalysisResult.SeverityLevel.MEDIUM;
        } else {
            return RootCauseAnalysisResult.SeverityLevel.LOW;
        }
    }

    /**
     * 创建未找到错误的报告
     */
    private RootCauseAnalysisResult createNoErrorReport(List<ServiceLog> logs) {
        RootCauseAnalysisResult result = new RootCauseAnalysisResult();
        result.setFaultService("unknown");
        result.setRootCauseSummary("未检测到错误");
        result.setDetailedAnalysis("在指定Trace ID的所有日志中未发现错误信息。系统运行正常。");
        result.setCodeLocation("无");
        result.setSuggestedFixType("NO_FIX");
        result.setAffectedServices(findAffectedServices(logs));
        result.setErrorType(RootCauseAnalysisResult.ErrorType.UNKNOWN);
        result.setSeverityLevel(RootCauseAnalysisResult.SeverityLevel.LOW);
        return result;
    }
}