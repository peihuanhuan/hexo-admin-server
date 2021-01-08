package net.peihuan.newblog.web.filter;

import lombok.extern.slf4j.Slf4j;
import net.peihuan.newblog.bean.enums.ResultEnum;
import net.peihuan.newblog.bean.vo.RestResult;
import net.peihuan.newblog.service.UserService;
import net.peihuan.newblog.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
@WebFilter(urlPatterns = "/*")
public class AuthorizationTokenFilter implements Filter {

    @Autowired
    private UserService userService;

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        String url = request.getRequestURI();

        if (url.equals("/admin/login")) {
            chain.doFilter(request, response);
            return;
        }

        final String authToken = request.getHeader("X-Token");

//        if (authToken == null) {
//            RestResult resultVO = RestResult.error(ResultEnum.UNAUTHORIZED);
//            WebUtils.responseWriteJson(resultVO, response);
//            return;
//        }
//        if (!authToken.equals(userService.getToken())) {
//            RestResult resultVO = RestResult.error(ResultEnum.AUTHORIZED_FAIL);
//            WebUtils.responseWriteJson(resultVO, response);
//            return;
//        }
        chain.doFilter(request, response);
    }
}
