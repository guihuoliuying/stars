package com.stars.modules.scene.packet.clientEnterFight;

import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.modules.scene.prodata.DynamicBlock;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by daiyaorong on 2016/9/2.
 */
public class ClientEnterPK extends ClientEnterFight {
    private int limitTime;// 倒计时,单位秒
    private int countdownOfBegin;//开始倒计时

    @Override
    public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {
        writeBase(buff);
        buff.writeInt(limitTime);
        buff.writeInt(countdownOfBegin);
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        byte isAgain = buff.readByte();
        setIsAgain(isAgain);
        setFightType(buff.readByte());
        setStageId(buff.readInt());
        int reborntime = buff.readInt();
        byte size = buff.readByte();
        if (size > 0) {
            List<FighterEntity> fighterEntityList = new LinkedList<>();
            byte fighterType;
            String uniqueId;
            for (byte index=0;index<size;index++) {
                fighterType = buff.readByte();
                uniqueId = buff.readString();
                FighterEntity fighter = new FighterEntity(fighterType, uniqueId);
                fighter.readFromBuff(buff);
                fighterEntityList.add(fighter);
            }
            setFighterEntityList(fighterEntityList);
        }
        short sizeShort = buff.readShort();
        if (sizeShort != 0) {
            Map<String, Byte> blockStatusMap = new HashMap<>();
            for (int i = 0; i < sizeShort; i++) {
                DynamicBlock dynamicBlock = new DynamicBlock();
                dynamicBlock.readFromBuff(buff);
                blockStatusMap.put(dynamicBlock.getUnniqueId(), buff.readByte());
            }
            addBlockStatusMap(blockStatusMap);
        }
    }

    public void setLimitTime(int limitTime) {
        this.limitTime = limitTime;
    }

	public void setCountdownOfBegin(int countdownOfBegin) {
		this.countdownOfBegin = countdownOfBegin;
	}

    @Override
    public void setBlockMap(Map<String, DynamicBlock> blockMap) {
        super.setBlockMap(blockMap);
        //pk场景默认开启
        Map<String, Byte> resultMap = new HashMap<>();
        for (DynamicBlock dynamicBlock : blockMap.values()) {
            resultMap.put(dynamicBlock.getUnniqueId(), SceneManager.BLOCK_OPEN);
        }
        addBlockStatusMap(resultMap);
    }
}
