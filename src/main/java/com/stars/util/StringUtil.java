package com.stars.util;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by jx on 2015/3/31.
 */
public class StringUtil {

	public static String AND_STR = "&";

	public static String EQUAL_STR = "=";

	public static String COMMA = ",";

	private static char[] hexDigit = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
			'a', 'b', 'c', 'd', 'e', 'f' };
	
	private static char[] Letter = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
		'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y','z'};
	
	private static char[] Number = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};

    private static boolean[] validChars = new boolean[128];

    static {
        Arrays.fill(validChars, false);
        /* 数字 */
        validChars['0'] = true;
        validChars['1'] = true;
        validChars['2'] = true;
        validChars['3'] = true;
        validChars['4'] = true;
        validChars['5'] = true;
        validChars['6'] = true;
        validChars['7'] = true;
        validChars['8'] = true;
        validChars['9'] = true;
        /* 大写字母 */
        validChars['A'] = true;
        validChars['B'] = true;
        validChars['C'] = true;
        validChars['D'] = true;
        validChars['E'] = true;
        validChars['F'] = true;
        validChars['G'] = true;
        validChars['H'] = true;
        validChars['I'] = true;
        validChars['J'] = true;
        validChars['K'] = true;
        validChars['L'] = true;
        validChars['M'] = true;
        validChars['N'] = true;
        validChars['O'] = true;
        validChars['P'] = true;
        validChars['Q'] = true;
        validChars['R'] = true;
        validChars['S'] = true;
        validChars['T'] = true;
        validChars['U'] = true;
        validChars['V'] = true;
        validChars['W'] = true;
        validChars['X'] = true;
        validChars['Y'] = true;
        validChars['Z'] = true;
        /* 小写字母 */
        validChars['a'] = true;
        validChars['b'] = true;
        validChars['c'] = true;
        validChars['d'] = true;
        validChars['e'] = true;
        validChars['f'] = true;
        validChars['g'] = true;
        validChars['h'] = true;
        validChars['i'] = true;
        validChars['j'] = true;
        validChars['k'] = true;
        validChars['l'] = true;
        validChars['m'] = true;
        validChars['n'] = true;
        validChars['o'] = true;
        validChars['p'] = true;
        validChars['q'] = true;
        validChars['r'] = true;
        validChars['s'] = true;
        validChars['t'] = true;
        validChars['u'] = true;
        validChars['v'] = true;
        validChars['w'] = true;
        validChars['x'] = true;
        validChars['y'] = true;
        validChars['z'] = true;
        /* 标点符号 */
        validChars['!'] = true;
        validChars['"'] = true;
        validChars['\''] = true;
        validChars[','] = true;
        validChars['-'] = true;
        validChars['.'] = true;
        validChars[':'] = true;
        validChars[';'] = true;
        validChars['='] = true;
        validChars['?'] = true;
        validChars['@'] = true;
        validChars['_'] = true;
        validChars['~'] = true;
    }

    public static boolean isNumeric(String str) {
        int startIndex = str.startsWith("-") ? 1 : 0;
        for (int i = startIndex; i < str.length(); i++) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

	public static String byteToHex(byte b) {
		char[] array = { hexDigit[(b >> 4) & 0x0f], hexDigit[b & 0x0f] };
		return new String(array);
	}

	public static String charToHex(char c) {
		return byteToHex((byte) (c >>> 8)) + byteToHex((byte) (c & 0xff));
	}

//	/**
//	 * 转换编码
//	 *
//	 * @param str
//	 * @return
//	 */
//	public static String getGBKString(String str) {
//		try {
//			return new String(str.getBytes("ISO-8859-1"), "GB2312");
//		} catch (UnsupportedEncodingException e) {
//			LogUtil.exceptionLog.info("getGBKString",  e);
//		}
//		return null;
//	}

	public static boolean isChinese(char c) {
		Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
		if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
				|| ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
				|| ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
				|| ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
			return true;
		}
		return false;
	}

	public static boolean isChineseWithoutPunctuation(char c) {
		Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
		if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A) {
			return true;
		}
		return false;
	}

    /* 数据库兼容 */
    public static boolean isValidString(String str) {
        int len = str.length();
        for (int i = 0; i < len; i++) {
            char c = str.charAt(i);
            if (!isChinese(c) && !(c >= 0 && c <= 127 && validChars[c])) {
                return false;
            }
        }
        return true;
    }

    public static boolean isValidStringWithoutPunctuation(String str) {
        int len = str.length();
        for (int i = 0; i < len; i++) {
            char c = str.charAt(i);
            if (!isChineseWithoutPunctuation(c) && !(c >= 0 && c <= 127 && Character.isLetterOrDigit(c))) {
                return false;
            }
        }
        return true;
    }

	/**
	 * 判断字符是否为中文
	 * 
	 * @param str
	 * @return
	 */
	public static boolean containsChinese(String str) {
		int len = str.length();
		for (int i = 0; i < len; i++) {
			if (isChinese(str.charAt(i))) {
				return true;
			}
		}
		return false;
	}

	public static boolean isEmpty(Object obj) {
		return obj == null;
	}

	public static boolean isEmpty(String str) {
		return str == null || str.trim().length() == 0;
	}

    public static boolean isEmptyIncludeZero(String str){
        return isEmpty(str) || str.equals("0");
    }

	public static boolean isEmpty(Collection collection) {
		return collection == null || collection.size() == 0;
	}

	public static boolean isEmpty(Map map) {
		return map == null || map.size() == 0;
	}

	public static boolean isEmpty(Object[] array) {
		return array == null || array.length == 0;
	}

	public static boolean isNotEmpty(String str) {
		return !isEmpty(str);
	}

	public static boolean isNotEmpty(Collection collection) {
		return !isEmpty(collection);
	}

	public static boolean isNotEmpty(Map map) {
		return !isEmpty(map);
	}

	public static boolean isNotEmpty(Object[] array) {
		return !isEmpty(array);
	}

	/**
	 * 检查字母是否为大写
	 *
	 * @param msg
	 * @return
	 */
	public static boolean containsUpperCase(String msg) {
		Objects.requireNonNull(msg);
		int len = msg.length();
		for (int i = 0; i < len; i++) {
			if (Character.isUpperCase(msg.charAt(i))) {
				return true;
			}
		}
		return false;
	}

    public static <V> V toArray(String src, Class<V> arrayClass, char delimiter) throws Exception {
        List<Object> list = new LinkedList<>();
        Class valClass = arrayClass.getComponentType();
        Constructor constructor = valClass.isPrimitive()
                ? getBoxingClass(valClass).getConstructor(String.class)
                : valClass.getConstructor(String.class);
        int fromIndex = 0;
        int toIndex = src.indexOf(delimiter);
        while (toIndex != -1) {
            list.add(constructor.newInstance(src.substring(fromIndex, toIndex).trim()));
            fromIndex = toIndex + 1;
            toIndex = src.indexOf(delimiter, fromIndex);
        }
        list.add(constructor.newInstance(src.substring(fromIndex).trim()));
        V valArray = (V) Array.newInstance(valClass, list.size());
        int listSize = list.size();
        for (int i = 0; i < listSize; i++) {
            Array.set(valArray, i, list.get(i));
        }
        return valArray;
    }

    private static Class getBoxingClass(Class primitiveClass) {
        if (primitiveClass == long.class) {
            return Long.class;
        }
        if (primitiveClass == int.class) {
            return Integer.class;
        }
        if (primitiveClass == short.class) {
            return Short.class;
        }
        if (primitiveClass == byte.class) {
            return Byte.class;
        }
        if (primitiveClass == double.class) {
            return Double.class;
        }
        if (primitiveClass == float.class) {
            return Float.class;
        }
        if (primitiveClass == boolean.class) {
            return Boolean.class;
        }
        if (primitiveClass == char.class) {
            return Character.class;
        }
        return Void.class;
    }

    public static <V> List<V> toArrayList(String src, Class<V> valClass, char delimiter) throws Exception {
        return (List<V>) toCollection0(src, ArrayList.class, valClass, delimiter);
    }

    public static <V> List<V> toLinkedList(String src, Class<V> valClass, char delimiter) throws Exception {
        return (List<V>) toCollection0(src, LinkedList.class, valClass, delimiter);
    }

    public static <V> Set<V> toHashSet(String src, Class<V> valClass, char delimiter) throws Exception {
        return (Set<V>) toCollection0(src, HashSet.class, valClass, delimiter);
    }

    private static <C extends Collection, V> Collection<V> toCollection0(String src, Class<C> collectionClass, Class<V> valClass, char delimiter) throws Exception {
        Collection<V> collection = collectionClass.newInstance();
        Constructor<V> valConstructor = valClass.getConstructor(String.class);
        src = src.trim();
        if (src.startsWith("[")) {
            src = src.substring(1, src.length()-1);
        }
        int fromIndex = 0;
        int toIndex = src.indexOf(delimiter);
        while (toIndex != -1) {
            collection.add(valConstructor.newInstance(src.substring(fromIndex, toIndex).trim()));
            fromIndex = toIndex + 1;
            toIndex = src.indexOf(delimiter, fromIndex);
        }
        if (fromIndex < src.length()) {
            collection.add(valConstructor.newInstance(src.substring(fromIndex).trim()));
        }
        return collection;
    }

    public static <K, V> Map<K, V> toMap(String src, Class<K> keyClass, Class<V> valClass, char mappingSign, char delimiter) {
        try {
            return toMap0(src, HashMap.class, keyClass, valClass, mappingSign, delimiter);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <K, V> ConcurrentMap<K, V> toConcurrentMap(String src, Class<K> keyClass, Class<V> valClass, char mappingSign, char delimiter) throws Exception {
        return (ConcurrentMap<K, V>) toMap0(src, ConcurrentHashMap.class, keyClass, valClass, mappingSign, delimiter);
    }

    public static <K, V> LinkedHashMap<K, V> toLinkedHashMap(String src, Class<K> keyClass, Class<V> valClass, char mappingSign, char delimiter) throws Exception {
        return (LinkedHashMap<K, V>) toMap0(src, LinkedHashMap.class, keyClass, valClass, mappingSign, delimiter);
    }

	private static <M extends Map, K, V> Map<K, V> toMap0(String src, Class<M> mapClass, Class<K> keyClass, Class<V> valClass, char mappingSign, char delimiter) throws Exception {
		Map<K, V> map = null;
		map = mapClass.newInstance();
        if (src == null || (src = src.trim()).length() == 0) {
            return map;
        }
		Constructor<K> keyConstructor = keyClass.getConstructor(String.class);
		Constructor<V> valConstructor = valClass.getConstructor(String.class);
		int left = 0;
        int idx = 0;
        boolean hasBracket = false;
        if (src.charAt(0) == '{') {
            idx = 1;
            hasBracket = true;
        }
        while (idx < src.length()) {
            int wordStart = idx;
			if (src.charAt(idx) == '(' || src.charAt(idx) == '[' || src.charAt(idx) == '{'
					|| src.charAt(idx) == ')' || src.charAt(idx) == ']' || src.charAt(idx) == '}') {
				break;
			}
            while (src.charAt(idx) != mappingSign) {
                idx++;
                if (idx >= src.length()) {
                    throw new IllegalArgumentException("待解析的字符串异常，str=" + src);
                }
            }
            K key = keyConstructor.newInstance(src.substring(wordStart, idx).trim());
            idx++;
            wordStart = idx;
            while (true) {
                char c = src.charAt(idx);
                if (c == '[' || c == '{' || c == '(') {
                    left++;
                }
                if ((c == delimiter || c == '}' || (!hasBracket && idx == src.length()-1)) && left == 0) {
                    V val = null;
					if (!hasBracket && idx == src.length()-1) {
						val = valConstructor.newInstance(src.substring(wordStart, idx + 1).trim());
					} else {
						val = valConstructor.newInstance(src.substring(wordStart, idx).trim());
					}
                    map.put(key, val);
                    idx++;
                    if (idx < src.length() && src.charAt(idx) == delimiter) {
                        idx++;
                    }
                    break;
                }
                if (c == ']' || c == '}' || c == ')') {
                    left--;
                }
                idx++;
                if (idx >= src.length()) {
                    throw new IllegalArgumentException("待解析的字符串异常，str=" + src);
                }
            }
        }
		return map;
	}

    public static String makeString(Map<? extends Object, ? extends Object> map, char mappingSign, char delimiter) {
        if (map == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<? extends Object, ? extends Object> entry : map.entrySet()) {
            sb.append(entry.getKey().toString())
                    .append(mappingSign)
                    .append(entry.getValue().toString())
                    .append(delimiter);
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    public static String makeString(Object[] objectArray, char delimiter) {
        if (objectArray == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Object obj : objectArray) {
            sb.append(obj).append(delimiter);
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length()-1);
        }
        return sb.toString();
    }

    public static String makeString(Collection<? extends Object> collection, char delimiter) {
        if (collection == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Object obj : collection) {
            sb.append(obj).append(delimiter);
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length()-1);
        }
        return sb.toString();
    }

    public static String makeString2(Collection<? extends Object> list, char delimiter) {
        return "[" + makeString(list, delimiter) + "]";
    }

	public static String makeString2(Map<? extends Object, ? extends Object> map, char mappingSign, char delimiter) {
		return "{" + makeString(map, mappingSign, delimiter) + "}";
	}

    public static String concat(String[] array) {
        StringBuilder sb = new StringBuilder();
        for (String s : array) {
            sb.append(s);
        }
        return sb.toString();
    }

    public static String getChineseNumber(int i){
        switch (i) {
            case 1: return "一";
            case 2: return "二";
            case 3: return "三";
            case 4: return "四";
            case 5: return "五";
            case 6: return "六";
            case 7: return "七";
            case 8: return "八";
            case 9: return "九";
            default: return String.valueOf(i);
        }
    }
    
    public static String toPositionByArray(int [] pos){
    	return pos[0]+"+"+pos[1]+"+"+pos[2];
    }

    public static String getRandomString(int size){
    	Random r = new Random();
    	StringBuffer buffer = new StringBuffer();
    	for (int i = 0; i < size; i++) {
			int a = r.nextInt(1);
			if (a == 0) {
				buffer.append(Number[r.nextInt(Number.length)]);
			}else {
				buffer.append(Letter[r.nextInt(Letter.length)]);
			}
		}
    	return buffer.toString();
    }
    
}
