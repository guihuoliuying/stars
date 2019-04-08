package com.stars.modules.ride.prodata;

import com.stars.core.attr.Attribute;
import com.stars.core.attr.FormularUtils;
import com.stars.modules.ride.RideConst;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.DateUtil;
import com.stars.util.StringUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by zhaowenshuo on 2016/9/18.
 */
public class RideInfoVo {

    private int rideId; // id
    private String name; // 名字
    private byte skintype;//皮肤类型
    private String skintypename;//皮肤类型名
    private String attribute;//皮肤属性
    private int fightScore;//皮肤增加的战力
    private short rank;//排序
    private String model; // 模型
    private String icon; // 图标
    private int scale; // 缩放比例
    private String pose; //静止动作
    private String pose2; //行走动作
    private int getWay; // 获得途径
    private String getWayText; // 获得途径说明文档
    private int moveSpeed; // 移动速度
    private byte reqtype;//获得类型
    private String reqitem;//需要道具
    private Map<Integer, Integer> reqItemMap;
    private byte color;//头像颜色
    private int reqridelevel;//激活所需的骑术等级
    private String walkSound;
    private String effect;
    private int reqrolelevel;//需要角色达到指定等级
    private int reqviplevel;//账号需要达到指定贵族等级
    private String usetime;//坐骑限时信息 {0：永久 ； 1：有效时间（时+分+秒）； 2：截止时间（年+月+日+时+分+秒）}
    private byte timeLimitType;//限时类型
    private int continueTime;//（限时坐骑）持续时间  类型1
    private int endTime;//(限时坐骑) 截止时间  类型2
    private String resolve;

    private int resolveItemId;
    private int resolveItemCount;

    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeInt(rideId);
        buff.writeString(name);
        buff.writeString(model);
        buff.writeString(icon);
        buff.writeInt(scale);
        buff.writeString(pose);
        buff.writeString(pose2);
    }

    public int getRideId() {
        return rideId;
    }

    public void setRideId(int rideId) {
        this.rideId = rideId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public int getScale() {
        return scale;
    }

    public void setScale(int scale) {
        this.scale = scale;
    }

    public String getPose() {
        return pose;
    }

    public void setPose(String pose) {
        this.pose = pose;
    }

    public String getPose2() {
        return pose2;
    }

    public void setPose2(String pose2) {
        this.pose2 = pose2;
    }

    public int getGetWay() {
        return getWay;
    }

    public void setGetWay(int getWay) {
        this.getWay = getWay;
    }

    public String getGetWayText() {
        return getWayText;
    }

    public void setGetWayText(String getWayText) {
        this.getWayText = getWayText;
    }

    public int getMoveSpeed() {
        return moveSpeed;
    }

    public void setMoveSpeed(int moveSpeed) {
        this.moveSpeed = moveSpeed;
    }

    public byte getSkintype() {
        return skintype;
    }

    public void setSkintype(byte skintype) {
        this.skintype = skintype;
    }

    public String getSkintypename() {
        return skintypename;
    }

    public void setSkintypename(String skintypename) {
        this.skintypename = skintypename;
    }

    public short getRank() {
        return rank;
    }

    public void setRank(short rank) {
        this.rank = rank;
    }

    public byte getReqtype() {
        return reqtype;
    }

    public void setReqtype(byte reqtype) {
        this.reqtype = reqtype;
    }

    public String getReqitem() {
        return reqitem;
    }

    public void setReqitem(String reqitem) {
        this.reqitem = reqitem;
        if (StringUtil.isNotEmpty(reqitem)) {
            reqItemMap = StringUtil.toMap(reqitem, Integer.class, Integer.class, '+', ',');
        }
    }

    public int getFightScore() {
        return fightScore;
    }

    public void setFightScore(int fightScore) {
        this.fightScore = fightScore;
    }

    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
        if (attribute != null) {
            Attribute attr = new Attribute(attribute);
            this.fightScore = FormularUtils.calFightScore(attr);
        }
    }

    public Map<Integer, Integer> getReqItemMap() {
        if (reqItemMap == null) {
            reqItemMap = new ConcurrentHashMap<>();
        }
        return reqItemMap;
    }

    public void setReqItemMap(Map<Integer, Integer> reqItemMap) {
        this.reqItemMap = reqItemMap;
    }

    public byte getColor() {
        return color;
    }

    public void setColor(byte color) {
        this.color = color;
    }

    public int getReqridelevel() {
        return reqridelevel;
    }

    public void setReqridelevel(int reqridelevel) {
        this.reqridelevel = reqridelevel;
    }

    public String getWalkSound() {
        return walkSound;
    }

    public void setWalkSound(String walkSound) {
        this.walkSound = walkSound;
    }

    public String getEffect() {
        return effect;
    }

    public void setEffect(String effect) {
        this.effect = effect;
    }

    public Integer getReqrolelevel() {
        return reqrolelevel;
    }

    public void setReqrolelevel(Integer reqrolelevel) {
        this.reqrolelevel = reqrolelevel;
    }

    public Integer getReqviplevel() {
        return reqviplevel;
    }

    public void setReqviplevel(Integer reqviplevel) {
        this.reqviplevel = reqviplevel;
    }

    public boolean checkCondition(int rolelevel, int vipLevel) {
        if (rolelevel >= reqrolelevel || vipLevel >= reqviplevel) {
            return true;
        }
        return false;
    }

    public String getUsetime() {
        return usetime;
    }

    public void setUsetime(String usetime) throws Exception {
        this.usetime = usetime;
        String[] arr = usetime.split("[|]");
        this.timeLimitType = Byte.parseByte(arr[0]);
        if (this.timeLimitType == RideConst.TIME_LIMIT_TYPE0) {

        } else if (this.timeLimitType == RideConst.TIME_LIMIT_TYPE1) {
            int[] timeArr = StringUtil.toArray(arr[1], int[].class, '+');
            continueTime += timeArr[0] * 3600;
            if (timeArr.length > 1) {
                continueTime += timeArr[1] * 60;
            }
            if (timeArr.length > 2) {
                continueTime += timeArr[2];
            }
        } else if (this.timeLimitType == RideConst.TIME_LIMIT_TYPE2) {
//			int[] timeArr = StringUtil.toArray(arr[1], int[].class, '+');
            this.endTime = (int) (DateUtil.toDate(arr[1]).getTime() / 1000);
        }
    }

    public byte getTimeLimitType() {
        return timeLimitType;
    }

    public void setTimeLimitType(byte timeLimitType) {
        this.timeLimitType = timeLimitType;
    }

    public int getContinueTime() {
        return continueTime;
    }

    public void setContinueTime(int continueTime) {
        this.continueTime = continueTime;
    }

    public int getEndTime() {
        return endTime;
    }

    public void setEndTime(int endTime) {
        this.endTime = endTime;
    }

    public String getResolve() {
        return resolve;
    }

    public void setResolve(String resolve) {
        this.resolve = resolve;
        if (resolve == null || resolve.equals("") || resolve.equals("0")) return;
        String[] tmp = resolve.split("\\+");
        this.resolveItemId = Integer.parseInt(tmp[0]);
        this.resolveItemCount = Integer.parseInt(tmp[1]);
    }

    public int getResolveItemId() {
        return resolveItemId;
    }

    public int getResolveItemCount() {
        return resolveItemCount;
    }
}
