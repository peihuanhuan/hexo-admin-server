package net.peihuan.newblog.bean.enums;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public enum ResultEnum {


    SUCCESS(200, "success"),

    PARAMS_ERROR(400, "参数错误"),

    UNAUTHORIZED(401, "未认证"),

    AUTHORIZED_FAIL(401, "token失效"),

    FORBIDDEN(403, "权限不足"),

    HTTP_METHOD_NOT_SUPPORT(405, "http方法不支持"),

    SYS_ERROR(500, "系统繁忙"),

    UN_SUPPORT_OPERATE(600, "不支持的操作类型"),

    ALI_ERROR(700, "阿里云请求出错"),


    ARTICLE_NOT_FOUND(1000, "文章不存在"),

    ARTICLE_TITLE_EXIST(1001, "已有正发布的相同标题的文章"),

    USER_NOT_FOUND(2000, "用户不存在"),

    PASSWORD_ERROR(2001, "密码错误"),

    IMAGE_NOT_FOUND(3000, "图片资源不存在"),

    ;


    private Integer code;

    private String msg;


    ResultEnum(Integer code, String message) {
        this.code = code;
        this.msg = message;
    }

    static Map<Integer, ResultEnum> enumMap = new HashMap<>();

    static {
        for (ResultEnum type : ResultEnum.values()) {
            enumMap.put(type.getCode(), type);
        }
    }

    public static ResultEnum getEnum(Integer code) {
        return enumMap.get(code);
    }

}
