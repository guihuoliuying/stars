package com.stars.services.family.welfare.redpacket;

import com.stars.services.Service;
import com.stars.services.family.FamilyAuth;
import com.stars.core.actor.invocation.ActorService;
import com.stars.core.actor.invocation.annotation.AsyncInvocation;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2016/9/6.
 */
public interface FamilyRedPacketService extends Service, ActorService {

    @AsyncInvocation
    void online(long familyId, long roleId);

    @AsyncInvocation
    void offline(long familyId);

    @AsyncInvocation
    void save();

    @AsyncInvocation
    void sendRedPacketInfo(FamilyAuth auth);

    boolean giveRedPacket(FamilyAuth auth);


    boolean seizeRedPacket(FamilyAuth auth, long redPacketId);

    @AsyncInvocation
    void addRedPacket(FamilyAuth auth, int number);

    /*  */
    @AsyncInvocation
    void addFamily(long familyId);

    @AsyncInvocation
    void addMember(long familyId, long memberId);

    @AsyncInvocation
    void delMember(long familyId, long memberId);

    @AsyncInvocation
    void updateMemberCount(long familyId, int memberCount);

    @AsyncInvocation
    void updateSeizedRedPacketInfo(FamilyAuth auth, long redPacketId, Map<Integer, Integer> toolMap);

}
