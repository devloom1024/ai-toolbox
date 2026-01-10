package com.devloom.ai.toolbox.common.config;

import com.devloom.ai.toolbox.auth.service.support.AuthProperties;

import java.net.InetSocketAddress;
import java.net.Proxy;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate 配置类
 *
 * @author DevLoom Team
 */
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean(name = "proxiedRestTemplate")
    public RestTemplate proxiedRestTemplate(AuthProperties authProperties) {
        RestTemplate restTemplate = new RestTemplate();
        AuthProperties.ProxyProperties proxy = authProperties.getLinuxdo().getProxy();

        if (proxy.isEnabled() && proxy.getPort() > 0) {
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            Proxy httpProxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxy.getHost(), proxy.getPort()));
            factory.setProxy(httpProxy);
            restTemplate.setRequestFactory(factory);
        }

        return restTemplate;
    }
}
