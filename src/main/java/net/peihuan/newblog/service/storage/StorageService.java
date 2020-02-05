package net.peihuan.newblog.service.storage;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    void upload(String objectName, MultipartFile file);
    void move(String sourceKey, String targetKey);
}
