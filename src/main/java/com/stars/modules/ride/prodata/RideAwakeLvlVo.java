package com.stars.modules.ride.prodata;

import com.stars.core.attr.Attribute;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.StringUtil;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/7/5.
 */
public class RideAwakeLvlVo {

    private int rideId;
    private int awakeLevel;
    private String awakeAttr;
    private String material;
    /*  */
    private Attribute attr;
    private Map<Integer, Integer> toolMap;

    public void writeToBuffer(NewByteBuffer buf) {
        buf.writeInt(rideId);
        buf.writeInt(awakeLevel);
        buf.writeString(awakeAttr);
        buf.writeString(material);
    }

    public int getRideId() {
        return rideId;
    }

    public void setRideId(int rideId) {
        this.rideId = rideId;
    }

    public int getAwakeLevel() {
        return awakeLevel;
    }

    public void setAwakeLevel(int awakeLevel) {
        this.awakeLevel = awakeLevel;
    }

    public String getAwakeAttr() {
        return awakeAttr;
    }

    public void setAwakeAttr(String awakeAttr) {
        this.awakeAttr = awakeAttr;
        this.attr = new Attribute(awakeAttr);
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
        this.toolMap = StringUtil.toMap(material, Integer.class, Integer.class, '+', ',');
    }

    public Attribute getAttr() {
        return attr;
    }

    public Map<Integer, Integer> getToolMap() {
        return toolMap;
    }

}
