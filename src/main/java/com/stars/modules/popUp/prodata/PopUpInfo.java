package com.stars.modules.popUp.prodata;

import com.stars.core.module.Module;
import com.stars.modules.push.conditionparser.PushCondLexer;
import com.stars.modules.push.conditionparser.PushCondParser;
import com.stars.modules.push.conditionparser.node.PushCondNode;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.DateUtil;
import com.stars.util.StringUtil;

import java.util.List;
import java.util.Map;

/**
 * Created by wuyuxing on 2017/3/27.
 */
public class PopUpInfo implements Comparable<PopUpInfo> {
    private int popUpId;
    private byte type;
    private String param;
    private int group;
    private int rank;
    private String triger;
    private String condition;
    private byte frequency;
    private String date;

    private long beginTime;
    private long endTime;
    private boolean loginCheck;
    private String sysName;
    private PushCondNode node;

    public int getPopUpId() {
        return popUpId;
    }

    public void setPopUpId(int popUpId) {
        this.popUpId = popUpId;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public int getGroup() {
        return group;
    }

    public void setGroup(int group) {
        this.group = group;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
        if(StringUtil.isEmpty(date) || date.equals("0")) return;
        String[] array = date.split(",");
        if(array == null || array.length < 2) return;
        this.beginTime = DateUtil.parseDateTime(array[0]);
        this.endTime = DateUtil.parseDateTime(array[1]);
    }

    public String getTriger() {
        return triger;
    }

    public void setTriger(String triger) {
        this.triger = triger;
        String[] arr = triger.split("\\|");
        if(arr == null||arr.length <= 0) return;
        for(String str : arr){
            if(str.equals("login")){
                this.loginCheck = true;
            }else if(str.contains("sysopen")){
                String[] array = str.split("\\+");
                if(array == null || array.length < 2) return;
                this.sysName = array[1];
            }
        }
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
        if(StringUtil.isEmpty(condition) || condition.equals("0")) return;
        this.node = new PushCondParser(new PushCondLexer(condition)).parse();
    }

    public byte getFrequency() {
        return frequency;
    }

    public void setFrequency(byte frequency) {
        this.frequency = frequency;
    }

    public boolean isLoginCheck() {
        return loginCheck;
    }

    public boolean matchSysName(List<String> sysNameList){
        if(StringUtil.isEmpty(this.sysName) || sysNameList == null) return false;
        return sysNameList.contains(sysName);
    }

    public boolean matchTimes(long now){
        return (beginTime == 0 || beginTime <= now) && (endTime == 0 || endTime >= now);
    }

    public boolean checkCondition(Map<String, Module> moduleMap){
        if(node == null) return true;
        return (long)node.eval(moduleMap) > 0;
    }

    @Override
    public int compareTo(PopUpInfo info) {
        if(this.group != info.getGroup()){
            return info.getGroup() - this.group;
        }
        if(this.rank != info.getRank()){
            return info.getRank() - this.rank;
        }
        if(this.popUpId != info.getPopUpId()){
            return info.getPopUpId() - this.popUpId;
        }
        return 1;
    }

    public void writeToBuff(NewByteBuffer buff){
        buff.writeInt(popUpId);
        buff.writeByte(type);
        buff.writeString(param);
        buff.writeByte(frequency);
    }

    @Override
    public String toString() {
        return Integer.toString(popUpId);
    }
}
