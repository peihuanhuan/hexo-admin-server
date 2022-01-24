package net.peihuan.newblog.web.controller;

import net.peihuan.newblog.bean.vo.RestResult;
import net.peihuan.newblog.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class RoleController {
    @Autowired
    UserService userService;

    @GetMapping("/roles")
    public RestResult getRoles(){
        return RestResult.success(userService.getRoles());
    }
}
