package test;

import java.io.*;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Log4jConfigTransform {

	public static String getNormalLogConfig(String logName, String filePattern, String fileName) {
		StringBuilder buffer = new StringBuilder();
		buffer.append(
				MessageFormat.format("<appender name=\"{0}\" class=\"org.apache.log4j.rolling.RollingFileAppender\">", logName))
				.append("\n	<rollingPolicy class=\"org.apache.log4j.rolling.TimeBasedRollingPolicy\">")
				.append(MessageFormat.format("\n		<param name=\"FileNamePattern\" value=\"{0}\" />", filePattern))
				.append(MessageFormat.format("\n		<param name=\"FileName\" value=\"{0}\" />", fileName)).append("\n	</rollingPolicy>")
				.append("\n	<layout class=\"org.apache.log4j.PatternLayout\">")
				.append("\n		<param name=\"ConversionPattern\" value=\"%d{yyyy-MM-dd HH:mm:ss}|%m%n\" />").append("\n	</layout>")
				.append("\n </appender> \n");
		return buffer.toString();
	}
	
//	<appender name="core_role" class="org.apache.log4j.rolling.RollingFileAppender">
//	<rollingPolicy class="org.apache.log4j.rolling.TimeBasedRollingPolicy">
//		<param name="FileNamePattern" value="log/backup_core_role/core_role.log.%d{yyyy-MM-dd-HH}" />
//	</rollingPolicy>
//	<layout class="org.apache.log4j.PatternLayout">
//		<param name="ConversionPattern" value="%d{yyyy-MM-dd HH:mm:ss}|%m%n" />
//	</layout>
//</appender>
//
//<appender name="core_role_flume" class="org.apache.flume.clients.log4jappender.Log4jAppender">
//	<param name="Hostname" value="127.0.0.1" />
//	<param name="Port" value="7777" />
//	<layout class="org.apache.log4j.PatternLayout">
//		<param name="ConversionPattern" value="%d{yyyy-MM-dd HH:mm:ss}|%m" />
//	</layout>
//</appender>
	

	public static String getFlumeLogConfig(String logName) {
		StringBuilder buffer = new StringBuilder();
		buffer.append(
				MessageFormat.format("<appender name=\"{0}\" class=\"org.apache.flume.clients.log4jappender.Log4jAppender\">",
						logName)).append("\n	<param name=\"Hostname\" value=\"127.0.0.1\" />")
				.append("\n	<param name=\"Port\" value=\"7777\" />").append("\n	<layout class=\"org.apache.log4j.PatternLayout\">")
				.append("\n	<param name=\"ConversionPattern\" value=\"%d{yyyy-MM-dd HH:mm:ss}|%m\" />").append(" \n	</layout>")
				.append("\n</appender> \n");
		return buffer.toString();
	}

	public static String getAppendStr(String logName, String apendName, String flumeName) {
		StringBuilder sb = new StringBuilder();
		if (flumeName != null) {
			sb.append(MessageFormat
					.format("<logger name=\"{0}\"> <level value=\"ALL\" /> <appender-ref ref=\"{1}\" /> <appender-ref ref=\"{2}\" /> </logger>",
							logName, apendName, flumeName));
		} else {
			sb.append(MessageFormat.format(
					"<logger name=\"{0}\"> <level value=\"ALL\" /> <appender-ref ref=\"{1}\" /> </logger>", logName, apendName));
		}
		// <logger name="tmpdebug"> <level value="ALL" /> <appender-ref
		// ref="tmpdebug" /> </logger>
		// <logger name="core_account"> <level value="ALL" /> <appender-ref
		// ref="core_account" /> <appender-ref ref="core_account_flume" />
		// </logger>
		return sb.toString();
	}

	public static final byte INIT = 0;
	public static final byte BEGIN = 1;
	public static final byte END = 2;

	public static class FlumeConfig {

		public static String beginFlag = "<logger name=";
		public static String endFlag = "</logger>";

		public byte status = INIT;

		public String name;
		public String AppenderRef;
		public String AppenderFlumeRef;

		// <logger name="core_account" level="info" includeLocation="true"
		// additivity="false">
		// <AppenderRef ref="core_account"/>
		// <AppenderRef ref="core_account_flume"/>
		// </logger>

		public boolean init(String line) {
			if (status == INIT) {
				if (line.contains(beginFlag)) {
					status = BEGIN;
				}
			} else if (status == BEGIN) {
				if (line.contains(endFlag)) {
					status = END;
				}
			}
			if (status == BEGIN) {
				String value = getAppenderRef(line);
				if (value != null) {
					if (value.contains("flume")) {
						AppenderFlumeRef = value;
					} else {
						AppenderRef = value;
					}
				}
			}
			if (status == END) {
				return true;
			}
			return false;
		}

		public String getAppenderRef(String line) {
			String key = "<AppenderRef ref=";
			if (line.contains(key)) {
				String[] sss = line.split("=");
				return sss[1].replace("\"", "").replace("/>", "").trim();
			}
			return null;
		}

	}

	public static class RollingFile {

		public static String beginFlag = "<RollingFile";
		public static String endFlag = "</RollingFile>";

		public byte status = INIT;

		public String name;
		public String fileName;
		public String filePattern;
		public String level;
		public String PatternLayout;

		public String AppenderRef;
		public String AppenderFlumeRef;

		public boolean init(String line) {
			if (status == INIT) {
				if (line.contains(beginFlag)) {
					status = BEGIN;
				}
			} else if (status == BEGIN) {
				if (line.contains(endFlag)) {
					status = END;
				}
			}
			if (status == BEGIN) {
				String value = getFilePattern(line);
				if (value != null) {
					filePattern = value;
				}
				value = getFileName(line);
				if (value != null) {
					fileName = value;
				}
				value = getName(line);
				if (value != null) {
					name = value;
				}
			}
			if (status == END) {
				return true;
			}
			return false;
		}

		public String getFilePattern(String line) {
			String key = "filePattern=";
			if (line.contains(key)) {
				String[] sss = line.split("=");
				return sss[1].replace("\"", "").replace(">", "").trim();
			}
			return null;
		}

		public String getName(String line) {
			String key = "<RollingFile name=";
			String space = " ";
			if (line.contains(key)) {
				String[] ss = line.split(space);
				for (String s : ss) {
					if (s.contains("name=")) {
						String[] sss = s.split("=");
						return sss[1].replace("\"", "").trim();
					}
				}
			}
			return null;
		}

		public String getFileName(String line) {
			String key = "fileName=";
			String space = " ";
			if (line.contains(key)) {
				String[] ss = line.split(space);
				for (String s : ss) {
					if (s.contains(key)) {
						String[] sss = s.split("=");
						try {
							return sss[1].replace("\"", "").trim();
						} catch (Throwable e) {
							e.printStackTrace();
						}
					}
				}
			}
			return null;
		}

		// <RollingFile name="core_stat_1"
		// fileName="log/${sys:server-log}/core_stat_1/core_stat_1.log"
		// immediateFlush="true" append="true"
		// filePattern="log/${sys:server-log}/core_stat_1/core_stat_1.log.%d{yyyy-MM-dd-HH}">
		// <Filters>
		// <ThresholdFilter level="INFO" onMatch="ACCEPT" onMismatch="DENY" />
		// </Filters>
		// <PatternLayout pattern="%msg%xEx%n" />
		// <Policies>
		// <TimeBasedTriggeringPolicy modulate = "false" />
		// </Policies>
		// </RollingFile>

		// <logger name="error" level="error" includeLocation="true"
		// additivity="false">
		// <AppenderRef ref="ErrorAsync"/>
		// </logger>
		//

	}

	public static void dealLineString(LinkedList<String> fileContentList) {
		List<RollingFile> rollList = new LinkedList<Log4jConfigTransform.RollingFile>();
		List<FlumeConfig> flCList = new LinkedList<Log4jConfigTransform.FlumeConfig>();
		Map<String, FlumeConfig> flumeLogMap = new HashMap<String, Log4jConfigTransform.FlumeConfig>();

		RollingFile nowR = new RollingFile();
		FlumeConfig nowF = new FlumeConfig();
		for (String line : fileContentList) {
			if (nowR.init(line)) {
				rollList.add(nowR);
				nowR = new RollingFile();
			}
			if (nowF.init(line)) {
				if (nowF.AppenderFlumeRef != null) {
					flumeLogMap.put(nowF.AppenderRef, nowF);
				}
				nowF = new FlumeConfig();
				flCList.add(nowF);
			}
		}

		BufferedWriter writer = null;
		File writeFile = new File("log/testWrite.xml");
		try {
			writer = new BufferedWriter(new FileWriter(writeFile));
//			for (String content : fileContentList) {
//				writer.write(content + "\n");
//			}
//			writer.flush();
//			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		for (RollingFile rf : rollList) {
			try {
				FlumeConfig fcf = flumeLogMap.get(rf.name);
				writer.write(getNormalLogConfig(rf.name, rf.filePattern, rf.fileName) + "\n");
				if (fcf != null) {
					writer.write(getFlumeLogConfig( fcf.AppenderFlumeRef) + "\n");
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		for (FlumeConfig fc : flCList) {
			try {
				writer.write(getAppendStr(fc.AppenderRef, fc.AppenderRef, fc.AppenderFlumeRef) + "\n");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			writer.flush();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void gmExec() {
		File files = new File("config/log4j2.xml");
		long now = System.currentTimeMillis();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(files));
		} catch (Exception e2) {
		}
		String lineStr = null;
		LinkedList<String> fileContentList = new LinkedList<>();
		try {
			while ((lineStr = reader.readLine()) != null) {
				fileContentList.add(lineStr);
			}
			reader.close();
		} catch (Exception e1) {
		}
		// BufferedWriter writer = null;
		// File writeFile = new File("log/testWrite.xml");
		// try {
		// writer =new BufferedWriter(new FileWriter(writeFile));
		// for(String content:fileContentList){
		// writer.write(content+"\n");
		// }
		// writer.flush();
		// writer.close();
		// } catch (IOException e) {
		// e.printStackTrace();
		// }

		dealLineString(fileContentList);

	}

	public static void main(String[] args) {
		gmExec();
	}
}
