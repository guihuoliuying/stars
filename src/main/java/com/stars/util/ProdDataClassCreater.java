package com.stars.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProdDataClassCreater {

	static Map<String,String> attrMaps = new HashMap<>();
	static Map<String, String> getsetMap = new HashMap<>();

	static {
		attrMaps.put("` int(", "int");
		attrMaps.put("` smallint(", "short");
		attrMaps.put("` varchar(", "String");
		attrMaps.put("` tinyint(", "byte");
		attrMaps.put("` bigint(", "long");
	}

	static {
		getsetMap.put("int", "Int");
		getsetMap.put("byte", "Byte");
		getsetMap.put("short", "Short");
		getsetMap.put("long", "Long");
		getsetMap.put("String", "String");
	}

	private static String changeStrToCode(String lineStr){
		String type = null;
		//判断是否有字段关键字
		for(String key: attrMaps.keySet()){
			if(lineStr.contains(key)){
				type = attrMaps.get(key);
				break;
			}
		}
		if(type == null)return lineStr;
		//获取字段名字
		Matcher p = Pattern.compile("`.*`").matcher(lineStr);
		if(!p.find())return lineStr;
		String fieldName = p.group(0).replace("`", "");
		//获取注释
		String note ="";
		if(lineStr.contains("COMMENT")){
			p = Pattern.compile("COMMENT.*'").matcher(lineStr);
			if(p.find()){
				System.err.println(p.group(0));
				note = p.group(0).replaceAll("COMMENT", "");
			}
		}
		StringBuilder strBuild = new StringBuilder();
		strBuild.append("	private ").append(type).append(" ").append(fieldName).append(" ;//").append(note);
		return strBuild.toString();
	}

	private static String makeWriteBuffCode(String lineStr){
		String type = null;
		for(String key: getsetMap.keySet()) {
			if (lineStr.contains(key)) {
				type = getsetMap.get(key);
				break;
			}
		}
		if(type == null)return null;
		//获取字段名字
		String fieldName = lineStr.substring(0, lineStr.indexOf(";")).trim().split(" ")[2];
		StringBuilder strBuild = new StringBuilder();
		strBuild.append("buff.write").append(type).append("(").append(fieldName).append(");");
		return strBuild.toString();
	}

	static String[] classFiles = new String[]{
//			"E:/YH02_Server/src/com/yinhan/server/main/business/fightSync/proddata/ActionProdData.java",
//			"D:/yh02_server_workspace/YH02_Server/src/com/yinhan/server/main/business/fightSync/proddata/AIProdData.java",
//			"D:/yh02_server_workspace/YH02_Server/src/com/yinhan/server/main/business/fightSync/proddata/BehaviorProdData.java",
//			"D:/yh02_server_workspace/YH02_Server/src/com/yinhan/server/main/business/fightSync/proddata/BulletProdData.java",
//			"D:/yh02_server_workspace/YH02_Server/src/com/yinhan/server/main/business/fightSync/proddata/CountProdData.java",
//			"D:/yh02_server_workspace/YH02_Server/src/com/yinhan/server/main/business/fightSync/proddata/EntityProdData.java",
//			"D:/yh02_server_workspace/YH02_Server/src/com/yinhan/server/main/business/fightSync/proddata/SkillProdData.java",
//			"D:/yh02_server_workspace/YH02_Server/src/com/yinhan/server/main/business/fightSync/proddata/StageProdData.java",
//			"D:/yh02_server_workspace/YH02_Server/src/com/yinhan/server/main/business/fightSync/proddata/StateProdData.java",
			"E:/Simeiren_Server/trunk/src/com/yinhan/modules/callboss/prodata/CallBossVo.java",
//			"E:/Simeiren_Server/trunk/src/com/yinhan/services/rank/prodata/RankDisplayVo.java",
//			"E:/Simeiren_Server/trunk/src/com/yinhan/services/rank/prodata/RankAwardVo.java",
	};

	/**
	 * 根据sql生成属性
	 * @param args
	 */
	public static void main(String[] args){
		for(String classFilePath:classFiles){
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(classFilePath)));
				StringBuilder strBuild = new StringBuilder();
				String lineData = "";
				while((lineData = br.readLine()) != null){
					strBuild.append(changeStrToCode(lineData));
					strBuild.append("\r");
				}
				FileWriter fw = new FileWriter(classFilePath);
				fw.write(strBuild.toString());
				br.close();
				fw.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * 生成writeBuffer方法
	 */
//	public static void main(String[] args){
//		for(String classFilePath:classFiles){
//			try {
//				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(classFilePath)));
//				StringBuilder strBuild = new StringBuilder();
//				String lineData = "";
//				while((lineData = br.readLine()) != null){
//                    if(lineData.contains("()")){
//                        break;
//                    }
//					if(makeWriteBuffCode(lineData) != null){
//                        System.err.println(makeWriteBuffCode(lineData));
//                    }
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//
//	}
}
