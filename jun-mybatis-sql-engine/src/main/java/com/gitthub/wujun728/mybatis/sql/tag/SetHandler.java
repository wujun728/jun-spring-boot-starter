package com.gitthub.wujun728.mybatis.sql.tag;

import org.dom4j.Element;

import com.gitthub.wujun728.mybatis.sql.node.MixedSqlNode;
import com.gitthub.wujun728.mybatis.sql.node.SetSqlNode;
import com.gitthub.wujun728.mybatis.sql.node.SqlNode;

import java.util.List;


public class SetHandler implements TagHandler{

    @Override
    public void handle(Element element, List<SqlNode> targetContents) {
        List<SqlNode> contents = XmlParser.parseElement(element);

        SetSqlNode node = new SetSqlNode(new MixedSqlNode(contents));
        targetContents.add(node);
    }
}
