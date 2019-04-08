package com.stars.services.rank.userdata;

import com.stars.db.DBUtil;
import com.stars.db.SqlUtil;
import com.stars.modules.dragonboat.DragonBoatManager;
import com.stars.modules.dragonboat.prodata.DragonBoatVo;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.LogUtil;

/**
 * Created by huwenjun on 2017/5/10.
 */
public class DragonBoatRankPo extends AbstractRankPo {
    private Long stageTime;//当前轮时间
    private Integer dragonBoatId;
    private Integer speed;
    private Integer upNum;//减速人气
    private Integer downNum;//提速人气

    @Override
    public long getUniqueId() {
        return dragonBoatId;
    }

    @Override
    public void writeToBuffer(int rankId, NewByteBuffer buff) {

    }

    public DragonBoatRankPo() {
    }

    public DragonBoatRankPo(Long stageTime, Integer dragonBoatId, Integer speed, Integer upNum, Integer downNum) {
        this.stageTime = stageTime;
        this.dragonBoatId = dragonBoatId;
        this.speed = speed;
        this.upNum = upNum;
        this.downNum = downNum;
    }

    @Override
    public AbstractRankPo copy() {
        try {
            return (AbstractRankPo) this.clone();
        } catch (CloneNotSupportedException e) {
            LogUtil.error("BestCPVoterRankPo克隆失败", e);
        }
        return null;
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "rankdragonboat", "stageTime=" + stageTime + " and dragonboatid=" + dragonBoatId);
    }

    @Override
    public String getDeleteSql() {
        return SqlUtil.getDeleteSql("rankdragonboat", "stageTime=" + stageTime + " and dragonboatid=" + dragonBoatId);
    }

    @Override
    public int compareTo(Object o) {
        DragonBoatRankPo other = (DragonBoatRankPo) o;
        if (!other.dragonBoatId.equals(this.dragonBoatId)) {
            if (!this.speed.equals(other.speed)) {
                return other.speed - this.speed;
            } else {
                return other.getDragonBoat().getOrder() - getDragonBoat().getOrder();
            }
        }
        return 0;
    }

    public Long getStageTime() {
        return stageTime;
    }

    public void setStageTime(Long stageTime) {
        this.stageTime = stageTime;
    }

    public Integer getDragonBoatId() {
        return dragonBoatId;
    }

    public void setDragonBoatId(Integer dragonBoatId) {
        this.dragonBoatId = dragonBoatId;

    }

    public Integer getSpeed() {
        return speed;
    }

    public void setSpeed(Integer speed) {
        this.speed = speed;
    }

    public Integer getUpNum() {
        return upNum;
    }

    public void setUpNum(Integer upNum) {
        this.upNum = upNum;
    }

    public Integer getDownNum() {
        return downNum;
    }

    public void setDownNum(Integer downNum) {
        this.downNum = downNum;
    }

    public DragonBoatVo getDragonBoat() {
        return DragonBoatManager.dragonBoatMap.get(dragonBoatId);
    }


}
