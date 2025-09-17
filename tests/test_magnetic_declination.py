#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
磁偏角计算器测试用例
"""

import unittest
import datetime
import sys
import os

# 添加src/python目录到路径
sys.path.append(os.path.join(os.path.dirname(__file__), '../src/python'))

from magnetic_declination import MagneticDeclinationCalculator


class TestMagneticDeclinationCalculator(unittest.TestCase):
    
    def setUp(self):
        """每个测试用例前的设置"""
        self.calculator = MagneticDeclinationCalculator()
    
    def test_initialization(self):
        """测试计算器初始化"""
        self.assertIsNotNone(self.calculator)
        self.assertIsNotNone(self.calculator.a)
        self.assertIsNotNone(self.calculator.b)
        self.assertEqual(len(self.calculator.a), 9)
        self.assertEqual(len(self.calculator.b), 9)
    
    def test_calculate_year_fraction(self):
        """测试年份分数计算"""
        # 测试2023年1月1日
        date = datetime.datetime(2023, 1, 1)
        year_fraction = self.calculator._calculate_year_fraction(date)
        self.assertEqual(year_fraction, 2023.0)
        
        # 测试2023年7月2日（约半年）
        date = datetime.datetime(2023, 7, 2)
        year_fraction = self.calculator._calculate_year_fraction(date)
        self.assertAlmostEqual(year_fraction, 2023.5, places=2)
    
    def test_calculate_b_north(self):
        """测试北向磁场分量计算"""
        # 测试北京附近的位置
        lat_rad = 0.696
        lon_rad = 2.032
        r_ratio = 1.0
        year_fraction = 2023.0
        
        b_north = self.calculator._calculate_b_north(lat_rad, lon_rad, r_ratio, year_fraction)
        self.assertIsInstance(b_north, float)
        # 检查结果是否在合理范围内（实际值应该在较大范围内变化）
        self.assertTrue(abs(b_north) < 100000)
    
    def test_calculate_b_east(self):
        """测试东向磁场分量计算"""
        # 测试北京附近的位置
        lat_rad = 0.696
        lon_rad = 2.032
        r_ratio = 1.0
        year_fraction = 2023.0
        
        b_east = self.calculator._calculate_b_east(lat_rad, lon_rad, r_ratio, year_fraction)
        self.assertIsInstance(b_east, float)
        # 检查结果是否在合理范围内
        self.assertTrue(abs(b_east) < 100000)
    
    def test_calculate_declination(self):
        """测试磁偏角计算"""
        # 测试北京的磁偏角
        # 注意：由于这是简化模型，实际值可能与真实值有差异
        latitude = 39.9042  # 北京纬度
        longitude = 116.4074  # 北京经度
        altitude = 0
        
        declination = self.calculator.calculate_declination(latitude, longitude, altitude)
        self.assertIsInstance(declination, float)
        # 检查结果是否在合理范围内（通常在-180到180之间）
        self.assertTrue(-180 <= declination <= 180)
    
    def test_calculate_declination_with_date(self):
        """测试带日期的磁偏角计算"""
        latitude = 39.9042
        longitude = 116.4074
        altitude = 0
        date = datetime.datetime(2023, 1, 1)
        
        declination = self.calculator.calculate_declination(latitude, longitude, altitude, date)
        self.assertIsInstance(declination, float)
    
    def test_calculate_declination_with_altitude(self):
        """测试不同高度的磁偏角计算"""
        latitude = 39.9042
        longitude = 116.4074
        
        # 海平面
        declination_sea = self.calculator.calculate_declination(latitude, longitude, 0)
        # 高海拔（1000米）
        declination_high = self.calculator.calculate_declination(latitude, longitude, 1000)
        
        self.assertIsInstance(declination_high, float)
        # 检查高海拔和海平面的结果是否有细微差异
        self.assertNotEqual(declination_sea, declination_high)
    
    def test_edge_cases(self):
        """测试边界情况"""
        # 北极
        declination_north_pole = self.calculator.calculate_declination(90, 0)
        self.assertIsInstance(declination_north_pole, float)
        
        # 南极
        declination_south_pole = self.calculator.calculate_declination(-90, 0)
        self.assertIsInstance(declination_south_pole, float)
        
        # 国际日期变更线
        declination_date_line = self.calculator.calculate_declination(0, 180)
        self.assertIsInstance(declination_date_line, float)


if __name__ == '__main__':
    unittest.main()