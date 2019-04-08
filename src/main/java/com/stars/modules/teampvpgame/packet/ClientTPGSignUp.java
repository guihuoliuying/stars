package com.stars.modules.teampvpgame.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.teampvpgame.TeamPVPGamePacketSet;
import com.stars.modules.teampvpgame.userdata.SignUpSubmiter;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by liuyuheng on 2016/12/19.
 */
public class ClientTPGSignUp extends PlayerPacket {
    private byte sendType;// 下发类型

    public static final byte SUBMIT_SIGNUP = 1;// 提交报名
    public static final byte RECEIVE_SIGNUP_CONFIRM = 2;// 收到报名确认

    /* 参数 */
    private int resetConfirmTime;// 剩余确认时间(秒)
    private SignUpSubmiter signUpSubmiter;// 提交报名者

    public ClientTPGSignUp() {
    }

    public ClientTPGSignUp(byte sendType) {
        this.sendType = sendType;
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return TeamPVPGamePacketSet.C_TPG_SIGNUP;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeByte(sendType);
        switch (sendType) {
            case SUBMIT_SIGNUP:
                buff.writeInt(resetConfirmTime);// 剩余时间(秒)
                break;
            case RECEIVE_SIGNUP_CONFIRM:
                buff.writeInt(resetConfirmTime);// 剩余时间(秒)
                break;
        }
    }

    public void setResetConfirmTime(int resetConfirmTime) {
        this.resetConfirmTime = resetConfirmTime;
    }

    public void setSignUpSubmiter(SignUpSubmiter signUpSubmiter) {
        this.signUpSubmiter = signUpSubmiter;
    }
}
