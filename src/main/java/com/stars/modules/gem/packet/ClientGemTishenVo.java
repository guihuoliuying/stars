package com.stars.modules.gem.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.gem.GemManager;
import com.stars.modules.gem.GemPacketSet;
import com.stars.modules.gem.prodata.GemLevelVo;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.ArrayList;
import java.util.List;

/**
 * 响应客户端请求装备提升的VO数据;
 * Created by panzhenfeng on 2016/7/19.
 */
public class ClientGemTishenVo extends PlayerPacket {

    private List<String> waitToSendList = null;

    public ClientGemTishenVo() {

    }

    /**
     * 注意，此时level_作为genLevel表的itemId字段;
     * @param level
     */
    public void addGemLevelVo(int level){
        GemLevelVo gemLevelVo = GemManager.getGemLevelVo(level);
        if(gemLevelVo != null){
            if (waitToSendList == null){
                waitToSendList = new ArrayList<>();
            }
            waitToSendList.add(gemLevelVo.getPackedString());
        }
    }


    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return GemPacketSet.C_EQUIPMENT_TISHEN_VO;
    }

    //TODO 之后要修改，判断resultContent为空时不进行下发数据;
    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        int count = waitToSendList == null?0:waitToSendList.size();
        buff.writeInt(count);
        for (int i = 0, len = count; i < len; i++) {
            buff.writeString(waitToSendList.get(i));
        }
    }

}