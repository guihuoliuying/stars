package com.stars.util.backdoor.macro;

import com.stars.util.backdoor.macro.impl.date.DateAddMacro;
import com.stars.util.backdoor.macro.impl.date.DateFormatMacro;
import com.stars.util.backdoor.macro.impl.date.DateNowMacro;
import com.stars.util.backdoor.macro.impl.string.StringConcatMacro;
import com.stars.util.backdoor.macro.impl.string.StringReplaceMacro;
import com.stars.util.backdoor.macro.impl.string.StringSubstringMacro;

import java.util.HashMap;
import java.util.Map;

public class MacroFactory {

	private static Map<String, Macro> macros = new HashMap<String, Macro>();
	
	static {
		register(new DateFormatMacro());
		register(new DateAddMacro());
		register(new DateNowMacro());
		register(new StringConcatMacro());
		register(new StringReplaceMacro());
		register(new StringSubstringMacro());
	}
	
	public static void register(Macro macro) {
		macros.put(macro.getName(), macro);
	}
	
	public static Macro getMacro(String macroName) {
		return macros.get(macroName);
	}
	

}
