package com.stars.services.postsync.aoi;

import com.stars.util.LogUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/6/23.
 */
public class AoiScene {

    Map<Long, AoiObject> objMap = new HashMap<>();

    String id;
    byte type;
    double r; // 检测半径
    double powOfR;

    AoiObject head = new AoiObject(-1, Double.MIN_VALUE, Double.MIN_VALUE);
    AoiObject tail = new AoiObject(-2, Double.MIN_VALUE, Double.MIN_VALUE);

    public AoiScene(String id, byte type, double r) {

        this.id = id;
        this.type = type;

        this.r = r;
        this.powOfR = r * r;
        head.xNext = tail;
        head.zNext = tail;

        tail.xPrev = head;
        tail.zPrev = head;
    }

    public String sceneId() {
        return id;
    }

    public byte sceneType() {
        return type;
    }

    public AoiObject aoiObject(long roleId) {
        return objMap.get(roleId);
    }

    public void enter(long roleId, double x, double z) {
        if (objMap.containsKey(roleId)) {
            update(roleId, x, z);
        } else {
            AoiObject node = new AoiObject(roleId, x, z);
            objMap.put(roleId, node);
            AoiObject cur = null;
            // x轴
            cur = head.xNext;
            while (cur != null) {
                if (cur.x > node.x || cur == tail) {
                    node.xNext = cur;
                    node.xPrev = cur.xPrev;
                    cur.xPrev.xNext = node;
                    cur.xPrev = node;
                    break;
                }
                cur = cur.xNext;
            }
            // z轴
            cur = head.zNext;
            while (cur != null) {
                if (cur.z > node.z || cur == tail) {
                    node.zNext = cur;
                    node.zPrev = cur.zPrev;
                    cur.zPrev.zNext = node;
                    cur.zPrev = node;
                    break;
                }
                cur = cur.zNext;
            }
        }
    }

    public void leave(long roleId) {
        AoiObject node = objMap.remove(roleId);
        if (node == null) {
            return;
        }
        node.xPrev.xNext = node.xNext;
        node.xNext.xPrev = node.xPrev;
        node.zPrev.zNext = node.zNext;
        node.zNext.zPrev = node.zPrev;

        node.xPrev = null;
        node.xNext = null;
        node.zPrev = null;
        node.zNext = null;
    }

    // fixme: need improve
    public void update(long roleId, double x, double z) {

        AoiObject node = objMap.get(roleId);
        if (node == null) {
            return;
        }

        if (x != node.x) {
            if (x > node.x) {
                AoiObject tmp = node.xNext;
                deleteAtX(node);
                while (tmp != null) {
                    if (x < tmp.x || tmp == tail) {
                        node.xPrev = tmp.xPrev;
                        node.xNext = tmp;
                        tmp.xPrev.xNext = node;
                        tmp.xPrev = node;
                        break;
                    }
                    tmp = tmp.xNext;
                }
            } else {
                AoiObject tmp = node.xPrev;
                deleteAtX(node);
                while (tmp != null) {
                    if (x > tmp.x || tmp == head) {
                        node.xPrev = tmp;
                        node.xNext = tmp.xNext;
                        tmp.xNext.xPrev = node;
                        tmp.xNext = node;
                        break;
                    }
                    tmp = tmp.xPrev;
                }
            }
        }

        if (z != node.z) {
            if (z > node.z) {
                AoiObject tmp = node.zNext;
                deleteAtZ(node);
                while (tmp != null) {
                    if (z < tmp.z || tmp == tail) {
                        node.zPrev = tmp.zPrev;
                        node.zNext = tmp;
                        tmp.zPrev.zNext = node;
                        tmp.zPrev = node;
                        break;
                    }
                    tmp = tmp.zNext;
                }
            } else {
                AoiObject tmp = node.zPrev;
                deleteAtZ(node);
                while (tmp != null) {
                    if (z > tmp.z || tmp == head) {
                        node.zPrev = tmp;
                        node.zNext = tmp.zNext;
                        tmp.zNext.zPrev = node;
                        tmp.zNext = node;
                        break;
                    }
                    tmp = tmp.zPrev;
                }
            }
        }

        node.x = x;
        node.z = z;
    }

    private void deleteAtX(AoiObject node) {
        node.xPrev.xNext = node.xNext;
        node.xNext.xPrev = node.xPrev;
        node.xPrev = null;
        node.xNext = null;
    }

    private void deleteAtZ(AoiObject node) {
        node.zPrev.zNext = node.zNext;
        node.zNext.zPrev = node.zPrev;
        node.zPrev = null;
        node.zNext = null;
    }

    public void check(AoiCallback callback) {

        for (AoiObject node : objMap.values()) {
            List<AoiObject> list = new ArrayList<>();
            AoiObject cur = null;
            // 往前
            cur = node.xPrev;
            while (cur != head) {
                double xDelta = node.x - cur.x;
                if (xDelta > r) {
                    break;
                }
                double zDelta = node.z - cur.z;
                if (xDelta * xDelta + zDelta * zDelta <= powOfR) {
                    list.add(cur);
                }
                cur = cur.xPrev;
            }
            // 往后
            cur = node.xNext;
            while (cur != tail) {
                double xDelta = cur.x - node.x;
                if (xDelta > r) {
                    break;
                }
                double zDelta = cur.z - node.z;
                if (xDelta * xDelta + zDelta * zDelta <= powOfR) {
                    list.add(cur);
                }
                cur = cur.xNext;
            }
            try {
                callback.exec(this, node.roleId, list);
            } catch (Throwable cause) {
                LogUtil.error("", cause);
            }
        }
    }

    @Override
    public String toString() {
        return Integer.toString(objMap.size());
    }

    public static void main(String[] args) {
        AoiScene scene = new AoiScene("", (byte) 0, 500);
//        scene.enter(1, 500, 500);
//        scene.enter(2, 550, 500);
//        scene.enter(3, 500, 550);
//        scene.enter(4, 535, 535);
//        scene.enter(5, 536, 536);
//        scene.enter(6, 535, 536);
//        scene.enter(7, 534, 536);

        for (int i = 0; i < 2000; i++) {
            scene.enter(i, 500, 500);
        }

        AoiCallback callback = new AoiCallback() {
            @Override
            public void exec(AoiScene scene, long roleId, List<AoiObject> aoiObjectList) {
//                System.out.println(roleId + ":" + aoiObjectList);
            }
        };
//        scene.check(callback);
        while (true) {
            long s = System.currentTimeMillis();
            scene.check(callback);
            long e = System.currentTimeMillis();
            System.out.println("elapsed: " + (e - s) / 1000.0);
            try {
                Thread.sleep(1000);
            } catch (Exception ex) {

            }
        }
    }

}
