package com.stars.server.proxy.weaknetwork;

import com.stars.util.backdoor.BackdoorContext;
import com.stars.util.backdoor.command.AbstractCommand;
import com.stars.util.backdoor.command.CommandOption;
import com.stars.util.backdoor.result.BackdoorCell;
import com.stars.util.backdoor.result.BackdoorResult;
import com.stars.util.backdoor.result.BackdoorRow;
import com.stars.util.backdoor.view.impl.ErrorView;
import com.stars.util.log.CoreLogger;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by zws on 2015/10/13.
 */
public class NetpCommand extends AbstractCommand {

    public static final String CMD_NETP = "@netp"; // stand for "net proxy"

    public NetpCommand() {
        super(CMD_NETP);
    }

    @Override
    public void exec(BackdoorContext context, List<CommandOption> optionList) {

        exec0(optionList.get(0).getValue());

        com.stars.util.backdoor.result.BackdoorResult result = new com.stars.util.backdoor.result.BackdoorResult(BackdoorResult.TYPE_NULL, new ErrorView());
        com.stars.util.backdoor.result.BackdoorCell cell = new BackdoorCell(0, 0, "Ok");
        com.stars.util.backdoor.result.BackdoorRow row = new BackdoorRow(0);
        row.addCell(cell);
        result.addRow(row);
        context.setLastCommandResult(result);
    }

    @Override
    public String getCode() {
        return CMD_NETP;
    }

    private void exec0(String op) {
        switch (op) {
            case "discard":
                NetProxy.flag = false;
                break;
            case "close":
                NetProxy.serverChannel.close();
                break;
            case "enableAutoRead":
                setAutoRead(true);
                break;
            case "disableAutoRead":
                setAutoRead(false);
                break;
            default:
                break;
        }
    }

    /**
     * 强制改变连接服与主服之间channel的可读状态
     */
    private void setAutoRead(boolean isAuto){
        Iterator iter = NetProxy.getSessionMap().entrySet().iterator();
        while(iter.hasNext()){
            Map.Entry<Integer,NetProxySession> entry = (Map.Entry<Integer,NetProxySession>)iter.next();
            entry.getValue().setAutoRead(isAuto);
            entry.getValue().getChannel().config().setAutoRead(isAuto);
        }
        CoreLogger.info("强制改变连接服autoRead，改变后状态为:"+isAuto);
    }
}
