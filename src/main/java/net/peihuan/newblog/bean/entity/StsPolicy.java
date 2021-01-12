package net.peihuan.newblog.bean.entity;

import lombok.Data;

@Data
public class StsPolicy {
     String expiration;
     String accessKeyId;
     String accessKeySecret;
     String securityToken;
     String requestId;
}
