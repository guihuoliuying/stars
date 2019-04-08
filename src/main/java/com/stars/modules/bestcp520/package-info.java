/**
 * Created by huwenjun on 2017/4/25.
 */
package com.stars.modules.bestcp520;
/**
 *上行协议：0x0250+子协议
 *  子协议：
 *      1:领奖
 *      2:最佳组合排行榜
 *      3:最佳组合的个人投票排行榜+(long)cpId
 *      4:投票+(long)cpId
 *下行协议:0x0251+子协议
 *  子协议:
 *      1:能领奖的宝箱个数+(int)宝箱个数
 *      2:下发活动相关数据+(string)规则+(string)活动时间+(int)消耗物品itemid+(string)奖励组
 *      3:最佳组合排行榜+（int）排行榜条数+[{( (int)排名 + (long)组合id + (string)组合名称 +(int)投票数+(string)组合描述
 *      +(string)组合npcid+(int)我的投票数+(int)我的排名【-1表示不再排行榜内】))}]
 *      4:最佳组合的个人投票排行榜+（int）排行榜条数+[{(long)角色id+(string)角色名称+(int)投票数+(long)组合id+(string)奖励showitem}]
 **/