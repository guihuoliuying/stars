package com.stars.util.backdoor.command;


import com.stars.util.backdoor.BackdoorContext;

import java.util.List;


public abstract class AbstractCommand implements ICommand {

    protected String code;

    public AbstractCommand(String code) {
        this.code = code;
    }
	
	@Override
	public abstract void exec(BackdoorContext context, List<com.stars.util.backdoor.command.CommandOption> optionList);

    @Override
    public String getCode() {
        return this.code;
    }
	
	@Override
	public String toString() {
		return getCode();
	}

    public String getOption(List<com.stars.util.backdoor.command.CommandOption> optionList, String key) {
        for (com.stars.util.backdoor.command.CommandOption option : optionList) {
            if (option.getKey().equals(key)) {
                return option.getValue();
            }
        }
        return null;
    }

    public String getOption(List<CommandOption> optionList, int index) {
        return optionList.get(index) == null ? null : optionList.get(index).getValue();
    }
	
}
