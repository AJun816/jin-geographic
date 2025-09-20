# WMM2025 磁偏角计算工具

## 项目简介

本项目提供了基于 World Magnetic Model 2025 (WMM2025) 的磁偏角计算功能，用于计算地球上任意位置和时间的磁偏角（地磁偏角/磁差）。项目提供了三种实现方式：

- **C++实现**：直接封装 GeographicLib 库，提供高性能的磁偏角计算
- **Java实现**：通过 JNI (Java Native Interface) 技术封装 C++ 实现，提供简洁易用的 Java 接口
- **JavaScript实现**：纯前端实现，无需后端服务器支持，可在浏览器中直接运行，特别优化了北京和陕西咸阳地区的计算精度

项目适用于地质勘探、测绘导航、地理信息系统等需要地磁数据的应用场景。

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
├── .gitignore            # Git 忽略文件配置
├── .vscode/              # VS Code 编辑器配置
├── README.md             # 项目说明文档
├── cpp/                  # C++ 实现目录
│   ├── GeographicLibWrapper.cpp  # GeographicLib C++ 封装
│   ├── Makefile          # C++ 编译配置
│   ├── README_CPP.md     # C++ 实现说明
│   ├── include/          # C++ 头文件
│   └── libgeographiclib_jni.dylib # 编译后的 C++ 共享库
├── java/                 # Java 实现目录
│   ├── README.md         # Java 实现说明
│   ├── src/              # Java 源代码
│   └── resources/        # Java 资源文件
├── magnetic-declination-js/ # JavaScript 实现目录
│   ├── LatLon.js         # 经纬度处理类
│   ├── WorldMagneticModel.js # 世界地磁模型实现
│   ├── MagneticDeclinationCalculator.js # 磁偏角计算器主类
│   ├── magnetic-declination-calculator.html # HTML 交互界面
│   ├── resources/        # JavaScript 资源文件
│   │   ├── wmm2025.wmm   # WMM2025 模型文件
│   │   └── wmm2025.wmm.cof # WMM2025 系数文件
│   ├── test-magnetic-declination.js # JavaScript 测试脚本
│   └── README.md         # JavaScript 实现说明
├── dist/                 # 分发文件目录
└── resources/            # 公共资源文件目录
    ├── wmm2025.wmm       # WMM2025 模型文件
    └── wmm2025.wmm.cof   # WMM2025 系数文件
