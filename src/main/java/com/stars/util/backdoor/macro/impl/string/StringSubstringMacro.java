package com.stars.util.backdoor.macro.impl.string;


import com.stars.util.backdoor.macro.Macro;

import java.util.List;

public class StringSubstringMacro extends Macro {

	public StringSubstringMacro() {
		super("#str.substring");
	}

	@Override
	protected String call(List<String> paramList) {
		String source = null;
		int beginIndex = -1;
		int endIndex = -1;
		switch (paramList.size()) {
		case 2:
			source = paramList.get(0);
			beginIndex = Integer.parseInt(paramList.get(1));
			return substring(source, beginIndex);
		case 3:
			source = paramList.get(0);
			beginIndex = Integer.parseInt(paramList.get(1));
			endIndex = Integer.parseInt(paramList.get(2));
			return substring(source, beginIndex, endIndex);
		default:
			throw new IllegalArgumentException(
					"Illegal argument number: " + paramList.size());
		}
	}
	
	private String substring(String source, int beginIndex) {
		return source.substring(beginIndex);
	}
	
	private String substring(String source, int beginIndex, int endIndex) {
		return source.substring(beginIndex, endIndex);
	}

}
