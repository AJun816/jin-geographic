#!/bin/bash
# 运行西安磁差计算程序的脚本

# 设置共享库路径
export DYLD_LIBRARY_PATH="$DYLD_LIBRARY_PATH:./src/main/cpp"

# 检查编译后的类文件是否存在
if [ ! -f "target/classes/com/jin/geographic/XianMagneticVariation.class" ]; then
    echo "编译XianMagneticVariation.java..."
    mkdir -p target/classes
    javac -d target/classes src/main/java/com/jin/geographic/XianMagneticVariation.java src/main/java/com/jin/geographic/GeographicLibWrapper.java
    if [ $? -ne 0 ]; then
        echo "编译失败！请检查Java代码是否正确。"
        exit 1
    fi
fi

# 运行Java程序
java -cp target/classes -Djava.library.path=./src/main/cpp com.jin.geographic.XianMagneticVariation
# Java版本注释：此脚本适用于Java 8及以上版本