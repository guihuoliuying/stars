package com.stars.util.ranklist;

import java.util.*;

/**
 * 为了链表加上一个索引（数组）
 * 例子1
 * size=4, interval=2
 * | 1 | 2 |    // 索引
 *   |   |
 *   |   +-------------------+
 *   |                       |
 * (Node 1) -> (Node 2) -> (Node 3) -> (Node 4)    // 链表
 *
 * 例子2
 * size=5, interval=2
 * | 1 | 2 | 3 |
 *   |   |   |
 *   |   |   +---------------------------------------+
 *   |   |                                           |
 *   |   +-------------------+                       |
 *   |                       |                       |
 * (Node 1) -> (Node 2) -> (Node 3) -> (Node 4) -> (Node 5)
 *
 * 查找：getRank
 * 1. 现在索引找（二分查找），定位到链表的结点
 * 2. 顺着链表继续找
 *
 * 增加：addRank
 * 1. 查找需要插入的位置前一个结点，插入新的排名对象（注意头结点的情况）
 * 2. 调整索引
 * 3. 删除尾结点
 *
 * 更新：updateRank
 * 1. 删除旧的排名对象（先找再删，并更新索引）
 * 2. 增加新的排名对象
 *
 * Created by zhaowenshuo on 2016/4/1.
 */
public class IndexList {

    private int size = 12;
    private int interval = 3;
    private long min = -100;

    private Index[] indexes; // 索引数组
    private Node head; // 头指针
    private Node tail; // 尾指针
    private Map<String, Node> nodeMap = new HashMap<>(); //

    private Comparator<Object> indexComparator = new Comparator<Object>() {
        @Override
        public int compare(Object o1, Object o2) {
            Index i1 = (Index) o1;
            Index i2 = (Index) o2;

            if (i1.val.getPoints() > i2.val.getPoints()) {
                return -1;
            }
            if (i1.val.getPoints() < i2.val.getPoints()) {
                return 1;
            }
            return 0;
        }
};

    public IndexList(int size, int interval, int min) {
        this.size = size;
        this.interval = interval;
        this.min = min;
        init();
    }

    public IndexList() {
        init();
    }

    /**
     * 初始化
     */
    private void init() {
        Node prev = null;
        indexes = new Index[size / interval + (size % interval == 0 ? 0 : 1)];
        nodeMap = new HashMap<>(size);
        // 初始化链表
        for (int i = size -1; i >= 0; i--) {
            Node node = new Node(new RankObj(Long.toString(min + i), min + i));
            if (head == null) {
                head = node;
            } else {
                prev.next = node;
                node.prev = prev;
            }
            prev = node;
            nodeMap.put(node.key, node);
        }
        tail = prev;
        // 初始化索引
        Node curr = head;
        for (int i = 0; i < size; i++) {
            if (i % interval == 0) {
                Index index = new Index(curr.key, curr);
                index.child = curr;
                curr.parent = index;
                indexes[i / interval] = index;
                if (i / interval > 0) {
                    indexes[i / interval -1].next = indexes[i / interval];
                }
            }

            curr = curr.next;
        }
    }

    private int getIndex(RankObj val) {
        return Arrays.binarySearch(indexes, new Index(val, null), indexComparator);
    }

    /**
     * 根据分值获取排名
     * @param key
     * @param val
     * @return
     */
    public int getRank(String key) {
        if (!nodeMap.containsKey(key)) {
            return -1;
        }
        RankObj val = nodeMap.get(key).val;
        Node temp = null;
        int idx = getIndex(val);
        if (idx >= 0) {
            temp = indexes[idx].child;
        } else {
            idx = -idx - 1; // 查找的元素应排在第几
            if (idx >= indexes.length) {
                idx = indexes.length - 1;
            }
            temp = indexes[idx].child;
        }
        // 先向前找，防止出现相同积分的情况
        int count = 1;
        while (temp.prev != null && temp.prev.val.getPoints() <= val.getPoints()) {
            temp = temp.prev;
            count--;
        }
        // 正式开始找
        do {
            if (temp.val.getPoints() == val.getPoints() && val.compareTo(temp.val) == 0) {
                return idx * interval + count;
            }
            temp = temp.next;
            count++;
        } while (temp != null && val.getPoints() <= temp.val.getPoints());
        return -1;
    }

