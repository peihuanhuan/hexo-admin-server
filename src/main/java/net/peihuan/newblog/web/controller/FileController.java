package net.peihuan.newblog.web.controller;


import lombok.SneakyThrows;
import net.peihuan.newblog.bean.vo.RestResult;
import net.peihuan.newblog.service.storage.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;

@RestController
@RequestMapping("/upload")
public class FileController {

    @Autowired
    private StorageService storageService;

    @SneakyThrows
    @GetMapping("/policy")
    public RestResult uploadPolicy(@RequestParam("title") String title) {
        title = URLEncoder.encode(title, "utf-8");
        return storageService.getPolicy(title);
    }
}
