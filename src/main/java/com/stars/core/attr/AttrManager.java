package com.stars.core.attr;






/**
 * Created by jx on 2015/4/3.
 */
public class AttrManager {

    /**
     * 计算属性
     * @param schoolType
     * @param level
     * @param attribute
     */
    public static Attribute computeAttribute(byte schoolType,short level, Attribute attribute){
        Attribute newAttribute = createAttribute(level, attribute);
        converAttribute(schoolType, newAttribute);
        return newAttribute;
    }


    /**
     * 计算属性
     * @param schoolType
     * @param attribute
     */
    public static Attribute computeAttribute(byte schoolType, Attribute attribute){
        Attribute newAttribute = createAttribute(attribute);
        converAttribute(schoolType, newAttribute);
        return newAttribute;
    }

    /**
     * 计算属性
     * @param tempAttr
     * @param attribute
     */
    public static Attribute computeAttribute(Attribute tempAttr, Attribute attribute){
        Attribute newAttribute = new Attribute();
//        newAttribute.setPower(attribute.getPower() - tempAttr.getPower());
//        newAttribute.setAgile(attribute.getAgile() - tempAttr.getAgile());
//        newAttribute.setWit(attribute.getWit() - tempAttr.getWit());
//        newAttribute.setHp(attribute.getHp() - tempAttr.getHp());
//        newAttribute.setAtt(attribute.getAtt() - tempAttr.getAtt());
//        newAttribute.setPhysicDef(attribute.getPhysicDef() - tempAttr.getPhysicDef());
//        newAttribute.setMagicDef(attribute.getMagicDef() - tempAttr.getMagicDef());
//        newAttribute.setCrit(attribute.getCrit() - tempAttr.getCrit());
//        newAttribute.setResi(attribute.getResi() - tempAttr.getResi());
//        newAttribute.setPowerGrow(attribute.getPowerGrow() - tempAttr.getPowerGrow());
//        newAttribute.setAgileGrow(attribute.getAgileGrow() - tempAttr.getAgileGrow());
//        newAttribute.setWitGrow(attribute.getWitGrow() - tempAttr.getWitGrow());
        return newAttribute;
    }


    /**
     * 转换属性
     * @param schoolType
     * @param newAttribute
     */
    private static void converAttribute(byte schoolType, Attribute newAttribute) {
//        if(newAttribute.getPower() > 0){
//            powerAttr(schoolType, newAttribute.getPower(), newAttribute);
//        }
//        if (newAttribute.getAgile() > 0){
//            agileAttr(schoolType, newAttribute.getAgile(), newAttribute);
//        }
//        if (newAttribute.getWit() > 0){
//            witAttr(schoolType, newAttribute.getWit(), newAttribute);
//        }
    }


    /**
     * 创建新属性对象（计算赋值后的）
     * @param level
     * @param attribute
     */
    private static Attribute createAttribute(short level, Attribute attribute){
//        int power = getAttrNum(attribute.getPower(),attribute.getPowerGrow(), level);
//        int agile = getAttrNum(attribute.getAgile(),attribute.getAgileGrow(), level);
//        int wit = getAttrNum(attribute.getWit(),attribute.getWitGrow(), level);
//
        Attribute newAttribute = new Attribute();
//        newAttribute.setPower(power);
//        newAttribute.setAgile(agile);
//        newAttribute.setWit(wit);
//        newAttribute.setHp(attribute.getHp());
//        newAttribute.setAtt(attribute.getAtt());
//        newAttribute.setPhysicDef(attribute.getPhysicDef());
//        newAttribute.setMagicDef(attribute.getMagicDef());
//        newAttribute.setCrit(attribute.getCrit());
//        newAttribute.setResi(attribute.getResi());
//        newAttribute.setPowerGrow(attribute.getPowerGrow());
//        newAttribute.setAgileGrow(attribute.getAgileGrow());
//        newAttribute.setWitGrow(attribute.getWitGrow());
        return newAttribute;
    }

