package com.stars.modules.pk;

import com.stars.core.event.EventDispatcher;
import com.stars.core.gmpacket.specialaccount.SpecialAccountManager;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.player.PlayerUtil;
import com.stars.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.buddy.BuddyModule;
import com.stars.modules.daily5v5.Daily5v5Module;
import com.stars.modules.deityweapon.DeityWeaponModule;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.friend.FriendModule;
import com.stars.modules.friend.event.FriendLogEvent;
import com.stars.modules.newequipment.NewEquipmentModule;
import com.stars.modules.pk.event.InvitePkEvent;
import com.stars.modules.pk.event.PermitPkEvent;
import com.stars.modules.pk.event.UpdateReceiveInviteEvent;
import com.stars.modules.pk.packet.ClientPKOption;
import com.stars.modules.pk.userdata.InvitorCache;
import com.stars.modules.pk.userdata.RolePKRecord;
import com.stars.modules.role.RoleModule;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.SceneModule;
import com.stars.modules.scene.fightdata.FighterCreator;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.modules.scene.imp.city.FamilyScene;
import com.stars.modules.scene.imp.city.SafeCityScene;
import com.stars.modules.scene.packet.ClientStageFinish;
import com.stars.modules.scene.packet.clientEnterFight.ClientEnterPK;
import com.stars.modules.scene.prodata.StageinfoVo;
import com.stars.modules.serverLog.event.SpecialAccountEvent;
import com.stars.modules.skill.SkillModule;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.session.SessionManager;
import com.stars.services.ServiceHelper;
import com.stars.services.role.RoleNotification;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledByteBufAllocator;

import java.util.*;


public class PKModule extends AbstractModule {
    private Map<Long, RolePKRecord> roleRecordMap = new HashMap<>();// pk记录,<enemyId, record>
    private Map<Long, InvitorCache> receiveInviteMap = new HashMap<>();// 收到的邀请记录,<邀请者id, cache>

