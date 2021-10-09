package net.peihuan.newblog.service;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.peihuan.newblog.bean.entity.Article;
import net.peihuan.newblog.bean.enums.ResultEnum;
import net.peihuan.newblog.bean.form.UpdateArticleForm;
import net.peihuan.newblog.config.BlogProperties;
import net.peihuan.newblog.exception.BaseException;
import net.peihuan.newblog.mapper.ArticleMapper;
import net.peihuan.newblog.service.storage.StorageService;
import net.peihuan.newblog.util.ArticleUtil;
import net.peihuan.newblog.util.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ArticleService extends ServiceImpl<ArticleMapper, Article> {


    @Autowired
    private BlogProperties blogProperties;
    @Autowired
    private StorageService storageService;
    @Autowired
    private CommonService commonService;


    @SuppressWarnings("unchecked")
    public Page page(String title, Integer page, Integer limit) {
        Page page1 = page(new Page(page, limit), Wrappers.<Article>lambdaQuery()
                .like(title != null, Article::getTitle, title)
                .orderByDesc(Article::getUpdateTime));

        page1.setRecords(ArticleUtil.convert2VO(page1.getRecords()));
        return page1;
    }


    public void deleteArticle(Long articleId) {
        Article article = getById(articleId);
        if (article == null) {
            throw new BaseException(ResultEnum.ARTICLE_NOT_FOUND);
        }
        removeById(articleId);
        deleteOriginalArticleFile(article.getPublishedTitle());
        log.info("--------------- 删除文件 {}", article.getPublishedTitle());
        commonService.generateHexoAndRefreshCdn();
    }

    public void unPublish(Long articleId) {
        Article article = getById(articleId);
        if (article == null) {
            throw new BaseException(ResultEnum.ARTICLE_NOT_FOUND);
        }

        Article update = new Article();
        update.setId(articleId);
        update.setPublish(false);
        updateById(update);

        deleteOriginalArticleFile(article.getPublishedTitle());
        log.info("--------------- 取消发布，删除文件 {}", article.getPublishedTitle());
        commonService.generateHexoAndRefreshCdn();
    }


    public Article updateArticle(UpdateArticleForm form) {

        if (form.getId() == null) {
            form.setId(IdWorker.getId());
        }

        if (isHomePage(form)) {
            Article article = updateHomePageHtml(form.getContent());
            commonService.generateHexoAndRefreshCdn();
            return article;
        }

        return updateArticleCore(form);
    }


    private Article updateArticleCore(UpdateArticleForm form) {
        Article one = getOne(Wrappers.<Article>lambdaQuery().eq(Article::getPublishedTitle, form.getTitle()).eq(Article::getPublish, true));

        if (one != null && !one.getId().equals(form.getId())) {
            throw new BaseException(ResultEnum.ARTICLE_TITLE_EXIST);
        }

        Article update = new Article();

        Article originalArticle = getById(form.getId());

        if (form.getPublish() != null && form.getPublish()) {
            // 只有发布的时候才会更改图片的路径
            update.setContent(correctImageAddress(form.getTitle(), form.getContent()));
        } else {
            update.setContent(form.getContent());
        }


        update.setId(form.getId());
        update.setTitle(form.getTitle());
        update.setTags(ArticleUtil.list2Str(form.getTags()));
        update.setCategories(ArticleUtil.list2Str(form.getCategories()));
        update.setUpdateTime(LocalDateTime.now());
        update.setPublish(form.getPublish());


        if (form.getPublish() != null) {
            deleteOriginalArticleFile(originalArticle.getPublishedTitle());
            if (form.getPublish()) {
                String content = generateHexoFileContent(update, originalArticle.getCreateTime());
                saveArticleFileToDisk(update.getTitle(), content);
                update.setPublishedTitle(update.getTitle());
            }
            commonService.generateHexoAndRefreshCdn();
        }

        saveOrUpdate(update);
        return update;
    }



    private boolean isHomePage(UpdateArticleForm form) {
        return form.getId() == 1;
    }



    private String correctImageAddress(String title, String content) {
        String staticHost = blogProperties.getAli().getOss().getOssStaticHost();
        if (!staticHost.endsWith("/")) {
            staticHost = staticHost + "/";
        }
        String[] staticHostSplit = staticHost.split("\\.");
        String reg = "(!\\[.*?]\\(http[^)]*\\." + staticHostSplit[staticHostSplit.length - 2] + "\\." + staticHostSplit[staticHostSplit.length - 1] + ")(\\S+)(/[^)]+)\\)";
        Pattern filePattern = Pattern.compile(reg);
        Matcher matcher = filePattern.matcher(content);
        while (matcher.find()) {
            String from = matcher.group(2) + matcher.group(3);
            // markdown图片链接中最好不要有空格这种字符
            title = ArticleUtil.trimPictureTitle(title);
            String to = title + matcher.group(3);
            if (!from.equals(to)) {
                log.info("--------------- oss：移动 {} 至 {}, reg: {}", from, to, reg);
                storageService.move(from, to);
            }
        }
        return content.replaceAll(reg, "$1" + title + "$3" + ")");
    }


    @SneakyThrows
    private void saveArticleFileToDisk(String title, String content) {
        String dir = blogProperties.getHexoPath() + "/source/_posts/";
        Path dirPath = Paths.get(dir);
        if (!Files.isWritable(dirPath)) {
            Files.createDirectories(dirPath);
        }
        Path filePath = Paths.get(dir + title + ".md");
        Files.deleteIfExists(filePath);
        Files.createFile(filePath);
        Files.write(filePath, content.getBytes());
    }

    @SneakyThrows
    private void deleteOriginalArticleFile(String title) {
        Path filePath = Paths.get(blogProperties.getHexoPath() + "/source/_posts/" + title + ".md");
        log.info("------------- 删除文件 {}", filePath.toString());
        Files.deleteIfExists(filePath);
    }


    private String generateHexoFileContent(Article article, LocalDateTime createTime) {
        return "---\n" + "title: " + article.getTitle() + "\n" + "date: " + DateUtils.toString(createTime, DateUtils.YYYY_MM_DD_HH_MM_SS_DTF) + "\n" + "tags: " + article.getTags() + "\n" + "categories: " + article.getCategories() + "\n" + "---\n" + article.getContent();
    }


    @SneakyThrows
    public Article updateHomePageHtml(String context) {
        String[] split = context.split("\n===\n");
        String before = null;
        String after;
        if (split.length == 1) {
            after = split[0];
        } else {
            before = split[0];
            after = split[1];
        }
        String md = before + getNewArticles() + after;

        Path filePath = Paths.get(blogProperties.getHexoPath() + "/source/home/index.md");
        Files.deleteIfExists(filePath);
        Files.createFile(filePath);

        Files.write(filePath, md.getBytes());

        Article article = new Article();
        article.setContent(context);
        article.setId(1L);
        updateById(article);
        return article;
    }

    private String getNewArticles() {
        List<Article> lastArticles = getLastArticles(10);
        StringBuilder builder = new StringBuilder();
        lastArticles.forEach(article -> builder.append("> 最新文章：").append(getArticleUrl(article)).append("（创建于 ").append(article.getCreateTime().format(DateTimeFormatter.ISO_LOCAL_DATE)).append("，更新于 ").append(article.getUpdateTime().format(DateTimeFormatter.ISO_LOCAL_DATE)).append("）\n"));
        return builder.toString();
    }

    public List<Article> getLastArticles(int count) {
        return list(Wrappers.<Article>lambdaQuery().gt(Article::getId, 1).eq(Article::getPublish, true).orderByDesc(Article::getUpdateTime).last("limit " + count));
    }

    @SneakyThrows
    public String getArticleUrl(Article article) {
        int year = article.getCreateTime().getYear();
        String month = String.format("%02d", article.getCreateTime().getMonthValue());
        String day = String.format("%02d", article.getCreateTime().getDayOfMonth());
        String host = "https://" + blogProperties.getAli().getCdn().getHost() + "/";
        String encodeTitle = URLEncoder.encode(article.getTitle(), "utf-8");
        // 加号需要替换成 %20
        encodeTitle = encodeTitle.replaceAll("\\+", "%20");
        return "[" + article.getTitle() + "](" + host + year + "/" + month + "/" + day + "/" + encodeTitle + ")";
    }


    /**
     * --------------------------------------------------------
     */

    public Map<String, Object> info() {
        List<Article> articles = list();
        Map<String, Object> info = new HashMap<>();
        info.put("published", articles.stream().filter(Article::getPublish).count());
        info.put("unPublished", articles.stream().filter(a -> !a.getPublish()).count());
        info.put("category", articles.stream().flatMap(article -> ArticleUtil.str2List(article.getCategories()).stream()).collect(Collectors.toSet()));
        info.put("tag", articles.stream().flatMap(article -> ArticleUtil.str2List(article.getTags()).stream()).collect(Collectors.toSet()));
        return info;
    }

}
