package com.stars.modules.gameboard;

import com.stars.AccountRow;
import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.modules.MConst;
import com.stars.modules.data.DataManager;
import com.stars.modules.demologin.LoginModule;
import com.stars.modules.demologin.LoginModuleHelper;
import com.stars.modules.demologin.userdata.LoginInfo;
import com.stars.modules.gameboard.packet.ClientGameboard;
import com.stars.modules.gameboard.prodata.GameboardVo;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by chenkeyu on 2017/1/5 18:33
 */
public class GameboardModule extends AbstractModule {

    public GameboardModule(String name, long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super(name, id, self, eventDispatcher, moduleMap);
    }

    /**
     * 弹出游戏公告
     */
    public void popGameboard() {
        List<GameboardVo> vos = new ArrayList<>();
        for (GameboardVo vo : GameboardManager.gameboardVoMap.values()) {
            if (isNotJudgeServerDate(vo) && isDateCondition(vo) && byPlatForm(vo)) {
                vos.add(vo);
            }
        }
        ClientGameboard cgb = new ClientGameboard();
        cgb.setGameboardVos(vos);
        send(cgb);
    }

    private boolean byPlatForm(GameboardVo vo) {
        try {
            if (vo.getChannels() == null || vo.getChannels().isEmpty())
                return true;
            LoginModule loginModule = module(MConst.Login);
            AccountRow accountRow = LoginModuleHelper.getOrLoadAccount(loginModule.getAccount(), null);
            LoginInfo loginInfo = accountRow.getLoginInfo();
            if (vo.getChannels().contains(loginInfo.getChannel().split("@")[0]))
                return true;
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 按照日期
     *
     * @param vo
     * @return
     */
    private boolean isDateCondition(GameboardVo vo) {
        return System.currentTimeMillis() >= vo.getStartDate() &&
                System.currentTimeMillis() <= vo.getEndDate();
    }

    /**
     * 是否需要判断开服时间
     *
     * @param vo
     * @return
     */
    private boolean isNotJudgeServerDate(GameboardVo vo) {
        int serverDays = DataManager.getServerDays();
        if (vo.getServerdate() == null || vo.getServerdate().equals("0")) return true;
        if (serverDays >= vo.getServerStart() && serverDays <= vo.getServerEnd()) return true;
        return false;
    }
}
