
## unmz-desensitization-spring-boot-starter

将本项目引入spring-boot项目中后,无需其他操作即可使用注解进行脱敏

在java对象属性上增加注解@Desensitization用来标识该属性用于脱敏,增加类型DesensitizationType.REAL_NAME来标识以何种格式进行脱敏

```java
    @Desensitization(DesensitizationType.REAL_NAME)
    private String name;
```