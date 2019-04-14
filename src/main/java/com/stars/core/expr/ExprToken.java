package com.stars.core.expr;

/**
 * Created by zhaowenshuo on 2017/3/24.
 */
public class ExprToken {

    private int tag;
    private int id;
    private String lexeme;

    public ExprToken(int tag, int id) {
        this.tag = tag;
        this.id = id;
    }

    public ExprToken(int tag, int id, String lexeme) {
        this.tag = tag;
        this.id = id;
        this.lexeme = lexeme;
    }

    public int tag() {
        return tag;
    }

    public String lexeme() {
        return lexeme;
    }

    @Override
    public String toString() {
//        return "{" + String.format("0x%04X", tag) + ", " + id + ",\"" + ((tag & 0xFF00) > 0 ? lexeme : (char) tag) + "\"}";
        return "{" + id + ",\"" + ((tag & 0xFF00) > 0 ? lexeme : (char) tag) + "\"}";
    }
}
