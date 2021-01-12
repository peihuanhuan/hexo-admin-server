package net.peihuan.newblog.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.peihuan.newblog.bean.entity.Article;
import net.peihuan.newblog.bean.enums.ResultEnum;
import net.peihuan.newblog.bean.form.NewArticleForm;
import net.peihuan.newblog.bean.form.UpdateArticleForm;
import net.peihuan.newblog.config.BlogProperties;
import net.peihuan.newblog.exception.BaseException;
import net.peihuan.newblog.mapper.ArticleMapper;
import net.peihuan.newblog.service.cdn.CdnService;
import net.peihuan.newblog.service.storage.StorageService;
import net.peihuan.newblog.util.ArticleUtil;
import net.peihuan.newblog.util.CmdUtil;
import net.peihuan.newblog.util.DateUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
    private CdnService cdnService;


    @SuppressWarnings("unchecked")
    public Page page(String title, Integer page, Integer limit) {
        Page page1 = page(new Page(page, limit),
                Wrappers.<Article>lambdaQuery().like(title != null, Article::getTitle, title)

                        .orderByDesc(Article::getCreateTime));


        page1.setRecords(ArticleUtil.convert2VO(page1.getRecords()));
        return page1;
    }


    public void deleteArticle(Long articleId) {
        Article article = getById(articleId);
        if (article == null) {
            throw new BaseException(ResultEnum.ARTICLE_NOT_FOUND);
        }
        removeById(articleId);
        deleteFile(article.getPublishedTitle());
        log.info("--------------- 删除文件 {}", article.getPublishedTitle());
        cdnService.refreshHoleSite();
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

        deleteFile(article.getPublishedTitle());
        log.info("--------------- 取消发布，删除文件 {}", article.getPublishedTitle());
        try{
            generateFile();
        }catch (Exception ex){
            System.out.println(ex.getMessage());
        }
        cdnService.refreshHoleSite();
    }


    public Article publishNewArticle(NewArticleForm form) {
        Article one = getOne(Wrappers.<Article>lambdaQuery().eq(Article::getPublishedTitle, form.getTitle())
                .eq(Article::getPublish, true));
        if (one != null) {
            throw new BaseException(ResultEnum.ARTICLE_TITLE_EXIST);
        }
        Article article = new Article();
        BeanUtils.copyProperties(form, article);
        article.setTags(ArticleUtil.list2Str(form.getTags()));
        article.setCategories(ArticleUtil.list2Str(form.getCategories()));
        article.setCreateTime(LocalDateTime.now());
        article.setUpdateTime(LocalDateTime.now());
        if (form.getPublish() != null && form.getPublish()) {
            article.setPublishedTitle(form.getTitle());
            saveArticleFile(article);
            log.info("--------------- 发布文章，保存markdown文件完成 {}", article.getTitle());
            cdnService.refreshHoleSite();
        }
        save(article);
        try{
            generateFile();
        }catch (Exception ex){
            System.out.println(ex.getMessage());
        }
        return article;
    }

    public Article updateArticle(UpdateArticleForm form) {
        if (form.getId() == null) {
            NewArticleForm newForm = new NewArticleForm();
            BeanUtils.copyProperties(form, newForm);
            return publishNewArticle(newForm);

        } else if (form.getId() == 1) {
            Article article = updateIndexHtml(form.getContent());
            cdnService.refreshHoleSite();
            return article;
        } else {

            Article article = getById(form.getId());
            if (article == null) {
                throw new BaseException(ResultEnum.ARTICLE_NOT_FOUND);
            }
            Article one = getOne(Wrappers.<Article>lambdaQuery().eq(Article::getPublishedTitle, form.getTitle())
                    .eq(Article::getPublish, true));
            if (one != null && !one.getId().equals(form.getId())) {
                throw new BaseException(ResultEnum.ARTICLE_TITLE_EXIST);
            }
            article.setTitle(form.getTitle());
            if (form.getPublish() != null && form.getPublish()) {
                // 只有发布的时候才会更改图片的路径
                article.setContent(correctImageAddress(form.getTitle(), form.getContent()));
            } else {
                article.setContent(form.getContent());
            }

            article.setTags(ArticleUtil.list2Str(form.getTags()));
            article.setCategories(ArticleUtil.list2Str(form.getCategories()));
            article.setUpdateTime(LocalDateTime.now());

            if (form.getPublish() != null) {
                if (form.getPublish()) {
                    saveArticleFile(article);
                    log.info("--------------- 更新并发布文章，保存markdown文件完成 {}", article.getTitle());
                    if (StringUtils.hasText(article.getPublishedTitle())
                            && !article.getTitle().equals(article.getPublishedTitle())) {
                        deleteFile(article.getPublishedTitle());
                        log.info("--------------- {} 与上次发布的文章标题名不一样，删除上次markdown文件 {}",
                                article.getTitle(), article.getPublishedTitle());
                    }
                    article.setPublishedTitle(article.getTitle());
                    try{
                        generateFile();
                    }catch (Exception ex){
                        System.out.println(ex.getMessage());
                    }
                    cdnService.refreshHoleSite();
                } else {
                    deleteFile(article.getPublishedTitle());
                    log.info("--------------- 取消文章发布，删除markdown文件完成 {}", article.getPublishedTitle());
                }
                article.setPublish(form.getPublish());
            }

            updateById(article);
            return article;
        }
    }

    public void generateFile(){
        // 生成静态文件
        String[] shs = new String[]{"cd " + blogProperties.getHexoPath(), "pwd", "hexo g"};
        CmdUtil.excuterBashs(shs);
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
    private void saveArticleFile(Article article) {
        String dir = blogProperties.getHexoPath() + "/source/_posts/";
        Path dirPath = Paths.get(dir);
        if (!Files.isWritable(dirPath)) {
            Files.createDirectories(dirPath);
        }
        Path filePath = Paths.get(dir + article.getTitle() + ".md");
        Files.deleteIfExists(filePath);
        Files.createFile(filePath);
        String content = generateHexoFileContent(article);
        Files.write(filePath, content.getBytes());
    }

    @SneakyThrows
    private void deleteFile(String title) {
        Path filePath = Paths.get(blogProperties.getHexoPath() + "/source/_posts/" + title + ".md");
        Files.deleteIfExists(filePath);
    }


    private String generateHexoFileContent(Article article) {
        return "---\n"
                + "title: " + article.getTitle() + "\n"
                + "date: " + DateUtils.toString(article.getCreateTime(), DateUtils.YYYY_MM_DD_HH_MM_SS_DTF) + "\n"
                + "tags: " + article.getTags() + "\n"
                + "categories: " + article.getCategories() + "\n"
                + "---\n"
                + article.getContent();
    }


    @SneakyThrows
    public Article updateIndexHtml(String context) {
        String[] split = context.split("\n===\n");
        String before = null;
        String after;
        if (split.length == 1) {
            after = split[0];
        } else {
            before = split[0];
            after = split[1];
        }
        //"---\n"
        //        + "title: Welcome.\n"
        //        + "date: 2020-11-21 22:31:06\n"
        //        + "permalink: index.html \n"
        //        + "---\n"
        String md = before + getNewArticles() + after;


        Path filePath = Paths.get(blogProperties.getHexoPath() + "/source/blog/index.md");
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
        lastArticles.forEach(article -> builder
                .append("> 最新文章：")
                .append(getArticleAddressMd(article))
                .append("（创建于 ")
                .append(article.getCreateTime().format(DateTimeFormatter.ISO_LOCAL_DATE))
                .append("，更新于 ")
                .append(article.getUpdateTime().format(DateTimeFormatter.ISO_LOCAL_DATE))
                .append("）\n"));
        return builder.toString();
    }

    public List<Article> getLastArticles(int count) {
        return list(Wrappers.<Article>lambdaQuery()
                .gt(Article::getId, 1).eq(Article::getPublish, true)
                .orderByDesc(Article::getUpdateTime).last("limit " + count));
    }

    public String getArticleAddressMd(Article article) {
        int year = article.getCreateTime().getYear();
        String month = String.format("%02d", article.getCreateTime().getMonthValue());
        String day = String.format("%02d", article.getCreateTime().getDayOfMonth());
        String host = "https://" + blogProperties.getAli().getCdn().getHost() + "/";
        return "[" + article.getTitle() + "](" + host + year + "/" + month + "/" + day + "/" + article.getTitle() + ")";
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


    public static void main(String[] args) {
        String[] split = "xxxxxx".split("\n===\n");
        System.out.println();
    }
}
