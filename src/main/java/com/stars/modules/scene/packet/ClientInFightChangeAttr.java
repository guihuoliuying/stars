package com.stars.modules.scene.packet;

import com.stars.core.attr.Attribute;
import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.scene.ScenePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * 战斗过程中更改玩家属性;
 * Created by panzhenfeng on 2016/8/25.
 */
public class ClientInFightChangeAttr extends PlayerPacket {
    //增加的属性值, TODO 之后玩家属性做成同步后,这里改为加成后的值;
    private Attribute addedAttr = new Attribute();
    private String hintStr = "";

    public ClientInFightChangeAttr() {

    }

    public boolean isValid(){
        return !addedAttr.isAllZero();
    }

    /**
     * 添加额外的属性值;
     * @param index
     * @param value
     */
    public void addAddedAttrValue(byte index, int value){
        addedAttr.addSingleAttr(index, value);
    }

    public void setHintStr(String value){
        hintStr = value;
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return ScenePacketSet.C_INFIGHT_CHANGE_ATTR;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        addedAttr.writeToBuffer(buff);
        buff.writeString(hintStr);
    }
}

