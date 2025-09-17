#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
地理磁计算器主入口
"""

from magnetic_declination import create_simple_gui, run_command_line_interface
import sys


def main():
    """主函数"""
    try:
        create_simple_gui()
    except Exception as e:
        print(f"GUI启动失败: {e}")
        print("切换到命令行界面...")
        run_command_line_interface()


if __name__ == "__main__":
    main()