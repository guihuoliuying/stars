package com.stars.util.dirtyword;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhaowenshuo on 2016/9/26.
 */
public class DirtyWordTireNode {

    protected char val;
    protected Map<Character, DirtyWordTireNode> children;
    protected boolean isDirtyWord;

    public DirtyWordTireNode(char val) {
        this.val = val;
        this.children = new HashMap<>();
    }

    public boolean isDirtyWord() {
        return isDirtyWord;
    }

    public void setDirtyWord(boolean dirtyWord) {
        isDirtyWord = dirtyWord;
    }

    public void addChild(CharSequence cs, int idx) {
        if (idx >= cs.length()) {
            setDirtyWord(true);
            return;
        }
        char val = cs.charAt(idx);
        DirtyWordTireNode child = children.get(val);
        if (child == null) {
            child = new DirtyWordTireNode(val);
            children.put(child.val, child);
        }
        child.addChild(cs, idx + 1);
    }

    public int hasPrefix(CharSequence cs, int idx) {
        if (idx >= cs.length()) {
            if (val != 0 && isDirtyWord()) {
                return idx;
            } else {
                return -1;
            }
        }
        char val = cs.charAt(idx);
        DirtyWordTireNode child = children.get(val);
        int findIndex = -1;
        if (child != null) {
            findIndex = child.hasPrefix(cs, idx + 1);
        }
        if (findIndex != -1) {
            return findIndex;
        } else if (val != 0 && isDirtyWord()) {
            return idx;
        } else {
            return -1;
        }
    }

    public void println(StringBuilder sb) {
        if (val == 0) {
        } else {
            sb.append(val);
            if (isDirtyWord()) {
            }
        }
        for (DirtyWordTireNode child : children.values()) {
            child.println(sb);
            sb.deleteCharAt(sb.length()-1);
        }
    }
}
