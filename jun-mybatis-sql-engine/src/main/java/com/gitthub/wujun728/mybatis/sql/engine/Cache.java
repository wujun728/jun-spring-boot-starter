package com.gitthub.wujun728.mybatis.sql.engine;

import java.util.concurrent.ConcurrentHashMap;

import com.gitthub.wujun728.mybatis.sql.node.SqlNode;


public class Cache {

    ConcurrentHashMap<String, SqlNode> nodeCache = new ConcurrentHashMap<>();

    public ConcurrentHashMap<String, SqlNode> getNodeCache() {
        return nodeCache;
    }
}
