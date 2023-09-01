package com.gitthub.wujun728.mybatis.sql.node;

import java.util.Set;

import com.gitthub.wujun728.mybatis.sql.context.Context;


public interface SqlNode {

    void apply(Context context);

    void applyParameter(Set<String> set);

}
