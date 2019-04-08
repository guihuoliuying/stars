package com.stars.modules.escort.userdata.vo;

/**
 * Created by wuyuxing on 2016/12/19.
 */
public class CargoRecord {
    private int cargoId;
    private byte hasUsed;

    public CargoRecord(int cargoId) {
        this.cargoId = cargoId;
        this.hasUsed = 0;
    }

    public CargoRecord(int cargoId, byte hasUsed) {
        this.cargoId = cargoId;
        this.hasUsed = hasUsed;
    }

    public int getCargoId() {
        return cargoId;
    }

    public void setCargoId(int cargoId) {
        this.cargoId = cargoId;
    }

    public byte getHasUsed() {
        return hasUsed;
    }

    public void setHasUsed(byte hasUsed) {
        this.hasUsed = hasUsed;
    }
}
