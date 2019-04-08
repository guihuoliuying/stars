package com.stars.modules.questionnaire.userdata;

import com.stars.db.DBUtil;
import com.stars.db.DbRow;
import com.stars.db.SqlUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by chenkeyu on 2017-05-16 14:28
 */
public class RoleQuestionnaire extends DbRow {
    private long roleId;
    private int group;
    private Map<Integer, List<String>> stepAndQuestion;//step,textId+textId|step,textId+textId
    private byte commit;

    public RoleQuestionnaire() {
    }

    public RoleQuestionnaire(long roleId) {
        this.roleId = roleId;
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public int getGroup() {
        return group;
    }

    public void setGroup(int group) {
        this.group = group;
    }

    public String getStepAndQuestion() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Integer, List<String>> entry : stepAndQuestion.entrySet()) {
            sb.append(entry.getKey()).append(",");
            for (String textId : entry.getValue()) {
                sb.append(textId).append("+");
            }
            sb.deleteCharAt(sb.length() - 1);
            sb.append("|");
        }
        return sb.toString();
    }

    public void setStepAndQuestionMap(String stepAndQuestionMap) {
        if (stepAndQuestionMap != null && stepAndQuestionMap.equals(""))
            return;
        String[] tmp0 = stepAndQuestionMap.split("\\|");
        for (String tmp1 : tmp0) {
            String[] tmp2 = tmp1.split(",");
            int step = Integer.parseInt(tmp2[0]);
            List<String> textStr = this.stepAndQuestion.get(step);
            if (textStr == null) {
                textStr = new ArrayList<>();
                stepAndQuestion.put(step, textStr);
            }
            String[] tmp3 = tmp0[1].split("\\+");
            for (String textId : tmp3) {
                textStr.add(textId);
            }
        }
    }

    public void setStepAndQuestion(String stepAndQuestion) {

    }

    public Map<Integer, List<String>> getStepAndQuestionMap() {
        return stepAndQuestion;
    }

    public byte getCommit() {
        return commit;
    }

    public void setCommit(byte commit) {
        this.commit = commit;
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "rolequestionnaire", " `roleid`=" + this.roleId);
    }

    @Override
    public String getDeleteSql() {
        return "delete from rolequestionnaire `roleid`=" + this.roleId;
    }
}
