package com.stars.modules.achievement;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.db.DBUtil;
import com.stars.modules.achievement.event.JoinActivityEvent;
import com.stars.modules.achievement.gm.ChangeAchieveRankCountGmHandler;
import com.stars.modules.achievement.handler.*;
import com.stars.modules.achievement.listener.*;
import com.stars.modules.achievement.prodata.AchievementStageVo;
import com.stars.modules.achievement.prodata.AchievementVo;
import com.stars.modules.book.event.BookAchieveEvent;
import com.stars.modules.buddy.event.BuddyAchieveEvent;
import com.stars.modules.changejob.event.ChangeJobAchieveEvent;
import com.stars.modules.daily.event.DailyFuntionEvent;
import com.stars.modules.daily5v5.event.Daily5v5AchieveEvent;
import com.stars.modules.deityweapon.event.ActiveDeityWeaponAchieveEvent;
import com.stars.modules.demologin.event.LoginSuccessEvent;
import com.stars.modules.dungeon.event.ChapterStarAchieveEvent;
import com.stars.modules.elitedungeon.event.EliteDungonAchieveEvent;
import com.stars.modules.family.event.FamilyAuthAchieveEvent;
import com.stars.modules.friend.event.FriendAchieveEvent;
import com.stars.modules.gem.event.GemEmbedAchievementEvent;
import com.stars.modules.gm.GmManager;
import com.stars.modules.guest.event.GuestAchieveEvent;
import com.stars.modules.name.event.RoleRenameEvent;
import com.stars.modules.newequipment.event.EquipChangeAchieveEvent;
import com.stars.modules.newequipment.event.EquipExtAttrAchieveEvent;
import com.stars.modules.newequipment.event.EquipStarAchieveEvent;
import com.stars.modules.newequipment.event.EquipStrengthAchieveEvent;
import com.stars.modules.newsignin.event.SigninAchieveEvent;
import com.stars.modules.ride.event.RideAchieveEvent;
import com.stars.modules.role.event.FightScoreAchieveEvent;
import com.stars.modules.role.event.FightScoreChangeEvent;
import com.stars.modules.role.event.RoleLevelAchieveEvent;
import com.stars.modules.scene.event.PassStageEvent;
import com.stars.modules.skill.event.SkillLevelAchieveEvent;
import com.stars.modules.title.event.TitleAchieveEvent;
import com.stars.modules.tool.event.AddToolEvent;
import com.stars.modules.trump.event.TrumpAchieveEvent;
import com.stars.util.LogUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by zhouyaohui on 2016/10/17.
 */
public class AchievementModuleFactory extends AbstractModuleFactory {

    private static ConcurrentMap<Integer, Class<? extends AchievementHandler>> handleMap = new ConcurrentHashMap<>();

    public AchievementModuleFactory() {
        super(new AchievementPacketSet());
    }

    @Override
    public void loadProductData() throws Exception {
        ConcurrentMap<Integer, AchievementVo> achievementVo = DBUtil.queryConcurrentMap(DBUtil.DB_PRODUCT, "achievementid", AchievementVo.class, "select * from achievement");
        ConcurrentMap<Integer, ConcurrentMap<Integer, AchievementVo>> typeMap = new ConcurrentHashMap<>();
        for (AchievementVo vo : achievementVo.values()) {
            if (!typeMap.containsKey(vo.getType())) {
                typeMap.put(vo.getType(), new ConcurrentHashMap<Integer, AchievementVo>());
            }
            ConcurrentMap<Integer, AchievementVo> map = typeMap.get(vo.getType());
            map.put(vo.getAchievementId(), vo);
        }

        AchievementManager.achievementVo = achievementVo;
        AchievementManager.typeMap = typeMap;

        String sql = "select * from achievementstage";
        Map<Integer, AchievementStageVo> achievementStageVoMap = DBUtil.queryMap(DBUtil.DB_PRODUCT,"stage",AchievementStageVo.class,sql);
        AchievementManager.setAchievementStageVoMap(achievementStageVoMap);
    }

