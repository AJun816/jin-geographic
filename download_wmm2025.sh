#!/bin/bash
# WMM2025数据集下载脚本（可选使用）

# 注意：本项目已将WMM2025数据集作为内部资源包含在内
# 通常情况下不需要运行此脚本，程序会自动从项目内部加载数据
# 仅在需要更新系统级数据集时使用此脚本

# 检查项目内部是否已有数据
if [ -f "src/main/resources/magnetic/wmm2025.wmm" ]; then
    echo "注意：WMM2025数据集已包含在项目内部（src/main/resources/magnetic/）"
    echo "程序会自动从项目内部加载数据，无需额外安装"
    read -p "是否仍要下载并安装系统级数据集？(y/n) " choice
    case "$choice" in 
        y|Y ) echo "继续下载安装...";
             ;;
        * ) echo "已取消下载安装";
            exit 0;
             ;;
    esac
fi

# 创建目录
mkdir -p /tmp/geographiclib_data

# 下载WMM2025数据集
echo "Downloading WMM2025 dataset..."
curl -L -o /tmp/geographiclib_data/WMM2025.wmm https://www.ngdc.noaa.gov/IAGA/vmod/wmm2025.wmm

# 检查下载是否成功
if [ -f /tmp/geographiclib_data/WMM2025.wmm ]; then
    echo "WMM2025 dataset downloaded successfully."
    
    # 创建GeographicLib数据目录
echo "Creating GeographicLib data directories..."
sudo mkdir -p /usr/local/share/GeographicLib/magnetic/

# 复制WMM2025数据文件
echo "Copying WMM2025 data files..."
sudo cp /tmp/geographiclib_data/WMM2025.wmm /usr/local/share/GeographicLib/magnetic/

# 设置权限
sudo chmod 644 /usr/local/share/GeographicLib/magnetic/WMM2025.wmm

echo "WMM2025 dataset has been successfully installed!"
echo "Data file location: /usr/local/share/GeographicLib/magnetic/WMM2025.wmm"
else
    echo "Failed to download WMM2025 dataset. Please try manually downloading from NOAA website."
    exit 1
fi

# 清理临时文件
rm -rf /tmp/geographiclib_data/

echo "\n提示："
echo "1. 通常情况下，您不需要运行此脚本，因为数据集已包含在项目内部"
echo "2. 程序会优先尝试从项目内部的src/main/resources/magnetic/目录加载数据"
echo "3. 仅当您希望更新系统级数据集时才需要此脚本"