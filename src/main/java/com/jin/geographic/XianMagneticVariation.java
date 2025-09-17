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
            
            double declinationMinutes = wrapper.getMagneticDeclinationByDate(
                latitude, longitude, altitude, year, month, day);
                
            // 转换为度并计算方向
            double declinationDegrees = declinationMinutes / 60.0;
            String direction = declinationMinutes > 0 ? "东偏" : "西偏";
            
            // 输出简明信息
            System.out.printf("西安磁偏角（%d-%02d-%02d）: %.2f° (%s)\n", 
                             year, month, day, Math.abs(declinationDegrees), direction);
            
            // 还可以计算多个时间点的磁差以观察变化
            System.out.println("\n计算西安在不同年份的磁偏角：");
            for (int y = 2025; y <= 2030; y++) {
                declinationMinutes = wrapper.getMagneticDeclinationByDate(
                    latitude, longitude, altitude, y, 1, 1);
                declinationDegrees = declinationMinutes / 60.0;
                direction = declinationMinutes > 0 ? "东偏" : "西偏";
                System.out.printf("%d年: %.2f° (%s)\n", y, Math.abs(declinationDegrees), direction);
            }
            
            // 添加说明
            System.out.println("\n说明：");
            System.out.println("1. WMM2025模型计算结果默认以弧分为单位，已转换为度显示");
            System.out.println("2. 西安地区的磁偏角约为2.22度西偏（2025年）");
            System.out.println("3. 负号表示西偏，正号表示东偏");
            System.out.println("4. 模型最佳预测期为2025年1月1日至2029年12月31日");
            
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