# 磁偏角计算工具使用指南

本文档提供了如何使用打包好的JAR文件进行磁偏角计算的详细说明。

## 1. 前提条件

- Java 8或更高版本
- macOS系统（当前构建的本地库仅支持macOS）

## 2. 文件结构

打包后的目录结构如下：

```
graphic-magnetic-calculator/
├── target/
│   ├── geographic-magnetic-calculator-1.0.0.jar          # 主JAR文件
│   ├── geographic-magnetic-calculator-1.0.0-jar-with-dependencies.jar  # 包含依赖的JAR
│   └── native-libs/                                      # 本地库文件
│       └── libgeographiclib_jni.dylib                    # macOS本地库
└── USAGE.md                                              # 使用指南
```

## 3. 使用方法

### 3.1 基本使用步骤

1. **设置本地库路径**
   在使用JAR文件之前，需要设置Java的本地库路径，指向包含`libgeographiclib_jni.dylib`的目录。

2. **将JAR添加到类路径**
   将打包好的JAR文件添加到Java程序的类路径中。

3. **初始化计算器**
   创建`MagneticDeclinationCalculator`实例并进行初始化。

4. **计算磁偏角**
   调用相应的方法计算指定位置和时间的磁偏角。

5. **释放资源**
   使用完毕后调用`cleanup()`方法释放资源。

### 3.2 代码示例

```java
import com.jin.geographic.MagneticDeclinationCalculator;

public class MagneticExample {
    public static void main(String[] args) {
        // 初始化计算器
        MagneticDeclinationCalculator calculator = new MagneticDeclinationCalculator();
        boolean initialized = calculator.initialize();
        
        if (initialized) {
            try {
                // 计算指定位置和日期的磁偏角（示例：西安市，2025年1月1日）
                double latitude = 34.3416;   // 北纬
                double longitude = 108.9398; // 东经
                double altitude = 400.0;     // 海拔（米）
                int year = 2025;
                int month = 1;
                int day = 1;
                
                // 计算磁偏角
                var result = calculator.calculateDeclinationByDate(
                    latitude, longitude, altitude, year, month, day);
                
                if (result.isSuccess()) {
                    double declination = result.getDeclination();
                    System.out.println("磁偏角：" + declination + " 度");
                    
                    // 将弧分转换为度
                    double declinationDegrees = declination / 60.0;
                    System.out.println("转换为度：" + declinationDegrees + " 度");
                    
                    // 确定方向（东偏或西偏）
                    String direction = declinationDegrees > 0 ? "东偏" : "西偏";
                    System.out.println("方向：" + direction);
                } else {
                    System.err.println("计算失败：" + result.getMessage());
                }
            } finally {
                // 释放资源
                calculator.cleanup();
            }
        } else {
            System.err.println("初始化失败，请检查WMM2025数据文件和本地库是否正确");
        }
    }
}
```

## 4. 命令行运行方式

### 4.1 直接运行示例程序

如果JAR配置了主类，可以使用以下命令直接运行：

```bash
# 设置本地库路径并运行JAR
java -Djava.library.path=./target/native-libs -jar ./target/geographic-magnetic-calculator-1.0.0.jar
```

### 4.2 在其他Java程序中使用

在编译和运行时，需要同时指定类路径和本地库路径：

```bash
# 编译Java程序
javac -cp ./target/geographic-magnetic-calculator-1.0.0.jar YourProgram.java

# 运行Java程序
java -cp .:./target/geographic-magnetic-calculator-1.0.0.jar -Djava.library.path=./target/native-libs YourProgram
```

## 5. 注意事项

1. **数据有效期**：WMM2025模型的数据有效期至2029年12月31日

2. **单位转换**：默认计算结果以弧分为单位，如需转换为度，请除以60

3. **高度参数**：建议使用海平面高度（0米）以获得更准确的结果

4. **本地库兼容性**：当前提供的本地库仅适用于macOS系统

5. **资源释放**：使用完毕后请务必调用`cleanup()`方法释放资源

## 6. 故障排除

### 6.1 常见问题

- **找不到本地库**：确保正确设置了`-Djava.library.path`参数，指向包含`libgeographiclib_jni.dylib`的目录

- **初始化失败**：检查WMM2025数据文件是否存在并可访问

- **计算结果异常**：确保输入的经纬度、高度和日期参数格式正确

- **系统兼容性**：当前提供的本地库仅支持macOS系统，其他系统需要重新编译本地库