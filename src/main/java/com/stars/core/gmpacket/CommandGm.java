package com.stars.core.gmpacket;

import com.stars.core.hotupdate.CommManagerProxy;
import com.stars.server.main.gmpacket.GmPacketHandler;
import com.stars.server.main.gmpacket.GmPacketResponse;
import com.stars.util.LogUtil;
import com.stars.util.StringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by wuyuxing on 2017/1/5.
 */
public class CommandGm extends GmPacketHandler {

    public static ReentrantLock gmLock = new ReentrantLock();
    @Override
    public String handle(HashMap args) {
        if(args == null){
            GmPacketResponse response = new GmPacketResponse(GmPacketResponse.SUC, 1, resultToJson("comm执行异常"));
            return response.toString();
        }
        String commStr = String.valueOf(args.get("command"));
        String[] commParam = commStr.split("#");
        List<String> paramerList = arrayToList(commParam);
        String result;
        Map<String,Object> map = new HashMap<>();
        if(paramerList == null) {
            result = "gm参数错误";
            map.put("resultMsg", result);
            GmPacketResponse response = new GmPacketResponse(GmPacketResponse.SUC, 1, resultToJson(result));
            return response.toString();
        }

        if(commParam!=null && commParam[0].equals("hotUpdateComm")){
            HotUpdateCommGm updateComm = new HotUpdateCommGm();
            return updateComm.handle(null);
        }

        try{
            result = CommManagerProxy.getManager().comm(paramerList);
            LogUtil.info("CommandGm|执行CommManager成功!");
        }catch (Exception e){
            result = "执行Command命令失败";
            LogUtil.info("CommandGm|执行CommManager失败!|"+e.getMessage());
        }
        map.put("resultMsg", result);
        GmPacketResponse response = new GmPacketResponse(GmPacketResponse.SUC, 1, resultToJson(map));
        return response.toString();
    }

    private List<String> arrayToList(String[] array){
        List<String> paramerList = new ArrayList<String>();
        for(int i=0;i<array.length;i++){
            if(StringUtil.isEmpty(array[i])) continue;
            paramerList.add(array[i]);
        }
        return paramerList;
    }
}
