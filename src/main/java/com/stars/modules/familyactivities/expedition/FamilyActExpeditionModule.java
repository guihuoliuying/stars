package com.stars.modules.familyactivities.expedition;

import com.stars.core.event.EventDispatcher;
import com.stars.core.gmpacket.specialaccount.SpecialAccountManager;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.modules.MConst;
import com.stars.modules.achievement.event.JoinActivityEvent;
import com.stars.modules.familyactivities.expedition.packet.ClientFamilyActExpedition;
import com.stars.modules.familyactivities.expedition.prodata.FamilyActExpeditionBuffInfoVo;
import com.stars.modules.familyactivities.expedition.prodata.FamilyExpeditionVo;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.SceneModule;
import com.stars.modules.scene.packet.ClientAddBuff;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.serverLog.event.SpecialAccountEvent;
import com.stars.modules.tool.ToolModule;
import com.stars.services.ServiceHelper;
import com.stars.services.activities.ActConst;
import com.stars.services.family.FamilyAuth;
import com.stars.services.family.FamilyConst;
import com.stars.util.I18n;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static com.stars.modules.familyactivities.FamilyActUtil.getAuth;
import static com.stars.modules.familyactivities.FamilyActUtil.hasFamily;
import static com.stars.modules.familyactivities.expedition.FamilyActExpeditionManager.*;

/**
 * Created by zhaowenshuo on 2016/10/8.
 */
public class FamilyActExpeditionModule extends AbstractModule {

    public static final String F_AVAIL_COUNT = "family.act.expe.availCount"; // 可以挑战次数
    public static final String F_MAX_ID = "family.act.expe.maxId"; // 最大通关的远征id
    public static final String F_LAST_ID = "family.act.expe.lastId"; // 上次通关的远征id（每天重置）
    public static final String F_CUR_ID = "family.act.expe.curId"; // 当前的远征id
    public static final String F_CUR_STEP = "family.act.expe.curStep"; // 当前的远征小关
    public static final String F_CUR_STATE = "family.act.expe.curState"; // 当前的远征状态
    public static final String F_BUFF_MAP = "family.act.expe.buffMap"; // 当前的技能使用状态

    private static volatile boolean isStart = false;

    public static void start() {
        synchronized (FamilyActExpeditionModule.class) {
            isStart = true;
            ServiceHelper.familyActEntryService().setOptions(
                    ActConst.ID_FAMILY_EXPEDITION, FamilyConst.ACT_BTN_MASK_DISPLAY | FamilyConst.ACT_BTN_MASK_LIGHT, -1, "");
        }
    }

    public static void end() {
        synchronized (FamilyActExpeditionModule.class) {
            isStart = false;
            ServiceHelper.familyActEntryService().setOptions(
                    ActConst.ID_FAMILY_EXPEDITION, FamilyConst.ACT_BTN_MASK_DISPLAY, -1, "");
        }
    }

