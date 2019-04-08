package com.stars.util.backdoor.parser;

public class Token {

	public final int tag;
	public Token(int t) { tag = t; }
	public String toString() { return "" + (char) tag; }
	
	public static final Token EOF = new Token(-1);
	
}
