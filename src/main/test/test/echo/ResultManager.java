package test.echo;

import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by zd on 2015/10/30.
 */
public class ResultManager {

    private static ResultManager resultManager = new ResultManager();
    private BlockingQueue<Long> times = new ArrayBlockingQueue<>(1024 * 1024 * 10);
    private AtomicLong sendNum = new AtomicLong();
    private HashMap<Long, Number> responses = new HashMap<>();

    {
        responses.put(5L, new Number());
        responses.put(100L, new Number());
        responses.put(101L, new Number());
    }

    public static ResultManager resultManager() {
        return resultManager;
    }

    static class Number {

        private int n = 0;

        public void incr() {
            n++;
        }

        public int getN() {
            int v = n;
            n = 0;
            return v;
        }

    }

    public void add(long time) {
        times.add(time);
    }

    public void stat() throws Exception {
        long send = sendNum.getAndSet(0);
        int size = times.size();
        for (int i = 0; i < size; i++) {
            responses.get(times.poll()).incr();
        }
        StringBuilder sb = new StringBuilder()
                .append(" request ").append(SimpleClient.REQUESTS)
                .append(" connection ").append(SimpleClient.CONNECTIONS)
                .append(" ioThread ").append(SimpleClient.IO_THREADS)
                .append(" flush ").append(send)
                .append(" response ").append(size);
        if (size == 0) {
            sb.append(" <=5ms ").append("0%")
                    .append(" <=100ms ").append("0%")
                    .append(" >100ms ").append("0%");
        } else {
            double d = size;
            sb.append(" <=5ms ").append(String.format("%.2f", responses.get(5L).getN() / d * 100)).append("%")
                    .append(" <=100ms ").append(String.format("%.2f", responses.get(100L).getN() / d * 100)).append("%")
                    .append(" >100ms ").append(String.format("%.2f", responses.get(101L).getN() / d * 100)).append("%");
        }
        System.out.println(sb);

    }

    public AtomicLong getSendNum() {
        return sendNum;
    }

}
