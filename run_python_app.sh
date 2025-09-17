#!/bin/bash

# 运行Python磁偏角计算器应用程序

# 使用Anaconda Python解释器
PYTHON_EXEC=/opt/anaconda3/bin/python

# 检查Python解释器是否存在
if [ ! -f "$PYTHON_EXEC" ]; then
    echo "未找到Anaconda Python解释器，尝试使用系统Python..."
    PYTHON_EXEC=python3
fi

# 确保安装了依赖
$PYTHON_EXEC -m pip install -r requirements.txt

# 运行Python应用程序
cd src/python
$PYTHON_EXEC main.py

# 检查退出码
if [ $? -ne 0 ]; then
    echo "GUI启动失败，尝试命令行界面..."
    $PYTHON_EXEC -c "from magnetic_declination import run_command_line_interface; run_command_line_interface()"
fi