package com.stars.core.gmpacket;

import com.stars.core.hotupdate.YinHanHotUpdateManager;
import com.stars.server.main.gmpacket.GmPacketHandler;
import com.stars.server.main.gmpacket.GmPacketResponse;
import com.stars.util.LogUtil;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by wuyuxing on 2017/1/5.
 */
public class HotUpdateCommGm extends GmPacketHandler {
    @Override
    public String handle(HashMap args) {
        String result = "热更CommManager失败";
        try{
            File f = new File("comm" + File.separator + "CommManager.class");
            if(f.exists()) {
                if (YinHanHotUpdateManager.hotUpdateClass("CommManager")){
                    LogUtil.info("HotUpdateCommGm|热更CommManager成功!");
                    result = "热更CommManager成功";
                }
            }
        }catch (Exception e){
            LogUtil.error(e.getMessage(),e);
            result = "热更CommManager失败";
            LogUtil.info("HotUpdateCommGm|热更CommManager失败!");
        }
        Map<String,Object> map = new HashMap<>();
        map.put("resultMsg", result);
        GmPacketResponse response = new GmPacketResponse(GmPacketResponse.SUC,1,resultToJson(map));
        return response.toString();
    }
}
