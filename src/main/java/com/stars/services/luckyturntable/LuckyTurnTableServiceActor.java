package com.stars.services.luckyturntable;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerSystem;
import com.stars.core.player.PlayerUtil;
import com.stars.modules.luckyturntable.LuckyTurnTableManager;
import com.stars.modules.luckyturntable.event.InitLuckyEvent;
import com.stars.modules.luckyturntable.packet.ClientLuckyTurnTable;
import com.stars.modules.operateactivity.OperateActivityConstant;
import com.stars.modules.operateactivity.OperateActivityManager;
import com.stars.services.SConst;
import com.stars.services.ServiceHelper;
import com.stars.services.ServiceSystem;
import com.stars.services.luckyturntable.cache.LuckyList;
import com.stars.core.actor.AbstractActor;
import com.stars.core.actor.invocation.ServiceActor;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by chenkeyu on 2017-07-13.
 */
public class LuckyTurnTableServiceActor extends ServiceActor implements LuckyTurnTableService {

    private List<LuckyList> luckyList;
    private int curActivityId = -1;
    private boolean isOpen = false;

    @Override
    public void init() throws Throwable {
        ServiceSystem.getOrAdd(SConst.LuckyTurnTableService, this);
        luckyList = new LinkedList<>();
        curActivityId = OperateActivityManager.getFirstActIdbyActType(OperateActivityConstant.ActType_LuckyTurnTable);
        int curActId = OperateActivityManager.getCurActivityId(OperateActivityConstant.ActType_LuckyTurnTable);
        if (curActId != -1 && !isOpen) {
            initActivity();
        }
    }

    @Override
    public void printState() {

    }

    @Override
    public void initActivity() {
        this.isOpen = true;
        ClientLuckyTurnTable turnTable = new ClientLuckyTurnTable(ClientLuckyTurnTable.iconState);
        turnTable.setIcon(LuckyTurnTableManager.INIT);
        for (AbstractActor actor : PlayerSystem.system().getActors().values()) {
            if (actor instanceof Player) {
                Player player = (Player) actor;
                PlayerUtil.send(player.id(), turnTable);
                ServiceHelper.roleService().notice(player.id(), new InitLuckyEvent());
            }
        }
    }

    @Override
    public void closeActivity() {
        this.isOpen = false;
        ClientLuckyTurnTable turnTable = new ClientLuckyTurnTable(ClientLuckyTurnTable.iconState);
        turnTable.setIcon(LuckyTurnTableManager.OVER);
        for (AbstractActor actor : PlayerSystem.system().getActors().values()) {
            if (actor instanceof Player) {
                Player player = (Player) actor;
                PlayerUtil.send(player.id(), turnTable);
            }
        }
    }

    @Override
    public void sendMainIcon(long roleId, boolean open) {
        ClientLuckyTurnTable turnTable = new ClientLuckyTurnTable(ClientLuckyTurnTable.iconState);
        turnTable.setIcon(isOpen && open ? LuckyTurnTableManager.INIT : LuckyTurnTableManager.OVER);
        PlayerUtil.send(roleId, turnTable);
    }

    @Override
    public void sendLuckyList(long roleId) {
        Collections.sort(luckyList);
        ClientLuckyTurnTable turnTable = new ClientLuckyTurnTable(ClientLuckyTurnTable.awardList);
        turnTable.setList(new LinkedList<>(luckyList));
        PlayerUtil.send(roleId, turnTable);
    }

    @Override
    public void addLuckyList(long roleId, String roleName, int itemId, int count) {
        if (luckyList.size() > LuckyTurnTableManager.luckyward_List - 1) {
            this.luckyList = luckyList.subList(0, LuckyTurnTableManager.luckyward_List - 1);
        }
        LuckyList list = new LuckyList();
        list.setRoleName(roleName);
        list.setItemId(itemId);
        list.setTime(System.currentTimeMillis());
        list.setCount(count);
        luckyList.add(list);
        Collections.sort(luckyList);
        ClientLuckyTurnTable turnTable = new ClientLuckyTurnTable(ClientLuckyTurnTable.awardList);
        turnTable.setList(new LinkedList<>(luckyList));
        PlayerUtil.send(roleId, turnTable);
    }
}
