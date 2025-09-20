/**
 * 磁差计算器工具类，提供计算磁差的主要功能
 */
// 确保在Node.js环境中正确导入依赖
const WorldMagneticModel = typeof window !== 'undefined' ? window.WorldMagneticModel : require('./WorldMagneticModel');
const LatLon = typeof window !== 'undefined' ? window.LatLon : require('./LatLon');

class MagneticDeclinationCalculator {
    constructor() {
        if (MagneticDeclinationCalculator.instance) {
            throw new Error('请使用getInstance()方法获取单例实例');
        }
        this.model = new WorldMagneticModel();
        this.isModelLoaded = false;
    }

    /**
     * 获取单例实例
     * @return {MagneticDeclinationCalculator} 计算器实例
     */
    static getInstance() {
        if (!MagneticDeclinationCalculator.instance) {
            MagneticDeclinationCalculator.instance = new MagneticDeclinationCalculator();
        }
        return MagneticDeclinationCalculator.instance;
    }

    /**
     * 加载WMM模型
     * @param {string} modelFilePath - 模型文件路径
     * @param {string} coefficientsFilePath - 系数文件路径
     * @return {Promise<void>} - 加载完成的Promise
     */
    async loadModel(modelFilePath, coefficientsFilePath) {
        try {
            await this.model.loadModel(modelFilePath, coefficientsFilePath);
            this.isModelLoaded = true;
            console.log('模型加载成功：', this.model.getName());
            console.log('模型描述：', this.model.getDescription());
            console.log('适用年份范围：', this.model.getMinTime(), ' - ', this.model.getMaxTime());
            console.log('适用高度范围：', this.model.getMinHeight(), ' - ', this.model.getMaxHeight(), '米');
        } catch (error) {
            console.error('模型加载失败：', error);
            throw error;
        }
    }

    /**
     * 使用度数格式计算磁差
     * @param {number} latitude - 纬度（度）
     * @param {number} longitude - 经度（度）
     * @param {number} height - 高度（米）
     * @param {number} year - 年份
     * @return {number} 磁差（度）
     */
    calculateDeclination(latitude, longitude, height = 0, year = 2025) {
        if (!this.isModelLoaded) {
            console.warn('警告：模型尚未加载，可能影响计算精度');
        }

        const latLon = new LatLon({
            latitude: latitude,
            longitude: longitude,
            height: height,
            year: year
        });
        return this.model.calculateDeclination(latLon);
    }

    /**
     * 使用度分秒格式计算磁差
     * @param {number} latDegrees - 纬度度数
     * @param {number} latMinutes - 纬度分数
     * @param {number} latSeconds - 纬度秒数
     * @param {number} lonDegrees - 经度度数
     * @param {number} lonMinutes - 经度分数
     * @param {number} lonSeconds - 经度秒数
     * @param {boolean} isLatitudeNorth - 是否北纬
     * @param {boolean} isLongitudeEast - 是否东经
     * @param {number} height - 高度（米）
     * @param {number} year - 年份
     * @return {number} 磁差（度）
     */
    calculateDeclinationFromDMS(latDegrees, latMinutes, latSeconds, 
                              lonDegrees, lonMinutes, lonSeconds, 
                              isLatitudeNorth, isLongitudeEast, 
                              height = 0, year = 2025) {
        if (!this.isModelLoaded) {
            console.warn('警告：模型尚未加载，可能影响计算精度');
        }

        const latLon = new LatLon({
            latDegrees: latDegrees,
            latMinutes: latMinutes,
            latSeconds: latSeconds,
            lonDegrees: lonDegrees,
            lonMinutes: lonMinutes,
            lonSeconds: lonSeconds,
            isLatitudeNorth: isLatitudeNorth,
            isLongitudeEast: isLongitudeEast,
            height: height,
            year: year
        });
        return this.model.calculateDeclination(latLon);
    }

    /**
     * 将度分秒格式转换为度数格式
     * @param {number} degrees - 度数
     * @param {number} minutes - 分数
     * @param {number} seconds - 秒数
     * @param {boolean} isPositive - 符号（true为正，false为负）
     * @return {number} 转换后的度数
     */
    convertDMStoDegrees(degrees, minutes, seconds, isPositive) {
        const value = degrees + minutes / 60.0 + seconds / 3600.0;
        return isPositive ? value : -value;
    }

    /**
     * 将度数格式转换为度分秒格式
     * @param {number} degrees - 度数
     * @return {Object} 度分秒对象 {degrees, minutes, seconds, isPositive}
     */
    convertDegreesToDMS(degrees) {
        return LatLon.convertDegreesToDMS(degrees);
    }

    /**
     * 格式化度分秒为字符串
     * @param {number} degrees - 度数
     * @param {number} minutes - 分数
     * @param {number} seconds - 秒数
     * @param {boolean} isPositive - 符号
     * @param {boolean} isLatitude - 是否为纬度
     * @return {string} 格式化后的度分秒字符串
     */
    formatDMS(degrees, minutes, seconds, isPositive, isLatitude) {
        return LatLon.formatDMS(degrees, minutes, seconds, isPositive, isLatitude);
    }

    /**
     * 获取加载的模型信息
     * @return {string} 模型信息字符串
     */
    getModelInfo() {
        return this.model.toString();
    }

    /**
     * 检查模型是否已加载
     * @return {boolean} 是否加载
     */
    isLoaded() {
        return this.isModelLoaded;
    }
}

// 初始化单例实例
MagneticDeclinationCalculator.instance = null;

// 导出类以便在浏览器和Node.js环境中使用
if (typeof module !== 'undefined' && typeof module.exports !== 'undefined') {
    module.exports = MagneticDeclinationCalculator;
} else {
    window.MagneticDeclinationCalculator = MagneticDeclinationCalculator;
}