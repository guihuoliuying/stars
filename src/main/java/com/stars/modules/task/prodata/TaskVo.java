package com.stars.modules.task.prodata;

import com.stars.modules.task.TaskManager;

import java.util.HashMap;
import java.util.Map;


/**
 * @author dz
 *任务策划数据，对应策划数据表
 */
public class TaskVo {
	/**
	 * 任务id,填正整数，表示任务唯一标识
	 */
	private int id;
	/**
	 * 任务名,填gametext表key的值，表示界面显示任务名字为对应文本
	 */
	private String name;

	/**
	 * 填图片美术资源名，用于界面显示
	 */
	private String icon;

	/**
	 * 详情界面描述,填gametext表key的值，表示任务详情界面显示的任务描述文本，使用符号表示超链接文本.
	 */
	private String desc;
	/**
	 * 任务类型,填1表示主线任务，2表示日常任务，3表示关卡任务，4表示支线任务，5表示活动任务。
	 */
	private byte sort;

	/**
	 * 奖励	填物品id=数量，多个用|隔开，表示完成任务后可以领取的奖励
	 */
	private String award;
	/**
	 *接受条件,格式为：最小等级|最大等级
	 */
//	private String condition;
	/**
	 * 接受优先级	填正整数，同等接受条件的情况下，接受优先级越高，越优先接受。同时用于主线任务、支线任务、关卡任务的顺序号。
	 */
	private int prior;

	/**
	 * 填1NPC对话，2收集道具，3通关,4需要达到等级
	 */
	private byte type;

	/**
	 * 完成条件
	 * type为1时，填0，type为2时，填itemid=数量，type为3时，填stageid，type为4时，填npcid，详见任务完成条件和临时交互npc说明。type为5时，填整数表示需要达到角色等级
	 */
//	private String target;

	/**
	 * 完成任务后自动接取下一步任务     格式： 任务ID|任务ID
	 */
//	private String next;

	/**
	 * 接受任务npc 填npcid，表示与npc对话接受任务，填0表示自动接受
	 */
	private int acceptnpc;

	/**
	 * 接受任务对话  填半身像1+左右+textid1|半身像2+左右+textid2|半身像3+左右+textid3，填0表示没有任务对话
	 */
	private String accepttalk;

	/**
	 * 提交任务npc 填npcid，表示与npc对话提交任务，填0表示自动提交
	 */
	private int submitnpc;

	/**
	 * 提交任务对话  填半身像1，左右，textid1|半身像2，左右，textid2|半身像3，左右，textid3，不能填0
	 */
	private String submittalk;


	/**
	 * 分组类型 填正整数，多条任务配置同一个值，表示属于同一组，用于主线或支线串行任务组，详见串行任务
	 */
	private int group;
	/**
	 * 是否每日充值 0--不重置 1--重置
	 */
	private byte dailyReset;
	/**
	 * 任务是否还有效 0--无效 1--有效  失效触发点为日重置
	 */
	private byte active;
	private String drama;// 剧情配置

	private String cg;

	private Map<Integer, Integer> awardMap;

	private int reqMinLevel = 1;//接取该任务的最小等级要求

	private int reqMaxLevel = 0;//接取该任务的最大等级要求

	private int reqPreTask = -1;//接取该任务的前置完成任务

	private int[] nextTask;

	private String target;

	private int targetCount;

	private String key;
	private String completeDesc;

