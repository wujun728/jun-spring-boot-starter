package com.jun.plugin.generator;

import lombok.Data;

/**
 * field info
 *
 */
@Data
public class FieldInfo {
    private String columnName;
    private String columnType;
    private String fieldName;
    private String fieldClass;
    private String fieldType;
    private String fieldComment;
    private Boolean isPrimaryKey;
    private Boolean isAutoIncrement;
    private long columnSize;
    private Boolean nullable;
    private Boolean comment;
    private String defaultValue;

}
