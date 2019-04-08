package com.stars.modules.scene.imp.fight;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.family.FamilyModule;
import com.stars.modules.familyactivities.FamilyActUtil;
import com.stars.modules.familyactivities.expedition.FamilyActExpeditionManager;
import com.stars.modules.familyactivities.expedition.FamilyActExpeditionModule;
import com.stars.modules.familyactivities.expedition.packet.ClientFamilyActExpedition;
import com.stars.modules.familyactivities.expedition.packet.ClientFamilyActExpeditionSceneFinished;
import com.stars.modules.familyactivities.expedition.prodata.FamilyActExpeditionStarAwardVo;
import com.stars.modules.familyactivities.expedition.prodata.FamilyExpeditionVo;
import com.stars.modules.scene.SceneModule;
import com.stars.modules.scene.packet.clientEnterFight.ClientEnterDungeon;
import com.stars.modules.scene.prodata.StageinfoVo;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.tool.ToolModule;
import com.stars.services.ServiceHelper;
import com.stars.services.family.FamilyAuth;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.stars.modules.scene.SceneManager.*;

/**
 * Created by zhaowenshuo on 2016/10/11.
 */
public class FamilyActExpeditionScene extends DungeonScene {

    private FamilyExpeditionVo vo;
    private int enemyKilledCount;

    @Override
    public boolean canEnter(Map<String, Module> moduleMap, Object vo) {
        return true;
    }

    @Override
    public void enter(Map<String, Module> moduleMap, Object obj) {
        this.vo = (FamilyExpeditionVo) obj;
        /* 扣减次数 */
        FamilyActExpeditionModule module = (FamilyActExpeditionModule) moduleMap.get(MConst.FamilyActExpe);
        module.setInt(FamilyActExpeditionModule.F_AVAIL_COUNT, 0);
        /* 初始化流程 */
        StageinfoVo stageVo = getStageVo(vo.getStageId());
        this.stageId = vo.getStageId();
        this.startTimestamp = System.currentTimeMillis();
        this.stageStatus = STAGE_PROCEEDING;
        FamilyActExpeditionModule expeditionModule = (FamilyActExpeditionModule) moduleMap.get(MConst.FamilyActExpe);
        expeditionModule.sendBuffState();
        ClientEnterDungeon enterFight = new ClientEnterDungeon();
        requestSendClientEnterFight(moduleMap, enterFight, stageVo);
        enterFight.addBuffData(FamilyActExpeditionManager.buffLevelMap);
        SceneModule sceneModule = (SceneModule) moduleMap.get(MConst.Scene);
        // 先发剧情播放记录
        sceneModule.sendPlayedDrama(this, stageId);
        sceneModule.send(enterFight);
    }

    @Override
    public void enemyDead(Map<String, Module> moduleMap, List<String> uIdList) {
        enemyKilledCount += uIdList.size();
        super.enemyDead(moduleMap, uIdList);
    }

