package net.peihuan.newblog.service.storage;

import net.peihuan.newblog.bean.vo.RestResult;
import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    void upload(String objectName, MultipartFile file);
    RestResult getPolicy();
    void move(String sourceKey, String targetKey);
}
