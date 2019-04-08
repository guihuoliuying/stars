package com.stars.util.dirtyword;

import com.stars.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhaowenshuo on 2016/9/26.
 */
public class DirtyWordTire {

    private DirtyWordTireNode root;

    public DirtyWordTire() {
        this.root = new DirtyWordTireNode((char) 0);
    }

    public void addDirtyWord(String dirtyWord) {
        dirtyWord = normalizeString(dirtyWord);
        if (dirtyWord == null || dirtyWord.length() == 0) {
            return;
        }
        root.addChild(dirtyWord, 0);
    }

    /**
     * 判断字符串中有没有敏感字
     * @param dirtyWord
     * @return
     */
    public boolean hasDirtyWord(String dirtyWord) {
        dirtyWord = normalizeString(dirtyWord);
        for (int i = 0; i < dirtyWord.length(); i++) {
            if (root.hasPrefix(dirtyWord, i) != -1) {
                return true;
            }
        }
        return false;
    }

    /**
     * 将字符串的敏感字都替换成字符'*'
     * @param dirtyWord
     * @return 转换后的字符串
     */
    public String replaceDirtyWord(String dirtyWord) {
        String normalizedString = normalizeString(dirtyWord);
        List<int[]> indexList = new ArrayList<>();
        int end = -1, len = normalizedString.length();
        for (int i = 0; i < len;) {
            if ((end = root.hasPrefix(normalizedString, i)) != -1) {
                indexList.add(new int[] { i, end });
                i = end;
            } else {
                i++;
            }
        }
        if (indexList.size() == 0) {
            return dirtyWord;
        } else {
            StringBuilder sb = new StringBuilder(normalizedString);
            for (int[] indexArray : indexList) {
                for (int i = indexArray[0]; i < indexArray[1]; i++) {
                    sb.setCharAt(i, '*');
                }
            }
            return sb.toString();
        }
    }

    /**
     * 规范化字符串（去掉空白符/标点，只保留字母、数字和中文）
     * @param dirtyWord
     * @return 转换后的字符串
     */
    private String normalizeString(String dirtyWord) {
        if (dirtyWord == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < dirtyWord.length(); i++) {
            char c = dirtyWord.charAt(i);
            if (Character.isLetterOrDigit(c) || StringUtil.isChineseWithoutPunctuation(c)) {
                sb.append(Character.toLowerCase(c));
            }
        }
        return sb.toString();
    }

}
