package com.stars.util.backdoor.command.impl;

import com.stars.util.backdoor.Backdoor;
import com.stars.util.backdoor.BackdoorContext;
import com.stars.util.backdoor.command.AbstractCommand;
import com.stars.util.backdoor.command.CommandOption;
import com.stars.util.backdoor.command.pipeline.CommandPipeline;
import com.stars.util.backdoor.parser.Lexer;
import com.stars.util.backdoor.parser.Parser;
import com.stars.util.backdoor.result.BackdoorResult;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhaowenshuo on 2016/2/16.
 */
public class LoopCommand extends AbstractCommand {

    public LoopCommand() {
        super("@loop");
    }

    @Override
    public void exec(com.stars.util.backdoor.BackdoorContext context, List<CommandOption> optionList) {
        String cmd = getOption(optionList, "c");
        int times = getOption(optionList, "t") == null ? 5 : Integer.parseInt(getOption(optionList, "t"));
        int interval = getOption(optionList, "i") == null ? 1 : Integer.parseInt(getOption(optionList, "i"));
        for (int i = 0; i < times; i++) {
            try {
                // Create context
                com.stars.util.backdoor.BackdoorContext ctx = BackdoorContext.newInstance();
                // Set the console
//                context.setBackdoor(this);
                // Set the input to context
                context.setInputCommand(cmd);
                // Set the buffered reader to context
                context.setBufferedReader(context.getBufferedReader());
                // Set the print writer to context
                context.setPrintWriter(context.getPrintWriter());
                // Call syntax parser to generate the pipeline
                CommandPipeline pipeline = new Parser(new Lexer(cmd)).program();
                // Execute
                pipeline.exec(context);
                BackdoorResult result = context.getLastCommandResult();
                // Output result
                if (null != result && BackdoorResult.TYPE_QUIT == result.getType()) {
                    break;
                } else if (null != result) {
                    Backdoor.printResult(context.getPrintWriter(), result);
                }
                context.getPrintWriter().println();
                context.getPrintWriter().flush();
                //
                TimeUnit.SECONDS.sleep(interval);
            } catch (Exception e) {
                e.printStackTrace(context.getPrintWriter());
                context.getPrintWriter().flush();
            }
        }
    }
}
