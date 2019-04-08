package com.stars.modules.newequipment.prodata;


/**
 * Created by zhanghaizhen on 2017/6/8.
 */
public class TokenWashVo {
    private long tokenWashId;
    private byte washType;
    private byte jobId;
    private String equipType;
    private String equipQuality;
    private String equipLevel;
    private long randomRangeId;
    private int weight;
    private byte minEquipType;
    private byte maxEquipType;
    private byte minEquipQuality;
    private byte maxEquipQuality;
    private short minEquipLevel;
    private short maxEquipLevel;


    public long getTokenWashId() {
        return tokenWashId;
    }

    public void setTokenWashId(long tokenWashId) {
        this.tokenWashId = tokenWashId;
    }

    public byte getWashType() {
        return washType;
    }

    public void setWashType(byte washType) {
        this.washType = washType;
    }

    public byte getJobId() {
        return jobId;
    }

    public void setJobId(byte jobId) {
        this.jobId = jobId;
    }

    public String getEquipType() {
        return equipType;
    }

    public void setEquipType(String equipType) {
        this.equipType = equipType;
        String[] array  = equipType.split("\\+");
        minEquipType = Byte.parseByte(array[0]);
        maxEquipType = Byte.parseByte(array[1]);

    }

    public String getEquipQuality() {
        return equipQuality;
    }

    public void setEquipQuality(String equipQuality) {
        this.equipQuality = equipQuality;
        String[] array = equipQuality.split("\\+");
        minEquipQuality = Byte.parseByte(array[0]);
        maxEquipQuality = Byte.parseByte(array[1]);

    }

    public String getEquipLevel() {
        return equipLevel;
    }

    public void setEquipLevel(String equipLevel) {
        this.equipLevel = equipLevel;
        String[] array = equipLevel.split("\\+");
        minEquipLevel = Short.parseShort(array[0]);
        maxEquipLevel = Short.parseShort(array[1]);
    }

    public long getRandomRangeId() {
        return randomRangeId;
    }

    public void setRandomRangeId(long randomRangeId) {
        this.randomRangeId = randomRangeId;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public boolean matchJobId(byte jobId){
        return this.jobId == jobId || this.jobId == (byte)0;
    }

    public boolean matchEquipType(byte equipType){
        return this.minEquipType <= equipType && this.maxEquipType >= equipType;
    }

    public boolean matchEquipLevel(short equipLevel){
        return this.minEquipLevel <= equipLevel && this.maxEquipLevel >= equipLevel;
    }

    public boolean matchEquipQuality(byte equipQuality){
        return this.minEquipQuality <= equipQuality && this.maxEquipQuality >= equipQuality;
    }

    public boolean matchWashType(byte washType){
        return this.washType == washType;
    }
}
