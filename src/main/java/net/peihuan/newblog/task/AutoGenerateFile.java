package net.peihuan.newblog.task;

import lombok.extern.slf4j.Slf4j;
import net.peihuan.newblog.service.ArticleService;
import net.peihuan.newblog.service.CommonService;
import net.peihuan.newblog.service.cdn.CdnService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AutoGenerateFile {
    @Autowired
    private CommonService commonService;
    @Autowired
    private CdnService cdnService;

    @Scheduled(cron = "0 0 * * * *")
    public void generateHexoFile() {

        log.info("开始生成静态文件！");
        commonService.generateHexoAndRefreshCdn();
        log.info("定时生成静态文件成功");

    }
}
