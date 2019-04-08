package com.stars.modules.searchtreasure.prodata;

import com.stars.core.attr.FormularUtils;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 仙山探宝关卡表;
 * Created by panzhenfeng on 2016/8/24.
 */
public class SearchStageVo {
    private int stageinfoid;
    private String layerName;
    private String chest;
    private String searchpoint;
    private String searchPointContent;

    public List<Integer> getAllSearchContentIds(){
        String[] arr = searchPointContent.split("\\|");
        String[] itemArr = null;
        String[] valueArr = null;
        String[] paramArr = null;
        int contentId = 0;
        List<Integer> rtnList = new ArrayList<>();
        for(int i = 0, len = arr.length; i<len; i++){
            itemArr = arr[i].split("=");
            valueArr = itemArr[1].split(",");
            for(int j = 0, jlen =valueArr.length; j<jlen; j++){
                paramArr = valueArr[j].split("\\+");
                contentId = Integer.parseInt(paramArr[0]);
                if(!rtnList.contains(contentId)){
                    rtnList.add(contentId);
                }
            }
        }
        return rtnList;
    }

    public void writeBuff(NewByteBuffer buff) {
        buff.writeInt(stageinfoid);
        buff.writeString(chest);
        buff.writeString(searchpoint);
        buff.writeString(searchPointContent);
    }

    public void writeBuffToClientShow(NewByteBuffer buff){
        buff.writeInt(stageinfoid);
        buff.writeString(layerName);
        buff.writeString(chest);
        buff.writeInt(getSearchPointCount());
    }

    public int getSearchPointCount(){
        String[] pathPointArr = searchpoint.split("\\|");
        return pathPointArr.length;
    }

    public String getPathPointInfo(int pathPointIndex){
        String[] pathPointArr = searchpoint.split("\\|");
        String[] itemArr = null;
        for (int i = 0, len = pathPointArr.length; i<len; i++){
            itemArr = pathPointArr[i].split(",");
            if(Integer.parseInt(itemArr[0]) == pathPointIndex){
                if(itemArr.length>1){
                    return  itemArr[1];
                }else{
                    return null;
                }
            }
        }
        return null;
    }

    public boolean isInPathPoint(int pathPointIndex, float x, float z){
        String pathPointInfo = getPathPointInfo(pathPointIndex);
        if(StringUtil.isNotEmpty(pathPointInfo)){
            String[] valueArr = pathPointInfo.split("\\+");
            //判断是否在圆内;
            return FormularUtils.isPointInCircle(Float.parseFloat(valueArr[0])/10, Float.parseFloat(valueArr[2])/10, Float.parseFloat(valueArr[3])/10, x, z);
        }
        return false;
    }

    public String getLayerName() {
        return layerName;
    }

    public void setLayerName(String layerName) {
        this.layerName = layerName;
    }

    public String getChest() {
        return chest;
    }

    public void setChest(String chest) {
        this.chest = chest;
    }

    public String getSearchpoint() {
        return searchpoint;
    }

    public void setSearchpoint(String searchpoint) {
        this.searchpoint = searchpoint;
    }

    public String getSearchPointContent() {
        return searchPointContent;
    }

    public void setSearchPointContent(String searchPointContent) {
        this.searchPointContent = searchPointContent;
    }

    public int getStageinfoid() {
        return stageinfoid;
    }

    public void setStageinfoid(int stageinfoid) {
        this.stageinfoid = stageinfoid;
    }
}
