package com.stars.util.backdoor.parser;


import com.stars.util.backdoor.variables.CVariablesTable;

import java.util.HashMap;
import java.util.Map;


/**
 * 1. support macro expand
 * 2. built-in variables
 * 3. command 
 * 
 * COMMAND 		--> @[a-zA-z]+
 * MACRO_NAME 	--> #[a-zA-z]+\\.[a-zA-z]+
 * ?????? 		--> ${[a-zA-Z]+}
 * @author Ghly
 *
 */
public class Lexer {

	public static int line = 1;
	char peek = ' ';
	Map<String, com.stars.util.backdoor.parser.Word> words = new HashMap<String, com.stars.util.backdoor.parser.Word>();
	
	private CharSequence cs;
	private int index;
	
	public Lexer(String line) {
		this.cs = line;
		index = 0;
	}
	
	void readChar() { 
		if (index < cs.length()) {
			peek = cs.charAt(index++); 
		} else {
			peek = (char) -1;
		}
	}
	
	boolean readChar(char c) {
		readChar();
		if (peek != c) return false;
		peek = ' ';
		return true;
	}
	
	public com.stars.util.backdoor.parser.Token scan() throws Exception {
		// eliminate white spaces
		for (;;readChar()) {
			if (peek == ' ' || peek == '\t') continue;
			else if (peek == '\n') line = line + 1;
			else break;
		}
		// recognize command
		if ('@' == peek) {
			StringBuilder sb = new StringBuilder();
			sb.append('@');
			readChar();
			while (Character.isLetter(peek)) {
				sb.append(peek);
				readChar();
			}
			return new com.stars.util.backdoor.parser.Word(sb.toString(), com.stars.util.backdoor.parser.Tag.COMMAND);
		}
		// recognize macro
		if ('#' == peek) {
			StringBuilder sb = new StringBuilder();
			sb.append('#');
			readChar();
			while (Character.isLetter(peek)) {
				sb.append(peek);
				readChar();
			}
			if ('.' != peek) {
				throw new Exception("");
			} else {
				sb.append('.');
				readChar();
			}
			while (Character.isLetter(peek)) {
				sb.append(peek);
				readChar();
			}
			return new com.stars.util.backdoor.parser.Word(sb.toString(), com.stars.util.backdoor.parser.Tag.MACRO_NAME);
		}
		// recognize single quotes
		if ('\'' == peek) {
			StringBuilder sb = new StringBuilder();
			readChar();
			while ('\'' != peek) {
				sb.append(peek);
				readChar();
			}
			readChar();
			return new com.stars.util.backdoor.parser.Word(sb.toString(), com.stars.util.backdoor.parser.Tag.TEXT);
		}
		// recognize text
		if (Character.isLetter(peek) 
				|| Character.isDigit(peek) 
				|| '-' == peek
				|| '"' == peek
				|| '_' == peek) {
			StringBuilder sb = new StringBuilder();
			while (Character.isLetter(peek) 
					|| Character.isDigit(peek) 
					|| '-' == peek
					|| '"' == peek
					|| '_' == peek) {
				if ('"' == peek) {
					readChar();
					while ('"' != peek) {
						if ('$' == peek) {
							sb.append(evaulateVariables());
						} else {
							sb.append(peek);
						}
						readChar();
					}
					readChar();
				} else {
					sb.append(peek);
					readChar();
				}
			}
			return new Word(sb.toString(), Tag.TEXT);
		}
		if ((char) -1 == peek) {
			return com.stars.util.backdoor.parser.Token.EOF;
		}
		com.stars.util.backdoor.parser.Token tok = new com.stars.util.backdoor.parser.Token(peek);
		peek = ' ';
		return tok;
				
	}
	
	String evaulateVariables() throws Exception {
		StringBuilder vars = new StringBuilder("$");
		if (readChar('{')) {
			vars.append("{");
			readChar();
			while ('}' != peek) {
				vars.append(peek);
				readChar();
			}
			vars.append("}");
			return CVariablesTable.evualate(vars.toString());
		} else {
			throw new Exception();
		}
	}
	
	
	public static void main(String[] args) throws Exception {
		String exam1 = "#date.add(1986 09 29 0000, d, 1)";											// ????????????

		@SuppressWarnings("unused") String exam2 = "#date.add(\"1986 09 29\"0000, d, 1)";			// ??????????????????
		@SuppressWarnings("unused") String exam3 = "#date.add(${SYSTIME_YMDHM}, d, 1)";				// ????????????????????????
		@SuppressWarnings("unused") String exam4 = "#date.add(\"${SYSTIME_YMDHM}\", d, 1)";			// ????????????????????????
		@SuppressWarnings("unused") String exam5 = "#date.add(\"${SYSTIME_YMD}\"\"${SYSTIME_HM}\", d, 1)";	// ?????????????????????
		@SuppressWarnings("unused") String exam6 = "#date.add(\"${SYSTIME_YMD}\"0000, d, 1)";		// ?????????????????????

		@SuppressWarnings("unused") String exam7 = "#date.add('1986 09 290000', d, 1)";				// ???????????????????
		@SuppressWarnings("unused") String exam8 = "#date.add('${SYSTIME_YMDHM}', d, 1)";			// ???????????????????????
		@SuppressWarnings("unused") String exam9 = "#date.add('1986 09 ''290000', d, 1)";			// ??????????????????????????
		@SuppressWarnings("unused") String exam10 = "#date.add('1986 09 '290000, d, 1)";			// ???????????????????????????

		@SuppressWarnings("unused") String exam11 = "#date.add(#date.add(#date.add(198609290000, d, -1), m, 1), y, 1)";

		@SuppressWarnings("unused") String exam12 = "@task period=\"* * - 9 1996\" time=#str.concat(\"${SYSPATH}/log/\", #date.format(\"${SYSTIME_YMDHM}\", \"MM-dd yyyy\")) command='@list obj=linkInst | grep content=#date.format(${SYSTIME_YMDHM}, yyyyMMddHHmm, \"MM-dd HH:mm yyyy\")' | @list obj=fileInst";
		
		Lexer lexer = new Lexer(exam1);
		com.stars.util.backdoor.parser.Token tok = null;
		while ((tok = lexer.scan()) != Token.EOF) {
			System.out.println(tok);
		}
	}
	
}
