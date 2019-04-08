package com.stars.multiserver.familywar;

import com.stars.util.ranklist.RankObj;

/**
 * Created by zhaowenshuo on 2016/12/19.
 */
public class FamilyWarPointsRankObj extends RankObj {

    private int serverId;
    private String roleName;
    private String familyName;

    public FamilyWarPointsRankObj(String key, long points) {
        super(key, points);
    }

    public FamilyWarPointsRankObj(String key, long points, int serverId, String roleName, String familyName) {
        super(key, points);
        this.serverId = serverId;
        this.roleName = roleName;
        this.familyName = familyName;
    }

    @Override
    public int compareTo(RankObj other) {
        int ret = super.compareTo(other);
        if (ret == 0 && other instanceof FamilyWarPointsRankObj) {
            ret = this.roleName.compareTo(((FamilyWarPointsRankObj) other).roleName);
        }
        return ret;
    }

    public int getServerId() {
        return serverId;
    }

    public String getRoleName() {
        return roleName;
    }
    
    public String getFamilyName() {
    	return familyName;
    }
    
    @Override
    public String toString() {
    	return roleName + "-" + familyName + "-" + getPoints();
    }
}
