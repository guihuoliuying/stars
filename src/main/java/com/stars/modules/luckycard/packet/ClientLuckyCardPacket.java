package com.stars.modules.luckycard.packet;

import com.stars.modules.luckycard.LuckyCardManager;
import com.stars.modules.luckycard.LuckyCardPacketSet;
import com.stars.modules.luckycard.pojo.LuckyCardAnnounce;
import com.stars.modules.luckycard.pojo.RoleLuckyCardTarget;
import com.stars.modules.luckycard.prodata.LuckyCard;
import com.stars.modules.luckycard.usrdata.RoleLuckyCard;
import com.stars.modules.luckycard.usrdata.RoleLuckyCardBox;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;
import com.stars.services.ServiceHelper;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by huwenjun on 2017/9/27.
 */
public class ClientLuckyCardPacket extends Packet {
    private short subType;
    public static final short SEND_ACTIVITY_STATUS = 1;//发送活动状态
    public static final short SEND_MAIN_UI_DATA = 2;//发送主界面数据
    public static final short SEND_LUCKY_1 = 3;//抽一次
    public static final short SEND_LUCKY_MORE = 4;//抽多次
    public static final short SEND_OPEN_TEMP_BOX = 5;//暂存箱
    private boolean includeProduct;
    private boolean open;
    private List<Integer> cardIds;//抽中卡id
    private int remainTicketCount;//剩余奖券
    private Map<Integer, Integer> reward;
    private Collection<RoleLuckyCardBox> roleLuckyCardBoxes;
    private int time;//目标次数
    private RoleLuckyCard specialRoleLuckyCard;
    List<RoleLuckyCardTarget> roleLuckyCardTargets;

    public ClientLuckyCardPacket(short subType) {
        this.subType = subType;
    }

    public ClientLuckyCardPacket() {
    }

    @Override
    public short getType() {
        return LuckyCardPacketSet.C_LUCKY_CARD;
    }

    @Override
    public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeShort(subType);
        switch (subType) {
            case SEND_ACTIVITY_STATUS: {
                buff.writeInt(open ? 1 : 0);
            }
            break;
            case SEND_MAIN_UI_DATA: {
                if (includeProduct) {
                    buff.writeInt(1);
                    buff.writeInt(LuckyCardManager.allCards.size());
                    for (LuckyCard luckyCard : LuckyCardManager.allCards) {
                        luckyCard.writeBuff(buff);
                    }
                } else {
                    buff.writeInt(0);
                }

                buff.writeInt(specialRoleLuckyCard.getCardId());//倾心稀有卡id
                buff.writeInt(specialRoleLuckyCard.getNotHit());//已倾心次数
                buff.writeInt(LuckyCardManager.luckyCardMap.get(specialRoleLuckyCard.getCardId()).getFullget() - specialRoleLuckyCard.getNotHit());//剩余必中次数
                buff.writeString(LuckyCardManager.luckyCardPayPayAward);//充值送奖券规则

                buff.writeInt(remainTicketCount);//剩余奖券
                buff.writeInt(LuckyCardManager.luckyCardConsumeUnit);//单次抽奖消耗奖券数
                LinkedList<LuckyCardAnnounce> luckyAnnounceTop10 = ServiceHelper.luckyCardService().getLuckyAnnounceTop10();
                buff.writeInt(luckyAnnounceTop10.size());
                for (LuckyCardAnnounce luckyCardAnnounce : luckyAnnounceTop10) {
                    luckyCardAnnounce.writeBuff(buff);
                }
                buff.writeInt(roleLuckyCardTargets.size());//选择的卡组
                for (RoleLuckyCardTarget roleLuckyCardTarget : roleLuckyCardTargets) {
                    buff.writeInt(roleLuckyCardTarget.getCardId());
                }
            }
            break;
            case SEND_LUCKY_1: {
                buff.writeInt(cardIds.get(0));
            }
            break;
            case SEND_LUCKY_MORE: {
                buff.writeInt(time);//期望抽奖次数
                buff.writeInt(cardIds.size());
                for (Integer cardId : cardIds) {
                    buff.writeInt(cardId);
                }
            }
            break;
            case SEND_OPEN_TEMP_BOX: {
                buff.writeInt(roleLuckyCardBoxes.size());
                for (RoleLuckyCardBox roleLuckyCardBox : roleLuckyCardBoxes) {
                    roleLuckyCardBox.writeBuff(buff);
                }
            }
            break;
        }
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {

    }

    @Override
    public void execPacket() {

    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public List<Integer> getCardIds() {
        return cardIds;
    }

    public void setCardIds(List<Integer> cardIds) {
        this.cardIds = cardIds;
    }

    public Map<Integer, Integer> getReward() {
        return reward;
    }

    public void setReward(Map<Integer, Integer> reward) {
        this.reward = reward;
    }

    public boolean isIncludeProduct() {
        return includeProduct;
    }

    public void setIncludeProduct(boolean includeProduct) {
        this.includeProduct = includeProduct;
    }

    public boolean isOpen() {
        return open;
    }

    public int getRemainTicketCount() {
        return remainTicketCount;
    }

    public void setRemainTicketCount(int remainTicketCount) {
        this.remainTicketCount = remainTicketCount;
    }

    public void setRoleLuckyCardBoxes(Collection<RoleLuckyCardBox> roleLuckyCardBoxes) {
        this.roleLuckyCardBoxes = roleLuckyCardBoxes;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public void setSpecialRoleLuckyCard(RoleLuckyCard specialRoleLuckyCard) {
        this.specialRoleLuckyCard = specialRoleLuckyCard;
    }

    public void setRoleLuckyCardTargets(List<RoleLuckyCardTarget> roleLuckyCardTargets) {
        this.roleLuckyCardTargets = roleLuckyCardTargets;
    }
}
