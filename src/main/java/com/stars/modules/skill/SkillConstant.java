package com.stars.modules.skill;

/**
 * Created by daiyaorong on 2016/8/8.
 */
public class SkillConstant {

    // 怪物技能等级
    public static final int MONSTER_SKILL_LEVEL = 1;

    //skilllvup的skilltype类型为被动技能
    public static final int LVUP_SKILLTYPE_PASS = 1;//这个是被动技能
    public static final int TRUMP_SKILLTYPE_PASS = 3;//这个是法宝被动技能
    public static final int ACTIVE_SKILL = 0;//主动技能
    public static final int UNIQUE_SKILL = 2;//绝技
    public static final int FASHIONCARD_SKILLTYPE_PASS = 4;//操蛋的时装技能

    //被动技能效果类型
    //修改角色属性
    public static final int PASS_EFFECT_ATTRIBUTE = 1;
    //加buff
    public static final int PASS_EFFECT_BUFF    = 2;
    // 普攻等级
    public static final int ROLE_NORMAL_LEVEL = 1;

    //技能类型
    //普攻
    public static final byte TYPE_NORMAL = 1;
    //小技能
    public static final byte TYPE_SKILL = 2;
    //闪避
    public static final byte TYPE_AVOID = 3;
    //大招
    public static final byte TYPE_ULTIMATE = 4;
}
