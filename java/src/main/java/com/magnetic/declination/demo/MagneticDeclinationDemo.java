package com.magnetic.declination.demo;

import com.magnetic.declination.utils.MagneticDeclinationCalculator;
import java.io.IOException;
import java.util.Scanner;

/**
 * 磁差计算控制台交互式Demo程序
 */
public class MagneticDeclinationDemo {
    private static MagneticDeclinationCalculator calculator = MagneticDeclinationCalculator.getInstance();
    private static Scanner scanner = new Scanner(System.in);
    
    public static void main(String[] args) {
        System.out.println("===== 磁差计算器Demo =====");
        
        // 加载WMM模型
        try {
            // 假设模型文件在当前目录下
            String modelFilePath = "wmm2025.wmm";
            String coefficientsFilePath = "wmm2025.wmm.cof";
            calculator.loadModel(modelFilePath, coefficientsFilePath);
        } catch (IOException e) {
            System.out.println("模型文件加载失败：" + e.getMessage());
            System.out.println("将使用内置的简化模型进行计算");
        }
        
        boolean running = true;
        while (running) {
            System.out.println("\n请选择操作：");
            System.out.println("1. 使用度分秒格式计算磁差");
            System.out.println("2. 使用度数格式计算磁差");
            System.out.println("3. 度分秒与度数格式转换");
            System.out.println("4. 查看模型信息");
            System.out.println("5. 退出程序");
            
            System.out.print("请输入选项[1-5]：");
            int choice = getIntInput();
            
            switch (choice) {
                case 1:
                    calculateDeclinationFromDMS();
                    break;
                case 2:
                    calculateDeclinationFromDegrees();
                    break;
                case 3:
                    convertCoordinates();
                    break;
                case 4:
                    showModelInfo();
                    break;
                case 5:
                    running = false;
                    System.out.println("感谢使用磁差计算器，再见！");
                    break;
                default:
                    System.out.println("无效的选项，请重新输入");
            }
        }
        
        scanner.close();
    }
    
    /**
     * 使用度分秒格式计算磁差
     */
    private static void calculateDeclinationFromDMS() {
        System.out.println("\n=== 使用度分秒格式计算磁差 ===");
        
        // 输入纬度
        System.out.print("请输入纬度度数：");
        int latDegrees = getIntInput();
        System.out.print("请输入纬度分数：");
        int latMinutes = getIntInput();
        System.out.print("请输入纬度秒数：");
        double latSeconds = getDoubleInput();
        System.out.print("是否北纬？(Y/N)：");
        boolean isLatitudeNorth = getYesNoInput();
        
        // 输入经度
        System.out.print("请输入经度度数：");
        int lonDegrees = getIntInput();
        System.out.print("请输入经度分数：");
        int lonMinutes = getIntInput();
        System.out.print("请输入经度秒数：");
        double lonSeconds = getDoubleInput();
        System.out.print("是否东经？(Y/N)：");
        boolean isLongitudeEast = getYesNoInput();
        
        // 输入高度和年份
        System.out.print("请输入高度（米）：");
        double height = getDoubleInput();
        System.out.print("请输入年份（如2025.5）：");
        double year = getDoubleInput();
        
        // 计算磁差
        double declination = calculator.calculateDeclination(latDegrees, latMinutes, latSeconds, 
                                                           lonDegrees, lonMinutes, lonSeconds, 
                                                           isLatitudeNorth, isLongitudeEast, 
                                                           height, year);
        
        // 格式化输出结果
        String latDMS = calculator.formatDMS(latDegrees, latMinutes, latSeconds, isLatitudeNorth, true);
        String lonDMS = calculator.formatDMS(lonDegrees, lonMinutes, lonSeconds, isLongitudeEast, false);
        
        System.out.println("\n===== 计算结果 ====");
        System.out.println("位置：" + latDMS + ", " + lonDMS);
        System.out.println("高度：" + height + " 米");
        System.out.println("年份：" + year);
        System.out.println("磁差：" + String.format("%.2f", declination) + " 度");
        System.out.println("（注：正值表示磁北偏东，负值表示磁北偏西）");
    }
    
    /**
     * 使用度数格式计算磁差
     */
    private static void calculateDeclinationFromDegrees() {
        System.out.println("\n=== 使用度数格式计算磁差 ===");
        
        System.out.print("请输入纬度（度，北纬为正，南纬为负）：");
        double latitude = getDoubleInput();
        System.out.print("请输入经度（度，东经为正，西经为负）：");
        double longitude = getDoubleInput();
        System.out.print("请输入高度（米）：");
        double height = getDoubleInput();
        System.out.print("请输入年份（如2025.5）：");
        double year = getDoubleInput();
        
        // 计算磁差
        double declination = calculator.calculateDeclination(latitude, longitude, height, year);
        
        System.out.println("\n===== 计算结果 ====");
        System.out.println("位置：" + String.format("%.6f", latitude) + "°, " + String.format("%.6f", longitude) + "°");
        System.out.println("高度：" + height + " 米");
        System.out.println("年份：" + year);
        System.out.println("磁差：" + String.format("%.2f", declination) + " 度");
        System.out.println("（注：正值表示磁北偏东，负值表示磁北偏西）");
    }
    
    /**
     * 坐标格式转换
     */
    private static void convertCoordinates() {
        System.out.println("\n=== 坐标格式转换 ===");
        System.out.println("1. 度分秒转换为度数");
        System.out.println("2. 度数转换为度分秒");
        System.out.print("请选择转换类型[1-2]：");
        int choice = getIntInput();
        
        if (choice == 1) {
            System.out.print("请输入度数：");
            int degrees = getIntInput();
            System.out.print("请输入分数：");
            int minutes = getIntInput();
            System.out.print("请输入秒数：");
            double seconds = getDoubleInput();
            System.out.print("是否为正值？(Y/N)：");
            boolean isPositive = getYesNoInput();
            
            double decimalDegrees = calculator.convertDMStoDegrees(degrees, minutes, seconds, isPositive);
            System.out.println("转换结果：" + String.format("%.6f", decimalDegrees) + " 度");
        } else if (choice == 2) {
            System.out.print("请输入度数：");
            double degrees = getDoubleInput();
            
            Object[] dms = calculator.convertDegreesToDMS(degrees);
            int d = (int) dms[0];
            int m = (int) dms[1];
            double s = (double) dms[2];
            boolean isPositive = (boolean) dms[3];
            
            System.out.println("转换结果：" + d + "°" + m + "′" + String.format("%.2f", s) + "″ " + (isPositive ? "(正)" : "(负)"));
        }
    }
    
    /**
     * 显示模型信息
     */
    private static void showModelInfo() {
        System.out.println("\n=== 模型信息 ===");
        System.out.println(calculator.getModelInfo());
    }
    
    /**
     * 获取整数输入
     * @return 用户输入的整数
     */
    private static int getIntInput() {
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.print("输入无效，请重新输入整数：");
            }
        }
    }
    
    /**
     * 获取浮点数输入
     * @return 用户输入的浮点数
     */
    private static double getDoubleInput() {
        while (true) {
            try {
                return Double.parseDouble(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.print("输入无效，请重新输入数字：");
            }
        }
    }
    
    /**
     * 获取是/否输入
     * @return true表示是，false表示否
     */
    private static boolean getYesNoInput() {
        while (true) {
            String input = scanner.nextLine().trim().toUpperCase();
            if (input.equals("Y") || input.equals("YES")) {
                return true;
            } else if (input.equals("N") || input.equals("NO")) {
                return false;
            } else {
                System.out.print("输入无效，请输入Y或N：");
            }
        }
    }
}