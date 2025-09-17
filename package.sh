#!/bin/bash

# 磁偏角计算工具打包脚本
# 该脚本用于将项目打包为可分发的JAR文件

# 确保脚本在出错时终止
set -e

# 检查是否安装了Maven
echo "检查Maven安装状态..."
if ! command -v mvn &> /dev/null
then
    echo "错误: 未找到Maven。请先安装Maven。"
    exit 1
fi

# 检查Java版本
echo "检查Java版本..."
java_version=$(java -version 2>&1 | awk -F[\"_] '/version/ {print $2}')
java_major_version=$(echo "$java_version" | cut -d'.' -f1)

if [ $java_major_version -lt 8 ]; then
    echo "错误: Java版本过低。需要Java 8或更高版本。"
    exit 1
fi

echo "Java版本: $java_version 已满足要求。"

# 检查JNI本地库是否存在
JNI_LIB_PATH="src/main/cpp/libgeographiclib_jni.dylib"
if [ ! -f "$JNI_LIB_PATH" ]; then
    echo "警告: 未找到JNI本地库 $JNI_LIB_PATH"
    echo "尝试编译JNI本地库..."
    cd src/main/cpp
    make all
    cd -  # 返回项目根目录
    
    if [ ! -f "$JNI_LIB_PATH" ]; then
        echo "错误: 无法编译JNI本地库。请检查GeographicLib依赖是否正确安装。"
        exit 1
    fi
fi

echo "JNI本地库检查通过。"

# 检查WMM2025数据文件
WMM_DATA_DIR="src/main/resources/magnetic"
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
    else
        echo "错误: 数据下载脚本不存在。请手动获取WMM2025数据文件。"
        exit 1
    fi
fi

echo "WMM2025数据文件检查通过。"

# 执行Maven打包命令
echo "开始使用Maven打包项目..."
mvn clean package

# 检查打包结果
TARGET_DIR="target"
if [ -f "$TARGET_DIR/geographic-magnetic-calculator-1.0.0.jar" ] && [ -f "$TARGET_DIR/geographic-magnetic-calculator-1.0.0-jar-with-dependencies.jar" ]; then
    echo "\n打包成功! 生成的文件："
    echo "- $TARGET_DIR/geographic-magnetic-calculator-1.0.0.jar"
    echo "- $TARGET_DIR/geographic-magnetic-calculator-1.0.0-jar-with-dependencies.jar"
    echo "- $TARGET_DIR/native-libs/libgeographiclib_jni.dylib"
    
    echo "\n使用指南："
    echo "1. 请参考USAGE.md文件了解如何使用打包后的JAR文件"
    echo "2. 使用时需要设置本地库路径：-Djava.library.path=./target/native-libs"
    echo "3. 确保JAR文件已添加到类路径中"
else
    echo "错误: 打包失败。请检查Maven输出信息。"
    exit 1
fi

# 为方便使用，复制关键文件到dist目录
if [ ! -d "dist" ]; then
    mkdir dist
fi

cp "$TARGET_DIR/geographic-magnetic-calculator-1.0.0-jar-with-dependencies.jar" dist/
cp -r "$TARGET_DIR/native-libs" dist/
cp USAGE.md dist/

chmod +x package.sh

echo "\n分发文件已复制到 dist/ 目录。"