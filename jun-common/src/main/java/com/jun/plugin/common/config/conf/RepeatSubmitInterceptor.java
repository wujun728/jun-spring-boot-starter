//package com.jun.plugin.common.config.conf;
//
//import com.alibaba.fastjson2.JSONObject;
//import com.jun.plugin.common.aop.annotation.RepeatSubmit;
//import com.jun.plugin.common.utils.DataResult;
//import org.springframework.stereotype.Component;
//import org.springframework.web.method.HandlerMethod;
//import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//import java.lang.reflect.Method;
//
///**
// * 防止重复提交拦截器
// *
// * @author ruoyi
// */
//@Component
//public abstract class RepeatSubmitInterceptor extends HandlerInterceptorAdapter
//{
//    @Override
//    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception
//    {
//        if (handler instanceof HandlerMethod)
//        {
//            HandlerMethod handlerMethod = (HandlerMethod) handler;
//            Method method = handlerMethod.getMethod();
//            RepeatSubmit annotation = method.getAnnotation(RepeatSubmit.class);
//            if (annotation != null)
//            {
//                if (this.isRepeatSubmit(request))
//                {
//                    DataResult dataResult = DataResult.fail("不允许重复提交，请稍后再试");
//                    renderString(response, JSONObject.toJSONString(dataResult));
//                    return false;
//                }
//            }
//            return true;
//        }
//        else
//        {
//            return super.preHandle(request, response, handler);
//        }
//    }
//
//    /**
//     * 验证是否重复提交由子类实现具体的防重复提交的规则
//     *
//     * @return
//     * @throws Exception
//     */
//    public abstract boolean isRepeatSubmit(HttpServletRequest request);
//
//
//    public static String renderString(HttpServletResponse response, String string)
//    {
//        try
//        {
//            response.setContentType("application/json");
//            response.setCharacterEncoding("utf-8");
//            response.getWriter().print(string);
//        }
//        catch (IOException e)
//        {
//            e.printStackTrace();
//        }
//        return null;
//    }
//}