```

## 核心功能

### 通用功能

1. **通用磁偏角计算**：支持通过经纬度、高度和时间计算地球上任意位置的磁偏角
2. **日期与儒略日支持**：提供基于日期（年、月、日）和儒略日两种时间输入方式
3. **完整错误处理**：包含结果封装和错误信息传递
4. **多平台支持**：跨平台兼容，可在Windows、MacOS和Linux系统上运行

### C++ 实现特点

1. **高性能计算**：直接调用GeographicLib库，计算效率高
2. **原生系统兼容**：提供各平台的共享库文件
3. **底层优化**：针对计算密集型场景进行了性能优化

### Java 实现特点

1. **简洁易用接口**：提供直观的Java API，隐藏底层实现细节
2. **JNI技术桥接**：通过JNI技术实现Java与C++的高效互操作
3. **完整资源管理**：提供资源初始化和清理机制
4. **调试辅助工具**：内置DebugHelper类，便于问题排查

### JavaScript 实现特点

1. **纯前端实现**：无需后端服务器支持，可在浏览器中直接运行
2. **度分秒支持**：提供度分秒(DMS)和十进制度(Decimal Degrees)两种格式的输入
3. **北京和咸阳地区优化**：针对这两个地区进行了算法优化，提高计算精度
4. **完全本地计算**：所有计算在本地完成，保护数据隐私
5. **HTML交互界面**：提供直观的用户界面，方便输入参数并查看结果

### JavaScript版本安装与配置

JavaScript版本是最简单易用的实现，无需任何安装和配置，直接在浏览器中运行。

#### 使用方法

1. **直接打开HTML文件**
   - 找到`magnetic-declination-js`目录
   - 双击打开`magnetic-declination-calculator.html`文件
   - 在界面上输入度分秒格式的经纬度、高度和年份
   - 点击"计算磁偏角"按钮获取结果

2. **作为JavaScript库引入**
   - 将`magnetic-declination-js`目录中的三个核心JavaScript文件引入到您的项目中：
     ```html
     <script src="LatLon.js"></script>
     <script src="WorldMagneticModel.js"></script>
     <script src="MagneticDeclinationCalculator.js"></script>
     ```
   - 然后可以在您的JavaScript代码中使用：
     ```javascript
     // 获取计算器实例
     const calculator = MagneticDeclinationCalculator.getInstance();
     
     // 使用度数格式计算磁偏角
     const declination = calculator.calculateDeclination(39.9042, 116.4074, 50, 2025.5);
     console.log(`磁偏角：${declination.toFixed(3)}°`);
     ```

**注意**：JavaScript版本在浏览器的安全限制下可能无法加载本地资源文件，但仍会使用内置的默认参数进行计算。

### Java版本安装与配置

#### 前提条件

1. Java JDK 8 或更高版本 - 项目已在Java 17环境下测试通过
2. 支持 C++ 共享库的运行环境
   - MacOS: 自带支持
   - Linux: 可能需要安装额外的依赖库
   - Windows: 需要适当的C++运行时环境

**注意**：WMM2025 数据集已包含在项目内部，无需额外下载

### Python版本安装与配置

#### 前提条件

1. Python 3.6 或更高版本
2. NumPy 库（项目依赖已在 requirements.txt 中指定）

#### 安装步骤

1. **安装依赖**
   
   项目已提供 requirements.txt 文件，包含必要的依赖项。可以使用 pip 安装：
   
   ```bash
   pip install -r requirements.txt
   ```
   
2. **使用运行脚本启动（推荐）**
   
   项目提供了便捷的启动脚本，可以直接运行：
   
   ```bash
   chmod +x run_python_app.sh  # 首次运行需要添加执行权限
   ./run_python_app.sh
   ```
   
   该脚本会自动检查依赖并尝试启动GUI界面，如果GUI不可用则启动命令行界面。

### Java版本兼容性

- 项目支持Java 8及以上版本的JDK/JRE
- 已在Java 17环境下测试通过
- 代码中主要使用了Java 8引入的java.time包（如LocalDateTime、DateTimeFormatter等），但没有使用Java 9及以上特有的特性
- 经分析，项目完全兼容Java 8运行环境
- 如遇到"无效源的发行版"错误，请检查脚本文件格式是否正确，确保文件末尾没有奇怪字符

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

## 核心类/模块说明

### JavaScript版本核心类

JavaScript版本提供了三个核心类，它们相互配合完成磁偏角计算功能：

#### LatLon
用于处理经纬度的表示和转换，支持度分秒与十进制度的相互转换。

**主要方法**：

- `static fromDegrees(degrees)`: 从十进制度数创建LatLon对象
- `static fromDMS(dmsString)`: 从度分秒字符串创建LatLon对象
- `toDegrees()`: 转换为十进制度数
- `toDMS()`: 转换为度分秒格式的字符串

#### WorldMagneticModel
封装了世界地磁模型的核心计算逻辑，基于WMM2025模型实现。

**主要方法**：

- `calculate(latitude, longitude, altitude, year)`: 计算指定位置和时间的地磁场参数
- `getDeclination(latitude, longitude, altitude, year)`: 获取指定位置和时间的磁偏角

#### MagneticDeclinationCalculator
JavaScript版本的主要计算器类，提供单例模式访问，封装了所有计算功能。

**主要方法**：

- `static getInstance()`: 获取计算器的单例实例
- `calculateDeclination(latitude, longitude, altitude, year)`: 计算磁偏角（返回度数）
- `calculateDeclinationByDMS(latitudeDMS, longitudeDMS, altitude, year)`: 使用度分秒格式计算磁偏角
- `validateCoordinates(latitude, longitude)`: 验证经纬度参数是否有效
- `validateAltitude(altitude)`: 验证高度参数是否有效
- `validateYear(year)`: 验证年份参数是否有效

### Java版本核心类

#### MagneticDeclinationCalculator

主要的磁偏角计算类，提供了初始化、计算和资源清理功能。

**主要方法**：

- `boolean initialize()`: 初始化计算器，加载 WMM2025 数据
- `MagneticDeclinationResult calculateDeclinationByDate(double latitude, double longitude, double altitude, int year, int month, int day)`: 根据日期计算磁偏角
- `MagneticDeclinationResult calculateDeclination(double latitude, double longitude, double altitude, double julianDay)`: 根据儒略日计算磁偏角
- `void cleanup()`: 清理资源

#### MagneticDeclinationResult

磁偏角计算结果类，封装了计算结果和状态信息。

**主要方法**：

- `double getDeclination()`: 获取计算得到的磁偏角值（单位：弧分）
- `boolean isSuccess()`: 检查计算是否成功
- `String getMessage()`: 获取结果消息或错误信息

### Python版本核心模块

#### magnetic_declination 模块

Python版本的核心模块，提供了完整的磁偏角计算功能。

**主要函数**：

- `calculate_declination(latitude, longitude, altitude, year, month, day)`: 根据日期计算磁偏角
- `calculate_declination_by_julian_day(latitude, longitude, altitude, julian_day)`: 根据儒略日计算磁偏角
- `convert_date_to_julian_day(year, month, day)`: 将日期转换为儒略日
- `convert_julian_day_to_date(julian_day)`: 将儒略日转换为日期
- `validate_coordinates(latitude, longitude)`: 验证经纬度参数是否有效
- `validate_altitude(altitude)`: 验证高度参数是否有效
- `validate_date(year, month, day)`: 验证日期参数是否有效

## 使用示例

### JavaScript版本使用示例

JavaScript版本提供了简洁的API接口，可以在浏览器中直接使用，也可以作为库引入到您的项目中。以下是主要使用方法的示例：

#### 1. 浏览器直接使用（HTML界面）

项目提供了完整的HTML界面，您可以直接打开使用：

```html
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>磁偏角计算器</title>
    <style>
        /* 样式省略 */
    </style>
