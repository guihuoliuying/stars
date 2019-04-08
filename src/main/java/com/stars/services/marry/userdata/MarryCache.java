package com.stars.services.marry.userdata;

import java.util.HashMap;
import java.util.Map;

/**
 * 角色个人数据容器
 * Created by zhouyaohui on 2016/12/2.
 */
public class MarryCache {
    private MarryRole marryRole;
    private Marry marry;
    private Map<Long, MarryProfress> profress = new HashMap<>(); // 表白了但是还没有回应
    private Map<Long, MarryProfress> profressed = new HashMap<>(); // 被表白但是还没有处理的

    public Marry getMarry() {
        return marry;
    }

    public void setMarry(Marry marry) {
        this.marry = marry;
    }

    public MarryRole getMarryRole() {
        return marryRole;
    }

    public void setMarryRole(MarryRole marryRole) {
        this.marryRole = marryRole;
    }

    public Map<Long, MarryProfress> getProfressed() {
        return profressed;
    }

    public void setProfressed(Map<Long, MarryProfress> profressed) {
        this.profressed = profressed;
    }

    public Map<Long, MarryProfress> getProfress() {
        return profress;
    }

    public void setProfress(Map<Long, MarryProfress> profress) {
        this.profress = profress;
    }
}