    /**
     * 查找结点
     * @param key
     * @param val
     * @return
     */
    public Node findNode(String key, RankObj val) {
        if (val.getPoints() < tail.val.getPoints()) {
            return tail;
        }
        if (val.getPoints() > head.val.getPoints()
                || (val.getPoints() == head.val.getPoints() && val.compareTo(head.val) < 0)) {
            return null;
        }
        Node temp = null;
        int idx = getIndex(val);
        if (idx >= 0) {
            temp = indexes[idx].child;
        } else {
            idx = -idx - 1; // 查找的元素应排在第几
            if (idx < indexes.length) {
                temp = indexes[idx].child;
            } else {
                temp = indexes[idx-1].child;
            }
        }
        // 先向前找，防止出现相同积分的情况
        while (temp.prev != null && temp.val.getPoints() <= val.getPoints()) {
            temp = temp.prev;
        }
        // 正式开始找
        Node next = temp.next;
        while (next != null && (val.getPoints() < next.val.getPoints() || (val.getPoints() == next.val.getPoints() && val.compareTo(next.val) > 0))) {
            temp = next;
            next = next.next;
        }

        return temp;
    }

    public boolean containsRank(String key) {
        return nodeMap.containsKey(key);
    }

    public RankObj addRank(String key, RankObj val) {
        Node node = new Node(key, val);
        Node prev = findNode(key, val);
        // 增加节点
        if (prev == null) { // 头结点
            node.next = head;
            node.parent = head.parent;
            node.parent.key = key;
            node.parent.val = val;
            node.parent.child = node;
            head.prev = node;
            head.parent = null;
            head = node;
        } else if (prev.next == null) { // 尾结点
            // 如果为尾结点，表示加不进去
//            prev.next = node;
//            node.prev = prev;
//            tail = node;
            return null;
        } else {
            prev.next.prev = node;
            node.next = prev.next;
            prev.next = node;
            node.prev = prev;
        }
        Node tempNode = node;
        // 调整索引
        while (tempNode.parent == null) {
        	tempNode = tempNode.prev;
        }
        Index index = tempNode.parent.next;
    	while (index != null) {
    		Node child = index.child;
    		child.parent = null;
    		index.child = child.prev; // 前一个
    		index.key = index.child.key;
    		index.val = index.child.val;
    		index.child.parent = index;
    		index = index.next;
    	}
        Node victim = null;
        if (nodeMap.size() >= size) {
            victim = tail;
            tail = tail.prev;
            tail.next = null;
        }

        nodeMap.put(key, node);
        if (victim != null) {
            nodeMap.remove(victim.key);
            return victim.val;
        }
        return null;
    }

    /**
     * 1. 先移除原来的排名
     * 2. 再加上更新的排名
     * @param oldKey
     * @param oldVal
     * @param newKey
     * @param newVal
     */
    public void updateRank(String key, long points) {
        Node oldNode = nodeMap.get(key);
        Node delNode = oldNode;
        if (oldNode != null && oldNode.next != null) {
            // 删除结点
            if (delNode == head) { // 头结点
                head = delNode.next;
                head.prev = null;

                head.parent = indexes[0];
                indexes[0].child = head;
                indexes[0].key = head.key;
                indexes[0].val = head.val;
                oldNode = head;
            } else {
            	if (delNode.prev != null) {
            		delNode.prev.next = delNode.next;
            	}
            	delNode.next.prev = delNode.prev;

                if (delNode.parent != null) { // 如果存在索引则更新索引
                    delNode.next.parent = delNode.parent;
                    delNode.next.parent.child = delNode.next;
                    delNode.next.parent.key = delNode.next.key;
                    delNode.next.parent.val = delNode.next.val;
                }
                oldNode = delNode.next;
            }

            // 调整结点
            while (oldNode.parent == null) {
                oldNode = oldNode.prev;
            }
            Index index = oldNode.parent.next;
            while (index != null) {
                Node child = index.child;
                child.parent = null;
                index.child = child.next; // 下一个
                if (index.child != null) {
                    index.key = index.child.key;
                    index.val = index.child.val;
                    index.child.parent = index;
                }
                index = index.next;
            }

            // 增加排名
            nodeMap.remove(key);
            delNode.val.points = points;
            addRank(key, delNode.val);
            return;
        }
    }



