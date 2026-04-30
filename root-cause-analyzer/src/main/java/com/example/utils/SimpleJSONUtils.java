package com.example.utils;

/**
 * 简化版JSON工具类 - 无外部依赖
 */
public class SimpleJSONUtils {

    /**
     * 将对象转换为JSON字符串
     */
    public static String toJson(Object obj) {
        if (obj == null) {
            return "null";
        }

        // 简单实现，只处理基本类型
        if (obj instanceof String) {
            return "\"" + escapeJson((String) obj) + "\"";
        }

        return obj.toString();
    }

    /**
     * 转义JSON特殊字符
     */
    private static String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
}