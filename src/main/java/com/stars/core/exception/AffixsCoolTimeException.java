package com.stars.core.exception;

import java.util.Map;

/**
 * 当附件处于冷却时间时无法获取附件
 * Created by huwenjun on 2017/3/30.
 */
public class AffixsCoolTimeException extends IllegalStateException {
    private Integer coolTime;
    private Map<Integer, Map<Integer, Integer>> allToolMap;
    public AffixsCoolTimeException(String s, Integer coolTime) {
        super(s);
        this.coolTime = coolTime;
    }

    public Integer getCoolTime() {
        return coolTime;
    }

    public void setCoolTime(Integer coolTime) {
        this.coolTime = coolTime;
    }

    public Map<Integer, Map<Integer, Integer>> getAllToolMap() {
        return allToolMap;
    }

    public void setAllToolMap(Map<Integer, Map<Integer, Integer>> allToolMap) {
        this.allToolMap = allToolMap;
    }
}
