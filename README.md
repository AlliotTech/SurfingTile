# SurfingTile

一个Android快速设置磁贴应用，用于控制box4服务。

## 功能特性

- ✅ 快速设置磁贴控制
- ✅ 实时状态显示
- ✅ 优化性能和资源管理
- ✅ 改进日志记录和调试信息
- ✅ 更新模块路径为box4

## 重要说明

**本应用仅支持box4服务，不支持Surfing服务。**

## 自动发布

本项目使用GitHub Actions进行自动构建和发布。

### 发布新版本

1. 更新版本号（在 `app/build.gradle.kts` 中）
2. 提交代码并推送
3. 创建并推送版本标签：

```bash
git tag v1.0.0
git push origin v1.0.0
```

4. GitHub Actions会自动：
   - 编译APK文件
   - 创建GitHub Release
   - 上传APK供下载

### 手动触发构建

你也可以在GitHub仓库的Actions页面手动触发构建。

## 本地构建

```bash
./gradlew assembleRelease
```

构建产物位于：`app/build/outputs/apk/release/app-release.apk`

## 致谢

本项目参考了 [GitMetaio/Surfing](https://github.com/GitMetaio/Surfing) 项目的设计思路，特此致谢。

## 许可证

本项目采用开源许可证，请遵守相关法律法规。 