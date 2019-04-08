package com.stars.modules.activeweapon;

import com.google.common.collect.Maps;
import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.activeweapon.handler.ConditionHandler;
import com.stars.modules.activeweapon.handler.ConditionHandlerFactory;
import com.stars.modules.activeweapon.packet.ClientActiveWeaponPacket;
import com.stars.modules.activeweapon.prodata.ActiveWeaponVo;
import com.stars.modules.activeweapon.usrdata.RoleActiveWeapon;
import com.stars.modules.drop.DropModule;
import com.stars.modules.operateactivity.OpActivityModule;
import com.stars.modules.operateactivity.OperateActivityConstant;
import com.stars.modules.operateactivity.OperateActivityManager;
import com.stars.modules.operateactivity.OperateActivityModule;
import com.stars.modules.operateactivity.prodata.OperateActVo;
import com.stars.modules.redpoint.RedPointConst;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.tool.ToolModule;
import com.stars.modules.tool.packet.ClientAward;
import com.stars.util.DateUtil;
import com.stars.util.StringUtil;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * Created by huwenjun on 2017/6/15.
 */
public class ActiveWeaponModule extends AbstractModule implements OpActivityModule {
    RoleActiveWeapon roleActiveWeapon;

    public ActiveWeaponModule(String name, long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super(name, id, self, eventDispatcher, moduleMap);
    }

    @Override
    public void onDataReq() throws Throwable {
        String sql = "select * from roleactiveweapon where roleid=%s;";
        roleActiveWeapon = DBUtil.queryBean(DBUtil.DB_USER, RoleActiveWeapon.class, String.format(sql, id()));
    }


    @Override
    public void onInit(boolean isCreation) throws Throwable {
        if (roleActiveWeapon == null) {
            roleActiveWeapon = new RoleActiveWeapon(id(), "", "");
            Map<Integer, Byte> rewardRecord = Maps.newHashMap();
            for (Map.Entry<Integer, ActiveWeaponVo> entry : ActiveWeaponManager.activeWeaponVoMap.entrySet()) {
                rewardRecord.put(entry.getKey(), (byte) -1);
            }
            roleActiveWeapon.setRewardRecord(rewardRecord);
            context().insert(roleActiveWeapon);
        }
        /**
         * 下面是为了避免线上部分玩家数据没有正常初始化，新角色不会出现此问题
         */
        if (StringUtil.isEmpty(roleActiveWeapon.getReward())) {
            Map<Integer, Byte> rewardRecord = Maps.newHashMap();
            for (Map.Entry<Integer, ActiveWeaponVo> entry : ActiveWeaponManager.activeWeaponVoMap.entrySet()) {
                rewardRecord.put(entry.getKey(), (byte) -1);
            }
            roleActiveWeapon.setRewardRecord(rewardRecord);
            context().update(roleActiveWeapon);
        }
        if (getCurShowActivityId() != -1) {
            String todayDateStr = DateUtil.getYMD_Str();
            roleActiveWeapon.addOnlineDay(todayDateStr);
            context().update(roleActiveWeapon);
            checkState();
        }

    }

    @Override
    public void onDailyReset(Calendar now, boolean isLogin) throws Throwable {
        int curShowActivityId = getCurShowActivityId();
        if (curShowActivityId != -1) {
            String todayDateStr = DateUtil.getYMD_Str();
            roleActiveWeapon.addOnlineDay(todayDateStr);
            context().update(roleActiveWeapon);
            checkState();
        }
    }

    /**
     * 活动重置逻辑(无需重置，谨慎使用)
     */
    public void onActivityReset() {
        roleActiveWeapon.setOnlineDays("");
        roleActiveWeapon.setReward("");
        context().update(roleActiveWeapon);
    }

    /**
     * 检测角色奖励状态
     */
    public void checkState() {
        for (Map.Entry<Integer, ActiveWeaponVo> entry : ActiveWeaponManager.activeWeaponVoMap.entrySet()) {
            Map<Integer, Byte> rewardRecord = roleActiveWeapon.getRewardRecord();
            if (rewardRecord.get(entry.getKey()) != 0) {
                ActiveWeaponVo activeWeaponVo = entry.getValue();
                ConditionHandler conditionHandler = ConditionHandlerFactory.newConditionHandler(activeWeaponVo.getType(), moduleMap(), activeWeaponVo);
                if (conditionHandler.check()) {
                    roleActiveWeapon.updateRewardRecord(entry.getKey(), (byte) 1);
                    signCalRedPoint(MConst.ActiveWeapon, RedPointConst.ACTIVE_WEAPON);
                }
            }
        }
        context().update(roleActiveWeapon);
    }

