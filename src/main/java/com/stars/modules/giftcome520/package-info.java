/**
 * Created by huwenjun on 2017/4/18.
 */
package com.stars.modules.giftcome520;
/**
 * 上行：0x023E+(byte)子协议
 * 子协议:
 *      1,领奖
 *      2,请求界面信息
 * 下行协议：0x023F+(byte)子协议
 * 子协议：
 *      1，下发界面数据：（String）规则描述+（String）时间描述+（String）提示+（int）掉落组id
 *      +（String）奖励showitem+（byte）按钮类型+(String)按钮文本
 *      2,下发个人活动状态:(byte)按钮类型
 *
 * 按钮类型==检测当前活动对于玩家的进行状态
 * -1:活动未开始
 * 0:奖励已领取
 * 1:奖励未领取
 * 2:活动已结束
 *
 **/