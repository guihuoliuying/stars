package com.stars.modules.push.conditionparser;

import static com.stars.modules.push.conditionparser.PushCondParserTag.*;

/**
 * Created by zhaowenshuo on 2017/3/24.
 */
public class PushCondLexer {

    private CharSequence cs;
    private int len;
    private int idx;

    private char peek = ' ';
    private StringBuilder lexemeBuilder = new StringBuilder();

    private int tokenId;

    public PushCondLexer(CharSequence str) {
        this.cs = str;
        this.len = str.length();
        this.tokenId = 0;
    }

    public PushCondToken scan() {
        if (peek == 0xFFFF) return null;
        while (isWhiteSpace(peek)) nextChar();

        if (peek == '(') return newTokenAndNextChar(TAG_PARENTHESIS_LEFT);
        if (peek == '（') return newTokenAndNextChar(TAG_PARENTHESIS_LEFT); // 中文
        if (peek == ')') return newTokenAndNextChar(TAG_PARENTHESIS_RIGHT);
        if (peek == '）') return newTokenAndNextChar(TAG_PARENTHESIS_RIGHT); // 中文
        if (peek == '[') return newTokenAndNextChar(TAG_BRACKET_LEFT);
        if (peek == '【') return newTokenAndNextChar(TAG_BRACKET_LEFT);
        if (peek == ']') return newTokenAndNextChar(TAG_BRACKET_RIGHT);
        if (peek == '】') return newTokenAndNextChar(TAG_BRACKET_RIGHT);
        if (peek == '{') return newTokenAndNextChar(TAG_BRACE_LEFT);
        if (peek == '}') return newTokenAndNextChar(TAG_BRACE_RIGHT);
        if (peek == ',') return newTokenAndNextChar(TAG_COMMA);
        if (peek == '，') return newTokenAndNextChar(TAG_COMMA);
        if (peek == '<' || peek == '《') {
            nextChar();
            if (peek == '=' || peek == '＝') return newTokenAndNextChar(TAG_RELATION_OP, "<=");
            else return newToken(TAG_RELATION_OP, "<");
        }
        if (peek == '>' || peek == '》') {
            nextChar();
            if (peek == '=' || peek == '＝') return newTokenAndNextChar(TAG_RELATION_OP, ">=");
            else return newToken(TAG_RELATION_OP, ">");
        }
        if (peek == '=' || peek == '＝') {
            nextChar();
            if (peek == '=' || peek == '＝') return newTokenAndNextChar(TAG_RELATION_OP, "==");
            else throw new IllegalStateException("条件解析异常|缺少:=");
        }
        if (peek == '!' || peek == '！') {
            nextChar();
            if (peek == '=' || peek == '＝') return newTokenAndNextChar(TAG_RELATION_OP, "!=");
            else throw new IllegalStateException("条件解析异常|缺少:=");
        }
        if (isDigit(peek)) {
            appendAndNextChar(peek);
            while (isDigit(peek)) appendAndNextChar(peek);
            return newToken(TAG_DIGITS, getLexemeAndClear());
        }
        if (peek == '\'' || peek == '‘' || peek == '"' || peek == '“') {
            nextChar();
            while (isIdentifierLetter(peek)) appendAndNextChar(peek);
            if (peek != '\'' && peek != '’' && peek != '"' && peek != '”') throw new IllegalStateException("条件解析异常|缺少:'");
            return newTokenAndNextChar(TAG_STRING, getLexemeAndClear());
        }
        if (isLetter(peek)) {
            appendAndNextChar(peek);
            while (isIdentifierLetter(peek)) appendAndNextChar(peek);
            String lexeme = getLexemeAndClear();
            if ("or".equalsIgnoreCase(lexeme)) return newToken(TAG_OR, "or");
            if ("and".equalsIgnoreCase(lexeme)) return newToken(TAG_AND, "and");
            if ("not".equalsIgnoreCase(lexeme)) return newToken(TAG_NOT, "not");
            if ("in".equalsIgnoreCase(lexeme)) return newToken(TAG_IN, "in");
            if ("between".equalsIgnoreCase(lexeme)) return newToken(TAG_BETWEEN, "between");
            return newToken(TAG_IDENTIFIER, lexeme);
        }
        throw new IllegalStateException("条件解析异常|不存在字符:" + peek);
    }

    private void nextChar() {
        if (idx < len)
            peek = cs.charAt(idx++);
        else
            peek = 0xFFFF;
    }

    private void appendAndNextChar(char peek) {
        lexemeBuilder.append(peek);
        nextChar();
    }

    private String getLexemeAndClear() {
        String lexeme = lexemeBuilder.toString();
        lexemeBuilder.delete(0, lexemeBuilder.length());
        return lexeme;
    }

    private PushCondToken newToken(int tag) {
        return new PushCondToken(tag, tokenId++);
    }

    private PushCondToken newToken(int tag, String lexeme) {
        return new PushCondToken(tag, tokenId++, lexeme);
    }

    private PushCondToken newTokenAndNextChar(int tag, String lexeme) {
        nextChar();
        return new PushCondToken(tag, tokenId++, lexeme);
    }

    private PushCondToken newTokenAndNextChar(int tag) {
        nextChar();
        return new PushCondToken(tag, tokenId++);
    }

    private boolean isWhiteSpace(char c) {
        return c == ' ' || c == '　' || c == '\n' || c == '\t' || c == '\b';
    }

    private boolean isDigit(char c) {
        return c >= 0x30 && c <= 0x39;
    }

    private boolean isLetter(char c) {
        return (c >= 0x61 && c <= 0x7A) || (c >= 0x41 && c <= 0x5A);
    }

    private boolean isIdentifierLetter(char c) {
        return isDigit(c) || isLetter(c) || c == '.' || c == '_';
    }
}
