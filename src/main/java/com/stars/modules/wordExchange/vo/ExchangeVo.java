package com.stars.modules.wordExchange.vo;

import com.stars.modules.wordExchange.prodata.CollectAwardVo;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.StringUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wuyuxing on 2017/3/14.
 */
public class ExchangeVo implements Comparable<ExchangeVo> {
    private int id;
    private int operateActId;
    private int order;
    private Map<Integer,Integer> reqItemMap;
    private String showName;
    private String showPic;
    private int dropGroup;
    private byte exchangeType;
    private int canExchangeCount;   //今日剩余兑换次数
    private List<Integer> reqItemList;

    public ExchangeVo(CollectAwardVo vo) {
        this.id = vo.getId();
        this.operateActId = vo.getOperateActId();
        this.order = vo.getOrder();
        this.reqItemMap = new HashMap<>(vo.getReqItemMap());
        this.showName = vo.getShowName();
        this.showPic = vo.getShowPic();
        this.dropGroup = vo.getDropGroup();
        this.exchangeType = vo.getResetType();
        this.reqItemList = vo.getReqItemList();
    }

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

    public Map<Integer, Integer> getReqItemMap() {
        return reqItemMap;
    }

    public void setReqItemMap(Map<Integer, Integer> reqItemMap) {
        this.reqItemMap = reqItemMap;
    }

    public String getShowName() {
        return showName;
    }

    public void setShowName(String showName) {
        this.showName = showName;
    }

    public int getDropGroup() {
        return dropGroup;
    }

    public void setDropGroup(int dropGroup) {
        this.dropGroup = dropGroup;
    }

    public int getCanExchangeCount() {
        return canExchangeCount;
    }

    public void setCanExchangeCount(int canExchangeCount) {
        this.canExchangeCount = canExchangeCount;
    }

    public void setShowPic(String showPic) {
        this.showPic = showPic;
    }

    public void setExchangeType(byte exchangeType) {
        this.exchangeType = exchangeType;
    }

    public void writeToBuff(NewByteBuffer buff){
        buff.writeInt(id);
        buff.writeInt(operateActId);
        buff.writeInt(order);
        buff.writeInt(dropGroup);
        buff.writeInt(canExchangeCount);
        buff.writeByte(exchangeType);
        buff.writeString(showName);
        buff.writeString(showPic);
        if(StringUtil.isEmpty(reqItemList)){
            buff.writeInt(0);
        }else{
            buff.writeInt(reqItemList.size());
            for(int code:reqItemList){
                buff.writeInt(code);
                buff.writeInt(reqItemMap.get(code));
            }
        }
    }

    @Override
    public int compareTo(ExchangeVo vo) {
        if(this.getOrder() != vo.getOrder()){
            return this.getOrder() - vo.getOrder();
        }else if(this.getId() != vo.getId()){
            return this.getId() - vo.getId();
        }
        return 1;
    }
}
