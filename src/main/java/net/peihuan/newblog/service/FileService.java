package net.peihuan.newblog.service;

import lombok.extern.slf4j.Slf4j;
import net.peihuan.newblog.config.BlogProperties;
import net.peihuan.newblog.service.storage.StorageService;
import net.peihuan.newblog.util.ArticleUtil;
import net.peihuan.newblog.util.StrUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;


@Slf4j
@Service
public class FileService {

    @Autowired
    private StorageService storageService;
    @Autowired
    private BlogProperties properties;

    public String upload(String title, MultipartFile file) {
        title = ArticleUtil.trimPictureTitle(title);
        String newName = StrUtils.uuid() + "." + FilenameUtils.getExtension(file.getOriginalFilename());
        if (!StringUtils.hasText(title)) {
            title = "tmp";
        }
        String path = title + "/" + newName;
        storageService.upload(path, file);
        return properties.getAli().getOss().getOssStaticHost() + path;
    }
}
