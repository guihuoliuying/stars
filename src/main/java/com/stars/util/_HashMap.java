package com.stars.util;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Created with IntelliJ IDEA.
 * User: yushan
 * Date: 11-11-29
 * Time: 上午10:10
 * To change this template use File | Settings | File Templates.
 */
public class _HashMap extends HashMap {

    public int getInt(String key) {
        return Integer.parseInt(this.get(key).toString());
    }

    public boolean contiansKey(String key){
        if(this.get(key)==null){
            return false;
        }else{
            return true;
        }
    }

    public String getString(String key) {
        key = getKey(key);
        if (this.get(key) != null)
        	return this.get(key).toString();
        else
        	return "";
    }

    public short getShort(String key) {
        key = getKey(key);
        return Short.parseShort(this.get(key).toString());
    }

    public byte getByte(String key) {
        key = getKey(key);
        return Byte.valueOf(this.get(key).toString());
    }

    public double getDouble(String key) {
        key = getKey(key);
        return Integer.parseInt(this.get(key).toString());
    }

    public float getFloat(String key) {
        key = getKey(key);
        return Float.parseFloat(this.get(key).toString());
    }

    public long getLong(String key) {
        key = getKey(key);
        return Long.parseLong(this.get(key).toString());
    }

    public boolean getBoolean(String key) {
        key = getKey(key);
        return Boolean.parseBoolean(this.get(key).toString());
    }

    public String getKey(String key) {
        if (key == null) return "";
        //对key进行检查 防止有人忘记写别名加"."
        //如key=m.dd 查询dd将会把dd转换成m.dd
        if (!this.containsKey(key) && key.indexOf(".") == -1) {
            Iterator iter = this.keySet().iterator();
            String tempKey;
            while (iter.hasNext()) {
                tempKey = iter.next().toString();
                if (tempKey.substring(tempKey.indexOf(".") + 1).equalsIgnoreCase(key.toString())) {
                    key = tempKey;
                }
            }
        }
        return key;
    }
    
    public boolean isNullValue(String key) {
    	key = getKey(key);
    	return this.get(key) == null;
    }
}
