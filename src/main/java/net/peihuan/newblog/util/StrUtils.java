package net.peihuan.newblog.util;

import lombok.experimental.UtilityClass;

import java.util.UUID;

@UtilityClass
public class StrUtils {
    public String uuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
