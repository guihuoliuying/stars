package com.stars.server.login.util;

/**
 * Created by zhaowenshuo on 2015/12/24.
 */
public class SimpleStringFormatter {

    private String[] segments;

    public SimpleStringFormatter(String template) {
        reqNotNull(template);
        this.segments = template.split("\\{\\}");
    }

    public String format(Object... args) {
        reqNotNull(args);
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (String seg : segments) {
            sb.append(seg);
            if (i < args.length) {
                reqNotNull(args[i]);
                sb.append(args[i++]);
            } else if (i < segments.length - 1) {
                sb.append("{}");
            }
        }
        return sb.toString();
    }

    private void reqNotNull(Object obj) {
        if (obj == null) {
            throw new NullPointerException();
        }
    }

    public static void main(String[] args) {
        SimpleStringFormatter ssf = new SimpleStringFormatter("{}, fuck {}, XX");
        System.out.println(ssf.format("hell", "you"));
        System.out.println(ssf.format("heaven", "xxxx"));
        System.out.println(ssf.format(1111, 1111L));

        long s = System.currentTimeMillis();
        for (int i = 0; i < 10000000; i++) {
            ssf.format(i, i + 11);
        }
        long e = System.currentTimeMillis();
        System.out.println(e - s);
    }
}