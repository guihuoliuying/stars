package com.stars.util.backdoor.parser;


import com.stars.util.backdoor.command.CommandFactory;
import com.stars.util.backdoor.command.CommandOption;
import com.stars.util.backdoor.command.pipeline.CommandPipeline;
import com.stars.util.backdoor.command.pipeline.CommandPipelineElement;
import com.stars.util.backdoor.macro.MacroFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple parser for console's command.  The BNF as follows:
 * 
 * pipeline 	--> pipeline "|" command | command
 * command 		--> COMMAND options
 * options 		--> options option | ��
 * option 		--> TEXT = macro | TEXT = TEXT
 * macro 		--> MACRO_NAME ( macroParams )
 * macroParams 	--> macroParams macroParam | macroParam
 * macroParam 	--> macro | TEXT | ��
 * 
 * @author Ghly
 *
 */
public class Parser {

	private com.stars.util.backdoor.parser.Lexer lexer;
	private Token look;
	
	public Parser(com.stars.util.backdoor.parser.Lexer l) throws Exception { lexer = l; move(); }
	void move() throws Exception { look = lexer.scan(); }
	void error(String s) throws Exception { throw new Exception(s); }
	void match(int t) throws Exception {
		if (look.tag == t) move();
		else error("syntax error");
	}
	
	public com.stars.util.backdoor.command.pipeline.CommandPipeline program() throws Exception {
		com.stars.util.backdoor.command.pipeline.CommandPipeline pipeline = new CommandPipeline(pipeline());
		return pipeline;
	}
	
	/* pipeline --> pipeline "|" command | command */
	com.stars.util.backdoor.command.pipeline.CommandPipelineElement pipeline() throws Exception {
		com.stars.util.backdoor.command.pipeline.CommandPipelineElement cur = command();
		com.stars.util.backdoor.command.pipeline.CommandPipelineElement next = null;
		if (look.tag == '|') {
			match('|');
			next = pipeline();
		}
		cur.setNext(next);
		return cur;
	}
	
	/* command --> COMMAND options */
	com.stars.util.backdoor.command.pipeline.CommandPipelineElement command() throws Exception {
		String commandName = ((com.stars.util.backdoor.parser.Word) look).lexeme;
		match(com.stars.util.backdoor.parser.Tag.COMMAND);
		List<com.stars.util.backdoor.command.CommandOption> options = options();
		com.stars.util.backdoor.command.pipeline.CommandPipelineElement elem = new com.stars.util.backdoor.command.pipeline.CommandPipelineElement(
				CommandFactory.parseCommand(commandName), options);
		return elem;
	}
	
	/* options --> options option | �� */
	List<com.stars.util.backdoor.command.CommandOption> options() throws Exception {
		List<com.stars.util.backdoor.command.CommandOption> options = new ArrayList<com.stars.util.backdoor.command.CommandOption>();
		if (look.tag == com.stars.util.backdoor.parser.Tag.TEXT) {
			options.add(option());
			options.addAll(options());
		}
		return options;
	}
	
	/* option --> TEXT = macro | TEXT = TEXT */
	com.stars.util.backdoor.command.CommandOption option() throws Exception {
		String key = ((com.stars.util.backdoor.parser.Word) look).lexeme;
		match(com.stars.util.backdoor.parser.Tag.TEXT);
		match('=');
		String val = null;
		if (look.tag == com.stars.util.backdoor.parser.Tag.MACRO_NAME) {
			val = macro();
		} else {
			val = ((com.stars.util.backdoor.parser.Word) look).lexeme;
			match(com.stars.util.backdoor.parser.Tag.TEXT);
		}
		return new CommandOption(key, val);
	}
	
	/* macro --> macroName ( macroParams ) */
	String macro() throws Exception {
		com.stars.util.backdoor.parser.Word macroName = (com.stars.util.backdoor.parser.Word) look;
		List<String> params = new ArrayList<String>();
		match(com.stars.util.backdoor.parser.Tag.MACRO_NAME);
		match('('); 
		params = macroParams(); 
		match(')');
		return MacroFactory.getMacro(macroName.lexeme).expand(params);
	}
	
	/* macroParams --> macroParams macroParam | macroParam */
	List<String> macroParams() throws Exception {
		List<String> params = new ArrayList<String>();
		if (look.tag == ',') {
			match(',');
		}
		String param = macroParam();
		if (null != param) {
			params.add(param);
			params.addAll(macroParams());
		}
		
		return params;
	}
	
	/* macroParam --> macro | TEXT | �� */
	String macroParam() throws Exception {
		String res = null;
		if (look.tag == ')') return null;
		if (look.tag == com.stars.util.backdoor.parser.Tag.MACRO_NAME) {
			res = macro();
		} else if (look.tag == com.stars.util.backdoor.parser.Tag.TEXT) {
			res = ((Word) look).lexeme;
			match(Tag.TEXT);
		} else {
			return "";
		}
		String temp = macroParam();
		res += temp == null ? "" : temp;
		return res;
	}
	
	public static void main(String[] args) throws Exception {
		String line1 = "@list obj=linkInst | @grep content=19860929";
		String line2 = "@list obj=linkInst | @grep content=#date.format(#date.add(#date.add(#date.add(198609290000, d, -1), m, 1), y, 1), 'yyyyMMddHHmm', 'MM-dd yyyy')";
		com.stars.util.backdoor.parser.Lexer lexer = new Lexer(line2);
		Parser parser = new Parser(lexer);
		CommandPipelineElement elem = parser.pipeline();
		System.out.println(elem.toPipelineString());
	}
	
}
