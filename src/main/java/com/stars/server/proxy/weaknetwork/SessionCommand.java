package com.stars.server.proxy.weaknetwork;

import com.stars.util.EmptyUtil;
import com.stars.util.backdoor.BackdoorContext;
import com.stars.util.backdoor.command.AbstractCommand;
import com.stars.util.backdoor.command.CommandOption;
import com.stars.util.backdoor.result.BackdoorCell;
import com.stars.util.backdoor.result.BackdoorResult;
import com.stars.util.backdoor.result.BackdoorRow;
import com.stars.util.backdoor.view.ViewUtil;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by zhangjiahua on 2015/10/27.
 */
public class SessionCommand extends AbstractCommand {

    public static final String CMD_SESSION = "@session"; // stand for "net proxy"

    public SessionCommand() {
        super(CMD_SESSION);
    }

    @Override
    public String getCode() {
        return CMD_SESSION;
    }

    @Override
    public void exec(com.stars.util.backdoor.BackdoorContext context, List<com.stars.util.backdoor.command.CommandOption> optionList) {
        if(optionList.size() == 1 && optionList.get(0).getValue().equals("list")){
            printAll(context);
            return;
        }

        //先拿到命令中的session，需要包含id或者filter
        NetProxySession session = null;
        for(com.stars.util.backdoor.command.CommandOption option:optionList){
            if(option.getKey().equals("id")){
                session= com.stars.server.proxy.weaknetwork.NetProxy.getSession(Integer.parseInt(option.getValue()));
            }else if(option.getKey().equals("filter")){
                session=getSessionByFileter(option.getValue());
            }
        }
        if(session == null){
            printNull(context);
            return;
        }
        for(CommandOption option:optionList){
            if(option.getKey().equals("delay")){
                int delay = Integer.parseInt(option.getValue());
                session.setDelay(delay);
            }else if(option.getKey().equals("read")){
                boolean autoRead = Boolean.parseBoolean(option.getValue());
                session.setAutoRead(autoRead);
                session.getChannel().config().setAutoRead(autoRead);
            }
        }
        com.stars.util.backdoor.result.BackdoorResult result = new com.stars.util.backdoor.result.BackdoorResult(com.stars.util.backdoor.result.BackdoorResult.TYPE_NULL, new com.stars.server.proxy.weaknetwork.SessionView());
        printSession(result, session, 0);
        context.setLastCommandResult(result);
    }

//    private TitleRow getSessionTitle() {
//        TitleRow titleRow = new TitleRow();
//        titleRow.add(SessionTitle.PROPERTY);
//        titleRow.add(SessionTitle.VALUE);
//        return titleRow;
//    }

    private void printNull(com.stars.util.backdoor.BackdoorContext context){
        com.stars.util.backdoor.result.BackdoorResult result = new com.stars.util.backdoor.result.BackdoorResult(com.stars.util.backdoor.result.BackdoorResult.TYPE_NULL, new com.stars.server.proxy.weaknetwork.SessionView());
        printSession(result, null, 0);
        context.setLastCommandResult(result);
    }

    private NetProxySession getSessionByFileter(String filter){
        if(EmptyUtil.isEmpty(filter)){
            return null;
        }
        String[] params = filter.split("&");
        if(params.length == 2){
            int removePort = Integer.parseInt(params[0]);
            int localPort = Integer.parseInt(params[1]);
            return getSession(removePort,localPort);
        }else if(params.length == 4){
            String remoteIp = params[0];
            int removePort = Integer.parseInt(params[1]);
            String localIp = params[2];
            int localPort = Integer.parseInt(params[3]);
            return getSession(remoteIp,removePort,localIp,localPort);
        }else{
            return null;
        }
    }

    private NetProxySession getSession(int removePort,int localPort){
        NetProxySession session = null;
        Iterator iter = com.stars.server.proxy.weaknetwork.NetProxy.getSessionMap().entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<Integer, NetProxySession> entry = (Map.Entry<Integer, NetProxySession>) iter.next();
            if (entry.getValue().isFix(removePort,localPort)) {
                session = entry.getValue();
                break;
            }
        }
        return session;
    }

    private NetProxySession getSession(String remoteIp, int remotePort, String localIp, int localPort) {
        NetProxySession session = null;
        Iterator iter = com.stars.server.proxy.weaknetwork.NetProxy.getSessionMap().entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<Integer, NetProxySession> entry = (Map.Entry<Integer, NetProxySession>) iter.next();
            if (entry.getValue().isFix(remoteIp, localIp, remotePort, localPort)) {
                session = entry.getValue();
                break;
            }
        }
        return session;
    }

    private void printSession(com.stars.util.backdoor.result.BackdoorResult result, NetProxySession session, int cidx) {
        if (result == null) {
            result = new com.stars.util.backdoor.result.BackdoorResult(com.stars.util.backdoor.result.BackdoorResult.TYPE_NULL, new com.stars.server.proxy.weaknetwork.SessionView());
        }
        com.stars.util.backdoor.result.BackdoorRow row = new BackdoorRow(cidx);
        com.stars.util.backdoor.result.BackdoorCell cell = null;

        // session id
        cell = new com.stars.util.backdoor.result.BackdoorCell(cidx, 0, com.stars.util.backdoor.view.ViewUtil.toStr(session.getSessionId(), ""));
        row.addCell(cell);
        // local address
        cell = new com.stars.util.backdoor.result.BackdoorCell(cidx, 1, session.getLocalIp() + ":" + session.getLocalPort());
        row.addCell(cell);
        // remote address
        cell = new com.stars.util.backdoor.result.BackdoorCell(cidx, 2, session.getRemoteIp() + ":" + session.getRemovePort());
        row.addCell(cell);
        // auto read
        cell = new com.stars.util.backdoor.result.BackdoorCell(cidx, 3, com.stars.util.backdoor.view.ViewUtil.toStr(session.isAutoRead(), "TRUE"));
        row.addCell(cell);
        // delay
        cell = new com.stars.util.backdoor.result.BackdoorCell(cidx, 4, com.stars.util.backdoor.view.ViewUtil.toStr(session.getDelay(), "0"));
        row.addCell(cell);
        // read count
        cell = new BackdoorCell(cidx, 5, ViewUtil.toStr(session.getReadCount(), "0"));
        row.addCell(cell);

        result.addRow(row);
    }

    private void printAll(BackdoorContext context){
        Iterator iter = NetProxy.getSessionMap().entrySet().iterator();
        com.stars.util.backdoor.result.BackdoorResult result = new com.stars.util.backdoor.result.BackdoorResult(BackdoorResult.TYPE_NULL, new SessionView());
        int cidx = 0;
        while(iter.hasNext()){
            Map.Entry<Integer,NetProxySession> entry = (Map.Entry<Integer,NetProxySession>)iter.next();
            printSession(result, entry.getValue(), cidx++);
        }
        context.setLastCommandResult(result);
    }
}
