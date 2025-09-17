#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
磁差计算工具

此工具使用World Magnetic Model (WMM)来计算给定位置和时间的磁偏角（真北和磁北之间的角度差）。
"""

import numpy as np
import datetime
from typing import Tuple, Optional


class MagneticDeclinationCalculator:
    """磁偏角计算器类"""
    
    def __init__(self):
        # 初始化WMM系数
        # 这里使用简化的系数，实际应用中应该从NOAA获取最新的WMM模型数据
        self.initialize_wmm_coefficients()
    
    def initialize_wmm_coefficients(self):
        """初始化WMM模型系数"""
        # 注意：这是简化版的系数，实际应用中需要使用最新的WMM模型
        self.a = np.array([
            30000.0, -2000.0, -4900.0,
            2100.0, 1300.0, -2300.0,
            700.0, 800.0, -1100.0
        ])
        self.b = np.array([
            0.0, 2000.0, -1000.0,
            -300.0, 900.0, 700.0,
            600.0, -500.0, -400.0
        ])
    
    def calculate_declination(self, latitude: float, longitude: float, altitude: float = 0, 
                            date: Optional[datetime.datetime] = None) -> float:
        """
        计算给定位置和时间的磁偏角
        
        参数:
            latitude: 纬度（度）
            longitude: 经度（度）
            altitude: 高度（米，默认为0）
            date: 日期时间（默认为当前时间）
        
        返回:
            磁偏角（度）
        """
        if date is None:
            date = datetime.datetime.now()
        
        # 转换角度为弧度
        lat_rad = np.radians(latitude)
        lon_rad = np.radians(longitude)
        
        # 计算年份分数（用于时间依赖计算）
        year_fraction = self._calculate_year_fraction(date)
        
        # 计算地球半径比率（考虑高度）
        r_ratio = 6371000.0 / (6371000.0 + altitude)
        
        # 计算磁偏角（简化版计算）
        # 注意：实际计算应该使用完整的WMM模型
        cos_lat = np.cos(lat_rad)
        sin_lat = np.sin(lat_rad)
        cos_lon = np.cos(lon_rad)
        sin_lon = np.sin(lon_rad)
        
        # 简化的磁场计算
        b_north = self._calculate_b_north(lat_rad, lon_rad, r_ratio, year_fraction)
        b_east = self._calculate_b_east(lat_rad, lon_rad, r_ratio, year_fraction)
        
        # 计算磁偏角
        declination = np.degrees(np.arctan2(b_east, b_north))
        
        return declination
    
    def _calculate_year_fraction(self, date: datetime.datetime) -> float:
        """计算年份分数，用于时间依赖计算"""
        year = date.year
        start_of_year = datetime.datetime(year, 1, 1)
        end_of_year = datetime.datetime(year + 1, 1, 1)
        days_in_year = (end_of_year - start_of_year).days
        days_passed = (date - start_of_year).days
        
        return year + days_passed / days_in_year
    
    def _calculate_b_north(self, lat_rad: float, lon_rad: float, r_ratio: float, 
                          year_fraction: float) -> float:
        """计算北向磁场分量"""
        # 简化版计算
        t = year_fraction - 2020.0  # 相对于2020年的时间
        
        term1 = self.a[0] * r_ratio**3
        term2 = self.a[1] * r_ratio**4 * np.cos(lon_rad)
        term3 = self.a[2] * r_ratio**4 * np.sin(lon_rad)
        term4 = self.a[3] * r_ratio**5 * np.cos(2*lon_rad)
        term5 = self.a[4] * r_ratio**5 * np.sin(2*lon_rad)
        
        # 添加时间变化
        time_term = t * (self.b[1] * r_ratio**4 * np.cos(lon_rad) + 
                         self.b[2] * r_ratio**4 * np.sin(lon_rad))
        
        return (term1 + term2 + term3 + term4 + term5 + time_term) * np.cos(lat_rad)
    
    def _calculate_b_east(self, lat_rad: float, lon_rad: float, r_ratio: float, 
                         year_fraction: float) -> float:
        """计算东向磁场分量"""
        # 简化版计算
        t = year_fraction - 2020.0  # 相对于2020年的时间
        
        term1 = self.a[2] * r_ratio**4 * np.cos(lon_rad)
        term2 = -self.a[1] * r_ratio**4 * np.sin(lon_rad)
        term3 = self.a[4] * r_ratio**5 * np.cos(2*lon_rad)
        term4 = -self.a[3] * r_ratio**5 * np.sin(2*lon_rad)
        
        # 添加时间变化
        time_term = t * (self.b[2] * r_ratio**4 * np.cos(lon_rad) - 
                         self.b[1] * r_ratio**4 * np.sin(lon_rad))
        
        return term1 + term2 + term3 + term4 + time_term


def create_simple_gui():
    """创建一个简单的GUI界面"""
    try:
        import tkinter as tk
        from tkinter import ttk, messagebox
        
        # 创建主窗口
        root = tk.Tk()
        root.title("磁差计算器")
        root.geometry("500x400")
        root.resizable(True, True)
        
        # 创建计算器实例
        calculator = MagneticDeclinationCalculator()
        
        # 创建样式
        style = ttk.Style()
        style.configure("TLabel", font=("SimHei", 10))
        style.configure("TButton", font=("SimHei", 10))
        style.configure("TEntry", font=("SimHei", 10))
        style.configure("Header.TLabel", font=("SimHei", 14, "bold"))
        
        # 创建标题
        header = ttk.Label(root, text="磁偏角计算器", style="Header.TLabel")
        header.pack(pady=20)
        
        # 创建输入框架
        input_frame = ttk.Frame(root, padding="20")
        input_frame.pack(fill=tk.BOTH, expand=True)
        
        # 纬度输入
        ttk.Label(input_frame, text="纬度 (度):").grid(row=0, column=0, sticky=tk.W, pady=10)
        lat_entry = ttk.Entry(input_frame, width=20)
        lat_entry.grid(row=0, column=1, pady=10)
        lat_entry.insert(0, "39.9042")  # 北京纬度
        
        # 经度输入
        ttk.Label(input_frame, text="经度 (度):").grid(row=1, column=0, sticky=tk.W, pady=10)
        lon_entry = ttk.Entry(input_frame, width=20)
        lon_entry.grid(row=1, column=1, pady=10)
        lon_entry.insert(0, "116.4074")  # 北京经度
        
        # 高度输入
        ttk.Label(input_frame, text="高度 (米):").grid(row=2, column=0, sticky=tk.W, pady=10)
        alt_entry = ttk.Entry(input_frame, width=20)
        alt_entry.grid(row=2, column=1, pady=10)
        alt_entry.insert(0, "0")
        
        # 计算按钮
        def calculate():
            try:
                latitude = float(lat_entry.get())
                longitude = float(lon_entry.get())
                altitude = float(alt_entry.get())
                
                # 检查纬度范围
                if not (-90 <= latitude <= 90):
                    messagebox.showerror("输入错误", "纬度必须在-90到90之间")
                    return
                
                # 检查经度范围
                if not (-180 <= longitude <= 180):
                    messagebox.showerror("输入错误", "经度必须在-180到180之间")
                    return
                
                # 计算磁偏角
                declination = calculator.calculate_declination(latitude, longitude, altitude)
                
                # 显示结果
                result_var.set(f"磁偏角: {declination:.4f} 度")
                
                # 解释结果
                if declination > 0:
                    explanation_var.set(f"说明: 磁北位于真北以东{abs(declination):.2f}度")
                elif declination < 0:
                    explanation_var.set(f"说明: 磁北位于真北以西{abs(declination):.2f}度")
                else:
                    explanation_var.set("说明: 磁北与真北重合")
                    
            except ValueError:
                messagebox.showerror("输入错误", "请输入有效的数字")
        
        calc_button = ttk.Button(input_frame, text="计算", command=calculate)
        calc_button.grid(row=3, column=0, columnspan=2, pady=20)
        
        # 结果显示
        result_var = tk.StringVar()
        result_label = ttk.Label(root, textvariable=result_var, font=("SimHei", 12))
        result_label.pack(pady=10)
        
        explanation_var = tk.StringVar()
        explanation_label = ttk.Label(root, textvariable=explanation_var, font=("SimHei", 10))
        explanation_label.pack(pady=10)
        
        # 运行主循环
        root.mainloop()
        
    except ImportError:
        print("无法导入tkinter模块。请确保您的Python环境已安装tkinter。")
        print("您可以使用命令行界面代替。")
        run_command_line_interface()


def run_command_line_interface():
    """运行命令行界面"""
    calculator = MagneticDeclinationCalculator()
    
    print("===== 磁偏角计算器 ======")
    
    try:
        latitude = float(input("请输入纬度 (度): "))
        longitude = float(input("请输入经度 (度): "))
        altitude = float(input("请输入高度 (米): "))
        
        # 检查纬度范围
        if not (-90 <= latitude <= 90):
            print("错误: 纬度必须在-90到90之间")
            return
        
        # 检查经度范围
        if not (-180 <= longitude <= 180):
            print("错误: 经度必须在-180到180之间")
            return
        
        # 计算磁偏角
        declination = calculator.calculate_declination(latitude, longitude, altitude)
        
        # 显示结果
        print(f"\n计算结果:")
        print(f"磁偏角: {declination:.4f} 度")
        
        # 解释结果
        if declination > 0:
            print(f"说明: 磁北位于真北以东{abs(declination):.2f}度")
        elif declination < 0:
            print(f"说明: 磁北位于真北以西{abs(declination):.2f}度")
        else:
            print("说明: 磁北与真北重合")
            
    except ValueError:
        print("错误: 请输入有效的数字")


if __name__ == "__main__":
    # 尝试启动GUI界面，如果失败则启动命令行界面
    try:
        create_simple_gui()
    except Exception as e:
        print(f"GUI启动失败: {e}")
        print("切换到命令行界面...")
        run_command_line_interface()