package com.stars.modules.chargepreference.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.chargepreference.ChargePrefManager;
import com.stars.modules.chargepreference.ChargePrefPacketSet;
import com.stars.modules.chargepreference.prodata.ChargePrefVo;
import com.stars.modules.chargepreference.userdata.RoleChargePrefPo;
import com.stars.modules.push.PushManager;
import com.stars.modules.push.prodata.PushVo;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/3/30.
 */
public class ClientChargePref extends PlayerPacket {

    public static final byte SUBTYPE_VIEW = 0x00;
    public static final byte SUBTYPE_CHOOSE = 0x01;

    private byte subtype;
    private int remainCount;
    private int chosenId;
    private Map<Integer, RoleChargePrefPo> prefPoMap;

    public ClientChargePref() {
    }

    public ClientChargePref(byte subtype) {
        this.subtype = subtype;
    }

    @Override
    public short getType() {
        return ChargePrefPacketSet.C_CHARGE_PREF;
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeByte(subtype);
        switch (subtype) {
            case SUBTYPE_VIEW:
                buff.writeInt(remainCount);
                buff.writeInt(chosenId);
                buff.writeInt(prefPoMap.size());
                for (RoleChargePrefPo po : prefPoMap.values()) {
                    writeRoleChargePrefPo(buff, po);
                }
                break;
            case SUBTYPE_CHOOSE:
                buff.writeInt(chosenId);
                break;
        }
    }

    private void writeRoleChargePrefPo(NewByteBuffer buff, RoleChargePrefPo po) {
        ChargePrefVo vo = ChargePrefManager.getPrefVo(po.getPrefId());
        PushVo pushVo = PushManager.getPushVo(vo.getPushId());
        buff.writeInt(vo.getPrefId()); // 特惠id
        buff.writeInt(vo.getRank()); //
        buff.writeInt(pushVo != null ? pushVo.getGroup() : 0); //
        buff.writeString(vo.getShowItem());
        buff.writeByte(vo.getIsNew());
        buff.writeInt(vo.getOriginPrice());
        buff.writeInt(vo.getCurrentPrice());
        buff.writeInt(vo.getRebatePrice());
        buff.writeInt(po.getChargeNumber());
        buff.writeByte((byte) (po.isRebate() ? 1 : 0));
    }

    public void setRemainCount(int remainCount) {
        this.remainCount = remainCount;
    }

    public void setChosenId(int chosenId) {
        this.chosenId = chosenId;
    }

    public void setPrefPoMap(Map<Integer, RoleChargePrefPo> prefPoMap) {
        this.prefPoMap = prefPoMap;
    }
}
