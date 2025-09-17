# WMM2025 磁偏角计算工具

## 项目简介

本项目提供了基于 World Magnetic Model 2025 (WMM2025) 的磁偏角计算功能，用于计算地球上任意位置和时间的磁偏角（地磁偏角/磁差）。项目通过 JNI (Java Native Interface) 技术封装了 GeographicLib C++ 库，提供了简洁易用的 Java 接口，适用于需要地磁数据的导航、测绘和地理信息系统等应用场景。

## 实现原理

本项目采用分层架构设计，通过 JNI 技术桥接 Java 和 C++ 代码，实现了高效的磁偏角计算功能。整体实现流程如下：

### 1. 架构分层

- **Java接口层**：提供面向用户的 Java API，处理参数验证、结果封装和异常转换
- **JNI桥接层**：负责 Java 与 C++ 之间的数据转换和方法调用
- **C++实现层**：封装 GeographicLib 库，调用 WMM2025 模型进行实际计算

### 2. 核心数据流

1. **初始化流程**：
   - Java 端调用 `MagneticDeclinationCalculator.initialize()`
   - 通过 JNI 调用 C++ 端 `Java_com_jin_geographic_GeographicLibWrapper_initialize()`
   - C++ 端加载 WMM2025 数据集，初始化 `GeographicLib::MagneticModel` 实例

2. **计算流程**：
   - Java 端调用 `calculateDeclinationByDate()` 或 `calculateDeclination()`
   - 转换时间格式（如有必要），调用 JNI 方法
   - C++ 端调用 `GeographicLib::MagneticModel` 计算磁场分量
   - 计算磁偏角并返回结果给 Java 端

3. **资源管理**：
   - Java 端调用 `cleanup()` 释放资源
   - C++ 端释放 `MagneticModel` 实例和相关资源

### 3. JNI 调用机制

项目通过 JNI 实现 Java 与 C++ 的互操作，关键机制包括：
- 函数命名约定：遵循 `Java_package_class_method` 格式
- 数据类型映射：Java 类型与 C++ 类型的对应转换
- 内存管理：避免内存泄漏，正确释放 JNI 资源

## 调试指南

### 调试辅助类 (DebugHelper)

项目提供了专门的 `DebugHelper` 类，帮助Java程序员更方便地进行调试和问题排查。该类提供了参数验证、日志记录、性能计时等多种调试工具。

**主要功能：**
- 启用/禁用调试模式
- 记录详细的调试日志
- 验证经纬度、日期和高度参数
- 打印计算结果的详细信息
- 测试JNI连接是否正常
- 获取和打印环境信息
- 性能计时功能

**使用示例：**

```java
// 启用调试模式
DebugHelper.enableDebug();

// 可选：将日志输出到文件
DebugHelper.setDebugLogFile("magnetic_debug.log");

// 打印环境信息
DebugHelper.printEnvironmentInfo();

// 验证参数
if (!DebugHelper.validateCoordinates(latitude, longitude)) {
    System.out.println("经纬度参数无效");
    return;
}

if (!DebugHelper.validateDate(year, month, day)) {
    System.out.println("日期参数无效");
    return;
}

// 开始计时
DebugHelper.startTimer();

// 执行计算
MagneticDeclinationResult result = calculator.calculateDeclinationByDate(
        latitude, longitude, altitude, year, month, day);

// 结束计时
DebugHelper.endTimer("磁偏角计算");

// 打印结果详情
DebugHelper.printResultDetails(result);
```

### Java 层调试

1. **检查初始化状态**
   - 初始化失败通常是 WMM2025 数据集未正确加载
   - 可以通过 `calculator.initialize()` 的返回值确认初始化状态
   - 查看 `MagneticDeclinationResult.getMessage()` 获取详细错误信息

2. **参数验证**
   - 使用 `DebugHelper.validateCoordinates()` 验证经纬度
   - 使用 `DebugHelper.validateDate()` 验证日期参数
   - 使用 `DebugHelper.validateAltitude()` 验证高度参数

3. **异常处理**
   - 捕获并记录 `MagneticDeclinationCalculator` 相关方法可能抛出的异常
   - 检查 `MagneticDeclinationResult.isSuccess()` 确认计算是否成功
   - 使用 `DebugHelper.printResultDetails()` 查看完整的计算结果信息

