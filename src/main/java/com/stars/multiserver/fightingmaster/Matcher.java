package com.stars.multiserver.fightingmaster;

import com.google.common.base.Preconditions;
import com.stars.util.LogUtil;

import java.util.List;
import java.util.Random;

/**
 * 匹配器，非线程安全
 * Created by zhouyaohui on 2016/11/11.
 */
public final class Matcher<T extends Matchable> {

    private final static int initCapacity = 1024;   // 初始大小
    private final static int growup = 512;  // 动态增长的大小，目前只考虑固定增长

    private int size = 0;
    private int capacity;
    private Matchable[] matchers;

    public Matcher() {
        this(initCapacity);
    }

    public Matcher(int init) {
        matchers = new Matchable[init];
        capacity = init;
    }

    /** 加入一个新匹配者 */
    public void add(T newer) {
        if (size + 1 >= capacity) {
            Matchable[] temp = new Matchable[capacity + growup];
            capacity += growup;
            System.arraycopy(matchers, 0, temp, 0, size);
            matchers = temp;
        }

        if (size == 0) {
            matchers[0] = newer;
            size++;
            return;
        }
        if (newer.compare(matchers[0]) <= 0) {
            freePosition(0);
            matchers[0] = newer;
            size++;
            return;
        }
        if (newer.compare(matchers[size - 1]) >= 0) {
            matchers[size] = newer;
            size++;
            return;
        }
        int index = binarySearch(newer);
        Preconditions.checkState(index != -1);
        if (newer.compare(matchers[index]) > 0) {
            index++;
        }
        freePosition(index);
        matchers[index] = newer;
        size++;
    }

    /** 偏移数组 */
    private void freePosition(int index) {
        System.arraycopy(matchers, index, matchers, index + 1, size - index);
    }

    /** 返回近似值的index */
    private int binarySearch(Matchable matcher) {
        int tempIndex = -1;
        int head = 0;
        int tail = size;
        int binary;
        while (tail > head) {
            binary = (head + tail) / 2;
            tempIndex = binary;
            if (matcher.compare(matchers[binary]) == 0) {
                break;
            } else if (matcher.compare(matchers[binary]) < 0) {
                tail = binary;
            } else {
                head = binary + 1;
            }
        }
        return tempIndex;
    }

    /** 加入一批新匹配者 */
    public void add(List<T> newers) {
        for (T newer : newers) {
            add(newer);
        }
    }

    /** 偏移数组 */
    private void mergePosition(int index) {
        System.arraycopy(matchers, index + 1, matchers, index, size - index);
    }

    /** 移除一个匹配者 */
    public void remove(T older) {
        int index = binarySearch(older);
        if (index == -1) {
            LogUtil.error("matcher size is empty. can not remove someone.");
            return;
        }

        for (int i = index; i < size; i++) {
            if (matchers[i] == older) {
                mergePosition(i);
                size--;
                break;
            }
        }

        for (int i = index - 1; i >= 0; i--) {
            if (matchers[i] == older) {
                mergePosition(i);
                size--;
                break;
            }
        }
    }

    /**
     * 匹配
     * @param reqMatcher 请求匹配者
     * @param adjust 浮动范围
     * @return 匹配到的，可能为null，表示没有符合条件的
     */
    public T match(Matchable reqMatcher, int adjust) {
        return match(reqMatcher, adjust, adjust);
    }

    /**
     * 匹配
     * @param matchable
     * @param floor 浮动下限，正数，表示比matchable 小 floor
     * @param ceil 浮动上限，正数，表示比matchable 大 ceil
     * @return
     */
    public T match(Matchable matchable, int floor, int ceil) {
        int index = binarySearch(matchable);
        if (index == -1) {
            return null;
        }

        int maxIndex = index;
        for (int i = index; i < size; i++) {
            if (matchers[i].compare(matchable) > ceil) {
                /** 超出浮动范围 */
                break;
            }
            if (matchers[i].compare(matchable) >= 0) {
                maxIndex = i;
            }
        }
        int minIndex = index;
        for (int i = index; i >= 0; i--) {
            if (matchable.compare(matchers[i]) > floor) {
                break;
            }
            if (matchable.compare(matchers[i]) >= 0) {
                minIndex = i;
            }
        }

        /** 找不到或者只找到一个符合条件 --》maxIndex==minIndex */
        if (maxIndex == minIndex && Math.abs(matchers[index].compare(matchable)) > Math.max(floor, ceil)) {
            return null;
        }

        Random random = new Random();
        int rInt = random.nextInt(maxIndex - minIndex + 1);
        if (matchers[minIndex + rInt] != matchable) {
            return (T)matchers[minIndex + rInt];
        }
        for (int i = minIndex + rInt + 1; i <= maxIndex; i++) {
            if (matchers[i] != matchable) {
                return (T) matchers[i];
            }
        }
        for (int i = minIndex + rInt - 1; i >= minIndex; i--) {
            if (matchers[i] != matchable) {
                return (T) matchers[i];
            }
        }
        return null;
    }

    public T getMax() {
        return (T) matchers[size - 1];
    }

    public void check() {
        for (int i = 0; i < size - 1; i++) {
            if (matchers[i].compare(matchers[i + 1]) > 0) {
                throw new RuntimeException("不是有序的啊。。。。");
            }
        }
    }

    public static void main(String[] args) {
        Matcher<Test> matchers = new Matcher<>();
        Test test1 = new Test(1);
        Test test2 = new Test(2);
        Test test3 = new Test(3);
        Test test4 = new Test(4);
        Test test5 = new Test(5);
        Test test6 = new Test(6);
        Test test7 = new Test(7);
        Test test41 = new Test(4);
        matchers.add(test1);
        matchers.add(test2);
        matchers.add(test3);
        matchers.add(test4);
        matchers.add(test5);
        matchers.add(test6);
        matchers.add(test7);
        matchers.add(test41);
        Test match = matchers.match(test4, 1);
    }

    static class Test implements Matchable {
        private int score;

        Test(int score) {
            this.score = score;
        }

        public int getScore() {
            return score;
        }

        @Override
        public int compare(Matchable other) {
            Test t = (Test) other;
            return score - t.getScore();
        }
    }
}
