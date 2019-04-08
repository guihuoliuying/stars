package com.stars.util.vowriter;

import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.StringUtil;

import java.util.Map;

/**
 * Created by wuyuxing on 2016/11/16.
 */
public class BuffUtil {
    public static void writeIntMapToBuff(NewByteBuffer buff, Map<Integer,Integer> map){
        if(StringUtil.isEmpty(map)){
            buff.writeInt(0);
        }else{
            buff.writeInt(map.size());
            for(Map.Entry<Integer,Integer> entry:map.entrySet()){
                buff.writeInt(entry.getKey());
                buff.writeInt(entry.getValue());
            }
        }
    }
}
