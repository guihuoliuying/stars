package com.stars.util.backdoor.command.pipeline;


import com.stars.util.backdoor.BackdoorContext;

public class CommandPipeline {
	
	private CommandPipelineElement head;
	
	public CommandPipeline() { }
	
	public CommandPipeline(CommandPipelineElement elem) {
		this.head = elem;
	}

	public void exec(BackdoorContext context) {
		head.exec(context);
	}
	
}