    /**
     * 领奖
     *
     * @param conditionId
     */
    public void takeReward(Integer conditionId) {
        Map<Integer, Byte> rewardRecord = roleActiveWeapon.getRewardRecord();
        Byte state = rewardRecord.get(conditionId);
        if (state == 1) {
            ActiveWeaponVo activeWeaponVo = ActiveWeaponManager.activeWeaponVoMap.get(conditionId);
            Integer groupId = activeWeaponVo.getReward();
            DropModule dropModule = module(MConst.Drop);
            Map<Integer, Integer> dropMap = dropModule.executeDrop(groupId, 1, false);
            ToolModule toolModule = module(MConst.Tool);
            toolModule.addAndSend(dropMap, EventType.ACTIVE_WEAPON.getCode());
            roleActiveWeapon.updateRewardRecord(conditionId, (byte) 0);
            ClientAward clientAward = new ClientAward(dropMap);
            clientAward.setType((byte) 1);
            toolModule.sendPacket(clientAward);
            sendTakedRecord();
            context().update(roleActiveWeapon);
            signCalRedPoint(MConst.ActiveWeapon, RedPointConst.ACTIVE_WEAPON);
        } else {
            warn("条件不满足");
        }
    }

    @Override
    public int getCurShowActivityId() {
        int curActivityId = OperateActivityManager.getCurActivityId(OperateActivityConstant.ActType_ActiveWeapon);
        if (curActivityId != -1) {
            OperateActVo operateActVo = OperateActivityManager.getOperateActVo(curActivityId);
            OperateActivityModule operateActivityModule = (OperateActivityModule) module(MConst.OperateActivity);
            boolean show = operateActivityModule.isShow(operateActVo.getRoleLimitMap());
            if (show) {
                if (roleActiveWeapon != null) {
                    Map<Integer, Byte> rewardRecord = roleActiveWeapon.getRewardRecord();
                    boolean complete = true;
                    for (Map.Entry<Integer, Byte> entry : rewardRecord.entrySet()) {
                        if (entry.getValue() != 0) {
                            complete = false;
                            break;
                        }
                    }
                    if (complete && !rewardRecord.isEmpty()) {
                        return -1;
                    }
                }
                return curActivityId;
            } else {
                return -1;
            }

        }
        return curActivityId;
    }


    @Override
    public byte getIsShowLabel() {
        return 0;
    }

    @Override
    public void calRedPoint(List<Integer> redPointIds, Map<Integer, String> redPointMap) {
        Map<Integer, Byte> rewardRecord = roleActiveWeapon.getRewardRecord();
        boolean canTaked = false;
        for (Byte state : rewardRecord.values()) {
            if (state == 1) {
                canTaked = true;
                break;
            }
        }
        if (canTaked) {
            redPointMap.put(RedPointConst.ACTIVE_WEAPON, "");
        } else {
            redPointMap.put(RedPointConst.ACTIVE_WEAPON, null);
        }
    }

    /**
     * 下发活跃神兵产品数据
     */
    public void sendActiveWeaponList() {
        ClientActiveWeaponPacket clientActiveWeaponPacket = new ClientActiveWeaponPacket(ClientActiveWeaponPacket.SEND_ACTIVE_WEAPON_LIST);
        clientActiveWeaponPacket.setRewardRecordMap(roleActiveWeapon.getRewardRecord());
        send(clientActiveWeaponPacket);
    }

    /**
     * 下发个人领奖记录
     * state：Byte:
     * 1:可领取
     * 0：已领取
     * -1:未满足条件
     */
    public void sendTakedRecord() {
        Map<Integer, Byte> rewardRecord = roleActiveWeapon.getRewardRecord();
        ClientActiveWeaponPacket clientActiveWeaponPacket = new ClientActiveWeaponPacket(ClientActiveWeaponPacket.SEND_TAKE_REWARD_RECORD);
        clientActiveWeaponPacket.setRewardRecordMap(rewardRecord);
        send(clientActiveWeaponPacket);
    }

    public RoleActiveWeapon getRoleActiveWeapon() {
        return roleActiveWeapon;
    }
}
