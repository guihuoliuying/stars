package com.stars.modules.luckycard.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.luckycard.LuckyCardModule;
import com.stars.modules.luckycard.LuckyCardPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huwenjun on 2017/9/27.
 */
public class ServerLuckyCardPacket extends PlayerPacket {
    private short subType;
    public static final short REQ_MAIN_DATA = 1;//请求主界面数据
    public static final short REQ_CHOOSE_CARD = 2;//选择卡牌
    public static final short REQ_LUCKY_GO = 3;//抽奖
    public static final short REQ_OPEN_TEMP_BOX = 4;//打开暂存箱
    public static final short REQ_RESOLVE = 5;//请求分解暂存箱物品
    public static final short REQ_GET = 6;//请求取出暂存箱物品
    private List<Integer> cardIds = new ArrayList<>();
    private int time;
    private int includeProduct;//是否包含产品数据
    private int cardId;

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        subType = buff.readShort();
        switch (subType) {
            case REQ_MAIN_DATA: {
                includeProduct = buff.readInt();//是否包含产品数据，1为包含，0为不需要
            }
            break;
            case REQ_CHOOSE_CARD: {
                int size = buff.readInt();
                for (int index = 0; index < size; index++) {
                    cardIds.add(buff.readInt());
                }
            }
            break;
            case REQ_LUCKY_GO: {
                time = buff.readInt();//次数
            }
            break;
            case REQ_RESOLVE: {
                cardId = buff.readInt();//对应的cardId，-1表示批量操作
            }
            break;
            case REQ_GET: {
                cardId = buff.readInt();//对应的cardId，-1表示批量操作
            }
        }
    }

    @Override
    public void execPacket(Player player) {
        LuckyCardModule luckyCardModule = module(MConst.LuckyCard);
        switch (subType) {
            case REQ_MAIN_DATA: {
                luckyCardModule.reqMainData(includeProduct == 1);
            }
            break;
            case REQ_CHOOSE_CARD: {
                luckyCardModule.reqChooseCards(cardIds);
            }
            break;
            case REQ_LUCKY_GO: {
                luckyCardModule.reqLuckyGo(time);
            }
            break;
            case REQ_OPEN_TEMP_BOX: {
                luckyCardModule.reqOpenTempBox();
            }
            break;
            case REQ_RESOLVE: {
                luckyCardModule.reqResolve(cardId);
            }
            break;
            case REQ_GET: {
                luckyCardModule.reqGet(cardId);
            }
        }
    }

    @Override
    public short getType() {
        return LuckyCardPacketSet.S_LUCKY_CARD;
    }
}
