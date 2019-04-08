package com.stars.services.chat.filter;

import com.stars.services.chat.ChatServiceActor;

/**
 * @author dengzhou
 *聊天过滤器父类
 */
public abstract class ChatFilter {
	
	private String flag;
	
	private ChatServiceActor serviceActor;
	
	public abstract Object filter(Object object);
	
	public void setFlag(String flag){
		this.flag = flag;
	}
	
	public String getFlag(){
		return flag;
	}

	public ChatServiceActor getServiceActor() {
		return serviceActor;
	}

	public void setServiceActor(ChatServiceActor serviceActor) {
		this.serviceActor = serviceActor;
	}
	
	public void removeChater(long roleId){
		
	}
	
}
