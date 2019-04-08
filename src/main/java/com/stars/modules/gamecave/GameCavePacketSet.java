package com.stars.modules.gamecave;

import com.stars.modules.gamecave.packet.*;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * 洞府(小游戏场景)协议相关;
 * Created by gaopeidian on 2017/1/13.
 */
public class GameCavePacketSet extends PacketSet {
    public static short S_GAMECAVE_VO = 0x01E7;
    public static short C_GAMECAVE_VO = 0x01E8;
    
    public static short S_GAMECAVE_DATA = 0x01E9;
    public static short C_GAMECAVE_DATA = 0x01EA;
    
    public static short S_TINY_GAME_DATA = 0x01EB;
    public static short C_TINY_GAME_DATA = 0x01EC;
    
    public static short S_CHOOSE_GAME_LIST = 0x01ED;
    public static short C_CHOOSE_GAME_LIST = 0x01EE;
    
    public static short S_CHOOSE_GAME = 0x01EF;
    public static short C_CHOOSE_GAME = 0x01F1;
    
    public static short S_REQUEST_PLAY_GAME = 0x01F2;
    public static short C_REQUEST_PLAY_GAME = 0x01F3;
    
    public static short S_ENTER_GAME_CAVE_SCENE = 0x01F4;
    public static short C_ENTER_GAME_CAVE_SCENE = 0x01F5;
    
    public static short S_START_GAME = 0x01F6;
    public static short C_START_GAME = 0x01F7;
    
    public static short S_FINISH_GAME = 0x01F8;
    public static short C_FINISH_GAME = 0x01F9;
    
    public static short S_EXIT_GAME = 0x01FA;
    public static short C_EXIT_GAME = 0x01FB;
    
    public static short S_REQUSET_CET_CARD = 0x01FC;
    public static short C_REQUSET_CET_CARD = 0x01FD;
    
    public static short S_GET_TINYGAME_REWARD_AND_CARD = 0x01FE;
    public static short C_GET_TINYGAME_REWARD_AND_CARD = 0x01FF;
    
    public static short S_GET_FINAL_REWARD = 0x0201;
    public static short C_GET_FINAL_REWARD = 0x0202;
    
    public static short S_GAME_RECORD_DATA = 0x0203;
    public static short C_GAME_RECORD_DATA = 0x0204;
    
    public static short S_GET_ALL_CARD = 0x0205;
    public static short C_GET_ALL_CARD = 0x0206;
    
    public static short C_GAME_CAVE_RESET = 0x0207;
    
    public GameCavePacketSet() {

    }

    @Override
    public List<Class<? extends com.stars.network.server.packet.Packet>> getPacketList() {
        List<Class<? extends com.stars.network.server.packet.Packet>> al = new ArrayList<Class<? extends Packet>>();
        al.add(ClientGameCaveVo.class);
        al.add(ServerGameCaveVo.class);
        
        al.add(ClientGameCaveData.class);
        al.add(ServerGameCaveData.class);
        
        al.add(ClientTinyGameData.class);
        al.add(ServerTinyGameData.class);
        
        al.add(ClientChooseGameList.class);
        al.add(ServerChooseGameList.class);
        
        al.add(ClientChooseGame.class);
        al.add(ServerChooseGame.class);
        
        al.add(ClientRequestPlayGame.class);
        al.add(ServerRequestPlayGame.class);
        
        al.add(ClientEnterGameScene.class);
        al.add(ServerEnterGameScene.class);
        
        al.add(ClientStartGame.class);
        al.add(ServerStartGame.class);
        
        al.add(ClientFinishGame.class);
        al.add(ServerFinishGame.class);
        
        al.add(ClientExitGame.class);
        al.add(ServerExitGame.class);
        
        al.add(ClientRequestGetCard.class);
        al.add(ServerRequestGetCard.class);
        
        al.add(ClientGetTinyGameRewardAndCard.class);
        al.add(ServerGetTinyGameRewardAndCard.class);
        
        al.add(ClientGetFinalReward.class);
        al.add(ServerGetFinalReward.class);
        
        al.add(ClientGameRecordData.class);
        al.add(ServerGameRecordData.class);
        
        al.add(ClientGetAllCard.class);
        al.add(ServerGetAllCard.class);
        return al;
    }

}