### JNI 层调试

1. **共享库加载问题**
   - 确保 `System.loadLibrary("geographiclib_jni")` 能够找到共享库
   - 检查 `java.library.path` 是否包含 `src/main/cpp` 目录
   - 对于 MacOS，可通过 `DYLD_LIBRARY_PATH` 环境变量设置

2. **调用错误排查**
   - JNI 调用失败可能会导致 `UnsatisfiedLinkError`
   - 确保 C++ 端函数签名与 Java 端 native 方法完全匹配
   - 检查日志中的 C++ 错误输出（通常输出到标准错误流）

### C++ 层调试

1. **查看错误日志**
   - C++ 端的错误信息会输出到标准错误流
   - 可以通过重定向标准错误流查看详细错误信息

2. **验证 WMM2025 数据**
   - 确认 `download_wmm2025.sh` 脚本正确下载了数据
   - 数据通常位于系统默认位置或通过 `initialize()` 方法指定的路径

## 项目结构

```
├── src/                  # 源代码目录
│   └── main/             # 主要代码
│       ├── cpp/          # C++ 共享库，封装了 GeographicLib
│       ├── java/         # Java 源代码
│       └── resources/    # 资源文件
│           └── magnetic/ # WMM2025 数据集文件
├── download_wmm2025.sh   # 下载 WMM2025 数据集的脚本（可选，项目已包含数据）
├── run_xian_magnetic.sh  # 西安地区磁偏角计算示例脚本
├── get_java_home.sh      # 获取 Java 环境的辅助脚本
└── README.md             # 项目说明文档
```

## 核心功能

1. **通用磁偏角计算**：支持通过经纬度、高度和时间计算地球上任意位置的磁偏角
2. **日期与儒略日支持**：提供基于日期（年、月、日）和儒略日两种时间输入方式
3. **完整错误处理**：包含结果封装和错误信息传递
4. **简单易用接口**：提供直观的 Java 接口，隐藏底层实现细节

## 安装与配置

### 前提条件

1. Java JDK 8 或更高版本
2. 支持 C++ 共享库的运行环境
   - MacOS: 自带支持
   - Linux: 可能需要安装额外的依赖库
   - Windows: 需要适当的C++运行时环境

**注意**：WMM2025 数据集已包含在项目内部，无需额外下载

### 安装步骤

1. **项目已包含 WMM2025 数据集**
   本项目已将 WMM2025 数据集作为内部资源包含在内，无需额外下载。
   程序会自动从项目的 resources/magnetic 目录加载数据，或在 JAR 包运行时自动提取。

2. **确保共享库路径正确**
   确保 `src/main/cpp` 目录下包含正确编译的共享库文件

3. **配置 Java 环境**
   ```bash
   source get_java_home.sh
   ```

## 核心类说明

### MagneticDeclinationCalculator

主要的磁偏角计算类，提供了初始化、计算和资源清理功能。

**主要方法**：

- `boolean initialize()`: 初始化计算器，加载 WMM2025 数据
- `MagneticDeclinationResult calculateDeclinationByDate(double latitude, double longitude, double altitude, int year, int month, int day)`: 根据日期计算磁偏角
- `MagneticDeclinationResult calculateDeclination(double latitude, double longitude, double altitude, double julianDay)`: 根据儒略日计算磁偏角
- `void cleanup()`: 清理资源

### MagneticDeclinationResult

磁偏角计算结果类，封装了计算结果和状态信息。

**主要方法**：

- `double getDeclination()`: 获取计算得到的磁偏角值（单位：弧分）
- `boolean isSuccess()`: 检查计算是否成功
- `String getMessage()`: 获取结果消息或错误信息

## 使用示例

项目中提供了完整的使用示例文件 `MagneticExample.java`，展示了如何使用核心功能计算磁偏角，包括：

1. 基本初始化和资源管理
2. 计算多个城市的磁偏角
3. 分析不同年份的磁偏角变化趋势

示例文件路径：`src/main/java/com/jin/geographic/MagneticExample.java`

以下是主要使用方法的简化示例：

