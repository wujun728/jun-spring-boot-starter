package com.gitthub.wujun728.mybatis.sql.tag;

import org.dom4j.Element;

import com.gitthub.wujun728.mybatis.sql.node.MixedSqlNode;
import com.gitthub.wujun728.mybatis.sql.node.SqlNode;
import com.gitthub.wujun728.mybatis.sql.node.TrimSqlNode;

import java.util.Arrays;
import java.util.List;


public class TrimHandler implements TagHandler {

    @Override
    public void handle(Element element, List<SqlNode> targetContents) {
        String prefix = element.attributeValue("prefix");
        String suffix = element.attributeValue("suffix");
        String prefixesToOverride = element.attributeValue("prefixesToOverride");
        List<String> prefixesOverride = prefixesToOverride == null ? null : Arrays.asList(prefixesToOverride.split("\\|"));
        String suffixesToOverride = element.attributeValue("suffixesToOverride");
        List<String> suffixesOverride = suffixesToOverride == null ? null : Arrays.asList(suffixesToOverride.split("\\|"));

        List<SqlNode> contents = XmlParser.parseElement(element);
        TrimSqlNode trimSqlNode = new TrimSqlNode(new MixedSqlNode(contents), prefix, suffix, prefixesOverride, suffixesOverride);
        targetContents.add(trimSqlNode);
    }
}
