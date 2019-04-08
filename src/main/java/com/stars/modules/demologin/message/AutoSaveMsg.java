package com.stars.modules.demologin.message;

import com.stars.core.persist.SaveDbResult;

/**
 * Created by zhaowenshuo on 2016/1/6.
 */
public class AutoSaveMsg extends BaseLoginMsg {

	private SaveDbResult result;
	
	public AutoSaveMsg(SaveDbResult result){
		this.result = result;
	}

	public SaveDbResult getResult() {
		return result;
	}

	public void setResult(SaveDbResult result) {
		this.result = result;
	}
}
