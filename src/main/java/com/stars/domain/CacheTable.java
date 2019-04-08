package com.stars.domain;

import com.stars.util.VersionBuilder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Created by zhaowenshuo on 2015/1/9.
 */
public abstract class CacheTable {

	/** 表标识, 用户对应roleId, 公共服对应公共业务唯一Id */
    private long key;
    /** delete sql */
    private List<String[]> removalList = new ArrayList<>();
    
    /** 版本号生成器 */
    private transient com.stars.util.VersionBuilder versionBuilder = null;
    
    
    public CacheTable(long key) {
        this.key = key;
    }

    final public long getKey() {
        return key;
    }

    final protected void addDelSql(String rowKey, String sql) {
    	String[] deleteInfos = { rowKey, sql };
        this.removalList.add(deleteInfos);
    }

    final public Iterator<String[]> delSqlIterator() {
        return removalList.iterator();
    }
    
    public List<String[]> getRemovalList() {
    	return removalList;
    }
    
    
    
    public List<com.stars.domain.CacheRow> changedRows() {
    	Iterator<? extends com.stars.domain.CacheRow> it = rowIterator();
		List<com.stars.domain.CacheRow> rows = new ArrayList<>();
		for ( ;it.hasNext(); ) {
			com.stars.domain.CacheRow cacheRow = it.next();
			if (cacheRow == null) {
				continue;
			}
			if (cacheRow.isNew() || cacheRow.isChanged()) {
				rows.add(cacheRow);
			}
		}
		return rows;
    }
    
    
    public CacheTable newTab() {
    	String tableName = getTableName();
    	CacheTable table = TableManager.newTable(tableName, key);
    	return table;
    }
    
    
    
    public CacheTable copyIfChanged() {
    	List<com.stars.domain.CacheRow> changeRows = changedRows();
    	if (changeRows.isEmpty() 
    			&& this.removalList.isEmpty()) {
			return null;
		}
		CacheTable table = newTab();
		table.removalList = this.removalList;
		table.versionBuilder = this.versionBuilder;
		table.loadFromCache(changeRows);
		return table;
    }
    
    
    
    public void setVersion(int version) {
    	lazyGet();
    	versionBuilder.resetVersion(version);
    }
    
    public int getVersion() {
    	lazyGet();
    	return versionBuilder.getCurVersion();
    }
    
    public int nextVersion() {
    	lazyGet();
    	return versionBuilder.build();
    }
	
	public void updateVersion(int oldVersion) {
		for (Iterator<? extends com.stars.domain.CacheRow> it = rowIterator(); it.hasNext();) {
			com.stars.domain.CacheRow cacheRow = it.next();
			if (cacheRow.isAlterState(oldVersion)) {
				cacheRow.setUnchange();	// 设置版本号
			}
		}
	}
	
	// 初始化调用
	public void rowInit() {
		Iterator<? extends com.stars.domain.CacheRow> it = rowIterator();
		if (it != null) {
			for ( ;it.hasNext(); ) {
				com.stars.domain.CacheRow cacheRow = it.next();
				if (cacheRow != null) {
					cacheRow.setTable(this);
				}
			}
		}
	}
	
	
	private void lazyGet() {
		if (versionBuilder == null) {
			versionBuilder = new VersionBuilder();
		}
	}
	
	
    /* refactor the api */

	public abstract String getTableName(); // 获取表名

    public abstract String getChangeSql(com.stars.domain.CacheRow row); // 获取插入或更新语句

    public abstract Iterator<? extends com.stars.domain.CacheRow> rowIterator(); // 行迭代器

    public abstract void load() throws Exception; // 加载数据
    
    @Deprecated
    public abstract boolean getAutoCommit(); // 获得SQL操作回滚后是否要单个SQL执行
    
    public abstract void loadFromCache(List<CacheRow> multiRows);	// 从缓存加载数据
    
}


