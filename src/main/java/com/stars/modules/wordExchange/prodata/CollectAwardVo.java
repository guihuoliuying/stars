package com.stars.modules.wordExchange.prodata;

import com.stars.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by wuyuxing on 2017/3/14.
 */
public class CollectAwardVo {

    public static final byte RESET_TYPE_NONE = 0;       //不重置
    public static final byte RESET_TYPE_DAILY = 1;      //日重置

    private int id;
    private int operateActId;
    private int order;
    private String reqItem;
    private Map<Integer,Integer> reqItemMap;
    private List<Integer> reqItemList;
    private String showName;
    private String showPic;
    private int dropGroup;
    private byte exchangeType;
    private int exchangeCount;
    private byte resetType;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getOperateActId() {
        return operateActId;
    }

    public void setOperateActId(int operateActId) {
        this.operateActId = operateActId;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getReqItem() {
        return reqItem;
    }

    public void setReqItem(String reqItem) {
        this.reqItem = reqItem;
        if(StringUtil.isEmpty(reqItem)) return;
        this.reqItemMap = StringUtil.toMap(reqItem, Integer.class, Integer.class, '=', '|');
        this.reqItemList = new ArrayList<>();
        String[] arr = reqItem.split("\\|");
        String[] array;
        for(String strData:arr){
            array = strData.split("=");
            reqItemList.add(Integer.parseInt(array[0]));
        }
    }

    public String getShowName() {
        return showName;
    }

    public void setShowName(String showName) {
        this.showName = showName;
    }

    public String getShowPic() {
        return showPic;
    }

    public void setShowPic(String showPic) {
        this.showPic = showPic;
    }

    public int getDropGroup() {
        return dropGroup;
    }

    public void setDropGroup(int dropGroup) {
        this.dropGroup = dropGroup;
    }

    public byte getExchangeType() {
        return exchangeType;
    }

    public void setExchangeType(byte exchangeType) {
        this.exchangeType = exchangeType;
    }

    public byte getResetType() {
        return resetType;
    }

    public void setResetType(byte resetType) {
        this.resetType = resetType;
    }

    public int getExchangeCount() {
        return exchangeCount;
    }

    public void setExchangeCount(int exchangeCount) {
        this.exchangeCount = exchangeCount;
    }

    public Map<Integer, Integer> getReqItemMap() {
        return reqItemMap;
    }

    public List<Integer> getReqItemList() {
        return reqItemList;
    }
}
