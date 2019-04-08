package com.stars.services.summary;

import com.stars.db.DBUtil;
import com.stars.db.DbRow;
import com.stars.db.SqlUtil;
import com.stars.util.LogUtil;

/**
 * Created by zhaowenshuo on 2016/9/21.
 */
public class SummaryComponentRow extends DbRow {

    private long roleId;
    private String componentName;
    private int version;
    private String componentValue;

    @Override
    public String getChangeSql() { // 估计要生成update on duplicate
//        return SqlUtil.getSql(this, DBUtil.DB_USER, "rolesummary", "`roleid`=" + roleId + " and `componentname`='" + componentName + "'");
        if (isInsert()) {
            try {
                String sql = SqlUtil.getInsertSql(DBUtil.DB_USER, this, "rolesummary") + " on duplicate key update `componentvalue`='" + componentValue + "', `version`=" + version;
                return sql;
            } catch (Throwable t) {
                LogUtil.error("摘要数据生成语句异常", t);
                return "";
            }
        } else {
            return SqlUtil.getSql(this, DBUtil.DB_USER, "rolesummary", "`roleid`=" + roleId + " and `componentname`='" + componentName + "'");
        }
    }

    @Override
    public String getDeleteSql() {
        return "delete from `rolesummary` where `roleid`=" + roleId + " and `componentname`='" + componentName + "'";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SummaryComponentRow that = (SummaryComponentRow) o;

        if (roleId != that.roleId) return false;
        if (!componentName.equals(that.componentName)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (roleId ^ (roleId >>> 32));
        result = 31 * result + componentName.hashCode();
        return result;
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getComponentValue() {
        return componentValue;
    }

    public void setComponentValue(String componentValue) {
        this.componentValue = componentValue;
    }
}
