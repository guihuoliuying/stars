package com.stars.modules.operateactivity.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.operateactivity.OperateActivityManager;
import com.stars.modules.operateactivity.OperateActivityPacketSet;
import com.stars.modules.operateactivity.prodata.OperateActVo;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.LogUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * Created by gaopeidian on 2016/11/21.
 */
public class ClientAllActivityInfo extends PlayerPacket {
    //<活动type，活动id>
    private Map<Integer, Integer> activityIdsMap = null;

    //<活动type，isShowLabel>
    private Map<Integer, Byte> isShowLabelMap = new HashMap<Integer, Byte>();

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return OperateActivityPacketSet.C_ALL_ACTIVITY_INFO;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        short size = (short) (activityIdsMap == null ? 0 : activityIdsMap.size());
        buff.writeShort(size);
        LogUtil.info("活动数据|activityMap:{}", activityIdsMap);
        if (size != 0) {
            Set<Map.Entry<Integer, Integer>> entrySet = activityIdsMap.entrySet();
            for (Map.Entry<Integer, Integer> entry : entrySet) {
                int activityId = entry.getValue();
                OperateActVo operateActVo = OperateActivityManager.getOperateActVo(activityId);
                if (operateActVo == null) {
                    continue;
                }
                int type = entry.getKey();
                byte isOpen = (byte) 1;
                byte isShowLabel = (byte) 0;
                if (isShowLabelMap.containsKey(type)) {
                    isShowLabel = isShowLabelMap.get(type);
                }

                buff.writeInt(activityId);
                buff.writeInt(type);
                buff.writeByte(isOpen);
                buff.writeString(operateActVo.getName());
                buff.writeString(operateActVo.getHotlabel());
                buff.writeInt(operateActVo.getOrder());
                buff.writeString(operateActVo.getRuledesc());
                buff.writeString(operateActVo.getTimedesc());
                buff.writeString(operateActVo.getOpenwindow());
                buff.writeString(operateActVo.getOpenTime());
                buff.writeByte(operateActVo.getClassType());
                buff.writeByte(isShowLabel);

                buff.writeString(operateActVo.getShowitem());
                buff.writeString(operateActVo.getShowpic());
                buff.writeString(operateActVo.getGotonpc());
                buff.writeString(operateActVo.getButtondesc());
            }
        }
    }

    public void setActivityIdsMap(Map<Integer, Integer> value) {
        this.activityIdsMap = value;
    }

    public void setIsShowLabelMap(Map<Integer, Byte> value) {
        this.isShowLabelMap = value;
    }
}