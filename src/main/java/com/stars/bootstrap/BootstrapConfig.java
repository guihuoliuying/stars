package com.stars.bootstrap;

import com.stars.util.XmlReadUtil;
import com.stars.util._XmlNode;
import io.netty.util.internal.StringUtil;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by jx on 2015/2/28.
 */
public class BootstrapConfig {

//    public static String MAPPING_PATH = "config/main/db_mapping.xml";    // 映射表配置路径
//    public static String XML_PATH = "./config/main/server.xml";    // 主服server连接配置
//    public static String START_PATH = "./config/server_start.properties"; //服务启动配置路劲
//    public static String COMMON_PATH = "./config/main/common_server.properties";//公共服配置
	
	public static String PATH = "./config/config.xml";

    public static final String MAIN = "mainServer"; // 主服
    public static final String FIGHT = "fightServer"; // 战斗服
    public static final String DBPROXY = "db"; // db服务
    public static final String COMMON = "common"; // 公共服
    public static final String ROUTE = "route"; //路由服
    public static final String CONNECTOR = "connector"; // 连接服
    public static final String CONFIG = "configer"; //配置服
    public static final String NETPROXY = "net";
    public static final String LOGIN = "login";// 登录服
    public static final String LOOTTREASURE = "loottreasure";//野外夺宝
    public static final String RMCHAT = "rmchat";//跨服聊天
	public static final String MULTI = "multi";
	public static final String FIGHTMANAGER = "fightmanager";//战斗管理服
	public static final String FIGHTMANAGER1 = "fightmanager1";//战斗管理服备
	public static final String PAYSERVER = "payserver";
	public static final String PAYSERVER1 = "payserver1";
	public static final String FAMILYWAR = "familywar";
	public static final String SKYRANK = "skyrank";// 天梯排行榜
	public static final String DAILY5V5 = "daily5v5";//日常5v5
	public static final String CAMP = "camp";//阵营

//    private Properties props;
    private String server;
    private ConcurrentHashMap<String, Properties> props;
    
    
    private ConcurrentHashMap<String, Properties>pubProps = new ConcurrentHashMap<String, Properties>();
    
    //加载服务启动配置
    public BootstrapConfig(String server){
        this.server = server;
        initServerConfig();
    }

    public String getServerName() {
    	return props.get(server).getProperty("serverName");
    }
    
    public int getServerId(){
    	return Integer.parseInt(props.get(server).getProperty("serverId"));
    }

    public String getBusinessName() {
        return props.get(server).getProperty("business-start");
    }
    
    public int getServerPort(){
    	return Integer.parseInt(props.get(server).getProperty("serverPort"));
    }
    
    public String getServerIp(){
    	return props.get(server).getProperty("serverIp");
    }
    
    public Properties getServerProp(){
    	return props.get(server);
    }

	public int getGmPort() {
		String port = props.get(server).getProperty("gmPort");
		if (StringUtil.isNullOrEmpty(port))
			return 0;
		return Integer.parseInt(port);
	}
	
	public int getHttpPort(){
		String port = props.get(server).getProperty("httpPort");
		if (StringUtil.isNullOrEmpty(port))
			return 0;
		return Integer.parseInt(port);
	}


	public void initServerConfig() {
    	if (props == null) {
    		props = new ConcurrentHashMap<String, Properties>();
		}
    	List<com.stars.util._XmlNode>serverList = XmlReadUtil.read(PATH);
    	com.stars.util._XmlNode data = (com.stars.util._XmlNode) serverList.get(0);
		List<com.stars.util._XmlNode> dataList = (List<com.stars.util._XmlNode>) data.getValue();
		for (com.stars.util._XmlNode node:dataList) {
			Properties prop = new Properties();
			List<com.stars.util._XmlNode> children = (List<com.stars.util._XmlNode>) node.getValue();
			for (_XmlNode child:children) {
				prop.put(child.getKey(), child.getValue());
//				System.out.println(child.getKey()+","+child.getValue());
			}
			props.put(node.getKey(), prop);
		}
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public ConcurrentHashMap<String, Properties> getProps() {
		return props;
	}

	public void setProps(ConcurrentHashMap<String, Properties> props) {
		this.props = props;
	}
	
	public String getServerType(){
		return props.get(server).getProperty("serverType");
	}

	public ConcurrentHashMap<String, Properties> getPubProps() {
		return pubProps;
	}
}
