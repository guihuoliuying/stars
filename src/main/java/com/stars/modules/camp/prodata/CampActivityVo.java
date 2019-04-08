package com.stars.modules.camp.prodata;

import com.stars.modules.camp.usrdata.RoleCampPo;
import com.stars.modules.camp.usrdata.RoleCampTimesPo;
import com.stars.multiserver.camp.usrdata.AllServerCampPo;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.DateUtil;
import com.stars.util.StringUtil;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by huwenjun on 2017/6/26.
 */
public class CampActivityVo implements Cloneable, Comparable<CampActivityVo> {
    private Integer id;//唯一
    private String name;//gametext的key
    private String desc;//gametext的key
    private String showaward;//填道具id=数量，表示道具及数量，多个用|隔开
    private Integer times;//0表示不限，大于0表示次数
    private String image;//配置资源路径名称
    private Integer officerlevel;//表示达到该官职等级时开启该活动
    private Integer camplevel;//表示达到该阵营等级时开启该活动
    private Integer open;//1表示显示，0不显示
    private int sort;//排序（用此字段必须使用拷贝对象）
    private String openWindow;//打开窗体
    private String openTime;//开启时间
    /**
     * int[]
     * 1时
     * 2分
     * 3秒
     */
    private Map<int[], int[]> openDateGroup = new LinkedHashMap<>();//开放时间组,

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

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getShowaward() {
        return showaward;
    }

    public void setShowaward(String showaward) {
        this.showaward = showaward;
    }

    public Integer getTimes() {
        return times;
    }

    public void setTimes(Integer times) {
        this.times = times;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Integer getOfficerlevel() {
        return officerlevel;
    }

    public void setOfficerlevel(Integer officerlevel) {
        this.officerlevel = officerlevel;
    }

    public Integer getCamplevel() {
        return camplevel;
    }

    public void setCamplevel(Integer camplevel) {
        this.camplevel = camplevel;
    }

    public String getOpenWindow() {
        return openWindow;
    }

    public void setOpenWindow(String openWindow) {
        this.openWindow = openWindow;
    }

    public Integer getOpen() {
        return open;
    }

    public void setOpen(Integer open) {
        this.open = open;
    }

    public boolean isOpen() {
        return open == 1;
    }

    public void writeBuff(NewByteBuffer buff) {
        buff.writeInt(id);
        buff.writeString(name);
        buff.writeString(desc);
        buff.writeString(showaward);
        buff.writeInt(times);
        buff.writeString(image);
        buff.writeInt(officerlevel);
        buff.writeInt(camplevel);
        buff.writeString(openWindow);
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    /**
     * 可以参与本活动
     *
     * @param roleCampPo
     * @return
     */
    public boolean canJoin(RoleCampPo roleCampPo, AllServerCampPo allServerCampPo) {
        return this.camplevel <= allServerCampPo.getLevel() && this.officerlevel <= roleCampPo.getCommonOfficer().getLevel();
    }

    public boolean canJoinTime(RoleCampTimesPo roleCampTimes) {
        return (this.getTimes() != 0 && roleCampTimes.getJoinTimesByActId(this.getId()) <= this.getTimes()) || this.getTimes() == 0;
    }

    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

    @Override
    public int compareTo(CampActivityVo o) {
        int diff = o.getSort() - this.getSort();
        if (diff == 0) {
            diff = this.getId() - o.getId();
        }
        return diff;
    }

    public String getOpenTime() {
        return openTime;
    }

    public void setOpenTime(String openTime) throws Exception {
        this.openTime = openTime;
        if (!openTime.trim().equals("0")) {
            LinkedHashMap<String, String> dateLinkedHashMap = StringUtil.toLinkedHashMap(openTime, String.class, String.class, ';', '|');
            for (Map.Entry<String, String> entry : dateLinkedHashMap.entrySet()) {
                String beginDateStr = entry.getKey();
                String endDateStr = entry.getValue();
                int[] beginTime = StringUtil.toArray(beginDateStr, int[].class, ':');
                int[] endTime = StringUtil.toArray(endDateStr, int[].class, ':');
                openDateGroup.put(beginTime, endTime);
            }
        }
    }

    /**
     * 检测是否为开放时间
     *
     * @return
     */
    public boolean checkOpenTime() {
        if (openDateGroup.size() == 0) {
            return true;
        }
        Date now = new Date();
        for (Map.Entry<int[], int[]> entry : openDateGroup.entrySet()) {
            int[] beginGroup = entry.getKey();
            Date beginDate = new Date();
            beginDate.setHours(beginGroup[0]);
            beginDate.setMinutes(beginGroup[1]);
            beginDate.setSeconds(beginGroup[2]);
            int[] endGroup = entry.getValue();
            Date endDate = new Date();
            endDate.setHours(endGroup[0]);
            endDate.setMinutes(endGroup[1]);
            endDate.setSeconds(endGroup[2]);
            if (DateUtil.isBetween(now, beginDate, endDate)) {
                return true;
            }
        }
        return false;
    }

}
