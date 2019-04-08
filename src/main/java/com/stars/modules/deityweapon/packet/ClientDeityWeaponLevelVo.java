package com.stars.modules.deityweapon.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.deityweapon.DeityWeaponPacketSet;
import com.stars.modules.deityweapon.prodata.DeityWeaponLevelVo;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.LogUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by panzhenfeng on 2016/12/2.
 */
public class ClientDeityWeaponLevelVo  extends PlayerPacket {

    private List<DeityWeaponLevelVo> deityWeaponLevelVoList = null;

    public ClientDeityWeaponLevelVo(){

    }

    public void addDeityWeaponLevelVo(DeityWeaponLevelVo deityWeaponLevelVo){
        if(deityWeaponLevelVo==null){
            LogUtil.error("找不到对应的deityweaponlevel 数据, 请确保客户端和服务端的数据是一致的!");
            return;
        }
        if(this.deityWeaponLevelVoList == null){
            this.deityWeaponLevelVoList = new ArrayList<>();
        }
        this.deityWeaponLevelVoList.add(deityWeaponLevelVo);
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        int count = deityWeaponLevelVoList == null?0:deityWeaponLevelVoList.size();
        buff.writeInt(count);
        for (int i = 0; i<count; i++){
            deityWeaponLevelVoList.get(i).writeBuff(buff);
        }
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return DeityWeaponPacketSet.C_DEITYWEAPONLEVEL_VO;
    }
}
