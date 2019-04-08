package com.stars.modules.scene.prodata;

import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by daiyaorong on 2016/6/30.
 */
public class Fcd {

    private String parameter;
    private String value;

    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeString(this.parameter);
        buff.writeString(this.value);
    }

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
