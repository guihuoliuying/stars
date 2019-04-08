package com.stars.modules.luckydraw.prodata;

import com.stars.modules.luckydraw.condition.ILuckyCondition;
import com.stars.modules.luckydraw.condition.LuckyCondition;
import com.stars.modules.luckydraw.condition.LuckyConditionFactory;
import com.stars.modules.luckydraw.userdata.RoleLuckyDrawPo;
import com.stars.modules.luckydraw.userdata.RoleLuckyDrawTimePo;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.LogUtil;
import com.stars.util.StringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by huwenjun on 2017/8/10.
 */
public class LuckyPumpAwardVo implements ILuckyCondition, Comparable<LuckyPumpAwardVo> {
    private int id;//	流水号	填整数，唯一 ，与转盘位置一致
    private String item;//	抽奖物品	itemid+数量
    private int odds;//权值 表示抽中该物品的权值
    /**
     * 特殊条件	类型1：前x次不会抽中该物品，格式：1+次数
     * 类型2：抽x次必定出现该物品，格式：2+次数
     * 类型3：抽中该物品x次后不再抽中该物品，格式：3+次数
     * 3种类型可同时触发，|隔开
     */
    private String condition;
    /**
     * 中奖信息 对应gametext表
     * 抽中该奖励时，会在界面显示该信息，
     * 也会进行跑马灯显示
     * 不配则不显示
     */
    private String desc;
    /**
     * 稀有物品底框 配置图片名称，表示稀有物品的特殊icon显示
     */
    private String rareicon;
    /**
     * 稀有物品特效 配置effectinfo表id，表示该物品上播放的特效
     */
    private String rareeffect;
    private int itemId;
    private int count;//奖励数量
    private int type;//对应活动类型
    private Map<Integer, Integer> itemMap = new HashMap<>();
    private List<LuckyCondition> luckyConditionList = new ArrayList<>();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
        itemMap = StringUtil.toMap(item, Integer.class, Integer.class, '+', '|');
        try {
            int[] group = StringUtil.toArray(item, int[].class, '+');
            itemId = group[0];
            count = group[1];
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
        }
    }

    public int getOdds() {
        return odds;
    }

    public void setOdds(int odds) {
        this.odds = odds;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
        luckyConditionList = LuckyConditionFactory.parseLuckyConditionList(condition);
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getRareicon() {
        return rareicon;
    }

    public void setRareicon(String rareicon) {
        this.rareicon = rareicon;
    }

    public String getRareeffect() {
        return rareeffect;
    }

    public void setRareeffect(String rareeffect) {
        this.rareeffect = rareeffect;
    }

    @Override
    public Boolean canDraw(RoleLuckyDrawPo roleLuckyDrawPo, RoleLuckyDrawTimePo roleLuckyDrawTimePo) {
        boolean canDraw = true;
        for (LuckyCondition luckyCondition : luckyConditionList) {
            Boolean canDrawInner = luckyCondition.canDraw(roleLuckyDrawPo, roleLuckyDrawTimePo);
            if (canDrawInner != null) {
                canDraw &= canDrawInner;
            }
        }
        return canDraw;
    }

    @Override
    public Boolean mustHit(RoleLuckyDrawPo roleLuckyDrawPo, RoleLuckyDrawTimePo roleLuckyDrawTimePo) {
        boolean mustHit = false;
        for (LuckyCondition luckyCondition : luckyConditionList) {
            Boolean mustHitInner = luckyCondition.mustHit(roleLuckyDrawPo, roleLuckyDrawTimePo);
            if (mustHitInner != null) {
                mustHit |= mustHitInner;
            }
        }
        return canDraw(roleLuckyDrawPo, roleLuckyDrawTimePo) && mustHit;
    }

    @Override
    public int compareTo(LuckyPumpAwardVo o) {
        return this.getId() - o.getId();
    }

    public void writeBuff(NewByteBuffer buff) {
        buff.writeInt(id);
        buff.writeInt(itemId);//奖励itemid
        buff.writeInt(count);//奖励数量
        buff.writeString(rareicon);//稀有物品底框
        buff.writeString(rareeffect);//稀有物品特效
    }

    public Map<Integer, Integer> getItemMap() {
        return itemMap;
    }

    public void setItemMap(Map<Integer, Integer> itemMap) {
        this.itemMap = itemMap;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
