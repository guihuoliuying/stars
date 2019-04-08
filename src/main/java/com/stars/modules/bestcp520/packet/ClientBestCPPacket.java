package com.stars.modules.bestcp520.packet;

import com.stars.modules.MConst;
import com.stars.modules.bestcp520.BestCPManager;
import com.stars.modules.bestcp520.BestCPPacketSet;
import com.stars.modules.data.DataManager;
import com.stars.modules.drop.DropManager;
import com.stars.modules.drop.prodata.DropVo;
import com.stars.modules.role.summary.RoleSummaryComponent;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;
import com.stars.services.ServiceHelper;
import com.stars.services.rank.RankConstant;
import com.stars.services.rank.userdata.AbstractRankPo;
import com.stars.services.rank.userdata.BestCPRankPo;
import com.stars.services.rank.userdata.BestCPVoterRankPo;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by huwenjun on 2017/5/20.
 */
public class ClientBestCPPacket extends Packet {
    private byte subType;
    public static final byte CAN_TAKE_REWARD = 1;//已经被领奖的记录
    public static final byte SEND_ACTIVITY_UI = 2;//下发活动界面文本
    public static final byte BEST_CP_RANK = 3;//最佳组合排行榜
    public static final byte BEST_CP_VOTER_RANK = 4;//最佳组合的个人投票排行榜
    private Integer voteSum;
    private String ruleDesc = BestCPManager.ruleDesc;
    private String timeDesc = BestCPManager.timeDesc;
    private List<AbstractRankPo> bestCPRankList;
    private List<AbstractRankPo> bestCPRankVoterList;
    private Set<Integer> takedGroup;
    private Map<Integer, Integer> myCPRank;

    public ClientBestCPPacket() {

    }


    public ClientBestCPPacket(byte subType) {
        this.subType = subType;
    }

    @Override
    public short getType() {
        return BestCPPacketSet.C_BEST_CP;
    }

