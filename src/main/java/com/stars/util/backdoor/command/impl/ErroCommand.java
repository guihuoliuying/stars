package com.stars.util.backdoor.command.impl;

import com.stars.util.backdoor.BackdoorContext;
import com.stars.util.backdoor.command.AbstractCommand;
import com.stars.util.backdoor.command.CommandOption;
import com.stars.util.backdoor.result.BackdoorCell;
import com.stars.util.backdoor.result.BackdoorResult;
import com.stars.util.backdoor.result.BackdoorRow;
import com.stars.util.backdoor.view.impl.ErrorView;

import java.util.List;

public class ErroCommand extends AbstractCommand {
	
	public static final String CMD_ERRO = "@erro";
	public ErroCommand() {
		super(CMD_ERRO);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void exec(
			BackdoorContext context,
			List<CommandOption> optionList) {
		
		BackdoorResult result =
				new BackdoorResult(BackdoorResult.TYPE_ERROR, new ErrorView());
		
		BackdoorCell cell = new BackdoorCell(
				0, 0, "Invalid command: " + context.getInputCommand());
		BackdoorRow row = new BackdoorRow(0);
		row.addCell(cell);
		
		result.addRow(row);
		context.setLastCommandResult(result);
		return;
	}

	@Override
	public String getCode() {
		return ErroCommand.CMD_ERRO;
	}

}
