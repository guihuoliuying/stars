package com.stars.db;

import java.io.Serializable;

public abstract class DbRow implements Serializable{
    //===================数据状态====================
    public static byte DB_SAVE = 0;    //保存
    public static byte DB_INSERT = 1;  //插入
    public static byte DB_UPDATE = 2;  //更新
    public static byte DB_DELETE = 3;  //删除
    public static byte DB_NONE = 4;    //没有

	//数据状态默认为保存状态
    private byte dbStatus = DB_SAVE;

    private boolean change;// todo:用于业务层根据数据是否修改过做出下发或其他处理

    public boolean isChange() {
        return change;
    }

    public void setChange(boolean chg) {
        this.change= chg;
    }

    // 是否打印sql异常log,每次状态重置后置为true,避免异常log过多
    public boolean printErrorLog = Boolean.TRUE;

    /**
     * 从数据库中读取数据的时候设置数据状态为保存状态
     */
    public void saveStatus() {
        dbStatus = DB_SAVE;
        change = false;
        resetPrintStatus();
    }

    /**
     * 设置数据为保存状态
     */
    public void setSaveStatus() {
        dbStatus = DB_SAVE;
        resetPrintStatus();
    }

    /**
     * 设置数据状态为更新状态
     */
    public void setUpdateStatus() {
        change = true;
        if (dbStatus == DB_SAVE) {
            dbStatus = DB_UPDATE;
        }
        resetPrintStatus();
    }

    /**
     * 设置数据状态为插入状态
     */
    public void setInsertStatus() {
        change = true;
        dbStatus = DB_INSERT;
        resetPrintStatus();
    }

    public void setDbStatus(byte status){
        dbStatus = status;
    }


    /**
     * 设置数据状态为删除状态
     */
    public void setDeleteStatus() {
        if( dbStatus == DB_INSERT ){
        	change = false;
        }else{
        	change = true;
        	dbStatus = DB_DELETE;
        }
        resetPrintStatus();
    }


    /**
     * 判断数据状态
     *
     * @return
     */
    public boolean isSave() {
        return dbStatus == DB_SAVE;
    }

    public boolean isUpdate() {
        return dbStatus == DB_UPDATE;
    }

    public boolean isInsert() {
        return dbStatus == DB_INSERT;
    }

    public boolean isDelete() {
        return dbStatus == DB_DELETE;
    }

    public byte getDbStatus(){
        return this.dbStatus;
    }

    /**
     * 获得更新/插入sql
     *
     * @return
     */
    public abstract String getChangeSql();

    /**
     * 获得删除sql
     *
     * @return
     */
    public abstract String getDeleteSql();

    private void resetPrintStatus() {
        this.printErrorLog = Boolean.TRUE;
    }
}