    @Override
    public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeByte(subType);
        switch (subType) {
            case CAN_TAKE_REWARD: {
                buff.writeInt(voteSum);
                buff.writeInt(takedGroup.size());//已经被领取的宝箱投票数
                for (Integer box : takedGroup) {
                    buff.writeInt(box);
                }
            }
            break;
            case SEND_ACTIVITY_UI: {
                buff.writeString(ruleDesc);//规则
                buff.writeString(timeDesc);//活动时间
                buff.writeInt(DataManager.getCommConfig("bestcp_ticketitem", 0));//投票消耗物品itemid
                buff.writeString(DataManager.getCommConfig("bestcp_personalreward"));//奖励组
            }
            break;
            case BEST_CP_RANK: {
                writeBestCPRank(buff);
            }
            break;
            case BEST_CP_VOTER_RANK: {
                writeBestCPVoterRank(buff);
            }
            break;
        }
    }

    private void writeBestCPVoterRank(com.stars.network.server.buffer.NewByteBuffer buff) {
        if (bestCPRankVoterList.size() > 0) {
            buff.writeInt(((BestCPVoterRankPo) bestCPRankVoterList.get(0)).getCpId());
        } else {
            buff.writeInt(-1);
        }
        int myrank = -1;
        buff.writeInt(bestCPRankVoterList.size());
        for (int i = 1; i <= bestCPRankVoterList.size(); i++) {
            BestCPVoterRankPo bestCPVoterRankPo = (BestCPVoterRankPo) bestCPRankVoterList.get(i - 1);
            RoleSummaryComponent roleSummaryComponent = (RoleSummaryComponent) ServiceHelper.summaryService().getSummaryComponent(bestCPVoterRankPo.getRoleId(), MConst.Role);
            if (getRoleId() == bestCPVoterRankPo.getRoleId() && i != bestCPRankVoterList.size()) {
                myrank = i;
            }
            buff.writeString(bestCPVoterRankPo.getRoleId() + "");
            buff.writeString(roleSummaryComponent.getRoleName());
            buff.writeInt(bestCPVoterRankPo.getVoteSum());
            int rank = 0;
            /**
             * 最后一名为自己
             */
            if (getRoleId() == bestCPVoterRankPo.getRoleId() && i == bestCPRankVoterList.size()) {
                rank = myrank;
            } else {
                rank = i;
            }
            if (rank == -1) {
                rank = bestCPVoterRankPo.getRank();
            }
            Integer dropId = BestCPManager.rankRewardMap.get(bestCPVoterRankPo.getCpId()).get(rank);
            String showItem = null;
            if (dropId != null) {
                DropVo dropVo = DropManager.getDropVo(dropId);
                showItem = dropVo.getShowItem();
            } else {
                rank = 999;
                showItem = "";
            }
            buff.writeString(showItem);
            buff.writeInt(rank);
        }


    }

    private void writeBestCPRank(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeInt(bestCPRankList.size());
        for (int rank = 1; rank <= bestCPRankList.size(); rank++) {
            BestCPRankPo bestCPRankPo = (BestCPRankPo) bestCPRankList.get(rank - 1);
            buff.writeInt(rank);
            buff.writeInt(bestCPRankPo.getCpId());
            buff.writeString(DataManager.getGametext(bestCPRankPo.getBestCP().getCpName()));
            buff.writeInt(bestCPRankPo.getVoteSum());
            buff.writeString(bestCPRankPo.getBestCP().getDesc());
            buff.writeString(bestCPRankPo.getBestCP().getCpRole());

            BestCPVoterRankPo bestCPVoterRankPo = (BestCPVoterRankPo) ServiceHelper.rankService().getRank(RankConstant.RANKID_BEST_CP_VOTER, getRoleId(), bestCPRankPo.getCpId());
            int myVoteSum;
            int myRank;
            if (bestCPVoterRankPo == null) {
                myVoteSum = 0;
                myRank = -1;
            } else {
                /**
                 * 为了避免在重排序前出现个别排名异常，此处不选择读取排行榜成员的排行，
                 * 缓存排名，出现异常排名则读取缓存
                 */
                myVoteSum = bestCPVoterRankPo.getVoteSum();
                myRank = bestCPVoterRankPo.getRank();
                if (myRank == 0) {
                    if (myCPRank.containsKey(bestCPRankPo.getCpId())) {
                        myRank = myCPRank.get(bestCPRankPo.getCpId());
                    }
                } else {
                    myCPRank.put(bestCPRankPo.getCpId(), myRank);
                }
            }
            buff.writeInt(myVoteSum);
            buff.writeInt(myRank);
            buff.writeString(bestCPRankPo.getBestCP().getIcon());//npc头像
        }
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {

    }


    @Override
    public void execPacket() {

    }


    public void setBestCPRankList(List<AbstractRankPo> bestCPRankList) {
        this.bestCPRankList = bestCPRankList;
    }

    public List<AbstractRankPo> getBestCPRankList() {
        return bestCPRankList;
    }

    public void setBestCPRankVoterList(List<AbstractRankPo> bestCPRankVoterList) {
        this.bestCPRankVoterList = bestCPRankVoterList;
    }

    public List<AbstractRankPo> getBestCPRankVoterList() {
        return bestCPRankVoterList;
    }

    public String getRuleDesc() {
        return ruleDesc;
    }

    public void setRuleDesc(String ruleDesc) {
        this.ruleDesc = ruleDesc;
    }

    public String getTimeDesc() {
        return timeDesc;
    }

    public void setTimeDesc(String timeDesc) {
        this.timeDesc = timeDesc;
    }

    public Integer getVoteSum() {
        return voteSum;
    }

    public void setVoteSum(Integer voteSum) {
        this.voteSum = voteSum;
    }

    public void setTakedGroup(Set<Integer> takedGroup) {
        this.takedGroup = takedGroup;
    }

    public Set<Integer> getTakedGroup() {
        return takedGroup;
    }

    public Map<Integer, Integer> getMyCPRank() {
        return myCPRank;
    }

    public void setMyCPRank(Map<Integer, Integer> myCPRank) {
        this.myCPRank = myCPRank;
    }

}
