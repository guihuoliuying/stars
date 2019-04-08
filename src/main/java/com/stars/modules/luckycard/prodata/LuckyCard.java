package com.stars.modules.luckycard.prodata;

import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.StringUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by huwenjun on 2017/9/27.
 */
public class LuckyCard implements Comparable<LuckyCard> {


    private int id;
    private int type;// '1表示稀有库2表示普通库\r\n',
    private String item;//'表示可选的道具及数量 格式：itemid+num',
    private int odds;//'表示对应物品初始抽中权值',
    private int order;// '排序优先级，大的排前',
    private int fullget;// '针对某个稀有道具抽奖次数达到对应次数，下次必定抽中该物品，当抽中该道具时，重置次数',
    private String getitem;// '在暂存箱中分解该道具可获得的道具\r\n格式：itemid+num\r\n',
    private String message;// '当配置了该字段内容时，抽中该道具时

    private Map<Integer, Integer> reward = new HashMap<>();
    private Map<Integer, Integer> resolveReward = new HashMap<>();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
        reward = StringUtil.toMap(item, Integer.class, Integer.class, '+', '|');
    }

    public int getOdds() {
        return odds;
    }

    public void setOdds(int odds) {
        this.odds = odds;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public int getFullget() {
        return fullget;
    }

    public void setFullget(int fullget) {
        this.fullget = fullget;
    }

    public String getGetitem() {
        return getitem;
    }

    public void setGetitem(String getitem) {
        this.getitem = getitem;
        resolveReward = StringUtil.toMap(getitem, Integer.class, Integer.class, '+', '|');
    }

    public Map<Integer, Integer> getResolveReward() {
        return resolveReward;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    @Override
    public int compareTo(LuckyCard o) {
        return o.getOrder() - this.getOrder();
    }

    public Map<Integer, Integer> getReward() {
        return reward;
    }

    public void writeBuff(NewByteBuffer buff) {
        buff.writeInt(id);
        buff.writeInt(type);// '1表示稀有库2表示普通库\r\n',
        buff.writeString(item);//'表示可选的道具及数量 格式：itemid+num',
        buff.writeInt(odds); //'表示对应物品初始抽中权值',
        buff.writeInt(order);// '排序优先级，大的排前',
        buff.writeInt(fullget);// '针对某个稀有道具抽奖次数达到对应次数，下次必定抽中该物品，当抽中该道具时，重置次数',
        buff.writeString(getitem);// '在暂存箱中分解该道具可获得的道具\r\n格式：itemid+num\r\n',
        buff.writeString(message);// '当配置了该字段内容时，抽中该道具时
    }
}
