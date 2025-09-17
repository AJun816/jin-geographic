package com.jin.geographic;

/**
 * GeographicLibWrapper - JNI接口类，用于封装GeographicLib的功能
 */
public class GeographicLibWrapper {
    // 加载C++实现的本地库
    static {
        System.loadLibrary("geographiclib_jni");
    }
    
    /**
     * 初始化GeographicLib库，加载WMM2025数据
     * @param dataPath WMM2025数据集的路径
     * @return 初始化是否成功
     */
    public native boolean initialize(String dataPath);
    
    /**
     * 计算指定位置和时间的磁偏角
     * @param latitude 纬度（度）
     * @param longitude 经度（度）
     * @param altitude 高度（米）
     * @param time 时间（儒略日）
     * @return 磁偏角（度）
     */
    public native double getMagneticDeclination(double latitude, double longitude, double altitude, double time);
    
    /**
     * 计算指定位置、日期的磁偏角（使用更友好的日期格式）
     * @param latitude 纬度（度）
     * @param longitude 经度（度）
     * @param altitude 高度（米）
     * @param year 年份
     * @param month 月份
     * @param day 日期
     * @return 磁偏角（度）
     */
    public native double getMagneticDeclinationByDate(double latitude, double longitude, double altitude, 
                                                    int year, int month, int day);
    
    /**
     * 释放GeographicLib资源
     */
    public native void cleanup();
}