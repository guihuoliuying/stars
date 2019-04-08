package test;

/**
 * Created by zws on 2015/11/3.
 */
public class ThreadLocalTest {
    public static ThreadLocal<Integer> threadLocal = new ThreadLocal<>();

    public static void main(String[] args) {
        int temp = 0;
        long s = System.nanoTime();
        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            for (int j = 0; j < Integer.MAX_VALUE; j++) {
                threadLocal.set(j);
            }
            temp = threadLocal.get();
        }
        long e = System.nanoTime();
        System.out.println("elapsed: " + (e - s) / 1000000.0 + "ms, " + temp);
    }
}
