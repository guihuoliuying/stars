package com.stars.services.sendvigour;

/**
 * Created by gaopeidian on 2017/3/30.
 * 不走actor消息处理机制，相当于一个普通的全局处理类
 */

public class SendVigourServiceActor implements SendVigourService {
	private int curStepId = -1;//当前时间段id，对应activityFlow配置表里的开始stepId，当当前不属于任何一个时间段时这个值为-1
	
    @Override
    public void init() throws Throwable {
       
    }

	@Override
	public void setCurStepId(int curStepId) {
		this.curStepId = curStepId;
	}

	@Override
	public int getCurStepId() {		
		return this.curStepId;
	} 
}
