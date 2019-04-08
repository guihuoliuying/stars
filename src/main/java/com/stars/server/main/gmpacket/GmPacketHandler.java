package com.stars.server.main.gmpacket;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 运维GM接口
 * Created by liuyuheng on 2016/12/10.
 */
public abstract class GmPacketHandler {
    public abstract String handle(HashMap args);

    public static String resultToJson(List list){
        Gson gson = new Gson();
        String result = gson.toJson(list);
        return result;
    }

    public static String resultToJson(Map<String,Object> map){
        List<Map> list = new ArrayList<>();
        list.add(map);
        return resultToJson(list);
    }

    public static String resultToJson(int value){
        Map<String,Object> map = new HashMap<>();
        map.put("value",value);
        return resultToJson(map);
    }

    public static String resultToJson(String value) {
        if(value == null || value.equals("")){
            List<Map> list = new ArrayList<>();
            return resultToJson(list);
        }

        Map<String,Object> map = new HashMap<>();
        map.put("value", value);
        return resultToJson(map);
    }
}
