package com.stars.services.chat;

import com.stars.services.Service;
import com.stars.services.chat.cache.LoopNoticeCache;
import com.stars.services.chat.userdata.ForbiddenChater;
import com.stars.core.actor.invocation.ActorService;
import com.stars.core.actor.invocation.annotation.AsyncInvocation;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by zhaowenshuo on 2016/7/22.
 */
public interface ChatService extends Service, ActorService {

    @AsyncInvocation
    void chat(String senderName, byte channel, long senderId, long receiverId, String content, boolean hasObject);

    @AsyncInvocation
    void putWorldChannelChater(long roleId, HashSet<Byte> refuseChannel);

    @AsyncInvocation
    void removeWorldChannelChater(long roleId);

    @AsyncInvocation
    void filterChatMessage(Object cm);

    @AsyncInvocation
    void putChatBlackers(long roleId, Set<Long> backers);

    @AsyncInvocation
    void addChatBlacker(long roleId, long blacker);

    @AsyncInvocation
    void removeChatBlacker(long roleId, long blacker);

    @AsyncInvocation
    void clearChatBlackers(long roleId);

    @AsyncInvocation
    void addFamilyMemberId(long familyId, long memberId);

    @AsyncInvocation
    void delFamilyMemberId(long familyId, long memberId);

    @AsyncInvocation
    void addTeamMemberId(long teamId, long memberId);

    @AsyncInvocation
    void delTeamMemberId(long teamId, long memberId);

    @AsyncInvocation
    void delTeam(long teamId);

    @AsyncInvocation
    void rmChatMessage(int serverId, ChatMessage cMessage);

    @AsyncInvocation
    void announce(String message);

    @AsyncInvocation
    void announce(String message, String... params);

    @AsyncInvocation
    public void forbidChater(long roleId, int serverId, long expireTime, String reason);

    @AsyncInvocation
    public void releaseForbidChater(long roleId, int serverId);

    public ForbiddenChater getForbiddenChater(long roleId);

    @AsyncInvocation
    public void save();

    public int newLoopNoticeId();

    public Map<Integer, LoopNoticeCache> queryLoopNotice();

    @AsyncInvocation
    public void publishEditLoopNotice(LoopNoticeCache noticeCache);

    @AsyncInvocation
    public void deleteLoopNotice(int noticeId);

    @AsyncInvocation
    public void gmLoopNotice();

    @AsyncInvocation
    public void collectPersonalOfflineChatMsg(ChatMessage cm);

    @AsyncInvocation
    public void sendPersonalOfflineChatMsgToPlayer(long roleId);

    @AsyncInvocation
    public void online(long roleId);

    @AsyncInvocation
    public void offline(long roleId);

}