```java
// 创建计算器实例
MagneticDeclinationCalculator calculator = new MagneticDeclinationCalculator();

// 初始化计算器
boolean initialized = calculator.initialize();

if (initialized) {
    // 计算指定位置和时间的磁偏角
    MagneticDeclinationResult result = calculator.calculateDeclinationByDate(
            纬度, 经度, 高度, 年份, 月份, 日期);
    
    if (result.isSuccess()) {
        double declinationMinutes = result.getDeclination(); // 弧分单位
        double declinationDegrees = declinationMinutes / 60.0; // 转换为度
        String direction = declinationMinutes > 0 ? "东偏" : "西偏";
        
        // 输出结果
        System.out.printf("磁偏角：%.6f度 (%s)\n", 
                         Math.abs(declinationDegrees), direction);
    }
    
    // 计算完成后清理资源
    calculator.cleanup();
}
```

## 运行示例

项目提供了两个主要的运行示例：

### 1. 使用脚本运行西安地区磁偏角计算

```bash
./run_xian_magnetic.sh
```

运行后，将显示西安（纬度34.3416，经度108.9398）在2025年至2030年期间的磁偏角数据。

### 2. 编译并运行完整示例程序

```bash
# 编译示例程序
javac -cp src/main/java src/main/java/com/jin/geographic/MagneticExample.java

# 运行示例程序
java -cp src/main/java -Djava.library.path=./src/main/cpp com.jin.geographic.MagneticExample
```

运行后，将显示多个城市的磁偏角计算结果以及北京地区多年的磁偏角变化趋势。

## 注意事项

1. **数据有效期**：WMM2025 模型的最佳预测期为 2025 年 1 月 1 日至 2029 年 12 月 31 日。对于超出此范围的日期，预测精度可能会降低。

2. **单位转换**：计算结果默认以弧分为单位，如需转换为度，请除以60

3. **高度参数**：高度参数以米为单位，建议使用标准海平面高度（0米）以获得最准确的结果。

4. **错误处理**：请务必检查 `MagneticDeclinationResult.isSuccess()` 的返回值，以确保计算结果有效。

## 故障排除

1. **初始化失败**：
   - 项目已包含数据集，通常不需要额外配置
   - 如果仍有问题，请确保 `src/main/resources/magnetic/` 目录下包含正确的数据文件
   - 检查 `java.library.path` 是否包含 `src/main/cpp` 目录

2. **计算结果异常**：确保提供的经纬度和日期参数在有效范围内。

3. **JNI 错误**：检查共享库是否正确编译，以及 `java.library.path` 是否包含共享库所在目录。

4. **跨平台兼容性**：在不同操作系统上运行时，可能需要重新编译 C++ 共享库。

## 打包说明

要将项目打包发送给其他 Java 后端同事使用，请按照以下步骤操作：

### 1. 确保所有必要文件已包含

项目已自动包含以下必要文件：
- Java 源代码和编译后的类文件
- C++ 共享库文件 (libgeographiclib_jni.dylib)
- WMM2025 数据集文件 (resources/magnetic/)
- 辅助脚本文件
- README.md 说明文档

### 2. 打包项目

您可以使用以下命令将项目打包为可分发的 zip 文件：

```bash
# 确保在项目根目录下
cd /Users/ajun/projects/projects/wmm/jin-Geographic

# 打包项目（包含所有必要文件）
zip -r wmm2025_magnetic_declination.zip . -x "*.git*" -x "*.DS_Store*" -x "target/*" -x "*.log"
```

### 3. 使用说明（给接收方）

收到打包文件后，其他开发者可以：

1. 解压 zip 文件
2. 确保已安装 Java JDK 8 或更高版本
3. 设置 Java 环境：`source get_java_home.sh`
4. 运行示例：`java -cp src/main/java -Djava.library.path=src/main/cpp com.jin.geographic.MagneticExample`

**重要说明**：
- 程序会自动从项目内部资源加载 WMM2025 数据集
- 在 JAR 包运行模式下，程序会自动提取数据到临时目录
- 无需额外下载或配置数据集
- C++ 共享库需要在目标系统上兼容，如果在不同操作系统上运行，可能需要重新编译

## 联系与支持

如有任何问题或需要技术支持，请联系项目维护人员。