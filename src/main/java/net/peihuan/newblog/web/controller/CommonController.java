package net.peihuan.newblog.web.controller;

import net.peihuan.newblog.bean.vo.RestResult;
import net.peihuan.newblog.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping
@RestController
public class CommonController {

    @Autowired
    private ArticleService articleService;

    @GetMapping("info")
    public RestResult tag() {
        return RestResult.success(articleService.info());
    }

}
