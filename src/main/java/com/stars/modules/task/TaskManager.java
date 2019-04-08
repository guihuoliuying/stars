package com.stars.modules.task;

import com.stars.modules.task.prodata.TaskVo;

import java.util.HashMap;

/**
 * @author dengzhou
 *
 */
public class TaskManager {

    /**
     * 主线任务
     */
    private static HashMap<Integer, TaskVo> TaskVoMap;

    /**
     * 任务分类定义
     */
    public static final byte Task_Sort_ZhuXian = 1;//主线任务
    public static final byte Task_Sort_Daily = 2;//日常任务
    public static final byte Task_Sort_Guanka = 3;//关卡任务
    public static final byte Task_Sort_HuoDong = 4;//活动任务
    public static final byte Task_Sort_AutoDaily = 5;//自动接取，每天会重置的日常任务
	public static final byte Task_Sort_Family = 8;//家族任务

	/**
	 * 任务重置定义
	 */
	public static final byte Task_Reset_Type_DailyReset = 1; //TaskVo dailyreset字段配置重置
	public static final byte Task_Reset_Type_Active = 2; //TaskVo actieve字段配置重置
	public static final byte Task_Non_Daily_Reset = 0; //非日重置
	public static final byte Task_Daily_Reset = 1; //日重置
	public static final byte Task_Inactive = 0;  //已失效
	public static final byte Task_Active = 1; //有效

    /**
     * 任务类型定义 1NPC对话，2收集道具，3通关,4需要达到等级
     */
    public static final byte Task_Type_TalkWithNpc = 1;
    public static final byte Task_Type_CollectTool = 2;//收集道具
    public static final byte Task_Type_PassGuanKa = 3;//通关
    public static final byte Task_Type_UpLevel = 4;//升级
	public static final byte Task_Type_CollectTool_NotDel = 5;//收集道具，和2的区别是，本类型任务完成之后不扣除道具
    public static final byte Task_Type_PassBraveStage = 6;//通关勇者试炼关卡

	public static final byte Task_Type_DailyCount = 7;// 皇榜悬赏
	public static final byte Task_Type_RideUpLv = 8;//坐骑升级
	public static final byte Task_Type_BuddyUpLv = 9;//伙伴升级
	public static final byte Task_Type_EquipStrengthLv = 10;//装备强化等级
	public static final byte Task_Type_EquipWashCount = 11;//装备洗练N次
	public static final byte Task_Type_PvpCount = 12;//参加演武场N次
	public static final byte Task_Type_Authentic = 13;// 鉴宝达到N次
	public static final byte Task_Type_FamEscortKill = 14;//运镖杀人次数
	public static final byte Task_Type_SoulLevel = 19;//表示需要元神等级达到某个部位某一等级
	public static final byte Task_Type_SoulStage = 20;//表示需要元神达到某一阶级

    
    /**
     * 任务状态定义
     */
    public static final byte Task_State_CanAccept = 1;//可接受
    public static final byte Task_State_Accept = 2;//已接受
    public static final byte Task_State_CanSubmit = 3;//可提交


    public static TaskVo getTaskById(int id) {
        return TaskVoMap.get(id);
    }

    public static TaskVo getFirstTaskByGroup(int group){
		if (TaskVoMap != null) {
			for (TaskVo taskVo : TaskVoMap.values()) {
				if (taskVo.getGroup() == group && taskVo.getReqPreTask() == 0) {
					return taskVo;
				}
			}
		}
		
		return null;
	}
    
	public static HashMap<Integer, TaskVo> getTaskVoMap() {
		return TaskVoMap;
	}


	public static void setTaskVoMap(HashMap<Integer, TaskVo> taskVoMap) {
		TaskVoMap = taskVoMap;
	}
	
	public static String getTaskKey(byte type,String target){
		StringBuffer buffer = new StringBuffer();
		switch (type) {
			case TaskManager.Task_Type_TalkWithNpc:
				buffer.append("npc");
				break;
			case TaskManager.Task_Type_CollectTool:
				buffer.append("tool");
				break;
			case TaskManager.Task_Type_PassGuanKa:
				buffer.append("dungeon");
				break;
			case TaskManager.Task_Type_UpLevel:
				buffer.append("level");
				break;
			case TaskManager.Task_Type_PassBraveStage:
				buffer.append("bravestage");
				break;
			case TaskManager.Task_Type_CollectTool_NotDel:
				buffer.append("toolnotdel");
				break;
			case TaskManager.Task_Type_DailyCount:
				buffer.append("daily");
				break;
			case TaskManager.Task_Type_RideUpLv:
				buffer.append("ride");
				break;
			case TaskManager.Task_Type_BuddyUpLv:
				buffer.append("buddy");
				break;
			case TaskManager.Task_Type_EquipStrengthLv:
				buffer.append("equipstrength");
				break;
			case TaskManager.Task_Type_EquipWashCount:
				buffer.append("equipwash");
				break;
			case TaskManager.Task_Type_PvpCount:
				buffer.append("pvp");
				break;
			case TaskManager.Task_Type_Authentic:
				buffer.append("authentic");
				break;
			case TaskManager.Task_Type_FamEscortKill:
				buffer.append("famescort");
				break;
			case TaskManager.Task_Type_SoulLevel:
				buffer.append("soullevel");
				break;
				case TaskManager.Task_Type_SoulStage:
				buffer.append("soulstage");
				break;

			default:
				buffer.append("default");
				break;
		}
		buffer.append(target);
		return buffer.toString();
	}

}
