package net.peihuan.newblog.config.aliyun;

import lombok.Data;

@Data
public class OSSConfig {
    private String bucketName;
    private String endpoint = "http://oss-cn-hangzhou.aliyuncs.com";
}
