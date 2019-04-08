package com.stars.multiserver.packet;

public class PacketDefine {
	public static short CREATE_FIGHTACTOR_REQ = 0x6180;//创建fightactor请求
	public static short CREATE_FIGHTACTOR_BACK = 0x6181;//创建fightactor返回
	public static short ADD_FIGHTER_TO_FIGHTACTOR = 0x6182;//添加新的玩家进fightactor
	public static short LUA_FRAM_DATA = 0x6183;//lua每帧返回数据
	public static short ADD_FIGHTER_BACK = 0x6184;//添加新的玩家进fightactor返回
	public static short OFFLINE_NOTICE = 0x6185;//离线通知
	public static short STOP_FIGHT_ACTOR = 0x6186;//停止fightactor
	public static short CLIENT_CLEAR_FIGHTERS = 0x6187;//通知客户端清理除自己外的其它玩家

}
