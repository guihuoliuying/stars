package com.stars.services.chat;

import com.stars.core.dao.DbRowDao;
import com.stars.core.gmpacket.specialaccount.SpecialAccountManager;
import com.stars.core.player.PlayerUtil;
import com.stars.coreManager.ExcutorKey;
import com.stars.coreManager.SchedulerManager;
import com.stars.db.DBUtil;
import com.stars.modules.chat.packet.ClientChatMessage;
import com.stars.modules.demologin.packet.ClientAnnouncement;
import com.stars.multiserver.MultiServerHelper;
import com.stars.services.ServiceHelper;
import com.stars.services.ServiceSystem;
import com.stars.services.ServiceUtil;
import com.stars.services.chat.cache.*;
import com.stars.services.chat.filter.*;
import com.stars.services.chat.userdata.ForbiddenChater;
import com.stars.util.LogUtil;
import com.stars.util.StringUtil;
import com.stars.core.actor.Actor;
import com.stars.core.actor.invocation.ServiceActor;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author dengzhou
 */
public class ChatServiceActor extends ServiceActor implements ChatService {

    /**
     * 聊天信息过滤器
     */
    public ArrayList<ChatFilter> chatFilters;

    /**
     * 禁言列表
     */
    public Map<Long, ForbiddenChater> forbiddenChaters;
    /**
     * 世界聊天人员名单
     */
    public ChatCache worldChaters;

    public RpcChatCache rpcChaters;
    /**
     * 帮派聊天人员名单
     */
    public Map<Long, Set<Long>> familyChannelMap;
    /**
     * 队伍聊天人员名单
     */
    public HashMap<Long, Set<Long>> teamChannelList;

    public HashMap<Long, Set<Long>> chatBlackerMap;

    private boolean startup = false;
    private DbRowDao chatDao = new DbRowDao();
    // 运营循环公告序号
    private int loopNoticeSeq = 0;
    // 运营循环公告
    private Map<Integer, LoopNoticeCache> loopNoticeCacheMap = new HashMap<>();

    private Map<Long, Boolean> playerOnlineStateMap;
    private Map<Long, Map<Long, LinkedList<ChatMessage>>> personalOfflineMsgMap;

    @Override
    public void init() throws Throwable {
//        PlayerSystem.getOrAddActor(-3, this);
        ServiceSystem.getOrAdd("chatService", this);

        worldChaters = new ChatCache();
        rpcChaters = new RpcChatCache();
        familyChannelMap = new HashMap<Long, Set<Long>>();
        teamChannelList = new HashMap<Long, Set<Long>>();

        chatBlackerMap = new HashMap<Long, Set<Long>>();
        forbiddenChaters = new HashMap<>();
        // 加载禁言名单
        synchronized (ChatServiceActor.class) {
            String sql = "select * from `forbiddenchater`; ";
            this.forbiddenChaters = DBUtil.queryMap(DBUtil.DB_USER, "roleid", ForbiddenChater.class, sql);
        }
        //添加过滤器
        chatFilters = new ArrayList<ChatFilter>();
        this.regesiterChatFilter(new ForbiddenChaterFilter("forbiddenChater", this));
        this.regesiterChatFilter(new DirtyWordChannelFilter("dirtyWord", this));
        this.regesiterChatFilter(new WorldChannelFilter("world", this));
        this.regesiterChatFilter(new PersonalChannelFilter("personal", this));
        this.regesiterChatFilter(new RMChannelFilter("rm", this));
        this.regesiterChatFilter(new RmCampChannelFilter("rmcamp", this));
        this.regesiterChatFilter(new FamilyChannelFilter("family", this));
        this.regesiterChatFilter(new TeamChannelFilter("team", this));
        this.regesiterChatFilter(new TeamInvitationChannelFilter("teamInvitation", this));
        this.regesiterChatFilter(new SystemChannelFilter("system", this));
        startup = true;
        SchedulerManager.scheduleAtFixedRateIndependent(ExcutorKey.Chat, new GmLoopNoticeTask(), 5, 1, TimeUnit.SECONDS);
        playerOnlineStateMap = new HashMap<>();
        personalOfflineMsgMap = new HashMap<>();
    }

    @Override
    public void printState() {
        LogUtil.info("容器大小输出:{},worldChaters.objectMap:{},familyChannelMap:{},teamChannelList:{}", this.getClass().getSimpleName(), worldChaters.getObjectMapSize(),
                familyChannelMap.size(), teamChannelList.size());
    }

