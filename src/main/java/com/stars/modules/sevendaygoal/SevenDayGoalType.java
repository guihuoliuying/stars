package com.stars.modules.sevendaygoal;

/**
 * Created by gaopeidian on 2016/12/19.
 */
public enum SevenDayGoalType{
	Ride_Stage_Goal(1001),
	Equipment_Strengthen_Level_Goal(2001),
	Equipment_Star_Level_Goal(2002),
	Buddy_Level_Goal(3001),
	Gem_Fight_Score_Goal(4001),
	Skill_Level_Goal(5001),
	Fight_Score_Goal(6001),
	Role_Level_Goal(6002),
	Guest_Level_Goal(7001),
	;
	
	private int goalType;
	
	private SevenDayGoalType(int gType){
		goalType = gType;
	}
	
	public int getGoalType(){
		return goalType;
	}
}