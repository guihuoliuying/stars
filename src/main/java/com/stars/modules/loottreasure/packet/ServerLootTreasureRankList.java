package com.stars.modules.loottreasure.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.core.player.PlayerUtil;
import com.stars.modules.MConst;
import com.stars.modules.loottreasure.LootTreasureManager;
import com.stars.modules.loottreasure.LootTreasurePacketSet;
import com.stars.modules.loottreasure.prodata.LootSectionVo;
import com.stars.modules.role.RoleModule;
import com.stars.modules.scene.SceneManager;
import com.stars.multiserver.LootTreasure.LTDamageRankVo;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.services.ServiceHelper;
import com.stars.util.StringUtil;

import java.util.List;

/**
 * 请求服务器排行榜信息;
 * Created by panzhenfeng on 2016/10/21.
 */
public class ServerLootTreasureRankList  extends PlayerPacket {
    public byte stageType;


    @Override
    public short getType() {
        return LootTreasurePacketSet.S_LOOTTREASURE_RANKLIST;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {

    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        stageType = buff.readByte();
    }


    @Override
    public void execPacket(Player player) {
        RoleModule roleModule = (RoleModule)this.module(MConst.Role);
        String lootSectionIdStr = ServiceHelper.lootTreasureService().getLootSectionIdByRoleId(roleModule.getRoleRow().getRoleId());
        int lootSectionId = 0;
        if (StringUtil.isNotEmpty(lootSectionIdStr)){
            lootSectionId = Integer.parseInt(lootSectionIdStr);
        }else{
            int roleLevel = roleModule.getLevel();
            LootSectionVo lootSectionVo = LootTreasureManager.getLootSectionVoByLevel(roleLevel);
            lootSectionId = lootSectionVo.getLevelsection();
        }
        List<LTDamageRankVo> damageRankVoList = ServiceHelper.lootTreasureService().getLtDamageRankVoLists(String.valueOf(lootSectionId));
        ClientLootTreasureRankList clientLootTreasureRankList = new ClientLootTreasureRankList(SceneManager.SCENETYPE_LOOTTREASURE_PVP, damageRankVoList);
        PlayerUtil.send(getRoleId(), clientLootTreasureRankList);
    }
}