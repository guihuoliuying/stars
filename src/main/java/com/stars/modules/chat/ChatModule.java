package com.stars.modules.chat;

import com.stars.core.event.EventDispatcher;
import com.stars.core.gmpacket.specialaccount.SpecialAccountManager;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.camp.CampModule;
import com.stars.modules.camp.usrdata.RoleCampPo;
import com.stars.modules.chat.packet.ClientChatMessage;
import com.stars.modules.chat.packet.ClientRefuseChannel;
import com.stars.modules.chat.prodata.ChatBanFreqRule;
import com.stars.modules.chat.prodata.ChatBanVo;
import com.stars.modules.chat.usrdata.ChaterInfo;
import com.stars.modules.demologin.LoginModule;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.family.FamilyModule;
import com.stars.modules.marry.MarryModule;
import com.stars.modules.redpoint.RedPointConst;
import com.stars.modules.role.RoleModule;
import com.stars.modules.serverLog.ServerLogModule;
import com.stars.modules.serverLog.event.SpecialAccountEvent;
import com.stars.modules.vip.VipModule;
import com.stars.network.server.packet.Packet;
import com.stars.services.ServiceHelper;
import com.stars.services.chat.ChatManager;
import com.stars.services.chat.ChatMessage;
import com.stars.util.DateUtil;
import com.stars.util.LogUtil;

import java.util.*;
import java.util.Map.Entry;

public class ChatModule extends AbstractModule {

    private ChaterInfo chaterInfo;
    private Set<Long> chatList;
    private boolean isOpen;
    private Long checkFriendId;
    private Map<Byte, Long> chatLimit = new HashMap<>();

    private HashMap<Byte, List<Long>> roleChatChannelTimeMap = new HashMap<Byte, List<Long>>();
    private final static String CHAT_BAN_FREQ_FLAG = "chat.ban.freq.flag";
    private final static String CHAT_BAN_SLIENT_FLAG = "chat.ban.slient.flag";
    private final static String CHAT_BAN_TIMESTAMP = "chat.ban.timestamp";
    private final static String CHAT_BAN_SLIENT_TIMESTAMP = "chat.ban.slient.timestamp";
    private Map<Byte, String> lastChatWordMap = new HashMap<>();
    private Map<Byte, Short> duplicatedWordCountMap = new HashMap<>(); //连续重复发言次数
    private long lastTeamInvitedTimeStamp = 0L;
    private boolean printDEBUGLog = false;

    public ChatModule(long id, Player self, EventDispatcher eventDispatcher,
                      Map<String, Module> moduleMap) {
        super("聊天", id, self, eventDispatcher, moduleMap);
    }

    @Override
    public void onSyncData() throws Throwable {
        ClientRefuseChannel cfc = new ClientRefuseChannel(); //下发禁言表的产品数据
        cfc.setSubType((byte) 1);
        cfc.setChatBanVoList(ChatManager.getChatBanVoList());
        send(cfc);

        sendChatBanTime2Client();
    }

    @Override
    public void onDataReq() throws Throwable {
        chaterInfo = DBUtil.queryBean(DBUtil.DB_USER, ChaterInfo.class, "select * from chaterinfo where roleid=" + id());
        if (chaterInfo == null) {
            chaterInfo = new ChaterInfo(id());
            this.context().insert(chaterInfo);
        }
    }

    @Override
    public void onCreation(String name, String account) throws Throwable {
        chaterInfo = new ChaterInfo(id());
        this.context().insert(chaterInfo);
    }

    @Override
    public void onInit(boolean isCreation) throws Throwable {
        if (!chaterInfo.refuseChannel(ChatManager.CHANNEL_WORLD)) {
            ServiceHelper.chatService().putWorldChannelChater(id(), chaterInfo.getRefuseChannelSet());
        }
        chatList = new HashSet<>();
        ServiceHelper.chatService().online(id());
        ServiceHelper.chatService().sendPersonalOfflineChatMsgToPlayer(id());
    }

    @Override
    public void onOffline() throws Throwable {
        ServiceHelper.chatService().removeWorldChannelChater(id());
        ServiceHelper.chatService().offline(id());
    }

    @Override
    public void calRedPoint(List<Integer> redPointIds, Map<Integer, String> redPointMap) {
        if (redPointIds.contains(Integer.valueOf(RedPointConst.FRIEND_CHAT))) {
            checkChatRedPoint(redPointMap);
        }
        if (redPointIds.contains(RedPointConst.MARRY_CHAT)) {
            MarryModule marryModule = module(MConst.Marry);
            boolean b = false;
            for (long id : chatList) {
                if (marryModule.marriageWith(id)) {
                    b = true;
                }
            }
            if (b) {
                redPointMap.put(RedPointConst.MARRY_CHAT, Boolean.TRUE.toString());
            } else {
                redPointMap.put(RedPointConst.MARRY_CHAT, null);
            }
        }
    }

