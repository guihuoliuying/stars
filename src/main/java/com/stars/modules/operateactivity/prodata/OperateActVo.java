package com.stars.modules.operateactivity.prodata;

import com.stars.modules.operateactivity.labeldisappear.LabelDisappearBase;
import com.stars.modules.operateactivity.opentime.ActOpenTimeBase;
import com.stars.modules.operateactivity.rolelimit.ActRoleLimitBase;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by gaopeidian on 2016/12/6.
 */
public class OperateActVo implements Comparable<OperateActVo> {
    private int operateactid;
    private byte classType;
    private int type;
    private byte open;
    private String name;
    private String hotlabel;
    private String labeldisappear;
    private String serverserial;
    private int order;
    private String ruledesc;
    private String timedesc;
    private String openTime;
    private String roleLimit;
    private String resettype;
    private String openwindow;
    private String channel;
    private String showitem;
    private String showpic;
    private String gotonpc;
    private String buttondesc;
    /* 内存数据 */
    private String mResettype = "";
    private int mResettypeParam = -1;
    private ActOpenTimeBase openTimeBase = null;
    private Map<Integer, ActRoleLimitBase> roleLimitMap = new HashMap<Integer, ActRoleLimitBase>();
    private LabelDisappearBase disappear = null;
    private int beginServer = 0;
    private int endServer = 0;

    public void writeToBuff(NewByteBuffer buff) {
        buff.writeInt(operateactid);
        buff.writeByte(classType);
        buff.writeInt(type);
        buff.writeByte(open);
        buff.writeString(name);
        buff.writeString(hotlabel);
        buff.writeString(labeldisappear);
        buff.writeString(serverserial);
        buff.writeInt(order);
        buff.writeString(ruledesc);
        buff.writeString(timedesc);
        buff.writeString(openTime);
        buff.writeString(resettype);
        buff.writeString(openwindow);
        buff.writeString(channel);
        buff.writeString(showitem);
        buff.writeString(showpic);
        buff.writeString(gotonpc);
        buff.writeString(buttondesc);
    }

    public int getOperateactid() {
        return operateactid;
    }

    public void setOperateactid(int value) {
        this.operateactid = value;
    }

    public byte getClassType() {
        return classType;
    }

    public void setClassType(byte value) {
        this.classType = value;
    }

    public int getType() {
        return type;
    }

    public void setType(int value) {
        this.type = value;
    }

    public byte getOpen() {
        return open;
    }

    public void setOpen(byte value) {
        this.open = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String value) {
        this.name = value;
    }

    public String getHotlabel() {
        return hotlabel;
    }

    public void setHotlabel(String value) {
        this.hotlabel = value;
    }

    public String getLabeldisappear() {
        return labeldisappear;
    }

    public void setLabeldisappear(String value) {
        this.labeldisappear = value;
        disappear = LabelDisappearBase.newDisappearBaseByStr(this.labeldisappear);
        if (disappear == null) {
            throw new IllegalArgumentException("活动标签消失配置格式错误，请检查id=" + this.operateactid);
        }
    }

    public String getServerserial() {
        return serverserial;
    }

    public void setServerserial(String value) {
        this.serverserial = value;

        if (serverserial == null || serverserial.equals("")) {
            return;
        }

        if (serverserial.equals("0")) {
            beginServer = -1;
            endServer = -1;
            return;
        }

        String sts[] = serverserial.split("\\+");
        if (sts.length >= 2) {
            if (!sts[0].equals("")) {
                beginServer = Integer.parseInt(sts[0]);
            }
            if (!sts[1].equals("")) {
                endServer = Integer.parseInt(sts[1]);
            }
        }
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int value) {
        this.order = value;
    }

    public String getRuledesc() {
        return ruledesc;
    }

    public void setRuledesc(String value) {
        this.ruledesc = value;
    }

    public String getTimedesc() {
        return timedesc;
    }

    public void setTimedesc(String value) {
        this.timedesc = value;
    }

    public String getOpenTime() {
        return openTime;
    }

    public void setOpenTime(String value) {
        this.openTime = value;
        openTimeBase = ActOpenTimeBase.newActOpenTimeBaseByStr(this.openTime);
    }

    public String getRoleLimit() {
        return roleLimit;
    }

    public void setRoleLimit(String value) {
        this.roleLimit = value;
        roleLimitMap = ActRoleLimitBase.getOpenLimitsByStr(roleLimit);
    }

    public String getResettype() {
        return resettype;
    }

    public void setResettype(String value) {
        this.resettype = value;

        if (resettype == null || resettype.equals("") || resettype.equals("0")) {
            return;
        }
        String sts[] = resettype.split("\\,");
        if (sts.length >= 2) {
            mResettype = sts[0];

            if (!sts[1].equals("")) {
                mResettypeParam = Integer.parseInt(sts[1]);
            }
        }
    }

    public String getOpenwindow() {
        return openwindow;
    }

    public void setOpenwindow(String value) {
        this.openwindow = value;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    /* 内存数据 */
    public String getMResettype() {
        return mResettype;
    }

    public int getMResetTypeParam() {
        return mResettypeParam;
    }

    public ActOpenTimeBase getActOpenTimeBase() {
        return openTimeBase;
    }

    public Map<Integer, ActRoleLimitBase> getRoleLimitMap() {
        return roleLimitMap;
    }

    public LabelDisappearBase getDisappear() {
        return disappear;
    }

    public int getBeginServer() {
        return beginServer;
    }

    public int getEndServer() {
        return endServer;
    }

    public String getShowitem() {
        return showitem;
    }

    public void setShowitem(String showitem) {
        this.showitem = showitem;
    }

    public String getShowpic() {
        return showpic;
    }

    public void setShowpic(String showpic) {
        this.showpic = showpic;
    }

    public String getGotonpc() {
        return gotonpc;
    }

    public void setGotonpc(String gotonpc) {
        this.gotonpc = gotonpc;
    }

    public String getButtondesc() {
        return buttondesc;
    }

    public void setButtondesc(String buttondesc) {
        this.buttondesc = buttondesc;
    }

    /**
     * 按活动id从小到大排
     */
    @Override
    public int compareTo(OperateActVo o) {
        if (this.getOperateactid() < o.getOperateactid()) {
            return -1;
        } else if (this.getOperateactid() > o.getOperateactid()) {
            return 1;
        } else {
            return 0;
        }
    }
}
