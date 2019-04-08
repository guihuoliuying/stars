package com.stars.modules.gamecave.prodata;

import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.LogUtil;

/**
 * Created by gaopeidian on 2017/1/12.
 */
public class GameCaveQuestionVo extends GameVoBase{
    private int questionId;
    private String questiondesc;
    private String correct;
    private String wrong;   
    private int time;
    private String score;
    
    /* 内存数据 */
    private int winScore = 0;
    private int loseScore = 0;
    private int coef = 0;

    public void writeToBuff(NewByteBuffer buff) {
      buff.writeInt(questionId);
      buff.writeInt(difficulty);
      buff.writeString(correct);
      buff.writeString(wrong);
      buff.writeInt(time);
      buff.writeString(questiondesc);
  }
    
    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int value) {
        this.questionId = value;
        this.id = value;
    }
    
    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int value) {
        this.difficulty = value;
    }

    public String getQuestionDesc() {
        return questiondesc;
    }

    public void setQuestionDesc(String value) {
        this.questiondesc = value;
    }
    
    public String getCorrect() {
        return correct;
    }

    public void setCorrect(String value) {
        this.correct = value;
    }

    public String getWrong() {
        return wrong;
    }

    public void setWrong(String value) {
        this.wrong = value;
    }
    
    public int getTime() {
        return time;
    }

    public void setTime(int value) {
        this.time = value;
    }
    
    public String getScore() {
        return score;
    }

    public void setScore(String value) {
        this.score = value;
        
        if (score == null || score.equals("") || score.equals("0")) {
			return;
		}
		String[] sts = score.split("\\+");
		int length = sts.length;
		if (length < 3) {
			LogUtil.info("GameCaveQuestionVo.setScore score length is less than 3");
			return;
		}
		
		winScore = Integer.parseInt(sts[0]);
		loseScore = Integer.parseInt(sts[1]);
		coef = Integer.parseInt(sts[2]);
    }
    
    
    public int getWinScore() {
        return winScore;
    }

    public int getLoseScore() {
        return loseScore;
    }
    
    public int getCoef() {
        return coef;
    }
    
}
