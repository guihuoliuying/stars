package com.stars.modules.scene.prodata;

import com.stars.network.server.buffer.NewByteBuffer;

/**
 * 动态阻挡对象
 * Created by liuyuheng on 2016/7/6.
 */
public class DynamicBlock {
    private String unniqueId;
    private int showSpawnId;
    private int hideSpawnId;
    private String resource;// 资源名
    private int x;
    private int y;
    private int z;
    private int angle;
    private int sizeX;// x尺寸
    private int sizeY;// y尺寸

    public DynamicBlock() {
    }

    public DynamicBlock(String unniqueId, String block) {
        String[] temp = block.split("\\+");
        this.unniqueId = unniqueId;
        this.showSpawnId = Integer.parseInt(temp[0]);
        this.hideSpawnId = Integer.parseInt(temp[1]);
        this.resource = temp[2];
        this.x = Integer.parseInt(temp[3]);
        this.y = Integer.parseInt(temp[4]);
        this.z = Integer.parseInt(temp[5]);
        this.angle = Integer.parseInt(temp[6]);
        this.sizeX = Integer.parseInt(temp[7]);
        this.sizeY = Integer.parseInt(temp[8]);
    }

    public void writeToBuff(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeString(unniqueId);
        buff.writeInt(x);
        buff.writeInt(y);
        buff.writeInt(z);
        buff.writeInt(angle);
        buff.writeString(resource);
        buff.writeInt(sizeX);
        buff.writeInt(sizeY);
    }

    public void readFromBuff(NewByteBuffer buff) {
        this.unniqueId = buff.readString();
        this.x = buff.readInt();
        this.y = buff.readInt();
        this.z = buff.readInt();
        this.angle = buff.readInt();
        this.resource = buff.readString();
        this.sizeX = buff.readInt();
        this.sizeY = buff.readInt();
    }

    public String getUnniqueId() {
        return unniqueId;
    }

    public int getShowSpawnId() {
        return showSpawnId;
    }

    public int getHideSpawnId() {
        return hideSpawnId;
    }
}
