package com.stars.modules.shop.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.push.PushManager;
import com.stars.modules.push.prodata.PushVo;
import com.stars.modules.shop.ShopManager;
import com.stars.modules.shop.ShopPacketSet;
import com.stars.modules.shop.prodata.Shop;
import com.stars.network.server.buffer.NewByteBuffer;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 用来发送商店的产品数据
 * Created by zhouyaohui on 2016/9/7.
 */
public class ClientShopData extends PlayerPacket {
    /****************** 常量 ********************/
    public static final byte IDLE = 0;
    public static final byte SHOPLIST = 1;  // 所有的产品数据
    public static final byte UPDATESHOP = 2;    // 更新产品数据
    public static final byte USERDATA = 3;  // 用户数据
    public static final byte BUYGOODS = 4;  // 购买相应

    /** 产品数据 */
    private byte opType = IDLE;
    private List<Shop> shopList;
    private List<Shop> addList;
    private List<Shop> subList;

    /** 用户数据 */
    private Map<Shop, Integer> dailyLimit;
    private Map<Shop, Integer> personalLimit;
    private Map<Shop, Integer> serviceLimit;
    private Set<Integer> randomBuy;
    private int serverDays;
    private int remainderFlushTimes;
    private int remainPayFlushTimes;// 剩余付费刷新次数

    /** 购买 */
    private byte buyResult;
    private int goodsId;
    private int remainder;
    private int count;

    private byte isFlush = 0;   // 默认不是玩家点击按钮刷新

    public void setCount(int count) {
        this.count = count;
    }

    public void setGoodsId(int goodsId) {
        this.goodsId = goodsId;
    }

    public void setRemainder(int remainder) {
        this.remainder = remainder;
    }

    public void setRandomBuy(Set<Integer> randomBuy) {
        this.randomBuy = randomBuy;
    }

    public void setIsFlush(byte isFlush) {
        this.isFlush = isFlush;
    }

    public void setBuyResult(byte buyResult) {
        this.buyResult = buyResult;
    }

    public void setDailyLimit(Map<Shop, Integer> dailyLimit) {
        this.dailyLimit = dailyLimit;
    }

    public void setPersonalLimit(Map<Shop, Integer> personalLimit) {
        this.personalLimit = personalLimit;
    }

    public void setServiceLimit(Map<Shop, Integer> serviceLimit) {
        this.serviceLimit = serviceLimit;
    }

    public void setServerDays(int serverDays) {
        this.serverDays = serverDays;
    }

    public void setRemainderFlushTimes(int remainderFlushTimes) {
        this.remainderFlushTimes = remainderFlushTimes;
    }

    public void setAddList(List<Shop> addList) {
        this.addList = addList;
    }

    public void setSubList(List<Shop> subList) {
        this.subList = subList;
    }

    public void setOpType(byte opType) {
        this.opType = opType;
    }

    public void setShopList(List<Shop> shopList) {
        this.shopList = shopList;
    }

    @Override
    public short getType() {
        return ShopPacketSet.C_SHOP_PRODUCT;
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeByte(opType);
        if (opType == SHOPLIST) {
            buff.writeInt(shopList.size());
            for (Shop item : shopList) {
                writeShopToBuffer(buff, item);
            }
        }
        if (opType == UPDATESHOP) {
            buff.writeInt(addList.size());
            for (Shop item : addList) {
                writeShopToBuffer(buff, item);
            }
            buff.writeInt(subList.size());
            for (Shop item : subList) {
                buff.writeInt(item.getGoodsId());
            }
            buff.writeByte(isFlush);
        }

        if (opType == USERDATA) {
            buff.writeInt(ShopManager.version);
            buff.writeInt(dailyLimit.size());
            for (Map.Entry<Shop, Integer> entry : dailyLimit.entrySet()) {
                buff.writeInt(entry.getKey().getGoodsId());
                buff.writeInt(entry.getKey().getDailyLimitNum() - entry.getValue());
            }
            buff.writeInt(personalLimit.size());
            for (Map.Entry<Shop, Integer> entry : personalLimit.entrySet()) {
                buff.writeInt(entry.getKey().getGoodsId());
                buff.writeInt(entry.getKey().getPersonalLimitNum() - entry.getValue());
            }
            buff.writeInt(serviceLimit.size());
            for (Map.Entry<Shop, Integer> entry : serviceLimit.entrySet()) {
                buff.writeInt(entry.getKey().getGoodsId());
                buff.writeInt(entry.getKey().getServiceLimitNum() - entry.getValue());
            }
            buff.writeInt(randomBuy.size());
            for (Integer goodsId : randomBuy) {
                buff.writeInt(goodsId);
            }
            buff.writeInt(serverDays);
            buff.writeInt((int)(System.currentTimeMillis() / 1000));
            buff.writeInt(remainderFlushTimes);
            buff.writeInt(remainPayFlushTimes);// 剩余付费刷新次数
        }

        if (opType == BUYGOODS) {
            buff.writeByte(buyResult);
            buff.writeInt(goodsId);
            buff.writeInt(remainder);
            buff.writeInt(count);
        }
    }

    /**
     * 写入单个商品数据
     * @param buff
     * @param item
     */
    private void writeShopToBuffer(NewByteBuffer buff, Shop item) {
        buff.writeInt(item.getGoodsId());
        buff.writeInt(item.getItemId());
        buff.writeInt(item.getShopType());
        buff.writeInt(item.getSubType());
        buff.writeString(item.getSalePic());
        buff.writeString(item.getPrimePrice());
        buff.writeString(item.getPrice());
        buff.writeInt(item.getCount());
        buff.writeInt(item.getPutOrder());
        buff.writeString(item.getServerDays());
        if (item.getShopType() == ShopManager.SHOPTYPE_TIMESHOP) { // 没办法，这样改最快
            int pushId = item.getGoodsId();
            PushVo pushVo = PushManager.getPushVo(pushId);
            if (pushVo != null && pushVo.hasExpirationDate()) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHH");
                buff.writeString(sdf.format(new Date(pushVo.getBeginTimeMillis()))
                        + "+" + sdf.format(new Date(pushVo.getEndTimeMillis())));
            } else {
                buff.writeString(item.getSaleDate());
            }
        } else {
            buff.writeString(item.getSaleDate());
        }
        buff.writeString(item.getExtendCondition());
    }

    public void setRemainPayFlushTimes(int remainPayFlushTimes) {
        this.remainPayFlushTimes = remainPayFlushTimes;
    }
}
