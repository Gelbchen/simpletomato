#!/bin/bash

# 创建必要的目录
mkdir -p apk

# 导出APK路径
export APK_PATH="app/build/outputs/apk/debug/app-debug.apk"

# 检查是否安装了Android SDK
if [ -z "$ANDROID_HOME" ]; then
  echo "请先设置ANDROID_HOME环境变量，指向Android SDK的安装位置"
  exit 1
fi

# 使用Android SDK的工具打包应用
echo "开始构建APK..."
"$ANDROID_HOME/build-tools/33.0.0/aapt" package -f -M app/src/main/AndroidManifest.xml -I "$ANDROID_HOME/platforms/android-33/android.jar" -F "$APK_PATH"

# 检查APK是否生成成功
if [ -f "$APK_PATH" ]; then
  # 复制APK到项目根目录
  cp "$APK_PATH" "apk/SimpleTomato.apk"
  echo "APK生成成功！文件位置: $(pwd)/apk/SimpleTomato.apk"
else
  echo "APK生成失败，请检查错误信息。"
fi

# 提供安装指南
echo ""
echo "============安装指南============"
echo "要在安卓设备上安装APK，请将apk/SimpleTomato.apk传输到您的设备。"
echo "然后在设备上找到该文件并点击安装。"
echo "注意：可能需要在设备上允许安装未知来源的应用。" 