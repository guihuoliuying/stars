package com.stars.util.uid;

public class IdHelper {
	
	public static final int WORKER_NUM_BITS = 5;	// 主服标识占位数
	public static final int SERVER_NUM_BITS = 16; 	// 大区id占位数
	public static final int DB_NUM_BITS = 8; 		// dbid占位数
	
	public static final int MAX_SERVERID = 1 << SERVER_NUM_BITS;	// 最大服务区ID
	public static final int MAX_DBID = 1 << DB_NUM_BITS;			// 最大数据库ID
	public static final int MAX_WORKERID = 1 << WORKER_NUM_BITS;	// 最大主服ID
	
	public static long analyzeDbId(long dbKey) {
//		return dbKey & 0xffffff;	// 加上区ID, 截取末尾24位
		return dbKey & 0xff;
	}
}
