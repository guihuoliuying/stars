package com.stars.network.server.packet;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.stars.network.server.buffer.NewByteBuffer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;

import java.io.IOException;

/**
 * Created by zd on 2015/5/20.
 */
public class SerializationPacket extends Packet {

    private Object object;

    public SerializationPacket() {
    }

    public SerializationPacket(Object object) {
        this.object = object;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    @Override
    public short getType() {
        return 0x7813;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        if (object == null) {
            throw new NullPointerException("SerializationPacket's object is null");
        }
        ByteBuf byteBuf = buff.getBuff();
        byteBuf.markWriterIndex();
        byteBuf.writeInt(0);
        int begin = byteBuf.writerIndex();
        Output output = new Output(new ByteBufOutputStream(byteBuf));
        Kryo kryo = new Kryo();
        kryo.writeClassAndObject(output, object);
        output.close();
        int end = byteBuf.writerIndex();
        byteBuf.resetWriterIndex();
        byteBuf.writeInt(end - begin);
        byteBuf.writerIndex(end);
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        int size = buff.readInt();
        Input input = new Input(new ByteBufInputStream(buff.getBuff(), size));
        setObject(new Kryo().readClassAndObject(input));
        input.close();
    }

    @Override
    public byte[] toByteArray() throws IOException {
        return new byte[0];
    }

    @Override
    public void execPacket() {
        // do not call it
    }

}
