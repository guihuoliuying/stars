package com.stars.modules.masternotice.recordmap;

public class MasterNoticeData{
	/**
	 * 皇榜悬赏ID，同产品数据的ID相同
	 */
	private int noticeId;	
	
	private byte status;
	
	/**
	 * 当前进度
	 */
	private int process;
	
	//皇榜悬赏任务的状态
	public static final byte STATUS_NOT_ACCEPT = 0;//未接受
	public static final byte STATUS_ACCEPT = 1;//接受但未完成
	public static final byte STATUS_FINISH = 2;//已完成
	
	public MasterNoticeData(int noticeId,byte status,int process){
		this.noticeId = noticeId;
		this.status = status;
		this.process = process;
	}

	public int getNoticeId() {
		return noticeId;
	}

	public void setNoticeId(int noticeId) {
		this.noticeId = noticeId;
	}

	public byte getStatus() {
		return status;
	}

	public void setStatus(byte status) {
		this.status = status;
	}
	
	public int getProcess() {
		return process;
	}

	public void setProcess(int process) {
		this.process = process;
	}
}
