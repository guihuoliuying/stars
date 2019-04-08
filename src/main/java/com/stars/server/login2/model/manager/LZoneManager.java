package com.stars.server.login2.model.manager;

import com.stars.server.login2.model.pojo.LZone;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by zhaowenshuo on 2016/2/19.
 */
public class LZoneManager {

    private static ConcurrentMap<Integer, LZone> zoneMap = new ConcurrentHashMap<>();

//    static {
//        for (int i = 1; i <= 3; i++) {
//            LZone zone = new LZone();
//            zone.setId(i);
//            zone.setServerList(new ArrayList<>());
//            zone.getServerList().add(new LZoneServer(1, "", new InetSocketAddress("127.0.0.1", 7090)));
//            zoneMap.putIfAbsent(i, zone);
//        }
//    }

    // todo: need set method

    public static LZone get(int id) {
        return zoneMap.get(id);
    }

    public static Iterator<LZone> zoneIterator() {
        return zoneMap.values().iterator();
    }

    public static Collection<LZone> zones() {
        return zoneMap.values();
    }

    public static void loadData() {
//        Connection conn = null;
//        Statement stmt = null;
//        ResultSet rs = null;
//        ConcurrentMap<Integer, LZone> zoneMap = new ConcurrentHashMap<>();
//        try {
//            conn = DbUtil.getConn(99);
//            stmt = conn.createStatement();
//            stmt.setFetchSize(50_000);
//            /* 加载game_zone */
//            rs = stmt.executeQuery("select * from `game_zone`");
//            while (rs.next()) {
//                int zoneId = rs.getInt(1);
//                String zoneName = rs.getString(2);
//                LZone zone = new LZone();
//                zone.setId(zoneId);
//                zone.setName(zoneName);
//                zone.setServerList(new ArrayList<LZoneServer>());
//                if (zoneMap.putIfAbsent(zone.getId(), zone) != null) {
//                	LogUtil.info("存在重复配置项");
//                }
//            }
//            close(rs);
//            /* 加载game_zone_server */
//            rs = stmt.executeQuery("select * from `game_zone_server`");
//            while (rs.next()) {
//                int id = rs.getInt("id");
//                int zoneId = rs.getInt("zone_id");
//                String ip = rs.getString("ip");
//                int port = rs.getInt("port");
//                LZoneServer server = new LZoneServer(id, "", new InetSocketAddress(ip, port));
//                LZone zone = zoneMap.get(zoneId);
//                if (zone != null) {
//                    zone.addServer(server);
//                } else {
//                	LogUtil.info("存在没有对应大区的服务");
//                }
//            }
//
//            LZoneManager.zoneMap = zoneMap;
//        } catch (SQLException e) {
//            e.printStackTrace();
//        } finally {
//            close(rs);
//            close(stmt);
//            close(conn);
//        }
    }

    private static void close(AutoCloseable closeable) {
        if (closeable != null)  {
            try {
                closeable.close();
            } catch (Exception e) {

            }
        }
    }

}
