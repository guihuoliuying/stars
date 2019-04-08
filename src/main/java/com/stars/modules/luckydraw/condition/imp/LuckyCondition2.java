package com.stars.modules.luckydraw.condition.imp;

import com.stars.modules.luckydraw.condition.LuckyCondition;
import com.stars.modules.luckydraw.userdata.RoleLuckyDrawPo;
import com.stars.modules.luckydraw.userdata.RoleLuckyDrawTimePo;

/**
 * Created by huwenjun on 2017/8/10.
 */
public class LuckyCondition2 extends LuckyCondition {

    public LuckyCondition2(int time) {
        super(time);
    }

    @Override
    public Boolean canDraw(RoleLuckyDrawPo roleLuckyDrawPo, RoleLuckyDrawTimePo roleLuckyDrawTimePo) {
        return null;
    }

    @Override
    public Boolean mustHit(RoleLuckyDrawPo roleLuckyDrawPo, RoleLuckyDrawTimePo roleLuckyDrawTimePo) {
        if (roleLuckyDrawPo.getNotHitTime()+1 >= getTime()) {
            return true;
        }
        return false;
    }
}
