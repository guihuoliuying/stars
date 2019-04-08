package com.stars.services.luckycard;

import com.stars.modules.data.DataManager;
import com.stars.modules.luckycard.LuckyCardManager;
import com.stars.modules.luckycard.pojo.LuckyCardAnnounce;
import com.stars.modules.luckycard.prodata.LuckyCard;
import com.stars.modules.tool.ToolManager;
import com.stars.modules.tool.productdata.ItemVo;
import com.stars.services.SConst;
import com.stars.services.ServiceHelper;
import com.stars.services.ServiceSystem;
import com.stars.core.actor.invocation.ServiceActor;

import java.util.LinkedList;

/**
 * Created by huwenjun on 2017/8/10.
 */
public class LuckyCardServiceActor extends ServiceActor implements LuckyCardService {
    private static int top = 10;
    private LinkedList<LuckyCardAnnounce> luckyCardAnnounces = new LinkedList<>();

    @Override
    public void init() throws Throwable {
        ServiceSystem.getOrAdd(SConst.LuckyCardService, this);
    }

    @Override
    public void printState() {

    }

    @Override
    public void luckyAnnounce(LuckyCardAnnounce luckyCardAnnounce) {
        luckyCardAnnounces.addFirst(luckyCardAnnounce);
        if (luckyCardAnnounces.size() > top) {
            luckyCardAnnounces.removeLast();
        }
        LuckyCard luckyCard = LuckyCardManager.luckyCardMap.get(luckyCardAnnounce.getCardId());
        String getitem = luckyCard.getItem();
        String[] group = getitem.split("\\+");
        int itemId = Integer.parseInt(group[0]);
        ItemVo itemVo = ToolManager.getItemVo(itemId);
        ServiceHelper.chatService().announce(luckyCard.getMessage(), luckyCardAnnounce.getRoleName(), DataManager.getGametext(itemVo.getName()), group[1]);
    }

    @Override
    public LinkedList<LuckyCardAnnounce> getLuckyAnnounceTop10() {
        return luckyCardAnnounces;
    }

}