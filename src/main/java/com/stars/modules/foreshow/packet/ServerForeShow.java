package com.stars.modules.foreshow.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.core.player.PlayerUtil;
import com.stars.modules.MConst;
import com.stars.modules.foreshow.ForeShowModule;
import com.stars.modules.foreshow.ForeShowPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenkeyu on 2016/11/1.
 */
public class ServerForeShow extends PlayerPacket {

    public static final byte openUnShow2OpenShow = 0x02;//客户端通知服务端某个系统表现完毕
    public static final byte foreShowText = 0x03;//客户端请求某个系统的条件文本
    public static final byte isOpen = 0x04;//某个系统是否开启

    private byte subtype;
    private List<String> opennames = new ArrayList<>();
    private String openname;
    private int size;

    @Override
    public void execPacket(Player player) {
        ForeShowModule foreShowModule = module(MConst.ForeShow);
        switch (subtype){
            case openUnShow2OpenShow:
                for(String name : opennames){
                    foreShowModule.openUnShow2OpenShow(name);
                }
                break;
            case foreShowText:
                for(String name : opennames){
                    foreShowModule.foreShowText(name);
                }
                break;
            case isOpen:
                ClientForeShow c = new ClientForeShow(ClientForeShow.isOpen);
                c.setOpen((byte) (foreShowModule.isOpen(openname)?1:0));
                PlayerUtil.send(getRoleId(), c);
            default:
                break;
        }
    }

    @Override
    public short getType() {
        return ForeShowPacketSet.S_FORESHOW;
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff){
        subtype = buff.readByte();
        switch (subtype){
            case openUnShow2OpenShow:
                size = buff.readByte();
                for (int i=0;i<size;i++){
                    opennames.add(buff.readString()) ;
                }
                break;
            case foreShowText:
                opennames.add(buff.readString());
                break;
            case isOpen:
                openname = buff.readString();
            default:
                break;
        }


    }
}
