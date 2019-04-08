package com.stars.modules.book.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.book.BookModule;
import com.stars.modules.book.BookPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.services.ServiceHelper;

/**
 * Created by zhoujin on 2017/5/9.
 */
public class ServerBook extends PlayerPacket {
    public final static byte REQ_OPEN_BOOK_PANEL = 1;          // 打开典籍面板
    public final static byte REQ_OPEN_HOLE = 2;                // 开孔
    public final static byte REQ_READ_BOOK = 3;                // 阅读
    public final static byte REQ_ACTIVE_BOOK = 4;              // 激活
    public final static byte REQ_LEARN_BOOK = 5;               // 领悟
    public final static byte REQ_READING_PLAYER_LIST = 6;      // 请求正在读书玩家列表
    public final static byte REQ_PLAYER_READING_BOOK = 7;      // 其他玩家正在读书列表
    public final static byte REQ_AWAKE_QUYUAN = 8;             // 唤醒屈原
    public final static byte REQ_PLAYER_BOOK_DETAIL = 9;       // 典籍详情
    public final static byte REQ_QUICK_FINISH = 10;            // 快速完成

    private byte reqType;

    // 开孔
    private byte holeId;

    // 阅读 激活 领悟 典籍详情
    private int bookId;

    // 唤醒 其他玩家正在读书列表
    private String target;

    // 其他玩家正在读书列表
    private byte subType; // 0代表普通请求，1代表交互成功后请求

    @Override
    public void execPacket(Player player) {
        BookModule bookModule = module(MConst.Book);
        switch (reqType) {
            case REQ_OPEN_BOOK_PANEL:
                bookModule.openBookPanel();
                break;
            case REQ_OPEN_HOLE:
                bookModule.openHole(holeId);
                break;
            case REQ_READ_BOOK:
                bookModule.readBook(bookId);
                break;
            case REQ_ACTIVE_BOOK:
                bookModule.activeBook(bookId);
                break;
            case REQ_LEARN_BOOK:
                bookModule.learnBook(bookId);
                break;
            case REQ_READING_PLAYER_LIST:
                ServiceHelper.bookService().readingBookRoleList(getRoleId());
                break;
            case REQ_PLAYER_READING_BOOK:
                ServiceHelper.bookService().playerReadingBookList(getRoleId(), subType, Long.valueOf(target));
                break;
            case REQ_AWAKE_QUYUAN:
                bookModule.awakeQuyuan(Long.valueOf(target));
                break;
            case REQ_PLAYER_BOOK_DETAIL:
                bookModule.bookDetail(bookId);
                break;
            case REQ_QUICK_FINISH:
                bookModule.quickReadBook(bookId);
                break;
            default:
                break;
        }
    }

    @Override
    public short getType() {
        return BookPacketSet.S_BOOK;
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        reqType = buff.readByte();
        switch (reqType) {
            case REQ_OPEN_HOLE:
                holeId = buff.readByte();
                break;
            case REQ_READ_BOOK:
                bookId = buff.readInt();
                break;
            case REQ_ACTIVE_BOOK:
                bookId = buff.readInt();
                break;
            case REQ_LEARN_BOOK:
                bookId = buff.readInt();
                break;
            case REQ_PLAYER_READING_BOOK:
                subType = buff.readByte();
                target = buff.readString();
                break;
            case REQ_AWAKE_QUYUAN:
                target = buff.readString();
                break;
            case REQ_PLAYER_BOOK_DETAIL:
                bookId = buff.readInt();
                break;
            case REQ_QUICK_FINISH:
                bookId = buff.readInt();
                break;
            default:
                break;
        }
    }
}
