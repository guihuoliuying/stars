package com.stars.core.expr.tips;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * type(3): value, function, dataset
 * extension id(16):
 * predicate id(10):
 * <p>
 * 通过枚举完成
 */
public class ExprTips {

    private final static Pattern placeholderPattern = Pattern.compile("\\{\\d+\\}");

    private int tipsId;
    private String tips;

    private boolean needParams;
//    private List<Integer> paramIndexList;

    public ExprTips(int tipsId, String tips) {
        this.tipsId = tipsId;
        this.tips = tips;
        this.needParams = false;
//        this.paramIndexList = new ArrayList<>();
        // init place holder
        Set<String> paramIndexSet = new HashSet<>();
        Matcher matcher = placeholderPattern.matcher(tips);
        if (matcher.find()) {
            this.needParams = true;
            int start;
            do {
                start = matcher.end();
                paramIndexSet.add(matcher.group());
            } while (matcher.find(start));
        }
        // check the index
        for (int i = 0; i < paramIndexSet.size(); i++) {
            if (!paramIndexSet.contains("{" + i + "}")) {
                throw new IllegalArgumentException("wrong param: {" + i + "}");
            }
        }
    }

    public String makeString(String... params) {
        String result = tips;
        for (int i = 0; i < params.length; i++) {
            result = result.replace("{" + i + "}", params[i]);
        }
        return result;
    }

    public int getTipsId() {
        return tipsId;
    }

    public String getTips() {
        return tips;
    }

    public boolean isNeedParams() {
        return needParams;
    }
}
