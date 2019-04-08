package com.stars.util;

import com.stars.util.log.CoreLogger;
import com.sun.tools.attach.VirtualMachine;

import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;

public class HotUpdate {
	public static boolean updateClass(String className) {
    	Field fieldSysPath;
    	try {
    	    fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
    	    fieldSysPath.setAccessible(true);
    	    fieldSysPath.set(null, null);
    	    String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
    	    VirtualMachine vm = VirtualMachine.attach(pid);
    	    String [] classStr = className.split("\\.");
    	    String path = "./class/" + classStr[classStr.length-1] + ".class";
    	    System.err.println("path="+path+"|className="+className);
    	    vm.loadAgent("./agent/Agent_fat.jar", path);
    	    return true;
    	} catch (Exception e) {
    	   CoreLogger.error(e.getMessage(), e);
    	}
    	return false;
        }
}
