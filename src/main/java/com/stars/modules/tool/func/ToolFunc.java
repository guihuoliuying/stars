package com.stars.modules.tool.func;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.dungeon.DungeonModule;
import com.stars.modules.role.RoleModule;
import com.stars.modules.tool.ToolManager;
import com.stars.modules.tool.ToolModule;
import com.stars.modules.tool.productdata.ItemVo;
import com.stars.modules.vip.VipModule;
import com.stars.util.DateUtil;
import com.stars.util.StringUtil;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Created by zhaowenshuo on 2016/5/16.
 */
public abstract class ToolFunc {

    ItemVo itemVo;
    
    private String useNotice;
    
    private String useFailedNotice;

    public ToolFunc(ItemVo itemVo) {
        this.itemVo = itemVo;
    }

    public ItemVo itemVo() {
        return itemVo;
    }

    /**
     * 解析配置数据
     * @param function
     */
    public abstract void parseData(String function);

    /**
     * 在使用道具之前必须进行检查
     * @param moduleMap 模块映射表
     * @param count 使用数量
     * @param args 其他参数 fixme: 如果道具有状态的话，估计增加RoleToolRow
     * @return
     */
    public abstract ToolFuncResult check(Map<String, Module> moduleMap, int count, Object... args);

    /**
     * 使用道具
     * @param moduleMap 模块映射表
     * @param count 使用数量
     * @param args 其他参数 fixme: 如果道具有状态的话，估计增加RoleToolRow
     */
    public abstract Map<Integer,Integer> use(Map<String, Module> moduleMap, int count, Object... args);
    
    
    /**
     * @return
     * 使用条件检测
     */
    public ToolFuncResult useCondition(Map<String, Module> moduleMap,Object... args){
    	Map<Byte, String> useCondition = itemVo.getUseCondition();
    	if (useCondition == null || useCondition.size() <= 0) {
			return null;
		}
		ToolModule toolModule = (ToolModule)moduleMap.get(MConst.Tool);
    	RoleModule rm = (RoleModule)moduleMap.get(MConst.Role);
    	Set<Entry<Byte, String>> set =  useCondition.entrySet();
    	for (Entry<Byte, String> entry : set) {
			switch (entry.getKey()) {
			case 1://等级限制
				if (rm.getLevel() < Integer.parseInt(entry.getValue())) {
					if (this.useFailedNotice != null) {
						return new ToolFuncResult(false, new ClientText(useFailedNotice));
					}
					return new ToolFuncResult(false, new ClientText("item_lvllimit",entry.getValue()));
				}
				break;
			case 2://通关副本
				DungeonModule dm = (DungeonModule)moduleMap.get(MConst.Dungeon);
				if (!dm.isPassDungeon(Integer.parseInt(entry.getValue()))) {
					if (this.useFailedNotice != null) {
						return new ToolFuncResult(false, new ClientText(useFailedNotice));
					}
					return new ToolFuncResult(false, new ClientText("item_copylimit",entry.getValue()));
				}
				break;
			case 3://VIP等级限制
				VipModule roleVip = (VipModule)moduleMap.get(MConst.Vip);
				int vipLevel = roleVip.getVipLevel();
				if(vipLevel < Integer.parseInt(entry.getValue())){
					if (this.useFailedNotice != null) {
						return new ToolFuncResult(false, new ClientText(useFailedNotice));
					}
					return new ToolFuncResult(false, new ClientText("item_viplvllimit",entry.getValue()));
				}
				break;
			case 4://限时道具
				long bornTime = (long)args[0];
				long timeLimit = Long.parseLong(entry.getValue());
				if (System.currentTimeMillis() > bornTime + timeLimit*DateUtil.HOUR) {
					if (this.useFailedNotice != null) {
						return new ToolFuncResult(false, new ClientText(useFailedNotice));
					}
					return new ToolFuncResult(false, new ClientText("超过使用时间%s",entry.getValue()));
				}
				break;
			case 5://期限道具
				Timestamp ts = Timestamp.valueOf(entry.getValue());
				if (ts.after(new Timestamp(System.currentTimeMillis()))) {
					if (this.useFailedNotice != null) {
						return new ToolFuncResult(false, new ClientText(useFailedNotice));
					}
					return new ToolFuncResult(false, new ClientText("超过使用期限%s",entry.getValue()));
				}
				break;
			case 6: //每日使用次数限制
				int dailyUsedTimes = toolModule.getDailyUsedLimitByItemId(itemVo.getItemId());
				if(dailyUsedTimes >= Integer.parseInt(entry.getValue())){
					return new ToolFuncResult(false, new ClientText("tool_use_daily_limit"));
				}
				break;
			case 7: //每周次数限制
				int weeklyUsedTimes = toolModule.getWeeklyUsedLimitByItemId(itemVo.getItemId());
				if(weeklyUsedTimes >= Integer.parseInt(entry.getValue())){
					return new ToolFuncResult(false, new ClientText("tool_use_weekly_limit"));
				}
				break;
			case 8: //永久使用次数限制
				int foreverUsedTimes = toolModule.getForeverUsedLimitByItemId(itemVo.getItemId());
				if(foreverUsedTimes >= Integer.parseInt(entry.getValue())){
					return new ToolFuncResult(false, new ClientText("tool_use_forever_limit"));
				}
				break;
			default:
				break;
			}
		}
    	return null;
    }
    
    public void parseNotice(String notice){
    	if (StringUtil.isEmpty(notice) || notice.equals("0")) {
			return;
		}
    	String[] s = notice.split("[+]");
//		if(!"0".equals(s[0])){
			this.setUseNotice(s[0]);
			this.setUseFailedNotice(s[1]);
//		}
    }

	public String getUseNotice() {
		return useNotice;
	}

	public void setUseNotice(String useNotice) {
		this.useNotice = useNotice;
	}

	public String getUseFailedNotice() {
		return useFailedNotice;
	}

	public void setUseFailedNotice(String useFailedNotice) {
		this.useFailedNotice = useFailedNotice;
	}

	public int canAutoUse(Map<String, Module> moduleMap, int count) {
		return count;
	}

	protected Map<Integer, Integer> removeAutoUseTool (Map<Integer, Integer> map){
		List<Integer> maps = isAutoUse(map);
		if(!maps.isEmpty()){
			for(int itemId:maps){
				map.remove(itemId);
			}
		}
		return map;
	}

	protected List<Integer> isAutoUse(Map<Integer, Integer> tempMap){
		List<Integer> itemIds = new ArrayList<>();
		ItemVo itemVo ;
		for(int itemId : tempMap.keySet()){
			itemVo = ToolManager.getItemVo(itemId);
			if(itemVo.isAutoUse()){
				itemIds.add(itemId);
			}
		}
		return itemIds;
	}
}
