package entity;

import io.github.wujun728.jdbc.annotation.Column;
import io.github.wujun728.jdbc.annotation.Table;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;


@Table("test")
@Data
public class Test11 implements Serializable {
        private static final long serialVersionUID = -1L;

        /**
         * 表的主键
         * 注意，如果设置为自增主键的话，则此字段必须为Long
         * 如果设置为uuid的话，则此字段必须为String
         * 如果设置为objectId的话，则此字段必须为String
         * 如果设置为assignId的话，则此字段必须为String或者Long
         */
        @Column(value = "id", primaryKey = true, assignId = true)
        private Long id;

        /**
         * 文件id
         */
        @Column("title")
        private String title;
        @Column("content")
        private String content333;

        /**
         * 文件原名称
         */
        @Column("field_name_test")
        private String fieldNametest;

        /**
         * 上传时间
         */
        @Column("create_time")
        private Date createTime;


}
