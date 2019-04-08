package com.stars.modules.collectphone.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.collectphone.CollectPhoneModule;
import com.stars.modules.collectphone.CollectPhonePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by huwenjun on 2017/9/13.
 */
public class ServerCollectPhonePacket extends PlayerPacket {
    private short subType;
    private int actType;
    private int step;
    private String answer;
    public static final short REQ_MAIN_UI_DATA = 1;//请求主界面数据
    public static final short REQ_SUBMIT_QUESTION = 2;//请求提交问题

    @Override
    public short getType() {
        return CollectPhonePacketSet.S_COLLECT_PHONE;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {

    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        actType = buff.readInt();
        subType = buff.readShort();
        switch (subType) {
            case REQ_SUBMIT_QUESTION: {
                step = buff.readInt();
                answer = buff.readString();
            }
        }
    }

    @Override
    public void execPacket(Player player) {
        CollectPhoneModule collectPhoneModule = module(MConst.CollectPhone);
        switch (subType) {
            case REQ_MAIN_UI_DATA: {
                collectPhoneModule.reqMainUiData();
            }
            break;
            case REQ_SUBMIT_QUESTION: {
                collectPhoneModule.reqSubmit(step, answer);
            }
            break;
        }
    }

    @Override
    public void execPacket() {

    }
}
