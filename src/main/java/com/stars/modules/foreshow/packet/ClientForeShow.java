package com.stars.modules.foreshow.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.foreshow.ForeShowPacketSet;
import com.stars.modules.foreshow.userdata.ForeShowStatePo;
import com.stars.modules.foreshow.userdata.NextForeShowPo;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.LogUtil;

import java.util.Map;
import java.util.Set;

/**
 * Created by chenkeyu on 2016/11/1.
 */
public class ClientForeShow extends PlayerPacket {
    public static final byte foreOpenMap = 0x00;//登陆发送全部已开放的系统（包括未表现的）
    public static final byte openUnShow = 0x01;//通知客户端系统开启
    public static final byte openText = 0x03;//发送系统文本
    public static final byte isOpen = 0x04;//某个系统是否开启

    public byte subtype;

    public ClientForeShow() {
    }

    public ClientForeShow(byte subtype) {
        this.subtype = subtype;
    }

    public Map<String, ForeShowStatePo> openShowMap;
    public Map<String, ForeShowStatePo> openUnShowMap;
    public Map<String, ForeShowStatePo> map;
    public Set<String> bossOpenShow;
    private String taskName;
    private String dungeonName;
    private String worldTitle;
    private String name;
    private byte open;
    byte flag;
    private String text;
    private NextForeShowPo nextForeShowPo;
    private String shortText;

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return ForeShowPacketSet.C_FORESHOW;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeByte(subtype);
        switch (subtype) {
            case foreOpenMap:
                writeMap(buff);
                break;
            case openUnShow:
                writeOpenUnShow(buff);
                break;
            case openText:
                buff.writeString(text);
                buff.writeString(name);
                buff.writeString(shortText);
                break;
            case isOpen:
                buff.writeByte(open);
        }
    }

    private void writeMap(NewByteBuffer buff) {
        buff.writeInt(openUnShowMap.size());
        for (ForeShowStatePo foreShowStatePo : openUnShowMap.values()) {
            foreShowStatePo.writeToBuff(buff);
        }
        buff.writeInt(openShowMap.size());
        for (ForeShowStatePo foreShowStatePo : openShowMap.values()) {
            foreShowStatePo.writeToBuff(buff);
        }
        buff.writeByte((byte) bossOpenShow.size());
        for (String open : bossOpenShow) {
            buff.writeString(open);
        }
        LogUtil.info("提前开放:{}", bossOpenShow);
        if (nextForeShowPo == null) {
            buff.writeString("");
            buff.writeByte((byte) 0);
        } else {
            buff.writeString(nextForeShowPo.getOpenname());
            buff.writeByte(nextForeShowPo.isShowEffect() ? (byte) 1 : (byte) 0);
        }
    }

    private void writeOpenUnShow(NewByteBuffer buff) {
        buff.writeInt(map.size());
        for (ForeShowStatePo foreShowStatePo : map.values()) {
            foreShowStatePo.writeToBuff(buff);
        }
        buff.writeByte((byte) bossOpenShow.size());
        for (String open : bossOpenShow) {
            buff.writeString(open);
        }
//        LogUtil.info("tri提前开放:{}", bossOpenShow);
        if (nextForeShowPo == null) {
            buff.writeString("");
            buff.writeByte((byte) 0);
        } else {
            buff.writeString(nextForeShowPo.getOpenname());
            buff.writeByte(nextForeShowPo.isShowEffect() ? (byte) 1 : (byte) 0);
        }
    }

    public byte getOpen() {
        return open;
    }

    public void setOpen(byte open) {
        this.open = open;
    }

    public Map<String, ForeShowStatePo> getMap() {
        return map;
    }

    public void setMap(Map<String, ForeShowStatePo> map) {
        this.map = map;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(byte flag) {
        this.flag = flag;
    }


    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getDungeonName() {
        return dungeonName;
    }

    public void setDungeonName(String dungeonName) {
        this.dungeonName = dungeonName;
    }

    public String getWorldTitle() {
        return worldTitle;
    }

    public void setWorldTitle(String worldTitle) {
        this.worldTitle = worldTitle;
    }

    public Map<String, ForeShowStatePo> getOpenUnShowMap() {
        return openUnShowMap;
    }

    public void setOpenUnShowMap(Map<String, ForeShowStatePo> openUnShowMap) {
        this.openUnShowMap = openUnShowMap;
    }

    public Map<String, ForeShowStatePo> getOpenShowMap() {
        return openShowMap;
    }

    public void setOpenShowMap(Map<String, ForeShowStatePo> openShowMap) {
        this.openShowMap = openShowMap;
    }

    public void setBossOpenShow(Set<String> bossOpenShow) {
        this.bossOpenShow = bossOpenShow;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public NextForeShowPo getNextForeShowPo() {
        return nextForeShowPo;
    }

    public void setNextForeShowPo(NextForeShowPo nextForeShowPo) {
        this.nextForeShowPo = nextForeShowPo;
    }

    public String getShortText() {
        return shortText;
    }

    public void setShortText(String shortText) {
        this.shortText = shortText;
    }
}
