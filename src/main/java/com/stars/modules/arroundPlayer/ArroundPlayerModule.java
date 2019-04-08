package com.stars.modules.arroundPlayer;

import com.stars.core.event.EventDispatcher;
import com.stars.core.gmpacket.specialaccount.SpecialAccountManager;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.modules.MConst;
import com.stars.modules.baby.BabyModule;
import com.stars.modules.baby.event.BabyFashionChangeEvent;
import com.stars.modules.deityweapon.DeityWeaponModule;
import com.stars.modules.deityweapon.event.DeityWeaponChangeEvent;
import com.stars.modules.demologin.LoginModule;
import com.stars.modules.family.FamilyModule;
import com.stars.modules.fashion.FashionModule;
import com.stars.modules.fashion.event.FashionChangeEvent;
import com.stars.modules.name.event.RoleRenameEvent;
import com.stars.modules.newequipment.NewEquipmentModule;
import com.stars.modules.newequipment.event.DragonBallChangeEvent;
import com.stars.modules.ride.RideModule;
import com.stars.modules.ride.event.RideChangeEvent;
import com.stars.modules.role.RoleModule;
import com.stars.modules.scene.ArroundScene;
import com.stars.modules.scene.Scene;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.SceneModule;
import com.stars.modules.scene.prodata.SafeinfoVo;
import com.stars.modules.vip.VipModule;
import com.stars.modules.vip.event.VipLevelupEvent;
import com.stars.services.ServiceHelper;
import com.stars.util.LogUtil;
import com.stars.util.StringUtil;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2016/8/12.
 */
public class ArroundPlayerModule extends AbstractModule {

    private int[] position;

    private long lastSyncArroundPlayer = 0;

