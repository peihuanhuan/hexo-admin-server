package net.peihuan.newblog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.peihuan.newblog.bean.entity.User;
import net.peihuan.newblog.bean.enums.ResultEnum;
import net.peihuan.newblog.exception.BaseException;
import net.peihuan.newblog.mapper.UserMapper;
import net.peihuan.newblog.util.StrUtils;
import org.apache.commons.codec.digest.Md5Crypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.lookup.MapDataSourceLookup;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import sun.security.provider.MD5;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
public class UserService extends ServiceImpl<UserMapper, User> {

    @Getter
    private String token = StrUtils.uuid();

    @Getter
    private Set<String> tokenSet = new HashSet<>();

    @Autowired
    private HttpServletRequest httpServletRequest;

    public String login(String username, String password) {
        User user = getOne(Wrappers.<User>lambdaQuery().eq(User::getUsername, username));
        if (user == null) {
            throw new BaseException(ResultEnum.USER_NOT_FOUND);
        }

        if (!DigestUtils.md5DigestAsHex(password.getBytes()).equals(user.getPassword())) {
            throw new BaseException(ResultEnum.PASSWORD_ERROR);
        }
        String newToken = user.getId() + "_" + StrUtils.uuid();
        tokenSet.add(newToken);
        user.setRole(1);
        return newToken;
    }

    public String loginGuest(String username, String password) {
//        User user = getOne(Wrappers.<User>lambdaQuery().eq(User::getUsername, username));
//        if (user == null) {
//            throw new BaseException(ResultEnum.USER_NOT_FOUND);
//        }
//
//        if (!DigestUtils.md5DigestAsHex(password.getBytes()).equals(user.getPassword())) {
//            throw new BaseException(ResultEnum.PASSWORD_ERROR);
//        }
        User user = new User();
        user.setUsername("游客" + System.currentTimeMillis());
        user.setPassword("");
        user.setRole(2);
        save(user);
        String newToken = user.getId() + "_" + StrUtils.uuid();
        tokenSet.add(newToken);
        return newToken;
    }

    public void loginOut() {
        tokenSet.remove(httpServletRequest.getHeader("X-Token"));
    }

    public String  getRoles() {
        Long userId = Long.parseLong(httpServletRequest.getHeader("X-Token").split("_")[0]);
        User one = getOne(new LambdaQueryWrapper<User>().eq(User::getId, userId)
                .eq(User::getDeleted, 0)
        );
        JsonObject jsonObject = new JsonObject();
        JsonArray roles=new JsonArray();
        roles.add(one.getRole()==1?"admin":"visitor");
        jsonObject.add("roles",roles);
        if(one.getRole()==1){
            jsonObject.addProperty("avatar","https://blogbed.oss-cn-shanghai.aliyuncs.com/static/avatar/dogplane.jpg");
        }
        jsonObject.addProperty("avatar","https://blogbed.oss-cn-shanghai.aliyuncs.com/static/avatar/cat.jpg");
        jsonObject.addProperty("name",one.getRole()==1?"超级管理员":"游客");
        return new Gson().toJson(jsonObject);
    }
}
