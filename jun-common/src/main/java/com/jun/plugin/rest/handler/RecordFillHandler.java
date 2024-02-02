package com.jun.plugin.rest.handler;

import cn.hutool.db.meta.Table;
import com.jun.plugin.db.record.Record;

public interface RecordFillHandler {

    default boolean openInsertFill() {
        return true;
    }

    default boolean openUpdateFill() {
        return true;
    }

    void insertFill(Table table, Record record);

    void updateFill(Table table, Record record);
}
