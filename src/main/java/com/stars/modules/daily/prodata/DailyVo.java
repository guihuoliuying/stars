package com.stars.modules.daily.prodata;

import com.stars.util.StringUtil;

import java.util.List;

public class DailyVo {

	/**
	 * 日常活动id	填正整数，表示日常活动唯一标识
	 */
	private short dailyid;

	/**
	 * 活动标签分类  填字符串，表示标签分类，一个活动可能在多个标签，格式为 1+2+3
	 */
	private String tagIds;

	/**
	 * 活动名	填gametext表textid，表示活动名字
	 */
	private String name;
	
	/**
	 * 活动描述	填gametext表textid，表示活动的简单描述内容
	 */
	private String describe;
	
	/**
	 * 活动图标	填图片美术资源名，用于界面显示
	 */
	private String icon;
	
	/**
	 * 活动按钮名字	填gametext表textid，表示各项活动按钮的名字
	 */
	private String btnname;
	
	/**
	 * 活动排序	填整数流水号，用于界面排序
	 */
	private short rank;
	/**
	 * 分类	填1表示主界面任务，填2表示家族活动
	 */
	private String sort;
	/**
	 * 活动目标	对应不同活动类型填不同的参数，详见活动类型
	 */
	private String target;
	/**
	 * 奖励	填道具id=数量，表示活跃度奖励，用于界面显示和完成活动后奖励的活跃度
	 */
	private String award;
	/**
	 * 进行次数	对应活动每个周期内可进行的次数，用于界面显示和计数
	 */
	private byte count;
	/**
	 * 奖励展示	填道具id=数量|道具id=数量，用于界面展示物品。
	 */
	private String showitem;
	/**
	 * 重置规则	填0表示次数不重置，填1表示次数每天6点重置，填2表示每周三6点重置。
	 */
	private byte reset;
	/**
	 * 相应模块系统名字，获得推荐战力用
	 */
	private String sysName;
	/**
	 * 是否有多倍奖励
	 */
	private byte mutipleind;
	/**
	 * 是否有超级大奖
	 */
	private byte superawardind;
	/**
	 * 系统开放名，对应open表的name字段
	 */
	private String openName;

	//非数据库字段，在内存里
	private List<Byte> tagList;
	private int canGetScore; //可获得的斗魂值

	public String getTagIds() {
		return tagIds;
	}
	public void setTagIds(String tagIds) throws Exception{
		this.tagIds = tagIds;
		this.tagList = StringUtil.toArrayList(tagIds,Byte.class,'+');

	}
	public short getDailyid() {
		return dailyid;
	}
	public void setDailyid(short dailyid) {
		this.dailyid = dailyid;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescribe() {
		return describe;
	}
	public void setDescribe(String describe) {
		this.describe = describe;
	}
	public String getIcon() {
		return icon;
	}
	public void setIcon(String icon) {
		this.icon = icon;
	}
	public String getBtnname() {
		return btnname;
	}
	public void setBtnname(String btnname) {
		this.btnname = btnname;
	}
	public short getRank() {
		return rank;
	}
	public void setRank(short rank) {
		this.rank = rank;
	}
	public String getSort() {
		return sort;
	}
	public void setSort(String sort) {
		this.sort = sort;
	}
	public String getTarget() {
		return target;
	}
	public void setTarget(String target) {
		this.target = target;
	}
	public String getAward() {
		return award;
	}
	public void setAward(String award) {
		this.award = award;
		if(StringUtil.isEmpty(award))
			return;
		String[] array = award.split("[+]");
		canGetScore = Integer.parseInt(array[1]);
	}
	public byte getCount() {		
		return count;
		
	}
	public void setCount(byte count) {
		this.count = count;
	}
	public String getShowitem() {
		if(showitem.equals("0"))
			return "";
		return showitem;
	}
	public void setShowitem(String showitem) {
		this.showitem = showitem;
	}

	public byte getReset() {
		return reset;
	}
	public void setReset(byte reset) {
		this.reset = reset;
	}

	public String getSysName() {
		return sysName;
	}

	public void setSysName(String sysName) {
		this.sysName = sysName;
	}

	public byte getMutipleind() {
		return mutipleind;
	}

	public void setMutipleind(byte mutipleind) {
		this.mutipleind = mutipleind;
	}

	public byte getSuperawardind() {
		return superawardind;
	}

	public void setSuperawardind(byte superawardind) {
		this.superawardind = superawardind;
	}

	public String getOpenName() {
		return openName;
	}

	public void setOpenName(String openName) {
		this.openName = openName;
	}

	public List<Byte> getTagList() {
		return tagList;
	}

	public void setTagList(List<Byte> tagList) {
		this.tagList = tagList;
	}

	public int getCanGetScore() {
		return canGetScore;
	}

	public void setCanGetScore(int canGetScore) {
		this.canGetScore = canGetScore;
	}
}
