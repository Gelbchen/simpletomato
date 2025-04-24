#!/bin/bash

# 这个脚本用来检查项目中的依赖冲突

# 确保gradlew有执行权限
chmod +x gradlew

# 输出依赖树并保存到文件
./gradlew app:dependencies > dependencies.txt

# 查找所有androidx.*依赖
echo "Checking AndroidX dependencies..."
grep -E "androidx\." dependencies.txt

# 查找可能的版本冲突
echo -e "\nPotential version conflicts:"
grep -E "androidx\." dependencies.txt | sort | uniq -c | sort -nr | grep -v "1 "

echo -e "\nDone!" 