package com.stars.modules.weeklyCharge.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.weeklyCharge.WeeklyChargePacketSet;
import com.stars.modules.weeklyCharge.prodata.WeeklyChargeVo;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.StringUtil;

import java.util.List;

/**
 * Created by chenxie on 2017/5/5.
 */
public class ClientWeeklyCharge extends PlayerPacket {

    /**
     * 当前角色周累计充值金额
     */
    private int roleTotalCharge;

    /**
     * 周累计充值奖励数据
     */
    private List<WeeklyChargeVo> list;

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return WeeklyChargePacketSet.C_WEEKLYCHARGE;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        writeViewToBuff(buff);
    }

    /**
     * 写入数据至缓冲区
     * @param buff
     */
    private void writeViewToBuff(NewByteBuffer buff) {
        buff.writeInt(roleTotalCharge);
        if(StringUtil.isEmpty(list)){
            buff.writeShort((short) 0);
        }else{
            buff.writeShort((short)list.size());
            for(WeeklyChargeVo vo : list){
                vo.writeToBuff(buff);
            }
        }
    }

    public void setRoleTotalCharge(int roleTotalCharge) {
        this.roleTotalCharge = roleTotalCharge;
    }

    public void setList(List<WeeklyChargeVo> list) {
        this.list = list;
    }

}
