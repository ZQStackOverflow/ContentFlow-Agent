package com.example;

import com.example.analyzer.TraceAnalyzer;
import com.example.model.*;
import com.example.parser.LogParser;
import com.example.parser.SourceCodeParser;
import com.example.utils.JSONUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

/**
 * 主程序入口 - 根因分析工具
 */
public class RootCauseAnalyzer {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("用法: java -jar root-cause-analyzer.jar <trace_id> <log_file_path> [source_code_dir]");
            System.out.println("示例: java -jar root-cause-analyzer.jar abc123 logs.txt ./src");
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
            List<ServiceLog> filteredLogs = logFilter.filterByTraceId(serviceLogs, traceId);

            if (filteredLogs.isEmpty()) {
                System.out.println("错误: 在日志中未找到Trace ID: " + traceId);
                return;
            }

            // 3. 解析源代码（如果提供了目录）
            SourceCodeParser sourceCodeParser = null;
            if (sourceCodeDir != null && new File(sourceCodeDir).exists()) {
                sourceCodeParser = new SourceCodeParser();
                sourceCodeParser.parseSourceCode(sourceCodeDir);
            }

            // 4. 执行根因分析
            TraceAnalyzer traceAnalyzer = new TraceAnalyzer(sourceCodeParser);
            RootCauseAnalysisResult result = traceAnalyzer.analyze(filteredLogs);

            // 5. 输出结果
            System.out.println(JSONUtils.toJson(result));

        } catch (Exception e) {
            System.err.println("分析过程中发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}