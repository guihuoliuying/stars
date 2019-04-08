package com.stars.modules.fashion.gm;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.fashion.FashionModule;
import com.stars.modules.fashion.summary.FashionSummaryComponentImpl;
import com.stars.modules.gm.GmHandler;
import com.stars.services.ServiceHelper;

import java.util.Map;

/**
 * Created by gaopeidian on 2016/10/08.
 */
public class FashionGmHandler implements GmHandler {

    @Override
    public void handle(long roleId, Map<String, Module> moduleMap, String[] args) throws Exception {
        FashionModule fashionModule = (FashionModule) moduleMap.get(MConst.Fashion);
        switch (args[0]) {
            case "syncall":
            {
            	fashionModule.sendAllFashionInfo();
                break;
            }           	
            case "dress":
            {
            	int fashionId = Integer.parseInt(args[1]);
            	fashionModule.dressFashion(fashionId);
                break;
            }          	
            case "undress":
            {
            	int fashionId = Integer.parseInt(args[1]);
            	fashionModule.undressFashion(fashionId);
                break;
            }
            case "sum":
            {
            	FashionSummaryComponentImpl fashionImpl = (FashionSummaryComponentImpl) ServiceHelper.
            			summaryService().getSummaryComponent(fashionModule.id(), MConst.Fashion);
            	int dressFashionId = fashionImpl.getDressFashionId();
                break;
            }
        }
    }

}
