package com.stars.core.expr.example;

import com.stars.core.expr.ExprConfig;
import com.stars.core.expr.example.extensions.function.ExampleEfRandom;
import com.stars.core.expr.example.extensions.value.ExampleEvLevel;
import com.stars.core.expr.example.extensions.value.ExampleEvVipLevel;
import com.stars.core.expr.tips.ExprTipsType;

public class ExampleExprConfig extends ExprConfig {

    public static final ExampleExprConfig config = new ExampleExprConfig();

    private ExampleExprConfig() {
        registerValue("level", ExampleEvLevel.class); // 人物等级
        registerValue("vipLevel", ExampleEvVipLevel.class); // vip等级

        registerFunc("random", ExampleEfRandom.class); // 随机数

        registerTips(ExprTipsType.VALUE, "level", "", 100, "等级不满足要求");
        registerTips(ExprTipsType.VALUE, "level", ">", 101, "等级需大于{0}");
        registerTips(ExprTipsType.VALUE, "level", ">=", 102, "等级需大于等于{0}");
        registerTips(ExprTipsType.VALUE, "level", "between", 103, "等级需在({0},{1})之间");

        registerTips(ExprTipsType.VALUE, "vipLevel", "", 200, "vip等级不满足要求");
        registerTips(ExprTipsType.VALUE, "vipLevel", ">", 201, "vip等级需大于{0}");
        registerTips(ExprTipsType.VALUE, "vipLevel", ">=", 202, "vip等级需大于等于{0}");
        registerTips(ExprTipsType.VALUE, "vipLevel", "between", 203, "vip等级需在({0},{1})之间");
    }

}
