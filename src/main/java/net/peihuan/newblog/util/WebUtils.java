package net.peihuan.newblog.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.PrintWriter;

@Slf4j
@UtilityClass
public class WebUtils {

    private final ObjectMapper objectMapper = new ObjectMapper();



    private static final String CURRENT_USER = "current_user";

    public void setUserIdInCurrentRequest(Long userId) {
        HttpServletRequest request = WebUtils.getCurrentRequest();
        request.setAttribute(CURRENT_USER, userId);
    }


    public Long getCurrentUserId() {
        HttpServletRequest request = WebUtils.getCurrentRequest();
        return (Long) request.getAttribute(CURRENT_USER);
    }




    /**
     * 获取当前request
     *
     * @return request
     */
    public HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            throw new IllegalStateException("当前线程中不存在 Request 上下文");
        }
        return attrs.getRequest();
    }


    /**
     * 封装response  统一json返回
     *
     * @param obj      对象
     * @param response 响应
     */
    public void responseWriteJson(Object obj, HttpServletResponse response) {
        printOut(obj, response, HttpStatus.OK);
    }

    /**
     * 封装response  统一json返回
     *
     * @param obj      对象
     * @param response 响应
     */
    public void responseWriteJson(Object obj, HttpServletResponse response, HttpStatus httpStatus) {
        printOut(obj, response, httpStatus);
    }


    /**
     * 输出response
     *
     * @param obj      对象
     * @param response 响应
     */
    private void printOut(Object obj, HttpServletResponse response, HttpStatus httpStatus) {
        response.setCharacterEncoding("UTF-8");
        response.setStatus(httpStatus.value());
        response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        try (PrintWriter printWriter = response.getWriter()) {
            printWriter.write(objectMapper.writeValueAsString(obj));
        } catch (Exception e) {
            log.error("写出响应发生异常", e);
        }
    }

}
