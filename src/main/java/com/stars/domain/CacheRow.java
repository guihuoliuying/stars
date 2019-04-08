package com.stars.domain;


/**
 * Created by zhaowenshuo on 2015/1/9.
 */
public abstract class CacheRow {
	
	/** insert */
    public static final byte STATE_NEW = 1;
    /** do nothing */
    public static final byte STATE_UNCHANGE = 2;
    /** update */
    public static final byte STATE_CHANGED = 3;
    
    /** 数据状态(插入, 更新) */
    private byte state = STATE_UNCHANGE;
    /** 数据库每行数据版本号*/
    private long rowVersion;
    /** 业务层数据保存控制版本号 */
    private transient int version;
    /** 实体缓存Table */
    private transient CacheTable table;
    
    
    public CacheRow(CacheTable table) {
    	this.table = table;
    }
    
    
    private void setState(byte state) {
        switch (state) {
            case STATE_NEW:
            case STATE_UNCHANGE:
            case STATE_CHANGED:
                this.state = state;
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    private byte getState() {
        return this.state;
    }
    
	private void setVersion() {
		this.version = table.nextVersion();
	}
    
// 	-- 以下setNew()、setUnchange()、setChanged()只在table对象不为null的情况下有效 --
    public void setNew() {
    	if (table != null) {
    		setState(STATE_NEW);
    		setVersion();
		}
    }

    public void setUnchange() {
    	if (table != null) {
    		setState(STATE_UNCHANGE);
    		setVersion();
    	}
    }

    public void setChanged() {
    	if (table != null) {
    		if (state != STATE_NEW) {
    			setState(STATE_CHANGED);
    		}
    		setVersion();
    	}
    }
// 	---------------------------------------------------------------------
    
    
    public void forceChange() {
    	setState(STATE_CHANGED);
    }
    
    public void unChangeNoVersion() {
    	setState(STATE_UNCHANGE);
    }
    
    
    public boolean isNew() {
        return getState() == STATE_NEW;
    }

    public boolean isUnchange() {
        return  getState() == STATE_UNCHANGE;
    }

    public boolean isChanged() {
        return getState() == STATE_CHANGED;
    }

	public int getVersion() {
		return version;
	}

	public boolean isAlterState(int version) {
		return this.version <= version;
	}
	
	public void setTable(CacheTable table) {
		this.table = table;
	}
	
	// 数据库底层调用
	public void setRowVersion(long rowVersion) {
		this.rowVersion = rowVersion;
	}
	
	public long getRowVersion() {
		return rowVersion;
	}
	
	// 返回最大版本号
	public long updateRowVersion(long rowVersion) {
		// 理论上传过来的rowVersion大于等于实体自身rowVersion, 如果判断没进来则生成的rowVersion有问题, 上层调用会打印日志
		if (this.rowVersion < rowVersion) {	
			this.rowVersion = rowVersion;	
		}									
		return this.rowVersion;
	}
	
	/**
	 * 返回redis缓存的唯一key(业务层自己组装)
	 * 
	 * @return {@link String}
	 */
	public abstract String cacheKey();
	
}
