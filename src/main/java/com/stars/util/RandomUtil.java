package com.stars.util;

import org.apache.commons.lang.math.RandomUtils;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Created by zhouyaohui on 2017/1/5.
 */
public class RandomUtil {

    public static Random SRANDOMINT = new Random();
    public static char[] CHARPOOL = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
    public static char[] NUMPOOL = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};

    /**
     * 随机区间 [min,max]
     * @param min
     * @param max
     * @return
     */
    public static int rand(int min, int max) {
        if (max < min) {
            throw new IllegalArgumentException("max must be bigger than min.");
        }
        if (min < 0 || max < 0) {
            throw new IllegalArgumentException("min and max must > 0.");
        }
        int sub = max - min;
        return new Random().nextInt(sub + 1) + min;
    }

    /**
     * (min, max] 区间随机
     * @param min
     * @param max
     * @return
     */
    public static int randLeft(int min, int max) {
        return rand(min + 1, max);
    }

    /**
     * [min, max) 区间随机
     * @param min
     * @param max
     * @return
     */
    public static int randRight(int min, int max) {
        return rand(min, max - 1);
    }

    /**
     * (min, max) 区间随机
     * @param min
     * @param max
     * @return
     */
    public static int randLeftRight(int min, int max) {
        return rand(min + 1, max - 1);
    }

    /**
     * 按权随机
     * @param c
     * @param powerField
     * @param count
     * @param repeat
     * @param <T>
     * @return
     */
    public static <T> List<T> powerRandom(Collection<T> c, String powerField, int count, boolean repeat) {
        List<T> source = new ArrayList<>();
        source.addAll(c);
        if (source.size() == 0) {
            throw new IllegalArgumentException("collection can not be null");
        }
        List<T> list = new ArrayList<>();
        try {
            Field field = source.get(0).getClass().getDeclaredField(powerField);
            field.setAccessible(true);
            if (source.size() <= count && repeat == false) {   // 要随机的数量大于列表数量
                list.addAll(source);
                return list;
            }
            for (int i = 0; i < count; i++) {
                T random = null;
                int power = 0;
                for (T obj : source) {
                    power += (Integer) field.get(obj);
                }
                int randInt = rand(1, power);
                Iterator<T> iter = source.iterator();
                while (iter.hasNext()) {
                    T temp = iter.next();
                    randInt -= (Integer) field.get(temp);
                    if (randInt <= 0) {
                        random = temp;
                        break;
                    }
                }
                list.add(random);
                if (repeat == false) {
                    source.remove(random);
                }
            }
        } catch (Exception e) {
            LogUtil.error("按权随机失败.", e);
        }
        return list;
    }
    
    /**
     * @param c
     * @param count
     * @return 从列表中取非重复的count个元素
     */
    public static <T> List<T> random(Collection<T> c,int count) {
    	List<T> list = new ArrayList<>();
    	if (c.size() <= count) {
			list.addAll(c);
			return list;
		}
    	Object[] source = c.toArray();
    	int delta = c.size() / count;
        int residue = c.size() % count;
        Random r = new Random();
        int index = 0;
        T t;
        for (int i = 0; i < count; i++) {
        	int tmpDelta = delta;
        	if (i < residue) {
        		tmpDelta = tmpDelta+1;
			}
        	t = (T)source[index + r.nextInt(tmpDelta)];
           index = index + tmpDelta;
           list.add(t);
        }
    	return list;
    }
    
    public static <T> List<T> random(Collection<T> c,int count,Collection<T> special) {
    	if (special == null || special.size() <= 0) {
			return random(c, count);
		}
    	List<T>tList = new ArrayList<T>();
    	for (T t : c) {
			if (!special.contains(t)) {
				tList.add(t);
			}
		}
    	return random(tList,count);
    }
    
    /**
     * @param map
     * @return
     * 按照权值随机取值
     */
    public static <T> T powerRandom(Map<T, Integer> map){
    	Iterator<T>it = map.keySet().iterator();
    	int[] disDel;
    	int total = 0;
    	Map<T, int[]>tMap = new HashMap<T, int[]>();
    	while (it.hasNext()) {
    		disDel = new int[2];
			T t = (T) it.next();
			Integer val = map.get(t);
			disDel[0] = total;
			disDel[1] = total + val - 1;
			total = total + val;
			tMap.put(t, disDel);
		}
    	Random r = new Random();
    	int randInt = r.nextInt(total);
    	it = tMap.keySet().iterator();
    	while (it.hasNext()) {
			T t = (T) it.next();
			disDel = tMap.get(t);
			if (randInt >= disDel[0] && randInt <= disDel[1]) {
				return t;
			}
		}
    	return null;
    }

    public static void main(String[] args) {
        int r1 = rand(10,10);
        int r2 = rand(10,20);
        for (int i = 0; i < 100000; i++) {
            int min = RandomUtils.nextInt(1000);
            int max = RandomUtils.nextInt(1000) + min;
            int rand = rand(min, max);
            if (rand < min || rand > max) {
                throw new IllegalStateException();
            }
        }
//        int r3 = rand(10,9);
        List<Test> list = new ArrayList<>();
        list.add(new Test(100));
        list.add(new Test(200));
        list.add(new Test(300));
        list.add(new Test(400));
        List<Test> random = powerRandom(list, "odds", 3, false);
    }

    static class Test {
        private int odds;
        Test(int odds) {
            this.odds = odds;
        }
    }

    public static String getRandomString(int length) {
        char[] output = new char[length];
        for (short i = 0; i < length; i++) {
            output[i] = CHARPOOL[Math.abs(SRANDOMINT.nextInt() % CHARPOOL.length)];
        }
        return String.valueOf(output);
    }
}
