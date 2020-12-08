package net.peihuan.newblog.task;

import lombok.extern.slf4j.Slf4j;
import net.peihuan.newblog.bean.entity.Article;
import net.peihuan.newblog.service.ArticleService;
import net.peihuan.newblog.service.cdn.CdnService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AutoUpdateIndex {

    @Autowired
    private ArticleService articleService;
    @Autowired
    private CdnService cdnService;

    @Scheduled(cron = "0 0/15 * * * ?")
    public void updateIndex() {

        log.info("开始定时更新 index");
        Article article = articleService.getById(1L);
        if (article == null) {
            return;
        }
        articleService.updateIndexHtml(article.getContent());
        cdnService.refreshHoleSite();

        log.info("定时更新 index");

    }
}
