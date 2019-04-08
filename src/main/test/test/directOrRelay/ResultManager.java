package test.directOrRelay;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by zd on 2015/10/30.
 */
public class ResultManager {

    private static ResultManager resultManager = new ResultManager();
    private BlockingQueue<Long> times = new LinkedBlockingQueue<>();
    private AtomicLong sendNum = new AtomicLong();
    private TreeMap<Long, Number> responses = new TreeMap<>();

    public static ResultManager resultManager() {
        return resultManager;
    }

    static class Number {

        private int n = 0;

        public Number incr() {
            n++;
            return this;
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
            if (!responses.containsKey(times.peek())) {
                responses.put(times.poll(), new Number().incr());
            } else {
                responses.get(times.poll()).incr();
            }
        }
        StringBuilder sb = new StringBuilder()
                .append(" request ").append(Client.REQUESTS)
                .append(" connection ").append(Client.CONNECTIONS)
                .append(" ioThread ").append(Client.IO_THREADS)
                .append(" flush ").append(send)
                .append(" response ").append(size);
        for (Map.Entry<Long, Number> e : responses.entrySet()) {
            double d = size;
            if (e.getKey() == 101L) {
                sb.append(" >100ms ");
            } else {
                sb.append(" <=").append(e.getKey()).append("ms ");
            }
            sb.append(String.format("%.2f", e.getValue().getN() / d * 100)).append("%");
        }
        System.out.println(sb.toString());
        responses.clear();
    }

    public AtomicLong getSendNum() {
        return sendNum;
    }

}
