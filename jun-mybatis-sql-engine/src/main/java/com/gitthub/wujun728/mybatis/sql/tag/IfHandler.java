package com.gitthub.wujun728.mybatis.sql.tag;

import org.dom4j.Element;

import com.gitthub.wujun728.mybatis.sql.node.IfSqlNode;
import com.gitthub.wujun728.mybatis.sql.node.MixedSqlNode;
import com.gitthub.wujun728.mybatis.sql.node.SqlNode;

import java.util.List;


public class IfHandler implements TagHandler {

    @Override
    public void handle(Element element, List<SqlNode> targetContents) {
        String test = element.attributeValue("test");
        if (test == null) {
            throw new RuntimeException("<if> tag missing test attribute");
        }

        List<SqlNode> contents = XmlParser.parseElement(element);

        IfSqlNode ifSqlNode = new IfSqlNode(test, new MixedSqlNode(contents));
        targetContents.add(ifSqlNode);

    }
}
