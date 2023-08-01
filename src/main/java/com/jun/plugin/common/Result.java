package com.jun.plugin.common;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONField;
import com.jun.plugin.common.exception.code.BaseResponseCode;
import lombok.Data;

/**
 * 返回值DataResult
 *
 * @author wujun
 * @version V1.0
 * @date 2020年3月18日
 */
@Data
public class Result {
	

    /**
     * 请求响应code，0为成功 其他为失败
     */
    private int code;

    /**
     * 响应异常码详细信息
     */
    private String msg;

    @JSONField(serializeFeatures = {JSONWriter.Feature.WriteMapNullValue})
    private Object data;
    
//    boolean success;
 

    public Result(int code, Object data) {
        this.code = code;
        this.data = data;
        this.msg = null;
    }

    public Result(int code, String msg, Object data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public Result(int code, String msg) {
        this.code = code;
        this.msg = msg;
        this.data = null;
    }


    public Result() {
        this.code = 0;
        this.msg = "操作成功";
        this.data = null;
    }

    public Result(Object data) {
        this.data = data;
        this.code = 0;
        this.msg = "操作成功";
    }
    public Result(Object data, int code, String msg) {
        this.data = data;
        this.code = code;
        this.msg = msg;
    } 

    /**
     * 自定义返回  data为null
     */
    public static Result getResult(int code, String msg) {
        return new Result(code, msg);
    }
    public static Result getResult(BaseResponseCode code) {
        return new Result(code.getCode(), code.getMsg());
    }

    

    /**
     * 操作成功 data为null
     */
    public static Result success() {
        return new Result();
    }

    /**
     * 操作成功 data 不为null
     */
    public static Result success(Object data) {
        return new Result(data);
    }

    /**
     * 操作失败 data 不为null
     */
    public static Result fail(String msg) {
        return new Result(500002, msg);
    }
    
    public static Result successWithMsg(String msg) {
    	return new Result(null,0,msg);
    }

    public static Result successWithData(Object data) {
    	return new Result(data,0,"操作成功");
    }
    
    public static Result successWithDataMsg(Object data, String msg) {
    	return new Result(data,0,msg);
    }
    


}
