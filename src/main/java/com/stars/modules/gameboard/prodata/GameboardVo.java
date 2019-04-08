package com.stars.modules.gameboard.prodata;

import com.stars.db.DBUtil;
import com.stars.db.DbRow;
import com.stars.db.SqlUtil;
import com.stars.network.server.buffer.NewByteBuffer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by chenkeyu on 2017/1/5 18:43
 */
public class GameboardVo extends DbRow {
    private int boardid;
    private byte label;
    private String title;
    private String text;
    private String date;
    private String serverdate;
    private String plateform;

    private List<String> channels;

    private long startDate;
    private long endDate;
    private int serverStart;
    private int serverEnd;

    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeInt(boardid);
        buff.writeByte(label);
        buff.writeString(title);
        buff.writeString(text);
        buff.writeString(plateform);
    }

    public GameboardVo() {
    }

    public int getBoardid() {
        return boardid;
    }

    public void setBoardid(int boardid) {
        this.boardid = boardid;
    }

    public byte getLabel() {
        return label;
    }

    public void setLabel(byte label) {
        this.label = label;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
        String[] args = date.split("\\&");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            this.startDate = simpleDateFormat.parse(args[0] + " 00:00:00").getTime();
            this.endDate = simpleDateFormat.parse(args[1] + " 23:59:59").getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public String getServerdate() {
        return serverdate;
    }

    public void setServerdate(String serverdate) {
        this.serverdate = serverdate;
        if (!"0".equals(serverdate)) {
            String[] args = serverdate.split("\\+");
            this.serverStart = Integer.parseInt(args[0]);
            this.serverEnd = Integer.parseInt(args[1]);
        }
    }

    public long getStartDateGm() {
        return startDate / 1000;
    }

    public long getEndDateGm() {
        return endDate / 1000;
    }

    public long getStartDate() {
        return startDate;
    }

    public long getEndDate() {
        return endDate;
    }

    public String getPlateform() {
        return plateform;
    }

    public void setPlateform(String plateform) {
        this.plateform = plateform;
        this.channels = new ArrayList<>();
        String[] tmp = plateform.split(",");
        for (String channel : tmp) {
            if (channel.equals(""))
                continue;
            this.channels.add(channel);
        }
    }

    public List<String> getChannels() {
        return channels;
    }

    public int getServerStart() {
        return serverStart;
    }

    public int getServerEnd() {
        return serverEnd;
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_PRODUCT, "gameboard", "`boardid`=" + this.boardid);
    }

    @Override
    public String getDeleteSql() {
        return "delete from `gameboard` where `boardid`=" + this.boardid;
    }
}
