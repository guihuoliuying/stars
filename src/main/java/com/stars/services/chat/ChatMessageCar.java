package com.stars.services.chat;

import java.nio.ByteBuffer;

/**
 * @author dengzhou
 *消息车
 */
public class ChatMessageCar {
	
	private ChatMessage[] messages;
	
	private int size;
	
	/**
	 * 消息byte数组存储，用于消息网络发送的时候，避免重复转换
	 */
	private byte[] messageBaytes;
	
	public ChatMessageCar(int carCapacity){
		size = 0;
		messages = new ChatMessage[carCapacity];
	}
	
	public void reset(){
		size = 0;
		//置空，便于及时垃圾回收
		for (int i = 0;i < size;i++) {
			messages[i] = null;
		}
		messageBaytes = null;
	}
	
	public boolean addChatMessage(ChatMessage message){
		messages[size] = message;
		size++;
		return true;
	}
	
	public int size(){
		return size;
	}
	
	public ChatMessage getChatMessage(int index){
		return messages[index];
	}
	
	public ChatMessage pollChatMessage(int index){
		ChatMessage tmp = messages[index];
		messages[index] = null;
		return tmp;
	}
	
	/**
	 * @param messageCar
	 * @return
	 * 将消息车的消息预先写入到buffer中，避免每个玩家都重复的写入
	 * 
	 */
	public byte[] getMessageBaytes(ByteBuffer buffer) {
		if (messageBaytes != null) {
			return messageBaytes;
		}
		buffer.put((byte)size());
		for (int i = 0; i < size(); i++) {
			getChatMessage(i).writeToBuffer(buffer);
		}
		return setMessageBaytes(buffer.array());
	}

	public byte[] setMessageBaytes(byte[] messageBaytes) {
		this.messageBaytes = messageBaytes;
		return messageBaytes;
	}
	
}
