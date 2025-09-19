package com.magnetic.declination.model;

/**
 * 经纬度模型类，支持度分秒格式和度数格式的经纬度表示
 */
public class LatLon {
    private double latitude; // 纬度（度）
    private double longitude; // 经度（度）
    private double height; // 高度（米）
    private double year; // 年份
    
    /**
     * 使用度数格式创建经纬度对象
     * @param latitude 纬度（度）
     * @param longitude 经度（度）
     * @param height 高度（米）
     * @param year 年份
     */
    public LatLon(double latitude, double longitude, double height, double year) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.height = height;
        this.year = year;
    }
    
    /**
     * 使用度分秒格式创建经纬度对象
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
     */
    public LatLon(int latDegrees, int latMinutes, double latSeconds, 
                 int lonDegrees, int lonMinutes, double lonSeconds, 
                 boolean isLatitudeNorth, boolean isLongitudeEast, 
                 double height, double year) {
        this.latitude = convertDMStoDegrees(latDegrees, latMinutes, latSeconds) * (isLatitudeNorth ? 1 : -1);
        this.longitude = convertDMStoDegrees(lonDegrees, lonMinutes, lonSeconds) * (isLongitudeEast ? 1 : -1);
        this.height = height;
        this.year = year;
    }
    
    /**
     * 将度分秒格式转换为度数格式
     * @param degrees 度数
     * @param minutes 分数
     * @param seconds 秒数
     * @return 转换后的度数
     */
    private double convertDMStoDegrees(int degrees, int minutes, double seconds) {
        return degrees + minutes / 60.0 + seconds / 3600.0;
    }
    
    // getter 和 setter 方法
    public double getLatitude() {
        return latitude;
    }
    
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
    
    public double getLongitude() {
        return longitude;
    }
    
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
    
    public double getHeight() {
        return height;
    }
    
    public void setHeight(double height) {
        this.height = height;
    }
    
    public double getYear() {
        return year;
    }
    
    public void setYear(double year) {
        this.year = year;
    }
    
    @Override
    public String toString() {
        return "LatLon{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                ", height=" + height +
                ", year=" + year +
                '}';
    }
}