    public PKModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super(MConst.Pk, id, self, eventDispatcher, moduleMap);
    }

    @Override
    public void onInit(boolean isCreation) throws Throwable {
    }

    @Override
    public void onDataReq() throws Throwable {
        roleRecordMap = DBUtil.queryMap(DBUtil.DB_USER, "enemyid", RolePKRecord.class,
                "select * from `rolepkrecord` where `roleid`=" + id());
    }

    private List<FighterEntity> generateEntityList(List<FighterEntity> originList, byte camp) {
        StageinfoVo stageVo = SceneManager.getStageVo(PKManager.pkStageId);// 注入pk场景
        RoleModule rm = this.module(MConst.Role);
        SkillModule skillModule = this.module(MConst.Skill);
        DeityWeaponModule deityWeaponModule = module(MConst.Deity);
        // 阵营为1(self),使用第一个坐标/朝向
        String pos = stageVo.getEnemyPos(0);
        int rot = stageVo.getEnemyRot(0);
        // 阵营为2(enemy),使用第二个坐标/朝向
        if (camp == FighterEntity.CAMP_ENEMY) {
            pos = stageVo.getEnemyPos(1);
            rot = stageVo.getEnemyRot(1);
        }
        NewEquipmentModule equipmentModule = (NewEquipmentModule) moduleMap().get(MConst.NewEquipment);
        List <String> dragonBallIdsList = equipmentModule.getDragonBallIdList();
        FighterEntity entity = FighterCreator.create(FighterEntity.TYPE_PLAYER, camp, rm.getRoleRow(),
                pos, rot, skillModule.getUseSkill(), skillModule.getSkillDamageMap(), skillModule.getTrumpPassSkillAttr(),
                deityWeaponModule.getCurRoleDeityWeapoonId(),dragonBallIdsList);
        originList.add(entity);
        /* 出战伙伴 */
        BuddyModule buddyModule = this.module(MConst.Buddy);
        // 有出战伙伴
        if (buddyModule.getFightBuddyId() != 0) {
            originList.add(FighterCreator.create(FighterEntity.TYPE_BUDDY, camp,
                    buddyModule.getRoleBuddy(buddyModule.getFightBuddyId())));
        }
        return originList;
    }

    /**
     * 邀请别人pk
     *
     * @param invitee
     */
    public void invitePK(long invitee) {
        if (invitee == id()) {
            return;
        }
        // 对方不在线
        if (!com.stars.network.server.session.SessionManager.getSessionMap().containsKey(invitee)) {
            warn("personalpk_send_offline");
            return;
        }
        InvitePkEvent event = new InvitePkEvent();
        RoleModule roleModule = module(MConst.Role);
        // 构建邀请者信息
        InvitorCache invitorCache = new InvitorCache(id(), roleModule.getRoleRow().getName());
        event.setInvitorId(id());
        event.setInvitedId(invitee);
        event.setInvitorCache(invitorCache);
        ServiceHelper.roleService().notice(invitee, new RoleNotification(event));
        warn("personalpk_send_sendsuc");
        fireSpecialAccountLogEvent("邀请切磋");
        if (SpecialAccountManager.isSpecialAccount(invitee)) {
            eventDispatcher().fire(new SpecialAccountEvent(invitee, "被邀请切磋", true));
        }
    }

    private void fireSpecialAccountLogEvent(String content) {
        if (SpecialAccountManager.isSpecialAccount(id())) {
            eventDispatcher().fire(new SpecialAccountEvent(id(), content, true));
        }
    }

    /**
     * 收到pk邀请
     *
     * @param event
     */
    public void receiveInvite(InvitePkEvent event) {
        long invitor = event.getInvitorId();// 邀请者id
        SceneModule sceneModule = module(MConst.Scene);
//        // 不在安全区
//        if (!(sceneModule.getScene() instanceof SafeCityScene)) {
//            return;
//        }
        receiveInviteMap.put(invitor, event.getInvitorCache());
        // 通知受邀者收到邀请
        ClientPKOption packet = new ClientPKOption(ClientPKOption.NEW_INVITE);
        packet.setNewInvite(event.getInvitorCache());
        send(packet);
    }

    /**
     * 发送接收到的邀请列表
     */
    public void sendReceiveInvite() {
        updateReceiveInvite();
        // send to client
        ClientPKOption packet = new ClientPKOption(ClientPKOption.RECEIVE_INVITE_LIST);
        packet.setReceiveInviteMap(receiveInviteMap);
        send(packet);
    }

    /**
     * 发送切磋记录
     */
    public void sendPvpRecord() {
        ClientPKOption packet = new ClientPKOption(ClientPKOption.PVP_RECORD_LIST);
        packet.setRolePKRecordList(roleRecordMap.values());
        send(packet);
    }

    /**
     * 同意邀请
     *
     * @param invitor
     */
    public void permitInvite(long invitor) {
        InvitorCache invitorCache = receiveInviteMap.get(invitor);
        receiveInviteMap.remove(invitor);
        if (invitorCache == null) {
            // 发送最新列表
            sendReceiveInvite();
            return;
        }
        // 对方不在线
        if (!SessionManager.getSessionMap().containsKey(invitor)) {
            warn("personalpk_respond_offline");
            // 发送最新列表
            sendReceiveInvite();
            return;
        }
        // 邀请已过期
        if (System.currentTimeMillis() - invitorCache.getCreateTimestamp() > PKManager.inviteAvailableTime) {
            warn("personalpk_respond_timeout");
            // 发送最新列表
            sendReceiveInvite();
            return;
        }
        // 自己不在安全区
        SceneModule sceneModule = module(MConst.Scene);
        if (!(sceneModule.getScene() instanceof SafeCityScene) && !(sceneModule.getScene() instanceof FamilyScene)) {
            PlayerUtil.send(invitor, new ClientText("personalpk_respond_battleing"));
            // 发送最新列表
            sendReceiveInvite();
            return;
        }
        // 被邀请者的数据发给邀请者
        List<FighterEntity> entityList = new LinkedList<>();
        generateEntityList(entityList, FighterEntity.CAMP_SELF);
        PermitPkEvent event = new PermitPkEvent(id(), entityList);
        ServiceHelper.roleService().notice(invitor, new RoleNotification(event));
        fireSpecialAccountLogEvent("同意切磋");
        if (SpecialAccountManager.isSpecialAccount(invitor)) {
            eventDispatcher().fire(new SpecialAccountEvent(invitor, "同意" + invitor + "的切磋", true));
        }
        FriendModule friendModule = module(MConst.Friend);
        if(friendModule.isFriend(invitor)){
        	FriendLogEvent friendLogEvent = new FriendLogEvent(FriendLogEvent.FIGHT);
        	friendLogEvent.setFriendId(invitor);
        	friendLogEvent.setState((byte)1);
        	eventDispatcher().fire(friendLogEvent);
        }
    }

    /**
     * 拒绝所有邀请
     */
    public void refuseAll() {
        for (long invitor : receiveInviteMap.keySet()) {
            refuseInvite(invitor, Boolean.FALSE);
        }
        warn("已拒绝所有请求");
        // 发送最新列表
        sendReceiveInvite();
        fireSpecialAccountLogEvent("拒绝切磋");
    }

    /**
     * 拒绝邀请
     *
     * @param invitor
     */
    public void refuseInvite(long invitor, boolean notice) {
        InvitorCache invitorCache = receiveInviteMap.get(invitor);
        receiveInviteMap.remove(invitor);
        if (invitorCache == null) {
            return;
        }
        // 邀请已过期
        if (System.currentTimeMillis() - invitorCache.getCreateTimestamp() > PKManager.inviteAvailableTime) {
            if (notice) {
                warn("personalpk_respond_timeout");
            }
            return;
        }
        if (notice) {
            send(new ClientText("personalpk_respond_refuse", invitorCache.getInvitorName()));
        }
        // 通知邀请者
        RoleModule roleModule = module(MConst.Role);
        PlayerUtil.send(invitor, new ClientText("personalpk_send_refused", roleModule.getRoleRow().getName()));
        fireSpecialAccountLogEvent("拒绝切磋");
        if (SpecialAccountManager.isSpecialAccount(invitor)) {
            eventDispatcher().fire(new SpecialAccountEvent(invitor, "拒绝" + invitor + "的切磋", true));
        }
        FriendModule friendModule = module(MConst.Friend);
        if(friendModule.isFriend(invitor)){
        	FriendLogEvent friendLogEvent = new FriendLogEvent(FriendLogEvent.FIGHT);
        	friendLogEvent.setFriendId(invitor);
        	friendLogEvent.setState((byte)2);
        	eventDispatcher().fire(friendLogEvent);
        }
    }

    /**
     * 收到同意邀请,判断自身状态,进入pvp战斗
     *
     * @param invitee
     * @param entityList
     */
    public void receivePermit(long invitee, List<FighterEntity> entityList) {
        SceneModule sceneModule = module(MConst.Scene);
        // 不在安全区
        if (!(sceneModule.getScene() instanceof SafeCityScene) && !(sceneModule.getScene() instanceof FamilyScene)) {
            PlayerUtil.send(invitee, new ClientText("personalpk_respond_battleing"));
            // 通知受邀者更新列表给客户端
            ServiceHelper.roleService().notice(invitee, new UpdateReceiveInviteEvent());
            return;
        }
        Daily5v5Module daily5v5Module = module(MConst.Daily5v5);
        if(daily5v5Module.chckIsMaching()){
        	PlayerUtil.send(invitee, new ClientText("personalpk_respond_battleing"));
        	ServiceHelper.roleService().notice(invitee, new UpdateReceiveInviteEvent());
        	return;
        }
        List<FighterEntity> selfEntity = new LinkedList<>();
        generateEntityList(selfEntity, FighterEntity.CAMP_ENEMY);
        entityList.addAll(selfEntity);
        // 构建pvp数据包
        ClientEnterPK enterPack = new ClientEnterPK();
        enterPack.setFightType(SceneManager.SCENETYPE_1V1PK);
        enterPack.setStageId(PKManager.pkStageId);
        // 限制时间
        enterPack.setLimitTime(
                SceneManager.getStageVo(PKManager.pkStageId).getVictoryConMap().get(SceneManager.VICTORY_CONDITION_TIME) / 1000);
        enterPack.setFighterEntityList(entityList);
        com.stars.network.server.buffer.NewByteBuffer buffer = new com.stars.network.server.buffer.NewByteBuffer(Unpooled.buffer());
        enterPack.writeToBuffer(buffer);
        byte[] bytes = new byte[buffer.getBuff().readableBytes()];
        buffer.getBuff().readBytes(bytes);
        buffer.getBuff().release();
//        ServiceHelper.pkService().startPVP(id(), invitee, bytes);

        ServiceHelper.pkService().startPvp(id(), invitee, enterPack);
        receiveInviteMap.clear();
    }

    /**
     * 进入pvp
     *
     * @param data
     */
    public void enterPk(byte[] data) {
        RoleModule rm = module(MConst.Role);
        rm.sendRoleAttr();
        SceneModule sm = this.module(MConst.Scene);
        com.stars.network.server.buffer.NewByteBuffer buffer = new NewByteBuffer(UnpooledByteBufAllocator.DEFAULT.buffer());
        buffer.getBuff().writeBytes(data);
        ClientEnterPK enterPK = new ClientEnterPK();
        enterPK.readFromBuffer(buffer);
        // 限制时间
        enterPK.setLimitTime(
                SceneManager.getStageVo(PKManager.pkStageId).getVictoryConMap().get(SceneManager.VICTORY_CONDITION_TIME) / 1000);
        buffer.getBuff().release();
        sm.enterScene(enterPK.getFightType(), enterPK.getStageId(), enterPK);
    }

    /**
     * pvp结束
     *
     * @param result
     * @param enemyRoleId
     */
    public void finishPk(byte result, long enemyRoleId) {
        addUpdatePvpRecord(result, enemyRoleId);
        ClientStageFinish packet = new ClientStageFinish(SceneManager.SCENETYPE_1V1PK, result);
        send(packet);
    }

    /**
     * 更新接收到的邀请
     * 1.判断是否超时
     */
    private void updateReceiveInvite() {
        Iterator<InvitorCache> iterator = receiveInviteMap.values().iterator();
        while (iterator.hasNext()) {
            InvitorCache invitorCache = iterator.next();
            if (System.currentTimeMillis() - invitorCache.getCreateTimestamp() > PKManager.inviteAvailableTime) {
                iterator.remove();
            }
        }
    }

    /**
     * pvp结果加入记录
     *
     * @param result
     * @param enemyRoleId
     */
    private void addUpdatePvpRecord(byte result, long enemyRoleId) {
        RolePKRecord rolePKRecord = roleRecordMap.get(enemyRoleId);
        // 对手有记录,更新
        if (rolePKRecord != null) {
            rolePKRecord.setResult(result);
            rolePKRecord.setCreateTimestamp(System.currentTimeMillis());
            context().update(rolePKRecord);
        } else {// 新增
            rolePKRecord = new RolePKRecord(id(), enemyRoleId, result);
            if (roleRecordMap.size() >= PKManager.recordMax) {
                List<RolePKRecord> list = new LinkedList<>();
                list.addAll(roleRecordMap.values());
                Collections.sort(list);
                RolePKRecord deleteRecord = list.get(0);
                roleRecordMap.remove(deleteRecord.getEnemyId());
                context().delete(deleteRecord);
            }
            roleRecordMap.put(rolePKRecord.getEnemyId(), rolePKRecord);
            context().insert(rolePKRecord);
        }
    }
}
