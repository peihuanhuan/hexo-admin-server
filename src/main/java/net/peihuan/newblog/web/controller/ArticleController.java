package net.peihuan.newblog.web.controller;


import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import net.peihuan.newblog.bean.entity.Article;
import net.peihuan.newblog.bean.form.NewArticleForm;
import net.peihuan.newblog.bean.form.UpdateArticleForm;
import net.peihuan.newblog.bean.vo.RestResult;
import net.peihuan.newblog.service.ArticleService;
import net.peihuan.newblog.util.ArticleUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/article")
public class ArticleController {

    @Autowired
    private ArticleService articleService;


    @GetMapping
    public RestResult articles(String title, Integer page, Integer limit) {
        return RestResult.success(articleService.page(title, page, limit));
    }

    @GetMapping("{id}")
    public RestResult article(@PathVariable Long id) {
        return RestResult.success(ArticleUtil.convert2VO(articleService.getById(id), true));
    }

    @DeleteMapping("{id}")
    public RestResult delete(@PathVariable Long id) {
        articleService.deleteArticle(id);
        return RestResult.success();
    }

    @PutMapping("/unpublish/{id}")
    public RestResult unpublish(@PathVariable Long id) {
        articleService.unPublish(id);
        return RestResult.success();
    }

    @PostMapping
    public RestResult newArticle(@RequestBody @Valid NewArticleForm form) {
        Article article = articleService.publishNewArticle(form);
        return RestResult.success(String.valueOf(article.getId()));
    }


    @PutMapping
    @SuppressWarnings("unchecked")
    public RestResult updateArticle(@RequestBody @Valid UpdateArticleForm form) {
        Article article = articleService.updateArticle(form);
        RestResult result = RestResult.success("content", article.getContent());
        ((Map)result.getData()).put("id", String.valueOf(article.getId()));
        return result;
    }
}
