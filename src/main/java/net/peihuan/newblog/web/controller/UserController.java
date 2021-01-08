package net.peihuan.newblog.web.controller;

import net.peihuan.newblog.bean.form.LoginForm;
import net.peihuan.newblog.bean.vo.RestResult;
import net.peihuan.newblog.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping
public class UserController {


    @Autowired
    private UserService userService;
    @PostMapping("login")
    public RestResult login(@Valid @RequestBody LoginForm form) {
        return RestResult.success("token", userService.login(form.getUsername(), form.getPassword()));
    }

    @PostMapping("logout")
    public RestResult logout() {
        return RestResult.success();
    }
}


