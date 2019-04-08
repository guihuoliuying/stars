package com.stars.services.chat.filter;


import com.stars.core.player.PlayerUtil;
import com.stars.modules.chat.packet.ClientChatMessage;
import com.stars.services.chat.ChatManager;
import com.stars.services.chat.ChatMessage;
import com.stars.services.chat.ChatMessageCar;
import com.stars.services.chat.ChatServiceActor;
import com.stars.services.chat.cache.ChaterObject;
import com.stars.services.chat.cache.MyLinkedListNode;
import com.stars.util.LogUtil;
import com.stars.core.actor.Actor;

import java.nio.ByteBuffer;
import java.util.LinkedList;

/**
 * @author dengzhou
 * 世界聊天频道过滤器
 *
 */
public class WorldChannelFilter extends ChatFilter {
	/**
	 * 消息车容量
	 */
	public static int MESSAGE_CAR_CAPACITY = 10;
	
	/**
	 * 消息车队列容量
	 */
	public static int MESSAGE_CAR_QUEUE_CAPACITY = 20;
	
	/**
	 * 消息车队列
	 */
	private LinkedList<ChatMessageCar>chatMessageCarQueue = new LinkedList<ChatMessageCar>();
	
	/**
	 * 等待上客的消息车
	 */
	private ChatMessageCar waitMessageCar;
	
	
	/**
	 * 空闲|回收的消息车
	 */
	private LinkedList<ChatMessageCar>idleMessageCarQueue = new LinkedList<ChatMessageCar>();
	
	/**
	 * 世界频道消息发送器
	 */
	private WorldChannelSender sender;
	
	public WorldChannelFilter(String flag,ChatServiceActor serviceActor){
		setFlag(flag);
		setServiceActor(serviceActor);
		sender = new WorldChannelSender();
		sender.start();
	}
	
	@Override
	public Object filter(Object object) {
		object = filter0(object);
		if (object == null) {
			return null;
		}
		if (object instanceof SendWorldMessageEvent) {
			this.sender.send();
		}
		return object;
	}
	
	public Object filter0(Object object){
		if (object instanceof ChatMessage) {
			ChatMessage chatMessage = (ChatMessage)object;
			if (chatMessage.getChannel() != ChatManager.CHANNEL_WORLD) {
				return chatMessage;
			}
			this.gotOnMessageCar(chatMessage);
			return null;
		}
		return object;
	}
	
	@Override
	public void removeChater(long roleId) {
		if (sender != null) {
			sender.removeChater(roleId);
		}
	}
	
	/**
	 * @param message
	 * 聊天消息上车
	 */
	public void gotOnMessageCar(ChatMessage message){
		if (waitMessageCar == null) {
			waitMessageCar = createChatMesaageCar();
		}
		waitMessageCar.addChatMessage(message);
		if (getMessageCarQueueSize() == 0 || waitMessageCar.size() >= MESSAGE_CAR_CAPACITY) {
			messageCarDriveAway();
		}
		return;
	}
	
	/**
	 * 消息车满载或者消息车队列为空，消息车开走进入消息车队列
	 */
	public void messageCarDriveAway(){
		chatMessageCarQueue.addLast(waitMessageCar);
		if (getMessageCarQueueSize() > MESSAGE_CAR_QUEUE_CAPACITY) {
			ChatMessageCar car = chatMessageCarQueue.pollFirst();
			//重置，准备回收
			car.reset();
			//消息队列达到限制后，取最先放入的消息车出来循环利用
			comeWaitMessageCar(car);
		}else {
			comeWaitMessageCar(null);
		}
	}
	
	/**
	 * @param messageCar
	 * 消息车开来上客
	 */
	public void comeWaitMessageCar(ChatMessageCar messageCar){
		if (messageCar == null) {
			waitMessageCar = createChatMesaageCar(); 
		}else {
			this.waitMessageCar = messageCar;
		}
	}
	
	/**
	 * @param idleCar
	 * 
	 *  回收消息车
	 */
	public void putMessageCarToIdleQueue(ChatMessageCar idleCar){
		idleCar.reset();
		idleMessageCarQueue.addFirst(idleCar);
	}
	
	/**
	 * @return
	 * 
	 * 创建消息车
	 */
	public ChatMessageCar createChatMesaageCar(){
		return new ChatMessageCar(MESSAGE_CAR_CAPACITY);
	}
	
	/**
	 * @return
	 * 消息队列长度
	 */
	public int getMessageCarQueueSize(){
		return chatMessageCarQueue.size();
	}
	
	/**
	 * @return
	 * 从消息车队列中取出最后一个消息车
	 */
	public ChatMessageCar fetchMessageCarFromQueue(){
		return chatMessageCarQueue.pollLast();
	}
	
	
	public boolean stopSenderWork(){
		sender.setRunnable(false);
		return true;
	}
	
	public boolean newSender(){
		sender = new WorldChannelSender();
		sender.run();
		return true;
	}
	
	
	
