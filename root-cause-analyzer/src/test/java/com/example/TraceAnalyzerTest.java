package com.example;

import com.example.model.ServiceLog;
import com.example.parser.LogParser;
import com.example.parser.LogFilter;
import com.example.analyzer.TraceAnalyzer;
import com.example.parser.SourceCodeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Trace分析器测试
 */
public class TraceAnalyzerTest {

    private TraceAnalyzer traceAnalyzer;
    private File testLogFile;
    private File tempSourceDir;

    @BeforeEach
    void setUp() throws IOException {
        // 创建临时源代码目录
        tempSourceDir = Files.createTempDirectory("test-source").toFile();
        File sourceFile = new File(tempSourceDir, "UserService.java");
        String sourceCode = "package com.example;\n" +
                           "\n" +
                           "public class UserService {\n" +
                           "    private UserMapper userMapper;\n" +
                           "\n" +
                           "    public void validateUser(User user) {\n" +
                           "        if (user == null) {\n" +
                           "            throw new NullPointerException(\"User cannot be null\");\n" +
                           "        }\n" +
                           "        String name = user.getName();\n" +
                           "        if (name == null || name.isEmpty()) {\n" +
                           "            throw new IllegalArgumentException(\"Invalid user name\");\n" +
                           "        }\n" +
                           "    }\n" +
                           "\n" +
                           "    public User findUserById(Long id) {\n" +
                           "        if (id == null) {\n" +
                           "            return null;\n" +
                           "        }\n" +
                           "        return userMapper.findById(id);\n" +
                           "    }\n" +
                           "}\n";

        Files.writeString(sourceFile.toPath(), sourceCode);

        // 解析源代码
        SourceCodeParser sourceCodeParser = new SourceCodeParser();
        sourceCodeParser.parseSourceCode(tempSourceDir.getAbsolutePath());

        // 创建分析器
        traceAnalyzer = new TraceAnalyzer(sourceCodeParser);

        // 创建测试日志文件
        testLogFile = File.createTempFile("test-trace", ".txt");

        String testLogs = "abc123 user-service 2024-01-15 10:00:00.123 INFO Login request received\n" +
                         "abc123 user-service 2024-01-15 10:00:00.234 DEBUG Finding user by ID\n" +
                         "abc123 user-service 2024-01-15 10:00:00.345 ERROR NullPointerException\n" +
                         "Exception in thread \"main\" java.lang.NullPointerException: Cannot invoke method 'getName()' on null object\n" +
                         "at com.example.UserService.validateUser(UserService.java:45)\n" +
                         "abc123 order-service 2024-01-15 10:00:01.123 ERROR Failed to create order\n" +
                         "Caused by: com.example.UserValidationException: User validation failed\n" +
                         "abc123 payment-service 2024-01-15 10:00:02.456 WARN Payment timeout\n";

        Files.writeString(testLogFile.toPath(), testLogs);
    }

    @Test
    void testAnalyzeTrace() throws IOException {
        // 解析日志
        LogParser logParser = new LogParser();
        List<ServiceLog> logs = logParser.parseLogFile(testLogFile.getAbsolutePath());

        // 过滤出特定Trace ID
        LogFilter logFilter = new LogFilter();
        List<ServiceLog> filteredLogs = logFilter.filterByTraceId(logs, "abc123");

        // 执行分析
        String result = traceAnalyzer.analyze(filteredLogs).toString();

        // 验证结果
        assertTrue(result.contains("fault_service"));
        assertTrue(result.contains("root_cause_summary"));
        assertTrue(result.contains("detailed_analysis"));
        assertTrue(result.contains("code_location"));
        assertTrue(result.contains("suggested_fix_type"));
    }

    @Test
    void testErrorTypeDetection() throws IOException {
        LogParser logParser = new LogParser();
        List<ServiceLog> logs = logParser.parseLogFile(testLogFile.getAbsolutePath());
        LogFilter logFilter = new LogFilter();
        List<ServiceLog> filteredLogs = logFilter.filterByTraceId(logs, "abc123");

        String result = traceAnalyzer.analyze(filteredLogs).toString();

        // 验证检测到的是空指针异常
        assertTrue(result.contains("NULL_POINTER_EXCEPTION"));
    }

    @Test
    void testCodeLocationDetection() throws IOException {
        LogParser logParser = new LogParser();
        List<ServiceLog> logs = logParser.parseLogFile(testLogFile.getAbsolutePath());
        LogFilter logFilter = new LogFilter();
        List<ServiceLog> filteredLogs = logFilter.filterByTraceId(logs, "abc123");

        String result = traceAnalyzer.analyze(filteredLogs).toString();

        // 验证代码位置包含正确的文件名
        assertTrue(result.contains("UserService.java"));
    }
}