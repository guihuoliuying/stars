package com.stars.modules.tecentvideo;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.modules.MConst;
import com.stars.modules.data.DataManager;
import com.stars.modules.operateactivity.OpActivityModule;
import com.stars.modules.operateactivity.OperateActivityConstant;
import com.stars.modules.operateactivity.OperateActivityManager;
import com.stars.modules.operateactivity.labeldisappear.DisappearByDays;
import com.stars.modules.operateactivity.labeldisappear.DisappearByTime;
import com.stars.modules.operateactivity.labeldisappear.LabelDisappearBase;
import com.stars.modules.operateactivity.labeldisappear.NeverDisappear;
import com.stars.modules.operateactivity.prodata.OperateActVo;

import java.util.Date;
import java.util.Map;

/**
 * Created by gaopeidian on 2017/5/8.
 */
public class TencentVideoModule extends AbstractModule implements OpActivityModule {
	public TencentVideoModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
		super(MConst.TencentVideo, id, self, eventDispatcher, moduleMap);
	}
	
    @Override
	public int getCurShowActivityId() {
    	return OperateActivityManager.getCurActivityId(OperateActivityConstant.ActType_TencentVideo);
	}
    
	@Override
	public byte getIsShowLabel() {
        int curActivityId = OperateActivityManager.getCurActivityId(OperateActivityConstant.ActType_TencentVideo);
        if (curActivityId == -1) return (byte)0;
        
        OperateActVo operateActVo = OperateActivityManager.getOperateActVo(curActivityId);
        if (operateActVo == null) return (byte)0;
        
        LabelDisappearBase labelDisappearBase = operateActVo.getDisappear();
        if (labelDisappearBase == null) return (byte)0;
        
        if (labelDisappearBase instanceof NeverDisappear) {
			return (byte)1;
		}else if(labelDisappearBase instanceof DisappearByDays){
            int openServerDays = DataManager.getServerDays();
            int continueDays = openServerDays;
            int canContinueDays = ((DisappearByDays)labelDisappearBase).getDays();
            return continueDays > canContinueDays ? (byte)0 : (byte)1;
        }else if (labelDisappearBase instanceof DisappearByTime) {
			Date date = ((DisappearByTime)labelDisappearBase).getDate();
			return date.getTime() < new Date().getTime() ? (byte)0 : (byte)1;
		}  
        
        return (byte)0;
	}
}