    public void addFamilyMemberId(long familyId) {
        if (!chaterInfo.refuseChannel(ChatManager.CHANNEL_FAMILY)) {
            ServiceHelper.chatService().addFamilyMemberId(familyId, id());
        }
    }

    public void refuseChannel(Map<Byte, Byte> map) {
        if (map == null || map.size() == 0) {
            return;
        }
        Set<Entry<Byte, Byte>> set = map.entrySet();
        boolean refuseWorldChannel = chaterInfo.refuseChannel(ChatManager.CHANNEL_WORLD);
        for (Entry<Byte, Byte> entry : set) {
            byte channel = entry.getKey();
            byte status = entry.getValue();
            if (status == 0 && chaterInfo.refuseChannel(channel)) {//允许
                chaterInfo.removeRefuseChannel(channel);
            } else if (status == 1 && !chaterInfo.refuseChannel(channel)) {//拒绝
                chaterInfo.addRefuseChannel(channel);
            }
        }
        FamilyModule family = (FamilyModule) moduleMap().get(MConst.Family);
        if (!chaterInfo.refuseChannel(ChatManager.CHANNEL_FAMILY)) {
            ServiceHelper.chatService().addFamilyMemberId(family.getAuth().getFamilyId(), id());
        } else {
            ServiceHelper.chatService().delFamilyMemberId(family.getAuth().getFamilyId(), id());
        }
        ServiceHelper.chatService().putWorldChannelChater(id(), chaterInfo.getRefuseChannelSet());
        this.context().update(chaterInfo);
    }

    public void sendRefuseChannel2Client() {
        ClientRefuseChannel cfc = new ClientRefuseChannel();
        cfc.setSubType((byte) 0);
        HashSet<Byte> set = chaterInfo.getRefuseChannelSet();
        for (Byte byte1 : set) {
            cfc.addRefuseChannel(byte1);
        }
        send(cfc);
    }

    public void addChatList(long senderId) {
        this.chatList.add(senderId);
        if (isOpen && senderId == checkFriendId) {
            this.chatList.remove(senderId);
        }
        signCalRedPoint(MConst.Chat, RedPointConst.FRIEND_CHAT);
        signCalRedPoint(MConst.Chat, RedPointConst.MARRY_CHAT);
    }

    public void removeChatList(long senderId) {
        this.chatList.remove(senderId);
        this.checkFriendId = senderId;
        signCalRedPoint(MConst.Chat, RedPointConst.FRIEND_CHAT);
        signCalRedPoint(MConst.Chat, RedPointConst.MARRY_CHAT);
    }

    public void chatIsOpen(byte open) {
        this.isOpen = open == 1 ? true : false;
    }

