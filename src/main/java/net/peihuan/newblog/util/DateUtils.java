package net.peihuan.newblog.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@UtilityClass
@Slf4j
public class DateUtils {

    public static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";
    public static final DateTimeFormatter YYYY_MM_DD_HH_MM_SS_DTF = DateTimeFormatter.ofPattern(YYYY_MM_DD_HH_MM_SS);

    public String toString(LocalDateTime time, DateTimeFormatter formatter) {
        if (time == null || formatter == null) {
            return null;
        }
        return formatter.format(time);
    }


}
