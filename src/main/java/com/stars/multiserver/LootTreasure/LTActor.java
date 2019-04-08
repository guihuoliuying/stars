package com.stars.multiserver.LootTreasure;

import com.stars.modules.loottreasure.packet.AttendLootTreasure;
import com.stars.modules.pk.packet.ConnectRegisterToFightServer;
import com.stars.modules.role.RoleManager;
import com.stars.modules.role.prodata.Job;
import com.stars.modules.role.prodata.Resource;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.modules.scene.packet.clientEnterFight.ClientEnterFight;
import com.stars.modules.scene.packet.clientEnterFight.ClientEnterLootTreasurePVP;
import com.stars.modules.skill.SkillManager;
import com.stars.modules.skill.prodata.SkillVo;
import com.stars.multiserver.fight.RoleId2ActorIdManager;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.core.actor.AbstractActor;
import com.stars.core.actor.Actor;
import io.netty.buffer.UnpooledByteBufAllocator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LTActor extends AbstractActor {

    private String id;

    private AbstractLootTreasure lootTreasure;

    public static Map<Long, AttendLootTreasure> attendMap;

    public LTActor(String id) {
        this.id = id;
        lootTreasure = new PVELootTreasure(this);
        attendMap = new ConcurrentHashMap<>();
    }

//	public void initPVPLootTreasure(){
//		lootTreasure = new PVPLootTreasure(this);
//	}

    @Override
    public void onReceived(Object message, Actor sender) {
        if (message instanceof AttendLootTreasure) {
            AttendLootTreasure attendLootTreasure = (AttendLootTreasure) message;
            long id = Long.parseLong(attendLootTreasure.getfEntity().getUniqueId());
            attendMap.put(id, attendLootTreasure);
            RoleId2ActorIdManager.put(id, attendLootTreasure.getId());
            RMLTRPCHelper.lootTreasureService.attendLTBack(attendLootTreasure.getServerId(),id, (byte)0);
            return;
        }
        if (message instanceof ConnectRegisterToFightServer) {
            ConnectRegisterToFightServer trts = (ConnectRegisterToFightServer) message;
            AttendLootTreasure attendLootTreasure = attendMap.get(trts.getFighter());
            if (attendLootTreasure != null) {
            	lootTreasure.newLooterCome(attendLootTreasure.getServerId(), attendLootTreasure.getServerName(), attendLootTreasure.getfEntity(), attendLootTreasure.getJobId());
                attendMap.remove(trts.getFighter());
			}
            return;
        }
        if (message instanceof StopLTActor) {
            this.stop();
            lootTreasure.stopSelf();
            lootTreasure = null;
            return;
        }
        lootTreasure.onReceived(message);
    }

    public AbstractLootTreasure getLootTreasure() {
        return lootTreasure;
    }

    public void setLootTreasure(AbstractLootTreasure lootTreasure) {
        this.lootTreasure = lootTreasure;
    }

    public static ClientEnterLootTreasurePVP newClientEnterPK(FighterEntity fEntity) {
        ClientEnterLootTreasurePVP enterPack = new ClientEnterLootTreasurePVP();
        enterPack.setIsAgain((byte) 0);
        enterPack.setFightType((byte) 13);
        enterPack.setStageId(9199);
        ArrayList<FighterEntity> al = new ArrayList<FighterEntity>();
        al.add(fEntity);
        enterPack.setFighterEntityList(al);
        fillClientEnterLootTreasurePvpSkillBuffData(enterPack);
        return enterPack;
    }

    public static ClientEnterLootTreasurePVP newClientEnterPK(Collection<FighterEntity> col) {
        ClientEnterLootTreasurePVP enterPack = new ClientEnterLootTreasurePVP();
        enterPack.setIsAgain((byte) 0);
        enterPack.setFightType((byte) 13);
        enterPack.setStageId(9199);
        enterPack.setFighterEntityList(col);
        fillClientEnterLootTreasurePvpSkillBuffData(enterPack);
        return enterPack;
    }

    public static void fillClientEnterLootTreasurePvpSkillBuffData(ClientEnterFight clientEnterFight){
        //下发除了当前fEntity外的其他job的技能和buff数据;
        Map<Integer, Job> jobMap = RoleManager.jobMap;
        Job job = null;
        Resource resource = null;
        SkillVo skillVo = null;
        List<Integer> skillVoList = null;
        Map<Integer, Integer> skillMap = new ConcurrentHashMap<>();
        for (Map.Entry<Integer, Job> kvp : jobMap.entrySet()) {
            job = kvp.getValue();
            resource = RoleManager.getResourceById(job.getModelres());
            skillVoList = resource.getSkillList();
            for (int i = 0, len = skillVoList.size(); i < len; i++) {
                skillVo = SkillManager.getSkillVo(skillVoList.get(i));
                skillMap.put(skillVo.getSkillid(), 1);
            }
            for (Integer pSkillId : job.getPSkillList()) {
                skillMap.put(pSkillId, 1);
            }
        }
        Collection<FighterEntity> fighterEntityCollection = clientEnterFight.getFighterEntityList();
        for(FighterEntity fe: fighterEntityCollection){
            skillMap.putAll(fe.getSkills());
        }
        clientEnterFight.addSkillData(skillMap);
    }

    public static byte[] getClientEnterPKData(ClientEnterLootTreasurePVP enterPK) {
        NewByteBuffer buffer = new NewByteBuffer(UnpooledByteBufAllocator.DEFAULT.buffer());
        enterPK.writeToBuffer(buffer);
        byte[] bytes = new byte[buffer.getBuff().readableBytes()];
        buffer.getBuff().readBytes(bytes);
        buffer.getBuff().release();
        return bytes;
    }

    public String getId() {
        return id;
    }
}
