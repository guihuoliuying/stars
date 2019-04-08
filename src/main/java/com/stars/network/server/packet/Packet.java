package com.stars.network.server.packet;

import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.session.GameSession;
import com.stars.util.log.CoreLogger;

import java.io.IOException;
import java.io.Serializable;

public abstract class Packet implements Serializable {

    private static final long serialVersionUID = 1L;

    public final static byte TRUE = 1;
    public final static byte FALSE = 0;

    private long roleId;
    private int packetId; // packetId, 为0的情况就不处理不记录
    private String packetSpecialMsg;
    protected com.stars.network.server.session.GameSession session;

    // 存储序列化后的内容
    private byte[] bytes;

    /**
     * 流量
     */
    private int dataLength = 0;

    public Packet() {
    }

    public abstract short getType();

    /**
     * 将包内的数据写入到指定的字节缓冲区中
     *
     * @param buff 指定的字节缓冲区
     */
    public abstract void writeToBuffer(NewByteBuffer buff);

    /**
     * 从指定的字节缓冲区中读取包的内容
     *
     * @param buff 指定的字节缓冲区
     */
    public abstract void readFromBuffer(NewByteBuffer buff);

    public byte[] toByteArray() throws IOException {
        return null;
    }

    public abstract void execPacket();

    public static Packet newPacket(NewByteBuffer buff, com.stars.network.server.session.GameSession session) {
        short type = 0;
        try {
            type = buff.readShort();
            Class clazz = PacketManager.get(type);
            if (clazz == null) {
                com.stars.util.log.CoreLogger.error("undefined packet type: 0x" + Integer.toHexString(type));
                return null;
            } else {
                Packet packet = (Packet) clazz.newInstance();
                //setSessions before readFromBuffer because readFromBuffer may has another packet
                packet.setSession(session);
                packet.setPacketId(buff.readInt()); // packet id
                packet.readFromBuffer(buff);

                if (!session.isServerSession()) {
                    packet.setRoleId(session.getRoleId());
                }
                return packet;
            }
        } catch (Exception e) {
            com.stars.util.log.CoreLogger.error("Packet.newPacket error, packetType:" + type, e);
            return null;
        }
    }
    
    public static Packet newPacket(NewByteBuffer buff) {
        short type = 0;
        try {
            type = buff.readShort();
            Class clazz = PacketManager.get(type);
            if (clazz == null) {
                com.stars.util.log.CoreLogger.error("undefined packet type: 0x" + Integer.toHexString(type));
                return null;
            } else {
                Packet packet = (Packet) clazz.newInstance();
                packet.setPacketId(buff.readInt()); // packet id
                packet.readFromBuffer(buff);
                return packet;
            }
        } catch (Exception e) {
            CoreLogger.error("Packet.newPacket error, packetType:" + type, e);
            return null;
        }
    }

    /**
     * packet返回数据包统一调用这个方法
     *
     * @param packet
     */
    protected void send(Packet packet) {
        PacketManager.send(session, packet);
    }

    public com.stars.network.server.session.GameSession getSession() {
        return session;
    }

    public void setSession(GameSession session) {
        this.session = session;
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public int getDataLength() {
        return dataLength;
    }

    public void setDataLength(int dataLength) {
        this.dataLength = dataLength;
    }

    public void setPacketId(int packetId) {
        this.packetId = packetId;
    }

    public int getPacketId() {
        return packetId;
    }

    public String getPacketSpecialMsg() {
        return packetSpecialMsg;
    }

    public void setPacketSpecialMsg(String packetSpecialMsg) {
        this.packetSpecialMsg = packetSpecialMsg;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }
}
