package com.stars.modules.camp.prodata;

import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by huwenjun on 2017/6/26.
 * 阵营主表
 */
public class CampAtrVo {
    private Integer type;//阵营类型
    private String name;//阵营名称
    private String desc;//阵营描述
    private String image;//阵营图片

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void writeBuff(NewByteBuffer buffer) {
        buffer.writeInt(type);
        buffer.writeString(name);
        buffer.writeString(desc);
        buffer.writeString(image);
    }
}
