@echo off
chcp 65001 >nul
title 患者系统 - 模块材料包 Word 生成工具
echo =======================================
echo   患者系统 - 模块材料包 Word 生成工具
echo =======================================
echo.

cd /d "%~dp0"

echo 正在生成 Word 文档（使用纯标准库，无需安装依赖）...
echo.

python generate_docx.py

echo.
if exist "模块材料包-患者系统.docx" (
    echo ✓ 生成成功！
    echo 文件路径：%~dp0模块材料包-患者系统.docx
    start "" "模块材料包-患者系统.docx"
) else (
    echo ✗ 生成失败
    echo 请确认 generate_docx.py 和 模块材料包-患者系统.md 存在
)

echo.
pause
