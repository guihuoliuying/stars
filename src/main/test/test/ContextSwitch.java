package test;

import java.util.Random;

/**
 * Created by zd on 2015/10/21.
 */
public class ContextSwitch {

    public static int a;

    public synchronized static int getA() {
        return a;
    }

    public synchronized static void setA(int a) {
        ContextSwitch.a = a;
    }

    public static void main(String[] args) {
        int n = args.length == 0 ? 800 : Integer.parseInt(args[0]);
        while (n > 0) {
            n--;
            new Thread() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            Thread.sleep(new Random().nextInt(10));
                            setA(getA() + 1);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }.start();
        }
    }

}
