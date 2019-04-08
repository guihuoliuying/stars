package com.stars.services.callboss.cache;

import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.services.callboss.CallBossConstant;

import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by liuyuheng on 2016/9/6.
 */
public class CallRecordCache {
    private int uniqueId;
    private int bossId;// bossid
    private long callRoleId;// 召唤者角色Id
    private String callRoleName;// 召唤者名称
    private byte selectRewardId;// 召唤者选择奖励组id
    private long callTime;// 召唤时间戳
    private TreeSet<RoleDamageCache> treeSet;// 排序容器(有限)
    private Map<Long, RoleDamageCache> roleRankMap;// 角色伤害排名 <roleId, rank>

    public CallRecordCache(int uniqueId, int bossId, long callRoleId, String roleName, byte selectRewardId) {
        this.uniqueId = uniqueId;
        this.bossId = bossId;
        this.callRoleId = callRoleId;
        this.callRoleName = roleName;
        this.callTime = System.currentTimeMillis();
        this.treeSet = new TreeSet<>();
        this.roleRankMap = new ConcurrentHashMap<>();
        this.selectRewardId = selectRewardId;
    }

    public void writeToBuff(NewByteBuffer buff) {
        buff.writeInt(uniqueId);
        buff.writeInt(bossId);
        buff.writeString(String.valueOf(callRoleId));
        buff.writeString(callRoleName);
        buff.writeLong(callTime);
    }

    public void addRoleDamage(RoleDamageCache newCache) {
        synchronized (this) {
            if (treeSet.size() >= CallBossConstant.RANK_VOLUME_MAX) {
                RoleDamageCache last = treeSet.last();
                if (newCache.compareTo(last) == -1) {
                    treeSet.pollLast();
                    addTreeSet(newCache);
                } else {
                    newCache.setRank(CallBossConstant.RANK_OVER_1000);
                }
            } else {
                addTreeSet(newCache);
            }
            roleRankMap.put(newCache.getRoleId(), newCache);
        }
    }

    private void addTreeSet(RoleDamageCache newCache) {
        if (roleRankMap.containsKey(newCache.getRoleId())) {
            treeSet.remove(roleRankMap.get(newCache.getRoleId()));
        }
        treeSet.add(newCache);
    }

    public RoleDamageCache getRoleDamageRank(long roleId) {
        return roleRankMap.get(roleId);
    }

    public void updateRoleDamageRank(RoleDamageCache cache) {
        roleRankMap.put(cache.getRoleId(), cache);
    }

    public int getUniqueId() {
        return uniqueId;
    }

    public int getBossId() {
        return bossId;
    }

    public long getCallRoleId() {
        return callRoleId;
    }

    public String getCallRoleName() {
        return callRoleName;
    }

    public long getCallTime() {
        return callTime;
    }

    public TreeSet<RoleDamageCache> getTreeSet() {
        return treeSet;
    }

    public byte getSelectRewardId() {
        return selectRewardId;
    }
}

