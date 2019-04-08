package com.stars.services.book;

import com.stars.core.player.PlayerUtil;
import com.stars.modules.book.BookManager;
import com.stars.modules.book.event.BeKickEvent;
import com.stars.modules.book.event.KickEvent;
import com.stars.modules.book.packet.ClientBook;
import com.stars.modules.book.prodata.BookInfo;
import com.stars.modules.book.prodata.BookRead;
import com.stars.modules.book.util.BookUtilTmp;
import com.stars.modules.book.util.RoleBookTmp;
import com.stars.modules.data.DataManager;
import com.stars.modules.drop.DropUtil;
import com.stars.services.SConst;
import com.stars.services.ServiceHelper;
import com.stars.services.ServiceSystem;
import com.stars.util.DateUtil;
import com.stars.util.RandomUtil;
import com.stars.core.actor.invocation.ServiceActor;

import java.util.*;


/**
 * Created by zhoujin on 2017/5/10.
 */
public class BookServiceActor extends ServiceActor implements BookService {
	/**
	 * 玩家交互时出现的数据不同步可忽略,并不是什么关键数据
	 */
    Map<Long,Map<Integer,BookUtilTmp>> roleReadBookList = new HashMap<>();

	// 分两个map是想减少roleReadBookList的size，这样遍历的时候量要少
	Map<Long,RoleBookTmp> roleBookTmpMap = new HashMap<>();
	@Override
	public void init() throws Throwable {
		ServiceSystem.getOrAdd(SConst.BookService, this);
	}

	@Override
	public void printState() {

	}

	@Override
	public void playerOnline(long roleId, List<BookUtilTmp> readBookList, RoleBookTmp roleBookTmp) {
		roleBookTmpMap.put(roleId,roleBookTmp);
		if (readBookList.size() == 0)
			return;

		Map<Integer,BookUtilTmp> map = new HashMap<>();

		for (BookUtilTmp book : readBookList) {
			map.put(book.getBookId(),book);
		}
		roleReadBookList.put(roleId,map);
	}

	@Override
	public void playerOffline(long roleId) {
		roleBookTmpMap.remove(roleId);
		roleReadBookList.remove(roleId);
	}

	@Override
	public void syncReadBook(long roleId,BookUtilTmp tmp, byte op) {
		if (op == 1) {
			// 增加
			if (!roleReadBookList.containsKey(roleId)) {
				Map<Integer,BookUtilTmp> map = new HashMap<>();
				roleReadBookList.put(roleId,map);
			}
			roleReadBookList.get(roleId).put(tmp.getBookId(),tmp);
		}else {
			// 减少
			Map<Integer,BookUtilTmp> map = roleReadBookList.get(roleId);
			if (null != map) {
				map.remove(tmp.getBookId());
				if (map.size() == 0) {
					roleReadBookList.remove(roleId);
				}
			}
		}
	}

	@Override
	public void resetBeKickTimes(long roleId) {
        if (!roleBookTmpMap.containsKey(roleId))
			return;
		roleBookTmpMap.get(roleId).setBeKickTimes((short)0);
	}

	/**
	 * 唤醒屈原
	 * @param roleId
	 * @param target
     */
	@Override
	public void awakePlayer(long roleId, long target) {
        if (!roleBookTmpMap.containsKey(target)) {
			awakeResult(roleId,target, BookManager.KICK_PLAYER_OFFLINE);
			return;
		}
		int maxBeKickTimes = DataManager.getCommConfig("book_passivehelpcount",10);
		if (roleBookTmpMap.get(target).getBeKickTimes() >= maxBeKickTimes) {
			awakeResult(roleId,target,BookManager.KICK_BEKICK_LIMIT);
			return;
		}
		int now = DateUtil.getSecondTime();
		int bookInter = DataManager.getCommConfig("book_inter",10);
        if (roleBookTmpMap.get(target).getBeLastKickTime() + bookInter > now) {
			awakeResult(roleId,target,BookManager.KICK_CD);
			return;
		}
		roleBookTmpMap.get(target).setBeLastKickTime(now);
		roleBookTmpMap.get(target).setBeKickTimes((short)(roleBookTmpMap.get(target).getBeKickTimes() + 1));
		Map<Integer,BookUtilTmp> tmpMap = roleReadBookList.get(target);
		if (null == tmpMap || tmpMap.size() == 0) {
			awakeResult(roleId,target,BookManager.KICK_BOOK_READED);
			return;
		}

		for (Map.Entry<Integer,BookUtilTmp> entry : tmpMap.entrySet()) {
			if (entry.getValue().getEndReadTime() > now) {
				BookRead bookRead = BookManager.getBookRead(entry.getValue().getBookId(),entry.getValue().getBookLv());
				if (null == bookRead) {
					continue;
				}else {
					entry.getValue().setEndReadTime(entry.getValue().getEndReadTime() - bookRead.getHelpaddtime());
				}
			}
		}
		awakeResult(roleId,target,BookManager.KICK_SUCCESS);
	}

