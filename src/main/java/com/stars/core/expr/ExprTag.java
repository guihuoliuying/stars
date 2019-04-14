package com.stars.core.expr;

/**
 * Created by zhaowenshuo on 2017/3/22.
 */
public interface ExprTag {

    int TAG_IDENTIFIER = 0xFF_00; // [a-zA-Z0-9._]+
    int TAG_DIGITS = 0xFF_01; // [0-9]+
    int TAG_STRING = 0xFF_02; // '[a-zA-Z0-9._]+'
    int TAG_RELATION_OP = 0xFF_03; // < | <= | > | >= | == | !=
    int TAG_OR = 0xFF_10; // or
    int TAG_AND = 0xFF_11; // and
    int TAG_NOT = 0xFF_12; // not
    int TAG_IN = 0xFF_13; // in
    int TAG_BETWEEN = 0xFF_14; // between
    int TAG_OP_POW = 0xFF_20; // ^
    int TAG_OP_MUL = 0xFF_21; // *
    int TAG_OP_DIV = 0xFF_22; // /
    int TAG_OP_MOD = 0xFF_23; // %
    int TAG_OP_ADD = 0xFF_24; // +
    int TAG_OP_SUB = 0xFF_25; // -
    int TAG_EOF = 0xFF_FF; // end of file

    int TAG_PARENTHESIS_LEFT = 0x0028; // (
    int TAG_PARENTHESIS_RIGHT = 0x0029; // )
    int TAG_BRACKET_LEFT = 0x005B; // [
    int TAG_BRACKET_RIGHT = 0x005D; // ]
    int TAG_BRACE_LEFT = 0x007B; // {
    int TAG_BRACE_RIGHT = 0x007D; // }
    int TAG_COMMA = 0x002C; // ,

}
