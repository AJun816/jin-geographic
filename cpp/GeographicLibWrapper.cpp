#include <jni.h>
#include <GeographicLib/MagneticModel.hpp>
#include <string>
#include <cmath>
#include <iostream>
#include <cctype>

// 使用命名空间
using namespace GeographicLib;

// 全局变量，用于存储MagneticModel实例
static MagneticModel* magneticModel = nullptr;

/**
 * （测试用）初始化GeographicLib库，加载WMM2025数据
 * @param dataPath WMM2025数据文件路径
 * @return 是否初始化成功
 */
bool initializeGeographicLib(const char* dataPath) {
    try {
        // 清理之前可能存在的模型实例
        if (magneticModel != nullptr) {
            delete magneticModel;
            magneticModel = nullptr;
        }
        
        std::string dataDirectory = (dataPath != nullptr) ? std::string(dataPath) : "";
        
        // 初始化WMM2025模型
        if (dataDirectory.empty()) {
            magneticModel = new MagneticModel("WMM2025");
        } else {
            magneticModel = new MagneticModel("WMM2025", dataDirectory);
        }
        
        return true;
    } catch (const std::exception& e) {
        std::cerr << "Error initializing GeographicLib: " << e.what() << std::endl;
        return false;
    }
}

/**
 * （测试用）释放GeographicLib资源
 */
void cleanupGeographicLib() {
    try {
        if (magneticModel != nullptr) {
            delete magneticModel;
            magneticModel = nullptr;
        }
    } catch (const std::exception& e) {
        std::cerr << "Error cleaning up GeographicLib: " << e.what() << std::endl;
    }
}

/**
 * 初始化GeographicLib库，加载WMM2025数据
 */
extern "C" JNIEXPORT jboolean JNICALL Java_com_jin_geographic_GeographicLibWrapper_initialize
  (JNIEnv *env, jobject obj, jstring dataPath) {
    try {
        // 清理之前可能存在的模型实例
        if (magneticModel != nullptr) {
            delete magneticModel;
            magneticModel = nullptr;
        }
        
        // 获取数据路径
        const char* path = env->GetStringUTFChars(dataPath, nullptr);
        std::string dataDirectory = (path != nullptr) ? std::string(path) : "";
        env->ReleaseStringUTFChars(dataPath, path);
        
        // 初始化WMM2025模型
        if (dataDirectory.empty()) {
            magneticModel = new GeographicLib::MagneticModel("WMM2025");
        } else {
            magneticModel = new GeographicLib::MagneticModel("WMM2025", dataDirectory);
        }
        
        return JNI_TRUE;
    } catch (const std::exception& e) {
        std::cerr << "Error initializing GeographicLib: " << e.what() << std::endl;
        return JNI_FALSE;
    }
}

/**
 * 计算指定位置和时间的磁偏角
 */
extern "C" JNIEXPORT jdouble JNICALL Java_com_jin_geographic_GeographicLibWrapper_getMagneticDeclination
  (JNIEnv *env, jobject obj, jdouble latitude, jdouble longitude, jdouble altitude, jdouble time) {
    try {
        if (magneticModel == nullptr) {
            throw std::runtime_error("GeographicLib not initialized");
        }
        
        double Bx, By, Bz;
        
        // 调用GeographicLib计算磁场分量
        (*magneticModel)(time, latitude, longitude, altitude, Bx, By, Bz);
        
        // 计算磁偏角
        double declination = std::atan2(By, Bx) * 180.0 / M_PI;
        
        return declination;
    } catch (const std::exception& e) {
        std::cerr << "Error calculating magnetic declination: " << e.what() << std::endl;
        return 0.0;
    }
}

/**
 * 将日期转换为儒略日
 */
double dateToJulianDay(int year, int month, int day) {
    int a = (14 - month) / 12;
    int y = year + 4800 - a;
    int m = month + 12 * a - 3;
    
    double jd = day + floor((153 * m + 2) / 5) + 365 * y + 
               floor(y / 4) - floor(y / 100) + floor(y / 400) - 32045;
    
    // GeographicLib使用的是中午儒略日
    return jd - 0.5;
}

/**
 * 计算指定位置、日期的磁偏角（使用更友好的日期格式）
 */
extern "C" JNIEXPORT jdouble JNICALL Java_com_jin_geographic_GeographicLibWrapper_getMagneticDeclinationByDate
  (JNIEnv *env, jobject obj, jdouble latitude, jdouble longitude, jdouble altitude, 
   jint year, jint month, jint day) {
    try {
        // 将日期转换为儒略日
        double jd = dateToJulianDay(year, month, day);
        
        // 调用上面的函数计算磁偏角
        return Java_com_jin_geographic_GeographicLibWrapper_getMagneticDeclination(
            env, obj, latitude, longitude, altitude, jd);
    } catch (const std::exception& e) {
        std::cerr << "Error calculating magnetic declination by date: " << e.what() << std::endl;
        return 0.0;
    }
}

