#!/bin/bash

# 磁偏角计算工具手动打包脚本
# 该脚本使用纯Java命令行工具打包项目，不依赖Maven或Gradle

# 确保脚本在出错时终止
set -e

# 项目配置
PROJECT_NAME="geographic-magnetic-calculator"
VERSION="1.0.0"
MAIN_CLASS="com.jin.geographic.MagneticDeclinationCalculator"

# 目录设置
SRC_DIR="src/main/java"
RESOURCES_DIR="src/main/resources"
JNI_LIB_DIR="src/main/cpp"
BUILD_DIR="build"
DIST_DIR="dist"

# 检查Java版本
echo "检查Java版本..."
java_version=$(java -version 2>&1 | awk -F[\"_] '/version/ {print $2}')
java_major_version=$(echo "$java_version" | cut -d'.' -f1)

if [ $java_major_version -lt 8 ]; then
    echo "错误: Java版本过低。需要Java 8或更高版本。"
    exit 1
fi

echo "Java版本: $java_version 已满足要求。"

# 创建构建目录
if [ -d "$BUILD_DIR" ]; then
    echo "清理构建目录..."
    rm -rf "$BUILD_DIR"
fi

mkdir -p "$BUILD_DIR/classes"
mkdir -p "$BUILD_DIR/resources"
mkdir -p "$BUILD_DIR/native-libs"

# 编译Java源代码
echo "编译Java源代码..."
javac -d "$BUILD_DIR/classes" $(find "$SRC_DIR" -name "*.java")

# 复制资源文件
echo "复制资源文件..."
if [ -d "$RESOURCES_DIR" ]; then
    cp -r "$RESOURCES_DIR"/* "$BUILD_DIR/resources/"
fi

# 检查并复制JNI本地库
JNI_LIB="libgeographiclib_jni.dylib"
if [ -f "$JNI_LIB_DIR/$JNI_LIB" ]; then
    echo "复制JNI本地库..."
    cp "$JNI_LIB_DIR/$JNI_LIB" "$BUILD_DIR/native-libs/"
else
    echo "警告: 未找到JNI本地库 $JNI_LIB"
    echo "尝试编译JNI本地库..."
    cd "$JNI_LIB_DIR"
    make all
    cd -  # 返回项目根目录
    
    if [ -f "$JNI_LIB_DIR/$JNI_LIB" ]; then
        cp "$JNI_LIB_DIR/$JNI_LIB" "$BUILD_DIR/native-libs/"
    else
        echo "错误: 无法编译JNI本地库。请检查GeographicLib依赖是否正确安装。"
        exit 1
    fi
fi

# 检查WMM2025数据文件
WMM_DATA_DIR="$BUILD_DIR/resources/magnetic"
if [ ! -d "$WMM_DATA_DIR" ]; then
    echo "警告: 未找到WMM2025数据目录"
    mkdir -p "$WMM_DATA_DIR"
fi

if [ ! -f "$WMM_DATA_DIR/wmm2025.wmm" ] || [ ! -f "$WMM_DATA_DIR/wmm2025.wmm.cof" ]; then
    echo "警告: WMM2025数据文件不完整"
    echo "尝试运行数据下载脚本..."
    if [ -f "download_wmm2025.sh" ]; then
        chmod +x download_wmm2025.sh
        ./download_wmm2025.sh
        if [ -d "magnetic" ]; then
            cp magnetic/* "$WMM_DATA_DIR/"
        fi
    else
        echo "错误: 数据下载脚本不存在。请手动获取WMM2025数据文件。"
        exit 1
    fi
fi

# 复制资源文件到classes目录，以便打包到JAR中
echo "准备打包资源文件..."
cp -r "$BUILD_DIR/resources"/* "$BUILD_DIR/classes/"

# 创建清单文件
echo "创建MANIFEST.MF文件..."
cat > "$BUILD_DIR/MANIFEST.MF" << EOF
Manifest-Version: 1.0
Created-By: $java_version
Main-Class: $MAIN_CLASS
EOF

# 打包JAR文件
echo "打包JAR文件..."
cd "$BUILD_DIR/classes"
jar cvfm "../$PROJECT_NAME-$VERSION.jar" "../MANIFEST.MF" *
cd -  # 返回项目根目录

# 创建分发目录
if [ -d "$DIST_DIR" ]; then
    echo "清理分发目录..."
    rm -rf "$DIST_DIR"
fi

mkdir -p "$DIST_DIR"

# 复制JAR文件和本地库到分发目录
echo "准备分发文件..."
cp "$BUILD_DIR/$PROJECT_NAME-$VERSION.jar" "$DIST_DIR/"
cp -r "$BUILD_DIR/native-libs" "$DIST_DIR/"

# 创建README文件，说明如何使用
echo "创建使用说明文件..."
cat > "$DIST_DIR/README.txt" << EOF
# 磁偏角计算工具使用说明

## 包含文件
- $PROJECT_NAME-$VERSION.jar: 主JAR文件
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

javac -cp $PROJECT_NAME-$VERSION.jar MagneticExample.java
java -cp .:$PROJECT_NAME-$VERSION.jar -Djava.library.path=./native-libs MagneticExample

### 2. 注意事项
- 数据有效期：WMM2025模型的数据有效期至2029年12月31日
- 单位转换：计算结果默认以弧分为单位，如需转换为度，请除以60
- 本地库兼容性：当前提供的本地库仅适用于macOS系统
- 资源释放：使用完毕后请务必调用cleanup()方法释放资源
EOF

chmod +x manual_package.sh

echo "\n打包成功!"
echo "生成的文件："
echo "- $DIST_DIR/$PROJECT_NAME-$VERSION.jar"
echo "- $DIST_DIR/native-libs/libgeographiclib_jni.dylib"
echo "- $DIST_DIR/README.txt"

echo "\n使用说明："
echo "1. 请参考$DIST_DIR/README.txt文件了解如何使用打包后的JAR文件"
echo "2. 使用时需要设置本地库路径：-Djava.library.path=./native-libs"
echo "3. 确保JAR文件已添加到类路径中"