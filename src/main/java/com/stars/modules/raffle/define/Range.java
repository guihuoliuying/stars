package com.stars.modules.raffle.define;

import com.stars.util.StringUtil;

/**
 * 
 * @author likang by 2017/4/21
 * 
 */

public class Range {
	// type(0:闭区间,1:开区间,2:左半开，3:右半开)
	public static final int CLOSE = 0;
	public static final int OPEN = 1;
	public static final int LEFT_OPEN = 2;
	public static final int RIGHT_OPEN = 3;

	// 实例区
	public final int type;
	public final int left;
	public final int right;

	public Range() {
		this(CLOSE, 0, 0);
	}

	public Range(int type, int left, int right) {
		this.type = type;
		this.left = left;
		this.right = right;
	}

	public Range(int left, int right) {
		this(CLOSE, left, right);
	}

	public boolean isHit(int value) {
		return compare(value) == 0;
	}

	public int compare(int value) {
		if (value < left) {
			return -1;
		}
		if (value > right) {
			return 1;
		}
		if (value == left &&  type == LEFT_OPEN) {
			return -1;
		}
		if (value == right && type == RIGHT_OPEN) {
			return 1;
		}
		return 0;
	}

	public String toString() {
		return String.format("%s+%s", left, right);
	}

	public static Range pase(String vipRangeStr) {
		if (StringUtil.isEmpty(vipRangeStr)) {
			return new Range();
		}
		String[] params = vipRangeStr.split("\\+");
		if (params.length != 2) {
			throw new RuntimeException("raffle.define.Range.pase() params is not match!");
		}
		int left = Integer.parseInt(params[0]);
		int right = Integer.parseInt(params[1]);
		return new Range(left, right);
	}

}