/**
 * 释放GeographicLib资源
 */
extern "C" JNIEXPORT void JNICALL Java_com_jin_geographic_GeographicLibWrapper_cleanup
  (JNIEnv *env, jobject obj) {
    try {
        if (magneticModel != nullptr) {
            delete magneticModel;
            magneticModel = nullptr;
        }
    } catch (const std::exception& e) {
        std::cerr << "Error cleaning up GeographicLib: " << e.what() << std::endl;
    }
}

/**
 * 将度分秒格式转换为十进制度数
 * @param degrees 度
 * @param minutes 分
 * @param seconds 秒
 * @param direction 方向（N/S/E/W）
 * @return 十进制度数
 */
double dmsToDecimal(double degrees, double minutes, double seconds, char direction) {
    double decimal = abs(degrees) + abs(minutes) / 60 + abs(seconds) / 3600;
    
    // 根据方向调整符号
    direction = toupper(direction);
    if (direction == 'S' || direction == 'W') {
        decimal = -decimal;
    }
    
    return decimal;
}

/**
 * 使用度分秒格式计算指定位置和日期的磁偏角
 * @param latDeg 纬度度
 * @param latMin 纬度分
 * @param latSec 纬度秒
 * @param latDir 纬度方向（N/S）
 * @param lonDeg 经度度
 * @param lonMin 经度分
 * @param lonSec 经度秒
 * @param lonDir 经度方向（E/W）
 * @param altitude 高度（米）
 * @param year 年份
 * @param month 月份
 * @param day 日期
 * @return 磁偏角（度）
 */
extern "C" JNIEXPORT jdouble JNICALL Java_com_jin_geographic_GeographicLibWrapper_getMagneticDeclinationFromDMS
  (JNIEnv *env, jobject obj, jdouble latDeg, jdouble latMin, jdouble latSec, jchar latDir,
   jdouble lonDeg, jdouble lonMin, jdouble lonSec, jchar lonDir,
   jdouble altitude, jint year, jint month, jint day) {
    try {
        // 将度分秒转换为十进制度
        double latitude = dmsToDecimal(latDeg, latMin, latSec, (char)latDir);
        double longitude = dmsToDecimal(lonDeg, lonMin, lonSec, (char)lonDir);
        
        // 调用现有的按日期计算磁偏角的函数
        return Java_com_jin_geographic_GeographicLibWrapper_getMagneticDeclinationByDate(
            env, obj, latitude, longitude, altitude, year, month, day);
    } catch (const std::exception& e) {
        std::cerr << "Error calculating magnetic declination from DMS: " << e.what() << std::endl;
        return 0.0;
    }
}

/**
 * （测试用）直接使用度分秒格式计算磁偏角的C++函数
 * @param latDeg 纬度度
 * @param latMin 纬度分
 * @param latSec 纬度秒
 * @param latDir 纬度方向（N/S）
 * @param lonDeg 经度度
 * @param lonMin 经度分
 * @param lonSec 经度秒
 * @param lonDir 经度方向（E/W）
 * @param altitude 高度（米）
 * @param year 年份
 * @param month 月份
 * @param day 日期
 * @return 磁偏角（度）
 */
double calculateDeclinationFromDMS(double latDeg, double latMin, double latSec, char latDir,
                                   double lonDeg, double lonMin, double lonSec, char lonDir,
                                   double altitude, int year, int month, int day) {
    try {
        // 将度分秒转换为十进制度
        double latitude = dmsToDecimal(latDeg, latMin, latSec, latDir);
        double longitude = dmsToDecimal(lonDeg, lonMin, lonSec, lonDir);
        
        // 将日期转换为儒略日
        double jd = dateToJulianDay(year, month, day);
        
        // 确保模型已初始化
        if (magneticModel == nullptr) {
            throw std::runtime_error("GeographicLib not initialized");
        }
        
        double Bx, By, Bz;
        
        // 调用GeographicLib计算磁场分量
        (*magneticModel)(jd, latitude, longitude, altitude, Bx, By, Bz);
        
        // 计算磁偏角
        double declination = std::atan2(By, Bx) * 180.0 / M_PI;
        
        return declination;
    } catch (const std::exception& e) {
        std::cerr << "Error in calculateDeclinationFromDMS: " << e.what() << std::endl;
        return 0.0;
    }
}