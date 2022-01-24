package net.peihuan.newblog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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


import javax.servlet.http.HttpServletRequest;
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
    HttpServletRequest httpServletRequest;
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
                .eq(Article::getUserId, httpServletRequest.getHeader("X-Token").split("_")[0])
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

    /**
     * 更新并发布文章
     *
     * @param form
     * @return
     */
    public Article updateAndPublish(UpdateArticleForm form) {

        // 1. 新增或更新文章
        Article article = addOrUpdateArticle(form);

        // 2. 发布文章
        publishArticle(article);

        return article;
    }

    /**
     * 发布某个id的文章
     * @param articleId
     */
    public void publishById(Long articleId) {
        // 1. 取出文章
        Article article = getById(articleId);
        if (article == null) {
            throw new BaseException(ResultEnum.ARTICLE_NOT_FOUND);
        }

        // 2. 发布文章
        publishArticle(article);
    }

    /**
     * 发布文章
     * @param article
     */
    public void publishArticle(Article article) {
        // 1. 更新文章状态
        article.setPublish(true);
        article.setPublishedTitle(article.getTitle());
        updateById(article);

        // 2. 生成文章内容
        String content = generateHexoFileContent(article, article.getCreateTime());

        // 3. 保存文章到磁盘
        saveArticleFileToDisk(article.getPublishedTitle(), content);

        // 4. 执行hexo生成并刷新CDN
        commonService.generateHexoAndRefreshCdn();
    }

    /**
     * 增加或更新文章
     * @param form
     * @return
     */
    public Article addOrUpdateArticle(UpdateArticleForm form) {
        // 如果id为null就执行新增文章
        if (form.getId() == null) {
            return addArticle(form);
        } else {
            // 否则就执行更新文章
            return update1Article(form);
        }
    }

    /**
     * 添加文章（可以添加所有文章，包含about home等特殊文章）
     *
     * @param form
     * @return
     */
    public Article addArticle(UpdateArticleForm form) {
        // 1. 给id
        form.setId(IdWorker.getId());
        // 2. 更新首页
        if (isHomePage(form)) {
            Article article = updateHomePageHtml(form.getContent());
            commonService.generateHexoAndRefreshCdn();
            return article;
        }
        // 3. 如果已经存在 已经发布&&同名 的文章，报文章存在错误
        Article one = getOne(Wrappers.<Article>lambdaQuery().eq(Article::getPublishedTitle, form.getTitle()).eq(Article::getPublish, true));
        if (one != null && !one.getId().equals(form.getId())) {
            throw new BaseException(ResultEnum.ARTICLE_TITLE_EXIST);
        }
        // 4. 保存
        Article articleToAdd = new Article();
        articleToAdd.setId(form.getId());
        articleToAdd.setContent(form.getContent());
        articleToAdd.setTitle(form.getTitle());
        articleToAdd.setTags(ArticleUtil.list2Str(form.getTags()));
        articleToAdd.setCategories(ArticleUtil.list2Str(form.getCategories()));
        articleToAdd.setUpdateTime(LocalDateTime.now());
        articleToAdd.setPublish(form.getPublish());
        articleToAdd.setUserId(Long.parseLong(httpServletRequest.getHeader("X-Token").split("_")[0]));
        save(articleToAdd);
        return articleToAdd;
    }

    /**
     * 更新文章，（可以更新所有文章，包括特殊文章，即 home、about等）
     *
     * @param form
     * @return
     */
    public Article update1Article(UpdateArticleForm form) {
        // 1. 更新首页
        if (isHomePage(form)) {
            Article article = updateHomePageHtml(form.getContent());
            commonService.generateHexoAndRefreshCdn();
            return article;
        }

        // 2. 如果发布了的文章标题已经存在，则报标题存在错误
        Article one = getOne(Wrappers.<Article>lambdaQuery().eq(Article::getPublishedTitle, form.getTitle()).eq(Article::getPublish, true));
        if (one != null && !one.getId().equals(form.getId())) {
            throw new BaseException(ResultEnum.ARTICLE_TITLE_EXIST);
        }

        // 3. 更新
        Article articleToUpdate = new Article();
        articleToUpdate.setId(form.getId());
        articleToUpdate.setContent(form.getContent());
        articleToUpdate.setTitle(form.getTitle());
        articleToUpdate.setTags(ArticleUtil.list2Str(form.getTags()));
        articleToUpdate.setCategories(ArticleUtil.list2Str(form.getCategories()));
        articleToUpdate.setUpdateTime(LocalDateTime.now());
        articleToUpdate.setPublish(form.getPublish());
        articleToUpdate.setUserId(Long.parseLong(httpServletRequest.getHeader("X-Token").split("_")[0]));
        updateById(articleToUpdate);
        return articleToUpdate;
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
        update.setUserId(Long.parseLong(httpServletRequest.getHeader("X-Token").split("_")[0]));


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
        String before = "";
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
        lastArticles.forEach(article -> builder.append("> 最新文章：")
                .append(getArticleUrl(article))
                .append("（创建于 ").append(article.getCreateTime() == null ? "????-??-??" : article.getCreateTime().format(DateTimeFormatter.ISO_LOCAL_DATE))
                .append("，更新于 ")
                .append(article.getUpdateTime() == null ? "????-??-??" : article.getUpdateTime().format(DateTimeFormatter.ISO_LOCAL_DATE)).append("）\n"));
        return builder.toString();
    }

    public List<Article> getLastArticles(int count) {
        return list(Wrappers.<Article>lambdaQuery().gt(Article::getId, 1).eq(Article::getPublish, true).orderByDesc(Article::getUpdateTime).last("limit " + count));
    }

    @SneakyThrows
    public String getArticleUrl(Article article) {
        String year = "??";
        String month = "??";
        String day = "??";
        if (article.getCreateTime() != null) {
            year = String.format("%02d", article.getCreateTime().getYear());
            month = String.format("%02d", article.getCreateTime().getMonthValue());
            day = String.format("%02d", article.getCreateTime().getDayOfMonth());
        }

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
        List<Article> articles = list(new LambdaQueryWrapper<Article>().eq(Article::getUserId, httpServletRequest.getHeader("X-Token").split("_")[0]));
        Map<String, Object> info = new HashMap<>();
        info.put("published", articles.stream().filter(Article::getPublish).count());
        info.put("unPublished", articles.stream().filter(a -> !a.getPublish()).count());
        info.put("category", articles.stream().flatMap(article -> ArticleUtil.str2List(article.getCategories()).stream()).collect(Collectors.toSet()));
        info.put("tag", articles.stream().flatMap(article -> ArticleUtil.str2List(article.getTags()).stream()).collect(Collectors.toSet()));
        return info;
    }


}
