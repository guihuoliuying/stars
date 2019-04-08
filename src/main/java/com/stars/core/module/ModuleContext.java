package com.stars.core.module;

import com.stars.core.persist.DbRowDao;
import com.stars.core.recordmap.RecordMap;
import com.stars.core.db.DbRow;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by zhaowenshuo on 2016/7/20.
 */
public class ModuleContext {

    // todo: 状态
    private DbRowDao dao = new DbRowDao();
    // todo: 属性
    private RecordMap recordMap;
    //
    private Set<String> summaryComponentUpdateMarkSet = new HashSet<>();

    public ModuleContext(long roleId) {
        dao = new DbRowDao("role:" + Long.toString(roleId));
    }

    /* 数据保存相关 */
    public boolean isSavingSucceeded() {
        return dao.isSavingSucceeded();
    }

    public void insert(DbRow row) {
        dao.insert(row);
    }

    public void insert(DbRow... rows) {
        dao.insert(rows);
    }

    public void update(DbRow row) {
        dao.update(row);
    }

    public void update(DbRow... rows) {
        dao.update(rows);
    }

    public void delete(DbRow row) {
        dao.delete(row);
    }

    public void delete(DbRow... rows) {
        dao.delete(rows);
    }

    public void flush() {
        dao.flush();
    }

    public void flush(boolean removeOnFailure) {
        dao.flush(removeOnFailure);
    }

    public void flush(boolean execOneByOneOnFailure, boolean removeOnFailure) {
        dao.flush(execOneByOneOnFailure, removeOnFailure);
    }

    public List<String> getSqlList() {
        return dao.getSqlList();
    }

    /* recordMap相关 */
    public void recordMap(RecordMap recordMap) {
        this.recordMap = recordMap;
    }

    public RecordMap recordMap() {
        return recordMap;
    }

    /* 摘要数据相关 */
    public void markUpdatedSummaryComponent(String moduleName) {
        summaryComponentUpdateMarkSet.add(moduleName);
    }

    public Set<String> getSummaryComponentUpdateMarkSet() {
        return summaryComponentUpdateMarkSet;
    }
}
