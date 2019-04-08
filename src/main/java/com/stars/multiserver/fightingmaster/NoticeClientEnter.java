package com.stars.multiserver.fightingmaster;

import java.util.List;

/**
 * Created by zhouyaohui on 2016/11/9.
 */
public class NoticeClientEnter {
    private String fightId;
    private List<String> skillList;

    public List<String> getSkillList() {
        return skillList;
    }

    public void setSkillList(List<String> skillList) {
        this.skillList = skillList;
    }

    public String getFightId() {
        return fightId;
    }

    public void setFightId(String fightId) {
        this.fightId = fightId;
    }
}
