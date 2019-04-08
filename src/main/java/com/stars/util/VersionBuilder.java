package com.stars.util;

import java.util.concurrent.atomic.AtomicInteger;


/**
 * 版本号生成器
 * 
 * @author huachp
 */
public class VersionBuilder {
	
	/** 版本号生成器 */
	private AtomicInteger versionBuilder = new AtomicInteger(0);
	
	public VersionBuilder() {}
	
	
	public VersionBuilder(int start) {
		this.versionBuilder.set(start);
	}
	
	public void resetVersion(int ver) {
		this.versionBuilder.set(ver);
	}
	
	public int build() {
		return versionBuilder.incrementAndGet();
	}
	
	public int getCurVersion() {
		return versionBuilder.get();
	}
	
}
