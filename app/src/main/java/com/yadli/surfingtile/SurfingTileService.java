package com.yadli.surfingtile;

import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.util.Log;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SurfingTileService extends TileService {

    private static final String TAG = Config.LOG_TAG;
    
    // 使用单线程执行器避免并发问题
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile boolean isProcessing = false;

    @Override
    public void onStartListening() {
        super.onStartListening();
        updateTileStateAsync();
    }

    @Override
    public void onClick() {
        super.onClick();
        
        // 防止重复点击
        if (isProcessing) {
            Log.d(TAG, "Operation in progress, ignoring click");
            return;
        }
        
        isProcessing = true;
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
            int currentState = getBoxState();
            int newState = determineNewState(currentState);
            
            boolean success = setBoxState(newState);
            if (success) {
                updateTileUI(newState);
                logStateChange(newState);
            } else {
                Log.e(TAG, "Failed to change box service state");
                // 恢复原状态
                updateTileUI(currentState);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling tile click", e);
            // 出错时设置为不可用状态
            updateTileUI(Tile.STATE_UNAVAILABLE);
        }
    }

    private int determineNewState(int currentState) {
        switch(currentState) {
            case Tile.STATE_ACTIVE:
                return Tile.STATE_INACTIVE;
            case Tile.STATE_INACTIVE:
                return Tile.STATE_ACTIVE;
            case Tile.STATE_UNAVAILABLE:
                Log.w(TAG, "Service unavailable, attempting to start");
                return Tile.STATE_ACTIVE;
            default:
                Log.w(TAG, "Unknown state: " + currentState + ", defaulting to active");
                return Tile.STATE_ACTIVE;
        }
    }

    private void updateTileStateAsync() {
        CompletableFuture.runAsync(() -> {
            try {
                int state = getBoxState();
                updateTileUI(state);
            } catch (Exception e) {
                Log.e(TAG, "Error updating tile state", e);
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
        } else if (state == Tile.STATE_INACTIVE) {
            Log.i(TAG, "Box service stopped successfully");
        }
    }

    private int getBoxState() {
        try {
            // 使用工具类执行curl命令
            ProcessUtils.ProcessResult result = ProcessUtils.executeCommand(
                Config.buildCurlCommand(), 
                Config.CONNECT_TIMEOUT_MS + Config.READ_TIMEOUT_MS
            );
            
            Log.d(TAG, "Curl result: " + result);
            
            if (ProcessUtils.isCurlSuccess(result.exitCode)) {
                Log.d(TAG, "Service appears to be running");
                return Tile.STATE_ACTIVE;
            } else {
                Log.d(TAG, "Service appears to be stopped");
                return Tile.STATE_INACTIVE;
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error checking service status: " + e.getMessage());
            return Tile.STATE_INACTIVE;
        }
    }

    private boolean setBoxState(int state) {
        try {
            boolean isEnableOperation = (state == Tile.STATE_ACTIVE);
            String fileCommand = Config.buildFileCommand(isEnableOperation);
            String[] suCommand = Config.buildSuCommand(fileCommand);
            
            // 使用工具类执行su命令
            ProcessUtils.ProcessResult result = ProcessUtils.executeCommand(
                suCommand, 
                Config.COMMAND_TIMEOUT_MS
            );
            
            Log.d(TAG, "File operation result: " + result);
            
            return ProcessUtils.isFileOperationSuccess(result.exitCode, isEnableOperation);
            
        } catch (Exception e) {
            Log.e(TAG, "Error changing box service state", e);
            return false;
        }
    }

    @Override
    public void onTileAdded() {
        super.onTileAdded();
        Log.i(TAG, "Surfing tile added to quick settings");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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
