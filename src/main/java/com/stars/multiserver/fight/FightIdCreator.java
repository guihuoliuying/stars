package com.stars.multiserver.fight;

import java.util.UUID;

public class FightIdCreator {
    /**
     * 保证全局唯一
     *
     * @return
     */
    public static synchronized String creatUUId() {
        return UUID.randomUUID().toString();
    }

}
