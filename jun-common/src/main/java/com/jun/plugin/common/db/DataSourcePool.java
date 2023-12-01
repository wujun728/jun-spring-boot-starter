package com.jun.plugin.common.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import cn.hutool.core.lang.Console;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.druid.pool.DruidDataSource;

import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.sql.DataSource;


@Slf4j
public class DataSourcePool {

    private static Lock lock = new ReentrantLock();

    private static Lock deleteLock = new ReentrantLock();

    //所有数据源的连接池存在map里
    static ConcurrentHashMap<String, DataSource> map = new ConcurrentHashMap<>();

    static ConcurrentHashMap<String, ActiveRecordPlugin> configmaps = new ConcurrentHashMap<>();

    public static DataSource init(String dsname,String url,String username,String password,String driver) {
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
    public static void add(String dsname,DataSource dataSource) {
            lock.lock();
            try {
                log.info(Thread.currentThread().getName() + "获取锁");
                map.put(dsname, dataSource);
                log.info("添加连接池成功：{}", dsname);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
    }
    public static DataSource get(String dsname) {
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
            DataSource druidDataSource = map.get(dsname);
            if (druidDataSource != null) {
                //druidDataSource.close();
                if(druidDataSource instanceof  DruidDataSource){
                    ((DruidDataSource)druidDataSource).close();
                }
                map.remove(dsname);
            }
        } catch (Exception e) {
            log.error(e.toString());
        } finally {
            deleteLock.unlock();
        }
    }

    public static Connection getConnection(String dsname) throws SQLException {
        return DataSourcePool.get(dsname).getConnection();
    }


    public static ActiveRecordPlugin getActiveRecordPlugin(String dsname) {
        if (configmaps.containsKey(dsname)) {
            return configmaps.get(dsname);
        } else {
            return null;
        }
    }

    public static String main = "main";

    public static ActiveRecordPlugin initActiveRecordPlugin(String configName,DataSource dataSource) {
        if (configmaps.containsKey(configName)) {
            log.warn("Config have bean created by configName: {}",configName);
            return configmaps.get(configName);
        } else {
            lock.lock();
            try {
                log.info(Thread.currentThread().getName() + "获取锁");
                if (!configmaps.containsKey(configName)) {
                    //DataSource ds = DataSourcePool.get(configName);
                    //DruidPlugin dp = new DruidPlugin(url, username, password);
                    ActiveRecordPlugin arp = new ActiveRecordPlugin(configName, dataSource);
                    arp.setDevMode(true);
                    arp.setShowSql(true);
                    //dp.start();
                    arp.start();
                    log.warn("Config have bean created by configName: {}",configName);
                    configmaps.put(configName, arp);
                    log.info("创建Druid连接池成功：{}", configName);
                }
            } catch (Exception e) {
                return null;
            } finally {
                lock.unlock();
            }
        }
        return configmaps.get(configName);
    }
}
