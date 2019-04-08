package com.stars.modules.deityweapon.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.deityweapon.DeityWeaponConstant;
import com.stars.modules.deityweapon.DeityWeaponPacketSet;
import com.stars.modules.deityweapon.userdata.RoleDeityWeapon;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.List;

/**
 * 响应客户端的神兵操作;
 * Created by panzhenfeng on 2016/12/2.
 */
public class ClientDeityWeaponOpr  extends PlayerPacket {
    private byte oprType;
    private byte errCode = 0;

    private List<RoleDeityWeapon> roleDeityWeaponList;
    //根据不同操作类型各自定义;
    private byte state;

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeByte(errCode);
        buff.writeByte(this.oprType);
        int count = 0;
        switch (this.oprType){
            case DeityWeaponConstant.INFO:
            case DeityWeaponConstant.INACTIVE:
            case DeityWeaponConstant.DISDRESS:
            case DeityWeaponConstant.DRESS:
                count = roleDeityWeaponList==null?0:roleDeityWeaponList.size();
                buff.writeInt(count);
                for(int i = 0; i<count; i++){
                    roleDeityWeaponList.get(i).writeBuff(buff);
                }
                break;
        }
    }


    @Override
    public short getType() {
        return DeityWeaponPacketSet.C_DEITYWEAPON_OPR;
    }

    public void setOprType(byte oprType) {
        this.oprType = oprType;
    }

    public void setRoleDeityWeaponList(List<RoleDeityWeapon> roleDeityWeaponList) {
        this.roleDeityWeaponList = roleDeityWeaponList;
    }

    public byte getErrCode() {
        return errCode;
    }

    public void setErrCode(byte errCode) {
        this.errCode = errCode;
    }
}
