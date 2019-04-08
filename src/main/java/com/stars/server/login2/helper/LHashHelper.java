package com.stars.server.login2.helper;

/**
 * Created by zhaowenshuo on 2016/2/24.
 */
public class LHashHelper {

    public static int sizeOfLoginServer = 3;
    public static int sizeOfDatabase = 3;

    public static int getDbId(String uniqueId) {
        return (Math.abs(uniqueId.hashCode()) % sizeOfDatabase) + 1;
    }

}
