package com.stars.multiserver.fightingmaster;

import java.util.*;

/**
 * 排行榜,非线程安全
 * Created by zhouyaohui on 2016/11/21.
 */
public class Ranker<T extends Rankable> {

    private Map<Integer, Segment> segmentMap = new HashMap<>();
    private Map<Rankable, Node> rankMap = new HashMap<>();
    private Segment head = new Segment();
    private Segment tail = new Segment();
    private int segmentCount = 0;
    private int segmentMinSize = 50;
    private int segmentMaxSize = 200;

    public Ranker() {
        Segment segment = new Segment();
        segment.segmentId = increamentSegment();
        segment.baseRank = 1;
        segmentMap.put(segment.segmentId, segment);
        head.next = segment;
        tail.pre = segment;
    }

    public Ranker(int segmentMinSize, int segmentMaxSize) {
        this();
        if (segmentMinSize * 2 >= segmentMaxSize ||
                segmentMinSize < 0 ||
                segmentMaxSize < 0) {
            throw new IllegalArgumentException();
        }
        this.segmentMinSize = segmentMinSize;
        this.segmentMaxSize = segmentMaxSize;
    }

    private int increamentSegment() {
        segmentCount++;
        return segmentCount;
    }

    /**
     * 添加一个排名者
     * @param newer
     */
    public void add(T newer) {
        // 从排行榜末尾开始加
        Node node = new Node();
        node.rankable = newer;
        rankMap.put(newer, node);
        Segment s = tail.pre;
        s.add(node);
    }

    /**
     * 更新排名
     * @param u
     */
    public void update(T u) {
        if (rankMap.containsKey(u)) {
            Node node = rankMap.get(u);
            segmentMap.get(node.segmentId).update(node);
        }
    }

    /**
     * 移除
     * @param d
     */
    public void delete(T d) {
        if (rankMap.containsKey(d)) {
            Node node = rankMap.get(d);
            Segment s = segmentMap.get(node.segmentId);
            s.delete(node);
            rankMap.remove(d);
        }
    }

    /**
     * 获取排名
     * @param r
     * @return
     */
    public int getRank(T r) {
        Node node = rankMap.get(r);
        if (node == null) {
            return 0;
        }
        return segmentMap.get(node.segmentId).baseRank + node.insideRank;
    }

    /**
     * 获取前多少名
     * @param size
     * @return
     */
    public List<T> getRankList(int size) {
        List<T> list = new ArrayList<>();
        Segment s = head.next;
        while (s != null) {
            if (size <= 0) {
                break;
            }
            for (int i = 0; i < s.map.size(); i++) {
                list.add((T)s.map.get(i).rankable);
                size--;
                if (size <= 0) {
                    break;
                }
            }
            s = s.next;
        }
        return list;
    }

    private class Node {
        Rankable rankable;
        int segmentId;
        int insideRank;

        public int compare(Node newer) {
            return rankable.compare(newer.rankable);
        }
    }

    private class Segment {
        int segmentId;
        int baseRank;
        Segment pre;
        Segment next;
        Map<Integer, Node> map = new HashMap<>();

        void delete(Node d) {
            for (int i = d.insideRank; i < map.size() - 1; i++) {
                swap(i, i + 1);
            }
            map.remove(d.insideRank);
            Segment s = next;
            while (s != null) {
                s.baseRank--;
                s = s.next;
            }
            // 向前融合
            if (map.size() <= segmentMinSize && pre != null) {
                preMerge();
                if (pre.map.size() > segmentMaxSize) {
                    pre.divide();
                }
            }
        }

        void preMerge() {
            for (int i = 0; i < map.size(); i++) {
                Node tempNode = map.get(i);
                tempNode.segmentId = pre.segmentId;
                tempNode.insideRank = pre.map.size();
                pre.map.put(tempNode.insideRank, tempNode);
            }
            segmentMap.remove(segmentId);
            pre.next = next;
            if (next == null) {
                tail.pre = pre;
            } else {
                next.pre = pre;
            }
        }

