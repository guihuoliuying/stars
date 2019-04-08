package com.stars.modules.gem.prodata;

import com.stars.core.attr.Attribute;
import com.stars.core.attr.FormularUtils;

/**
 * 宝石等级配置数据项;
 * Created by panzhenfeng on 2016/7/25.
 */
public class GemLevelVo implements Comparable{
    private int itemId;
    private byte type;
    private String name;
    private int level;
    private String attribute;
    private String compoundmaterial;
    private String levelupmaterial; //这个字段无用了,因为之后装备要修改,这里就暂时不去掉了;
    private int levellimit;

    // 属性对象
    private Attribute attributePacked = new Attribute();
    private int fightScore = 0;

    public Attribute getAttributeattribute() {
        return attributePacked;
    }

    public String getPackedString() {
        StringBuffer sb = new StringBuffer();
        sb.append(itemId);
        sb.append(";");
        sb.append(type);
        sb.append(";");
        sb.append(name);
        sb.append(";");
        sb.append(level);
        sb.append(";");
        sb.append(attribute);
        sb.append(";");
        sb.append(compoundmaterial);
        sb.append(";");
        sb.append(levelupmaterial);
        sb.append(";");
        sb.append(fightScore);
        sb.append(";");
        sb.append(levellimit);
        return  sb.toString();
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
        attributePacked.strToAttribute(this.attribute);
        this.setFightScore(FormularUtils.calFightScore(attributePacked));
    }

    public String getCompoundmaterial() {
        return compoundmaterial;
    }

    public void setCompoundmaterial(String compoundmaterial) {
        this.compoundmaterial = compoundmaterial;
    }

    public String getLevelupmaterial() {
        return levelupmaterial;
    }

    public void setLevelupmaterial(String levelupmaterial) {
        this.levelupmaterial = levelupmaterial;
    }


    @Override
    public int compareTo(Object o) {
        if(o instanceof GemLevelVo){
            GemLevelVo tmpVo = (GemLevelVo)o;
            if(tmpVo.getFightScore() > this.fightScore){
                return 1;
            }else if(tmpVo.getFightScore() == this.fightScore){
                if(tmpVo.getLevel()>this.level){
                    return 1;
                }else if(tmpVo.getLevel() == this.level){
                    return  0;
                }else{
                    return -1;
                }
            }
        }
        return  -1;
    }

    public int getLevellimit() {
        return levellimit;
    }

    public void setLevellimit(int levellimit) {
        this.levellimit = levellimit;
    }

    public int getFightScore() {
        return fightScore;
    }

    public void setFightScore(int fightScore) {
        this.fightScore = fightScore;
    }
}
