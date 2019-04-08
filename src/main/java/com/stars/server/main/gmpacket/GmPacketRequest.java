package com.stars.server.main.gmpacket;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by liuyuheng on 2016/12/9.
 */
public class GmPacketRequest {
    protected int opType;// 接口号
    protected String sign;// 签名
    protected HashMap<String, Object> args = new HashMap<>();// 参数
    protected HashMap<String,ArrayList<String>> argList = new HashMap<String,ArrayList<String>>();//参数
    protected HashMap<String, Object> result = new HashMap();// 参数

    protected String argsStr;// 上行参数

    /**
     * 执行
     */
    public String execute() {
        if (opType == 0 && args.isEmpty() && !result.isEmpty()) {// 反向返回opType在参数里面
            int type = Integer.parseInt((String) result.get("optype"));
            switch (type) {
                case 6:// 兑换礼包,根据约定转换
                    opType = 20039;
                    break;
            }
        }
        GmPacketHandler handler = GmPacketManager.getHandler(opType);
        if (argList.size() > 0) {
            return handler.handle(argList);
        } else {
            return handler.handle(args.isEmpty() ? result : args);
        }
    }

    @Override
    public String toString() {
//        格式:{"opType":1001,"sign":md5(publicKey+args),"args":{"value":0}}
        StringBuilder builder = new StringBuilder("");
        builder.append("{")
                .append("\"opType\":")
                .append(opType)
                .append(",")
                .append("\"sign\":")
                .append("\"")
                .append(sign)
                .append("\"")
                .append(",")
                .append("\"args\":")
                .append(argsStr)
                .append("}");
        return builder.toString();
    }

    public int getOpType() {
        return opType;
    }

    public void setOpType(int opType) {
        this.opType = opType;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

	public HashMap<String, Object> getArgs() {
		return args;
	}

	public void setArgs(HashMap<String, Object> args) {
		this.args = args;
	}

	public HashMap<String, ArrayList<String>> getArgList() {
		return argList;
	}

	public void setArgList(HashMap<String, ArrayList<String>> argList) {
		this.argList = argList;
	}

    public String getArgsStr() {
        return argsStr;
    }

    public void setArgsStr(String argsStr) {
        this.argsStr = argsStr;
    }
}
