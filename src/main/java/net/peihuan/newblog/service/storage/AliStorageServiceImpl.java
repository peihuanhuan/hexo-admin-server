package net.peihuan.newblog.service.storage;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.peihuan.newblog.bean.enums.ResultEnum;
import net.peihuan.newblog.config.aliyun.AliyunConfig;
import net.peihuan.newblog.config.BlogProperties;
import net.peihuan.newblog.exception.BaseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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

    public static void main(String[] args) {
        OSS ossClient = new OSSClientBuilder()
                .build("http://oss-cn-hangzhou.aliyuncs.com", "LTAI4Fm1AjN1U5L346Jz57qU", "6HQvGSBoqjHX6rCnDbKIoVObPyAb7a");
        ossClient.copyObject("peihuan-blog", "a/xx", "peihuan-blog", "a/xx/ac/");
        ossClient.deleteObject("peihuan-blog", "a/b/c/0fe6541c9ed44bf09b96c01d16d7f199.pdf");
        ossClient.shutdown();
    }
}
