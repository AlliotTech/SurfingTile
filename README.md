# SurfingTile - Android快速设置面板应用

## 项目概述

SurfingTile 是一个Android快速设置面板（Quick Settings Tile）应用，用于控制Box网络代理服务的启动和停止。用户可以通过Android下拉通知栏的快速设置区域快速切换代理服务状态。

## 主要功能

- 🔄 **快速切换**: 一键开启/关闭Box代理服务
- 📊 **状态监控**: 实时显示服务运行状态
- ⚡ **异步处理**: 非阻塞操作，提升用户体验
- 🛡️ **错误处理**: 完善的异常处理和状态恢复机制

## 技术架构

### 核心组件

1. **SurfingTileService**: 主要的Tile服务类，处理用户交互
2. **Config**: 配置管理类，集中管理所有配置参数
3. **ProcessUtils**: 进程操作工具类，提供进程执行和结果处理功能

### 架构特点

- **模块化设计**: 功能分离，便于维护和测试
- **异步处理**: 使用CompletableFuture避免阻塞UI线程
- **资源管理**: 完善的资源清理机制
- **错误恢复**: 智能的状态恢复和错误处理

## 优化内容

### 1. 代码结构优化

#### 原始问题
- 所有逻辑集中在单个类中
- 硬编码的配置参数
- 重复的进程操作代码

#### 优化方案
- 创建了`Config`类集中管理配置
- 创建了`ProcessUtils`工具类处理进程操作
- 将业务逻辑分离到独立方法中

### 2. 性能优化

#### 原始问题
- 同步操作可能阻塞UI
- 没有防止重复点击机制
- 进程等待可能无限期阻塞

#### 优化方案
- 使用`CompletableFuture`进行异步处理
- 添加`isProcessing`标志防止重复点击
- 实现超时机制避免无限等待

### 3. 错误处理优化

#### 原始问题
- 异常处理不够完善
- 错误状态下的UI更新不明确
- 缺乏详细的日志记录

#### 优化方案
- 完善的try-catch异常处理
- 错误状态下的状态恢复机制
- 详细的日志记录和错误分类

### 4. 代码质量提升

#### 原始问题
- 缺乏单元测试
- 代码注释不足
- 没有代码覆盖率统计

#### 优化方案
- 添加了完整的单元测试
- 详细的代码注释和文档
- 启用代码覆盖率统计

## 安装和使用

### 系统要求

- Android 7.0+ (API 24+)
- Root权限（用于文件操作）
- 已安装Box代理服务

### 安装步骤

1. 编译并安装APK
2. 授予Root权限
3. 在快速设置面板中添加"Surfing"图块
4. 点击图块即可切换服务状态

### 使用方法

- **绿色状态**: 服务正在运行
- **灰色状态**: 服务已停止
- **红色状态**: 服务不可用

## 开发指南

### 项目结构

```
app/src/main/java/com/yadli/surfingtile/
├── SurfingTileService.java    # 主服务类
├── Config.java               # 配置管理类
└── ProcessUtils.java         # 进程工具类

app/src/test/java/com/yadli/surfingtile/
└── SurfingTileServiceTest.java  # 单元测试
```

### 构建和测试

```bash
# 构建项目
./gradlew build

# 运行单元测试
./gradlew test

# 生成测试覆盖率报告
./gradlew testDebugUnitTestCoverage
```

### 配置说明

主要配置参数在`Config.java`中：

- `SERVICE_URL`: Box服务地址
- `DISABLE_FILE_PATH`: 控制文件路径
- `CONNECT_TIMEOUT_MS`: 连接超时时间
- `READ_TIMEOUT_MS`: 读取超时时间
- `COMMAND_TIMEOUT_MS`: 命令执行超时时间

## 技术细节

### 服务状态检测

使用curl命令检测本地9090端口：

```bash
curl -s --connect-timeout 1 -m 1 http://localhost:9090
```

- 退出码0或28: 服务运行中
- 其他退出码: 服务已停止

### 服务控制机制

通过操作控制文件来管理服务：

- **启动服务**: 删除`/data/adb/modules/box4/disable`文件
- **停止服务**: 创建`/data/adb/modules/box4/disable`文件

### 异步处理流程

1. 用户点击图块
2. 检查是否正在处理中
3. 异步获取当前状态
4. 确定新状态
5. 执行状态切换
6. 更新UI显示
7. 记录操作日志

## 故障排除

### 常见问题

1. **图块显示红色**
   - 检查设备是否已Root
   - 确认Box服务是否正确安装

2. **点击无响应**
   - 检查Root权限是否已授予
   - 查看日志确认错误信息

3. **状态检测不准确**
   - 确认Box服务端口是否为9090
   - 检查网络连接状态

### 日志查看

```bash
adb logcat | grep SurfingTileService
```

## 贡献指南

1. Fork项目
2. 创建功能分支
3. 提交更改
4. 添加测试
5. 提交Pull Request

## 许可证

本项目采用MIT许可证，详见LICENSE文件。

## 更新日志

### v1.0 (优化版本)
- ✅ 重构代码结构，提高可维护性
- ✅ 添加异步处理，提升用户体验
- ✅ 完善错误处理和状态恢复
- ✅ 添加单元测试和代码覆盖率
- ✅ 优化性能和资源管理
- ✅ 改进日志记录和调试信息
- ✅ 更新模块路径为box4

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