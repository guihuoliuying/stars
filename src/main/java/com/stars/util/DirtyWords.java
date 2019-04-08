package com.stars.util;

import com.stars.util.dirtyword.DirtyWordTire;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zhaowenshuo on 2017/4/26.
 */
public class DirtyWords {

    public static String objectPatternString = "\\[obj:(\\w|:|/)*\\]";

    private static boolean[] specialChars = new boolean[128];
    public static DirtyWordTire dirtyWordTire = new DirtyWordTire(); // 版署
    public static DirtyWordTire dirtyWordExt1Tire = new DirtyWordTire(); // 扩展
    public static Pattern pattern; // 正则
    public static Pattern objectPattern; // obj正则

    static {
        /* obj正则 */
        objectPattern = Pattern.compile(objectPatternString);
        /* 特殊字符 */
        Arrays.fill(specialChars, false);
        specialChars['@'] = true;
        specialChars['#'] = true;
        specialChars['$'] = true;
        specialChars['%'] = true;
        specialChars['\\'] = true;
        specialChars['\n'] = true;
        specialChars['\t'] = true;
        specialChars['\b'] = true;
        specialChars['/'] = true;
        specialChars['<'] = true;
        specialChars['>'] = true;
    }

    private static boolean hasSpecialChar(String string){
        int len = string.length();
        for (int i = 0; i < len; i++) {
            char c = string.charAt(i);
            if (c < 128 && specialChars[c]) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasDirtyWordRegex(String str) {
        return pattern.matcher(str).find();
    }

    public static boolean hasDirtyWord(String str) {
        return dirtyWordTire.hasDirtyWord(str);
    }

    public static boolean hasDirtyWordExt1(String str) {
        return dirtyWordExt1Tire.hasDirtyWord(str);
    }

    public static boolean hasDirtyWordWithObject(String str) {
        if (str.contains("obj")) {
            String[] a = str.split(objectPatternString);
            String s = StringUtil.concat(a);
            if (hasDirtyWord(s)) {
                return true;
            } else {
                return false;
            }
        } else {
            return hasDirtyWord(str);
        }
    }

    private static String replaceDirtyWordRegex(String str) {
        return pattern.matcher(str).replaceAll("***");
    }

    /* 特殊方法 */
    /**
     * 检查名字
     * @param name
     * @return
     */
    public static boolean checkName(String name) {
        return hasDirtyWord(name) || hasDirtyWordExt1(name) || hasDirtyWordRegex(name)
                || !StringUtil.isValidStringWithoutPunctuation(name);
    }

    /**
     * 检查公告内容
     * @param notice
     * @return
     */
    public static boolean checkNotice(String notice) {
        return hasDirtyWord(notice) || hasDirtyWordExt1(notice) || hasDirtyWordRegex(notice)
                || !StringUtil.isValidString(notice);
    }

    /**
     * 规范化聊天内容
     * @param message
     * @return
     */
    public static String normalizeChatMessage(String message) {
        message = dirtyWordTire.replaceDirtyWord(message);
        return replaceDirtyWordRegex(message);
    }

    public static String normalizeChatMessageWithObject(String content) {
        Matcher m = objectPattern.matcher(content);
        String[] a = content.split(objectPatternString);
        // 防止用表情/道具分隔敏感字，拼在一起判断，如果存在，则一律替换
        String s = StringUtil.concat(a);
        if (hasDirtyWord(s)) {
            for (int i = 0; i < a.length; i++) {
                a[i] = "*";
            }
        }
        // 分段判断
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < a.length; i++) {
            sb.append(normalizeChatMessage(a[i])).append(m.find() ? m.group() : "");
        }
        while (m.find()) { sb.append(m.group()); }
        return sb.toString();
    }
}
