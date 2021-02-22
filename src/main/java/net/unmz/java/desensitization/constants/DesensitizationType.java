package net.unmz.java.desensitization.constants;

/**
 * Project Name:
 * 功能描述：
 *
 * @author faritor@unmz.net
 * @version 1.0
 * @date 2021-1-25 16:45
 * @since JDK 1.8
 */
public enum DesensitizationType {

    PHONE("phone", "11位手机号", "^(\\d{3})\\d{4}(\\d{4})$", "$1****$2"),
    ID_CARD("idCard", "16或者18身份证号", "^(\\d{4})\\d{11,13}(\\w{1})$", "$1****$2"),
    BANK_CARD("bankCardNo", "银行卡号", "^(\\d{4})\\d*(\\d{4})$", "$1****$2"),
    ADDRESS("address", "地址", "(?<=.{3}).*(?=.{3})", "*"),
    REAL_NAME("realName", "真实姓名", "(?<=.{1}).*(?=.{1})", "*"),
    EMAIL("email", "电子邮箱", "(\\w+)\\w{5}@(\\w+)", "$1***@$2"),
    CUSTOM("custom", "自定义正则处理", ""),
    TRUNCATE("truncate", "字符串截取处理", "");

    private final String type;

    private final String describe;

    private final String[] regular;

    DesensitizationType(String type, String describe, String... regular) {
        this.type = type;
        this.describe = describe;
        this.regular = regular;
    }

    public String getType() {
        return type;
    }


    public String getDescribe() {
        return describe;
    }


    public String[] getRegular() {
        return regular;
    }

}
