package com.stars.modules.base.condition;

import com.stars.core.expr.ExprConfig;
import com.stars.modules.base.condition.dataset.activedjob.PcdsActivedJobSet;
import com.stars.modules.base.condition.dataset.dungeonpassed.PcdsDungeonPassedSet;
import com.stars.modules.base.condition.dataset.offlineHour.PcdsOfflineSet;
import com.stars.modules.base.condition.dataset.tool.PcdsToolSet;
import com.stars.modules.base.condition.func.datetime.PcfNow;
import com.stars.modules.base.condition.func.datetime.PcfToHour;
import com.stars.modules.base.condition.func.dungeon.PcfDungeonIsActived;
import com.stars.modules.base.condition.func.dungeon.PcfDungeonIsPassed;
import com.stars.modules.base.condition.func.open.PcfIsOpen;
import com.stars.modules.base.condition.value.*;
import com.stars.modules.base.condition.value.fight.*;

public class BaseExprConfig extends ExprConfig {

    public static final BaseExprConfig config = new BaseExprConfig();

    private BaseExprConfig() {

        /* 单值 */
        registerValue("level", PcvLevel.class); // 等级
        registerValue("fight", PcvFight.class); // 战力
        registerValue("gold", PcvGold.class); // 元宝
        registerValue("money", PcvMoney.class); // 金币
        registerValue("vigor", PcvVigor.class); // 体力
        registerValue("offlinehours", PcvOfflineHours.class); // 这次登陆和上次登陆的时间差（小时）
        registerValue("serverdays", PcvServerDays.class); // 开服的第几天
        registerValue("channel", PcvChannel.class); // 渠道号
        registerValue("jobid", PcvJobId.class); // 职业id
        registerValue("iswxbinded", PcvIsWxBinded.class); // 是否微信绑定
        registerValue("charge", PcvCharge.class); // 充值金额
        registerValue("fight_familyskill", PcvFightFamilySkill.class); // 战力-伙伴
        registerValue("fight_gem", PcvFightGem.class); // 战力-宝石
        registerValue("fight_equip", PcvFightEquip.class); // 战力-装备
        registerValue("fight_level", PcvFightLevel.class); // 战力-等级
        registerValue("fight_skill", PcvFightSkill.class); // 战力-技能
        registerValue("fight_title", PcvFightTitle.class); // 战力-称号
        registerValue("babystage", PcvBabyStage.class); //宝宝阶段
        registerValue("babylv", PcvBabyLv.class); //宝宝等级

        /* 集合 */
        registerDataSet("bag", PcdsToolSet.class); // 背包
        registerDataSet("activedjob", PcdsActivedJobSet.class); // 已激活的职业
        registerDataSet("dungeon_passed", PcdsDungeonPassedSet.class); // 通关关卡集合
        registerDataSet("loginid", PcdsOfflineSet.class); // 账号下各角色数据集合

        /* 函数 */
        registerFunc("now", PcfNow.class); // 获取当前时间
        registerFunc("tohour", PcfToHour.class); // 将毫秒转换成小时
        registerFunc("isopen", PcfIsOpen.class); // 判断某个系统是否开启

        registerFunc("dungeon_isactived", PcfDungeonIsActived.class); // 判断某个关卡是否激活
        registerFunc("dungeon_ispassed", PcfDungeonIsPassed.class); // 判断某个关卡是否通关
    }

}
