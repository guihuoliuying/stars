package test;

/**
 * Created by zws on 2015/10/22.
 */
public class ContextSwitchNonSync {

    public static void main(String[] args) {
        int n = args.length == 0 ? 800 : Integer.parseInt(args[0]);
        while (n > 0) {
            n--;
            new Thread() {
//                public long a = 0L;
                @Override
                public void run() {
                    while (true) {
                        try {
//                            a++;
                            Thread.sleep(5);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            break;
                        }
                    }
                }
            }.start();
        }
    }

}