    @Override
    public void init() throws Exception {
        GmManager.reg("achieverankcount", new ChangeAchieveRankCountGmHandler());
        register(AchievementHandler.TYPE_TOOL, AchieveToolHandler.class);   // 道具
        register(AchievementHandler.TYPE_ACTIVITY, AchieveActivityHandler.class);   // 活动
        register(AchievementHandler.TYPE_BUDDY, AchieveBuddyHandler.class); // 伙伴
        register(AchievementHandler.TYPE_RIDE, AchieveRideHandler.class);   // 坐骑
        register(AchievementHandler.TYPE_DEITY, AchieveDeityHandler.class);   // 神兵
        register(AchievementHandler.TYPE_EQUIP, AchievementEquipHandler.class); // 装备
        register(AchievementHandler.TYPE_GEM, AchieveGemHandler.class); // 宝石
        register(AchievementHandler.TYPE_PART, AchievePartHandler.class);   // 部位
        register(AchievementHandler.TYPE_ROLELEVEL, AchieveRoleLevelHandler.class); // 角色等级
        register(AchievementHandler.TYPE_FAMILY, AchieveFamilyHandler.class);   // 家族
        register(AchievementHandler.TYPE_SKILL, AchieveSkillHandler.class); // 技能
        register(AchievementHandler.TYPE_FIGHTING, AchieveFightingHandler.class);   // 战力
        register(AchievementHandler.TYPE_SWEEP, AchieveSweepHandler.class); // 扫荡
        register(AchievementHandler.TYPE_LOGIN, AchieveLoginHandler.class); // 连续登陆
        register(AchievementHandler.TYPE_DUNGEON, AchieveDungeonHandler.class); // 关卡
        register(AchievementHandler.TYPE_CHAPTER, AchieveChaptHandler.class);   // 章节
        register(AchievementHandler.TYPE_WASH, AchieveWashHandler.class);
        register(AchievementHandler.TYPE_FRIEND, AchieveFriendHandler.class);   //好友
        register(AchievementHandler.TYPE_GUEST, AchieveGuestHandler.class);     //门客
        register(AchievementHandler.TYPE_TRUMP, AchieveTrumpHandler.class); //法宝
        register(AchievementHandler.TYPE_BOOK, AchieveBookHandler.class); //典籍
        register(AchievementHandler.TYPE_DAILY5V5, AchieveDaily5v5Handlder.class); //日常5v5(函谷战场)
        register(AchievementHandler.TYPE_ELITE_DUNGEON, AchieveEliteDungeonHandler.class); //精英战场
        register(AchievementHandler.TYPE_SIGNIN, AchieveSigninHandler.class);  //签到
        register(AchievementHandler.TYPE_TITLE, AchieveTitleHandler.class); //称号
        register(AchievementHandler.TYPE_CHANGEJOB, AchieveChangeJobHandler.class); //转职
    }

    private void register(int type, Class<? extends AchievementHandler> handler) {
        if (handleMap.containsKey(type)) {
            throw new IllegalArgumentException("成就类型已经注册:" + type);
        }
        handleMap.put(type, handler);
    }

    @Override
    public Module newModule(long id, Player self, EventDispatcher eventDispatcher, Map map) {
        return new AchievementModule(id, self, eventDispatcher, map);
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
        eventDispatcher.reg(RoleRenameEvent.class, new RoleRenameListener(module));
        eventDispatcher.reg(FightScoreChangeEvent.class, new RoleFightChangeListener(module));
        eventDispatcher.reg(AddToolEvent.class, new AchieveToolListener(module));
        eventDispatcher.reg(JoinActivityEvent.class, new AchieveActivityListener(module));
        eventDispatcher.reg(RideAchieveEvent.class, new AchieveRideListener(module));
        eventDispatcher.reg(BuddyAchieveEvent.class, new AchieveBuddyListener(module));
        eventDispatcher.reg(EquipChangeAchieveEvent.class, new AchieveEquipListener(module));
        eventDispatcher.reg(RoleLevelAchieveEvent.class, new AchieveRoleLevelListener(module));
        eventDispatcher.reg(FamilyAuthAchieveEvent.class, new AchieveFamilyListener(module));
        eventDispatcher.reg(FightScoreAchieveEvent.class, new AchieveFightingListener(module));
        eventDispatcher.reg(DailyFuntionEvent.class, new AchieveSweepListener(module));
        eventDispatcher.reg(LoginSuccessEvent.class, new AchieveLoginListener(module));
        eventDispatcher.reg(GemEmbedAchievementEvent.class, new AchieveGemListener(module));
        eventDispatcher.reg(SkillLevelAchieveEvent.class, new AchieveSkillListener(module));
        eventDispatcher.reg(PassStageEvent.class, new AchieveDungeonListener(module));
        eventDispatcher.reg(ChapterStarAchieveEvent.class, new AchieveChapterListener(module));
        AchievePartListener listener = new AchievePartListener(module);
        eventDispatcher.reg(EquipStarAchieveEvent.class, listener);
        eventDispatcher.reg(EquipStrengthAchieveEvent.class, listener);
        eventDispatcher.reg(EquipExtAttrAchieveEvent.class, new AchieveWashListener(module));
        eventDispatcher.reg(ActiveDeityWeaponAchieveEvent.class, new AchieveDeityWeaponListener(module));
        eventDispatcher.reg(GuestAchieveEvent.class, new AchieveGuestListener(module));
        eventDispatcher.reg(FriendAchieveEvent.class, new AchieveFriendListener(module));
        eventDispatcher.reg(TrumpAchieveEvent.class, new AchieveTrumpListener(module));
        eventDispatcher.reg(BookAchieveEvent.class, new AchieveBookListener(module));
        eventDispatcher.reg(Daily5v5AchieveEvent.class, new AchieveDaily5v5Listener(module));
        eventDispatcher.reg(EliteDungonAchieveEvent.class, new AchieveEliteDungonListener(module));
        eventDispatcher.reg(SigninAchieveEvent.class, new AchieveSigninListener(module));
        eventDispatcher.reg(TitleAchieveEvent.class, new AchieveTitleListener(module));
        eventDispatcher.reg(ChangeJobAchieveEvent.class, new AchieveChangeJobListener(module));
    }

    public static AchievementHandler newHandle(int type) {
        if (!handleMap.containsKey(type)) {
            com.stars.util.LogUtil.error("{} 类型的成就处理类不存在", type);
            return null;
        }
        Class<? extends AchievementHandler> clzz = handleMap.get(type);
        AchievementHandler handler = null;
        try {
            handler = clzz.newInstance();
        } catch (Exception e) {
            LogUtil.error("生成成就处理类出错", e);
        }
        return handler;
    }
}
