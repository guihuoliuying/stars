package com.stars.modules.fashion.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.fashion.FashionModule;
import com.stars.modules.fashion.FashionPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * 客户端请求时装数据
 * Created by gaopeidian on 2016/10/08.
 */
public class ServerFashion  extends PlayerPacket {
	public static final byte REQ_SYNC_ALL = 0x01; // 请求同步所有时装的信息
	public static final byte REQ_DRESS = 0x02; // 同步某个时装的信息
	public static final byte REQ_UNDRESS = 0x03; // 通知激活时装
	
	private byte subtype;
	
	private int fashionId;
	
    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        subtype = buff.readByte();
        switch (subtype) {
            case REQ_DRESS:
            case REQ_UNDRESS:
            	fashionId = buff.readInt();
                break;
        }
    }
	
    @Override
    public void execPacket(Player player) {
        FashionModule fashionModule = module(MConst.Fashion);
        switch (subtype) {
            case REQ_SYNC_ALL:
            	fashionModule.sendAllFashionInfo();
                break;
            case REQ_DRESS:
            	fashionModule.dressFashion(fashionId);
                break;
            case REQ_UNDRESS:
            	fashionModule.undressFashion(fashionId);
                break;
        }
    }

    @Override
    public short getType() {
        return FashionPacketSet.S_FASHION;
    }  
}
