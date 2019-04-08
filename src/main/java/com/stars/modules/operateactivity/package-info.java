/**
 * Created by huwenjun on 2017/5/26.
 */
package com.stars.modules.operateactivity;
/**
 * 请注意：4=创角时间,格式为4|x+y,表示创角x~y天开放此活动意味着此活动是永久开启的，只和角色创建角色时间有关系。故在选择时间4类型时，请直接拿产品数据即可，无需走活动控制，
 * 务必不要使用getCurActivityId(int activityType)方法，否则同一时间type 4类型如果配置多个id的活动将会只能拿到一个，可调用以下方法List<OperateActVo> getOperateActVoListByType(int type)，
 * 在各自活动模块重写public int getCurShowActivityId()方法判断创建角色时间 选择合适的活动id即可。
 **/