package com.stars.util.backdoor.command;

public class CommandOption {
	
	private final String key;
	private final String val;
	
	public CommandOption(String key, String value) {
		this.key = key;
		this.val = value;
	}
    
    public String getKey() {
        return this.key;
    }
    
    public String getValue() {
        return this.val;
    }
    
    @Override
    public String toString() {
    	return this.key + "=" + this.val;
    }
}
