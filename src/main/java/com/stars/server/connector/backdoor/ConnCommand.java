package com.stars.server.connector.backdoor;

import com.stars.server.connector.Connector;
import com.stars.server.connector.ConnectorConfig;
import com.stars.util.backdoor.BackdoorContext;
import com.stars.util.backdoor.command.AbstractCommand;
import com.stars.util.backdoor.command.CommandOption;
import com.stars.util.backdoor.result.BackdoorCell;
import com.stars.util.backdoor.result.BackdoorResult;
import com.stars.util.backdoor.result.BackdoorRow;
import com.stars.util.backdoor.view.impl.KeyValueView;

import java.util.List;

/**
 * Created by zws on 2015/11/6.
 */
public class ConnCommand extends AbstractCommand {

    public static final String CMD_CONN = "@conn";

    public ConnCommand() {
        super(CMD_CONN);
    }

    @Override
    public void exec(BackdoorContext context, List<com.stars.util.backdoor.command.CommandOption> optionList) {
        CommandOption option = optionList.get(0);
        com.stars.util.backdoor.result.BackdoorResult result = null;
        switch (option.getValue()) {
            case "relay":
                result = relay();
                break;
            case "writeback":
                result = writeback();
                break;
        }
        context.setLastCommandResult(result);
    }

    private com.stars.util.backdoor.result.BackdoorResult listConfig() {
        com.stars.server.connector.ConnectorConfig config = com.stars.server.connector.Connector.config;
//        ConsoleResult result = new ConsoleResult()

        return null;
    }

    private com.stars.util.backdoor.result.BackdoorResult relay() {
        return relayOrWriteback(true);
    }

    private com.stars.util.backdoor.result.BackdoorResult writeback() {
        return relayOrWriteback(false);
    }

    private com.stars.util.backdoor.result.BackdoorResult relayOrWriteback(boolean needRelay) {
        ConnectorConfig config = Connector.config;
        config.needRelayTestPacket(needRelay);

        com.stars.util.backdoor.result.BackdoorResult result = new com.stars.util.backdoor.result.BackdoorResult(BackdoorResult.TYPE_NULL, new KeyValueView());
        com.stars.util.backdoor.result.BackdoorRow row = new BackdoorRow(0);

        com.stars.util.backdoor.result.BackdoorCell cell = new com.stars.util.backdoor.result.BackdoorCell(0, 0, "needRelay");
        row.addCell(cell);

        cell = new BackdoorCell(0, 1, Boolean.toString(config.needRelayTestPacket()));
        row.addCell(cell);

        result.addRow(row);

        return result;
    }

}
