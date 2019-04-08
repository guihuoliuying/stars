package com.stars.util.backdoor.command.impl;

import com.stars.util.backdoor.BackdoorContext;
import com.stars.util.backdoor.command.AbstractCommand;
import com.stars.util.backdoor.command.CommandOption;
import com.stars.util.backdoor.result.BackdoorCell;
import com.stars.util.backdoor.result.BackdoorResult;
import com.stars.util.backdoor.result.BackdoorRow;

import java.util.Iterator;
import java.util.List;

/**
 * Created by zws on 2015/10/13.
 */
public class GrepCommand extends AbstractCommand {

    private static final String CMD_GREP = "@grep";

    public GrepCommand() {
        super(CMD_GREP);
    }

    @Override
    public void exec(BackdoorContext context, List<com.stars.util.backdoor.command.CommandOption> optionList) {
        BackdoorResult prevResult = context.getLastCommandResult();
        CommandOption op = optionList.get(0);

        BackdoorResult nextResult = new BackdoorResult(prevResult.getType(), prevResult.getView());
        Iterator<BackdoorRow> rowItor = prevResult.rowIterator();
        while (rowItor.hasNext()) {
            BackdoorRow row = rowItor.next();
            Iterator<BackdoorCell> cellItor = row.cellIterator();
            while (cellItor.hasNext()) {
                BackdoorCell cell = cellItor.next();
                if (cell.contain(op.getValue())) {
                    nextResult.addRow(row);
                    break;
                }
            }
        }
        context.setLastCommandResult(nextResult);
    }

    @Override
    public String getCode() {
        return CMD_GREP;
    }
}
