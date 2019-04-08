package com.stars.modules.collectphone.usrdata;

import com.stars.core.db.DBUtil;
import com.stars.core.db.DbRow;
import com.stars.core.db.SqlUtil;
import com.stars.util.StringUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by huwenjun on 2017/9/20.
 */
public class RoleCollectPhone extends DbRow {
    private long roleid;
    private int actType;
    private int joinStatus;//1:参加，0：未参加
    private String answer = "";
    private long lastMsgTime = 0;//上次发短信的时间
    /**
     * 《step,answer》
     */
    private Map<Integer, String> answerMap = new HashMap<>();

    public RoleCollectPhone() {
    }

    public RoleCollectPhone(long roleid, int actType, int join) {
        this.roleid = roleid;
        this.actType = actType;
        this.joinStatus = join;
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "rolecollectphone", "roleid=" + roleid + " and acttype=" + actType);
    }

    @Override
    public String getDeleteSql() {
        return SqlUtil.getDeleteSql("rolecollectphone", "roleid=" + roleid + " and acttype=" + actType);
    }

    public long getRoleid() {
        return roleid;
    }

    public void setRoleid(long roleid) {
        this.roleid = roleid;
    }

    public int getActType() {
        return actType;
    }

    public void setActType(int actType) {
        this.actType = actType;
    }

    public int getJoinStatus() {
        return joinStatus;
    }

    public void setJoinStatus(int joinStatus) {
        this.joinStatus = joinStatus;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
        answerMap = StringUtil.toMap(answer, Integer.class, String.class, ':', ';');
    }

    public Map<Integer, String> getAnswerMap() {
        return answerMap;
    }

    public void setAnswerMap(Map<Integer, String> answerMap) {
        this.answerMap = answerMap;
    }

    public void put(int step, String answer) {
        answerMap.put(step, answer);
        this.answer = StringUtil.makeString(answerMap, ':', ';');
    }

    public long getLastMsgTime() {
        return lastMsgTime;
    }

    public void setLastMsgTime(long lastMsgTime) {
        this.lastMsgTime = lastMsgTime;
    }
}
