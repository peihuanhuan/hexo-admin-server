package net.peihuan.newblog.config.aliyun;

import lombok.Data;

@Data
public class AliyunConfig {
    private String accessKeyId;
    private String accessKeySecret;
    private String roleArn;
    private String roleSessionName;
    private String stsEndpoint;

    private OSSConfig oss;
    private CDNConfig cdn;

}
