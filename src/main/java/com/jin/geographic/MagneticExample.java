package com.jin.geographic;

/**
 * 磁偏角计算工具使用示例
 * 
 * 本类展示了如何使用MagneticDeclinationCalculator类计算地球上任意位置的磁偏角
 * 包括初始化、计算和资源清理的完整流程
 * 
 * 同时展示了DebugHelper调试辅助类的使用方法
 */
public class MagneticExample {
    public static void main(String[] args) {
        // 启用调试模式（生产环境中可以注释掉这行）
        DebugHelper.enableDebug();
        
        // 打印环境信息，有助于调试环境相关问题
        DebugHelper.printEnvironmentInfo();
        
        System.out.println("WMM2025 磁偏角计算工具使用示例");
        System.out.println("=================================");
        
        // 创建计算器实例
        MagneticDeclinationCalculator calculator = new MagneticDeclinationCalculator();
        
        try {
            // 开始计时，测量初始化性能
            DebugHelper.startTimer();
            
            // 初始化计算器
            boolean initialized = calculator.initialize();
            
            // 结束初始化计时
            DebugHelper.endTimer("初始化计算器");
            
            if (!initialized) {
                System.out.println("初始化失败，请检查 WMM2025 数据是否正确安装");
                DebugHelper.log("初始化失败，可能原因：WMM2025数据缺失或路径错误");
                return;
            }
            
            System.out.println("成功初始化磁偏角计算器！");
            System.out.println();
            
            // 示例1：计算北京的磁偏角
            DebugHelper.log("开始计算北京地区磁偏角");
            calculateLocation(calculator, 39.9042, 116.4074, "北京", 0.0, 2025, 1, 1);
            
            // 示例2：计算上海的磁偏角
            DebugHelper.log("开始计算上海地区磁偏角");
            calculateLocation(calculator, 31.2304, 121.4737, "上海", 0.0, 2025, 1, 1);
            
            // 示例3：计算西安的磁偏角
            DebugHelper.log("开始计算西安地区磁偏角");
            calculateLocation(calculator, 34.3416, 108.9398, "西安", 0.0, 2025, 1, 1);
            
            // 示例4：计算不同年份的磁偏角变化趋势
            System.out.println("\n示例4：北京地区2025-2030年磁偏角变化趋势：");
            System.out.printf("%10s %20s\n", "年份", "磁偏角（度）");
            System.out.println("----------------------------------------");
            
            for (int year = 2025; year <= 2030; year++) {
                MagneticDeclinationResult result = calculator.calculateDeclinationByDate(
                        39.9042, 116.4074, 0.0, year, 1, 1);
                
                if (result.isSuccess()) {
                    double declinationDegrees = result.getDeclination() / 60.0;
                    String direction = result.getDeclination() > 0 ? "东偏" : "西偏";
                    
                    System.out.printf("%10d %10.6f度 %s\n", 
                                     year, Math.abs(declinationDegrees), direction);
                } else {
                    System.out.printf("%10d 计算失败\n", year);
                    DebugHelper.log("" + year + "年计算失败：" + result.getMessage());
                }
            }
            
        } catch (Exception e) {
            System.err.println("计算过程中发生错误：" + e.getMessage());
            e.printStackTrace();
        } finally {
            // 清理资源
            DebugHelper.log("开始清理资源");
            calculator.cleanup();
            DebugHelper.log("资源清理完成");
            System.out.println("\n磁偏角计算器资源已清理。");
        }
    }
    
    /**
     * 计算并显示指定位置的磁偏角
     */
    private static void calculateLocation(
            MagneticDeclinationCalculator calculator, 
            double lat, double lon, String locationName, 
            double altitude, int year, int month, int day) {
        // 验证参数
        if (!DebugHelper.validateCoordinates(lat, lon)) {
            System.out.printf("%s坐标参数无效，跳过计算\n", locationName);
            return;
        }
        
        if (!DebugHelper.validateDate(year, month, day)) {
            System.out.printf("%s日期参数无效，跳过计算\n", locationName);
            return;
        }
        
        if (!DebugHelper.validateAltitude(altitude)) {
            System.out.printf("%s高度参数超出通常范围\n", locationName);
        }
        
        System.out.printf("%s（纬度: %.4f, 经度: %.4f）%d年%d月%d日的磁偏角：\n", 
                         locationName, lat, lon, year, month, day);
        
        // 开始计时
        DebugHelper.startTimer();
        
        MagneticDeclinationResult result = calculator.calculateDeclinationByDate(
                lat, lon, altitude, year, month, day);
        
        // 结束计时
        DebugHelper.endTimer(locationName + "磁偏角计算");
        
        // 打印结果详情到调试日志
        DebugHelper.printResultDetails(result);
        
        if (result.isSuccess()) {
            double declinationMinutes = result.getDeclination();
            double declinationDegrees = declinationMinutes / 60.0;
            String direction = declinationMinutes > 0 ? "东偏" : "西偏";
            
            System.out.printf("  - 度数表示：%.6f度 %s\n", Math.abs(declinationDegrees), direction);
            System.out.printf("  - 弧分表示：%.6f弧分 %s\n", Math.abs(declinationMinutes), direction);
        } else {
            System.out.println("  - 计算失败：" + result.getMessage());
        }
        
        System.out.println();
    }
}