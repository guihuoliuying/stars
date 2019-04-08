package com.stars.util;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by zhaowenshuo on 2016/12/20.
 */
public class SetUtil {


    /**
     * 交集
     * @param s1
     * @param s2
     * @param <E>
     * @return
     */
    public static <E> Set<E> intersect(Set<E> s1, Set<E> s2) {
        Set<E> ret = new HashSet<>();
        for (E e : s1) {
            if (s2.contains(e)) {
                ret.add(e);
            }
        }
        return ret;
    }

    /**
     * 并集
     * @param s1
     * @param s2
     * @param <E>
     * @return
     */
    public static <E> Set<E> union(Set<E> s1, Set<E> s2) {
        Set<E> ret = new HashSet<>();
        ret.addAll(s1);
        ret.addAll(s2);
        return ret;
    }

    public static <E> E randomElem(Set<E> s) {
        Iterator<E> it = s.iterator();
        if (it.hasNext()) {
            return it.next();
        }
        return null;
    }

    public static <E> E randomAndRemoveElem(Set<E> s) {
        E e = randomElem(s);
        if (e != null) {
            s.remove(e);
        }
        return e;
    }

}
