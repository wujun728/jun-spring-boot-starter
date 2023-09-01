package com.gitthub.wujun728.mybatis.sql.tag;

import org.dom4j.Element;

import com.gitthub.wujun728.mybatis.sql.node.SqlNode;

import java.util.List;

public interface TagHandler {

    void handle(Element element, List<SqlNode> contents);
}