    public void filterChatMessage(Object cm, boolean filterDirty) {
        Object tp = cm;
        for (ChatFilter chatFilter : chatFilters) {
            if (!filterDirty && (chatFilter instanceof DirtyWordChannelFilter)) {
                continue;
            }
            tp = chatFilter.filter(tp);
            if (tp == null) {
                return;
            }
        }
    }

    /**
     * 玩家上线
     */
    @Override
    public void online(long roleId) {
        playerOnlineStateMap.put(roleId, true);
    }

    /**
     * 玩家离线
     */
    @Override
    public void offline(long roleId) {
        playerOnlineStateMap.put(roleId, false);
    }

    /**
     * 缓存私聊离线消息
     *
     * @param cm
     */
    @Override
    public void collectPersonalOfflineChatMsg(ChatMessage cm) {
        long receiverId = cm.getReceiver();
        long senderId = cm.getSenderId();
        Boolean online = playerOnlineStateMap.get(receiverId);
        if (online != null && online) {
            ClientChatMessage ccm = new ClientChatMessage(cm);
            PlayerUtil.send(receiverId, ccm);
            return;
        }
        Map<Long, LinkedList<ChatMessage>> roleOfflineMsgMap = personalOfflineMsgMap.get(receiverId);
        if (roleOfflineMsgMap == null) {
            roleOfflineMsgMap = new HashMap<>();
            personalOfflineMsgMap.put(receiverId, roleOfflineMsgMap);
        }
        LinkedList<ChatMessage> senderMsgList = roleOfflineMsgMap.get(senderId);
        if (senderMsgList == null) {
            senderMsgList = new LinkedList<>();
            roleOfflineMsgMap.put(senderId, senderMsgList);
        }
        if (senderMsgList.size() >= ChatManager.PERSONAL_OFFLINE_MSG_SIZE) {
            senderMsgList.removeFirst();
        }
        senderMsgList.addLast(cm);
    }

    /**
     * 推送私聊离线信息给玩家
     *
     * @param roleId
     */
    @Override
    public void sendPersonalOfflineChatMsgToPlayer(long roleId) {
        Map<Long, LinkedList<ChatMessage>> roleOfflineMsgMap = personalOfflineMsgMap.get(roleId);
        if (StringUtil.isEmpty(roleOfflineMsgMap)) return;
        for (LinkedList<ChatMessage> roleOfflineMsgList : roleOfflineMsgMap.values()) {
            if (StringUtil.isEmpty(roleOfflineMsgList)) return;
            int size = roleOfflineMsgList.size();
            for (int i = 0; i < size; i++) {
                ChatMessage chatMessage = roleOfflineMsgList.removeFirst();
                ClientChatMessage cm = new ClientChatMessage(chatMessage);
                PlayerUtil.send(chatMessage.getReceiver(), cm);
                ServiceHelper.friendService().updateContacts(chatMessage.getReceiver(), chatMessage.getSenderId());
            }
        }
        roleOfflineMsgMap.clear();
    }

    /**
     * @param cm 处理消息入口
     */
    public void filterChatMessage(Object cm) {
        filterChatMessage(cm, Boolean.TRUE);
    }

    public void chat(String senderName, byte channel, long senderId, long receiverId, String message, boolean hasObject) {
        if (SpecialAccountManager.isSpecialAccount(senderId)) {
            //// TODO: 2017-03-25 这里打日志 特殊账号不允许有交互内容
            return;
        }
        ChatMessage cm = new ChatMessage();
        cm.setServerId(MultiServerHelper.getServerId());
        cm.setSenderId(senderId);
        cm.setSenderName(senderName);
        cm.setSenderJob((byte) 0);
        cm.setSenderLevel((short) 0);
        cm.setChannel(channel);
        cm.setReceiver(receiverId);
        cm.setContent(message);
        cm.setContainsObject(hasObject);
        filterChatMessage(cm, Boolean.FALSE);
    }

    /**
     * @param filter
     * @return 注册消息过滤器
     */
    public boolean regesiterChatFilter(ChatFilter filter) {
        return chatFilters.add(filter);
    }

    /**
     * @param filter
     * @param after
     * @return 在after过滤器之后注册过滤器，用于运行中加入一些过滤器
     */
    public boolean regesiterChatFilter(ChatFilter filter, String after) {
        if (chatFilters.size() <= 0) {
            return regesiterChatFilter(filter);
        }
        int index = getFilterIndex(after);
        chatFilters.add(index + 1, filter);
        return true;
    }

