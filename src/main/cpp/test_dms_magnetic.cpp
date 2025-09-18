#include <iostream>
#include "GeographicLibWrapper.cpp"

int main() {
    // 初始化WMM2025模型
    // 正确的相对路径
    const char* dataPath = "../../../src/main/resources/magnetic";
    bool initialized = initializeGeographicLib(dataPath);
    
    if (!initialized) {
        std::cerr << "Failed to initialize WMM2025 model" << std::endl;
        return 1;
    }
    
    std::cout << "WMM2025 model initialized successfully" << std::endl;
    
    try {
        // 测试位置：39.900000°N, 116.400000°E
        // 转换为度分秒格式：
        // 纬度：39°54'00"N
        // 经度：116°24'00"E
        double latDeg = 39.0;
        double latMin = 54.0;
        double latSec = 0.0;
        char latDir = 'N';
        
        double lonDeg = 116.0;
        double lonMin = 24.0;
        double lonSec = 0.0;
        char lonDir = 'E';
        
        // 高度（米）
        double altitude = 0.0;
        
        // 日期：2025年1月1日（WMM2025模型的适用年份）
        int year = 2025;
        int month = 1;
        int day = 1;
        
        // 添加调试信息的版本
        double latitude = dmsToDecimal(latDeg, latMin, latSec, latDir);
        double longitude = dmsToDecimal(lonDeg, lonMin, lonSec, lonDir);
        
        std::cout << "\n调试信息：" << std::endl;
        std::cout << "转换后的纬度：" << latitude << "°" << std::endl;
        std::cout << "转换后的经度：" << longitude << "°" << std::endl;
        
        // 手动计算磁偏角以获取详细信息
        double debugJd = dateToJulianDay(year, month, day);
        std::cout << "儒略日：" << debugJd << std::endl;
        
        double Bx, By, Bz;
        if (magneticModel != nullptr) {
            (*magneticModel)(debugJd, latitude, longitude, altitude, Bx, By, Bz);
            std::cout << "磁场分量：Bx=" << Bx << " nT, By=" << By << " nT, Bz=" << Bz << " nT" << std::endl;
        }
        
        // 计算磁偏角
        double declination = std::atan2(By, Bx) * 180.0 / M_PI;
        
        // 输出结果
        std::cout << "\n测试位置：" << latDeg << "°" << latMin << "'" << latSec << "\"" << latDir << ", " \
                  << lonDeg << "°" << lonMin << "'" << lonSec << "\"" << lonDir << std::endl;
        std::cout << "高度：" << altitude << " 米" << std::endl;
        std::cout << "日期：" << year << "-" << month << "-" << day << std::endl;
        
        // 调整磁偏角到[-180, 180]范围内
        double adjustedDeclination = fmod(declination + 180.0, 360.0) - 180.0;
        
        std::cout << "计算得到的磁偏角：" << adjustedDeclination << "°" << std::endl;
        
        // 判断东/西向
        if (adjustedDeclination > 0) {
            std::cout << "（东向）" << std::endl;
        } else if (adjustedDeclination < 0) {
            std::cout << "（西向）" << std::endl;
        } else {
            std::cout << "（无磁偏角）" << std::endl;
        }
        
        // 与预期值（7.53° W）比较
        double expectedDeclination = -7.53; // 西向为负
        double tolerance = 0.5; // 允许的误差范围
        
        std::cout << "\n验证结果：" << std::endl;
        std::cout << "预期磁偏角：" << expectedDeclination << "° W" << std::endl;
        
        if (std::abs(declination - expectedDeclination) <= tolerance) {
            std::cout << "✅ 计算结果与预期值一致（误差在允许范围内）" << std::endl;
        } else {
            std::cout << "❌ 计算结果与预期值存在较大差异（误差超出允许范围）" << std::endl;
            std::cout << "实际误差：" << std::abs(declination - expectedDeclination) << "°" << std::endl;
        }
        
        // 额外测试：使用十进制度直接计算，验证度分秒转换的准确性
        double decimalLat = 39.900000;
        double decimalLon = 116.400000;
        
        // 将日期转换为儒略日
        double jd = dateToJulianDay(year, month, day);
        
        // 直接使用十进制度计算磁偏角
        if (magneticModel != nullptr) {
            double Bx, By, Bz;
            (*magneticModel)(jd, decimalLat, decimalLon, altitude, Bx, By, Bz);
            double directDeclination = std::atan2(By, Bx) * 180.0 / M_PI;
            
            std::cout << "\n额外验证：" << std::endl;
            std::cout << "使用十进制度直接计算的磁偏角：" << directDeclination << "°" << std::endl;
            
            // 比较度分秒转换和直接使用十进制度的结果差异
            double conversionDiff = std::abs(declination - directDeclination);
            std::cout << "度分秒转换与直接使用十进制度的结果差异：" << conversionDiff << "°" << std::endl;
            
            if (conversionDiff < 0.0001) {
                std::cout << "✅ 度分秒转换功能正常" << std::endl;
            } else {
                std::cout << "❌ 度分秒转换存在精度问题" << std::endl;
            }
        }
        
    } catch (const std::exception& e) {
        std::cerr << "Error during test: " << e.what() << std::endl;
    }
    
    // 清理资源
    cleanupGeographicLib();
    
    return 0;
}