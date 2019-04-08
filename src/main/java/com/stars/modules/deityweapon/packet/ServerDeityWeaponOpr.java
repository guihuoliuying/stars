package com.stars.modules.deityweapon.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.deityweapon.DeityWeaponConstant;
import com.stars.modules.deityweapon.DeityWeaponModule;
import com.stars.modules.deityweapon.DeityWeaponPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * 请求神兵操作;
 * Created by panzhenfeng on 2016/12/2.
 */
public class ServerDeityWeaponOpr extends PlayerPacket {

    //操作类型;
    private byte oprType;
    //神兵类型;
    private byte deityweaponType;

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        oprType = buff.readByte();
        deityweaponType = buff.readByte();
    }

    @Override
    public void execPacket(Player player) {
        DeityWeaponModule deityWeaponModule = (DeityWeaponModule)module(MConst.Deity);
        switch (this.oprType){
            case DeityWeaponConstant.INFO:
                deityWeaponModule.syncToClientAllRoleDeityInfo();
                break;
            case DeityWeaponConstant.DRESS:
                deityWeaponModule.rqeuestDress(deityweaponType, true);
                break;
            case DeityWeaponConstant.DISDRESS:
                deityWeaponModule.requestDisDress(deityweaponType, true);
                break;
            case DeityWeaponConstant.FORGE:
                deityWeaponModule.requestForge(deityweaponType);
                break;
        }
    }

    @Override
    public short getType() {
        return DeityWeaponPacketSet.S_DEITYWEAPON_OPR;
    }
}
