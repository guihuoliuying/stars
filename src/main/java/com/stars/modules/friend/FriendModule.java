package com.stars.modules.friend;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.modules.MConst;
import com.stars.modules.friend.event.FriendAchieveEvent;
import com.stars.modules.friend.event.FriendLogEvent;
import com.stars.modules.redpoint.RedPointConst;
import com.stars.modules.role.RoleModule;
import com.stars.modules.role.userdata.Role;
import com.stars.modules.tool.ToolManager;
import com.stars.modules.tool.ToolModule;
import com.stars.modules.tool.productdata.ItemVo;
import com.stars.services.ServiceHelper;
import com.stars.services.friend.userdata.FriendRolePo;
import com.stars.services.friend.userdata.ReceiveFlowerRecordPo;
import com.stars.services.summary.SummaryComponent;
import com.stars.util.StringUtil;

import java.util.*;

/**
 * Created by zhaowenshuo on 2016/8/11.
 */
public class FriendModule extends AbstractModule {

    private Set<Long> friendList;
    private Set<Long> blackList;
    private Set<Long> applyList;
    private Set<Long> vigorList;
    private boolean canReceiveVigor;

    public FriendModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super("好友", id, self, eventDispatcher, moduleMap);
    }

    @Override
    public void onInit(boolean isCreation) throws Throwable {
        RoleModule roleModule = (RoleModule) module(MConst.Role);
        Role role = roleModule.getRoleRow();
        ServiceHelper.friendService().online(id(), role.getName(), role.getJobId(), role.getLevel(), role.getFightScore());
        /*ServiceHelper.friendService().getDailyReceiveVigorTimes(id());
        ServiceHelper.friendService().sendFriendList(id());
        ServiceHelper.friendService().sendReceivedApplicationList(id());*/
        vigorList = new HashSet<>();
    }

    @Override
    public void onSyncData() throws Throwable {
        if(StringUtil.isEmpty(friendList))  //成就达成登陆检测
            return;
        FriendAchieveEvent event = new FriendAchieveEvent(friendList.size());
        eventDispatcher().fire(event);
    }

    @Override
    public void onReconnect() throws Throwable {
        RoleModule roleModule = (RoleModule) module(MConst.Role);
        Role role = roleModule.getRoleRow();
        ServiceHelper.friendService().online(id(), role.getName(), role.getJobId(), role.getLevel(), role.getFightScore());
    }

    @Override
    public void onOffline() throws Throwable {
        ServiceHelper.friendService().offline(id());
    }

    public void innerAddVigorList(long friendId) {
        if (vigorList == null) {
            vigorList = new HashSet<>();
        }
        if (canReceiveVigor) {
            this.vigorList.add(friendId);
            signCalRedPoint(MConst.Friend, RedPointConst.FRIEND_VIGOR);
        }
    }

    public void innerRemoveVigorList(Long friendId) {
        if (vigorList == null) {
            return;
        }
        this.vigorList.remove(friendId);
        signCalRedPoint(MConst.Friend, RedPointConst.FRIEND_VIGOR);
    }

    public void innerRemoveAllVigorList() {
        if (vigorList == null) {
            return;
        }
        vigorList.clear();
        signCalRedPoint(MConst.Friend, RedPointConst.FRIEND_VIGOR);
    }

    /*public void innerSetVigorList(Set<Long> vigorList) {
        this.vigorList = vigorList;
    }*/

    public void setCanReceiveVigor(boolean canReceiveVigor) {
        this.canReceiveVigor = canReceiveVigor;
    }

    public void innerAddApplyList(long friendId) {
        this.applyList.add(friendId);
        signCalRedPoint(MConst.Friend, RedPointConst.FRIEND_APPLY);
    }

    public void innerRemoveApplyList(long friendId) {
        this.applyList.remove(friendId);
        signCalRedPoint(MConst.Friend, RedPointConst.FRIEND_APPLY);
    }

    public void innerSetApplyList(Set<Long> applyList) {
        this.applyList = applyList;
    }

    public void innerSetFriendList(Set<Long> friendList) {
        this.friendList = new HashSet<>(friendList);

        FriendAchieveEvent event = new FriendAchieveEvent(friendList.size());
        eventDispatcher().fire(event);
    }

    public void innerSetBlackList(Set<Long> blackList) {
        this.blackList = new HashSet<>(blackList);
    }

    public void innerNewFriend(long friendId) {
        this.friendList.add(friendId);

        FriendAchieveEvent event = new FriendAchieveEvent(friendList.size());
        eventDispatcher().fire(event);
    }

    public void innerDelFriend(long friendId) {
        this.friendList.remove(friendId);
    }

    public void innerNewBlacker(long blackerId) {
        this.blackList.add(blackerId);
    }

    public void innerDelBlacker(long blackerId) {
        this.blackList.remove(blackerId);
    }

    public boolean isFriend(long roleId) {
        return friendList.contains(roleId);
    }

    public boolean isBlacker(long roleId) {
        return blackList.contains(roleId);
    }

    /**
     * 赠送鲜花
     */
    public void sendFlower(long friendId, int itemId, int count) {
        ItemVo itemVo = ToolManager.getItemVo(itemId);
        if (itemVo == null) return;
        if (count <= 0 || count > 999) return;

        boolean toolEnough = false;
        ToolModule toolModule = module(MConst.Tool);
        long hasCount = toolModule.getCountByItemId(itemId);
        if (hasCount >= count) {//鲜花数量足够
            if (toolModule.deleteAndSend(itemId, count)) {//直接扣除鲜花道具
                toolEnough = true;
            } else {//扣除道具失败
                return;
            }
        }

        if (!toolEnough) {//鲜花不足,需要购买
            int needBuyCount = (int) (count - hasCount);
            //直接购买
            if (toolModule.deleteAndSend(itemVo.getBuyPrice()[0], needBuyCount * (itemVo.getBuyPrice()[1]))) {
                if (hasCount > 0) {//扣除拥有的鲜花道具
                    toolModule.deleteAndSend(itemId, (int) hasCount);
                }
            } else {//金钱不足
                return;
            }
        }
        fireSpecialAccountLogEvent("赠送鲜花");
        //执行赠送鲜花
        ServiceHelper.friendService().sendFlower(id(), friendId, itemId, count);
    }

    @Override
    public void onUpateSummary(Map<String, SummaryComponent> componentMap) {

    }

    @Override
    public void calRedPoint(List<Integer> redPointIds, Map<Integer, String> redPointMap) {
        if (redPointIds.contains(Integer.valueOf(RedPointConst.FRIEND_APPLY))) {
            checkApplyRedPoint(redPointMap);
        }
        if (redPointIds.contains(Integer.valueOf(RedPointConst.FRIEND_VIGOR))) {
            checkVigorRedPoint(redPointMap);
        }
    }

    private void checkApplyRedPoint(Map<Integer, String> redPointMap) {
        checkRedPoint(redPointMap, applyList, RedPointConst.FRIEND_APPLY);
    }

    private void checkVigorRedPoint(Map<Integer, String> redPointMap) {
        //不清楚这里为什么会有玩家自己的id，先做特殊处理
        Iterator<Long> iterator = vigorList.iterator();
        while (iterator.hasNext()) {
            if (iterator.next() == id()) {
                vigorList.remove(id());
            }
        }
        checkRedPoint(redPointMap, vigorList, RedPointConst.FRIEND_VIGOR);
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

    public void syncReceiveFlowerRecord(FriendRolePo rolePo, ReceiveFlowerRecordPo recordPo) {

    }

    public void fireSpecialAccountLogEvent(String content) {

    }

    public void friendLog(FriendLogEvent event) {

    }

    public Set<Long> getFriendList() {
        return friendList;
    }


}
