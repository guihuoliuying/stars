package com.stars.multiserver.payServer;

public class PayMessage {
	
	private HttpConnection channel;
	private String gSon;
	
	public PayMessage(HttpConnection channel,String gSon){
		this.channel = channel;
		this.gSon = gSon;
	}

	public HttpConnection getChannel() {
		return channel;
	}

	public void setChannel(HttpConnection channel) {
		this.channel = channel;
	}

	public String getgSon() {
		return gSon;
	}

	public void setgSon(String gSon) {
		this.gSon = gSon;
	}

}
