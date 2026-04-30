@echo off
echo ==========================================
echo 全链路日志根因分析系统演示
echo ==========================================
echo.

echo 1. 演示空指针异常分析...
echo Trace ID: abc123
echo 日志文件: example-logs.txt
echo.
java StandaloneRootCauseAnalyzer abc123 example-logs.txt
echo.
echo ==========================================
echo.

echo 2. 演示多错误类型分析...
echo Trace ID: abc123
echo 日志文件: example-error-logs.txt
echo.
java StandaloneRootCauseAnalyzer abc123 example-error-logs.txt
echo.
echo ==========================================
echo.

echo 3. 演示无错误场景...
echo Trace ID: def456
echo 日志文件: example-logs.txt
echo.
java StandaloneRootCauseAnalyzer def456 example-logs.txt
echo.
echo ==========================================
echo.
echo 演示完成！