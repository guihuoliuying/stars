package com.stars.util.log;


import org.apache.commons.io.IOUtils;
import org.apache.log4j.xml.DOMConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


/**
 * 底层日志
 * <p/>
 * 日志有很多不完善的地方，请大家帮忙改进
 * <p/>
 * 日志尽量不要自己写，最好调用该类生成日志的方法
 */
public class CoreLogger {

	// 日志开关可以通过配置控制, 在<Logger></Logger>层日志配置可配置日志级别, 
	// 配置后只能输出此级别以上的日志信息
	
    static Logger consoleLogger; // 基础logger
    static Logger errorLogger; // 错误logger

//    public static String getParams(String log,Object... params){
//    	if(params ==null)return log;
//    	StringBuilder strs = new StringBuilder();
//    	strs.append(log);
//    	for(Object o:params){
//    		if(o == null)continue;
//    		strs.append("|");
//    		strs.append(o.toString());
//    	}
//    	return strs.toString();
//    }
    
	public static void warn(String log) {
        consoleLogger.warn(log);
    }

    public static void warn(String log, Object... params) {
        if (params == null || params.length == 0) {
            consoleLogger.warn(log);
        } else {
            consoleLogger.warn(log,params);
        }
    }

    public static void warn(String log, Throwable cause) {
        consoleLogger.warn(log, cause);
    }

    public static void trace(String log) {
        consoleLogger.trace(log);
    }

    public static void trace(String log, Object... params) {
        if (params == null || params.length == 0) {
            consoleLogger.trace(log);
        } else {
            consoleLogger.trace(log,params);
        }
    }
    
	/**
	 * 输出info级别的日志
	 * 
	 * @param log
	 */
	public static void info(String log) {
		consoleLogger.info(log);
	}
	

	/**
	 * 格式化输出info级别的日志
	 * 
	 * @param log
	 * @param params
	 */
	public static void info(String log, Object ...params) {
		if (params == null || params.length == 0) {
			consoleLogger.info(log);
		} else {
			consoleLogger.info(log,params);
		}
	}

	
	/**
	 * 输出debug级别的日志
	 * 
	 * @param log
	 */
	public static void debug(String log) {
		consoleLogger.debug(log);
	}
	

	/**
	 * 格式化输出debug级别的日志
	 * 
	 * @param log
	 * @param params
	 */
	public static void debug(String log, Object ...params) {
		if (params == null || params.length == 0) {
			consoleLogger.debug(log);
		} else {
			consoleLogger.debug(log,params);
		}
	}
	
	
	/**
	 * 输出error级别的日志，包括异常信息
	 * @param info
	 * @param e
	 */
	public static void error(String log, Throwable e) {
		errorLogger.error(log, e);
	}
	
	
	/**
	 * 输出error级别的日志
	 * 
	 * @param info
	 * @param params
	 */
	public static void error(String log, Object ...params) {
		if (params == null || params.length == 0) {
			errorLogger.error(log);
		} else {
			errorLogger.error(log,params);
		}
	}
	
	
	
	
	/**
	 * 是否debug模式
	 * 
	 * @return {@link Boolean}
	 */
	public static boolean debugEnabled() {
		return consoleLogger.isDebugEnabled();
	}
	
	public static boolean init() {
		String log4jFile = "config/log4j_real.xml";
		// 1、生成临时的文件
		if (!genRealLog4jXml(log4jFile))
			return false;
		// 2、从临时文件里读取配置
		DOMConfigurator.configure(log4jFile);// 加载.xml文件
		// 3、删除临时的文件
		File file = new File(log4jFile);
		if (file.exists()) {
			file.delete();
		}
		consoleLogger = LoggerFactory.getLogger("console");
		errorLogger = LoggerFactory.getLogger("error");
		return true;
	}
    
	/**
	 * 初始化日志, 所有日志配置在底层初始化
	 */
	public static void init(String logPath) {

		if (logPath.length() > 0) {
			System.setProperty("sys:server-log", logPath);
		}
		
		boolean logRight  = init();
		if(!logRight){
			System.err.println("error ! 日志启动失败，请检查！");
			System.exit(0);
		}
//		if(true)return;
//		
//		// all logger async, use disruptor, just one thread
//		// System.setProperty(Constants.LOG4J_CONTEXT_SELECTOR,
//		// "org.apache.logging.log4j.core.async.AsyncLoggerContextSelector");
//
//	
//		
//		try {
////			if (logPath.length() > 0) {
//				System.setProperty("server-log", logPath);
////			}
//			File configFile = new File("./config/log4j2.xml");
////			ConfigurationSource source = new ConfigurationSource(
////					new FileInputStream(configFile), configFile);
////			Configurator.initialize(null, source);
//			InternalLoggerFactory.setDefaultFactory(new CommonsLoggerFactory());
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		consoleLogger = LoggerFactory.getLogger("console");
		errorLogger = LoggerFactory.getLogger("error");
	}
	
	public static boolean FLUME_LOG_SWITCH = true;
	
	private static boolean testFlumeClient() {
		Socket socket = null;
		try {
			socket = new Socket("127.0.0.1", 7777);
		} catch (IOException e) {
			return false;
		} finally {
			if (socket != null) {
				try {
					socket.close();
				} catch (Exception e2) {
					socket = null;
				}
			}
		}
		return true;
	}
	
	private static boolean genRealLog4jXml(String realLog4jFile) {
		List<String> config = new ArrayList<String>();
		List<String> temps = new ArrayList<String>();
		List<String> flumesNames = new ArrayList<String>();
		String name = null;
		String configFile ="config/log4j1.xml";
		boolean isFlumeConfig = false;
		try {
			if (!FLUME_LOG_SWITCH || (!System.getProperty("os.name").contains("Linux"))) {
				// 读取配置，去掉flume的配置
				List<String> lines = IOUtils.readLines(new FileInputStream(new File(configFile)), "UTF-8");
				// 遍历每行内容，去掉flume日志配置信息
				for (String line : lines) {
					if (line.contains("org.apache.flume") && line.contains("<appender")) {
						isFlumeConfig = true;
						name = line.split(" ")[1].replace("name=\"", "").replace("\"", "");
						flumesNames.add(name);
					}
					// 非flume日志配置信息加入列表
					if (!isFlumeConfig) {
						temps.add(line);
					}
					if (isFlumeConfig && line.contains("</appender>")) {
						isFlumeConfig = false;
					}
				}

				// 遍历列表，去掉flume日志引用
				for (String temp : temps) {
					for (String flumeName : flumesNames) {
						if (temp.contains("<appender-ref ref=\"" + flumeName + "\" />")) {
							temp = temp.replace("<appender-ref ref=\"" + flumeName + "\" />", "");
						}
					}
					config.add(temp);
				}
			} else {
				if (!testFlumeClient())
					return false;

				// 直接读取配置，不去掉flume配置
				List<String> lines = IOUtils.readLines(new FileInputStream(new File(configFile)), "UTF-8");
				config.addAll(lines);
			}
			try {
				// 已存在，先删除
				File outputFile = new File(realLog4jFile);
				if (outputFile.exists()) {
					outputFile.delete();
				}

				FileOutputStream outPutStream = new FileOutputStream(outputFile);
				IOUtils.writeLines(config, "\n", outPutStream, "UTF-8");
				IOUtils.closeQuietly(outPutStream); // 需要关闭
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}


}
