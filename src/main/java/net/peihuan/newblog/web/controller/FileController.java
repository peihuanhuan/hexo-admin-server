package net.peihuan.newblog.web.controller;


import net.peihuan.newblog.bean.vo.RestResult;
import net.peihuan.newblog.service.FileService;
import net.peihuan.newblog.service.storage.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/upload")
public class FileController {

    @Autowired
    private FileService fileService;

    @Autowired
    private StorageService storageService;

    @PostMapping()
    public RestResult upload(@RequestParam("title") String title,
                             @RequestParam("file") MultipartFile file) {
        String path = fileService.upload(title, file);
        return RestResult.success(path);
    }

    @GetMapping("/policy")
    public RestResult uploadPolicy() {
        return storageService.getPolicy();
    }
}
