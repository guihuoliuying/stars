package com.stars.modules.marry.summary;

import com.stars.services.summary.AbstractSummaryComponent;
import com.stars.services.summary.SummaryConst;
import com.stars.util.MapUtil;
import com.stars.util.StringUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhanghaizhen on 2017/7/3.
 */
public class MarrySummaryComponentImpl extends AbstractSummaryComponent implements MarrySummaryComponent {

    private long coupleRoleId;
    private byte marryState;

    public MarrySummaryComponentImpl(){}

    public MarrySummaryComponentImpl(long coupleRoleId,byte marryState){
        this.marryState = marryState;
        this.coupleRoleId = coupleRoleId;
    }
    @Override
    public String getName() {
        return SummaryConst.C_MARRY;
    }

    @Override
    public int getLatestVersion() {
        return 0;
    }

    @Override
    public void fromString(int version, String str) {
        Map<String, String> map = StringUtil.toMap(str, String.class, String.class, '=', ',');
        this.marryState = com.stars.util.MapUtil.getByte(map, "marryState", (byte)0);
        this.coupleRoleId = com.stars.util.MapUtil.getLong(map, "coupleRoleId", 0L);
    }

    @Override
    public String makeString() {
        Map<String, String> map = new HashMap<>();
        com.stars.util.MapUtil.setByte(map, "marryState", marryState);
        MapUtil.setLong(map, "coupleRoleId", coupleRoleId);
        return StringUtil.makeString2(map, '=', ',');
    }

    @Override
    public long getCoupleRoleId() {
        return coupleRoleId;
    }

    @Override
    public byte getMarryState() {
        return marryState;
    }

    @Override
    public void setCoupleRoleId(long coupleRoleId) {
        this.coupleRoleId = coupleRoleId;
    }

    @Override
    public void setMarryState(byte marryState) {
        this.marryState = marryState;
    }
}
