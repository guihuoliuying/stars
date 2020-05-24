package com.stars.modules.friend;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.modules.data.DataManager;
import com.stars.modules.friend.event.*;
import com.stars.modules.friend.listener.*;
import com.stars.modules.role.event.RoleLevelUpEvent;
import com.stars.services.friend.FriendServiceActor;
import com.stars.services.friend.summary.FriendFlowerSummaryComponentImpl;
import com.stars.services.summary.Summary;
import com.stars.services.summary.SummaryConst;
import com.stars.util.StringUtil;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2016/8/12.
 */
public class FriendModuleFactory extends AbstractModuleFactory<FriendModule> {

    public FriendModuleFactory() {
        super(new FriendPacketSet());
    }

    @Override
    public FriendModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
        return new FriendModule(id, self, eventDispatcher, map);
    }

    @Override
    public void init() throws Exception {
        Summary.regComponentClass(SummaryConst.C_FRIEND_FLOWER, FriendFlowerSummaryComponentImpl.class);
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
        eventDispatcher.reg(RoleLevelUpEvent.class, new FriendLevelUpListener((FriendModule) module));
        eventDispatcher.reg(FriendInitEvent.class, new FriendInitListener((FriendModule) module));
        eventDispatcher.reg(FriendNewFriendEvent.class, new FriendNewFriendListener((FriendModule) module));
        eventDispatcher.reg(FriendDelFriendEvent.class, new FriendDelFriendListener((FriendModule) module));
        eventDispatcher.reg(FriendNewBlackerEvent.class, new FriendNewBlackerListener((FriendModule) module));
        eventDispatcher.reg(FriendDelBlackerEvent.class, new FriendDelBlackerListener((FriendModule) module));
        eventDispatcher.reg(FriendApplyAddEvent.class,new FriendAddApplyListener((FriendModule)module));
        eventDispatcher.reg(FriendRemoveApplyEvent.class,new FriendRemoveApplyListener((FriendModule)module));
        eventDispatcher.reg(FriendAddVigorEvent.class,new FriendAddVigorListener((FriendModule)module));
        eventDispatcher.reg(FriendRemoveVigorEvent.class,new FriendRemoveVigorListener((FriendModule)module));
        eventDispatcher.reg(FriendRemoveAllVigorEvent.class, new FriendRemoveAllVigorListener((FriendModule) module));
        eventDispatcher.reg(FriendCanReceiveVigorEvent.class,new FriendCanReceiveVigorListener((FriendModule) module));
        eventDispatcher.reg(FriendReceiveFlowerEvent.class,new FriendReceiveListener((FriendModule) module));
        eventDispatcher.reg(FriendLogEvent.class, new FriendLogListener((FriendModule) module));
    }

    @Override
    public void loadProductData() throws Exception {
        int recommLevelRange = DataManager.getCommConfig("friend_recommend_levelrange", 0);
        int recommMinLevel = DataManager.getCommConfig("friend_recommend_minlevel", 0);
        int recommListSize = DataManager.getCommConfig("friend_recommend_num", 0);
        int blackerMaxSize = DataManager.getCommConfig("friend_blacklist_nummax", 0);
        int contactsListSize = DataManager.getCommConfig("", 5);

        // 加载玩家等级
        String friendSizeStr = DataManager.getCommConfig("friend_maxnum", "999+50");
        Map<Integer, Integer> friendSizeMap = StringUtil.toMap(friendSizeStr, Integer.class, Integer.class, '+', '|');
        // 获取玩家的最大等级
        int maxLevel = 0;
        for (Integer level : friendSizeMap.keySet()) {
            maxLevel = level > maxLevel ? level : maxLevel;
        }
        int recommMaxLevel = maxLevel;
        // 生成玩家等级对应的好友数量
        int[] friendSizeArray = new int[maxLevel + 1];
        int markSize = 0;
        for (int i = recommMaxLevel; i > 0; i--) {
            if (friendSizeMap.containsKey(i)) {
                markSize = friendSizeMap.get(i);
            }
            friendSizeArray[i] = markSize;
        }

        String firstSendFlowerAwardStr = DataManager.getCommConfig("friend_sendflower_firstreward", "");
        Map<Integer, Integer> sendVigorAward = StringUtil.toMap(
                DataManager.getCommConfig("friend_sendvigor_rewarditem", "4+1"), Integer.class, Integer.class, '+', ',');

        /* 赋值 */
        FriendServiceActor.recommLevelRange = recommLevelRange;
        FriendServiceActor.recommMinLevel = recommMinLevel;
        FriendServiceActor.recommListSize = recommListSize;
        FriendServiceActor.blackerMaxSize = blackerMaxSize;
        FriendServiceActor.contactsListSize = contactsListSize;
        FriendServiceActor.recommMaxLevel = recommMaxLevel;
        FriendServiceActor.friendSizeArray = friendSizeArray;
        FriendServiceActor.FIRST_SEND_FLOWER_AWARD = StringUtil.toMap(firstSendFlowerAwardStr, Integer.class, Integer.class, '+', ',');
        FriendServiceActor.sendVigorAward = sendVigorAward;
    }
}
