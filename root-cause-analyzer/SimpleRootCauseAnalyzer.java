package com.example;

import com.example.model.*;
import com.example.parser.*;
import com.example.analyzer.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.time.*;
import java.time.format.*;
import java.util.regex.*;

/**
 * 完全独立的根因分析器 - 无外部依赖
 */
public class SimpleRootCauseAnalyzer {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("用法: java SimpleRootCauseAnalyzer <trace_id> <log_file_path> [source_code_dir]");
            System.out.println("示例: java SimpleRootCauseAnalyzer abc123 example-logs.txt example-src");
            return;
        }

        String traceId = args[0];
        String logFilePath = args[1];
        String sourceCodeDir = args.length > 2 ? args[2] : null;

        try {
            // 1. 解析日志文件
            LogParser logParser = new LogParser();
            List<ServiceLog> serviceLogs = logParser.parseLogFile(logFilePath);

            // 2. 过滤出指定Trace ID的日志
            LogFilter logFilter = new LogFilter();
            List<ServiceLog> filteredLogs = logFilter.filterByTraceId(serviceLogs, traceId);

            if (filteredLogs.isEmpty()) {
                System.out.println("错误: 在日志中未找到Trace ID: " + traceId);
                return;
            }

            // 3. 执行根因分析
            SourceCodeParser sourceCodeParser = null;
            if (sourceCodeDir != null && new File(sourceCodeDir).exists()) {
                sourceCodeParser = new SourceCodeParser();
                sourceCodeParser.parseSourceCode(sourceCodeDir);
            }

            TraceAnalyzer traceAnalyzer = new TraceAnalyzer(sourceCodeParser);
            RootCauseAnalysisResult result = traceAnalyzer.analyze(filteredLogs);

            // 4. 输出结果
            System.out.println(toJson(result));

        } catch (Exception e) {
            System.err.println("分析过程中发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 简化的JSON输出
    private static String toJson(RootCauseAnalysisResult result) {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"fault_service\": \"").append(escapeJson(result.getFaultService())).append("\",\n");
        json.append("  \"root_cause_summary\": \"").append(escapeJson(result.getRootCauseSummary())).append("\",\n");
        json.append("  \"detailed_analysis\": \"").append(escapeJson(result.getDetailedAnalysis())).append("\",\n");
        json.append("  \"code_location\": \"").append(escapeJson(result.getCodeLocation())).append("\",\n");
        json.append("  \"suggested_fix_type\": \"").append(escapeJson(result.getSuggestedFixType())).append("\",\n");
        json.append("  \"related_log_entries\": [\n");
        for (int i = 0; i < result.getRelatedLogEntries().size(); i++) {
            json.append("    \"").append(escapeJson(result.getRelatedLogEntries().get(i))).append("\"");
            if (i < result.getRelatedLogEntries().size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }
        json.append("  ],\n");
        json.append("  \"affected_services\": [\n");
        for (int i = 0; i < result.getAffectedServices().size(); i++) {
            json.append("    \"").append(escapeJson(result.getAffectedServices().get(i))).append("\"");
            if (i < result.getAffectedServices().size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }
        json.append("  ],\n");
        json.append("  \"error_type\": \"").append(result.getErrorType()).append("\",\n");
        json.append("  \"severity_level\": \"").append(result.getSeverityLevel()).append("\"\n");
        json.append("}");
        return json.toString();
    }

    private static String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
}