    /**
     * @param filterFlag
     * @return 注销消息过滤器
     */
    public boolean unregesiterChatFilter(String filterFlag) {
        int index = getFilterIndex(filterFlag);
        if (index != -1) {
            chatFilters.remove(index);
            return true;
        }
        return false;
    }

    public int getFilterIndex(String filterFlag) {
        int index = 0;
        boolean contain = false;
        for (ChatFilter filter : chatFilters) {
            if (filter.getFlag().equals(filterFlag)) {
                contain = true;
                break;
            }
            index++;
        }
        if (contain) {
            return index;
        }
        return -1;
    }

    public void receiveChatMessage(ChatMessage cm) {

    }

    public Set<Long> getFamilyList(long familyId) {
        return familyChannelMap.get(familyId);
    }

    public Set<Long> getTeamList(long teamId) {
        return teamChannelList.get(teamId);
    }

    @Override
    public void putWorldChannelChater(long roleId, HashSet<Byte> refuseChannel) {
        ChaterObject object = worldChaters.get(roleId);
        if (object != null) {
            object.setRefuseChannel(refuseChannel);
            return;
        }
        object = new ChaterObject(roleId);
        object.setRefuseChannel(refuseChannel);
        worldChaters.add(object);

    }


    @Override
    public void removeWorldChannelChater(long roleId) {
        for (ChatFilter chatFilter : chatFilters) {
            if (chatFilter.getFlag().equals("world") || chatFilter.getFlag().equals("rm")) {
                chatFilter.removeChater(roleId);
            }
        }
        worldChaters.remove(roleId);
    }


    public ChaterObject getChaterObj(long roleId) {
        return this.worldChaters.get(roleId);
    }

    @Override
    public void putChatBlackers(long roleId, Set<Long> backers) {
        Set<Long> set = chatBlackerMap.get(roleId);
        if (set == null) {
            set = new HashSet<Long>(backers);
            chatBlackerMap.put(roleId, set);
            return;
        }
        set.addAll(backers);
    }

    @Override
    public void addChatBlacker(long roleId, long blacker) {
        Set<Long> set = chatBlackerMap.get(roleId);
        if (set == null) {
            set = new HashSet<Long>();
            chatBlackerMap.put(roleId, set);
        }
        set.add(blacker);
    }

    @Override
    public void removeChatBlacker(long roleId, long blacker) {
        Set<Long> set = chatBlackerMap.get(roleId);
        if (set != null) {
            set.remove(blacker);
        }
    }

    @Override
    public void clearChatBlackers(long roleId) {
        chatBlackerMap.remove(roleId);
    }

    @Override
    public void addFamilyMemberId(long familyId, long memberId) {
        if (familyId == 0) {
            return;
        }
        Set<Long> memberSet = familyChannelMap.get(familyId);
        if (memberSet == null) {
            memberSet = new HashSet<>();
            familyChannelMap.put(familyId, memberSet);
        }
        memberSet.add(memberId);
    }

    @Override
    public void delFamilyMemberId(long familyId, long memberId) {
        Set<Long> memberSet = familyChannelMap.get(familyId);
        if (memberSet != null) {
            memberSet.remove(memberId);
            if (memberSet.isEmpty()) {
                familyChannelMap.remove(familyId);
            }
        }
    }

    @Override
    public void addTeamMemberId(long teamId, long memberId) {
        Set<Long> memberSet = teamChannelList.get(teamId);
        if (memberSet == null) {
            memberSet = new HashSet<>();
            teamChannelList.put(teamId, memberSet);
        }
        memberSet.add(memberId);
    }



    @Override
    public void delTeamMemberId(long teamId, long memberId) {
        Set<Long> memberSet = teamChannelList.get(teamId);
        if (memberSet != null) {
            memberSet.remove(memberId);
            if (memberSet.isEmpty()) {
                teamChannelList.remove(teamId);
            }
        }
    }

    @Override
    public void delTeam(long teamId) {
        Set<Long> memberSet = teamChannelList.get(teamId);
        if (memberSet != null) {
            teamChannelList.remove(teamId);
        }
    }

    /**
     * @param roleId
     * @param suspect
     * @return suspect是否在roleId的黑名单里
     */
    public boolean isBlacker(long roleId, long suspect) {
        Set<Long> set = chatBlackerMap.get(roleId);
        if (set == null) {
            return false;
        }
        return set.contains(suspect);
    }

    public MyLinkedListNode getFirstWorldChannelOrder() {
        return worldChaters.getFirstObject();
    }

