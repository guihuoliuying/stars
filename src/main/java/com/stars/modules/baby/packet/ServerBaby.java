package com.stars.modules.baby.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.baby.BabyModule;
import com.stars.modules.baby.BabyPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by chenkeyu on 2017-07-20.
 */
public class ServerBaby extends PlayerPacket {
    private static final byte view = 0x00;//打开界面
    private static final byte prayOrFeed = 0x01;//求子或培养
    private static final byte upgrade = 0x02;//升级
    private static final byte sweep = 0x03;//扫荡
    private static final byte attr_tips = 0x04;//请求扫荡产品数据
    private static final byte change_name = 0x05;//请求改名
    private static final byte buy_count = 0x06;//购买次数
    private static final byte baby_follow = 0x07;//宝宝跟随
    private static final byte sweepCount = 0x08;//请求各功能扫荡次数
    private static final byte REQ_FASHION_LIST = 0x09;//请求时装列表
    private static final byte REQ_USE_FASHION = 0x0A;//请求使用时装
    private static final byte REQ_ACTIVE_FASHION = 0x0B;//请求激活时装
    private byte subType;
    private int type;
    private String newName;
    private int times;
    private byte isFollow;
    private int fashionId;
    private int extendId;

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        subType = buff.readByte();
        switch (subType) {
            case prayOrFeed:
                type = buff.readInt();//0:普通，1:付费
                break;
            case sweep:
                type = buff.readInt();//1=坐骑；2=强化石；3=夫妻；4=六国寻宝
                extendId = buff.readInt();//扩展id
                break;
            case change_name:
                newName = buff.readString();
                break;
            case buy_count:
                times = buff.readInt();
                break;
            case baby_follow:
                isFollow = buff.readByte();//1:跟随，0:不跟随
                break;
            case REQ_USE_FASHION: {
                fashionId = buff.readInt();
            }
            break;
            case REQ_ACTIVE_FASHION: {
                fashionId = buff.readInt();
            }
            break;
            default:
                break;
        }
    }

    @Override
    public void execPacket(Player player) {
        BabyModule baby = module(MConst.Baby);
        switch (subType) {
            case view:
                baby.view();
                break;
            case prayOrFeed:
                baby.prayOrBringUp(type);
                break;
            case upgrade:
                baby.updateStage();
                break;
            case sweep:
                baby.sweep(type,extendId);
                break;
            case attr_tips:
                baby.sendAttrTips();
                break;
            case change_name:
                baby.changeBabyName(newName);
                break;
            case buy_count:
                baby.addExtraTimes(times);
                break;
            case baby_follow:
                baby.follow(isFollow);
                break;
            case sweepCount:
                baby.getSweepCount();
                break;
            case REQ_FASHION_LIST: {
                baby.reqBabyFashionList();
            }
            break;
            case REQ_USE_FASHION: {
                baby.reqUseFashionById(fashionId);
            }
            break;
            case REQ_ACTIVE_FASHION: {
                baby.reqActiveFashion(fashionId);
            }
            break;
            default:
                break;
        }
    }


    @Override
    public short getType() {
        return BabyPacketSet.S_BABY;
    }
}
