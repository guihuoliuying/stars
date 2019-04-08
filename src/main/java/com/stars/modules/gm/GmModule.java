package com.stars.modules.gm;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.modules.MConst;
import com.stars.modules.demologin.LoginModule;
import com.stars.modules.gm.packet.ServerGm;
import com.stars.modules.redpoint.RedPointConst;
import com.stars.util.LogUtil;
import com.stars.util.StringUtil;

import java.util.List;
import java.util.Map;

/**
 * Created by zhaowenshuo on 2016/1/14.
 */
public class GmModule extends AbstractModule {

    public GmModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super(MConst.Gm, id, self, eventDispatcher, moduleMap);
    }

    public void handle(ServerGm packet) {
        String commandLine = packet.getCommandLine().replace("，", ",");
        exec(commandLine);
    }

    public void exec(String commandLine) {
        String command = null;
        String[] args = null;
        if (commandLine.contains(" ")) {
            try {
                command = commandLine.substring(0, commandLine.indexOf(' ')).toLowerCase();
                args = StringUtil.toArray(commandLine.substring(command.length() + 1), String[].class, ',');
                for (int i = 0; i < args.length; i++) {
                    args[i] = args[i].trim();
                }
            } catch (Exception e) {
                com.stars.util.LogUtil.error(e.getMessage(), e);
                warn("GM命令参数解析出错");
            }
        } else {
            command = commandLine;
        }

        if (GmManager.contains(command)) {
            try {
                GmManager.get(command).handle(id(), moduleMap(), args);
            } catch (Exception e) {
                LogUtil.error(e.getMessage(), e);
                warn("GM命令出错");
            }
        } else {
            warn("GM命令不存在");
        }
    }

    @Override
    public void onInit(boolean isCreation) throws Throwable {
        signRedPoint();
    }

    @Override
    public void calRedPoint(List<Integer> redPointIds, Map<Integer, String> redPointMap) {
        if (redPointIds.contains(RedPointConst.GM_MESSAGE)){
            if(GmManager.GM_MESSAGE_REDPOINTS.containsKey(id())){
                redPointMap.put(RedPointConst.GM_MESSAGE,"");
                GmManager.GM_MESSAGE_REDPOINTS.remove(id());
            }else {
                redPointMap.put(RedPointConst.GM_MESSAGE,null);
            }
        }
    }

    public void signRedPoint(){
        LoginModule loginModule = module(MConst.Login);
        if(!loginModule.isOnline()) return;
        if(GmManager.GM_MESSAGE_REDPOINTS.containsKey(id())){
            signCalRedPoint(MConst.Gm, RedPointConst.GM_MESSAGE);
        }
    }
}
