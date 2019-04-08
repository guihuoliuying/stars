package com.stars.util.backdoor.command;


import com.stars.util.backdoor.command.impl.*;

import java.util.HashMap;
import java.util.Map;

public class CommandFactory {
	
	private static Map<String, com.stars.util.backdoor.command.ICommand> commandsMap
			= new HashMap<String, com.stars.util.backdoor.command.ICommand>();
	
	public static void register(com.stars.util.backdoor.command.ICommand command) {
		CommandFactory.commandsMap.put(command.getCode(), command);
	}
	
	static {
		/* command that is public */
		CommandFactory.register(new QuitCommand());
        CommandFactory.register(new GrepCommand());
		CommandFactory.register(new LoopCommand());
		/* command that is for debugging */
		CommandFactory.register(new InfoCommand());
		/* command that is for private used */
		CommandFactory.register(new NullCommand());
		CommandFactory.register(new ErroCommand());
	}

	public static com.stars.util.backdoor.command.ICommand parseCommand(String commandString) {
		ICommand cmd = commandsMap.get(commandString.trim());
		if (null != cmd) {
			return commandsMap.get(commandString.trim());
		} else {
			return commandsMap.get(ErroCommand.CMD_ERRO);
		}
	}
	
}
