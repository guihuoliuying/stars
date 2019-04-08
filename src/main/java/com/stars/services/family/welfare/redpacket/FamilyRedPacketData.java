package com.stars.services.family.welfare.redpacket;

import com.stars.services.family.welfare.redpacket.userdata.FamilyRedPacketMemberPo;
import com.stars.services.family.welfare.redpacket.userdata.FamilyRedPacketRecordPo;

import java.util.List;
import java.util.Map;

/**
 * Created by zhaowenshuo on 2016/9/6.
 */
public class FamilyRedPacketData {

    private Map<Long, FamilyRedPacketMemberPo> memberRedPacketPoMap;
    private List<FamilyRedPacketRecordPo> recordPoList;

    private int memberCount;

    public FamilyRedPacketRecordPo getLastRecord() {
        if (recordPoList.size() == 0) {
            return null;
        }
        return recordPoList.get(recordPoList.size()-1);
    }

    public Map<Long, FamilyRedPacketMemberPo> getMemberRedPacketPoMap() {
        return memberRedPacketPoMap;
    }

    public void setMemberRedPacketPoMap(Map<Long, FamilyRedPacketMemberPo> memberRedPacketPoMap) {
        this.memberRedPacketPoMap = memberRedPacketPoMap;
    }

    public List<FamilyRedPacketRecordPo> getRecordPoList() {
        return recordPoList;
    }

    public void setRecordPoList(List<FamilyRedPacketRecordPo> recordPoList) {
        this.recordPoList = recordPoList;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }
}
