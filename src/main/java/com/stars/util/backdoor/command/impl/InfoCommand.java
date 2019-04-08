package com.stars.util.backdoor.command.impl;

import com.stars.util.backdoor.BackdoorContext;
import com.stars.util.backdoor.command.AbstractCommand;
import com.stars.util.backdoor.command.CommandOption;
import com.stars.util.backdoor.command.ICommand;
import com.stars.util.backdoor.result.BackdoorCell;
import com.stars.util.backdoor.result.BackdoorResult;
import com.stars.util.backdoor.result.BackdoorRow;
import com.stars.util.backdoor.view.impl.KeyValueView;

import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

public class InfoCommand extends AbstractCommand implements ICommand {
	
	private static final String CMD_INFO = "@info";

    public InfoCommand() {
        super(CMD_INFO);
    }

    @Override
	public void exec(BackdoorContext context, List<CommandOption> optionList) {

		BackdoorResult result =
				new BackdoorResult(BackdoorResult.TYPE_NULL, new KeyValueView());
		
		BackdoorRow row = null;
		BackdoorCell cell = null;
		Properties properties = System.getProperties();
		int count = 0;
		for (Entry<Object, Object> entry : properties.entrySet()) {
			String key = (String) entry.getKey();
			String val = (String) entry.getValue();
			
			row = new BackdoorRow(count);
			// set key
			cell = new BackdoorCell(count, 0, key);
			row.addCell(cell);
			cell = new BackdoorCell(count, 1, val);
			row.addCell(cell);
			result.addRow(row);
		}
		context.setLastCommandResult(result);
	}

	@Override
	public String getCode() {
		return InfoCommand.CMD_INFO;
	}

}
