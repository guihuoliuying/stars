package com.stars.util.backdoor.macro.impl.string;


import com.stars.util.backdoor.macro.Macro;

import java.util.List;

public class StringConcatMacro extends Macro {

	public StringConcatMacro() {
		super("#str.concat");
	}

	@Override
	public String call(List<String> paramList) {
		if (2 != paramList.size()) {
			throw new IllegalArgumentException();
		}
		String str1 = paramList.get(0);
		String str2 = paramList.get(1);
		return str1.concat(str2);
	}

}
