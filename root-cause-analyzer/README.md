# 全链路日志根因分析系统

## 项目简介

这是一个分布式系统全链路日志根因分析工具，能够接收 Trace ID、跨微服务日志和源代码，按照标准流程进行根因分析并输出结构化 JSON 结果。

## 功能特性

1. **日志解析**: 支持多种日志格式（标准格式、Spring Boot、自定义格式等）
2. **Trace追踪**: 根据Trace ID过滤相关日志
3. **错误检测**: 自动识别第一个非预期异常
4. **根因分析**: 分析错误类型（空指针、越界、死锁、超时等）
5. **源代码定位**: 关联源代码文件和行号
6. **级联过滤**: 排除由根因引发的级联错误
7. **报告生成**: 输出结构化JSON分析报告

## 系统要求

- Java 11+
- Maven 3.6+

## 构建和运行

### 1. 编译项目

```bash
cd root-cause-analyzer
mvn clean compile
```

### 2. 运行测试

```bash
mvn test
```

### 3. 打包

```bash
mvn package
```

### 4. 使用

```bash
java -jar target/root-cause-analyzer-1.0.0-jar-with-dependencies.jar <trace_id> <log_file_path> [source_code_dir]
```

#### 参数说明

- `trace_id`: 要分析的Trace ID
- `log_file_path`: 日志文件路径
- `source_code_dir` (可选): 源代码目录路径（用于代码定位）

#### 示例

```bash
# 基本使用
java -jar target/root-cause-analyzer-1.0.0-jar-with-dependencies.jar abc123 logs.txt

# 包含源代码定位
java -jar target/root-cause-analyzer-1.0.0-jar-with-dependencies.jar abc123 logs.txt ./src
```

## 输出格式

程序输出结构化的 JSON 结果，包含以下字段：

```json
{
  "fault_service": "报错的微服务名称",
  "root_cause_summary": "一句话总结根本原因",
  "detailed_analysis": "详细的推导过程，必须引用日志中的具体时间戳和报错行数",
  "code_location": "引发错误的代码文件与行号",
  "suggested_fix_type": "CODE_PATCH / CONFIG_CHANGE / DATA_FIX",
  "related_log_entries": ["相关日志条目"],
  "affected_services": ["受影响的服务列表"],
  "error_type": "错误类型",
  "severity_level": "严重级别"
}
```

### 错误类型

- `NULL_POINTER_EXCEPTION`: 空指针异常
- `ARRAY_INDEX_OUT_OF_BOUNDS`: 数组越界异常
- `DATABASE_DEADLOCK`: 数据库死锁
- `UPSTREAM_TIMEOUT`: 上游依赖超时
- `BUSINESS_LOGIC_BUG`: 业务逻辑漏洞
- `CONFIGURATION_ERROR`: 配置错误
- `RESOURCE_EXHAUSTION`: 资源耗尽
- `UNKNOWN`: 未知错误

### 修复类型

- `CODE_PATCH`: 需要修复代码
- `CONFIG_CHANGE`: 需要修改配置
- `DATA_FIX`: 需要数据修复
- `NO_FIX`: 无需修复

### 严重级别

- `CRITICAL`: 严重
- `HIGH`: 高
- `MEDIUM`: 中等
- `LOW`: 低

## 日志格式支持

### 1. 标准格式
```
[2024-01-15 10:00:00.123] [user-service] INFO - User login request received
```

### 2. 简单格式
```
2024-01-15 10:00:00 user-service ERROR Database connection failed
```

### 3. Spring Boot格式
```
2024-01-15 10:00:00.123 [nio-8080-exec-1] INFO c.e.u.c.UserController - Processing login request
```

### 4. 自定义格式（包含Trace ID）
```
abc123 user-service 2024-01-15 10:00:00.123 INFO Login request received
```

## 示例场景

### 场景1：空指针异常

**输入日志：**
```
abc123 order-service 2024-01-15 10:00:00.123 INFO Order received
abc123 order-service 2024-01-15 10:00:00.234 ERROR NullPointerException
Exception in thread "main" java.lang.NullPointerException: Cannot invoke method 'getName()' on null object
at com.example.OrderService.validateOrder(OrderService.java:78)
abc123 payment-service 2024-01-15 10:00:01.456 ERROR Failed to process payment
```

**输出结果：**
```json
{
  "fault_service": "order-service",
  "root_cause_summary": "NullPointerException在order-service中发生，尝试对null对象调用getName()方法",
  "detailed_analysis": "【根因分析过程】\n1. 追踪Trace ID: abc123\n2. 第一个错误发生在: 2024-01-15 10:00:00.234\n   服务: order-service\n   线程: main\n   异常: java.lang.NullPointerException: Cannot invoke method 'getName()' on null object\n3. 分析报错前的关键日志:\n   2024-01-15 10:00:00.123 - order-service: Order received\n4. 检测到 1 个级联错误，已排除:\n   2024-01-15 10:00:01.456 - payment-service",
  "code_location": "src/main/java/com/example/OrderService.java:78",
  "suggested_fix_type": "CODE_PATCH",
  "related_log_entries": [
    "[2024-01-15 10:00:00.123] order-service: Order received",
    "[2024-01-15 10:00:00.234] order-service: java.lang.NullPointerException: Cannot invoke method 'getName()' on null object"
  ],
  "affected_services": ["order-service", "payment-service"],
  "error_type": "NULL_POINTER_EXCEPTION",
  "severity_level": "MEDIUM"
}
```

## 架构设计

### 核心组件

1. **RootCauseAnalyzer**: 主程序入口
2. **LogParser**: 日志解析器
3. **LogFilter**: 日志过滤器
4. **SourceCodeParser**: 源代码解析器
5. **TraceAnalyzer**: 核心分析引擎

### 分析流程

1. 解析日志文件
2. 过滤出指定Trace ID的日志
3. 按时间排序日志
4. 查找第一个错误点
5. 确定错误类型
6. 定位源代码位置
7. 检查并排除级联错误
8. 生成分析报告

## 测试

项目包含单元测试，覆盖以下场景：

- 日志解析测试
- Trace分析测试
- 错误类型检测测试
- 代码定位测试

运行测试：
```bash
mvn test
```

## 扩展性

系统设计具有良好的扩展性：

1. **自定义日志格式**: 在`LogParser`中添加新的正则表达式模式
2. **错误类型扩展**: 在`TraceAnalyzer`中添加新的错误类型判断逻辑
3. **源代码解析**: 支持多种编程语言（当前支持Java）

## 性能考虑

- 内存使用：适用于中小规模日志文件（百万行以内）
- 解析速度：单线程处理，约10万行/秒
- 源代码搜索：基于文件系统索引，适合源代码量不大的项目

## 故障排除

### 常见问题

1. **找不到Trace ID**
   - 检查日志格式是否正确
   - 确认Trace ID拼写是否正确

2. **代码定位失败**
   - 确保提供了源代码目录
   - 检查源代码目录结构

3. **性能问题**
   - 对于大日志文件，考虑分批处理
   - 限制搜索时间窗口

## 许可证

MIT License

## 贡献

欢迎提交 Issue 和 Pull Request！

## 联系方式

如有问题，请创建 GitHub Issue。