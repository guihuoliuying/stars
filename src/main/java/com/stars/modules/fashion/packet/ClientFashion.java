package com.stars.modules.fashion.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.fashion.FashionPacketSet;
import com.stars.modules.fashion.prodata.FashionVo;
import com.stars.modules.fashion.userdata.RoleFashion;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.DateUtil;
import com.stars.util.vowriter.BuffUtil;

import java.util.Map;


/**
 * 响应客户端请求时装数据
 * Created by gaopeidian on 2016/10/08.
 */
public class ClientFashion extends PlayerPacket {
	public static final byte RESP_SYNC_ALL = 0x01; // 同步所有时装的信息
	public static final byte RESP_SYNC_CUR_FASHION = 0x02; // 同步当前所穿时装
	public static final byte RESP_ACTIVE = 0x03; // 通知激活时装
    
	private byte subtype;
	
	private int curFashionId;//当前所穿时装的id，若没穿则为-1
	
	private RoleFashion roleFashion;
	
	private Map<Integer, RoleFashion> roleFashionMap;
	private Map<Integer, FashionVo> fashionVoMap;
	private byte activeType;  //0 延时  1 新激活 2 分解
    private int remainSecond; //剩余秒数
    private int addSecond; //增加秒数
    private Map<Integer,Integer>resolveGetMap;
    private int showExpressId;
    private long expiredTimestamp;
	public ClientFashion(){
		
	}
	
    public ClientFashion(byte subtype) {
        this.subtype = subtype;
    }
    
    public void setRoleFashion(RoleFashion roleFashion){
    	this.roleFashion = roleFashion;
    }
    
    public void setRoleFashionMap(Map<Integer, RoleFashion> roleFashionMap){
    	this.roleFashionMap = roleFashionMap;
    }
    
    public void setFashionVoMap(Map<Integer, FashionVo> fashionVoMap){
    	this.fashionVoMap = fashionVoMap;
    }
    
    public void setCurFashionId(int curFashionId){
    	this.curFashionId = curFashionId;
    }

    public int getRemainSecond() {
        return remainSecond;
    }

    public void setRemainSecond(int remainSecond) {
        this.remainSecond = remainSecond;
    }

    public int getAddSecond() {
        return addSecond;
    }

    public void setAddSecond(int addSecond) {
        this.addSecond = addSecond;
    }

    public byte getActiveType() {
        return activeType;
    }

    public void setActiveType(byte activeType) {
        this.activeType = activeType;
    }

    public Map<Integer, Integer> getResolveGetMap() {
        return resolveGetMap;
    }

    public void setResolveGetMap(Map<Integer, Integer> resolveGetMap) {
        this.resolveGetMap = resolveGetMap;
    }

    public int getShowExpressId() {
        return showExpressId;
    }

    public void setShowExpressId(int showExpressId) {
        this.showExpressId = showExpressId;
    }

    public long getExpiredTimestamp() {
        return expiredTimestamp;
    }

    public void setExpiredTimestamp(long expiredTimestamp) {
        this.expiredTimestamp = expiredTimestamp;
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return FashionPacketSet.C_FASHION;
    }

    @Override
    public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeByte(subtype);
        switch (subtype) {
            case RESP_SYNC_ALL:
            	writeSyncAll(buff);
                break;
            case RESP_SYNC_CUR_FASHION:
            	writeSyncCurFashion(buff);
                break;
            case RESP_ACTIVE:
                writeActive(buff);
                break;
        }
    }
    
    private void writeSyncAll(com.stars.network.server.buffer.NewByteBuffer buff){
    	int size = fashionVoMap.size();
    	buff.writeByte((byte)size);
        long now = System.currentTimeMillis();
        for (FashionVo fashionVo : fashionVoMap.values()) {
        	int fashionId = fashionVo.getFashionId();
        	if (roleFashionMap.containsKey(fashionId)) {
				RoleFashion roleFashion = roleFashionMap.get(fashionId);
                long remainTime = (roleFashion.getExpiredTime() - now)/ DateUtil.SECOND; //剩余秒数
				buff.writeInt(roleFashion.getFashionId());
	        	buff.writeByte((byte)1);
	        	buff.writeByte(roleFashion.getIsDress());
                buff.writeLong(remainTime<=0?0L:remainTime);
			}else{
				buff.writeInt(fashionId);
	        	buff.writeByte((byte)0);
	        	buff.writeByte((byte)0);
                buff.writeLong(0L);
			}	
        }
    }
    
    private void writeSyncCurFashion(com.stars.network.server.buffer.NewByteBuffer buff){
    	buff.writeInt(curFashionId);
        buff.writeLong(expiredTimestamp);
    }
    
    private void writeActive(NewByteBuffer buff) {
        buff.writeByte(activeType);
        buff.writeInt(roleFashion.getFashionId());
        if (activeType == (byte)0) { //延时
            buff.writeInt(remainSecond>0?remainSecond:0);
            buff.writeInt(addSecond>0?addSecond:0);
        }else if (activeType == (byte)1){
            buff.writeInt(showExpressId);
        }else if (activeType == (byte)2){
            BuffUtil.writeIntMapToBuff(buff, resolveGetMap);
        }
    }
}