package com.jun.plugin.common.util;

import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;

import lombok.extern.slf4j.Slf4j;


@Slf4j
public class DbPoolManager {

    private static Lock lock = new ReentrantLock();

    private static Lock deleteLock = new ReentrantLock();

    //所有数据源的连接池存在map里
    static ConcurrentHashMap<String, DruidDataSource> map = new ConcurrentHashMap<>();

    public static DruidDataSource init(String dsname,String url,String username,String password,String driver) {
        if (map.containsKey(dsname)) {
            return map.get(dsname);
        } else {
            lock.lock();
            try {
                log.info(Thread.currentThread().getName() + "获取锁");
                if (!map.containsKey(dsname)) {
                    DruidDataSource druidDataSource = new DruidDataSource();
                    druidDataSource.setName(dsname);
                    druidDataSource.setUrl(url);
                    druidDataSource.setUsername(username);
                    druidDataSource.setPassword(password);
                    druidDataSource.setDriverClassName(driver);
                    druidDataSource.setConnectionErrorRetryAttempts(3);       //失败后重连次数
                    druidDataSource.setBreakAfterAcquireFailure(true);
                    map.put(dsname, druidDataSource);
                    log.info("创建Druid连接池成功：{}", dsname);
                }
                return map.get(dsname);
            } catch (Exception e) {
                return null;
            } finally {
                lock.unlock();
            }
        }
    }
    public static Boolean exitst(String dsname) {
        if (map.containsKey(dsname)) {
            return true;
        } else {
            return false;
        }
    }
    public static DruidDataSource get(String dsname) {
        if (map.containsKey(dsname)) {
            return map.get(dsname);
        } else {
            return null;
        }
    }

    //删除数据库连接池
    public static void remove(String dsname) {
        deleteLock.lock();
        try {
            DruidDataSource druidDataSource = map.get(dsname);
            if (druidDataSource != null) {
                druidDataSource.close();
                map.remove(dsname);
            }
        } catch (Exception e) {
            log.error(e.toString());
        } finally {
            deleteLock.unlock();
        }

    }

    public static DruidPooledConnection getPooledConnection(String dsname) throws SQLException {
        DruidDataSource pool = DbPoolManager.get(dsname);
        DruidPooledConnection connection = pool.getConnection();
//        log.info("获取连接成功");
        return connection;
    }
}
