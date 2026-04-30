#!/bin/bash

echo "=========================================="
echo "全链路日志根因分析系统演示"
echo "=========================================="
echo ""

echo "示例1: 分析空指针异常"
echo "------------------------------------------"
java StandaloneRootCauseAnalyzer abc123 example-logs.txt
echo ""

echo "=========================================="
echo "示例2: 分析多错误场景"
echo "------------------------------------------"
java StandaloneRootCauseAnalyzer abc123 example-error-logs.txt
echo ""

echo "=========================================="
echo "示例3: 分析无错误场景"
echo "------------------------------------------"
java StandaloneRootCauseAnalyzer def456 example-logs.txt
echo ""

echo "=========================================="
echo "演示完成！"