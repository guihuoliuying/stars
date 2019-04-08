package com.stars.core.rpc2.packet;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;
import com.stars.core.actor.invocation.InvocationMessage;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;

/**
 * Rpc调用请求
 * Created by zhaowenshuo on 2016/11/2.
 */
public class RpcInvocationReq extends Packet {

    /**
     * Kryo序列工具实例（用线程局部变量来做）
     */
    private static ThreadLocal<Kryo> kryoThreadLocal = new ThreadLocal<Kryo>() {
        @Override
        protected Kryo initialValue() {
            return new Kryo();
        }
    };

    private com.stars.core.actor.invocation.InvocationMessage invocationMessage;

    @Override
    public short getType() {
        return 0x7F21;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
//        LogUtil.info("Rpc|调用|InvocationMessage:{}", invocationMessage);
        // 序列化并写到buff中
        Kryo kryo = kryoThreadLocal.get();
        Output output = new Output(new ByteBufOutputStream(buff.getBuff()));
        kryo.writeClassAndObject(output, invocationMessage);
        output.flush();
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        // 反序列化
        Kryo kryo = kryoThreadLocal.get();
        invocationMessage = (com.stars.core.actor.invocation.InvocationMessage) kryo.readClassAndObject(
                new Input(new ByteBufInputStream(buff.getBuff())));
    }

    @Override
    public void execPacket() {

    }

    public com.stars.core.actor.invocation.InvocationMessage getInvocationMessage() {
        return invocationMessage;
    }

    public void setInvocationMessage(InvocationMessage invocationMessage) {
        this.invocationMessage = invocationMessage;
    }
}
