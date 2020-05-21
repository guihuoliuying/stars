package com.stars.core.gmpacket.tester;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.stars.core.gmpacket.GmPacketDefine;
import com.stars.server.main.gmpacket.GmPacketRequest;
import com.stars.util.LogUtil;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/1/10.
 */
public class GmTester {

    private static Map<Integer, String> jsonMap = new HashMap<>();

    public static void init() {
        LogUtil.init();
        GmPacketDefine.reg();
    }

    public static void test(String json, String... params) {

        if (params != null) {
            for (String param : params) {
                json = json.replaceFirst("\\{\\}", param);
            }
        }
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(
                        new TypeToken<HashMap<String, Object>>() {}.getType(),
                        new JsonDeserializer<HashMap<String, Object>>() {
                            @Override
                            public HashMap<String, Object> deserialize(JsonElement jelem, Type type, JsonDeserializationContext ctx) throws JsonParseException {
                                HashMap<String, Object> map = new HashMap<>();
                                JsonObject jobject = jelem.getAsJsonObject();
                                for (Map.Entry<String, JsonElement> entry : jobject.entrySet()) {
                                    if (entry.getValue().isJsonPrimitive()) {
                                        map.put(entry.getKey(), entry.getValue().getAsString());
                                    } else if (entry.getValue().isJsonObject()) {
                                        map.put(entry.getKey(), deserialize(entry.getValue(), type, ctx));
                                    } else if (entry.getValue().isJsonArray()) {
                                        map.put(entry.getKey(), deserializeArray(entry.getValue(), type, ctx));
                                    }
                                }
                                return map;
                            }
                            private List<Object> deserializeArray(JsonElement jelem, Type type, JsonDeserializationContext ctx) {
                                List<Object> list = new ArrayList<Object>();
                                for (JsonElement jitem : jelem.getAsJsonArray()) {
                                    if (jitem.isJsonPrimitive()) {
                                        list.add(jitem.getAsString());
                                    } else if (jitem.isJsonObject()) {
                                        list.add(deserialize(jitem, type, ctx));
                                    } else if (jitem.isJsonArray()) {
                                        list.add(deserializeArray(jitem, type, ctx));
                                    }
                                }
                                return list;
                            }
                        }).create();
        GmPacketRequest req = gson.fromJson(json, GmPacketRequest.class);
        if (req == null) {
            throw new RuntimeException("Gm请求为空");
        }
        System.out.println("Gm响应: " + req.execute());
    }

    public static void test(int index, String... params) {
        String json = jsonMap.get(index);
        if (json == null) {
            throw new RuntimeException("不存在对应index: " + index);
        }
        test(json, params);
    }

    static {
        jsonMap.put(1, "{\"opType\":20016,\"sign\":md5(publicKey+args),\"args\":{\"roleId\":4194572,\"title\":\"mail_title\",\"content\":\"mail_content\",\"itemDict\":[{\"type\":1,\"code\":1,\"amount\":100},{\"type\":1,\"code\":2,\"amount\":123}],\"coolTime\":321,\"expireTime\":654}}");
        jsonMap.put(2, "{\"opType\":20021,\"sign\":md5(publicKey+args),\"args\":{\"roleId\":4194572,\"serverId\":123}}");
        jsonMap.put(3, "{\"opType\":20022,\"sign\":md5(publicKey+args),\"args\":{\"roleId\":4194572,\"mailId\":[{}]}}");
        jsonMap.put(4, "{\"opType\":20025,\"sign\":md5(publicKey+args),\"args\":{\"roleId\":4194364,\"serverId\":1,\"expiresTime\":{},\"blockReason\":\"用外挂\"}}");
        jsonMap.put(5, "{\"opType\":20026,\"sign\":md5(publicKey+args),\"args\":{\"roleId\":4194364,\"serverId\":1}}");
        jsonMap.put(6, "{\"opType\":20023,\"sign\":md5(publicKey+args),\"args\":{\"serverId\":1,\"blockAccount\":{},\"expiresTime\":{},\"blockReason\":{}}}");
        jsonMap.put(7, "{\"opType\":20024,\"sign\":md5(publicKey+args),\"args\":{\"serverId\":1,\"blockAccount\":{}}}");
        jsonMap.put(8, "{\"opType\":20008,\"sign\":md5(publicKey+args),\"args\":{\"title\":{},\"content\":{},\"startTime\":{},\"endTime\":{},\"cycleInterval\":1,\"priority\":{}}}");
        jsonMap.put(9, "{\"opType\":20009,\"sign\":md5(publicKey+args),\"args\":{\"serverId\":{}}}");
        jsonMap.put(10, "{\"opType\":20010,\"sign\":md5(publicKey+args),\"args\":{\"serverId\":{},\"noticeId\":{}}}");
        /* 更新自定义GM */
        jsonMap.put(11, "{\"opType\":9998,\"args\":{\"value\":0},\"argList\":{}}");
        /* 执行自定义GM */
        jsonMap.put(12, "{\"opType\":20020,\"args\":{\"command\":\"bindRole#zws5#4194472\"},\"argList\":{}}");
        jsonMap.put(13, "{\"opType\":20020,\"args\":{\"command\":\"bindRole2#4194472#4194776\"},\"argList\":{}}");
        /* 踢人下线 */
        jsonMap.put(14, "{\"opType\":1002,\"args\":{\"value\":\"all\"},\"argList\":{}}");
    }

    public static void main(String[] args) {
        test(3, "13");
    }

}
