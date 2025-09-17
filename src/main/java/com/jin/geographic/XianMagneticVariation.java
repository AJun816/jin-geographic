package com.jin.geographic;

/**
 * XianMagneticVariation - 专门用于计算西安磁差的类
 */
public class XianMagneticVariation {
    public static void main(String[] args) {
        System.out.println("计算西安的磁差");
        System.out.println("==================");
        
        // 创建GeographicLib包装器实例
        GeographicLibWrapper wrapper = new GeographicLibWrapper();
        
        try {
            // 初始化GeographicLib，使用系统默认路径
            boolean initialized = wrapper.initialize("");
            
            if (!initialized) {
                System.out.println("初始化GeographicLib失败！");
                System.out.println("\n可能的原因：");
                System.out.println("1. WMM2025数据文件未找到。");
                System.out.println("2. 本地库'geographiclib_jni'未找到。");
                return;
            }
            
            System.out.println("成功初始化GeographicLib！");
            
            // 西安的地理位置信息
            double latitude = 34.3416;    // 纬度
            double longitude = 108.9398;  // 经度
            double altitude = 400.0;      // 海拔高度（米）
            
            // 计算2025年1月1日的磁差
            int year = 2025;
            int month = 1;
            int day = 1;
            
            double declination = wrapper.getMagneticDeclinationByDate(
                latitude, longitude, altitude, year, month, day);
            
            System.out.printf("西安磁差（%d-%02d-%02d）: %.6f 度\n", 
                             year, month, day, declination);
            
            // 还可以计算多个时间点的磁差以观察变化
            System.out.println("\n计算西安在不同年份的磁差：");
            for (int y = 2025; y <= 2030; y++) {
                declination = wrapper.getMagneticDeclinationByDate(
                    latitude, longitude, altitude, y, 1, 1);
                System.out.printf("%d年: %.6f 度\n", y, declination);
            }
            
        } catch (Exception e) {
            System.err.println("计算过程中出错: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 清理资源
            wrapper.cleanup();
            System.out.println("\nGeographicLib资源已清理。");
        }
    }
}