#include <jni.h>
#include <GeographicLib/MagneticModel.hpp>
#include <string>
#include <cmath>
#include <iostream>

// 全局变量，用于存储MagneticModel实例
static GeographicLib::MagneticModel* magneticModel = nullptr;

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