</head>
<body>
    <div class="calculator-container">
        <h2>磁偏角计算器</h2>
        <!-- 输入表单和结果显示区域 -->
        <div class="result" id="result"></div>
    </div>
    
    <script src="LatLon.js"></script>
    <script src="WorldMagneticModel.js"></script>
    <script src="MagneticDeclinationCalculator.js"></script>
    <script>
        // 简单实现示例
        document.getElementById('calculateBtn').addEventListener('click', function() {
            const calculator = MagneticDeclinationCalculator.getInstance();
            // 获取表单输入并计算磁偏角
            // 显示计算结果
        });
    </script>
</body>
</html>
```

#### 2. 作为JavaScript库使用

您可以将JavaScript版本作为库引入到您的项目中，直接调用API进行计算：

```javascript
// 获取计算器实例
const calculator = MagneticDeclinationCalculator.getInstance();

// 验证参数
if (!calculator.validateCoordinates(39.9042, 116.4074)) {
    console.log("经纬度参数无效");
    return;
}

if (!calculator.validateYear(2025.5)) {
    console.log("年份参数无效");
    return;
}

// 使用十进制度计算磁偏角（返回度数）
const declination = calculator.calculateDeclination(39.9042, 116.4074, 50, 2025.5);
const direction = declination > 0 ? "东偏" : "西偏";

console.log(`磁偏角：${Math.abs(declination).toFixed(6)}度 (${direction})`);

// 也可以使用度分秒格式计算
const declinationDMS = calculator.calculateDeclinationByDMS("39°54′15.12″N", "116°24′26.64″E", 50, 2025.5);
console.log(`使用度分秒计算的磁偏角：${Math.abs(declinationDMS).toFixed(6)}度`);
```

### Java版本使用示例

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

### Python版本使用示例

Python版本提供了简洁明了的函数接口，可以直接调用计算磁偏角。以下是主要使用方法的示例：

```python
# 导入模块
from src.python.magnetic_declination import calculate_declination, validate_coordinates, validate_date

# 验证参数
if not validate_coordinates(latitude=34.3416, longitude=108.9398):
    print("经纬度参数无效")
    exit()

if not validate_date(year=2025, month=1, day=1):
    print("日期参数无效")
    exit()

# 计算磁偏角（返回弧分单位）
result = calculate_declination(
    latitude=34.3416,  # 纬度（西安）
    longitude=108.9398,  # 经度（西安）
    altitude=400,  # 高度（米）
    year=2025,  # 年份
    month=1,  # 月份
    day=1  # 日期
)

# 处理结果
if result['success']:
    declination_minutes = result['declination']
    declination_degrees = declination_minutes / 60.0  # 转换为度
    direction = "东偏" if declination_minutes > 0 else "西偏"
    
    print(f"磁偏角：{abs(declination_degrees):.6f}度 ({direction})")
else:
    print(f"计算失败：{result['message']}")
```

## 运行示例

### JavaScript版本运行示例

JavaScript版本的运行方式非常简单，不需要任何编译或安装步骤，可以直接在浏览器中运行。

#### 1. 直接打开HTML文件（推荐）

项目提供了完整的HTML界面文件，您只需双击打开即可使用：

1. 找到`magnetic-declination-js`目录
2. 双击打开`magnetic-declination-calculator.html`文件
3. 浏览器会自动加载并显示计算器界面
4. 在界面上输入经纬度（支持度分秒格式）、高度和年份
5. 点击"计算磁偏角"按钮获取结果

#### 2. 通过本地HTTP服务器运行（适合开发测试）

如果您需要在本地开发环境中测试JavaScript版本，可以使用简单的HTTP服务器：

```bash
# 进入magnetic-declination-js目录
cd magnetic-declination-js

# 使用Python启动简单的HTTP服务器
python -m http.server 8000

# 或使用Node.js的http-server包
npx http-server -p 8000
```

然后在浏览器中访问：`http://localhost:8000/magnetic-declination-calculator.html`

### Java版本运行示例

项目提供了两个主要的Java版本运行示例：

#### 1. 使用脚本运行西安地区磁偏角计算

```bash
./run_xian_magnetic.sh
```

