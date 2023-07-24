package com.gitthub.wujun728.engine.base;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.util.List;

/**
 * BaseEntity
 *
 * @author wujun
 * @version V1.0
 * @date 2020年3月18日
 */
@Data
public class BaseEntity {
	// -------------------------------------分页信息----------------------------------------------
    @JSONField(serialize = false)
    @TableField(exist = false)
    private int page = 1;

    @JSONField(serialize = false)
    @TableField(exist = false)
    private int limit = 10;
    
    @JSONField(serialize = false)
    @TableField(exist = false)
    private String keyword;

    // -------------------------------------数据权限----------------------------------------------
    /**
     * 数据权限：用户id
     */
    @TableField(exist = false)
    private List<String> createIds;
    
	@TableField(exist = false)
	private Integer isOwner;
}
