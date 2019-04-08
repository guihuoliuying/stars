package com.stars.services.family.activities.bonfire.cache;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wuyuxing on 2017/3/11.
 */
public class RoleQuestionCache {
    private long familyId;
    private long roleId;
    private String name;
    private int rightAnswerCount;
    private List<Integer> answerList;//已经回答过的题目id

    public RoleQuestionCache(long familyId, long roleId, String name) {
        this.familyId = familyId;
        this.roleId = roleId;
        this.name = name;
        this.rightAnswerCount = 0;
        this.answerList = new ArrayList<>();
    }

    public long getFamilyId() {
        return familyId;
    }

    public void setFamilyId(long familyId) {
        this.familyId = familyId;
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRightAnswerCount() {
        return rightAnswerCount;
    }

    public void setRightAnswerCount(int rightAnswerCount) {
        this.rightAnswerCount = rightAnswerCount;
    }
    public void addRightCount(){
        this.rightAnswerCount++;
    }

    public List<Integer> getAnswerList() {
        return answerList;
    }

    public void setAnswerList(List<Integer> answerList) {
        this.answerList = answerList;
    }

    public boolean hasAlreadyAnswer(int questionId){
        return answerList.contains(questionId);
    }

    public void addAnswerQuestionId(int questionId){
        if(hasAlreadyAnswer(questionId)) return;
        answerList.add(questionId);
    }
}
