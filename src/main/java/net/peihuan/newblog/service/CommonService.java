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
        cdnService.refreshHoleSite();
    }

    public void executeHexoGenerateCommand() {
        // 生成静态文件
        String[] shs = new String[]{"cd " + blogProperties.getHexoPath(), "pwd", "hexo g"};
        CmdUtil.excuterBashs(shs);
    }
}
