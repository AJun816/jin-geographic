/**
 * 经纬度模型类，支持度分秒格式和度数格式的经纬度表示
 */
class LatLon {
    /**
     * 构造函数
     * @param {Object} options - 配置选项
     */
    constructor(options) {
        if (options.latitude !== undefined && options.longitude !== undefined) {
            // 使用度数格式创建
            this.latitude = options.latitude;  // 纬度（度）
            this.longitude = options.longitude; // 经度（度）
            this.height = options.height || 0; // 高度（米）
            this.year = options.year || 2025; // 年份
        } else if (options.latDegrees !== undefined && options.lonDegrees !== undefined) {
            // 使用度分秒格式创建
            this.latitude = LatLon.convertDMStoDegrees(options.latDegrees, options.latMinutes, options.latSeconds, options.isLatitudeNorth);
            this.longitude = LatLon.convertDMStoDegrees(options.lonDegrees, options.lonMinutes, options.lonSeconds, options.isLongitudeEast);
            this.height = options.height || 0;
            this.year = options.year || 2025;
        } else {
            throw new Error('Invalid parameters for LatLon constructor');
        }
    }

    /**
     * 将度分秒格式转换为度数格式
     * @param {number} degrees - 度数
     * @param {number} minutes - 分数
     * @param {number} seconds - 秒数
     * @param {boolean} isPositive - 是否为正数（北纬或东经）
     * @return {number} 转换后的度数
     */
    static convertDMStoDegrees(degrees, minutes, seconds, isPositive = true) {
        const value = degrees + minutes / 60.0 + seconds / 3600.0;
        return isPositive ? value : -value;
    }

    /**
     * 将度数格式转换为度分秒格式
     * @param {number} degrees - 度数
     * @return {Object} 度分秒对象 {degrees, minutes, seconds, isPositive}
     */
    static convertDegreesToDMS(degrees) {
        const isPositive = degrees >= 0;
        degrees = Math.abs(degrees);
        
        const intDegrees = Math.floor(degrees);
        const remaining = degrees - intDegrees;
        const intMinutes = Math.floor(remaining * 60);
        const intSeconds = (remaining * 60 - intMinutes) * 60;
        
        return {
            degrees: intDegrees,
            minutes: intMinutes,
            seconds: intSeconds,
            isPositive: isPositive
        };
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
    static formatDMS(degrees, minutes, seconds, isPositive, isLatitude) {
        const direction = isLatitude 
            ? (isPositive ? 'N' : 'S') 
            : (isPositive ? 'E' : 'W');
        
        return `${degrees}°${minutes}′${seconds.toFixed(2)}″${direction}`;
    }

    // Getter 和 setter 方法
    getLatitude() {
        return this.latitude;
    }

    setLatitude(latitude) {
        this.latitude = latitude;
    }

    getLongitude() {
        return this.longitude;
    }

    setLongitude(longitude) {
        this.longitude = longitude;
    }

    getHeight() {
        return this.height;
    }

    setHeight(height) {
        this.height = height;
    }

    getYear() {
        return this.year;
    }

    setYear(year) {
        this.year = year;
    }

    toString() {
        return `LatLon{latitude=${this.latitude}, longitude=${this.longitude}, height=${this.height}, year=${this.year}}`;
    }
}

// 导出类以便在浏览器和Node.js环境中使用
if (typeof module !== 'undefined' && typeof module.exports !== 'undefined') {
    module.exports = LatLon;
} else {
    window.LatLon = LatLon;
}