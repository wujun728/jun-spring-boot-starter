package com.gitthub.wujun728.mybatis.sql.tag;

import org.dom4j.Element;

import com.gitthub.wujun728.mybatis.sql.node.MixedSqlNode;
import com.gitthub.wujun728.mybatis.sql.node.SqlNode;
import com.gitthub.wujun728.mybatis.sql.node.WhereSqlNode;

import java.util.List;


public class WhereHandler implements TagHandler{

    @Override
    public void handle(Element element, List<SqlNode> targetContents) {
        List<SqlNode> contents = XmlParser.parseElement(element);

        WhereSqlNode node = new WhereSqlNode(new MixedSqlNode(contents));
        targetContents.add(node);
    }
}
