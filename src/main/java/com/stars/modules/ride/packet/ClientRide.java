package com.stars.modules.ride.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.ride.RidePacketSet;
import com.stars.modules.ride.userdata.RoleRidePo;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Created by zhaowenshuo on 2016/9/18.
 */
public class ClientRide extends PlayerPacket {

    public static final byte RESP_SYNC = 0x00; // 用于登录时同步数据（坐骑等级/是否拥有）
    public static final byte RESP_ACTIVE = 0x01; // 用于同步骑乘状态
    public static final byte RESP_GET = 0x02; // 通知获得坐骑
    public static final byte RESP_VIEW = 0x03; // 坐骑界面
    public static final byte RESP_UPDATE_SHOW = 0x04; // 坐骑界面
    public static final byte RESP_OVER_TIME = 0x05; // 坐骑过期

    public static final byte RESP_UPGRADE_ONE = 0x10;
    public static final byte RESP_UPGRADE_TEN = 0x11;

    public static final byte RESP_UPGRADE_AWAKE_LEVEL_ONE = 0x20;

    public byte subtype;
    public int rideId;
    public int activeRideId;
    public RoleRidePo ridePo;
    public Map<Integer, RoleRidePo> ridePoMap;
    private int prevStage;
    public int prevLevel;
    private int currStage;
    public int currLevel;
    private int awakeLevel; // 觉醒等级
    private byte getType;
    private Map<Integer, Integer> toolMap = new HashMap<Integer, Integer>();
    private String rideName;

    public ClientRide() {
    }

    public ClientRide(byte subtype) {
        this.subtype = subtype;
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return RidePacketSet.C_RIDE;
    }

    @Override
    public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeByte(subtype);
        switch (subtype) {
            case RESP_SYNC:
                writeSync(buff);
                break;
            case RESP_ACTIVE:
                writeActive(buff);
                break;
            case RESP_GET:
                writeGet(buff);
                break;
            case RESP_VIEW:
                writeView(buff);
                break;
            case RESP_UPDATE_SHOW:
                writeView(buff);
                break;
            case RESP_UPGRADE_ONE:
                writeUpgradeOne(buff);
                break;
            case RESP_UPGRADE_TEN:
                writeUpgradeTen(buff);
                break;
            case RESP_OVER_TIME:
            	buff.writeString(rideName);
            	buff.writeInt(rideId);
            	break;
            case RESP_UPGRADE_AWAKE_LEVEL_ONE:
                buff.writeInt(rideId);
                buff.writeInt(awakeLevel);
                break;
        }
    }
    
    private void writeSync(com.stars.network.server.buffer.NewByteBuffer buff) {
        for (RoleRidePo ridePo : ridePoMap.values()) {
            ridePo.writeToBuffer(buff);
        }
    }

    private void writeActive(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeInt(activeRideId);
    }

    private void writeGet(com.stars.network.server.buffer.NewByteBuffer buff) {
        ridePo.writeToBuffer(buff);
        buff.writeByte(getType);
        int size = toolMap.size();
        buff.writeByte((byte)size);
        Entry<Integer, Integer> entry = null;
        if(size>0){
        	Iterator<Entry<Integer, Integer>> iterator = toolMap.entrySet().iterator();
        	for(;iterator.hasNext();){
        		entry = iterator.next();
        		buff.writeInt(entry.getKey());//道具id
        		buff.writeInt(entry.getValue());//道具数量
        	}
        }
    }

    private void writeView(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeByte((byte) ridePoMap.size());
        for (RoleRidePo ridePo : ridePoMap.values()) {
            ridePo.writeToBuffer(buff);
        }
    }

    private void writeUpgradeOne(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeInt(prevLevel);
        buff.writeInt(currLevel);
    }

    private void writeUpgradeTen(NewByteBuffer buff) {
        buff.writeInt(prevLevel);
        buff.writeInt(currLevel);
    }

    public int getRideId() {
        return rideId;
    }

    public void setRideId(int rideId) {
        this.rideId = rideId;
    }

    public int getActiveRideId() {
        return activeRideId;
    }

    public void setActiveRideId(int activeRideId) {
        this.activeRideId = activeRideId;
    }

    public RoleRidePo getRidePo() {
        return ridePo;
    }

    public void setRidePo(RoleRidePo ridePo) {
        this.ridePo = ridePo;
    }

    public Map<Integer, RoleRidePo> getRidePoMap() {
        return ridePoMap;
    }

    public void setRidePoMap(Map<Integer, RoleRidePo> ridePoMap) {
        this.ridePoMap = ridePoMap;
    }

    public int getPrevLevel() {
        return prevLevel;
    }

    public void setPrevLevel(int prevLevel) {
        this.prevLevel = prevLevel;
    }

    public int getCurrLevel() {
        return currLevel;
    }

    public void setCurrLevel(int currLevel) {
        this.currLevel = currLevel;
    }

	public int getPrevStage() {
		return prevStage;
	}

	public void setPrevStage(int prevStage) {
		this.prevStage = prevStage;
	}

	public int getCurrStage() {
		return currStage;
	}

	public void setCurrStage(int currStage) {
		this.currStage = currStage;
	}

	public byte getGetType() {
		return getType;
	}

	public void setGetType(byte getType) {
		this.getType = getType;
	}

	public Map<Integer, Integer> getToolMap() {
		return toolMap;
	}

	public void setToolMap(Map<Integer, Integer> toolMap) {
		this.toolMap = toolMap;
	}

	public String getRideName() {
		return rideName;
	}

	public void setRideName(String rideName) {
		this.rideName = rideName;
	}

    public int getAwakeLevel() {
        return awakeLevel;
    }

    public void setAwakeLevel(int awakeLevel) {
        this.awakeLevel = awakeLevel;
    }
}