    public ArroundPlayerModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super("位置同步", id, self, eventDispatcher, moduleMap);
    }

    @Override
    public void onInit(boolean isCreation) throws Throwable {
        RoleModule roleModule = (RoleModule) module(MConst.Role);
        if (SceneManager.getSafeVo(roleModule.getSafeStageId()) == null) {
            /** 找不到安全区的产品配置，强制回到初始安全区 */
            roleModule.updateSafeStageId(SceneManager.initSafeStageId);
        }
        this.position = getRolePosition();
    }


    @Override
    public void onCreation(String name, String account) throws Throwable {

    }

    @Override
    public void onOffline() throws Throwable {
        RoleModule roleModule = (RoleModule) module(MConst.Role);
        ServiceHelper.arroundPlayerService().removeArroundPlayer(roleModule.getJoinSceneStr(), id());
    }

    @Override
    public void onSyncData() throws Throwable {
        if (!SpecialAccountManager.isSpecialAccount(id())) {
            ServiceHelper.arroundPlayerService().addArroundPlayer(newArroundPlayer());
        }
    }

    @Override
    public void onExit() throws Throwable {
        RoleModule roleModule = (RoleModule) module(MConst.Role);
        ServiceHelper.arroundPlayerService().removeArroundPlayer(roleModule.getJoinSceneStr(), id());
    }

    public void onReconnect() throws Throwable {
        if (!SpecialAccountManager.isSpecialAccount(id())) {
            ServiceHelper.arroundPlayerService().addArroundPlayer(newArroundPlayer());
        }
    }

    private int[] getRolePosition() {
        int p[] = new int[3];
        RoleModule roleModule = (RoleModule) module(MConst.Role);
        SafeinfoVo sf = SceneManager.getSafeVo(roleModule.getSafeStageId());
        String[] s = sf.getCharPosition().split("[+]");
        p[0] = Integer.parseInt(s[0]);
        p[1] = Integer.parseInt(s[1]);
        p[2] = Integer.parseInt(s[2]);
        return p;
    }

    public void heartbeat(int sceneId, int x, int y, int z) {
        if (SpecialAccountManager.isSpecialAccount(id())) {
            return;
        }
        // 设置心跳时间戳
        LoginModule loginModule = module(MConst.Login);
        loginModule.setHeartbeatTimestamp(System.currentTimeMillis());

        if (sceneId == 0) {
            return;
        }
        SceneModule sm = (SceneModule) module(MConst.Scene);
        RoleModule roleModule = (RoleModule) module(MConst.Role);
        if ((sm.getScene() instanceof ArroundScene) == false) {
            return;
        }

        int[] tmpP = new int[3];
        tmpP[0] = x;
        tmpP[1] = y;
        tmpP[2] = z;
        try {
            //城镇场景更新玩家当前坐标
            ArroundScene arroundScene = (ArroundScene) sm.getScene();
            arroundScene.updatePositionByClient(tmpP, sceneId, -1);
        } catch (Throwable e) {
            LogUtil.error(e.getMessage(), e);
        }

        if (sm.getScene().getSceneId() != sceneId) {
            /** 已经切换场景，之前场景的心跳包忽略 */
            return;
        }
        // 更新坐标
        if (this.position[0] != x || this.position[1] != y || this.position[2] != z) {
            this.position[0] = x;
            this.position[1] = y;
            this.position[2] = z;
            ServiceHelper.arroundPlayerService().updatePosition(
                    roleModule.getJoinSceneStr(), id(), this.position);
        }

        /** 安全区，更新角色位置,不积极更新，依赖于其他字段的更新保存*/
        int[] p = roleModule.getRoleRow().getPosition();
        p[0] = position[0];
        p[1] = position[1];
        p[2] = position[2];

        long curTime = System.currentTimeMillis();
        if (lastSyncArroundPlayer == 0 || curTime - lastSyncArroundPlayer > 5000) {
            ServiceHelper.arroundPlayerService().flushArroundPlayers(
                    roleModule.getJoinSceneStr(), id(), sm.getScene().getSceneMsg());
            lastSyncArroundPlayer = curTime;
        }
    }

    public ArroundPlayer newArroundPlayer() {
        RoleModule roleModule = (RoleModule) module(MConst.Role);
        RideModule rideModule = (RideModule) module(MConst.Ride);
        SceneModule sceneModule = (SceneModule) module(MConst.Scene);
        NewEquipmentModule newEquipmentModule = (NewEquipmentModule) module(MConst.NewEquipment);
        FashionModule fashionModule = module(MConst.Fashion);
        DeityWeaponModule deityWeaponModule = (DeityWeaponModule) module(MConst.Deity);
        FamilyModule fModule = module(MConst.Family);
        BabyModule baby = module(MConst.Baby);
        ArroundPlayer p = new ArroundPlayer();
        p.setRoleId(id());
        p.setLevel((short) roleModule.getLevel());
        p.setName(roleModule.getRoleRow().getName());
        p.setJob(roleModule.getRoleRow().getJobId());
        p.setSceneId(roleModule.getJoinSceneStr());
        p.setOriginSceneId(String.valueOf(sceneModule.getScene().getSceneId()));
        p.setArroundId(roleModule.getArroundId());
        p.setSceneType(sceneModule.getScene().getSceneType());
        p.setActiveRideId(rideModule.getActiveRideId());
        p.setX(position[0]);
        p.setFightScore(roleModule.getFightScore());
        p.setY(position[1]);
        p.setZ(position[2]);
        p.setCurFashionId(fashionModule.getDressFashionId());
        p.setDeityweaponType(deityWeaponModule.getCurRoleDeityWeaponType());
        p.setCurTitleId(roleModule.getTitleId());
        VipModule vipModule = module(MConst.Vip);
        p.setCutVipLevel(vipModule.getVipLevel());
        p.setFamilyId(String.valueOf(fModule.getAuth().getFamilyId()));
        p.setDragonBallList(newEquipmentModule.getDragonBallIdList());
        p.setBabyFollow(baby.isBabyFollow());
        p.setBabyCurFashionId(baby.getBabyFashion());
        p.setCurFashionCardId(roleModule.getRoleRow().getCurFashionCardId());
        return p;
    }

    public void doEnterSceneEvent(byte sceneType, String sceneId, byte lastSceneType, String lastSecenId) {
        if (SpecialAccountManager.isSpecialAccount(id())) {
            return;
        }
        Scene curScene = SceneManager.newScene(sceneType);
        Scene preScene = null;
        if (lastSceneType != -1) {
            preScene = SceneManager.newScene(lastSceneType);
        }
        if (preScene != null && preScene instanceof ArroundScene) {
            ServiceHelper.arroundPlayerService().removeArroundPlayer(lastSecenId, id());
        }
        if (curScene instanceof ArroundScene) {
            if (StringUtil.isEmpty(sceneId)) {
                return;
            }
//            LogUtil.info("------------------------>"+((RoleModule)module("role")).getArroundId());
            ServiceHelper.arroundPlayerService().addArroundPlayer(newArroundPlayer());
        }
    }

    public void doRideChangeEvent(RideChangeEvent event) {
        if (SpecialAccountManager.isSpecialAccount(id())) {
            return;
        }
        RoleModule roleModule = (RoleModule) module(MConst.Role);
        ServiceHelper.arroundPlayerService().updateActiveRideId(
                roleModule.getJoinSceneStr(), id(), event.getCurrActiveRideId());
    }

    public void doFashionChangeEvent(FashionChangeEvent event) {
        if (SpecialAccountManager.isSpecialAccount(id())) {
            return;
        }
        RoleModule roleModule = (RoleModule) module(MConst.Role);
        ServiceHelper.arroundPlayerService().updateCurFashionId(
                roleModule.getJoinSceneStr(), id(), event.getCurFashionId());
    }

    public void doBabyFashionChangeEvent(BabyFashionChangeEvent event) {
        if (SpecialAccountManager.isSpecialAccount(id())) {
            return;
        }
        RoleModule roleModule = (RoleModule) module(MConst.Role);
        ServiceHelper.arroundPlayerService().updateBabyCurFashionId(
                roleModule.getJoinSceneStr(), id(), event.getCurFashionId());
    }

    public void doDragonBallChangeEvent(DragonBallChangeEvent event) {
        if (SpecialAccountManager.isSpecialAccount(id())) {
            return;
        }
        RoleModule roleModule = (RoleModule) module(MConst.Role);
        ServiceHelper.arroundPlayerService().updateDragonBallList(
                roleModule.getJoinSceneStr(), id(), event.getDragonBallList());

    }

    public void doVipLevelUpEvent(VipLevelupEvent event) {
        if (SpecialAccountManager.isSpecialAccount(id())) {
            return;
        }
        RoleModule roleModule = module(MConst.Role);
        if (SpecialAccountManager.isSpecialAccount(id())) {
            return;
        }
        ServiceHelper.arroundPlayerService().updateCurVipLevel(
                roleModule.getJoinSceneStr(), id(), event.getNewVipLevel());
    }

    public void doDeityWeaponChangeEvent(DeityWeaponChangeEvent event) {
        if (SpecialAccountManager.isSpecialAccount(id())) {
            return;
        }
        RoleModule roleModule = (RoleModule) module(MConst.Role);
        ServiceHelper.arroundPlayerService().updateCurDeityWeaponType(
                roleModule.getJoinSceneStr(), id(), event.getCurDressdeityweaponType());
    }

    public int[] getPosition() {
        return position;
    }

    /**
     * 切换场景需要先这是位置，防止其他玩家刷新到的位置
     * 是该玩家在上一个场景中的位置导致位置和场景不同步
     *
     * @param p
     */
    public void setPosition(String p) {
        if (StringUtil.isEmpty(p)) {
            return;
        }
        String[] strp = p.split("[+]");
        position[0] = Integer.valueOf(strp[0]);
        position[1] = Integer.valueOf(strp[1]);
        position[2] = Integer.valueOf(strp[2]);
    }

    public void onRoleRename(RoleRenameEvent event) {
        RoleModule roleModule = (RoleModule) module(MConst.Role);
        String sceneId = roleModule.getJoinSceneStr();
        ServiceHelper.arroundPlayerService().updateRoleRename(id(), sceneId, event.getNewName());
    }
}
