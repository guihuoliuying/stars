package com.stars.util.backdoor.macro.impl.string;


import com.stars.util.backdoor.macro.Macro;

import java.util.List;

public class StringReplaceMacro extends Macro {

	public StringReplaceMacro() {
		super("#str.replace");
	}

	@Override
	protected String call(List<String> paramList) {
		String string = null;
		String target = null;
		String replacement = null;
		switch (paramList.size()) {
		case 3:
			string = paramList.get(0);
			target = paramList.get(1);
			replacement = paramList.get(2);
			return string.replace(target, replacement);
		default:
			throw new IllegalArgumentException(
					"Illegal argument number: " + paramList.size());
		}
	}

}