    /**
     * 修正数据
     * @param alterNum
     * @param attribute
     */
    public static void updateAttribute(double alterNum, Attribute attribute){
//        attribute.setPower((int)(attribute.getPower() * alterNum));
//        attribute.setAgile((int)(attribute.getAgile() * alterNum));
//        attribute.setWit((int)(attribute.getAgile() * alterNum));
//        attribute.setHp((int)(attribute.getHp() * alterNum));
//        attribute.setAtt((int)(attribute.getAtt() * alterNum));
//        attribute.setPhysicDef((int)(attribute.getPhysicDef() * alterNum));
//        attribute.setMagicDef((int)(attribute.getMagicDef() * alterNum));
//        attribute.setCrit((int)(attribute.getCrit() * alterNum));
//        attribute.setResi((int)(attribute.getResi() * alterNum));
//        attribute.setPowerGrow((int)(attribute.getPowerGrow() * alterNum));
//        attribute.setAgileGrow((int)(attribute.getAgileGrow() * alterNum));
//        attribute.setWitGrow((int)(attribute.getWitGrow() * alterNum));
    }

    /**
     * 创建新属性对象（计算赋值后的）
     * @param attribute
     */
    private static Attribute createAttribute(Attribute attribute){
        Attribute newAttribute = new Attribute();
//        newAttribute.setPower(attribute.getPower());
//        newAttribute.setAgile(attribute.getAgile());
//        newAttribute.setWit(attribute.getWit());
//        newAttribute.setHp(attribute.getHp());
//        newAttribute.setAtt(attribute.getAtt());
//        newAttribute.setPhysicDef(attribute.getPhysicDef());
//        newAttribute.setMagicDef(attribute.getMagicDef());
//        newAttribute.setCrit(attribute.getCrit());
//        newAttribute.setResi(attribute.getResi());
//        newAttribute.setPowerGrow(attribute.getPowerGrow());
//        newAttribute.setAgileGrow(attribute.getAgileGrow());
//        newAttribute.setWitGrow(attribute.getWitGrow());
        return newAttribute;
    }


    /**
     * 获得计算后属性值
     * @param baseNum
     * @param growNum
     * @param roleLevel
     */
//    private static int getAttrNum(int baseNum,int growNum, short roleLevel){
//        return (int)(baseNum + Math.floor(growNum * roleLevel / HeroDataPool.INSTANCE.COMPUTE_NUM));
//    }

    /**
     * 根据智力换算其他属性
     * @param schoolType
     * @param wit
     * @param newAttribute
     */
    private static void witAttr(byte schoolType, int wit, Attribute newAttribute) {
//        if (schoolType == HeroDataPool.INSTANCE.WIT){
//            newAttribute.setAtt(newAttribute.getAtt() + wit * 1);
//        }
//        int newMagicdef =  (int)Math.floor(wit * 0.5);
//        newAttribute.setMagicDef(newAttribute.getMagicDef() + newMagicdef);
//
//        int newTough =  wit * 1;
//        newAttribute.setResi(newAttribute.getResi() + newTough);
    }


    /**
     * 根据敏捷换算其他属性
     * @param schoolType
     * @param agile
     * @param newAttribute
     */
    private static void agileAttr(byte schoolType, int agile, Attribute newAttribute) {
//        if (schoolType == HeroDataPool.INSTANCE.AGILE){
//            newAttribute.setAtt(newAttribute.getAtt() + agile * 1);
//        }
//        int newCrit =  agile * 1;
//        newAttribute.setCrit(newAttribute.getCrit() + newCrit);
//
//        int newTough =  agile * 1;
//        newAttribute.setResi(newAttribute.getResi() + newTough);
    }


    /**
     * 根据力量换算其他属性
     * @param schoolType
     * @param power
     * @param newAttribute
     */
    private static void powerAttr(byte schoolType, int power, Attribute newAttribute) {
//        if (schoolType == HeroDataPool.INSTANCE.POWER){
//            newAttribute.setAtt(newAttribute.getAtt() + power * 1);
//        }
//        int newMatter =  (int)Math.floor(power * 0.5);
//        newAttribute.setPhysicDef(newAttribute.getPhysicDef() + newMatter);
//
//        int newHp =  (int)Math.floor(power * 20);
//        newAttribute.setHp(newAttribute.getHp() + newHp);
    }
}
