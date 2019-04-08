package com.stars.modules.friendInvite;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.db.DBUtil;
import com.stars.modules.data.DataManager;
import com.stars.modules.friendInvite.event.BindInviteCodeEvent;
import com.stars.modules.friendInvite.listener.InviteListener;
import com.stars.modules.friendInvite.prodata.InviteVo;

import java.util.Map;

/**
 * Created by chenxie on 2017/6/7.
 */
public class InviteModuleFactory extends AbstractModuleFactory<InviteModule> {

    public InviteModuleFactory() {
        super(new InvitePacketSet());
    }

    @Override
    public void loadProductData() throws Exception {
        InviteManager.INVITEATR_ROLELIMIT = DataManager.getCommConfig("inviteatr_rolelimit", 0);
        InviteManager.INVITEATR_REWARD_FIRST = DataManager.getCommConfig("inviteatr_reward_first", 0);
        InviteManager.INVITEATR_REWARD_EVERYTIME = DataManager.getCommConfig("inviteatr_reward_everytime", 0);
        InviteManager.INVITEATR_REWARD_LIMIT = DataManager.getCommConfig("inviteatr_reward_limit", 0);
        InviteManager.INVITEATR_REWARD_BIND = DataManager.getCommConfig("inviteatr_reward_bind", 0);
        String sql = "select * from inviteatr";
        Map<String, InviteVo> inviteVoMap = DBUtil.queryMap(DBUtil.DB_PRODUCT, "channel", InviteVo.class, sql);
        InviteManager.INVITE_VO_MAP = inviteVoMap;
        // 邀请码自增序列
        String sql_ = "select * from sequence";
        Long sequence = DBUtil.queryBean(DBUtil.DB_USER, Long.class, sql_);
        if(sequence == null){ //新服数据库没有sequence需要初始化一条0的数据
            sequence = 0L;
            sql = "insert into sequence value (0)";
            DBUtil.execSql(DBUtil.DB_USER,sql);
        }
        InviteManager.SEQUENCE = sequence;
    }

    @Override
    public InviteModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
        return new InviteModule(id, self, eventDispatcher, map);
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
        InviteListener inviteListener = new InviteListener((InviteModule) module);
        eventDispatcher.reg(BindInviteCodeEvent.class, inviteListener);
    }

}
