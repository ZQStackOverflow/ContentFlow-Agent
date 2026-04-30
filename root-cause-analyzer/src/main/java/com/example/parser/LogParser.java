package com.example.parser;

import com.example.model.ServiceLog;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * 日志解析器
 */
public class LogParser {

    // 常见的日志正则表达式模式
    private static final Pattern[] LOG_PATTERNS = {
        // 标准格式: [timestamp] [service] [level] message
        Pattern.compile("^\\[([^\\]]+)\\]\\s*\\[([^\\]]+)\\]\\s*\\[(\\w+)\\]\\s*(.*)$"),

        // 简单格式: timestamp service level message
        Pattern.compile("^([^\\s]+)\\s+([^\\s]+)\\s+(\\w+)\\s+(.*)$"),

        // Spring Boot日志
        Pattern.compile("^(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}.\\d{3})\\s+\\[([^\\]]+)\\]\\s+(\\w+)\\s+-\\s+(.*)$"),

        // 自定义格式: trace-id service-name timestamp level message
        Pattern.compile("^([a-f0-9-]+)\\s+([^\\s]+)\\s+(\\d{4}-\\d{2}-\\d{2} [^\\s]+)\\s+(\\w+)\\s+(.*)$")
    };

    /**
     * 解析日志文件
     */
    public List<ServiceLog> parseLogFile(String filePath) throws IOException {
        List<ServiceLog> logs = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                ServiceLog log = parseLogLine(line, lineNumber);
                if (log != null) {
                    logs.add(log);
                }
            }
        }

        return logs;
    }

    /**
     * 解析单行日志
     */
    private ServiceLog parseLogLine(String line, int lineNumber) {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }

        line = line.trim();
        ServiceLog log = new ServiceLog();
        log.setMessage(line);

        // 尝试不同的日志格式
        for (Pattern pattern : LOG_PATTERNS) {
            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                // 根据匹配的模式设置日志字段
                if (pattern == LOG_PATTERNS[0]) {
                    // 标准格式: [timestamp] [service] [level] message
                    log.setTimestamp(matcher.group(1));
                    log.setServiceName(matcher.group(2));
                    log.setLevel(matcher.group(3));
                    log.setMessage(matcher.group(4));
                } else if (pattern == LOG_PATTERNS[1]) {
                    // 简单格式: timestamp service level message
                    log.setTimestamp(matcher.group(1));
                    log.setServiceName(matcher.group(2));
                    log.setLevel(matcher.group(3));
                    log.setMessage(matcher.group(4));
                } else if (pattern == LOG_PATTERNS[2]) {
                    // Spring Boot日志
                    log.setTimestamp(matcher.group(1));
                    log.setLoggerName(matcher.group(2));
                    log.setLevel(matcher.group(3));
                    log.setMessage(matcher.group(4));
                    // 从logger中提取service name
                    extractServiceFromLogger(log, matcher.group(2));
                } else if (pattern == LOG_PATTERNS[3]) {
                    // 自定义格式: trace-id service-name timestamp level message
                    log.setTraceId(matcher.group(1));
                    log.setServiceName(matcher.group(2));
                    log.setTimestamp(matcher.group(3));
                    log.setLevel(matcher.group(4));
                    log.setMessage(matcher.group(5));
                }
                break;
            }
        }

        // 检查是否包含异常信息
        if (line.contains("Exception:") || line.contains("Error:") ||
            line.contains("ERROR") || line.contains("FATAL")) {
            log.setLevel("ERROR");
            extractExceptionFromMessage(log);
        }

        return log;
    }

    /**
     * 从logger名称中提取服务名
     */
    private void extractServiceFromLogger(ServiceLog log, String loggerName) {
        if (loggerName.contains(".")) {
            String[] parts = loggerName.split("\\.");
            // 取最后一部分作为服务名
            String serviceName = parts[parts.length - 1];
            if (!serviceName.equals("root")) {
                log.setServiceName(serviceName);
            }
        }
    }

    /**
     * 从消息中提取异常信息
     */
    private void extractExceptionFromMessage(ServiceLog log) {
        String message = log.getMessage();
        int exceptionIndex = message.indexOf("Exception:");
        if (exceptionIndex != -1) {
            String exception = message.substring(exceptionIndex + 10).trim();
            if (!exception.isEmpty()) {
                log.setException(exception);
                log.setStackTrace(extractStackTrace(message, exceptionIndex));
            }
        }
    }

    /**
     * 提取堆栈跟踪
     */
    private String extractStackTrace(String message, int startIndex) {
        // 简单实现：返回异常后的剩余内容
        return message.substring(startIndex);
    }
}