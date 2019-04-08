package com.stars.util.backdoor.command;


import com.stars.util.backdoor.BackdoorContext;

import java.util.List;

public interface ICommand {

    /**
     * execute command
     * @param context the context of that command
     * @param optionList
     * @return
     */
    public void exec(BackdoorContext context, List<CommandOption> optionList);
    
    public String getCode();
}
