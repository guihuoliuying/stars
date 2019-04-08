package com.stars.modules.shop.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.shop.ShopModule;
import com.stars.modules.shop.ShopPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by zhouyaohui on 2016/9/7.
 */
public class ServerShopData extends PlayerPacket {

    /** 常量 */
    public final static byte IDLE = 0;
    public final static byte ALL = 1;   // 请求所有
    public final static byte UPWDATE = 2;   // 请求更新数据
    public final static byte BUY = 3;   //  购买
    public final static byte FLUSH = 4; // 刷新

    private byte opType;
    private int goodsId;
    private int count;
    private int version;

    @Override
    public void execPacket(Player player) {
        ShopModule shopModule = module(MConst.Shop);
        if (opType == ALL || opType == UPWDATE) {
            shopModule.openShop(version);
        }
        if (opType == BUY) {
            shopModule.buyGoodsById(goodsId, count);
        }
        if (opType == FLUSH) {
            shopModule.flush();
        }
    }

    @Override
    public short getType() {
        return ShopPacketSet.S_SHOP_PRODUCT;
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        opType = buff.readByte();
        if (opType == ALL || opType == UPWDATE) {
            version = buff.readInt();
        }
        if (opType == BUY) {
            goodsId = buff.readInt();
            count = buff.readInt();
        }
    }
}
