package com.jun.plugin.framework.config;

import javax.servlet.http.HttpServletRequest;

import cn.hutool.extra.spring.SpringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.jun.plugin.common.utils.ServletUtils;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 服务相关配置
 * 
 * @author ruoyi
 *
 */
@Slf4j
@Component
public class ServerConfig implements ApplicationListener<WebServerInitializedEvent> {

    @Override
    public void onApplicationEvent(WebServerInitializedEvent event) {
        try {
            InetAddress inetAddress = Inet4Address.getLocalHost();
            String hostAddress = inetAddress.getHostAddress();
            int serverPort = event.getWebServer().getPort();
            String serverPath = event.getApplicationContext().getApplicationName();
            log.info("项目启动成功！访问地址: http://{}:{}{}", hostAddress, serverPort, serverPath);
            log.info("本机地址: http://localhost:{}{}", serverPort, serverPath);

            Environment env = SpringUtil.getApplicationContext().getEnvironment();
            log.info("\n----------------------------------------------------------\n\t" +
                            "SpringbootApplication '{}' is running! Access URLs:\n\t" +
                            "Login: \thttp://{}:{}/\n\t" +
                            "----------------------------------------------------------",
                    env.getProperty("spring.application.name"),
                    InetAddress.getLocalHost().getHostAddress(),
                    env.getProperty("server.port"),
                    InetAddress.getLocalHost().getHostAddress(),
                    env.getProperty("server.port"));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
    /**
     * 获取完整的请求路径，包括：域名，端口，上下文访问路径
     * 
     * @return 服务地址
     */
    public String getUrl()
    {
        HttpServletRequest request = ServletUtils.getRequest();
        return getDomain(request);
    }

    public static String getDomain(HttpServletRequest request)
    {
        StringBuffer url = request.getRequestURL();
        String contextPath = request.getServletContext().getContextPath();
        return url.delete(url.length() - request.getRequestURI().length(), url.length()).append(contextPath).toString();
    }
}
