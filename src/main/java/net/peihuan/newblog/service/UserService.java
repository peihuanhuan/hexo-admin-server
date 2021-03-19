package net.peihuan.newblog.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.peihuan.newblog.bean.entity.User;
import net.peihuan.newblog.bean.enums.ResultEnum;
import net.peihuan.newblog.exception.BaseException;
import net.peihuan.newblog.mapper.UserMapper;
import net.peihuan.newblog.util.StrUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

@Slf4j
@Service
public class UserService extends ServiceImpl<UserMapper, User> {

    @Getter
    private String token = StrUtils.uuid();

    public String login(String username, String password) {
        User user = getOne(Wrappers.<User>lambdaQuery().eq(User::getUsername, username));
        if (user == null) {
            throw new BaseException(ResultEnum.USER_NOT_FOUND);
        }

        if (!DigestUtils.md5DigestAsHex(password.getBytes()).equals(user.getPassword())) {
            throw new BaseException(ResultEnum.PASSWORD_ERROR);
        }

        token = StrUtils.uuid();
        return token;
    }
}
