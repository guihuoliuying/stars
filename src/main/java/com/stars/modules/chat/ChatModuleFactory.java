package com.stars.modules.chat;

import com.stars.core.annotation.DependOn;
import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.chat.event.ChatNewMessageEvent;
import com.stars.modules.chat.gm.AnnounceGmHandler;
import com.stars.modules.chat.gm.ChatBanGmHandler;
import com.stars.modules.chat.listener.*;
import com.stars.modules.chat.prodata.ChatBanVo;
import com.stars.modules.data.DataManager;
import com.stars.modules.family.event.FamilyAuthUpdatedEvent;
import com.stars.modules.friend.event.FriendDelBlackerEvent;
import com.stars.modules.friend.event.FriendInitEvent;
import com.stars.modules.friend.event.FriendNewBlackerEvent;
import com.stars.modules.gm.GmManager;
import com.stars.modules.role.event.RoleLevelUpEvent;
import com.stars.services.chat.ChatManager;

import java.util.List;
import java.util.Map;

@DependOn({MConst.Data})
public class ChatModuleFactory extends AbstractModuleFactory<ChatModule> {

    public ChatModuleFactory() {
        super(new ChatPacketSet());
    }

    @Override
    public ChatModule newModule(long id, Player self,
                                EventDispatcher eventDispatcher, Map<String, Module> map) {
        return new ChatModule(id, self, eventDispatcher, map);
    }

    @Override
    public void loadProductData() throws Exception {
        String chat_limittimeStr = DataManager.getCommConfig("chat_limittime");
        ChatConst.chatLimitTime = chat_limittimeStr != null ? Integer.parseInt(chat_limittimeStr)/1000 : null;
        ChatConst.chatEnterTimeTips = DataManager.getGametext("chat_entertimetips");
        ChatConst.teamChatLimitTime = DataManager.getCommConfig("chat_teamlimittime", 0) / 1000;

        //加载禁言规则表chatban
        String sql = "select * from `chatban`";
        List<ChatBanVo> chatBanList = DBUtil.queryList(
                DBUtil.DB_PRODUCT, ChatBanVo.class, sql);
        ChatManager.setChatBanVoList(chatBanList);
    }

    @Override
    public void init() throws Exception {
        GmManager.reg("chatban", new ChatBanGmHandler());
        GmManager.reg("announce", new AnnounceGmHandler());
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
        eventDispatcher.reg(RoleLevelUpEvent.class, new ChatLevelUpListener(module));
        eventDispatcher.reg(FriendInitEvent.class, new ChatFriendInitListener((ChatModule) module)); // 好友初始化事件
        eventDispatcher.reg(FriendNewBlackerEvent.class, new ChatFriendNewBlackerListener((ChatModule) module)); // 新增黑名单事件
        eventDispatcher.reg(FriendDelBlackerEvent.class, new ChatFriendDelBlackerListener((ChatModule) module)); // 移除黑名单事件
        eventDispatcher.reg(ChatNewMessageEvent.class, new ChatNewMessageListener((ChatModule) module));
        eventDispatcher.reg(FamilyAuthUpdatedEvent.class, new FamilyAuthUpdatedListener((ChatModule) module));
    }
}
