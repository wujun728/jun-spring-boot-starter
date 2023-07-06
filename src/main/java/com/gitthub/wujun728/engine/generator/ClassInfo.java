package com.gitthub.wujun728.engine.generator;

import lombok.Data;

import java.util.List;

/**
 * class info
 *
 */
@Data
public class ClassInfo {
    private String tableName;
    private String className;
	private String classComment;
	private int pkSize;
	private List<FieldInfo> fieldList;

}