package com.magnetic.declination.utils;

import com.magnetic.declination.model.LatLon;
import com.magnetic.declination.model.WorldMagneticModel;
import java.io.IOException;

/**
 * 磁差计算器工具类，提供计算磁差的主要功能
 */
public class MagneticDeclinationCalculator {
    private WorldMagneticModel model;
    private static volatile MagneticDeclinationCalculator instance;
    
    /**
     * 私有构造函数，防止外部直接实例化
     */
    private MagneticDeclinationCalculator() {
        this.model = new WorldMagneticModel();
    }
    
    /**
     * 获取单例实例
     * @return MagneticDeclinationCalculator实例
     */
    public static MagneticDeclinationCalculator getInstance() {
        if (instance == null) {
            synchronized (MagneticDeclinationCalculator.class) {
                if (instance == null) {
                    instance = new MagneticDeclinationCalculator();
                }
            }
        }
        return instance;
    }
    
    /**
     * 加载WMM模型
     * @param modelFilePath 模型文件路径
     * @param coefficientsFilePath 系数文件路径
     * @throws IOException 当文件读取失败时抛出
     */
    public void loadModel(String modelFilePath, String coefficientsFilePath) throws IOException {
        model.loadModel(modelFilePath, coefficientsFilePath);
        System.out.println("模型加载成功：" + model.getName());
        System.out.println("模型描述：" + model.getDescription());
        System.out.println("适用年份范围：" + model.getMinTime() + " - " + model.getMaxTime());
        System.out.println("适用高度范围：" + model.getMinHeight() + " - " + model.getMaxHeight() + "米");
    }
    
    /**
     * 使用度数格式计算磁差
     * @param latitude 纬度（度）
     * @param longitude 经度（度）
     * @param height 高度（米）
     * @param year 年份
     * @return 磁差（度）
     */
    public double calculateDeclination(double latitude, double longitude, double height, double year) {
        LatLon latLon = new LatLon(latitude, longitude, height, year);
        return model.calculateDeclination(latLon);
    }
    
    /**
     * 使用度分秒格式计算磁差
     * @param latDegrees 纬度度数
     * @param latMinutes 纬度分数
     * @param latSeconds 纬度秒数
     * @param lonDegrees 经度度数
     * @param lonMinutes 经度分数
     * @param lonSeconds 经度秒数
     * @param isLatitudeNorth 是否北纬
     * @param isLongitudeEast 是否东经
     * @param height 高度（米）
     * @param year 年份
     * @return 磁差（度）
     */
    public double calculateDeclination(int latDegrees, int latMinutes, double latSeconds, 
                                     int lonDegrees, int lonMinutes, double lonSeconds, 
                                     boolean isLatitudeNorth, boolean isLongitudeEast, 
                                     double height, double year) {
        LatLon latLon = new LatLon(latDegrees, latMinutes, latSeconds, 
                                 lonDegrees, lonMinutes, lonSeconds, 
                                 isLatitudeNorth, isLongitudeEast, 
                                 height, year);
        return model.calculateDeclination(latLon);
    }
    
    /**
     * 将度分秒格式转换为度数格式
     * @param degrees 度数
     * @param minutes 分数
     * @param seconds 秒数
     * @param isPositive 符号（true为正，false为负）
     * @return 转换后的度数
     */
    public double convertDMStoDegrees(int degrees, int minutes, double seconds, boolean isPositive) {
        double value = degrees + minutes / 60.0 + seconds / 3600.0;
        return isPositive ? value : -value;
    }
    
    /**
     * 将度数格式转换为度分秒格式
     * @param degrees 度数
     * @return 度分秒数组，格式为[度数, 分数, 秒数, 是否为正]
     */
    public Object[] convertDegreesToDMS(double degrees) {
        boolean isPositive = degrees >= 0;
        degrees = Math.abs(degrees);
        
        int intDegrees = (int) Math.floor(degrees);
        double remaining = degrees - intDegrees;
        int intMinutes = (int) Math.floor(remaining * 60);
        double intSeconds = (remaining * 60 - intMinutes) * 60;
        
        return new Object[]{intDegrees, intMinutes, intSeconds, isPositive};
    }
    
    /**
     * 格式化度分秒为字符串
     * @param degrees 度数
     * @param minutes 分数
     * @param seconds 秒数
     * @param isPositive 符号
     * @param isLatitude 是否为纬度
     * @return 格式化后的度分秒字符串
     */
    public String formatDMS(int degrees, int minutes, double seconds, boolean isPositive, boolean isLatitude) {
        String direction = isLatitude 
                ? (isPositive ? "N" : "S") 
                : (isPositive ? "E" : "W");
        
        return String.format("%d°%d′%.2f″%s", degrees, minutes, seconds, direction);
    }
    
    /**
     * 获取加载的模型信息
     * @return 模型信息字符串
     */
    public String getModelInfo() {
        return model.toString();
    }
}