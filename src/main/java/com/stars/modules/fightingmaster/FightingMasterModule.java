package com.stars.modules.fightingmaster;

import com.stars.core.event.EventDispatcher;
import com.stars.core.gmpacket.specialaccount.SpecialAccountManager;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.modules.MConst;
import com.stars.modules.buddy.BuddyModule;
import com.stars.modules.buddy.userdata.RoleBuddy;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.drop.DropModule;
import com.stars.modules.family.FamilyModule;
import com.stars.modules.fightingmaster.event.FiveRewardStatusEvent;
import com.stars.modules.fightingmaster.event.GetFiveRewardEvent;
import com.stars.modules.fightingmaster.packet.ClientFightingMaster;
import com.stars.modules.foreshow.ForeShowConst;
import com.stars.modules.foreshow.ForeShowModule;
import com.stars.modules.name.event.RoleRenameEvent;
import com.stars.modules.newequipment.NewEquipmentConstant;
import com.stars.modules.newequipment.NewEquipmentModule;
import com.stars.modules.newequipment.userdata.RoleEquipment;
import com.stars.modules.redpoint.RedPointConst;
import com.stars.modules.scene.fightdata.FighterCreator;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.serverLog.ServerLogModule;
import com.stars.modules.serverLog.ThemeType;
import com.stars.modules.serverLog.event.SpecialAccountEvent;
import com.stars.modules.tool.ToolModule;
import com.stars.modules.tool.packet.ClientAward;
import com.stars.multiserver.MainRpcHelper;
import com.stars.multiserver.MultiServerHelper;
import com.stars.network.server.packet.Packet;
import com.stars.network.server.packet.PacketManager;
import com.stars.util.LogUtil;
import com.stars.util.ServerLogConst;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * Created by zhouyaohui on 2016/11/7.
 */
public class FightingMasterModule extends AbstractModule {
    private byte fiveRewardStatus = FiveRewardStatusEvent.CAN_NOT_GET;

