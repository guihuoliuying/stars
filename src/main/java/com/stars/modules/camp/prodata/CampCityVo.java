package com.stars.modules.camp.prodata;

import com.stars.modules.camp.CampManager;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.StringUtil;

import java.util.Map;

/**
 * Created by huwenjun on 2017/6/26.
 * 阵营城市配置
 */
public class CampCityVo implements Comparable<CampCityVo> {
    private Integer id;//填数值，流水号
    private String name;//gametext的key
    private Integer level;//填数值，标示该城市属于那个城市等级
    private Integer camptype;//填整数，表示阵营，1表示齐国，2表示楚国
    private String place;//填数值，与势力图中城市位置对应
    private Integer condition;//数值，表示阵营等级达到该值时开启该城市
    private String image;//填图片路径名称，表示该城市在势力图上显示的外观
    private String officerlv;//格式：官职min,官职max。表示在该官职范围内的玩家才能入驻该城市
    private String rareofficer;//格式：稀有官职id+数量|稀有官职id+数量…
    //    private Integer rareOfficerNum;//稀有官职数量
    private Map<Integer, Integer> rareOfficerMap;

    private int officerLvMin;//官职等级限制min
    private int officerLvMax;//官职等级限制max

    public void writeBuff(NewByteBuffer buffer) {
        buffer.writeInt(id);
        buffer.writeString(name);
        buffer.writeInt(level);
        buffer.writeInt(camptype);
        buffer.writeString(place);
        buffer.writeInt(condition);
        buffer.writeString(image);
        buffer.writeString(officerlv);
        buffer.writeString(rareofficer);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Integer getCamptype() {
        return camptype;
    }

    public void setCamptype(Integer camptype) {
        this.camptype = camptype;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public Integer getCondition() {
        return condition;
    }

    public void setCondition(Integer condition) {
        this.condition = condition;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getOfficerlv() {
        return officerlv;
    }

    public void setOfficerlv(String officerlv) {
        this.officerlv = officerlv;
        Integer[] group = null;
        try {
            group = StringUtil.toArray(officerlv, Integer[].class, ',');
        } catch (Exception e) {
            e.printStackTrace();
        }
        officerLvMin = group[0];
        officerLvMax = group[1];
    }

    public String getRareofficer() {
        return rareofficer;
    }

    public void setRareofficer(String rareofficer) {
        this.rareofficer = rareofficer;
        rareOfficerMap = StringUtil.toMap(rareofficer, Integer.class, Integer.class, '+', '|');
    }

    @Override
    public int compareTo(CampCityVo o) {
        return this.getLevel() - o.getLevel();
    }

    public Map<Integer, Integer> getRareOfficerMap() {
        return rareOfficerMap;
    }

    public boolean canJoin(Integer level) {
        return level >= officerLvMin && level <= officerLvMax;
    }

    public CampCityVo getNextLevelCity() {
        Map<Integer, CampCityVo> lvCityMap = CampManager.campCityLvListMap.get(camptype);
        return lvCityMap.get(level + 1);
    }

    public int getRareOfficerNum() {
        int count = 0;
        for (Integer num : rareOfficerMap.values()) {
            count += num;
        }
        return count;
    }
}