    /**
     * 获取前100个链表结点
     * @return
     */
    public List<RankObj> getTop(int n) {
        List<RankObj> list = new ArrayList<>();
        Node node = head;
        for (int i = 0; i < n; i++) {
            if (node == null || node.val == null || node.val.getPoints() < 0) {
                break;
            }
            list.add(node.val);
            node = node.next;
        }
        return list;
    }

    /**
     * 获取全部链表结点
     * @return
     */
    public List<RankObj> getAll() {
        List<RankObj> list = new ArrayList<>();
        Node node = head;
        for (int i = 0; i < size; i++) {
            if (node == null || node.val == null || node.val.getPoints() < 0) {
                break;
            }
            list.add(node.val);
            node = node.next;
        }
        return list;
    }
    
    /**
     * 获得排名信息
     * @param key
     * @return
     */
    public RankObj getRankObjByKey(String key) {
    	if (!nodeMap.containsKey(key)) return null;
    	return nodeMap.get(key).val;
    }

    public String toString() {
        String str = "[ ";
        Node curr = head;
        while (curr != null) {
            str += curr + ", ";
            curr = curr.next;
        }
        str += " ]";
        return "IndexList = { indexes = " + Arrays.toString(indexes) + ", list = " + str + " }";
    }

    public static void main(String[] args) {
        IndexList list = new IndexList(50000, 150, -50000);
        int size = 1000000;
        long s = System.currentTimeMillis();
        for (int i = 0; i < size; i++) {
            list.addRank("" + i, new RankObj("" + i, i));
        }
        long e = System.currentTimeMillis();


//        s = System.currentTimeMillis();
        Random random = new Random();
//        for (int i = 0; i < size; i++) {
//            int index = random.nextInt(size);
//            int index2 = index + size;
//            list.updateRank("" + index, index2);
//        }
//        e = System.currentTimeMillis();

        s = System.currentTimeMillis();
        int size2 = size + size;
//        int size2 = size;
        RankObj rankObj = null;
        int rank = 0;
        for (int i = 0; i < size; i++) {
//            int index = random.nextInt(size2);
            rank = list.getRank("" + (i-49999));
        }
        e = System.currentTimeMillis();
    }

}

/**
 * 索引结点
 */
class Index {
    String key; // 键
    RankObj val; // 值
    Node child; // 子结点（链表结点）
    Index next; // 后一个结点

    public Index(String key, Node child) {
        this.key = key;
        this.child = child;
    }

    public Index(RankObj val, Node child) {
        this.val = val;
        this.child = child;
    }

    @Override
    public String toString() {
        return "Index{" +
                "key=" + key +
                '}';
    }
}

/**
 * 链表结点
 */
class Node {
    String key; // 键
    RankObj val; // 值

    Node prev; // 前一个结点
    Node next; // 后一个结点
    Index parent; // 父结点（索引）

    public Node(String key, RankObj val) {
        this.key = key;
        this.val = val;
    }

    public Node(RankObj val) {
        this.key = val.getKey();
        this.val = val;
    }

    @Override
    public String toString() {
        return "Node{key=" + key + ";val=" + val.getPoints() + "}";
    }
}


