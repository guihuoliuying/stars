package com.stars.core.attr;


import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.LogUtil;
import com.stars.util.StringUtil;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Created by jx on 2015/3/31.
 */
public class Attribute implements Cloneable, Serializable {

    private final static int attrLength = Attr.values().length + AttrExt.values().length * 10;

    private int fightNum;//战力

    private int[] attributes;//属性数组

    public int[] getAttributes() {
        return attributes;
    }

    public void setAttributes(int[] attributes) {
        this.attributes = attributes;
    }

    public Attribute() {
        this.attributes = new int[attrLength];
    }

    /**
     * 以字符串的格式初始化属性
     * 格式1:attack=10;defence=100
     * 格式2:0=10,1=100
     */
    public Attribute(String attrStr) {
        this.attributes = new int[attrLength];
        strToAttribute(attrStr);
    }

    public Attribute(Attribute attr) {
        this.attributes = new int[attrLength];
        int index = 0;
        for (int value : attr.getAttributes()) {
            attributes[index] = value;
            index = index + 1;
        }
    }

    @Override
    public String toString() {
        return "Attribute{" +
                "attributes=" + Arrays.toString(attributes) +
                '}';
    }

    public boolean isAllZero() {
        for (int atr : attributes) {
            if (atr > 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * 拆分属性
     * 属性id
     *
     * @param atr 1=100;2=200 || power=100;agile=200
     */
    public void strToAttribute(String atr) {
        if (atr != null && atr.trim().length() > 0) {
            if (atr.contains(";")) {
                atr = atr.replace(";", ",");
            }
            String atrs[] = atr.split(",");
            for (byte i = 0; i < atrs.length; i++) {
                setValue(atrs[i].split("="));
            }
        }
    }

    /**
     * 获得属性的字符串
     *
     * @return
     */
    public String getAttributeStr() {
        StringBuffer bufStr = new StringBuffer();
        for (byte i = 0; i < this.attributes.length; i++) {
            if (this.attributes[i] > 0) {
                bufStr.append(Attr.getAttrNameByIndex(i)).append("=").append(this.attributes[i]).append(",");
            }
        }
        if (bufStr.length() > 0)
            bufStr.deleteCharAt(bufStr.lastIndexOf(","));
        return bufStr.toString();
    }

    /**
     * 设置值
     *
     * @param atrs
     */
    public void setValue(String[] atrs) {
        if (atrs == null || atrs.length != 2) return;
        boolean isNum = StringUtil.isNumeric(atrs[0]);
        int attrId;
        if (isNum) {
            attrId = Byte.parseByte(atrs[0]);
        } else {
            attrId = Attr.getIndexByteEn(atrs[0]);
        }
        int value = 0;
        try {
            value = Integer.parseInt(atrs[1]);
        } catch (NumberFormatException e) {
            LogUtil.error(e.getMessage(), e);
        }
        attrId = mappingIndex(attrId);
        if (attrId >= 0 && attrId < attrLength)
            //重复属性要支持叠加
            this.attributes[attrId] = this.attributes[attrId] + value;
        else
            LogUtil.error("Attribute找不到属性对应下标,atrs={}", atrs[0]);
    }

    public int get(Attr attrType) {
        if (attrType.getIndexId() >= 0 && attrType.getIndexId() < attributes.length) {
            return attributes[attrType.getIndexId()];
        }
        return 0;
    }

    /**
     * 根据属性名获得属性值
     *
     * @param attrName
     * @return 错误的属性名返回-1
     */
    public int get(String attrName) {
        int index = Attr.getIndexByteEn(attrName);
        if (index < 0) {
            LogUtil.error("属性名={}的属性不存在", attrName, new Throwable());
            return -1;
        }
        return attributes[index];
    }

    /**
     * 增加所有属性
     *
     * @param attribute
     */
    public void addAttribute(Attribute attribute) {
        if (attribute != null) {
            int temps[] = attribute.getAttributes();
            for (byte i = 0; i < temps.length; i++) {
                addSingleAttr(i, temps[i]);
            }
        }
    }
    /**
     *减少所有属性
     *
     * @param attribute
     */
    public void subAttribute(Attribute attribute) {
        if (attribute != null) {
            int temps[] = attribute.getAttributes();
            for (byte i = 0; i < temps.length; i++) {
                addSingleAttr(i, -temps[i]);
            }
        }
    }
    /**
     * 增加属性
     *
     * @param attribute
     * @param rate      比例
     * @param base      基数
     */
    public void addAttribute(Attribute attribute, int rate, int base) {
        if (attribute != null) {
            int temps[] = attribute.getAttributes();
            for (byte i = 0; i < temps.length; i++) {
                addSingleAttr(i, temps[i] * rate / base);
            }
        }
    }

    /**
     * 增加单个属性值
     *
     * @param index
     * @param value
     */
    public void addSingleAttr(int index, int value) {
        index = mappingIndex(index);
        if (index < 0 || index >= attrLength || value == 0) return;
        this.attributes[index] += value;
    }

    public void addSingleAttr(String attrName, int value) {
        int index = Attr.getIndexByteEn(attrName);
        addSingleAttr(index, value);
    }

    public void setSingleAttr(int index, int value) {
        index = mappingIndex(index);
        if (index < 0 || index >= attrLength || value == 0) return;
        this.attributes[index] = value;
    }

    public void setSingleAttr(String attrName, int value) {
        int index = Attr.getIndexByteEn(attrName);
        setSingleAttr(index, value);
    }

    public void writeToBuffer(NewByteBuffer buff) {
        byte count = getNotZeroAttrNum();
        buff.writeByte(count);
        if (count > 0) {
            for (byte i = 0; i < this.attributes.length; i++) {
                if (this.attributes[i] != 0) {
                    buff.writeShort(i);
                    buff.writeInt(this.attributes[i]);
                }
            }
        }
    }

    public void readFightAtrFromBuffer(NewByteBuffer buff) {
        byte count = buff.readByte();
        if (count > 0) {
            short key;
            for (byte i = 0; i < count; i++) {
                key = buff.readShort();
                this.attributes[key] = buff.readInt();
            }
        }
    }

    public Attribute clone() {
        Attribute newAttr = null;
        try {
            newAttr = (Attribute) super.clone();
        } catch (CloneNotSupportedException e) {
            LogUtil.error(e.getMessage(), e);
        }
        newAttr.setAttributes(null);
        if (this.attributes == null) return newAttr;
        int newAttributes[] = Arrays.copyOf(attributes, attributes.length);
        newAttr.setAttributes(newAttributes);
        return newAttr;
    }

    public void setFightNum(int fightNum) {
        this.fightNum = fightNum;
    }

    /**
     * 获得战斗力
     */
    public int getFightNum() {
        return this.fightNum;
    }

    /**
     * 获得有效属性长度
     */
    public byte getNotZeroAttrNum() {
        if (this.attributes == null)
            return 0;
        byte count = 0;
        for (byte i = 0; i < this.attributes.length; i++) {
            if (this.attributes[i] != 0) {
                count++;
            }
        }
        return count;
    }

    /**
     * 获得第一个不为0的属性index
     */
    public byte getFirstNotZeroAttrIndex() {
        if (this.attributes == null) return 0;
        for (byte i = 0; i < this.attributes.length; i++) {
            if (this.attributes[i] != 0) {
                return i;
            }
        }
        return 0;
    }

    /**
     * 发送属性描述
     */
//    public void attrContent(long roleId){
//        StringBuilder sb = new StringBuilder();
//        for (byte attrId = 0; attrId < attributes.length; attrId++) {
//            int attr = attributes[attrId];
//             if (attr > 0){
//                 if(sb.length() > 0){
//                     sb.append(",");
//                 }
//                 sb.append(Attr.getNameMap().get(attrId))
//                         .append("+").append(attr);
//             }
//        }
//        if(sb.length() > 0){
//            PacketManager.send(roleId, new ClientText(sb.toString()));
//        }
//    }

    /**
     * 映射index
     *
     * @param index
     * @return
     */
    private int mappingIndex(int index) {
        if (index >= 100) {
            return index - 100 + Attr.values().length;
        }
        return index;
    }

    /**
     * 替换自身属性值
     *
     * @param replaceAttribute 替换值
     */
    public void replaceAttr(Attribute replaceAttribute) {
        for (int i = 0; i < replaceAttribute.getAttributes().length; i++) {
            int value = replaceAttribute.getAttributes()[i];
            if (value != 0) {// 没有配置值的不替换
                getAttributes()[i] = value;
            }
        }
    }

    public int getAttack() {
        return this.attributes[Attr.ATTACK.getIndexId()];
    }

    public void setAttack(int attack) {
        this.attributes[Attr.ATTACK.getIndexId()] = attack;
    }

    public int getDefense() {
        return this.attributes[Attr.DEFENSE.getIndexId()];
    }

    public void setDefense(int defense) {
        this.attributes[Attr.DEFENSE.getIndexId()] = defense;
    }

    public int getHp() {
        return this.attributes[Attr.HP.getIndexId()];
    }

    public void setHp(int hp) {
        this.attributes[Attr.HP.getIndexId()] = hp;
    }

    public int getMp() {
        return this.attributes[Attr.MP.getIndexId()];
    }

    public void setMp(int mp) {
        this.attributes[Attr.MP.getIndexId()] = mp;
    }

    public int getHit() {
        return this.attributes[Attr.HIT.getIndexId()];
    }

    public void setHit(int hit) {
        this.attributes[Attr.HIT.getIndexId()] = hit;
    }

    public int getAvoid() {
        return this.attributes[Attr.AVOID.getIndexId()];
    }

    public void setAvoid(int avoid) {
        this.attributes[Attr.AVOID.getIndexId()] = avoid;
    }

    public int getCrit() {
        return this.attributes[Attr.CRIT.getIndexId()];
    }

    public void setCrit(int crit) {
        this.attributes[Attr.CRIT.getIndexId()] = crit;
    }

    public int getAnticrit() {
        return this.attributes[Attr.ANTICRIT.getIndexId()];
    }

    public void setAnticrit(int anticrit) {
        this.attributes[Attr.ANTICRIT.getIndexId()] = anticrit;
    }

    public int getCrithurtadd() {
        return this.attributes[Attr.CRITHURTADD.getIndexId()];
    }

    public void setCrithurtadd(int piece) {
        this.attributes[Attr.CRITHURTADD.getIndexId()] = piece;
    }

    public int getCrithurtreduce() {
        return this.attributes[Attr.CRITHURTREDUCE.getIndexId()];
    }

    public void setCrithurtreduce(int armor) {
        this.attributes[Attr.CRITHURTREDUCE.getIndexId()] = armor;
    }

    public int getMaxhp() {
        return this.attributes[Attr.MAXHP.getIndexId()];
    }

    public void setMaxhp(int maxhp) {
        this.attributes[Attr.MAXHP.getIndexId()] = maxhp;
    }
}