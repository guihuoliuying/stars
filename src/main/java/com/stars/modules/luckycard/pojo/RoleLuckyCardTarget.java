package com.stars.modules.luckycard.pojo;

import com.stars.modules.luckycard.LuckyCardManager;
import com.stars.modules.luckycard.prodata.LuckyCard;

/**
 * Created by huwenjun on 2017/9/27.
 */
public class RoleLuckyCardTarget implements Comparable<RoleLuckyCardTarget> {
    private int cardId;
    private int odds;

    public RoleLuckyCardTarget(int cardId) {
        this.cardId = cardId;
    }

    public int getCardId() {
        return cardId;
    }

    public void setCardId(int cardId) {
        this.cardId = cardId;
    }

    public int getOdds() {
        return odds;
    }

    public void setOdds(int odds) {
        this.odds = odds;
    }

    public LuckyCard getLuckyCard() {
        return LuckyCardManager.luckyCardMap.get(cardId);
    }

    @Override
    public int compareTo(RoleLuckyCardTarget o) {
        int type = this.getLuckyCard().getType() - o.getLuckyCard().getType();
        if (type != 0) {
            return type;
        }
        return o.getLuckyCard().getOrder() - this.getLuckyCard().getOrder();
    }
}
