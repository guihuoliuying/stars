package com.stars.services.postsync.aoi;

/**
 * Created by zhaowenshuo on 2017/6/23.
 */
public class AoiObject {
    long roleId;
    double x;
    double z;

    AoiObject xPrev;
    AoiObject xNext;

    AoiObject zPrev;
    AoiObject zNext;

    public AoiObject(long roleId, double x, double z) {
        this.roleId = roleId;
        this.x = x;
        this.z = z;
    }

    public long roleId() {
        return roleId;
    }

    public double x() {
        return x;
    }

    public double z() {
        return z;
    }

    @Override
    public String toString() {
        return "(" + roleId + "," + x + "," + z + ")";
    }
}
