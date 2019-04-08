package com.stars.util.backdoor.view;

import com.stars.util.backdoor.result.BackdoorCell;

import java.util.Arrays;

public class ViewLayout {

	private final int width;
	private final String titleName;
//	private final String separator;
	
	public ViewLayout(String titileName, int width) {
		this.width = width;
		this.titleName = titileName;
//		char[] cs = new char[width];
//		Arrays.fill(cs, '-');
//		this.separator = new String(cs);
	}
	
	public String getTitleName() {
		return getDisplayedValue(this.titleName, ' ');
	}
	
	public String getSeparator() {
		return getDisplayedValue("", '-');
	}
	
	private String getDisplayedValue(String value, char padding) {
		// make the value satisfy the layout
		if (value.length() > width) {
			value = value.substring(0, width);
		} else if (value.length() < width) {
			char[] cs = new char[width-value.length()];
			Arrays.fill(cs, padding);
			value = value + new String(cs);
		}
		return value;
	}
	
	public String getDisplayedValue(com.stars.util.backdoor.result.BackdoorCell cell) {
		String value = toNonEscape(cell.getValue());
		return getDisplayedValue(value, ' ');

	}

    public String toNonEscape(String value) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            if (value.charAt(i) == '\n') {
                sb.append("\\n");
            } else if (value.charAt(i) == '\r') {
                sb.append("\\r");
            } else {
                sb.append(value.charAt(i));
            }
        }
        return sb.toString();
    }
	
	public static void main(String[] args) {
		ViewLayout layout = new ViewLayout("FILE TYPE", 10);
		com.stars.util.backdoor.result.BackdoorCell cell = new BackdoorCell(0, 0, "interface");
		System.out.println(layout.getTitleName());
		System.out.println(layout.getSeparator());
		System.out.println(layout.getDisplayedValue(cell));
	}
	
}
