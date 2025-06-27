package com.yadli.surfingtile;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.util.Log;
import android.widget.Toast;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SurfingTileService extends TileService {

    private static final String TAG = Config.LOG_TAG;
    
    // 使用单线程执行器避免并发问题
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile boolean isProcessing = false;
    
    // 用于在主线程中显示Toast的Handler
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    public void onStartListening() {
        super.onStartListening();
        showToast("Tile开始监听");
        updateTileStateAsync();
    }

    @Override
    public void onClick() {
        super.onClick();
        
        // 防止重复点击
        if (isProcessing) {
            showToast("操作进行中，请稍候...");
            Log.d(TAG, "Operation in progress, ignoring click");
            return;
        }
        
        isProcessing = true;
        showToast("正在处理点击事件...");
        Log.d(TAG, "onClick enter");
        
        // 异步处理点击事件
        CompletableFuture.runAsync(() -> {
            try {
                handleTileClick();
            } finally {
                isProcessing = false;
            }
        }, executor);
    }

    private void handleTileClick() {
        try {
            showToast("检查当前状态...");
            int currentState = getBoxState();
            int newState = determineNewState(currentState);
            
            showToast("正在切换状态...");
            boolean success = setBoxState(newState);
            if (success) {
                updateTileUI(newState);
                logStateChange(newState);
                showToast("状态切换成功");
            } else {
                Log.e(TAG, "Failed to change box service state");
                showToast("状态切换失败");
                // 恢复原状态
                updateTileUI(currentState);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling tile click", e);
            showToast("操作出错: " + e.getMessage());
            // 出错时设置为不可用状态
            updateTileUI(Tile.STATE_UNAVAILABLE);
        }
    }

    private int determineNewState(int currentState) {
        switch(currentState) {
            case Tile.STATE_ACTIVE:
                showToast("当前状态: 运行中 → 停止");
                return Tile.STATE_INACTIVE;
            case Tile.STATE_INACTIVE:
                showToast("当前状态: 已停止 → 启动");
                return Tile.STATE_ACTIVE;
            case Tile.STATE_UNAVAILABLE:
                showToast("当前状态: 不可用 → 尝试启动");
                Log.w(TAG, "Service unavailable, attempting to start");
                return Tile.STATE_ACTIVE;
            default:
                showToast("当前状态: 未知 → 默认启动");
                Log.w(TAG, "Unknown state: " + currentState + ", defaulting to active");
                return Tile.STATE_ACTIVE;
        }
    }

    private void updateTileStateAsync() {
        CompletableFuture.runAsync(() -> {
            try {
                showToast("更新磁贴状态...");
                int state = getBoxState();
                updateTileUI(state);
                showToast("状态更新完成");
            } catch (Exception e) {
                Log.e(TAG, "Error updating tile state", e);
                showToast("状态更新失败: " + e.getMessage());
                updateTileUI(Tile.STATE_UNAVAILABLE);
            }
        }, executor);
    }

    private void updateTileUI(int state) {
        Tile tile = getQsTile();
        if (tile != null) {
            tile.setState(state);
            tile.updateTile();
        }
    }

    private void logStateChange(int state) {
        if (state == Tile.STATE_ACTIVE) {
            Log.i(TAG, "Box service started successfully");
            showToast("Box服务已启动");
        } else if (state == Tile.STATE_INACTIVE) {
            Log.i(TAG, "Box service stopped successfully");
            showToast("Box服务已停止");
        }
    }

    private int getBoxState() {
        try {
            showToast("检查服务状态...");
            // 使用工具类执行curl命令
            ProcessUtils.ProcessResult result = ProcessUtils.executeCommand(
                Config.buildCurlCommand(), 
                Config.CONNECT_TIMEOUT_MS + Config.READ_TIMEOUT_MS
            );
            
            Log.d(TAG, "Curl result: " + result);
            showToast("Curl结果: " + result.exitCode);
            
            if (ProcessUtils.isCurlSuccess(result.exitCode)) {
                Log.d(TAG, "Service appears to be running");
                showToast("服务正在运行");
                return Tile.STATE_ACTIVE;
            } else {
                Log.d(TAG, "Service appears to be stopped");
                showToast("服务已停止");
                return Tile.STATE_INACTIVE;
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error checking service status: " + e.getMessage());
            showToast("检查状态出错: " + e.getMessage());
            return Tile.STATE_INACTIVE;
        }
    }

    private boolean setBoxState(int state) {
        try {
            boolean isEnableOperation = (state == Tile.STATE_ACTIVE);
            String fileCommand = Config.buildFileCommand(isEnableOperation);
            String[] suCommand = Config.buildSuCommand(fileCommand);
            
            showToast("执行命令: " + fileCommand);
            
            // 使用工具类执行su命令
            ProcessUtils.ProcessResult result = ProcessUtils.executeCommand(
                suCommand, 
                Config.COMMAND_TIMEOUT_MS
            );
            
            Log.d(TAG, "File operation result: " + result);
            showToast("命令执行结果: " + result.exitCode);
            
            boolean success = ProcessUtils.isFileOperationSuccess(result.exitCode, isEnableOperation);
            if (success) {
                showToast("文件操作成功");
            } else {
                showToast("文件操作失败");
            }
            
            return success;
            
        } catch (Exception e) {
            Log.e(TAG, "Error changing box service state", e);
            showToast("状态切换出错: " + e.getMessage());
            return false;
        }
    }

    /**
     * 显示Toast消息
     */
    private void showToast(String message) {
        try {
            // 使用Handler在主线程中显示Toast
            mainHandler.post(() -> {
                Toast.makeText(this, "SurfingTile: " + message, Toast.LENGTH_SHORT).show();
            });
        } catch (Exception e) {
            Log.e(TAG, "Error showing toast: " + e.getMessage());
        }
    }

    @Override
    public void onTileAdded() {
        super.onTileAdded();
        Log.i(TAG, "Surfing tile added to quick settings");
        showToast("Surfing磁贴已添加到快速设置");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        showToast("Tile服务已销毁");
        // 清理资源
        if (!executor.isShutdown()) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(Config.EXECUTOR_SHUTDOWN_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}
