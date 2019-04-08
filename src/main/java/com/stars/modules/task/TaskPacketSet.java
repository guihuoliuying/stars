package com.stars.modules.task;

import com.stars.modules.task.packet.*;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

public class TaskPacketSet extends PacketSet {

    public static short Client_Task_List = 0x0036;//任务列表

    public static short Client_Task_Process = 0x0037;//任务进度

    public static short Client_Task_Remove = 0x0038;//删除任务

    public static short Server_Task_Submit = 0x0039;//提交任务

    public static short Client_Task_Accept = 0x003A;//接受任务

    public TaskPacketSet() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public List<Class<? extends com.stars.network.server.packet.Packet>> getPacketList() {
        // TODO Auto-generated method stub
        List<Class<? extends com.stars.network.server.packet.Packet>> al = new ArrayList<Class<? extends Packet>>();
        al.add(ClientTaskList.class);
        al.add(ClientTaskProcess.class);
        al.add(ClientRemoveTask.class);
        al.add(ServerSubmitTask.class);
        al.add(ServerAcceptTask.class);
        return al;
    }
}
