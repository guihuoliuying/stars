package com.stars.modules.operateCheck;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.util.LogUtil;

import java.util.HashMap;
import java.util.Map;

public class OperateCheckModule extends AbstractModule {

	private static Map<Long, Map<Integer, Long>> checkMap = new HashMap<>();
	
	public OperateCheckModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
		super("CD时间检测", id, self, eventDispatcher, moduleMap);
	}
	
	@Override
	public void onOffline() throws Throwable {
		removeCheckInfo(id());
	}
	
	/**
	 * 玩家操作cd间隔检测
	 * @param id
	 * @param operateId 唯一操作编号    (在OperateConst里添加)
	 * @param checkTime 间隔时间     （毫秒）
	 * @return true 检测通过正常操作         false 操作过于频繁
	 */
	public static boolean checkOperate(long id, int operateId, long checkTime){
		try {
			long currentTime = System.currentTimeMillis();
			Map<Integer, Long> roleOpMap = checkMap.get(id);
			if(roleOpMap==null){
				roleOpMap = new HashMap<>();
				checkMap.put(id, roleOpMap);
			}
			Long opTime = roleOpMap.get(operateId);
			if(opTime==null){
				roleOpMap.put(operateId, currentTime);
				return true;
			}else{
				long passTime = currentTime-opTime;
				if(passTime >= checkTime){
					roleOpMap.put(operateId, currentTime);
					return true;
				}
			}
		} catch (Exception e) {
			com.stars.util.LogUtil.error("玩家操作cd间隔检测失败, roleId:"+id, e);
		}
		//提示
//		PacketManager.send(id, new ClientText("操作过于频繁"));
		return false;
	}
	
	public static void removeCheckInfo(long id){
		try {			
			checkMap.remove(id);
		} catch (Exception e) {
			LogUtil.error("CD检测数据移除失败, roleId:"+id, e);
		}
	}

}
