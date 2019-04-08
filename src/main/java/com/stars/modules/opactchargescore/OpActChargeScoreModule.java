package com.stars.modules.opactchargescore;

import com.stars.core.event.EventDispatcher;
import com.stars.core.gmpacket.specialaccount.SpecialAccountManager;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.demologin.LoginModule;
import com.stars.modules.opactchargescore.userdata.RoleCharge;
import com.stars.modules.operateactivity.OpActivityModule;
import com.stars.modules.operateactivity.OperateActivityConstant;
import com.stars.modules.operateactivity.OperateActivityManager;
import com.stars.modules.operateactivity.labeldisappear.DisappearByDays;
import com.stars.modules.operateactivity.labeldisappear.DisappearByTime;
import com.stars.modules.operateactivity.labeldisappear.LabelDisappearBase;
import com.stars.modules.operateactivity.labeldisappear.NeverDisappear;
import com.stars.modules.operateactivity.opentime.ActOpenTime5;
import com.stars.modules.operateactivity.opentime.ActOpenTimeBase;
import com.stars.modules.operateactivity.prodata.OperateActVo;
import com.stars.services.ServiceHelper;
import com.stars.services.rank.RankConstant;
import com.stars.services.rank.userdata.OpActChargeRankPo;
import com.stars.util.DateUtil;

import java.util.Date;
import java.util.Map;

/**
 * Created by likang on 2017/4/12.
 */

public class OpActChargeScoreModule extends AbstractModule implements OpActivityModule {
	private RoleCharge roleCharge;
	private int curActivityId;
	private long validity;

	public OpActChargeScoreModule(String name, long id, Player self, EventDispatcher eventDispatcher,
                                  Map<String, Module> moduleMap) {
		super(name, id, self, eventDispatcher, moduleMap);
	}

	@Override
	public int getCurShowActivityId() {
		if (isOpenActivity()) {
			return curActivityId;
		}
		return -1;
	}

	@Override
	public byte getIsShowLabel() {
		curActivityId = getCurShowActivityId();
		if (curActivityId == -1) {
			return (byte) 0;
		}
		OperateActVo operateActVo = OperateActivityManager.getOperateActVo(curActivityId);
		if (operateActVo == null) {
			return (byte) 0;
		}
		LabelDisappearBase labelDisappearBase = operateActVo.getDisappear();
		if (labelDisappearBase == null) {
			return (byte) 0;
		}
		if (labelDisappearBase instanceof NeverDisappear) {
			return (byte) 1;
		} else if (labelDisappearBase instanceof DisappearByDays) {
			return (byte) 0;
		} else if (labelDisappearBase instanceof DisappearByTime) {
			Date date = ((DisappearByTime) labelDisappearBase).getDate();
			return date.getTime() < new Date().getTime() ? (byte) 0 : (byte) 1;
		}
		return (byte) 0;
	}

	public void handleChargeEvent(int money) {
		LoginModule loginModule = module(MConst.Login);
		String account = loginModule.getAccount();
		if (!isOpenActivity() || SpecialAccountManager.isSpecialAccount(account)) {
			return;
		}
		roleCharge.addTotalCharge(money);
		context().update(roleCharge);
		OpActChargeRankPo ocrp = OpActChargeRankPo.build(roleCharge);
		ServiceHelper.rankService().updateRank(RankConstant.RANK_TYPE_OPACT, ocrp);
	}

	/**
	 * 是否开启活动
	 *
	 * @return
	 */
	public boolean isOpenActivity() {
		curActivityId = OperateActivityManager.getCurActivityId(OperateActivityConstant.ActType_ChargeScore);
		if (curActivityId == -1) {
			return false;
		}
		OperateActVo actVo = OperateActivityManager.getOperateActVo(curActivityId);
		if (actVo == null) {
			return false;
		}
		ActOpenTimeBase openTimeBase = actVo.getActOpenTimeBase();
		if (!(openTimeBase instanceof ActOpenTime5)) {
			return false;
		}
		ActOpenTime5 openTime5 = (ActOpenTime5) openTimeBase;
		validity = openTime5.getEndDate().getTime();
		return DateUtil.isBetween(new Date(), openTime5.getStartDate(), openTime5.getEndDate());
	}

	@Override
	public void onDataReq() throws Throwable {
		String sql = String.format("select * from rolecharge where roleid = %s;", id());
		roleCharge = DBUtil.queryBean(DBUtil.DB_USER, RoleCharge.class, sql);
		isOpenActivity(); //初始化活动时间
		if (roleCharge == null) {
			roleCharge = new RoleCharge(id());
			roleCharge.setValidity(validity);
			roleCharge.setTime(new Date().getTime());
			context().insert(roleCharge);
		} else {
			if (roleCharge.getValidity() < validity) { //过期活动
				roleCharge.reset(validity);
				context().update(roleCharge);
			}
		}

	}

	@Override
	public void onCreation(String name, String account) throws Throwable {
		isOpenActivity(); //初始化活动时间
		roleCharge = new RoleCharge(id());
		roleCharge.setTime(new Date().getTime());
		roleCharge.setValidity(validity);
		context().insert(roleCharge);// 添加插入语句
	}

}
