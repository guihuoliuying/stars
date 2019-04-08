package com.stars.util.backdoor.variables;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

public class CVariables {
	
	public static final CVariables VARS_SYSTIME_YMDHM = new CSystemTimeVariables("${SYSTIME_YMDHM}", null, "yyyyMMddHHmm");
	public static final CVariables VARS_SYSTIME_YMDH = new CSystemTimeVariables("${SYSTIME_YMDH}", null, "yyyyMMddHH");
	public static final CVariables VARS_SYSTIME_YMD = new CSystemTimeVariables("${SYSTIME_YMD}", null, "yyyyMMdd");
	public static final CVariables VARS_SYSTIME_YM = new CSystemTimeVariables("${SYSTIME_YM}", null, "yyyyMM");
	public static final CVariables VARS_SYSTIME_Y = new CSystemTimeVariables("${SYSTIME_Y}", null, "yyyy");
	public static final CVariables VARS_SYSTIME_HM = new CSystemTimeVariables("${SYSTIME_HM}", null, "HHmm");
	public static final CVariables VARS_SYSECHO = new CVariables("${SYSECHO}", "true");
	
	public static final CVariables VARS_SYSPATH = new CVariables("${SYSPATH}") {
		@Override
		public String getValue() {
			String path = "";
			try {
				path = new File("").getAbsolutePath();
			} catch (Exception e) {
				
			}
			return path;
		}
	};
	
	private String name;
	private String value;
	
	public CVariables(String name) {
		this(name, null);
	}
	
	public CVariables(String name, String value) {
		this.name = name;
		this.value = value;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getValue() {
		if (null == value) return "";
		String res = value;
		Iterator<CVariables> itor = com.stars.util.backdoor.variables.CVariablesTable.varsIterator();
		while (itor.hasNext()) {
			CVariables vars = itor.next();
			if (vars == this) continue;
			if (res.contains(vars.getName())) {
				res = res.replace(vars.getName(), vars.getValue());
			}
		}
		return res;
	}
	
	public static void main(String[] args) throws IOException {
		CVariables vars =  new CVariables("${doubleDate}", "${SYS_TIME_yyyyMMdd}:${SYS_TIME}");
		CVariablesTable.register(vars);
		vars = new CVariables("${squareDate}", "${doubleDate}:${SYS_TIME}");
		System.out.println(vars.getValue());
//		System.out.println(file.getCanonicalPath());
//		System.out.println(file.getAbsolutePath());
		System.out.println(VARS_SYSPATH.getValue());
	}
	
	private static class CSystemTimeVariables extends CVariables {
		private ThreadLocal<SimpleDateFormat> sdf;
		public CSystemTimeVariables(String name, String value, final String pattern) {
			super(name);
			this.sdf = new ThreadLocal<SimpleDateFormat>() {
				@Override
				protected SimpleDateFormat initialValue() {
					return new SimpleDateFormat(pattern);
				}
			};
		}
		@Override
		public String getValue() {
			return sdf.get().format(new Date());
		}
	}
}
