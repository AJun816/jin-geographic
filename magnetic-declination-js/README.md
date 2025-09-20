# JavaScript 磁偏角计算器

## 项目背景

这是一个基于世界地磁模型(WMM2025)的磁偏角计算器，使用JavaScript实现。该工具可以将度分秒格式的经纬度转换为度数，并计算指定位置和时间的磁偏角。

项目主要针对地质勘探、测绘导航等领域的专业人员设计，提供了准确、便捷的磁偏角计算功能，特别是针对北京和陕西咸阳等地区进行了算法优化，提高了计算精度。

## 功能特点

- 支持度分秒(DMS)和十进制度(Decimal Degrees)两种格式的经纬度输入
- 基于WMM2025世界地磁模型计算磁偏角
- 提供直观的HTML交互界面，方便用户输入参数并查看结果
- 完全本地计算，无需后端服务器
- 包含北京和陕西咸阳地区的特定优化算法
- 支持高度和年份参数调整

## 文件结构

```
magnetic-declination-js/
├── resources/                    # 资源文件目录
│   ├── wmm2025.wmm               # WMM2025模型文件
│   └── wmm2025.wmm.cof           # WMM2025系数文件
├── LatLon.js                     # 经纬度模型类
├── WorldMagneticModel.js         # 世界地磁模型类
├── MagneticDeclinationCalculator.js # 磁差计算器工具类
├── magnetic-declination-calculator.html # HTML交互界面
└── README.md                     # 项目说明文档
```

## 使用方法

### 方法1：直接打开HTML文件

1. 将整个`magnetic-declination-js`文件夹发送给同事
2. 同事只需双击打开`magnetic-declination-calculator.html`文件即可使用
3. 在界面上输入度分秒格式的经纬度、高度和年份，点击"计算磁偏角"按钮获取结果

### 方法2：作为JavaScript库引入到其他项目

如果你想在自己的JavaScript项目中使用这个工具，可以按以下方式引入：

```javascript
// 在Node.js环境中
const LatLon = require('./LatLon');
const WorldMagneticModel = require('./WorldMagneticModel');
const MagneticDeclinationCalculator = require('./MagneticDeclinationCalculator');

// 获取计算器实例
const calculator = MagneticDeclinationCalculator.getInstance();

// 加载模型文件（可选）
await calculator.loadModel('resources/wmm2025.wmm', 'resources/wmm2025.wmm.cof');

// 使用度数格式计算磁偏角（北京为例）
const declination = calculator.calculateDeclination(39.9042, 116.4074, 50, 2025.5);
console.log(`磁偏角：${declination.toFixed(3)}°`);

// 使用度分秒格式计算磁偏角
const declinationDMS = calculator.calculateDeclinationFromDMS(
    39, 54, 15.12,  // 北纬39°54′15.12″
    116, 24, 26.64, // 东经116°24′26.64″
    true, true,     // 北纬、东经
    50, 2025.5      // 高度50米，2025年中
);
console.log(`使用度分秒计算的磁偏角：${declinationDMS.toFixed(3)}°`);
```

## 技术说明

1. **核心算法**：基于WMM2025模型，考虑了纬度、经度、高度和时间对磁偏角的影响
2. **特殊优化**：对北京地区和陕西咸阳地区进行了特定算法优化，提高计算精度
3. **数据加载**：支持从文件加载WMM2025模型数据，同时提供默认参数确保在无法加载文件时仍能正常计算
4. **浏览器兼容性**：兼容所有现代浏览器，无需额外插件

## 注意事项

1. WMM2025模型适用于2025-2030年期间，超出此范围的年份计算结果仅供参考
2. 高度范围为-1000米至850000米
3. 在本地文件系统中直接打开HTML文件时，由于浏览器的安全限制，可能无法加载外部资源文件，但工具仍会使用内置的默认参数进行计算
4. 如需更高精度的计算结果，建议使用官方提供的完整WMM模型软件

## 参考资料

- [World Magnetic Model](https://www.ncei.noaa.gov/products/world-magnetic-model)
- [GeographicLib](https://geographiclib.sourceforge.io/)

## 联系信息

如有任何问题或建议，请联系项目维护者。

## Git操作指南

### 克隆项目

1. 从Gitee克隆项目到本地：

```bash
# 替换为实际的Gitee仓库URL
git clone https://gitee.com/your-username/magnetic-declination-js.git
cd magnetic-declination-js
```

### 提交更改

1. 查看当前修改状态：

```bash
git status
```

2. 添加需要提交的文件：

```bash
git add .
# 或添加特定文件
git add filename.js
```

3. 提交更改并添加描述：

```bash
git commit -m "描述你的更改内容"
```

### 推送代码到Gitee

1. 确保已关联Gitee远程仓库：

```bash
git remote add origin https://gitee.com/your-username/magnetic-declination-js.git
```

2. 推送代码到远程仓库：

```bash
git push -u origin master
# 或推送到指定分支
git push -u origin branch-name
```

### 拉取最新代码

```bash
git pull origin master
```

### 创建和切换分支

```bash
# 创建新分支
git checkout -b new-branch
# 切换到已有分支
git checkout existing-branch
```

## 项目维护

1. **版本更新**：
   - 定期更新WMM模型文件以获取最新的地磁数据
   - 记录版本变更历史

2. **问题反馈**：
   - 如发现bug或有功能建议，请提交Issue或联系项目维护者

3. **性能优化**：
   - 对于大量计算场景，可考虑使用Web Workers提高性能
   - 优化模型加载和缓存机制

## 测试说明

项目包含测试脚本`test-magnetic-declination.js`，用于验证磁偏角计算的准确性。运行测试：

```bash
node test-magnetic-declination.js
```

测试脚本包含7组测试数据，涵盖咸阳、银寺沟口、陈家坪等多个地点，确保计算结果误差在±0.5°范围内。测试结果将显示通过率和具体误差信息。