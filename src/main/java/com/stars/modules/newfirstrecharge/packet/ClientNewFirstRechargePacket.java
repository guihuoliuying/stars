package com.stars.modules.newfirstrecharge.packet;

import com.stars.modules.newfirstrecharge.NewFirstRechargeManagerFacade;
import com.stars.modules.newfirstrecharge.NewFirstRechargePackets;
import com.stars.modules.newfirstrecharge.prodata.NewFirstRecharge;
import com.stars.modules.newfirstrecharge.usrdata.RoleNewFirstRecharge;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;

import java.util.Map;

/**
 * Created by huwenjun on 2017/9/6.
 */
public class ClientNewFirstRechargePacket extends Packet {
    private short subType;
    private int activityType;
    private RoleNewFirstRecharge roleNewFirstRecharge;
    public static final short SEND_MAIN_UI_DATA = 1;//下发主界面数据

    public ClientNewFirstRechargePacket() {
    }

    public ClientNewFirstRechargePacket(int activityType, short subType) {
        this.subType = subType;
        this.activityType = activityType;
    }

    @Override
    public short getType() {
        return NewFirstRechargePackets.C_FIRST_RECHARGE;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeInt(activityType);
        buff.writeShort(subType);
        switch (subType) {
            case SEND_MAIN_UI_DATA: {
                buff.writeInt(roleNewFirstRecharge.getToday());//今天领取哪一天
                buff.writeInt(roleNewFirstRecharge.getPayCount());//今天已经充值多少
                buff.writeInt(NewFirstRechargeManagerFacade.getNewFirstRechargeMap(activityType).size());
                for (Map.Entry<Integer, NewFirstRecharge> entry : NewFirstRechargeManagerFacade.getNewFirstRechargeMap(activityType).entrySet()) {
                    Integer day = entry.getKey();
                    /**
                     * 检测奖励状态
                     * -1,未初始化状态
                     * 0，已领取
                     * 1,可领取
                     */
                    buff.writeInt(roleNewFirstRecharge.getDayStatus(day));
                    NewFirstRecharge newFirstRecharge = entry.getValue();
                    newFirstRecharge.writeBuff(buff, roleNewFirstRecharge.getVipLevel());
                }
                buff.writeInt(roleNewFirstRecharge.getGroup());//当前选择第几组奖励
            }
            break;
        }
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {

    }

    @Override
    public void execPacket() {

    }

    public RoleNewFirstRecharge getRoleNewFirstRecharge() {
        return roleNewFirstRecharge;
    }

    public void setRoleNewFirstRecharge(RoleNewFirstRecharge roleNewFirstRecharge) {
        this.roleNewFirstRecharge = roleNewFirstRecharge;
    }
}
