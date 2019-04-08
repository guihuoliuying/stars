package com.stars.modules.buddy.packet;

import com.stars.modules.buddy.BuddyManager;
import com.stars.modules.buddy.BuddyPacketSet;
import com.stars.modules.buddy.pojo.BuddyGuardPo;
import com.stars.modules.buddy.prodata.BuddyGuard;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by huwenjun on 2017/8/31.
 */
public class ClientGuardPacket extends Packet {
    private short subType;
    public static final short SEND_BUDDY_GUARD_MAIN = 1;
    private Map<Integer, Map<Integer, BuddyGuardPo>> allBuddyGuardPoMap;
    private Set<Integer> openGroupIds;
    private boolean includeProductData;

    public ClientGuardPacket() {
    }

    public ClientGuardPacket(short subType) {
        this.subType = subType;
    }


    @Override
    public void readFromBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {

    }

    @Override
    public void execPacket() {

    }


    @Override
    public short getType() {
        return BuddyPacketSet.C_BUDDY_GUARD;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeShort(subType);
        switch (subType) {
            case SEND_BUDDY_GUARD_MAIN: {
                buff.writeInt(includeProductData ? 1 : 0);//是否包含产品数据:1:包含:0:不包含
                buff.writeInt(BuddyManager.buddyGuardGroupMap.size());
                for (Map.Entry<Integer, List<BuddyGuard>> entry : BuddyManager.buddyGuardGroupMap.entrySet()) {
                    List<BuddyGuard> buddyGuards = entry.getValue();
                    Integer groupId = entry.getKey();
                    buff.writeInt(groupId);
                    buff.writeString(BuddyManager.buddyGuardGroupIdNameMap.get(groupId));//组名称
                    if (openGroupIds.contains(groupId)) {
                        buff.writeInt(1);
                    } else {
                        buff.writeInt(0);
                    }
                    buff.writeInt(buddyGuards.size());
                    for (BuddyGuard buddyGuard : buddyGuards) {
                        if (includeProductData) {
                            buddyGuard.writeBuff(buff);
                        } else {
                            buff.writeInt(buddyGuard.getPosition());
                        }
                        BuddyGuardPo buddyGuardPo = allBuddyGuardPoMap.get(groupId).get(buddyGuard.getPosition());
                        /**
                         * 0表示伙伴未开放（敬请期待）
                         * 1表示已激活
                         * 2表示未激活
                         * 3表示角色条件不满足(不显示页签)
                         */
                        buff.writeInt(buddyGuardPo.getStatus());
                    }
                }
            }
            break;
        }
    }

    public short getSubType() {
        return subType;
    }

    public void setSubType(short subType) {
        this.subType = subType;
    }

    public Map<Integer, Map<Integer, BuddyGuardPo>> getAllBuddyGuardPoMap() {
        return allBuddyGuardPoMap;
    }

    public void setAllBuddyGuardPoMap(Map<Integer, Map<Integer, BuddyGuardPo>> allBuddyGuardPoMap) {
        this.allBuddyGuardPoMap = allBuddyGuardPoMap;
    }

    public Set<Integer> getOpenGroupIds() {
        return openGroupIds;
    }

    public void setOpenGroupIds(Set<Integer> openGroupIds) {
        this.openGroupIds = openGroupIds;
    }

    public boolean isIncludeProductData() {
        return includeProductData;
    }

    public void setIncludeProductData(boolean includeProductData) {
        this.includeProductData = includeProductData;
    }
}