	private void awakeResult(long roleId, long target, byte result) {
        if (BookManager.KICK_SUCCESS != result) {
			// 各种原因失败
			Map<Integer,Integer> toolMap = new HashMap<>();
			KickEvent ev = new KickEvent(result,target,toolMap);
			ServiceHelper.roleService().notice(roleId,ev);
		}else {
            // int dropId = DataManager.getCommConfig("book_passivehelpaward",45);
			// Map<Integer, Integer> toolMap = DropUtil.executeDrop(dropId, 1);
			Map<Integer, Integer> toolMap = new HashMap<>();
			Map<Integer,BookUtilTmp> tmpMap = roleReadBookList.get(target);
			if (null != tmpMap && tmpMap.size() > 0) {
				LinkedList<Integer> randList = new LinkedList<>();
				for (Map.Entry<Integer,BookUtilTmp> entry : tmpMap.entrySet()) {
					randList.add(entry.getKey());
				}
				int random = RandomUtil.rand(0,randList.size()-1);
				int randbookId = randList.get(random);
				BookInfo bookInfo = BookManager.getBookInfo(randbookId);
				if (null != bookInfo) {
					toolMap = DropUtil.executeDrop(bookInfo.getHelpaward(), 1);
				}
			}
			// 通知唤醒成功
			KickEvent ev = new KickEvent(BookManager.KICK_SUCCESS,target,toolMap);
			ServiceHelper.roleService().notice(roleId,ev);
			// 通知被唤醒
			BeKickEvent event = new BeKickEvent();
			ServiceHelper.roleService().notice(target,event);
		}
	}

	private RoleBookTmp isReading(long roleId, RoleBookTmp rolebook,
								  Map<Integer,BookUtilTmp> bookUtilTmpMap,byte icon) {
		if ( null == rolebook || null == bookUtilTmpMap)
			return null;
		int now = DateUtil.getSecondTime();
		int maxBeKickTimes = DataManager.getCommConfig("book_passivehelpcount",10);
		if (rolebook.getBeKickTimes() >= maxBeKickTimes) return null;
		int bookInter = DataManager.getCommConfig("book_inter",10);
		if (rolebook.getBeLastKickTime() + bookInter > now) return null;
		boolean isReading = false;
		for (Map.Entry<Integer,BookUtilTmp> entry : bookUtilTmpMap.entrySet()) {
			if (entry.getValue().getEndReadTime() > now) {
				isReading = true;
				break;
			}
		}
		if (isReading) {
			RoleBookTmp tmp = new RoleBookTmp();
			tmp.setRoleId(roleId);
			tmp.setName(rolebook.getName());
			tmp.setJobId(rolebook.getJobId());
			tmp.setLevel(rolebook.getLevel());
			tmp.setIcon(icon);
            return tmp;
		}else
			return null;
	}

	/**
	 * 正在读书的玩家列表
	 * @param roleId
     */
	@Override
	public void readingBookRoleList(long roleId) {
        ClientBook res = new ClientBook();
		res.setResType(ClientBook.RES_READING_PLAYER_LIST);
		List<RoleBookTmp> resList = new ArrayList<>();
		Set<Long> tmpset = new HashSet<>();
		List<Long> friendList = ServiceHelper.friendService().getFriendList(roleId);
		long familyId = ServiceHelper.familyRoleService().getFamilyId(roleId);
		List<Long> familyList = ServiceHelper.familyMainService().getMemberIdList(familyId, roleId);
		// 好友--家族--陌生人
		int maxDisplay = DataManager.getCommConfig("book_displaycount",10);
        for (Long id : friendList) {
			if (roleBookTmpMap.containsKey(id) && roleReadBookList.containsKey(id) && roleReadBookList.get(id).size() > 0) {
				if (id == roleId)
					continue;
				RoleBookTmp tmp = isReading(id,roleBookTmpMap.get(id),roleReadBookList.get(id),BookManager.IOCN_FRIEND);
				if (null != tmp) {
					tmpset.add(id);
					resList.add(tmp);
				}
				if (tmpset.size() >= maxDisplay)
					break;
			}
		}
		if (tmpset.size() < maxDisplay) {
			for (Long id : familyList) {
				if (roleBookTmpMap.containsKey(id) && roleReadBookList.containsKey(id) && roleReadBookList.get(id).size() > 0) {
					if (id == roleId)
						continue;
					if (tmpset.contains(id))
						continue;
					RoleBookTmp tmp = isReading(id,roleBookTmpMap.get(id),roleReadBookList.get(id),BookManager.IOCN_FAMILY);
					if (null != tmp) {
						tmpset.add(id);
						resList.add(tmp);
					}
					if (tmpset.size() >= maxDisplay)
						break;
				}
			}
		}
		if (tmpset.size() < maxDisplay) {
			for (Map.Entry<Long,Map<Integer,BookUtilTmp>> entry : roleReadBookList.entrySet()) {
				if (entry.getKey() == roleId)
					continue;
				if (roleBookTmpMap.containsKey(entry.getKey())) {
					if (tmpset.contains(entry.getKey()))
						continue;
					RoleBookTmp tmp = isReading(entry.getKey(),roleBookTmpMap.get(entry.getKey()),entry.getValue(),BookManager.IOCN_NOMAL);
					if (null != tmp) {
						tmpset.add(entry.getKey());
						resList.add(tmp);
					}
					if (tmpset.size() >= maxDisplay)
						break;
				}
			}
		}
		res.setRoleList(resList);
		PlayerUtil.send(roleId, res);
	}

	/**
	 * 玩家读书列表
	 * @param roleId
	 * @param target
     */

	@Override
	public void playerReadingBookList(long roleId, byte subType, long target) {
		ClientBook res = new ClientBook();
		res.setResType(ClientBook.RES_PLAYER_READING_BOOK);
		res.setSubType(subType);
		res.setTarget("" + target);
		List<BookUtilTmp> resList = new ArrayList<>();
		int now = DateUtil.getSecondTime();
		Map<Integer,BookUtilTmp> bookmap = roleReadBookList.get(target);
		if (null != bookmap && bookmap.size() > 0) {
            for (Map.Entry<Integer,BookUtilTmp> entry : bookmap.entrySet()) {
                if (entry.getValue().getEndReadTime() > now) {
					resList.add(entry.getValue());
				}
			}
		}
		res.setBookList(resList);
		PlayerUtil.send(roleId, res);
	}
}
