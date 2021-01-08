package net.peihuan.newblog.config;

import lombok.Data;
import net.peihuan.newblog.config.aliyun.AliyunConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "blog")
public class BlogProperties {

    private String hexoPath = "/tmp";

    private String staticHost;

    private AliyunConfig ali = new AliyunConfig();

}
