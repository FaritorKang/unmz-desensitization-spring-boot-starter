package net.unmz.java.desensitization.config;

import net.unmz.java.desensitization.interceptor.DesensitizationInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Project Name:
 * 功能描述：
 *
 * @author faritor@unmz.net
 * @version 1.0
 * @date 2021-1-28 18:59
 * @since JDK 1.8
 */
@Configuration
public class DesensitizationConfiguration {

    /**
     * 启用脱敏拦截器
     *
     * @return
     */
    @Bean
    public DesensitizationInterceptor desensitization() {
        return new DesensitizationInterceptor();
    }

}
