package com.stars.modules.poem.userdata;

/**
 * Created by gaoepidian on 2017/1/9.
 */
public class PoemData {
	public int poemId;
	public int finishDungeonCount;
	public int totalDungeonCount;
	
	//产品数据
	public int bossDungeonId;
	public String worldTitle;
	public String worldName;
	public String generalDrop;//通关奖励
	public int recommend;//推荐战力
	public String showItem;
	public String showdescwin;
	public int teamType;
	
	public PoemData(int poemId , int finishDungeonCount , int totalDungeonCount
			, int bossDungeonId , String worldTitle , String worldName , String generalDrop , int recommend
			,  String showItem , String showdescwin , int teamType) {
		this.poemId = poemId;
		this.finishDungeonCount = finishDungeonCount;
		this.totalDungeonCount = totalDungeonCount;
		this.bossDungeonId = bossDungeonId;
		this.worldTitle = worldTitle;
		this.worldName = worldName;
		this.generalDrop = generalDrop;
		this.recommend = recommend;
		this.showItem = showItem;
		this.showdescwin = showdescwin;
		this.teamType = teamType;
	}
}
