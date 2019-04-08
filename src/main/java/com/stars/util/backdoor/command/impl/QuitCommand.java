package com.stars.util.backdoor.command.impl;


import com.stars.util.backdoor.BackdoorContext;
import com.stars.util.backdoor.command.AbstractCommand;
import com.stars.util.backdoor.command.CommandOption;
import com.stars.util.backdoor.command.ICommand;
import com.stars.util.backdoor.result.BackdoorResult;
import com.stars.util.backdoor.view.impl.NullView;

import java.util.List;

public class QuitCommand extends AbstractCommand implements ICommand {
	
	private static final String CMD_QUIT = "@quit";

	public QuitCommand() {
		super(CMD_QUIT);
	}

	@Override
	public void exec(BackdoorContext context, List<CommandOption> optionList) {
        context.setLastCommandResult(new BackdoorResult(BackdoorResult.TYPE_QUIT, new NullView()));
	}

	@Override
	public String getCode() {
		return QuitCommand.CMD_QUIT;
	}

}
