package com.stars.modules.scene.userdata;

import com.stars.core.db.DBUtil;
import com.stars.core.db.DbRow;
import com.stars.core.db.SqlUtil;
import com.stars.util.StringUtil;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by liuyuheng on 2017/1/3.
 */
public class RoleDrama extends DbRow {
    private long roleId;
    private String type;// 剧情类型
    private String paramId;// 对应功能Id
    private String dramaId;// 已播放剧情Id

    /* 内存数据 */
    private Set<String> dramaSet;

    public RoleDrama() {
    }

    public RoleDrama(long roleId, String type, String paramId) {
        this.roleId = roleId;
        this.type = type;
        this.paramId = paramId;
        this.dramaId = "";
        this.dramaSet = new HashSet<>();
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "roledrama", " `roleid`=" + roleId + " and `type`='" + type
                + "' and `paramid`='" + paramId + "'");
    }

    @Override
    public String getDeleteSql() {
        return SqlUtil.getDeleteSql("roledrama", " `roleid`=" + roleId + " and `type`='" + type
                + "' and `paramid`='" + paramId + "'");
    }

    public void addPlayedDrama(String addDramaId) {
        dramaSet.add(addDramaId);
        StringBuilder builder = new StringBuilder("");
        for (String dramaId : dramaSet) {
            if (builder.length() > 0) {
                builder.append(",");
            }
            builder.append(dramaId);
        }
        this.dramaId = builder.toString();
    }

    public Set<String> getDramaSet() {
        return dramaSet;
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getParamId() {
        return paramId;
    }

    public void setParamId(String paramId) {
        this.paramId = paramId;
    }

    public String getDramaId() {
        return dramaId;
    }

    public void setDramaId(String dramaId) throws Exception {
        this.dramaId = dramaId;
        this.dramaSet = new HashSet<>();
        if (StringUtil.isEmpty(dramaId)) {
            return;
        }
        dramaSet.addAll(StringUtil.toArrayList(dramaId, String.class, ','));
    }
}
