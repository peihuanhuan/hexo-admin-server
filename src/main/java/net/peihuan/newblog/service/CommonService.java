package net.peihuan.newblog.service;

import lombok.extern.slf4j.Slf4j;
import net.peihuan.newblog.config.BlogProperties;
import net.peihuan.newblog.service.cdn.CdnService;
import net.peihuan.newblog.util.CmdUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CommonService {

    @Autowired
    private BlogProperties blogProperties;
    @Autowired
    private CdnService cdnService;

    @Async
    public void generateHexoAndRefreshCdn() {
        executeHexoGenerateCommand();
        try {
            cdnService.refreshHoleSite();
        }catch (RuntimeException e){
            log.info("cdn同步错误");
            log.info(e.getMessage());
        }
    }

    public void executeHexoGenerateCommand() {
        // 生成静态文件
        String[] shs = new String[]{"cd " + blogProperties.getHexoPath(), "pwd", "hexo g"};
        CmdUtil.excuterBashs(shs);
    }
}
