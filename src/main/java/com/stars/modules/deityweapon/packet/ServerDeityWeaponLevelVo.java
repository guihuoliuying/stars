package com.stars.modules.deityweapon.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.core.player.PlayerUtil;
import com.stars.modules.deityweapon.DeityWeaponManager;
import com.stars.modules.deityweapon.DeityWeaponPacketSet;
import com.stars.modules.deityweapon.prodata.DeityWeaponLevelVo;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by panzhenfeng on 2016/12/2.
 */
public class ServerDeityWeaponLevelVo  extends PlayerPacket {
    private byte deityweaponType;
    private String deityweaponLvlStr;

    //用于查询不同等级的信息;
    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        deityweaponType = buff.readByte();
        deityweaponLvlStr = buff.readString();
    }

    @Override
    public void execPacket(Player player) {
        ClientDeityWeaponLevelVo clientDeityWeaponLevelVo = new ClientDeityWeaponLevelVo();
        DeityWeaponLevelVo deityWeaponLevelVo = null;
        String[] requestLvlArr = deityweaponLvlStr.split("[+]");
        for (int i = 0, len = requestLvlArr.length; i<len; i++){
            deityWeaponLevelVo = DeityWeaponManager.getDeityWeaponLevelVo(deityweaponType, Integer.parseInt(requestLvlArr[i]));
            clientDeityWeaponLevelVo.addDeityWeaponLevelVo(deityWeaponLevelVo);
        }
        PlayerUtil.send(getRoleId(), clientDeityWeaponLevelVo);
    }

    @Override
    public short getType() {
        return DeityWeaponPacketSet.S_DEITYWEAPONLEVEL_VO;
    }
}