    /**
     * WORLD = 0,		-- 世界频道
     * PERSONAL = 1,	-- 私聊频道
     * FAMILY = 2,		-- 帮派频道
     * TEAM = 3,		-- 队伍频道
     * SYSTEM = 4,		-- 系统频道
     * RPC = 5,		-- 跨服频道
     *
     * @param cm
     */
    public void chatMessage(ChatMessage cm) {
//        /**
//         * 判断是否由于频繁发消息而被限制，
//         */
//        if (limitByChannel(cm))
//            return;
        long now = System.currentTimeMillis(); //当前时间戳
        if (cm.getChannel() == ChatManager.CHANNEL_TEAM_INVITATION) {
            long interval = now - lastTeamInvitedTimeStamp;
            if (interval < ChatConst.teamChatLimitTime * DateUtil.SECOND) {
                send(new ClientText("您发送消息太频繁，请稍后再发"));
                return;
            }
            lastTeamInvitedTimeStamp = now;
        }

        long roleChatBanTimestamp = context().recordMap().getLong(CHAT_BAN_TIMESTAMP, 0l);
        byte roleBanFlag = context().recordMap().getByte(CHAT_BAN_FREQ_FLAG, (byte) 0);
        long roleSilentTimestamp = context().recordMap().getLong(CHAT_BAN_SLIENT_TIMESTAMP, 0l);
        byte roleChatSilentFlag = context().recordMap().getByte(CHAT_BAN_SLIENT_FLAG, (byte) 0);
        if (cm.getChannel() != ChatManager.CHANNEL_PERSONAL && cm.getChannel() != ChatManager.CHANNEL_TEAM_INVITATION) { //个人聊天和组队喊话不受禁言规则影响
            /**
             * 禁言状态监测
             */
            if (roleBanFlag == (byte) 1 && roleChatBanTimestamp < now) {  //解除禁言
                context().recordMap().setByte(CHAT_BAN_FREQ_FLAG, (byte) 0);
            } else if (roleBanFlag == (byte) 1) { //还未解除禁言
                send(new ClientText("您发送消息太频繁，请稍后再发"));
                return;
            }

            if (roleSilentTimestamp < now && roleChatSilentFlag == (byte) 1) { //解除静默禁言
                context().recordMap().setByte(CHAT_BAN_SLIENT_FLAG, (byte) 0);
            }
            /**
             * 禁言规则检测
             */
            ChatBanVo roleChatBanVo = getRoleChatBanVo(cm);
            if (roleChatBanVo == null) {
                com.stars.util.LogUtil.info("策划没有配这个玩家的禁言规则，请检查chatban和这个玩家" + id());
            } else {
                com.stars.network.server.packet.Packet checkPacket = checkChatBan(roleChatBanVo, cm);
                if (checkPacket != null) {
                    send(checkPacket);
                    return;
                }
            }
        }

        RoleModule rm = (RoleModule) module(MConst.Role);
        if (rm.getRoleRow().getLevel() < 15) {
            return;
        }
        VipModule vip = module(MConst.Vip);

        if (vip.getVipLevel() <= 0 && rm.getRoleRow().getLevel() < 35) {
            send(new ClientText("等级达到35级才能发送消息"));
            return;
        }
        CampModule campModule = module(MConst.Camp);
        RoleCampPo roleCamp = campModule.getRoleCamp();
        LoginModule loginModule = module(MConst.Login);
        cm.setSenderId(id());
        cm.setSenderName(rm.getRoleRow().getName());
        cm.setSenderJob((byte) rm.getRoleRow().getJobId());
        cm.setSenderLevel((short) rm.getLevel());
        cm.setSenderVipLv(vip.getVipLevel());
        cm.setAccount(loginModule.getAccount());
        if (cm.getChannel() == ChatManager.CHANNEL_RM_CAMP) {
            if (roleCamp != null) {
                cm.setCampType(roleCamp.getCampType());
                cm.setCommonOfficerId(roleCamp.getCommonOfficerId());
                cm.setRareOfficerId(roleCamp.getRareOfficerId());
                cm.setDesignateOfficerId(roleCamp.getDesignateOfficerId());
            }
        }
        ServerLogModule log = module(MConst.ServerLog);
        log.log_chat(cm);    // 聊天日志
        //查看玩家是否处于静默禁言中，如果在，则只发送给自己
        roleChatSilentFlag = context().recordMap().getByte(CHAT_BAN_SLIENT_FLAG, (byte) 0);
        String lastWord = new String(cm.getContent());
        lastChatWordMap.put(cm.getChannel(), lastWord);
        if (roleChatSilentFlag == (byte) 1 && cm.getChannel() != ChatManager.CHANNEL_PERSONAL
                && cm.getChannel() != ChatManager.CHANNEL_TEAM_INVITATION) {
            sendMessageToMe(rm.getRoleRow().getName(), cm.getContent(), cm.getChannel());
            return;
        }
        if (cm.getChannel() != ChatManager.CHANNEL_PERSONAL && cm.getChannel() != ChatManager.CHANNEL_TEAM_INVITATION) {
            List<Long> roleChatTimestamp = roleChatChannelTimeMap.get(cm.getChannel());
            if (roleChatTimestamp == null) {
                roleChatTimestamp = new ArrayList<>();
            }
            roleChatTimestamp.add(System.currentTimeMillis());
            roleChatChannelTimeMap.put(cm.getChannel(), roleChatTimestamp);
        }

        ServiceHelper.chatService().filterChatMessage(cm);
        if (SpecialAccountManager.isSpecialAccount(cm.getSenderId())) {
            eventDispatcher().fire(new SpecialAccountEvent(cm.getSenderId(), "在" + cm.getChannel() + "频道发消息", true));
        }
    }

    /**
     * 判断是否由于频繁发消息而被限制，
     *
     * @param cm
     * @return
     */
    private boolean limitByChannel(ChatMessage cm) {

        switch (cm.getChannel()) {
            case ChatConst.WORLD: {
                if (limitByTime(cm, ChatConst.chatLimitTime))
                    return true;
            }
            break;
            case ChatConst.FAMILY: {
                if (limitByTime(cm, ChatConst.chatLimitTime))
                    return true;
            }
            break;
            case ChatConst.RPC: {
                if (limitByTime(cm, ChatConst.chatLimitTime))
                    return true;
            }
            break;
            case ChatConst.TEAM: {
                if (limitByTime(cm, ChatConst.teamChatLimitTime)) {
                    return true;
                }
            }
            break;
        }
        return false;
    }

