package com.stars.modules.oldplayerback.pojo;

/**
 * Created by huwenjun on 2017/7/14.
 */
public class RewardPosition {
    private int position;
    private int groupId;
    private int isRare;
    private String image;

    private RewardPosition(int position, int groupId, int isRare, String image) {
        this.position = position;
        this.groupId = groupId;
        this.isRare = isRare;
        this.image = image;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public int getIsRare() {
        return isRare;
    }

    public void setIsRare(int isRare) {
        this.isRare = isRare;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public static RewardPosition parse(String params) {
        String[] param = params.split("\\+");
        return new RewardPosition(Integer.parseInt(param[0]), Integer.parseInt(param[1]), Integer.parseInt(param[2]), param[3]);
    }
}
