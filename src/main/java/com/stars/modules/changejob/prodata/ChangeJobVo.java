package com.stars.modules.changejob.prodata;

import com.stars.util.StringUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by huwenjun on 2017/5/24.
 */
public class ChangeJobVo implements Comparable<ChangeJobVo>{
    private Integer jobType;//职业type
    private Integer viplevel;//解锁所需vip
    private String reqitem;//解锁消耗
    private Integer level;//解锁角色等级
    private String reqJob;//转职消耗
    private Integer changetime;//转职冷却
    private Integer change;//默认解锁
    private String image;//角色图片

    private Map<Integer, Integer> reqItemMap = new HashMap<>();
    private Map<Integer, Integer> reqJobMap = new HashMap<>();

    public Integer getJobType() {
        return jobType;
    }

    public void setJobType(Integer jobType) {
        this.jobType = jobType;
    }

    public Integer getViplevel() {
        return viplevel;
    }

    public void setViplevel(Integer viplevel) {
        this.viplevel = viplevel;
    }

    public String getReqitem() {
        return reqitem;
    }

    public void setReqitem(String reqitem) {
        this.reqitem = reqitem;
        if (!reqitem.equals("0")) {
            reqItemMap = StringUtil.toMap(reqitem, Integer.class, Integer.class, '+', '|');
        }
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getReqJob() {
        return reqJob;
    }

    public void setReqJob(String reqJob) {
        this.reqJob = reqJob;
        if (!reqJob.equals("0")) {
            reqJobMap = StringUtil.toMap(reqJob, Integer.class, Integer.class, '+', '|');
        }
    }

    public Integer getChangetime() {
        return changetime;
    }

    public void setChangetime(Integer changetime) {
        this.changetime = changetime;
    }

    public Integer getChange() {
        return change;
    }

    public void setChange(Integer change) {
        this.change = change;
    }

    public Map<Integer, Integer> getReqItemMap() {
        return reqItemMap;
    }

    public void setReqItemMap(Map<Integer, Integer> reqItemMap) {
        this.reqItemMap = reqItemMap;
    }

    public Map<Integer, Integer> getReqJobMap() {
        return reqJobMap;
    }

    public void setReqJobMap(Map<Integer, Integer> reqJobMap) {
        this.reqJobMap = reqJobMap;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    @Override
    public int compareTo(ChangeJobVo o) {
        return this.getJobType()-o.getJobType();
    }
}
