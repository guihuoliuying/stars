package test;

import java.util.Random;

/**
 * Created by zws on 2015/11/3.
 */
public class ThreadLocalTest2 {

    public static int[] primitiveInt = new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
    public static Integer[] objectInt = new Integer[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };

    public static Random random = new Random();

    public static int a = 0;
    public static volatile int b = 0;
    public static ThreadLocal<Integer> c = new ThreadLocal<>();

    public static void main(String[] args) {
        long s, e;
        while (true) {

            s = System.nanoTime();
            for (int i = 0; i < Integer.MAX_VALUE; i++) {
                a = primitiveInt[random.nextInt(10)];
            }
            e = System.nanoTime();
            System.out.println("A: elapsed=" + (e - s) / 1000000.0 + "ms, a=" + a);


            s = System.nanoTime();
            for (int i = 0; i < Integer.MAX_VALUE; i++) {
                b = primitiveInt[random.nextInt(10)];
            }
            e = System.nanoTime();
            System.out.println("B: elapsed=" + (e - s) / 1000000.0 + "ms, b=" + b);


            s = System.nanoTime();
            for (int i = 0; i < Integer.MAX_VALUE; i++) {
                c.set(objectInt[random.nextInt(10)]);
            }
            e = System.nanoTime();
            System.out.println("C: elapsed=" + (e - s) / 1000000.0 + "ms, c=" + c.get());
        }
    }

}
