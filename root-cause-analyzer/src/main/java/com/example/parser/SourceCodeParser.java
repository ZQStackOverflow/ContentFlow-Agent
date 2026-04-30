package com.example.parser;

import com.example.model.SourceCodeFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * 源代码解析器
 */
public class SourceCodeParser {

    private Map<String, SourceCodeFile> sourceCodeFiles = new HashMap<>();
    private Map<String, List<String>> serviceToFiles = new HashMap<>();

    /**
     * 解析源代码目录
     */
    public void parseSourceCode(String sourceCodeDir) throws IOException {
        File dir = new File(sourceCodeDir);
        if (!dir.exists() || !dir.isDirectory()) {
            throw new IOException("源代码目录不存在: " + sourceCodeDir);
        }

        // 递归遍历目录
        Files.walk(Paths.get(sourceCodeDir))
            .filter(path -> path.toString().endsWith(".java"))
            .forEach(this::parseJavaFile);

        // 构建服务到文件的映射
        buildServiceToFileMapping();
    }

    /**
     * 解析单个Java文件
     */
    private void parseJavaFile(Path path) {
        try {
            String content = new String(Files.readAllBytes(path));
            SourceCodeFile sourceFile = new SourceCodeFile();

            sourceFile.setFilePath(path.toString());
            sourceFile.setFileName(path.getFileName().toString());
            sourceFile.setContent(content);

            // 提取服务名（基于包名）
            String serviceName = extractServiceNameFromPackage(content);
            sourceFile.setServiceName(serviceName);

            // 提取类名
            String className = extractClassName(content);
            sourceFile.setClassName(className);

            sourceCodeFiles.put(path.toString(), sourceFile);
        } catch (IOException e) {
            System.err.println("无法读取文件: " + path + ", 错误: " + e.getMessage());
        }
    }

    /**
     * 从包名中提取服务名
     */
    private String extractServiceNameFromPackage(String content) {
        // 查找package语句
        int packageIndex = content.indexOf("package ");
        if (packageIndex != -1) {
            int end = content.indexOf(';', packageIndex);
            if (end != -1) {
                String packageStatement = content.substring(packageIndex + 7, end);
                // 取最后一个包名作为服务名
                String[] packages = packageStatement.split("\\.");
                return packages[packages.length - 1];
            }
        }
        return "unknown";
    }

    /**
     * 提取类名
     */
    private String extractClassName(String content) {
        // 查找public class声明
        Pattern pattern = Pattern.compile("public\\s+class\\s+(\\w+)");
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "unknown";
    }

    /**
     * 构建服务到文件的映射
     */
    private void buildServiceToFileMapping() {
        for (SourceCodeFile file : sourceCodeFiles.values()) {
            String serviceName = file.getServiceName();
            serviceToFiles.computeIfAbsent(serviceName, k -> new ArrayList<>())
                        .add(file.getFilePath());
        }
    }

    /**
     * 根据服务名获取相关文件
     */
    public List<SourceCodeFile> getFilesByService(String serviceName) {
        List<String> filePaths = serviceToFiles.getOrDefault(serviceName, new ArrayList<>());
        return filePaths.stream()
                      .map(sourceCodeFiles::get)
                      .collect(Collectors.toList());
    }

    /**
     * 根据文件路径获取源代码文件
     */
    public SourceCodeFile getFileByPath(String filePath) {
        return sourceCodeFiles.get(filePath);
    }

    /**
     * 搜索包含特定关键词的代码行
     */
    public List<SourceCodeLine> searchCode(String serviceName, String keyword) {
        List<SourceCodeLine> results = new ArrayList<>();

        List<SourceCodeFile> files = getFilesByService(serviceName);
        for (SourceCodeFile file : files) {
            String[] lines = file.getContent().split("\\n");
            for (int i = 0; i < lines.length; i++) {
                if (lines[i].contains(keyword)) {
                    SourceCodeLine line = new SourceCodeLine();
                    line.setFilePath(file.getFilePath());
                    line.setLineNumber(i + 1);
                    line.setContent(lines[i]);
                    line.setServiceName(serviceName);
                    results.add(line);
                }
            }
        }

        return results;
    }
}