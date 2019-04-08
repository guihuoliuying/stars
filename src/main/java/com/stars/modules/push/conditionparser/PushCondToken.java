package com.stars.modules.push.conditionparser;

/**
 * Created by zhaowenshuo on 2017/3/24.
 */
public class PushCondToken {

    private int tag;
    private int id;
    private String lexeme;

    public PushCondToken(int tag, int id) {
        this.tag = tag;
        this.id = id;
    }

    public PushCondToken(int tag, int id, String lexeme) {
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