        void update(Node node) {
            if (pre != null && node.compare(pre.map.get(pre.map.size() - 1)) > 0) {
                // 比前一段最后一名要大，跨段
                delete(node);
                if (pre.next != this) {
                    // 删除的时候发生融合
                    pre.next.add(node);
                } else {
                    pre.add(node);
                }
            } else if (next != null && node.compare(next.map.get(0)) < 0) {
                // 比后一段第一名还要小，跨段
                delete(node);
                // add 不会往后查找，这里手动查找到对应的段
                Segment temp = next;
                while (temp != null && node.compare(temp.map.get(0)) < 0 && temp.next != null) {
                    temp = temp.next;
                }
                temp.add(node);
            } else {
                if (map.get(node.insideRank - 1) != null && map.get(node.insideRank - 1).compare(node) < 0) {
                    for (int i = node.insideRank; i > 0; i--) {
                        if (map.get(i - 1).compare(map.get(i)) > 0) {
                            break;
                        } else {
                            swap(i - 1, i);
                        }
                    }
                } else if (map.get(node.insideRank + 1) != null && map.get(node.insideRank + 1).compare(node) > 0) {
                    for (int i = node.insideRank; i < map.size() - 1; i++) {
                        if (map.get(i).compare(map.get(i + 1)) > 0) {
                            break;
                        } else {
                            swap(i, i + 1);
                        }
                    }
                }
            }
        }

        void swap(int pre, int next) {
            Node temp = map.get(pre);
            map.put(pre, map.get(next));
            map.put(next, temp);
            map.get(pre).insideRank = pre;
            map.get(next).insideRank = next;
        }

        void add(Node newer) {
            if (pre != null && pre.map.get(pre.map.size() - 1).compare(newer) < 0) {
                // 有前一段，并且比前一段最后一名要大
                pre.add(newer);
            } else {
                // 先添加到最后一名
                newer.segmentId = segmentId;
                newer.insideRank = map.size();
                map.put(newer.insideRank, newer);
                for (int i = map.size() - 1; i > 0; i--) {
                    if (map.get(i - 1).compare(map.get(i)) > 0) {
                        // 有序
                        break;
                    } else {
                        swap(i - 1, i);
                    }
                }
                // 更新后续段的排名
                Segment temp = next;
                while (temp != null) {
                    temp.baseRank++;
                    temp = temp.next;
                }
                // 分裂
                if (map.size() > segmentMaxSize) {
                    divide();
                }
            }
        }

        // 分裂处理
        void divide() {
            int divide = map.size() / 2;
            Segment s = new Segment();
            s.segmentId = increamentSegment();
            s.baseRank = baseRank + divide;
            s.next = next;
            s.pre = this;
            if (s.next == null) {
                tail.pre = s;
            } else {
                next.pre = s;
            }
            next = s;
            segmentMap.put(s.segmentId, s);
            int size = map.size();
            for (int i = divide; i < size; i++) {
                Node node = map.get(i);
                map.remove(node.insideRank);
                s.map.put(i - divide, node);
                node.insideRank = i - divide;
                node.segmentId = s.segmentId;
            }
        }
    }

    public static void main(String[] args) {
        Ranker<Test> ranker = new Ranker<>(50, 200);
        final int size = 50000;
        Test[] tests = new Test[size];
        for (int i = 0; i < size; i++) {
            tests[i] = new Test(i);
            ranker.add(tests[i]);
        }
        Random random = new Random();
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            int randomInt = random.nextInt(size);
            int change = 50 - random.nextInt(100);
            tests[randomInt].add(change);
            ranker.update(tests[randomInt]);
        }
        ranker.check();
        List<Test> rank = ranker.getRankList(200);
        int test = ranker.getRank(tests[200]);
    }

    private void check() {
        Segment s = head.next;
        while (s != null) {
            for (int i = 0; i < s.map.size() - 1; i++) {
                if (s.map.get(i).compare(s.map.get(i + 1)) < 0) {
                    throw new RuntimeException("rank error");
                }
            }
            s = s.next;
        }
    }

    static class Test implements Rankable<Test> {
        int value;

        Test(int value) {
            this.value = value;
        }

        public void add(int add) {
            value += add;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }

        @Override
        public int compare(Test other) {
            return value - other.value;
        }
    }
}