    /**
     * 获得符合玩家当前的禁言规则
     *
     * @param cm
     * @return
     */
    private ChatBanVo getRoleChatBanVo(ChatMessage cm) {
        List<ChatBanVo> chatBanVoList = ChatManager.getChatBanVoList();
        for (ChatBanVo chatBanVo : chatBanVoList) {
            if (chatBanVo.getChannel() != cm.getChannel()) //非本频道过滤
                continue;
            RoleModule role = (RoleModule) module(MConst.Role);
            if (role.getLevel() < chatBanVo.getMinLv() || role.getLevel() > chatBanVo.getMaxLv()) //等级段不对过滤
                continue;
            VipModule roleVip = (VipModule) module(MConst.Vip);
            if (roleVip.getVipLevel() < chatBanVo.getMinVipLv() || roleVip.getVipLevel() > chatBanVo.getMaxVipLv()) //贵族等级段不对过滤
                continue;
            return chatBanVo;
        }
        return null;
    }

    /**
     * 检查玩家是否符合相应的禁言规则
     *
     * @param roleChatBanVo
     * @return
     */
    private Packet checkChatBan(ChatBanVo roleChatBanVo, ChatMessage cm) {
        List<Long> roleChatTimeList = roleChatChannelTimeMap.get(roleChatBanVo.getChannel());
        if (roleChatTimeList == null) {
            roleChatTimeList = new ArrayList<Long>();
        }

        //获取检测最长的间隔，缓解缓存压力
        long now = System.currentTimeMillis();
        long maxRecordTimeInterval = roleChatBanVo.getMaxFreqSecond() * DateUtil.SECOND;
        long needRemainTime = now - maxRecordTimeInterval;
        Iterator iterator = roleChatTimeList.iterator();
        while (iterator.hasNext()) {
            long historyTimestamp = (long) iterator.next();
            if (historyTimestamp < needRemainTime) {
                iterator.remove();
            } else {
                break;
            }
        }
        //检测是否会被禁言
        List<ChatBanFreqRule> chatBanFreqRuleList = roleChatBanVo.getFreqRuleList();
        for (ChatBanFreqRule freqRule : chatBanFreqRuleList) {
            List<Long> tmpChatTimeList = new ArrayList<>();
            tmpChatTimeList.addAll(roleChatTimeList);  //先加入玩家所有存的时间戳
            long checkTimeStampInterval = freqRule.getSecondInterval() * DateUtil.SECOND;
            Iterator historyTimestampIter = tmpChatTimeList.iterator();
            while (historyTimestampIter.hasNext()) {
                long historyTimeStamp = (long) historyTimestampIter.next();
                if ((now - historyTimeStamp) > checkTimeStampInterval) {
                    historyTimestampIter.remove(); //删除玩家不在时间范围内的时间戳
                } else {
                    break;
                }
            }
            printDebugLog("聊天禁言容器|roleid:" + id() + "|禁言频道：" + cm.getChannel() + "|禁言规则频率时间段：" + freqRule.getSecondInterval() + "s|当前容器大小：" + tmpChatTimeList.size());
            if (tmpChatTimeList.size() >= freqRule.getFreqLimit()) {
                //玩家要被禁言
                context().recordMap().setByte(CHAT_BAN_FREQ_FLAG, (byte) 1);
                context().recordMap().setLong(CHAT_BAN_TIMESTAMP, now + freqRule.getBanSeconds() * DateUtil.SECOND);
                printDebugLog("普通禁言触发|roleid|" + id() + "|禁言频道：" + cm.getChannel() + "|触发禁言规则:" + freqRule.toString());
                sendChatBanTime2Client();
                return new ClientText("您发送消息太频繁，请稍后再发");
            }
        }

        String lastChatWord = lastChatWordMap.get(cm.getChannel());
        short duplicatedWordCount = duplicatedWordCountMap.get(cm.getChannel()) == null ? 0 : duplicatedWordCountMap.get(cm.getChannel());
        if (lastChatWord != null && lastChatWord.equals(cm.getContent())) {
            duplicatedWordCount++;
        } else {
            duplicatedWordCount = 1;
        }
        duplicatedWordCountMap.put(cm.getChannel(), duplicatedWordCount); //刷新频道内重复发言次数

        Iterator iter = roleChatBanVo.getSilentRuleMap().entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<Short, Short> entry = (Map.Entry<Short, Short>) iter.next();
            if (duplicatedWordCount > entry.getKey()) {
                long silentTimeStamp = entry.getValue() * DateUtil.SECOND;
                long currentEndSilentTimestamp = context().recordMap().getLong(CHAT_BAN_TIMESTAMP, 0l);
                //选择禁言时间较长的那个
                long newEndSilentTimestamp = (now + silentTimeStamp) > currentEndSilentTimestamp ? now + silentTimeStamp : currentEndSilentTimestamp;
                context().recordMap().setByte(CHAT_BAN_SLIENT_FLAG, (byte) 1);
                context().recordMap().setLong(CHAT_BAN_SLIENT_TIMESTAMP, newEndSilentTimestamp);
                printDebugLog("静默禁言触发|roleid:" + id() + "|禁言频道：" + cm.getChannel() + "|检测发言次数限制：" + entry.getKey() + "|重复发言次数:" + duplicatedWordCount + "|静默禁言时间：" + (newEndSilentTimestamp - now) / DateUtil.SECOND + "s");
            }
        }
        return null;
    }

    /**
     * 发送到自己的某个频道
     *
     * @param sender  发送者名字
     * @param message 发送内容
     * @param channel 发送到的频道
     */
    public void sendMessageToMe(String sender, String message, byte channel) {
        ChatMessage msg = new ChatMessage();
        msg.setChannel(channel);
        msg.setSenderName(sender);
        msg.setContent(message);
        ClientChatMessage packet = new ClientChatMessage(msg);
        send(packet);
    }

    /**
     * 改变玩家禁言状态
     *
     * @param type                1--禁言  2--静默禁言 0--全部
     * @param status              0--解除  1--禁言
     * @param chatBanEndTimestamp 结束禁言时间戳
     */
    public void changRoleChatBan(byte type, byte status, long chatBanEndTimestamp) {
        if (type == (byte) 0) {  //全部禁言类型
            context().recordMap().setByte(CHAT_BAN_SLIENT_FLAG, status);
            context().recordMap().setLong(CHAT_BAN_SLIENT_TIMESTAMP, chatBanEndTimestamp);
            context().recordMap().setByte(CHAT_BAN_FREQ_FLAG, status);
            context().recordMap().setLong(CHAT_BAN_TIMESTAMP, chatBanEndTimestamp);
            sendChatBanTime2Client();
        } else if (type == (byte) 1) { //禁言
            context().recordMap().setByte(CHAT_BAN_FREQ_FLAG, status);
            context().recordMap().setLong(CHAT_BAN_TIMESTAMP, chatBanEndTimestamp);
            sendChatBanTime2Client();
        } else if (type == (byte) 2) { //静默禁言
            context().recordMap().setByte(CHAT_BAN_SLIENT_FLAG, status);
            context().recordMap().setLong(CHAT_BAN_SLIENT_TIMESTAMP, chatBanEndTimestamp);
        }

    }

    private void sendChatBanTime2Client() {
        ClientRefuseChannel roleCfc = new ClientRefuseChannel(); //下发玩家被禁言秒数
        roleCfc.setSubType((byte) 2);
        long now = System.currentTimeMillis();
        long banEndTimestamp = context().recordMap().getLong(CHAT_BAN_TIMESTAMP, 0l);
        int interval = (int) ((banEndTimestamp - now) / DateUtil.SECOND);
        roleCfc.setRemainSecond(interval > 0 ? interval : 0);
        send(roleCfc);
    }

    private boolean limitByTime(ChatMessage cm, int limitTime) {
        long now = now();
        Long last = chatLimit.get(cm.getChannel());
        if (last != null) {
            long result = now - last;
            if (result < limitTime) {
                String msg = String.format(ChatConst.chatEnterTimeTips, result);
                warn(msg);
                return true;
            }
        }
        chatLimit.put(cm.getChannel(), now);
        return false;
    }

    private void checkChatRedPoint(Map<Integer, String> redPointMap) {
        checkRedPoint(redPointMap, chatList, RedPointConst.FRIEND_CHAT);
    }

    private void checkRedPoint(Map<Integer, String> redPointMap, Set<Long> list, int redPointConst) {
        StringBuilder builder = new StringBuilder("");
        if (!list.isEmpty()) {
            Iterator<Long> iterator = list.iterator();
            while (iterator.hasNext()) {
                builder.append(iterator.next()).append("+");
            }
            redPointMap.put(redPointConst, builder.toString().isEmpty() ? null : builder.toString());
        } else {
            redPointMap.put(redPointConst, null);
        }
    }

    private void printDebugLog(String log) {
        if (printDEBUGLog == true) {
            LogUtil.info(log);
        }
    }


}
