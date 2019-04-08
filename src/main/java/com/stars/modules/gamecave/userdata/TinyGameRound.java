package com.stars.modules.gamecave.userdata;

import com.stars.core.db.DBUtil;
import com.stars.core.db.DbRow;
import com.stars.core.db.SqlUtil;

/**
 * Created by gaoepidian on 2016/9/13.
 */
public class TinyGameRound extends DbRow {
    private long roleId;
    private int gameId;
    private int roundIndex;    
    private int leftTime;
    private byte isWin;
    private int successCount;
    private String dataStr;
    private int roundId;
 
    public TinyGameRound() {
    }
    
    public TinyGameRound(long roleId, int gameId, int roundIndex, int leftTime, byte isWin, int successCount, String dataStr, int roundId) {
        this.roleId = roleId;
        this.gameId = gameId;
        this.roundIndex = roundIndex;       
        this.leftTime = leftTime;
        this.isWin = isWin;
        this.successCount = successCount;
        this.dataStr = dataStr;
        this.roundId = roundId;
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "tinygameround", " roleid=" + this.getRoleId() + " and gameId=" + this.getGameId());
    }

    @Override
    public String getDeleteSql() {
        return SqlUtil.getDeleteSql("tinygameround", " roleid=" + this.getRoleId());
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }
    
    public int getGameId(){
    	return gameId;
    }
    
    public void setGameId(int value){
    	this.gameId = value;
    }

    public int getRoundIndex(){
    	return roundIndex;
    }
    
    public void setRoundIndex(int value){
    	this.roundIndex = value;
    }
    
    public int getLeftTime(){
    	return leftTime;
    }
    
    public void setLeftTime(int value){
    	this.leftTime = value;
    }
    
    public byte getIsWin(){
    	return isWin;
    }
    
    public void setIsWin(byte value){
    	this.isWin = value;
    }
    
    public int getSuccessCount(){
    	return successCount;
    }
    
    public void setSuccessCount(int value){
    	this.successCount = value;
    }
    
    public String getDataStr(){
    	return dataStr;
    }
    
    public void setDataStr(String value){
        this.dataStr = value;
    }
    
    public int getRoundId(){
    	return roundId;
    }
    
    public void setRoundId(int value){
    	this.roundId = value;
    }
}
