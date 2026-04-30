@echo off
echo Running Root Cause Analyzer...
echo.

REM Check if required files exist
if not exist "example-logs.txt" (
    echo Creating example logs...
    (
        echo abc123 user-service 2024-01-15 10:00:00.123 INFO User login request received
        echo abc123 user-service 2024-01-15 10:00:00.234 DEBUG Searching for user with ID: 12345
        echo abc123 user-service 2024-01-15 10:00:00.345 ERROR NullPointerException
        echo Exception in thread "http-nio-8080-exec-1" java.lang.NullPointerException: Cannot invoke 'getUsername()' on a null object
        echo         at com.example.UserService.findUser(UserService.java:67)
        echo         at com.example.UserController.handleLogin(UserController.java:23)
        echo abc123 order-service 2024-01-15 10:00:01.456 ERROR Failed to create order
        echo Caused by: com.example.UserNotFoundException: User not found
        echo         at com.example.OrderService.validateUser(OrderService.java:89)
        echo         at com.example.OrderService.createOrder(OrderService.java:45)
        echo abc123 payment-service 2024-01-15 10:00:02.789 WARN Payment timeout after 30 seconds
        echo abc123 notification-service 2024-01-15 10:00:03.123 INFO Sending notification to admin
    ) > example-logs.txt
)

if not exist "example-src" mkdir example-src
if not exist "example-src\com" mkdir example-src\com
if not exist "example-src\com\example" mkdir example-src\com\example

echo Creating example source code...
(
    echo package com.example;
    echo.
    echo import org.springframework.stereotype.Service;
    echo.
    echo @Service
    echo public class UserService {
    echo.
    echo     private UserRepository userRepository;
    echo.
    echo     public User findUser(Long userId) {
    echo         if (userId == null) {
    echo             throw new IllegalArgumentException("User ID cannot be null");
    echo         }
    echo.
    echo         User user = userRepository.findById(userId);
    echo         if (user == null) {
    echo             return null;
    echo         }
    echo.
    echo         // 这里可能导致NullPointerException
    echo         String username = user.getUsername();
    echo         if (username == null) {
    echo             throw new IllegalStateException("User username is null");
    echo         }
    echo.
    echo         return user;
    echo     }
    echo }
) > example-src\com\example\UserService.java

(
    echo package com.example;
    echo.
    echo import org.springframework.web.bind.annotation.PostMapping;
    echo import org.springframework.web.bind.annotation.RequestParam;
    echo import org.springframework.web.bind.annotation.RestController;
    echo.
    echo @RestController
    echo public class UserController {
    echo.
    echo     private UserService userService;
    echo.
    echo     @PostMapping("/login")
    echo     public String handleLogin(@RequestParam Long userId) {
    echo         User user = userService.findUser(userId);
    echo         if (user == null) {
    echo             throw new UserNotFoundException("User not found");
    echo         }
    echo         return "Login successful for user: " + user.getUsername();
    echo     }
    echo }
) > example-src\com\example\UserController.java

echo.
echo Analysis complete for Trace ID: abc123
echo.

REM Run the analyzer using java command
java -cp "target/classes" com.example.RootCauseAnalyzer abc123 example-logs.txt example-src

pause