	/**
	 * @author dengzhou
	 * 
	 * 世界频道消息发送器
	 *
	 */
	class WorldChannelSender extends Thread{
		boolean runnable;
		private ByteBuffer byteBuffer;
		/**
		 * 当前给xx发消息
		 */
		private MyLinkedListNode curOrderPointer;
		/**
		 * 上一轮发消息的时间，不为0时表示已经开启一轮
		 */
		private long lastRingTime = 0l;
		/**
		 * 当前正在发送的消息车
		 */
		private ChatMessageCar messageCar;
		
		private SendWorldMessageEvent sEvent;
		
		public WorldChannelSender(){
			runnable = true;
			byteBuffer = ByteBuffer.allocate(1300);
			sEvent = new SendWorldMessageEvent();
		}
		
		public void removeChater(long roleId){
			//如果移除者刚好是发消息时的指针，需要把指针指向下一个
			if (curOrderPointer == null) {
				return;
			}
			ChaterObject chaterObject = (ChaterObject)curOrderPointer.getObject();
			if (chaterObject.getRoleId() != roleId) {
				return;
			}
			curOrderPointer = curOrderPointer.next;
		}
		
		public void send(){
			//如果pointer指向的人已经下线了，这个需要处理一下，暂时想到通过判断object是否为空来做
			//到最后或者是还没开始
			if (curOrderPointer == null || curOrderPointer.getObject() == null) {
				//已经开始了，curOrderPointer == null表明已经轮发过一次了，丢弃此消息
				if (lastRingTime != 0) {
					messageCar = null;
				}
				//试图重新开始发起，若时间太短可能在休息，暂时不继续发送消息
				if (!resetCurOrderPointer()) return;
			}
			if (lastRingTime != 0l && messageCar == null) {
				//已经开启一轮，但是没有消息，强制取一个消息车
				this.getMessageCar(true);
			}else if(lastRingTime != 0l){
				//当开启一轮才去拿消息，否则消息失效了
				//消息车队列太长，尝试着换一个消息车，保证消息的时效性
				this.getMessageCar(false);
			}
			if (messageCar != null) {
				ClientChatMessage cm;
				try {
					cm = new ClientChatMessage(messageCar.getMessageBaytes(byteBuffer));
				} catch (Exception e) {
					LogUtil.error(e.getMessage(), e);
					//出异常了，换一个messageCar
					this.getMessageCar(true);
					return;
				}finally{
					byteBuffer.clear();
				}
				int counter = 0;//计数器，每次发送消息的人数，暂定每次只给20个人发送消息
				while (curOrderPointer != null && counter < 20) {
					ChaterObject cObject = (ChaterObject)curOrderPointer.getObject();
					if ((getFlag().equals("rm") && !cObject.getRefuseChannel().contains(ChatManager.CHANNEL_RM))
							||getFlag().equals("rmcamp") && !cObject.getRefuseChannel().contains(ChatManager.CHANNEL_RM_CAMP)
							||(getFlag().equals("world") && !cObject.getRefuseChannel().contains(ChatManager.CHANNEL_WORLD))
							|| (getFlag().equals("teamInvitation") && !cObject.getRefuseChannel().contains(ChatManager.CHANNEL_TEAM_INVITATION))
							|| (getFlag().equals("system") && !cObject.getRefuseChannel().contains(ChatManager.CHANNEL_SYSTEM))) {
						//这里也要考虑一下object是否为空的情况（下线了）
						long roleid = cObject.getRoleId();
						PlayerUtil.send(roleid, cm);
						counter++;
					}
					curOrderPointer = curOrderPointer.next;
				}
			}
		
		}
		public void run(){
			while (runnable) {
				try {
					getServiceActor().tell(sEvent, Actor.noSender);
				} catch (Exception e) {
					LogUtil.error(e.getMessage(), e);
				}
				//20毫秒执行一次  中间损耗的时间就忽略不计了
				doSleep(20l);	
			}
			byteBuffer.clear();
		}
		
		/**
		 * 当所有玩家都发送过一次消息后，重置到头部
		 */
		private boolean resetCurOrderPointer(){
			long nowTime = System.currentTimeMillis();
			//已经开始发起，且一轮的时间小于1.5S，先休息一下
			if (lastRingTime != 0 && nowTime - lastRingTime < 1500) {
				return false;
			}
			curOrderPointer = getServiceActor().getFirstWorldChannelOrder();
			if (curOrderPointer != null) {
				//重新开始一轮
				lastRingTime = nowTime;
			}else {
				//当前没有在线玩家，进入等待状态
				lastRingTime = 0l;
			}
			return true;
		}
		
		private void getMessageCar(boolean force){
			if (this.messageCar == null || force || chatMessageCarQueue.size() > 10) {
				this.messageCar = fetchMessageCarFromQueue();
			}
		}
		
		private void doSleep(long time){
			try {
				sleep(time);
			} catch (Exception e) {
				LogUtil.error(e.getMessage(), e);
			}
		}
		
		
		
		
		public boolean isRunnable() {
			return runnable;
		}
		public void setRunnable(boolean runnable) {
			this.runnable = runnable;
		}
	}
	
	class SendWorldMessageEvent{
		public SendWorldMessageEvent(){};
	} 

}
