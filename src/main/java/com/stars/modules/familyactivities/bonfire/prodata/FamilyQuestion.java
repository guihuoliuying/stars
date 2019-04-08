package com.stars.modules.familyactivities.bonfire.prodata;

import com.stars.util.StringUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wuyuxing on 2017/3/7.
 */
public class FamilyQuestion {
    private int questionId;
    private String questionDesc;
    private String correct;
    private Map<Integer,String> correctMap;
    private int time;
    private int odds;
    private String award;
    private Map<Integer,Integer> awardMap;
    private String wrongAward;
    private Map<Integer,Integer> wrongAwardMap;
    private int fireExp;
    private int answerCount;

    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    public String getQuestionDesc() {
        return questionDesc;
    }

    public void setQuestionDesc(String questionDesc) {
        this.questionDesc = questionDesc;
    }

    public String getCorrect() {
        return correct;
    }

    public void setCorrect(String correct) {
        this.correct = correct;
        if(StringUtil.isEmpty(correct)) return;
        String[] arr = correct.split("\\+");
        answerCount = arr.length;
        correctMap = new HashMap<>();
        for(int i=0;i<answerCount;i++){
            correctMap.put(i+1,arr[i]);
        }
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public int getOdds() {
        return odds;
    }

    public void setOdds(int odds) {
        this.odds = odds;
    }

    public String getAward() {
        return award;
    }

    public void setAward(String award) {
        this.award = award;
        this.awardMap = StringUtil.toMap(award, Integer.class, Integer.class, '+', '|');
    }

    public String getWrongAward() {
        return wrongAward;
    }

    public void setWrongAward(String wrongAward) {
        this.wrongAward = wrongAward;
        this.wrongAwardMap = StringUtil.toMap(wrongAward, Integer.class, Integer.class, '+', '|');
    }

    public int getFireExp() {
        return fireExp;
    }

    public void setFireExp(int fireExp) {
        this.fireExp = fireExp;
    }

    public int getAnswerCount() {
        return answerCount;
    }

    public Map<Integer, Integer> getAwardMap() {
        return awardMap;
    }

    public Map<Integer, Integer> getWrongAwardMap() {
        return wrongAwardMap;
    }

    public Map<Integer, String> getCorrectMap() {
        return correctMap;
    }

    public String getRightDesc(){
        return correctMap.get(1);//默认第一个为正确答案
    }
}
