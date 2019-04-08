package com.stars.util.backdoor.macro.impl.date;


import com.stars.util.backdoor.macro.Macro;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

public class DateFormatMacro extends Macro {

	public DateFormatMacro() {
		super("#date.format");
	}

	@Override
	public String call(List<String> paramList) {
		if (3 != paramList.size()) {
			throw new IllegalArgumentException();
		}
		String date = paramList.get(0);
		String inputFormat = paramList.get(1);
		String outputFormat = paramList.get(2);
		try {
			return new SimpleDateFormat(outputFormat)
					.format(new SimpleDateFormat(inputFormat).parse(date));
		} catch (ParseException e) {
			return "";
		}
	}

}
