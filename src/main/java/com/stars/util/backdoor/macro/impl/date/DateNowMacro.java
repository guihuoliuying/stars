package com.stars.util.backdoor.macro.impl.date;


import com.stars.util.backdoor.macro.Macro;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class DateNowMacro extends Macro {

	public DateNowMacro() {
		super("#date.now");
	}

	@Override
	public String call(List<String> paramList) {
		return new SimpleDateFormat("yyyyMMddHHmm").format(new Date());
	}

}
