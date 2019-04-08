/**
 * Created by huwenjun on 2017/4/12.
 */
package com.stars.modules.welfareaccount;
/**
 * 下行协议
 0x0232+(byte)子协议
 子协议：
 1,是否是福利号:+(byte)<1或0>
 2,虚拟币数量:+(int)虚拟币数量

 上行协议
 0x0231++(byte)子协议
 子协议：
 1，查询是否是福利账号
 2，（内部）查询虚拟币,无警告
 3，（外部）查询虚拟币
 4，虚拟充值+（int）chargeid+（byte）paypoint
 **/
