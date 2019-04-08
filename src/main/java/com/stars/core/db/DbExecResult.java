package com.stars.core.db;

import java.util.LinkedList;
import java.util.List;

/**
 * sql执行结果
 * Created by liuyuheng on 2016/6/21.
 */
public class DbExecResult {
    private boolean isSuccess;// 是否成功
    private List<String> failedSqlList = new LinkedList<>();// 执行失败sql

    public DbExecResult(boolean isSuccess) {
        this.isSuccess = isSuccess;
    }

    public DbExecResult(boolean isSuccess, List<String> failedSqlList) {
        this.isSuccess = isSuccess;
        this.failedSqlList = failedSqlList;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public List<String> getFailedSqlList() {
        return failedSqlList;
    }
}
