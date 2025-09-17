package com.jin.geographic;

import java.io.PrintStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * DebugHelper - 调试辅助类，提供磁偏角计算相关的调试工具
 * 帮助Java程序员更好地排查和解决计算过程中可能出现的问题
 */
public class DebugHelper {
    
    private static boolean debugEnabled = false;
    private static PrintStream debugStream = System.err;
    private static long startTimeMillis;
    
    /**
     * 启用调试模式
     */
    public static void enableDebug() {
        debugEnabled = true;
        log("调试模式已启用");
    }
    
    /**
     * 禁用调试模式
     */
    public static void disableDebug() {
        log("调试模式已禁用");
        debugEnabled = false;
    }
    
    /**
     * 设置调试日志输出到文件
     * @param filePath 日志文件路径
     * @throws IOException 文件操作异常
     */
    public static void setDebugLogFile(String filePath) throws IOException {
        debugStream = new PrintStream(new FileOutputStream(filePath, true));
        log("调试日志已重定向到文件: " + filePath);
    }
    
    /**
     * 开始性能计时
     */
    public static void startTimer() {
        startTimeMillis = System.currentTimeMillis();
    }
    
    /**
     * 结束性能计时并返回耗时
     * @param operationName 操作名称
     * @return 操作耗时（毫秒）
     */
    public static long endTimer(String operationName) {
        long endTimeMillis = System.currentTimeMillis();
        long duration = endTimeMillis - startTimeMillis;
        log("操作 ['" + operationName + "'] 耗时: " + duration + "ms");
        return duration;
    }
    
    /**
     * 记录调试信息
     * @param message 调试信息
     */
    public static void log(String message) {
        if (debugEnabled) {
            String timestamp = java.time.LocalDateTime.now().format(
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
            debugStream.println("[DEBUG " + timestamp + "] " + message);
        }
    }
    
    /**
     * 验证经纬度参数是否有效
     * @param latitude 纬度
     * @param longitude 经度
     * @return 验证结果
     */
    public static boolean validateCoordinates(double latitude, double longitude) {
        boolean isValid = true;
        
        if (latitude < -90 || latitude > 90) {
            log("无效的纬度值: " + latitude + "，应在-90到90之间");
            isValid = false;
        }
        
        if (longitude < -180 || longitude > 180) {
            log("无效的经度值: " + longitude + "，应在-180到180之间");
            isValid = false;
        }
        
        return isValid;
    }
    
    /**
     * 验证日期参数是否有效
     * @param year 年份
     * @param month 月份
     * @param day 日期
     * @return 验证结果
     */
    public static boolean validateDate(int year, int month, int day) {
        boolean isValid = true;
        
        if (year < 2000 || year > 2100) {
            log("警告: 年份 " + year + " 超出WMM2025模型最佳适用范围(2000-2100)");
        }
        
        if (month < 1 || month > 12) {
            log("无效的月份值: " + month + "，应在1到12之间");
            isValid = false;
        }
        
        if (day < 1 || day > 31) {
            log("无效的日期值: " + day + "，应在1到31之间");
            isValid = false;
        }
        
        return isValid;
    }
    
    /**
     * 验证高度参数是否合理
     * @param altitude 高度（米）
     * @return 验证结果
     */
    public static boolean validateAltitude(double altitude) {
        boolean isValid = true;
        
        if (altitude < -1000 || altitude > 10000) {
            log("警告: 高度 " + altitude + " 米超出通常适用范围(-1000到10000米)");
        }
        
        return isValid;
    }
    
    /**
     * 打印磁偏角计算结果的详细信息
     * @param result 计算结果对象
     */
    public static void printResultDetails(MagneticDeclinationResult result) {
        if (result == null) {
            log("计算结果为null");
            return;
        }
        
        StringBuilder details = new StringBuilder();
        details.append("计算结果详情:\n");
        details.append("- 是否成功: ").append(result.isSuccess()).append("\n");
        details.append("- 磁偏角(弧分): ").append(result.getDeclination()).append("\n");
        details.append("- 磁偏角(度): ").append(result.getDeclination() / 60.0).append("\n");
        details.append("- 方向: ").append(result.getDeclination() > 0 ? "东偏" : "西偏").append("\n");
        details.append("- 消息: ").append(result.getMessage()).append("\n");
        
        log(details.toString());
    }
    
    /**
     * 测试JNI连接是否正常
     * @param wrapper GeographicLibWrapper实例
     * @return 测试结果
     */
    public static boolean testJniConnection(GeographicLibWrapper wrapper) {
        if (wrapper == null) {
            log("GeographicLibWrapper实例为null");
            return false;
        }
        
        try {
            // 测试一个简单的经纬度点（赤道上的点）
            startTimer();
            double testResult = wrapper.getMagneticDeclination(0.0, 0.0, 0.0, 2460323.5); // 2025-01-01的儒略日
            endTimer("JNI连接测试");
            
            log("JNI连接测试成功，赤道(0°, 0°)处2025年1月1日磁偏角: " + testResult + "度");
            return true;
        } catch (Throwable t) {
            log("JNI连接测试失败: " + t.getMessage());
            log("异常类型: " + t.getClass().getName());
            return false;
        }
    }
    
    /**
     * 获取环境信息，用于调试
     * @return 环境信息字符串
     */
    public static String getEnvironmentInfo() {
        StringBuilder info = new StringBuilder();
        info.append("环境信息:\n");
        info.append("- Java版本: ").append(System.getProperty("java.version")).append("\n");
        info.append("- Java库路径: ").append(System.getProperty("java.library.path")).append("\n");
        info.append("- 操作系统: ").append(System.getProperty("os.name")).append(" ").append(System.getProperty("os.version")).append("\n");
        info.append("- 架构: ").append(System.getProperty("os.arch")).append("\n");
        info.append("- 当前工作目录: ").append(System.getProperty("user.dir")).append("\n");
        
        return info.toString();
    }
    
    /**
     * 打印环境信息
     */
    public static void printEnvironmentInfo() {
        log(getEnvironmentInfo());
    }
}