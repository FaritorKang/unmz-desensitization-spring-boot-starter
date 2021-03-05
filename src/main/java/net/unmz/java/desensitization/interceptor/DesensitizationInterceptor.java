package net.unmz.java.desensitization.interceptor;

import net.unmz.java.desensitization.annotation.Desensitization;
import net.unmz.java.desensitization.constants.DesensitizationType;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Project Name:
 * 功能描述：
 *
 * @author faritor@unmz.net
 * @version 1.0
 * @date 2021-1-25 16:47
 * @since JDK 1.8
 */
@Intercepts({
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class,
                RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class}),})
public class DesensitizationInterceptor implements Interceptor {

    private static final Logger logger = LoggerFactory.getLogger(DesensitizationInterceptor.class);

    private static final Map<String, DesensitizationType> desensitizationMap = new LinkedHashMap<>();

    static {
        initDesensitizationMap();
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object result = invocation.proceed();
        // 如果需要对结果脱敏，则执行
        // 先对Map进行处理
        if (result instanceof Map) {
            return this.desensitizationMap(result);
        }

        // 处理集合
        if (result instanceof ArrayList<?>) {
            List<?> list = (ArrayList<?>) result;
            return this.desensitization(list);
        }

        // 处理单个bean
        return this.desensitization(result);
    }

    private static void initDesensitizationMap() {
        desensitizationMap.put("idCode", DesensitizationType.ID_CARD);
        desensitizationMap.put("idCard", DesensitizationType.ID_CARD);
        desensitizationMap.put("userIDCard", DesensitizationType.ID_CARD);
        desensitizationMap.put("userIdCard", DesensitizationType.ID_CARD);

        desensitizationMap.put("username", DesensitizationType.REAL_NAME);
        desensitizationMap.put("address", DesensitizationType.ADDRESS);

        desensitizationMap.put("name", DesensitizationType.REAL_NAME);
        desensitizationMap.put("realName", DesensitizationType.REAL_NAME);

        desensitizationMap.put("email", DesensitizationType.EMAIL);

    }

    /*
     * 对map脱敏
     */
    private Object desensitizationMap(Object result) {
        Map<String, Object> mapResult = (Map) result;
        if (mapResult == null || mapResult.isEmpty()) {
            return mapResult;
        }

        Set<String> keySet = mapResult.keySet();
        for (String key : keySet) {
            if (desensitizationMap.containsKey(key)) {
                DesensitizationType DesensitizationType = desensitizationMap.get(key);
                String replacedVal = getReplacedVal(DesensitizationType, MapUtils.getString(mapResult, key), null);
                mapResult.put(key, replacedVal);
            }
        }
        return result;
    }

    private List<?> desensitization(List<?> list) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }

        Class<?> cls = null;
        for (Object o : list) {
            // 脱敏map，改变引用地址(根据静态配置脱敏)
            if (o instanceof Map) {
                o = desensitizationMap(o);
                continue;
            }

            // 脱敏bean(根据注解脱敏)
            if (cls == null) {
                cls = o.getClass();
            }
            o = desensitization(o);
        }
        return list;
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    /**
     * 用于在Mybatis配置文件中指定一些属性的，注册当前拦截器的时候可以设置一些属性
     */
    @Override
    public void setProperties(Properties properties) {
    }

    private Object desensitization(Object obj) {
        if (obj == null) {
            return obj;
        }
        Class<?> cls = obj.getClass();
        Field[] objFields = cls.getDeclaredFields();
        if (ArrayUtils.isEmpty(objFields)) {
            return obj;
        }

        for (Field field : objFields) {
            if ("serialVersionUID".equals(field.getName())) {
                continue;
            }

            Desensitization desensitization;
            if (String.class != field.getType() || (desensitization = field.getAnnotation(Desensitization.class)) == null) {
                continue;
            }

            try {
                field.setAccessible(true);
                String value = field.get(obj) != null ? field.get(obj).toString() : null;
                if (StringUtils.isBlank(value)) {
                    continue;
                }

                value = getReplacedVal(desensitization.value(), value, desensitization.attach());
                field.set(obj, value);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return obj;
    }

    private String getReplacedVal(DesensitizationType type, String value, String[] attachs) {
        List<String> regular;
        switch (type) {
            case CUSTOM:
                regular = Arrays.asList(attachs);
                break;
            case TRUNCATE:
                regular = truncateRender(attachs);
                break;
            default:
                regular = Arrays.asList(type.getRegular());
        }

        if (regular.size() > 1) {
            String match = regular.get(0);
            String result = regular.get(1);
            if (null != match && result != null && match.length() > 0) {
                value = value.replaceAll(match, result);
                return value;
            }
        }

        return "";
    }

    private List<String> truncateRender(String[] attaches) {
        List<String> regular = new ArrayList<>();
        if (null != attaches && attaches.length > 1) {
            String rule = attaches[0];
            String size = attaches[1];
            String template, result;
            if ("0".equals(rule)) {
                template = "^(\\S{%s})(\\S+)$";
                result = "$1";
            } else if ("1".equals(rule)) {
                template = "^(\\S+)(\\S{%s})$";
                result = "$2";
            } else {
                return regular;
            }
            try {
                if (Integer.parseInt(size) > 0) {
                    regular.add(0, String.format(template, size));
                    regular.add(1, result);
                }
            } catch (Exception e) {
                logger.warn("ValueDesensitizeFilter truncateRender size {} exception", size, e);
            }
        }
        return regular;
    }

}
