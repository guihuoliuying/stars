package com.stars.modules.newequipment.prodata;

import com.stars.modules.newequipment.NewEquipmentManager;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.StringUtil;

import java.util.Map;

/**
 * Created by huwenjun on 2017/11/3.
 */
public class NewEquipmentUpgradeVo implements Comparable<NewEquipmentUpgradeVo> {
    private int nowEquipId;// '当前装备id',
    private int laterEquipId;// '升级后装备id',
    private String reqItem;// '升级消耗',
    private int reqLevel;// '升级所需角色等级',
    private Map<Integer, Integer> reqItemMap;//消耗物品

    public int getNowEquipId() {
        return nowEquipId;
    }

    public void setNowEquipId(int nowEquipId) {
        this.nowEquipId = nowEquipId;
    }

    public int getLaterEquipId() {
        return laterEquipId;
    }

    public void setLaterEquipId(int laterEquipId) {
        this.laterEquipId = laterEquipId;
    }

    public String getReqItem() {
        return reqItem;
    }

    public void setReqItem(String reqItem) {
        this.reqItem = reqItem;
        reqItemMap = StringUtil.toMap(reqItem, Integer.class, Integer.class, '+', '&');
    }

    public int getReqLevel() {
        return reqLevel;
    }

    public void setReqLevel(int reqLevel) {
        this.reqLevel = reqLevel;
    }

    public EquipmentVo getNowEquipment() {
        return NewEquipmentManager.getEquipmentVo(nowEquipId);
    }

    public Map<Integer, Integer> getReqItemMap() {
        return reqItemMap;
    }

    @Override
    public int compareTo(NewEquipmentUpgradeVo o) {
        return getNowEquipment().getType() - o.getNowEquipment().getType();
    }

    public void writeBuff(NewByteBuffer buffer) {
        buffer.writeInt(nowEquipId);//当前装备id
        buffer.writeInt(laterEquipId);//升级后的装备id
        buffer.writeString(reqItem);//需要消耗的物品
        buffer.writeInt(reqLevel);//需要操作的等级
    }
}