    /**
     * 发送滚动公告给所有在线玩家
     * message：发送的内容
     */
    public void announce(String message) {
        ClientAnnouncement clientAnnouncement = new ClientAnnouncement(message);
        ServiceUtil.sendPacketToOnline(clientAnnouncement, null);
    }

    /**
     * 发送滚动公告给所有在线玩家
     * message：发送的内容
     * params：替换%s的参数
     */
    public void announce(String message, String... params) {
        ClientAnnouncement clientAnnouncement = new ClientAnnouncement(message, params);
        ServiceUtil.sendPacketToOnline(clientAnnouncement, null);
    }

    @Override
    public void rmChatMessage(int serverId, ChatMessage cMessage) {
        ((RMChannelFilter) chatFilters.get(getFilterIndex("rm"))).sendRMMessage(cMessage);
    }


    @Override
    public void onReceived0(Object message, Actor sender) {
        if (!startup) {
            return;
        }
        this.filterChatMessage(message);
    }

    /**
     * 是否禁言
     *
     * @param roleId
     * @return
     */
    public boolean isForbidden(long roleId) {
        ForbiddenChater forbiddenChater = forbiddenChaters.get(roleId);
        if (forbiddenChater == null)
            return false;
        return System.currentTimeMillis() < forbiddenChater.getExpireTime();
    }

    /**
     * 禁言角色
     *
     * @param roleId
     * @param expireTime
     * @param reason
     */
    @Override
    public void forbidChater(long roleId, int serverId, long expireTime, String reason) {
        ForbiddenChater forbiddenChater = forbiddenChaters.get(roleId);
        if (forbiddenChater == null) {
//			if (forbiddenChater.getServerId() != serverId) {
//				return;
//			}
            forbiddenChater = new ForbiddenChater(roleId, System.currentTimeMillis(), expireTime, reason);
            forbiddenChaters.put(roleId, forbiddenChater);
            chatDao.insert(forbiddenChater);
            return;
        }
        forbiddenChater.setStartTime(System.currentTimeMillis());
        forbiddenChater.setExpireTime(expireTime);
        forbiddenChater.setReason(reason);
        chatDao.update(forbiddenChater);
    }

    /**
     * 解除角色禁言
     *
     * @param roleId
     * @param serverId
     * @return
     */
    @Override
    public void releaseForbidChater(long roleId, int serverId) {
        ForbiddenChater forbiddenChater = forbiddenChaters.get(roleId);
        if (forbiddenChater == null) {
            return;
        }
//		if (forbiddenChater.getServerId() != serverId)
//			return;
        forbiddenChater.setExpireTime(0);
        chatDao.update(forbiddenChater);
    }

    @Override
    public void save() {
        chatDao.flush();
    }

    @Override
    public ForbiddenChater getForbiddenChater(long roleId) {
        if (!forbiddenChaters.containsKey(roleId))
            return null;
        return forbiddenChaters.get(roleId);
    }

    public int newLoopNoticeId() {
        return ++loopNoticeSeq;
    }

    public Map<Integer, LoopNoticeCache> queryLoopNotice() {
        return loopNoticeCacheMap;
    }

    public void publishEditLoopNotice(LoopNoticeCache noticeCache) {
        loopNoticeCacheMap.put(noticeCache.getNoticeId(), noticeCache);
    }

    public void deleteLoopNotice(int noticeId) {
        loopNoticeCacheMap.remove(noticeId);
    }

    public void gmLoopNotice() {
        if (StringUtil.isEmpty(loopNoticeCacheMap)) {
            return;
        }
        List<LoopNoticeCache> noticeList = new LinkedList<>();
        for (LoopNoticeCache noticeCache : loopNoticeCacheMap.values()) {
            List<Long> removeExecuteTime = new LinkedList<>();
            for (long executeTime : noticeCache.getExecuteList()) {
                if (System.currentTimeMillis() >= executeTime) {
                    removeExecuteTime.add(executeTime);
                }
            }
            noticeCache.removeExecuteTime(removeExecuteTime);
            if (!removeExecuteTime.isEmpty()) {
                noticeList.add(noticeCache);
            }
        }
        // 优先级排序
        Collections.sort(noticeList, new Comparator<LoopNoticeCache>() {
            @Override
            public int compare(LoopNoticeCache o1, LoopNoticeCache o2) {
                return o1.getPriority() - o2.getPriority();
            }
        });
        for (LoopNoticeCache noticeCache : noticeList) {
            announce(noticeCache.getTitle() + ":" + noticeCache.getContent());
        }
    }

    class GmLoopNoticeTask implements Runnable {

        @Override
        public void run() {
            gmLoopNotice();
        }
    }
}