	public byte getType() {
		return type;
	}
	public void setType(byte type) {
		this.type = type;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public byte getSort() {
		return sort;
	}
	public void setSort(byte sort) {
		this.sort = sort;
	}
	public String getTarget() {
		return target;
	}
	public void setTarget(String target) {
		String[] sts = target.split("\\=");
		this.target = sts[0];
		if (sts.length > 1) {
			this.targetCount = Integer.parseInt(sts[1]);
		}else {
			this.targetCount = 0;
		}
		setKey(TaskManager.getTaskKey(type, this.target));
	}
	public void setAward(String award) {
		if (award == null || award.equals("") || award.equals("0")) {
			this.award = "";
			awardMap = null;
			return;
		}
		this.award = award;
		awardMap = new HashMap<Integer, Integer>();
		String[] sts = award.split("\\|");
		String[] ts;
		for(String tmp : sts){
			ts = tmp.split("=");
			awardMap.put(Integer.parseInt(ts[0]), Integer.parseInt(ts[1]));
		}
	}
	public void setCondition(String condition) {
		if (condition == null || condition.equals("") || condition.equals("0")) {
			return;
		}
		String sts[] = condition.split("\\+");
		if (!sts[0].equals("")) {
			this.setReqMinLevel(Integer.parseInt(sts[0]));
		}
		if (!sts[1].equals("")) {
			this.setReqMaxLevel(Integer.parseInt(sts[1]));
		}

		if (!sts[2].equals("")) {
			this.setReqPreTask(Integer.parseInt(sts[2]));
		}
	}

	public String getCondition(){
		return "";
	}

	public int getPrior() {
		return prior;
	}
	public void setPrior(int prior) {
		this.prior = prior;
	}

	public void setNext(String next) {
		if (next == null || next.equals("") || "0".equals(next)) {
			nextTask = null;
			return;
		}
		String[] sts = next.split("\\|");
		nextTask = new int[sts.length];
		for (int i = 0; i < sts.length; i++) {
			nextTask[i] = Integer.parseInt(sts[i]);
		}
	}

	public String getNext(){
		return "";
	}

	public int getNextTaskId(){
		if (nextTask != null && nextTask.length > 0) {
			return nextTask[0];
		}

		return 0;
	}

	public String getAccepttalk() {
		return accepttalk;
	}
	public void setAccepttalk(String accepttalk) {
		this.accepttalk = accepttalk;
	}
	public int getAcceptnpc() {
		return acceptnpc;
	}
	public void setAcceptnpc(int acceptnpc) {
		this.acceptnpc = acceptnpc;
	}
	public int getSubmitnpc() {
		return submitnpc;
	}
	public void setSubmitnpc(int submitnpc) {
		this.submitnpc = submitnpc;
	}
	public String getSubmittalk() {
		return submittalk;
	}
	public void setSubmittalk(String submittalk) {
		this.submittalk = submittalk;
	}
	public int getGroup() {
		return group;
	}
	public void setGroup(int group) {
		this.group = group;
	}

	public int getReqMinLevel() {
		return reqMinLevel;
	}

	public void setReqMinLevel(int reqMinLevel) {
		this.reqMinLevel = reqMinLevel;
	}

	public int getReqMaxLevel() {
		return reqMaxLevel;
	}

	public void setReqMaxLevel(int reqMaxLevel) {
		this.reqMaxLevel = reqMaxLevel;
	}

	public int getReqPreTask() {
		return reqPreTask;
	}

	public void setReqPreTask(int reqPreTask) {
		this.reqPreTask = reqPreTask;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public int getTargetCount() {
		return targetCount;
	}
	public void setTargetCount(int targetCount) {
		this.targetCount = targetCount;
	}
	public Map<Integer, Integer> getAwardMap() {
		return awardMap;
	}
	public void setAwardMap(Map<Integer, Integer> awardMap) {
		this.awardMap = awardMap;
	}
	public int[] getNextTask() {
		return nextTask;
	}
	public void setNextTask(int[] nextTask) {
		this.nextTask = nextTask;
	}
	public String getIcon() {
		return icon;
	}
	public void setIcon(String icon) {
		this.icon = icon;
	}
	public String getAward() {
		return award;
	}

	public String getDrama() {
		return drama;
	}

	public void setDrama(String drama) throws Exception {
		this.drama = drama;
	}

	public String getCg() {
		return cg;
	}

	public void setCg(String cg) {
		this.cg = cg;
	}

	public byte getDailyReset() {
		return dailyReset;
	}

	public void setDailyReset(byte dailyReset) {
		this.dailyReset = dailyReset;
	}

	public byte getActive() {
		return active;
	}

	public void setActive(byte active) {
		this.active = active;
	}

	public String getCompleteDesc() {
		return completeDesc;
	}

	public void setCompleteDesc(String completeDesc) {
		this.completeDesc = completeDesc;
	}
}
