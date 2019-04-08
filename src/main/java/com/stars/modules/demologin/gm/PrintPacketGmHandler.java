package com.stars.modules.demologin.gm;

import com.stars.core.module.Module;
import com.stars.modules.gm.GmHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by chenkeyu on 2017-07-31.
 */
public class PrintPacketGmHandler implements GmHandler {
    private static boolean isPrintPacket = false;
    private static List<String> packetList = new ArrayList<>();
    private static boolean all = false;

    @Override
    public void handle(long roleId, Map<String, Module> moduleMap, String[] args) throws Exception {
        all = false;
        int openOrClose = Integer.parseInt(args[0]);
        isPrintPacket = openOrClose == 1;
        if (!isPrintPacket) {
            packetList.clear();
        } else {
            if (args[1].equals("1")) {
                all = true;
            }else if (args[1].equals("0")){
                all = false;
            }
            packetList = new ArrayList<>(Arrays.asList(args));
        }

    }

    public static boolean canPrintPacket(String packetType) {
        if (isPrintPacket && all && packetList.contains(packetType)) {
            return true;
        }
        if (isPrintPacket && !all && !packetList.contains(packetType)) {
            return true;
        }
        return false;
    }
}
