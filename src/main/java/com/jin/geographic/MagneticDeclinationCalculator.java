/**
 * MagneticDeclinationCalculator - 提供通用的磁偏角计算功能
 * 
 * 该类封装了GeographicLibWrapper，提供了更友好的接口来计算地球上任意位置的磁偏角
 */
package com.jin.geographic;

/**
 * 磁偏角计算结果类，包含计算结果和相关信息
 */
class MagneticDeclinationResult {
    private double declination; // 磁偏角值（度）
    private boolean success;    // 计算是否成功
    private String message;     // 结果消息或错误信息

    public MagneticDeclinationResult(double declination, boolean success, String message) {
        this.declination = declination;
        this.success = success;
        this.message = message;
    }

    public double getDeclination() {
        return declination;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        if (success) {
            return String.format("Magnetic declination: %.6f degrees", declination);
        } else {
            return "Failed to calculate magnetic declination: " + message;
        }
    }
}

/**
 * 磁偏角计算器类，提供通用的磁偏角计算方法
 */
public class MagneticDeclinationCalculator {
    private GeographicLibWrapper wrapper;
    private static final String DEFAULT_DATA_PATH = "";
    private String resourceDataPath = null;

    /**
     * 构造函数，初始化计算器
     */
    public MagneticDeclinationCalculator() {
        this.wrapper = new GeographicLibWrapper();
        // 尝试设置项目内部资源路径
        this.resourceDataPath = getResourceDataPath();
    }

