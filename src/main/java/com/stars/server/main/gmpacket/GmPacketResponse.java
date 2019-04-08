package com.stars.server.main.gmpacket;

/**
 * Created by liuyuheng on 2016/12/10.
 */
public class GmPacketResponse {
    private int status;// 状态;0表示成功，1表示网络超时
    private int count;// 总记录条数
    private String result;// 详细数据列表

    public static int SUC = 0;// 成功
    public static int TIMEOUT = 1;// 网络超时

    public GmPacketResponse(int status, int count, String result) {
        this.status = status;
        this.count = count;
        this.result = result;
    }

    @Override
    public String toString() {
//        格式:{"status":0,"count":1,"result":[{"value":0}]}
        StringBuilder builder = new StringBuilder("");
        builder.append("{")
                .append("\"status\":")
                .append(status)
                .append(",")
                .append("\"count\":")
                .append(count)
                .append(",")
                .append("\"result\":")
                .append(result)
                .append("}");
        return builder.toString();
    }

    public int getStatus() {
        return status;
    }

    public int getCount() {
        return count;
    }

    public String getResult() {
        return result;
    }
}
