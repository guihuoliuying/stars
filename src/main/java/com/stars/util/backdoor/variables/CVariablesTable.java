package com.stars.util.backdoor.variables;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class CVariablesTable {
	
	private static final Map<String, CVariables> table;
	
	static {
		table = new HashMap<String, CVariables>();
		register(CVariables.VARS_SYSTIME_YMDHM);
		register(CVariables.VARS_SYSTIME_YMDH);
		register(CVariables.VARS_SYSTIME_YMD);
		register(CVariables.VARS_SYSTIME_YM);
		register(CVariables.VARS_SYSTIME_Y);
		register(CVariables.VARS_SYSTIME_HM);
		register(CVariables.VARS_SYSPATH);
	}

	public static CVariables register(CVariables vars) {
		return table.put(vars.getName(), vars);
	}
	
	public static CVariables unregister(CVariables vars) {
		return table.remove(vars.getName());
	}
	
	public static Iterator<CVariables> varsIterator() {
		return table.values().iterator();
	}
	
	public static CVariables get(String name) {
		return table.get(name);
	}
	
	public static String evualate(String name) {
		return table.get(name).getValue();
	}
}