    @Override
    public void finishDeal(Map<String, Module> moduleMap, byte finish) {
        FamilyActExpeditionModule module = (FamilyActExpeditionModule) moduleMap.get(MConst.FamilyActExpe);
        FamilyModule familyModule = (FamilyModule) moduleMap.get(MConst.Family);
        if (finish == STAGE_FAIL) {
            ClientFamilyActExpeditionSceneFinished packet = new ClientFamilyActExpeditionSceneFinished();
            packet.setCurExpeId(vo.getExpeditionId());
            packet.setCurExpeStep(vo.getStep());
            packet.setStatus(finish);
            packet.setStar((byte) (0));
            packet.setUseTime(0);
            module.send(packet);
            return;
        }
        ToolModule toolModule = (ToolModule) moduleMap.get(MConst.Tool);
        endTimestamp = System.currentTimeMillis();
        int timeElapsedSec = (int) ((endTimestamp - startTimestamp - spendTime) / 1000);
        // 记录状态
        module.setInt(FamilyActExpeditionModule.F_CUR_STEP, module.getInt(FamilyActExpeditionModule.F_CUR_STEP) + 1); // 难度加一
        FamilyExpeditionVo nextVo = findNextStep(vo);
        if (nextVo == null) {
            module.setInt(FamilyActExpeditionModule.F_CUR_STATE, FamilyActExpeditionManager.STATE_PASSED);
            if (vo.getExpeditionId() > module.getInt(FamilyActExpeditionModule.F_MAX_ID)) {
                module.setInt(FamilyActExpeditionModule.F_MAX_ID, vo.getExpeditionId());
                module.setInt(FamilyActExpeditionModule.F_AVAIL_COUNT, 1); // 新开难度：增加次数
                module.clearValueMap(FamilyActExpeditionModule.F_BUFF_MAP); // 新开难度：重置buff
            }
            module.setInt(FamilyActExpeditionModule.F_LAST_ID, vo.getExpeditionId());
            /* 增加家族资金 */
            FamilyAuth auth = familyModule.getAuth();
            if (FamilyActUtil.hasFamily(auth)) {
                ServiceHelper.familyMainService().addMoneyAndUpdateContribution(
                        auth, auth.getRoleId(), vo.getFamilyMoneyAward(), 0, 0, 0);
            }
        }
        // 计算星级
        byte star = calcStar(enemyKilledCount, timeElapsedSec);
        // 发送奖励
        Map<Integer, Integer> awardMap = new HashMap<>();
        for (int i = 0; i <= star; i++) {
            FamilyActExpeditionStarAwardVo awardVo = vo.getStarAwardList().get(i);
            if (!awardMap.containsKey(awardVo.getItemId())) {
                awardMap.put(awardVo.getItemId(), awardVo.getItemCount());
            } else {
                awardMap.put(awardVo.getItemId(), awardMap.get(awardVo.getItemId()) + awardVo.getItemCount());
            }
        }
        toolModule.addAndSend(awardMap, EventType.FAMILYACT.getCode());

        ClientFamilyActExpeditionSceneFinished packet = new ClientFamilyActExpeditionSceneFinished();
        packet.setCurExpeId(vo.getExpeditionId());
        packet.setCurExpeStep(vo.getStep());
        packet.setStatus(finish);
        packet.setStar((byte) (star + 1));
        packet.setUseTime(timeElapsedSec);
        module.send(packet);

        module.sendView(ClientFamilyActExpedition.SUBTYPE_VIEW); //

        if (nextVo == null) { // 更新信息
            module.sendView(ClientFamilyActExpedition.SUBTYPE_UPDATED_ALL);
        }
    }

    private int getCondType() {
        return vo.getStarAwardList().get(0).getCondType();
    }

    private byte calcStar(int enemyKilledCount, int timeElapsedSec) {
        int condType = getCondType();
        switch (condType) {
            case 1:
                return calcStarByKilledCount(enemyKilledCount);
            case 2:
                return calcStarByTimeElapsed(timeElapsedSec);
        }
        return -1;
    }

    private byte calcStarByKilledCount(int killedCount) {
        for (byte i = 2; i >= 0; i--) {
            if (killedCount >= vo.getStarAwardList().get(i).getCondCount()) {
                return i;
            }
        }
        return -1;
    }

    private byte calcStarByTimeElapsed(int timeElapsedSec) {
        for (byte i = 2; i >= 0; i--) {
            if (timeElapsedSec <= vo.getStarAwardList().get(i).getCondCount()) {
                return i;
            }
        }
        return -1;
    }

    private FamilyExpeditionVo findNextStep(FamilyExpeditionVo vo) {
        if (!FamilyActExpeditionManager.expeditionVoMap.containsKey(vo.getExpeditionId())) {
            throw new IllegalStateException("not found product data");
        }
        return FamilyActExpeditionManager.expeditionVoMap.get(vo.getExpeditionId()).get(vo.getStep() + 1);
    }

}
