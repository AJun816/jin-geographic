# 磁偏角计算工具使用说明

## 包含文件
- geographic-magnetic-calculator-1.0.0.jar: 主JAR文件
- native-libs/: 包含JNI本地库
  - libgeographiclib_jni.dylib: macOS本地库

## 使用方法

### 1. 编译并运行示例代码

创建一个示例Java文件（例如MagneticExample.java）：

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
                    double declinationMinutes = result.getDeclination();
                    // 将弧分转换为度
                    double declinationDegrees = declinationMinutes / 60.0;
                    // 确定方向（东偏或西偏）
                    String direction = declinationDegrees > 0 ? "东偏" : "西偏";
                    
                    System.out.printf("%.2f° (%s)\n", Math.abs(declinationDegrees), direction);
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

编译和运行示例代码：

javac -cp geographic-magnetic-calculator-1.0.0.jar MagneticExample.java
java -cp .:geographic-magnetic-calculator-1.0.0.jar -Djava.library.path=./native-libs MagneticExample

### 2. 注意事项
- 数据有效期：WMM2025模型的数据有效期至2029年12月31日
- 单位转换：计算结果默认以弧分为单位，如需转换为度，请除以60
- 本地库兼容性：当前提供的本地库仅适用于macOS系统
- 资源释放：使用完毕后请务必调用cleanup()方法释放资源
