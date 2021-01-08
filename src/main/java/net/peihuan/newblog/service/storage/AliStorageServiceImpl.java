package net.peihuan.newblog.service.storage;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.auth.sts.AssumeRoleRequest;
import com.aliyuncs.auth.sts.AssumeRoleResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.peihuan.newblog.bean.enums.ResultEnum;
import net.peihuan.newblog.bean.vo.RestResult;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.peihuan.newblog.bean.enums.ResultEnum;
import net.peihuan.newblog.config.aliyun.AliyunConfig;
import net.peihuan.newblog.config.BlogProperties;
import net.peihuan.newblog.exception.BaseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class AliStorageServiceImpl implements StorageService {

    @Autowired
    private BlogProperties blogProperties;

    @Override
    @SneakyThrows
    public void upload(String objectName, MultipartFile file) {
        AliyunConfig aliyunConfig = blogProperties.getAli();
        OSS ossClient = new OSSClientBuilder()
                .build(aliyunConfig.getOss().getEndpoint(), aliyunConfig.getAccessKeyId(), aliyunConfig.getAccessKeySecret());
        ossClient.putObject(aliyunConfig.getOss().getBucketName(), objectName, file.getInputStream());
        ossClient.shutdown();
    }

    @Override
    public RestResult getPolicy() {

        AliyunConfig aliyunConfig = blogProperties.getAli();
        String policy = "{\n" +
                "    \"Version\": \"1\", \n" +
                "    \"Statement\": [\n" +
                "        {\n" +
                "            \"Action\": [\n" +
                "                \"oss:*\"\n" +
                "            ], \n" +
                "            \"Resource\": [\n" +
                "                \"acs:oss:*:*:*\" \n" +
                "            ], \n" +
                "            \"Effect\": \"Allow\"\n" +
                "        }\n" +
                "    ]\n" +
                "}";
        try {
            // 添加endpoint（直接使用STS endpoint，前两个参数留空，无需添加region ID）
            DefaultProfile.addEndpoint("", "", "Sts", aliyunConfig.getStsEndpoint());
            // 构造default profile（参数留空，无需添加region ID）
            IClientProfile profile = DefaultProfile.getProfile("", aliyunConfig.getAccessKeyId(), aliyunConfig.getAccessKeySecret());
            // 用profile构造client
            DefaultAcsClient client = new DefaultAcsClient(profile);
            final AssumeRoleRequest request = new AssumeRoleRequest();
            request.setMethod(MethodType.POST);
            request.setRoleArn(aliyunConfig.getRoleArn());
            request.setRoleSessionName(aliyunConfig.getRoleSessionName());
            request.setPolicy(policy); // 若policy为空，则用户将获得该角色下所有权限
            request.setDurationSeconds(1000L); // 设置凭证有效时间
            final AssumeRoleResponse response = client.getAcsResponse(request);
            Map<String, String> map = new HashMap<>();
            map.put("Expiration", response.getCredentials().getExpiration());
            map.put("accessKeyId", response.getCredentials().getAccessKeyId());
            map.put("accessKeySecret", response.getCredentials().getAccessKeySecret());
            map.put("securityToken", response.getCredentials().getSecurityToken());
            map.put("requestId", response.getRequestId());
            map.put("bucket", aliyunConfig.getOss().getBucketName());
            map.put("region", aliyunConfig.getOss().getRegion());
            log.info("Expiration: " + response.getCredentials().getExpiration());
            log.info("Access Key Id: " + response.getCredentials().getAccessKeyId());
            log.info("Access Key Secret: " + response.getCredentials().getAccessKeySecret());
            log.info("Security Token: " + response.getCredentials().getSecurityToken());
            log.info("RequestId: " + response.getRequestId());
            return RestResult.success(map);
        } catch (ClientException e) {
            log.error("Failed：");
            log.error("Error code: " + e.getErrCode());
            log.error("Error message: " + e.getErrMsg());
            log.error("RequestId: " + e.getRequestId());
            return RestResult.error(ResultEnum.ALI_ERROR, e.getErrMsg());
        }
    }

    @Override
    public void move(String sourceKey, String targetKey) {
        try {
            AliyunConfig aliyunConfig = blogProperties.getAli();
            OSS ossClient = new OSSClientBuilder()
                    .build(aliyunConfig.getOss().getEndpoint(), aliyunConfig.getAccessKeyId(), aliyunConfig.getAccessKeySecret());
            ossClient.copyObject(aliyunConfig.getOss().getBucketName(), sourceKey, aliyunConfig.getOss().getBucketName(), targetKey);
            ossClient.deleteObject(aliyunConfig.getOss().getBucketName(), sourceKey);
            ossClient.shutdown();
        } catch (OSSException e) {
            if (e.getErrorCode().equals("NoSuchKey")) {
                throw new BaseException(ResultEnum.IMAGE_NOT_FOUND, sourceKey);

            } else  {
                throw new BaseException(ResultEnum.ALI_ERROR);
            }
        }
    }
}
