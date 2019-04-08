package com.stars.services.rank.userdata;

import com.stars.db.DBUtil;
import com.stars.db.SqlUtil;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.services.rank.RankConstant;
import com.stars.util.LogUtil;

/**
 * Created by liuyuheng on 2016/8/22.
 */
public class RoleRankPo extends AbstractRankPo {
    /* 入库数据 */
    private long roleId;
    private int fightScore;
    private int gamecaveScore;
    private int roleLevel;// 等级
    private int skyTowerLayerSerial;//镇妖塔等级;
    private long accDamage;//家族探宝累积伤害

    /* 内存数据 */
    private  String roleName;// 角色名称
    private int roleJobId;// 职业id
    private String familyName;// 帮派名称

    public RoleRankPo() {
    }

    public RoleRankPo(long roleId) {
        this.roleId = roleId;
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "allrank", "`roleid`=" + roleId);
    }

    @Override
    public String getDeleteSql() {
        return "delete from `allrank` where `roleid`=" + roleId;
    }

    @Override
    public int compareTo(Object o) {
        switch (getRankId()) {
            case RankConstant.RANKID_FIGHTSCORE:
                return compareByFightScore((RoleRankPo) o);
            case RankConstant.RANKID_GAMECAVESCORE:
                return compareByGamecaveScore((RoleRankPo) o);
            case RankConstant.RANKID_SKYTOWERLAYER:
                return compareBySkyTowerLayerSerial((RoleRankPo) o);
            case RankConstant.RANKID_ROLELEVEL:
            case RankConstant.RANKID_TOTAL_ROLELEVEL:
                return compareByRoleLevel((RoleRankPo) o);
            case RankConstant.RANKID_ROLEFAMILYTREASURE:
                return compareByAccDamage((RoleRankPo) o);
            default:
                break;
        }
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RoleRankPo that = (RoleRankPo) o;

        if (roleId != that.roleId) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return (int) (roleId ^ (roleId >>> 32));
    }

    public AbstractRankPo copy() {
        try {
            return (AbstractRankPo) this.clone();
        } catch (CloneNotSupportedException e) {
            LogUtil.error("RoleRankPo克隆失败", e);
        }
        return null;
    }

    private int compareByAccDamage(RoleRankPo compare) {
        // 必须先判断roleId,保证同一角色数据覆盖
        if (roleId == compare.getRoleId()) {
            return 0;
        }
        if (getAccDamage() != compare.getAccDamage()) {
            return getAccDamage() < compare.getAccDamage() ? 1 : -1;
        } else {
            return compareRoleId(compare.getRoleId());
        }
    }

    /**
     * 战力排行榜排序
     *
     * @param compare
     * @return
     */
    private int compareByFightScore(RoleRankPo compare) {
        // 必须先判断roleId,保证同一角色数据覆盖
        if (roleId == compare.getRoleId()) {
            return 0;
        }
        if (getFightScore() != compare.getFightScore()) {
            return getFightScore() < compare.getFightScore() ? 1 : -1;
        } else {
            return compareRoleId(compare.getRoleId());
        }
    }

    /**
     * 洞府游戏积分排行榜排序
     *
     * @param compare
     * @return
     */
    private int compareByGamecaveScore(RoleRankPo compare) {
        // 必须先判断roleId,保证同一角色数据覆盖
        if (roleId == compare.getRoleId()) {
            return 0;
        }
        if (getGamecaveScore() != compare.getGamecaveScore()) {
            return getGamecaveScore() < compare.getGamecaveScore() ? 1 : -1;
        } else if (getRoleLevel() != compare.getRoleLevel()) {
            return getRoleLevel() > compare.getRoleLevel() ? 1 : -1;
        } else {
            return compareRoleId(compare.getRoleId());
        }
    }

    private int compareBySkyTowerLayerSerial(RoleRankPo compare) {
        // 必须先判断roleId,保证同一角色数据覆盖
        if (roleId == compare.getRoleId()) {
            return 0;
        }
        if (getSkyTowerLayerSerial() != compare.getSkyTowerLayerSerial()) {
            return getSkyTowerLayerSerial() < compare.getSkyTowerLayerSerial() ? 1 : -1;
        } else if (getFightScore() != compare.getFightScore()) {
            return getFightScore() < compare.getFightScore() ? 1 : -1;
        } else {
            return compareRoleId(compare.getRoleId());
        }
    }

    /**
     * 角色等级排行榜
     * 等级较大——》战力较大——》roleId较小（先注册）
     *
     * @param compare
     * @return
     */
    private int compareByRoleLevel(RoleRankPo compare) {
        // 必须先判断roleId,保证同一角色数据覆盖
        if (roleId == compare.getRoleId()) {
            return 0;
        }
        if (getRoleLevel() != compare.getRoleLevel()) {
            return getRoleLevel() < compare.getRoleLevel() ? 1 : -1;
        } else if (getFightScore() != compare.getFightScore()) {
            return getFightScore() < compare.getFightScore() ? 1 : -1;
        } else {
            return compareRoleId(compare.getRoleId());
        }
    }

    protected int compareRoleId(long compare) {
        return roleId == compare ? 0 : roleId < compare ? -1 : 1;// roleId小的在前面
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public int getFightScore() {
        return fightScore;
    }

    public int getGamecaveScore() {
        return gamecaveScore;
    }

    public long getAccDamage() {
        return accDamage;
    }

    public void setAccDamage(long accDamage) {
        this.accDamage = accDamage;
    }

    public void setGamecaveScore(int gamecaveScore) {
        this.gamecaveScore = gamecaveScore;
    }

    public void setFightScore(int fightScore) {
        this.fightScore = fightScore;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public int getRoleJobId() {
        return roleJobId;
    }

    public void setRoleJobId(int roleJobId) {
        this.roleJobId = roleJobId;
    }

    public int getRoleLevel() {
        return roleLevel;
    }

    public void setRoleLevel(int roleLevel) {
        this.roleLevel = roleLevel;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public int getSkyTowerLayerSerial() {
        return skyTowerLayerSerial;
    }

    public void setSkyTowerLayerSerial(int skyTowerLayerSerial) {
        this.skyTowerLayerSerial = skyTowerLayerSerial;
    }

    @Override
    public long getUniqueId() {
        return roleId;
    }

    @Override
    public void writeToBuffer(int rankId, NewByteBuffer buff) {
        switch (rankId) {
            case RankConstant.RANKID_FIGHTSCORE:
                buff.writeInt(getRank());
                buff.writeString(String.valueOf(getRoleId()));
                buff.writeString(getRoleName());
                buff.writeInt(getRoleLevel());
                buff.writeInt(getFightScore());
                break;
            case RankConstant.RANKID_GAMECAVESCORE:
                buff.writeInt(getRank());
                buff.writeString(String.valueOf(getRoleId()));
                buff.writeString(getRoleName());
                buff.writeInt(getRoleLevel());
                buff.writeInt(getGamecaveScore());
                break;
            case RankConstant.RANKID_SKYTOWERLAYER:
                buff.writeInt(getRank());
                buff.writeString(String.valueOf(getRoleId()));
                buff.writeString(getRoleName());
                buff.writeInt(getRoleLevel());
                buff.writeInt(getFightScore());
                buff.writeInt(getSkyTowerLayerSerial());
                break;
            case RankConstant.RANKID_ROLELEVEL:
            case RankConstant.RANKID_TOTAL_ROLELEVEL:
                buff.writeInt(getRank());
                buff.writeString(String.valueOf(getRoleId()));
                buff.writeString(getRoleName());
                buff.writeInt(getRoleLevel());
                buff.writeInt(getFightScore());
                break;
            case RankConstant.RANKID_ROLEFAMILYTREASURE:
                buff.writeInt(getRank());
                buff.writeString(String.valueOf(getRoleId()));
                buff.writeString(getRoleName());
                buff.writeInt(getRoleLevel());
                buff.writeInt(getFightScore());
                buff.writeLong(getAccDamage());
                break;
            default:
                break;
        }
    }
}
