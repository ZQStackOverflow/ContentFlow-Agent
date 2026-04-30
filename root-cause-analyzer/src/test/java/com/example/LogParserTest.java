package com.example;

import com.example.model.ServiceLog;
import com.example.parser.LogParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 日志解析器测试
 */
public class LogParserTest {

    private LogParser logParser;
    private File testLogFile;

    @BeforeEach
    void setUp() throws IOException {
        logParser = new LogParser();

        // 创建测试日志文件
        testLogFile = File.createTempFile("test-logs", ".txt");

        // 写入测试日志
        String testLogs = "[2024-01-15 10:00:00.123] [user-service] INFO - User login request received\n" +
                         "[2024-01-15 10:00:00.234] [user-service] DEBUG - Username: testuser\n" +
                         "[2024-01-15 10:00:00.345] [user-service] ERROR - NullPointerException\n" +
                         "Exception in thread \"main\" java.lang.NullPointerException: Cannot invoke method 'getName()' on null object\n" +
                         "at com.example.UserService.validateUser(UserService.java:45)\n" +
                         "[2024-01-15 10:00:00.456] [order-service] ERROR - Failed to process order due to upstream error\n" +
                         "Caused by: com.example.UserValidationException: User validation failed\n" +
                         "\n";

        Files.writeString(testLogFile.toPath(), testLogs);
    }

    @Test
    void testParseLogFile() throws IOException {
        List<ServiceLog> logs = logParser.parseLogFile(testLogFile.getAbsolutePath());

        // 验证解析结果
        assertEquals(5, logs.size());

        // 验证第一条日志
        ServiceLog firstLog = logs.get(0);
        assertEquals("2024-01-15 10:00:00.123", firstLog.getTimestamp());
        assertEquals("user-service", firstLog.getServiceName());
        assertEquals("INFO", firstLog.getLevel());

        // 验证错误日志
        ServiceLog errorLog = logs.stream()
                                .filter(ServiceLog::isError)
                                .findFirst()
                                .orElseThrow();

        assertEquals("user-service", errorLog.getServiceName());
        assertEquals("ERROR", errorLog.getLevel());
        assertTrue(errorLog.getException().contains("NullPointerException"));
    }

    @Test
    void testParseDifferentLogFormats() throws IOException {
        // 创建不同格式的测试文件
        File differentFormatsFile = File.createTempFile("different-formats", ".txt");

        String differentLogs = "2024-01-15 10:01:00 user-service ERROR Database connection failed\n" +
                              "[2024-01-15 10:01:01] [payment-service] WARN - Payment timeout approaching\n" +
                              "2024/01/15 10:01:02 [inventory-service] FATAL - Out of stock\n" +
                              "abc123 checkout-service 2024-01-15 10:01:03 INFO Order processed\n";

        Files.writeString(differentFormatsFile.toPath(), differentLogs);

        List<ServiceLog> logs = logParser.parseLogFile(differentFormatsFile.getAbsolutePath());

        assertEquals(4, logs.size());

        // 验证不同格式都被正确解析
        assertTrue(logs.stream().anyMatch(log -> "user-service".equals(log.getServiceName())));
        assertTrue(logs.stream().anyMatch(log -> "payment-service".equals(log.getServiceName())));
        assertTrue(logs.stream().anyMatch(log -> "inventory-service".equals(log.getServiceName())));
        assertTrue(logs.stream().anyMatch(log -> "checkout-service".equals(log.getServiceName())));
    }
}