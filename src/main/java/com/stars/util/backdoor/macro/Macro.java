package com.stars.util.backdoor.macro;

import java.util.List;

public abstract class Macro {
	
	private String name;
	
	protected Macro(String name) {
		this.name = name;
	}
	
	public String getName() { return this.name; }
	
	public final String expand(List<String> paramList) {
		return call(paramList);
	}
	
	protected abstract String call(List<String> paramList);
	
}