运行后，将显示西安（纬度34.3416，经度108.9398）在2025年至2030年期间的磁偏角数据。

#### 2. 编译并运行完整示例程序

```bash
# 编译示例程序
javac -cp src/main/java src/main/java/com/jin/geographic/MagneticExample.java

# 运行示例程序
java -cp src/main/java -Djava.library.path=./src/main/cpp com.jin.geographic.MagneticExample
```

运行后，将显示多个城市的磁偏角计算结果以及北京地区多年的磁偏角变化趋势。

### Python版本运行示例

Python版本提供了多种运行方式：

#### 1. 使用提供的运行脚本（推荐）

```bash
./run_python_app.sh
```

该脚本会自动检测系统环境，并根据情况启动GUI界面或命令行界面。

#### 2. 直接运行Python主程序

```bash
python src/python/main.py
```

#### 3. 运行Python测试用例

项目提供了完整的单元测试，可以验证功能是否正常：

```bash
python -m unittest discover -s tests
```

运行后，将显示8个测试用例的执行结果，全部通过表示功能正常。

## 注意事项

### 通用注意事项

1. **数据有效期**：WMM2025 模型的最佳预测期为 2025 年 1 月 1 日至 2029 年 12 月 31 日。对于超出此范围的日期，预测精度可能会降低。

2. **单位转换**：计算结果默认以弧分为单位，如需转换为度，请除以60

3. **高度参数**：高度参数以米为单位，建议使用标准海平面高度（0米）以获得最准确的结果。

4. **错误处理**：请务必检查结果中的成功标志，以确保计算结果有效。

### JavaScript版本特殊注意事项

1. **浏览器兼容性**：JavaScript版本在现代浏览器（Chrome、Firefox、Safari、Edge）中均可正常运行

2. **安全限制**：在浏览器的安全限制下，JavaScript版本可能无法直接加载本地文件，但仍会使用内置的默认参数进行计算

3. **精度优化**：JavaScript版本针对北京和陕西咸阳地区进行了特殊的精度优化

4. **单位**：JavaScript版本的计算结果直接以度为单位返回，无需额外转换

5. **数据有效期**：与其他版本相同，WMM2025模型的最佳预测期为2025年1月1日至2029年12月31日

### Python版本特殊注意事项

1. **环境配置**：确保使用正确的Python环境，特别是在安装了多个Python版本的系统上

2. **依赖管理**：使用pip安装requirements.txt中指定的依赖

3. **GUI可用性**：GUI界面需要安装额外的依赖库，如果无法使用GUI，程序会自动切换到命令行界面

4. **测试验证**：建议在使用前运行单元测试确保功能正常：`python -m unittest discover -s tests`

## 故障排除

### JavaScript版本故障排除

1. **浏览器兼容性问题**：如果在某些浏览器中无法正常运行，请尝试使用最新版本的Chrome、Firefox、Safari或Edge浏览器

2. **计算结果与预期不符**：
   - 检查输入的经纬度格式是否正确（支持度分秒或十进制度）
   - 确认年份在WMM2025模型的有效预测期内（2025-2029年）
   - 高度参数以米为单位，请确保输入正确

3. **页面加载错误**：
   - 检查三个核心JavaScript文件（LatLon.js、WorldMagneticModel.js、MagneticDeclinationCalculator.js）是否在正确的位置
   - 如果通过本地文件系统直接打开HTML文件，某些浏览器可能会有安全限制，建议使用HTTP服务器方式运行

### Java和Python版本故障排除

1. **初始化失败**：
   - 项目已包含数据集，通常不需要额外配置
   - 如果仍有问题，请确保 `src/main/resources/magnetic/` 目录下包含正确的数据文件
   - 检查 `java.library.path` 是否包含 `src/main/cpp` 目录

2. **计算结果异常**：确保提供的经纬度和日期参数在有效范围内。

3. **JNI 错误**：检查共享库是否正确编译，以及 `java.library.path` 是否包含共享库所在目录。

4. **跨平台兼容性**：在不同操作系统上运行时，可能需要重新编译 C++ 共享库。

## 打包说明

### JavaScript版本打包说明

JavaScript版本的打包非常简单，因为它是纯前端实现，不需要任何编译或依赖管理。

1. **必要文件**：
   - `magnetic-declination-calculator.html`：主界面文件
   - `LatLon.js`：经纬度处理类
   - `WorldMagneticModel.js`：地磁模型实现
   - `MagneticDeclinationCalculator.js`：计算器主类
   - 可选的CSS文件（如果有）

2. **打包方式**：
   只需将上述文件复制到目标位置即可，无需额外的打包工具。

3. **接收方使用方法**：
   - 双击HTML文件直接在浏览器中打开
   - 或通过HTTP服务器提供访问

### Java版本打包说明

要将Java版本打包发送给其他Java后端同事使用，请按照以下步骤操作：

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