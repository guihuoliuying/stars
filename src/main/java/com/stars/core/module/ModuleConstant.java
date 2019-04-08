package com.stars.core.module;
/**
 * 模块常量
 * Created by panzhenfeng on 2016/6/24.
 */
public enum ModuleConstant {
//    Equipment(10500, "equipment", "装备"),
//    Login(10071, "login", "登录"),
//    SkyTower(30000, "skytower", "镇妖塔"),
//    SearchTreasure(10781, "searchtreasure", "仙山寻宝"),
//    GameCave(30050, "gamecave", "洞府"),
//    Mind(30600, "mind", "心法"),
//    Fashion(30700, "fashion", "时装"),
//    LootTreasure(30130, "loottreasure", "野外夺宝"),
//    Bonfire(10733, "family.act.bonfire", "家族篝火"),

//    Achievement(30181,"achievement","成就"),
    ;

    private int clientSystemConstant;
    private String moduleName;
    private String moduleNameZh;


    private ModuleConstant(int clientSystemConstant, String moduleName, String moduleNameZh) {
        this.clientSystemConstant = clientSystemConstant;
        this.moduleName = moduleName;
        this.moduleNameZh = moduleNameZh;
    }

    public int getClientSystemConstant() {
        return clientSystemConstant;
    }

    public String getModuleName() {
        return moduleName;
    }

    public String getModuleNameZh() {
        return moduleNameZh;
    }

}