    /**
     * 初始化计算器，加载WMM2025数据
     * 
     * @param dataPath WMM2025数据集的路径，如果为空则使用系统默认路径
     * @return 是否初始化成功
     */
    public boolean initialize(String dataPath) {
        try {
            System.out.println("Initializing MagneticDeclinationCalculator with data path: " + 
                             (dataPath.isEmpty() ? "system default" : dataPath));
            boolean success = wrapper.initialize(dataPath);
            if (!success) {
                System.err.println("Failed to initialize with path: " + 
                                 (dataPath.isEmpty() ? "system default" : dataPath));
            }
            return success;
        } catch (Exception e) {
            System.err.println("Error during initialization: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 使用默认路径初始化计算器
     * 优先尝试使用项目内部资源路径
     * 
     * @return 是否初始化成功
     */
    public boolean initialize() {
        // 优先尝试使用项目内部资源路径
        if (resourceDataPath != null) {
            boolean success = initialize(resourceDataPath);
            if (success) {
                return true;
            }
        }
        // 如果内部资源路径初始化失败，再尝试使用系统默认路径
        return initialize(DEFAULT_DATA_PATH);
    }
    
    /**
     * 获取项目内部资源数据路径
     * 处理文件系统和JAR包两种运行环境
     * 
     * @return 资源数据路径，如果获取失败则返回null
     */
    private String getResourceDataPath() {
        try {
            // 尝试直接获取资源路径（文件系统运行环境）
            java.net.URL resourceUrl = getClass().getResource("/magnetic/wmm2025.wmm");
            if (resourceUrl == null) {
                System.out.println("WMM2025 data file not found in resources");
                return null;
            }
            
            // 检查是否在JAR包中运行
            if (resourceUrl.getProtocol().equals("jar")) {
                System.out.println("Running in JAR mode, extracting WMM2025 data to temporary directory");
                return extractResourcesToTempDir();
            } else {
                // 从文件系统直接读取
                java.nio.file.Path resourceDir = java.nio.file.Paths.get(getClass().getResource("/magnetic").toURI());
                return resourceDir.toString();
            }
        } catch (Exception e) {
            System.err.println("Failed to get resource data path: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 在JAR包运行模式下，将WMM2025资源文件提取到临时目录
     * 
     * @return 临时目录路径，如果提取失败则返回null
     */
    private String extractResourcesToTempDir() {
        try {
            // 创建临时目录
            java.nio.file.Path tempDir = java.nio.file.Files.createTempDirectory("wmm2025_");
            tempDir.toFile().deleteOnExit();
            
            // 提取wmm2025.wmm文件
            extractResource("/magnetic/wmm2025.wmm", tempDir.resolve("wmm2025.wmm"));
            
            // 提取wmm2025.wmm.cof文件
            extractResource("/magnetic/wmm2025.wmm.cof", tempDir.resolve("wmm2025.wmm.cof"));
            
            System.out.println("Successfully extracted WMM2025 data to temporary directory: " + tempDir.toString());
            return tempDir.toString();
        } catch (Exception e) {
            System.err.println("Failed to extract WMM2025 data to temporary directory: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 将资源文件从JAR包中提取到指定位置
     */
    private void extractResource(String resourcePath, java.nio.file.Path targetPath) throws Exception {
        try (java.io.InputStream in = getClass().getResourceAsStream(resourcePath);
             java.io.OutputStream out = java.nio.file.Files.newOutputStream(targetPath)) {
            if (in == null) {
                throw new java.io.FileNotFoundException("Resource not found: " + resourcePath);
            }
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }

    /**
     * 通用方法：使用儒略日时间计算指定位置的磁偏角
     * 
     * @param latitude 纬度（度），北纬为正
     * @param longitude 经度（度），东经为正
     * @param altitude 高度（米），海平面以上为正
     * @param julianDay 儒略日时间
     * @return 包含磁偏角计算结果的对象
     */
    public MagneticDeclinationResult calculateDeclination(
            double latitude, double longitude, double altitude, double julianDay) {
        try {
            // 检查是否已初始化
            if (!isInitialized()) {
                boolean initSuccess = initialize();
                if (!initSuccess) {
                    return new MagneticDeclinationResult(0.0, false, 
                            "Failed to initialize GeographicLib. Check if WMM2025 data is available.");
                }
            }

            double declination = wrapper.getMagneticDeclination(latitude, longitude, altitude, julianDay);
            return new MagneticDeclinationResult(
                    declination,
                    true,
                    String.format("Successfully calculated magnetic declination at (%.4f°, %.4f°, %.1f m) using Julian day %.2f",
                            latitude, longitude, altitude, julianDay)
            );
        } catch (Exception e) {
            return new MagneticDeclinationResult(0.0, false, e.getMessage());
        }
    }

    /**
     * 通用方法：使用年、月、日计算指定位置的磁偏角
     * 
     * @param latitude 纬度（度），北纬为正
     * @param longitude 经度（度），东经为正
     * @param altitude 高度（米），海平面以上为正
     * @param year 年份
     * @param month 月份（1-12）
     * @param day 日期（1-31）
     * @return 包含磁偏角计算结果的对象
     */
    public MagneticDeclinationResult calculateDeclinationByDate(
            double latitude, double longitude, double altitude, int year, int month, int day) {
        try {
            // 检查是否已初始化
            if (!isInitialized()) {
                boolean initSuccess = initialize();
                if (!initSuccess) {
                    return new MagneticDeclinationResult(0.0, false, 
                            "Failed to initialize GeographicLib. Check if WMM2025 data is available.");
                }
            }

            double declination = wrapper.getMagneticDeclinationByDate(
                    latitude, longitude, altitude, year, month, day);
            return new MagneticDeclinationResult(
                    declination,
                    true,
                    String.format("Successfully calculated magnetic declination at (%.4f°, %.4f°, %.1f m) for date %d-%02d-%02d",
                            latitude, longitude, altitude, year, month, day)
            );
        } catch (Exception e) {
            return new MagneticDeclinationResult(0.0, false, e.getMessage());
        }
    }

    /**
     * 检查GeographicLib是否已初始化
     * 
     * @return 是否已初始化
     */
    private boolean isInitialized() {
        // 由于我们无法直接检查GeographicLib的初始化状态，
        // 这里简单地尝试一个快速计算来验证
        try {
            // 使用一个已知安全的位置和时间进行快速验证
            double testDeclination = wrapper.getMagneticDeclination(0, 0, 0, 2460364.5);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 释放资源
     */
    public void cleanup() {
        try {
            wrapper.cleanup();
        } catch (Exception e) {
            System.err.println("Error during cleanup: " + e.getMessage());
        }
    }
}