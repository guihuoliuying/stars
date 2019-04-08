package com.stars.modules.gamecave.tinygame;

/**
 * 小游戏的枚举,方便添加配置参数;
 * Created by gaopeidian on 2017/1/13.
 */
public enum TinyGameEnum {
    Answer(TinyGameBase.AnswerType, TinyGameAnswer.class, false), //答题;
    Archer(TinyGameBase.ArcherType, TinyGameArcher.class, false), //射箭;
            ;

    private byte tinygameType ;
    private Class<? extends  TinyGameBase> tinygameClass;
    private boolean isTimeLimit;


    /**
     *
     * @param tinygameType 小游戏类型;
     * @param tinygameClass 小游戏对应的处理类;
     * @param isTimeLimit 是否需要时间限制;
     */
    private TinyGameEnum(byte tinygameType, Class<? extends TinyGameBase> tinygameClass, boolean isTimeLimit) {
        this.tinygameType = tinygameType;
        this.tinygameClass = tinygameClass;
        this.isTimeLimit = isTimeLimit;
    }

    public byte getTinygameType() {
        return tinygameType;
    }

    public boolean getIsTimeLimit(){
        return isTimeLimit;
    }

    public Class<? extends  TinyGameBase> getTinygameClass() {
        return tinygameClass;
    }

    public static TinyGameEnum getByType(byte type) {
        for (TinyGameEnum e : TinyGameEnum.values()) {
            if (e.tinygameType == type){
                return e;
            }
        }
        return null;
    }


    public static TinyGameEnum getByClass(Class<? extends  TinyGameBase> tinygameClass){
        for (TinyGameEnum e : TinyGameEnum.values()) {
            if (e.tinygameClass.equals(tinygameClass)){
                return e;
            }
        }
        return null;
    }
}

