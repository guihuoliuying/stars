package com.stars.multiserver.familywar.data;

import com.stars.db.DBUtil;
import com.stars.db.DbRow;
import com.stars.db.SqlUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by chenkeyu on 2017-04-26 20:46
 */
public class FamilyWarFixture extends DbRow {
    private int warType;
    private int serverId;
    private Map<Integer, Long> fixtureFamily;//fixture --familyId

    public FamilyWarFixture() {
    }

    public FamilyWarFixture(int warType, int serverId) {
        this.warType = warType;
        this.serverId = serverId;
        this.fixtureFamily = new HashMap<>();
    }

    public int getWarType() {
        return warType;
    }

    public void setWarType(int warType) {
        this.warType = warType;
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public String getFixtureFamily() {
        StringBuffer buffer = new StringBuffer();
        for (Map.Entry<Integer, Long> entry : fixtureFamily.entrySet()) {
            if (buffer.length() > 0) {
                buffer.append("|");
            }
            buffer.append(entry.getKey()).append("=").append(entry.getValue());
        }
        return buffer.toString();
    }

    public void setFixtureFamily(String fixtureFamilyStr) {

    }

    public Map<Integer, Long> getFixtureFamilyMap() {
        return fixtureFamily;
    }

    public void setFixtureFamilyMap(String fixtureFamilyStr) {
        if (fixtureFamilyStr == null || fixtureFamilyStr.equals("")) {
            return;
        }
        String[] fixtureFamilyTmpStr = fixtureFamilyStr.split("\\|");
        for (String fixtureFamilyS : fixtureFamilyTmpStr) {
            String[] tmp = fixtureFamilyS.split("\\=");
            fixtureFamily.put(Integer.parseInt(tmp[0]), Long.parseLong(tmp[1]));
        }
    }

    public void updateFixture(int fix, long familyId) {
        fixtureFamily.put(fix, familyId);
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_COMMON, "familywarfixture", " `wartype`=" + warType + " and `serverid`=" + serverId);
    }

    @Override
    public String getDeleteSql() {
        return "delete from familywarfixture where `wartype`= " + warType + " and `serverid`=" + serverId;
    }
}
