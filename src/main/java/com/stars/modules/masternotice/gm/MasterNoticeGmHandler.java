package com.stars.modules.masternotice.gm;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.gm.GmHandler;
import com.stars.modules.masternotice.MasterNoticeManager;
import com.stars.modules.masternotice.MasterNoticeModule;
import com.stars.modules.scene.event.PassMasterNoticeStageEvent;
import com.stars.modules.shop.event.BuyGoodsEvent;

import java.util.Map;

/**
 * Created by gaopeidian on 2016/11/21.
 */
public class MasterNoticeGmHandler implements GmHandler {

    @Override
    public void handle(long roleId, Map<String, Module> moduleMap, String[] args) throws Exception {
        MasterNoticeModule masterNoticeModule = (MasterNoticeModule) moduleMap.get(MConst.MasterNotice);
        switch (args[0]) {
            case "accept":
            {
            	int noticeId = Integer.parseInt(args[1]);
            	masterNoticeModule.acceptNotice(noticeId);
                break;
            } 
            case "submit":
            {
            	int noticeId = Integer.parseInt(args[1]);
            	masterNoticeModule.submitNotice(noticeId,true);
                break;
            }
            case "freerefresh":
            {
            	masterNoticeModule.refreshNoticesByFree();
                break;
            }
            case "buyrefresh":
            {
            	masterNoticeModule.refreshNoticesByItem();
                break;
            }
            case "stageevent":
            {
            	int stageId = Integer.parseInt(args[1]);
            	PassMasterNoticeStageEvent event = new PassMasterNoticeStageEvent(stageId);
            	//masterNoticeModule.testFireEvent(event);
                break;
            }
            case "buyevent":
            {
            	int goodsId = Integer.parseInt(args[1]);
            	BuyGoodsEvent event = new BuyGoodsEvent(goodsId , 1);
            	//masterNoticeModule.testFireEvent(event);
                break;
            }
            case "test":
            {
            	int nobelLevel = Integer.parseInt(args[1]);
            	int totalCount = MasterNoticeManager.getTotalCountByNobelLevel(nobelLevel);
                break;
            }
        }
    }

}