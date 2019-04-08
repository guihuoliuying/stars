package com.stars.util.backdoor.command.pipeline;

import com.stars.util.backdoor.BackdoorContext;
import com.stars.util.backdoor.command.CommandOption;
import com.stars.util.backdoor.command.ICommand;

import java.util.List;

public class CommandPipelineElement {
	
	private ICommand command;
	private List<com.stars.util.backdoor.command.CommandOption> optionList;
	private CommandPipelineElement next;
	
	public CommandPipelineElement(
			ICommand command, 
			List<CommandOption> optionList) {
		
		this.command = command;
		this.optionList = optionList;
	}
	
	public void setNext(CommandPipelineElement next) {
		this.next = next;
	}
	
	public void exec(BackdoorContext context) {
		command.exec(context, optionList);
		if (null != next) {
			next.exec(context);
		}
	}
	
	@Override
	public String toString() {
		return "CMD=" + this.command + ", OPT=" + this.optionList;
	}
	
	public String toPipelineString() {
		return toString() + (null == next ? "" : " | " + next.toPipelineString());
	}
	
}