    public FightingMasterModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super("斗神殿", id, self, eventDispatcher, moduleMap);
    }

    @Override
    public void onInit(boolean isCreation) throws Throwable {
        try {
            // 检查排行榜奖励，不在排行榜单上面的上线发
            MainRpcHelper.fightingMasterService().checkRankAward(MultiServerHelper.getFightingMasterServer(), id(),
                    MultiServerHelper.getServerId());
        } catch (Exception e) {
//            LogUtil.error("can not connect to fighting master server.", e);
        }
    }

    @Override
    public void onDailyReset(Calendar now, boolean isLogin) throws Throwable {
        fiveRewardStatus = FiveRewardStatusEvent.CAN_NOT_GET;
        //红点检查
        signCalRedPoint(MConst.FightingMaster, RedPointConst.FIVE_FIGHT_REWARD);
    }

    @Override
    public void calRedPoint(List<Integer> redPointIds, Map<Integer, String> redPointMap) {
        if (redPointIds.contains(Integer.valueOf(RedPointConst.FIVE_FIGHT_REWARD))) {
            if (fiveRewardStatus == FiveRewardStatusEvent.CAN_GET) {
                redPointMap.put(RedPointConst.FIVE_FIGHT_REWARD, "");
            } else {
                redPointMap.put(RedPointConst.FIVE_FIGHT_REWARD, null);
            }
        }
    }

    /**
     * 进入斗神殿
     */
    public void enterFightingMaster() {
        BuddyModule bm = module(MConst.Buddy);
        RoleBuddy roleBuddy = bm.getRoleBuddy(bm.getFightBuddyId());

        FighterEntity buddy = null;
        if (roleBuddy != null) {
            buddy = FighterCreator.create(FighterEntity.TYPE_BUDDY, FighterEntity.CAMP_SELF,
                    roleBuddy);
        }

        //获取有效勋章id
        int validMedalId = -1;
        ForeShowModule foreShowModule = (ForeShowModule) module(MConst.ForeShow);
        if (foreShowModule.isOpen(ForeShowConst.MEDAL)) {
            NewEquipmentModule newEquipmentModule = (NewEquipmentModule) module(MConst.NewEquipment);
            RoleEquipment roleEquipment = newEquipmentModule.getRoleEquipByType(NewEquipmentConstant.MEDAL_EQUIPMENT_TYPE);
            if (roleEquipment != null) {
                validMedalId = roleEquipment.getEquipId();
            }
        }
        String familyName = "";
        FamilyModule familyModule = module(MConst.Family);
        if (familyModule != null && familyModule.getAuth() != null) {
            familyName = familyModule.getAuth().getFamilyName();
        }
        fireSpecialAccountLogEvent("进入斗神殿");
        MainRpcHelper.fightingMasterService().enterFightingMaster(MultiServerHelper.getFightingMasterServer(),
                MultiServerHelper.getServerId(), FighterCreator.createSelf(moduleMap()), buddy, validMedalId, familyName);
    }

    public void getFightCount() {
        LogUtil.info("=======请求战斗次数======");
        MainRpcHelper.fightingMasterService().getFightCount(MultiServerHelper.getFightingMasterServer(), MultiServerHelper.getServerId(), id());
    }

    /**
     * 进入斗神殿回调
     *
     * @param success
     * @param enterPacket
     */
    public void enterCallback(boolean success, Packet enterPacket) {
        if (success) {
            // 路由必须在enterfightingmaster 这个rpc回调回来且，保证且路由时根据roleid找得到serverid
            MultiServerHelper.modifyConnectorRoute(id(), MultiServerHelper.getFightingMasterServer());
            send(enterPacket);
            ServerLogModule log = (ServerLogModule) module(MConst.ServerLog);
            log.Log_core_activity(ServerLogConst.ACTIVITY_START, ThemeType.ACTIVITY_103.getThemeId(), 0);
        } else {
            // 进入失败，连接切回来
            MultiServerHelper.modifyConnectorRoute(id(), MultiServerHelper.getServerId());
            send(new ClientText("进入斗神殿失败"));
            ServerLogModule log = (ServerLogModule) module(MConst.ServerLog);
            log.Log_core_activity(ServerLogConst.ACTIVITY_FAIL, ThemeType.ACTIVITY_103.getThemeId(), 0);
        }
    }

    public void handleFiveRewardStatusEvent(FiveRewardStatusEvent event) {
        fiveRewardStatus = event.getStatus();
        //红点检查
        signCalRedPoint(MConst.FightingMaster, RedPointConst.FIVE_FIGHT_REWARD);
    }

    public void handleGetFiveRewardEvent(GetFiveRewardEvent event) {
        int groupId = event.getRewardGroupId();
        DropModule dropModule = (DropModule) module(MConst.Drop);
        Map<Integer, Integer> award = dropModule.executeDrop(groupId, 1, true);

        ClientFightingMaster packet = new ClientFightingMaster();
        packet.setResType(ClientFightingMaster.VIEW_FIVEAWARD);
        packet.setAward(award);
        PacketManager.send(id(), packet);

        ToolModule toolModule = (ToolModule) module(MConst.Tool);
        Map<Integer, Integer> map = toolModule.addAndSend(award, EventType.FIVE_AWARD.getCode());
        //发获奖提示到客户端
        ClientAward clientAward = new ClientAward(map);
        send(clientAward);
    }

    private void fireSpecialAccountLogEvent(String content) {
        if (SpecialAccountManager.isSpecialAccount(id())) {
            eventDispatcher().fire(new SpecialAccountEvent(id(), content, true));
        }
    }

    public void onRoleRename(RoleRenameEvent event) {
        String newName = event.getNewName();
        MainRpcHelper.fightingMasterService().updateRoleName(MultiServerHelper.getFightingMasterServer(), id(), newName);
    }
}
