package com.stars.network.server.config;

public class ConfigSwitch {
	public static boolean os_windows = false;
	
	static {
		if (System.getProperty("os.name").equals("Linux")) {
			os_windows = false;
		}
	}
}
