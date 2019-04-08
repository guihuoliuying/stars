package com.stars.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * 日志相关操作类
 * <p/>
 * 日志有很多不完善的地方，请大家帮忙改进
 * <p/>
 * 日志尽量不要自己写，最好调用该类生成日志的方法
 */
public class LogUtil {

    // 开关
    public static boolean needDebugLog = true;
    public static boolean needErrorLog = true;
    public static boolean needSqlLog = true;

	// 所有日志
    static Logger consoleLog;
    static Logger exceptionLog ;
    

	public static void errPrintln(String log) {
		System.err.println(log);
	}

	/**
	 * 输出info级别的日志
	 * @param log
	 */
	public static void info(String log) {
		consoleLog.info(log);
	}

	/**
	 * 格式化输出info级别的日志
	 * 
	 * @param log
	 * @param params
	 */
	public static void info(String log, Object... params) {
		consoleLog.info(log, params);
	}

	/**
	 * 输出debug级别的日志
	 * @param log
	 */
	public static void debug(String log) {
		if (needDebugLog) {
			LogUtil.consoleLog.debug(log);
		}
	}

	/**
	 * 格式化输出debug级别的日志
	 * @param log
	 * @param params
	 */
	public static void debug(String log, Object... params) {
		if (needDebugLog) {
			LogUtil.consoleLog.debug(log, params);
		}
	}

	/**
	 * 输出debug级别的日志，包括异常信息
	 * @param info
	 * @param e
	 */
	public static void debug(String info, Throwable e) {
		if (needDebugLog) {
			if (e == null) {
				consoleLog.debug(info);
			} else {
				consoleLog.debug(info, e);
			}
		}
	}

	/**
	 * 输出error级别的日志，包括异常信息
	 * @param info
	 * @param e
	 */
	public static void error(String errorMsg, Throwable e) {
		if (needErrorLog) {
			exceptionLog.error(errorMsg, e);
		}
	}

	public static void error(String errorMsg) {
		if (needErrorLog) {
			exceptionLog.error(errorMsg);
		}
	}

    public static void error(String pattern, Object... params) {
        if (needErrorLog) {
            exceptionLog.error(pattern, params);
        }
    }


	/**
	 * 初始化日志
	 */
	public static void init() {
//		try {
//			System.setProperty("server-log", logPath);
//			File configFile = new File("./config/log4j2.xml");
//			ConfigurationSource source = new ConfigurationSource(
//					new FileInputStream(configFile), configFile);
//		
//			Configurator.initialize(null, source);
//			InternalLoggerFactory.setDefaultFactory(new CommonsLoggerFactory());
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		
//		/SMR_Server/config/testWrite.xml
		
		consoleLog = LoggerFactory.getLogger("console");
		exceptionLog = LoggerFactory.getLogger("error");
	}

}
