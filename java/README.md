# 磁差计算工具类库

这是一个用Java编写的磁差计算工具类库，可以将度分秒格式的经纬度转换为度数格式，并使用世界地磁模型(WMM)计算磁差。

## 功能特点

- 支持度分秒(DMS)和度数(Decimal Degrees)两种格式的经纬度表示
- 支持度分秒和度数格式之间的相互转换
- 基于世界地磁模型(WMM2025)计算磁差
- 提供控制台交互式Demo程序，方便用户验证结果
- 单例模式设计，确保资源高效利用

## 项目结构

```
src/
├── main/
│   ├── java/com/magnetic/declination/
│   │   ├── model/         # 数据模型
│   │   │   ├── LatLon.java             # 经纬度模型类
│   │   │   └── WorldMagneticModel.java # 世界地磁模型类
│   │   ├── utils/         # 工具类
│   │   │   └── MagneticDeclinationCalculator.java # 磁差计算器工具类
│   │   └── demo/          # 演示程序
│   │       └── MagneticDeclinationDemo.java # 控制台交互式Demo
│   └── resources/         # 资源文件
└── test/                  # 测试代码
```

## 核心类说明

### 1. LatLon 类

用于表示经纬度，支持度分秒和度数两种创建方式。

**主要方法**：
- `LatLon(double latitude, double longitude, double height, double year)` - 使用度数格式创建
- `LatLon(int latDegrees, int latMinutes, double latSeconds, ...)` - 使用度分秒格式创建
- 各种getter和setter方法

### 2. WorldMagneticModel 类

用于解析WMM模型文件并计算磁差。

**主要方法**：
- `loadModel(String modelFilePath, String coefficientsFilePath)` - 加载模型文件
- `calculateDeclination(LatLon latLon)` - 计算给定位置的磁差

### 3. MagneticDeclinationCalculator 类

核心工具类，提供计算磁差的主要功能，采用单例模式。

**主要方法**：
- `getInstance()` - 获取单例实例
- `loadModel(String modelFilePath, String coefficientsFilePath)` - 加载WMM模型
- `calculateDeclination(double latitude, double longitude, double height, double year)` - 使用度数格式计算磁差
- `calculateDeclination(int latDegrees, int latMinutes, double latSeconds, ...)` - 使用度分秒格式计算磁差
- `convertDMStoDegrees(int degrees, int minutes, double seconds, boolean isPositive)` - 度分秒转度数
- `convertDegreesToDMS(double degrees)` - 度数转度分秒

## 使用示例

### 1. 基本使用

```java
// 获取计算器实例
MagneticDeclinationCalculator calculator = MagneticDeclinationCalculator.getInstance();

// 加载模型文件
calculator.loadModel("wmm2025.wmm", "wmm2025.wmm.cof");

// 使用度数格式计算磁差（北京为例）
double latitude = 39.9042; // 北纬39.9042度
double longitude = 116.4074; // 东经116.4074度
double height = 50; // 高度50米
double year = 2025.5; // 2025年中
double declination = calculator.calculateDeclination(latitude, longitude, height, year);

System.out.println("磁差：" + declination + " 度");

// 使用度分秒格式计算磁差（北京为例）
double declinationDMS = calculator.calculateDeclination(
    39, 54, 15.12, // 北纬39°54′15.12″
    116, 24, 26.64, // 东经116°24′26.64″
    true, true, // 北纬、东经
    50, 2025.5 // 高度50米，2025年中
);

System.out.println("使用度分秒计算的磁差：" + declinationDMS + " 度");
```

### 2. 格式转换

```java
// 度分秒转度数
int degrees = 39;
int minutes = 54;
double seconds = 15.12;
boolean isPositive = true; // 北纬或东经
double decimalDegrees = calculator.convertDMStoDegrees(degrees, minutes, seconds, isPositive);

// 度数转度分秒
Object[] dms = calculator.convertDegreesToDMS(decimalDegrees);
int d = (int) dms[0];
int m = (int) dms[1];
double s = (double) dms[2];
boolean pos = (boolean) dms[3];
```

## 运行Demo程序

控制台交互式Demo程序提供了以下功能：

1. 使用度分秒格式计算磁差
2. 使用度数格式计算磁差
3. 度分秒与度数格式转换
4. 查看模型信息
5. 退出程序

### 编译和运行

在项目根目录下执行以下命令：

```bash
# 编译代码
javac -d bin src/main/java/com/magnetic/declination/**/*.java

# 运行Demo程序
java -cp bin com.magnetic.declination.demo.MagneticDeclinationDemo
```

## WMM模型文件说明

本工具使用世界地磁模型(WMM2025)进行磁差计算。项目中包含两个必要的模型文件：

- `wmm2025.wmm` - 包含模型的元数据信息
- `wmm2025.wmm.cof` - 包含模型的系数数据

这些文件需要放置在程序运行的当前目录下。

## 注意事项

1. 本工具的磁差计算结果基于WMM2025模型，适用于2025-2030年期间
2. 高度适用范围为-1000米到850000米
3. 磁差计算结果为正值表示磁北偏东，负值表示磁北偏西
4. 由于WMM模型系数文件是二进制格式，当前实现使用了简化的计算方法

## 参考资料

- [World Magnetic Model](https://www.ncei.noaa.gov/products/world-magnetic-model)
- [WMM Format Documentation](http://geographiclib.sf.net/html/magnetic.html#magneticformat)