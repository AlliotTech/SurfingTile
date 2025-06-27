package com.yadli.surfingtile;

import android.os.Build;
import android.util.Log;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

/**
 * 进程操作工具类
 * 提供进程执行、输出读取、超时控制等功能
 */
public final class ProcessUtils {
    
    private static final String TAG = Config.LOG_TAG + ".ProcessUtils";
    
    // 私有构造函数防止实例化
    private ProcessUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    /**
     * 进程执行结果
     */
    public static class ProcessResult {
        public final int exitCode;
        public final String output;
        public final String error;
        public final boolean timedOut;
        
        public ProcessResult(int exitCode, String output, String error, boolean timedOut) {
            this.exitCode = exitCode;
            this.output = output;
            this.error = error;
            this.timedOut = timedOut;
        }
        
        public boolean isSuccessful() {
            return !timedOut && exitCode == 0;
        }
        
        @Override
        public String toString() {
            return String.format("ProcessResult{exitCode=%d, output='%s', error='%s', timedOut=%s}", 
                exitCode, output, error, timedOut);
        }
    }
    
    /**
     * 执行命令并等待结果
     */
    public static ProcessResult executeCommand(String[] command, long timeoutMs) {
        try {
            Log.d(TAG, "Executing command: " + String.join(" ", command));
            
            Process process = Runtime.getRuntime().exec(command);
            ProcessResult result = waitForProcess(process, timeoutMs);
            
            Log.d(TAG, "Command result: " + result);
            return result;
            
        } catch (Exception e) {
            Log.e(TAG, "Error executing command", e);
            return new ProcessResult(-1, "", e.getMessage(), false);
        }
    }
    
    /**
     * 等待进程完成
     */
    private static ProcessResult waitForProcess(Process process, long timeoutMs) throws Exception {
        boolean completed = waitForProcessWithTimeout(process, timeoutMs);
        int exitCode = completed ? process.exitValue() : -1;
        
        ProcessOutput output = readProcessOutput(process);
        
        return new ProcessResult(exitCode, output.output.toString(), output.error.toString(), !completed);
    }
    
    /**
     * 带超时的进程等待
     */
    private static boolean waitForProcessWithTimeout(Process process, long timeoutMs) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                return process.waitFor(timeoutMs, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        } else {
            // 兼容旧版本Android
            try {
                process.waitFor();
                return true;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
    }
    
    /**
     * 读取进程输出
     */
    private static ProcessOutput readProcessOutput(Process process) throws Exception {
        ProcessOutput output = new ProcessOutput();
        
        // 读取标准输出
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.output.append(line).append('\n');
            }
        }
        
        // 读取错误输出
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.error.append(line).append('\n');
            }
        }
        
        return output;
    }
    
    /**
     * 内部类用于存储进程输出
     */
    private static class ProcessOutput {
        final StringBuilder output = new StringBuilder();
        final StringBuilder error = new StringBuilder();
    }
    
    /**
     * 检查curl退出码的含义
     */
    public static boolean isCurlSuccess(int exitCode) {
        // curl退出码说明：
        // 0: 成功连接
        // 28: 超时（但可能服务正在运行，端口开放）
        // 7: 连接失败（服务未运行）
        // 其他: 各种错误
        
        return exitCode == 0 || exitCode == 28;
    }
    
    /**
     * 检查文件操作是否成功
     */
    public static boolean isFileOperationSuccess(int exitCode, boolean isEnableOperation) {
        Log.d(TAG, "Checking file operation success: exitCode=" + exitCode + ", isEnable=" + isEnableOperation);
        
        if (isEnableOperation) {
            // 启用操作：删除disable文件
            // exitCode 0: 文件存在且删除成功
            // exitCode 1: 文件不存在（也是成功，因为目标就是让文件不存在）
            return exitCode == 0 || exitCode == 1;
        } else {
            // 禁用操作：创建disable文件
            // exitCode 0: 创建成功
            return exitCode == 0;
        }
    }
} 