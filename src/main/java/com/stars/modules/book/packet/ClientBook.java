package com.stars.modules.book.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.book.BookManager;
import com.stars.modules.book.BookPacketSet;
import com.stars.modules.book.prodata.BookInfo;
import com.stars.modules.book.prodata.BookRead;
import com.stars.modules.book.prodata.OpenHoleInfo;
import com.stars.modules.book.userdata.RoleBook;
import com.stars.modules.book.userdata.RoleBookUtil;
import com.stars.modules.book.util.BookUtilTmp;
import com.stars.modules.book.util.RoleBookTmp;
import com.stars.modules.data.DataManager;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.DateUtil;

import java.util.List;
import java.util.Map;

/**
 * Created by zhoujin on 2017/5/9.
 */
public class ClientBook extends PlayerPacket {
    public final static byte RES_OPEN_BOOK_PANEL = 1;          // 打开典籍面板
    public final static byte RES_OPEN_HOLE = 2;                // 开孔
    public final static byte RES_READ_BOOK = 3;                // 阅读
    public final static byte RES_ACTIVE_BOOK = 4;              // 激活
    public final static byte RES_LEARN_BOOK = 5;               // 领悟
    public final static byte RES_READING_PLAYER_LIST = 6;      // 请求正在读书玩家列表
    public final static byte RES_PLAYER_READING_BOOK = 7;      // 其他玩家正在读书列表
    public final static byte RES_AWAKE_QUYUAN = 8;             // 唤醒屈原
    public final static byte RES_PLAYER_BOOK_DETAIL = 9;       // 典籍详情
    public final static byte RES_ACTIVE_BOOK_LIST = 10;        // 已激活典籍列表
    public final static byte RES_BOOK_NUM = 11;                // 典籍碎片数量增加
    public final static byte RES_BOOK_UPDATE_TIME = 12;        // 更新时间

    private byte resType;

    // 打开典籍面板
    private RoleBook selfRoleBook;
    private Map<Integer, RoleBookUtil> selfBookMap;

    // 开孔
    private byte holeId;

    // 激活
    private int bookId;
    private byte bookStatus;  // 0未激活 1激活

    // 阅读
    private int startReadTime;
    private int endReadTime;
    private int remainTime; //剩余时间

    // 领悟
    private short bookLv; // 领悟后等级

    // 请求正在读书玩家列表
    private List<RoleBookTmp> roleList;

    // 其他玩家正在读书列表
    private byte subType;  // 0代表普通请求，1代表交互成功后请求
    private List<BookUtilTmp> bookList;

    // 唤醒屈原
    private String target;
    private byte result;   // 唤醒结果

    // 典籍详情

    // 已激活典籍列表
    private List<Integer> activeList;

    // 典籍碎片数量增加
    private int bookNum;

    private String reqItem;

    public void setResType(byte resType) {
        this.resType = resType;
    }

    public void setSelfRoleBook(RoleBook selfRoleBook) {
        this.selfRoleBook = selfRoleBook;
    }

    public void setSelfBookMap(Map<Integer, RoleBookUtil> selfBookMap) {
        this.selfBookMap = selfBookMap;
    }

