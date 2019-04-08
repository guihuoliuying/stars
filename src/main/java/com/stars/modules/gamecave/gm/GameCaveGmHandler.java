package com.stars.modules.gamecave.gm;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.gamecave.GameCaveModule;
import com.stars.modules.gm.GmHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by gaopeidian on 2017/1/18.
 */
public class GameCaveGmHandler implements GmHandler {

    @Override
    public void handle(long roleId, Map<String, Module> moduleMap, String[] args) throws Exception {
        GameCaveModule gamecaveModule = (GameCaveModule) moduleMap.get(MConst.GameCave);
        switch (args[0]) {
            case "test":
            {
            	int gameId = 1005;
            	List<Integer> chooseCardIds = new ArrayList<Integer>();
            	chooseCardIds.add(14);
            	chooseCardIds.add(37);
            	chooseCardIds.add(31);
            	gamecaveModule.sendGameCaveData();
                break;
            }            
            case "start":
            {
            	int gameId = Integer.parseInt(args[1]);
           	    int roundId = Integer.parseInt(args[2]);
            	gamecaveModule.startGame(gameId, roundId);
                break;
            }
            case "finish":
            {
            	int gameId = Integer.parseInt(args[1]);
           	    int roundId = Integer.parseInt(args[2]);
           	    String dataStr = args[3];
            	gamecaveModule.finishGame(gameId, roundId, dataStr);
                break;
            }
        }
    }
}