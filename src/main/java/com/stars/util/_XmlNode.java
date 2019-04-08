package com.stars.util;

import java.util.HashMap;

/**
 * xml读取辅助类
 */
public class _XmlNode {

    private String key;
    private HashMap attrMap;
    private Object value;

    public _XmlNode(){

    }
    public _XmlNode(String key, HashMap attrMap, Object value) {
        this.key = key;
        this.attrMap = attrMap;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public HashMap getAttrMap() {
        return attrMap;
    }

    public void setAttrMap(HashMap attrMap) {
        this.attrMap = attrMap;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

}
