package com.yadli.surfingtile;

/**
 * 应用配置管理类
 * 集中管理所有配置参数，便于维护和修改
 */
public final class Config {
    
    // 服务相关配置
    public static final String SERVICE_URL = "http://localhost:9090";
    public static final String DISABLE_FILE_PATH = "/data/adb/modules/box4/disable";
    
    // 超时配置（毫秒）
    public static final int CONNECT_TIMEOUT_MS = 1000;
    public static final int READ_TIMEOUT_MS = 1000;
    public static final int COMMAND_TIMEOUT_MS = 5000;
    public static final int EXECUTOR_SHUTDOWN_TIMEOUT_MS = 1000;
    
    // 日志标签
    public static final String LOG_TAG = "SurfingTileService";
    
    // curl命令相关
    public static final String CURL_COMMAND = "curl";
    public static final String CURL_SILENT_FLAG = "-s";
    public static final String CURL_CONNECT_TIMEOUT_FLAG = "--connect-timeout";
    public static final String CURL_MAX_TIME_FLAG = "-m";
    
    // 系统命令
    public static final String SU_COMMAND = "su";
    public static final String SU_EXEC_FLAG = "-c";
    
    // 文件操作命令
    public static final String REMOVE_FILE_CMD = "rm -f";
    public static final String CREATE_FILE_CMD = "touch";
    
    // 私有构造函数防止实例化
    private Config() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    /**
     * 构建curl命令参数
     */
    public static String[] buildCurlCommand() {
        return new String[]{
            CURL_COMMAND,
            CURL_SILENT_FLAG,
            CURL_CONNECT_TIMEOUT_FLAG,
            String.valueOf(CONNECT_TIMEOUT_MS / 1000),
            CURL_MAX_TIME_FLAG,
            String.valueOf(READ_TIMEOUT_MS / 1000),
            SERVICE_URL
        };
    }
    
    /**
     * 构建su命令参数
     */
    public static String[] buildSuCommand(String command) {
        return new String[]{SU_COMMAND, SU_EXEC_FLAG, command};
    }
    
    /**
     * 构建文件操作命令
     */
    public static String buildFileCommand(boolean enable) {
        if (enable) {
            return REMOVE_FILE_CMD + " " + DISABLE_FILE_PATH;
        } else {
            return CREATE_FILE_CMD + " " + DISABLE_FILE_PATH;
        }
    }
} 