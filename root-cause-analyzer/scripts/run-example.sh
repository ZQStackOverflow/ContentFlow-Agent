#!/bin/bash

# 运行示例脚本

echo "=== 全链路日志根因分析系统示例 ==="
echo ""

# 检查参数
if [ $# -lt 2 ]; then
    echo "用法: $0 <trace_id> <log_file> [source_code_dir]"
    echo "示例: $0 abc123 example-logs.txt ./src"
    exit 1
fi

TRACE_ID=$1
LOG_FILE=$2
SOURCE_DIR=$3

# 检查日志文件是否存在
if [ ! -f "$LOG_FILE" ]; then
    echo "错误: 日志文件 $LOG_FILE 不存在"
    echo ""
    echo "创建示例日志文件..."

    # 创建示例日志文件
    cat > example-logs.txt << 'EOF'
abc123 user-service 2024-01-15 10:00:00.123 INFO User login request received
abc123 user-service 2024-01-15 10:00:00.234 DEBUG Searching for user with ID: 12345
abc123 user-service 2024-01-15 10:00:00.345 ERROR NullPointerException
Exception in thread "http-nio-8080-exec-1" java.lang.NullPointerException: Cannot invoke 'getUsername()' on a null object
        at com.example.UserService.findUser(UserService.java:67)
        at com.example.UserController.handleLogin(UserController.java:23)
        at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:77)
abc123 order-service 2024-01-15 10:00:01.456 ERROR Failed to create order
Caused by: com.example.UserNotFoundException: User not found
        at com.example.OrderService.validateUser(OrderService.java:89)
        at com.example.OrderService.createOrder(OrderService.java:45)
abc123 payment-service 2024-01-15 10:00:02.789 WARN Payment timeout after 30 seconds
abc123 notification-service 2024-01-15 10:00:03.123 INFO Sending notification to admin

# 另一个Trace的示例
def456 user-service 2024-01-15 10:01:00.123 INFO User login request received
def456 user-service 2024-01-15 10:01:00.234 DEBUG User found: admin
def456 order-service 2024-01-15 10:01:01.456 INFO Order created successfully
def456 payment-service 2024-01-15 10:01:02.789 INFO Payment processed successfully
EOF

    echo "已创建 example-logs.txt 文件"
    LOG_FILE="example-logs.txt"
fi

# 创建示例源代码目录
if [ ! -d "$SOURCE_DIR" ]; then
    echo "创建示例源代码目录..."
    mkdir -p example-src/com/example
    SOURCE_DIR="./example-src"

    # 创建示例Java代码
    cat > example-src/com/example/UserService.java << 'EOF'
package com.example;

import org.springframework.stereotype.Service;

@Service
public class UserService {

    private UserRepository userRepository;

    public User findUser(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        User user = userRepository.findById(userId);
        if (user == null) {
            return null;
        }

        // 这里可能导致NullPointerException
        String username = user.getUsername();
        if (username == null) {
            throw new IllegalStateException("User username is null");
        }

        return user;
    }

    public User findByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }

        return userRepository.findByUsername(username);
    }
}
EOF

    cat > example-src/com/example/UserController.java << 'EOF'
package com.example;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    private UserService userService;

    @PostMapping("/login")
    public String handleLogin(@RequestParam Long userId) {
        User user = userService.findUser(userId);
        if (user == null) {
            throw new UserNotFoundException("User not found");
        }
        return "Login successful for user: " + user.getUsername();
    }
}
EOF
fi

echo "分析 Trace ID: $TRACE_ID"
echo "日志文件: $LOG_FILE"
echo "源代码目录: $SOURCE_DIR"
echo ""

# 构建项目
echo "构建项目..."
mvn clean compile -q

if [ $? -ne 0 ]; then
    echo "构建失败"
    exit 1
fi

echo "构建完成"
echo ""

# 运行分析
echo "开始分析..."
mvn exec:java -q -Dexec.mainClass="com.example.RootCauseAnalyzer" \
    -Dexec.args="$TRACE_ID $LOG_FILE $SOURCE_DIR"

echo ""
echo "分析完成"