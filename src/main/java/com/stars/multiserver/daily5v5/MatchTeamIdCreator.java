package com.stars.multiserver.daily5v5;

public class MatchTeamIdCreator {
	
	public static int seed = 1;
	
	public static synchronized int creatId(){
		return seed++;
	}

}
