package com.stars.services.postsync;

import com.stars.modules.positionsync.PositionSyncManager;
import com.stars.services.family.FamilyAuth;
import com.stars.services.family.FamilyPost;
import com.stars.services.postsync.aoi.AoiObject;

import java.util.*;

/**
 * Created by zhaowenshuo on 2017/6/24.
 */
public class PositionSyncRelationServiceImpl implements PositionSyncRelationService {

    private Map<Long, PositionSyncRelation> relationMap = new HashMap<>();

    @Override
    public boolean isHighVip(long otherId) {
        PositionSyncRelation relation = relationMap.get(otherId);
        if (relation != null) {
            return relation.getVipLevel() >= PositionSyncManager.HighVipLevel;
        }
        return false;
    }

    @Override
    public boolean isCouple(long roleId, long otherId) {
        PositionSyncRelation relation = relationMap.get(roleId);
        if (relation != null) {
            return relation.getCoupleId() == otherId;
        }
        return false;
    }

    @Override
    public boolean isFriend(long roleId, long otherId) {
        PositionSyncRelation relation = relationMap.get(roleId);
        if (relation != null && relation.getFriendIdSet() != null) {
            return relation.getFriendIdSet().contains(otherId);
        }
        return false;
    }

    @Override
    public boolean isFamilyOffice(long roleId, long otherId) {
        PositionSyncRelation selfRelation = relationMap.get(roleId);
        PositionSyncRelation otherRelation = relationMap.get(otherId);
        if (selfRelation != null && otherRelation != null && selfRelation.getAuth() != null && otherRelation.getAuth() != null) {
            if (selfRelation.getAuth().getFamilyId() == otherRelation.getAuth().getFamilyId()) {
                FamilyPost post = otherRelation.getAuth().getPost();
                return post == FamilyPost.ELDER || post == FamilyPost.ASSISTANT || post == FamilyPost.MASTER;
            }
        }
        return false;
    }

    @Override
    public boolean isFamilyMember(long roleId, long otherId) {
        PositionSyncRelation selfRelation = relationMap.get(roleId);
        PositionSyncRelation otherRelation = relationMap.get(otherId);
        if (selfRelation != null && otherRelation != null && selfRelation.getAuth() != null && otherRelation.getAuth() != null) {
            return selfRelation.getAuth().getFamilyId() == otherRelation.getAuth().getFamilyId();
        }
        return false;
    }

    private int getPostId(long roleId) {
        PositionSyncRelation relation = relationMap.get(roleId);
        if (relation != null && relation.getAuth() != null) {
            return relation.getAuth().getPost().getId();
        }
        return FamilyPost.ERROR_ID;
    }

    private int getVipLevel(long roleId) {
        PositionSyncRelation relation = relationMap.get(roleId);
        if (relation != null) {
            return relation.getVipLevel();
        }
        return 0;
    }

    @Override
    public List<AoiObject> sort(final long roleId, List<AoiObject> otherIdList) {
        Collections.sort(otherIdList, new Comparator<AoiObject>() {
            @Override
            public int compare(AoiObject otherAoiObj1, AoiObject otherAoiObj2) {
                long otherRoleId1 = otherAoiObj1.roleId();
                long otherRoleId2 = otherAoiObj2.roleId();
                /* 判断姻缘 */
                if (isCouple(roleId, otherRoleId1)) return -1;
                if (isCouple(roleId, otherRoleId2)) return 1;
                /* 判断家族族长/副族长/长老 */
                if (isFamilyOffice(roleId, otherRoleId1) || isFamilyOffice(roleId, otherRoleId2)) {
                    int postId1 = getPostId(otherRoleId1);
                    int postId2 = getPostId(otherRoleId2);
                    if (postId1 != postId2) {
                        return postId1 - postId2;
                    }
                }
                /* 判断高V */
                if (isHighVip(otherRoleId1) && !isHighVip(otherRoleId2)) return -1;
                if (!isHighVip(otherRoleId1) && isHighVip(otherRoleId2)) return 1;
                if (isHighVip(otherRoleId1) && isHighVip(otherRoleId2) && getVipLevel(otherRoleId1) != getVipLevel(otherRoleId2)) {
                    return getVipLevel(otherRoleId2) - getVipLevel(otherRoleId1);
                }
                /* 判断好友 */
                if (isFriend(roleId, otherRoleId1) && !isFriend(roleId, otherRoleId2)) return -1;
                if (!isFriend(roleId, otherRoleId1) && isFriend(roleId, otherRoleId2)) return 1;
                /* 判断家族成员 */
                if (isFamilyMember(roleId, otherRoleId1) && !isFamilyMember(roleId, otherRoleId2)) return -1;
                if (!isFamilyMember(roleId, otherRoleId1) && isFamilyMember(roleId, otherRoleId2)) return 1;
                /* 判断roleId */
                return otherRoleId1 < otherRoleId2 ? -1 : 1;
            }
        });
        return otherIdList;
    }

    @Override
    public void addRelation(long roleId) {
        if (!relationMap.containsKey(roleId)) {
            PositionSyncRelation relation = new PositionSyncRelation(roleId);
            relationMap.put(roleId, relation);
        }
    }

    @Override
    public void delRelation(long roleId) {
        relationMap.remove(roleId);
    }

    @Override
    public void updateVip(long roleId, int vipLevel) {
        PositionSyncRelation relation = relationMap.get(roleId);
        if (relation != null) {
            relation.setVipLevel(vipLevel);
        }
    }

    @Override
    public void updateCoupleId(long roleId, long coupleId) {
        PositionSyncRelation relation = relationMap.get(roleId);
        if (relation != null) {
            relation.setCoupleId(coupleId);
        }
    }

    // fixme:
    @Override
    public void addFriendId(long roleId, Collection<Long> friendIdSet) {
        PositionSyncRelation relation = relationMap.get(roleId);
        if (relation != null && relation.getFriendIdSet() != null) {
            relation.getFriendIdSet().addAll(friendIdSet);
        }
    }

    @Override
    public void delFriendId(long roleId, Collection<Long> friendIdSet) {
        PositionSyncRelation relation = relationMap.get(roleId);
        if (relation != null && relation.getFriendIdSet() != null) {
            relation.getFriendIdSet().removeAll(friendIdSet);
        }
    }

    @Override
    public void updateFamilyAuth(long roleId, FamilyAuth auth) {
        PositionSyncRelation relation = relationMap.get(roleId);
        if (relation != null) {
            relation.setAuth(new FamilyAuth(auth));
        }
    }
}