    public void setHoleId(byte holeId) {
        this.holeId = holeId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public void setBookLv(short bookLv) {
        this.bookLv = bookLv;
    }

    public void setBookStatus(byte bookStatus) {
        this.bookStatus = bookStatus;
    }

    public void setStartReadTime(int startReadTime) {
        this.startReadTime = startReadTime;
    }

    public void setEndReadTime(int endReadTime) {
        this.endReadTime = endReadTime;
    }

    public void setRemainTime(int remainTime) {
        this.remainTime = remainTime;
    }

    public void setBookList(List<BookUtilTmp> bookList) {
        this.bookList = bookList;
    }

    public void setRoleList(List<RoleBookTmp> roleList) {
        this.roleList = roleList;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public void setResult(byte result) {
        this.result = result;
    }

    public void setSubType(byte subType) {
        this.subType = subType;
    }

    public void setActiveList(List<Integer> activeList) {
        this.activeList = activeList;
    }

    public void setBookNum(int bookNum) {
        this.bookNum = bookNum;
    }

    public void setReqItem(String reqItem) {
        this.reqItem = reqItem;
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return BookPacketSet.C_BOOK;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeByte(resType);
        switch (resType) {
            case RES_OPEN_BOOK_PANEL: {
                int now = DateUtil.getSecondTime();
                int maxKickTimes = DataManager.getCommConfig("book_activehelpcount", 10);
                buff.writeShort((short) (maxKickTimes - selfRoleBook.getKickTimes()));
                // 开孔信息(未开孔的发开孔条件)
                buff.writeString(selfRoleBook.getHoleStr());
                int size = (BookManager.openHoleInfoMap.size() - selfRoleBook.getHoleSet().size());
                buff.writeInt(size);
                for (Map.Entry<Byte, OpenHoleInfo> entry : BookManager.openHoleInfoMap.entrySet()) {
                    if (selfRoleBook.getHoleSet().contains(entry.getKey()))
                        continue;
                    buff.writeByte(entry.getValue().getHoleId());
                    buff.writeInt(entry.getValue().getLv());
                    buff.writeInt(entry.getValue().getViplv());
                    buff.writeInt(entry.getValue().getItemId());
                    buff.writeInt(entry.getValue().getItemNum());
                }
                // 所有书籍信息(基本信息)
                buff.writeInt(BookManager.bookInfoMap.size());
                for (Map.Entry<Integer, BookInfo> entry : BookManager.bookInfoMap.entrySet()) {
                    buff.writeInt(entry.getValue().getBookid());
                    buff.writeString(entry.getValue().getName());
                    buff.writeString(entry.getValue().getIcon());
                    buff.writeByte(entry.getValue().getQuality());
                    short tmpBookLv = 0;
                    int tmpStartTime = 0;
                    int tmpEndTime = 0;
                    int tmpRemainTime = 0;
                    byte tmpActive = 0;
                    int tmpRolelvLimit = 0;
                    int tmpNum = 0;
                    if (selfBookMap.containsKey(entry.getKey())) {
                        tmpBookLv = selfBookMap.get(entry.getKey()).getBookLv();
                        tmpStartTime = selfBookMap.get(entry.getKey()).getStartReadTime();
                        tmpEndTime = selfBookMap.get(entry.getKey()).getEndReadTime();
                        tmpActive = selfBookMap.get(entry.getKey()).getBookStatus();
                        if (0 != tmpEndTime) {
                            tmpRemainTime = (now - tmpEndTime) >= 0 ? 0 : (tmpEndTime - now);
                        }
                        BookRead bookRead = BookManager.getBookRead(entry.getValue().getBookid(), tmpBookLv);
                        tmpRolelvLimit = bookRead.getRolelevel();
                        tmpNum = selfBookMap.get(entry.getKey()).getBookNum();
                    }
                    buff.writeShort(tmpBookLv);
                    buff.writeByte(entry.getValue().getDisplay());
                    buff.writeInt(entry.getValue().getRank());
                    buff.writeInt(tmpStartTime);
                    buff.writeInt(tmpEndTime);
                    buff.writeInt(tmpRemainTime);
                    buff.writeByte(tmpActive);
                    buff.writeInt(tmpRolelvLimit);
                    buff.writeInt(entry.getValue().getReqitemmax());
                    buff.writeInt(tmpNum);
                }
                break;
            }
            case RES_OPEN_HOLE: {
                buff.writeByte(holeId);
                break;
            }
            case RES_READ_BOOK: {
                buff.writeInt(bookId);
                buff.writeInt(startReadTime);
                buff.writeInt(endReadTime);
                buff.writeInt(remainTime);
                break;
            }
            case RES_ACTIVE_BOOK: {
                buff.writeInt(bookId);
                buff.writeByte(BookManager.BOOK_ACTIVE);
                break;
            }
            case RES_LEARN_BOOK: {
                buff.writeInt(bookId);
                buff.writeShort(bookLv);
                String tmpLvupreqitem = "";
                int tmpRolelvLimit = 0;
                BookInfo bookInfo = BookManager.getBookInfo(bookId);
                if (bookLv < bookInfo.getMaxlv()) {
                    BookRead bookRead = BookManager.getBookRead(bookId, bookLv);
                    tmpLvupreqitem = bookRead.getLvupreqitem();
                    tmpRolelvLimit = bookRead.getRolelevel();
                }
                buff.writeString(tmpLvupreqitem);
                buff.writeInt(tmpRolelvLimit);
                break;
            }
            case RES_READING_PLAYER_LIST: {
                buff.writeInt(roleList.size());
                for (RoleBookTmp tmp : roleList) {
                    buff.writeString("" + tmp.getRoleId());
                    buff.writeString(tmp.getName());
                    buff.writeInt(tmp.getLevel());
                    buff.writeInt(tmp.getJobId());
                    buff.writeByte(tmp.getIcon());
                }
                break;
            }
            case RES_PLAYER_READING_BOOK: {
                buff.writeByte(subType);
                buff.writeString(target);
                buff.writeInt(bookList.size());
                int now = DateUtil.getSecondTime();
                for (BookUtilTmp tmp : bookList) {
                    buff.writeInt(tmp.getBookId());
                    buff.writeShort(tmp.getBookLv());
                    BookInfo bookInfo = BookManager.getBookInfo(tmp.getBookId());
                    buff.writeByte(bookInfo.getQuality());
                    buff.writeString(bookInfo.getName());
                    buff.writeString(bookInfo.getIcon());
                    buff.writeInt(tmp.getStartReadTime());
                    buff.writeInt(tmp.getEndReadTime());
                    int tmpRemainTime = (tmp.getEndReadTime() - now > 0) ? (tmp.getEndReadTime() - now) : 0;
                    buff.writeInt(tmpRemainTime);
                }
                break;
            }
            case RES_AWAKE_QUYUAN: {
                buff.writeString(target);
                buff.writeByte(result);
                break;
            }
            case RES_PLAYER_BOOK_DETAIL: {
                int now = DateUtil.getSecondTime();
                buff.writeInt(bookId);
                BookInfo bookInfo = BookManager.getBookInfo(bookId);
                buff.writeByte(bookInfo.getQuality());
                buff.writeString(bookInfo.getName());
                buff.writeString(bookInfo.getIcon());
                buff.writeString(bookInfo.getInfodesc());
                buff.writeInt(bookInfo.getReqitem());
                buff.writeInt(bookInfo.getReqitemmax());
                buff.writeByte(bookInfo.getDisplay());
                buff.writeInt(bookInfo.getRank());
                buff.writeShort(bookInfo.getMaxlv());
                int tmpNum = 0;
                byte tmpActive = 0;
                short tmpLv = 0;
                int tmpStartTime = 0;
                int tmpEndTime = 0;
                int tmpRemainTime = 0;
                String tmpLvupreqitem = "";
                if (selfBookMap.containsKey(bookId)) {
                    tmpNum = selfBookMap.get(bookId).getBookNum();
                    tmpActive = selfBookMap.get(bookId).getBookStatus();
                    tmpLv = selfBookMap.get(bookId).getBookLv();
                    tmpStartTime = selfBookMap.get(bookId).getStartReadTime();
                    tmpEndTime = selfBookMap.get(bookId).getEndReadTime();
                    if (0 != tmpEndTime) {
                        tmpRemainTime = (now - tmpEndTime) >= 0 ? 0 : (tmpEndTime - now);
                    }
                    if (tmpLv < bookInfo.getMaxlv()) {
                        BookRead bookRead = BookManager.getBookRead(bookId, tmpLv);
                        tmpLvupreqitem = bookRead.getLvupreqitem();
                    }
                }
                buff.writeInt(tmpNum);
                buff.writeByte(tmpActive);
                buff.writeShort(tmpLv);
                buff.writeInt(tmpStartTime);
                buff.writeInt(tmpEndTime);
                buff.writeInt(tmpRemainTime);
                buff.writeString(tmpLvupreqitem);
                break;
            }
            case RES_ACTIVE_BOOK_LIST: {
                int size = activeList.size();
                buff.writeInt(size);
                for (Integer bookId : activeList) {
                    buff.writeInt(bookId);
                }
                break;
            }
            case RES_BOOK_NUM: {
                buff.writeInt(bookId);
                buff.writeInt(bookNum);
                break;
            }
            case RES_BOOK_UPDATE_TIME: {
                buff.writeInt(bookId);
                buff.writeInt(startReadTime);
                buff.writeInt(endReadTime);
                buff.writeInt(remainTime);
                buff.writeString(reqItem);
                break;
            }
            default:
                break;
        }
    }
}
