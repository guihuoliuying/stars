package com.stars.core;

import java.util.Arrays;

/**
 * Created by zhaowenshuo on 2016/1/8.
 */
public class AccessControl {

    /*
     * 访问控制（可用作动态开启/关闭协议）
     * 使用位图标记数据包是否可用（每一位对应一个数据包）
     */
    public static boolean notUseBitmap; // 不做同步，依赖其他同步机制，最终对所有玩家可见
    public static long[] bitmap; // 不做同步，依赖其他同步机制，最终对所有玩家可见

    static {
        notUseBitmap = true;
        bitmap = new long[512];
        Arrays.fill(bitmap, 0xFFFF_FFFF_FFFF_FFFFL);
    }

    public static boolean canAccess(short type) {
        return notUseBitmap || checkBitmap(type); // 利用短路，减少位运算
    }

    public static boolean checkBitmap(short type) {
        return (bitmap[type >> 6] & (1L << (type & 0x003F))) != 0;
    }

    public static void setBitmap(int type, boolean flag) {
        if (flag) {
            bitmap[index(type)] |= offset(type);
        } else {
            bitmap[index(type)] &= ~offset(type);
        }
    }

    public static void setBitmap(int ltype, int rtype, boolean flag) {
        for (int t = ltype; t <= rtype; t++) {
            setBitmap(t, flag);
        }
    }

    public static void updateBitmap() {
        for (int i = 0; i < bitmap.length; i++) {
            if (bitmap[i] != 0xFFFF_FFFF_FFFF_FFFFL) {
                notUseBitmap = false;
                return;
            }
        }
        notUseBitmap = true;
    }

    private static int index(int type) {
        return type >> 6;
    }

    private static long offset(int type) {
        return (long) 1 << (type & 0x003F);
    }

}
