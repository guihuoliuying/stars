package com.stars.services.book;

import com.stars.modules.book.util.BookUtilTmp;
import com.stars.modules.book.util.RoleBookTmp;
import com.stars.services.Service;
import com.stars.core.actor.invocation.ActorService;
import com.stars.core.actor.invocation.annotation.AsyncInvocation;

import java.util.List;
/**
 * Created by zhoujin on 2017/5/10.
 */
public interface BookService extends Service, ActorService {

    @AsyncInvocation
    void playerOnline(long roleId, List<BookUtilTmp> readBookList, RoleBookTmp roleBookTmp);

    @AsyncInvocation
    void playerOffline(long roleId);

    @AsyncInvocation
    void syncReadBook(long roleId, BookUtilTmp tmp, byte op);

    @AsyncInvocation
    void resetBeKickTimes(long roleId);

    @AsyncInvocation
    void awakePlayer(long roleId, long target);

    @AsyncInvocation
    void readingBookRoleList(long roleId);

    @AsyncInvocation
    void playerReadingBookList(long roleId, byte subType, long target);
}
