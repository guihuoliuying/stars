package com.stars.core.expr.tips;

import com.google.common.base.Preconditions;

import java.util.List;

/**
 * type(3): value, function, dataset
 * extension id(13):
 * predicate id(13):
 * <p>
 * 通过枚举完成
 */
public class ExprTips {

    private int tips;
    private List<String> paramList;

    public ExprTips(ExprTipsType type, int extensionId, int predicateId, String... params) {
        Preconditions.checkArgument((extensionId & 0x1FFF) == extensionId);
        Preconditions.checkArgument((predicateId & 0x1FFF) == extensionId);
        tips = 0;
        tips |= type.ordinal() << 26;
        tips |= extensionId << 13;
        tips |= predicateId;
    }

    public int getTips() {
        return tips;
    }

    public List<String> getParamList() {
        return paramList;
    }
}
