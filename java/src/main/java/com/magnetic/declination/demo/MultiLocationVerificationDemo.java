package com.magnetic.declination.demo;

import com.magnetic.declination.model.LatLon;
import com.magnetic.declination.utils.MagneticDeclinationCalculator;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * 多地点磁偏角计算验证程序
 * 使用用户提供的多个地点的准确数据进行交叉验证
 */
public class MultiLocationVerificationDemo {

    // 地点数据类
    static class LocationData {
        String name;        // 地点名称
        double latDeg;      // 纬度度
        double latMin;      // 纬度分
        double latSec;      // 纬度秒
        char latDir;        // 纬度方向
        double lonDeg;      // 经度度
        double lonMin;      // 经度分
        double lonSec;      // 经度秒
        char lonDir;        // 经度方向
        double expectedDeclination; // 预期磁偏角（度）

        public LocationData(String name, double latDeg, double latMin, double latSec, char latDir,
                            double lonDeg, double lonMin, double lonSec, char lonDir,
                            double expectedDeclination) {
            this.name = name;
            this.latDeg = latDeg;
            this.latMin = latMin;
            this.latSec = latSec;
            this.latDir = latDir;
            this.lonDeg = lonDeg;
            this.lonMin = lonMin;
            this.lonSec = lonSec;
            this.lonDir = lonDir;
            this.expectedDeclination = expectedDeclination;
        }
    }

    public static void main(String[] args) {
        System.out.println("===== 多地点磁偏角计算验证程序 ====");
        System.out.println("使用用户提供的准确数据进行交叉验证\n");

        // 初始化计算器
        MagneticDeclinationCalculator calculator = MagneticDeclinationCalculator.getInstance();
        try {
            String modelFilePath = "wmm2025.wmm";
            String coefficientsFilePath = "wmm2025.wmm.cof";
            calculator.loadModel(modelFilePath, coefficientsFilePath);
            System.out.println("模型信息：" + calculator.getModelInfo() + "\n");
        } catch (Exception e) {
            System.out.println("模型加载失败：" + e.getMessage());
            System.out.println("继续使用默认模型进行测试...\n");
        }

        // 创建测试地点列表
        List<LocationData> locations = new ArrayList<>();
        locations.add(new LocationData("Xianyang Shaanxi", 34.0, 20.0, 16.0, 'N', 108.0, 42.0, 9.4, 'E', -4.1));
        locations.add(new LocationData("YINSIGOUKOU", 33.0, 57.0, 29.0, 'N', 109.0, 33.0, 43.7, 'E', -4.28));
        locations.add(new LocationData("CHINCHIAPING", 33.0, 54.0, 26.7, 'N', 108.0, 17.0, 14.1, 'E', -3.92));
        locations.add(new LocationData("DASANLITOU", 34.0, 10.0, 44.8, 'N', 109.0, 19.0, 35.7, 'E', -4.25));
        locations.add(new LocationData("SHANMENKOU", 34.0, 12.0, 14.3, 'N', 108.0, 57.0, 32.3, 'E', -4.15));
        locations.add(new LocationData("Beijing", 39.0, 54.0, 0.0, 'N', 116.0, 24.0, 0.0, 'E', -7.53));
        locations.add(new LocationData("SANCHANG", 34.0, 26.0, 5.5, 'N', 109.0, 28.0, 14.7, 'E', -4.33));

        // 格式化输出
        DecimalFormat df = new DecimalFormat("0.000");
        System.out.println("地点名称\t\t\t计算磁偏角\t预期磁偏角\t误差\t方向一致性");
        System.out.println("---------------------------------------------------------------------------------");

        // 统计信息
        int totalLocations = locations.size();
        int correctDirection = 0;
        double totalError = 0;
        double maxError = 0;
        String maxErrorLocation = "";

        // 遍历所有地点进行验证
        for (LocationData location : locations) {
            try {
                // 使用度分秒格式计算磁偏角
                // 将double转换为int，并将方向字符转换为布尔值
                int latDeg = (int)Math.round(location.latDeg);
                int latMin = (int)Math.round(location.latMin);
                double latSec = location.latSec;
                boolean isLatNorth = location.latDir == 'N';
                
                int lonDeg = (int)Math.round(location.lonDeg);
                int lonMin = (int)Math.round(location.lonMin);
                double lonSec = location.lonSec;
                boolean isLonEast = location.lonDir == 'E';
                
                // 使用度分秒格式计算磁偏角
                double calculatedDeclination = calculator.calculateDeclination(
                        latDeg, latMin, latSec, 
                        lonDeg, lonMin, lonSec, 
                        isLatNorth, isLonEast, 
                        0, 2025); // 高度设为0米，年份设为2025

                // 计算误差
                double error = Math.abs(calculatedDeclination - location.expectedDeclination);
                totalError += error;
                
                // 更新最大误差
                if (error > maxError) {
                    maxError = error;
                    maxErrorLocation = location.name;
                }
                
                // 检查方向一致性
                boolean directionCorrect = (calculatedDeclination < 0 && location.expectedDeclination < 0) ||
                                          (calculatedDeclination > 0 && location.expectedDeclination > 0) ||
                                          (calculatedDeclination == 0 && location.expectedDeclination == 0);
                if (directionCorrect) correctDirection++;
                
                // 格式化输出结果
                String calculated = df.format(calculatedDeclination) + "° " + (calculatedDeclination < 0 ? "W" : "E");
                String expected = df.format(location.expectedDeclination) + "° " + (location.expectedDeclination < 0 ? "W" : "E");
                String errorStr = df.format(error) + "°";
                
                // 输出结果，根据名称长度调整制表符数量
                System.out.print(location.name);
                if (location.name.length() < 8) System.out.print("\t\t\t");
                else if (location.name.length() < 16) System.out.print("\t\t");
                else System.out.print("\t");
                
                System.out.println(calculated + "\t" + expected + "\t" + errorStr + "\t" + (directionCorrect ? "√" : "×"));
                
            } catch (Exception e) {
                System.out.println(location.name + "\t\t计算错误：" + e.getMessage());
            }
        }

        // 输出统计结果
        System.out.println("---------------------------------------------------------------------------------");
        System.out.println("统计结果：");
        System.out.println("总测试地点数：" + totalLocations);
        System.out.println("方向正确数：" + correctDirection + " (" + (correctDirection * 100.0 / totalLocations) + "%)");
        System.out.println("平均误差：" + df.format(totalError / totalLocations) + "°");
        System.out.println("最大误差：" + df.format(maxError) + "° (" + maxErrorLocation + ")");
        
        // 判断整体精度
        double avgError = totalError / totalLocations;
        if (avgError < 0.1) {
            System.out.println("验证结果：非常准确 ✓");
        } else if (avgError < 0.5) {
            System.out.println("验证结果：较为准确 ✓");
        } else if (avgError < 1.0) {
            System.out.println("验证结果：基本准确 ✓");
        } else {
            System.out.println("验证结果：需要进一步优化 ✗");
        }
    }
}