package com.stars.modules.gm.gmhandler;

import com.stars.core.module.Module;
import com.stars.modules.familyactivities.war.FamilyActWarManager;
import com.stars.modules.gm.GmHandler;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.packet.clientEnterFight.ClientEnterFamilyWarEliteFight;
import com.stars.modules.scene.prodata.DynamicBlock;
import com.stars.modules.scene.prodata.StageinfoVo;
import com.stars.modules.skill.SkillManager;
import com.stars.modules.skill.prodata.SkillVo;
import com.stars.multiserver.MainRpcHelper;
import com.stars.multiserver.MultiServerHelper;
import com.stars.multiserver.familywar.knockout.fight.elite.FamilyWarEliteFightArgs;
import com.stars.multiserver.fight.FightIdCreator;
import com.stars.multiserver.fight.handler.FightConst;
import com.stars.network.PacketUtil;
import com.stars.services.ServiceHelper;
import com.stars.services.fightServerManager.FSManagerServiceActor;
import com.stars.util.LogUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by chenkeyu on 2017-08-11.
 */
public class FightServerGmHandler implements GmHandler {
    @Override
    public void handle(long roleId, Map<String, Module> moduleMap, String[] args) throws Exception {
        int count = Integer.parseInt(args[0]);
        int serverId = ServiceHelper.fsManagerService().getFightServer(FSManagerServiceActor.FIGHT_SERVER_LEVEL_COMM);
        FamilyWarEliteFightArgs args0 = new FamilyWarEliteFightArgs();
        args0.setBattleId("");
        args0.setRoleWarType(new HashMap<Long, Integer>());
        args0.setRoleWarType(new HashMap<Long, Integer>());
        args0.setCampMap(new HashMap<Long, Byte>());
        args0.setCamp1MainServerId(0);
        args0.setCamp2MainServerId(0);
        args0.setCreateTimestamp(System.currentTimeMillis());
        for (int i = 0; i < count; i++) {
            MainRpcHelper.fightBaseService().createFight(serverId, FightConst.T_FAMILY_WAR_ELITE_FIGHT, MultiServerHelper.getServerId(),
                    FightIdCreator.creatUUId() + "-" + count, createEnterEliteFightPacket(), args0);
            com.stars.util.LogUtil.info("第 {} 场战斗创建完毕", count);
        }
    }

    private byte[] createEnterEliteFightPacket() {
        StageinfoVo stageVo = SceneManager.getStageVo(FamilyActWarManager.stageIdOfEliteFight);
        ClientEnterFamilyWarEliteFight enterPacket = new ClientEnterFamilyWarEliteFight();
        enterPacket.setFightType(SceneManager.SCENETYPE_FAMILY_WAR_ELITE_FIGHT); // buffer
        enterPacket.setStageId(FamilyActWarManager.stageIdOfEliteFight);
        enterPacket.setLimitTime(FamilyActWarManager.familywar_lasttime);
        enterPacket.setStartRemainderTime(FamilyActWarManager.DYNAMIC_BLOCK_TIME);
        enterPacket.setSkillVoMap(new HashMap<Integer, SkillVo>(SkillManager.getSkillVoMap()));
        enterPacket.addMonsterVoMap(stageVo.getMonsterVoMap());
        /* 动态阻挡数据 */
        Map<String, Byte> blockStatus = new HashMap<>();
        for (DynamicBlock dynamicBlock : stageVo.getDynamicBlockMap().values()) {
            blockStatus.put(dynamicBlock.getUnniqueId(), SceneManager.BLOCK_CLOSE);
            if (dynamicBlock.getShowSpawnId() == 0) {
                blockStatus.put(dynamicBlock.getUnniqueId(), SceneManager.BLOCK_OPEN);
            }
        }
        LogUtil.info("动态阻挡数据Elites:{}", blockStatus);
        enterPacket.setBlockMap(stageVo.getDynamicBlockMap());
        enterPacket.addBlockStatusMap(blockStatus);
        return PacketUtil.packetToBytes(enterPacket);
    }
}
