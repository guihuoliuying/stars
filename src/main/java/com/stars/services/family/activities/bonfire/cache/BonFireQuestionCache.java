package com.stars.services.family.activities.bonfire.cache;

import java.util.List;

/**
 * Created by wuyuxing on 2017/3/11.
 */
public class BonFireQuestionCache {
    private int questionId;
    private List<Integer> answerList;   //题目序号
    private boolean isEnd;

    public BonFireQuestionCache(int questionId) {
        this.questionId = questionId;
    }

    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    public List<Integer> getAnswerList() {
        return answerList;
    }

    public void setAnswerList(List<Integer> answerList) {
        this.answerList = answerList;
    }

    public boolean isEnd() {
        return isEnd;
    }

    public void setEnd(boolean isEnd) {
        this.isEnd = isEnd;
    }

    public int getRightIndex(){
        for(int i=0,length = answerList.size();i<length;i++){
            if(answerList.get(i) == 1) return i+1;
        }
        return 0;
    }
}
