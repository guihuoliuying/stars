package com.stars.services.postsync;

import com.stars.services.family.FamilyAuth;
import com.stars.services.postsync.aoi.AoiObject;

import java.util.Collection;
import java.util.List;

/**
 * Created by zhaowenshuo on 2017/6/23.
 */
public interface PositionSyncRelationService {

    boolean isHighVip(long otherId);

    boolean isCouple(long roleId, long otherId);

    boolean isFriend(long roleId, long otherId);

    boolean isFamilyOffice(long roleId, long otherId);

    boolean isFamilyMember(long roleId, long otherId);

    List<AoiObject> sort(long roleId, List<AoiObject> otherIdList);

    void addRelation(long roleId);

    void delRelation(long roleId);

    void updateVip(long roleId, int vipLevel);

    void updateCoupleId(long roleId, long coupleId);

    void addFriendId(long roleId, Collection<Long> friendIdSet);

    void delFriendId(long roleId, Collection<Long> friendIdSet);

    void updateFamilyAuth(long roleId, FamilyAuth auth);

}