    public FamilyActExpeditionModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super("家族远征", id, self, eventDispatcher, moduleMap);
    }

    @Override
    public void onDailyReset(Calendar now, boolean isLogin) throws Throwable {
        setInt(F_AVAIL_COUNT, 1);
        setInt(F_CUR_STATE, STATE_NOT_STARTED);
        setInt(F_LAST_ID, 0);
        clearValueMap(F_BUFF_MAP);
    }

    @Override
    public void onInit(boolean isCreation) throws Throwable {
    }

    @Override
    public void onOffline() throws Throwable {
        if (getInt(F_CUR_STATE) == STATE_PASSED) {
            setInt(F_CUR_STATE, STATE_END);
            getAward(-1);
        }
    }

    public void view() {
        if (!isStart) {
            warn("common_tips_nobegin");
            return;
        }
        sendView(ClientFamilyActExpedition.SUBTYPE_VIEW);
        fireSpecialAccountLogEvent("打开家族远征界面");
    }

    public void fight(int expeId) {
        if (!isStart) {
            warn("common_tips_nobegin");
            return;
        }
        FamilyAuth auth = getAuth(moduleMap());
        SceneModule sceneModule = (SceneModule) module(MConst.Scene);
        if (!hasFamily(auth)) {
            warn("no family no talk");
            return;
        }
        int availCount = getInt(F_AVAIL_COUNT);
        int curId = getInt(F_CUR_ID);
        int maxId = getInt(F_MAX_ID);
        int curStep = getInt(F_CUR_STEP);
        int curState = getInt(F_CUR_STATE);
        if (availCount == 0) {
            if (expeId != curId) {
                if (expeditionVoMap.containsKey(curId)) {
                    FamilyExpeditionVo vo = expeditionVoMap.get(curId).get(1);
                    if (vo != null) {
                        warn("family_tips_nocontinue", vo.getName());
                        return;
                    }
                }
                warn("common_tips_loading");
                return;
            }
            if (curState == STATE_END) {
                warn("family_tips_stepover");
                return;
            }
            if (expeditionVoMap.containsKey(expeId)) {
                FamilyExpeditionVo vo = expeditionVoMap.get(expeId).get(1);
                if (vo != null && auth.getFamilyLevel() < vo.getReqFamilyLevel()) {
                    warn("family_tips_reqfamilylv", Integer.toString(vo.getReqFamilyLevel()));
                    return;
                }
            }
        } else {
            if (expeId <= 0 || expeId > maxId + 1) {
                warn("family_tips_noopen");
                return;
            }
            if (familyLevel2ExpeditionIdMap.containsKey(auth.getFamilyLevel()) && expeId > familyLevel2ExpeditionIdMap.get(auth.getFamilyLevel())) {
                if (expeditionVoMap.containsKey(expeId)) {
                    FamilyExpeditionVo vo = expeditionVoMap.get(expeId).get(1);
                    if (vo != null) {
                        warn("family_tips_reqfamilylv", Integer.toString(vo.getReqFamilyLevel()));
                        return;
                    }
                }
                warn("common_tips_loading");
                return;
            }
            if (getInt(F_LAST_ID) != 0 && expeId != maxId + 1) {
                warn("family_tips_stepover");
                return;
            }
            if (!expeditionVoMap.containsKey(expeId)) {
                warn("common_tips_loading");
                return;
            }
            setInt(F_CUR_ID, curId = expeId);
            setInt(F_CUR_STEP, curStep = 1);
            setInt(F_CUR_STATE, STATE_STARTED);
        }
        FamilyExpeditionVo vo = expeditionVoMap.get(curId).get(curStep);
        if (vo == null) {
            warn("common_tips_loading");
            return;
        }
        sceneModule.enterScene(SceneManager.SCENETYPE_FAMILY_EXPEDITION,
                vo.getExpeditionId() * 1000 + vo.getStep(), vo);
        fireSpecialAccountLogEvent("参与家族远征活动");
        eventDispatcher().fire(new JoinActivityEvent(JoinActivityEvent.FAMILY_EXPE)); // 参加远征活动事件
    }

    public void getAward(int itemId) {
        int state = context().recordMap().getInt(F_CUR_STATE);
        if (state == STATE_NOT_STARTED || state == STATE_STARTED) {
            warn(I18n.get("family.expedition.notStart"));
            return;
        }
        if (state == STATE_END) {
            warn(I18n.get("family.expedition.end"));
            return;
        }
        setInt(F_CUR_STATE, STATE_END);
        int curExpeId = getInt(F_CUR_ID);
        int curExpeStep = getInt(F_CUR_STEP);
        if (!expeditionVoMap.containsKey(curExpeId)) {
            warn("common_tips_loading");
            return;
        }
        FamilyExpeditionVo expeVo = expeditionVoMap.get(curExpeId).get(curExpeStep - 1);
        if (expeVo == null) {
            warn("common_tips_loading");
            return;
        }
        if (itemId == -1) {
            for (Integer key : expeVo.getChooseAwardMap().keySet()) {
                itemId = key;
                break;
            }
        }
        Integer itemCount = expeVo.getChooseAwardMap().get(itemId);
        if (itemCount == null) {
            warn(I18n.get("family.expedition.notItemId"));
            return;
        }
        ToolModule toolModule = (ToolModule) module(MConst.Tool);
        toolModule.addAndSend(itemId, itemCount, EventType.FAMILYACT.getCode());
        fireSpecialAccountLogEvent("领取家族远征活动奖励");
    }

    public void addBuff(int buffId) {
        SceneModule sceneModule = (SceneModule) module(MConst.Scene);
        FamilyActExpeditionBuffInfoVo buffInfoVo = buffInfoVoMap.get(buffId);
        if (!sceneModule.getScene().isInFightScene()) {
            warn(I18n.get("family.expedition.buff"));
            return;
        }
        if (buffInfoVo == null) {
            warn("common_tips_loading");
            return;
        }
        // todo: 判断是否已经使用过
        if (getIntFromMap(F_BUFF_MAP, buffId, 0) != 0) {
            warn("family_tips_nobuffcount");
            return;
        }
        ToolModule toolModule = (ToolModule) module(MConst.Tool);
        if (toolModule.deleteAndSend(buffInfoVo.getReqItemId(), buffInfoVo.getReqItemCount(), EventType.FAMILYBUFF.getCode())) {
            setIntToMap(F_BUFF_MAP, buffId, 1);
            sendBuffState();
            send(new ClientAddBuff(buffId, 1)); // maybe some problem
        } else {
            warn("family_tips_noreqitem",
                    Integer.toString(buffInfoVo.getReqItemId()),
                    Integer.toString(buffInfoVo.getReqItemCount()));
            return;
        }
        fireSpecialAccountLogEvent("家族远征活动Buff");
    }

    public void sendBuffState() {
        Map<Integer, Byte> stateMap = new HashMap<>();
        for (Integer buffId : buffLevelMap.keySet()) {
            stateMap.put(buffId, (byte) getIntFromMap(F_BUFF_MAP, buffId, 0));
        }
        ClientFamilyActExpedition packet = new ClientFamilyActExpedition(ClientFamilyActExpedition.SUBTYPE_BUFF);
        packet.setBuffMap(stateMap);
        send(packet);
    }

    public void sendView(byte subtype) {
        FamilyAuth auth = getAuth(moduleMap());
        if (!hasFamily(auth)) {
            warn(I18n.get("family.expedition.notFamily"));
            return;
        }
        /* get recomId */
        int nextId = Math.min(getInt(F_MAX_ID, 0) + 1,
                familyLevel2ExpeditionIdMap.containsKey(auth.getFamilyLevel()) ?
                        familyLevel2ExpeditionIdMap.get(auth.getFamilyLevel()) : maxId);
        nextId = nextId > maxId ? maxId : nextId;
        /* get displayId */
        int displayId1, displayId2, displayId3;
        if (nextId == 1) {
            displayId1 = 1;
            displayId2 = 2;
            displayId3 = 3;
        } else if (nextId == maxId || nextId == Integer.MAX_VALUE) {
            displayId1 = maxId - 2;
            displayId2 = maxId - 1;
            displayId3 = maxId;
        } else {
            displayId1 = nextId - 1;
            displayId2 = nextId;
            displayId3 = nextId + 1;
        }
        /* get cur pass map */
//        Map<String, Integer> curPassedIdMap = new HashMap<>();
//        if (getIntFromMap(F_CUR_PASSED_ID_MAP, "lastOne", 0) != 0) {
//            curPassedIdMap.put("lastOne", getIntFromMap(F_CUR_PASSED_ID_MAP, "lastOne", 0));
//        }
//        if (getIntFromMap(F_CUR_PASSED_ID_MAP, "lastTwo", 0) != 0) {
//            curPassedIdMap.put("lastTwo", getIntFromMap(F_CUR_PASSED_ID_MAP, "lastTwo", 0));
//        }

        ClientFamilyActExpedition packet = new ClientFamilyActExpedition(subtype);
        packet.setMaxPassedId(getInt(F_MAX_ID));
        packet.setAvailCount(getInt(F_AVAIL_COUNT));
        packet.setCurId(getInt(F_AVAIL_COUNT) == 0 ? getInt(F_CUR_ID) : nextId);
        packet.setCurStep(getInt(F_AVAIL_COUNT) == 1 ? 0 : getInt(F_CUR_STEP)); // 如果还没打过（有可挑战次数）就下发0
        packet.setDisplayId1(displayId1);
        packet.setDisplayId2(displayId2);
        packet.setDisplayId3(displayId3);
//        packet.setCurPassedIdMap(curPassedIdMap);

        send(packet);
    }

    private int getRecommId(int expeId, int familyLevel) {
        return Math.min(getInt(F_MAX_ID) + 1, familyLevel2ExpeditionIdMap.get(familyLevel));
    }

    public void gmOpenAll() {
        setInt(F_MAX_ID, maxId);
    }

    private void fireSpecialAccountLogEvent(String content) {
        if (SpecialAccountManager.isSpecialAccount(id())) {
            eventDispatcher().fire(new SpecialAccountEvent(id(), content, true));
        }
    }
}
