package net.unmz.java.desensitization.annotation;

import net.unmz.java.desensitization.config.DesensitizationConfiguration;
import net.unmz.java.desensitization.constants.DesensitizationType;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Project Name:
 * 功能描述：
 *
 * @author faritor@unmz.net
 * @version 1.0
 * @date 2021-1-25 16:14
 * @since JDK 1.8
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Import({DesensitizationConfiguration.class})
public @interface Desensitization {

    DesensitizationType value();

    String[] attach() default "";

}
