package com.stars.modules.baby.prodata;

import com.stars.core.attr.Attribute;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.StringUtil;

import java.util.Map;

/**
 * Created by chenkeyu on 2017-07-20.
 */
public class BabyVo {
    private int id;
    private int stage;
    private int level;
    private int rate;
    private int progress;
    private String normalBringUp;
    private String payBringUp;
    private String attr;
    private String extraAttr;
//    private int monsterId;

    private int normalItemId;
    private int normalCount;
    private int payItemId;
    private int payCount;
//    private MonsterVo useMonsterVo;// 使用怪物模型对象

    public void writeToBuff0(com.stars.network.server.buffer.NewByteBuffer buff) {
//        buff.writeInt(id);          //id
        buff.writeInt(progress);    //当前阶段的满进度
        buff.writeString(normalBringUp);//普通培养的itemId+count
        buff.writeString(payBringUp);//付费培养的itemId+count
    }

    public void writeToBuff1(NewByteBuffer buff) {

    }

    private Attribute attribute = new Attribute();
    private Attribute extraAttribute = new Attribute();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStage() {
        return stage;
    }

    public void setStage(int stage) {
        this.stage = stage;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getRate() {
        return rate;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public String getNormalBringUp() {
        return normalBringUp;
    }

    public void setNormalBringUp(String normalBringUp) {
        this.normalBringUp = normalBringUp;
        String[] tmp = normalBringUp.split("\\+");
        this.normalItemId = Integer.parseInt(tmp[0]);
        this.normalCount = Integer.parseInt(tmp[1]);
    }

    public String getPayBringUp() {
        return payBringUp;
    }

    public void setPayBringUp(String payBringUp) {
        this.payBringUp = payBringUp;
        String[] tmp = payBringUp.split("\\+");
        this.payItemId = Integer.parseInt(tmp[0]);
        this.payCount = Integer.parseInt(tmp[1]);
    }

    public String getAttr() {
        return attr;
    }

    public void setAttr(String attr) {
        this.attr = attr;
        if (attr == null || attr.equals("") || attr.equals("0")) return;
        Map<String, Integer> attrMap = StringUtil.toMap(attr, String.class, Integer.class, '=', ',');
        for (Map.Entry<String, Integer> entry : attrMap.entrySet()) {
            attribute.setSingleAttr(entry.getKey(), entry.getValue());
        }
    }

    public String getExtraAttr() {
        return extraAttr;
    }

    public void setExtraAttr(String extraAttr) {
        this.extraAttr = extraAttr;
        if (extraAttr == null || extraAttr.equals("") || extraAttr.equals("0")) return;
        Map<String, Integer> attrMap = StringUtil.toMap(extraAttr, String.class, Integer.class, '=', ',');
        for (Map.Entry<String, Integer> entry : attrMap.entrySet()) {
            extraAttribute.setSingleAttr(entry.getKey(), entry.getValue());
        }
    }

    public Attribute getExtraAttribute() {
        return extraAttribute;
    }

    public Attribute getAttribute() {
        return attribute;
    }

    public int getNormalItemId() {
        return normalItemId;
    }

    public int getNormalCount() {
        return normalCount;
    }

    public int getPayItemId() {
        return payItemId;
    }

    public int getPayCount() {
        return payCount;
    }

    @Override
    public String toString() {
        return "BabyVo{" +
                "id=" + id +
                ", stage=" + stage +
                ", level=" + level +
                ", rate=" + rate +
                ", progress=" + progress +
                ", normalBringUp='" + normalBringUp + '\'' +
                ", payBringUp='" + payBringUp + '\'' +
                ", attr='" + attr + '\'' +
                '}';
    }
}
