package com.stars.modules.gamecave.prodata;

import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.LogUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gaopeidian on 2017/1/12.
 */
public class GameCaveShootOldVo extends GameVoBase{
    private int oldShootId;
    private String bagPosition;  
    private int time;
    private String score;
    
    /* 内存数据 */
    //<<posX,posY>>
    private List<List<Integer>> bagPositionList = new ArrayList<List<Integer>>();
    
    private int winScore = 0;
    private int loseScore = 0;
    private int coef = 0;

    public void writeToBuff(NewByteBuffer buff) {
    	buff.writeInt(oldShootId);
        buff.writeInt(difficulty);
        buff.writeString(bagPosition);
        buff.writeInt(time);
    }
    
    public int getOldShootId() {
        return oldShootId;
    }

    public void setOldShootId(int value) {
        this.oldShootId = value;
        this.id = value;
    }
    
    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int value) {
        this.difficulty = value;
    }

    public String getBagPosition() {
        return bagPosition;
    }

    public void setBagPosition(String value) {
        this.bagPosition = value;
        
        if (bagPosition == null || bagPosition.equals("") || bagPosition.equals("0")) {
			return;
		}
		String[] sts = bagPosition.split("\\,");
		String[] ts;
		for(String tmp : sts){
			ts = tmp.split("\\+");
			if (ts.length >= 2) {
				List<Integer> pos = new ArrayList<Integer>();
				pos.add(Integer.parseInt(ts[0]));
				pos.add(Integer.parseInt(ts[1]));
				bagPositionList.add(pos);
			}
		}
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
    
    
    public List<List<Integer>> getBagPositionList(){
    	return bagPositionList;
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
