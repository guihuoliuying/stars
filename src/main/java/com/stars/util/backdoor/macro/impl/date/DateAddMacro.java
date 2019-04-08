package com.stars.util.backdoor.macro.impl.date;


import com.stars.util.backdoor.macro.Macro;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class DateAddMacro extends Macro {

	public DateAddMacro() {
		super("#date.add");
	}

	@Override
	public String call(List<String> paramList) {
		if (3 != paramList.size()) {
			throw new IllegalArgumentException();
		}
		String dateStr = paramList.get(0);
		String typeStr = paramList.get(1);
		String stepStr = paramList.get(2);

		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
		try {
			calendar.setTime(sdf.parse(dateStr));
			int type = parseType(typeStr);
			int step = Integer.parseInt(stepStr);
			calendar.add(type, step);
			return sdf.format(calendar.getTime());
		} catch (ParseException e) {
			return null;
		}
		
	}
	
	private int parseType(String typeStr) {
		if ("y".equals(typeStr)) {
			return Calendar.YEAR;
		} else if ("M".equals(typeStr)) {
			return Calendar.MONTH;
		} else if ("d".equals(typeStr)) {
			return Calendar.DATE;
		} else if ("H".equals(typeStr)) {
			return Calendar.HOUR_OF_DAY;
		} else if ("m".equals(typeStr)) {
			return Calendar.MINUTE;
		} else {
			throw new IllegalArgumentException();
		}
	}

}
